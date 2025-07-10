package ie.dacelonid.git.plumbing.objects;

import ie.dacelonid.git.ZlibHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static ie.dacelonid.git.utils.HexUtilities.computeSha1;

public class BlobObject extends GitObject {
    public BlobObject(String mode, String name, String sha1) {
        this.type = "blob";
        this.mode = mode;
        this.name = name;
        this.sha1 = sha1;
    }

    public BlobObject(String mode, String name) {
        this.type = "blob";
        this.mode = mode;
        this.name = name;
    }

    public String writeNewBlob(String fileName, File gitRootDir, Path currentDirectory) throws Exception {
        String contentToWrite = getFileContentsToWriteToBlob(fileName, currentDirectory);
        sha1 = computeSha1(contentToWrite);
        File blobFile = createFileToWriteTo(gitRootDir, sha1);
        Files.write(blobFile.toPath(), ZlibHandler.compress(contentToWrite.getBytes()));
        return sha1;
    }

    private File createFileToWriteTo(File gitRootDirectory, String sha1) throws Exception {
        final File blob_dir = new File(gitRootDirectory, "objects/" + sha1.substring(0, 2));
        blob_dir.mkdirs();
        return new File(blob_dir, sha1.substring(2));

    }

    private String getFileContentsToWriteToBlob(String filename, Path currentDirectory) throws IOException {
        String contents = Files.readString(new File(currentDirectory.toFile(), filename).toPath());
        return "blob " + contents.length() + "\0" + contents;
    }
}
