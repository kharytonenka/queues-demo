package com.mk;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DeliverCallback;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

public class RpcQueueTest extends BaseTest {

    public static final String QUEUE_SENDER_NAME = "mk-demo-rpc-queue-sender";

    public static final String QUEUE_RECEIVER_NAME = "mk-demo-rpc-queue-receiver";

    public static final String MESSAGE_BODY = "rpc queue message";

    @BeforeClass(dependsOnMethods = "createConnectionAndChannel")
    public void createQueues() throws IOException, TimeoutException {
        channel.queueDeclare(QUEUE_SENDER_NAME, false, false, false, null);
        channel.queueDeclare(QUEUE_RECEIVER_NAME, false, false, false, null);
    }

    @Test
    public void createServerTest() throws IOException {
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            AMQP.BasicProperties replyProperties = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(delivery.getProperties().getCorrelationId())
                    .build();

            String message = new String(delivery.getBody(), "UTF-8");
            String response = processMessage(message);

            channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProperties, response.getBytes("UTF-8"));
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        };

        channel.basicConsume(QUEUE_SENDER_NAME, false, deliverCallback, (consumerTag -> {
        }));
    }

    @Test(dependsOnMethods = "createServerTest")
    public void sendMessageFromClientToServerTest() throws IOException, InterruptedException {
        final String corrId = UUID.randomUUID().toString();

        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .correlationId(corrId)
                .replyTo(QUEUE_RECEIVER_NAME)
                .build();

        System.out.println(String.format("Initial message: %s", MESSAGE_BODY));

        channel.basicPublish("", QUEUE_SENDER_NAME, props, MESSAGE_BODY.getBytes("UTF-8"));

        final BlockingQueue<String> response = new ArrayBlockingQueue<>(1);

        channel.basicConsume(QUEUE_RECEIVER_NAME, true, (consumerTag, delivery) -> {
            if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                response.offer(new String(delivery.getBody(), "UTF-8"));
            }
        }, consumerTag -> {
        });

        String result = response.take();
        System.out.println(String.format("Processed message: %s", result));

        Assert.assertEquals(result, MESSAGE_BODY.toUpperCase());
    }

    protected String processMessage(String message) {
        return message.toUpperCase();
    }

    @AfterClass(alwaysRun = true)
    public void cleanResources() throws IOException {
        channel.queueDelete(QUEUE_SENDER_NAME);
        channel.queueDelete(QUEUE_RECEIVER_NAME);
        connection.close();
    }
}
