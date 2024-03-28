package sg.edu.nus.comp.cs4218.impl.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class contains unit tests for the {@link CatArgsParser} class, specifically verifying its
 * ability to parse command-line arguments correctly.
 */
public class CatArgsParserTest {
    public static final String FILE = "file";
    public static final String FILE_2 = "file2";
    CatArgsParser catArgsParser;

    @BeforeEach
    public void setUp() {
        catArgsParser = new CatArgsParser();
    }

    static Stream<Arguments> threeCombinationsWithLineNumberProvider() {
        return Stream.of(
                Arguments.of((Object) new String[]{"-n", FILE, FILE_2}), // files
                Arguments.of((Object) new String[]{"-n"}), // stdin
                Arguments.of((Object) new String[]{"-n", FILE, "-"}) //stdin and files
        );
    }

    static Stream<Arguments> noCatStdinProvider() {
        return Stream.of(
                Arguments.of((Object) new String[]{"-n", FILE, FILE_2}),
                Arguments.of((Object) new String[]{"-n", FILE, "-"}),
                Arguments.of((Object) new String[]{FILE, FILE_2}),
                Arguments.of((Object) new String[]{FILE, "-"})
        );
    }

    static Stream<Arguments> catStdinProvider() {
        return Stream.of(
                Arguments.of((Object) new String[]{"-n"}),
                Arguments.of((Object) new String[]{})
        );
    }

    static Stream<Arguments> catFileAndStdinProvider() {
        return Stream.of(
                Arguments.of((Object) new String[]{"-n", FILE, "-"}),
                Arguments.of((Object) new String[]{FILE, "-"})
        );
    }

    static Stream<Arguments> noCatFileAndStdinProvider() {
        return Stream.of(
                Arguments.of((Object) new String[]{"-n", FILE, FILE_2}),
                Arguments.of((Object) new String[]{FILE, FILE_2}),
                Arguments.of((Object) new String[]{}),
                Arguments.of((Object) new String[]{"-n"})
        );
    }

    static Stream<Arguments> catFilesProvider() {
        return Stream.of(
                Arguments.of((Object) new String[]{"-n", FILE, FILE_2}),
                Arguments.of((Object) new String[]{FILE, FILE_2})
        );
    }

    static Stream<Arguments> noCatFilesProvider() {
        return Stream.of(
                Arguments.of((Object) new String[]{"-n"}),
                Arguments.of((Object) new String[]{}),
                Arguments.of((Object) new String[]{"-n", FILE, "-"}),
                Arguments.of((Object) new String[]{FILE, "-"})
        );
    }

    /**
     * Tests that when the `-n` flag is present in the arguments, the `isShowLineNumber` method returns true.
     *
     * @param args The command-line arguments to be parsed.
     * @throws InvalidArgsException if an exception occurs during parsing.
     */
    @ParameterizedTest
    @MethodSource("threeCombinationsWithLineNumberProvider")
    void isShowLineNumber_ContainsFlag_ReturnsTrue(String... args) throws InvalidArgsException {
        catArgsParser.parse(args);
        assertTrue(catArgsParser.isShowLineNumber());
    }

    /**
     * Tests that when no files are provided in the arguments, the `isCatStdin` method returns true.
     *
     * @param args The command-line arguments to be parsed.
     * @throws InvalidArgsException if an exception occurs during parsing.
     */
    @ParameterizedTest
    @MethodSource("catStdinProvider")
    void isCatStdin_NoFiles_ReturnsTrue(String... args) throws InvalidArgsException {
        catArgsParser.parse(args);
        assertTrue(catArgsParser.isCatStdin());
    }

    /**
     * Tests that when files or a mix of files and "-" are provided in the arguments, the `isCatStdin` method returns false.
     *
     * @param args The command-line arguments to be parsed.
     * @throws InvalidArgsException if an exception occurs during parsing.
     */
    @ParameterizedTest
    @MethodSource("noCatStdinProvider")
    void isCatStdin_HasFilesOrMixed_ReturnsFalse(String... args) throws InvalidArgsException {
        catArgsParser.parse(args);
        assertFalse(catArgsParser.isCatStdin());
    }

    /**
     * Tests that when a mix of a file and "-" is provided in the arguments, the `isCatFileAndStdin` method returns true.
     *
     * @param args The command-line arguments to be parsed.
     * @throws InvalidArgsException if an exception occurs during parsing.
     */
    @ParameterizedTest
    @MethodSource("catFileAndStdinProvider")
    void isCatFileAndStdin_HasMixedFileAndDash_ReturnsTrue(String... args) throws InvalidArgsException {
        catArgsParser.parse(args);
        assertTrue(catArgsParser.isCatFileAndStdin());
    }

    /**
     * Tests that when no mix of file and "-" is provided in the arguments, the `isCatFileAndStdin` method returns false.
     *
     * @param args The command-line arguments to be parsed.
     * @throws InvalidArgsException if an exception occurs during parsing.
     */
    @ParameterizedTest
    @MethodSource("noCatFileAndStdinProvider")
    void isCatFileAndStdin_NoMixedFileAndDash_ReturnsFalse(String... args) throws InvalidArgsException {
        catArgsParser.parse(args);
        assertFalse(catArgsParser.isCatFileAndStdin());
    }

    /**
     * Tests that when at least one file is provided in the arguments, the `isCatFiles` method returns true.
     *
     * @param args The command-line arguments to be parsed.
     * @throws InvalidArgsException if an exception occurs during parsing.
     */
    @ParameterizedTest
    @MethodSource("catFilesProvider")
    void isCatFiles_HasFiles_ReturnsTrue(String... args) throws InvalidArgsException {
        catArgsParser.parse(args);
        assertTrue(catArgsParser.isCatFiles());
    }

    /**
     * Tests that when no files are provided in the arguments, the `isCatFiles` method returns false.
     *
     * @param args The command-line arguments to be parsed.
     * @throws InvalidArgsException if an exception occurs during parsing.
     */
    @ParameterizedTest
    @MethodSource("noCatFilesProvider")
    void isCatFiles_NoFiles_ReturnsFalse(String... args) throws InvalidArgsException {
        catArgsParser.parse(args);
        assertFalse(catArgsParser.isCatFiles());
    }

    /**
     * Tests that the `getFileNames` method returns all the file names provided in the arguments.
     *
     * @throws InvalidArgsException if an exception occurs during parsing.
     */
    @Test
    void getFileNames_HasFiles_ReturnsFiles() throws InvalidArgsException {
        String[] args = {"-n", FILE, FILE_2};
        catArgsParser.parse(args);
        assertTrue(catArgsParser.getFileNames().contains(FILE));
        assertTrue(catArgsParser.getFileNames().contains(FILE_2));
        assertTrue(catArgsParser.getFileNames().size() == 2);
    }

    /**
     * Tests that the `getFileNames` method returns an empty list when no files are provided in the arguments.
     *
     * @throws InvalidArgsException if an exception occurs during parsing.
     */
    @Test
    void getFileNames_NoFiles_ReturnsEmptyList() throws InvalidArgsException {
        String[] args = {"-n"};
        catArgsParser.parse(args);
        assertTrue(catArgsParser.getFileNames().isEmpty());
    }
}
