package pl.edu.agh.dsrg.sr;

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
    private String clusterName;

    public DistributedMap(String clusterName, String multicastIP) throws Exception {
        this.localMap = new HashMap<>();
        this.channel = new JChannel(false);
        this.multicastIP = multicastIP;
        this.clusterName = clusterName;
        init_channel();
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
            sendOperation(MapCommandType.PUT, key, value);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        localMap.put(key, value);
    }

    @Override
    public Integer remove(String key) {
        try {
            sendOperation(MapCommandType.REMOVE, key, null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return localMap.remove(key);
    }

    public void close() {
        this.channel.close();
    }

    private void init_channel() throws Exception {
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
                .addProtocol(new FLUSH());

        stack.init();

        channel.setReceiver(new Receiver(this, channel));
        //CONNECT
        channel.connect(this.clusterName);
        channel.getState(null, 0);
    }

    private void sendOperation(MapCommandType mapCommandType, String key, Integer value) throws Exception {
        MapCommand mapCommand = new MapCommand(mapCommandType, key, value);
        byte[] byteBuffer = mapCommand.toByteArray();
        Message message = new Message(null, null, byteBuffer);
        this.channel.send(message);
    }

    @Override
    public String toString() {
        if (localMap.keySet().size() == 0) {
            return "{}";
        }
        StringBuilder mapAsString = new StringBuilder("{");
        for (String key : localMap.keySet()) {
            mapAsString.append(String.format("[%s : %d], ", key, localMap.get(key)));
        }
        mapAsString.delete(mapAsString.length() - 2, mapAsString.length()).append("}");
        return mapAsString.toString();
    }
}
