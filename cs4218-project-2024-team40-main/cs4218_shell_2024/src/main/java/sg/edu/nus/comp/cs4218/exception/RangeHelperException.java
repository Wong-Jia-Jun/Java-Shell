package sg.edu.nus.comp.cs4218.exception;

/**
 * Exception thrown by the RangeHelper class.
 *
 * This exception is specific to the RangeHelper class and extends {@link Exception}.
 * It adds the prefix "RangeHelper: " to the error message to indicate that it originated from the RangeHelper class.
 */
public class RangeHelperException extends Exception {

    private static final long serialVersionUID = 2333796686823942499L;

    /**
     * Constructs a new RangeHelperException with the specified message.
     *
     * @param message The message describing the exception.
     */
    public RangeHelperException(String message) {
        super("RangeHelper: " + message);
    }
}

