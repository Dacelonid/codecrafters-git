package ie.dacelonid.git.plumbing.objects;

import ie.dacelonid.git.plumbing.BlobUtils;
import ie.dacelonid.git.utils.FileUtilities;
import ie.dacelonid.git.utils.HexUtilities;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static ie.dacelonid.git.utils.HexUtilities.bytesToHex;
import static ie.dacelonid.git.utils.HexUtilities.hexToBytes;

public abstract class GitObject {
    protected String mode;
    protected String type;
    protected String sha1;
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

    public static byte[] getBlobContents(String sha1, File gitRootDir) throws Exception {
        final File blob = getFileFromSha1Hash(gitRootDir, sha1);
        return FileUtilities.getUncompressedFileContents(blob);
    }

    public static String getTypeFromSha1Hash(String objectId, File gitRootDirectory) throws Exception {
        String contents = getFileContents(objectId, gitRootDirectory);
        return contents.split("\0")[0].split(" ")[0];
    }

    public static String getFileContents(String objectId, File gitRootDirectory) throws Exception {
        return new String(getBlobContents(objectId, gitRootDirectory), StandardCharsets.UTF_8);
    }

    public static String getSizeFromSha1Hash(String objectId, File gitRootDirectory) throws Exception {
        String contents = getFileContents(objectId, gitRootDirectory);
        return contents.split("\0")[0].split(" ")[1];
    }


    public String getMode() {
        return mode;
    }

    public String getName() {
        return name;
    }

    public String getSha1() {
        return sha1;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return mode + " " + type + " " + sha1 + " " + name;
    }

    public byte[] toBytes() {
        byte[] modeBytes = mode.getBytes(StandardCharsets.UTF_8);
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        byte[] sha1_bytes = hexToBytes(sha1);
        byte[] result = new byte[modeBytes.length + 1 + nameBytes.length + 1 +sha1_bytes.length];
        int pos = 0;
        System.arraycopy(modeBytes, 0, result, pos, modeBytes.length);
        pos += modeBytes.length;
        result[pos++] = ' ';
        System.arraycopy(nameBytes, 0, result, pos, nameBytes.length);
        pos += nameBytes.length;
        result[pos++] = 0;
        System.arraycopy(sha1_bytes, 0, result, pos, sha1_bytes.length);

        return result;

    }

    public static GitObject from(String mode, String name, byte[] sha) {
        return "40000".equals(mode)
                ? new TreeObject(mode, name, bytesToHex(sha))
                : new BlobObject(mode, name, bytesToHex(sha));
    }
}
