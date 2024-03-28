package sg.edu.nus.comp.cs4218.impl.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests the functionality of the `WcArgsParser` class.
 * It verifies the parsing behavior for various input scenarios involving flags and file names.
 */
public class WcArgsParserTest {
    public static final String FILE_1 = "file1.txt";
    public static final String FILE_2 = "file2.txt";
    private WcArgsParser wcArgsParser;

    @BeforeEach
    void setUp() {
        wcArgsParser = new WcArgsParser();
    }

    /**
     * Tests parsing arguments with no options. Verifies that the single file is included and all
     * word, line, and byte count options are false.
     *
     * @throws InvalidArgsException If an error occurs during parsing.
     */
    @Test
    void parse_NoOptions_DefaultBehavior() throws InvalidArgsException {
        String[] args = {FILE_1};
        wcArgsParser.parse(args);

        assertTrue(wcArgsParser.getFileNames().contains(FILE_1));
        assertFalse(wcArgsParser.isByteCount());
        assertFalse(wcArgsParser.isLineCount());
        assertFalse(wcArgsParser.isWordCount());
    }

    /**
     * Tests parsing arguments with the "-c" (byte count) option and a single file.
     * Verifies that byte count is true, other options are false, and the file is included.
     *
     * @throws InvalidArgsException If an error occurs during parsing.
     */
    @Test
    void parse_ByteCountOptionWithFiles_ShouldReturnCorrectList() throws InvalidArgsException {
        String[] args = {"-c", FILE_1};
        wcArgsParser.parse(args);

        assertTrue(wcArgsParser.isByteCount());
        assertFalse(wcArgsParser.isLineCount());
        assertFalse(wcArgsParser.isWordCount());
        assertEquals(Arrays.asList(FILE_1), wcArgsParser.getFileNames());
    }

    /**
     * Tests parsing arguments with the "-l" (line count) option and a single file.
     * Verifies that line count is true, other options are false, and the file is included.
     *
     * @throws InvalidArgsException If an error occurs during parsing.
     */
    @Test
    void parse_LineCountOptionWithFiles_ShouldReturnCorrectList() throws InvalidArgsException {
        String[] args = {"-l", FILE_1};
        wcArgsParser.parse(args);

        assertFalse(wcArgsParser.isByteCount());
        assertTrue(wcArgsParser.isLineCount());
        assertFalse(wcArgsParser.isWordCount());
        assertEquals(Arrays.asList(FILE_1), wcArgsParser.getFileNames());
    }

    /**
     * Tests parsing arguments with the "-w" (word count) option and a single file.
     * Verifies that word count is true, other options are false, and the file is included.
     *
     * @throws InvalidArgsException If an error occurs during parsing.
     */
    @Test
    void parse_WordCountOptionWithFiles_ShouldReturnCorrectList() throws InvalidArgsException {
        String[] args = {"-w", FILE_1};
        wcArgsParser.parse(args);

        assertFalse(wcArgsParser.isByteCount());
        assertFalse(wcArgsParser.isLineCount());
        assertTrue(wcArgsParser.isWordCount());
        assertEquals(List.of(FILE_1), wcArgsParser.getFileNames());
    }

    /**
     * Tests parsing arguments with all options ("-clw") and multiple files.
     * Verifies that all options (byte count, line count, word count) are true and
     * all files are included.
     *
     * @throws InvalidArgsException If an error occurs during parsing.
     */
    @Test
    void parse_AllOptionsWithMultipleFiles_ShouldReturnCorrectList() throws InvalidArgsException {
        String[] args = {"-clw", FILE_1, FILE_2};
        wcArgsParser.parse(args);

        assertTrue(wcArgsParser.isByteCount());
        assertTrue(wcArgsParser.isLineCount());
        assertTrue(wcArgsParser.isWordCount());
        assertEquals(Arrays.asList(FILE_1, FILE_2), wcArgsParser.getFileNames());
    }

    /**
     * Tests parsing arguments with separate options ("-c", "-l", "-w") and multiple files.
     * Verifies that all options (byte count, line count, word count) are true and
     * all files are included.
     *
     * @throws InvalidArgsException If an error occurs during parsing.
     */
    @Test
    void parse_OptionsSeparatedWithMultipleFiles_ShouldReturnCorrectList() throws InvalidArgsException {
        String[] args = {"-c", "-l", "-w", FILE_1, FILE_2};
        wcArgsParser.parse(args);

        assertTrue(wcArgsParser.isByteCount());
        assertTrue(wcArgsParser.isLineCount());
        assertTrue(wcArgsParser.isWordCount());
        assertEquals(Arrays.asList(FILE_1, FILE_2), wcArgsParser.getFileNames());
    }

    /**
     * Tests parsing arguments with an invalid flag.
     * Verifies that an InvalidArgsException is thrown.
     */
    @Test
    void parse_InvalidOption_ThrowsException() {
        String[] args = {"-z", FILE_1};
        assertThrows(InvalidArgsException.class, () -> wcArgsParser.parse(args));
    }

    /**
     * Tests parsing arguments with only the "-w" (word count) option and no files.
     * Verifies that word count is true and no files are included (standard input assumed).
     *
     * @throws InvalidArgsException If an error occurs during parsing.
     */
    @Test
    void parse_NoFiles_StandardInputAssumed() throws InvalidArgsException {
        String[] args = {"-w"};
        wcArgsParser.parse(args);

        assertTrue(wcArgsParser.isWordCount());
        assertTrue(wcArgsParser.getFileNames().isEmpty());
    }
}
