package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.GrepException;
import sg.edu.nus.comp.cs4218.impl.parser.GrepArgsParser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;


/**
 * Tests Functionality of grep application
 */
public class GrepApplicationIT { //NOPMD

    /**
     * The constant LOREM.
     */
    public static final String LOREM = "Lorem";
    public static final String INPUT_1 = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";
    public static final String INPUT_2 = "Lorem ipsum dolor sit amet";
    public static final String FILE_LOREM = "fileWithLorem.txt";
    public static final String FILE_MUL_LOREM ="fileWithMultipleLorem.txt";
    private static final String INVLD_PAT_MSG = "Unclosed character class near index 8" + STRING_NEWLINE
            + "[[invalid" + STRING_NEWLINE + "        ^";

    private void createTestFile(String filename, String content) throws IOException {
        Path path = Paths.get(filename);
        Files.write(path, content.getBytes());
    }

    /**
     * The type Positive tests.
     */
    @Nested
    class PositiveTests {
        private GrepApplication grepApplication;
        private OutputStream stdout;
        private static final String RES_H = FILE_LOREM + ":Lorem ipsum dolor sit amet, consectetur adipiscing elit.";
        private static final String RES = INPUT_1;
        private final String RES_ALL_FLG = FILE_LOREM + ":1" + STRING_NEWLINE + FILE_MUL_LOREM + ":2";
        private final String MUL_RES_H = FILE_MUL_LOREM + ":Lorem ipsum dolor sit amet, consectetur adipiscing elit." + STRING_NEWLINE +
                FILE_MUL_LOREM + ":Lorem ipsum dolor sit amet, consectetur adipiscing elit.";

        /**
         * Sets up.
         *
         * @throws IOException the io exception
         */
        @BeforeEach
        void setUp() throws IOException {
            grepApplication = new GrepApplication(new GrepArgsParser());
            stdout = new ByteArrayOutputStream();
            createTestFile(FILE_LOREM, INPUT_1);
            createTestFile(FILE_MUL_LOREM,
                    INPUT_1 + STRING_NEWLINE +
                            "Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua." + STRING_NEWLINE +
                            INPUT_1);
        }

        /**
         * Run print file name option with pattern and files prints file name and match.
         */
        @Test
        void run_PrintFileNameOptionWithPatternAndFiles_PrintsFileNameAndMatch() {
            String[] args = {"-H", LOREM, FILE_LOREM};
            InputStream stdin = new ByteArrayInputStream("".getBytes());
            assertDoesNotThrow(() -> grepApplication.run(args, stdin, stdout));
            assertEquals(RES_H + STRING_NEWLINE, stdout.toString());
        }

        /**
         * Run case insensitive option with pattern and files prints matches ignoring case.
         */
        @Test
        void run_CaseInsensitiveOptionWithPatternAndFiles_PrintsMatchesIgnoringCase() {
            String[] args = {"-i", "LOREM", FILE_LOREM};
            InputStream stdin = new ByteArrayInputStream("".getBytes());
            assertDoesNotThrow(() -> grepApplication.run(args, stdin, stdout));
            assertEquals(RES + STRING_NEWLINE, stdout.toString());
        }

        /**
         * Run combined options with pattern and files.
         */
        @Test
        void run_CombinedOptionsWithPatternAndFiles_PrintCorrect() {
            String[] args = {"-i", "-c", "-H", LOREM, FILE_LOREM, FILE_MUL_LOREM};
            InputStream stdin = new ByteArrayInputStream("".getBytes());
            assertDoesNotThrow(() -> grepApplication.run(args, stdin, stdout));
            assertEquals(RES_ALL_FLG + STRING_NEWLINE, stdout.toString());
        }

        /**
         * Grep from files with valid pattern and existing file prints matching lines.
         *
         * @throws Exception the exception
         */
        @Test
        void grepFromFiles_WithValidPatternAndExistingFile_PrintsMatchingLines() throws Exception {
            String[] fileNames = {FILE_LOREM};
            String result = grepApplication.grepFromFiles(LOREM, false, false, false, fileNames);
            assertTrue(result.contains(LOREM));
        }

        /**
         * Grep from files with count option and multiple matches counts matches correctly.
         *
         * @throws Exception the exception
         */
        @Test
        void grepFromFiles_WithCountOptionAndMultipleMatches_CountsMatchesCorrectly() throws Exception {
            String[] fileNames = {FILE_MUL_LOREM};
            String result = grepApplication.grepFromFiles(LOREM, false, true, false, fileNames);
            assertEquals("2", result.trim());
        }

        /**
         * Grep from stdin with matching pattern prints matching lines.
         *
         * @throws Exception the exception
         */
        @Test
        void grepFromStdin_WithMatchingPattern_PrintsMatchingLines() throws Exception {
            String input = "Lorem ipsum dolor sit amet\nConsectetur adipiscing elit\n";
            InputStream stdin = new ByteArrayInputStream(input.getBytes());
            String result = grepApplication.grepFromStdin(LOREM, false, false, false, stdin);
            assertTrue(result.contains("Lorem ipsum"));
        }

        /**
         * Grep from stdin with count option and multiple matches counts matches correctly.
         *
         * @throws Exception the exception
         */
        @Test
        void grepFromStdin_WithCountOptionAndMultipleMatches_CountsMatchesCorrectly() throws Exception {
            String input = "Lorem ipsum dolor sit amet\nLorem ipsum\n";
            InputStream stdin = new ByteArrayInputStream(input.getBytes());
            String result = grepApplication.grepFromStdin(LOREM, false, true, false, stdin);
            assertEquals("2", result.trim());
        }

        /**
         * Run with stdin and pattern matches from stdin.
         *
         * @throws Exception the exception
         */
        @Test
        void run_WithStdinAndPattern_MatchesFromStdin() throws Exception {
            String input = "Matching from stdin" + STRING_NEWLINE + "Another line" + STRING_NEWLINE;
            InputStream stdin = new ByteArrayInputStream(input.getBytes());
            String[] args = {"Matching"};
            assertDoesNotThrow(() -> grepApplication.run(args, stdin, stdout));
            assertTrue(stdout.toString().contains("Matching from stdin"));
        }

        /**
         * Run with files and pattern matches from files.
         *
         * @throws Exception the exception
         */
        @Test
        void run_WithFilesAndPattern_MatchesFromFiles() throws Exception {
            String[] args = {"-H", LOREM, FILE_MUL_LOREM};
            OutputStream stdout = new ByteArrayOutputStream();
            InputStream stdin = new ByteArrayInputStream("".getBytes());
            assertDoesNotThrow(() -> grepApplication.run(args, stdin, stdout));
            assertEquals(MUL_RES_H + STRING_NEWLINE, stdout.toString());
        }

        /**
         * Run with invalid arguments throws exception.
         */
        @Test
        void run_WithInvalidArguments_ThrowsException() {
            String[] args = {"-unknownOption"};
            assertThrows(GrepException.class, () -> grepApplication.run(args, null, System.out));
        }
    }

    /**
     * The type Negative tests.
     */
    @Nested
    class NegativeTests {
        private GrepApplication grepApplication;
        private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

        /**
         * Sets up.
         *
         * @throws IOException the io exception
         */
        @BeforeEach
        void setUp() throws IOException {
            grepApplication = new GrepApplication(new GrepArgsParser());
            System.setOut(new PrintStream(outContent));
            createTestFile(FILE_LOREM, INPUT_1);
            createTestFile(FILE_MUL_LOREM,
                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n" +
                            "Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.\n" +
                            INPUT_1);
        }


        /**
         * Run no pattern provided throws exception.
         */
        @Test
        void run_NoPatternProvided_ThrowsException() {
            String[] args = {"-c", FILE_LOREM};
            assertThrows(GrepException.class, () -> grepApplication.run(args, null, System.out));
        }

        /**
         * Run missing arguments throws exception.
         */
        @Test
        void run_MissingArguments_ThrowsException() {
            String[] args = {};
            assertThrows(GrepException.class, () -> grepApplication.run(args, null, System.out));
        }

        /**
         * Run invalid option throws exception.
         */
        @Test
        void run_InvalidOption_ThrowsException() {
            String[] args = {"-z", "pattern", FILE_LOREM};
            assertThrows(GrepException.class, () -> grepApplication.run(args, null, System.out));
        }

        /**
         * Grep from files directory given reports is directory error.
         *
         * @throws Exception the exception
         */
        @Test
        void grepFromFiles_DirectoryGiven_ReportsIsDirectoryError() throws Exception {
            String[] fileNames = {"directoryName"};
            String result = grepApplication.grepFromFiles(
                    LOREM, false, false, false, fileNames);
            assertTrue(result.contains("No such file or directory"));
        }

        /**
         * Grep from files with null file names throws exception.
         *
         * @throws Exception the exception
         */
        @Test
        void grepFromFiles_NullFileNames_ThrowsException() throws Exception {
            Throwable exception = assertThrows(GrepException.class, () -> grepApplication.grepFromFiles(
                    LOREM, false, false, false, (String[]) null));
            assertEquals(new GrepException(ERR_NULL_ARGS).getMessage(),
                    exception.getMessage());
        }

        /**
         * Grep from files with null pattern throws exception.
         *
         * @throws Exception the exception
         */
        @Test
        void grepFromFiles_NullPattern_ThrowsException() throws Exception {
            String[] fileNames = {FILE_LOREM};
            Throwable exception = assertThrows(GrepException.class, () -> grepApplication.grepFromFiles(
                    null, false, false, false, fileNames));
            assertEquals(new GrepException(GrepApplication.EMPTY_PATTERN).getMessage(),
                    exception.getMessage());
        }

        /**
         * Grep files invalid file string returns exception text. (Invalid for java `Path.resolve`)
         * @throws AbstractApplicationException
         */
        @Test
        void grepFromFiles_InvalidFileString_ReturnsExceptionText() throws AbstractApplicationException {
            String invalidFileName = "invalidFile:";
            String result = grepApplication.grepFromFiles(LOREM, false, false, false, invalidFileName);
            assertEquals(new GrepException(invalidFileName, ERR_FILE_NOT_FND).getMessage(), result);
        }

        /**
         * Grep from stdin invalid pattern throws exception.
         */
        @Test
        void grepFromStdin_InvalidPattern_ThrowsException() {
            String input = INPUT_2;
            InputStream stdin = new ByteArrayInputStream(input.getBytes());
            Throwable exp = assertThrows(GrepException.class, () -> grepApplication.grepFromStdin(
                    "[[invalid", false, false, false, stdin));
            assertEquals(new GrepException(INVLD_PAT_MSG).getMessage(), exp.getMessage());
        }

        /**
         * This test case checks if an exception is thrown when a null pattern is provided to the
         * grepFromFileAndStdin method.
         */
        @Test
        void grepFromFileAndStdin_NullPattern_ThrowsException() {
            String[] fileNames = {FILE_LOREM};
            String input = INPUT_2;
            InputStream stdin = new ByteArrayInputStream(input.getBytes());
            Throwable exception = assertThrows(GrepException.class, () -> grepApplication.grepFromFileAndStdin(
                    null, false, false, false, stdin, fileNames));
            assertEquals(new GrepException(GrepApplication.EMPTY_PATTERN).getMessage(),
                    exception.getMessage());
        }

        /**
         * The test checks that an exception is thrown when an empty pattern is provided to the
         * grepFromFileAndStdin method.
         */
        @Test
        void grepFromFileAndStdin_EmptyPattern_ThrowsException() {
            String[] fileNames = {FILE_LOREM};
            String input = INPUT_2;
            InputStream stdin = new ByteArrayInputStream(input.getBytes());
            Throwable exception = assertThrows(GrepException.class, () -> grepApplication.grepFromFileAndStdin(
                    "", false, false, false, stdin, fileNames));
            assertEquals(new GrepException(GrepApplication.EMPTY_PATTERN).getMessage(),
                    exception.getMessage());
        }

        /**
         * The function tests that passing null file names to the grepFromFileAndStdin method throws a
         * GrepException.
         */
        @Test
        void grepFromFileAndStdin_NullFileNames_ThrowsException() {
            String input = INPUT_2;
            InputStream stdin = new ByteArrayInputStream(input.getBytes());
            Throwable exception = assertThrows(GrepException.class, () -> grepApplication.grepFromFileAndStdin(
                    LOREM, false, false, false, stdin, (String[]) null));
            assertEquals(new GrepException(ERR_NULL_ARGS).getMessage(),
                    exception.getMessage());
        }

        /**
         * The function tests that an exception is thrown when attempting to grep from a file and stdin
         * with a null stdin input.
         */
        @Test
        void grepFromFileAndStdin_NullStdin_ThrowsException() {
            String[] fileNames = {FILE_LOREM};
            Throwable exception = assertThrows(GrepException.class, () -> grepApplication.grepFromFileAndStdin(
                    LOREM, false, false, false, null, fileNames));
            assertEquals(new GrepException(ERR_NULL_STREAMS).getMessage(),
                    exception.getMessage());
        }
    }

    @Nested
    class GrepFromFileAndStdinTests {
        /**
         * The function tests the `grepFromFileAndStdin` method in a Java application by providing a
         * valid pattern, file, and input, and asserts that the output contains the expected result.
         */
        @Test
        void grepFromFileAndStdin_WithValidPatternAndFile_PrintsMatchingLines() throws Exception {
            GrepApplication grepApplication = new GrepApplication(new GrepArgsParser());
            String[] fileNames = {FILE_LOREM};
            String input = INPUT_2;
            InputStream stdin = new ByteArrayInputStream(input.getBytes());
            String result = grepApplication.grepFromFileAndStdin(LOREM, false, false, false, stdin, fileNames);
            assertTrue(result.contains(LOREM));
        }
    }

    /**
     * Tear down.
     *
     * @throws IOException the io exception
     */
    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(Paths.get(FILE_LOREM));
        Files.deleteIfExists(Paths.get(FILE_MUL_LOREM));
        System.setOut(System.out);
    }
}
