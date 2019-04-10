package src.pl.edu.agh.student.swieczor.rabbitmqhospital.message;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import src.pl.edu.agh.student.swieczor.rabbitmqhospital.ExaminationType;

public class RequestExaminationMessage extends ExaminationMessage {

    public RequestExaminationMessage(ExaminationType examinationType, String patient) {
        super(examinationType, patient);
    }

    @Override
    public String toString() {
        return super.toString() + " REQUEST";
    }

    public void send(Channel channel, AMQP.BasicProperties properties) {
        String routingKey = "request." + this.examinationType.toString().toLowerCase();
        super.send(channel, routingKey, properties);
    }
}
