package com.deltacodex.ee.jms;

import jakarta.jms.*;

import javax.naming.InitialContext;
import javax.naming.NamingException;


public class QueueSender {
    public static void main(String[] args) {
        try {
            InitialContext ctx = new InitialContext();
            QueueConnectionFactory queueConnectionFactory =
                    (QueueConnectionFactory) ctx.lookup("jms/MyQueueConnectionFactory");
            System.out.println(queueConnectionFactory);

            QueueConnection connection = queueConnectionFactory.createQueueConnection();

            QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            System.out.println(session);

            Queue queue = (Queue) ctx.lookup("jms/MyQueue");

            jakarta.jms.QueueSender sender = session.createSender(queue);

            for (int i = 0; i < 10; i++) {
                TextMessage message = session.createTextMessage();
                message.setText("Hello World, This is a Queue Message, from JQS Sender "+i);
                sender.send(message);
            }

            sender.close();
            session.close();
            connection.close();

        } catch (NamingException | JMSException e) {
            throw new RuntimeException(e);
        }
    }
}
