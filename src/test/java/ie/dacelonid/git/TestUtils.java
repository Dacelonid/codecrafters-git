package ie.dacelonid.git;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
@SuppressWarnings("ResultOfMethodCallIgnored")
public class TestUtils {
    static void createBlob(File tempDir, String sha1, String actualContent) throws IOException {
        File blob = createFileForBlob(tempDir, sha1);
        String contentToWrite = "blob " + actualContent.length() + "\0" + actualContent;
        Files.write(blob.toPath(), ZlibHandler.compress(contentToWrite.getBytes()));
    }

    private static File createFileForBlob(File tempDir, String sha1) throws IOException {
        File file = new File(tempDir, ".git/objects/" + sha1.substring(0, 2));
        file.mkdirs();
        File testFile = new File(file, sha1.substring(2));
        testFile.createNewFile();
        return testFile;
    }
}
