package com.example.demo.redisClustter;

import com.example.demo.ApplicationContextUtil;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author wjx
 */
@Component
public class DistributedRedisLockUtil {

    private static final String LOCK_TITLE = "redisLock_";

    public static boolean acquire(String lockName){
        Redisson redisson = (Redisson)ApplicationContextUtil.getApplicationContext().getBean("redisson");
        String key = LOCK_TITLE + lockName;
        RLock mylock = redisson.getLock(key);
        mylock.lock(2, TimeUnit.MINUTES);
        System.out.println("======lock======"+Thread.currentThread().getName());
        return  true;
    }

    public static void release(String lockName){
        Redisson redisson = (Redisson)ApplicationContextUtil.getApplicationContext().getBean("redisson");
        String key = LOCK_TITLE + lockName;
        RLock mylock = redisson.getLock(key);
        mylock.unlock();
        System.out.println("======unlock======"+Thread.currentThread().getName());
    }
}
