package ie.dacelonid.git;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class TestUtils {


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
