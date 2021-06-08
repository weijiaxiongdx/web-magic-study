package com.example.demo.mylock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * 自定义锁-不可重入
 */
public class MyLock implements Lock{

    public static void main(String[] args){
        MyLock myLock = new MyLock();

        new Thread(()->{
            try {
                System.out.println("线程t1获取锁start");
                myLock.lock();
                myLock.lock();//阻塞

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("线程t1获取锁end");
            } finally {
                myLock.unlock();
                System.out.println("线程t1释放锁");
            }

        },"t1").start();

        new Thread(()->{
            try {
                System.out.println("线程t2获取锁start");
                myLock.lock();
                System.out.println("线程t2获取锁end");
            } finally {
                myLock.unlock();
                System.out.println("线程t2释放锁");
            }
        },"t2").start();
    }

    /**
     * 独占锁
     */
    class MySync extends AbstractQueuedSynchronizer{

        @Override
        protected boolean tryAcquire(int arg) {
            if(compareAndSetState(0,1)){
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }

            return false;
        }

        @Override
        protected boolean tryRelease(int arg) {
            setExclusiveOwnerThread(null);

            /**
             * state使用volatile修饰，有写屏障，放到后面，也可保证setExclusiveOwnerThread的修改对其他线程可见
             */
            setState(0);
            return true;
        }

        /**
         * 是否持有独占锁
         * @return
         */
        @Override
        protected boolean isHeldExclusively() {
            return getState() == 1;
        }

        public Condition newCondition(){
            return new ConditionObject();
        }
    }



    MySync mySync = new MySync();


    @Override
    public void lock() {
        mySync.acquire(1);
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        mySync.acquireInterruptibly(1);
    }

    @Override
    public boolean tryLock() {
        return mySync.tryAcquire(1);
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return mySync.tryAcquireNanos(1,unit.toNanos(time));
    }

    @Override
    public void unlock() {
        mySync.release(1);
    }

    @Override
    public Condition newCondition() {
        return mySync.newCondition();
    }
}
