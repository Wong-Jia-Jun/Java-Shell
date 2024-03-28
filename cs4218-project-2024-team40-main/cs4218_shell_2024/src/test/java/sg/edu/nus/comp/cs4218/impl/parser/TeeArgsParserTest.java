package sg.edu.nus.comp.cs4218.impl.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests the functionality of the `TeeArgsParser` class.
 * It verifies the parsing behavior for various input scenarios involving flags and file names.
 */
public class TeeArgsParserTest {
    private TeeArgsParser teeArgsParser;
    public static final String ILLEGAL_FLAG_MSG = "illegal option -- ";
    public static final String FILE_1 = "file1.txt";

    @BeforeEach
    void setUp() {
        teeArgsParser = new TeeArgsParser();
    }

    /**
     * Tests parsing arguments with no options. Verifies that the single file is included and append mode is false.
     *
     * @throws InvalidArgsException If an error occurs during parsing.
     */
    @Test
    void parse_NoOptions_DefaultBehavior() throws InvalidArgsException {
        String[] args = {FILE_1};
        teeArgsParser.parse(args);

        assertTrue(teeArgsParser.getFileNames().contains(FILE_1));
        assertFalse(teeArgsParser.isAppend());

    }

    /**
     * Tests parsing arguments with the "-a" (append) option and a single file.
     * Verifies that append mode is true and the file is included.
     *
     * @throws InvalidArgsException If an error occurs during parsing.
     */
    @Test
    void parse_CreateParentOption_CorrectNonArgsFlags() throws InvalidArgsException {
        String[] args = {"-a", "file1.txt"};
        teeArgsParser.parse(args);

        assertTrue(teeArgsParser.isAppend());
        assertEquals(Arrays.asList("file1.txt"), teeArgsParser.getFileNames());
    }

    /**
     * Tests parsing arguments with the "-a" (append) option and multiple files.
     * Verifies that append mode is true and all files are included.
     *
     * @throws InvalidArgsException If an error occurs during parsing.
     */
    @Test
    void parse_CreateParentOptionsWithMultipleFiles_CorrectNonArgs() throws InvalidArgsException {
        String[] args = {"-a", FILE_1, "file2.txt"};
        teeArgsParser.parse(args);

        assertTrue(teeArgsParser.isAppend());
        assertEquals(Arrays.asList(FILE_1, "file2.txt"), teeArgsParser.getFileNames());
    }

    /**
     * Tests parsing arguments with a single invalid flag.
     * Verifies that an InvalidArgsException is thrown with the correct error message
     * containing the first encountered illegal flag.
     *
     * @param flag The invalid flag to be tested.
     */
    @ParameterizedTest
    @ValueSource(strings = {"-p","-b","-c","-d","-e","-f","-g"})
    void parse_SingleInvalidOption_ThrowsExceptionFirstIllegalFlag(String flag) {
        String expected =  ILLEGAL_FLAG_MSG + flag.replace("-", "");
        String[] args = {flag, FILE_1};
        Throwable exp = assertThrows(InvalidArgsException.class, () -> teeArgsParser.parse(args));
        assertEquals(expected, exp.getMessage());
    }

    /**
     * Tests parsing arguments with multiple invalid flags.
     * Verifies that an InvalidArgsException is thrown with the correct error message
     * containing the first encountered illegal flag.
     */
    @Test
    void parse_InvalidOption_ThrowsExceptionFirstIllegalFlag() {
        String expected =  ILLEGAL_FLAG_MSG + "c";
        String[] args = {"-clw", "-l",  FILE_1};
        Throwable exp = assertThrows(InvalidArgsException.class, () -> teeArgsParser.parse(args));
        assertEquals(expected, exp.getMessage());
    }

    /**
     * Tests parsing arguments with only the "-a" (append) option and no files.
     * Verifies that append mode is true and no files are included (standard input assumed).
     *
     * @throws InvalidArgsException If an error occurs during parsing.
     */
    @Test
    void parse_NoFiles_StandardInputAssumed() throws InvalidArgsException {
        String[] args = {"-a"};
        teeArgsParser.parse(args);

        assertTrue(teeArgsParser.isAppend());
        assertTrue(teeArgsParser.getFileNames().isEmpty());
    }
}

