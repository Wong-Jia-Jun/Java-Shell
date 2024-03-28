package sg.edu.nus.comp.cs4218.exception;

/**
 * Exception used to send a signal to the shell to exit.
 *
 * This exception extends {@link AbstractApplicationException} and is specifically used to signal
 * the shell to exit with a specified exit code. The exit code is passed as the message parameter
 * to the constructor. The prefix "exit: " is added to the error message to indicate that it originated
 * from the exit command.
 */
public class ExitException extends AbstractApplicationException {
    private static final long serialVersionUID = 6517503252362314995L;

    /**
     * Constructs a new ExitException with the specified message.
     *
     * @param message The exit code.
     */
    public ExitException(String message) {
        super("exit: " + message);
    }
}

