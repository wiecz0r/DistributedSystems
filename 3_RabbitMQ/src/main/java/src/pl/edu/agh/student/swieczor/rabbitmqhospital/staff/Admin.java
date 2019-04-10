package src.pl.edu.agh.student.swieczor.rabbitmqhospital.staff;

import src.pl.edu.agh.student.swieczor.rabbitmqhospital.message.InformationMessage;
import src.pl.edu.agh.student.swieczor.rabbitmqhospital.staff.consumer.ReceiveConsumer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeoutException;

import static src.pl.edu.agh.student.swieczor.rabbitmqhospital.Color.*;

public class Admin extends Staff {

    Admin() throws IOException, TimeoutException {
        super();
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        System.out.println(ANSI_BLUE + "Administrator" + ANSI_RESET);
        Admin admin = new Admin();

        String loggerQ = admin.createQueue("#");
        admin.channel.basicConsume(loggerQ,new ReceiveConsumer(admin.channel));

        System.out.println("SEND TO EVERYONE: ");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true){
            String message = br.readLine();
            InformationMessage infoMsg = new InformationMessage(message);
            infoMsg.sendMsg(admin.channel,"info",null);
        }

    }
}
