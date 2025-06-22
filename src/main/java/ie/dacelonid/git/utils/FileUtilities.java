package ie.dacelonid.git.utils;

import ie.dacelonid.git.ZlibHandler;
import ie.dacelonid.git.exceptions.GitCouldNotCreateDirectoryException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileUtilities {

    public static void createDirectory(File dir) throws GitCouldNotCreateDirectoryException {
        if (!dir.mkdirs())
            throw new GitCouldNotCreateDirectoryException(dir);
    }


    public static File getBlob(String[] args, File gitRootDirectory) {
        final String sha1 = args[args.length - 1];
        String dir = sha1.substring(0, 2); //Directory is first 2 characters of SHA1
        String blobname = sha1.substring(2); //Filename is the remaining SHA1
        final File blob = new File(gitRootDirectory, "objects/" + dir + "/" + blobname);
        if (!blob.exists()) {
            System.out.println("File does not exist");
        }
        return blob;
    }

    public static String getUncompressedBlobContents(File objectFile) throws Exception {
        byte[] decompressed = ZlibHandler.decompress(Files.readAllBytes(objectFile.toPath()));
        String fileContents = new String(decompressed, StandardCharsets.UTF_8);
        fileContents = fileContents.substring(fileContents.indexOf("\0") + 1);
        return fileContents;
    }

    public static void createBlob(String sha1, File gitRootDirectory, String contents) throws Exception {
        final File blob_dir = new File(gitRootDirectory, "objects/" + sha1.substring(0, 2));
        blob_dir.mkdirs();
        File blobFile = new File(blob_dir, sha1.substring(2));
        Files.write(blobFile.toPath(), ZlibHandler.compress(contents.getBytes()));
    }

    public static String sha1(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 algorithm not available", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b)); // lowercase hex
        }
        return sb.toString();
    }

    public static String getFileContentsToWriteToBlob(String[] args, Path currentDirectory) throws IOException {
        String filename = args[args.length - 1];
        String contents = Files.readString(new File(currentDirectory.toFile(), filename).toPath());
        return "blob " + contents.length() + "\0" + contents;
    }
}
