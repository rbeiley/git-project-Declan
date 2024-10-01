import java.io.BufferedWriter;
import java.io.File;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;

public class BlobTester {
    public static void main(String[] args) {
        try {
            // Creates test file if it doesn't exist and converts it into a blob
            File test = new File("BlobTestFile.txt");
            System.out.println("New Test File Created: " + test.createNewFile());
            FileWriter write = new FileWriter("BlobTestFile.txt", true);
            String testString = "AHHHHHHHHHHHHH";
            for (int i = 0; i < testString.length(); i++) {
                write.append(testString.charAt(i));
            }
            write.close();
            Blob testBlob = new Blob(test);

            // Testing
            String encrypted = Sha1.encryptThisString(testString);
            boolean pass1 = false;
            boolean pass2 = true;

            // Testing if Index was properly created
            BufferedReader checkIndex = new BufferedReader(new FileReader("git/index"));
            while (checkIndex.ready()) {
                if (checkIndex.readLine().equals(encrypted + " " + test.getName())) {
                    System.out.println("Blob Input Correctly in Index");
                    pass1 = true;
                }
            }
            checkIndex.close();

            // Testing If Blob was Properly Created
            if (!testBlob.compress) {
                if (new File("git/objects/" + encrypted).exists())
                    System.out.println("Blob object created and named properly : " + encrypted);
                BufferedReader checkBlob = new BufferedReader(new FileReader("git/objects/" + encrypted));
                int counter = 0;
                while (checkBlob.ready()) {
                    if (checkBlob.read() != testString.charAt(counter)) {
                        System.out.println("File Contents are not equal");
                        pass2 = false;
                    }
                    counter++;
                }
                checkBlob.close();
            }
            else
            {
                System.out.println("File Is Compressed");
            }

            // Final Check
            if (pass1 && pass2)
                System.out.println("Successful Blob Test");
            else
                System.out.println("Unsuccessful Blob Test");
            reset();
        } catch (Exception e) {
            System.out.println("File Not Found");
        }
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

    //Deletes all objects in git objects folder and clears index data
    public static void reset() {
        try {
            // reset index
            File index = new File("git/index");
            index.delete();
            index.createNewFile();

            // reset objects
            File objects = new File("git/objects");
            resetRecur(objects);
            objects.mkdir();
        } catch (Exception e) {
            System.out.println("File Could Not Be Created : " + e);
        }
    }

    private static void resetRecur (File file)
    {
        if (file.isDirectory())
        {
            String [] ls = file.list();
            for (String fileName : ls) {
                resetRecur(new File(file.getPath() + "/" + fileName));
            }
        }
        file.delete();
    }
}
