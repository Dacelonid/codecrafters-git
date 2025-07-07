package ie.dacelonid.git.plumbing;

import ie.dacelonid.git.ZlibHandler;
import ie.dacelonid.git.exceptions.GitExceptions;
import ie.dacelonid.git.utils.FileUtilities;
import ie.dacelonid.git.utils.GitTreeParser;
import ie.dacelonid.git.utils.TreeEntry;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static ie.dacelonid.git.utils.HexUtilities.bytesToHex;
import static ie.dacelonid.git.utils.HexUtilities.computeSha1;
import static ie.dacelonid.git.utils.FileUtilities.getFileContentsToWriteToBlob;

public class BlobUtils {

    private static void createBlob(File gitRootDirectory, String sha1, String contents) throws Exception {
        final File blob_dir = new File(gitRootDirectory, "objects/" + sha1.substring(0, 2));
        blob_dir.mkdirs();
        File blobFile = new File(blob_dir, sha1.substring(2));
        Files.write(blobFile.toPath(), ZlibHandler.compress(contents.getBytes()));
    }

    public static void printBlob(String fileContents) throws Exception {
        System.out.print(fileContents.substring(fileContents.indexOf("\0") + 1));
    }

    public static void printTree(byte[] full) throws Exception {
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
        return new File(gitRootDirectory, "objects/" + dir + "/" + fileName);
    }

    public static String writeBlob(String fileName, File gitRootDir, Path currentDirectory) throws Exception {
        String contentToWrite = getFileContentsToWriteToBlob(fileName, currentDirectory);
        String sha1 = computeSha1(contentToWrite);
        createBlob(gitRootDir, sha1, contentToWrite);
        return sha1;
    }

    public static List<TreeEntry> listTree(String sha1, File gitRootDir) throws Exception {
        File treeFile = getFileFromSha1Hash(gitRootDir, sha1);
        if (treeFile.exists()) {
            byte[] decompressed = ZlibHandler.decompress(Files.readAllBytes(treeFile.toPath()));
            return GitTreeParser.parseTree(decompressed);
        }
        throw new GitExceptions();
    }
}
