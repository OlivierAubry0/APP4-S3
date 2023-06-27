import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class DataLinkLayer {
    private static final int CRC_POLYNOMIAL = 0xEDB88320;
    private static final int HEADER_SIZE = 20;

    public static byte[] addCRC(byte[] data) {
        int crc = calculateCRC(data);
        ByteBuffer buffer = ByteBuffer.allocate(data.length + 4);
        buffer.put(data);
        buffer.putInt(crc);
        return buffer.array();
    }

    private static int calculateCRC(byte[] data) {
        int crc = 0xFFFFFFFF;
        for (byte b : data) {
            crc ^= b;
            for (int i = 0; i < 8; i++) {
                if ((crc & 1) == 1) {
                    crc = (crc >>> 1) ^ CRC_POLYNOMIAL;
                } else {
                    crc >>>= 1;
                }
            }
        }
        return crc;
    }

    public static byte[] removeCRC(byte[] data) {
        return Arrays.copyOfRange(data, 0, data.length - 4);
    }

    public static boolean isCRCValid(byte[] data) {
        byte[] dataWithoutCRC = removeCRC(data);
        int originalCRC = ByteBuffer.wrap(data, dataWithoutCRC.length, 4).getInt();
        int calculatedCRC = calculateCRC(dataWithoutCRC);
        return originalCRC == calculatedCRC;
    }


    public static void writeLog(String logMessage) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("liasonDeDonnes.log", true))) {
            writer.println(logMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
