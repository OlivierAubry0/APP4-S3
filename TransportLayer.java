import java.io.*;
import java.net.*;
import java.nio.file.Path;

public class TransportLayer {
    private static final int BUFFER_SIZE = 1024;
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private int port;

    public TransportLayer(DatagramSocket socket, InetAddress serverAddress, int port) {
        this.socket = socket;
        this.serverAddress = serverAddress;
        this.port = port;
    }

    public void sendData(InputStream fileInputStream) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;

        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
            DatagramPacket dataPacket = new DatagramPacket(buffer, bytesRead, serverAddress, port);
            socket.send(dataPacket);
        }
    }

    public void sendTerminationSignal() throws IOException {
        byte[] terminationSignal = {0};
        DatagramPacket terminationPacket = new DatagramPacket(terminationSignal, terminationSignal.length, serverAddress, port);
        socket.send(terminationPacket);
    }
}
