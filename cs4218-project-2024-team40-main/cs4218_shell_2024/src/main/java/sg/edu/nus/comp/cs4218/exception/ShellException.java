package sg.edu.nus.comp.cs4218.exception;

/**
 * Exception thrown by the shell.
 *
 * This exception is specific to the shell and extends {@link Exception}.
 * It adds the prefix "shell: " to the error message to indicate that it originated from the shell.
 */
public class ShellException extends Exception {

    private static final long serialVersionUID = -4439395674558704575L;

    /**
     * Constructs a new ShellException with the specified message.
     *
     * @param message The message describing the exception.
     */
    public ShellException(String message) {
        super("shell: " + message);
    }

    /**
     * Constructs a new ShellException with the message from the specified exception.
     *
     * @param exception The exception causing the error.
     */
    public ShellException(Exception exception) {
        super("shell: " + exception.getMessage());
    }
}
