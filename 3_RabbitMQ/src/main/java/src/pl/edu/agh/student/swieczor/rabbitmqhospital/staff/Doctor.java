package src.pl.edu.agh.student.swieczor.rabbitmqhospital.staff;

import com.rabbitmq.client.AMQP.BasicProperties;
import src.pl.edu.agh.student.swieczor.rabbitmqhospital.ExaminationType;
import src.pl.edu.agh.student.swieczor.rabbitmqhospital.message.RequestExaminationMessage;
import src.pl.edu.agh.student.swieczor.rabbitmqhospital.staff.consumer.ReceiveConsumer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeoutException;

public class Doctor extends Staff {


    public Doctor() throws IOException, TimeoutException {
        super();
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        Doctor doctor = new Doctor();
        System.out.println("Doctor");

        String infoQ = doctor.createQueue("info");
        doctor.channel.basicConsume(infoQ,true, new ReceiveConsumer(doctor.channel));

        String examinationResultsQ = doctor.createQueue("doctor");
        RequestExaminationMessage msg = new RequestExaminationMessage(ExaminationType.HIP,"Kowalski");
        BasicProperties props = new BasicProperties.Builder().replyTo(examinationResultsQ).build();


        msg.send(doctor.channel,props);


    }
}
