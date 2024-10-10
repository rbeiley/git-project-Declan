import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;
public class GitTester
{
    public static void main(String[] args) {
        //gitCheck();
        // deleteGit();
        try {
            Git.initRepo();
            createTestRoot();
            Git.commit("D", "HI MR LOPEZ");
            Git.commit("D Again", "COMMIT NUMBER DOS");
            File file3 = new File ("root/file3.txt");
            file3.createNewFile();
            FileWriter fw = new FileWriter(file3);
            fw.write("FILE THREE");
            fw.close();
            Git.stage(file3);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    //Checks git to make sure basic infrastructure is intact
    public static boolean gitCheck ()
    {      
        File index = new File ("git/index");
        File gitDir = new File ("git");
        File objectsDir = new File ("git/objects");
        return gitDir.isDirectory() && objectsDir.isDirectory() && index.exists();
    }
    public static boolean deleteGit ()
    {
        try {
            resetRecur(new File("git"));
            return true;
        } catch (Exception e) {
            System.out.println("Failed to Reset : " + e);
            return false;
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
    public static void createTestRoot () throws IOException
    {
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
    }
}