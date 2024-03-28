package sg.edu.nus.comp.cs4218.impl.cmd;

import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.*;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;


/**
 * A Call Command is a sub-command consisting of at least one non-keyword or quoted.
 * <p>
 * Command format: (<non-keyword> or <quoted>) *
 */
public class CallCommand implements Command {
    private final List<String> argsList;
    private final ApplicationRunner appRunner;
    private final ArgumentResolver argumentResolver;
    private final IORedirectionHandlerFactory ioRedirFact;

    public CallCommand(List<String> argsList, ApplicationRunner appRunner, ArgumentResolver argumentResolver,
            IORedirectionHandlerFactory ioRedirFact) throws ShellException {
        if (appRunner == null || argumentResolver == null) {
            throw new ShellException(ERR_NULL_ARGS);
        }

        this.argsList = argsList;
        this.appRunner = appRunner;
        this.argumentResolver = argumentResolver;
        this.ioRedirFact = ioRedirFact;
    }
    /**
     * Evaluates command using data provided through stdin stream. Write result to stdout stream.
     * @throws ShellException, when null/empty args or other  exception when error reading stdin
     */
    @Override
    public void evaluate(InputStream stdin, OutputStream stdout)
            throws AbstractApplicationException, ShellException, FileNotFoundException {
        if (argsList == null || argsList.isEmpty()) {
            throw new ShellException(ERR_SYNTAX);
        }
        if(stdin == null || stdout == null){
            throw new ShellException(ERR_NULL_STREAMS);
        }

        // Handle IO redirection
        IORedirectionHandler redirHandler = ioRedirFact.createIORedirectionHandler(argsList, stdin, stdout,
                argumentResolver);
        redirHandler.extractRedirOptions();
        List<String> noRedirArgsList = redirHandler.getNoRedirArgsList();
        InputStream inputStream = redirHandler.getInputStream(); //NOPMD - dont want to close right after as shell wants to keep taking from System.in
        OutputStream outputStream = redirHandler.getOutputStream();//NOPMD - dont want to close right after as shell wants to keep writing to System.out

        // Handle quoting + globing + command substitution
        List<String> parsedArgsList = argumentResolver.parseArguments(noRedirArgsList);
        //fixed bug here!!
        if (!parsedArgsList.isEmpty()) {
            String app = parsedArgsList.remove(0);
            appRunner.runApp(app, parsedArgsList.toArray(new String[0]), inputStream, outputStream);
        }

    }

    @Override
    public void terminate() {
        // Unused for now
    }

    public List<String> getArgsList() {
        return argsList;
    }
}
