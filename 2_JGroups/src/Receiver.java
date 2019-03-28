import org.jgroups.*;
import org.jgroups.util.Util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class Receiver extends ReceiverAdapter {
    private final HashMap<String, Integer> localMapState;
    private final JChannel channel;

    public Receiver(DistributedMap distributedMap, JChannel channel) {
        this.localMapState = distributedMap.getLocalMap();
        this.channel = channel;
    }

    @Override
    public void receive(Message msg) {
        MapCommand receivedCommand = MapCommand.fromByteArray(msg.getRawBuffer());
        System.out.println(Color.PURPLE + "Received message " + receivedCommand.getMapOperation().toString() + " from [" + msg.getSrc() + "]" + Color.RESET);
        System.out.flush();

        switch (receivedCommand.getMapOperation()) {
            case PUT:
                localMapState.put(receivedCommand.getKey(), receivedCommand.getValue());
                break;
            case REMOVE:
                localMapState.remove(receivedCommand.getKey());
                break;
            default:
                break;
        }
    }

    // From: http://www.jgroups.org/manual/index.html#StateTransfer
    @Override
    public void getState(OutputStream output) throws Exception {
        synchronized (localMapState) {
            Util.objectToStream(localMapState, new DataOutputStream(output));
        }
    }

    @Override
    public void setState(InputStream input) throws Exception {
        Map<String, Integer> map;
        map = (HashMap<String, Integer>) Util.objectFromStream(new DataInputStream(input));
        synchronized (localMapState) {
            localMapState.clear();
            localMapState.putAll(map);
        }
    }

    @Override
    public void viewAccepted(View view) {
        System.out.println("Actual view: " + Color.BLUE + view + Color.WHITE);
        if (view instanceof MergeView) {
            ViewHandler handler = new ViewHandler(this.channel, (MergeView) view);
            handler.start();
        }
    }

    private static class ViewHandler extends Thread {
        JChannel ch;
        MergeView view;

        private ViewHandler(JChannel ch, MergeView view) {
            this.ch = ch;
            this.view = view;
        }

        public void run() {
            View tmp_view = view.getSubgroups().get(0); // picks the first
            Address local_addr = ch.getAddress();
            if (!tmp_view.getMembers().contains(local_addr)) {
                System.out.println("Not member of the new primary partition ("
                        + tmp_view + "), will re-acquire the state");
                try {
                    ch.getState(null, 20000);
                } catch (Exception ignored) {
                }
            } else {
                System.out.println("Doing nothing...");
            }
        }
    }
}
