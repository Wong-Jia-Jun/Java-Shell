package sg.edu.nus.comp.cs4218.impl.util;

import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_MULT_STREAMS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_REDIR_INPUT;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_REDIR_OUTPUT;

/**
 * Utility class for handling input and output redirection in shell commands.
 */
public class IORedirectionHandler {
    private final List<String> argsList;
    private final ArgumentResolver argumentResolver;
    private  InputStream origInputStream;
    private  OutputStream origOutputStream;
    private List<String> noRedirArgsList;
    private InputStream inputStream;
    private OutputStream outputStream;


    public IORedirectionHandler(List<String> argsList, InputStream origInputStream,
                                OutputStream origOutputStream, ArgumentResolver argumentResolver) {
        this.argsList = argsList;
        this.inputStream = origInputStream;
        this.origInputStream = origInputStream;
        this.outputStream = origOutputStream;
        this.origOutputStream = origOutputStream;
        this.argumentResolver = argumentResolver;
    }

    /**
     * Extracts input and output redirection options from the command arguments list.
     *
     * @throws AbstractApplicationException If an application exception occurs.
     * @throws ShellException               If a shell-related exception occurs.
     * @throws FileNotFoundException       If a file specified in the arguments is not found.
     */
    @SuppressWarnings("PMD.ExcessiveMethodLength")// - May split up method if time allows
    public void extractRedirOptions() throws AbstractApplicationException, ShellException, FileNotFoundException {

        if (argsList == null || argsList.isEmpty()) {
            throw new ShellException(ERR_SYNTAX);
        }

        noRedirArgsList = new LinkedList<>();
        ListIterator<String> argsIterator = argsList.listIterator();
        while (argsIterator.hasNext()) {
            String arg = argsIterator.next();


            if (!isRedirOperator(arg)) {
                noRedirArgsList.add(arg);
                continue;
            }


            String file = argsIterator.next();

            if (isRedirOperator(file)) {
                throw new ShellException(ERR_SYNTAX);
            }


            // handle quoting + globing + command substitution in file arg
            List<String> fileSegment = argumentResolver.resolveOneArgument(file, new RegexArgument());

            if (fileSegment.size() > 1) {

                throw new ShellException(ERR_SYNTAX);
            }
            file = fileSegment.get(0);

            if (arg.equals(String.valueOf(CHAR_REDIR_INPUT))) {
                IOUtils.closeInputStream(inputStream);

                if (!inputStream.equals(origInputStream)) { // Already have a stream
                    throw new ShellException(ERR_MULT_STREAMS);
            }

                    inputStream = IOUtils.openInputStream(file);
//                     fixed bug here by reassigning orig to allow multiple non conseq < in one command
                    origInputStream =inputStream;



            } else if (arg.equals(String.valueOf(CHAR_REDIR_OUTPUT))) {
                IOUtils.closeOutputStream(outputStream);
                if (!outputStream.equals(origOutputStream)) { // Already have a stream
                    throw new ShellException(ERR_MULT_STREAMS);
                }
                outputStream = IOUtils.openOutputStream(file);
//                 fixed bug here by reassigning orig to allow multiple non conseq > in one command
                origOutputStream = outputStream;

            }
        }
    }

    /**
     * Returns the list of arguments without redirection operators.
     *
     * @return The list of arguments without redirection operators.
     */
    public List<String> getNoRedirArgsList() {
        return noRedirArgsList;
    }

    /**
     * Returns the input stream after redirection.
     *
     * @return The input stream after redirection.
     */
    public InputStream getInputStream() {
        return inputStream;
    }

    /**
     * Returns the output stream after redirection.
     *
     * @return The output stream after redirection.
     */
    public OutputStream getOutputStream() {
        return outputStream;
    }

    /**
     * Checks if a string represents a redirection operator.
     *
     * @param str The string to check.
     * @return True if the string is a redirection operator, false otherwise.
     */
    boolean isRedirOperator(String str) {
        return str.equals(String.valueOf(CHAR_REDIR_INPUT)) || str.equals(String.valueOf(CHAR_REDIR_OUTPUT));
    }
}
