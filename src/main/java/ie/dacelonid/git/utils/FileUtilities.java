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

    public static String getUncompressedFileContents(File objectFile) throws Exception {
        byte[] decompressed = ZlibHandler.decompress(Files.readAllBytes(objectFile.toPath()));
        return new String(decompressed, StandardCharsets.UTF_8);
    }

    public static String computeSha1(String input) {
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
