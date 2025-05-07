package com.deltacodex.ee.client;

import jakarta.jms.*;

import javax.naming.InitialContext;
import javax.naming.NamingException;

//Edit Config -> Modify Options -> Allow Multiple Instance

public class QueueReceiver {
    public static void main(String[] args) {
        try {
            InitialContext ctx = new InitialContext();
            QueueConnectionFactory queueConnectionFactory =
                    (QueueConnectionFactory) ctx.lookup("jms/MyQueueConnectionFactory");
            System.out.println(queueConnectionFactory);

            QueueConnection connection = queueConnectionFactory.createQueueConnection();
            connection.start();

//            QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            QueueSession session = connection.createQueueSession(false, Session.CLIENT_ACKNOWLEDGE);
            System.out.println(session);

            Queue queue = (Queue) ctx.lookup("jms/MyQueue");
            jakarta.jms.QueueReceiver receiver = session.createReceiver(queue);

/*
            Message received_Message = receiver.receive();
            System.out.printf("Message received: %s\n", received_Message.getBody(String.class));
*/

            receiver.setMessageListener(message -> {
                try {
                    String msg = message.getBody(String.class);
                    System.out.println(msg);
                    message.acknowledge();
                } catch (JMSException e) {
                    throw new RuntimeException(e);
                }
            });

            while (true) {}

        } catch (NamingException | JMSException e) {
            throw new RuntimeException(e);
        }
    }
}
