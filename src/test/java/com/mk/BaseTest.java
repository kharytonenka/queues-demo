package com.mk;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.testng.annotations.BeforeClass;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class BaseTest {

    protected Channel channel;

    protected Connection connection;

    @BeforeClass
    public void createConnectionAndChannel() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();

        factory.setHost(System.getProperty("cloudamqp.host"));
        factory.setVirtualHost(System.getProperty("cloudamqp.virtualHost"));
        factory.setUsername(System.getProperty("cloudamqp.username"));
        factory.setPassword(System.getProperty("cloudamqp.password"));

        connection = factory.newConnection();
        channel = connection.createChannel();
    }

}
