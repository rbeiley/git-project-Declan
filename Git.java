import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
public class Git
{
    public static void main(String[] args) throws IOException{
        File git = new File("git");
        if (!git.isDirectory())
            git.mkdir();
        File objects = new File("git/objects");
        if (!objects.isDirectory())
            objects.mkdir();
        File index = new File("git","index");
        index.createNewFile();
    }
}