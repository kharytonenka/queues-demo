package com.mk;

import com.rabbitmq.client.DeliverCallback;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class BasicQueueTest extends BaseTest {

    public static final String QUEUE_NAME = "demo-queue";

    public static final String EXCHANGE_DIRECT_TYPE_NAME = "demo-exchange.direct";

    public static final String ROUTING_KEY = "basic-message";

    public static final String MESSAGE_BODY = "Queue Message";

    public static final int NUMBER_OF_MESSAGES = 10;

    public static final int ONE_MINUTE_TIMEOUT = 60000;

    protected List<String> receivedMessages = new ArrayList<>();

    @BeforeClass(dependsOnMethods = "createConnectionAndChannel")
    public void createExchangeAndQueue() throws IOException, TimeoutException {
        channel.exchangeDeclare(EXCHANGE_DIRECT_TYPE_NAME, "direct");
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        channel.queueBind(QUEUE_NAME, EXCHANGE_DIRECT_TYPE_NAME, ROUTING_KEY);
    }

    @Test
    public void sendMessagesToQueueTest() throws IOException {
        for (int i = 0; i < NUMBER_OF_MESSAGES; i++) {
            channel.basicPublish(EXCHANGE_DIRECT_TYPE_NAME, ROUTING_KEY, null, MESSAGE_BODY.getBytes());
        }
    }

    @Test(dependsOnMethods = "sendMessagesToQueueTest")
    public void receiveMessagesFromQueueTest() throws IOException, InterruptedException {
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            processMessage(message);
            saveMessage(message);
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        };

        channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> {
        });

        waitForMessages(receivedMessages, NUMBER_OF_MESSAGES, ONE_MINUTE_TIMEOUT);

        System.out.println("Received messages:");
        receivedMessages.forEach(System.out::println);

        Assert.assertEquals(receivedMessages.size(), NUMBER_OF_MESSAGES);
        receivedMessages.forEach(it -> Assert.assertEquals(it, MESSAGE_BODY));
    }

    protected void processMessage(String message) {
        try {
            Thread.sleep((long) message.length() * 100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void saveMessage(String message) {
        receivedMessages.add(message);
    }

    protected void waitForMessages(List<String> receivedMessages, int expectedNumberOfMessages, int timeout) throws InterruptedException {
        long timeSnapshot = System.currentTimeMillis();

        while (receivedMessages.size() < expectedNumberOfMessages || System.currentTimeMillis() > timeSnapshot + timeout) {
            Thread.sleep(1000);
        }
    }

    @AfterClass(alwaysRun = true)
    public void cleanResources() throws IOException {
        channel.queueDelete(QUEUE_NAME);
        channel.exchangeDelete(EXCHANGE_DIRECT_TYPE_NAME);
        connection.close();
    }


}
