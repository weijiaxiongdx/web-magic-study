package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WebMagicStudyApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebMagicStudyApplication.class, args);

		ObjectLayoutTest jol = new ObjectLayoutTest();
		jol.test31();

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
