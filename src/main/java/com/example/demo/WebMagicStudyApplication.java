package com.example.demo;

import com.example.demo.concurrentlist.ConcurrentListTest;
import com.example.demo.jvm.JVMTest;
import com.example.demo.proxy.dynamic.JDKProxy;
import com.example.demo.recursion.RecursionTest;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WebMagicStudyApplication {

	public static void main(String[] args) {
        /**
         * 第一种启动方式
         */
		//SpringApplication.run(WebMagicStudyApplication.class, args);


        /**
         * 第二种启动方式
         *   禁止输出启动springboot启动标识
         */
        SpringApplication application = new SpringApplication(WebMagicStudyApplication.class);
        application.setBannerMode(Banner.Mode.OFF);
		application.setRegisterShutdownHook(false);
		ApplicationContextUtil.setApplicationContext(application.run(args));

		ObjectLayoutTest jol = new ObjectLayoutTest();
		jol.test50();

		//ObjectLayoutTest.SerSingleTon.test49();

		/*JDKProxy jdkProxy = new JDKProxy();
		jdkProxy.createJDKProxy();*/

		/*RecursionTest recursionTest = new RecursionTest();
		recursionTest.test50();*/

		/*JVMTest jvmTest = new JVMTest();
		jvmTest.test();*/

		/*ConcurrentListTest concurrentListTest = new ConcurrentListTest();
		concurrentListTest.test2();*/

     /*   Random random = new Random();
        int capacity = 100000;
//		int[] arr = {98,23,45,78,100,134,2,4,6,65,34,90,21,32,1};
        int[] arr = new int[capacity];
        for (int i = 0; i < capacity; i++) {
            arr[i] = random.nextInt(capacity);
        }
        long startTime = System.currentTimeMillis();
        jol.test21(arr,0,arr.length - 1);
        System.out.println("快速排序耗时: " + (System.currentTimeMillis() - startTime));

        long startTime2 = System.currentTimeMillis();
        jol.test22(arr);
        System.out.println("冒泡排序耗时: " + (System.currentTimeMillis() - startTime2));*/


    /*ProducerConsumer producerConsumer = new ProducerConsumer();
		producerConsumer.test();*/


     /*   TencentCloudApiTest tencentCloudApiTest = new TencentCloudApiTest();
		tencentCloudApiTest.test2();*/
	}
}
