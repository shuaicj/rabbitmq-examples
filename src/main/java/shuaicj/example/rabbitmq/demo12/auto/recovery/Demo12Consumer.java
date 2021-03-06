package shuaicj.example.rabbitmq.demo12.auto.recovery;

import java.io.IOException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Address;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import lombok.extern.slf4j.Slf4j;
import shuaicj.retry.RetryUtil;

/**
 * See http://www.rabbitmq.com/api-guide.html#recovery.
 */
@Slf4j
public class Demo12Consumer {

    private static final String CONN_STRING = "127.0.0.1";

    private static final String Q = "demo12-queue";

    public static void main(String[] args) throws InterruptedException {
        ConnectionFactory factory = new ConnectionFactory();
        Address[] addresses = Address.parseAddresses(CONN_STRING);
        Connection connection = RetryUtil.retry("new connection", () -> factory.newConnection(addresses));
        Channel channel = RetryUtil.retry("new channel", () -> connection.createChannel());

        boolean durable = true;
        RetryUtil.retry("queue declare", () -> channel.queueDeclare(Q, durable, false, false, null));

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body) throws IOException {
                try {
                    doWork(body);
                } finally {
                    // basicAck or basicReject can be executed only once, or an exception will be thrown occasionally
                    // saying "unknown deliveryTag".
                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
            }
        };

        boolean autoAck = false;
        RetryUtil.retryForever("consume", () -> channel.basicConsume(Q, autoAck, consumer));
    }

    private static void doWork(byte[] body) {
        String message = new String(body);
        log.info("Message received: " + message);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("Work done! " + message);
    }
}
