package ie.dacelonid.git.utils;

import ie.dacelonid.git.ZlibHandler;
import ie.dacelonid.git.exceptions.GitCouldNotCreateDirectoryException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.DeflaterOutputStream;

import static ie.dacelonid.git.plumbing.BlobUtils.getFileFromSha1Hash;

public class FileUtilities {

    public static void createDirectory(File dir) throws GitCouldNotCreateDirectoryException {
        if (!dir.mkdirs())
            throw new GitCouldNotCreateDirectoryException(dir);
    }

    public static byte[] getUncompressedFileContents(File objectFile) throws Exception {
        return ZlibHandler.decompress(Files.readAllBytes(objectFile.toPath()));
    }

    public static String getFileContentsToWriteToBlob(String filename, Path currentDirectory) throws IOException {
        String contents = Files.readString(new File(currentDirectory.toFile(), filename).toPath());
        return "blob " + contents.length() + "\0" + contents;
    }

    public static void writeObject(File gitDir, String sha1, byte[] data) throws IOException, GitCouldNotCreateDirectoryException {
        String dir = sha1.substring(0, 2);
        File objectDir = new File(gitDir, "objects/" + dir);
        File objectFile = getFileFromSha1Hash(gitDir, sha1);
        if (!objectFile.exists()) {
            createDirectory(objectDir);
            try (OutputStream out = new DeflaterOutputStream(new FileOutputStream(objectFile))) {
                out.write(data);
            }
        }
    }
}
