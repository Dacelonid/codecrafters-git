package ie.dacelonid.git.exceptions;

public class GitRepoAlreadyExists extends GitExceptions {
    public String getMessage() {
        return "Could not initialise directory, already exists ";
    }
}
