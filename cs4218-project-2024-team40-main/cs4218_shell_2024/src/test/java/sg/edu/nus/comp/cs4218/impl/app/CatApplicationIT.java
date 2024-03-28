package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CatException;
import sg.edu.nus.comp.cs4218.impl.parser.CatArgsParser;

import java.io.*;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;


/**
 * Integration Tests of cat
 */
public class CatApplicationIT { //NOPMD - suppressed ClassNamingConventions - As per Teaching Team's guidelines
    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;
    private final String expectedContent = "Hello\n"
            + "\n"
            + "From\n"
            + "CatTest.txt\n"
            + "£И€𐍈\n";
    private final String numberedContent = "     1  Hello\n"
            + "     2  \n"
            + "     3  From\n"
            + "     4  CatTest.txt\n"
            + "     5  £И€𐍈\n";

    private PipedOutputStream pipedOut;
    private ByteArrayOutputStream testOutContent;
    private final static String BASE_PATH = Environment.currentDirectory;
    private final String testFileDir = Paths.get("src", "test", "resources", "app", "CatApplicationTest")
            .toAbsolutePath().toString();
    private CatApplication catApplication;
    private static final String TEST_FILE = "dir/catTestCrlf.txt";

    private static class CatApplicationStubForFilesAndStdIn extends CatApplication {
        /**
         * Instantiates a new Cat application stub for files and std in.
         */
        public CatApplicationStubForFilesAndStdIn() {
            super(new CatArgsParser());
        }
        public void setStdout(OutputStream stdout) {
            this.stdout = stdout;
        }
    }

    private String normalizeLineEndings(String input) {
        return input.replace("\r\n", "\n");
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
        PipedInputStream pipedIn = new PipedInputStream(pipedOut); //NOPMD - suppressed CloseResource - Resource is used for tests
        System.setIn(pipedIn);
        testOutContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(testOutContent));
        Environment.currentDirectory = testFileDir;
        catApplication = new CatApplication(new CatArgsParser());
    }

    /**
     * Restore streams.
     */
    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut); // Restore System.out
        System.setIn(originalIn); // Restore System.in
        Environment.currentDirectory = BASE_PATH;

    }
    @AfterAll
    static void restoreEnv(){
        Environment.currentDirectory = BASE_PATH;
    }

    /**
     * Run normal cat of file print file content.
     *
     * @param fileDir the file dir
     * @throws AbstractApplicationException the abstract application exception
     */
    @ParameterizedTest
    @ValueSource(strings = {TEST_FILE, "dir/catTestLf.txt"})
    void run_NormalCatOfFile_PrintFileContent(String fileDir) throws AbstractApplicationException {
        String[] args = {fileDir};
        catApplication.run(args, System.in, System.out);
        String output = normalizeLineEndings(testOutContent.toString());
        assertEquals(expectedContent, output);
    }

    /**
     * Run normal cat of file with line number print file content.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void run_NormalCatOfFileWithLineNumber_PrintFileContent() throws AbstractApplicationException {
        String[] args = {"-n", "dir/catTestLf.txt"};
        catApplication.run(args, System.in, System.out);
        String output = normalizeLineEndings(testOutContent.toString());
        assertEquals(numberedContent, output);
    }

    /**
     * Run cat multiple files print all file content.
     *
     * @param size the size
     * @throws AbstractApplicationException the abstract application exception
     */
    @ParameterizedTest
    @ValueSource(ints = {2, 3, 6})
    void run_CatMultipleFiles_PrintAllFileContent(int size) throws AbstractApplicationException {
        String[] args = new String[size];
        Arrays.fill(args, TEST_FILE);
        catApplication.run(args, System.in, System.out);
        String output = normalizeLineEndings(testOutContent.toString());
        assertEquals(expectedContent.repeat(size), output);
    }

    /**
     * Run cat multiple files with line number print all file content.
     *
     * @param size the size
     * @throws AbstractApplicationException the abstract application exception
     */
    @ParameterizedTest
    @ValueSource(ints = {2, 3, 6})
    void run_CatMultipleFilesWithLineNumber_PrintAllFileContent(int size) throws AbstractApplicationException {
        String[] args = new String[size + 1];
        Arrays.fill(args, TEST_FILE);
        args[0] = "-n";
        catApplication.run(args, System.in, System.out);
        String output = normalizeLineEndings(testOutContent.toString());
        assertEquals(numberedContent.repeat(size), output);
    }

    /**
     * Run no args for cat stdin print stdin content.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     * @throws InterruptedException         the interrupted exception
     */
    @Test
    void run_NoArgsForCatStdin_PrintStdinContent() throws AbstractApplicationException,
            IOException, InterruptedException {
        String[] args = {};
        writeToStdin(expectedContent);
        catApplication.run(args, System.in, System.out);
        String output = normalizeLineEndings(testOutContent.toString());
        assertEquals(expectedContent, output);
    }

    /**
     * Run cat files and stdin print all file content and stdin content.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     * @throws InterruptedException         the interrupted exception
     */
    @Test
    void run_CatFilesAndStdin_PrintAllFileContentAndStdinContent() throws AbstractApplicationException,
            IOException, InterruptedException {
        String[] args = {TEST_FILE, "-", "-", "dir/catTestLf.txt"};
        writeToStdin(expectedContent.repeat(2));
        catApplication.run(args, System.in, System.out);
        String output = normalizeLineEndings(testOutContent.toString());
        assertEquals(expectedContent.repeat(4), output);
    }

    /**
     * Run null input stream throw exception.
     */
    @Test
    void run_NullInputStream_ThrowException() {
        String[] args = {"dir/catTest.txt"};
        Throwable thrown = assertThrows(CatException.class,
                () -> catApplication.run(args, null, System.out));
        assertEquals(new CatException(ERR_NULL_STREAMS).getMessage(), thrown.getMessage());
    }

    /**
     * Run null output stream throw exception.
     */
    @Test
    void run_NullOutputStream_ThrowException() {
        String[] args = {"dir/catTest.txt"};
        Throwable thrown = assertThrows(CatException.class,
                () -> catApplication.run(args, System.in, null));
        assertEquals(new CatException(ERR_NULL_STREAMS).getMessage(), thrown.getMessage());
    }

    /**
     * Run null args throw exception.
     */
    @Test
    void run_NullArgs_ThrowException() {
        Throwable thrown = assertThrows(CatException.class,
                () -> catApplication.run(null, System.in, System.out));
        assertEquals(new CatException(ERR_NULL_ARGS).getMessage(), thrown.getMessage());
    }

    /**
     * Cat stdin null stdin throw exception.
     */
    @Test
    void catStdin_NullStdin_ThrowException() {
        Throwable thrown = assertThrows(CatException.class,
                () -> catApplication.catStdin(false, null));
        assertEquals(new CatException(ERR_NULL_STREAMS).getMessage(), thrown.getMessage());
    }

    /**
     * Cat stdin normal cat print stdin content.
     *
     * @throws IOException                  the io exception
     * @throws InterruptedException         the interrupted exception
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void catStdin_NormalCat_PrintStdinContent() throws IOException, InterruptedException,
            AbstractApplicationException {
        writeToStdin(expectedContent);
        String output = normalizeLineEndings(catApplication.catStdin(false, System.in));
        assertEquals(expectedContent, output);
    }

    /**
     * Cat stdin cat with line number print stdin content with line number.
     *
     * @throws IOException                  the io exception
     * @throws InterruptedException         the interrupted exception
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void catStdin_CatWithLineNumber_PrintStdinContentWithLineNumber() throws IOException, InterruptedException,
            AbstractApplicationException {
        writeToStdin(expectedContent);
        String output = normalizeLineEndings(catApplication.catStdin(true, System.in));
        assertEquals(numberedContent, output);
    }

    /**
     * Cat files null files throw exception.
     */
    @Test
    void catFiles_NullFiles_ThrowException() {
        Throwable thrown = assertThrows(CatException.class,
                () -> catApplication.catFiles(false, (String[]) null));
        assertEquals(new CatException(ERR_NULL_ARGS).getMessage(), thrown.getMessage());
    }

    /**
     * Cat files empty files throw exception.
     */
    @Test
    void catFiles_EmptyFiles_ThrowException() {
        String[] files = {};
        Throwable thrown = assertThrows(CatException.class,
                () -> catApplication.catFiles(false, files));
        assertEquals(new CatException(ERR_NO_ARGS).getMessage(), thrown.getMessage());
    }

    /**
     * Cat files invalid file string returns exception text. (Invalid for java `Path.resolve`)
     *
     * @throws AbstractApplicationException
     */
    @Test
    void catFiles_InvalidFileString_ReturnsExceptionText() throws AbstractApplicationException {
        String invalidFileName = "invalid:";
        String actual = catApplication.catFiles(false, invalidFileName);
        assertEquals(new CatException(ERR_FILE_NOT_FND, invalidFileName).getMessage() + STRING_NEWLINE, actual);
    }

    /**
     * Cat files non existent file returns exception text.
     */
    @Test
    void catFiles_NonExistentFile_ReturnsExceptionText() throws AbstractApplicationException {
        String[] files = {"nonexistent"};
        String actual = catApplication.catFiles(false, files);
        assertEquals(new CatException(ERR_FILE_NOT_FND, files[0]).getMessage() + STRING_NEWLINE, actual);
    }

    /**
     * Cat files cat directory returns exception text.
     */
    @Test
    void catFiles_ArgIsDirectory_ReturnsExceptionText() throws AbstractApplicationException {
        String[] files = {"dir"};
        String actual = catApplication.catFiles(false, files);
        assertEquals(new CatException(ERR_IS_DIR, files[0]).getMessage() + STRING_NEWLINE, actual);
    }

    /**
     * Cat files normal cat print file content.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void catFiles_NormalCat_PrintFileContent() throws AbstractApplicationException {
        String[] files = {TEST_FILE};
        String output = normalizeLineEndings(catApplication.catFiles(false, files));
        assertEquals(expectedContent, output);
    }

    /**
     * Cat files cat with line number print file content with line number.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void catFiles_CatWithLineNumber_PrintFileContentWithLineNumber() throws AbstractApplicationException {
        String[] files = {TEST_FILE};
        String output = normalizeLineEndings(catApplication.catFiles(true, files));
        assertEquals(numberedContent, output);
    }

    /**
     * Cat files cat multiple files with line number print all file content with line number.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void catFiles_CatMultipleFilesWithLineNumber_PrintAllFileContentWithLineNumber() throws AbstractApplicationException {
        String[] files = {TEST_FILE, TEST_FILE};
        String output = normalizeLineEndings(catApplication.catFiles(true, files));
        assertEquals(numberedContent.repeat(2), output);
    }

    /**
     * Cat file and stdin null stdin throw exception.
     */
    @Test
    void catFileAndStdin_NullStdin_ThrowException() {
        String[] files = {TEST_FILE};
        Throwable thrown = assertThrows(CatException.class,
                () -> catApplication.catFileAndStdin(false, null, files));
        assertEquals(new CatException(ERR_NULL_STREAMS).getMessage(), thrown.getMessage());
    }

    /**
     * Cat file and stdin null files throw exception.
     */
    @Test
    void catFileAndStdin_NullFiles_ThrowException() {
        Throwable thrown = assertThrows(CatException.class,
                () -> catApplication.catFileAndStdin(false, System.in, (String[]) null));
        assertEquals(new CatException(ERR_NULL_ARGS).getMessage(), thrown.getMessage());
    }

    /**
     * Cat file and stdin empty files throw exception.
     */
    @Test
    void catFileAndStdin_EmptyFiles_ThrowException() {
        String[] files = {};
        Throwable thrown = assertThrows(CatException.class,
                () -> catApplication.catFileAndStdin(false, System.in, files));
        assertEquals(new CatException(ERR_NO_ARGS).getMessage(), thrown.getMessage());
    }

    /**
     * Cat file and stdin file then stdin print file content a nd return stdin content.
     *
     * @throws IOException                  the io exception
     * @throws InterruptedException         the interrupted exception
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void catFileAndStdin_FileThenStdin_PrintFileContentANdReturnStdinContent() throws IOException, InterruptedException,
            AbstractApplicationException {
        Thread catAppThread = new Thread(() -> {
            CatApplicationStubForFilesAndStdIn catApplication = new CatApplicationStubForFilesAndStdIn();
            catApplication.setStdout(System.out);
            String[] files = {TEST_FILE, "-"};
            try {
                String stdinAftercat = catApplication.catFileAndStdin(false, System.in, files);
                assertEquals("1234\n", normalizeLineEndings(stdinAftercat));
            } catch (AbstractApplicationException e) {
                e.printStackTrace();
                assert false;
            }
        });
        catAppThread.start();
        Thread.sleep(100);
        writeToStdin("1234");
        assertEquals(expectedContent, normalizeLineEndings(testOutContent.toString()));
        catAppThread.interrupt();
    }
}
