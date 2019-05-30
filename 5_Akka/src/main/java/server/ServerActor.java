package server;

import akka.actor.*;
import akka.japi.pf.DeciderBuilder;
import messaging.Request;
import scala.concurrent.duration.Duration;
import server.order.OrderSupervisor;
import server.search.SearchSupervisor;
import server.stream.StreamManager;

import java.util.concurrent.TimeUnit;

import static akka.actor.SupervisorStrategy.restart;

public class ServerActor extends AbstractLoggingActor {
    public static final String ORDERS_FILE = "database/orders.txt";

    private ActorRef searchManager;
    private ActorRef orderManager;
    private ActorRef streamManager;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(Request.class, request -> {
            switch (request.type){
                case ORDER:
                    orderManager.tell(request,sender());
                    break;
                case SEARCH:
                    searchManager.tell(request,sender());
                    break;
                case STREAM:
                    streamManager.tell(request,sender());
                    break;
                default:
                    break;
            }
        }).matchAny(o -> log().info("received unknown message")).build();
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return new OneForOneStrategy(5, Duration.create(10, TimeUnit.SECONDS), DeciderBuilder.
                matchAny(o -> restart()).
                build());
    }

    @Override
    public void preStart() {
        searchManager = context().actorOf(Props.create(SearchSupervisor.class),"search_manager");
        orderManager = context().actorOf(Props.create(OrderSupervisor.class),"order_manager");
        streamManager = context().actorOf(Props.create(StreamManager.class),"stream_manager");
    }
}
