package com.example.demo;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 不配置webEnvironment，执行测试方法时会报错javax.websocket.server.ServerContainer not available
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebMagicStudyApplicationTests {

	@Autowired
	private RocketMQTemplate rocketMQTemplate;

	@Test
	void contextLoads() {
	}

	/**
	 * 引入rocketmq-spring-boot-starter.2.2.0版本时，会报错
	 */
	@Test
	void test2(){
		// 第一个参数就是消息的Topic
		rocketMQTemplate.convertAndSend("wjx-topic","rocketmq-springboot-message");
		System.out.println("rocketMQTemplate发送了消息");
	}
}
