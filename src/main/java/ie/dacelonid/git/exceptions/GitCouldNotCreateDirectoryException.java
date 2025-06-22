package ie.dacelonid.git.exceptions;

import java.io.File;

public class GitCouldNotCreateDirectoryException extends GitExceptions {
    private final File dir;

    public GitCouldNotCreateDirectoryException(File dir) {
        this.dir = dir;
    }

    public String getMessage() {
        return "Could not create directory " + dir.getAbsolutePath();
    }
}
