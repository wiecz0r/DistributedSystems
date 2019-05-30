package server.order;

import akka.actor.AbstractActor;
import akka.actor.ActorNotFound;
import akka.actor.ActorRef;
import messages.MessageType;
import messages.OrderResponse;
import messages.Request;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

public class OrderManager extends AbstractActor {
    @Override
    public Receive createReceive() {
        return receiveBuilder().
                match(Request.class, r -> {
                    if (r.type.equals(MessageType.ORDER)) {
                        try {
                            ActorRef searchManager = context().system().actorSelection("user/server/search_manager").
                                    resolveOne(Duration.ofSeconds(1)).toCompletableFuture().get();
                            searchManager.tell(new Request(r.title,MessageType.SEARCH),getSelf());
                            getContext().become(receiveSearchResponse(sender()));
                        }
                        catch (Exception ex){
                            sender().tell(new OrderResponse(false),null);
                        }

                    }
                }).build();
    }

    private Receive receiveSearchResponse(ActorRef client){

    }
}
