package com.example.demo.action;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * canal+mysql+redis同步增量数据（全都是本地环境，redis启的是本地搭建的集群，canal可结合zk实现HA）
 * canal工作原理：
 *     canal模拟mysql slave的交互协议，伪装自己为mysql slave，向mysql master发送dump协议
 *     mysql master收到dump请求，开始推送binary log给slave(也就是canal)
 *     canal解析binary log对象(原始为byte流)
 *
 * 1.Canal是通过数据库binlog日志来进行同步的，所以要确保mysql开启了binlog日志,配置后重启mysql。
 *  my.ini配置文件中[mysqld]部分配置以下信息
 *      log-bin=mysql-bin
 *      binlog-format=ROW
 *      server_id=987654321
 *
 * 2.创建用于同步数据的mysql账户并授权
 *   create user wjx identified by 'wjx';
 *   GRANT SELECT, REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'wjx'@'%';
 *   flush privileges;
 *
 * 3.mysql8中创建用户默认加密方式为caching_sha2_password，访问CanalAction中的test接口中会报如下错误(wjx为数据库用户名)
 *   wjx caching_sha2_password Auth failed
 *
 *   此时可先通过以下语句查看验证方式(user为系统表)
 *   select host,user authentication_string,plugin from user;
 *
 *   输出内容如下，由以下输出内容可知，wjx用户的验证方式为caching_sha2_password
 *   +-----------+-----------------------+-----------------------+
 *   | host      | authentication_string | plugin                |
 *   +-----------+-----------------------+-----------------------+
 *   | %         | wjx                   | caching_sha2_password |
 *   | localhost | mysql.infoschema      | caching_sha2_password |
 *   | localhost | mysql.session         | caching_sha2_password |
 *   | localhost | mysql.sys             | caching_sha2_password |
 *   | localhost | root                  | mysql_native_password |
 *   +-----------+-----------------------+-----------------------+
 *
 *   报错解决方案，执行以下语句修改密码验证方式为mysql_native_password（@符号后面为上表中的host字段值）
 *   ALTER USER 'wjx'@'%' IDENTIFIED WITH mysql_native_password BY 'wjx';
 *
 * 4.下载canal.deployer-1.1.5服务，本机目录为C:\Users\Administrator\Desktop\canal\canal.deployer-1.1.5（版本过低，会报客户端版本错误，并提示upgrade）
 * 5.配置instance.properties文件，设置用户名和密码（第2步中创建的用户名和密码）
 *   canal.instance.dbUsername=wjx
 *   canal.instance.dbPassword=wjx
 *
 * 6.双击startup.bat启动canal服务
 * 7.启动web服务并调用/canal/test接口
 * 8.手动修改数据库中的数据
 * 9.在redis集群对应节点中查看数据
 */
@RestController
@RequestMapping(value = "/canal")
public class CanalAction {

    @Autowired
    private Redisson redisson;

    @GetMapping(value = "/test")
    public void test(){

        /**
         * 创建链接,127.0.0.1是ip，11111是canal中conf/canal.properties文件里配置的端口号
         * ，example是canal虚拟的模块名，在canal.properties文件canal.destinations= example 这段可以自行修改。wjx是创建的数据库账号和密码
         */
        CanalConnector connector = CanalConnectors.newSingleConnector(new InetSocketAddress("127.0.0.1",11111), "example", "wjx", "wjx");
        int batchSize = 1000;
        int emptyCount = 0;
        try {
            connector.connect();
            connector.subscribe(".*\\..*");
            connector.rollback();
            int totalEmtryCount = 1200;
            while (emptyCount < totalEmtryCount) {
                Message message = connector.getWithoutAck(batchSize);
                long batchId = message.getId();
                int size = message.getEntries().size();
                if (batchId == -1 || size == 0) {
                    emptyCount++;
                    System.out.println("empty count : " + emptyCount);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    emptyCount = 0;
                    printEntry(message.getEntries());
                }

                connector.ack(batchId);
                // connector.rollback(batchId); // 处理失败, 回滚数据
            }

            System.out.println("empty too many times, exit");
        } finally {
            connector.disconnect();
        }
   }

    private void printEntry(List<CanalEntry.Entry> entrys) {
        for (CanalEntry.Entry entry : entrys) {
            if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONBEGIN || entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND) {
                continue;
            }

            CanalEntry.RowChange rowChage = null;
            try {
                rowChage = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
            } catch (Exception e) {
                throw new RuntimeException("ERROR ## parser of eromanga-event has an error , data:" + entry.toString(), e);
            }

            CanalEntry.EventType eventType = rowChage.getEventType();
            System.out.println(String.format("================> binlog[%s:%s] , name[%s,%s] , eventType : %s",
                    entry.getHeader().getLogfileName(), entry.getHeader().getLogfileOffset(),
                    entry.getHeader().getSchemaName(), entry.getHeader().getTableName(),
                    eventType));

            for (CanalEntry.RowData rowData : rowChage.getRowDatasList()) {
                if (eventType == CanalEntry.EventType.DELETE) {
                    printColumn(rowData.getBeforeColumnsList());
                    redisDelete(rowData.getBeforeColumnsList(), entry.getHeader().getTableName());
                } else if (eventType == CanalEntry.EventType.INSERT) {
                    printColumn(rowData.getAfterColumnsList());
                    redisInsert(rowData.getAfterColumnsList(), entry.getHeader().getTableName());
                } else {
                    System.out.println("-------> before");
                    printColumn(rowData.getBeforeColumnsList());
                    System.out.println("-------> after");
                    printColumn(rowData.getAfterColumnsList());
                    redisUpdate(rowData.getAfterColumnsList(), entry.getHeader().getTableName());
                }
            }
        }
    }

    private static void printColumn(List<CanalEntry.Column> columns) {
        for (CanalEntry.Column column : columns) {
            System.out.println(column.getName() + " : " + column.getValue() + "    update=" + column.getUpdated());
        }
    }

    private void redisInsert(List<CanalEntry.Column> columns, String tablename) {
        JSONObject json = new JSONObject();
        for (CanalEntry.Column column : columns) {
            json.put(column.getName(), column.getValue());
        }
        if (columns.size() > 0) {
            RBucket<String> keyObj = redisson.getBucket(tablename + columns.get(0).getValue());
            keyObj.set(json.toJSONString());
        }
    }

    private void redisUpdate(List<CanalEntry.Column> columns, String tablename) {
        JSONObject json = new JSONObject();
        for (CanalEntry.Column column : columns) {
            json.put(column.getName(), column.getValue());
        }
        if (columns.size() > 0) {
            RBucket<String> keyObj = redisson.getBucket(tablename + columns.get(0).getValue());
            keyObj.set(json.toJSONString());
        }
    }

    private void redisDelete(List<CanalEntry.Column> columns, String tablename) {
        JSONObject json = new JSONObject();
        for (CanalEntry.Column column : columns) {
            json.put(column.getName(), column.getValue());
        }
        if (columns.size() > 0) {
            RBucket<String> keyObj = redisson.getBucket(tablename + columns.get(0).getValue());
            keyObj.delete();
        }
    }
}
