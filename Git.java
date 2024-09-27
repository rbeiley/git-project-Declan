import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
public class Git
{
    //Creating git and objects directory as well as index file
    public static void main(String[] args) throws IOException{
        try {
            File git = new File("git");
            if (!git.isDirectory())
                git.mkdir();
            File objects = new File("git/objects");
            if (!objects.isDirectory())
                objects.mkdir();
            File index = new File("git/index");
            index.createNewFile();
        } catch (Exception e) {
            System.out.println("Failed to Create Git Infrastructure : " + e);
        }
    }
}