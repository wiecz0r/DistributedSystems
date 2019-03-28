import java.io.*;

class MapCommand implements Serializable {
    private MapCommandType mapOperation;
    private String key;
    private Integer value;

    MapCommand(MapCommandType mapCommandType, String key, Integer value) {
        this.mapOperation = mapCommandType;
        this.key = key;
        this.value = value;
    }

    public MapCommandType getMapOperation() {
        return mapOperation;
    }

    public String getKey() {
        return key;
    }

    public Integer getValue() {
        return value;
    }

    public byte[] toByteArray() throws IOException {
        byte[] bytes;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(this);
            oos.flush();
            bytes = bos.toByteArray();
        }
        return bytes;
    }

    public static MapCommand fromByteArray(byte[] stream) {
        MapCommand cmd = null;

        try (ByteArrayInputStream bais = new ByteArrayInputStream(stream);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            cmd = (MapCommand) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            // Error in de-serialization
            e.printStackTrace();
        }
        return cmd;
    }
}
