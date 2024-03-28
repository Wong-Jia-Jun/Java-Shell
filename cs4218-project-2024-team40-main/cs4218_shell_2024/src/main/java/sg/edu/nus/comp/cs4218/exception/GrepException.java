package sg.edu.nus.comp.cs4218.exception;

/**
 * Exception thrown by the grep command.
 *
 * This exception is specific to the grep command and extends {@link AbstractApplicationException}.
 * It adds the prefix "grep: " to the error message to indicate that it originated from the grep command.
 */
public class GrepException extends AbstractApplicationException {

    private static final long serialVersionUID = -5883292222072101576L;
    private static final String GREP = "grep: ";

    /**
     * Constructs a new GrepException with the specified message.
     *
     * @param message The message describing the exception.
     */
    public GrepException(String message) {
        super(GREP + message);
    }

    /**
     * Constructs a new GrepException with the specified cause.
     * @param cause
     */
    public GrepException(Exception cause) {
        super(GREP + cause.getMessage());
    }

    /**
     * Constructs a new GrepException with the specified message and file.
     * @param message
     * @param file
     */
    public GrepException(String message, String file) {
        super(GREP + file + ": " + message);
    }

    /**
     * Constructs a new GrepException with the specified file and cause.
     * @param file
     * @param cause
     */
    public GrepException(String file, Exception cause) {
        super(GREP + file + ": " + cause.getMessage());
    }
}
