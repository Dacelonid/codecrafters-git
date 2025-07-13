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

public class TreeObject extends GitObject {
    private final List<GitObject> directoryContents = new ArrayList<>();

    public TreeObject(String mode, String name, String sha1) {
        this.type = "tree";
        this.mode = mode;
        this.name = name;
        this.sha1 = sha1;
    }

    public TreeObject(String name) {
        this.type = "tree";
        this.mode = "40000";
        this.name = name;
    }

    /**
     * Recursively builds the object hierarchy and writes all GitObjects to the object store.
     */
    public String write(File gitDirectory, File directory) throws Exception {
        buildTree(gitDirectory, directory);
        writeTree(gitDirectory);
        return sha1;
    }

    private void buildTree(File gitDirectory, File currentDir) throws Exception {
        File[] children = currentDir.listFiles();
        if (children == null) return;
        for (File child : children) {
            if(".git".equals(child.getName())){
                continue;
            }
            if (child.isDirectory()) {
                TreeObject subTree = new TreeObject(child.getName());
                subTree.buildTree(gitDirectory, child);
                subTree.writeTree(gitDirectory); // calculate sha1 of subtree and write it
                directoryContents.add(subTree);
            } else {
                BlobObject blob = new BlobObject("100644", child.getName());
                blob.writeNewBlob(child.getName(), gitDirectory, child.getParentFile().toPath());
                directoryContents.add(blob);
            }
        }
    }

    private void writeTree(File gitDirectory) throws Exception {
        byte[] content = convertTreeToBytes();
        byte[] fullData = prependHeader(content);
        this.sha1 = computeSha1(fullData);
        writeObject(gitDirectory, sha1, fullData);
    }

    private byte[] convertTreeToBytes() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        directoryContents.sort(Comparator.comparing(GitObject::getName));
        for (GitObject obj : directoryContents) {
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

    public List<GitObject> getDirectoryContents() {
        return directoryContents;
    }
}
