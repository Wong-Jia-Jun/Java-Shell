package sg.edu.nus.comp.cs4218.impl.cmd;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockedStatic;
import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ExitException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.cmd.stubs.CommandStub;
import sg.edu.nus.comp.cs4218.impl.cmd.stubs.CommandStubThatFails;
import sg.edu.nus.comp.cs4218.impl.cmd.stubs.ExitCommandStub;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;

/**
 * The type Sequence command test.
 */
public class SequenceCommandTest {
    private static final Command SUCCESS_COMMAND = new CommandStub();
    private static final Command FAIL_COMMAND = new CommandStubThatFails();
    private static final Command EXIT_COMMAND = new ExitCommandStub();
    private static final String TEST_STRING = "HelloWorld";

    /**
     * The type Successful command evaluations.
     */
    @Nested
    class SuccessfulCommandEvaluations {
        private SequenceCommand sequenceCommand;
        private  InputStream inputStream;
        private OutputStream outputStream;
        private MockedStatic<IOUtils> mockedIOUtils;

        /**
         * Sets up.
         *
         * @throws ShellException the shell exception
         */
        @BeforeEach
        void setUp() throws ShellException {
            sequenceCommand = new SequenceCommand(List.of(SUCCESS_COMMAND, SUCCESS_COMMAND));
            inputStream = mock(InputStream.class);
            outputStream = mock(OutputStream.class);
            mockedIOUtils = mockStatic(IOUtils.class);
            mockedIOUtils.when(() -> IOUtils.getLinesFromInputStream(inputStream))
                    .thenReturn(List.of("Hello", "World"));
        }

        /**
         * Tear down.
         */
        @AfterEach
        void tearDown() {
            sequenceCommand = null;
            inputStream = null;
            outputStream = null;
            mockedIOUtils.close();
        }

        /**
         * Evaluate two commands take input and give output prints output.
         *
         * @throws AbstractApplicationException the abstract application exception
         * @throws ShellException               the shell exception
         * @throws IOException                  the io exception
         */
        @Test
        void evaluate_TwoCommandsTakeInputAndGiveOutput_PrintsOutput() throws AbstractApplicationException,
                ShellException, IOException {
            sequenceCommand.evaluate(inputStream, outputStream);
            String expectedOutput = TEST_STRING + System.lineSeparator();
            verify(outputStream, times(2)).write(expectedOutput.getBytes());
        }

        /**
         * Evaluate successful command then exit command prints output and throw exit exception.
         *
         * @throws IOException    the io exception
         * @throws ShellException the shell exception
         */
        @Test
        void evaluate_SuccessfulCommandThenExitCommand_PrintsOutputAndThrowExitException()
                throws IOException, ShellException {
            sequenceCommand = new SequenceCommand(List.of(SUCCESS_COMMAND, EXIT_COMMAND));
            Throwable thrown = assertThrows(ExitException.class, () -> {
                sequenceCommand.evaluate(inputStream, outputStream);
            });
            assertEquals(new ExitException("exit").getMessage(), thrown.getMessage());
            String expectedOutput = TEST_STRING + System.lineSeparator();
            verify(outputStream, times(1)).write(expectedOutput.getBytes());
        }

        /**
         * Evaluate exit then successful command throws exit exception and doesnt execute second.
         *
         * @throws IOException    the io exception
         * @throws ShellException the shell exception
         */
        @Test
        void evaluate_ExitThenSuccessfulCommand_ThrowsExitExceptionAndDoesntExecuteSecond()
                throws IOException, ShellException {
            sequenceCommand = new SequenceCommand(List.of(EXIT_COMMAND, SUCCESS_COMMAND));
            Throwable thrown = assertThrows(ExitException.class, () -> {
                sequenceCommand.evaluate(inputStream, outputStream);
            });
            assertEquals(new ExitException("exit").getMessage(), thrown.getMessage());
            verify(outputStream, times(0)).write(any());
        }
    }

    /**
     * The type Unsuccessful command evaluations.
     */
    @Nested
    class UnsuccessfulCommandEvaluations {
        /**
         * The Byte captor.
         */
        @Captor
        ArgumentCaptor<byte[]> byteCaptor;
        private SequenceCommand sequenceCommand;
        private  InputStream inputStream;
        private OutputStream outputStream;
        private MockedStatic<IOUtils> mockedIOUtils;

        /**
         * Sets up.
         */
        @BeforeEach
        void setUp() {
            inputStream = mock(InputStream.class);
            outputStream = mock(OutputStream.class);
            mockedIOUtils = mockStatic(IOUtils.class);
            mockedIOUtils.when(() -> IOUtils.getLinesFromInputStream(inputStream))
                    .thenReturn(List.of("Hello", "World"));
        }

        /**
         * Tear down.
         */
        @AfterEach
        void tearDown() {
            sequenceCommand = null;
            inputStream = null;
            outputStream = null;
            mockedIOUtils.close();
        }

        /**
         * Sequence command null commands throws shell exception.
         */
        @Test
        void sequenceCommand_NullCommands_ThrowsShellException() {
            Throwable thrown = assertThrows(ShellException.class, () -> new SequenceCommand(null));
            assertEquals(new ShellException(ERR_NULL_ARGS).getMessage(),
                    thrown.getMessage());
        }

        /**
         * Evaluate two commands first command fails prints error and executes second.
         *
         * @throws AbstractApplicationException the abstract application exception
         * @throws ShellException               the shell exception
         * @throws IOException                  the io exception
         */
        @Test
        void evaluate_TwoCommandsFirstCommandFails_PrintsErrorAndExecutesSecond() throws AbstractApplicationException,
                ShellException, IOException {
            sequenceCommand = new SequenceCommand(List.of(FAIL_COMMAND, SUCCESS_COMMAND));
            sequenceCommand.evaluate(inputStream, outputStream);
            String expectedMsg = new ShellException("Error").getMessage() + System.lineSeparator();
            String expectedOutput = TEST_STRING + System.lineSeparator();
            byteCaptor = ArgumentCaptor.forClass(byte[].class);
            verify(outputStream, times(2)).write(byteCaptor.capture());
            List<byte[]> capturedValues = byteCaptor.getAllValues();
            String actualErrorMsg = new String(capturedValues.get(0));
            String actualSuccessMsg = new String(capturedValues.get(1));
            assertEquals(expectedMsg, actualErrorMsg);
            assertEquals(expectedOutput, actualSuccessMsg);
        }

        /**
         * Evaluate two commands second command fails executes first then prints error.
         *
         * @throws AbstractApplicationException the abstract application exception
         * @throws ShellException               the shell exception
         * @throws IOException                  the io exception
         */
        @Test
        void evaluate_TwoCommandsSecondCommandFails_ExecutesFirstThenPrintsError() throws AbstractApplicationException,
                ShellException, IOException {
            sequenceCommand = new SequenceCommand(List.of(SUCCESS_COMMAND, FAIL_COMMAND));
            sequenceCommand.evaluate(inputStream, outputStream);
            String expectedMsg = new ShellException("Error").getMessage() + System.lineSeparator();
            String expectedOutput = TEST_STRING + System.lineSeparator();
            byteCaptor = ArgumentCaptor.forClass(byte[].class);
            verify(outputStream, times(2)).write(byteCaptor.capture());
            List<byte[]> capturedValues = byteCaptor.getAllValues();
            String actualErrorMsg = new String(capturedValues.get(1));
            String actualSuccessMsg = new String(capturedValues.get(0));
            assertEquals(expectedMsg, actualErrorMsg);
            assertEquals(expectedOutput, actualSuccessMsg);
        }

        /**
         * Evaluate two commands second command fails executes first tries to print but cannot
         * print so throws exception.
         *
         * @throws AbstractApplicationException the abstract application exception
         * @throws ShellException               the shell exception
         * @throws IOException                  the io exception
         */
        @Test
        void evaluate_TwoCommandsSecondCommandFailsCannotWrite_ThrowsException() throws AbstractApplicationException,
                ShellException, IOException {
            sequenceCommand = new SequenceCommand(List.of(SUCCESS_COMMAND, FAIL_COMMAND));
            doThrow(new IOException(TEST_STRING)).when(outputStream).write(any());
            Throwable thrown = assertThrows(ShellException.class, () -> {
                sequenceCommand.evaluate(inputStream, outputStream);
            });
            assertEquals(new ShellException(TEST_STRING).getMessage(), thrown.getMessage());

        }
    }

    static Stream<Arguments> getCommandArgsProvider() {
        return Stream.of(
                Arguments.of((Object) List.of(SUCCESS_COMMAND)),
                Arguments.of((Object) List.of(SUCCESS_COMMAND, SUCCESS_COMMAND))
        );
    }

    @ParameterizedTest
    @MethodSource("getCommandArgsProvider")
    void getCommands_PassInCommandListInConstructor_ReturnsCommandList(List<Command> commands) throws ShellException {
        SequenceCommand sequenceCommand = new SequenceCommand(commands);
        assertEquals(commands, sequenceCommand.getCommands());
    }
}
