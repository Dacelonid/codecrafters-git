package ie.dacelonid.git.utils;

import ie.dacelonid.git.plumbing.objects.GitObject;

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
            i++; // skip space

            int nameStart = i;
            while (data[i] != 0) i++;
            String name = new String(data, nameStart, i - nameStart, StandardCharsets.UTF_8);
            i++; // skip null

            byte[] sha = new byte[20];
            System.arraycopy(data, i, sha, 0, 20);
            i += 20;
            entries.add(GitObject.from(mode, name, sha));
        }
        return entries;
    }

}