package sg.edu.nus.comp.cs4218.exception;

/**
 * Exception thrown by the wc command.
 *
 * This exception is specific to the wc command and extends {@link AbstractApplicationException}.
 * It adds the prefix "wc: " to the error message to indicate that it originated from the wc command.
 */
public class WcException extends AbstractApplicationException {

    private static final long serialVersionUID = -8535567786679220113L;

    /**
     * Constructs a new WcException with the specified message.
     *
     * @param message The message describing the exception.
     */
    public WcException(String message) {
        super("wc: " + message);
    }

    /**
     * Constructs a new WcException with the specified exception and message.
     *
     * @param exception The exception causing the error.
     * @param message   The message describing the exception.
     */
    public WcException(Exception exception, String message) {
        super("wc: " + message);
    }

    /**
     * Constructs a new WcException with the message from the specified exception.
     *
     * @param exception The exception causing the error.
     */
    public WcException(Exception exception) {
        super("wc: " + exception.getMessage());
    }
}
