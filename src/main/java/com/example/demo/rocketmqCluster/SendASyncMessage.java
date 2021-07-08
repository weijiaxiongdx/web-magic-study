package com.example.demo.rocketmqCluster;

import com.alibaba.fastjson.JSON;
import com.example.demo.rocketmq.User;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

/**
 * 发送异步消息：发送消息后不阻塞等mq返回，可用于响应时间敏感场景
 * @author
 */
public class SendASyncMessage {
    public static void main(String[] args) throws MQClientException {
        DefaultMQProducer producer = new DefaultMQProducer("wjx-group");
        producer.setNamesrvAddr("192.168.1.88:9876;192.168.1.248:9876");
        producer.start();
        try {
            for (int i=0;i<10;i++){
                User user = new User();
                user.setId(i);
                user.setName("张"+String.valueOf(i));
                Message message = new Message("wjx-topic", "user-tag3", JSON.toJSONString(user).getBytes());
                System.out.println("生产者发送消息:"+JSON.toJSONString(user));

                producer.send(message, new SendCallback() {
                    @Override
                    public void onSuccess(SendResult sendResult) {
                        System.out.println("发送成功回调： " + sendResult);
                    }

                    @Override
                    public void onException(Throwable throwable) {
                        System.out.println("发送错误回调： " + throwable.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
