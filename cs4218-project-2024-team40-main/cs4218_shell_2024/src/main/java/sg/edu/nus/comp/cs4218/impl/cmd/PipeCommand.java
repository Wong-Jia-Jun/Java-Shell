package sg.edu.nus.comp.cs4218.impl.cmd;

import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;

import java.io.*;
import java.util.List;

/**
 * A Pipe Command is a sub-command consisting of two Call Commands separated with a pipe,
 * or a Pipe Command and a Call Command separated with a pipe.
 * <p>
 * Command format: <Call> | <Call> or <Pipe> | <Call>
 */
public class PipeCommand implements Command {
    private final List<CallCommand> callCommands;

    public PipeCommand(List<CallCommand> callCommands) {
        this.callCommands = callCommands;
    }

    @Override
    public void evaluate(InputStream stdin, OutputStream stdout)
            throws AbstractApplicationException, ShellException, FileNotFoundException {
        AbstractApplicationException absAppException = null;
        ShellException shellException = null;
        FileNotFoundException fileNotFndExptn = null;

        InputStream nextInputStream = stdin;
        OutputStream nextOutputStream; //NOPMD - Unimplemented

        for (int i = 0; i < callCommands.size(); i++) {
            CallCommand callCommand = callCommands.get(i);

            if (absAppException != null || shellException != null || fileNotFndExptn != null) {
                callCommand.terminate();
                continue;
            }

            try {
                nextOutputStream = new ByteArrayOutputStream();
                if (i == callCommands.size() - 1) {
                    nextOutputStream = stdout;
                }
                callCommand.evaluate(nextInputStream, nextOutputStream);

                if (i != callCommands.size() - 1) {
                    nextInputStream = new ByteArrayInputStream(((ByteArrayOutputStream) nextOutputStream).toByteArray()); //NOPMD - Unimplemented
                }
            } catch (AbstractApplicationException e) {
                absAppException = e;
            } catch (ShellException e) {
                shellException = e;
            } catch (FileNotFoundException e) {
                fileNotFndExptn = e;
            } // We catch here to satisfy req: "If an exception occurred in any of these parts, the
            // exception is thrown, and the rest of the parts are terminated."
        }

        if (absAppException != null) {
            throw absAppException;
        }
        if (shellException != null) {
            throw shellException;
        }
        if (fileNotFndExptn != null) {
            throw fileNotFndExptn;
        }
    }

    @Override
    public void terminate() {
        // Unused for now
    }

    public List<CallCommand> getCallCommands() {
        return callCommands;
    }
}
