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
            newGit.stage("root/subdir/file3.txt");
            newGit.stage("root/subdir/file4.txt");
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

            // **Print contents of root/subdir tree**
            String initialSubdirTreeHash = getSubdirTreeHash("git/objects/" + initialTreeHash, "root/subdir");
            if (initialSubdirTreeHash != null) {
                System.out.println("\nContents of root/subdir tree file:");
                printFileContents("git/objects/" + initialSubdirTreeHash);
            }

            // **First Test: Deleting Files**

            // Delete file2.txt
            File fileToDelete1 = new File("root/file2.txt");
            if (fileToDelete1.delete()) {
                System.out.println("\nDeleted file: " + fileToDelete1.getPath());
            } else {
                System.out.println("\nFailed to delete file: " + fileToDelete1.getPath());
            }

            // Delete file4.txt
            File fileToDelete2 = new File("root/subdir/file4.txt");
            if (fileToDelete2.delete()) {
                System.out.println("Deleted file: " + fileToDelete2.getPath());
            } else {
                System.out.println("Failed to delete file: " + fileToDelete2.getPath());
            }

            // Stage the deletions
            newGit.stage("root/file2.txt");
            newGit.stage("root/subdir/file4.txt");

            // Make a new commit after deletion
            String deleteCommitSha = newGit.commit("Author", "Removed file2.txt and file4.txt");
            System.out.println("\nCommit SHA after deletion: " + deleteCommitSha);

            // Print out the contents of the commit file after deletion
            System.out.println("\nContents of commit file after deletion:");
            printFileContents("git/objects/" + deleteCommitSha);

            // Get the tree hash from the new commit file
            String deleteTreeHash = getTreeHashFromCommit("git/objects/" + deleteCommitSha);

            // Print out the contents of the tree file after deletion
            System.out.println("\nContents of tree file after deletion:");
            printFileContents("git/objects/" + deleteTreeHash);

            // **Print contents of root/subdir tree after deletion**
            String deleteSubdirTreeHash = getSubdirTreeHash("git/objects/" + deleteTreeHash, "root/subdir");
            if (deleteSubdirTreeHash != null) {
                System.out.println("\nContents of root/subdir tree file after deletion:");
                printFileContents("git/objects/" + deleteSubdirTreeHash);
            }

            // **Second Test: Editing Files**

            // Read old contents of file1.txt and file3.txt
            String oldContentFile1 = readFileContents("root/file1.txt");
            String oldContentFile3 = readFileContents("root/subdir/file3.txt");

            // Edit file1.txt by adding a new word
            File fileToEdit1 = new File("root/file1.txt");
            BufferedWriter writer1 = new BufferedWriter(new FileWriter(fileToEdit1, true)); // Append mode
            String newWord1 = getRandomWord();
            writer1.write(" " + newWord1);
            writer1.close();
            System.out.println("\nEdited file: " + fileToEdit1.getPath());

            // Edit file3.txt by adding a new word
            File fileToEdit3 = new File("root/subdir/file3.txt");
            BufferedWriter writer3 = new BufferedWriter(new FileWriter(fileToEdit3, true)); // Append mode
            String newWord3 = getRandomWord();
            writer3.write(" " + newWord3);
            writer3.close();
            System.out.println("Edited file: " + fileToEdit3.getPath());

            // Read new contents of file1.txt and file3.txt
            String newContentFile1 = readFileContents("root/file1.txt");
            String newContentFile3 = readFileContents("root/subdir/file3.txt");

            // Stage the edited files
            newGit.stage("root/file1.txt");
            newGit.stage("root/subdir/file3.txt");

            // Make a new commit after editing
            String editCommitSha = newGit.commit("Author", "Edited file1.txt and file3.txt");
            System.out.println("\nCommit SHA after editing: " + editCommitSha);

            // Print out the contents of the commit file after editing
            System.out.println("\nContents of commit file after editing:");
            printFileContents("git/objects/" + editCommitSha);

            // Get the tree hash from the new commit file
            String editTreeHash = getTreeHashFromCommit("git/objects/" + editCommitSha);

            // Print out the contents of the tree file after editing
            System.out.println("\nContents of tree file after editing:");
            printFileContents("git/objects/" + editTreeHash);

            // **Print contents of root/subdir tree after editing**
            String editSubdirTreeHash = getSubdirTreeHash("git/objects/" + editTreeHash, "root/subdir");
            if (editSubdirTreeHash != null) {
                System.out.println("\nContents of root/subdir tree file after editing:");
                printFileContents("git/objects/" + editSubdirTreeHash);
            }

            // Print old and new contents of file1.txt and file3.txt
            System.out.println("\nOld contents of file1.txt:");
            System.out.println(oldContentFile1);
            System.out.println("\nNew contents of file1.txt:");
            System.out.println(newContentFile1);

            System.out.println("\nOld contents of file3.txt:");
            System.out.println(oldContentFile3);
            System.out.println("\nNew contents of file3.txt:");
            System.out.println(newContentFile3);

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

        // Create subdir inside root
        File subDir = new File("root/subdir");
        subDir.mkdir();

        // Create file3.txt inside subdir with a random word
        File file3 = new File("root/subdir/file3.txt");
        file3.createNewFile();
        String word3 = getRandomWord();
        BufferedWriter writer3 = new BufferedWriter(new FileWriter(file3));
        writer3.write(word3);
        writer3.close();

        // Create file4.txt inside subdir with a random word
        File file4 = new File("root/subdir/file4.txt");
        file4.createNewFile();
        String word4 = getRandomWord();
        BufferedWriter writer4 = new BufferedWriter(new FileWriter(file4));
        writer4.write(word4);
        writer4.close();

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

    // Method to get the hash of root/subdir tree from root tree file
    public static String getSubdirTreeHash(String rootTreeFilePath, String subdirPath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(rootTreeFilePath));
        String line;
        String subdirTreeHash = null;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("tree ") && line.contains(subdirPath)) {
                String[] parts = line.split(" ");
                if (parts.length >= 3 && parts[2].equals(subdirPath)) {
                    subdirTreeHash = parts[1]; // The hash is the second part
                    break;
                }
            }
        }
        reader.close();
        return subdirTreeHash;
    }
}
