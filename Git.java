import java.io.FileWriter;
import java.io.IOException;
import java.nio.Buffer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.time.LocalDateTime;

public class Git
{
    private static File root = new File("root");
    private static ArrayList<String> entries = new ArrayList<String>();
    public static void initRepo() throws IOException {
        File git = new File("git");
        if (!git.isDirectory())
            git.mkdir();
        File objects = new File("git" + File.separator + "objects");
        if (!objects.isDirectory())
            objects.mkdir();
        File index = new File("git" + File.separator + "index");
        index.createNewFile();
        File head = new File ("git/HEAD");
        head.createNewFile();
        
        entries = convertToArray(index);
    }
    public static String commit (String author, String message) throws IOException
    {
        //tree: (ensure that a root exists as a blob already)
        String rootSha = findHashInEntries(entries, root.getPath());
        if (rootSha.equals(""))
        {
            rootSha = Blob.addTree(root.getPath(), root.getName());
        }
        
        StringBuilder sb = new StringBuilder("tree:\t"+rootSha+'\n');
        
        //parent:
        File head = new File ("git/HEAD");
        BufferedReader br = new BufferedReader(new FileReader(head));
        sb.append("parent:\t"+br.readLine()+'\n');
        br.close();

        //author:
        sb.append("author:\t" + author+'\n');

        //time:
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy");  
        LocalDateTime now = LocalDateTime.now();
        sb.append("time:\t"+dtf.format(now)+'\n');

        //message:
        sb.append("message:\t"+message);
        
        String sha = Sha1.encryptThisString(sb.toString());
        File commitFile = new File ("git/objects/"+sha);
        BufferedWriter bw = new BufferedWriter(new FileWriter(commitFile));
        bw.write(sb.toString());
        bw.close();
        
        FileWriter fwHead = new FileWriter(head);
        fwHead.append(sha);
        fwHead.close();
        return sha;
    }
    public static void stage (File stagingFile) throws IOException
    {
        if (!stagingFile.exists())
            throw new FileNotFoundException("stagingFile does not exist");
        
        String stagePath = stagingFile.getPath();
        String oldStageHash = findHashInEntries(entries, stagePath);
        String newStageHash = Sha1.encryptThisString(Blob.getContents(stagingFile));
        if (!oldStageHash.equals(newStageHash))
        {
            Blob.addTree(root.getPath(), root.getName());
        }
    }
    //Converts File to array of entries
    public static ArrayList<String> convertToArray (File file) throws IOException
    {
        BufferedReader bf = new BufferedReader(new FileReader(file));
        ArrayList<String> list = new ArrayList<String>();
        while (bf.ready())
        {
            list.add(bf.readLine());
        }
        bf.close();
        return list;
    }
    //converts arraylist of entries back into a String to be entered into a file
    public static String convertToString (ArrayList<String> list)
    {
        StringBuilder sb = new StringBuilder();
        for (String string : list) {
            sb.append(string);
        }
        return sb.toString();
    }
    //Finds the hash of a file given a path; Returns an empty string if non exists
    //@param filePath the file path that would appear in the log
    public static String findHashInEntries (ArrayList<String> entries, String filePath)
    {
        for (String string : entries) {
            if (string.indexOf(filePath)>0)
            {
                return string.substring(4, string.indexOf(' ',6));
            }
        }
        return "";
    }
    //set entries list with a newentries list
    public static void setEntries (ArrayList<String> newEntries)
    {
        entries = newEntries;
        // File index = new File("git/index");
        // if (!index.exists())
        //     throw new FileNotFoundException("index not initialized");
        // entries = convertToArray(index);
    }
    //getEntries
    public static ArrayList<String> getEntries ()
    {
        return entries;
    }
    //sets the hash of an entry given the filepath of that entry and the new hash.
    //@return boolean true if entry was successful changed, false if filepath was not found or newHash is the same as the old hash
    public static boolean setHashInEntry (String newHash, String filePath) throws IOException
    {
        for (int i = 0; i < entries.size(); i++) {
            String entryString = entries.get(i);
            String oldHash = entryString.substring(5, entryString.indexOf(' ',6));
            if (entryString.indexOf(filePath)>0 && !oldHash.equals(newHash))
            {
                entries.set(i, entryString.substring(0,5) + ' ' + newHash + ' ' +  filePath);
                updateIndex();
                return true;
            }
        }
        return false;
    }
    //updates the index based on the current state of the entries static variable
    private static void updateIndex () throws IOException
    {
        String input = convertToString(entries);
        File index = new File("git/index");
        try {
            FileWriter fw = new FileWriter(index);
            fw.append(input);
            fw.close();
        } catch (Exception e) {
            System.out.println("File Not Found : " + e);
        }
    }
}