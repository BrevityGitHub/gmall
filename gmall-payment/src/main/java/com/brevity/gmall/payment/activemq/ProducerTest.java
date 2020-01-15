package com.brevity.gmall.payment.activemq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.*;

public class ProducerTest {
    public static void main(String[] args) throws JMSException {

        /*
        1.创建工厂
        2.创建连接，并打开连接
        3.创建session会话
        4.创建队列
        5.创建消息提供者
        6.创建消息对象
        7.发送消息
        8.关闭
         */

        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory("tcp://192.168.116.136:61616");
        Connection connection = activeMQConnectionFactory.createConnection();
        connection.start();

        // 第一个参数表示是否开启事务，第二个参数表示开启事务后以什么方式处理
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        Queue brevity = session.createQueue("brevity");
        MessageProducer producer = session.createProducer(brevity);
        ActiveMQTextMessage activeMQTextMessage = new ActiveMQTextMessage();
        activeMQTextMessage.setText("好累呀!");
        producer.send(activeMQTextMessage);

        producer.close();
        session.close();
        connection.close();
    }
}
