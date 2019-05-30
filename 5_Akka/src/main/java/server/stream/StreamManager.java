package server.stream;

import akka.actor.AbstractLoggingActor;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.japi.pf.DeciderBuilder;
import akka.japi.pf.ReceiveBuilder;
import messaging.MessageType;
import messaging.Request;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

import static akka.actor.SupervisorStrategy.stop;

public class StreamManager extends AbstractLoggingActor {
    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
                .match(Request.class, request -> {
                    if(request.type.equals(MessageType.STREAM)){
                        context().actorOf(Props.create(StreamWorker.class)).tell(request,getSender());
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
        return new OneForOneStrategy(5, Duration.create(10, TimeUnit.SECONDS), DeciderBuilder.
                matchAny(o -> stop()).
                build());
    }
}
