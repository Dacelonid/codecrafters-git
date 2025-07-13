package ie.dacelonid.git.commands;

import ie.dacelonid.git.plumbing.objects.GitObject;

import java.io.File;
import java.util.List;

import static ie.dacelonid.git.plumbing.BlobUtils.parseTree;
import static ie.dacelonid.git.utils.HexUtilities.bytesToHex;

public enum LsTreeCommand {
    NAME_ONLY("--name-only") {
        @Override
        public void handle(String objectId, File gitRootDirectory) throws Exception {
            List<GitObject> treeEntries = parseTree(objectId, gitRootDirectory);
            treeEntries.forEach(treeEntry -> System.out.println(treeEntry.getName()));
        }
    },
    TREES("-d") {
        @Override
        public void handle(String objectId, File gitRootDirectory) throws Exception {
            List<GitObject> treeEntries = parseTree(objectId, gitRootDirectory);
            treeEntries.stream()
                    .filter(t -> "tree".equals(t.getType()))
                    .map(GitObject::getName)
                    .forEach(System.out::println);
        }

    },
    RECURSE("-r") {
        @Override
        public void handle(String objectId, File gitRootDirectory) throws Exception {
            List<GitObject> treeEntries = parseTree(objectId, gitRootDirectory); //geteverything on the root dir
            for (GitObject objects : treeEntries) {
                System.out.println(objects.getName());
                if ("40000".equals(objects.getMode())) {
                    try {
                        handle(objects.getSha1(), gitRootDirectory);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }

        }
    };

    private final String option;

    LsTreeCommand(String option) {
        this.option = option;
    }

    public static LsTreeCommand fromOption(String opt) {
        for (LsTreeCommand mode : values()) {
            if (mode.option.equals(opt.trim())) return mode;
        }
        throw new IllegalArgumentException("Unknown option: " + opt);
    }

    public abstract void handle(String objectId, File gitRootDirectory) throws Exception;
}
