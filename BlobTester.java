import java.io.BufferedWriter;
import java.io.File;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;

public class BlobTester {
    public static void main (String [] args) throws Exception {
        Git.initRepo();

        GitTester.createTestRoot();

        Blob.addTree("root", "root");
    }

    //Deletes all objects in git objects folder and clears index data
    public static void reset() {
        try {
            // reset index
            File index = new File("git/index");
            index.delete();
            index.createNewFile();

            // reset objects
            File objects = new File("git/objects");
            Git.resetRecur(objects);
            objects.mkdir();
        } catch (Exception e) {
            System.out.println("File Could Not Be Created : " + e);
        }
    }
}
