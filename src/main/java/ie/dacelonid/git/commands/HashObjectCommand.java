package ie.dacelonid.git.commands;

import ie.dacelonid.git.plumbing.objects.BlobObject;
import ie.dacelonid.git.plumbing.objects.GitObject;

import java.io.File;
import java.nio.file.Path;

import static ie.dacelonid.git.plumbing.objects.BlobObject.writeBlob;

public class HashObjectCommand {
    public static void handle(String fileName, File gitRootDirectory, Path currentDirectory) throws Exception {
//        BlobObject obj = new BlobObject("100644", fileName);
//        String sha1 = obj.writeNewBlob(fileName, gitRootDirectory, currentDirectory);
        String sha1 = writeBlob(fileName, gitRootDirectory, currentDirectory);
        System.out.println(sha1);
    }
}
