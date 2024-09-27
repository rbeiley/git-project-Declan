import java.io.*;
import java.nio.file.*;
import java.util.zip.*;
import java.io.File;
 
/**
 * This Java program compresses files in ZIP format (particularly for blobs).
 *
 * @author www.codejava.net
 */
public class ZipFile {
 
    public static File zipFile(String filePath) {
        try {
            File file = new File(filePath);
            String zipFileName = file.getName().concat(".zip");
 
            FileOutputStream fos = new FileOutputStream(zipFileName);
            ZipOutputStream zos = new ZipOutputStream(fos);
 
            zos.putNextEntry(new ZipEntry(file.getName()));
 
            byte[] bytes = Files.readAllBytes(Paths.get(filePath));
            zos.write(bytes, 0, bytes.length);
            zos.closeEntry();
            zos.close();
            return new File(zipFileName);
        } catch (FileNotFoundException ex) {
            System.err.format("The file %s does not exist", filePath);
        } catch (IOException ex) {
            System.err.println("I/O error: " + ex);
        }
        return null;
    }
}
