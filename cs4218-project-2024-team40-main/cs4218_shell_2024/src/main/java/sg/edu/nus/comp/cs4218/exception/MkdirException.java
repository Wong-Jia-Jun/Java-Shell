package sg.edu.nus.comp.cs4218.exception;

/**
 * Exception thrown by the mkdir command.
 *
 * This exception is specific to the mkdir command and extends {@link AbstractApplicationException}.
 * It adds the prefix "mkdir: " to the error message to indicate that it originated from the mkdir command.
 */
public class MkdirException extends AbstractApplicationException {

    private static final long serialVersionUID = -7005801205007805286L;

    /**
     * Constructs a new MkdirException with the specified message.
     *
     * @param message The message describing the exception.
     */
    public MkdirException(String message) {
        super("mkdir: " + message);
    }
}

