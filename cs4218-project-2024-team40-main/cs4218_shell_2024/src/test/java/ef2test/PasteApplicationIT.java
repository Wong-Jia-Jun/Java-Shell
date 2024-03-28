package ef2test;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.PasteException;
import sg.edu.nus.comp.cs4218.impl.app.PasteApplication;
import sg.edu.nus.comp.cs4218.impl.parser.PasteArgsParser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;


/**
 * Tests Functionality of paste application
 */
public class PasteApplicationIT { //NOPMD -suppressed ClassNamingConventions - As per Teaching Team's guidelines
    /**
     * The Paste application.
     */
    PasteApplication pasteApplication;
    /**
     * The Paste parser.
     */
    PasteArgsParser pasteParser;
    private static final String BASE_PATH = Environment.currentDirectory;
    private static final String TEST_PATH = BASE_PATH + File.separator + "pasteTestFolder";
    private static final String FILE_PATH_A = TEST_PATH + File.separator +"A.txt";
    private static final String FILE_PATH_B = TEST_PATH + File.separator + "B.txt";
    private static final String FILE_PATH_INVALID = TEST_PATH + File.separator + "hi" + File.separator + "C.txt";
    private static final String MISSING_FILE_PATH = TEST_PATH + File.separator + "nonExistent.txt";
    private static final String FOLDER1_PATH = TEST_PATH + File.separator +"folder";
    private static final String STDIN_ARG = "-";
    private static List<String> contents = new ArrayList<>();
    private static final String[] LINES1 = {"1", "2", "3", "4"};
    private static final String[] LINES2 = {"A", "B", "C", "D"};
    private static final String SINGLE_ARG = "Single Arg";
    private static final String MULTIPLE_ARG = "1" + STRING_NEWLINE + "2" + STRING_NEWLINE;
    private static final String MTL_ARG_RES = "1" + STRING_NEWLINE + "2";
    private static final String MTL_ARG_S_RES = "1\t2";
    private static final String SERIAL_ARG = "1\t2";
    private static final String SINGLE_ARG_L2 = "Single Arg\t1" + STRING_NEWLINE + "\t2" + STRING_NEWLINE + "\t3" + STRING_NEWLINE + "\t4";
    private static final String L1_EXPECTED = "1" + STRING_NEWLINE + "2" + STRING_NEWLINE + "3" + STRING_NEWLINE + "4";
    private static final String L1_SERIAL = "1\t2\t3\t4";
    private static final String SINGLE_L2_SERIAL = "Single Arg" + STRING_NEWLINE + L1_SERIAL;


    private static final String L2_EXPECTED = "A\n" + "B\n" +"C\n" +"D\n";
    private static final String L1_L2_MERGE_DEF =  "1\tA" + STRING_NEWLINE
            + "2\tB" + STRING_NEWLINE
            + "3\tC" + STRING_NEWLINE
            + "4\tD";
    private static final String L1_L2_SERIAL =  "1\t2\t3\t4" + STRING_NEWLINE + "A\tB\tC\tD";
    private static final String TEST_STR = "Test";
    private static final String BLANK_FILE = "";
    /**
     * The constant ILLEGAL_FLAG.
     */
    public static final String ILLEGAL_FLAG = "illegal option -- ";

    /**
     * Write to file.
     *
     * @param file    the file
     * @param content the content
     * @throws IOException the io exception
     */
    public static void writeToFile(Path file, String... content) throws IOException {
        contents = new ArrayList<>();
        contents.addAll(Arrays.asList(content));
        Files.write(file, contents, WRITE, TRUNCATE_EXISTING);

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
     * Sets each.
     *
     * @throws IOException the io exception
     */
    @BeforeEach
    public void setupEach() throws  IOException{
        contents = new ArrayList<>();
        this.pasteParser = new PasteArgsParser();
        this.pasteApplication = new PasteApplication(pasteParser);
        Files.deleteIfExists(Paths.get(TEST_PATH));
        Files.createDirectory(Paths.get(TEST_PATH));
        Files.createDirectory(Paths.get(FOLDER1_PATH));
        Files.createFile(Paths.get(FILE_PATH_A));
        Files.createFile(Paths.get(FILE_PATH_B));
        writeToFile(Paths.get(FILE_PATH_A), LINES1);
        writeToFile(Paths.get(FILE_PATH_B), LINES2);


    }

    /**
     * Teardown.
     */
    @AfterEach
    void teardown()  {
        Environment.currentDirectory = BASE_PATH;
        deleteDirectory(new File(TEST_PATH));
    }


    /* mergeStdin */
    /**
     * Merge file non exist file name throw exception.
     */
    @Test
    void mergeFile_NonExistFileName_ThrowException() {
        String[] files = {FILE_PATH_INVALID};
        Throwable exp = assertThrows(PasteException.class, ()-> pasteApplication.mergeFile(false, files));
        assertEquals(new PasteException(FILE_PATH_INVALID, ERR_FILE_NOT_FND).getMessage(), exp.getMessage());
    }

    /**
     * Merge file input file is dir throw exception.
     */
    @Test
    void mergeFile_InputFileIsDir_ThrowException() {
        String[] files = {FILE_PATH_A, FOLDER1_PATH};
        Throwable exp = assertThrows(PasteException.class, ()-> pasteApplication.mergeFile(false, files));
        assertEquals(new PasteException(FOLDER1_PATH, ERR_IS_DIR_FILE).getMessage(), exp.getMessage());
    }

    /**
     * Merge files invalid file string returns exception text. (Invalid for java `Path.resolve`)
     * @throws AbstractApplicationException
     */
    @Test
    void mergeFile_InvalidFileString_ReturnsExceptionText() {
        String invalidFileName = "invalid:";
        Throwable exp = assertThrows(PasteException.class, () -> pasteApplication.mergeFile(false, invalidFileName));
          assertEquals(new PasteException(invalidFileName, ERR_FILE_NOT_FND).getMessage(), exp.getMessage());
    }

    /**
     * Merge file one file arg echo stdin arg.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void mergeFile_OneFileArg_EchoStdinArg() throws AbstractApplicationException{
        String[] files = {FILE_PATH_A};
        String expected = L1_EXPECTED;
        String output = pasteApplication.mergeFile(false, files);
        assertEquals(expected, output);
    }

    /**
     * Merge file one stdin arg option sep by tab.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void mergeFile_OneStdinArgOption_SepByTab() throws AbstractApplicationException {
        String[] files = {FILE_PATH_A};
        String expected = L1_SERIAL;
        String output = pasteApplication.mergeFile(true, files);
        assertEquals(expected, output);
    }

    /**
     * Merge file multiple stdin arg correct output.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void mergeFile_MultipleStdinArg_CorrectOutput() throws AbstractApplicationException {
        String[] files = {FILE_PATH_A, FILE_PATH_B};
        String expected = L1_L2_MERGE_DEF;
        String output = pasteApplication.mergeFile(false, files);
        assertEquals(expected, output);
    }

    /**
     * Merge file multiple stdin arg option 1 file at a time.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void mergeFile_MultipleStdinArgOption_1FileAtATime() throws AbstractApplicationException {
        String[] files = {FILE_PATH_A, FILE_PATH_B};
        String expected = L1_L2_SERIAL;
        String output = pasteApplication.mergeFile(true, files);
        assertEquals(expected, output);
    }

    /**
     * Merge files and stdin invalid file string returns exception text. (Invalid for java `Path.resolve`)
     * @throws AbstractApplicationException
     */
    @Test
    void mergeFileAndStdin_InvalidFileString_ReturnsExceptionText() {
        String invalidFileName = "invalid:";
        InputStream inputStream = new ByteArrayInputStream(new byte[0]);
        Throwable exp = assertThrows(PasteException.class, () -> pasteApplication.mergeFileAndStdin(false, inputStream, invalidFileName));
        assertEquals(new PasteException(invalidFileName, ERR_FILE_NOT_FND).getMessage(), exp.getMessage());
    }

    /**
     * Merge file and stdin stdin one file same len lines sep by tab.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void mergeFileAndStdin_StdinOneFileSameLen_LinesSepByTab() throws AbstractApplicationException {
        String argument = L1_EXPECTED;
        byte[] bytes = argument.getBytes();
        InputStream inputStream = new ByteArrayInputStream(bytes);
        String[] files = {STDIN_ARG, FILE_PATH_B};
        String expected = L1_L2_MERGE_DEF;
        String actual = pasteApplication.mergeFileAndStdin(false, inputStream, files);
        assertEquals(expected,actual);


    }

    /**
     * Merge file and stdin stdin one file diff len lines sep by tab.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    // where number of lines in File > or < number of lines of stdin
    @Test
    void mergeFileAndStdin_StdinOneFileDiffLen_LinesSepByTab() throws AbstractApplicationException {
        String argument = SINGLE_ARG;
        byte[] bytes = argument.getBytes();
        InputStream inputStream = new ByteArrayInputStream(bytes);
        String[] files = {STDIN_ARG, FILE_PATH_A};
        String expected = SINGLE_ARG_L2;
        String actual = pasteApplication.mergeFileAndStdin(false, inputStream, files);
        assertEquals(expected,actual);
    }

    /**
     * Merge file and stdin stdin one file option diff len 1 file at a time.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void mergeFileAndStdin_StdinOneFileOptionDiffLen_1FileAtATime()throws AbstractApplicationException {
        String argument = SINGLE_ARG;
        byte[] bytes = argument.getBytes();
        InputStream inputStream = new ByteArrayInputStream(bytes);
        String[] files = {STDIN_ARG, FILE_PATH_A};
        String expected = SINGLE_L2_SERIAL;
        String actual = pasteApplication.mergeFileAndStdin(true, inputStream, files);
        assertEquals(expected,actual);

    }

    /**
     * Merge file and stdin stdin one file option same len 1 file at a time.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void mergeFileAndStdin_StdinOneFileOptionSameLen_1FileAtATime() throws AbstractApplicationException {
        String argument = L1_EXPECTED;
        byte[] bytes = argument.getBytes();
        InputStream inputStream = new ByteArrayInputStream(bytes);
        String[] files = {STDIN_ARG, FILE_PATH_B};
        String expected = L1_L2_SERIAL;
        String actual = pasteApplication.mergeFileAndStdin(true, inputStream, files);
        assertEquals(expected,actual);

    }

    /**
     * Run invalid input file throw exception.
     */
    @Test
    void run_InvalidInputFile_ThrowException() {
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] args = {MISSING_FILE_PATH, FILE_PATH_B};
        assertThrows(PasteException.class, ()-> pasteApplication.run(args, System.in, outputStream));
    }

    /**
     * Run input file is dir throw exception.
     */
    @Test
    void run_InputFileisDir_ThrowException() {
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] args = {FOLDER1_PATH, FILE_PATH_B};
        assertThrows(PasteException.class, ()-> pasteApplication.run(args, System.in, outputStream));
    }

    /**
     * Run stdin merge lines correct.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void run_Stdin_MergeLinesCorrect() throws AbstractApplicationException{
        String argument = MULTIPLE_ARG;
        byte[] bytes = argument.getBytes();
        InputStream inputStream = new ByteArrayInputStream(bytes);
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] args = {};
        pasteApplication.run(args,inputStream, outputStream);
        assertEquals(MULTIPLE_ARG, outputStream.toString());
    }

    /**
     * Run valid files merge lines correct.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void run_ValidFiles_MergeLinesCorrect() throws  AbstractApplicationException{
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] args = {FILE_PATH_A, FILE_PATH_B};
        pasteApplication.run(args,System.in, outputStream);
        assertEquals(L1_L2_MERGE_DEF + STRING_NEWLINE, outputStream.toString());
    }

    /**
     * Run valid files and stdin merge lines correct.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void run_ValidFilesAndStdin_MergeLinesCorrect() throws  AbstractApplicationException {
        OutputStream outputStream = new ByteArrayOutputStream();
        String argument = SINGLE_ARG;
        byte[] bytes = argument.getBytes();
        InputStream inputStream = new ByteArrayInputStream(bytes);
        String[] args = {"-", FILE_PATH_A};
        pasteApplication.run(args,inputStream, outputStream);
        assertEquals(SINGLE_ARG_L2 + STRING_NEWLINE, outputStream.toString());
    }

    /**
     * Run stdin option merge lines correct.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void run_StdinOption_MergeLinesCorrect() throws AbstractApplicationException {
        String argument = MULTIPLE_ARG;
        byte[] bytes = argument.getBytes();
        InputStream inputStream = new ByteArrayInputStream(bytes);
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] args = {"-s"};
        pasteApplication.run(args,inputStream, outputStream);
        assertEquals(SERIAL_ARG + STRING_NEWLINE, outputStream.toString());
    }

    /**
     * Run valid files option merge lines correct.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void run_ValidFilesOption_MergeLinesCorrect() throws AbstractApplicationException {
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] args = {"-s", FILE_PATH_A, FILE_PATH_B};
        pasteApplication.run(args,System.in, outputStream);
        assertEquals(L1_L2_SERIAL + STRING_NEWLINE, outputStream.toString());
    }

    /**
     * Run valid files and stdin option merge lines correct.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void run_ValidFilesAndStdinOption_MergeLinesCorrect() throws AbstractApplicationException{
        OutputStream outputStream = new ByteArrayOutputStream();
        String argument = SINGLE_ARG;
        byte[] bytes = argument.getBytes();
        InputStream inputStream = new ByteArrayInputStream(bytes);
        String[] args = {"-s", "-", FILE_PATH_A};
        pasteApplication.run(args,inputStream, outputStream);
        assertEquals(SINGLE_L2_SERIAL + STRING_NEWLINE, outputStream.toString());
    }

    /**
     * Run invalid flag throw exception.
     *
     * @param flag the flag
     */
    @ParameterizedTest
    @ValueSource(strings = {"-a", "-b", "-c", "-d", "-e", "-f", "-g", "-h"})
    void run_InvalidFlag_ThrowException(String flag) {
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] args = {flag, MISSING_FILE_PATH, FILE_PATH_B};
        assertThrows(PasteException.class, ()-> pasteApplication.run(args, System.in, outputStream));
    }

}
