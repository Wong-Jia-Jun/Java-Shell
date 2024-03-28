package sg.edu.nus.comp.cs4218.impl.cmd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;
import sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandlerFactory;

import java.io.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Integration tests for functionality of the {@link SequenceCommand} class.
 * Integrating with the Command class
 */
public class SequenceCommandIT { //NOPMD - suppressed ClassNamingConventions - As per teaching team's request
    private static final ArgumentResolver ARG_RESOLVER = new ArgumentResolver();
    private static final ApplicationRunner APP_RUNNER = new ApplicationRunner();
    public static final String HELLO_WORLD = "Hello World";
    private CallCommand echoCmd = null;
    private PipeCommand pipedCmd = null;
    private CallCommand lsCmd = null;

    @BeforeEach
    void setup() throws ShellException {
        echoCmd = new CallCommand(List.of("echo", HELLO_WORLD), APP_RUNNER, ARG_RESOLVER, new IORedirectionHandlerFactory());
        CallCommand sortCmd = new CallCommand(List.of("sort"), APP_RUNNER, ARG_RESOLVER, new IORedirectionHandlerFactory());
        lsCmd = new CallCommand(List.of("ls", "./src"), APP_RUNNER, ARG_RESOLVER, new IORedirectionHandlerFactory());
        pipedCmd = new PipeCommand(List.of(lsCmd, sortCmd));
    }

    @Test
    void evaluate_CallCommand_evaluatesSuccess() throws ShellException, FileNotFoundException, AbstractApplicationException {
        SequenceCommand sequenceCommand = new SequenceCommand(List.of(echoCmd, lsCmd));
        OutputStream outputStream = new ByteArrayOutputStream();
        sequenceCommand.evaluate(System.in, outputStream);
        String expectedResult = HELLO_WORLD + System.lineSeparator() +
                "src:" + System.lineSeparator() +
                "main" + System.lineSeparator() +
                "test" + System.lineSeparator();
        assertEquals(expectedResult, outputStream.toString());
    }

    @Test
    void evaluate_PipedAndCallCommand_evaluatesSuccess() throws ShellException, FileNotFoundException, AbstractApplicationException {
        SequenceCommand sequenceCommand = new SequenceCommand(List.of(echoCmd, pipedCmd, echoCmd));
        OutputStream outputStream = new ByteArrayOutputStream();
        sequenceCommand.evaluate(System.in, outputStream);
        String expectedResult = HELLO_WORLD + System.lineSeparator() +
                "main" + System.lineSeparator() +
                "src:" + System.lineSeparator() +
                "test" + System.lineSeparator() +
                HELLO_WORLD + System.lineSeparator();
        assertEquals(expectedResult, outputStream.toString());
    }
}
