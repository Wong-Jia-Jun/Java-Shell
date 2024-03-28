package sg.edu.nus.comp.cs4218.impl.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;
import sg.edu.nus.comp.cs4218.impl.cmd.PipeCommand;
import sg.edu.nus.comp.cs4218.impl.cmd.SequenceCommand;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;

/**
 * Integration tests for functionality of the {@link CommandBuilder} class.
 * Note that CommandBuilder has no unit tests as it's single method only creates commands.
 * For more meaningful test cases, the functionality is tested without a unit test.
 */
public class CommandBuilderIT { //NOPMD - suppressed ClassNamingConventions - As per Teaching Team's guidelines

    /**
     * The App runner.
     */
    ApplicationRunner appRunner;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        // TODO: make this a mock or something
        appRunner = mock(ApplicationRunner.class);
    }

    /**
     * Parse command null or empty command throws shell exception.
     *
     * @param command the command
     */
    @ParameterizedTest
    @NullAndEmptySource
    void parseCommand_NullOrEmptyCommand_ThrowsShellException(String command) {
        Throwable thrown = assertThrows(ShellException.class, () -> CommandBuilder.parseCommand(command, appRunner));
        assertEquals(new ShellException(ERR_SYNTAX).getMessage(), thrown.getMessage());
    }

    /**
     * Parse command contains new line throws shell exception.
     */
    @Test
    void parseCommand_ContainsNewLine_ThrowsShellException() {
        String newLine = System.lineSeparator();
        Throwable thrown = assertThrows(ShellException.class, () -> CommandBuilder.parseCommand(
                "echo hello" + newLine, appRunner));
        assertEquals(new ShellException(ERR_SYNTAX).getMessage(), thrown.getMessage());
    }

    /**
     * Parse command contains invalid string throws shell exception.
     *
     * @param str the str
     */
    @ParameterizedTest
    @ValueSource(strings = {"\"", "`", "'", "|", "<", ">", ";"})
    void parseCommand_ContainsInvalidString_ThrowsShellException(String str) {
        Throwable thrown = assertThrows(ShellException.class, () -> CommandBuilder.parseCommand(str, appRunner));
        assertEquals(new ShellException(ERR_SYNTAX).getMessage(), thrown.getMessage());
    }

    /**
     * The type Call command tests.
     */
    @Nested
    class CallCommandTests {
        /**
         * Parse command single command returns call command.
         *
         * @param args the args
         * @throws ShellException the shell exception
         */
        @ParameterizedTest
        @ValueSource(strings = {"test arg1  arg2", " test arg1 arg2 ", " test  arg1   arg2"})
        void parseCommand_SingleCommand_ReturnsCallCommand(String args) throws ShellException {
            Command command = CommandBuilder.parseCommand(args, appRunner);
            assertTrue(command instanceof CallCommand, "Command is not an instance of CallCommand");
            CallCommand callCommand = (CallCommand) command;
            List<String> argsList = callCommand.getArgsList();
            assertEquals(3, argsList.size());
            assertEquals("test", argsList.get(0));
            assertEquals("arg1", argsList.get(1));
            assertEquals("arg2", argsList.get(2));
        }
    }

    /**
     * The type Semicolon command tests.
     */
    @Nested
    class PipeWithSequenceCommandTests {
        @ParameterizedTest
        @ValueSource(strings = {"test1 | test2; test3", " test1 | test2 ; test3 "})
        void parseCommand_PipeWithSequence_ReturnsSequenceCommand(String args) throws ShellException {
            Command command = CommandBuilder.parseCommand(args, appRunner);
            assertTrue(command instanceof SequenceCommand, "Command is not an instance of SequenceCommand");
            SequenceCommand sequenceCommand = (SequenceCommand) command;
            List<Command> commands = sequenceCommand.getCommands();
            assertEquals(2, commands.size());
            assertTrue(commands.get(0) instanceof PipeCommand);
            assertTrue(commands.get(1) instanceof CallCommand);
            PipeCommand pipeCommand = (PipeCommand) commands.get(0);
            CallCommand callCommand = (CallCommand) commands.get(1);
            List<CallCommand> callCommands = pipeCommand.getCallCommands();
            assertEquals(2, callCommands.size());
            assertEquals("test1", callCommands.get(0).getArgsList().get(0));
            assertEquals("test2", callCommands.get(1).getArgsList().get(0));
            assertEquals("test3", callCommand.getArgsList().get(0));
        }
    }

    @Nested
    class PipeCommandTests {
        @ParameterizedTest
        @ValueSource(strings = {"test1 |  test2   ", " test1 | test2 "})
        void parseCommand_TwoCommandsWithPipe_ReturnsPipeCommand(String args) throws ShellException {
            Command command = CommandBuilder.parseCommand(args, appRunner);
            assertTrue(command instanceof PipeCommand, "Command is not an instance of PipeCommand");
            PipeCommand pipeCommand = (PipeCommand) command;
            List<CallCommand> callCommands = pipeCommand.getCallCommands();
            assertEquals(2, callCommands.size());
            assertEquals("test1", callCommands.get(0).getArgsList().get(0));
            assertEquals("test2", callCommands.get(1).getArgsList().get(0));
        }

        @ParameterizedTest
        @ValueSource(strings = {"| test1 | test2", "test1 | | test2", "test1 | test2 |"})
        void parseCommand_InvalidPipe_ThrowsShellException(String command) {
            Throwable thrown = assertThrows(ShellException.class, () -> CommandBuilder
                    .parseCommand(command, appRunner));
            assertEquals(new ShellException(ERR_SYNTAX).getMessage(), thrown.getMessage());
        }
    }

    @Nested
    class SemicolonCommandTests {
        /**
         * Parse command two commands with spaces returns sequence command.
         *
         * @param args the args
         * @throws ShellException the shell exception
         */
        @ParameterizedTest
        @ValueSource(strings = {"test; test1", " test  ; test1 "})
        void parseCommand_TwoCommandsWithSpaces_ReturnsSequenceCommand(String args) throws ShellException {
            Command command = CommandBuilder.parseCommand(args, appRunner);
            assertTrue(command instanceof SequenceCommand, "Command is not an instance of SequenceCommand");
            SequenceCommand sequenceCommand = (SequenceCommand) command;
            List<Command> commands = sequenceCommand.getCommands();
            assertEquals(2, commands.size());
            assertTrue(commands.get(0) instanceof CallCommand);
            assertTrue(commands.get(1) instanceof CallCommand);
            CallCommand cmd1 = (CallCommand) commands.get(0);
            CallCommand cmd2 = (CallCommand) commands.get(1);
            assertEquals("test", cmd1.getArgsList().get(0));
            assertEquals("test1", cmd2.getArgsList().get(0));
        }

        /**
         * Parse command invalid semicolon throws shell exception.
         *
         * @param command the command
         */
        @ParameterizedTest
        @ValueSource(strings = {"; test; tes1", "test1; test2;", "test1; ;test2", "test1;;test2"})
        void parseCommand_InvalidSemicolon_ThrowsShellException(String command) {
            Throwable thrown = assertThrows(ShellException.class, () -> CommandBuilder
                    .parseCommand(command, appRunner));
            assertEquals(new ShellException(ERR_SYNTAX).getMessage(), thrown.getMessage());
        }

        /**
         * Parse command semicolon with pipe gives correct commands.
         *
         * @throws ShellException the shell exception
         */
        @Test
        void parseCommand_SemicolonWithPipe_GivesCorrectCommands() throws ShellException {
            String command = "cmd1 | cmd2; cmd3";
            Command cmd = CommandBuilder.parseCommand(command, appRunner);
            assertTrue(cmd instanceof SequenceCommand);
            SequenceCommand sequenceCommand = (SequenceCommand) cmd;
            List<Command> commands = sequenceCommand.getCommands();
            assertEquals(2, commands.size());
            assertTrue(commands.get(0) instanceof PipeCommand);
            assertTrue(commands.get(1) instanceof CallCommand);
            PipeCommand pipeCommand = (PipeCommand) commands.get(0);
            CallCommand callCommand3 = (CallCommand) commands.get(1);
            List<CallCommand> callCommands = pipeCommand.getCallCommands();
            assertEquals(2, callCommands.size());
            assertEquals("cmd1", callCommands.get(0).getArgsList().get(0));
            assertEquals("cmd2", callCommands.get(1).getArgsList().get(0));
            assertEquals("cmd3", callCommand3.getArgsList().get(0));
        }
    }
}
