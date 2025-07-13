package ie.dacelonid.git.plumbing.objects;

import ie.dacelonid.git.plumbing.BlobUtils;
import ie.dacelonid.git.utils.FileUtilities;
import ie.dacelonid.git.utils.HexUtilities;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
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
        // Remove leading zero from mode if present
        int modeInt = Integer.parseInt(mode, 8);  // parse as octal
        String normalizedMode = Integer.toOctalString(modeInt);  // Git omits leading zeros, e.g. "40000"

        byte[] modeBytes = normalizedMode.getBytes(StandardCharsets.UTF_8);
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        byte[] sha1Bytes = hexToBytes(sha1);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            out.write(modeBytes);
            out.write(' ');
            out.write(nameBytes);
            out.write(0);  // NULL byte
            out.write(sha1Bytes);
        } catch (IOException e) {
            throw new UncheckedIOException(e); // Should never happen with ByteArrayOutputStream
        }

        return out.toByteArray();
    }


    public static GitObject from(String mode, String name, byte[] sha) {
        return "40000".equals(mode)
                ? new TreeObject(mode, name, bytesToHex(sha))
                : new BlobObject(mode, name, bytesToHex(sha));
    }
}
