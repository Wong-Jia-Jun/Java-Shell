package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.exception.WcException;
import sg.edu.nus.comp.cs4218.impl.parser.WcArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;


/**
 * Integration Tests of WcApplication with IOUtils and WcArgsParser
 */
public class WcApplicationIT { //NOPMD - suppressed ClassNamingConventions - As per Teaching Team's guidelines
    private WcApplication wcApplication;
    private static boolean hasCarriageReturn = false;
    private static final String TAB = "\t";
    private static final String TEST_RESOURCE_DIR = "src/test/resources/app/WcApplicationTest/";
    private static final String SHORT_TEXT = TEST_RESOURCE_DIR + "short_sample_text.txt";
    private static final String MEDIUM_TEXT = TEST_RESOURCE_DIR + "medium_sample_text.txt";
    private static final String LONG_TEXT = TEST_RESOURCE_DIR + "long_sample_text.txt";
    private static final String STDIN_TEST_STRING = "Hello World";
    private static final String WC_TOTAL_POSTFIX = " total";
    private static final String WC_PREFIX = "wc: ";

    /* Strings of expected result to stdout */

    private static String getExpectedCountForEmptyStdin() {
        return TAB + 0 +
                TAB + 0 +
                TAB + 0 +
                " -";
    }

    private static String getExpectedCountForShortText() {
        return TAB + 1 +
                TAB + 5 +
                TAB + (hasCarriageReturn ? 25 : 24) +
                " " + SHORT_TEXT;
    }

    private static String getExpectedCountForLongText() {
        return TAB + 1 +
                TAB + 100 +
                TAB + (hasCarriageReturn ? 670 : 669) +
                " " + LONG_TEXT;
    }

    private static String getExpectedCountForStdin() {
        return TAB + 0 +
                TAB + 2 +
                TAB + 11 +
                " -";
    }

    private static String getResultForTwoFilesAndNoDashes() {
        List<String> expectedResult = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        expectedResult.add(getExpectedCountForShortText());
        expectedResult.add(getExpectedCountForLongText());
        stringBuilder.append(TAB + 2)
                .append(TAB + 105).append(TAB).append(hasCarriageReturn ? 695 : 693)
                .append(WC_TOTAL_POSTFIX);
        expectedResult.add(stringBuilder.toString());
        return String.join(STRING_NEWLINE, expectedResult);
    }

    private static String getResultForNoFilesAndTwoDashes() {
        List<String> expectedResult = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        expectedResult.add(getExpectedCountForStdin());
        expectedResult.add(getExpectedCountForEmptyStdin());
        stringBuilder.append(TAB + 0)
                .append(TAB + 2)
                .append(TAB + 11)
                .append(WC_TOTAL_POSTFIX);
        expectedResult.add(stringBuilder.toString());
        return String.join(STRING_NEWLINE, expectedResult);
    }

    private static String getResultForOneFileAndOneDash() {
        List<String> expectedResult = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        expectedResult.add(getExpectedCountForShortText());
        expectedResult.add(getExpectedCountForStdin());
        stringBuilder.append(TAB + 1)
                .append(TAB + 7).append(TAB).append(hasCarriageReturn ? 36 : 35)
                .append(WC_TOTAL_POSTFIX);
        expectedResult.add(stringBuilder.toString());
        return String.join(STRING_NEWLINE, expectedResult);
    }

    private static String getResultForTwoFilesAndTwoDashes() {
        List<String> expectedResult = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        expectedResult.add(getExpectedCountForShortText());
        expectedResult.add(getExpectedCountForStdin());
        expectedResult.add(getExpectedCountForLongText());
        expectedResult.add(getExpectedCountForEmptyStdin());
        stringBuilder.append(TAB + 2)
                .append(TAB + 107).append(TAB).append(hasCarriageReturn ? 706 : 704)
                .append(WC_TOTAL_POSTFIX);
        expectedResult.add(stringBuilder.toString());
        return String.join(STRING_NEWLINE, expectedResult);
    }


    /* Helper methods */

    private static Boolean hasCRLFLineEndings(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            int prevChar = -1;
            int currentChar;
            while ((currentChar = reader.read()) != -1) {
                if (prevChar == '\r' && currentChar == '\n') {
                    return true; // Windows-style line ending found
                }
                prevChar = currentChar;
            }
        }
        return false;
    }

    private static long[] formatResultForCarriageReturn (long... count) {
        if (hasCarriageReturn) {
            count[2] += count[0];
        }
        return count;
    }

    /**
     * Init.
     *
     * @throws IOException the io exception
     */
    @BeforeAll
    static void init() throws IOException {
        Path pathShortText = Path.of(SHORT_TEXT);
        Path pathMedText = Path.of(MEDIUM_TEXT);
        Path pathLongText = Path.of(LONG_TEXT);

        boolean shortTextCRLF = hasCRLFLineEndings((pathShortText).toFile());
        boolean medTextCRLF =  hasCRLFLineEndings((pathMedText).toFile());
        boolean longTextCRLF = hasCRLFLineEndings((pathLongText).toFile());

        if (shortTextCRLF && medTextCRLF && longTextCRLF) {
            hasCarriageReturn = true;
        }
    }

    /**
     * Setup wcApplication
     */
    @BeforeEach
    void setup() {
        wcApplication = new WcApplication(new WcArgsParser());
    }

    /**
     * Gets count report empty string should return correct word count.
     *
     * @throws WcException  the wc application exception
     * @throws IOException  the io exception
     */
    @Test
    void getCountReport_EmptyString_ShouldReturnCorrectWordCount() throws WcException, IOException {
        try (InputStream inputStream = new ByteArrayInputStream("".getBytes())) {
            long[] count = wcApplication.getCountReport(inputStream);
            long[] expectedCount = {0L, 0L, 0L};
            assertArrayEquals(expectedCount, count);
        }
    }

    /**
     * Gets count report one line five words should return correct word count.
     *
     * @throws WcException  the wc application exception
     * @throws IOException  the io exception
     */
    @Test
    void getCountReport_OneLineFiveWords_ShouldReturnCorrectWordCount() throws IOException, WcException {
        try (InputStream inputStream = new FileInputStream(SHORT_TEXT)) {
            long[] count = wcApplication.getCountReport(inputStream);
            long[] expectedCount = {1L, 5L, 24L};
            assertArrayEquals(formatResultForCarriageReturn(expectedCount), count);
        }
    }

    /**
     * Gets count report three lines five words should return correct word count.
     *
     * @throws WcException  the wc application exception
     * @throws IOException  the io exception
     */
    @Test
    void getCountReport_ThreeLinesFiveWords_ShouldReturnCorrectWordCount() throws WcException, IOException {
        try (InputStream inputStream = new FileInputStream(MEDIUM_TEXT)) {
            long[] count = wcApplication.getCountReport(inputStream);
            long[] expectedCount = {3L, 15L, 75L};
            assertArrayEquals(formatResultForCarriageReturn(expectedCount), count);
        }
    }

    /**
     * Gets count report one line hundred words should return correct word count.
     *
     * @throws WcException  the wc application exception
     * @throws IOException  the io exception
     */
    @Test
    void getCountReport_OneLineHundredWords_ShouldReturnCorrectWordCount() throws WcException, IOException {
        try (InputStream inputStream = new FileInputStream(LONG_TEXT)) {
            long[] count = wcApplication.getCountReport(inputStream);
            long[] expectedCount = {1L, 100L, 669L};
            assertArrayEquals(formatResultForCarriageReturn(expectedCount), count);
        }
    }

    /**
     * Gets count report special characters should return correct word count.
     *
     * @throws WcException  the wc application exception
     * @throws IOException  the io exception
     */
    @Test
    void getCountReport_SpecialCharacters_ShouldReturnCorrectWordCount() throws WcException, IOException {
        InputStream inputStream = new ByteArrayInputStream("!@#$%^&*()_+".getBytes());
        long[] count = wcApplication.getCountReport(inputStream);
        long[] expectedCount = {0L, 1L, 12L};
        assertArrayEquals(expectedCount, count);
        inputStream.close();
    }

    /**
     * Count from stdin null stdin throw wc exception.
     */
    @Test
    void countFromStdin_NullStdin_ThrowWcException() {
        Throwable thrown = assertThrows(WcException.class,
                () -> wcApplication.countFromStdin(false, false, false, null));
        assertEquals(WC_PREFIX + ERR_NULL_STREAMS, thrown.getMessage());
    }

    /**
     * Count from stdin one line five words should return correct string.
     *
     * @throws WcException      the WcApplication exception
     * @throws ShellException   the shell exception
     * @throws IOException      the io exception
     */
    @Test
    void countFromStdin_OneLineFiveWords_ShouldReturnCorrectString() throws WcException, ShellException, IOException {
        String targetFile = SHORT_TEXT;
        try (InputStream inputStream = IOUtils.openInputStream(targetFile)) {
            String result = wcApplication.countFromStdin(false, false, false, inputStream);
            String expectedResult = getExpectedCountForShortText();
            expectedResult = expectedResult.substring(0, expectedResult.lastIndexOf(" " + targetFile));
            assertEquals(expectedResult, result);
        }
    }

    /**
     * Count from stdin all flags one line five words should return correct string.
     *
     * @throws WcException      the WcApplication exception
     * @throws ShellException   the shell exception
     * @throws IOException      the io exception
     */
    @Test
    void countFromStdin_AllFlagsOneLineFiveWords_ShouldReturnCorrectString() throws WcException, ShellException, IOException {
        String targetFile = SHORT_TEXT;
        try (InputStream inputStream = IOUtils.openInputStream(targetFile)) {
            String result = wcApplication.countFromStdin(true, true, true, inputStream);
            String expectedResult = getExpectedCountForShortText();
            expectedResult = expectedResult.substring(0, expectedResult.lastIndexOf(" " + targetFile));
            assertEquals(expectedResult, result);
        }
    }

    /**
     * Count from files wrong filename throw wc exception.
     *
     * @throws WcException  the WcApplication exception
     */
    @Test
    void countFromFiles_WrongFilename_ThrowWcException() throws WcException {
        String result = wcApplication.countFromFiles(false, false, false, "Hello.txt");
        assertEquals(WC_PREFIX + ERR_FILE_NOT_FND, result);
    }

    /**
     * Count from files directory throw wc exception.
     *
     * @throws WcException  the WcApplication exception
     */
    @Test
    void countFromFiles_Directory_ThrowWcException() throws WcException {
        String result = wcApplication.countFromFiles(false, false, false, "src");
        assertEquals(WC_PREFIX + ERR_IS_DIR, result);
    }

    /**
     * Count from files null filename throw wc exception.
     */
    @Test
    void countFromFiles_NullFilename_ThrowWcException() {
        Throwable thrown = assertThrows(WcException.class,
                () -> wcApplication.countFromFiles(false, false, false, (String[]) null));
        assertEquals(WC_PREFIX + ERR_GENERAL, thrown.getMessage());
    }

    /**
     * Count from files and stdin null stdin throw wc exception.
     */
    @Test
    void countFromFilesAndStdin_NullStdin_ThrowWcException() {
        String[] targetFiles = {SHORT_TEXT};
        Throwable thrown = assertThrows(WcException.class,
                () -> wcApplication.countFromFileAndStdin(false, false, false, null, targetFiles));
        assertEquals(WC_PREFIX + ERR_NULL_STREAMS, thrown.getMessage());
    }

    /**
     * Count from files and stdin null filename throw wc exception.
     */
    @Test
    void countFromFilesAndStdin_NullFilename_ThrowWcException() {
        InputStream inputStream = new ByteArrayInputStream(STDIN_TEST_STRING.getBytes());
        Throwable thrown = assertThrows(WcException.class,
                () -> wcApplication.countFromFileAndStdin(false, false, false, inputStream, (String[]) null));
        assertEquals(WC_PREFIX + ERR_GENERAL, thrown.getMessage());
    }

    /**
     * Count from files and stdin one file and one dash should return correct count.
     *
     * @throws WcException  the WcApplication exception
     */
    @Test
    void countFromFilesAndStdin_OneFileAndOneDash_ShouldReturnCorrectCount() throws WcException {
        String targetStdin = "-";
        String[] targetFiles = {SHORT_TEXT, targetStdin};
        InputStream inputStream = new ByteArrayInputStream(STDIN_TEST_STRING.getBytes());
        String result = wcApplication.countFromFileAndStdin(false, false, false, inputStream, targetFiles);
        String expectedResult = getResultForOneFileAndOneDash();
        assertEquals(expectedResult, result);
    }

    /**
     * Count from files and stdin two files and two dashes should return correct count.
     *
     * @throws WcException  the WcApplication exception
     */
    @Test
    void countFromFilesAndStdin_TwoFilesAndTwoDashes_ShouldReturnCorrectCount() throws WcException {
        String[] targetFiles = {SHORT_TEXT, "-", LONG_TEXT, "-"};
        InputStream inputStream = new ByteArrayInputStream(STDIN_TEST_STRING.getBytes());
        String result = wcApplication.countFromFileAndStdin(false, false, false, inputStream, targetFiles);
        String expectedResult = getResultForTwoFilesAndTwoDashes();
        assertEquals(expectedResult, result);
    }

    /**
     * Run null output stream should throw wc exception.
     */
    @Test
    void run_NullOutputStream_ShouldThrowWcException() {
        String[] args = {""};
        Throwable thrown = assertThrows(WcException.class,
                () -> wcApplication.run(args, System.in, null));
        assertEquals(WC_PREFIX + ERR_NULL_STREAMS, thrown.getMessage());
    }

    /**
     * Run null input stream should throw wc exception.
     */
    @Test
    void run_NullInputStream_ShouldThrowWcException() {
        String[] args = {""};
        Throwable thrown = assertThrows(WcException.class,
                () -> wcApplication.run(args, null, System.out));
        assertEquals(WC_PREFIX + ERR_NULL_STREAMS, thrown.getMessage());
    }

    /**
     * Run null stream should throw wc exception.
     */
    @Test
    void run_NullStream_ShouldThrowWcException() {
        Throwable thrown = assertThrows(WcException.class,
                () -> wcApplication.run(null, System.in, System.out));
        assertEquals(WC_PREFIX + ERR_NULL_ARGS, thrown.getMessage());
    }


    /**
     * Run invalid file should throw wc exception.
     *
     * @throws InvalidArgsException the invalid args exception
     */
    @ParameterizedTest
    @ValueSource(strings = {"resources", "resources:"})
    void run_InvalidFile_ShouldThrowWcException(String str) throws WcException {
        String[] targetFiles = {str};
        OutputStream outputStream = new ByteArrayOutputStream();
        wcApplication.run(targetFiles, System.in, outputStream);
        assertEquals(WC_PREFIX + ERR_FILE_NOT_FND + STRING_NEWLINE, outputStream.toString());
    }

    /**
     * Run one file should return correct string.
     *
     * @throws WcException  the WcApplication exception
     */
    @Test
    void run_OneFile_ShouldReturnCorrectString() throws WcException {
        String[] targetFiles = {SHORT_TEXT};
        OutputStream outputStream = new ByteArrayOutputStream();
        wcApplication.run(targetFiles, System.in, outputStream);
        String expectedResult = getExpectedCountForShortText() + STRING_NEWLINE;
        assertEquals(expectedResult, outputStream.toString());
    }

    /**
     * Run two files should return correct string.
     *
     * @throws WcException  the WcApplication exception
     */
    @Test
    void run_TwoFiles_ShouldReturnCorrectString() throws WcException {
        String[] targetFiles = {SHORT_TEXT, LONG_TEXT};
        OutputStream outputStream = new ByteArrayOutputStream();
        wcApplication.run(targetFiles, System.in, outputStream);
        String expectedResult = getResultForTwoFilesAndNoDashes() + STRING_NEWLINE;
        assertEquals(expectedResult, outputStream.toString());
    }

    /**
     * Run one dash should return correct string.
     *
     * @throws WcException  the WcApplication exception
     */
    @Test
    void run_OneDash_ShouldReturnCorrectString() throws WcException {
        String[] targetFiles = {"-"};
        InputStream inputStream = new ByteArrayInputStream(STDIN_TEST_STRING.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        wcApplication.run(targetFiles, inputStream, outputStream);
        String expectedResult = getExpectedCountForStdin() + STRING_NEWLINE;
        assertEquals(expectedResult, outputStream.toString());
    }

    /**
     * Run two dashes should return correct string.
     *
     * @throws WcException  the WcApplication exception
     */
    @Test
    void run_TwoDashes_ShouldReturnCorrectString() throws WcException {
        String[] targetFiles = {"-", "-"};
        InputStream inputStream = new ByteArrayInputStream(STDIN_TEST_STRING.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        wcApplication.run(targetFiles, inputStream, outputStream);
        String expectedResult = getResultForNoFilesAndTwoDashes() + STRING_NEWLINE;
        assertEquals(expectedResult, outputStream.toString());
    }

    /**
     * Run two files and two dashes should return correct string.
     *
     * @throws WcException  the WcApplication exception
     */
    @Test
    void run_TwoFilesAndTwoDashes_ShouldReturnCorrectString() throws WcException {
        String[] targetFiles = {SHORT_TEXT, "-", LONG_TEXT, "-"};
        InputStream inputStream = new ByteArrayInputStream(STDIN_TEST_STRING.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        wcApplication.run(targetFiles, inputStream, outputStream);
        String expectedResult = getResultForTwoFilesAndTwoDashes() + STRING_NEWLINE;
        assertEquals(expectedResult, outputStream.toString());
    }
}
