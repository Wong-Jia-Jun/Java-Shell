package sg.edu.nus.comp.cs4218.exception;

/**
 * Exception thrown by the mv command.
 * This exception is specific to the mv command and extends {@link AbstractApplicationException}.
 * It adds the prefix "mv: " to the error message to indicate that it originated from the mv command.
 */
public class MvException extends AbstractApplicationException {
    /**
     * Constructs a new MvException with the specified message.
     *
     * @param message The message describing the exception.
     */
    public MvException(String message) {
        super("mv: " + message);
    }

    /**
     * Constructs a new MvException with the specified filename and message.
     *
     * @param filename The filename causing the exception.
     * @param message  The message describing the exception.
     */
    public MvException(String filename, String message) {
        super("mv: cannot stat " + filename + ": " +message);
    }

    /**
     * Constructs a new MvException with the specified exception
     *
     * @param exception The exception.
     */
    public MvException(Exception exception) {
        super("mv: " + exception.getMessage());
    }
}
