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
 * This class contains unit tests for the {@link MkdirArgsParser} class, specifically verifying its
 * ability to parse command-line arguments correctly.
 */
public class MkdirArgsParserTest {
    private MkdirArgsParser mkdirArgsParser;
    public static final String ILLEGAL_FLAG_MSG = "illegal option -- ";
    public static final String FILE_1 = "file1.txt ";

    @BeforeEach
    void setUp() {
        mkdirArgsParser = new MkdirArgsParser();
    }

    /**
     * Tests that when no options are provided, `isCreateMissingParent` returns false and
     * `getFileNames` returns the single file argument.
     *
     * @throws InvalidArgsException if an exception occurs during parsing.
     */
    @Test
    void isCreateMissingParent_NoOptions_ReturnFalse() throws InvalidArgsException {
        String[] args = {FILE_1};
        mkdirArgsParser.parse(args);
        assertFalse(mkdirArgsParser.isCreateMissingParent());
    }

    /**
     * Tests that when no options are provided, `getFileNames` returns the single file argument.
     *
     * @throws InvalidArgsException if an exception occurs during parsing.
     */
    @Test
    void getFileNames_NoOptions_CorrectNonArgsFlags() throws InvalidArgsException {
        String[] args = {FILE_1};
        mkdirArgsParser.parse(args);
        assertEquals(List.of(FILE_1), mkdirArgsParser.getFileNames());
    }

    /**
     * Tests that when the `-p` option is provided, `isCreateMissingParent` returns true and
     * `getFileNames` returns the single file argument.
     *
     * @throws InvalidArgsException if an exception occurs during parsing.
     */
    @Test
    void isCreateMissingParent_CreateParentOption_ReturnTrue() throws InvalidArgsException {
        String[] args = {"-p", FILE_1};
        mkdirArgsParser.parse(args);
        assertTrue(mkdirArgsParser.isCreateMissingParent());
    }

    /**
     * Tests that when the `-p` option is provided with multiple files, `isCreateMissingParent`
     * returns true and `getFileNames` returns all provided file arguments.
     *
     * @throws InvalidArgsException if an exception occurs during parsing.
     */
    @Test
    void getFileNames_CreateParentOption_CorrectNonArgsFlags() throws InvalidArgsException {
        String[] args = {"-p", FILE_1};
        mkdirArgsParser.parse(args);
        assertEquals(Arrays.asList(FILE_1), mkdirArgsParser.getFileNames());
    }

    /**
     * Tests that when the `-p` option is provided with multiple files, `isCreateMissingParent`
     * returns true and `getFileNames` returns all provided file arguments.
     *
     * @throws InvalidArgsException if an exception occurs during parsing.
     */
    @Test
    void isCreateMissingParent_CreateParentOptionsWithMultipleFiles_CorrectNonArgs() throws InvalidArgsException {
        String[] args = {"-p", FILE_1, "file2.txt"};
        mkdirArgsParser.parse(args);
        assertTrue(mkdirArgsParser.isCreateMissingParent());
    }

    /**
     * Tests that when the `-p` option is provided with multiple files, `getFileNames` returns all
     * provided file arguments.
     *
     * @throws InvalidArgsException if an exception occurs during parsing.
     */
    @Test
    void getFileNames_CreateParentOptionsWithMultipleFiles_CorrectNonArgs() throws InvalidArgsException {
        String[] args = {"-p", FILE_1, "file2.txt"};
        mkdirArgsParser.parse(args);
        assertEquals(Arrays.asList(FILE_1, "file2.txt"), mkdirArgsParser.getFileNames());
    }

    /**
     * Tests that when a single invalid option is provided:
     *  - An {@link InvalidArgsException} is thrown with the correct error message.
     *  - `isCreateMissingParent` returns false.
     *  - `getFileNames` returns only the valid file argument.
     *
     * @param flag The invalid flag to be used in the test.
     */
    @ParameterizedTest
    @ValueSource(strings = {"-a","-b","-c","-d","-e","-f","-g"})
    void isCreateMissingParent_SingleInvalidOption_ReturnFalse(String flag) {
        String expected =  ILLEGAL_FLAG_MSG + flag.replace("-", "");
        String[] args = {flag, "file1.txt"};
        Throwable exp = assertThrows(InvalidArgsException.class, () -> mkdirArgsParser.parse(args));
        assertEquals(expected, exp.getMessage());
        assertFalse(mkdirArgsParser.isCreateMissingParent());
    }

    /**
     * Tests the behavior of `getFileNames` when a single invalid option is provided
     * and `-p` is also present, but no other valid arguments are given.
     *
     * @param flag The single invalid option to be tested.
     */
    @ParameterizedTest
    @ValueSource(strings = {"-a","-b","-c","-d","-e","-f","-g"})
    void getFileNames_SingleInvalidOption_EmptyNonArgsFlags(String flag) {
        String expected =  ILLEGAL_FLAG_MSG + flag.replace("-", "");
        String[] args = {flag, "file1.txt"};
        Throwable exp = assertThrows(InvalidArgsException.class, () -> mkdirArgsParser.parse(args));
        assertEquals(expected, exp.getMessage());
        assertEquals(List.of("file1.txt"), mkdirArgsParser.getFileNames());
    }

    /**
     * Tests if `isCreateMissingParent` returns true when only the `-p` flag is provided
     * and no file names are given.
     *
     * @throws InvalidArgsException If an error occurs during parsing.
     */
    @Test
    void isCreateMissingParent_NoFiles_ReturnTrue() throws InvalidArgsException {
        String[] args = {"-p"};
        mkdirArgsParser.parse(args);
        assertTrue(mkdirArgsParser.isCreateMissingParent());
    }

    /**
     * Tests if `getFileNames` returns an empty list when only the `-p` flag is provided
     * and no file names are given.
     *
     * @throws InvalidArgsException If an error occurs during parsing.
     */
    @Test
    void getFileNames_NoFiles_EmptyNonArgs() throws InvalidArgsException {
        String[] args = {"-p"};
        mkdirArgsParser.parse(args);
        assertTrue(mkdirArgsParser.getFileNames().isEmpty());
    }
}
