package server;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Server {
    public static void main(String[] args) throws IOException {
        File configFile = new File("src/main/java/server/server.conf");
        System.out.println(configFile.getAbsolutePath());
        Config config = ConfigFactory.parseFile(configFile);
        final ActorSystem system = ActorSystem.create("server_system",config);
        final ActorRef serverActor = system.actorOf(Props.create(ServerActor.class),"server");

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            String line = br.readLine();
            if (line.equals("q")) {
                break;
            }
            serverActor.tell(line,null);
        }
        system.terminate();
    }
}
