package server;

import akka.actor.*;
import akka.japi.pf.DeciderBuilder;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import messaging.Request;
import server.order.OrderSupervisor;
import server.search.SearchSupervisor;
import server.stream.StreamManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;

import static akka.actor.SupervisorStrategy.restart;

public class Server extends AbstractLoggingActor {
    public static final String ORDERS_FILE = "order/orders.txt";

    private ActorRef searchManager;
    private ActorRef orderManager;
    private ActorRef streamManager;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(Request.class, request -> {
            switch (request.type){
                case ORDER:
                    orderManager.tell(request,sender());
                case SEARCH:
                    searchManager.tell(request,sender());
                case STREAM:
                    streamManager.tell(request,sender());
                default:
                    break;
            }
        }).matchAny(o -> log().info("received unknown message")).build();
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return new OneForOneStrategy(5, Duration.ofSeconds(10), DeciderBuilder.
                matchAny(o -> restart()).
                build());
    }

    @Override
    public void preStart() {
        searchManager = context().actorOf(Props.create(SearchSupervisor.class),"search_manager");
        orderManager = context().actorOf(Props.create(OrderSupervisor.class),"order_manager");
        streamManager = context().actorOf(Props.create(StreamManager.class),"stream_manager");
    }

    public static void main(String[] args) throws IOException {
        final Config config = ConfigFactory.parseFile(new File("server/server.conf"));
        final ActorSystem system = ActorSystem.create("server_system",config);
        final ActorRef serverActor = system.actorOf(Props.create(Server.class),"server");
        System.out.println(serverActor.path().address());

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            String line = br.readLine();
            if (line.equals("q")) {
                break;
            }
        }
        system.terminate();
    }
}
