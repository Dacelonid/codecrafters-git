package ie.dacelonid.git.utils;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GitTreeParser {
    public static List<TreeEntry> parseTree(byte[] data) {
        List<TreeEntry> entries = new ArrayList<>();
        int i = 0;

        // Skip header: "tree <size>\0"
        while (data[i] != 0) i++;
        i++; // skip null

        while (i < data.length) {
            int modeStart = i;
            while (data[i] != ' ') i++;
            String mode = new String(data, modeStart, i - modeStart, StandardCharsets.UTF_8);
            i++; // skip space

            int nameStart = i;
            while (data[i] != 0) i++;
            String name = new String(data, nameStart, i - nameStart, StandardCharsets.UTF_8);
            i++; // skip null

            byte[] sha = new byte[20];
            System.arraycopy(data, i, sha, 0, 20);
            i += 20;

            entries.add(new TreeEntry(mode, name, sha));
        }

        return entries;
    }

    public static byte[] serializeTree(List<TreeEntry> entries) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        for (TreeEntry entry : entries) {
            String header = entry.mode() + " " + entry.name();
            byte[] headerBytes = header.getBytes(StandardCharsets.UTF_8);
            out.writeBytes(headerBytes);
            out.write(0); // null separator between header and SHA
            out.writeBytes(entry.sha1()); // raw SHA-1 bytes (20 bytes)
        }

        // Prepend the Git object header: "tree <size>\0"
        byte[] body = out.toByteArray();
        String prefix = "tree " + body.length + "\0";
        ByteArrayOutputStream finalOut = new ByteArrayOutputStream();
        finalOut.writeBytes(prefix.getBytes(StandardCharsets.UTF_8));
        finalOut.writeBytes(body);

        return finalOut.toByteArray();
    }
}