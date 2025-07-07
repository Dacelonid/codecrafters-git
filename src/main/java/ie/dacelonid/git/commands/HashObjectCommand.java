package ie.dacelonid.git.commands;

import java.io.File;
import java.nio.file.Path;

import static ie.dacelonid.git.plumbing.BlobUtils.writeBlob;

public class HashObjectCommand {
    public static void handle(String commandTarget, File gitRootDirectory, Path currentDirectory) throws Exception {
        String sha1 = writeBlob(commandTarget, gitRootDirectory, currentDirectory);
        System.out.println(sha1);
    }
}
