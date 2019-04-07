package src.pl.edu.agh.student.swieczor.rabbitmqhospital.staff.consumer;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import src.pl.edu.agh.student.swieczor.rabbitmqhospital.message.RequestExaminationMessage;
import src.pl.edu.agh.student.swieczor.rabbitmqhospital.message.ResultExaminationMessage;

public class RequestConsumer extends ReceiveConsumer {
    /**
     * Constructs a new instance and records its association to the passed-in channel.
     *
     * @param channel the channel to which this consumer is attached
     */
    public RequestConsumer(Channel channel) {
        super(channel);
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
        super.handleDelivery(consumerTag, envelope, properties, body);
        RequestExaminationMessage requestMsg = (RequestExaminationMessage) message;
        ResultExaminationMessage resultMsg = new ResultExaminationMessage(
                requestMsg.getExaminationType(), requestMsg.getPatient(), "");
        resultMsg.send(getChannel(), requestMsg.getRoutingKey());
    }
}