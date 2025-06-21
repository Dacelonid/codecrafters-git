package utils;
import java.io.File;

public class FileWalker {
    public static void listFilesRecursively(File root) {
        if (root == null || !root.exists()) {
            System.out.println("Invalid path: " + root);
            return;
        }

        if (root.isDirectory()) {
            System.out.println("[DIR]  " + root.getAbsolutePath());
            File[] children = root.listFiles();
            if (children != null) {
                for (File file : children) {
                    listFilesRecursively(file);
                }
            }
        } else {
            System.out.println("[FILE] " + root.getAbsolutePath());
        }
    }
}
