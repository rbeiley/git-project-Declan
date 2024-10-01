import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
public class Blob
{
    public static boolean compress = true;
    public static void makeBlob(File file) throws IOException {
        String blobType = "";
        if (file.isFile())
            blobType += "blob ";
        else
            blobType += "tree ";
        
        //Make compressed blob file or make uncompressed blob file
        String sha1;
        File hashFile;
        if (compress)
        {
            hashFile = zipContents("git/objects", file.getPath());
            sha1 = hashFile.getName();
        }
        else
        {
            sha1 = Sha1.encryptThisString(getContents(file));
            
            //Create Blob in objects directory
            hashFile = new File("git" + File.separator + "objects" + File.separator + sha1);
            hashFile.createNewFile();
            copyFile(file, hashFile);
        }
        
        //Prevent Duplicate Index Entries + Enter new Entries into index
        String path = file.getPath();
        String indexOutput = blobType + sha1 + " " + path;
        BufferedReader reader = new BufferedReader(new FileReader("git" + File.separator + "index"));
        Boolean newBlob = true;
        while (reader.ready()) {
            if (reader.readLine().equals(indexOutput))
                newBlob = false;
        }
        reader.close();
        if (newBlob) {
            BufferedWriter writeIndex = new BufferedWriter(new FileWriter("git" + File.separator + "index", true));                writeIndex.write(indexOutput);
            writeIndex.newLine();
            writeIndex.close();
        }
    }

    public static String addTree(String path, String name) throws IOException{
        File blob = new File(path);
        if (!blob.exists())
            throw new IOException("Path doesn't exist");
        File tree = new File ("git" + File.separator + "objects" + File.separator + name);
        if (blob.isDirectory()) {
            File[] files = blob.listFiles();
            String treeIndex = "";
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                String hash = "";
                if (!file.isDirectory()) {
                    hash = Sha1.encryptThisString(tree.getName());
                    treeIndex += "blob " + hash + " " + file.getName() + "\n";
                    makeBlob(file);
                }
                else {
                    hash = addTree(file.getPath(), file.getName());
                    treeIndex += "tree " + hash + " " + file.getName() + "\n";
                }
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(tree));
            writer.write(treeIndex);
            writer.close();
            String toIndex = "tree " + Sha1.encryptThisString(tree.getName()) + " " + path + "\n";
            BufferedWriter newWriter = new BufferedWriter(new FileWriter(new File ("git" + File.separator + "index"), true));
            newWriter.write(toIndex);
            newWriter.close();
        }
        return Sha1.encryptThisString(tree.getName());
    }

    public static void copyFile(File input, File output) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(input));
        String contents = "";
        while (reader.ready()) {
            contents += reader.readLine();
        }
        reader.close();
        BufferedWriter writer = new BufferedWriter(new FileWriter(output));
        writer.write(contents);
        writer.close();
    }

    public static String getContents(File input) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(input));
        String contents = "";
        while (reader.ready()) {
            contents += reader.readLine();
        }
        reader.close();
        return contents;
    }
    //@param dirPath path to directory where new zipped file will go
    public static File zipContents(String dirPath, String filePath) {
        try {
            File file = new File(filePath);
            String placeHold = "";
            FileOutputStream fos = new FileOutputStream(placeHold);
            ZipOutputStream zos = new ZipOutputStream(fos);
 
            zos.putNextEntry(new ZipEntry(file.getName()));
 
            byte[] bytes = Files.readAllBytes(Paths.get(filePath));
            zos.write(bytes, 0, bytes.length);
            zos.closeEntry();
            zos.close();
            return new File(dirPath + File.separator + Sha1.encryptThisString(getContents(placeHold)));
        } catch (FileNotFoundException ex) {
            System.err.format("The file %s does not exist", filePath);
        } catch (IOException ex) {
            System.err.println("I/O error: " + ex);
        }
        return null;
    } 
}