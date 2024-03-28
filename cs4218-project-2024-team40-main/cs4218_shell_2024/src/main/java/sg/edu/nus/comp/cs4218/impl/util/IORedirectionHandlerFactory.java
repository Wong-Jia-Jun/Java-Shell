package sg.edu.nus.comp.cs4218.impl.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
/**
 * The IORedirectionHandlerFactory class is responsible for creating instances of
 * {@link IORedirectionHandler} based on the provided parameters.
 */
public class IORedirectionHandlerFactory {
    /**
     * Creates a new instance of IORedirectionHandler with the specified parameters.
     *
     * @param argsList          The list of command arguments.
     * @param input             The input stream.
     * @param output            The output stream.
     * @param argumentResolver  The argument resolver instance.
     * @return A new instance of IORedirectionHandler.
     */
    public IORedirectionHandler createIORedirectionHandler(List<String> argsList, InputStream input,
            OutputStream output, ArgumentResolver argumentResolver) {
        return new IORedirectionHandler(argsList, input, output, argumentResolver);
    }
}
