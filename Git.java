import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
public class Git
{
    public static void main(String[] args) throws IOException{
        mkGit();
        mkObjects();
        mkIndex();
    }
    public static void mkObjects ()
    {
        File objects = new File("git/objects");
        if (!objects.isDirectory())
            objects.mkdir();
    }
    public static void mkGit ()
    {
        File git = new File("git");
        if (!git.isDirectory())
            git.mkdir();
    }
    public static void mkIndex ()
    {
        File index = new File("git/index");
        index.createNewFile();
    }
}