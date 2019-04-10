package src.pl.edu.agh.student.swieczor.rabbitmqhospital;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeoutException;

public class App {
    public static final String EXCHANGE_NAME = "HOSPITAL";

    public static void main(String[] args) throws IOException, TimeoutException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter 'start' to make connections, create exchange and queues; 'stop' to exit program");
        String readln = br.readLine().toLowerCase();
        while(true){
            if (readln.equals("start")){
                ConnectionFactory factory = new ConnectionFactory();
                factory.setHost("localhost");
                Connection connection = factory.newConnection();
                Channel channel = connection.createChannel();
                channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
                channel.basicQos(1);

                for (ExaminationType examinationType : ExaminationType.values()) {
                    String q_name = examinationType.toString().toLowerCase();
                    channel.queueDeclare(q_name, false, false, false, null);
                    String routingKey = "request." + q_name;
                    channel.queueBind(q_name, EXCHANGE_NAME, routingKey);
                    System.out.printf("Created queue '%s' with routingKey: '%s'\n", q_name, routingKey);
                }
                while(true){
                    if(br.readLine().toLowerCase().equals("stop")){
                        channel.close();
                        connection.close();
                        System.out.println("Terminating...");
                        return;
                    }
                }
            } else {
                System.out.println("Enter 'start' to make connections, create exchange and queues; 'stop' to exit program");
            }
        }
    }
}
