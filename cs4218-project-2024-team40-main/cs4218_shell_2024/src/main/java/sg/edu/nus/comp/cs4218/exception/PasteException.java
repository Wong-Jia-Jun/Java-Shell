package sg.edu.nus.comp.cs4218.exception;

/**
 * Exception thrown by the paste command.
 *
 * This exception is specific to the paste command and extends {@link AbstractApplicationException}.
 * It adds the prefix "paste: " to the error message to indicate that it originated from the paste command.
 */
public class PasteException extends AbstractApplicationException {

    private static final long serialVersionUID = -742723164724927309L;

    /**
     * Constructs a new PasteException with the specified message.
     *
     * @param message The message describing the exception.
     */
    public PasteException(String message) {
        super("paste: " + message);
    }

    /**
     * Constructs a new PasteException with the specified message and cause.
     *
     * @param message The message describing the exception.
     * @param file File that caused the exception.
     */
    public PasteException(String file, String message) {
        super("paste: " + file + ": " + message);
    }

    /**
     * Constructs a new PasteException with the specified exception.
     *
     * @param exception The exception that is thrown.
     */
    public PasteException(Exception exception) {
        super("paste: " + exception.getMessage());
    }
}

