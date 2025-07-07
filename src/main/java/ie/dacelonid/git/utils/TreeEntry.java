package ie.dacelonid.git.utils;

public class TreeEntry{

    private final String mode;
    private final String name;
    private final byte[] sha1;
    private final String type;

    public TreeEntry(String mode, String name, byte[] sha1) {
        this.mode = ("40000".equals(mode)?"040000":mode);
        this.name = name;
        this.sha1 = sha1;
        this.type = ("40000".equals(mode)?"tree":"blob");
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
}
