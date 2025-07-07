package ie.dacelonid.git.utils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public record TreeObject(String mode, String type, byte[] sha1, String name) {
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

    @Override
    public String toString() {
        return mode + " " + type + " " + name + " " + Arrays.toString(sha1);
    }
}
