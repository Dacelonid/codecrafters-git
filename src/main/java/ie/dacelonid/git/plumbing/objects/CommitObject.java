package ie.dacelonid.git.plumbing.objects;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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
        StringBuilder sb = new StringBuilder();

        sb.append("tree ").append(treeSha1).append("\n");
        if (parentSha != null) {
            sb.append("parent ").append(parentSha).append("\n");
        }

        String timestamp = String.valueOf(time);
        String timezone = "+0000"; // You can compute it dynamically if needed

        sb.append("author ")
                .append(name).append(" <").append(email).append("> ")
                .append(timestamp).append(" ").append(timezone).append("\n");

        sb.append("committer ")
                .append(name).append(" <").append(email).append("> ")
                .append(timestamp).append(" ").append(timezone).append("\n");

        sb.append("\n");  // Empty line before commit message
        sb.append(commitMsg);
        sb.append("\n");  // Empty line before commit message

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }


    public byte[] toBytess() {
        byte[] treeHeaderbytes = "tree".getBytes(StandardCharsets.UTF_8);
        byte[] treesha1bytes = hexToBytes(treeSha1);
        byte[] parentshabytes = parentSha == null ? new byte[0]:hexToBytes(parentSha);
        byte[] authorHeadingbytes = "author".getBytes(StandardCharsets.UTF_8);
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        byte[] emailBytes = email.getBytes(StandardCharsets.UTF_8);
        byte[] timeBytes = longToBytes(time);
        byte[] committerHeadingbytes = "committer".getBytes(StandardCharsets.UTF_8);
        byte[] commitMsgbytes = commitMsg.getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[treeHeaderbytes.length + 1 + treesha1bytes.length + 1 +
                parentshabytes.length + 1 +
                authorHeadingbytes.length + 1 + nameBytes.length + 1 + emailBytes.length + 1 + timeBytes.length + 1 +
                committerHeadingbytes.length + 1 + nameBytes.length + 1 + emailBytes.length + 1 + timeBytes.length + 1 +
                commitMsgbytes.length];
        int pos = 0;

        //tree
        System.arraycopy(treeHeaderbytes, 0, result, pos, treeHeaderbytes.length);
        pos += treeHeaderbytes.length;
        result[pos++] = ' ';
        System.arraycopy(treesha1bytes, 0, result, pos, treesha1bytes.length);
        pos += treesha1bytes.length;
        result[pos++] = '\n';

        //parents
        System.arraycopy(parentshabytes, 0, result, pos, parentshabytes.length);
        pos += parentshabytes.length;
        result[pos++] = '\n';

        //Author
        System.arraycopy(authorHeadingbytes, 0, result, pos, authorHeadingbytes.length);
        pos += authorHeadingbytes.length;
        result[pos++] = ' ';
        System.arraycopy(nameBytes, 0, result, pos, nameBytes.length);
        pos += nameBytes.length;
        result[pos++] = ' ';
        System.arraycopy(emailBytes, 0, result, pos, emailBytes.length);
        pos += emailBytes.length;
        result[pos++] = ' ';
        System.arraycopy(timeBytes, 0, result, pos, timeBytes.length);
        pos += timeBytes.length;
        result[pos++] = '\n';

        //Committer
        System.arraycopy(committerHeadingbytes, 0, result, pos, committerHeadingbytes.length);
        pos += committerHeadingbytes.length;
        result[pos++] = ' ';
        System.arraycopy(nameBytes, 0, result, pos, nameBytes.length);
        pos += nameBytes.length;
        result[pos++] = ' ';
        System.arraycopy(emailBytes, 0, result, pos, emailBytes.length);
        pos += emailBytes.length;
        result[pos++] = ' ';
        System.arraycopy(timeBytes, 0, result, pos, timeBytes.length);
        pos += timeBytes.length;
        result[pos++] = '\n';


        //Commit Msg
        System.arraycopy(commitMsgbytes, 0, result, pos, commitMsgbytes.length);

        return result;
    }

    private byte[] longToBytes(long value) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES); // 8 bytes
        buffer.putLong(value);
        return buffer.array();
    }
}
