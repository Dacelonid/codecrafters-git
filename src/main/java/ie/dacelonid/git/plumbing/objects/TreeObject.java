package ie.dacelonid.git.plumbing.objects;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static ie.dacelonid.git.utils.FileUtilities.writeObject;
import static ie.dacelonid.git.utils.HexUtilities.computeSha1;
import static ie.dacelonid.git.utils.HexUtilities.hexToBytes;

public class TreeObject extends GitObject {
    private List<GitObject> directoryContents = new ArrayList<>();
    public TreeObject(String mode, String name, String sha1) {
        this.type = "tree";
        this.mode = mode;
        this.name = name;
        this.sha1 = sha1;
    }

    public TreeObject(String name) {
        this.type = "tree";
        this.mode = "040000";
        this.name = name;
    }

    public String write(File gitDirectory, File name) throws Exception {
        directoryContents.addAll(getAllFilesAndDirs(gitDirectory, name));
        return directoryContents.getFirst().getSha1();
    }

    private List<GitObject> getAllFilesAndDirs(File gitDirectory, File dirOrFile) throws Exception {
        List<GitObject> objects = new ArrayList<>();
        if (dirOrFile.isDirectory()) {
            File[] children = dirOrFile.listFiles();
            if (children != null && !dirOrFile.equals(gitDirectory)) {
                for (File child : children) {
                    objects.addAll(getAllFilesAndDirs(gitDirectory, child));
                }
                String sha1 = writeTree(gitDirectory, objects);
                return List.of(GitObject.from("040000", dirOrFile.getName(), hexToBytes(sha1)));
            }
        } else {
            BlobObject gitObject = new BlobObject("100644", dirOrFile.getName());
            gitObject.writeNewBlob(dirOrFile.getName(), gitDirectory, dirOrFile.toPath().getParent());
            objects.add(gitObject);
        }

        return objects;
    }

    private String writeTree(File gitDirectory, List<GitObject> objects) throws Exception {
        byte[] content = convertTreeToBytes(objects);
        byte[] fullData = prependHeader(content);
        String sha1 = computeSha1(fullData);
        writeObject(gitDirectory, sha1, fullData);
        return sha1;
    }

    private byte[] convertTreeToBytes(List<GitObject> objects) {
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

    private byte[] prependHeader(byte[] body) {
        String header = "tree " + body.length + "\0";
        byte[] headerBytes = header.getBytes(StandardCharsets.UTF_8);
        byte[] full = new byte[headerBytes.length + body.length];
        System.arraycopy(headerBytes, 0, full, 0, headerBytes.length);
        System.arraycopy(body, 0, full, headerBytes.length, body.length);
        return full;
    }
}
