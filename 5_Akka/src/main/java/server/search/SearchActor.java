package server.search;

import akka.actor.AbstractLoggingActor;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.japi.pf.DeciderBuilder;
import akka.japi.pf.ReceiveBuilder;
import messaging.MessageType;
import messaging.Request;

import java.time.Duration;

import static akka.actor.SupervisorStrategy.stop;

public class SearchSupervisorSingleDb extends AbstractLoggingActor {
    @Override
    public Receive createReceive() {
        

        return ReceiveBuilder.create()
                .match(Request.class, request -> {
                    if(request.type.equals(MessageType.SEARCH)){
                        getContext().actorOf(Props.create(SearchSupervisorSingleDb.class)).tell(request,getSender());
                    }
                    else{
                        log().info("received unknown message");
                    }
                })
                .matchAny(o -> log().info("received unknown message"))
                .build();
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return new OneForOneStrategy(5, Duration.ofSeconds(10), DeciderBuilder.
                matchAny(o -> stop()).
                build());
    }

}
