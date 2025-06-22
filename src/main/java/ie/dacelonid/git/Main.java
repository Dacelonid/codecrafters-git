package ie.dacelonid.git;


import java.nio.file.Path;

public class Main {
  public static void main(String[] args) throws Exception {
    GitCommand gitCommand = new GitCommand();
    Path currentDirectory = Path.of(System.getProperty("user.dir"));
    gitCommand.handle(args, currentDirectory);
  }
}
