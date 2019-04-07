package src.pl.edu.agh.student.swieczor.rabbitmqhospital.staff;

import com.rabbitmq.client.*;
import src.pl.edu.agh.student.swieczor.rabbitmqhospital.App;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public abstract class Staff {
    private Channel channel;

    public Staff() throws IOException, TimeoutException {
        init();
    }

    private void init() throws IOException, TimeoutException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");

        this.channel = connectionFactory.newConnection().createChannel();
    }

    private String createQueue(String routeKey) throws IOException {
        String queue = channel.queueDeclare().getQueue();
        channel.queueBind(queue, App.EXCHANGE_NAME, routeKey);

        System.out.printf("Created queue '%s' with route key [%s]",queue,routeKey);
        return queue;
    }




}
