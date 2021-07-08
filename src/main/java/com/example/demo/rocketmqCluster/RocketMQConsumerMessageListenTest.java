package com.example.demo.rocketmqCluster;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(topic = "wjx-topic",consumerGroup = "rocketmq-springboot")
public class RocketMQConsumerMessageListenTest implements RocketMQListener<String> {

    @Override
    public void onMessage(String s) {
        System.out.println("接收到消息：" + s);
    }
}
