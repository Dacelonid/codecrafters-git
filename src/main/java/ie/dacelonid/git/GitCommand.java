package ie.dacelonid.git;

import ie.dacelonid.git.exceptions.*;
import ie.dacelonid.git.plumbing.RepoUtils;

import java.io.File;
import java.nio.file.Path;

import static ie.dacelonid.git.plumbing.BlobUtils.*;
import static ie.dacelonid.git.plumbing.RepoUtils.*;

public class GitCommand {

    public void handleCommand(String[] args, Path currentDirectory) throws Exception {
        final String command = args[0];
        try {
            final File gitRootDirectory = getGitRootDirectory(currentDirectory);
            switch (command) {
                case "init" -> initializeRepo(gitRootDirectory);
                case "cat-file" -> printBlob(getCommandTarget(args), gitRootDirectory);
                case "hash-object" -> writeBlob(getCommandTarget(args), gitRootDirectory, currentDirectory);
                case "ls-tree" -> listTree(getCommandTarget(args), gitRootDirectory);
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

    private static String getCommandTarget(String[] args) {
        return args[args.length - 1];
    }
}
