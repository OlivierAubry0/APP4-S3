import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class MainClient {
    private static final int BUFFER_SIZE = 1024;
    private static final int PORT = 25000;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java MainClient <server_ip> <file_path>");
            return;
        }

        String serverIP = args[0];
        String filePath = args[1];
        Boolean error = Boolean.valueOf(args[2]);

        try {
            DatagramSocket socket = new DatagramSocket();
            InetAddress serverAddress = InetAddress.getByName(serverIP);

            // Extract file name from the file path
            String filename = Path.of(filePath).getFileName().toString();

            // Send the filename as the first packet
            byte[] filenameData = filename.getBytes();
            DatagramPacket filenamePacket = new DatagramPacket(filenameData, filenameData.length, serverAddress, PORT);
            socket.send(filenamePacket);

            TransportLayer transportLayer = new TransportLayer(socket, serverAddress, PORT);
            transportLayer.sendData(Path.of(filePath), error);
            transportLayer.sendTerminationSignal();

            // Receive acknowledgement from the server
            byte[] receiveBuffer = new byte[BUFFER_SIZE];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            socket.receive(receivePacket);

            // Only consider the actual received data
            byte[] ackData = Arrays.copyOf(receivePacket.getData(), receivePacket.getLength());

            System.out.println("Client received acknowledgment: " + Arrays.toString(ackData));

            if (!DataLinkLayer.isCRCValid(ackData)) {
                System.out.println("Invalid CRC in acknowledgment");
                DataLinkLayer.writeLog("Invalid CRC in received data from server: " + serverIP + ":" + PORT);
            } else {
                String acknowledgment = new String(DataLinkLayer.removeCRC(ackData));
                System.out.println("Server acknowledgment: " + acknowledgment);
            }

            // Close file input stream and socket
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
