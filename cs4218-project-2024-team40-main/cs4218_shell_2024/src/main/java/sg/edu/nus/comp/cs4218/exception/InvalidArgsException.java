package sg.edu.nus.comp.cs4218.exception;

/**
 * Exception thrown when invalid arguments are provided.
 *
 * This exception indicates that invalid arguments were provided to a command or method.
 * It extends {@link Exception} and provides constructors to initialize the exception message
 * and optionally specify a cause.
 */
public class InvalidArgsException extends Exception {

    /**
     * Constructs a new InvalidArgsException with the specified detail message.
     *
     * @param message The detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     */
    public InvalidArgsException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidArgsException with the specified detail message and cause.
     *
     * @param message The detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     * @param cause   The cause (which is saved for later retrieval by the {@link #getCause()} method).
     *                (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public InvalidArgsException(String message, Throwable cause) {
        super(message, cause);
    }
}

