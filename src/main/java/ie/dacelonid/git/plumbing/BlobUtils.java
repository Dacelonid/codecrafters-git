package ie.dacelonid.git.plumbing;

import ie.dacelonid.git.plumbing.objects.GitObject;
import ie.dacelonid.git.utils.GitTreeParser;

import java.io.File;
import java.util.List;

public class BlobUtils {

    public static void printBlob(String sha1, File gitRootDirectory) throws Exception {
        String contents = GitObject.getFileContents(sha1, gitRootDirectory);
        System.out.print(contents.substring(contents.indexOf("\0") + 1));
    }

    public static void printTree(String objectId, File gitRootDirectory) throws Exception {
        byte[] full = GitObject.getBlobContents(objectId, gitRootDirectory);
        List<GitObject> treeEntries = GitTreeParser.parseTree(full);
        treeEntries.forEach(System.out::println);
    }

    public static List<GitObject> parseTree(String sha1, File gitRootDir) throws Exception {
        return GitTreeParser.parseTree(GitObject.getBlobContents(sha1, gitRootDir));
    }


}
