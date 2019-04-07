package src.pl.edu.agh.student.swieczor.rabbitmqhospital.message;

public class InformationMessage extends Message {
    private String messageText;

    public InformationMessage(String messageText) {
        super();
        this.messageText = messageText;
    }

    public String getMessageText() {
        return messageText;
    }

    @Override
    public String toString() {
        return messageText;
    }
}
