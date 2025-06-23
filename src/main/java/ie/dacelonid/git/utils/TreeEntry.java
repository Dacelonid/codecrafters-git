package ie.dacelonid.git.utils;

public record TreeEntry(String mode, String name, byte[] sha1) {
    @Override
    public String toString() {
        return mode + " " + name + " " + bytesToHex(sha1);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
