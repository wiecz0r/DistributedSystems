package src.pl.edu.agh.student.swieczor.rabbitmqhospital.message;

public class RequestExaminationMessage extends ExaminationMessage {
    private String routingKey;

    public RequestExaminationMessage(ExaminationType examinationType, String patient, String routingKey) {
        super(examinationType, patient);
        this.routingKey = routingKey;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    @Override
    public String toString() {
        return super.toString() + " REQUEST";
    }
}
