package sg.edu.nus.comp.cs4218.exception;

/**
 * Exception thrown by the cut command.
 *
 * This exception is specific to the cut command and extends {@link AbstractApplicationException}.
 * It adds the prefix "cut: " to the error message to indicate that it originated from the cut command.
 */
public class CutException extends AbstractApplicationException {

    private static final long serialVersionUID = -3332160063321214849L;

    /**
     * Constructs a new CutException with the specified message.
     *
     * @param message The message describing the exception.
     */
    public CutException(String message) {
        super("cut: " + message);
    }

    /**
     * Constructs a new CutException with the specified message and cause.
     *
     * @param message The message describing the exception.
     * @param cause   The cause of the exception.
     */
    public CutException(String message, Throwable cause) {
        super(String.format("cut: %s: %s", message, cause.getMessage()));
    }

    /**
     * Constructs a new CutException with the specified message and file.
     * @param message
     * @param file
     */
    public CutException(String message, String file) {
        super("cut: " + file + ": " + message);
    }

    /**
     * Constructs a new CutException with the specified exception.
     *
     * @param exception The exception that is thrown.
     */
    public CutException(Exception exception) {
        super("cut: " + exception.getMessage());
    }
}
