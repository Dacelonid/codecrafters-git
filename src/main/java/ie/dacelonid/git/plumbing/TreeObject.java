package ie.dacelonid.git.plumbing;

public class TreeObject extends GitObject{
    public TreeObject(String mode, String name, byte[] sha1){
        this.type = "tree";
        this.mode = mode;
        this.name = name;
        this.sha1 = sha1;
    }
}
