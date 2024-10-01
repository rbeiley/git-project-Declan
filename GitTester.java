import java.io.File;
public class GitTester
{
    public static void main(String[] args) {
        gitCheck();
        // deleteGit();
    }
    public static boolean gitCheck ()
    {      
        File index = new File ("git/index");
        File gitDir = new File ("git");
        File objectsDir = new File ("git/objects");
        return gitDir.isDirectory() && objectsDir.isDirectory() && index.exists();
    }
    public static boolean deleteGit ()
    {
        if (gitCheck())
        {
            File index = new File ("git/index");
            File gitDir = new File ("git");
            File objectsDir = new File ("git/objects");
            index.delete();
            objectsDir.delete();
            gitDir.delete();
            System.out.println("Deleting git");
            return true;
        }
        else
        {
            System.out.println("check failed");
            return false;
        }
    }
}