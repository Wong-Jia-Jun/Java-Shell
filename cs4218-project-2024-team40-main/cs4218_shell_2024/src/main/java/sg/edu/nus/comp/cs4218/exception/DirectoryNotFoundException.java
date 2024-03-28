package sg.edu.nus.comp.cs4218.exception;

/**
 * Exception thrown when a directory is not found.
 *
 * This exception indicates that a directory specified by the user or application could not be found.
 * It extends {@link Exception} and provides a constructor to initialize the exception message.
 */
public class DirectoryNotFoundException extends Exception {

    private static final long serialVersionUID = 9208237916723540057L;

    /**
     * Constructs a new DirectoryNotFoundException with the specified message.
     *
     * @param message The message describing the exception.
     */
    public DirectoryNotFoundException(String message) {
        super(message);
    }
}

