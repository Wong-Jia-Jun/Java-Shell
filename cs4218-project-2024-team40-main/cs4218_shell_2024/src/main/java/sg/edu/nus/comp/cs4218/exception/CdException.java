package sg.edu.nus.comp.cs4218.exception;

/**
 * Exception thrown by the cd command.
 *
 * This exception is specific to the cd command and extends {@link AbstractApplicationException}.
 * It adds the prefix "cd: " to the error message to indicate that it originated from the cd command.
 */
public class CdException extends AbstractApplicationException {

    private static final long serialVersionUID = -4730922172179294678L;

    /**
     * Constructs a new CdException with the specified message.
     *
     * @param message The message describing the exception.
     */
    public CdException(String message) {
        super("cd: " + message);
    }
}
