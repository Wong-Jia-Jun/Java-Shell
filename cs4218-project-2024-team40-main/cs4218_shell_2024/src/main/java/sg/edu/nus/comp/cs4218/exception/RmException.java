package sg.edu.nus.comp.cs4218.exception;

/**
 * Exception thrown by the rm command.
 *
 * This exception is specific to the rm command and extends {@link AbstractApplicationException}.
 * It adds the prefix "rm: " to the error message to indicate that it originated from the rm command.
 */
public class RmException extends AbstractApplicationException {

    private static final long serialVersionUID = 6616752571518808461L;
    private static final String RM_PREFIX = "rm: ";

    /**
     * Constructs a new RmException with the specified message.
     *
     * @param message The message describing the exception.
     */
    public RmException(String message) {
        super(RM_PREFIX + message);
    }

    /**
     * Constructs a new RmException with the specified exception.
     *
     * @param exception The exception causing the error.
     */
    public RmException(Exception exception) {
        super(RM_PREFIX + exception.getMessage());
    }

    /**
     * Constructs a new RmException with the specified path and exception.
     *
     * @param path      The path of the file or directory causing the error.
     * @param exception The exception causing the error.
     */
    public RmException(String path, Exception exception) {
        super(RM_PREFIX + String.format("cannot remove '%s': %s", path, exception.getMessage()));
    }

    /**
     * Constructs a new RmException with the specified path and exception message.
     *
     * @param path              The path of the file or directory causing the error.
     * @param exceptionMessage The message describing the exception.
     */
    public RmException(String path, String exceptionMessage) {
        super(RM_PREFIX + String.format("cannot remove '%s': %s", path, exceptionMessage));
    }
}
