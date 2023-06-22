import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;

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

        try {
            DatagramSocket socket = new DatagramSocket();
            InetAddress serverAddress = InetAddress.getByName(serverIP);

            // Extract file name from the file path
            String filename = Path.of(filePath).getFileName().toString();

            // Send the filename as the first packet
            byte[] filenameData = filename.getBytes();
            DatagramPacket filenamePacket = new DatagramPacket(filenameData, filenameData.length, serverAddress, PORT);
            socket.send(filenamePacket);

            // Prepare file input stream
            InputStream fileInputStream = Files.newInputStream(Path.of(filePath));

            // Send file data in packets
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                DatagramPacket dataPacket = new DatagramPacket(buffer, bytesRead, serverAddress, PORT);
                socket.send(dataPacket);
            }

            // Send termination signal
            byte[] terminationSignal = {0};
            DatagramPacket terminationPacket = new DatagramPacket(terminationSignal, terminationSignal.length, serverAddress, PORT);
            socket.send(terminationPacket);

            // Receive acknowledgement from the server
            byte[] receiveBuffer = new byte[BUFFER_SIZE];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            socket.receive(receivePacket);

            String acknowledgment = new String(receivePacket.getData(), 0, receivePacket.getLength());
            System.out.println("Server acknowledgment: " + acknowledgment);

            // Close file input stream and socket
            fileInputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
