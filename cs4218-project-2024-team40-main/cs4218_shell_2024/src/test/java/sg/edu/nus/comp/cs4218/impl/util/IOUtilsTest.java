package sg.edu.nus.comp.cs4218.impl.util;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.*;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.ShellException;

import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static org.mockito.Mockito.*;


/**
 * The type Io utils test.
 */
class IOUtilsTest {
    private static List<String> contents = new ArrayList<>();
    private static final String BASE_DIR = Environment.currentDirectory;
    private static final String TEST_RESOURCE_DIR = "src/test/resources/app";
    private static final String TEST_DIR = Environment.currentDirectory + File.separator + TEST_RESOURCE_DIR + File.separator + "IOUtilsTest";
    private static final String TEST_TXT = "test.txt";
    private static final String TEST_PATH = TEST_DIR + File.separator + TEST_TXT;
    private static final String NON_EXISTENT_TEXT =  "test1.txt";
    private static final String MISSING_PATH = TEST_DIR + File.separator + "Wrong" + File.separator + NON_EXISTENT_TEXT;
    private static final String MISSING_TEST_PATH = TEST_DIR + File.separator + NON_EXISTENT_TEXT;
    private static final String SHELL_PREFIX = "shell: ";
    private static final String[] LINES1 = {"File", "example"};

    /**
     * Input streams content equal boolean.
     *
     * @param inputStream1 the input stream 1
     * @param inputStream2 the input stream 2
     * @return the boolean
     * @throws IOException the io exception
     */
    boolean inputStreamsContentEqual(InputStream inputStream1, InputStream inputStream2) throws IOException {
        int byte1;
        int byte2;

        // Keep reading bytes from both input streams until one of them ends
        while ((byte1 = inputStream1.read()) != -1 && (byte2 = inputStream2.read()) != -1) {
            // If the bytes read are different, the content is not equal
            if (byte1 != byte2) {
                return false;
            }
        }
        // If one stream has more bytes than the other, they are not equal
        return inputStream1.read() == -1 && inputStream2.read() == -1;
    }

    /**
     * Write to file.
     *
     * @param file    the file
     * @param content the content
     * @throws IOException the io exception
     */
    public static void writeToFile(Path file, String... content) throws IOException {
        contents = new ArrayList<>();
        for (String line : content) {
            contents.add(line);
        }
        Files.write(file, contents, WRITE, TRUNCATE_EXISTING);
    }

    /**
     * Sets before all.
     */
    @BeforeAll
    static void setupBeforeAll() {
        Environment.currentDirectory = TEST_DIR;
    }

    /**
     * Sets before each.
     *
     * @throws IOException the io exception
     */
    @BeforeEach
    void setupBeforeEach() throws IOException {
        writeToFile(Paths.get(TEST_PATH), LINES1);
        if(Files.exists(Path.of(MISSING_TEST_PATH))){
            Path.of(MISSING_TEST_PATH).toFile().delete();
        }
    }

    /**
     * Sets after each.
     *
     * @throws IOException the io exception
     */
    @AfterEach
    void setupAfterEach() throws IOException {
        writeToFile(Paths.get(TEST_PATH), LINES1);
        if(Files.exists(Path.of(MISSING_TEST_PATH))){
            Path.of(MISSING_TEST_PATH).toFile().delete();
        }
    }

    /**
     * Tear down after all.
     */
    @AfterAll
    static void tearDownAfterAll() {
        Environment.currentDirectory = BASE_DIR;
    }

    /**
     * Resolve file path valid path return correct path.
     */
    @Test
    void resolveFilePath_ValidPath_returnCorrectPath() throws IOException {
        Path actual = IOUtils.resolveFilePath(TEST_PATH);
        Path expected = Paths.get(TEST_PATH);
        assertEquals(actual,expected);
    }

    /**
     * Open input stream existing file success.
     *
     * @throws ShellException the shell exception
     * @throws IOException    the io exception
     */
    @Test
    void openInputStream_ExistingFile_Success() throws ShellException, IOException {
        try( InputStream expected =  new FileInputStream(new File(TEST_PATH));
             InputStream actual = IOUtils.openInputStream(TEST_PATH)){
            assertTrue(inputStreamsContentEqual(actual, expected));
        }
    }

    /**
     * Open input stream non existing file throws exception.
     */
    @Disabled
    @Test
    void openInputStream_NonExistingFile_ThrowsException() {
        String expected = SHELL_PREFIX + ERR_FILE_NOT_FND;
        Throwable exp = assertThrows(ShellException.class, () ->
                IOUtils.openInputStream(MISSING_TEST_PATH));
        assertEquals(expected, exp.getMessage());
    }

    /**
     * Close input stream valid input stream input stream closed.
     *
     * @throws ShellException the shell exception
     * @throws IOException    the io exception
     */
    @Test
    void closeInputStream_ValidInputStream_InputStreamClosed() throws ShellException, IOException {
        try(FileInputStream inputStream = new FileInputStream(new File(TEST_PATH))){
            IOUtils.closeInputStream(inputStream);
            // Attempt to read from the closed stream and expect an IOException
            assertThrows(IOException.class, () -> inputStream.read());
        }
    }

    /**
     * Close input stream system in not closed.
     *
     * @throws IOException the io exception
     */
    @Test
    void closeInputStream_SystemIn_NotClosed() throws IOException {
        try(InputStream inputStream = System.in) {
            assertDoesNotThrow(() -> IOUtils.closeInputStream(inputStream));
        }
    }

    /**
     * Close input stream null stream nothing happen.
     */
    @Test
    void closeInputStream_NullStream_NothingHappen() {
        //same as above
        assertDoesNotThrow(() ->
                IOUtils.closeInputStream(null)
        );
    }

    /**
     * Close input stream unclosable stream throws exception.
     *
     * @throws IOException the io exception
     */
    @Test
    void closeInputStream_UnclosableStream_ThrowsException() throws IOException {
        // mock an input stream that is unclosable
        InputStream mock = mock(InputStream.class); //NOPMD - suppressed CloseResource - resource is used for testing and is closed
        doThrow(new IOException(ERR_CLOSE_STREAMS)).when(mock).close();
        Throwable exp = assertThrows(ShellException.class, () ->
                IOUtils.closeInputStream(mock));
        assertEquals(SHELL_PREFIX + ERR_CLOSE_STREAMS, exp.getMessage());
    }

    /**
     * Close output stream system out system out not closed.
     */
    @Test
    void closeOutputStream_SystemOut_SystemOutNotClosed() {
        assertDoesNotThrow(() -> {
            IOUtils.closeOutputStream(System.out);
            // if its closed would throw IO
        });
    }

    /**
     * Close output stream null stream does nothing.
     */
    @Test
    void closeOutputStream_NullStream_DoesNothing() {
        assertDoesNotThrow(() ->
                IOUtils.closeOutputStream(null)
        );
    }

    /**
     * Close output stream valid stream success.
     *
     * @throws ShellException the shell exception
     * @throws IOException    the io exception
     */
    @Test
    void closeOutputStream_ValidStream_Success() throws ShellException, IOException {
       try(OutputStream outputStream = new FileOutputStream(new File(TEST_PATH))) {
           IOUtils.closeOutputStream(outputStream);
           String data = "Hello, world!";
           byte[] bytes = data.getBytes();
           //cannot write to stream when closed
           assertThrows(IOException.class, () -> outputStream.write(bytes));
       }

    }

    /**
     * Close output stream unclosable stream throws exception.
     *
     * @throws IOException the io exception
     */
    @Test
    void closeOutputStream_UnclosableStream_ThrowsException() throws IOException {
        OutputStream mock = mock(OutputStream.class); //NOPMD - suppressed CloseResource - resource is used for testing and is closed
        doThrow(new IOException(ERR_CLOSE_STREAMS)).when(mock).close();
        assertThrows(ShellException.class, () ->
                IOUtils.closeOutputStream(mock));
    }

    /**
     * Open output stream existing file success.
     *
     * @throws ShellException the shell exception
     * @throws IOException    the io exception
     */
    @Test
    void openOutputStream_ExistingFile_Success() throws ShellException, IOException {
        assertDoesNotThrow(() ->
                IOUtils.openOutputStream(TEST_PATH)
        );
    }

    /**
     * Open output stream non existing file create file.
     *
     * @throws IOException    the io exception
     * @throws ShellException the shell exception
     */
    @Test
    void openOutputStream_NonExistingFile_CreateFile() throws IOException, ShellException{
        IOUtils.openOutputStream(NON_EXISTENT_TEXT);
        assertTrue(Files.exists(Paths.get(MISSING_TEST_PATH)));
    }

    /**
     * Open output stream existing file is dir throw exception.
     *
     * @throws IOException    the io exception
     * @throws ShellException the shell exception
     */
    @Test
    void openOutputStream_ExistingFileIsDir_ThrowException() throws IOException, ShellException{
        Throwable exp = assertThrows(ShellException.class, () ->IOUtils.openOutputStream(TEST_DIR));
        assertEquals(SHELL_PREFIX + ERR_IS_DIR, exp.getMessage());
    }

    /**
     * Open output stream non existing file path throw exception.
     *
     * @throws IOException    the io exception
     * @throws ShellException the shell exception
     */
    @Test
    void openOutputStream_NonExistingFilePath_ThrowException() throws IOException, ShellException{
        Throwable exp = assertThrows(ShellException.class, () ->IOUtils.openOutputStream(MISSING_PATH));
        assertEquals(SHELL_PREFIX + ERR_FILE_NOT_FND, exp.getMessage());
    }

    /**
     * Gets lines from input stream valid file input stream returns lines from stream.
     *
     * @throws IOException the io exception
     */
    @Test
    void getLinesFromInputStream_ValidFileInputStream_ReturnsLinesFromStream() throws IOException {
        List<String> expected = Arrays.asList(LINES1);
        List<String> actual  = IOUtils.getLinesFromInputStream(new FileInputStream(new File(TEST_PATH)));
        assertEquals(expected, actual);

    }

    /**
     * Gets lines from input stream null stream throws exception.
     */
    @Test
    void getLinesFromInputStream_NullStream_ThrowsException() {
        Throwable exp = assertThrows(NullPointerException.class, () ->
                IOUtils.getLinesFromInputStream(null));
        assertEquals(new NullPointerException().getMessage(), exp.getMessage());
    }

}
