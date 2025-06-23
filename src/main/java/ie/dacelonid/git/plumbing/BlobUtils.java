package ie.dacelonid.git.plumbing;

import ie.dacelonid.git.ZlibHandler;
import ie.dacelonid.git.utils.FileUtilities;
import ie.dacelonid.git.utils.GitTreeParser;
import ie.dacelonid.git.utils.TreeEntry;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static ie.dacelonid.git.utils.FileUtilities.computeSha1;
import static ie.dacelonid.git.utils.FileUtilities.getFileContentsToWriteToBlob;

public class BlobUtils {

    public static void createBlob(File gitRootDirectory, String sha1, String contents) throws Exception {
        final File blob_dir = new File(gitRootDirectory, "objects/" + sha1.substring(0, 2));
        blob_dir.mkdirs();
        File blobFile = new File(blob_dir, sha1.substring(2));
        Files.write(blobFile.toPath(), ZlibHandler.compress(contents.getBytes()));
    }

    public static void printBlob(String sha1, File gitRootDir) throws Exception {
        final File blob = getFileFromSha1Hash(gitRootDir, sha1);
        String fileContents = FileUtilities.getUncompressedFileContents(blob);
        System.out.print(fileContents.substring(fileContents.indexOf("\0") + 1));
    }

    public static File getFileFromSha1Hash(File gitRootDirectory, String sha1) {
        String dir = sha1.substring(0, 2); //Directory is first 2 characters of SHA1
        String fileName = sha1.substring(2); //Filename is the remaining SHA1
        final File file = new File(gitRootDirectory, "objects/" + dir + "/" + fileName);
        if (!file.exists()) {
            System.out.println("File does not exist");
        }
        return file;
    }

    public static void writeBlob(String fileName, File gitRootDir, Path currentDirectory) throws Exception {
        String contentToWrite = getFileContentsToWriteToBlob(fileName, currentDirectory);
        String sha1 = computeSha1(contentToWrite);
        createBlob(gitRootDir, sha1, contentToWrite);
        System.out.print(sha1);
    }

    public static void listTree(String sha1, File gitRootDir) throws Exception {
        File treeFile = getFileFromSha1Hash(gitRootDir, sha1);
        if(treeFile.exists()) {
            byte[] decompressed = ZlibHandler.decompress(Files.readAllBytes(treeFile.toPath()));
            List<TreeEntry> entries = GitTreeParser.parseTree(decompressed);
            entries.forEach(s -> System.out.println(s.name()) );
        }
    }
}
