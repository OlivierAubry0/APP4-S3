import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

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

    public void sendData(Path filePath, boolean simulateError) throws IOException {
        byte[] fileData = Files.readAllBytes(filePath);
        int start = 0;

        while (start < fileData.length) {
            int end = Math.min(fileData.length, start + BUFFER_SIZE);
            byte[] chunk = Arrays.copyOfRange(fileData, start, end);

            // Ajouter CRC au paquet
            byte[] chunkWithCRC = DataLinkLayer.addCRC(chunk);
            if (simulateError) {
                // Simulate error by modifying the CRC value
                chunkWithCRC[chunkWithCRC.length - 1]++; // Modify the last byte of CRC
            }

            DatagramPacket sendPacket = new DatagramPacket(chunkWithCRC, chunkWithCRC.length, serverAddress, port);
            socket.send(sendPacket);

            start = end;
        }
    }

    public void receiveData(OutputStream fileOutputStream) throws IOException {
        // Receive and write file data
        while (true) {
            byte[] receiveBuffer = new byte[BUFFER_SIZE];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            socket.receive(receivePacket);

            // Only consider the actual received data
            byte[] data = Arrays.copyOf(receiveBuffer, receivePacket.getLength());

            // Check for termination signal first
            if (data[0] == -1) {
                break;
            }

            // Then check for CRC validity
            if (!DataLinkLayer.isCRCValid(data)) {
                System.out.println("Invalid CRC in received data");
                // Handle error...
            } else {
                // Remove CRC before writing to file
                byte[] dataWithoutCRC = DataLinkLayer.removeCRC(data);
                fileOutputStream.write(dataWithoutCRC, 0, dataWithoutCRC.length);
            }
        }
    }

    public void sendTerminationSignal() throws IOException {
        byte[] terminationSignal = {-1};
        DatagramPacket terminationPacket = new DatagramPacket(terminationSignal, terminationSignal.length, serverAddress, port);
        socket.send(terminationPacket);
    }
}
