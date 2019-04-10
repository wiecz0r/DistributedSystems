package src.pl.edu.agh.student.swieczor.rabbitmqhospital.staff;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import src.pl.edu.agh.student.swieczor.rabbitmqhospital.App;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

abstract class Staff {
    Channel channel;

    Staff() throws IOException, TimeoutException {
        init();
    }

    private void init() throws IOException, TimeoutException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");

        this.channel = connectionFactory.newConnection().createChannel();
    }

    String createQueue(String routeKey) throws IOException {
        String queue = channel.queueDeclare().getQueue();
        channel.queueBind(queue, App.EXCHANGE_NAME, routeKey);

        System.out.printf("Created queue '%s' with route key [%s]\n", queue, routeKey);
        return queue;
    }


}
