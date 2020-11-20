package com.example.demo.rocketmq;

import com.alibaba.fastjson.JSON;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;


/**
 * 事务消息
 */
public class TranscationProducer {

    public static void main(String[] args) throws MQClientException {
        TransactionMQProducer producer = new TransactionMQProducer("test-group");
        // 192.168.1.81本机 windows服务器;192.168.1.88开发环境linux服务器
        producer.setNamesrvAddr("192.168.1.81:9876;192.168.1.88:9876");
        producer.setInstanceName("rmq-instance");
        producer.setTransactionListener(new TransactionListener(){
            @Override
            public LocalTransactionState executeLocalTransaction(Message message, Object o) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("执行本地事务,事务id: " + message.getTransactionId());
                return LocalTransactionState.COMMIT_MESSAGE;
            }

            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt messageExt) {
                System.out.println("本地事务回查,事务id: " + messageExt.getTransactionId());
                return LocalTransactionState.COMMIT_MESSAGE;
            }
        } );
        producer.start();
        User user = new User();
        user.setId(1);
        user.setName("张"+String.valueOf(1));
        Message message = new Message("wjx-topic", "user-tag", JSON.toJSONString(user).getBytes());

        System.out.println("生产者发送消息:"+ JSON.toJSONString(user));
        SendResult sendResult = producer.sendMessageInTransaction(message, null);
        System.out.println("发送结果: " + JSON.toJSONString(sendResult));
        producer.shutdown();
    }
}
