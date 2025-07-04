package ie.dacelonid.git.utils;

import java.io.File;

public class FileWalker {
    public static void listFilesRecursively(File dirOrFile) {
        if (dirOrFile == null || !dirOrFile.exists()) {
            System.err.println("Invalid path: " + dirOrFile);
            return;
        }

        if (dirOrFile.isDirectory()) {
            System.err.println("[DIR]  " + dirOrFile.getAbsolutePath());
            File[] children = dirOrFile.listFiles();
            if (children != null) {
                for (File file : children) {
                    listFilesRecursively(file);
                }
            }
        } else {
            System.err.println("[FILE] " + dirOrFile.getAbsolutePath());
//            try {
//                System.out.println("Blob ->" + getUncompressedFileContents(dirOrFile));
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }

        }
    }

}
