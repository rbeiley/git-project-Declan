import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
public class Git
{
    public static void initRepo() throws IOException {
        File git = new File("git");
        if (!git.isDirectory())
            git.mkdir();
        File objects = new File("git" + File.separator + "objects");
        if (!objects.isDirectory())
            objects.mkdir();
        File index = new File("git" + File.separator + "index");
        index.createNewFile();
    }
}