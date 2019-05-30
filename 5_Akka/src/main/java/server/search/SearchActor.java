package server.search;

import akka.actor.*;
import akka.japi.pf.DeciderBuilder;
import akka.japi.pf.ReceiveBuilder;
import messaging.MessageType;
import messaging.Request;
import messaging.SearchResponse;

import scala.concurrent.duration.*;

import java.util.concurrent.TimeUnit;

import static akka.actor.SupervisorStrategy.stop;

public class SearchActor extends AbstractLoggingActor {
    private final String[] databases = {"database/DB1.txt","database/DB2.txt"};
    private ActorRef client;
    private int dbCounter = databases.length;

    @Override
    public Receive createReceive() {

        return ReceiveBuilder.create()
                .match(Request.class, request -> {
                    if(request.type.equals(MessageType.SEARCH)){
                        client = getSender();
                        for(String database : databases){
                            getContext().actorOf(Props.create(SearchActorSingleDb.class,database)).tell(request,getSelf());
                        }
                    }
                    else{
                        log().info("received wrong request type");
                    }
                })
                .match(SearchResponse.class, sr->{
                    if(sr.bookTitle==null && dbCounter>1){
                        dbCounter--;
                    }else {
                        client.tell(sr,getSelf());
                        getContext().stop(getSelf());
                    }
                })
                .matchAny(o -> log().info("received unknown message"))
                .build();
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();
        getContext().getChildren().forEach(c->getContext().stop(c));
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return new OneForOneStrategy(5, Duration.create(10, TimeUnit.SECONDS), DeciderBuilder.
                matchAny(o -> stop()).
                build());
    }

}
