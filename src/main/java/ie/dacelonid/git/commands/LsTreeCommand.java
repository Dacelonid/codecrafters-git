package ie.dacelonid.git.commands;

import ie.dacelonid.git.utils.TreeEntry;

import java.io.File;
import java.util.List;

import static ie.dacelonid.git.plumbing.BlobUtils.listTree;
import static ie.dacelonid.git.utils.FileUtilities.bytesToHex;

public enum LsTreeCommand {
    NAME_ONLY("--name-only") {
        @Override
        public void handle(String objectId, File gitRootDirectory) throws Exception {
            List<TreeEntry> treeEntries = listTree(objectId, gitRootDirectory);
            treeEntries.forEach(treeEntry -> System.out.println(treeEntry.name()));
        }
    },
    TREES("-d") {
        @Override
        public void handle(String objectId, File gitRootDirectory) throws Exception {
            List<TreeEntry> treeEntries = listTree(objectId, gitRootDirectory);
            treeEntries.stream()
                    .filter(t -> "40000".equals(t.mode()))
                    .map(TreeEntry::name)
                    .forEach(System.out::println);
        }

    },
    RECURSE("-r") {
        @Override
        public void handle(String objectId, File gitRootDirectory) throws Exception {
            List<TreeEntry> treeEntries = listTree(objectId, gitRootDirectory); //geteverything on the root dir
            for (TreeEntry treeEntry : treeEntries) {
                System.out.println(treeEntry.name());
                if ("40000".equals(treeEntry.mode())) {
                    try {
                        handle(bytesToHex(treeEntry.sha1()), gitRootDirectory);
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
