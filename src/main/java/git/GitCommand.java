package git;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static utils.FileWalker.listFilesRecursively;

public class GitCommand {
    public void handle(String[] args, Path currentDirectory) throws Exception {
        final String command = args[0];
        switch (command) {
            case "init" -> {
                final File root = new File(currentDirectory.toFile(), ".git");
                if( root.exists()){
                    System.out.println("Could not initialise directory, already exists ");
                    return;
                }
                root.mkdirs();
                new File(root, "objects").mkdirs();
                new File(root, "refs").mkdirs();
                final File head = new File(root, "HEAD");

                try {
                    head.createNewFile();
                    Files.write(head.toPath(), "ref: refs/heads/main\n".getBytes());
                    System.out.println("Initialized git directory");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            case "cat-file" -> {
                readFromFile(args, currentDirectory);
            }
            default -> System.out.println("Unknown command: " + command);
        }

    }


    private static void readFromFile(String[] args, Path currentDirectory) throws Exception {
        final String file = args[args.length-1];
        final File root = new File(currentDirectory.toFile(), ".git");
        String dir = file.substring(0, 2);
        String blob = file.substring(2);
        final File objectFile = new File(root, "objects/" + dir+ "/" + blob);
        if(!objectFile.exists()){
            System.out.println("File does not exist");
        }
        byte[] decompressed = ZlibHandler.decompress(Files.readAllBytes(objectFile.toPath()));
        String fileContents = new String(decompressed, StandardCharsets.UTF_8);
        fileContents = fileContents.substring(fileContents.indexOf("\0")+1);
        System.out.print(fileContents);
    }
}
