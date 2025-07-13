package ie.dacelonid.git.plumbing.objects;

import ie.dacelonid.git.exceptions.GitExceptions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import static ie.dacelonid.git.plumbing.objects.GitObject.getTypeFromSha1Hash;
import static ie.dacelonid.git.utils.FileUtilities.writeObject;
import static ie.dacelonid.git.utils.HexUtilities.computeSha1;

public class CommitObject {
    private final String name;
    private final String email;
    private final String treeSha1;
    private final File gitRootDirectory;
    private String commitSha1;
    private final long time;
    private final String commitMsg;
    private final String parentSha;

    public CommitObject(String[] args, File gitRootDirectory) throws Exception {
        this.gitRootDirectory = gitRootDirectory;
        name = "Ken";
        email = "ken@codecrafters.com";
        treeSha1 = findSha1(args);
        commitMsg = findCommitMsg(args);
        parentSha = findParentSha(args);
        time = System.nanoTime();
    }

    private String findSha1(String[] args) throws Exception {
        for (String arg : args) { //TODO need to ensure that this is the tree SHA and not the parent sha
            if (arg.length() == 40) {
                String type = getTypeFromSha1Hash(arg, gitRootDirectory);
                if("tree".equals(type)){
                    return arg;
                }
            }
        }
        throw new GitExceptions();
    }

    private String findCommitMsg(String[] args) {
        for (int x = 0; x < args.length; x++) {
            if ("-m".equals(args[x])) return args[x + 1];
        }
        return "";
    }

    private String findParentSha(String[] args) {
        for (int x = 0; x < args.length; x++) {
            if ("-p".equals(args[x])) return args[x + 1];
        }
        return null;
    }

    public void write(File gitDirectory) throws Exception {
        writeTree(gitDirectory);
    }

    private void writeTree(File gitDirectory) throws Exception {
        byte[] content = convertTreeToBytes();
        byte[] fullData = prependHeader(content);
        this.commitSha1 = computeSha1(fullData);
        writeObject(gitDirectory, commitSha1, fullData);
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

    public String getSha1() {
        return commitSha1;
    }
}
