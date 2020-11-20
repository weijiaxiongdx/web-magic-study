package com.example.demo.rocketmq;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;

public class Consumer {

    public static void main(String[] args) throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("test-group");
        consumer.setNamesrvAddr("192.168.1.81:9876;192.168.1.88:9876");
        consumer.setInstanceName("rmq-instance");
        consumer.subscribe("wjx-topic", "user-tag");
        consumer.registerMessageListener((List<MessageExt> msgList, ConsumeConcurrentlyContext context)->{
            for (MessageExt msg : msgList) {
                System.out.println("消费者消费数据: " + new String(msg.getBody()));
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        consumer.start();
    }
}
