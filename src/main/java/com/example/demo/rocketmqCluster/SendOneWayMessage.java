package com.example.demo.rocketmqCluster;

import com.alibaba.fastjson.JSON;
import com.example.demo.rocketmq.User;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;

/**
 * 发送单向消息：用于不需要知道发送结果的场景
 * @author
 */
public class SendOneWayMessage {
    public static void main(String[] args) throws MQClientException {
        DefaultMQProducer producer = new DefaultMQProducer("wjx-group");
        producer.setNamesrvAddr("192.168.1.88:9876;192.168.1.248:9876");
        producer.start();
        try {
            for (int i=0;i<10;i++){
                User user = new User();
                user.setId(i);
                user.setName("张"+String.valueOf(i));
                Message message = new Message("wjx-topic", "user-tag5-one-way", JSON.toJSONString(user).getBytes());
                System.out.println("生产者发送消息:"+JSON.toJSONString(user));
                producer.sendOneway(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        producer.shutdown();
    }
}
