package com.example.demo.redissonSubscribePublish;

import org.redisson.Redisson;
import org.redisson.api.RTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 生产者
 * @author
 */
@Component
@RestController
@RequestMapping(value = "/redisson")
public class Publisher {

    @Autowired
    private Redisson redisson;

    @PostMapping(value = "publish")
    public void publishMessage(String message){
        RTopic rTopic = redisson.getTopic("9527");
        for (int i = 0; i < 10000; i++) {
            rTopic.publish(i);
            System.out.println(Thread.currentThread().getName() + "线程<发送>了消息: " + i);
        }
    }
}
