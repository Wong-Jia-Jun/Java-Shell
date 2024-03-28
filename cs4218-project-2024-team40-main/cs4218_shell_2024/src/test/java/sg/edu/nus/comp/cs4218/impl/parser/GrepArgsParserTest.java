package sg.edu.nus.comp.cs4218.impl.parser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.GrepException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;

import static org.junit.jupiter.api.Assertions.*;

public class GrepArgsParserTest {
    private GrepArgsParser grepArgsParser;
    public static final String FILE_1 = "file1.txt";
    public static final String FILE_2 = "file2.txt";
    public static final String PATTERN = "pattern";
    private static final String CPLX_PATTERN = "complexPattern";
    @BeforeEach
    void setUp() {
        grepArgsParser = new GrepArgsParser();
    }

    /**
     * Test case where no options are provided, only the pattern is provided.
     * @throws InvalidArgsException
     */
    @Test
    void parse_NoOptions_PatternAndNoFiles() throws InvalidArgsException {
        String[] args = {"testPattern"};
        grepArgsParser.parse(args);

        assertArrayEquals(new String[]{}, grepArgsParser.getFileNames()); // Expecting an empty array now
        assertEquals("testPattern", grepArgsParser.getPattern());
        assertFalse(grepArgsParser.isCount());
        assertFalse(grepArgsParser.isIgnoreCase());
        assertFalse(grepArgsParser.isPrintFileName());
    }

    /**
     * Test case where no options are provided, only the pattern is provided.
     */
    @Test
    void parse_NoPattern_ThrowsException() {
        String[] args = {"-i"};
        Throwable exp = assertThrows(InvalidArgsException.class, () -> grepArgsParser.parse(args));
        assertEquals(new IllegalArgumentException(GrepArgsParser.ILLEGAL_NO_PAT).getMessage(), exp.getMessage());
    }

    @Test
    void parse_CaseInsensitiveOptionWithPattern_CorrectVariables() throws InvalidArgsException {
        String[] args = {"-i", PATTERN};
        grepArgsParser.parse(args);

        assertTrue(grepArgsParser.isIgnoreCase());
        assertFalse(grepArgsParser.isCount());
        assertFalse(grepArgsParser.isPrintFileName());
        assertEquals(PATTERN, grepArgsParser.getPattern());
        assertArrayEquals(new String[]{}, grepArgsParser.getFileNames());
    }

    @Test
    void parse_CountOptionWithPatternAndFiles_CorrectVariables() throws InvalidArgsException {
        String[] args = {"-c", PATTERN, FILE_1};
        grepArgsParser.parse(args);

        assertTrue(grepArgsParser.isCount());
        assertFalse(grepArgsParser.isIgnoreCase());
        assertFalse(grepArgsParser.isPrintFileName());
        assertEquals(PATTERN, grepArgsParser.getPattern());
        assertArrayEquals(new String[]{FILE_1}, grepArgsParser.getFileNames());
    }

    @Test
    void parse_PrintFileNameOptionWithPatternAndFiles_CorrectVariables() throws InvalidArgsException {
        String[] args = {"-H", PATTERN, FILE_1};
        grepArgsParser.parse(args);

        assertTrue(grepArgsParser.isPrintFileName());
        assertFalse(grepArgsParser.isIgnoreCase());
        assertFalse(grepArgsParser.isCount());
        assertEquals(PATTERN, grepArgsParser.getPattern());
        assertArrayEquals(new String[]{FILE_1}, grepArgsParser.getFileNames());
    }

    @Test
    void parse_MultipleOptionsWithPatternAndFiles_CorrectVariables() throws InvalidArgsException {
        String[] args = {"-i", "-c", "-H", "searchPattern", FILE_1, FILE_2};
        grepArgsParser.parse(args);

        assertTrue(grepArgsParser.isIgnoreCase());
        assertTrue(grepArgsParser.isCount());
        assertTrue(grepArgsParser.isPrintFileName());
        assertEquals("searchPattern", grepArgsParser.getPattern());
        assertArrayEquals(new String[]{FILE_1, FILE_2}, grepArgsParser.getFileNames());
    }

    @Test
    void parse_EmptyArguments_ThrowsException() {
        String[] args = {};
        Throwable exp = assertThrows(InvalidArgsException.class, () -> grepArgsParser.parse(args));
        assertEquals(new IllegalArgumentException(GrepArgsParser.ILLEGAL_NO_PAT).getMessage(), exp.getMessage());
    }

    @Test
    void parse_CaseInsensitiveAndCountOptionsWithPatternAndFiles_CorrectVariables() throws InvalidArgsException {
        String[] args = {"-i", "-c", PATTERN, FILE_1};
        grepArgsParser.parse(args);

        assertTrue(grepArgsParser.isIgnoreCase());
        assertTrue(grepArgsParser.isCount());
        assertEquals(PATTERN, grepArgsParser.getPattern());
        assertArrayEquals(new String[]{FILE_1}, grepArgsParser.getFileNames());
    }

    @Test
    void parse_CountAndPrintFileNameOptionsWithPatternAndMultipleFiles_CorrectVariables() throws InvalidArgsException {
        String[] args = {"-c", "-H", PATTERN, FILE_1, FILE_2};
        grepArgsParser.parse(args);

        assertTrue(grepArgsParser.isCount());
        assertTrue(grepArgsParser.isPrintFileName());
        assertEquals(PATTERN, grepArgsParser.getPattern());
        assertArrayEquals(new String[]{FILE_1, FILE_2}, grepArgsParser.getFileNames());
    }

    @Test
    void parse_AllOptionsTogether_CorrectVariables() throws InvalidArgsException {
        String[] args = {"-i", "-c", "-H", CPLX_PATTERN, FILE_1, FILE_2};
        grepArgsParser.parse(args);

        assertTrue(grepArgsParser.isIgnoreCase());
        assertTrue(grepArgsParser.isCount());
        assertTrue(grepArgsParser.isPrintFileName());
        assertEquals(CPLX_PATTERN, grepArgsParser.getPattern());
        assertArrayEquals(new String[]{FILE_1, FILE_2}, grepArgsParser.getFileNames());
    }

    @Test
    void parse_InvalidOption_ThrowsException() {
        String[] args = {"-x", PATTERN};
        Throwable exp = assertThrows(InvalidArgsException.class, () -> grepArgsParser.parse(args));
        assertEquals(new IllegalArgumentException(GrepArgsParser.ILLEGAL_FLAG_MSG + "x").getMessage(), exp.getMessage());
    }

    @Test
    void parse_CaseInsensitiveOptionWithoutPattern_ThrowsException() {
        String[] args = {"-i"};
        assertThrows(InvalidArgsException.class, () -> grepArgsParser.parse(args));
    }

    @Test
    void parse_CountOptionWithoutPattern_ThrowsException() {
        String[] args = {"-c"};
        assertThrows(InvalidArgsException.class, () -> grepArgsParser.parse(args));
    }

    @Test
    void isGrepFromStdin_Yes_ReturnsTrue() throws InvalidArgsException {
        String[] args = {"-i", "-c", "-H", CPLX_PATTERN};
        grepArgsParser.parse(args);
        assertTrue(grepArgsParser.isGrepFromStdin());
        assertFalse(grepArgsParser.isGrepFromFiles());
        assertFalse(grepArgsParser.isGrepFromFilesAndStdin());
    }

    @Test
    void isGrepFromFiles_Yes_ReturnsTrue() throws InvalidArgsException {
        String[] args = {"-i", "-c", "-H", CPLX_PATTERN, FILE_1, FILE_2};
        grepArgsParser.parse(args);
        assertFalse(grepArgsParser.isGrepFromStdin());
        assertTrue(grepArgsParser.isGrepFromFiles());
        assertFalse(grepArgsParser.isGrepFromFilesAndStdin());
    }

    @Test
    void isGrepFromFilesAndStdin_Yes_ReturnsTrue() throws InvalidArgsException {
        String[] args = {"-i", "-c", "-H", CPLX_PATTERN, FILE_1, "-"};
        grepArgsParser.parse(args);
        assertFalse(grepArgsParser.isGrepFromStdin());
        assertFalse(grepArgsParser.isGrepFromFiles());
        assertTrue(grepArgsParser.isGrepFromFilesAndStdin());
    }
}