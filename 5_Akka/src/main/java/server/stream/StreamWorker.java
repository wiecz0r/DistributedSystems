package server.stream;

import akka.Done;
import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.japi.pf.ReceiveBuilder;
import akka.stream.ActorMaterializer;
import akka.stream.IOResult;
import akka.stream.ThrottleMode;
import akka.stream.javadsl.*;
import akka.util.ByteString;
import messaging.*;
import scala.concurrent.duration.FiniteDuration;

import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

public class StreamWorker extends AbstractLoggingActor {
    private ActorRef client = null;

    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
                .match(Request.class, r -> {
                    client = sender();
                    if (r.type.equals(MessageType.STREAM)) {
                        try {
                            ActorSelection searchManager = context().system().actorSelection("user/server/search_manager");
                            searchManager.tell(new Request(r.title, MessageType.SEARCH), getSelf());
                        } catch (Exception ex) {
                            client.tell(new StreamResponse("Failure"), getSelf());
                            context().stop(getSelf());
                        }

                    }
                    else{
                        log().info("Wrong request type");
                    }
                })
                .match(SearchResponse.class, sr -> {
                    if(sr.bookTitle != null){
                        if(sr.filePath == null){
                            client.tell(new StreamResponse("Book text not found"),null);
                        }
                        else {
                            readLinesAndSendToClient(sr.filePath);
                        }
                    }
                    else{
                        client.tell(new StreamResponse("Book not found!"), getSelf());
                        context().stop(self());
                    }
                })
                .matchAny(o -> log().info("received unknown message"))
                .build();
    }


    private void readLinesAndSendToClient(String path){
        Source<String, CompletionStage<IOResult>> source = FileIO.fromPath(Paths.get(path))
                .via(Framing.delimiter(ByteString.fromString("\n"), 1024, FramingTruncation.ALLOW))
                .throttle(1, FiniteDuration
                        .apply(1, TimeUnit.SECONDS),1, ThrottleMode.shaping())
                .map(ByteString::utf8String);
        ActorMaterializer mat = ActorMaterializer.create(getContext().getSystem());
        Sink<String, CompletionStage<Done>> sink =
                Sink.foreach(line -> client.tell(new StreamResponse(line),null));
        source.runWith(sink, mat);
    }

}
