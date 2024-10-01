import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
public class BlobTester {
    public static void main (String [] args) throws Exception {
        Git.initRepo();

        File newDirectory = new File ("root" + File.separator + "dir");
        newDirectory.mkdirs();

        File file1 = new File ("root" + File.separator + "file1.txt");
        file1.createNewFile();
        BufferedWriter writer = new BufferedWriter(new FileWriter(file1));
        writer.write("file1");
        writer.close();

        File file2 = new File ("root" + File.separator + "dir" + File.separator + "file2.txt");
        file2.createNewFile();
        BufferedWriter writer2 = new BufferedWriter(new FileWriter(file2));
        writer2.write("file2");
        writer2.close();

        Blob.addTree("root", "root");
    }
}
