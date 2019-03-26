import org.jgroups.*;
import org.jgroups.protocols.*;
import org.jgroups.protocols.pbcast.*;
import org.jgroups.stack.ProtocolStack;

import java.net.InetAddress;
import java.util.HashMap;

public class DistributedMap implements SimpleStringMap {
    private HashMap<String, Integer> localMap;
    private JChannel channel;
    private String multicastIP;

    public DistributedMap(String clusterName, String multicastIP) throws Exception {
        this.localMap = new HashMap<>();
        this.channel = new JChannel(false);
        this.multicastIP = multicastIP;
        init_channel(clusterName);
    }

    @Override
    public boolean containsKey(String key) {
        return localMap.containsKey(key);
    }

    public HashMap<String, Integer> getLocalMap() {
        return localMap;
    }

    @Override
    public Integer get(String key) {
        return localMap.get(key);
    }

    @Override
    public void put(String key, Integer value) {
        try {
            sendOperation(MapOperationEnum.PUT, key, value);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        localMap.put(key, value);
    }

    @Override
    public Integer remove(String key) {
        try {
            sendOperation(MapOperationEnum.REMOVE, key, null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return localMap.remove(key);
    }

    private void init_channel(String clusterName) throws Exception {
        System.setProperty("java.net.preferIPv4Stack", "true");

        ProtocolStack stack = new ProtocolStack();
        channel.setProtocolStack(stack);
        stack.addProtocol(new UDP().setValue("mcast_group_addr", InetAddress.getByName(this.multicastIP)))
                .addProtocol(new PING())
                .addProtocol(new MERGE3())
                .addProtocol(new FD_SOCK())
                .addProtocol(new FD_ALL()
                        .setValue("timeout", 12000)
                        .setValue("interval", 3000))
                .addProtocol(new VERIFY_SUSPECT())
                .addProtocol(new BARRIER())
                .addProtocol(new NAKACK2())
                .addProtocol(new UNICAST3())
                .addProtocol(new STABLE())
                .addProtocol(new GMS())
                .addProtocol(new UFC())
                .addProtocol(new MFC())
                .addProtocol(new FRAG2())
                .addProtocol(new STATE())
                .addProtocol(new SEQUENCER())
                .addProtocol(new FLUSH());

        stack.init();

        channel.setReceiver(new Receiver(this, channel));
        //CONNECT
        channel.connect(clusterName);
        channel.getState(null, 0);
    }

    private void sendOperation(MapOperationEnum mapOperationEnum, String key, Integer value) throws Exception {
        MapCommand mapCommand = new MapCommand(mapOperationEnum, key, value);
        byte[] byteBuffer = mapCommand.toByteArray();
        Message message = new Message(null, null, byteBuffer);
        this.channel.send(message);
    }
}
