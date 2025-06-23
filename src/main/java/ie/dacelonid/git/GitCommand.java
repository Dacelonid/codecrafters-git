package ie.dacelonid.git;

import ie.dacelonid.git.exceptions.*;
import ie.dacelonid.git.plumbing.BlobUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static ie.dacelonid.git.utils.FileUtilities.*;

public class GitCommand {

    public void handleCommand(String[] args, Path currentDirectory) throws Exception {
        final String command = args[0];
        try {
            final File gitRootDirectory = getGitRootDirectory(currentDirectory);
            switch (command) {
                case "init" -> initializeRepo(gitRootDirectory);
                case "cat-file" -> BlobUtils.printBlob(args[args.length-1], gitRootDirectory);
                case "hash-object" -> BlobUtils.writeBlob(args, gitRootDirectory, currentDirectory);
                case "ls-tree" -> BlobUtils.listTree(args, gitRootDirectory);
                default -> System.out.println("Unknown command: " + command);
            }
        } catch (GitCouldNotCreateDirectoryException e) {
            System.out.println(e.getMessage());
        } catch (GitRepoAlreadyExists e) {
            System.out.println(e.getMessage());
        }

    }

    private void initializeRepo(File gitRootDir) throws GitExceptions {
        if(gitRootDir.exists()){
            throw new GitRepoAlreadyExists();
        }
        createGitDirectoryStructure(gitRootDir);
        createHeadFile(gitRootDir);
        System.out.println("Initialized git directory");
    }

    private File getGitRootDirectory(Path currentDirectory) throws GitRepoNotInitialized {
        return new File(currentDirectory.toFile(), ".git");
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
