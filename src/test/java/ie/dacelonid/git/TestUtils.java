package ie.dacelonid.git;

import ie.dacelonid.git.utils.TreeEntry;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import static ie.dacelonid.git.utils.GitTreeParser.serializeTree;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class TestUtils {
    static void createBlob(File tempDir, String sha1, String actualContent) throws IOException {
        File blob = createFileForBlob(tempDir, sha1);
        String contentToWrite = "blob " + actualContent.length() + "\0" + actualContent;
        Files.write(blob.toPath(), ZlibHandler.compress(contentToWrite.getBytes()));
    }

    public static File createFileForBlob(File tempDir, String sha1) throws IOException {
        File file = new File(tempDir, ".git/objects/" + sha1.substring(0, 2));
        file.mkdirs();
        File testFile = new File(file, sha1.substring(2));
        testFile.createNewFile();
        return testFile;
    }

    public static void writeToFile(File file, String content) throws IOException {
        Files.write(file.toPath(), content.getBytes());
    }

    public static String readBlob(File tempDir, String sha1) throws Exception {
        final File root = new File(tempDir, ".git");
        final File blob = getBlob(sha1.substring(0, 2), sha1.substring(2), root);
        return getUncompressedBlobContents(blob);
    }

    public static File getBlob(String dir, String blobName, File root) {

        final File blob = new File(root, "objects/" + dir + "/" + blobName);
        if (!blob.exists()) {
            System.out.println("File does not exist");
        }
        return blob;
    }

    private static String getUncompressedBlobContents(File objectFile) throws Exception {
        byte[] decompressed = ZlibHandler.decompress(Files.readAllBytes(objectFile.toPath()));
        String fileContents = new String(decompressed, StandardCharsets.UTF_8);
        fileContents = fileContents.substring(fileContents.indexOf("\0") + 1);
        return fileContents;
    }

    public static String createTreeFile(File tempDir, String[] fileContents) throws IOException {
        String givenSha1 = "be80d6b281f79ac42fe6e632209841a2dcadb061"; //hardcoded valid sha1 hash, but isn't a hash of the actual contents of the file
        File treeFile = createFileForBlob(tempDir, givenSha1);
        Files.write(treeFile.toPath(), ZlibHandler.compress(getTreeFileContents(fileContents)));
        return givenSha1;
    }

    public static byte[] getTreeFileContents(String[] fileContents) {
        List<TreeEntry> entries = new ArrayList<>();
        String mode = "";
        for(String fileContent : fileContents) {
            if(fileContent.startsWith("dir"))
                mode = "040000";
            else
                mode = "100644";
            entries.add(new TreeEntry(mode, fileContent, new byte[20]));
        }
        return serializeTree(entries);
    }

    public static void copyGitObjectsFromResources(Path tempDir) throws IOException {
        Path gitObjectsSrc = Path.of("src/test/resources/objects");
        Path gitObjectsDest = tempDir.resolve(".git").resolve("objects");
        Files.createDirectories(gitObjectsDest);
        Files.walk(gitObjectsSrc).forEach(source -> {
            try {
                Path dest = gitObjectsDest.resolve(gitObjectsSrc.relativize(source));
                if (Files.isDirectory(source)) {
                    Files.createDirectories(dest);
                } else {
                    Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }
}
