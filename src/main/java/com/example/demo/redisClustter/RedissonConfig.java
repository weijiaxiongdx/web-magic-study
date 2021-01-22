package com.example.demo.redisClustter;

import org.redisson.Redisson;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class RedissonConfig{


    @Value("${wjx.str}")
    private String str;

    @Value("${wjx.xin}")
    private String xin;

    @Value("${wjx.specialStr}")
    private String specialStr;

    @Value("${wjx.specialStr2}")
    private String specialStr2;

    @Value("#{'${spring.redis.cluster.nodes}'.split(',')}")
    private List<String> nodes;

    @Bean
    public Redisson redisson() {

        System.out.println("str: " + str);
        System.out.println("xin: " + xin);
        //redisson版本是3.5，集群的ip前面要加上“redis://”，不然会报错，3.2版本可不加
        List<String> clusterNodes = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i++) {
            clusterNodes.add("redis://" + nodes.get(i));
        }
        Config config = new Config();
        ClusterServersConfig clusterServersConfig = config.useClusterServers()
                .addNodeAddress(clusterNodes.toArray(new String[clusterNodes.size()]));
        return (Redisson) Redisson.create(config);
    }
}
