package com.example.demo.redissonSubscribePublish;

import org.redisson.Redisson;
import org.redisson.api.RTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 消费者
 * @author
 */
@Component
public class Subscriber implements ApplicationRunner{

    @Autowired
    private Redisson redisson;

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        System.out.println("容器启动完成了，订阅主题: 9527");
        RTopic rTopic = redisson.getTopic("9527");
        rTopic.addListener(String.class, (CharSequence charSequence, String message)->{
            System.out.println(Thread.currentThread().getName() + "线程<收到>了消息: " + message);
        });
        System.out.println("222222222");
    }
}

