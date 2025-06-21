package ie.dacelonid.git;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.nio.file.Path;

public class Main {
  public static void main(String[] args) throws Exception {
    ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
    GitCommand gitCommand = context.getBean(GitCommand.class);
    Path currentDirectory = Path.of(System.getProperty("user.dir"));

    gitCommand.handle(args, currentDirectory);

  }
}
