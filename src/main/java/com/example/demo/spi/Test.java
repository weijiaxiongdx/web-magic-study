package com.example.demo.spi;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * SPI（Service Provider Interface）是JDK内置的服务发现机制，用在不同模块间通过接口调用服务
 *     1.服务调用方通过ServiceLoader.load加载服务接口的实现类实例
 *     2.服务提供方实现服务接口后，在自己Jar包的META-INF/services目录(项目的resources目录)下新建一个接口名全名的文件，并将具体实现类全名写入
 */
public class Test {
    public static void main(String[] args) {

        ServiceLoader<SPITestService> services = ServiceLoader.load(SPITestService.class);
        Iterator<SPITestService> iterator = services.iterator();
        while (iterator.hasNext()){
            iterator.next().speak();
        }
    }
}
