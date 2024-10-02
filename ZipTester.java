import java.io.*;
import java.lang.*;
import java.util.zip.*;
import java.nio.*;
import java.nio.file.Paths;
import java.nio.file.Files;
public class ZipTester {
    public static void main(String[] args) {
        try {
            File testFile = new File ("TestFile.txt");
            testZip("git/objects",testFile);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }
    public static String testZip (String newFilePath, File ogFile) throws IOException
    {
        File tempFile = new File("zipTesterFileTEMP.txt");
        StringBuilder sb = new StringBuilder();
        FileReader reader = new FileReader(ogFile);
        while (reader.ready())
        {
            sb.append(reader.read());
        }
        
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(tempFile));
        ZipEntry e = new ZipEntry("tester.txt");
        out.putNextEntry(e);

        byte[] data = sb.toString().getBytes();
        out.write(data, 0, data.length);
        out.closeEntry();

        out.close();

        String sha1 = Sha1.encryptThisString(sb.toString());
        File blob = new File (newFilePath + File.separator + sha1);
        FileOutputStream fos = new FileOutputStream(blob);
        Files.copy(tempFile.toPath(), fos);
        tempFile.delete();
        return sha1;
    }
}
