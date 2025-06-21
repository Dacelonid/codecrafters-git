package ie.dacelonid.git.utils;

import ie.dacelonid.git.ZlibHandler;
import ie.dacelonid.git.exceptions.GitCouldNotCreateDirectoryException;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class FileUtilities {

    public void createDirectory(File dir) throws GitCouldNotCreateDirectoryException {
        if (!dir.mkdirs())
            throw new GitCouldNotCreateDirectoryException(dir);
    }


    public void readFromFile(String[] args, Path currentDirectory) throws Exception {
        final File root = new File(currentDirectory.toFile(), ".git");
        final File blob = getBlob(args, root);
        String fileContents = getFileContents(blob);
        System.out.print(fileContents);
    }

    public File getBlob(String[] args, File root) {
        final String sha1 = args[args.length - 1];
        String dir = sha1.substring(0, 2); //Directory is first 2 characters of SHA1
        String blobname = sha1.substring(2); //Filename is the remaining SHA1
        final File blob = new File(root, "objects/" + dir + "/" + blobname);
        if (!blob.exists()) {
            System.out.println("File does not exist");
        }
        return blob;
    }

    public String getFileContents(File objectFile) throws Exception {
        byte[] decompressed = ZlibHandler.decompress(Files.readAllBytes(objectFile.toPath()));
        String fileContents = new String(decompressed, StandardCharsets.UTF_8);
        fileContents = fileContents.substring(fileContents.indexOf("\0") + 1);
        return fileContents;
    }
}
