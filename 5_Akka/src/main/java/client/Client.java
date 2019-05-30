package client;

import akka.actor.*;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import messaging.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Client extends AbstractLoggingActor {
    private final String SERVER_PATH = "akka.tcp://server_system@127.0.0.1:2552/user/server";
    private ActorSelection serverSelection;

    public Client() {
        this.serverSelection = getContext().actorSelection(SERVER_PATH);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, this::stringReceived)
                .match(OrderResponse.class,resp -> {
                    if(resp.result){
                        System.out.println("Book ordered!");
                    }
                    else{
                        System.out.println("Book NOT ordered! It hasn't been found in our library.");
                    }
                })
                .match(SearchResponse.class, resp -> {
                    if(resp.bookTitle!=null){
                        System.out.println(resp.bookTitle + " found! Price: " + resp.price);
                    }
                    else{
                        System.out.println("Book not found!");
                    }
                })
                .match(StreamResponse.class, resp -> System.out.println(resp.getLine()))
                .matchAny(o -> log().info("received unknown message")).build();
    }

    private void stringReceived(String string){
        String[] parts = string.split(" ");
        if(parts.length!=2){
            log().info("Wrong no of args");
        }
        switch (parts[0].toLowerCase()){
            case "order":
                serverSelection.tell(new Request(parts[1], MessageType.ORDER),getSelf());
                //System.out.println("ORDER title: " + parts[1]);
                break;
            case "search":
                serverSelection.tell(new Request(parts[1], MessageType.SEARCH),getSelf());
                //System.out.println("SEARCH for title: " + parts[1]);
                break;
            case "stream":
                serverSelection.tell(new Request(parts[1], MessageType.STREAM),getSelf());
                //System.out.println("STREAM title: " + parts[1]);
                break;
            default:
                log().info("UNKNOWN COMMAND");
                break;
        }
    }

    public static void main(String[] args) throws IOException {
        final Config config = ConfigFactory.parseFile(new File("src/main/java/client/client.conf"));
        final ActorSystem system = ActorSystem.create("client_system",config);
        final ActorRef clientActor = system.actorOf(Props.create(Client.class),"client");

        System.out.println("\nWELCOME TO LIBRARY!\nPossible commands: 'search [title]', 'order [title]', 'stream [title]'");

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            String line = br.readLine();
            clientActor.tell(line,null);
            if (line.equals("q")) {
                break;
            }
        }
        system.terminate();
    }
}
