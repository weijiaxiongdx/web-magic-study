package com.example.demo.rocketmqCluster;

import com.alibaba.fastjson.JSON;
import com.example.demo.rocketmq.User;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

/**
 * 发送同步消息：生产者发送消息后会阻塞，等待mq返回，可靠性高，一般用于发送重要通知消息，比如发送短信
 * @author
 */
public class SendSyncMessage {
    public static void main(String[] args) throws MQClientException {
        DefaultMQProducer producer = new DefaultMQProducer("wjx-group");
        producer.setNamesrvAddr("192.168.1.88:9876;192.168.1.248:9876");
        producer.start();
        try {
            for (int i=0;i<10;i++){
                User user = new User();
                user.setId(i);
                user.setName("张"+String.valueOf(i));
                Message message = new Message("wjx-topic", "user-tag", JSON.toJSONString(user).getBytes());
                System.out.println("生产者发送消息:"+JSON.toJSONString(user));
                SendResult result = producer.send(message);
                System.out.println("返回信息: " + result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        producer.shutdown();
    }
}
