package com.example.demo.proxy;

/**
 * @author
 * 使用代理类原因
 * 1.安全，屏蔽了客户端直接访问真实对象
 * 2.远程调用，使用代理类处理远程方法的调用细节
 * 3.提升性能，封装真实对象，从而达到延迟加载的目的
 */
public class ProxyTest {

    // 主题接口
    public interface IDBQuery{
        String request();
    }


    // 真实主题。重量级对象，创建过程很慢
    public class DBQuery implements IDBQuery{
        public DBQuery(){
            System.out.println("这个过程可能非常耗时，通过代理来实现延迟加载");
        }

        @Override
        public String request(){
            return "真实主题实现";
        }
    }


    // 代理对象
    public class DBQueryProxy implements IDBQuery{
        private DBQuery real;

        @Override
        public String request() {
            if(real == null){
                real = new DBQuery();
            }
            return real.request();
        }
    }



    public void test(){
        IDBQuery idbQuery = new DBQueryProxy();

        // 在真正使用的时候才创建真实对象
        idbQuery.request();
    }
}
