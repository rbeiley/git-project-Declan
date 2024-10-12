import java.io.FileWriter;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.file.Files;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.nio.file.Paths;

public class Git implements GitInterface 
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
    public String commit (String author, String message) throws IOException
    {
        try {
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
        } catch (Exception e) {
            throw new IOException();
        }
    }
    public void stage (String filePath) throws IOException
    {
        File stagingFile = new File(filePath);
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
    
    public void checkout(String commitHash) throws IOException
    {
        File commitFile = new File ("git/objects/" + commitHash);
        if (!commitFile.exists())
            throw new NullPointerException("commitHash points to nothing");
        BufferedReader bf = new BufferedReader(new FileReader(commitFile));

        String treeHash;
        String treeLine = bf.readLine();
        bf.close();
        if (treeLine.indexOf("tree:\t")>-1)
            treeHash = treeLine.substring(treeLine.indexOf('\t')+1);
        else
            throw new IOException("improper commit format");

        resetRecur(root);
        ArrayList<String> newIndexList = construct(new File("git/objects/" + treeHash),root.getPath());
        setEntries(newIndexList);
    }
    //recursively constructs Root and components from a tree file and updates index
    //@param pathToRoot path the root directory that will hold the root directory
    private ArrayList<String> construct (File treeFile, String pathToRoot) throws IOException
    {
        if (!treeFile.exists())
            throw new FileNotFoundException("treeFile not found");
        ArrayList<String> newIndex = new ArrayList<String>();
        File newFile = new File(pathToRoot);
        newFile.mkdir();
        newIndex.add("tree " + treeFile.getName() + ' ' + pathToRoot);
        ArrayList<String> treeEntries = convertToArray(treeFile);
        for (String string : treeEntries) {
            String path = string.substring(string.indexOf(' ', 6) + 1);
            String hash = string.substring(5, string.indexOf(' ',6));
            if (string.substring(0,4).equals("blob"))
            {
                Files.copy(new File ("git/objects/" + hash).toPath(), new FileOutputStream(new File(path)));
                newIndex.add("blob " + treeFile.getName() + ' ' + path);
            }
            else
            {
                File inputFile = new File("git/objects/" + hash);
                if (!inputFile.exists())
                    throw new FileNotFoundException("inputFile not found");
                newIndex.addAll(construct(inputFile, path));
            }
        }
        return newIndex;
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
            if (string.indexOf('\n')>-1)
                sb.append(string);
            else
                sb.append(string+'\n');
        }
        return sb.toString();
    }
    //Finds the hash of a file given a path; Returns an empty string if non exists
    //@param filePath the file path that would appear in the log for the hashed file
    public static String findHashInEntries (ArrayList<String> list, String filePath)
    {
        for (String string : list) {
            int indexOfFilePath = string.indexOf(filePath);
            if (indexOfFilePath>0 && indexOfFilePath + filePath.length() == string.length())
            {
                return string.substring(5, string.indexOf(' ',6));
            }
        }
        return "";
    }
    //set entries list with a newentries list
    public static void setEntries (ArrayList<String> newEntries)
    {
        try {
            entries = newEntries;
            updateIndex();
        } catch (Exception e) {
            // TODO: handle exception
        }
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
            int indexOfFilePath = entryString.indexOf(filePath);
            if (indexOfFilePath>0 && !oldHash.equals(newHash) && indexOfFilePath + filePath.length()==entryString.length())
            {
                entries.set(i, entryString.substring(0,5) + newHash + ' ' +  filePath);
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
    //Adds entry to entries list ensuring entry doesn't have a newline character
    public static void addEntry (String entryString) throws IOException
    {
        if (entryString.indexOf('\n')>-1)
            entries.add(entryString.substring(0, entryString.length()-1));
        else
            entries.add(entryString);
        updateIndex();
    }
    public static void resetRecur (File file)
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