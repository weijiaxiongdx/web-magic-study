package com.example.demo.concurrentlist;


import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class ConcurrentListTest {

    CopyOnWriteArrayList copyOnWriteArrayList = new CopyOnWriteArrayList<>();
    LinkedBlockingQueue linkedBlockingQueue = new LinkedBlockingQueue<>();

    /**
     *  CopyOnWriteArrayList用于读多写少的场景
     *  优点： 读不加锁、写加锁，读写可并发执行
     *  缺点： 占用内存(内存大时可难会引起频繁GC)、无法保证实时性(只能保证最终一致性)
     *
     *  CopyOnWriteArraySet内部实现完全依赖于CopyOnWriteArrayList，特性完全一致
     */
    public void test(){

        new Thread(()->{
            for (int i = 0; i < 100; i++) {
                copyOnWriteArrayList.add(i);
                System.out.println("线程t4写" + i);
            }
        },"t4").start();

        new Thread(()->{
            copyOnWriteArrayList.forEach(item-> System.out.println("线程t1输出: "+item));
        },"t1").start();


        new Thread(()->{
            copyOnWriteArrayList.forEach(item-> System.out.println("线程t2输出: "+item));
        },"t2").start();


        new Thread(()->{
            copyOnWriteArrayList.forEach(item-> System.out.println("线程t3输出: "+item));
        },"t3").start();
    }


    /**
     * LinkedBlockingQueue主要功能： 简化多线程间数据共享(典型场景为生产者-消费者)
     * ConcurrentLinkedQueue 高性能队列
     */
    public void test2(){
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        ExecutorService executorService2 = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10; i++) {
            executorService.submit(()->{
                for (int j = 0; j < 100; j++) {
                    try {
                        linkedBlockingQueue.put(Thread.currentThread().getName() + "写字符串" + j);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });

            executorService2.submit(()->{
                for (int j = 0; j < 100; j++) {
                    try {
                        System.out.println(Thread.currentThread().getName() + "读字符串: " +  linkedBlockingQueue.take());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
