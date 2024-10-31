import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.io.FileOutputStream;
import java.util.zip.ZipOutputStream;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Blob {
    // compress boolean is for zip compression which uses zipContents method to
    // compress files
    public static boolean compress = false;

    // Makes blob and puts the string into the index
    public static String makeBlob(File file) throws IOException {
        String blobType = "";
        if (file.isFile())
            blobType += "blob ";
        else
            blobType += "tree ";
        // Make compressed blob file or make uncompressed blob file
        String sha1 = "";
        File hashFile;
        if (compress) {
            try {
                hashFile = zipContents("git/objects", file);
                sha1 = hashFile.getName();
            } catch (Exception e) {
                System.out.println(e);
            }
        } else {
            sha1 = Sha1.encryptThisString(getContents(file));

            // Create Blob in objects directory
            hashFile = new File("git" + File.separator + "objects" + File.separator + sha1);
            hashFile.createNewFile();
            copyFile(file, hashFile);
        }

        // Prevent Duplicate Index Entries + Enter new Entries into index
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
            Git.addEntry(indexOutput);
        }
        return sha1;
    }

    // adds tree recursively using make blob method
    // @param path to object
    // @param name of object
    public static String addTree(String path, String name) throws IOException {
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
                } else {
                    hash = addTree(file.getPath(), file.getName());
                    treeData += "tree " + hash + " " + file.getPath() + "\n";
                }
            }
        }

        if (treeData.equals(""))
            treeData += getContents(blob);

        File tree;
        if (compress) {
            File tempFile = new File("tempTreeFile.txt");
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
            writer.write(treeData);
            writer.close();
            tree = zipContents("git/objects", tempFile);
            tempFile.delete();
        } else {
            tree = new File("git" + File.separator + "objects" + File.separator + Sha1.encryptThisString(treeData));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tree));
            writer.write(treeData);
            writer.close();
        }

        // Ensures no duplicates are being entered into index and adds tree entry
        String hash = Sha1.encryptThisString(treeData);
        String toIndex = "tree " + hash + " " + path + "\n";
        ArrayList<String> entries = Git.getEntries();
        String hashInEntries = Git.findHashInEntries(entries, path);
        if (hashInEntries.equals("")) {
            Git.addEntry(toIndex);
        } else if (!hashInEntries.equals(hash)) {
            Git.setHashInEntry(hash, path);
        }
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

    // @param dirPath path to directory where new zipped file will go
    public static File zipContents(String newFilePath, File ogFile) throws IOException {
        File tempFile = new File("zipTesterFileTEMP.txt");
        StringBuilder sb = new StringBuilder();
        FileReader reader = new FileReader(ogFile);
        while (reader.ready()) {
            sb.append(reader.read());
        }

        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(tempFile));
        ZipEntry e = new ZipEntry("tester.txt");
        out.putNextEntry(e);

        byte[] data = sb.toString().getBytes();
        out.write(data, 0, data.length);
        out.closeEntry();

        out.close();

        String sha1 = Sha1.encryptThisString(sb.toString());
        File blob = new File(newFilePath + File.separator + sha1);
        FileOutputStream fos = new FileOutputStream(blob);
        Files.copy(tempFile.toPath(), fos);

        tempFile.delete();
        return blob;
    }
}