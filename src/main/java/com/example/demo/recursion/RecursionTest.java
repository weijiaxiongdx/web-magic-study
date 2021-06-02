package com.example.demo.recursion;

/**
 * 递归
 */
public class RecursionTest {

    private int count = 0;
    public void test51(){
        count++;
        test51();
    }

    /**
     * 栈深度测试
     * 栈桢：包括局部变量表(字空间可以重用、会影响GC)、操作数栈、动态链接方法、返回地址
     * 调用方法：栈桢的入栈和出栈
     * 函数嵌套调用的次数由栈的大小决定，栈越大，函数嵌套调用的次数越多。对一个函数而言，它的参数越多，内部局部变量越多，
     * 所占的栈空间就越多，嵌套调用次数就会减少
     */
    public void test50(){
        try {
            test51();
        } catch (Throwable e) {
            //栈大小为默认值时，连续运行6次，栈深度分别为16807、16954、16923、18007、18076、17302
            //通过JVM参数-Xss100M，将栈大小设置为100M时，栈深度分别为2357932、3071059、3022570、3296218、2880050、2680205
            System.out.println("栈的深度为：" + count);
            e.printStackTrace();
        }
    }


    /**
     * long、double类型在局部变更表占2个字空间
     */
    public void test52(){
        long a = 0L;
        long b = 0L;
    }
}
