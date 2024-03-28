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
 * This class contains unit tests for the {@link MvArgsParser} class, specifically verifying its
 * ability to parse command-line arguments correctly.
 */
public class MvArgsParserTest {
    public static final String FILE = "file";
    public static final String FILE_2 = "file2";
    public static final String FILE_3 = "file3";
    MvArgsParser parser;

    @BeforeEach
    public void setUp() {
        parser = new MvArgsParser();
    }

    static Stream<Arguments> threeCombinationsWithFlagProvider() {
        return Stream.of(
                Arguments.of((Object) new String[]{"-n", FILE}),
                Arguments.of((Object) new String[]{"-n", FILE, FILE_2}),
                Arguments.of((Object) new String[]{"-n", FILE, FILE_2, FILE_3})
        );
    }

    static Stream<Arguments> threeCombinationsWithNoFlagProvider() {
        return Stream.of(
                Arguments.of((Object) new String[]{FILE}),
                Arguments.of((Object) new String[]{FILE, FILE_2}),
                Arguments.of((Object) new String[]{FILE, FILE_2, FILE_3})
        );
    }


    /**
     * Tests that when the `-n` flag is not present in the arguments, the `isOverwrite` method returns true.
     *
     * @param args The command-line arguments to be parsed.
     * @throws InvalidArgsException if an exception occurs during parsing.
     */
    @ParameterizedTest
    @MethodSource("threeCombinationsWithNoFlagProvider")
    void isOverwrite_NoFlag_ReturnsTrue(String... args) throws InvalidArgsException {
        parser.parse(args);
        assertTrue(parser.isOverwrite());
    }


    /**
     * Tests that when the `-n` flag is present in the arguments, the `isOverwrite` method returns false.
     *
     * @param args The command-line arguments to be parsed.
     * @throws InvalidArgsException if an exception occurs during parsing.
     */
    @ParameterizedTest
    @MethodSource("threeCombinationsWithFlagProvider")
    void isOverwrite_ContainsFlag_ReturnsFalse(String... args) throws InvalidArgsException {
        parser.parse(args);
        assertFalse(parser.isOverwrite());
    }

    /**
     * Tests that the `getFileNames` method returns all the file names provided in the arguments.
     *
     * @throws InvalidArgsException if an exception occurs during parsing.
     */
    @Test
    void getFileNames_HasFiles_ReturnsFiles() throws InvalidArgsException {
        String[] args = {"-n", FILE, FILE_2};
        parser.parse(args);
        assertTrue(parser.getFileNames().contains(FILE));
        assertTrue(parser.getFileNames().contains(FILE_2));
        assertEquals(2, parser.getFileNames().size());
    }

    /**
     * Tests that the `getFileNames` method returns an empty list when no files are provided in the arguments.
     *
     * @throws InvalidArgsException if an exception occurs during parsing.
     */
    @Test
    void getFileNames_NoFiles_ReturnsEmptyList() throws InvalidArgsException {
        String[] args = {"-n"};
        parser.parse(args);
        assertTrue(parser.getFileNames().isEmpty());
    }
}
