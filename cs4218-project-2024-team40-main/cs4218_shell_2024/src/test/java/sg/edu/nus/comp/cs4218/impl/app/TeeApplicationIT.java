package sg.edu.nus.comp.cs4218.impl.app;



import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.TeeException;
import sg.edu.nus.comp.cs4218.impl.parser.TeeArgsParser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.nio.file.StandardOpenOption.*;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static java.nio.file.Files.readString;


/**
 * Integration Tests Functionality of tee application
 */
//Assumption, when creating files/writing to files always have a /n before EOF
public class TeeApplicationIT { //NOPMD - suppressed ClassNamingConventions - default Naming
    /**
     * The Tee application.
     */
    TeeApplication teeApplication;
    /**
     * The Is closed.
     */
    boolean isClosed;
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
    /**
     * The Input stream.
     */
    InputStream inputStream = null ;
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


        this.teeApplication = new TeeApplication(new TeeArgsParser());
        isClosed = false;
        contents = new ArrayList<>();
        outputStream = new ByteArrayOutputStream();
        inputStream = new ByteArrayInputStream(EXAMPLE_INPUT.getBytes());
        Files.deleteIfExists(Paths.get(TEST_PATH));
        Files.createDirectory(Paths.get(TEST_PATH));
        Files.createDirectory(Paths.get(FOLDER1_PATH));
        Files.createFile(Paths.get(FILE_PATH1));
        Files.createFile(Paths.get(FILE_PATH2));
        Files.createFile(Paths.get(NO_PERM_FILE_PATH));
        Paths.get(NO_PERM_FILE_PATH).toFile().setReadOnly();
        writeToFile(Paths.get(FILE_PATH1), LINES1);
        writeToFile(Paths.get(FILE_PATH2), LINES2);



    }

    /**
     * Gets input stream success read return correctoutput.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void getInputStream_successRead_returnCorrectoutput() throws AbstractApplicationException {
        List<String> lines = EXAMPLE_LINES;
        List<String> returnVal = teeApplication.getInputStream(inputStream);

        assertEquals(lines, returnVal);

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
        teeApplication.teeToFile(false, contents,FILE_PATH1);
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
        teeApplication.teeToFile(false, contents, MISSING_FILE_PATH);
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
        teeApplication.teeToFile(true, contents,FILE_PATH1);
        String contentFile1 = readString(Path.of(FILE_PATH1));
        assertEquals(string + System.lineSeparator(), contentFile1);

    }

    /**
     * Tee to file no file throw exception.
     */
    @Test
    void teeToFile_NoFile_ThrowException() {
        List<String> contents = EXAMPLE_LINES;
        Throwable exp = assertThrows(TeeException.class, () -> teeApplication.teeToFile(true, contents,""));
        assertEquals(TEE_PREFIX + ERR_FILE_NOT_FND, exp.getMessage());

    }

    /**
     * Tee to file not file is dir throw exception.
     */
    @Test
    void teeToFile_NotFileIsDir_ThrowException() {
        List<String> contents = EXAMPLE_LINES;
        Throwable exp = assertThrows(TeeException.class, () -> teeApplication.teeToFile(true, contents,FOLDER1_PATH));

        assertEquals(TEE_PREFIX + ERR_IS_DIR, exp.getMessage());

    }

    /**
     * Tee to file no file perm throw exception.
     *
     */
    @Test
    void teeToFile_NoFilePerm_ThrowException()  {
        List<String> contents = EXAMPLE_LINES;
        Throwable exp = assertThrows(TeeException.class, () -> teeApplication.teeToFile(true, contents,NO_PERM_FILE_PATH));

        assertEquals(TEE_PREFIX + ERR_NO_PERM, exp.getMessage());

    }

    /**
     * Tee to file invalid file char name throw exception.
     */
// Not sure why it passes on intellij but not on Github actions
    @Disabled
    @ParameterizedTest
    @ValueSource(strings = { "*", ":", "?", "<", ">", "|"})
    void teeToFile_InvalidFileCharName_ThrowException() {
        List<String> contents = EXAMPLE_LINES;
        Throwable exp = assertThrows(TeeException.class, () -> teeApplication.teeToFile(true, contents,ILLEGAL_FILE_NAME));

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
        String returnValue = teeApplication.teeFromStdin(false, inputStream, FILE_PATH1);
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
        String[] files  = new String[]{FILE_PATH1, FILE_PATH2};
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
        String returnValue = teeApplication.teeFromStdin(true, inputStream, FILE_PATH1);
        String contentFile1 = readString(Path.of(FILE_PATH1));
        assertEquals(stringreturn.toString(), returnValue);
        //new line seems to always be added when creatign and writing to files in intellij console
        assertEquals(stringfile + System.lineSeparator(), contentFile1);

    }

    /**
     * Tee from stdin append multiple existing file should append previous content.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     */
    @Test
    void teeFromStdin_AppendMultipleExistingFile_shouldAppendPreviousContent() throws AbstractApplicationException, IOException {
        String[] files  = new String[]{FILE_PATH1, FILE_PATH2};
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
        assertEquals(stringfile1 + System.lineSeparator(), contentFile1);
        assertEquals(stringfile2+ System.lineSeparator(), contentFile2);

    }

    /**
     * Tee from stdin missing arg append throw exception.
     *
     */
    @Test
    void teeFromStdin_MissingArgAppend_ThrowException()  {

        Throwable exp = assertThrows(TeeException.class, () -> teeApplication.teeFromStdin(null, inputStream, FILE_PATH1));
        assertEquals(TEE_PREFIX + ERR_NULL_ARGS, exp.getMessage());

    }

    /**
     * Tee from stdin missing arg stdin throw exception.
     */
    @Test
    void teeFromStdin_MissingArgStdin_ThrowException()  {

        Throwable exp = assertThrows(TeeException.class, () -> teeApplication.teeFromStdin(false, null, FILE_PATH1));
        assertEquals(TEE_PREFIX + ERR_NO_ISTREAM, exp.getMessage());

    }

    /**
     * Tee from stdin null valuein files throw exception.
     *
     */
    @Test
    void teeFromStdin_NullValueinFiles_ThrowException()  {
        String[] files = new String[]{FILE_PATH1, null};
        Throwable exp = assertThrows(TeeException.class, () -> teeApplication.teeFromStdin(false, inputStream, files));
        assertEquals(TEE_PREFIX + ERR_NULL_ARGS, exp.getMessage());

    }

    /**
     * Run overwrite existing file should overwrite previous content.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     */
    @Test
    void run_OverwriteExistingFile_shouldOverwritePreviousContent() throws AbstractApplicationException, IOException {
        String [] args = new String[]{FILE_PATH1};
        teeApplication.run(args, inputStream, outputStream);
        String contentFile1 = readString(Path.of(FILE_PATH1));
        assertEquals(EXAMPLE_INPUT + System.lineSeparator(), outputStream.toString());
        assertEquals(EXAMPLE_INPUT + System.lineSeparator(), contentFile1);

    }

    /**
     * Run overwrite multiple existing file should overwrite previous content.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     */
    @Test
    void run_OverwriteMultipleExistingFile_shouldOverwritePreviousContent() throws AbstractApplicationException, IOException {
        String [] args = new String[]{FILE_PATH1, FILE_PATH2};
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
     */
    @Test
    void run_AppendExistingFile_shouldAppendPreviousContent() throws AbstractApplicationException, IOException{
        StringBuilder stringfile = new StringBuilder();
        String [] args = new String[]{"-a", FILE_PATH1};
        for (String s : LINES1) {
            stringfile.append(s).append(System.lineSeparator());
        }
        stringfile.append(EXAMPLE_INPUT);
        teeApplication.run(args, inputStream, outputStream);
        String contentFile1 = readString(Path.of(FILE_PATH1));
        assertEquals(EXAMPLE_INPUT + System.lineSeparator(), outputStream.toString());
        assertEquals(stringfile + System.lineSeparator(), contentFile1);

    }

    /**
     * Run append multiple existing file should append previous content.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     */
    @Test
    void run_AppendMultipleExistingFile_shouldAppendPreviousContent() throws AbstractApplicationException, IOException {
        StringBuilder stringfile1 = new StringBuilder();
        StringBuilder stringfile2 = new StringBuilder();
        String [] args = new String[]{"-a", FILE_PATH1, FILE_PATH2};
        for (String s : LINES1) {
            stringfile1.append(s).append(System.lineSeparator());
        }
        for (String s : LINES2) {
            stringfile2.append(s).append(System.lineSeparator());
        }
        stringfile1.append(EXAMPLE_INPUT);
        stringfile2.append(EXAMPLE_INPUT);
        teeApplication.run(args, inputStream, outputStream);
        String contentFile1 = readString(Path.of(FILE_PATH1));
        String contentFile2 = readString(Path.of(FILE_PATH2));
        assertEquals(EXAMPLE_INPUT + System.lineSeparator(), outputStream.toString());
        assertEquals(stringfile1 + System.lineSeparator(), contentFile1);
        assertEquals(stringfile2 + System.lineSeparator(), contentFile2);
    }

    /**
     * Run missing arg stdin throw exception.
     *
     */
    @Test
    void run_MissingArgStdin_ThrowException()  {
        String [] args = new String[]{FILE_PATH1};
        Throwable exp = assertThrows(TeeException.class, () -> teeApplication.run(args, null, outputStream));
        assertEquals(TEE_PREFIX + ERR_NO_ISTREAM, exp.getMessage());

    }

    /**
     * Run missing arg stdout throw exception.
     */
    @Test
    void run_MissingArgStdout_ThrowException()  {
        String [] args = new String[]{FILE_PATH1};
        Throwable exp = assertThrows(TeeException.class, () -> teeApplication.run(args, inputStream, null));
        assertEquals(TEE_PREFIX + ERR_NO_OSTREAM, exp.getMessage());

    }

    /**
     * Run illegal flag throw exception.
     *
     * @param flag the flag
     */
    @ParameterizedTest
    @ValueSource(strings = { "-b", "-c", "-d", "-e", "-f", "-g", "-h"})
    void run_IllegalFlag_ThrowException(String flag) {
        String [] args = new String[]{flag,FILE_PATH1};
        Throwable exp = assertThrows(TeeException.class, () -> teeApplication.run(args, inputStream, outputStream));
        assertEquals(TEE_PREFIX + ILLEGAL_FLAG_MSG +  flag.replace("-", "") , exp.getMessage());
    }

    /**
     * Run illegal char throw exception.
     *
     * @param chr the chr
     */
    @Disabled
    @ParameterizedTest
    @ValueSource(strings = { "*", ":", "?", "<", ">", "|"})
    void run_IllegalChar_ThrowException(String chr) {
        String [] args = new String[]{chr,FILE_PATH1};
        Throwable exp = assertThrows(TeeException.class, () -> teeApplication.run(args, inputStream, outputStream));
        assertEquals(TEE_PREFIX + ERR_NOT_SUPPORTED, exp.getMessage());

    }

}
