package sg.edu.nus.comp.cs4218.exception;

/**
 * Exception thrown by the sort command.
 *
 * This exception is specific to the sort command and extends {@link AbstractApplicationException}.
 * It adds the prefix "sort: " to the error message to indicate that it originated from the sort command.
 */
public class SortException extends AbstractApplicationException {

    private static final long serialVersionUID = 3894758187716957490L;
    public static final String INVALID_CMD = "Invalid command code.";
    public static final String PROB_SORT_FILE = "Problem sort from file: ";
    public static final String PROB_SORT_STDIN = "Problem sort from stdin: ";

    /**
     * Constructs a new SortException with the specified message.
     *
     * @param message The message describing the exception.
     */
    public SortException(String message) {
        super("sort: " + message);
    }

    /**
     * Constructs a new SortException with the specified exception.
     *
     * @param exception The exception causing the error.
     */
    public SortException(Exception exception) {
        super("sort: " + exception.getMessage());
    }

    /**
     * Constructs a new SortException with the specified file and message.
     * @param file
     * @param message
     */
    public SortException(String file, String message) {
        super("sort: " + file + ": " + message);
    }
}
