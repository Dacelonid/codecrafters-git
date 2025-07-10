package ie.dacelonid.git.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import ie.dacelonid.git.plumbing.objects.TreeObject;
import ie.dacelonid.git.plumbing.objects.BlobObject;
import ie.dacelonid.git.plumbing.objects.GitObject;

import static ie.dacelonid.git.plumbing.BlobUtils.writeBlob;
import static ie.dacelonid.git.utils.HexUtilities.bytesToHex;
import static ie.dacelonid.git.utils.HexUtilities.computeSha1;
import static ie.dacelonid.git.utils.FileUtilities.writeObject;


public class TreeUtilities {

    public static String writeTree(File gitDirectory, File dirOrFile) throws Exception {
        List<GitObject> objects = getAllFilesAndDirs(gitDirectory, dirOrFile);
        return bytesToHex(objects.getFirst().getSha1());
    }

    private static List<GitObject> getAllFilesAndDirs(File gitDirectory, File dirOrFile) throws Exception {
        List<GitObject> objects = new ArrayList<>();
        if (dirOrFile.isDirectory()) {
            File[] children = dirOrFile.listFiles();
            if (children != null && !dirOrFile.equals(gitDirectory)) {
                for (File child : children) {
                    objects.addAll(getAllFilesAndDirs(gitDirectory, child));
                }
                String sha1 = writeTree(gitDirectory, objects);
                return List.of(new TreeObject("040000", dirOrFile.getName(), HexUtilities.hexToBytes(sha1)));
            }
        } else {
            String sha1 = writeBlob(dirOrFile.getName(), gitDirectory, dirOrFile.toPath().getParent());
            objects.add(new BlobObject("100644", dirOrFile.getName(), HexUtilities.hexToBytes(sha1)));
        }

        return objects;
    }

    private static String writeTree(File gitDirectory, List<GitObject> objects) throws Exception {
        byte[] content = serializeTree(objects);
        byte[] fullData = prependHeader(content);
        String sha1 = computeSha1(fullData);
        writeObject(gitDirectory, sha1, fullData);
        return sha1;
    }

    private static byte[] serializeTree(List<GitObject> objects) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        objects.sort(Comparator.comparing(GitObject::getName));
        for (GitObject obj : objects) {
            try {
                out.write(obj.toBytes());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return out.toByteArray();
    }

    private static byte[] prependHeader( byte[] body) {
        String header = "tree " + body.length + "\0";
        byte[] headerBytes = header.getBytes(StandardCharsets.UTF_8);
        byte[] full = new byte[headerBytes.length + body.length];
        System.arraycopy(headerBytes, 0, full, 0, headerBytes.length);
        System.arraycopy(body, 0, full, headerBytes.length, body.length);
        return full;
    }

}
