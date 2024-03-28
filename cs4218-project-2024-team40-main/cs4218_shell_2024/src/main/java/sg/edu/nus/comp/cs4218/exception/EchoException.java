package sg.edu.nus.comp.cs4218.exception;

/**
 * Exception thrown by the echo command.
 *
 * This exception is specific to the echo command and extends {@link AbstractApplicationException}.
 * It adds the prefix "echo: " to the error message to indicate that it originated from the echo command.
 */
public class EchoException extends AbstractApplicationException {

    private static final long serialVersionUID = 7499486452467089104L;

    /**
     * Constructs a new EchoException with the specified message.
     *
     * @param message The message describing the exception.
     */
    public EchoException(String message) {
        super("echo: " + message);
    }
}
