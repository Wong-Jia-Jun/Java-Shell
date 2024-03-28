package sg.edu.nus.comp.cs4218.exception;

/**
 * Exception thrown by the tee command.
 *
 * This exception is specific to the tee command and extends {@link AbstractApplicationException}.
 * It adds the prefix "tee: " to the error message to indicate that it originated from the tee command.
 */
public class TeeException extends AbstractApplicationException {

    /**
     * Constructs a new TeeException with the specified message.
     *
     * @param message The message describing the exception.
     */
    public TeeException(String message) {
        super("tee: " + message);
    }
}

