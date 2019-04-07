package src.pl.edu.agh.student.swieczor.rabbitmqhospital.staff.consumer;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import src.pl.edu.agh.student.swieczor.rabbitmqhospital.message.Message;


public class ReceiveConsumer extends DefaultConsumer {
    Message message;

    public ReceiveConsumer(Channel channel) {
        super(channel);
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
        message = Message.deserialize(body);
        if (message != null) {
            System.out.println("Received message:\n" + message.toString());
        }
    }
}
