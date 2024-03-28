package sg.edu.nus.comp.cs4218.impl.cmd.stubs;

import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;
import sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandlerFactory;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class CallCommandStub extends CallCommand {
    public CallCommandStub() throws ShellException {
        super(new ArrayList<>(List.of("echo", "hello")), new ApplicationRunner(), new ArgumentResolver(),
                new IORedirectionHandlerFactory());
    }

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
