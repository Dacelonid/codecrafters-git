package ie.dacelonid.git.exceptions;

public class GitCouldNotCreateHead extends GitExceptions {
    @Override
    public String getMessage() {
        return "Could not initilialise HEAD reference";
    }
}
