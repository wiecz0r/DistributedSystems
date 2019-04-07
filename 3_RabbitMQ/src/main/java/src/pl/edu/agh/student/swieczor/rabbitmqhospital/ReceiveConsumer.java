package src.pl.edu.agh.student.swieczor.rabbitmqhospital;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import src.pl.edu.agh.student.swieczor.rabbitmqhospital.message.Message;

import java.io.IOException;

public class ReceiveConsumer extends DefaultConsumer {
    /**
     * Constructs a new instance and records its association to the passed-in channel.
     *
     * @param channel the channel to which this consumer is attached
     */
    public ReceiveConsumer(Channel channel) {
        super(channel);
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        Message message = Message.deserialize(body);
        if (message != null) {
            System.out.println("Received message:\n" + message.toString());
        }
    }
}
