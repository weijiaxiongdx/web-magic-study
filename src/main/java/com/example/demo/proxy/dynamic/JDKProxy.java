package com.example.demo.proxy.dynamic;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author
 * JDK动态代理，只能代理接口(被代理的类需要实现接口，如果没有实现接口则可使用CGLIB生成代理对象，spring就是这么做的)
 */
public class JDKProxy {

    // 主题接口
    public interface IDBQuery{
        String request();
    }


    // 真实主题。重量级对象，创建过程很慢
    public class DBQuery implements IDBQuery {
        public DBQuery(){
            System.out.println("这个过程可能非常耗时，通过代理来实现延迟加载...");
        }

        @Override
        public String request(){
            return "真实主题实现";
        }
    }


    // 动态生成代理对象
    public IDBQuery createJDKProxy(){
        // 生成一个实现了IDBQuery接口的代理类
        IDBQuery idbQuery = (IDBQuery)Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), IDBQuery.class.getInterfaces(), new JDKDBQueryHandler());
        System.out.println("动态生成的代理类对象： " + idbQuery);
        idbQuery.request();//通过代理对象调用真实对象方法时，会执行JDKDBQueryHandler的invoke方法
        System.out.println("通过代理对象调用方法完成");
        return idbQuery;
    }


    // 代理类的内部逻辑
    public class JDKDBQueryHandler implements InvocationHandler{
        IDBQuery real = null;

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if(real == null){
                System.out.println("创建真实对象");
                real = new DBQuery();
            }
            return real.request();
        }
    }
}
