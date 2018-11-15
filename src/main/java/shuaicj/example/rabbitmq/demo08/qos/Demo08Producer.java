package shuaicj.example.rabbitmq.demo08.qos;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Demo08Producer {

    private static final String CONN_STRING = "127.0.0.1";

    private static final String Q = "demo08-queue";

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        Connection connection = factory.newConnection(Address.parseAddresses(CONN_STRING));
        Channel channel = connection.createChannel();

        boolean durable = true;
        channel.queueDeclare(Q, durable, false, false, null);

        for (int i = 0; i < 10; i++) {
            String message = "Hello World! " + i;
            channel.basicPublish("", Q, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
            log.info("Message sent: " + message);
        }

        channel.close();
        connection.close();
    }
}
