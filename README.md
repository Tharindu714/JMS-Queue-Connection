# Java Messaging Service (JMS) Queue Connection Project

> **Purpose**: Demonstrate a point‑to‑point JMS Queue implementation using GlassFish/Payara’s JMS provider. Includes producer (`JMS‑Producer`), consumer (`JMS‑Consumer`), server setup screenshots, and in‑depth discussion of acknowledgement modes.

---

## 📁 Project Structure

```
Java-Messaging-Service-Queue-Connection/
├─ JMS-Producer/                     # Maven project: sends messages to the queue
├─ JMS-Consumer/                     # Maven project: receives messages from the queue
├─ How to execute admin console commands.txt  # Admin Console start/stop guide
├─ JMS Connection Factory.PNG        # Admin Console screenshot of Connection Factory
├─ JMS Queue Resources.PNG           # Admin Console screenshot of Queue configuration
├─ JMS-Producer Output.PNG           # Console output illustrating message send
└─ JMS-Consumer Output.PNG           # Console output illustrating message receive
```

---

## 🛠 Prerequisites

1. **Java 11+** JDK
2. **Apache Maven 3.6+**
3. **GlassFish 7** or **Payara 6** (Jakarta EE 9+)
4. Access to **asadmin** CLI or **Admin Console** at `http://localhost:4848`

---

## 1. Configure JMS Resources on the Server

### 1.1 Start GlassFish Domain

Refer to the **Admin Console Commands** section below for the exact commands.

### 1.2 Create a JMS Queue Connection Factory

1. In Admin Console, navigate to **Resources ▶ JMS ▶ Connection Factories ▶ New**.
2. Fill in:

   * **JNDI Name**: `jms/MyQueueConnectionFactory`
   * **Resource Type**: `javax.jms.QueueConnectionFactory`
3. Click **Save**.

![Connection Factory](https://github.com/user-attachments/assets/db404306-ca58-4e94-bca9-165a086d249e)


**CLI Alternative:**

```bash
asadmin create-jms-resource \
  --restype javax.jms.QueueConnectionFactory \
  --property Name=MyQueueConnectionFactory jms/MyQueueConnectionFactory
```

### 1.3 Create a JMS Queue Destination

1. Go to **Resources ▶ JMS ▶ Destination Resources ▶ New**.
2. Enter:

   * **JNDI Name**: `jms/MyQueue`
   * **Type**: `javax.jms.Queue`
   * **Physical Destination Name**: `MyQueue`
3. Click **Save**.

![Destination resource](https://github.com/user-attachments/assets/91f9c7dc-ecc9-4928-8ea5-0e8f00d751e9)


**CLI Alternative:**

```bash
asadmin create-jms-resource \
  --restype javax.jms.Queue \
  --property Name=MyQueue jms/MyQueue
```

---

## 2. JMS Producer: JMS-Producer

### 2.1 Build the Producer

```bash
cd JMS-Producer
mvn clean package
```

### 2.2 Run the Producer

```bash
java -jar target/JMS-Producer-1.0.jar
```

* The producer reads lines from the console and sends each as a JMS `TextMessage`.
* Type `exit` to close the application.


#### Producer Code Snippet

```java
InitialContext ctx = new InitialContext();
QueueConnectionFactory factory = (QueueConnectionFactory)
    ctx.lookup("jms/MyQueueConnectionFactory");
QueueConnection conn = factory.createQueueConnection();
conn.start();

QueueSession session = conn.createQueueSession(
    false, Session.AUTO_ACKNOWLEDGE);
Queue queue = (Queue) ctx.lookup("jms/MyQueue");
QueueSender sender = session.createSender(queue);

Scanner scanner = new Scanner(System.in);
while (true) {
    String text = scanner.nextLine();
    if ("exit".equalsIgnoreCase(text)) break;
    TextMessage msg = session.createTextMessage(text);
    sender.send(msg);
}
session.close();
conn.close();
```

---

## 3. JMS Consumer: JMS-Consumer

### 3.1 Build the Consumer

```bash
cd JMS-Consumer
mvn clean package
```

### 3.2 Run the Consumer

```bash
java -jar target/JMS-Consumer-1.0.jar
```

* The consumer listens for messages indefinitely and prints each received `TextMessage`.

![JQS Reciver](https://github.com/user-attachments/assets/7c5a7194-076a-4215-b414-b8449a8acd70)


#### Consumer Code Snippet

```java
InitialContext ctx = new InitialContext();
QueueConnectionFactory factory = (QueueConnectionFactory)
    ctx.lookup("jms/MyQueueConnectionFactory");
QueueConnection conn = factory.createQueueConnection();
conn.start();

QueueSession session = conn.createQueueSession(
    false, Session.CLIENT_ACKNOWLEDGE);
Queue queue = (Queue) ctx.lookup("jms/MyQueue");
QueueReceiver receiver = session.createReceiver(queue);

receiver.setMessageListener(message -> {
    try {
        String text = ((TextMessage) message).getText();
        System.out.println("Received: " + text);
        // Explicit acknowledge in CLIENT_ACKNOWLEDGE mode:
        message.acknowledge();
    } catch (JMSException e) {
        e.printStackTrace();
    }
});

// Keep the application alive
Thread.currentThread().join();
```

---

## 4. Acknowledgement Modes Explained

| Mode                         | Description                                                                                                                                                |
| ---------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `Session.AUTO_ACKNOWLEDGE`   | The session automatically acknowledges receipt **after** the `MessageListener.onMessage` returns successfully. This is the simplest mode.                  |
| `Session.CLIENT_ACKNOWLEDGE` | The client must explicitly call `message.acknowledge()` to acknowledge all messages received so far. Offers precise control but requires careful handling. |

> **When to use:**
>
> * **AUTO\_ACKNOWLEDGE** for straightforward scenarios where message loss is acceptable on failure.
> * **CLIENT\_ACKNOWLEDGE** when you need to process a batch of messages and only acknowledge after all succeed, or when re-delivery on failure is important.

---

## 5. Admin Console Commands

```text
# How to start Admin Console
PS> cd "C:\Program Files\payara6\bin"
PS> .\asadmin start-domain domain1
Waiting for domain1 to start ......
Successfully started domain1 (Admin Port: 4848)

# How to stop Admin Console
PS> .\asadmin stop-domain domain1

# Watch Real-Time Server Log
PS> Get-Content \
       "C:\Program Files\glassfish7\glassfish\domains\domain1\logs\server.log" -Wait
```

*(Adjust paths if using glassfish: `C:\Program Files\glassfish7\bin`)*

---

## 📖 Summary & Next Steps

* You’ve built and run a JMS point‑to‑point example with queue messaging.
* Experiment with **durable subscriptions**, **transacted sessions**, and **JMS transactions**.
* Integrate JMS producers/consumers into a Jakarta EE web application or Spring Boot service.

Happy messaging!
