package com.example.demo;


import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class ProducerConsumer {

    Queue queue = new LinkedList();

    public void test(){
        Producer producer = new Producer(queue,10);
        Consumer consumer = new Consumer(queue,10);
        new Thread(producer).start();
        new Thread(consumer).start();
    }


    class Producer implements Runnable{
        Queue<Integer> queue;
        int capacity;
        int i = 0;

        public Producer(Queue queue,int capacity){
            this.queue = queue;
            this.capacity = capacity;
        }


        @Override
        public void run() {
            while (true) {
                synchronized (queue){
                    while (queue.size() == 10) {
                        System.out.println("队列已满，等待中...");
                        try {
                            queue.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    System.out.println("生产的数据为: " + i);
                    queue.offer(i++);
                    queue.notifyAll();

                    try {
                        Thread.sleep(new Random().nextInt(5000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    class Consumer implements Runnable{
        Queue<Integer> queue;
        int capacity;

        public Consumer(Queue queue,int capacity){
            this.queue = queue;
            this.capacity = capacity;
        }


        @Override
        public void run() {
            while(true){
                synchronized (queue){
                    while (queue.isEmpty()) {
                        System.out.println("队列为空，等待中...");
                        try {
                            queue.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    int r = queue.remove();
                    queue.notifyAll();
                    System.out.println("消费的数据为: " + r);

                    try {
                        Thread.sleep(new Random().nextInt(5000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
