package ef2test;

import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CutException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;
import sg.edu.nus.comp.cs4218.impl.cmd.PipeCommand;
import sg.edu.nus.comp.cs4218.impl.parser.CutArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;
import sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandlerFactory;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class PipeCommandIT { //NOPMD
    private static final String PATH_FIRST = "src";
    private static final String[] FILE_PATH = {"test", "resources", "cmd", "PipeCommandIT"};
    private static final List<String> CAT_STDIN_ARGS = new ArrayList<>(List.of("cat", "-"));
    private static final List<String> ECHO_STDIN_ARGS = new ArrayList<>(List.of("echo", "hello world"));
    private static final List<String> CUT_FAIL_ARGS = new ArrayList<>(List.of("cut", "a.txt"));
    private static final List<String> CUT_IO_RED = new ArrayList<>(List.of("cut", "-c", "1-5", "-", "<", "hello.txt"));
    private static final String RES_STR = "hello world" + System.lineSeparator();
    private static final String CUT_RES = "hello" + System.lineSeparator();
    private static final List<String> CD_ARGS = new ArrayList<>(List.of("cd"));

    @Test
    void evaluate_TypicalCase_Success() throws ShellException, FileNotFoundException, AbstractApplicationException {
        ByteArrayInputStream input = new ByteArrayInputStream(new byte[]{});
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PipeCommand pipeCommand = new PipeCommand(List.of(new CallCommand(ECHO_STDIN_ARGS, new ApplicationRunner(), new ArgumentResolver(), new IORedirectionHandlerFactory()), new CallCommand(CAT_STDIN_ARGS, new ApplicationRunner(), new ArgumentResolver(), new IORedirectionHandlerFactory())));
        pipeCommand.evaluate(input, out);
        assertEquals(RES_STR, out.toString());
    }

    @Test
    void evaluate_FailThenSuccess_ThrowsErrorSecondIsTerminatedNotEvaluated() throws ShellException, FileNotFoundException, AbstractApplicationException {
        ByteArrayInputStream input = new ByteArrayInputStream(new byte[]{});
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CallCommand spySuccessCommand = spy(new CallCommand(CAT_STDIN_ARGS, new ApplicationRunner(), new ArgumentResolver(), new IORedirectionHandlerFactory()));
        PipeCommand pipeCommand = new PipeCommand(List.of(new CallCommand(CUT_FAIL_ARGS, new ApplicationRunner(), new ArgumentResolver(), new IORedirectionHandlerFactory()), spySuccessCommand));
        Throwable exp = assertThrows(CutException.class,
                () -> {pipeCommand.evaluate(input, out);});
        assertEquals(new CutException(new IllegalArgumentException(CutArgsParser.ILLEGAL_MSS_FLAG)).getMessage(), exp.getMessage());
        verify(spySuccessCommand, never()).evaluate(any(InputStream.class), any(OutputStream.class));
        verify(spySuccessCommand, times(1)).terminate();
    }

    @Test
    void evaluate_TypicalWithIORedirection_Success() throws ShellException, FileNotFoundException, AbstractApplicationException {
        String originalDir = Environment.currentDirectory;
        Environment.currentDirectory = Paths.get(PATH_FIRST, FILE_PATH).toAbsolutePath().toString();
        ByteArrayInputStream input = new ByteArrayInputStream(new byte[]{});
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PipeCommand pipeCommand = new PipeCommand(List.of(new CallCommand(CUT_IO_RED, new ApplicationRunner(), new ArgumentResolver(), new IORedirectionHandlerFactory()), new CallCommand(CAT_STDIN_ARGS, new ApplicationRunner(), new ArgumentResolver(), new IORedirectionHandlerFactory())));
        pipeCommand.evaluate(input, out);
        assertEquals(CUT_RES, out.toString());
        Environment.currentDirectory = originalDir;
    }

    @Test
    void evaluate_FirstOutputsSecondNoOutput_NoOutput() throws ShellException, FileNotFoundException, AbstractApplicationException {
        ByteArrayInputStream input = new ByteArrayInputStream(new byte[]{});
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PipeCommand pipeCommand = new PipeCommand(List.of(new CallCommand(ECHO_STDIN_ARGS, new ApplicationRunner(), new ArgumentResolver(), new IORedirectionHandlerFactory()), new CallCommand(CD_ARGS, new ApplicationRunner(), new ArgumentResolver(), new IORedirectionHandlerFactory())));
        pipeCommand.evaluate(input, out);
        assertEquals("", out.toString());
    }
}
