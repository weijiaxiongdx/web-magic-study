package com.example.demo;


import org.openjdk.jol.info.ClassLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.LongStream;

public class ObjectLayoutTest {

    protected static Logger logger = LoggerFactory.getLogger(ObjectLayoutTest.class);
    Object lock = new Object();

    Object aLock = new Object();
    Object bLock = new Object();

    int i = 0;
    int a = 0;
    int c = 10;
    AtomicInteger atomicInteger = new AtomicInteger(0);
    AtomicInteger atomicInteger2 = new AtomicInteger(100);
    LongAdder longAdder = new LongAdder();
    LongAdder longAdder2 = new LongAdder();
    int threadCount = 200;
    int lockCount = 200;
    long totalNum = 1000;
    Boolean flag = true;
    Random random = new Random();
    private int ticket = 100;

    Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread th, Throwable ex) {
            System.out.println("线程名称: " + th.getName());
            System.out.println(ex);
        }
    };


    // java中运行js程序
    public void test30(){
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName( "JavaScript" );
        try {
            System.out.println("Result: " + engine.eval( "function f() {return 1;}; f() + 1;" ));
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }


    // jdbc 批量插入百万级数据
    public void test29(){
        Connection conn = null;
        PreparedStatement stmt = null;
        String url = "jdbc:mysql://localhost:3306/mall_v2.8.1?autoReconnect=true&useUnicode=true&characterEncoding=utf-8&serverTimezone=UTC";
        String sql = "insert into wjx_test(sal,name,full_name,sex) values (?,?,?,?)";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(url, "root", "123456");
            stmt = conn.prepareStatement(sql);
            conn.setAutoCommit(false);
            long startTime = System.currentTimeMillis();
            for (int i = 1; i <= 2000000; i++) {
                stmt.setFloat(1, 666.6F);
                stmt.setString(2, "狄仁杰" + (10000000+i));
                stmt.setString(3, "fullname狄仁杰" + i);
                stmt.setString(4, "男");
                stmt.addBatch();
                if(i % 10000==0){
                    stmt.executeBatch();
                    conn.commit();
                }
            }
            long endTime = System.currentTimeMillis();
            System.out.println("耗时: " + (endTime-startTime)); //1百万数据每5000提交一次,插入完总耗时: 115.468秒、113.082秒、113.641秒、110.710秒、111.238秒
                                                               //2百万数据每10000提交一次,插入完总耗时: 227.454秒、229.535秒、226.161秒
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                stmt.close();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // ReentrantReadWriteLock
    public void test28(){
        ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
        ReentrantReadWriteLock.ReadLock readLock = reentrantReadWriteLock.readLock();
        ReentrantReadWriteLock.WriteLock writeLock = reentrantReadWriteLock.writeLock();

        logger.info("主线程开始");
        new Thread(()->{
            logger.info("开始获取读锁");
            readLock.lock();
            for (int j = 0; j < 10 ; j++) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("t2线程: " + j);
            }
            readLock.unlock();
            logger.info("读锁释放了");
        },"t2").start();


        new Thread(()->{
//            readLock.lock();
            logger.info("开始获取写锁");
            writeLock.lock();
            for (int j = 0; j < 10 ; j++) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("t3线程: " + j);
            }
//            readLock.unlock();
            writeLock.unlock();
        },"t3").start();
    }

    // ReentrantReadWriteLock
    public void test27(){
        ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
        ReentrantReadWriteLock.WriteLock writeLock = reentrantReadWriteLock.writeLock();
        ReentrantReadWriteLock.ReadLock readLock = reentrantReadWriteLock.readLock();
        writeLock.lock();
        logger.info("get write lock");

        readLock.lock(); //从写锁降级成读锁，并不会自动释放当前线程获取的写锁，仍然需要显示的释放，否则别的线程永远也获取不到写锁
        logger.info("get read lock");


        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        new Thread(()->{
            logger.info("另外一个线程开始获取写锁");
            writeLock.lock(); //会阻塞(最终调用的是unsafe的park方法),因为main线程还没释放写锁
            logger.info("另外一个线程获取写锁成功");
        },"t2").start();
    }

    // ReentrantReadWriteLock
    public void test26(){
        ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
        reentrantReadWriteLock.readLock().lock();
        logger.info("get read lock");

        //reentrantReadWriteLock.readLock().unlock();//不加这一行会在获取写锁时产生阻塞，因为在没有释放读锁的情况下，就去申请写锁，这属于锁升级，ReentrantReadWriteLock是不支持锁升级的。

        reentrantReadWriteLock.writeLock().lock();
        logger.info("get write lock");
    }

    public void test25(){
       /* ConcurrentSkipListSet concurrentSkipListSet = new ConcurrentSkipListSet<String>();
        concurrentSkipListSet.add("n");
        concurrentSkipListSet.add("h");
        concurrentSkipListSet.add("a");
        concurrentSkipListSet.add("c");
        concurrentSkipListSet.add("q");
        concurrentSkipListSet.add("a");
        concurrentSkipListSet.add("a");
        concurrentSkipListSet.add("b");
        System.out.println(concurrentSkipListSet);

        outF: for (int j = 0; j < concurrentSkipListSet.size(); j++) {
            System.out.println("j: "+j);
            innF: for (int k = 0; k < concurrentSkipListSet.size(); k++) {
                System.out.println("k: "+k);
                if (k == 2){
                    break outF;
                }
            }
        }*/

        HashMap hashMap = new HashMap(16);
        hashMap.put("1","a");
        hashMap.put("2","b");
        hashMap.put("1","c");
        hashMap.put(null,null);
        System.out.println(hashMap);

        int i = -5;
        System.out.println(Integer.toBinaryString(i));
        System.out.println(Integer.toBinaryString(i >> 2));
        System.out.println(Integer.toBinaryString(i >>> 2));

        int j = 5;
        System.out.println(Integer.toBinaryString(j));
        System.out.println(Integer.toBinaryString(j >> 2));
        System.out.println(Integer.toBinaryString(j >>> 2));

        System.out.println(0^1);
        System.out.println(0^0);

        System.out.println(hashMap.hashCode());
        System.out.println(Integer.toBinaryString(hashMap.hashCode()));
    }

    // ExecutorService 多线程求数组元素之和
    public void test24(){
        List<Future<Long>> resultList = new ArrayList<>();
        long[] longArr = LongStream.rangeClosed(1,100).toArray();
        int coreNum = Runtime.getRuntime().availableProcessors();
        logger.info("核心数为: " + coreNum);
        ExecutorService executorService = Executors.newFixedThreadPool(coreNum);
        int part = longArr.length/coreNum;
        for (int j = 0; j < coreNum; j++) {
            int from = j * part;
            int to = (j == coreNum - 1) ? longArr.length - 1 : (j + 1) * part - 1;
            logger.info("开始索引: {}, 结束索引: {}",from,to);
            Future<Long> future = executorService.submit(new ArrSumTask(longArr,from,to));
            resultList.add(future);
        }

        long total = 0L;
        int i = 0;
        for (Future<Long> f : resultList) {
            try {
                total += f.get();
//                logger.info("获取了第 {} 个结果,{}",++i,total);
            } catch (Exception e) {
                logger.error("获取结果异常, {}",e.getMessage());
            }
        }

        System.out.println("总和为: " + total);
    }

    //FutureTask
    public void test23(){
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, 20,10 ,
                TimeUnit.SECONDS, new LinkedBlockingDeque<>(500));
        Task task = new Task();
        FutureTask<Integer> futureTask = new FutureTask<>(task);
        threadPoolExecutor.submit(futureTask);

        try {
             if(futureTask.isDone()){
                 logger.info("任务完成了");
             } else {
                 logger.info("任务还没完成");
             }

            logger.info("开始获取结果");
            int sum = futureTask.get();
            logger.info("结果: " + sum);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    // Bubble Sort
    public void test22(int[] arr){
        for (int j = 0; j < arr.length - 1; j++) {
            for (int k = 0; k < arr.length-1-j ; k++) {
                if(arr[k]>arr[k+1]){
                    int temp = arr[k];
                    arr[k] = arr[k+1];
                    arr[k+1] = temp;
                }
            }
        }
    }

    // Quick sort
    public void test21(int[] arr,int left,int right){

        if(left > right){
            return;
        }

        int base = arr[left];
        int i = left;
        int j = right;

        while (i < j){
            while (arr[j] >= base && i < j){
                j--;
            }

            while (arr[i] <= base && i < j){
                i++;
            }

            if (i < j){
                int temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }

        arr[left] = arr[i];
        arr[i] = base;

        test21(arr,left,i-1);
        test21(arr,i+1,right);
    }

    //interrupt
    public void test20(){
        Thread t1 = new Thread(()->{
            while(true){
                if(Thread.currentThread().isInterrupted()){
                    logger.info("t1线程被中断了...");
                    break;
                }

                try {
                    Thread.sleep(random.nextInt(100));
                    logger.info("t1线程正常执行业务中...");
                } catch (InterruptedException e) {
                    logger.info("t1线程阻塞时被中断了...");
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
            }
        },"t1");
        t1.start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        t1.interrupt();
    }

    //Exchanger
    public void test19(){
        Exchanger<String> exchanger = new Exchanger();
        Random random = new Random();
        new Thread(()->{
            String t1 = Thread.currentThread().getName() + "线程的数据111";
            try {
                Thread.sleep(random.nextInt(100));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                logger.info(Thread.currentThread().getName() + "线程交换前的数据: " + t1);
                String t2 = exchanger.exchange(t1);
                logger.info(Thread.currentThread().getName() + "线程交换后的数据: " + t2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();


        new Thread(){
            @Override
            public void run() {
                String t2 = Thread.currentThread().getName() + "线程的数据222";
                try {
                    Thread.sleep(random.nextInt(100));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                try {
                    logger.info(Thread.currentThread().getName() + "线程交换前的数据: " + t2);
                    String t1 = exchanger.exchange(t2);
                    logger.info(Thread.currentThread().getName() + "线程交换后的数据: " + t1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    //Future
    public void test18(){
        ExecutorService executorService = Executors.newCachedThreadPool();
        Random random = new Random();
        int total = 0;
        /*for (int j = 1; j <= 10; j++) {
            int k = j;
            Future<Integer> future = executorService.submit(()->{
                try {
                    Thread.sleep(random.nextInt(100));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return k;
            });

            try {
               total = total + future.get(); //相当于串行执行
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        System.out.println("总和: " + total);*/

        Future<Integer> future1 = executorService.submit(()->{
            try {
                //Thread.sleep(random.nextInt(100));
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 100;
        });

        Future<Integer> future2 = executorService.submit(()->{
            try {
                //Thread.sleep(random.nextInt(100));
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 200;
        });

        Future<Integer> future3 = executorService.submit(()->{
            try {
                //Thread.sleep(random.nextInt(100));
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 300;
        });

        try {
            logger.info("主线程开始获取结果");
//            int result1 = future1.get();
            int result1 = 0;

            try {
                result1 = future1.get(2,TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                logger.info("等了两秒没有获取到结果");
                e.printStackTrace();
            }

            logger.info("result1: " + result1);

            if(future2.isDone()){
                int result2 = future2.get();
                logger.info("result2: " + result2);
            }

            int result3 = future3.get();
            logger.info("result3: " + result3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    //CyclicBarrier
    public void test17(){
        ExecutorService executorService = Executors.newCachedThreadPool();
        CyclicBarrier cyclicBarrier = new CyclicBarrier(3, ()->{
            logger.info("汇总完成");
        });

        for (int j = 0; j < 3; j++) {
            executorService.submit(()->{
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                logger.info(Thread.currentThread().getName() + "线程执行中...");

                try {
                    cyclicBarrier.await();
                    logger.info(Thread.currentThread().getName() + "线程执行完成");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
            });
        }


        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int j = 0; j < 5; j++) {
            executorService.submit(()->{
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                logger.info(Thread.currentThread().getName() + "线程执行中...");

                try {
                    cyclicBarrier.await();
                    logger.info(Thread.currentThread().getName() + "线程执行完成");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    //CountDownLatch
    public void test16(){
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch countDownLatch = new CountDownLatch(10);
        String[] all = new String[10];
        Random random = new Random();
        for (int j = 0; j < 10; j++) {
            int z = j;
            executorService.submit(()->{
                for (int k = 1; k <= 100; k++) {
                    try {
                        Thread.sleep(random.nextInt(100));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    all[z] = k + "%";
                    System.out.print("\r" + Arrays.toString(all));
                }

                countDownLatch.countDown();
            });
        }

        try {
            countDownLatch.await();
            System.out.println("匹配成功,进入游戏");
            executorService.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //CountDownLatch
    public void test15(){
        long startTime = System.currentTimeMillis();
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch countDownLatch = new CountDownLatch(lockCount);
        CountDownLatch countDownLatch2 = new CountDownLatch(lockCount);
        for (int j = 0; j < lockCount; j++) {
            int k = j;
            executorService.submit(()->{
                logger.info("第 " + k + "个任务执行中...");
                try {
                    countDownLatch.await();

                    for (int y = 0; y < 100000; y++) {
                        longAdder2.increment();
                    }
                    countDownLatch2.countDown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            countDownLatch.countDown();
        }

        try {
            countDownLatch2.await();
            long endTime = System.currentTimeMillis();
            logger.info("最终结果: {},耗时: {}", longAdder2.longValue(),endTime-startTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //CountDownLatch
    public void test14(){
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch countDownLatch = new CountDownLatch(10);

        for (int j = 0; j < 10; j++) {
            Runnable runnable = new Runnable(){
                @Override
                public void run(){
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    logger.info(Thread.currentThread().getName() + "线程任务执行了");
                    countDownLatch.countDown();
                }
            };

            executorService.submit(runnable);
        }
        executorService.shutdown();

        try {
            logger.info("主线程等待中....");
            countDownLatch.await();
            logger.info("主线程结束等待");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //Semaphore
    public void test13(){
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        Semaphore semaphore = new Semaphore(5);;
        for (int j = 0; j < 10; j++) {
           Runnable runnable = new Runnable(){
               @Override
               public void run() {
                   try {
                       semaphore.acquire();
                   } catch (InterruptedException e) {
                       e.printStackTrace();
                   }

                   logger.info(Thread.currentThread().getName() + "线程已获得许可，当前还剩下 " + semaphore.availablePermits());

                   try {
                       Thread.sleep(3000);
                       logger.info(Thread.currentThread().getName() + "线程处理业务中...");
                   } catch (InterruptedException e) {
                       e.printStackTrace();
                   }

                   semaphore.release();
                   logger.info(Thread.currentThread().getName() + "已释放了锁");
               }
           };

           executorService.execute(runnable);
        }
    }

    public void test12(){
        new Thread(()->{
            while (c > 0){
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                c--;
                System.out.println(Thread.currentThread().getName() + "线程减1之后的值为: " + c);
            }
        },"t1").start();

        new Thread(()->{
            while (c < 20){
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

               c++;
               System.out.println(Thread.currentThread().getName() + "线程加1之后的值为: " + c);
            }
        },"t2").start();
    }

    //Synchronized
    public void test11(){
        Thread t1 = new Thread(()->{
            synchronized (aLock){
                System.out.println(Thread.currentThread().getName() + "线程获取a锁");

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                synchronized (bLock){
                    System.out.println(Thread.currentThread().getName() + "线程获取b锁");
                }
            }
        },"t1");
        t1.start();

        Thread t2 = new Thread(()->{
            synchronized (bLock){
                System.out.println(Thread.currentThread().getName() + "线程获取b锁");
                synchronized (aLock){
                    System.out.println(Thread.currentThread().getName() + "线程获取a锁");
                }
            }
        },"t2");
        t2.start();
    }

    //LongAdder
    public void test10(){
        Thread t1 = new Thread(()->{
            System.out.println(Thread.currentThread().getName() + "开始执行");
            for (int j = 0; j < 100000; j++) {
                longAdder.increment();
            }
        },"t1");
        t1.start();

        Thread t2 = new Thread(()->{
            System.out.println(Thread.currentThread().getName() + "开始执行");
            for (int m = 0; m < 100000; m++) {
                longAdder.increment();
            }
        },"t2");
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("总和为: " + longAdder.longValue());
        System.out.println("总和为: " + longAdder.sum());
    }

    //AtomicInteger
    public void test9(){
        Thread t1 = new Thread(()->{
            System.out.println(Thread.currentThread().getName() + "开始执行");
            for (int j = 0; j < 100000; j++) {
                atomicInteger.incrementAndGet();
            }
        },"t1");
        t1.start();

        Thread t2 = new Thread(()->{
            System.out.println(Thread.currentThread().getName() + "开始执行");
            for (int m = 0; m < 100000; m++) {
                atomicInteger.incrementAndGet();
            }
        },"t2");
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("总和为: " + atomicInteger.get());
    }

    //Synchronized
    public void test8(){
       Thread t1 = new Thread(()->{
           System.out.println(Thread.currentThread().getName() + "开始执行");
           synchronized (lock){
               System.out.println(Thread.currentThread().getName() + "获得锁");
               for (int j = 0; j < 100000; j++) {
                   i++;
               }
           }
        },"t1");
       t1.start();

       Thread t2 = new Thread(()->{
           System.out.println(Thread.currentThread().getName() + "开始执行");
           synchronized (lock){
               System.out.println(Thread.currentThread().getName() + "获得锁");
               for (int m = 0; m < 100000; m++) {
                   i++;
               }
           }
        },"t2");
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("总和为: " + i);
    }

    //UncaughtExceptionHandler
    public void test7(){
        Thread t1 = new Thread(()->{
            System.out.println("t1线程执行了...");
//            throw new RuntimeException();
            System.out.println(1/0);
        },"t1");

        t1.setUncaughtExceptionHandler(handler);
        t1.start();
    }

    public void test6(){ // -XX:-DoEscapeAnalysis -XX:-EliminateLocks 怎么没效果？？？
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000000 ; i++) {
            Dog dog = new Dog();
            synchronized (dog){
                System.out.println("锁消除" + i);
            }
        }
        long endTime = System.currentTimeMillis();

        System.out.println("耗时: " + (endTime - startTime));
    }

    public void test5(){
        Thread thread1 = new Thread(()->{
            logger.info("线程1开始执行");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.error("interrupt1");
                e.printStackTrace();
            }

            synchronized (lock){
                System.out.println("enter lock");
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    logger.error("interrupt2");
                }
            }

            logger.info("线程1执行完了");

        },"t1");
        thread1.start();


        new Thread(()->{
            logger.info("线程2开始执行");
            thread1.interrupt();
            logger.info("线程2执行完了");

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            synchronized (lock){
                lock.notify();
            }

            logger.info("线程2真的执行完了");
        },"t2").start();
    }

    //LockSupport
    public void test4(){
        Thread thread1 = new Thread(()->{
            logger.info("线程1开始执行");
            LockSupport.park();
            logger.info("线程1执行完了");

        },"t1");
        thread1.start();

        new Thread(()->{
            logger.info("线程2开始执行");
            LockSupport.unpark(thread1);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            logger.info("线程2执行完了");
        },"t2").start();
    }

    public void test3(){

        Thread thread1 = new Thread(()->{
            logger.info("线程1开始执行");
            LockSupport.park();
            logger.info("线程1执行完了");

        },"t1");
        thread1.start();


        new Thread(()->{
            logger.info("线程2开始执行");

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            logger.info("线程2执行完了");
            LockSupport.unpark(thread1);

        },"t2").start();
    }

    public void test2(){

        new Thread(()->{
            logger.info("线程1开始执行");

            synchronized (lock){
                try {
                    logger.info("线程1 waiting...");
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            logger.info("线程1执行完了");

        },"t1").start();


        new Thread(()->{
            logger.info("线程2开始执行");

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            logger.info("线程2执行完了");

            synchronized (lock){
                lock.notify();
            }

        },"t2").start();
    }

    public void test() {// -XX:+UseBiasedLocking -XX:BiasedLockingStartupDelay=0 BiasedLockingStartupDelay延时设为0就开启了偏向锁

//        try {
//            Thread.sleep(500);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        Dog dog = new Dog();
//        System.out.println(dog.hashCode()); // 调用对象的hashCode方法会禁用偏向锁
        System.out.println(Thread.currentThread().getName() + "线程1: " + ClassLayout.parseInstance(dog).toPrintable());
        new Thread(()->{
            synchronized (dog){
                System.out.println(Thread.currentThread().getName() + "线程2: " + ClassLayout.parseInstance(dog).toPrintable());
            }
        },"t1").start();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName() + "线程3: " + ClassLayout.parseInstance(dog).toPrintable());


        System.out.println("============================================================================================");
        new Thread(()->{
            synchronized (dog){
                System.out.println(Thread.currentThread().getName() + "线程4: " + ClassLayout.parseInstance(dog).toPrintable());
            }
        },"t2").start();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName() + "线程5: " + ClassLayout.parseInstance(dog).toPrintable());
    }

    public class Dog {

        private Integer id;
//
        private String name;

        private String address;
//
//        public Integer getId() {
//            return id;
//        }
//
//        public void setId(Integer id) {
//            this.id = id;
//        }
//
//        public String getName() {
//            return name;
//        }
//
//        public void setName(String name) {
//            this.name = name;
//        }
    }

    public class Task implements Callable<Integer>{
        @Override
        public Integer call() throws Exception {
            int sum = 0;
            for (int j = 1; j <= 100; j++) {
                Thread.sleep(20);
                sum = sum + j;
            }
            return sum;
        }
    }

    // 数组求和任务
    private static class ArrSumTask implements Callable<Long> {
        private long[] numberArr;
        private int from;
        private int to;

        public ArrSumTask(long[] numberArr, int from, int to) {
            this.numberArr = numberArr;
            this.from = from;
            this.to = to;
        }

        @Override
        public Long call() {
            long total = 0;
            for (int i = from; i <= to; i++) {
                total += numberArr[i];
            }
            return total;
        }
    }
}
