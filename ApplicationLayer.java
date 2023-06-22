
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
public class ApplicationLayer {
    public static byte[] readFile(String filePath) throws IOException {
        File file = new File(filePath);
        byte[] fileData = new byte[(int) file.length()];

        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(fileData);
        }

        return fileData;
    }
}
