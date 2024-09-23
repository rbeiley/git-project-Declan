import java.io.File;
import java.io.IOException;
public class BlobTester {
    public static void main(String[] args) {
        try {
            File test = new File("BlobTestFile.txt");
            Blob testBlob = new Blob(test);
            System.out.println("running code");
        } catch (Exception e) {
            System.out.println("File Not Found");
        }
    }
}
