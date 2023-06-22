
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TransportLayer {
    private static final int PACKET_SIZE = 200;

    public static List<byte[]> createPackets(byte[] fileData) {
        List<byte[]> packets = new ArrayList<>();

        int fileLength = fileData.length;
        int numPackets = (int) Math.ceil((double) fileLength / PACKET_SIZE);

        for (int i = 0; i < numPackets; i++) {
            int offset = i * PACKET_SIZE;
            int length = Math.min(PACKET_SIZE, fileLength - offset);
            byte[] packet = new byte[length];
            System.arraycopy(fileData, offset, packet, 0, length);
            packets.add(packet);
        }

        return packets;
    }

    public static byte[] createHeader(int sequenceNumber, int totalPackets, String fileName) {
        String headerString = String.format("SEQ:%d TOTAL:%d FILE:%s", sequenceNumber, totalPackets, fileName);
        return headerString.getBytes();
    }

    public static int extractSequenceNumber(byte[] header) {
        String headerString = new String(header);
        String sequenceNumberString = headerString.split("SEQ:")[1].split(" ")[0];
        return Integer.parseInt(sequenceNumberString);
    }

    public static int extractTotalPackets(byte[] header) {
        String headerString = new String(header);
        String totalPacketsString = headerString.split("TOTAL:")[1].split(" ")[0];
        return Integer.parseInt(totalPacketsString);
    }

    public static String extractFileName(byte[] header) {
        String headerString = new String(header);
        return headerString.split("FILE:")[1];
    }
}
