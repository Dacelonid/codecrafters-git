package ie.dacelonid.git.plumbing;

import ie.dacelonid.git.ZlibHandler;
import ie.dacelonid.git.utils.FileUtilities;
import ie.dacelonid.git.utils.GitTreeParser;
import ie.dacelonid.git.utils.TreeEntry;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static ie.dacelonid.git.utils.FileUtilities.getFileContentsToWriteToBlob;
import static ie.dacelonid.git.utils.HexUtilities.computeSha1;

public class BlobUtils {

    private static void createBlob(File gitRootDirectory, String sha1, String contents) throws Exception {
        final File blob_dir = new File(gitRootDirectory, "objects/" + sha1.substring(0, 2));
        blob_dir.mkdirs();
        File blobFile = new File(blob_dir, sha1.substring(2));
        Files.write(blobFile.toPath(), ZlibHandler.compress(contents.getBytes()));
    }

    public static void printBlob(String objectId, File gitRootDirectory) throws Exception {
        String contents = getFileContents(objectId, gitRootDirectory);
        System.out.print(contents.substring(contents.indexOf("\0") + 1));
    }

    private static String getFileContents(String objectId, File gitRootDirectory) throws Exception {
        return new String(getBlobContents(objectId, gitRootDirectory), StandardCharsets.UTF_8);
    }

    public static void printTree(String objectId, File gitRootDirectory) throws Exception {
        byte[] full = getBlobContents(objectId, gitRootDirectory);
        List<TreeEntry> treeEntries = GitTreeParser.parseTree(full);
        treeEntries.forEach(System.out::println);
    }

    public static byte[] getBlobContents(String sha1, File gitRootDir) throws Exception {
        final File blob = getFileFromSha1Hash(gitRootDir, sha1);
        return FileUtilities.getUncompressedFileContents(blob);
    }

    public static File getFileFromSha1Hash(File gitRootDirectory, String sha1) {
        String dir = sha1.substring(0, 2); //Directory is first 2 characters of SHA1
        String fileName = sha1.substring(2); //Filename is the remaining SHA1

        if (new File(gitRootDirectory, "objects/" + dir).exists()) {
            for (File file : Objects.requireNonNull(new File(gitRootDirectory, "objects/" + dir).listFiles())) {
                if (file.getName().startsWith(fileName)) {
                    return file;
                }
            }
        }
        return new File(gitRootDirectory, "objects/" + dir + "/" + fileName); //doesn't exist not very good handling
    }

    public static String writeBlob(String fileName, File gitRootDir, Path currentDirectory) throws Exception {
        String contentToWrite = getFileContentsToWriteToBlob(fileName, currentDirectory);
        String sha1 = computeSha1(contentToWrite);
        createBlob(gitRootDir, sha1, contentToWrite);
        return sha1;
    }

    public static List<TreeEntry> listTree(String sha1, File gitRootDir) throws Exception {
        return GitTreeParser.parseTree(getBlobContents(sha1, gitRootDir));
    }

    public static String getTypeFromSha1Hash(String objectId, File gitRootDirectory) throws Exception {
        String contents = getFileContents(objectId, gitRootDirectory);
        return contents.split("\0")[0].split(" ")[0];
    }

    public static String getSizeFromSha1Hash(String objectId, File gitRootDirectory) throws Exception {
        String contents = getFileContents(objectId, gitRootDirectory);
        return contents.split("\0")[0].split(" ")[1];
    }


}
