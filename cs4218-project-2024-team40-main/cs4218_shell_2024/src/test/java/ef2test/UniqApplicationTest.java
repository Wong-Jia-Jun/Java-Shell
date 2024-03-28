package ef2test;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.exception.UniqueException;
import sg.edu.nus.comp.cs4218.impl.app.UniqApplication;
import sg.edu.nus.comp.cs4218.impl.parser.UniqArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.nio.file.Files.readString;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.nio.file.StandardOpenOption.CREATE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;


/**
 * Tests Functionality of uniq application
 */
//whoever takes over this rmbr to mock .parse method
public class UniqApplicationTest {
    /**
     * The Uniq application.
     */
    UniqApplication uniqApplication;
    /**
     * The Uniq args parser.
     */
    UniqArgsParser uniqArgsParser;

    private static final String BASE_PATH = Environment.currentDirectory;
    private static final String TEST_RESOURCE_DIR = "src/test/resources/app";
    private static final String TEST_PATH = BASE_PATH + File.separator + TEST_RESOURCE_DIR + File.separator + "UniqApplicationTest";
    private static final String FILE_A = "A.txt";
    private static final String FILE_PATH_A = TEST_PATH + File.separator + FILE_A;
    private static final String FILE_B = "B.txt";
    private static final String FILE_PATH_B = TEST_PATH + File.separator + FILE_B;
    private static final String FILE_NOT_EXIST = "notExist.txt";
    private static final String FILE_PATH_NOEXST = TEST_PATH + File.separator + FILE_NOT_EXIST;

    private static final String MISSING_FILE = "nonExistent.txt";
    private static final String MISSING_FILE_PATH = TEST_PATH + File.separator + MISSING_FILE;
    private static final String FILE_EMPTY = "empty.txt";
    private static final String FILE_PATH_EMPTY = TEST_PATH + File.separator + FILE_EMPTY;
    private static final String FILE_NO_PERM = "noWritePerm.txt";
    private static final String FILE_NO_PERM_PATH = TEST_PATH + File.separator + FILE_NO_PERM;
    private static final String FILE_NO_READ_PERM = "noReadPerm.txt";
    private static final String FILE_PATH_NOREAD = TEST_PATH + File.separator + FILE_NO_READ_PERM;
    private static final String FOLDER1 = "folder";
    private static final String FOLDER1_PATH = TEST_PATH + File.separator +"folder";
    private static List<String> contents = new ArrayList<>();
    public static final String DUPLICATE2 = "duplicate2";
    public static final String NONDUPLICATE = "nonduplicate";
    private static final String[] LINES1 = {"duplicate1", "duplicate1", DUPLICATE2, DUPLICATE2, NONDUPLICATE};
    private static final String[] LINES2 = {"A", "B", "A", "B", "C"};
    private static final List<String> LIST_LINES_1 = Arrays.asList(LINES1);
    /**
     * The constant DUPLICATE_1.
     */
    public static final String DUPLICATE_1 = "duplicate1\n";
    /**
     * The constant DUPLICATE_2.
     */
    public static final String DUPLICATE_2 = "duplicate2\n";
    private static final String STDIN_1 = DUPLICATE_1 + DUPLICATE_1 + DUPLICATE_2 + DUPLICATE_2 + NONDUPLICATE;

    /**
     * The constant STDIN_A.
     */
    public static final String STDIN_A = "A\n";
    /**
     * The constant STDIN_B.
     */
    public static final String STDIN_B = "B\n";

    private static final String EXPECTED_L1 = DUPLICATE_1 + DUPLICATE_2 + NONDUPLICATE;
    private static final String EXPECTED_L1_COUNT = "\t2 duplicate1\n" + "\t2 duplicate2\n" + "\t1 nonduplicate";
    private static final String EXPECTED_L1_DUP = DUPLICATE_1 + DUPLICATE2;
    private static final String EXPECTED_L1_DUP_ALL = DUPLICATE_1 + DUPLICATE_1 + DUPLICATE_2 + DUPLICATE2; //NOPMD - suppressed LongVariable - as short as possible
    private static final String EXPECTED_L2 = STDIN_A + STDIN_B + STDIN_A + STDIN_B + "C\n";
    private static final String EXPECTED_L2_COUNT = "\t1 A\n" + "\t1 B\n" +"\t1 A\n" +"\t1 B\n"+ "\t1 C\n";
    private static final String EXPECTED_L2_DUP = "";
    private static final String EXPECTED_L2_DUP_ALL = ""; //NOPMD - suppressed LongVariable - as short as possible
    private static final String EXPECTED_L1_DUP_AND_COUNT = "\t2 duplicate1\n" + "\t2 duplicate2"; //NOPMD - suppressed LongVariable - as short as possible
    private static final String UNIQ_PREFIX = "uniq: ";
    private static final String NEW_LINE = "\n";
    private static final String INCOMPAT_FLAG_MSG = "printing all duplicated lines and repeat counts is meaningless";
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
     * normalzie line ending
     *
     * @param input    the input string
     * @return string that has separators replaced to \n
     */
    private String normalizeLineEndings(String input) {
        return input.replace("\n", "\r\n");
    }
    private String normalizeLineEndings2(String input) {
        return input.replace("\r\n", "\n");
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

    @BeforeAll
    static void setupBeforeAll() {
        Environment.currentDirectory = TEST_PATH;
    }

    /**
     * Sets each.
     *
     * @throws IOException the io exception
     */
    @BeforeEach
    public void setupEach() throws  IOException{
        contents = new ArrayList<>();
        this.uniqArgsParser = mock(UniqArgsParser.class);
        this.uniqApplication = new UniqApplication(uniqArgsParser);
        Files.deleteIfExists(Paths.get(TEST_PATH));
        Files.createDirectory(Paths.get(TEST_PATH));
        Files.createDirectory(Paths.get(FOLDER1_PATH));
        Files.createFile(Paths.get(FILE_PATH_A));
        Files.createFile(Paths.get(FILE_PATH_B));
        Files.createFile(Paths.get(FILE_PATH_EMPTY));
        Files.createFile(Paths.get(FILE_NO_PERM_PATH));
        Paths.get(FILE_NO_PERM_PATH).toFile().setReadOnly();
        Path filePath = Paths.get(FILE_PATH_NOREAD);
        Files.createFile(filePath);
        boolean result = Paths.get(FILE_NO_PERM_PATH).toFile().setReadable(false);
        writeToFile(Paths.get(FILE_PATH_A), LINES1);
        writeToFile(Paths.get(FILE_PATH_B), LINES2);


    }

    /**
     * Teardown.
     */
    @AfterEach
    void teardown()  {
        deleteDirectory(new File(TEST_PATH));
    }
    @AfterAll
    static void teardownAll(){
        Environment.currentDirectory = BASE_PATH;
    }

    /**
     * Uniq to file, input file is dir should throw exception.
     */
    @Test
    void uniqToFile_FileDir_ThrowException() {
        try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
            mockedStatic.when(() -> IOUtils.resolveFilePath(FOLDER1))
                    .thenReturn(Paths.get(FOLDER1_PATH));
            Throwable exp = assertThrows(UniqueException.class, () ->uniqApplication.uniqToFile(STDIN_1, FOLDER1));
            String expected = UNIQ_PREFIX + ERR_IS_DIR;
            assertEquals(expected, exp.getMessage());
        }
    }

    /**
     * Uniq to file, input file no write perm should throw exception.
     */
    @Test
    void uniqToFile_FileNoPerm_ThrowException(){
        try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
            mockedStatic.when(() -> IOUtils.resolveFilePath(FILE_NO_PERM))
                    .thenReturn(Paths.get(FILE_NO_PERM_PATH));
            Throwable exp = assertThrows(UniqueException.class, () ->uniqApplication.uniqToFile(STDIN_1, FILE_NO_PERM));
            String expected = UNIQ_PREFIX + ERR_NO_PERM;
            assertEquals(expected, exp.getMessage());
        }
    }

    /**
     * Uniq to file, valid existing file Correct behaviour.
     * @throws UniqueException, IOException
     */
    @Test
    void uniqToFile_ValidFile_WriteCorrectFile() throws UniqueException, IOException {
        try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
            mockedStatic.when(() -> IOUtils.resolveFilePath(FILE_A))
                    .thenReturn(Paths.get(FILE_PATH_A));
            uniqApplication.uniqToFile(STDIN_1, FILE_A);
            String contentFile = readString(Path.of(FILE_PATH_A));
            assertEquals(STDIN_1, contentFile);
        }
    }

    /**
     * Uniq to file, non existing  file Correct behaviour.
     * @throws UniqueException, IOException
     */
    @Test
    void uniqToFile_ValidNonExistingFile_WriteCorrectFile() throws UniqueException, IOException {
        try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
            mockedStatic.when(() -> IOUtils.resolveFilePath(FILE_NOT_EXIST))
                    .thenReturn(Paths.get(FILE_PATH_NOEXST));
            uniqApplication.uniqToFile(STDIN_1, FILE_NOT_EXIST);
            assert (Files.exists(Path.of(FILE_PATH_NOEXST)));
            String contentFile = readString(Path.of(FILE_PATH_NOEXST));
            assertEquals(STDIN_1, contentFile);
        }
    }

    /**
     * Uniq to file, mock the IO exception when writing to file to simulate real life situation.
     * @throws UniqueException, IOException
     */
    @Test
    void uniqToFile_ErrorWriteFile_ThrowsException()   {
        try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class);
             MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedStatic.when(() -> IOUtils.resolveFilePath(FILE_NOT_EXIST))
                    .thenReturn(Paths.get(FILE_PATH_NOEXST));
            mockedFiles.when(() -> Files.write(Paths.get(FILE_PATH_NOEXST), STDIN_1.getBytes(), CREATE, WRITE, TRUNCATE_EXISTING)).thenThrow(IOException.class);
            Throwable exp = assertThrows(UniqueException.class, () ->uniqApplication.uniqToFile(STDIN_1, FILE_NOT_EXIST));
            String expected = UNIQ_PREFIX + ERR_WRITING_FILE;
            assertEquals(expected , exp.getMessage());
        }
    }

    /**
     * Uniqinput Alloption true should throw exception.
     */
    @Test
    void uniqInput_AllOption_ThrowException() throws  AbstractApplicationException{
        String[] args = {STDIN_1};
        List<String> inputlines = Arrays.asList(args);
        Throwable exp = assertThrows(UniqueException.class, () -> uniqApplication.uniqInput(true,true,true, inputlines, null));
        String expected = UNIQ_PREFIX + INCOMPAT_FLAG_MSG;
        assertEquals(expected, exp.getMessage());
    }

    /**
     * Uniqinput iscount and AllDup option true should throw exception.
     */
    @Test
    void uniqInput_CountAllDupOption_ThrowException() throws  AbstractApplicationException{
        String[] args = {STDIN_1};
        List<String> inputlines = Arrays.asList(args);
        Throwable exp = assertThrows(UniqueException.class, () -> uniqApplication.uniqInput(true,false,true, inputlines, null));
        String expected = UNIQ_PREFIX + INCOMPAT_FLAG_MSG;
        assertEquals(expected, exp.getMessage());
    }

    /**
     * Uniqinput no option should behave as expected with duplicate lines printed only
     */
    @Test
    void uniqInput_NoOption_DefaultBehaviour() throws  AbstractApplicationException{
        List<String> inputlines = Arrays.asList(LINES1);
        String actual = uniqApplication.uniqInput(false,false,false, inputlines, null);
        String expected = EXPECTED_L1;
        assertEquals(expected, normalizeLineEndings2(actual));
    }

    /**
     * Uniq from file null input file throw exception.
     */
    @Test
   void uniqFromFile_nullInputFile_ThrowException(){
        Throwable exp = assertThrows(UniqueException.class ,() -> uniqApplication.uniqFromFile(false, false, false, null,null ));
        String expected = UNIQ_PREFIX + ERR_NULL_ARGS;
        assertEquals(expected, exp.getMessage());
    }

    /**
     * Uniq from file null is count throw exception.
     */
    @Test
    void uniqFromFile_nullIsCount_ThrowException(){
        Throwable exp = assertThrows(UniqueException.class ,() -> uniqApplication.uniqFromFile(null, false, false, FILE_PATH_A,null ));
        String expected = UNIQ_PREFIX + ERR_NULL_ARGS;
        assertEquals(expected, exp.getMessage());

    }

    /**
     * Uniq from file null is duplicate throw exception.
     */
    @Test
    void uniqFromFile_nullIsDuplicate_ThrowException(){
        Throwable exp = assertThrows(UniqueException.class ,() -> uniqApplication.uniqFromFile(false, null, false, FILE_PATH_A,null ));
        String expected = UNIQ_PREFIX + ERR_NULL_ARGS;
        assertEquals(expected, exp.getMessage());


    }

    /**
     * Uniq from file null is duplicate all throw exception.
     */
    @Test
    void uniqFromFile_nullIsDuplicateAll_ThrowException(){
        Throwable exp = assertThrows(UniqueException.class ,() -> uniqApplication.uniqFromFile(false, false, null, FILE_PATH_A,null ));
        String expected = UNIQ_PREFIX + ERR_NULL_ARGS;
        assertEquals(expected, exp.getMessage());


    }

    /**
     * Uniq from file non existent throw exception.
     */
    @Test
    void uniqFromFile_NonExistFile_ThrowException(){
        try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
            mockedStatic.when(() -> IOUtils.resolveFilePath(MISSING_FILE))
                    .thenReturn(Paths.get(MISSING_FILE_PATH));
            Throwable exp = assertThrows(UniqueException.class, () -> uniqApplication.uniqFromFile(false, false, false, MISSING_FILE, null));
            String expected = UNIQ_PREFIX + MISSING_FILE + ": " + ERR_FILE_NOT_FND;
            assertEquals(expected, exp.getMessage());
        }
    }

    /**
     * Uniq from file is DIR throw exception.
     */
    @Test
    void uniqFromFile_FileIsDIR_ThrowException(){
        try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
            mockedStatic.when(() -> IOUtils.resolveFilePath(FOLDER1))
                    .thenReturn(Paths.get(FOLDER1_PATH));
            Throwable exp = assertThrows(UniqueException.class, () -> uniqApplication.uniqFromFile(false, false, false, FOLDER1, null));
            String expected = UNIQ_PREFIX + String.format("error reading '%s' %s", FOLDER1, ERR_IS_DIR_FILE);
            assertEquals(expected, exp.getMessage());
        }
    }

    /**
     * Uniq from file error closing stream throw exception.
     */
    @Test
    void uniqFromFile_ErrorClosingStream_ThrowException() throws AbstractApplicationException{
        try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
            mockedStatic.when(() -> IOUtils.resolveFilePath(FILE_A))
                    .thenReturn(Paths.get(FILE_PATH_A));
            mockedStatic.when(() -> IOUtils.closeInputStream(any()))
                    .thenThrow(ShellException.class);
            String exp =uniqApplication.uniqFromFile(false, false, false, FILE_A, null);
            String expected = UNIQ_PREFIX + ERR_CLOSE_STREAMS;
            assertEquals(expected, exp);
        }

    }

    /**
     * Uniq from file error reading line from input throw exception.
     */
    @Test
    void uniqFromFile_ErrorReadLine_ThrowException() throws AbstractApplicationException{
        try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
            mockedStatic.when(() -> IOUtils.resolveFilePath(FILE_A))
                    .thenReturn(Paths.get(FILE_PATH_A));
            mockedStatic.when(() -> IOUtils.getLinesFromInputStream(any()))
                    .thenThrow(IOException.class);
            String exp = uniqApplication.uniqFromFile(false, false, false, FILE_A, null);
            String expected = UNIQ_PREFIX + ERR_IO_EXCEPTION;
            assertEquals(expected, exp);
        }
    }

    /**
     * Uniq from file No Read Perm throw exception.
     */
    @Test
    void uniqFromFile_FileNoReadPerm_ThrowException() throws IOException {
        Path mockedPath = mock(Path.class);
        File mockedFile = mock(File.class);
        try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class);
              ) {
            mockedStatic.when(() -> IOUtils.resolveFilePath(FILE_NO_READ_PERM))
                    .thenReturn(mockedPath);
            when(mockedPath.toFile()).thenReturn(mockedFile);
            when(mockedFile.exists()).thenReturn(true);
            when(mockedFile.isDirectory()).thenReturn(false);
            when(mockedFile.canRead()).thenReturn(false);
            Throwable exp = assertThrows(UniqueException.class, () -> uniqApplication.uniqFromFile(false, false, false, FILE_NO_READ_PERM, null));
            String expected = UNIQ_PREFIX + ERR_NO_PERM;
            assertEquals(expected, exp.getMessage());
        }
    }

    /**
     * Uniq from empty file should return empty string.
     * @throws UniqueException, IOException
     * **/

    @Test
    void uniqFromFile_EmptyFile_ReturnsEmpty() throws IOException, AbstractApplicationException {
        try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
            mockedStatic.when(() -> IOUtils.resolveFilePath(FILE_EMPTY))
                    .thenReturn(Paths.get(FILE_PATH_EMPTY));
            mockedStatic.when(() -> IOUtils.openInputStream(FILE_EMPTY))
                    .thenReturn(new FileInputStream(FILE_PATH_EMPTY));
            mockedStatic.when(() -> IOUtils.getLinesFromInputStream(any(InputStream.class)))
                    .thenReturn(new ArrayList<>());

            String result = uniqApplication.uniqFromFile(false, false, false, FILE_EMPTY, null);
            assertTrue(result.isBlank());

        }
    }
    /**
     * Uniq from file no option return correct.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void uniqFromFile_NoOptionValidFile_ReturnCorrect() throws AbstractApplicationException, IOException{
        try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
            mockedStatic.when(() -> IOUtils.resolveFilePath(FILE_A))
                    .thenReturn(Paths.get(FILE_PATH_A));
            mockedStatic.when(() -> IOUtils.openInputStream(FILE_A))
                    .thenReturn(new FileInputStream(FILE_PATH_A));
            mockedStatic.when(() -> IOUtils.getLinesFromInputStream(any(InputStream.class)))
                    .thenReturn(LIST_LINES_1);
            String actual = uniqApplication.uniqFromFile(false, false, false, FILE_A, null);
            assertEquals(EXPECTED_L1, normalizeLineEndings2(actual));
        }
    }

    /**
     * Uniq from file is count return correct.
     *
     * @throws AbstractApplicationException the abstract application exception, IOException
     */
    @Test
    void uniqFromFile_IsCount_ReturnCorrect() throws AbstractApplicationException, IOException{
        try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
            mockedStatic.when(() -> IOUtils.resolveFilePath(FILE_A))
                    .thenReturn(Paths.get(FILE_PATH_A));
            mockedStatic.when(() -> IOUtils.openInputStream(FILE_A))
                    .thenReturn(new FileInputStream(FILE_PATH_A));
            mockedStatic.when(() -> IOUtils.getLinesFromInputStream(any(InputStream.class)))
                    .thenReturn(LIST_LINES_1);
            String actual = uniqApplication.uniqFromFile(true, false, false, FILE_A, null);
            assertEquals(EXPECTED_L1_COUNT, normalizeLineEndings2(actual));
        }
    }

    /**
     * Uniq from file is duplicate return correct.
     *
     * @throws AbstractApplicationException the abstract application exception, IOException
     */
    @Test
    void uniqFromFile_IsDuplicate_ReturnCorrect() throws AbstractApplicationException, IOException {
        try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
            mockedStatic.when(() -> IOUtils.resolveFilePath(FILE_A))
                    .thenReturn(Paths.get(FILE_PATH_A));
            mockedStatic.when(() -> IOUtils.openInputStream(FILE_A))
                    .thenReturn(new FileInputStream(FILE_PATH_A));
            mockedStatic.when(() -> IOUtils.getLinesFromInputStream(any(InputStream.class)))
                    .thenReturn(LIST_LINES_1);
            String actual = uniqApplication.uniqFromFile(false, true, false, FILE_A, null);
            assertEquals(EXPECTED_L1_DUP, normalizeLineEndings2(actual));
        }

    }

    /**
     * Uniq from file is duplicate all return correct.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void uniqFromFile_IsDuplicateAll_ReturnCorrect() throws AbstractApplicationException, IOException{
        try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
            mockedStatic.when(() -> IOUtils.resolveFilePath(FILE_A))
                    .thenReturn(Paths.get(FILE_PATH_A));
            mockedStatic.when(() -> IOUtils.openInputStream(FILE_A))
                    .thenReturn(new FileInputStream(FILE_PATH_A));
            mockedStatic.when(() -> IOUtils.getLinesFromInputStream(any(InputStream.class)))
                    .thenReturn(LIST_LINES_1);
            String actual = uniqApplication.uniqFromFile(false, false, true, FILE_A, null);
            assertEquals(EXPECTED_L1_DUP_ALL, normalizeLineEndings2(actual));
        }
    }

    /**
     * Uniq from file is duplicate n duplicate all return correct.
     *
     * @throws AbstractApplicationException the abstract application exception, IOException
     */
    @Test
    void uniqFromFile_IsDuplicateNDuplicateAll_ReturnCorrect() throws AbstractApplicationException, IOException{
        try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
            mockedStatic.when(() -> IOUtils.resolveFilePath(FILE_A))
                    .thenReturn(Paths.get(FILE_PATH_A));
            mockedStatic.when(() -> IOUtils.openInputStream(FILE_A))
                    .thenReturn(new FileInputStream(FILE_PATH_A));
            mockedStatic.when(() -> IOUtils.getLinesFromInputStream(any(InputStream.class)))
                    .thenReturn(LIST_LINES_1);
            String actual = uniqApplication.uniqFromFile(false, true, true, FILE_A, null);
            assertEquals(EXPECTED_L1_DUP_ALL, normalizeLineEndings2(actual));
        }
    }

    /**
     * Uniq from file is duplicate n count return correct.
     *
     * @throws AbstractApplicationException the abstract application exception,IOException
     */
    @Test
    void uniqFromFile_IsDuplicateNCount_ReturnCorrect() throws AbstractApplicationException, IOException{
        try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
            mockedStatic.when(() -> IOUtils.resolveFilePath(FILE_A))
                    .thenReturn(Paths.get(FILE_PATH_A));
            mockedStatic.when(() -> IOUtils.openInputStream(FILE_A))
                    .thenReturn(new FileInputStream(FILE_PATH_A));
            mockedStatic.when(() -> IOUtils.getLinesFromInputStream(any(InputStream.class)))
                    .thenReturn(LIST_LINES_1);
            String actual = uniqApplication.uniqFromFile(true, true, false, FILE_A, null);
            assertEquals(EXPECTED_L1_DUP_AND_COUNT, normalizeLineEndings2(actual));
        }
    }

    /**
     * Uniq from file is count n duplicate all throw exception.
     * @throws IOException
     */
    @Test
    void uniqFromFile_IsCountNDuplicateAll_ThrowException() throws  IOException{
        try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
            mockedStatic.when(() -> IOUtils.resolveFilePath(FILE_A))
                    .thenReturn(Paths.get(FILE_PATH_A));
            mockedStatic.when(() -> IOUtils.openInputStream(FILE_A))
                    .thenReturn(new FileInputStream(FILE_PATH_A));
            mockedStatic.when(() -> IOUtils.getLinesFromInputStream(any(InputStream.class)))
                    .thenReturn(LIST_LINES_1);
            Throwable exp = assertThrows(UniqueException.class, () -> uniqApplication.uniqFromFile(true, false, true, FILE_A, null));
            String expected = UNIQ_PREFIX + INCOMPAT_FLAG_MSG;
            assertEquals(expected, exp.getMessage());
        }
    }

    /**
     * Uniq from file all option throw exception.
     * @throws IOException
     */
    @Test
    void uniqFromFile_AllOption_ThrowException() throws IOException{
        try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
            mockedStatic.when(() -> IOUtils.resolveFilePath(FILE_A))
                    .thenReturn(Paths.get(FILE_PATH_A));
            mockedStatic.when(() -> IOUtils.openInputStream(FILE_A))
                    .thenReturn(new FileInputStream(FILE_PATH_A));
            mockedStatic.when(() -> IOUtils.getLinesFromInputStream(any(InputStream.class)))
                    .thenReturn(LIST_LINES_1);
            Throwable exp = assertThrows(UniqueException.class, () -> uniqApplication.uniqFromFile(true, true, true, FILE_A, null));
            String expected = UNIQ_PREFIX + INCOMPAT_FLAG_MSG;
            assertEquals(expected, exp.getMessage());
        }
    }

    /**
     * Uniq from file no option to output file should hae default behaviour.
     * @throws IOException
     */
    @Test
    void uniqFromFile_ToOutputFile_WriteCorrect() throws IOException, AbstractApplicationException{
        try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
            mockedStatic.when(() -> IOUtils.resolveFilePath(FILE_A))
                    .thenReturn(Paths.get(FILE_PATH_A));
            mockedStatic.when(() -> IOUtils.resolveFilePath(FILE_B))
                    .thenReturn(Paths.get(FILE_PATH_B));
            mockedStatic.when(() -> IOUtils.openInputStream(FILE_A))
                    .thenReturn(new FileInputStream(FILE_PATH_A));
            mockedStatic.when(() -> IOUtils.getLinesFromInputStream(any(InputStream.class)))
                    .thenReturn(LIST_LINES_1);
            String output = uniqApplication.uniqFromFile(false, false, false, FILE_A, FILE_B);
            String actual = readString(Path.of(FILE_PATH_B));
            assertEquals(EXPECTED_L1, normalizeLineEndings2(actual));
            assertEquals(EXPECTED_L1, normalizeLineEndings2(output));
        }
    }

    /**
     * Uniq from stdin null input file throw exception.
     */
    @Test
    void uniqFromStdin_nullInputFile_ThrowException(){
        Throwable exp = assertThrows(UniqueException.class ,() -> uniqApplication.uniqFromStdin(false, false, false, null,null ));
        String actual = UNIQ_PREFIX + ERR_NULL_ARGS;
        assertEquals(actual, exp.getMessage());
    }

    /**
     * Uniq from stdin null is count throw exception.
     */
    @Test
    void uniqFromStdin_nullIsCount_ThrowException(){
        String argument = STDIN_1;
        byte[] bytes = argument.getBytes();
        InputStream inputStream = new ByteArrayInputStream(bytes);
        Throwable exp = assertThrows(UniqueException.class ,() -> uniqApplication.uniqFromStdin(null, false, false, inputStream,null ));
        String actual = UNIQ_PREFIX + ERR_NULL_ARGS;
        assertEquals(actual, exp.getMessage());
    }

    /**
     * Uniq from stdin null is duplicate throw exception.
     */
    @Test
    void uniqFromStdin_nullIsDuplicate_ThrowException(){

        String argument = STDIN_1;
        byte[] bytes = argument.getBytes();
        InputStream inputStream = new ByteArrayInputStream(bytes);
        Throwable exp = assertThrows(UniqueException.class, () -> uniqApplication.uniqFromStdin(false, null, false, inputStream, null));
        String actual = UNIQ_PREFIX + ERR_NULL_ARGS;
        assertEquals(actual, exp.getMessage());

    }

    /**
     * Uniq from stdin null is duplicate all throw exception.
     */
    @Test
    void uniqFromStdin_nullIsDuplicateAll_ThrowException(){
        String argument = STDIN_1;
        byte[] bytes = argument.getBytes();
        InputStream inputStream = new ByteArrayInputStream(bytes);
        Throwable exp = assertThrows(UniqueException.class ,() -> uniqApplication.uniqFromStdin(false, false, null, inputStream,null ));
        String actual = UNIQ_PREFIX + ERR_NULL_ARGS;
        assertEquals(actual, exp.getMessage());
    }

    /**
     * Uniq from stdin no option return correct.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void uniqFromStdin_NoOption_ReturnCorrect() throws AbstractApplicationException{
        try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
            mockedStatic.when(() -> IOUtils.getLinesFromInputStream(any(InputStream.class)))
                    .thenReturn(LIST_LINES_1);
            String argument = STDIN_1;
            byte[] bytes = argument.getBytes();
            InputStream inputStream = new ByteArrayInputStream(bytes);
            String actual = uniqApplication.uniqFromStdin(false, false, false, inputStream, null);
            assertEquals(EXPECTED_L1, normalizeLineEndings2(actual));
        }
    }
    /**
     * Error getting lines from input.
     *
     */
    @Test
    void uniqFromStdin_IOErrorWhenGetLineFromInput_ThrowException() throws AbstractApplicationException{
        try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
            mockedStatic.when(() -> IOUtils.getLinesFromInputStream(any(InputStream.class)))
                    .thenThrow(IOException.class);
            String argument = STDIN_1;
            byte[] bytes = argument.getBytes();
            InputStream inputStream = new ByteArrayInputStream(bytes);
            String exp = uniqApplication.uniqFromStdin(false, false, false, inputStream, null);
            assertEquals(UNIQ_PREFIX + ERR_IO_EXCEPTION, exp);
        }
    }

    /**
     * Uniq from stdin is count return correct.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void uniqFromStdin_IsCount_ReturnCorrect() throws AbstractApplicationException{
        try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
            mockedStatic.when(() -> IOUtils.getLinesFromInputStream(any(InputStream.class)))
                    .thenReturn(LIST_LINES_1);
            String argument = STDIN_1;
            byte[] bytes = argument.getBytes();
            InputStream inputStream = new ByteArrayInputStream(bytes);
            String actual = uniqApplication.uniqFromStdin(true, false, false, inputStream, null);
            assertEquals(EXPECTED_L1_COUNT, normalizeLineEndings2(actual));
        }
    }

    /**
     * Uniq from stdin is duplicate return correct.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void uniqFromStdin_IsDuplicate_ReturnCorrect() throws AbstractApplicationException{
        try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
            mockedStatic.when(() -> IOUtils.getLinesFromInputStream(any(InputStream.class)))
                    .thenReturn(LIST_LINES_1);
            String argument = STDIN_1;
            byte[] bytes = argument.getBytes();
            InputStream inputStream = new ByteArrayInputStream(bytes);
            String actual = uniqApplication.uniqFromStdin(false, true, false, inputStream, null);
            assertEquals(EXPECTED_L1_DUP, normalizeLineEndings2(actual));
        }

    }

    /**
     * Uniq from stdin is duplicate all return correct.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void uniqFromStdin_IsDuplicateAll_ReturnCorrect() throws AbstractApplicationException{
        try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
            mockedStatic.when(() -> IOUtils.getLinesFromInputStream(any(InputStream.class)))
                    .thenReturn(LIST_LINES_1);
            String argument = STDIN_1;
            byte[] bytes = argument.getBytes();
            InputStream inputStream = new ByteArrayInputStream(bytes);
            String actual = uniqApplication.uniqFromStdin(false, false, true, inputStream, null);
            assertEquals(EXPECTED_L1_DUP_ALL, normalizeLineEndings2(actual));
        }
    }

    /**
     * Uniq from stdin is duplicate n duplicate all return correct.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void uniqFromStdin_IsDuplicateNDuplicateAll_ReturnCorrect() throws AbstractApplicationException {
        try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
            mockedStatic.when(() -> IOUtils.getLinesFromInputStream(any(InputStream.class)))
                    .thenReturn(LIST_LINES_1);
            String argument = STDIN_1;
            byte[] bytes = argument.getBytes();
            InputStream inputStream = new ByteArrayInputStream(bytes);
            String actual = uniqApplication.uniqFromStdin(false, true, true, inputStream, null);
            assertEquals(EXPECTED_L1_DUP_ALL, normalizeLineEndings2(actual));
        }
    }

    /**
     * Uniq from stdin is duplicate n count return correct.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void uniqFromStdin_IsDuplicateNCount_ReturnCorrect() throws AbstractApplicationException {
        try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
            mockedStatic.when(() -> IOUtils.getLinesFromInputStream(any(InputStream.class)))
                    .thenReturn(LIST_LINES_1);
            String argument = STDIN_1;
            byte[] bytes = argument.getBytes();
            InputStream inputStream = new ByteArrayInputStream(bytes);
            String actual = uniqApplication.uniqFromStdin(true, true, false, inputStream, null);
            assertEquals(EXPECTED_L1_DUP_AND_COUNT, normalizeLineEndings2(actual));
        }
    }

    /**
     * Uniq from stdin is count n duplicate all throw exception.
     */
    @Test
    void uniqFromStdin_IsCountNDuplicateAll_ThrowException() {
        String argument = STDIN_1;
        byte[] bytes = argument.getBytes();
        InputStream inputStream = new ByteArrayInputStream(bytes);
        Throwable exp = assertThrows(UniqueException.class, () -> uniqApplication.uniqFromStdin(true,false,true, inputStream, null));
        String expected = UNIQ_PREFIX + INCOMPAT_FLAG_MSG;
        assertEquals(expected, exp.getMessage());
    }

    /**
     * Uniq from stdin all option throw exception.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void uniqFromStdin_AllOption_ThrowException() throws AbstractApplicationException{
        String argument = STDIN_1;
        byte[] bytes = argument.getBytes();
        InputStream inputStream = new ByteArrayInputStream(bytes);
        Throwable exp = assertThrows(UniqueException.class, () -> uniqApplication.uniqFromStdin(true,true,true, inputStream, null));
        String expected = UNIQ_PREFIX + INCOMPAT_FLAG_MSG;
        assertEquals(expected, exp.getMessage());
    }
    /**
     * Uniq from stdin all option to file return correcct output
     *
     * @throws AbstractApplicationException the abstract application exception, IOException
     */
    @Test
    void uniqFromStdin_NoOptionToFile_ReturnCorrect() throws AbstractApplicationException, IOException{
        try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
            mockedStatic.when(() -> IOUtils.resolveFilePath(FILE_B)).thenReturn(Paths.get(FILE_PATH_B));
            mockedStatic.when(() -> IOUtils.getLinesFromInputStream(any(InputStream.class)))
                    .thenReturn(LIST_LINES_1);
            String argument = STDIN_1;
            byte[] bytes = argument.getBytes();
            InputStream inputStream = new ByteArrayInputStream(bytes);
            String actual = uniqApplication.uniqFromStdin(false, false, false, inputStream, FILE_B);
            String fileContent = readString(Path.of(FILE_PATH_B));
            assertEquals(EXPECTED_L1, normalizeLineEndings2(actual));
            assertEquals(EXPECTED_L1, normalizeLineEndings2(fileContent));
        }
    }
    @Test
    void uniqInput_CountNAllRepeat_ThrowsException() {
        Throwable exp = assertThrows(UniqueException.class, () ->uniqApplication.uniqInput(true,false,true, LIST_LINES_1, null));
        String expected = UNIQ_PREFIX + INCOMPAT_FLAG_MSG;
        assertEquals(expected, exp.getMessage());


    }
    @Test
    void uniqInput_AllOption_ThrowsException(){
        Throwable exp = assertThrows(UniqueException.class, () ->uniqApplication.uniqInput(true,true,true, LIST_LINES_1, null));
        String expected = UNIQ_PREFIX + INCOMPAT_FLAG_MSG;
        assertEquals(expected, exp.getMessage());
    }
    @Test
    void uniqInput_NoOptionNoOutputFile_DefaultBehaviour() throws  UniqueException{
        String actual = uniqApplication.uniqInput(false,false,false, LIST_LINES_1, null);
        String expected = EXPECTED_L1;
        assertEquals(expected, normalizeLineEndings2(actual));
    }
    @Test
    void uniqInput_NoOptionToOutputFile_DefaultBehaviour() throws  UniqueException, IOException{
        String actual = uniqApplication.uniqInput(false,false,false, LIST_LINES_1, FILE_B);
        String expected =EXPECTED_L1;
        assertEquals(expected, normalizeLineEndings2(actual));
    }


    /**
     * Run null std in throw exception.
     */
    @Test
    void run_NullStdIn_ThrowException() {
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] args = {FILE_A};
        Throwable exp = assertThrows(UniqueException.class, () -> uniqApplication.run(args, null, outputStream));
        String expected = UNIQ_PREFIX + ERR_NULL_STREAMS;
        assertEquals(expected, exp.getMessage());
    }

    /**
     * Run null std out throw exception.
     */
    @Test
    void run_NullStdOut_ThrowException() {
        InputStream inputStream = new ByteArrayInputStream(STDIN_1.getBytes());
        String[] args = {FILE_A};
        Throwable exp = assertThrows(UniqueException.class, () -> uniqApplication.run(args, inputStream, null));
        String expected = UNIQ_PREFIX + ERR_NULL_STREAMS;
        assertEquals(expected, exp.getMessage());
    }

    /**
     * Run no option file return correct.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void run_NoOptionFile_ReturnCorrect() throws AbstractApplicationException, InvalidArgsException {
        InputStream inputStream = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] args = {FILE_A};
        List<String> fileNames = Arrays.asList(args);
        doNothing().when(uniqArgsParser).parse(args);
        when(uniqArgsParser.getFileNames()).thenReturn(fileNames);
        when(uniqArgsParser.isCount()).thenReturn(false);
        when(uniqArgsParser.isRepeated()).thenReturn(false);
        when(uniqArgsParser.isAllRepeated()).thenReturn(false);
        when(uniqArgsParser.getInputFile()).thenReturn(FILE_A);
        when(uniqArgsParser.getOutputFile()).thenReturn(null);
        uniqApplication.run(args, inputStream, outputStream);
        assertEquals(EXPECTED_L1 + NEW_LINE, normalizeLineEndings2(outputStream.toString()));
    }

    /**
     * Run is count file return correct.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void run_IsCountFile_ReturnCorrect() throws AbstractApplicationException,InvalidArgsException{
        InputStream inputStream = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] args = {"-c", FILE_A};
        List<String> fileNames = Arrays.asList(FILE_A);
        doNothing().when(uniqArgsParser).parse(args);
        when(uniqArgsParser.getFileNames()).thenReturn(fileNames);
        when(uniqArgsParser.isCount()).thenReturn(true);
        when(uniqArgsParser.isRepeated()).thenReturn(false);
        when(uniqArgsParser.isAllRepeated()).thenReturn(false);
        when(uniqArgsParser.getInputFile()).thenReturn(FILE_A);
        when(uniqArgsParser.getOutputFile()).thenReturn(null);
        uniqApplication.run(args, inputStream, outputStream);
        assertEquals(EXPECTED_L1_COUNT + NEW_LINE , normalizeLineEndings2(outputStream.toString()));
    }

    /**
     * Run is duplicate file return correct.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void run_IsDuplicateFile_ReturnCorrect() throws AbstractApplicationException, InvalidArgsException{
        InputStream inputStream = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] args = {"-d", FILE_A};
        List<String> fileNames = Arrays.asList(FILE_A);
        doNothing().when(uniqArgsParser).parse(args);
        when(uniqArgsParser.getFileNames()).thenReturn(fileNames);
        when(uniqArgsParser.isCount()).thenReturn(false);
        when(uniqArgsParser.isRepeated()).thenReturn(true);
        when(uniqArgsParser.isAllRepeated()).thenReturn(false);
        when(uniqArgsParser.getInputFile()).thenReturn(FILE_A);
        when(uniqArgsParser.getOutputFile()).thenReturn(null);
        uniqApplication.run(args, inputStream, outputStream);
        assertEquals(EXPECTED_L1_DUP + NEW_LINE, normalizeLineEndings2(outputStream.toString()));

    }

    /**
     * Run is duplicate all file return correct.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void run_IsDuplicateAllFile_ReturnCorrect() throws AbstractApplicationException,InvalidArgsException{
        InputStream inputStream = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] args = {"-D", FILE_A};
        List<String> fileNames = Arrays.asList(FILE_A);
        doNothing().when(uniqArgsParser).parse(args);
        when(uniqArgsParser.getFileNames()).thenReturn(fileNames);
        when(uniqArgsParser.isCount()).thenReturn(false);
        when(uniqArgsParser.isRepeated()).thenReturn(false);
        when(uniqArgsParser.isAllRepeated()).thenReturn(true);
        when(uniqArgsParser.getInputFile()).thenReturn(FILE_A);
        when(uniqArgsParser.getOutputFile()).thenReturn(null);
        uniqApplication.run(args, inputStream, outputStream);
        assertEquals(EXPECTED_L1_DUP_ALL + NEW_LINE, normalizeLineEndings2(outputStream.toString()));
    }

    /**
     * Run is duplicate n duplicate all file return correct.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void run_IsDuplicateNDuplicateAllFile_ReturnCorrect() throws AbstractApplicationException, InvalidArgsException {
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] args = {"-D", "-d", FILE_A};
        List<String> fileNames = Arrays.asList(FILE_A);
        doNothing().when(uniqArgsParser).parse(args);
        when(uniqArgsParser.getFileNames()).thenReturn(fileNames);
        when(uniqArgsParser.isCount()).thenReturn(false);
        when(uniqArgsParser.isRepeated()).thenReturn(true);
        when(uniqArgsParser.isAllRepeated()).thenReturn(true);
        when(uniqArgsParser.getInputFile()).thenReturn(FILE_A);
        when(uniqArgsParser.getOutputFile()).thenReturn(null);
        uniqApplication.run(args, System.in, outputStream);
        assertEquals(EXPECTED_L1_DUP_ALL + NEW_LINE, normalizeLineEndings2(outputStream.toString()));
    }

    /**
     * Run is duplicate n count file return correct.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void run_IsDuplicateNCountFile_ReturnCorrect() throws AbstractApplicationException, InvalidArgsException {
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] args = {"-c", "-d", FILE_A};
        List<String> fileNames = Arrays.asList(FILE_A);
        doNothing().when(uniqArgsParser).parse(args);
        when(uniqArgsParser.getFileNames()).thenReturn(fileNames);
        when(uniqArgsParser.isCount()).thenReturn(true);
        when(uniqArgsParser.isRepeated()).thenReturn(true);
        when(uniqArgsParser.isAllRepeated()).thenReturn(false);
        when(uniqArgsParser.getInputFile()).thenReturn(FILE_A);
        when(uniqArgsParser.getOutputFile()).thenReturn(null);
        uniqApplication.run(args, System.in, outputStream);
        assertEquals(EXPECTED_L1_DUP_AND_COUNT + NEW_LINE, normalizeLineEndings2(outputStream.toString()));
    }

    /**
     * Run is count n duplicate all file throw exception.
     */
    @Test
    void run_IsCountNDuplicateAllFile_ThrowException() throws InvalidArgsException {
        String[] args = {"-D" , "-c", FILE_A};
        OutputStream outputStream = new ByteArrayOutputStream();
        List<String> fileNames = Arrays.asList(FILE_A);
        doNothing().when(uniqArgsParser).parse(args);
        when(uniqArgsParser.getFileNames()).thenReturn(fileNames);
        when(uniqArgsParser.isCount()).thenReturn(true);
        when(uniqArgsParser.isRepeated()).thenReturn(false);
        when(uniqArgsParser.isAllRepeated()).thenReturn(true);
        when(uniqArgsParser.getInputFile()).thenReturn(FILE_A);
        when(uniqArgsParser.getOutputFile()).thenReturn(null);
        Throwable exp = assertThrows(UniqueException.class, () -> uniqApplication.run(args, System.in, outputStream));
        String expected = UNIQ_PREFIX + INCOMPAT_FLAG_MSG;
        assertEquals(expected, exp.getMessage());
    }

    /**
     * Run all option file throw exception.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void run_AllOptionFile_ThrowException() throws InvalidArgsException{
        String[] args = {"-D" , "-c", "-d", FILE_A};
        OutputStream outputStream = new ByteArrayOutputStream();
        List<String> fileNames = Arrays.asList(FILE_A);
        doNothing().when(uniqArgsParser).parse(args);
        when(uniqArgsParser.getFileNames()).thenReturn(fileNames);
        when(uniqArgsParser.isCount()).thenReturn(true);
        when(uniqArgsParser.isRepeated()).thenReturn(true);
        when(uniqArgsParser.isAllRepeated()).thenReturn(true);
        when(uniqArgsParser.getInputFile()).thenReturn(FILE_A);
        when(uniqArgsParser.getOutputFile()).thenReturn(null);
        Throwable exp = assertThrows(UniqueException.class, () -> uniqApplication.run(args, System.in, outputStream));
        String expected = UNIQ_PREFIX + INCOMPAT_FLAG_MSG;
        assertEquals(expected, exp.getMessage());
    }

    /**
     * Run no option to output file throw exception.
     *
     * @throws AbstractApplicationException the abstract application exception, IOException
     */
    @Test
    void run_NoOptionFileToOutFile_ReturnCorrect() throws InvalidArgsException, AbstractApplicationException, IOException{
        String[] args = {FILE_A, FILE_B};
        OutputStream outputStream = new ByteArrayOutputStream();
        List<String> fileNames = Arrays.asList(FILE_A, FILE_B);
        doNothing().when(uniqArgsParser).parse(args);
        when(uniqArgsParser.getFileNames()).thenReturn(fileNames);
        when(uniqArgsParser.isCount()).thenReturn(false);
        when(uniqArgsParser.isRepeated()).thenReturn(false);
        when(uniqArgsParser.isAllRepeated()).thenReturn(false);
        when(uniqArgsParser.getInputFile()).thenReturn(FILE_A);
        when(uniqArgsParser.getOutputFile()).thenReturn(FILE_B);
        uniqApplication.run(args, System.in, outputStream);
        String outputFile = readString(Path.of(FILE_PATH_B));
        assertEquals(EXPECTED_L1, normalizeLineEndings2(outputFile));
    }
    /**
     * Run no option Error writing stdout throw exception.
     *
     * @throws InvalidArgsException  IOException
     */
    @Test
    void run_ErrorWriteStdout_ReturnCorrect() throws InvalidArgsException, IOException{
        String[] args = {FILE_A};
        OutputStream outputStream = mock(OutputStream.class);//NOPMD mocked class no need close
        List<String> fileNames = Arrays.asList(FILE_A);
        doNothing().when(uniqArgsParser).parse(args);
        when(uniqArgsParser.getFileNames()).thenReturn(fileNames);
        when(uniqArgsParser.isCount()).thenReturn(false);
        when(uniqArgsParser.isRepeated()).thenReturn(false);
        when(uniqArgsParser.isAllRepeated()).thenReturn(false);
        when(uniqArgsParser.getInputFile()).thenReturn(FILE_A);
        when(uniqArgsParser.getOutputFile()).thenReturn(null);
        doThrow(IOException.class).when(outputStream).write(any());
        Throwable exp = assertThrows(UniqueException.class, () -> uniqApplication.run(args, System.in, outputStream));
        String expected = UNIQ_PREFIX + ERR_WRITE_STREAM;
        assertEquals(expected, exp.getMessage());
    }
    /**
     * Run Too many arguements > 2 throw exception.
     *
     * @throws InvalidArgsException  IOException
     */
    @Test
    void run_TooManyArgs_ReturnCorrect() throws InvalidArgsException, IOException{
        String[] args = {FILE_A, FILE_B, FILE_EMPTY};
        OutputStream outputStream = new ByteArrayOutputStream();
        List<String> fileNames = Arrays.asList(FILE_A, FILE_B, FILE_EMPTY);
        doNothing().when(uniqArgsParser).parse(args);
        when(uniqArgsParser.getFileNames()).thenReturn(fileNames);
        when(uniqArgsParser.isCount()).thenReturn(false);
        when(uniqArgsParser.isRepeated()).thenReturn(false);
        when(uniqArgsParser.isAllRepeated()).thenReturn(false);
        when(uniqArgsParser.getInputFile()).thenReturn(FILE_A);
        when(uniqArgsParser.getOutputFile()).thenReturn(FILE_B);
        Throwable exp = assertThrows(UniqueException.class, () -> uniqApplication.run(args, System.in, outputStream));
        String expected = UNIQ_PREFIX + ERR_EXTRA_OPERAND + String.format("'%s'", FILE_EMPTY);
        assertEquals(expected, exp.getMessage());
    }

    /**
     * Run no option from stdin return correct.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void run_NoOptionStdin_ReturnCorrect() throws AbstractApplicationException, InvalidArgsException{
        InputStream inputStream = new ByteArrayInputStream(STDIN_1.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] args = {"-"};
        List<String> fileNames = Arrays.asList(args);
        doNothing().when(uniqArgsParser).parse(args);
        when(uniqArgsParser.getFileNames()).thenReturn(fileNames);
        when(uniqArgsParser.isCount()).thenReturn(false);
        when(uniqArgsParser.isRepeated()).thenReturn(false);
        when(uniqArgsParser.isAllRepeated()).thenReturn(false);
        when(uniqArgsParser.getInputFile()).thenReturn("-");
        when(uniqArgsParser.getOutputFile()).thenReturn(null);
        uniqApplication.run(args, inputStream, outputStream);
        assertEquals(EXPECTED_L1 + NEW_LINE, normalizeLineEndings2(outputStream.toString()));
    }

    /**
     * Run is count file stdin return correct.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void run_IsCountStdin_ReturnCorrect() throws AbstractApplicationException, InvalidArgsException{
        InputStream inputStream = new ByteArrayInputStream(STDIN_1.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] args = {"-c","-"};
        List<String> fileNames = Arrays.asList(args);
        doNothing().when(uniqArgsParser).parse(args);
        when(uniqArgsParser.getFileNames()).thenReturn(fileNames);
        when(uniqArgsParser.isCount()).thenReturn(true);
        when(uniqArgsParser.isRepeated()).thenReturn(false);
        when(uniqArgsParser.isAllRepeated()).thenReturn(false);
        when(uniqArgsParser.getInputFile()).thenReturn("-");
        when(uniqArgsParser.getOutputFile()).thenReturn(null);
        uniqApplication.run(args, inputStream, outputStream);
        assertEquals(EXPECTED_L1_COUNT + NEW_LINE, normalizeLineEndings2(outputStream.toString()));
    }

    /**
     * Run is duplicate file stdin return correct.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void run_IsDuplicateStdin_ReturnCorrect() throws AbstractApplicationException, InvalidArgsException{
        InputStream inputStream = new ByteArrayInputStream(STDIN_1.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] args = {"-d","-"};
        List<String> fileNames = Arrays.asList(args);
        doNothing().when(uniqArgsParser).parse(args);
        when(uniqArgsParser.getFileNames()).thenReturn(fileNames);
        when(uniqArgsParser.isCount()).thenReturn(false);
        when(uniqArgsParser.isRepeated()).thenReturn(true);
        when(uniqArgsParser.isAllRepeated()).thenReturn(false);
        when(uniqArgsParser.getInputFile()).thenReturn("-");
        when(uniqArgsParser.getOutputFile()).thenReturn(null);
        uniqApplication.run(args, inputStream, outputStream);
        assertEquals(EXPECTED_L1_DUP + NEW_LINE, normalizeLineEndings2(outputStream.toString()));

    }

    /**
     * Run is duplicate all file stdin return correct.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void run_IsDuplicateAllStdin_ReturnCorrect() throws AbstractApplicationException, InvalidArgsException{
        InputStream inputStream = new ByteArrayInputStream(STDIN_1.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] args = {"-D","-"};
        List<String> fileNames = Arrays.asList(args);
        doNothing().when(uniqArgsParser).parse(args);
        when(uniqArgsParser.getFileNames()).thenReturn(fileNames);
        when(uniqArgsParser.isCount()).thenReturn(false);
        when(uniqArgsParser.isRepeated()).thenReturn(false);
        when(uniqArgsParser.isAllRepeated()).thenReturn(true);
        when(uniqArgsParser.getInputFile()).thenReturn("-");
        when(uniqArgsParser.getOutputFile()).thenReturn(null);
        uniqApplication.run(args, inputStream, outputStream);
        assertEquals(EXPECTED_L1_DUP_ALL + NEW_LINE, normalizeLineEndings2(outputStream.toString()));

    }

    /**
     * Run is duplicate n duplicate all file stdin return correct.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void run_IsDuplicateNDuplicateAllStdin_ReturnCorrect() throws AbstractApplicationException, InvalidArgsException {
        InputStream inputStream = new ByteArrayInputStream(STDIN_1.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] args = {"-d","-D","-"};
        List<String> fileNames = new ArrayList<>();
        fileNames.add("-");
        doNothing().when(uniqArgsParser).parse(args);
        when(uniqArgsParser.getFileNames()).thenReturn(fileNames);
        when(uniqArgsParser.isCount()).thenReturn(false);
        when(uniqArgsParser.isRepeated()).thenReturn(true);
        when(uniqArgsParser.isAllRepeated()).thenReturn(true);
        when(uniqArgsParser.getInputFile()).thenReturn("-");
        when(uniqArgsParser.getOutputFile()).thenReturn(null);
        uniqApplication.run(args, inputStream, outputStream);
        assertEquals(EXPECTED_L1_DUP_ALL + NEW_LINE, normalizeLineEndings2(outputStream.toString()));
    }

    /**
     * Run is duplicate n count file stdin return correct.
     *
     * @throws AbstractApplicationException the abstract application exception, invalidargsException
     */
    @Test
    void run_IsDuplicateNCountStdin_ReturnCorrect() throws AbstractApplicationException, InvalidArgsException {
        InputStream inputStream = new ByteArrayInputStream(STDIN_1.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] args = {"-c","-d","-"};
        List<String> fileNames = new ArrayList<>();
        fileNames.add("-");
        doNothing().when(uniqArgsParser).parse(args);
        when(uniqArgsParser.getFileNames()).thenReturn(fileNames);
        when(uniqArgsParser.isCount()).thenReturn(true);
        when(uniqArgsParser.isRepeated()).thenReturn(true);
        when(uniqArgsParser.isAllRepeated()).thenReturn(false);
        when(uniqArgsParser.getInputFile()).thenReturn("-");
        when(uniqArgsParser.getOutputFile()).thenReturn(null);
        uniqApplication.run(args, inputStream, outputStream);
        assertEquals(EXPECTED_L1_DUP_AND_COUNT + NEW_LINE, normalizeLineEndings2(outputStream.toString()));

    }

    /**
     * Run invalid option throw exception.
     *
     * @throws InvalidArgsException the abstract application exception
     */
    @ParameterizedTest
    @ValueSource(strings = { "-b", "-i", "-a", "-e", "-f", "-g", "-h"})
    void run_invalidFlag_ThrowException(String flag) throws InvalidArgsException{
        String[] args = {flag, FILE_A};
        OutputStream outputStream = new ByteArrayOutputStream();
        List<String> fileNames = Arrays.asList(FILE_A);
        doThrow(new InvalidArgsException(ILLEGAL_FLAG_MSG + flag)).when(uniqArgsParser).parse(args);
        when(uniqArgsParser.getFileNames()).thenReturn(fileNames);
        when(uniqArgsParser.isCount()).thenReturn(false);
        when(uniqArgsParser.isRepeated()).thenReturn(false);
        when(uniqArgsParser.isAllRepeated()).thenReturn(false);
        when(uniqArgsParser.getInputFile()).thenReturn(FILE_A);
        when(uniqArgsParser.getOutputFile()).thenReturn(null);
        Throwable exp = assertThrows(UniqueException.class, () -> uniqApplication.run(args, System.in, outputStream));
        String expected = UNIQ_PREFIX + ILLEGAL_FLAG_MSG + flag;
        assertEquals(expected, exp.getMessage());
    }

}
