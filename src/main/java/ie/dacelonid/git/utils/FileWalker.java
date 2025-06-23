package ie.dacelonid.git.utils;
import java.io.File;

import static ie.dacelonid.git.utils.FileUtilities.getBlob;
import static ie.dacelonid.git.utils.FileUtilities.getUncompressedBlobContents;

public class FileWalker {
    public static void listFilesRecursively(File dirOrFile) {
        if (dirOrFile == null || !dirOrFile.exists()) {
            System.out.println("Invalid path: " + dirOrFile);
            return;
        }

        if (dirOrFile.isDirectory()) {
            System.out.println("[DIR]  " + dirOrFile.getAbsolutePath());
            File[] children = dirOrFile.listFiles();
            if (children != null) {
                for (File file : children) {
                    listFilesRecursively(file);
                }
            }
        } else {
            System.out.println("[FILE] " + dirOrFile.getAbsolutePath());
            try {
                System.out.println("Blob ->" + getUncompressedBlobContents(dirOrFile));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
    }public static void listFilesRecursively(File dirOrFile, String filenameToFind) {
        if (dirOrFile == null || !dirOrFile.exists()) {
            System.out.println("Invalid path: " + dirOrFile);
            return;
        }

        if (dirOrFile.isDirectory()) {
//            System.out.println("[DIR]  " + dirOrFile.getAbsolutePath());
            File[] children = dirOrFile.listFiles();
            if (children != null) {
                for (File file : children) {
                    listFilesRecursively(file, filenameToFind);
                }
            }
        } else {
            if(dirOrFile.getName().equals(filenameToFind.substring(2))) {
//                System.out.println("[FILE] " + dirOrFile.getAbsolutePath());
                try {
                    System.out.println("Bob ->" + getUncompressedBlobContents(dirOrFile));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
