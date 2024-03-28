package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CutException;
import sg.edu.nus.comp.cs4218.impl.parser.CutArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.RangeHelperFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;


/**
 * Integration test of Functionality of cur application
 */

public class CutApplicationIT { //NOPMD - suppressed ClassNamingConventions - As per Teaching Team's guidelines
    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;
    private final static String BASE_DIR = Environment.currentDirectory;
    private final String testFileDir = Paths.get("src", "test", "resources", "app", "CutApplicationTest")
            .toAbsolutePath().toString();
    private final String inputContent = "Hello" + System.lineSeparator() + System.lineSeparator() + "From"
            + System.lineSeparator() + "CutTest" + System.lineSeparator() + "£И€\uD800\uDF48" + System.lineSeparator();
    private final String expectedChars = "Hll" + System.lineSeparator()
            + System.lineSeparator()
            + "Fom" + System.lineSeparator()
            + "CtT" + System.lineSeparator()
            + "£€𐍈" + System.lineSeparator();

    private final String expectedBytes = "Hll" + System.lineSeparator()
            + System.lineSeparator()
            + "Fom" + System.lineSeparator()
            + "CtT" + System.lineSeparator()
            + getByteVersionOfUtf8TestStr()
            + System.lineSeparator();
    private static final String ARG_1_AND_3_TO_4 = "1,3-4";
    private List<int[]> ranges;
    private PipedInputStream pipedIn;
    private PipedOutputStream pipedOut;
    private ByteArrayOutputStream testOutContent;
    private CutApplication cutApplication;
    private static final String TEST_FILE_CRLF = "dir/cutTestCrlf.txt";
    private static final String TEST_FILE_LF = "dir/cutTestLf.txt";

    private static class CutApplicationStubForFiles extends CutApplication {
        /**
         * Instantiates a new Cut application stub for files.
         */
        public CutApplicationStubForFiles() {
            super(new CutArgsParser(), new RangeHelperFactory());
        }
        public void setStdOut(OutputStream stdOut) {
            this.stdOut = stdOut;
        }
        public void setStdIn(InputStream stdIn) {
            this.stdIn = stdIn;
        }
    }

    private String getByteVersionOfUtf8TestStr() {
        byte[] bytes = "£И€\uD800\uDF48".getBytes(StandardCharsets.UTF_8);
        StringBuilder result = new StringBuilder();
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        for (int i = 0; i < 4; i++) {
            if (i == 1) {
                continue;
            }
            byteStream.write(bytes[i]);
        }
        result.append(byteStream.toString(StandardCharsets.UTF_8));
        return result.toString();
    }

    private void writeToStdin(String input) throws IOException, InterruptedException {
        pipedOut.write(input.getBytes());
        pipedOut.flush();
        Thread.sleep(100);
        pipedOut.close();
    }

    /**
     * Sets up.
     *
     * @throws IOException the io exception
     */
    @BeforeEach
    void setUp() throws IOException {
        pipedOut = new PipedOutputStream();
        pipedIn = new PipedInputStream(pipedOut);
        System.setIn(pipedIn);
        testOutContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(testOutContent));
        Environment.currentDirectory = testFileDir;
        ranges = new ArrayList<>();
        ranges.add(new int[]{1, 1});
        ranges.add(new int[]{3, 4});
        cutApplication = new CutApplication(new CutArgsParser(), new RangeHelperFactory());
    }

    /**
     * Restore streams.
     *
     * @throws IOException the io exception
     */
    @AfterEach
    void restoreStreams() throws IOException {
        System.setOut(originalOut); // Restore System.out
        System.setIn(originalIn); // Restore System.in
        pipedIn.close();
    }
    @AfterAll
    static void restoreEnv() {
        Environment.currentDirectory = BASE_DIR;
    }
    /**
     * Run normal cut of file by char print file content.
     *
     * @param fileDir the file dir
     * @throws AbstractApplicationException the abstract application exception
     */
    @ParameterizedTest
    @ValueSource(strings = {TEST_FILE_CRLF, TEST_FILE_LF})
    void run_NormalCutOfFileByChar_PrintFileContent(String fileDir) throws AbstractApplicationException {
        String[] args = {"-c", ARG_1_AND_3_TO_4, fileDir};
        cutApplication.run(args, System.in, System.out);
        String output = testOutContent.toString();
        assertEquals(expectedChars, output);
    }

    /**
     * Run normal cut of file by byte print file content.
     *
     * @param fileDir the file dir
     * @throws AbstractApplicationException the abstract application exception
     */
    @ParameterizedTest
    @ValueSource(strings = {TEST_FILE_CRLF, TEST_FILE_LF})
    void run_NormalCutOfFileByByte_PrintFileContent(String fileDir) throws AbstractApplicationException {
        String[] args = {"-b", ARG_1_AND_3_TO_4, fileDir};
        cutApplication.run(args, System.in, System.out);
        String output = testOutContent.toString();
        assertEquals(expectedBytes, output);
    }

    /**
     * Run cut multiple files by char print file content.
     *
     * @param size the size
     * @throws AbstractApplicationException the abstract application exception
     */
    @ParameterizedTest
    @ValueSource(ints = {2 ,3 ,6})
    void run_CutMultipleFilesByChar_PrintFileContent(int size) throws AbstractApplicationException {
        String[] args = new String[size + 2];
        Arrays.fill(args, TEST_FILE_LF);
        args[0] = "-c";
        args[1] = ARG_1_AND_3_TO_4;
        cutApplication.run(args, System.in, System.out);
        String output = testOutContent.toString();
        String expected = expectedChars.repeat(size);
        assertEquals(expected, output);
    }

    /**
     * Run cut multiple files by byte print file content.
     *
     * @param size the size
     * @throws AbstractApplicationException the abstract application exception
     */
    @ParameterizedTest
    @ValueSource(ints = {2 ,3 ,6})
    void run_CutMultipleFilesByByte_PrintFileContent(int size) throws AbstractApplicationException {
        String[] args = new String[size + 2];
        Arrays.fill(args, TEST_FILE_LF);
        args[0] = "-b";
        args[1] = ARG_1_AND_3_TO_4;
        cutApplication.run(args, System.in, System.out);
        String output = testOutContent.toString();
        String expected = expectedBytes.repeat(size);
        assertEquals(expected, output);
    }

    /**
     * Run char and no files for stdin print stdin content.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     * @throws InterruptedException         the interrupted exception
     */
    @Test
    void run_CharAndNoFilesForStdin_PrintStdinContent()
            throws AbstractApplicationException, IOException, InterruptedException {
        writeToStdin(inputContent);
        String[] args = {"-c", ARG_1_AND_3_TO_4};
        cutApplication.run(args, System.in, System.out);
        String output = testOutContent.toString();
        assertEquals(expectedChars, output);
    }

    /**
     * Run byte and no files for stdin print stdin content.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     * @throws InterruptedException         the interrupted exception
     */
    @Test
    void run_ByteAndNoFilesForStdin_PrintStdinContent()
            throws AbstractApplicationException, IOException, InterruptedException {
        writeToStdin(inputContent);
        String[] args = {"-b", ARG_1_AND_3_TO_4};
        cutApplication.run(args, System.in, System.out);
        String output = testOutContent.toString();
        assertEquals(expectedBytes, output);
    }

    /**
     * Run null input stream throw exception.
     */
    @Test
    void run_NullInputStream_ThrowException() {
        String[] args = {"-c", ARG_1_AND_3_TO_4};
        Throwable exp = assertThrows(CutException.class,
                () -> {cutApplication.run(args, null, System.out);});
        assertEquals(new CutException(ERR_NULL_STREAMS).getMessage(), exp.getMessage());
    }

    /**
     * Run null output stream throw exception.
     */
    @Test
    void run_NullOutputStream_ThrowException() {
        String[] args = {"-c", ARG_1_AND_3_TO_4};
        Throwable exp = assertThrows(CutException.class,
                () -> {cutApplication.run(args, System.in, null);});
        assertEquals(new CutException(ERR_NULL_STREAMS).getMessage(), exp.getMessage());
    }

    /**
     * Run null args throw exception.
     */
    @Test
    void run_NullArgs_ThrowException() {
        String[] args = null;
        Throwable exp = assertThrows(CutException.class,
                () -> {cutApplication.run(args, System.in, System.out);});
        assertEquals(new CutException(ERR_NULL_ARGS).getMessage(), exp.getMessage());
    }

    /**
     * Run both flags throw exception.
     */
    @Test
    void run_BothFlags_ThrowException() {
        String[] args = {"-c", "-b", ARG_1_AND_3_TO_4, TEST_FILE_LF};
        Throwable exp = assertThrows(CutException.class,
                () -> {cutApplication.run(args, System.in, System.out);});
        assertEquals(new CutException(CutArgsParser.ILLEGAL_BOTH_FLAG).getMessage(), exp.getMessage());
    }

    /**
     * Cut from stdin null input stream throw exception.
     */
    @Test
    void cutFromStdin_NullInputStream_ThrowException() {
        Throwable exp = assertThrows(CutException.class,
                () -> {cutApplication.cutFromStdin(true, false, ranges, null);});
        assertEquals(new CutException(ERR_NULL_STREAMS).getMessage(), exp.getMessage());
    }

    /**
     * Cut from stdin null args throw exception.
     */
    @Test
    void cutFromStdin_NullArgs_ThrowException() {
        Throwable exp = assertThrows(CutException.class,
                () -> {cutApplication.cutFromStdin(true, false, null, System.in);});
        assertEquals(new CutException(ERR_NULL_ARGS).getMessage(), exp.getMessage());
    }

    /**
     * Cut from stdin both flags throw exception.
     */
    @Test
    void cutFromStdin_BothFlags_ThrowException() {
        Throwable exp = assertThrows(CutException.class,
                () -> {cutApplication.cutFromStdin(true, true, ranges, System.in);});
        assertEquals(new CutException(CutArgsParser.ILLEGAL_BOTH_FLAG).getMessage(), exp.getMessage());
    }

    /**
     * Cut from stdin char print stdin content.
     *
     * @throws IOException                  the io exception
     * @throws InterruptedException         the interrupted exception
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void cutFromStdin_Char_PrintStdinContent() throws IOException, InterruptedException, AbstractApplicationException {
        writeToStdin(inputContent);
        String output = cutApplication.cutFromStdin(true, false, ranges, System.in);
        assertEquals(expectedChars, output);
    }

    /**
     * Cut from stdin byte print stdin content.
     *
     * @throws IOException                  the io exception
     * @throws InterruptedException         the interrupted exception
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void cutFromStdin_Byte_PrintStdinContent() throws IOException, InterruptedException, AbstractApplicationException {
        writeToStdin(inputContent);
        String output = cutApplication.cutFromStdin(false, true, ranges, System.in);
        assertEquals(expectedBytes, output);
    }

    /**
     * Cut from files null input stream throw exception.
     */
    @Test
    void cutFromFiles_NullInputStream_ThrowException() {
        CutApplicationStubForFiles cutApplication = new CutApplicationStubForFiles();
        cutApplication.setStdIn(null);
        Throwable exp = assertThrows(CutException.class,
                () -> {cutApplication.cutFromFiles(true, false, ranges,
                        TEST_FILE_LF);});
        assertEquals(new CutException(ERR_NULL_STREAMS).getMessage(), exp.getMessage());
    }

    /**
     * Cut from files null output stream throw exception.
     */
    @Test
    void cutFromFiles_NullOutputStream_ThrowException() {
        CutApplicationStubForFiles cutApplication = new CutApplicationStubForFiles();
        cutApplication.setStdOut(null);
        Throwable exp = assertThrows(CutException.class,
                () -> {cutApplication.cutFromFiles(true, false, ranges,
                        TEST_FILE_LF);});
        assertEquals(new CutException(ERR_NULL_STREAMS).getMessage(), exp.getMessage());
    }

    /**
     * Cut from files null files throw exception.
     */
    @Test
    void cutFromFiles_NullFiles_ThrowException() {
        CutApplicationStubForFiles cutApplication = new CutApplicationStubForFiles();
        cutApplication.setStdIn(System.in);
        cutApplication.setStdOut(System.out);
        Throwable exp = assertThrows(CutException.class,
                () -> {cutApplication.cutFromFiles(true, false, ranges,
                        (String[]) null);});
        assertEquals(new CutException(ERR_NULL_ARGS).getMessage(), exp.getMessage());
    }

    /**
     * Cut from files empty files throw exception.
     */
    @Test
    void cutFromFiles_EmptyFiles_ThrowException() {
        CutApplicationStubForFiles cutApplication = new CutApplicationStubForFiles();
        cutApplication.setStdIn(System.in);
        cutApplication.setStdOut(System.out);
        Throwable exp = assertThrows(CutException.class,
                () -> {cutApplication.cutFromFiles(true, false, ranges,
                        new String[0]);});
        assertEquals(new CutException(ERR_NO_ARGS).getMessage(), exp.getMessage());
    }

    /**
     * Cut from files null ranges throw exception.
     */
    @Test
    void cutFromFiles_NullRanges_ThrowException() {
        CutApplicationStubForFiles cutApplication = new CutApplicationStubForFiles();
        cutApplication.setStdIn(System.in);
        cutApplication.setStdOut(System.out);
        Throwable exp = assertThrows(CutException.class,
                () -> {cutApplication.cutFromFiles(true, false, null,
                        TEST_FILE_LF);});
        assertEquals(new CutException(ERR_NULL_ARGS).getMessage(), exp.getMessage());
    }

    /**
     * Cut from files empty ranges throw exception.
     */
    @Test
    void cutFromFiles_EmptyRanges_ThrowException() {
        CutApplicationStubForFiles cutApplication = new CutApplicationStubForFiles();
        cutApplication.setStdIn(System.in);
        cutApplication.setStdOut(System.out);
        Throwable exp = assertThrows(CutException.class,
                () -> {cutApplication.cutFromFiles(true, false, new ArrayList<>(),
                        TEST_FILE_LF);});
        assertEquals(new CutException(ERR_NO_ARGS).getMessage(), exp.getMessage());
    }

    /**
     * Cut files invalid file string returns exception text. (Invalid for java `Path.resolve`)
     * @throws AbstractApplicationException
     */
    @Test
    void cutFromFiles_InvalidFileString_ReturnsExceptionText() throws AbstractApplicationException {
        String invalidFileName = "invalid:";
        cutApplication.setStdOut(System.out);
        cutApplication.setStdIn(System.in);
        String actual = cutApplication.cutFromFiles(false, false, ranges, invalidFileName);
        assertEquals(new CutException(ERR_FILE_NOT_FND, invalidFileName).getMessage() + STRING_NEWLINE, actual);
    }

    /**
     * Cut from files file then stdin by char print file content and return stdin content.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     * @throws InterruptedException         the interrupted exception
     */
    @Test
    void cutFromFiles_FileThenStdinByChar_PrintFileContentAndReturnStdinContent()
            throws AbstractApplicationException, IOException, InterruptedException {
        Thread cutAppThread = new Thread(() -> {
            CutApplicationStubForFiles cutApplication = new CutApplicationStubForFiles();
            cutApplication.setStdIn(System.in);
            cutApplication.setStdOut(System.out);
            try {
                String stdinAfterCut = cutApplication.cutFromFiles(true, false, ranges,
                        TEST_FILE_LF, "-");
                assertEquals("134" + System.lineSeparator(), stdinAfterCut);
            } catch (AbstractApplicationException e) {
                e.printStackTrace();
            }
        });
        cutAppThread.start();
        Thread.sleep(100);
        writeToStdin("1234");
        assertEquals(expectedChars,
                testOutContent.toString());
        cutAppThread.interrupt();
    }
}
