package sg.edu.nus.comp.cs4218.impl.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;

import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Integration Tests for functionality of ArgumentResolver with RegexArgument and StringUtils.
 */
public class ArgumentResolverIT { //NOPMD - suppressed ClassNamingConventions - As per Teaching Team's guidelines
    /**
     * The Argument resolver.
     */
    ArgumentResolver argumentResolver;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        argumentResolver = new ArgumentResolver();
    }

    /**
     * Resolve one argument contains single quotes removes single quotes.
     *
     * @throws FileNotFoundException        the file not found exception
     * @throws AbstractApplicationException the abstract application exception
     * @throws ShellException               the shell exception
     */
    @Test
    void resolveOneArgument_ContainsSingleQuotes_RemovesSingleQuotes() throws FileNotFoundException, AbstractApplicationException, ShellException {
        String commandString = "echo 'Hello World";
        String expectedString = "echo Hello World";
        List<String> expectedStrings = new LinkedList<>();
        expectedStrings.add(expectedString);
        List<String> result = argumentResolver.resolveOneArgument(commandString, new RegexArgument());
        assertEquals(expectedStrings, result);
    }

    /**
     * Resolve one argument contains double quotes removes double quotes.
     *
     * @throws FileNotFoundException        the file not found exception
     * @throws AbstractApplicationException the abstract application exception
     * @throws ShellException               the shell exception
     */
    @Test
    void resolveOneArgument_ContainsDoubleQuotes_RemovesDoubleQuotes() throws FileNotFoundException, AbstractApplicationException, ShellException {
        String commandString = "echo \"Hello World\"";
        List<String> expectedStrings = new LinkedList<>();
        expectedStrings.add("echo Hello World");
        List<String> result = argumentResolver.resolveOneArgument(commandString, new RegexArgument());
        assertEquals(expectedStrings, result);
    }

    /**
     * Resolve one argument contains back quotes in single quotes ignores sub command.
     *
     * @throws FileNotFoundException        the file not found exception
     * @throws AbstractApplicationException the abstract application exception
     * @throws ShellException               the shell exception
     */
    @Test
    void resolveOneArgument_ContainsBackQuotesInSingleQuotes_IgnoresSubCommand() throws FileNotFoundException, AbstractApplicationException, ShellException {
        String commandString = "echo 'This is space:`echo \" \"`.'";
        List<String> expectedStrings = new LinkedList<>();
        expectedStrings.add("echo This is space:`echo \" \"`.");
        List<String> result = argumentResolver.resolveOneArgument(commandString, new RegexArgument());
        assertEquals(expectedStrings, result);
    }

    /**
     * Resolve one argument contains back quotes in double quotes in single quotes ignores sub command.
     *
     * @throws FileNotFoundException        the file not found exception
     * @throws AbstractApplicationException the abstract application exception
     * @throws ShellException               the shell exception
     */
    @Test
    void resolveOneArgument_ContainsBackQuotesInDoubleQuotesInSingleQuotes_IgnoresSubCommand() throws FileNotFoundException, AbstractApplicationException, ShellException {
        String commandString = "echo '\"This is space `echo \" \"`\"'";
        List<String> expectedStrings = new LinkedList<>();
        expectedStrings.add("echo \"This is space `echo \" \"`\"");
        List<String> result = argumentResolver.resolveOneArgument(commandString, new RegexArgument());
        assertEquals(expectedStrings, result);
    }

    /**
     * Resolve one argument contains back quotes in double quotes executes sub command.
     *
     * @throws FileNotFoundException        the file not found exception
     * @throws AbstractApplicationException the abstract application exception
     * @throws ShellException               the shell exception
     */
    @Test
    void resolveOneArgument_ContainsBackQuotesInDoubleQuotes_ExecutesSubCommand() throws FileNotFoundException, AbstractApplicationException, ShellException {
        String commandString = "echo \"This is space:`echo \" \"`.\"";
        List<String> expectedStrings = new LinkedList<>();
        expectedStrings.add("echo This is space: .");
        List<String> result = argumentResolver.resolveOneArgument(commandString, new RegexArgument());
        assertEquals(expectedStrings, result);
    }

    /**
     * Resolve one argument contains back quotes in single quotes in double quotes executes sub command.
     *
     * @throws FileNotFoundException        the file not found exception
     * @throws AbstractApplicationException the abstract application exception
     * @throws ShellException               the shell exception
     */
    @Test
    void resolveOneArgument_ContainsBackQuotesInSingleQuotesInDoubleQuotes_ExecutesSubCommand() throws FileNotFoundException, AbstractApplicationException, ShellException {
        String commandString = "echo \"'This is space `echo \" \"`'\"";
        List<String> expectedStrings = new LinkedList<>();
        expectedStrings.add("echo 'This is space  '");
        List<String> result = argumentResolver.resolveOneArgument(commandString, new RegexArgument());
        assertEquals(expectedStrings, result);
    }

    /**
     * Parse arguments contains back quotes executes sub command.
     *
     * @throws FileNotFoundException        the file not found exception
     * @throws AbstractApplicationException the abstract application exception
     * @throws ShellException               the shell exception
     */
    @Test
    void parseArguments_ContainsBackQuotes_ExecutesSubCommand() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> argsStrings = new LinkedList<>();
        argsStrings.add("echo");
        argsStrings.add("`echo \"Hello World\"`");
        List<String> expectedStrings = new LinkedList<>();
        expectedStrings.add("echo");
        expectedStrings.add("Hello");
        expectedStrings.add("World");
        List<String> result = argumentResolver.parseArguments(argsStrings);
        assertEquals(expectedStrings, result);
    }
}
