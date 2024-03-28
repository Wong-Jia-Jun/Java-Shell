package sg.edu.nus.comp.cs4218.impl.cmd;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;

import java.io.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandler;
import sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandlerFactory;


import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static org.mockito.Mockito.*;

/**
 * Tests Functionality of Call Command
 */
public class CallCommandTest {

    /**
     * The Call command.
     */
    CallCommand callCommand;
    /**
     * The Args list.
     */
    List<String> argsList;

    private InputStream inputStream;
    private OutputStream outputStream;

    private ApplicationRunner appRunnerStub;
    private ArgumentResolver argResolverStub;
    private IORedirectionHandlerFactory ioRedirectFactory;
    private static final String ORIGINAL_DIR = Environment.currentDirectory;
    /**
     * The constant ECHO_COMMAND.
     */
    public static final String ECHO_COMMAND = "echo";

    /**
     * Sets up each.
     */
    @BeforeEach
    void setUpEach() {
        argsList = new ArrayList<>();
        inputStream = mock(InputStream.class);
        outputStream = mock(OutputStream.class);
        appRunnerStub = mock(ApplicationRunner.class);
        argResolverStub = mock(ArgumentResolver.class);
        ioRedirectFactory = mock(IORedirectionHandlerFactory.class);
        IORedirectionHandler ioRedirectHandler = mock(IORedirectionHandler.class);
        when(ioRedirectFactory.createIORedirectionHandler(anyList(), any(InputStream.class),
                any(OutputStream.class), any(ArgumentResolver.class)))
                .thenReturn(ioRedirectHandler);
        when(ioRedirectHandler.getInputStream()).thenReturn(inputStream);
        when(ioRedirectHandler.getOutputStream()).thenReturn(outputStream);
        when(ioRedirectHandler.getNoRedirArgsList()).thenReturn(argsList);
    }

    /**
     * Tear down each.
     */
    @AfterEach
    void tearDownEach() {
        argsList.clear();
    }

    /**
     * Evaluate valid args list calls app runner with app and args.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws ShellException               the shell exception
     * @throws FileNotFoundException        the file not found exception
     */
//Positive test cases add on after implementing commands correctly
    @Test
    void evaluate_ValidArgsList_CallsAppRunnerWithAppAndArgs()
            throws AbstractApplicationException, ShellException, FileNotFoundException {
        argsList.add("test");
        argsList.add("arg");
        when(argResolverStub.parseArguments(anyList())).thenReturn(argsList);
        callCommand = new CallCommand(argsList, appRunnerStub, argResolverStub, ioRedirectFactory);
        callCommand.evaluate(inputStream, outputStream);
        String[] args = {"arg"};
        verify(appRunnerStub).runApp("test", args, inputStream, outputStream);
    }


    /**
     * Evaluate null args throw shell exception.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws ShellException               the shell exception
     * @throws FileNotFoundException        the file not found exception
     */
//Negative test cases
    @Test
    void evaluate_NullArgs_ThrowShellException() throws AbstractApplicationException, ShellException, FileNotFoundException {
        when(argResolverStub.parseArguments(null)).thenReturn(argsList);
        callCommand = new CallCommand(null, appRunnerStub, argResolverStub, ioRedirectFactory);
        assertThrows(ShellException.class, () -> callCommand.evaluate(inputStream, outputStream), ERR_SYNTAX);
    }

    /**
     * Evaluate empty args list throw shell exception.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws ShellException               the shell exception
     * @throws FileNotFoundException        the file not found exception
     */
    @Test
    void evaluate_EmptyArgsList_ThrowShellException() throws AbstractApplicationException, ShellException,
            FileNotFoundException{
        when(argResolverStub.parseArguments(argsList)).thenReturn(argsList);
        callCommand = new CallCommand(argsList, appRunnerStub, argResolverStub, ioRedirectFactory);
        assertThrows(ShellException.class, () -> callCommand.evaluate(inputStream, outputStream), ERR_SYNTAX);
    }

    /**
     * Call command test null app runner should throw shell exception.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws ShellException               the shell exception
     * @throws FileNotFoundException        the file not found exception
     */
    @Test
    public void callCommandTest_NullAppRunner_ShouldThrowShellException() throws AbstractApplicationException,
            ShellException, FileNotFoundException {
        when(argResolverStub.parseArguments(argsList)).thenReturn(argsList);
        assertThrows(ShellException.class, () -> new CallCommand(Arrays.asList(ECHO_COMMAND, "testing"),
                null, argResolverStub, ioRedirectFactory), ERR_NULL_ARGS);

    }
}