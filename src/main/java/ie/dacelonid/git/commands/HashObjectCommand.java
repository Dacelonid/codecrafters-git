package ie.dacelonid.git.commands;

import ie.dacelonid.git.plumbing.objects.BlobObject;

import java.io.File;
import java.nio.file.Path;

public class HashObjectCommand {
    public static void handle(String fileName, File gitRootDirectory, Path currentDirectory) throws Exception {
        BlobObject blobObject = new BlobObject("100644", fileName);
        String sha1 = blobObject.writeNewBlob(fileName, gitRootDirectory, currentDirectory);
        System.out.println(sha1);
    }
}
