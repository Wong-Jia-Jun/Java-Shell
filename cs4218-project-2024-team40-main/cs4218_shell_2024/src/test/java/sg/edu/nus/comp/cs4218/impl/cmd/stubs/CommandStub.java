package sg.edu.nus.comp.cs4218.impl.cmd.stubs;

import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class CommandStub implements Command {
    @Override
    public void evaluate(InputStream stdin, OutputStream stdout)
            throws AbstractApplicationException, ShellException {
        try {
            List<String> lines = IOUtils.getLinesFromInputStream(stdin);
            for (String line : lines) {
                stdout.write(line.getBytes());
            }
            stdout.write(System.lineSeparator().getBytes());
        } catch (IOException e) {
            throw new ShellException(e);
        }
    }

    @Override
    public void terminate() {
        // Unused for now
    }
}
