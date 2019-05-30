package server.search;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import messaging.MessageType;
import messaging.Request;
import messaging.SearchResponse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class SearchActorSingleDb extends AbstractLoggingActor {
    private String database;

    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
                .match(Request.class, request -> {
                    if(request.type.equals(MessageType.SEARCH)){
                       getSender().tell(getBooksDetails(request.title),getSelf());
                    }
                    else{
                        log().info("received unknown message");
                    }
                })
                .matchAny(o -> log().info("received unknown message"))
                .build();
    }

    private SearchResponse getBooksDetails(String title) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(database)));
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitted = line.trim().split(";");
                if (splitted[0].equals(title)) {
                    log().info("Found book!");
                        return new SearchResponse(title,Double.parseDouble(splitted[1]),splitted.length<3 ? null : splitted[2]);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new SearchResponse();
    }


    public SearchActorSingleDb(String database) {
        this.database = database;
    }
}
