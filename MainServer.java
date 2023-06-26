import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class MainServer {
    private static final int BUFFER_SIZE = 1024;
    private static final int PORT = 25000;

    public static void main(String[] args) {
        try {
            DatagramSocket socket = new DatagramSocket(PORT);
            System.out.println("Server started and listening on port " + PORT);

            while (true) {
                byte[] receiveBuffer = new byte[BUFFER_SIZE];
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(receivePacket);

                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();
                System.out.println("Received connection from client: " + clientAddress + ":" + clientPort);

                // Extract filename from the first packet
                String filename = new String(receivePacket.getData(), 0, receivePacket.getLength());
                System.out.println("Receiving file: " + filename);

                // Prepare file output stream
                Path filePath = Path.of(filename);
                OutputStream fileOutputStream = Files.newOutputStream(filePath, StandardOpenOption.CREATE);

// Receive and write file data
                while (true) {
                    socket.receive(receivePacket);
                    byte[] data = receivePacket.getData();
                    System.out.println("Received data: " + new String(data, 0, receivePacket.getLength()));



                    // Write packet data to file
                    fileOutputStream.write(data, 0, receivePacket.getLength());
                    // Check for termination signal
                    if (data[0] == 0) {
                        System.out.println("File received successfully.");
                        break;
                    }

                }

// Close file output stream
                fileOutputStream.close();

// Send acknowledgement to the client
                byte[] ackData = "File received successfully.".getBytes();
                DatagramPacket ackPacket = new DatagramPacket(ackData, ackData.length, clientAddress, clientPort);
                socket.send(ackPacket);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

