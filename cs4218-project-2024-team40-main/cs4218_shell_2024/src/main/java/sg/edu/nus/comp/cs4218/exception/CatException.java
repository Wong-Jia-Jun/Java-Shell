package sg.edu.nus.comp.cs4218.exception;

/**
 * Exception thrown by the cat command.
 *
 * This exception is specific to the cat command and extends {@link AbstractApplicationException}.
 * It adds the prefix "cat: " to the error message to indicate that it originated from the cat command.
 */
public class CatException extends AbstractApplicationException {

    private static final long serialVersionUID = 2333796686823942499L;

    /**
     * Constructs a new CatException with the specified message.
     *
     * @param message The message describing the exception.
     */
    public CatException(String message) {
        super("cat: " + message);
    }

    /**
     * Constructs a new CatException with the specified message and cause.
     *
     * @param message The message describing the exception.
     * @param cause   The cause of the exception.
     */
    public CatException(String message, Throwable cause) {
        super(String.format("cat: %s: %s", message, cause.getMessage()));
    }

    /**
     * Constructs a new CatException with the specified message and file.
     * @param message
     * @param file
     */
    public CatException(String message, String file) {
        super("cat: " + file + ": " + message);
    }

    /**
     * Constructs a new CatException with the specified exception.
     *
     * @param exception The exception that is thrown.
     */
    public CatException(Exception exception) {
        super("cat: " + exception.getMessage());
    }
}
