package com.example.demo.rocketmq;

import com.alibaba.fastjson.JSON;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;

/**
 * 生产者，发送普通消息，验证通过
 */
public class Producer {

    public static void main(String[] args) throws MQClientException {
        DefaultMQProducer producer = new DefaultMQProducer("test-group");
        // 192.168.1.81本机 windows服务器;192.168.1.88开发环境linux服务器
        producer.setNamesrvAddr("192.168.1.81:9876;192.168.1.88:9876");
        producer.setInstanceName("rmq-instance");
        producer.start();
        try {
            for (int i=0;i<100;i++){
                User user = new User();
                user.setId(i);
                user.setName("张"+String.valueOf(i));
                Message message = new Message("wjx-topic", "user-tag", JSON.toJSONString(user).getBytes());
                System.out.println("生产者发送消息:"+JSON.toJSONString(user));
                producer.send(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        producer.shutdown();
    }
}
