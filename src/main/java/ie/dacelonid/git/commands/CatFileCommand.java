package ie.dacelonid.git.commands;

import ie.dacelonid.git.plumbing.GitObject;

import java.io.File;

import static ie.dacelonid.git.plumbing.BlobUtils.*;

public enum CatFileCommand {
    PRINT("-p") {
        @Override
        public void handle(String objectId, File gitRootDirectory) throws Exception {
            String type = getTypeFromSha1Hash(objectId, gitRootDirectory);
            if("blob".equals(type)) {
                printBlob(objectId, gitRootDirectory);
            }else
                printTree(objectId, gitRootDirectory);
        }
    },
    EXISTS("-e") {
        @Override
        public void handle(String objectId, File gitRootDirectory) throws Exception {
            File objectFile = GitObject.getFileFromSha1Hash(gitRootDirectory, objectId);
            if(!objectFile.exists()){
                System.out.println("fatal: Not a valid object name " + objectId);
            }
        }
    },
    TYPE("-t") {
        @Override
        public void handle(String objectId, File gitRootDirectory) throws Exception {
            String type = getTypeFromSha1Hash(objectId, gitRootDirectory);
            System.out.println(type);
        }

    }, SIZE("-s"){
        @Override
        public void handle(String objectId, File gitRootDirectory) throws Exception {
            String size = getSizeFromSha1Hash(objectId, gitRootDirectory);
            System.out.println(size);
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
