package ie.dacelonid.git.commands;

import ie.dacelonid.git.utils.TreeEntry;

import java.io.File;
import java.util.List;

import static ie.dacelonid.git.plumbing.BlobUtils.listTree;
import static ie.dacelonid.git.utils.HexUtilities.bytesToHex;

public enum LsTreeCommand {
    NAME_ONLY("--name-only") {
        @Override
        public void handle(String objectId, File gitRootDirectory) throws Exception {
            List<TreeEntry> treeEntries = listTree(objectId, gitRootDirectory);
            treeEntries.forEach(treeEntry -> System.out.println(treeEntry.getName()));
        }
    },
    TREES("-d") {
        @Override
        public void handle(String objectId, File gitRootDirectory) throws Exception {
            List<TreeEntry> treeEntries = listTree(objectId, gitRootDirectory);
            treeEntries.stream()
                    .filter(t -> "tree".equals(t.getType()))
                    .map(TreeEntry::getName)
                    .forEach(System.out::println);
        }

    },
    RECURSE("-r") {
        @Override
        public void handle(String objectId, File gitRootDirectory) throws Exception {
            List<TreeEntry> treeEntries = listTree(objectId, gitRootDirectory); //geteverything on the root dir
            for (TreeEntry treeEntry : treeEntries) {
                System.out.println(treeEntry.getName());
                if ("040000".equals(treeEntry.getMode())) {
                    try {
                        handle(bytesToHex(treeEntry.getSha1()), gitRootDirectory);
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
