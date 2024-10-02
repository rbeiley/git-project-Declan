import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.io.ZipOutputStream;
import java.util.zip.ZipOutputStream;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
public class Blob
{
    public static boolean compress = true;
    public static String makeBlob(File file) throws IOException {
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
        if (newBlob && blobType.equals("blob ")) {
            BufferedWriter writeIndex = new BufferedWriter(new FileWriter("git" + File.separator + "index", true));                
            writeIndex.write(indexOutput);
            writeIndex.newLine();
            writeIndex.close();
        }
        return sha1;
    }

    public static String addTree(String path, String name) throws IOException{
        File blob = new File(path);
        if (!blob.exists())
            throw new IOException("Path doesn't exist");
        String treeData = "";
        if (blob.isDirectory()) {
            File[] files = blob.listFiles();
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                String hash = "";
                if (!file.isDirectory()) {
                    hash = makeBlob(file);
                    treeData += "blob " + hash + " " + file.getPath() + "\n";
                }
                else {
                    hash = addTree(file.getPath(), file.getName());
                    treeData += "tree " + hash + " " + file.getPath() + "\n";
                }
            }
        }
        
        if (treeData.equals(""))
            treeData += getContents(blob);

        File tree = new File ("git" + File.separator + "objects" + File.separator + Sha1.encryptThisString(treeData));
        BufferedWriter writer = new BufferedWriter(new FileWriter(tree));
        writer.write(treeData);
        writer.close();
        String toIndex = "tree " + Sha1.encryptThisString(tree.getName()) + " " + path + "\n";
        BufferedWriter newWriter = new BufferedWriter(new FileWriter(new File ("git" + File.separator + "index"), true));
        newWriter.write(toIndex);
        newWriter.close();

        return tree.getName();
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
            String tempFileName = file.getName() + ".zip";
            FileOutputStream fos = new FileOutputStream(tempFileName);
            ZipOutputStream zos = new ZipOutputStream(fos);
 
            ZipEntry enter = new ZipEntry(file.getName());
            zos.putNextEntry(enter);
 
            byte[] bytes = Files.readAllBytes(Paths.get(filePath));
            zos.write(bytes, 0, bytes.length);
            zos.closeEntry();
            zos.close();

            File renamedFile = new File(tempFileName);
            File namedFile = new File (dirPath + File.separator + Sha1.encryptThisString(enter.toString()));
            System.out.println(renamedFile.renameTo(namedFile));
            return renamedFile;
        } catch (FileNotFoundException ex) {
            System.err.format("The file %s does not exist", filePath);
        } catch (IOException ex) {
            System.err.println("I/O error: " + ex);
        }
        return null;
    } 
}