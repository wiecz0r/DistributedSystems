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

    Doctor() throws IOException, TimeoutException {
        super();
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        Doctor doctor = new Doctor();
        System.out.println("Doctor");

        //Declare info-queue
        String infoQ = doctor.createQueue("info");
        doctor.channel.basicConsume(infoQ,true, new ReceiveConsumer(doctor.channel));

        //Declare result-queue
        String examinationResultsQ = doctor.createQueue("doctor");
        BasicProperties props = new BasicProperties.Builder().replyTo(examinationResultsQ).build();

        doctor.channel.basicConsume(examinationResultsQ,true,new ReceiveConsumer(doctor.channel));

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while(true){
            System.out.println("Send message to technician.\nPatient name:");
            String patient = br.readLine();
            System.out.println("Examination type (knee / hip / elbow): ");
            String type = br.readLine().toUpperCase();

            RequestExaminationMessage requestMsg =
                    new RequestExaminationMessage(ExaminationType.valueOf(type),patient);
            requestMsg.sendMsg(doctor.channel,props);
        }



    }
}
