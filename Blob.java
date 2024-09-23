import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
public class Blob
{
    public Blob (File file) throws IOException
    {
        String code = getFileData(file);
        String hash = Sha1.encryptThisString(code);
        File blob = new File("git/objects/" + hash);
        blob.createNewFile();
        FileWriter writeToBlob = new FileWriter("git/objects/" + hash);
        writeToBlob.append(code);
        writeToBlob.close();
        FileWriter writeToIndex = new FileWriter("git/index", true);
        writeToIndex.append(hash + ' ' + file.getName() + '\n');
        writeToIndex.close();
    }
    public static String getFileData (File data) throws IOException
    {
        FileReader read = new FileReader(data);
        StringBuilder sb = new StringBuilder();
        while(read.ready())
        {
            sb.append((char)read.read());
        }
        read.close();
        return sb.toString();
    }
}