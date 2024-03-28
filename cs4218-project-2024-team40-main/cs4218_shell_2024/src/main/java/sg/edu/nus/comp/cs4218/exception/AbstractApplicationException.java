package sg.edu.nus.comp.cs4218.exception;

/**
 * An abstract class that serves as the base class for application exceptions.
 *
 * Subclasses of this class should extend its functionality by providing specific implementations
 * for their respective purposes. They can add additional constructors, methods, and fields as needed.
 */
public abstract class AbstractApplicationException extends Exception {

    private static final long serialVersionUID = -6276854591710517685L;

    /**
     * Constructs a new AbstractApplicationException with the specified message.
     *
     * @param message The message describing the exception.
     */
    public AbstractApplicationException(String message) {
        super(message);
    }
}
