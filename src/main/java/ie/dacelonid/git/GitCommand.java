package ie.dacelonid.git;

import ie.dacelonid.git.exceptions.GitCouldNotCreateDirectoryException;
import ie.dacelonid.git.exceptions.GitCouldNotCreateHead;
import ie.dacelonid.git.exceptions.GitExceptions;
import ie.dacelonid.git.exceptions.GitRepoAlreadyExists;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static ie.dacelonid.git.utils.FileUtilities.createDirectory;
import static ie.dacelonid.git.utils.FileUtilities.readFromFile;

public class GitCommand {

    public void handle(String[] args, Path currentDirectory) throws Exception {
        final String command = args[0];
        try {
            switch (command) {
                case "init" -> initializeRepo(currentDirectory);
                case "cat-file" -> readFromFile(args, currentDirectory);
                default -> System.out.println("Unknown command: " + command);
            }
        } catch (GitCouldNotCreateDirectoryException e) {
            System.out.println(e.getMessage());
        } catch (GitRepoAlreadyExists e) {
            System.out.println(e.getMessage());
        }

    }

    private void initializeRepo(Path currentDirectory) throws GitExceptions {
        final File gitDirectory = getGitRootDirectoryIfNotExisting(currentDirectory);
        createGitDirectoryStructure(gitDirectory);

        createHeadFile(gitDirectory);
        System.out.println("Initialized git directory");
    }

    private File getGitRootDirectoryIfNotExisting(Path currentDirectory) throws GitRepoAlreadyExists {
        final File gitDirectory = new File(currentDirectory.toFile(), ".git");
        if (gitDirectory.exists()) {
            throw new GitRepoAlreadyExists();
        }
        return gitDirectory;
    }

    private void createGitDirectoryStructure(File gitDirectory) throws GitCouldNotCreateDirectoryException {
        createDirectory(gitDirectory);
        createDirectory(new File(gitDirectory, "refs"));
        createDirectory(new File(gitDirectory, "objects"));
    }

    private void createHeadFile(File gitDirectory) throws GitCouldNotCreateHead {
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


}
