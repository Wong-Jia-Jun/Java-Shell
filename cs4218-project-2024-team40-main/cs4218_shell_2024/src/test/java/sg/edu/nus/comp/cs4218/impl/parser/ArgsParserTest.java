package sg.edu.nus.comp.cs4218.impl.parser;

import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class contains unit tests for the {@link ArgsParser} class, specifically verifying its
 * ability to parse command-line arguments correctly.
 */
public class ArgsParserTest {
    public static final String TEST_1 = "test1";
    public static final String TEST_2 = "test2";
    public static final String FILE_NAME = "filename.txt";
    private ArgsParser argsParser;


    /**
     * Sets up the test environment by initializing a new {@link ArgsParser} instance.
     */
    @BeforeEach
    void setUp() {
        argsParser = new ArgsParser();
    }

    /**
     * Tests that when no arguments are provided, all internal fields of the `ArgsParser` instance are empty.
     *
     * @throws InvalidArgsException if an exception occurs during parsing.
     */
    @Test
    void parse_NoArgs_AllFlagsEmpty() throws InvalidArgsException {
        ArgsParser argsParser = new ArgsParser();

        argsParser.parse(new String[0]);

        //Test that are no flags, nonflags or legal flags
        assertEquals(Arrays.asList(), argsParser.nonFlagArgs);
        assertEquals(0, argsParser.legalFlags.size());
        assertEquals(0, argsParser.flags.size());


    }

    /**
     * Tests that when null arguments are provided, an exception is thrown.
     *
     * @throws InvalidArgsException if an exception occurs during parsing.
     */
    @Test
    void parse_Null_ThrowsException() throws InvalidArgsException {
        ArgsParser argsParser = new ArgsParser();
        Throwable exception = assertThrows(InvalidArgsException.class, () -> argsParser.parse((String[]) null));
        assertEquals(ArgsParser.ILLEGAL_NULL_MSG, exception.getMessage());
    }

    /**
     * Tests that when no flags are provided, only non-flag arguments are parsed and stored in the `nonFlagArgs` list.
     *
     * @throws InvalidArgsException if an exception occurs during parsing.
     */
    @Test
    void parse_NoFlags_OnlyNonFLagArgsParsed() throws InvalidArgsException {
        ArgsParser argsParser = new ArgsParser();

        String[] args = {TEST_1, TEST_2};
        String[] expectedArgs = {TEST_1, TEST_2};
        argsParser.parse(args);

        //  there are no flags passed, and legal flags , all args treated as nonFlagArgs
        assertEquals(Arrays.asList(expectedArgs), argsParser.nonFlagArgs);
        assertEquals(0, argsParser.flags.size());
        assertEquals(0, argsParser.legalFlags.size());

    }

    /**
     * Tests that when a legal flag is provided, it is parsed and stored in the `flags` list and the corresponding legal flag is marked as present.
     *
     * @throws InvalidArgsException if an exception occurs during parsing.
     */
    @Test
    void parse_LegalFlag_FlagsAndLegalFlagsParsed() throws InvalidArgsException {
        argsParser.legalFlags.add('a');
        String[] args = new String[]{"-a"};
        argsParser.parse(args);

        assertEquals(0, argsParser.nonFlagArgs.size());
        assertEquals(1, argsParser.flags.size());
        assertTrue(argsParser.flags.contains('a'));

    }

    /**
     * Tests that when an illegal flag is provided, an `InvalidArgsException` is thrown.
     *
     * @throws InvalidArgsException if the test throws the expected exception.
     */
    @Test
    void parse_InvalidFlag_ThrowException() throws InvalidArgsException {
        //since no legal flags added a is counted as illegal so exception thrown
        String[] args = new String[]{"-a"};
        assertThrows(InvalidArgsException.class, () -> argsParser.parse(args));
    }

    /**
     * Tests that flag parsing is case-sensitive and an illegal flag with different case throws an `InvalidArgsException`.
     *
     * @throws InvalidArgsException if the test throws the expected exception.
     */
    @Test
    void parse_InvalidFlagCaseSensitive_ThrowException() throws InvalidArgsException {
        argsParser.legalFlags.add('A');
        String[] args = new String[]{"-a"};
        assertThrows(InvalidArgsException.class, () -> argsParser.parse(args));
    }

    /**
     * Tests that when multiple legal flags are provided, they are all parsed and stored in the `flags` list, and their corresponding legal flags are marked as present.
     *
     * @throws InvalidArgsException if an exception occurs during parsing.
     */
    @Test
    void parse_MultipleFlags_AllFlagsParsed() throws InvalidArgsException {
        argsParser.legalFlags.addAll(Arrays.asList('a', 'b', 'c'));
        String[] args = {"-abc"};
        argsParser.parse(args);

        assertTrue(argsParser.flags.containsAll(Arrays.asList('a', 'b', 'c')));
        assertEquals(0, argsParser.nonFlagArgs.size());
    }

    /**
     * Tests that when flags and non-flag arguments are mixed, flags are parsed first and
     * non-flag arguments are parsed after them, correctly populating the corresponding lists.
     *
     * @throws InvalidArgsException if an exception occurs during parsing.
     */
    @Test
    void parse_FlagsWithNonFlagArgs_NonFlagArgsParsedAfterFlags() throws InvalidArgsException {
        argsParser.legalFlags.add('a');
        String[] args = {"-a", FILE_NAME};
        argsParser.parse(args);

        assertTrue(argsParser.flags.contains('a'));
        assertEquals(Arrays.asList(FILE_NAME), argsParser.nonFlagArgs);
    }

    /**
     * Tests that when an empty flag (just a hyphen) is provided, it's not considered a valid flag
     * and is instead treated as a non-flag argument.
     *
     * @throws InvalidArgsException if an exception occurs during parsing.
     */
    @Test
    void parse_EmptyFlag_ArgsConsideredNonFlag() throws InvalidArgsException {
        String[] args = {"-", FILE_NAME};
        argsParser.parse(args);

        assertEquals(Arrays.asList("-", FILE_NAME), argsParser.nonFlagArgs);
        assertTrue(argsParser.flags.isEmpty());
    }

    /**
     * Tests that when flags and non-flag arguments are mixed in different orders, they are
     * correctly parsed and stored in their respective lists.
     *
     * @throws InvalidArgsException if an exception occurs during parsing.
     */
    @Test
    void parse_FlagAndNonFlagMixed_CorrectlyParsed() throws InvalidArgsException {
        argsParser.legalFlags.addAll(Arrays.asList('a', 'b'));
        String[] args = {"-a", TEST_1, "-b", TEST_2};
        argsParser.parse(args);

        assertTrue(argsParser.flags.containsAll(Arrays.asList('a', 'b')));
        assertEquals(Arrays.asList(TEST_1, TEST_2), argsParser.nonFlagArgs);
    }

    /**
     * Tests that when an unknown flag is provided within a group of valid flags, an
     * `InvalidArgsException` is thrown.
     */
    @Test
    void parse_UnknownFlagInMiddleOfValidFlags_ThrowsException() {
        argsParser.legalFlags.addAll(Arrays.asList('a', 'b'));
        String[] args = {"-abc"};

        assertThrows(InvalidArgsException.class, () -> argsParser.parse(args));
    }

    /**
     * Tests that a single-character argument without a hyphen is not considered a flag and is
     * treated as a non-flag argument.
     *
     * @throws InvalidArgsException if an exception occurs during parsing.
     */
    @Test
    void parse_SingleCharNonFlag_TreatedAsNonFlag() throws InvalidArgsException {
        String[] args = {"a"};
        argsParser.parse(args);

        assertEquals(Arrays.asList("a"), argsParser.nonFlagArgs);
        assertTrue(argsParser.flags.isEmpty());
    }

    /**
     * Tests that when a mix of valid and invalid flags is provided, the invalid flags cause an
     * `InvalidArgsException` to be thrown, even if valid flags are present.
     */
    @Test
    void parse_MixedValidAndInvalidFlags_InvalidFlagsThrowException() {
        argsParser.legalFlags.add('a');
        String[] args = {"-a", "-b"};

        assertThrows(InvalidArgsException.class, () -> argsParser.parse(args));
    }
}
