import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

import java.io.InputStream;
import java.io.OutputStream;

public class Receiver extends ReceiverAdapter {
    private final DistributedMap distributedMap;
    private final JChannel channel;

    public Receiver(DistributedMap distributedMap, JChannel channel) {
        this.distributedMap = distributedMap;
        this.channel = channel;
    }

    @Override
    public void receive(Message msg) {
        MapCommand receivedCommand = MapCommand.fromByteArray(msg.getRawBuffer());
        System.out.println("Received message [" + msg.getSrc() + "] : " + receivedCommand);

        switch (receivedCommand.getMapOperation()) {
            case PUT:
                distributedMap.getLocalMap().put(receivedCommand.getKey(), receivedCommand.getValue());
                break;
            case REMOVE:
                distributedMap.getLocalMap().remove(receivedCommand.getKey());
                break;
            default:
                break;
        }
    }

    @Override
    public void getState(OutputStream output) throws Exception {
        super.getState(output);
    }

    @Override
    public void setState(InputStream input) throws Exception {
        super.setState(input);
    }

    @Override
    public void viewAccepted(View view) {
        super.viewAccepted(view);
    }
}
