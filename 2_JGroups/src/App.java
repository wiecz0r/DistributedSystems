import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.jgroups.util.Triple;

public class App {
    private static final String MULTICAST_IP = "230.100.120.6";
    private static final String CLUSTER_NAME = "myCluster";

    private static DistributedMap map;


    public static void main(String[] args) throws Exception {
        System.setProperty("java.net.preferIPv4Stack", "true");

        map = new DistributedMap(CLUSTER_NAME, MULTICAST_IP);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Closing program...");
            map.close();
        }));

        readCommandLoop();
    }

    private static void readCommandLoop() throws IOException {
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(isr);
        String readLine;

        while (true) {
            System.out.print("cmd> ");
            System.out.flush();
            readLine = br.readLine();
            Triple<String, String, Integer> parsedCmdTriple = ParseCommand(readLine);
            String key = parsedCmdTriple.getVal2();
            Integer val = parsedCmdTriple.getVal3();

            System.out.print(Color.YELLOW);
            switch (parsedCmdTriple.getVal1().toLowerCase()) {
                case "quit":
                case "exit":
                    map.close();
                    br.close();
                    isr.close();
                    return;
                case "put":
                    map.put(key, val);
                    System.out.printf("Put [%s : %d]%n", key, val);
                    break;
                case "get":
                    System.out.printf("Got val [%d]%n", map.get(key));
                    break;
                case "containsKey":
                    String contains = map.containsKey(key) ? "contains" : "does not contain";
                    System.out.printf("Map %s key [%s]%n", contains, key);
                    break;
                case "remove":
                    String removed = map.remove(key) != null ? "Removed" : "Key does not exist in map. Not removed";
                    System.out.printf("%s key [%s]%n", removed, key);
                    break;
                case "show":
                    System.out.println(map.toString());
                    break;
                default:
                    System.out.println("Wrong command!\npossible comands:\nput (key) (val) | get (key) | containsKey (key) | remove (key) | show | quit");
                    break;
            }
            System.out.print(Color.RESET);
        }
    }

    private static Triple<String, String, Integer> ParseCommand(String readLine) {
        String[] partsOfLine = readLine.split(" ");
        Triple<String, String, Integer> parsedCommand = new Triple<>(null, null, null);
        try {
            parsedCommand.setVal1(partsOfLine[0]);
            parsedCommand.setVal2(partsOfLine[1]);
            parsedCommand.setVal3(Integer.parseInt(partsOfLine[2]));
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException ignored) {
        }

        return parsedCommand;
    }
}