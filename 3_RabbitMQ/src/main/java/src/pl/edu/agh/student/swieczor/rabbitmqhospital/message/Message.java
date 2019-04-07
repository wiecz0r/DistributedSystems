package src.pl.edu.agh.student.swieczor.rabbitmqhospital.message;

import com.rabbitmq.client.Channel;
import src.pl.edu.agh.student.swieczor.rabbitmqhospital.App;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class Message implements Serializable {
    private String timestamp;

    public Message() {
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
    }

    public static Message deserialize(byte[] byteArray) {
        ByteArrayInputStream bis = new ByteArrayInputStream(byteArray);
        try (ObjectInputStream oi = new ObjectInputStream(bis)) {
            return (Message) oi.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public byte[] serialize() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(this);
            oos.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void send(Channel channel, String routingKey) {
        try {
            channel.basicPublish(App.EXCHANGE_NAME, routingKey, null, this.serialize());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.printf("Sent message with key [%s]:\n%s\n", routingKey, this.toString());
    }
}
