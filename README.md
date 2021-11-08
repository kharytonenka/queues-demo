# Queues demo

## Prerequisites:

RabbitMQ instance is created at cloudamqp.com

### 1. Test `BasicQueueTest`:

1. Exchange of type `direct` is created.
2. Queue is created and bound to mentioned exchange with specific routing key.
3. 10 messages are published to the exchange with mentioned routing key.
4. Messages are consumed from the queue (with fake processing using timeout), number of messages and content of each message are verified. Basic acknowledgement is used (`autoAck = false`).
5. Exchange and queue are deleted.

Passed test console output:
```
Received messages:
Queue Message
Queue Message
Queue Message
Queue Message
Queue Message
Queue Message
Queue Message
Queue Message
Queue Message
Queue Message
```

### 2. Test `RpcQueueTest`:

1. Two queues are created: one for sending message, one for receiving.
2. Server is created with the following processing: income message is converted to upper case.
3. Client is created, sent message to server and received response, response is verified (that it's in upper case).
4. Queues are deleted.

Passed test console output:
```
Initial message: rpc queue message
Processed message: RPC QUEUE MESSAGE
```

Official RabbitMQ tutorials were used (see below).

## Useful links:
1. Rabbit MQ Tutorials: https://www.rabbitmq.com/getstarted.html