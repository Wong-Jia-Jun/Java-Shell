package sg.edu.nus.comp.cs4218.impl.cmd.stubs;

import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ExitException;

import java.io.InputStream;
import java.io.OutputStream;

public class ExitCommandStub implements Command {
    @Override
    public void evaluate(InputStream stdin, OutputStream stdout)
            throws AbstractApplicationException {
        throw new ExitException("exit");
    }

    @Override
    public void terminate() {
        // Unused for now
    }
}
