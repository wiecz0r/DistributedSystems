package server.order;

import akka.NotUsed;
import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.stream.ActorMaterializer;
import akka.stream.IOResult;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import akka.util.Timeout;
import messaging.MessageType;
import messaging.OrderResponse;
import messaging.Request;
import messaging.SearchResponse;
import server.ServerActor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

public class OrderActor extends AbstractLoggingActor {

    private ActorRef client = null;

    @Override
    public AbstractLoggingActor.Receive createReceive() {
        return receiveBuilder().
                match(Request.class, r -> {
                    client = sender();
                    if (r.type.equals(MessageType.ORDER)) {
                        try {
                            ActorSelection searchManager = context().system().actorSelection("user/server/search_manager");
                            searchManager.tell(new Request(r.title, MessageType.SEARCH), getSelf());
                        } catch (Exception ex) {
                            client.tell(new OrderResponse(false), getSelf());
                            context().stop(getSelf());
                        }

                    }
                    else{
                        log().info("Wrong request type");
                    }
                })
                .match(SearchResponse.class, sr -> {
                    if(sr.bookTitle != null){
                        saveOrder(sr.bookTitle);

                        client.tell(new OrderResponse(true),getSelf());
                        context().stop(getSelf());
                    }
                    else{
                        client.tell(new OrderResponse(false), getSelf());
                        context().stop(self());
                    }
                })
                .matchAny(o -> log().info("received unknown message"))
                .build();
    }

    private void saveOrder(String bookTitle) {
            Sink<ByteString, CompletionStage<IOResult>> sink = FileIO.toPath(Paths.get(ServerActor.ORDERS_FILE),
                    Collections.singleton(StandardOpenOption.APPEND));
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            Source<ByteString, NotUsed> source = Source.single(ByteString.fromString(timestamp + " - " + bookTitle + "\n"));
            ActorMaterializer mat = ActorMaterializer.create(getContext().getSystem());
            source.runWith(sink, mat);
        }


//        File file = new File(ServerActor.ORDERS_FILE);
//        try {
//            FileWriter fr = new FileWriter(file, true);
//            fr.write(bookTitle + "\n");
//            fr.close();
//        }
//        catch (IOException ex){
//            ex.printStackTrace();
//        }
//    }


}
