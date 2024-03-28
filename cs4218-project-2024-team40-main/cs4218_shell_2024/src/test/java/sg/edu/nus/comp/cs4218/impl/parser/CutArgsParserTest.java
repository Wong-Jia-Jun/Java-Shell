package sg.edu.nus.comp.cs4218.impl.parser;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class contains unit tests for the {@link CutArgsParser} class, specifically verifying its
 * ability to parse command-line arguments correctly.
 */
public class CutArgsParserTest {
    public static final String ARG_1_3 = "1-3";
    public static final String FILE_2 = "file2";
    public static final String FILES = "files";
    public static final String FILE_1 = "file1";
    CutArgsParser cutArgsParser;

    /**
     * Tests that when both the character and byte flags are provided, an
     * {@link InvalidArgsException} is thrown.
     */
    @Test
    void parse_BothFlags_ThrowsInvalidArgsException() {
        cutArgsParser = new CutArgsParser();
        String[] args = {"-c", ARG_1_3, "-b", ARG_1_3};
        Throwable thrown = assertThrows(InvalidArgsException.class, () -> cutArgsParser.parse(args));
        assertEquals(new InvalidArgsException(CutArgsParser.ILLEGAL_BOTH_FLAG).getMessage()
                , thrown.getMessage());
    }

    /**
     * Tests that when no flags are provided, an {@link InvalidArgsException} is thrown.
     */
    @Test
    void parse_NoFlags_ThrowsInvalidArgsException() {
        cutArgsParser = new CutArgsParser();
        String[] args = {ARG_1_3};
        Throwable thrown = assertThrows(InvalidArgsException.class, () -> cutArgsParser.parse(args));
        assertEquals(new InvalidArgsException(CutArgsParser.ILLEGAL_MSS_FLAG).getMessage()
                , thrown.getMessage());
    }

    /**
     * Tests that when a range in the arguments doesn't match the expected format, an
     * {@link InvalidArgsException} is thrown.
     *
     * @param range The invalid range string to be used in the test.
     */
    @ParameterizedTest
    @ValueSource(strings = {"1-3-4", "a1bc", "1@3"})
    void parse_RangeNotMatchingRegex_ThrowsInvalidArgsException(String range) {
        String[] flags = {"-b", "-c"};
        for (String flag : flags) {
            cutArgsParser = new CutArgsParser();
            String[] args = {flag, range};
            Throwable thrown = assertThrows(InvalidArgsException.class, () -> cutArgsParser.parse(args));
            assertEquals(new InvalidArgsException(CutArgsParser.ILLEGAL_RANGE_MSG).getMessage()
                    + " " + range, thrown.getMessage());
        }
    }

    /**
     * Tests that when invalid flags in the arguments an
     * {@link InvalidArgsException} is thrown.
     *
     */
    @Test
    void parse_InvalidFlags_ThrowsInvalidArgsException() {
        cutArgsParser = new CutArgsParser();
        String[] args = {"-X", ARG_1_3};
        Throwable thrown = assertThrows(InvalidArgsException.class, () -> cutArgsParser.parse(args));
        assertEquals(new InvalidArgsException(CutArgsParser.ILLEGAL_FLAG_MSG + "X").getMessage(),
                thrown.getMessage());
    }

    /**
     * Tests that when the start index in the range is less than 1, an
     * {@link InvalidArgsException} is thrown.
     *
     * @param flag The flag to be used in the test (`-c` or `-b`).
     */
    @ParameterizedTest
    @ValueSource(strings = {"-c", "-b"})
    void parse_RangeLessThan1_ThrowsInvalidArgsException(String flag) {
        cutArgsParser = new CutArgsParser();
        String[] args = {flag, "0-3"};
        Throwable thrown = assertThrows(InvalidArgsException.class, () -> cutArgsParser.parse(args));
        assertEquals(new InvalidArgsException(CutArgsParser.ILLEGAL_ONE_INDEX).getMessage()
                + " " + "0-3", thrown.getMessage());
    }

    /**
     * Tests that when the end index in the range is less than the start index, an
     * {@link InvalidArgsException} is thrown.
     *
     * @param flag The flag to be used in the test (`-c` or `-b`).
     */
    @ParameterizedTest
    @ValueSource(strings = {"-c", "-b"})
    void parse_RangeEndGreaterThanStart_ThrowsInvalidArgsException(String flag) {
        cutArgsParser = new CutArgsParser();
        String[] args = {flag, "3-1"};
        Throwable thrown = assertThrows(InvalidArgsException.class, () -> cutArgsParser.parse(args));
        assertEquals(new InvalidArgsException(CutArgsParser.ILLEGAL_DCS_RANGE).getMessage()
                + " " + "3-1", thrown.getMessage());
    }

    static Stream<Arguments> getCutFilesArgs() {
        return Stream.of(
                Arguments.of((Object) new String[]{"-c", ARG_1_3, "file", FILE_2}),
                Arguments.of((Object) new String[]{"-c", ARG_1_3, "-"}),
                Arguments.of((Object) new String[]{"-c", ARG_1_3, "file", "-"})
        );
    }

    /**
     * Tests that when the character flag is used with files, `isCutByFiles` returns true.
     *
     * @param args The arguments to be parsed, including the character flag, range, and files.
     * @throws InvalidArgsException if an exception occurs during parsing.
     */
    @ParameterizedTest
    @MethodSource("getCutFilesArgs")
    void isCutByFiles_CharFlagAndFiles_ReturnsTrue(String... args) throws InvalidArgsException {
        cutArgsParser = new CutArgsParser();
        cutArgsParser.parse(args);
        assertTrue(cutArgsParser.isCutFiles());
    }

    /**
     * A nested class containing tests specific to the character flag (`-c`).
     */
    @Nested
    class CharFlagTests {
        /**
         * Tests that when the character flag is used with a valid range and either no arguments,
         * standard input, or files, `isCutByChar` returns true.
         *
         * @param controlStdinout An optional argument representing either a file or standard input (`-`).
         *
         */
        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {FILES, "-"})
        void isCutByChar_CharFlagAndValidRangeForFileAndStdinCombis_ReturnsTrue(String controlStdinout)
                throws InvalidArgsException {
            cutArgsParser = new CutArgsParser();
            if (controlStdinout != null) {
                cutArgsParser.parse("-c", ARG_1_3, controlStdinout);
                assertTrue(cutArgsParser.isCutByChar());
            } else {
                cutArgsParser.parse("-c", ARG_1_3);
                assertTrue(cutArgsParser.isCutByChar());
            }
        }

        /**
         * Tests that when the character flag is used with a valid range and either no arguments,
         * standard input, or files, `isCutByByte` returns false.
         *
         * @param controlStdinout An optional argument representing either a file or standard input (`-`).
         */
        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {FILES, "-"})
        void isCutByByte_CharFlagAndValidRangeForFileAndStdinCombis_ReturnsFalse(String controlStdinout)
                throws InvalidArgsException {
            cutArgsParser = new CutArgsParser();
            if (controlStdinout != null) {
                cutArgsParser.parse("-c", ARG_1_3, controlStdinout);
                assertFalse(cutArgsParser.isCutByByte());
            } else {
                cutArgsParser.parse("-c", ARG_1_3);
                assertFalse(cutArgsParser.isCutByByte());
            }
        }

        /**
         * Tests that when the character flag is used with a valid range and no files,
         * `isCutByStdin` returns true.
         */
        @Test
        void isCutByStdin_CharFlagAndNoFiles_ReturnsTrue() throws InvalidArgsException {
            cutArgsParser = new CutArgsParser();
            cutArgsParser.parse("-c", ARG_1_3);
            assertTrue(cutArgsParser.isCutStdin());
        }

        /**
         * Tests that when the character flag is used with a valid range and a file,
         * `isCutByStdin` returns false.
         *
         * @param arg The file argument to be used in the test.
         * @throws InvalidArgsException if an exception occurs during parsing.
         */
        @ParameterizedTest
        @ValueSource(strings = {FILE_1, "-"})
        void isCutByStdin_CharFlagAndHasFiles_ReturnsFalse(String arg) throws InvalidArgsException {
            cutArgsParser = new CutArgsParser();
            cutArgsParser.parse("-c", ARG_1_3, arg);
            assertFalse(cutArgsParser.isCutStdin());
        }

        /**
         * Tests that when the character flag is used with a valid range and no files,
         * `isCutFiles` returns false.
         *
         * @throws InvalidArgsException if an exception occurs during parsing.
         */
        @Test
        void isCutByFiles_CharFlagAndNoFiles_ReturnsFalse() throws InvalidArgsException {
            cutArgsParser = new CutArgsParser();
            cutArgsParser.parse("-c", ARG_1_3);
            assertFalse(cutArgsParser.isCutFiles());
        }

        /**
         * Tests that when the character flag is used with a valid range and files,
         * `getFiles` returns the provided file names.
         *
         * @throws InvalidArgsException if an exception occurs during parsing.
         */
        @Test
        void getFiles_CharFlagAndFiles_ReturnsFiles() throws InvalidArgsException {
            cutArgsParser = new CutArgsParser();
            cutArgsParser.parse("-c", ARG_1_3, FILE_1, FILE_2);
            String[] expected = {FILE_1, FILE_2};
            String[] actual = cutArgsParser.getFiles();
            assertArrayEquals(expected, actual);
        }

        /**
         * Tests that when the character flag is used with a valid range and no files,
         * `getFiles` returns an empty array.
         *
         * @throws InvalidArgsException if an exception occurs during parsing.
         */
        @Test
        void getFiles_CharFlagAndStdin_FilesArrayEmpty() throws InvalidArgsException {
            cutArgsParser = new CutArgsParser();
            cutArgsParser.parse("-c", ARG_1_3);
            String[] expected = {};
            String[] actual = cutArgsParser.getFiles();
            assertArrayEquals(expected, actual);
        }
    }

    /**
     * A nested class containing tests specific to the byte flag (`-b`).
     */
    @Nested
    class ByteFlagTests {
        /**
         * Tests that when the byte flag is used with a valid range and either no arguments,
         * standard input, or files, `isCutByByte` returns true.
         *
         * @param controlStdinout An optional argument representing either a file or standard input (`-`).
         */
        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {FILES, "-"})
        void isCutByByte_ByteFlagAndValidRangeForFileAndStdinCombis_ReturnsTrue(String controlStdinout)
                throws InvalidArgsException {
            cutArgsParser = new CutArgsParser();
            if (controlStdinout != null) {
                cutArgsParser.parse("-b", ARG_1_3, controlStdinout);
                assertTrue(cutArgsParser.isCutByByte());
            } else {
                cutArgsParser.parse("-b", ARG_1_3);
                assertTrue(cutArgsParser.isCutByByte());
            }
        }

        /**
         * Tests that when the byte flag is used with a valid range and either no arguments,
         * standard input, or files, `isCutByChar` returns false.
         *
         * @param controlStdinout An optional argument representing either a file or standard input (`-`).
         */
        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {FILES, "-"})
        void isCutByChar_ByteFlagAndValidRangeForFileAndStdinCombis_ReturnsFalse(String controlStdinout)
                throws InvalidArgsException {
            cutArgsParser = new CutArgsParser();
            if (controlStdinout != null) {
                cutArgsParser.parse("-b", ARG_1_3, controlStdinout);
                assertFalse(cutArgsParser.isCutByChar());
            } else {
                cutArgsParser.parse("-b", ARG_1_3);
                assertFalse(cutArgsParser.isCutByChar());
            }
        }

        /**
         * Tests that when the byte flag is used with a valid range and no files,
         * `isCutByStdin` returns true.
         *
         * @throws InvalidArgsException if an exception occurs during parsing.
         */
        @Test
        void isCutByStdin_ByteFlagAndNoFiles_ReturnsTrue() throws InvalidArgsException {
            cutArgsParser = new CutArgsParser();
            cutArgsParser.parse("-b", ARG_1_3);
            assertTrue(cutArgsParser.isCutStdin());
        }

        /**
         * Tests that when the byte flag is used with a valid range and a file,
         * `isCutByStdin` returns false.
         *
         * @param arg The file argument to be used in the test.
         * @throws InvalidArgsException if an exception occurs during parsing.
         */
        @ParameterizedTest
        @ValueSource(strings = {FILE_1, "-"})
        void isCutByStdin_ByteFlagAndHasFiles_ReturnsFalse(String arg) throws InvalidArgsException {
            cutArgsParser = new CutArgsParser();
            cutArgsParser.parse("-b", ARG_1_3, arg);
            assertFalse(cutArgsParser.isCutStdin());
        }

        /**
         * Tests that when the byte flag is used with a valid range and no files,
         * `isCutFiles` returns false.
         *
         * @throws InvalidArgsException if an exception occurs during parsing.
         */
        @Test
        void isCutByFiles_ByteFlagAndNoFiles_ReturnsFalse() throws InvalidArgsException {
            cutArgsParser = new CutArgsParser();
            cutArgsParser.parse("-b", ARG_1_3);
            assertFalse(cutArgsParser.isCutFiles());
        }

        /**
         * Tests that when the byte flag is used with a valid range and files,
         * `getFiles` returns the provided file names.
         *
         * @throws InvalidArgsException if an exception occurs during parsing.
         */
        @Test
        void getFiles_ByteFlagAndFiles_ReturnsFiles() throws InvalidArgsException {
            cutArgsParser = new CutArgsParser();
            cutArgsParser.parse("-b", ARG_1_3, FILE_1, FILE_2);
            String[] expected = {FILE_1, FILE_2};
            String[] actual = cutArgsParser.getFiles();
            assertArrayEquals(expected, actual);
        }

        /**
         * Tests that when the byte flag is used with a valid range and no files,
         * `getFiles` returns an empty array.
         *
         * @throws InvalidArgsException if an exception occurs during parsing.
         */
        @Test
        void getFiles_ByteFlagAndStdin_FilesArrayEmpty() throws InvalidArgsException {
            cutArgsParser = new CutArgsParser();
            cutArgsParser.parse("-b", ARG_1_3);
            String[] expected = {};
            String[] actual = cutArgsParser.getFiles();
            assertArrayEquals(expected, actual);
        }
    }
}
