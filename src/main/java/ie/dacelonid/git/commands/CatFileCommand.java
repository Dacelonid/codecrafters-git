package ie.dacelonid.git.commands;

import ie.dacelonid.git.plumbing.objects.GitObject;

import java.io.File;

import static ie.dacelonid.git.plumbing.BlobUtils.*;
import static ie.dacelonid.git.plumbing.objects.GitObject.getTypeFromSha1Hash;

public enum CatFileCommand {
    PRINT("-p") {
        @Override
        public void handle(String sha1, File gitRootDirectory) throws Exception {
            String type = getTypeFromSha1Hash(sha1, gitRootDirectory);

            if("blob".equals(type)) {
                printBlob(sha1, gitRootDirectory);
            }else if("commit".equals(type)){
                printCommit(sha1, gitRootDirectory);
            }else
                printTree(sha1, gitRootDirectory);
        }
    },
    EXISTS("-e") {
        @Override
        public void handle(String objectId, File gitRootDirectory) {
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
            String size = GitObject.getSizeFromSha1Hash(objectId, gitRootDirectory);
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
