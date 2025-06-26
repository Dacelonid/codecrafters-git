package ie.dacelonid.git;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

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

}
