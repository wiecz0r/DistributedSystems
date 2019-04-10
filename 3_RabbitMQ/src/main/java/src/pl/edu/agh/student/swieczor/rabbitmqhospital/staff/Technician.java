package src.pl.edu.agh.student.swieczor.rabbitmqhospital.staff;

import src.pl.edu.agh.student.swieczor.rabbitmqhospital.ExaminationType;
import src.pl.edu.agh.student.swieczor.rabbitmqhospital.staff.consumer.ReceiveConsumer;
import src.pl.edu.agh.student.swieczor.rabbitmqhospital.staff.consumer.RequestReceiveConsumer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeoutException;


public class Technician extends Staff {

    private ExaminationType type1;
    private ExaminationType type2;


    Technician(ExaminationType type1, ExaminationType type2) throws IOException, TimeoutException {
        super();
        this.type1=type1;
        this.type2=type2;
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        System.out.println("Technician");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("You must enter two specializations (knee / hip / elbow).\n1st:");
        ExaminationType type1 = ExaminationType.valueOf(br.readLine().toUpperCase());
        System.out.println("2nd:");
        Technician technician = new Technician(type1,ExaminationType.valueOf(br.readLine().toUpperCase()));

        //Declare info-queue
        String infoQ = technician.createQueue("info");
        technician.channel.basicConsume(infoQ,true, new ReceiveConsumer(technician.channel));

        technician.channel.basicConsume(technician.type1.toString().toLowerCase(),true,
                new RequestReceiveConsumer(technician.channel));
        technician.channel.basicConsume(technician.type2.toString().toLowerCase(),true,
                new RequestReceiveConsumer(technician.channel));

    }
}
