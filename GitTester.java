import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Random;

public class GitTester {
    public static void main(String[] args) {
        try {
            // Initialize Git repository
            Git.initRepo();

            // Create test files
            createTestFiles();

            Git newGit = new Git();

            // Stage and commit initial files
            newGit.stage("root/file1.txt");
            newGit.stage("root/file2.txt");
            String initialCommitSha = newGit.commit("Author", "Initial commit");
            System.out.println("Initial commit SHA: " + initialCommitSha);

            // Print out the contents of the initial commit file
            System.out.println("\nContents of initial commit file:");
            printFileContents("git/objects/" + initialCommitSha);

            // Get the tree hash from the commit file
            String initialTreeHash = getTreeHashFromCommit("git/objects/" + initialCommitSha);

            // Print out the contents of the initial tree file
            System.out.println("\nContents of initial tree file:");
            printFileContents("git/objects/" + initialTreeHash);

            // **First Test: Deleting a File**

            // Delete file2.txt
            File fileToDelete = new File("root/file2.txt");
            if (fileToDelete.delete()) {
                System.out.println("\nDeleted file: " + fileToDelete.getPath());
            } else {
                System.out.println("\nFailed to delete file: " + fileToDelete.getPath());
            }

            // Stage the deletion
            newGit.stage("root/file2.txt");

            // Make a new commit after deletion
            String deleteCommitSha = newGit.commit("Author", "Removed file2.txt");
            System.out.println("\nCommit SHA after deletion: " + deleteCommitSha);

            // Print out the contents of the commit file after deletion
            System.out.println("\nContents of commit file after deletion:");
            printFileContents("git/objects/" + deleteCommitSha);

            // Get the tree hash from the new commit file
            String deleteTreeHash = getTreeHashFromCommit("git/objects/" + deleteCommitSha);

            // Print out the contents of the tree file after deletion
            System.out.println("\nContents of tree file after deletion:");
            printFileContents("git/objects/" + deleteTreeHash);

            // **Second Test: Editing a File**

            // Read old contents of file1.txt
            String oldContent = readFileContents("root/file1.txt");

            // Edit file1.txt by adding a new word
            File fileToEdit = new File("root/file1.txt");
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileToEdit, true)); // Append mode
            String newWord = getRandomWord();
            writer.write(" " + newWord);
            writer.close();
            System.out.println("\nEdited file: " + fileToEdit.getPath());

            // Read new contents of file1.txt
            String newContent = readFileContents("root/file1.txt");

            // Stage the edited file
            newGit.stage("root/file1.txt");

            // Make a new commit after editing
            String editCommitSha = newGit.commit("Author", "Edited file1.txt");
            System.out.println("\nCommit SHA after editing: " + editCommitSha);

            // Print out the contents of the commit file after editing
            System.out.println("\nContents of commit file after editing:");
            printFileContents("git/objects/" + editCommitSha);

            // Get the tree hash from the new commit file
            String editTreeHash = getTreeHashFromCommit("git/objects/" + editCommitSha);

            // Print out the contents of the tree file after editing
            System.out.println("\nContents of tree file after editing:");
            printFileContents("git/objects/" + editTreeHash);

            // Print old and new contents of file1.txt
            System.out.println("\nOld contents of file1.txt:");
            System.out.println(oldContent);
            System.out.println("\nNew contents of file1.txt:");
            System.out.println(newContent);

        } catch (Exception e) {
            System.out.println("An error occurred: " + e);
            e.printStackTrace();
        }
    }

    // Method to create test files
    public static void createTestFiles() throws IOException {
        File rootDir = new File("root");
        rootDir.mkdir();

        // Create file1.txt with a random word
        File file1 = new File("root/file1.txt");
        file1.createNewFile();
        String word1 = getRandomWord();
        BufferedWriter writer1 = new BufferedWriter(new FileWriter(file1));
        writer1.write(word1);
        writer1.close();

        // Create file2.txt with a random word
        File file2 = new File("root/file2.txt");
        file2.createNewFile();
        String word2 = getRandomWord();
        BufferedWriter writer2 = new BufferedWriter(new FileWriter(file2));
        writer2.write(word2);
        writer2.close();

        System.out.println("Created test files with random words.");
    }

    // Method to get a random word
    public static String getRandomWord() {
        String[] words = { "apple", "banana", "cherry", "date", "elderberry",
                "fig", "grape", "honeydew", "kiwi", "lemon" };
        Random rand = new Random();
        return words[rand.nextInt(words.length)];
    }

    // Method to print file contents
    public static void printFileContents(String filePath) throws IOException {
        // System.out.println("File: " + filePath);
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        reader.close();
    }

    // Method to read file contents
    public static String readFileContents(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        StringBuilder contents = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            contents.append(line);
        }
        reader.close();
        return contents.toString();
    }

    // Method to get tree hash from commit file
    public static String getTreeHashFromCommit(String commitFilePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(commitFilePath));
        String line;
        String treeHash = null;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("tree:\t")) {
                treeHash = line.substring(6); // skip "tree:\t"
                break;
            }
        }
        reader.close();
        if (treeHash != null) {
            return treeHash;
        } else {
            throw new IOException("Invalid commit file format: Tree hash not found");
        }
    }

    // Method to get blob hash for a file from a tree file
    public static String getBlobHashFromTree(String treeFilePath, String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(treeFilePath));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains(filePath)) {
                String[] parts = line.split(" ");
                if (parts.length >= 3 && parts[0].equals("blob")) {
                    reader.close();
                    return parts[1]; // Return the blob hash
                }
            }
        }
        reader.close();
        return null;
    }
}
