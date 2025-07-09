package ie.dacelonid.git.plumbing;

public class BlobObject extends GitObject {
    public BlobObject(String mode, String name, byte[] sha1) {
        this.type = "blob";
        this.mode = mode;
        this.name = name;
        this.sha1 = sha1;
    }
}
