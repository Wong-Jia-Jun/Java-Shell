package ef2test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.impl.parser.ArgsParser;
import sg.edu.nus.comp.cs4218.impl.parser.PasteArgsParser;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Tests Functionality of pasteargsparser
 */
public class PasteArgsParserTest {

    private PasteArgsParser pasteArgsParser;
    /**
     * The constant ILLEGAL_FLAG_MSG.
     */
    public static final String ILLEGAL_FLAG_MSG = "illegal option -- ";
    /**
     * The constant FILE_1.
     */
    public static final String FILE_1 = "file1.txt ";
    /**
     * The constant FILE_2.
     */
    public static final String FILE_2 = "file2.txt ";

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        pasteArgsParser = new PasteArgsParser();
    }

    /**
     * Is serial no options 2 file return false.
     *
     * @throws InvalidArgsException the invalid args exception
     */
    @Test
    void isSerial_NoOptions2File_ReturnFalse() throws InvalidArgsException {
        String[] args = {FILE_1, FILE_2};
        pasteArgsParser.parse(args);
        assertFalse(pasteArgsParser.isSerial());

    }

    /**
     * Gets file names no options 2 file correct non args flags.
     *
     * @throws InvalidArgsException the invalid args exception
     */
    @Test
    void getFileNames_NoOptions2File_CorrectNonArgsFlags() throws InvalidArgsException {
        String[] args = {FILE_1, FILE_2};
        pasteArgsParser.parse(args);
        assertEquals(List.of(args), pasteArgsParser.getFileNames());

    }

    /**
     * Is serial serial option 2 file return true.
     *
     * @throws InvalidArgsException the invalid args exception
     */
    @Test
    void isSerial_SerialOption2File_ReturnTrue() throws InvalidArgsException {
        String[] args = {"-s", FILE_1, FILE_2};
        pasteArgsParser.parse(args);
        assertTrue(pasteArgsParser.isSerial());
    }

    /**
     * Gets file names serial option 2 file correct non args flags.
     *
     * @throws InvalidArgsException the invalid args exception
     */
    @Test
    void getFileNames_SerialOption2File_CorrectNonArgsFlags() throws InvalidArgsException {
        String[] args = {"-s",FILE_1, FILE_2};
        pasteArgsParser.parse(args);
        assertEquals(List.of(FILE_1, FILE_2), pasteArgsParser.getFileNames());

    }

    /**
     * Is serial no option stdin only return false.
     *
     * @throws InvalidArgsException the invalid args exception
     */
    @Test
    void isSerial_NoOptionStdinOnly_ReturnFalse() throws InvalidArgsException {
        String[] args = {};
        pasteArgsParser.parse(args);
        assertFalse(pasteArgsParser.isSerial());
    }

    /**
     * Gets file names no option stdin only correct non args flags.
     *
     * @throws InvalidArgsException the invalid args exception
     */
    @Test
    void getFileNames_NoOptionStdinOnly_CorrectNonArgsFlags() throws InvalidArgsException {
        String[] args = {};
        pasteArgsParser.parse(args);
        assertTrue(pasteArgsParser.getFileNames().isEmpty());
    }

    /**
     * Is serial serial option stdin only return true.
     *
     * @throws InvalidArgsException the invalid args exception
     */
    @Test
    void isSerial_SerialOptionStdinOnly_ReturnTrue() throws InvalidArgsException {
        String[] args = {"-s"};
        pasteArgsParser.parse(args);
        assertTrue(pasteArgsParser.isSerial());
    }

    /**
     * Gets file names serial option stdin only correct non args flags.
     *
     * @throws InvalidArgsException the invalid args exception
     */
    @Test
    void getFileNames_SerialOptionStdinOnly_CorrectNonArgsFlags() throws InvalidArgsException {
        String[] args = {};
        pasteArgsParser.parse(args);
        assertEquals(List.of(args), pasteArgsParser.getFileNames());
    }

    /**
     * Is serial no option stdin file return false.
     *
     * @throws InvalidArgsException the invalid args exception
     */
    @Test
    void isSerial_NoOptionStdinFile_returnFalse() throws InvalidArgsException {
        String[] args = {"-", FILE_1};
        pasteArgsParser.parse(args);
        assertFalse(pasteArgsParser.isSerial());

    }

    /**
     * Gets file names no option stdin file correct non args flags.
     *
     * @throws InvalidArgsException the invalid args exception
     */
    @Test
    void getFileNames_NoOptionStdinFile_CorrectNonArgsFlags() throws InvalidArgsException {
        String[] args = {"-", FILE_1};
        pasteArgsParser.parse(args);
        assertEquals(List.of(args), pasteArgsParser.getFileNames());
    }

    /**
     * Is serial serial option stdin file return f true.
     *
     * @throws InvalidArgsException the invalid args exception
     */
    @Test
    void isSerial_SerialOptionStdinFile_returnFTrue() throws InvalidArgsException {
        String[] args = {"-s", "-", FILE_1};
        pasteArgsParser.parse(args);
        assertTrue(pasteArgsParser.isSerial());
    }

    /**
     * Gets file names serial option stdin file correct non args flags.
     *
     * @throws InvalidArgsException the invalid args exception
     */
    @Test
    void getFileNames_SerialOptionStdinFile_CorrectNonArgsFlags() throws InvalidArgsException {
        String[] args = {"-s", "-", FILE_1};
        pasteArgsParser.parse(args);
        assertEquals(List.of("-", FILE_1), pasteArgsParser.getFileNames());
    }

    static Stream<Arguments> noPasteStdinProvider() {
        return Stream.of(
                Arguments.of((Object) new String[]{"-s", FILE_1, FILE_2}),
                Arguments.of((Object) new String[]{"-s", FILE_1}),
                Arguments.of((Object) new String[]{FILE_1, FILE_2}),
                Arguments.of((Object) new String[]{FILE_1}),
                Arguments.of((Object) new String[]{"-"})
        );
    }

    /**
     * Tests that when files are provided in the arguments, the `isPasteStdin` method returns false.
     *
     * @param args The command-line arguments to be parsed.
     * @throws InvalidArgsException if an exception occurs during parsing.
     */
    @ParameterizedTest
    @MethodSource("noPasteStdinProvider")
    void isPasteStdin_HasFileArgs_ReturnsFalse(String... args) throws InvalidArgsException {
        pasteArgsParser.parse(args);
        assertFalse(pasteArgsParser.isPasteStdin());
    }

    static Stream<Arguments> pasteStdinProvider() {
        return Stream.of(
                Arguments.of((Object) new String[]{"-s"}),
                Arguments.of((Object) new String[]{})
        );
    }

    /**
     * Tests that when no files are provided in the arguments, the `isPasteStdin` method returns true.
     *
     * @param args The command-line arguments to be parsed.
     * @throws InvalidArgsException if an exception occurs during parsing.
     */
    @ParameterizedTest
    @MethodSource("pasteStdinProvider")
    void isPasteStdin_NoFileArgs_ReturnsTrue(String... args) throws InvalidArgsException {
        pasteArgsParser.parse(args);
        assertTrue(pasteArgsParser.isPasteStdin());
    }

    static Stream<Arguments> pasteFileAndStdinProvider() {
        return Stream.of(
                Arguments.of((Object) new String[]{"-s", FILE_1, "-"}),
                Arguments.of((Object) new String[]{FILE_1, "-"})
        );
    }

    /**
     * Tests that when both file names and `-` (stdin) are provided in the arguments, the `isPasteFileAndStdin` method returns true.
     *
     * @param args The command-line arguments to be parsed.
     * @throws InvalidArgsException if an exception occurs during parsing.
     */
    @ParameterizedTest
    @MethodSource("pasteFileAndStdinProvider")
    void isPasteFileAndStdin_HasFileAndStdinArgs_ReturnsTrue(String... args) throws InvalidArgsException {
        pasteArgsParser.parse(args);
        assertTrue(pasteArgsParser.isPasteFileAndStdin());
    }

    static Stream<Arguments> noPasteFileAndStdinProvider() {
        return Stream.of(
                Arguments.of((Object) new String[]{"-s", FILE_1, FILE_2}),
                Arguments.of((Object) new String[]{}),
                Arguments.of((Object) new String[]{FILE_1, FILE_2})
        );
    }

    /**
     * Tests that when no files or a mix of files and "-" are provided in the arguments, the `isPasteFileAndStdin` method returns false.
     *
     * @param args The command-line arguments to be parsed.
     * @throws InvalidArgsException if an exception occurs during parsing.
     */
    @ParameterizedTest
    @MethodSource("noPasteFileAndStdinProvider")
    void isPasteFileAndStdin_NoFileAndStdinArgs_ReturnsFalse(String... args) throws InvalidArgsException {
        pasteArgsParser.parse(args);
        assertFalse(pasteArgsParser.isPasteFileAndStdin());
    }

    static Stream<Arguments> pasteFilesProvider() {
        return Stream.of(
                Arguments.of((Object) new String[]{"-s", FILE_1, FILE_2}),
                Arguments.of((Object) new String[]{FILE_1, FILE_2})
        );
    }

    /**
     * Tests that when files are provided in the arguments, the `isPasteFiles` method returns true.
     *
     * @param args The command-line arguments to be parsed.
     * @throws InvalidArgsException if an exception occurs during parsing.
     */
    @ParameterizedTest
    @MethodSource("pasteFilesProvider")
    void isPasteFiles_HasFileArgs_ReturnsTrue(String... args) throws InvalidArgsException {
        pasteArgsParser.parse(args);
        assertTrue(pasteArgsParser.isPasteFiles());
    }

    static Stream<Arguments> noPasteFilesProvider() {
        return Stream.of(
                Arguments.of((Object) new String[]{"-s"}),
                Arguments.of((Object) new String[]{}),
                Arguments.of((Object) new String[]{"-s", FILE_1, "-"}),
                Arguments.of((Object) new String[]{"-"})
        );
    }

    /**
     * Tests that when no files are provided in the arguments, the `isPasteFiles` method returns false.
     *
     * @param args The command-line arguments to be parsed.
     * @throws InvalidArgsException if an exception occurs during parsing.
     */
    @ParameterizedTest
    @MethodSource("noPasteFilesProvider")
    void isPasteFiles_NoFileArgs_ReturnsFalse(String... args) throws InvalidArgsException {
        pasteArgsParser.parse(args);
        assertFalse(pasteArgsParser.isPasteFiles());
    }

    /**
     * Parse single invalid option throws exception first illegal flag.
     *
     * @param flag the flag
     */
    @ParameterizedTest
    @ValueSource(strings = {"-a", "-b", "-c", "-d", "-e", "-f", "-g"})
    void parse_SingleInvalidOption_ThrowsExceptionFirstIllegalFlag(String flag) {
        String expected = ILLEGAL_FLAG_MSG + flag.replace("-", "");
        String[] args = {flag, "file1.txt"};
        Throwable exp = assertThrows(InvalidArgsException.class, () -> pasteArgsParser.parse(args));
        assertEquals(expected, exp.getMessage());
    }
}
