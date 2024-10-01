import java.io.File;
public class GitTester
{
    public static void main(String[] args) {
        gitCheck();
        // deleteGit();
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
}