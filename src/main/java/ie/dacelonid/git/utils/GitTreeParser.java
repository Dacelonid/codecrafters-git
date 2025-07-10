package ie.dacelonid.git.utils;

import ie.dacelonid.git.plumbing.objects.GitObject;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GitTreeParser {
    public static List<GitObject> parseTree(byte[] data) {
        List<GitObject> entries = new ArrayList<>();
        int i = 0;

        // Skip header: "tree <size>\0"
        while (data[i] != 0) i++;
        i++; // skip null

        while (i < data.length) {
            int modeStart = i;
            while (data[i] != ' ') i++;
            String mode = new String(data, modeStart, i - modeStart, StandardCharsets.UTF_8);
            if("40000".equals(mode)) mode = "040000";
            i++; // skip space

            int nameStart = i;
            while (data[i] != 0) i++;
            String name = new String(data, nameStart, i - nameStart, StandardCharsets.UTF_8);
            i++; // skip null

            byte[] sha = new byte[20];
            System.arraycopy(data, i, sha, 0, 20);
            i += 20;

            entries.add(new GitObject.GitObjectBuilder().mode(mode).name(name).sha1(sha).build());
        }
        return entries;
    }

    public static byte[] serializeTree(List<GitObject> entries) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        for (GitObject entry : entries) {
            String header = entry.getMode() + " " + entry.getName();
            byte[] headerBytes = header.getBytes(StandardCharsets.UTF_8);
            out.writeBytes(headerBytes);
            out.write(0); // null separator between header and SHA
            out.writeBytes(entry.getSha1()); // raw SHA-1 bytes (20 bytes)
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