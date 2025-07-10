package ie.dacelonid.git.plumbing.objects;

import ie.dacelonid.git.utils.HexUtilities;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public abstract class GitObject {
    protected String mode;
    protected String type;
    protected byte[] sha1;
    protected String name;

    public static File getFileFromSha1Hash(File gitRootDirectory, String sha1) {
        String dir = sha1.substring(0, 2); //Directory is first 2 characters of SHA1
        String fileName = sha1.substring(2); //Filename is the remaining SHA1

        if (new File(gitRootDirectory, "objects/" + dir).exists()) {
            for (File file : Objects.requireNonNull(new File(gitRootDirectory, "objects/" + dir).listFiles())) {
                if (file.getName().startsWith(fileName)) {
                    return file;
                }
            }
        }
        return new File(gitRootDirectory, "objects/" + dir + "/" + fileName); //doesn't exist not very good handling
    }


    public String getMode() {
        return mode;
    }

    public String getName() {
        return name;
    }

    public byte[] getSha1() {
        return sha1;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return mode + " " + type + " " + HexUtilities.bytesToHex(sha1) + " " + name;
    }

    public byte[] toBytes() {
        byte[] modeBytes = mode.getBytes(StandardCharsets.UTF_8);
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[modeBytes.length + 1 + nameBytes.length + 1 + sha1.length];

        int pos = 0;
        System.arraycopy(modeBytes, 0, result, pos, modeBytes.length);
        pos += modeBytes.length;
        result[pos++] = ' ';
        System.arraycopy(nameBytes, 0, result, pos, nameBytes.length);
        pos += nameBytes.length;
        result[pos++] = 0;
        System.arraycopy(sha1, 0, result, pos, sha1.length);

        return result;

    }

    public static class GitObjectBuilder {
        String mode;
        byte[] sha1;
        String name;

        public GitObjectBuilder mode(String mode) {
            this.mode = mode;
            return this;
        }

        public GitObjectBuilder name(String name) {
            this.name = name;
            return this;
        }

        public GitObjectBuilder sha1(byte[] sha1) {
            this.sha1 = sha1;
            return this;
        }

        public GitObject build() {
            if ("040000".equals(mode)) {
                return new TreeObject(mode, name, sha1);
            }
            return new BlobObject(mode, name, sha1);
        }
    }
}
