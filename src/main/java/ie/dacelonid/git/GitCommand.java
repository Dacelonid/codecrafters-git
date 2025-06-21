package ie.dacelonid.git;

import ie.dacelonid.git.exceptions.GitCouldNotCreateDirectoryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ie.dacelonid.git.utils.FileUtilities;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class GitCommand {
    @Autowired
    private FileUtilities fileUtilities;

    public void handle(String[] args, Path currentDirectory) throws Exception {
        final String command = args[0];
        try {
            switch (command) {
                case "init" -> {
                    final File root = new File(currentDirectory.toFile(), ".git");
                    if (root.exists()) {
                        System.out.println("Could not initialise directory, already exists ");
                        return;
                    }
                    fileUtilities.createDirectory(root);
                    fileUtilities.createDirectory(new File(root, "refs"));
                    fileUtilities.createDirectory(new File(root, "objects"));

                    final File head = new File(root, "HEAD");
                    try {
                        head.createNewFile();
                        Files.write(head.toPath(), "ref: refs/heads/main\n".getBytes());
                        System.out.println("Initialized git directory");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                case "cat-file" -> fileUtilities.readFromFile(args, currentDirectory);
                default -> System.out.println("Unknown command: " + command);
            }
        } catch (GitCouldNotCreateDirectoryException e) {
            System.out.println(e.getMessage());
        }

    }


}
