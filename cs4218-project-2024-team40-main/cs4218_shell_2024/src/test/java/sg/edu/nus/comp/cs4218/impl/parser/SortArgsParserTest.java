package sg.edu.nus.comp.cs4218.impl.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests the functionality of the `SortArgsParser` class.
 * It verifies the parsing behavior for various input scenarios involving flags and file names.
 */
public class SortArgsParserTest {
    public static final String FILE = "file1.txt";
    private SortArgsParser sortArgsParser;

    @BeforeEach
    void setUp() {
        sortArgsParser = new SortArgsParser();
    }

    /**
     * Tests if isFirstWordNumber returns false when no "-n" option is provided.
     *
     * @throws InvalidArgsException If an error occurs during parsing.
     */
    @Test
    void isFirstWordNumber_NoFirstWordNumberOption_ReturnsFalse() throws InvalidArgsException {
        String[] args = {FILE};
        sortArgsParser.parse(args);
        assertFalse(sortArgsParser.isFirstWordNumber());
    }

    /**
     * Tests if isFirstWordNumber returns true when the "-n" option is provided.
     *
     * @throws InvalidArgsException If an error occurs during parsing.
     */
    @Test
    void isFirstWordNumber_FirstWordNumberOption_ReturnsTrue() throws InvalidArgsException {
        String[] args = {"-n", FILE};
        sortArgsParser.parse(args);
        assertTrue(sortArgsParser.isFirstWordNumber());
    }

    /**
     * Tests if isReverseOrder returns false when no "-r" option is provided.
     *
     * @throws InvalidArgsException If an error occurs during parsing.
     */
    @Test
    void isReverseOrder_NoReverseOrderOption_ReturnsFalse() throws InvalidArgsException {
        String[] args = {FILE};
        sortArgsParser.parse(args);
        assertFalse(sortArgsParser.isReverseOrder());
    }

    /**
     * Tests if isReverseOrder returns true when the "-r" option is provided.
     *
     * @throws InvalidArgsException If an error occurs during parsing.
     */
    @Test
    void isReverseOrder_ReverseOrderOption_ReturnsTrue() throws InvalidArgsException {
        String[] args = {"-r", FILE};
        sortArgsParser.parse(args);
        assertTrue(sortArgsParser.isReverseOrder());
    }

    /**
     * Tests if isCaseIndependent returns false when no "-f" option is provided.
     *
     * @throws InvalidArgsException If an error occurs during parsing.
     */
    @Test
    void isCaseIndependent_NoCaseIndependentOption_ReturnsFalse() throws InvalidArgsException {
        String[] args = {FILE};
        sortArgsParser.parse(args);
        assertFalse(sortArgsParser.isCaseIndependent());
    }

    /**
     * Tests if isCaseIndependent returns true when the "-f" option is provided.
     *
     * @throws InvalidArgsException If an error occurs during parsing.
     */
    @Test
    void isCaseIndependent_CaseIndependentOption_ReturnsTrue() throws InvalidArgsException {
        String[] args = {"-f", FILE};
        sortArgsParser.parse(args);
        assertTrue(sortArgsParser.isCaseIndependent());
    }

    static Stream<Arguments> noFilesProvider() {
        return Stream.of(
                Arguments.of((Object) new String[]{"-f"}),
                Arguments.of((Object) new String[]{}),
                Arguments.of((Object) new String[]{"-f", "-r"}),
                Arguments.of((Object) new String[]{"-f", "-n", "-r"})
        );
    }

    static Stream<Arguments> hasFilesProvider() {
        return Stream.of(
                Arguments.of((Object) new String[]{"-f", "file"}),
                Arguments.of((Object) new String[]{"file", "files"}),
                Arguments.of((Object) new String[]{"-f", "-r", "files"}),
                Arguments.of((Object) new String[]{"-f", "-n", "-r", "file"})
        );
    }

    /**
     * Tests if isSortFromStdin returns true when no file names are provided.
     *
     * @param args The arguments to be parsed.
     * @throws InvalidArgsException If an error occurs during parsing.
     */
    @ParameterizedTest
    @MethodSource("noFilesProvider")
    void isSortFromStdin_NoFiles_ReturnsTrue(String... args) throws InvalidArgsException {
        sortArgsParser.parse(args);
        assertTrue(sortArgsParser.isSortFromStdin());
    }

    /**
     * Tests if isSortFromStdin returns false when file names are provided.
     *
     * @param args The arguments to be parsed.
     * @throws InvalidArgsException If an error occurs during parsing.
     */
    @ParameterizedTest
    @MethodSource("hasFilesProvider")
    void isSortFromStdin_HasFiles_ReturnsFalse(String... args) throws InvalidArgsException {
        sortArgsParser.parse(args);
        assertFalse(sortArgsParser.isSortFromStdin());
    }

    /**
     * Tests if isSortFromFiles returns false when no file names are provided.
     *
     * @param args The arguments to be parsed.
     * @throws InvalidArgsException If an error occurs during parsing.
     */
    @ParameterizedTest
    @MethodSource("noFilesProvider")
    void isSortFromFiles_NoFiles_ReturnsFalse(String... args) throws InvalidArgsException {
        sortArgsParser.parse(args);
        assertFalse(sortArgsParser.isSortFromFiles());
    }

    /**
     * Tests if isSortFromFiles returns true when file names are provided.
     *
     * @param args The arguments to be parsed.
     * @throws InvalidArgsException If an error occurs during parsing.
     */
    @ParameterizedTest
    @MethodSource("hasFilesProvider")
    void isSortFromFiles_HasFiles_ReturnsTrue(String... args) throws InvalidArgsException {
        sortArgsParser.parse(args);
        assertTrue(sortArgsParser.isSortFromFiles());
    }

    /**
     * Tests if getFileNames returns an empty list when no file names are provided.
     *
     * @throws InvalidArgsException If an error occurs during parsing.
     */
    @Test
    void getFileNames_NoFiles_ReturnsEmptyList() throws InvalidArgsException {
        String[] args = {"-n"};
        sortArgsParser.parse(args);
        assertTrue(sortArgsParser.getFileNames().isEmpty());
    }

    /**
     * Tests if getFileNames returns a list containing the provided file names.
     *
     * @throws InvalidArgsException If an error occurs during parsing.
     */
    @Test
    void getFileNames_MultipleFiles_ReturnsListOfFiles() throws InvalidArgsException {
        String[] args = {"-n", FILE, "file2.txt"};
        sortArgsParser.parse(args);
        assertEquals(Arrays.asList(FILE, "file2.txt"), sortArgsParser.getFileNames());
    }

    /**
     * Tests parsing arguments with no options. Verifies default behavior and no exception.
     *
     * @throws InvalidArgsException If an error occurs during parsing.
     */
    @Test
    void parse_NoOptions_DefaultBehaviorNoError() throws InvalidArgsException {
        String[] args = {FILE};
        sortArgsParser.parse(args);

        assertTrue(sortArgsParser.getFileNames().contains(FILE));
        assertFalse(sortArgsParser.isFirstWordNumber());
        assertFalse(sortArgsParser.isReverseOrder());
        assertFalse(sortArgsParser.isCaseIndependent());
    }

//    @Test
//    void parse_FirstWordNumberOptionWithFiles() throws InvalidArgsException {
//        String[] args = {"-n", "file1.txt"};
//        sortArgsParser.parse(args);
//
//        assertTrue(sortArgsParser.isFirstWordNumber());
//        assertFalse(sortArgsParser.isReverseOrder());
//        assertFalse(sortArgsParser.isCaseIndependent());
//        assertEquals(Arrays.asList("file1.txt"), sortArgsParser.getFileNames());
//    }
//
//    @Test
//    void parse_ReverseOrderOptionWithFiles() throws InvalidArgsException {
//        String[] args = {"-r", "file1.txt"};
//        sortArgsParser.parse(args);
//
//        assertFalse(sortArgsParser.isFirstWordNumber());
//        assertTrue(sortArgsParser.isReverseOrder());
//        assertFalse(sortArgsParser.isCaseIndependent());
//        assertEquals(List.of("file1.txt"), sortArgsParser.getFileNames());
//    }
//
//    @Test
//    void parse_CaseIndependentOptionWithFiles() throws InvalidArgsException {
//        String[] args = {"-f", "file1.txt"};
//        sortArgsParser.parse(args);
//
//        assertFalse(sortArgsParser.isFirstWordNumber());
//        assertFalse(sortArgsParser.isReverseOrder());
//        assertTrue(sortArgsParser.isCaseIndependent());
//        assertEquals(List.of("file1.txt"), sortArgsParser.getFileNames());
//    }
//
//    @Test
//    void parse_AllOptionsWithMultipleFiles() throws InvalidArgsException {
//        String[] args = {"-nrf", "file1.txt", "file2.txt"};
//        sortArgsParser.parse(args);
//
//        assertTrue(sortArgsParser.isFirstWordNumber());
//        assertTrue(sortArgsParser.isReverseOrder());
//        assertTrue(sortArgsParser.isCaseIndependent());
//        assertEquals(Arrays.asList("file1.txt", "file2.txt"), sortArgsParser.getFileNames());
//    }
//
//    @Test
//    void parse_OptionsSeparatedWithMultipleFiles() throws InvalidArgsException {
//        String[] args = {"-n", "-r", "-f", "file1.txt", "file2.txt"};
//        sortArgsParser.parse(args);
//
//        assertTrue(sortArgsParser.isFirstWordNumber());
//        assertTrue(sortArgsParser.isReverseOrder());
//        assertTrue(sortArgsParser.isCaseIndependent());
//        assertEquals(Arrays.asList("file1.txt", "file2.txt"), sortArgsParser.getFileNames());
//    }
//
//    @Test
//    void parse_InvalidOption_ThrowsException() {
//        String[] args = {"-z", "file1.txt"};
//        assertThrows(InvalidArgsException.class, () -> sortArgsParser.parse(args));
//    }
//
//    @Test
//    void parse_NoFiles_StandardInputAssumed() throws InvalidArgsException {
//        String[] args = {"-n"};
//        sortArgsParser.parse(args);
//
//        assertTrue(sortArgsParser.isFirstWordNumber());
//        assertTrue(sortArgsParser.getFileNames().isEmpty());
//    }
}

