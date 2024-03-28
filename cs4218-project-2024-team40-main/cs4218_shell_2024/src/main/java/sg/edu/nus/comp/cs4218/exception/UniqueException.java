package sg.edu.nus.comp.cs4218.exception;

/**
 * Exception thrown by the uniq command.
 *
 * This exception is specific to the uniq command and extends {@link AbstractApplicationException}.
 * It adds the prefix "uniq: " to the error message to indicate that it originated from the uniq command.
 */
public class UniqueException extends AbstractApplicationException {

    /**
     * Constructs a new UniqueException with the specified message.
     *
     * @param message The message describing the exception.
     */
    public UniqueException(String message) {
        super("uniq: " + message);
    }
}

