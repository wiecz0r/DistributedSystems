
public class App {
    private static final String MULTICAST_IP = "230.100.214.13";
    private static final String CLUSTER_NAME = "myCluster";


    public static void main(String[] args) throws Exception {
        DistributedMap map = new DistributedMap(CLUSTER_NAME, MULTICAST_IP);
    }
}