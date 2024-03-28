package sg.edu.nus.comp.cs4218.exception;

/**
 * Exception thrown by the ls command.
 *
 * This exception is specific to the ls command and extends {@link AbstractApplicationException}.
 * It adds the prefix "ls: " to the error message to indicate that it originated from the ls command.
 */
public class LsException extends AbstractApplicationException {

    private static final long serialVersionUID = 5001961656291923161L;

    /**
     * Constructs a new LsException with the specified message.
     *
     * @param message The message describing the exception.
     */
    public LsException(String message) {
        super("ls: " + message);
    }

}
