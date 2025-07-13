package ie.dacelonid.git.plumbing;

import ie.dacelonid.git.exceptions.GitCouldNotCreateDirectoryException;
import ie.dacelonid.git.exceptions.GitCouldNotCreateHead;
import ie.dacelonid.git.exceptions.GitRepoNotInitialized;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static ie.dacelonid.git.utils.FileUtilities.createDirectory;

public class RepoUtils {
    public static void createGitDirectoryStructure(File gitDirectory) throws GitCouldNotCreateDirectoryException {
        createDirectory(gitDirectory);
        createDirectory(new File(gitDirectory, "refs"));
        createDirectory(new File(gitDirectory, "objects"));
    }

    public static void createHeadFile(File gitDirectory) throws GitCouldNotCreateHead {
        final File head = new File(gitDirectory, "HEAD");
        try {
            boolean fileCreated = head.createNewFile();
            if (!fileCreated)
                throw new GitCouldNotCreateHead();
            Files.write(head.toPath(), "ref: refs/heads/main\n".getBytes());
        } catch (IOException e) {
            throw new GitCouldNotCreateHead();
        }
    }

    public static File getGitRootDirectory(Path currentDirectory) {
        return new File(currentDirectory.toFile(), ".git");
    }
}
