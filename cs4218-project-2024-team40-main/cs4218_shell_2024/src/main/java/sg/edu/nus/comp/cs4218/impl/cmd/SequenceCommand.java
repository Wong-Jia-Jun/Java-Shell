package sg.edu.nus.comp.cs4218.impl.cmd;

import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ExitException;
import sg.edu.nus.comp.cs4218.exception.ShellException;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

/**
 * A Sequence Command is a sub-command consisting of two Commands separated with a semicolon.
 * <p>
 * Command format: <Command> ; <Command>
 */
public class SequenceCommand implements Command {
    private final List<Command> commands;

    public SequenceCommand(List<Command> commands) throws ShellException {
        if (commands == null) {
            throw new ShellException(ERR_NULL_ARGS);
        }
        this.commands = commands;
    }

    @Override
    public void evaluate(InputStream stdin, OutputStream stdout)
            throws AbstractApplicationException, ShellException, FileNotFoundException {
        ExitException exitException = null;

        for (Command command : commands) {
            try {
                OutputStream outputStream = new ByteArrayOutputStream();
                command.evaluate(stdin, outputStream);

                String outputLine = outputStream.toString();
                if (!outputLine.isEmpty()) {
                    printOutputLines(outputLine, stdout);
                }
            } catch (ExitException e) {
                exitException = e;
                break;
            } catch (AbstractApplicationException | ShellException e) {
                printOutputLines(e.getMessage() + STRING_NEWLINE, stdout);
            }
        }

        if (exitException != null) {
            throw exitException;
        }
    }

    @Override
    public void terminate() {
        // Unused for now
    }

    public List<Command> getCommands() {
        return commands;
    }

    private void printOutputLines(String line, OutputStream stdout) throws ShellException {
        try {
            stdout.write(line.getBytes());
        } catch (IOException e) {
            throw new ShellException(e.getMessage());//NOPMD - throw here to get caught in SHell impl and show excpt msg
        }
    }
}
