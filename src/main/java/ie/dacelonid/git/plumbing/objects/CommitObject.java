package ie.dacelonid.git.plumbing.objects;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static ie.dacelonid.git.utils.FileUtilities.writeObject;
import static ie.dacelonid.git.utils.HexUtilities.computeSha1;
import static ie.dacelonid.git.utils.HexUtilities.hexToBytes;

public class CommitObject extends GitObject{
    private final String name;
    private final String email;
    private final String treeSha1;
    private final String parentSha1 = "";

    private String sha1;
    private long time;
    private final String commitMsg;
    private final String parentSha;

    public String getSha1() {
        return sha1;
    }

    public CommitObject(String name, String email, String treesha1, long time, String commitMsg, String parentSha) {
        this.name = name;
        this.email = email;
        this.treeSha1 = treesha1;
        this.time = time;
        this.commitMsg = commitMsg;
        this.parentSha = parentSha;
    }

    public void write(File gitDirectory) throws Exception {
        writeTree(gitDirectory);
    }


    private void writeTree(File gitDirectory) throws Exception {
        byte[] content = convertTreeToBytes();
        byte[] fullData = prependHeader(content);
        this.sha1 = computeSha1(fullData);
        writeObject(gitDirectory, sha1, fullData);
    }

    private byte[] convertTreeToBytes() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(this.toBytes());
        return out.toByteArray();
    }

    private byte[] prependHeader(byte[] body) {
        String header = "commit " + body.length + "\0";
        byte[] headerBytes = header.getBytes(StandardCharsets.UTF_8);
        byte[] full = new byte[headerBytes.length + body.length];
        System.arraycopy(headerBytes, 0, full, 0, headerBytes.length);
        System.arraycopy(body, 0, full, headerBytes.length, body.length);
        return full;
    }


    public byte[] toBytes() {
        String timestamp = String.valueOf(time);
        String timezone = "+0000"; // Consider computing dynamically if needed

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            // tree <sha1>
            out.write(("tree " + treeSha1 + "\n").getBytes(StandardCharsets.UTF_8));

            // parent <sha1> (optional)
            if (parentSha != null) {
                out.write(("parent " + parentSha + "\n").getBytes(StandardCharsets.UTF_8));
            }

            // author <name> <email> <timestamp> <timezone>
            out.write(("author " + name + " <" + email + "> " + timestamp + " " + timezone + "\n").getBytes(StandardCharsets.UTF_8));

            // committer <name> <email> <timestamp> <timezone>
            out.write(("committer " + name + " <" + email + "> " + timestamp + " " + timezone + "\n").getBytes(StandardCharsets.UTF_8));

            // empty line before commit message
            out.write("\n".getBytes(StandardCharsets.UTF_8));

            // commit message
            out.write(commitMsg.getBytes(StandardCharsets.UTF_8));
            out.write("\n".getBytes(StandardCharsets.UTF_8));  // optional trailing newline
        } catch (IOException e) {
            throw new UncheckedIOException(e); // should never happen
        }

        return out.toByteArray();
    }


    private byte[] longToBytes(long value) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES); // 8 bytes
        buffer.putLong(value);
        return buffer.array();
    }
}
