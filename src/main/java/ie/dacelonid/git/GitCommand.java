package ie.dacelonid.git;

import ie.dacelonid.git.commands.CatFileCommand;
import ie.dacelonid.git.commands.HashObjectCommand;
import ie.dacelonid.git.commands.LsTreeCommand;
import ie.dacelonid.git.exceptions.GitCouldNotCreateDirectoryException;
import ie.dacelonid.git.exceptions.GitExceptions;
import ie.dacelonid.git.exceptions.GitRepoAlreadyExists;
import ie.dacelonid.git.plumbing.objects.TreeObject;

import java.io.File;
import java.nio.file.Path;

import static ie.dacelonid.git.plumbing.RepoUtils.*;

public class GitCommand {

    public void handleCommand(String[] args, Path currentDirectory) throws Exception {
        final String command = args[0];
        try {
            final File gitRootDirectory = getGitRootDirectory(currentDirectory);
            switch (command) {
                case "init" -> initializeRepo(gitRootDirectory);
                case "cat-file" -> CatFileCommand.fromOption(getCommandOptions(args)).handle(getCommandTarget(args), gitRootDirectory);
                case "hash-object" -> HashObjectCommand.handle(getCommandTarget(args), gitRootDirectory, currentDirectory);
                case "ls-tree" -> LsTreeCommand.fromOption(getCommandOptions(args)).handle(getCommandTarget(args), gitRootDirectory);
                case "write-tree" -> writeTreeToDisk(gitRootDirectory, currentDirectory);
                default -> System.out.println("Unknown command: " + command);
            }
        } catch (GitCouldNotCreateDirectoryException e) {
            System.out.println(e.getMessage());
        } catch (GitRepoAlreadyExists e) {
            System.out.println(e.getMessage());
        }

    }

    private void writeTreeToDisk(File gitRootDirectory, Path currentDirectory) throws Exception {
        TreeObject obj = new TreeObject(currentDirectory.toFile().getName());
        String sha1 = obj.write(gitRootDirectory, currentDirectory.toFile());
//        String sha1 = writeTree(gitRootDirectory, currentDirectory.toFile());

        System.out.println(sha1);
    }

    private String getCommandOptions(String[] args) {
        return args[1];
    }

    private void initializeRepo(File gitRootDir) throws GitExceptions {
        if (gitRootDir.exists()) {
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
