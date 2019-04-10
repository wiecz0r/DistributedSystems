package src.pl.edu.agh.student.swieczor.rabbitmqhospital.message;

import com.rabbitmq.client.Channel;
import src.pl.edu.agh.student.swieczor.rabbitmqhospital.ExaminationType;

public class ResultExaminationMessage extends ExaminationMessage {
    private String examinationResult;

    public ResultExaminationMessage(ExaminationType examinationType, String patient, String examinationResult) {
        super(examinationType, patient);
        this.examinationResult = examinationResult;
    }

    public String getExaminationResult() {
        return examinationResult;
    }

    @Override
    public String toString() {
        return super.toString() + " " + examinationResult;
    }


    public void sendMsg(Channel channel, String routingKey) {
        super.sendMsg(channel, routingKey, null);
    }
}
