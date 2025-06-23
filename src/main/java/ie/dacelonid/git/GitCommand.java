package ie.dacelonid.git;

import ie.dacelonid.git.exceptions.*;
import ie.dacelonid.git.utils.FileWalker;
import ie.dacelonid.git.utils.GitTreeParser;
import ie.dacelonid.git.utils.TreeEntry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static ie.dacelonid.git.utils.FileUtilities.*;

public class GitCommand {

    public void handleCommand(String[] args, Path currentDirectory) throws Exception {
        final String command = args[0];
        try {
            switch (command) {
                case "init" -> initializeRepo(currentDirectory);
                case "cat-file" -> readBlob(args, currentDirectory);
                case "hash-object" -> writeBlob(args, currentDirectory);
                case "ls-tree" -> listTree(args, currentDirectory);
                default -> System.out.println("Unknown command: " + command);
            }
        } catch (GitCouldNotCreateDirectoryException e) {
            System.out.println(e.getMessage());
        } catch (GitRepoAlreadyExists e) {
            System.out.println(e.getMessage());
        }

    }

    private void listTree(String[] args, Path currentDirectory) throws Exception {
        String hash = args[args.length - 1];
        File treeFile = new File(currentDirectory.toFile(), ".git/objects/" + hash.substring(0,2) + "/" + hash.substring(2));
        if(treeFile.exists()) {
            byte[] decompressed = ZlibHandler.decompress(Files.readAllBytes(treeFile.toPath()));
            List<TreeEntry> entries = GitTreeParser.parseTree(decompressed);
            entries.forEach(s -> System.out.println(s.name()) );
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

    private File getGitRootDirectory(Path currentDirectory) throws GitRepoNotInitialized {
        final File gitDirectory = new File(currentDirectory.toFile(), ".git");
        if (gitDirectory.exists()) {
            return gitDirectory;
        }
        throw new GitRepoNotInitialized();
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

    private void readBlob(String[] args, Path currentDirectory) throws Exception {
        final File blob = getBlob(args, getGitRootDirectory(currentDirectory));
        String fileContents = getUncompressedBlobContents(blob);
        System.out.print(fileContents);
    }

    private void writeBlob(String[] args, Path currentDirectory) throws Exception {
        String contentToWrite = getFileContentsToWriteToBlob(args, currentDirectory);
        String sha1 = sha1(contentToWrite);
        createBlob(sha1, getGitRootDirectory(currentDirectory), contentToWrite);
        System.out.print(sha1);
    }
}
