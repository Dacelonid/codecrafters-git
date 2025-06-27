package ie.dacelonid.git.commands;

import java.io.File;

import static ie.dacelonid.git.plumbing.BlobUtils.*;

public enum CatFileCommand {
    PRINT("-p") {
        @Override
        public void handle(String objectId, File gitRootDirectory) throws Exception {
            printBlob(objectId, gitRootDirectory);
        }
    },
    EXISTS("-e") {
        @Override
        public void handle(String objectId, File gitRootDirectory) throws Exception {
            File objectFile = getFileFromSha1Hash(gitRootDirectory, objectId);
            if(!objectFile.exists()){
                System.out.println("fatal: Not a valid object name " + objectId);
            }
        }
    },
    TYPE("-t") {
        @Override
        public void handle(String objectId, File gitRootDirectory) throws Exception {
            String contents = getBlobContents(objectId, gitRootDirectory);
            String type = contents.split("\0")[0].split(" ")[0];
            System.out.println(type);
        }
    }, SIZE("-s"){
        @Override
        public void handle(String objectId, File gitRootDirectory) throws Exception {
            String contents = getBlobContents(objectId, gitRootDirectory);
            String type = contents.split("\0")[0].split(" ")[1];
            System.out.println(type);
        }
    };

    private final String option;
    CatFileCommand(String option) {
        this.option = option;
    }
    public static CatFileCommand fromOption(String opt) {
        for (CatFileCommand mode : values()) {
            if (mode.option.equals(opt)) return mode;
        }
        throw new IllegalArgumentException("Unknown option: " + opt);
    }

    public abstract void handle(String objectId, File gitRootDirectory) throws Exception;
}
