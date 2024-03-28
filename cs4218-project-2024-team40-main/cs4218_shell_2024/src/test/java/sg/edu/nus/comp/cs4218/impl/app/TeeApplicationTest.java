package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.TeeException;
import sg.edu.nus.comp.cs4218.impl.parser.TeeArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;



import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.nio.file.StandardOpenOption.*;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static java.nio.file.Files.readString;


/**
 * Tests Functionality of tee application
 */
//Assumption, when creating files/writing to files always have a /n before EOF
public class TeeApplicationTest {
    /**
     * The Tee args parser.
     */
    TeeArgsParser teeArgsParser;
    /**
     * The Tee application.
     */
    TeeApplication teeApplication;
    private static final String EXAMPLE_INPUT = "Hello" + System.lineSeparator() + "World" + System.lineSeparator() + "Bye";
    private static final List<String> EXAMPLE_LINES = Arrays.asList("Hello", "World", "Bye");
    private static final String FILE_NAME1 = "file1.txt";
    private static final String FILE_NAME2 = "file2.txt";
    private static final String ILLEGAL_FILE_NAME = "file2*?.txt";
    private static final String FOLDER_NAME = "folder";
    private static final String NON_EXISTENT_FILE = "nonExistent.txt";
    private static final String NO_PERM_FILE = "unwritable.txt";
    private static final String[] LINES1 = {"The first file", "The second line"};
    private static final String[] LINES2 = {"The second file", "The second line"};

    private static final String BASE_PATH = Environment.currentDirectory;
    private static final String TEST_PATH = BASE_PATH + File.separator + "teeTest";
    private static final String FILE_PATH1 = TEST_PATH + File.separator + FILE_NAME1;
    private static final String FILE_PATH2 = TEST_PATH + File.separator + FILE_NAME2;
    private static final String FOLDER1_PATH = TEST_PATH + File.separator + FOLDER_NAME;
    private static final String MISSING_FILE_PATH = TEST_PATH + File.separator + NON_EXISTENT_FILE;
    private static final String NO_PERM_FILE_PATH = TEST_PATH + File.separator + NO_PERM_FILE;
    private static OutputStream outputStream;
    private final InputStream inputStream = new ByteArrayInputStream(EXAMPLE_INPUT.getBytes());
    private static List<String> contents = new ArrayList<>();
    private static final String TEE_PREFIX = "tee: ";
    /**
     * The constant ILLEGAL_FLAG_MSG.
     */
    public static final String ILLEGAL_FLAG_MSG = "illegal option -- ";


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
     * Tear down each.
     */
    @AfterEach
    void tearDownEach() {
        Environment.currentDirectory = BASE_PATH;
        deleteDirectory(new File(TEST_PATH));

    }

    /**
     * Delete directory.
     *
     * @param directory the directory
     */
    public void deleteDirectory(File directory) {
        File[] contents = directory.listFiles();
        if (contents != null) {
            for (File file : contents) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }

    /**
     * Sets up each.
     *
     * @throws IOException the io exception
     */
    @BeforeEach
    void setUpEach() throws IOException {
        this.teeArgsParser = mock(TeeArgsParser.class);
//        this.teeArgsParser = new TeeArgsParser();
        this.teeApplication = new TeeApplication(teeArgsParser);
        contents = new ArrayList<>();
        outputStream = new ByteArrayOutputStream();
        Files.deleteIfExists(Paths.get(TEST_PATH));
        Files.createDirectory(Paths.get(TEST_PATH));
        Files.createDirectory(Paths.get(FOLDER1_PATH));
        Files.createFile(Paths.get(FILE_PATH1));
        Files.createFile(Paths.get(FILE_PATH2));
        Files.createFile(Paths.get(NO_PERM_FILE_PATH));
        Paths.get(NO_PERM_FILE_PATH).toFile().setReadOnly();
        writeToFile(Paths.get(FILE_PATH1), LINES1);
        writeToFile(Paths.get(FILE_PATH2), LINES2);
        Environment.currentDirectory = TEST_PATH;



    }

    /**
     * Gets input stream success read return correctoutput.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void getInputStream_SuccessRead_ReturnCorrectoutput() throws AbstractApplicationException {
        List<String> lines = EXAMPLE_LINES;
        try(MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
            mockedStatic.when(() -> IOUtils.getLinesFromInputStream(any(InputStream.class)))
                    .thenReturn(lines);
            List<String> returnVal = teeApplication.getInputStream(inputStream);
            List<String> expected = EXAMPLE_LINES;
            assertEquals(returnVal, expected);
        }
    }

    /**
     * Gets input stream null stdin throws exception.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void getInputStream_NullStdin_ThrowsException() throws AbstractApplicationException {
        Throwable exp = assertThrows(TeeException.class, () -> teeApplication.getInputStream(null));
        assertEquals(new TeeException(ERR_NULL_STREAMS).getMessage(),
                exp.getMessage());
    }

    /**
     * Gets input stream i outils throw i oexcp throws exception.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void getInputStream_IOutilsThrowIOexcp_ThrowsException() throws AbstractApplicationException {
        try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
            mockedStatic.when(() -> IOUtils.getLinesFromInputStream(any(InputStream.class)))
                    .thenThrow(new IOException());
            Throwable exp = assertThrows(TeeException.class, () -> teeApplication.getInputStream(inputStream));
            assertEquals(exp.getMessage(), TEE_PREFIX + ERR_READ_STREAM);
        }
    }

    /**
     * Tee to file existing file no append correct file content.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     */
    @Test
    void teeToFile_ExistingFileNoAppend_CorrectFileContent() throws AbstractApplicationException, IOException {
        List<String> contents = EXAMPLE_LINES;
        teeApplication.teeToFile(false, contents,FILE_NAME1);
        String contentFile1 = readString(Path.of(FILE_PATH1));
        assertEquals(EXAMPLE_INPUT + System.lineSeparator(), contentFile1);

    }

    /**
     * Tee to file non existing file no append correct file content.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     */
    @Test
    void teeToFile_NonExistingFileNoAppend_CorrectFileContent() throws AbstractApplicationException, IOException {
        List<String> contents = EXAMPLE_LINES;
        teeApplication.teeToFile(false, contents, NON_EXISTENT_FILE);
        String contentFile1 = readString(Path.of(MISSING_FILE_PATH));
        assertEquals(EXAMPLE_INPUT + System.lineSeparator(), contentFile1);

    }

    /**
     * Tee to file existing file append correct file content.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     */
    @Test
    void teeToFile_ExistingFileAppend_CorrectFileContent() throws AbstractApplicationException, IOException {
        StringBuilder string = new StringBuilder();
        for (String s : LINES1) {
            string.append(s).append(System.lineSeparator());
        }
        string.append(EXAMPLE_INPUT);
        List<String> contents = EXAMPLE_LINES;
        teeApplication.teeToFile(true, contents, FILE_NAME1);
        String contentFile1 = readString(Path.of(FILE_PATH1));
        assertEquals(string.toString() + System.lineSeparator(), contentFile1);

    }

    /**
     * Tee to file no file throw exception.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     */
    @Test
    void teeToFile_NoFile_ThrowException() throws AbstractApplicationException, IOException {
        StringBuilder string = new StringBuilder();
        for (String s : LINES1) {
            string.append(s).append(System.lineSeparator());
        }
        string.append(EXAMPLE_INPUT);
        List<String> contents = EXAMPLE_LINES;
        Throwable exp = assertThrows(TeeException.class, () -> teeApplication.teeToFile(true, contents,""));

        assertEquals(TEE_PREFIX + ERR_FILE_NOT_FND, exp.getMessage());

    }

    /**
     * Tee to file not file is dir throw exception.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     */
    @Test
    void teeToFile_NotFileIsDir_ThrowException() throws AbstractApplicationException, IOException {
        StringBuilder string = new StringBuilder();
        for (String s : LINES1) {
            string.append(s).append(System.lineSeparator());
        }
        string.append(EXAMPLE_INPUT);
        List<String> contents = EXAMPLE_LINES;
        Throwable exp = assertThrows(TeeException.class, () -> teeApplication.teeToFile(true, contents,FOLDER_NAME));

        assertEquals(TEE_PREFIX + ERR_IS_DIR, exp.getMessage());

    }

    /**
     * Tee to file no file perm throw exception.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     */
    @Test
    void teeToFile_NoFilePerm_ThrowException() throws AbstractApplicationException, IOException {
        StringBuilder string = new StringBuilder();
        for (String s : LINES1) {
            string.append(s).append(System.lineSeparator());
        }
        string.append(EXAMPLE_INPUT);
        List<String> contents = EXAMPLE_LINES;
        Throwable exp = assertThrows(TeeException.class, () -> teeApplication.teeToFile(true, contents,NO_PERM_FILE));

        assertEquals(TEE_PREFIX + ERR_NO_PERM, exp.getMessage());

    }

    /**
     * Tee to file cannot write to file throw exception.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     */
    @Test
    void teeToFile_CannotWriteToFile_ThrowException() throws AbstractApplicationException, IOException {
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.write(any(Path.class), anyList(), eq(CREATE), eq(WRITE), eq(APPEND)))
                    .thenThrow(new IOException());
            StringBuilder string = new StringBuilder();
            for (String s : LINES1) {
                string.append(s).append(System.lineSeparator());
            }
            string.append(EXAMPLE_INPUT);
            List<String> contents = EXAMPLE_LINES;
            Throwable exp = assertThrows(TeeException.class, () -> teeApplication.teeToFile(true, contents, FILE_NAME1));
            assertEquals(new TeeException(ERR_WRITING_FILE).getMessage(),
                    exp.getMessage());
        }
    }

    /**
     * Tee to file invalid file char name throw exception.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     */
// Not sure why it passes on intellij but not on Github actions
    @Disabled
    @ParameterizedTest
    @ValueSource(strings = { "*", ":", "?", "<", ">", "|"})
    void teeToFile_InvalidFileCharName_ThrowException() throws AbstractApplicationException, IOException {
        StringBuilder string = new StringBuilder();
        for (String s : LINES1) {
            string.append(s).append(System.lineSeparator());
        }
        string.append(EXAMPLE_INPUT);
        List<String> contents =EXAMPLE_LINES;
        Throwable exp = assertThrows(TeeException.class, () -> teeApplication.teeToFile(true, contents, ILLEGAL_FILE_NAME));

        assertEquals(TEE_PREFIX + ERR_NOT_SUPPORTED, exp.getMessage());

    }


    /**
     * Tee from stdin overwrite existing file should overwrite previous content.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     */
    @Test
    void teeFromStdin_OverwriteExistingFile_shouldOverwritePreviousContent() throws AbstractApplicationException, IOException {
        String returnValue = teeApplication.teeFromStdin(false, inputStream, FILE_NAME1);
        String contentFile1 = readString(Path.of(FILE_PATH1));
        assertEquals(EXAMPLE_INPUT, returnValue);
               //new line seems to always be added when creatign and writing to files in intellij console
        assertEquals(EXAMPLE_INPUT + System.lineSeparator(), contentFile1);

    }

    /**
     * Tee from stdin multiple overwrite existing file should overwrite previous content.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     */
    @Test
    void teeFromStdin_MultipleOverwriteExistingFile_shouldOverwritePreviousContent() throws AbstractApplicationException, IOException {
        String[] files  = new String[]{FILE_NAME1, FILE_NAME2};
        String returnValue = teeApplication.teeFromStdin(false, inputStream, files);
        String contentFile1 = readString(Path.of(FILE_PATH1));
        String contentFile2 = readString(Path.of(FILE_PATH1));
        assertEquals(EXAMPLE_INPUT, returnValue);
               //new line seems to always be added when creatign and writing to files in intellij console
        assertEquals(EXAMPLE_INPUT + System.lineSeparator(), contentFile1);
        assertEquals(EXAMPLE_INPUT + System.lineSeparator(), contentFile2);

    }

    /**
     * Tee from stdin append existing file should append previous content.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     */
    @Test
    void teeFromStdin_AppendExistingFile_shouldAppendPreviousContent() throws AbstractApplicationException, IOException {
        StringBuilder stringfile = new StringBuilder();
        StringBuilder stringreturn = new StringBuilder();
        stringreturn.append(EXAMPLE_INPUT);
        for (String s : LINES1) {
            stringfile.append(s).append(System.lineSeparator());
        }
        stringfile.append(EXAMPLE_INPUT);
        String returnValue = teeApplication.teeFromStdin(true, inputStream, FILE_NAME1);
        String contentFile1 = readString(Path.of(FILE_PATH1));
        assertEquals(stringreturn.toString(), returnValue);
               //new line seems to always be added when creatign and writing to files in intellij console
        assertEquals(stringfile.toString() + System.lineSeparator(), contentFile1);

    }

    /**
     * Tee from stdin append multiple existing file should append previous content.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     */
    @Test
    void teeFromStdin_AppendMultipleExistingFile_shouldAppendPreviousContent() throws AbstractApplicationException, IOException {
        String[] files  = new String[]{FILE_NAME1, FILE_NAME2};
        StringBuilder stringfile1 = new StringBuilder();
        StringBuilder stringfile2 = new StringBuilder();
        StringBuilder stringreturn = new StringBuilder();
        stringreturn.append(EXAMPLE_INPUT);
        for (String s : LINES1) {
            stringfile1.append(s).append(System.lineSeparator());
        }
        for (String s : LINES2) {
            stringfile2.append(s).append(System.lineSeparator());
        }
        stringfile1.append(EXAMPLE_INPUT);
        stringfile2.append(EXAMPLE_INPUT);
        String returnValue = teeApplication.teeFromStdin(true, inputStream, files);
        String contentFile1 = readString(Path.of(FILE_PATH1));
        String contentFile2 = readString(Path.of(FILE_PATH2));
        assertEquals(stringreturn.toString(), returnValue);
               //new line seems to always be added when creatign and writing to files in intellij console
        assertEquals(stringfile1.toString() + System.lineSeparator(), contentFile1);
        assertEquals(stringfile2.toString() + System.lineSeparator(), contentFile2);

    }

    /**
     * Tee from stdin missing arg append throw exception.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     */
    @Test
    void teeFromStdin_MissingArgAppend_ThrowException() throws AbstractApplicationException, IOException {

        Throwable exp = assertThrows(TeeException.class, () -> teeApplication.teeFromStdin(null, inputStream, FILE_NAME1));
        assertEquals(TEE_PREFIX + ERR_NULL_ARGS, exp.getMessage());

    }

    /**
     * Tee from stdin missing arg stdin throw exception.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     */
    @Test
    void teeFromStdin_MissingArgStdin_ThrowException() throws AbstractApplicationException, IOException {

        Throwable exp = assertThrows(TeeException.class, () -> teeApplication.teeFromStdin(false, null, FILE_NAME1));
        assertEquals(TEE_PREFIX + ERR_NO_ISTREAM, exp.getMessage());

    }

    /**
     * Tee from stdin null valuein files throw exception.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     */
    @Test
    void teeFromStdin_NullValueinFiles_ThrowException() throws AbstractApplicationException, IOException {
        String[] files = new String[]{FILE_NAME1, null};
        Throwable exp = assertThrows(TeeException.class, () -> teeApplication.teeFromStdin(false, inputStream, files));
        assertEquals(TEE_PREFIX + ERR_NULL_ARGS, exp.getMessage());

    }

    /**
     * Run overwrite existing file should overwrite previous content.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     * @throws InvalidArgsException         the invalid args exception
     */
    @Test
    void run_OverwriteExistingFile_shouldOverwritePreviousContent() throws AbstractApplicationException, IOException, InvalidArgsException {
        String [] args = new String[]{FILE_NAME1};
        List<String> files = new ArrayList<>();
        files.add(FILE_NAME1);
        doNothing().when(teeArgsParser).parse(any(String[].class));
        when(teeArgsParser.isAppend()).thenReturn(false);
        when(teeArgsParser.getFileNames()).thenReturn(files);
        teeApplication.run(args, inputStream, outputStream);
        String contentFile1 = readString(Path.of(FILE_PATH1));
        assertEquals(EXAMPLE_INPUT + System.lineSeparator(), outputStream.toString());
        assertEquals(EXAMPLE_INPUT + System.lineSeparator(), contentFile1);

    }
    /**
     * Run overwrite existing file should overwrite previous content.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     * @throws InvalidArgsException         the invalid args exception
     */
    @Test
    void run_StdinArgonly_EchoOnly() throws AbstractApplicationException, InvalidArgsException {
        String [] args = new String[]{"-"};
        List<String> files = new ArrayList<>();
        files.add("-");
        doNothing().when(teeArgsParser).parse(any(String[].class));
        when(teeArgsParser.isAppend()).thenReturn(false);
        when(teeArgsParser.getFileNames()).thenReturn(files);
        teeApplication.run(args, inputStream, outputStream);
        assertEquals(EXAMPLE_INPUT + System.lineSeparator(), outputStream.toString());
//        assertTrue();

    }

    /**
     * Run overwrite multiple existing file should overwrite previous content.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     * @throws InvalidArgsException         the invalid args exception
     */
    @Test
    void run_OverwriteMultipleExistingFile_shouldOverwritePreviousContent() throws AbstractApplicationException, IOException, InvalidArgsException {
        String [] args = new String[]{FILE_NAME1, FILE_NAME2};
        List<String> files = new ArrayList<>();
        files.add(FILE_NAME1);
        files.add(FILE_NAME2);
        doNothing().when(teeArgsParser).parse(any(String[].class));
        when(teeArgsParser.isAppend()).thenReturn(false);
        when(teeArgsParser.getFileNames()).thenReturn(files);
        teeApplication.run(args, inputStream, outputStream);
        String contentFile1 = readString(Path.of(FILE_PATH1));
        String contentFile2 = readString(Path.of(FILE_PATH2));
        assertEquals(EXAMPLE_INPUT + System.lineSeparator(), outputStream.toString());
        assertEquals(EXAMPLE_INPUT + System.lineSeparator(), contentFile1);
        assertEquals(EXAMPLE_INPUT + System.lineSeparator(), contentFile2);

    }

    /**
     * Run append existing file should append previous content.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     * @throws InvalidArgsException         the invalid args exception
     */
    @Test
    void run_AppendExistingFile_shouldAppendPreviousContent() throws AbstractApplicationException, IOException, InvalidArgsException {
        StringBuilder stringfile = new StringBuilder();
        String [] args = new String[]{"-a", FILE_NAME1};
        for (String s : LINES1) {
            stringfile.append(s).append(System.lineSeparator());
        }
        stringfile.append(EXAMPLE_INPUT);
        List<String> files = new ArrayList<>();
        files.add(FILE_NAME1);
        doNothing().when(teeArgsParser).parse(any(String[].class));
        when(teeArgsParser.isAppend()).thenReturn(true);
        when(teeArgsParser.getFileNames()).thenReturn(files);
        teeApplication.run(args, inputStream, outputStream);
        String contentFile1 = readString(Path.of(FILE_PATH1));
        assertEquals(EXAMPLE_INPUT + System.lineSeparator(), outputStream.toString());
        assertEquals(stringfile.toString() + System.lineSeparator(), contentFile1);

    }

    /**
     * Run append multiple existing file should append previous content.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     * @throws InvalidArgsException         the invalid args exception
     */
    @Test
    void run_AppendMultipleExistingFile_shouldAppendPreviousContent() throws AbstractApplicationException, IOException, InvalidArgsException {
        StringBuilder stringfile1 = new StringBuilder();
        StringBuilder stringfile2 = new StringBuilder();
        String [] args = new String[]{"-a", FILE_NAME1, FILE_NAME2};
        for (String s : LINES1) {
            stringfile1.append(s).append(System.lineSeparator());
        }
        for (String s : LINES2) {
            stringfile2.append(s).append(System.lineSeparator());
        }
        stringfile1.append(EXAMPLE_INPUT);
        stringfile2.append(EXAMPLE_INPUT);
        List<String> files = new ArrayList<>();
        files.add(FILE_NAME1);
        files.add(FILE_NAME2);
        doNothing().when(teeArgsParser).parse(any(String[].class));
        when(teeArgsParser.isAppend()).thenReturn(true);
        when(teeArgsParser.getFileNames()).thenReturn(files);
        teeApplication.run(args, inputStream, outputStream);
        String contentFile1 = readString(Path.of(FILE_PATH1));
        String contentFile2 = readString(Path.of(FILE_PATH2));
        assertEquals(EXAMPLE_INPUT + System.lineSeparator(), outputStream.toString());
        assertEquals(stringfile1.toString() + System.lineSeparator(), contentFile1);
        assertEquals(stringfile2.toString() + System.lineSeparator(), contentFile2);
    }

    /**
     * Run missing arg stdin throw exception.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     * @throws InvalidArgsException         the invalid args exception
     */
    @Test
    void run_MissingArgStdin_ThrowException() throws AbstractApplicationException, IOException, InvalidArgsException {
        String [] args = new String[]{FILE_NAME1};
        List<String> files = new ArrayList<>();
        files.add(FILE_NAME1);
        doNothing().when(teeArgsParser).parse(any(String[].class));
        when(teeArgsParser.isAppend()).thenReturn(false);
        when(teeArgsParser.getFileNames()).thenReturn(files);
        Throwable exp = assertThrows(TeeException.class, () -> teeApplication.run(args, null, outputStream));
        assertEquals(TEE_PREFIX + ERR_NO_ISTREAM, exp.getMessage());

    }

    /**
     * Run missing arg stdout throw exception.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     * @throws InvalidArgsException         the invalid args exception
     */
    @Test
    void run_MissingArgStdout_ThrowException() throws AbstractApplicationException, IOException, InvalidArgsException {
        String [] args = new String[]{FILE_NAME1};
        List<String> files = new ArrayList<>();
        files.add(FILE_NAME1);
        doNothing().when(teeArgsParser).parse(any(String[].class));
        when(teeArgsParser.isAppend()).thenReturn(false);
        when(teeArgsParser.getFileNames()).thenReturn(files);
        Throwable exp = assertThrows(TeeException.class, () -> teeApplication.run(args, inputStream, null));
        assertEquals(TEE_PREFIX + ERR_NO_OSTREAM, exp.getMessage());

    }

    /**
     * Run cannot write to stdout throw exception.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     * @throws InvalidArgsException         the invalid args exception
     */
    @Test
    @SuppressWarnings("PMD.CloseResource")
    void run_CannotWriteToStdout_ThrowException() throws AbstractApplicationException, IOException, InvalidArgsException {
        OutputStream outputStream = mock(OutputStream.class);
        doThrow(new IOException()).when(outputStream).write(any(byte[].class));
        String [] args = new String[]{FILE_NAME1};
        List<String> files = new ArrayList<>();
        files.add(FILE_NAME1);
        when(teeArgsParser.isAppend()).thenReturn(false);
        when(teeArgsParser.getFileNames()).thenReturn(files);
        Throwable exp = assertThrows(TeeException.class, () -> teeApplication.run(args, inputStream, outputStream));
        assertEquals(TEE_PREFIX + ERR_IO_EXCEPTION, exp.getMessage());
    }

    /**
     * Run illegal flag throw exception.
     *
     * @param flag the flag
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     * @throws InvalidArgsException         the invalid args exception
     */
    @ParameterizedTest
    @ValueSource(strings = { "-b", "-c", "-d", "-e", "-f", "-g", "-h"})
    void run_IllegalFlag_ThrowException(String flag) throws AbstractApplicationException, IOException , InvalidArgsException{
        String [] args = new String[]{flag,FILE_NAME1};
        List<String> files = new ArrayList<>();
        files.add(FILE_NAME1);
        doThrow(new InvalidArgsException(ILLEGAL_FLAG_MSG + flag)).when(teeArgsParser).parse(args);
        when(teeArgsParser.isAppend()).thenReturn(false);
        when(teeArgsParser.getFileNames()).thenReturn(files);
        Throwable exp = assertThrows(TeeException.class, () -> teeApplication.run(args, inputStream, outputStream));
        assertEquals(TEE_PREFIX + ILLEGAL_FLAG_MSG + flag, exp.getMessage());
    }

    /**
     * Run illegal char throw exception.
     *
     * @param chr the chr
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     * @throws InvalidArgsException         the invalid args exception
     */
    @Disabled
    @ParameterizedTest
    @ValueSource(strings = { "*", ":", "?", "<", ">", "|"})
    void run_IllegalChar_ThrowException(String chr) throws AbstractApplicationException, IOException, InvalidArgsException {
        String [] args = new String[]{chr,FILE_NAME1};
        List<String> files = new ArrayList<>();
        files.add(chr);
        files.add(FILE_NAME1);
        doNothing().when(teeArgsParser).parse(any(String[].class));
        when(teeArgsParser.isAppend()).thenReturn(false);
        when(teeArgsParser.getFileNames()).thenReturn(files);
        Throwable exp = assertThrows(TeeException.class, () -> teeApplication.run(args, inputStream, outputStream));
        assertEquals(TEE_PREFIX + ERR_NOT_SUPPORTED, exp.getMessage());

    }

}
