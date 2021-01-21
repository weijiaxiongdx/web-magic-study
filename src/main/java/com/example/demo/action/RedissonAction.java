package com.example.demo.action;

import com.example.demo.redisClustter.DistributedRedisLockUtil;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * redis集群-Redisson锁测试
 * windows redis集群搭建步骤（3主3从）
 * 1.下载并启动6台redis服务器（改配置文件中的端口等信息）
 * 2.Ruby环境安装
 *      2.1双击下载的rubyinstaller-2.2.4-x64.exe安装即可
 *      2.2安装Ruby环境下Redis的驱动,将下载的"Ruby环境下Redis的驱动文件(redis-3.2.2.gem)"拷贝到Ruby安装根目录
 *         然后执行在cmd下执行 gem install --local redis-3.2.2.gem 安装命令
 * 3.将下载的“创建Redis集群的ruby脚本文件redis-trib.rb”文件拷贝到Redis安装根目录(任意一个redis安装目录,例子为master-7000目录)
 * 4.cmd下执行以下命令创建redis集群(master-7000目录下)
 *   redis-trib.rb create --replicas 1 127.0.0.1:7000 127.0.0.1:7001 127.0.0.1:7002 127.0.0.1:7003 127.0.0.1:7004 127.0.0.1:7005
 * 5.cmd下执行以下命令检测集群是否创建成功(master-7000目录下)
 *   redis-trib.rb check 127.0.0.1:7000命令
 * 6.cmd下执行以下命令，启动客户端(master-7000目录下)
 *   -c是必须要的，表示以集群方式启动，如果没加，则执行set等命令时会报错
 *   redis-cli.exe -c -h 127.0.0.1 -p 7000
 *
 */
@RestController
@RequestMapping(value = "/redisson")
public class RedissonAction {

    @Autowired
    private Redisson redisson;

    @Autowired
    private DistributedRedisLockUtil distributedRedisLockUtil;

    @PostMapping(value = "lock")
    public void redissonTest(){
        RLock rLock = redisson.getLock("wjx_lock");
        System.out.println("开始获得锁");
        rLock.lock();
        System.out.println(Thread.currentThread().getName() + "线程获得了锁");
        for (int j = 0; j < 500; j++) {
            System.out.println(Thread.currentThread().getName() + "线程输出: "+ j);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        rLock.unlock();
        System.out.println(Thread.currentThread().getName() + "线程释放了锁");

    }

    @PostMapping(value = "lock2")
    public void redissonTest2(){
        System.out.println("开始获得锁");
        DistributedRedisLockUtil.acquire("wjx_lock");
        System.out.println(Thread.currentThread().getName() + "线程获得了锁");
        for (int j = 0; j < 500; j++) {
            System.out.println(Thread.currentThread().getName() + "线程输出: "+ j);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        DistributedRedisLockUtil.release("wjx_lock");
        System.out.println(Thread.currentThread().getName() + "线程释放了锁");
    }
}
