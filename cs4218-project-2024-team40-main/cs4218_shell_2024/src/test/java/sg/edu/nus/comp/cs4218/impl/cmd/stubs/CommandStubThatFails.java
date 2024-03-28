package sg.edu.nus.comp.cs4218.impl.cmd.stubs;

import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.exception.ShellException;

import java.io.InputStream;
import java.io.OutputStream;

public class CommandStubThatFails implements Command {
    @Override
    public void evaluate(InputStream stdin, OutputStream stdout)
            throws ShellException {
        throw new ShellException("Error");
    }

    @Override
    public void terminate() {
        // Unused for now
    }
}
