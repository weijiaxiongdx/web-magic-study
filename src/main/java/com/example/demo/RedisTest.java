package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/redis")
public class RedisTest {

    @Autowired
    StringRedisTemplate redisTemplate;

    @GetMapping(value = "/test")
    public void test(){
        redisTemplate.opsForValue().set("zhang","san");
        System.out.println("成功");
    }


    // redis必须在处理完所有命令前先缓存起所有命令的处理结果。打包的命令越多，缓存消耗内存也越多。所以并不是打包的命令越多越好
    @GetMapping(value = "/test2")
    public void test2(){
        long startTime = System.currentTimeMillis();
        List<Object> resultList = redisTemplate.executePipelined((RedisCallback<Object>) redisConnection -> {
            //redisConnection.openPipeline(); //可以调用，也可以不调用
            for (int i = 0; i < 100000; ++i) {
                redisConnection.set(("E" + i).getBytes(), (i + "").getBytes());
            }
            //redisConnection.closePipeline();//不能调用，调用了拿不到返回值
            return null;
        });

        long endTime = System.currentTimeMillis();
        System.out.println("管道耗时: " + (endTime - startTime)); //1000个命令--373ms 100000个命令--1348ms、2181ms、2267ms、2605ms
        for (Object obj : resultList){
            System.out.println("结果： " + obj);

        }
    }


    @GetMapping(value = "/test3")
    public void test3(){
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            redisTemplate.opsForValue().set("F"+i,i+"");
        }
        long endTime = System.currentTimeMillis();
        System.out.println("耗时: " + (endTime - startTime)); //1000个命令--465ms 100000个命令--8575ms、8433ms、8021ms
    }
}
