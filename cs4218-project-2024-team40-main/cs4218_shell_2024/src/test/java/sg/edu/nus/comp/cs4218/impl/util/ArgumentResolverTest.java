package sg.edu.nus.comp.cs4218.impl.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;

import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for functionality of ArgumentResolver
 */
public class ArgumentResolverTest {
    /**
     * The Argument resolver.
     */
    ArgumentResolver argumentResolver;

    /**
     * The type Regex argument mock.
     */
    public static class RegexArgumentMock implements IRegexArgument {
        private final List<String> globedFiles;

        /**
         * Instantiates a new Regex argument mock.
         *
         * @param globFiles the glob files
         */
        public RegexArgumentMock(List<String> globFiles) {
            globedFiles = globFiles;
        }

        @Override
        public void append(char chr) {} //NOPMD - suppressed UncommentedEmptyMethodBody - method from interface

        @Override
        public void appendAsterisk() {} //NOPMD - suppressed UncommentedEmptyMethodBody - method from interface

        @Override
        public void merge(String str) {} //NOPMD - suppressed UncommentedEmptyMethodBody - method from interface

        @Override
        public void merge(IRegexArgument other) {} //NOPMD - suppressed UncommentedEmptyMethodBody - method from interface

        @Override
        public List<String> globFiles() {
            return globedFiles;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        argumentResolver = spy(ArgumentResolver.class);
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

        // Remove RegexArgument Dependency
        List<String> globFilesResult = new LinkedList<>();
        globFilesResult.add(expectedString);
        IRegexArgument mockedRegex = new RegexArgumentMock(globFilesResult);

        List<String> result = argumentResolver.resolveOneArgument(commandString, mockedRegex);
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

        // mock RegexArgument
        List<String> globFilesResult = new LinkedList<>();
        globFilesResult.add("echo Hello World");
        IRegexArgument mockedRegex = new RegexArgumentMock(globFilesResult);

        // spy get methods
        doReturn(mockedRegex).when(argumentResolver).makeRegexArgument();
        doReturn(mockedRegex).when(argumentResolver).makeRegexArgument(anyString());

        List<String> result = argumentResolver.resolveOneArgument(commandString, mockedRegex);
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

        // mock RegexArgument
        List<String> globFilesResult = new LinkedList<>();
        globFilesResult.add("echo This is space:`echo \" \"`.");
        IRegexArgument mockedRegex = new RegexArgumentMock(globFilesResult);

        // spy get methods
        doReturn(mockedRegex).when(argumentResolver).makeRegexArgument();
        doReturn(mockedRegex).when(argumentResolver).makeRegexArgument(anyString());

        List<String> result = argumentResolver.resolveOneArgument(commandString, mockedRegex);
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

        // mock RegexArgument
        List<String> globFilesResult = new LinkedList<>();
        globFilesResult.add("echo \"This is space `echo \" \"`\"");
        IRegexArgument mockedRegex = new RegexArgumentMock(globFilesResult);

        // spy get methods
        doReturn(mockedRegex).when(argumentResolver).makeRegexArgument();
        doReturn(mockedRegex).when(argumentResolver).makeRegexArgument(anyString());

        List<String> result = argumentResolver.resolveOneArgument(commandString, mockedRegex);
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
        // stub evaluateSubCommand
        doReturn(" ").when(argumentResolver).evaluateSubCommand("echo \" \"");

        // mock RegexArgument
        List<String> globFilesResult = new LinkedList<>();
        globFilesResult.add("echo This is space: .");
        IRegexArgument mockedRegex = new RegexArgumentMock(globFilesResult);

        // spy get methods
        doReturn(mockedRegex).when(argumentResolver).makeRegexArgument();
        doReturn(mockedRegex).when(argumentResolver).makeRegexArgument(anyString());

        List<String> result = argumentResolver.resolveOneArgument(commandString, mockedRegex);

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

        // stub evaluateSubCommand
        doReturn(" ").when(argumentResolver).evaluateSubCommand("echo \" \"");

        // mock RegexArgument
        List<String> globFilesResult = new LinkedList<>();
        globFilesResult.add("echo 'This is space  '");
        IRegexArgument mockedRegex = new RegexArgumentMock(globFilesResult);

        // spy get methods
        doReturn(mockedRegex).when(argumentResolver).makeRegexArgument();
        doReturn(mockedRegex).when(argumentResolver).makeRegexArgument(anyString());

        List<String> result = argumentResolver.resolveOneArgument(commandString, mockedRegex);

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
    void parseArguments_ContainsBackQuotes_ReturnsCorrectStrings() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> argsStrings = new LinkedList<>();
        argsStrings.add("echo");
        argsStrings.add("`echo \"Hello World\"`");
        List<String> expectedStrings = new LinkedList<>();
        expectedStrings.add("echo");
        expectedStrings.add("Hello");
        expectedStrings.add("World");

        // mock RegexArgument
        IRegexArgument mockedRegex = new RegexArgumentMock(List.of(""));
        doReturn(mockedRegex).when(argumentResolver).makeRegexArgument();
        // spy resolveOneArgument
        doReturn(List.of("echo")).when(argumentResolver).resolveOneArgument(argsStrings.get(0), mockedRegex);
        doReturn(List.of("Hello", "World")).when(argumentResolver).resolveOneArgument(argsStrings.get(1), mockedRegex);

        List<String> result = argumentResolver.parseArguments(argsStrings);
        assertEquals(expectedStrings, result);
    }
}
