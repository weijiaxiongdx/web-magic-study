package com.example.demo.rocketmqCluster;

import com.alibaba.fastjson.JSON;
import com.example.demo.rocketmq.User;
import org.apache.commons.lang.StringUtils;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;


/**
 * 发送事务消息
 * 最多回查15次，可在broker配置文件中配置，超过则默认丢弃消息
 */
public class SendTranscationMessage {

    public static void main(String[] args) throws MQClientException {
        TransactionMQProducer producer = new TransactionMQProducer("wjx-group");
        producer.setNamesrvAddr("192.168.1.88:9876;192.168.1.248:9876");
        producer.setTransactionListener(new TransactionListener(){
            @Override
            public LocalTransactionState executeLocalTransaction(Message message, Object o) {
                if(StringUtils.equals(message.getTags(),"user-tag-transaction-message")){
                    // 这里一般执行本地事务，保存日志(用于幂等,有日志则不执行本地事务，没有才会执行本地事务并发送消息)
                    System.out.println("执行本地事务并提交消息,事务id: " + message.getTransactionId());
                    return LocalTransactionState.COMMIT_MESSAGE;
                } else {
                    System.out.println("返回事务的状态为不明确,事务id: " + message.getTransactionId());
                    // 此状态的事务消息，rocketmq会进行回查，即调用checkLocalTransaction方法
                    return LocalTransactionState.UNKNOW;
                }
            }

            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt messageExt) {
                System.out.println("本地事务回查,事务id: " + messageExt.getTransactionId());
                return LocalTransactionState.COMMIT_MESSAGE;
            }
        });
        producer.start();

        for (int i=0;i<10;i++){
            User user = new User();
            user.setId(i);
            user.setName("张"+String.valueOf(i));
            Message message = new Message("wjx-topic", "user-tag-transaction-message3", JSON.toJSONString(user).getBytes());
            System.out.println("生产者发送事务消息:"+ JSON.toJSONString(user));
            SendResult result = producer.sendMessageInTransaction(message,null);
            System.out.println("返回信息: " + result);
        }
    }
}
