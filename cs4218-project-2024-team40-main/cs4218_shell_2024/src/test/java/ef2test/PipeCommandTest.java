package ef2test;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;
import sg.edu.nus.comp.cs4218.impl.cmd.PipeCommand;
import sg.edu.nus.comp.cs4218.impl.cmd.stubs.CallCommandStub;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class PipeCommandTest {
    private PipeCommand pipeCommand;
    private List<CallCommand> callCommands;
    private final List<String> INPUT_LINES = List.of("Hello", "World");

    @BeforeEach
    void setUp() {
        callCommands = new ArrayList<>();
    }

    @AfterEach
    void tearDown() {
        pipeCommand = null;
        callCommands = null;
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 5})
    void getCallCommands_PassInCallCommandsOnInit_ReturnsCallCommands(int numCommands) {
        CallCommand callCommand = mock(CallCommand.class);
        for (int i = 0; i < numCommands; i++) {
            callCommands.add(callCommand);
        }
        pipeCommand = new PipeCommand(callCommands);
        assertEquals(callCommands, pipeCommand.getCallCommands());
        assertEquals(numCommands, pipeCommand.getCallCommands().size());
    }

    @Nested
    class SuccessfulEvaluations {
        private InputStream inputStream;
        private OutputStream outputStream;

        @BeforeEach
        void setUp() {
            inputStream = mock(InputStream.class);
            outputStream = mock(OutputStream.class);
        }

        @AfterEach
        void tearDown() {
            inputStream = null;
            outputStream = null;
        }

        @Test
        void evaluate_ThreeCmdsSuccess_PrintsCorrectOutput() throws Exception {
            inputStream = new ByteArrayInputStream(("test").getBytes());
            CallCommand success = new CallCommandStub();
            callCommands.add(success);
            callCommands.add(success);
            callCommands.add(success);
            pipeCommand = new PipeCommand(callCommands);
            pipeCommand.evaluate(inputStream, outputStream);
            verify(outputStream, times(1))
                    .write(("test").getBytes());
        }
    }

    @Nested
    class UnsuccessfulEvaluations {
        private InputStream inputStream;
        private OutputStream outputStream;

        @BeforeEach
        void setUp() {
            inputStream = mock(InputStream.class);
            outputStream = mock(OutputStream.class);
        }

        @AfterEach
        void tearDown() {
            inputStream = null;
            outputStream = null;
        }

        @Test
        void evaluate_ThrowsAbstractExp_ThrowsAbstractExp() throws Exception {
            CallCommand abstractExpCmd = mock(CallCommand.class);
            AbstractApplicationException exp = mock(AbstractApplicationException.class);
            doThrow(exp).when(abstractExpCmd).evaluate(any(InputStream.class), any(OutputStream.class));
            callCommands.add(abstractExpCmd);
            pipeCommand = new PipeCommand(callCommands);
            Throwable thrown = assertThrows(AbstractApplicationException.class, () -> {
                pipeCommand.evaluate(inputStream, outputStream);
            });
            assertEquals(exp, thrown);
        }

        @Test
        void evaluate_ThrowsShellExp_ThrowsShellExp() throws Exception {
            CallCommand shellExpCmd = mock(CallCommand.class);
            ShellException exp = mock(ShellException.class);
            doThrow(exp).when(shellExpCmd).evaluate(any(InputStream.class), any(OutputStream.class));
            callCommands.add(shellExpCmd);
            pipeCommand = new PipeCommand(callCommands);
            Throwable thrown = assertThrows(ShellException.class, () -> {
                pipeCommand.evaluate(inputStream, outputStream);
            });
            assertEquals(exp, thrown);
        }

        @Test
        void evaluate_EvaluateThrowsFileNotFoundException_ThrowsFileNotFoundException() throws Exception {
            CallCommand fileExpCmd = mock(CallCommand.class);
            FileNotFoundException exp = mock(FileNotFoundException.class);
            doThrow(exp).when(fileExpCmd).evaluate(any(InputStream.class), any(OutputStream.class));
            callCommands.add(fileExpCmd);
            pipeCommand = new PipeCommand(callCommands);
            Throwable thrown = assertThrows(FileNotFoundException.class, () -> {
                pipeCommand.evaluate(inputStream, outputStream);
            });
            assertEquals(exp, thrown);
        }

        @Test
        void evaluate_TwoCmdsSuccessThenThrowsAbstractException_ThrowsAbstractExceptionAndNothingPrinted()
                throws Exception {
            try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
                mockedStatic.when(() -> IOUtils.getLinesFromInputStream(any(InputStream.class)))
                        .thenReturn(INPUT_LINES);
                CallCommand successCmd = new CallCommandStub();
                CallCommand abstractExpCmd = mock(CallCommand.class);
                AbstractApplicationException exp = mock(AbstractApplicationException.class);
                doThrow(exp).when(abstractExpCmd).evaluate(any(InputStream.class), any(OutputStream.class));
                callCommands.add(successCmd);
                callCommands.add(abstractExpCmd);
                pipeCommand = new PipeCommand(callCommands);
                Throwable thrown = assertThrows(AbstractApplicationException.class, () -> {
                    pipeCommand.evaluate(inputStream, outputStream);
                });
                assertEquals(exp, thrown);
                verify(outputStream, times(0)).write(any(byte[].class));
            }
        }

        @Test
        void evaluate_TwoCmdsSuccessThenThrowsShellException_ThrowsShellExceptionAndNothingPrinted()
                throws Exception {
            try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
                mockedStatic.when(() -> IOUtils.getLinesFromInputStream(any(InputStream.class)))
                        .thenReturn(INPUT_LINES);
                CallCommand successCmd = new CallCommandStub();
                CallCommand shellExpCmd = mock(CallCommand.class);
                ShellException exp = mock(ShellException.class);
                doThrow(exp).when(shellExpCmd).evaluate(any(InputStream.class), any(OutputStream.class));
                callCommands.add(successCmd);
                callCommands.add(shellExpCmd);
                pipeCommand = new PipeCommand(callCommands);
                Throwable thrown = assertThrows(ShellException.class, () -> {
                    pipeCommand.evaluate(inputStream, outputStream);
                });
                assertEquals(exp, thrown);
                verify(outputStream, times(0)).write(any(byte[].class));
            }
        }

        @Test
        void evaluate_TwoCmdsSuccessThenThrowsFileNotFoundException_ThrowsFileNotFoundExceptionAndNothingPrinted()
                throws Exception {
            try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
                mockedStatic.when(() -> IOUtils.getLinesFromInputStream(any(InputStream.class)))
                        .thenReturn(INPUT_LINES);
                CallCommand successCmd = new CallCommandStub();
                CallCommand fileExpCmd = mock(CallCommand.class);
                FileNotFoundException exp = mock(FileNotFoundException.class);
                doThrow(exp).when(fileExpCmd).evaluate(any(InputStream.class), any(OutputStream.class));
                callCommands.add(successCmd);
                callCommands.add(fileExpCmd);
                pipeCommand = new PipeCommand(callCommands);
                Throwable thrown = assertThrows(FileNotFoundException.class, () -> {
                    pipeCommand.evaluate(inputStream, outputStream);
                });
                assertEquals(exp, thrown);
                verify(outputStream, times(0)).write(any(byte[].class));
            }
        }

        @Test
        void evaluate_TwoCmdsAbstractExceptionThenSuccess_ThrowsAbstractExceptionAndNothingPrintedAndSecondTerminateNotEvaluate()
                throws Exception {
            try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
                mockedStatic.when(() -> IOUtils.getLinesFromInputStream(any(InputStream.class)))
                        .thenReturn(INPUT_LINES);
                CallCommand successCmd = mock(CallCommand.class);
                CallCommand abstractExpCmd = mock(CallCommand.class);
                AbstractApplicationException exp = mock(AbstractApplicationException.class);
                doThrow(exp).when(abstractExpCmd).evaluate(any(InputStream.class), any(OutputStream.class));
                callCommands.add(abstractExpCmd);
                callCommands.add(successCmd);
                pipeCommand = new PipeCommand(callCommands);
                Throwable thrown = assertThrows(AbstractApplicationException.class, () -> {
                    pipeCommand.evaluate(inputStream, outputStream);
                });
                assertEquals(exp, thrown);
                verify(outputStream, times(0)).write(any(byte[].class));
                verify(successCmd, times(0)).evaluate(any(InputStream.class),
                        any(OutputStream.class));
                verify(successCmd, times(1)).terminate();
            }
        }

        @Test
        void evaluate_TwoCmdsShellExceptionThenSuccess_ThrowsShellExceptionAndNothingPrintedAndSecondTerminateNotEvaluate()
                throws Exception {
            try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
                mockedStatic.when(() -> IOUtils.getLinesFromInputStream(any(InputStream.class)))
                        .thenReturn(INPUT_LINES);
                CallCommand successCmd = mock(CallCommand.class);
                CallCommand shellExpCmd = mock(CallCommand.class);
                ShellException exp = mock(ShellException.class);
                doThrow(exp).when(shellExpCmd).evaluate(any(InputStream.class), any(OutputStream.class));
                callCommands.add(shellExpCmd);
                callCommands.add(successCmd);
                pipeCommand = new PipeCommand(callCommands);
                Throwable thrown = assertThrows(ShellException.class, () -> {
                    pipeCommand.evaluate(inputStream, outputStream);
                });
                assertEquals(exp, thrown);
                verify(outputStream, times(0)).write(any(byte[].class));
                verify(successCmd, times(0)).evaluate(any(InputStream.class),
                        any(OutputStream.class));
                verify(successCmd, times(1)).terminate();
            }
        }

        @Test
        void evaluate_TwoCmdsFileNotFoundExceptionThenSuccess_ThrowsExceptionAndNothingPrintedAndSecondTerminateNotEvaluate()
                throws Exception {
            try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
                mockedStatic.when(() -> IOUtils.getLinesFromInputStream(any(InputStream.class)))
                        .thenReturn(INPUT_LINES);
                CallCommand successCmd = mock(CallCommand.class);
                CallCommand fileExpCmd = mock(CallCommand.class);
                FileNotFoundException exp = mock(FileNotFoundException.class);
                doThrow(exp).when(fileExpCmd).evaluate(any(InputStream.class), any(OutputStream.class));
                callCommands.add(fileExpCmd);
                callCommands.add(successCmd);
                pipeCommand = new PipeCommand(callCommands);
                Throwable thrown = assertThrows(FileNotFoundException.class, () -> {
                    pipeCommand.evaluate(inputStream, outputStream);
                });
                assertEquals(exp, thrown);
                verify(outputStream, times(0)).write(any(byte[].class));
                verify(successCmd, times(0)).evaluate(any(InputStream.class),
                        any(OutputStream.class));
                verify(successCmd, times(1)).terminate();
            }
        }
    }
}
