package sg.edu.nus.comp.cs4218.impl.parser;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests the functionality of the `RmArgsParser` class.
 * It verifies the parsing behavior for various input scenarios involving flags and non-flag arguments.
 *
 */
public class RmArgsParserTest {
    private RmArgsParser rmArgsParser;

    @BeforeEach
    void setUp() {
        rmArgsParser = new RmArgsParser();
    }

    @AfterEach
    void teardown() {
        rmArgsParser = null;
    }

    /**
     * Tests parsing legal flags (-r, -d, -rd).
     * Verifies that non-flag arguments are empty, flags are parsed,
     * and either isEmptyFolder or isRecursive is true.
     *
     * @param str The input string containing a legal flag.
     * @throws InvalidArgsException If an error occurs during parsing.
     */
    @ParameterizedTest
    @ValueSource(strings = {"-r", "-d", "-rd"})
    void parse_LegalFlag_FlagsAndLegalFlagsParsed(String str) throws InvalidArgsException {
        rmArgsParser.parse(str);

        assertEquals(0, rmArgsParser.nonFlagArgs.size());
        assertFalse(rmArgsParser.flags.isEmpty());
        assertTrue(rmArgsParser.isEmptyFolder() || rmArgsParser.isRecursive());
        if (str.contains("r")) {
            assertTrue(rmArgsParser.isRecursive());
        }
        if (str.contains("d")) {
            assertTrue(rmArgsParser.isEmptyFolder());
        }
    }

    /**
     * Tests parsing an invalid flag. Verifies that an InvalidArgsException is thrown.
     */
    @Test
    void parse_InvalidFlag_ThrowException() {
        String[] args = new String[]{"-a"};
        assertThrows(InvalidArgsException.class, () -> rmArgsParser.parse(args));
        assertFalse(rmArgsParser.isEmptyFolder());
        assertFalse(rmArgsParser.isRecursive());
    }

    /**
     * Tests parsing flags with incorrect case sensitivity (uppercase).
     * Verifies that an InvalidArgsException is thrown.
     *
     * @param str The input string containing an uppercase flag (e.g., "-R").
     */
    @ParameterizedTest
    @ValueSource(strings = {"-R", "-D", "-RD"})
    void parse_InvalidFlagCaseSensitive_ThrowException(String str) {
        assertThrows(InvalidArgsException.class, () -> rmArgsParser.parse(str));
        assertFalse(rmArgsParser.isEmptyFolder());
        assertFalse(rmArgsParser.isRecursive());
    }

    /**
     * Tests parsing a combination of flags and non-flag arguments,
     * where non-flag arguments come after the flags.
     * Verifies that non-flag arguments are parsed correctly.
     *
     * @throws InvalidArgsException If an error occurs during parsing.
     */
    @Test
    void parse_FlagsWithNonFlagArgs_NonFlagArgsParsedAfterFlags() throws InvalidArgsException {
        String[] args = {"-r", "filename.txt"};
        rmArgsParser.parse(args);
        assertFalse(rmArgsParser.isEmptyFolder());
        assertTrue(rmArgsParser.isRecursive());
        assertEquals(List.of("filename.txt"), rmArgsParser.nonFlagArgs);
    }

    /**
     * Tests parsing a mixture of flags and non-flag arguments in any order.
     * Verifies that flags and non-flag arguments are parsed correctly.
     *
     * @throws InvalidArgsException If an error occurs during parsing.
     */
    @Test
    void parse_FlagAndNonFlagMixed_CorrectlyParsed() throws InvalidArgsException {
        String[] args = {"-r", "test1", "-d", "test2"};
        rmArgsParser.parse(args);
        assertTrue(rmArgsParser.isRecursive());
        assertTrue(rmArgsParser.isEmptyFolder());
        assertEquals(Arrays.asList("test1", "test2"), rmArgsParser.nonFlagArgs);
    }

    /**
     * Tests parsing a string containing an unknown flag in the middle of valid flags.
     * Verifies that an InvalidArgsException is thrown.
     */
    @Test
    void parse_UnknownFlagInMiddleOfValidFlags_ThrowsException() {
        String[] args = {"-rda"};
        assertThrows(InvalidArgsException.class, () -> rmArgsParser.parse(args));
        assertTrue(rmArgsParser.isRecursive());
        assertTrue(rmArgsParser.isEmptyFolder());
    }

    /**
     * Tests parsing single-character strings that are not flags.
     * Verifies that they are treated as non-flag arguments.
     *
     * @param str The single-character input string.
     * @throws InvalidArgsException If an error occurs during parsing.
     */
    @ParameterizedTest
    @ValueSource(strings = {"r", "d"})
    void parse_SingleCharNonFlag_TreatedAsNonFlag(String str) throws InvalidArgsException {
        rmArgsParser.parse(str);
        assertFalse(rmArgsParser.isEmptyFolder());
        assertFalse(rmArgsParser.isRecursive());
        assertEquals(List.of(str), rmArgsParser.nonFlagArgs);
    }

    /**
     * Tests parsing a combination of valid and invalid flags.
     * Verifies that an InvalidArgsException is thrown due to the presence of invalid flags.
     */
    @Test
    void parse_MixedValidAndInvalidFlags_InvalidFlagsThrowException() {
        String[] args = {"-r", "-b"};
        assertThrows(InvalidArgsException.class, () -> rmArgsParser.parse(args));
        assertFalse(rmArgsParser.isEmptyFolder());
        assertTrue(rmArgsParser.isRecursive());
    }
}
