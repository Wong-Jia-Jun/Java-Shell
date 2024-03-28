package ef2test;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.UniqueException;
import sg.edu.nus.comp.cs4218.impl.app.UniqApplication;
import sg.edu.nus.comp.cs4218.impl.parser.UniqArgsParser;


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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;


public class UniqApplicationIT { //NOPMD - suppressed ClassNamingConventions - As per Teaching Team's guidelines
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
    private static final String TEST_PATH = BASE_PATH + File.separator + TEST_RESOURCE_DIR + File.separator + "UniqApplicationITest";
    private static final String FILE_A = "A.txt";
    private static final String FILE_PATH_A = TEST_PATH + File.separator + FILE_A;
    private static final String FILE_B = "B.txt";
    private static final String FILE_PATH_B = TEST_PATH + File.separator + FILE_B;

    private static final String MISSING_FILE = "nonExistent.txt";
    private static final String MISSING_FILE_PATH = TEST_PATH + File.separator + MISSING_FILE;
    private static final String FILE_EMPTY = "empty.txt";
    private static final String FILE_PATH_EMPTY = TEST_PATH + File.separator + FILE_EMPTY;
    private static final String FILE_NO_PERM = "noPerm.txt";
    private static final String FILE_NO_PERM_PATH = TEST_PATH + File.separator + FILE_NO_PERM;
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
        this.uniqArgsParser = new UniqArgsParser();
        this.uniqApplication = new UniqApplication(uniqArgsParser);
        Files.deleteIfExists(Paths.get(TEST_PATH));
        Files.createDirectory(Paths.get(TEST_PATH));
        Files.createDirectory(Paths.get(FOLDER1_PATH));
        Files.createFile(Paths.get(FILE_PATH_A));
        Files.createFile(Paths.get(FILE_PATH_B));
        Files.createFile(Paths.get(FILE_PATH_EMPTY));
        Files.createFile(Paths.get(FILE_NO_PERM_PATH));
        Paths.get(FILE_NO_PERM_PATH).toFile().setReadOnly();
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
            Throwable exp = assertThrows(UniqueException.class, () ->uniqApplication.uniqToFile(STDIN_1, FOLDER1));
            String expected = UNIQ_PREFIX + ERR_IS_DIR;
            assertEquals(expected, exp.getMessage());

    }
    /**
     * Uniq to file, input file no write perm should throw exception.
     */
    @Test
    void uniqToFile_FileNoPerm_ThrowException(){
            Throwable exp = assertThrows(UniqueException.class, () ->uniqApplication.uniqToFile(STDIN_1, FILE_NO_PERM));
            String expected = UNIQ_PREFIX + ERR_NO_PERM;
            assertEquals(expected, exp.getMessage());

    }
    /**
     * Uniq to file, valid input file Correct behaviour.
     * @throws UniqueException, IOException
     */
    @Test
    void uniqToFile_ValidFile_WriteCorrectFile() throws UniqueException, IOException {
            uniqApplication.uniqToFile(STDIN_1, FILE_A);
            String contentFile = readString(Path.of(FILE_PATH_A));
            assertEquals(STDIN_1, contentFile);

    }
    /**
     * Uniq from file non existent throw exception.
     */
    @Test
    void  uniqFromFile_NonExistFile_ThrowException(){

        Throwable exp = assertThrows(UniqueException.class, () -> uniqApplication.uniqFromFile(false, false, false, MISSING_FILE, null));
        String expected = UNIQ_PREFIX + MISSING_FILE + ": " + ERR_FILE_NOT_FND;
        assertEquals(expected, exp.getMessage());

    }
    /**
     * Uniq from empty file should return empty string.
     * @throws UniqueException, IOException
     * **/

    @Test
    void uniqFromFile_EmptyFile_ReturnsEmpty() throws IOException, AbstractApplicationException {
        String result = uniqApplication.uniqFromFile(false, false, false, FILE_EMPTY, null);
        assertTrue(result.isBlank());

    }
    /**
     * Uniq from file no option return correct.
     *
     * @throws AbstractApplicationException the abstract application exception
     */

    @Test
    void  uniqFromFile_NoOptionValidFile_ReturnCorrect() throws AbstractApplicationException {

        String actual = uniqApplication.uniqFromFile(false, false, false, FILE_A, null);
        assertEquals(EXPECTED_L1, normalizeLineEndings2(actual));

    }

    /**
     * Uniq from file is count return correct.
     *
     * @throws AbstractApplicationException the abstract application exception, IOException
     */

    @Test
    void  uniqFromFile_IsCount_ReturnCorrect() throws AbstractApplicationException{

        String actual = uniqApplication.uniqFromFile(true, false, false, FILE_A, null);
        assertEquals(EXPECTED_L1_COUNT, normalizeLineEndings2(actual));

    }

    /**
     * Uniq from file is duplicate return correct.
     *
     * @throws AbstractApplicationException the abstract application exception, IOException
     */

    @Test
    void  uniqFromFile_IsDuplicate_ReturnCorrect() throws AbstractApplicationException {

        String actual = uniqApplication.uniqFromFile(false, true, false, FILE_A, null);
        assertEquals(EXPECTED_L1_DUP, normalizeLineEndings2(actual));


    }

    /**
     * Uniq from file is duplicate all return correct.
     *
     * @throws AbstractApplicationException the abstract application exception
     */

    @Test
    void  uniqFromFile_IsDuplicateAll_ReturnCorrect() throws AbstractApplicationException{

        String actual = uniqApplication.uniqFromFile(false, false, true, FILE_A, null);
        assertEquals(EXPECTED_L1_DUP_ALL, normalizeLineEndings2(actual));

    }

    /**
     * Uniq from file is duplicate n duplicate all return correct.
     *
     * @throws AbstractApplicationException the abstract application exception, IOException
     */

    @Test
    void  uniqFromFile_IsDuplicateNDuplicateAll_ReturnCorrect() throws AbstractApplicationException{

        String actual = uniqApplication.uniqFromFile(false, true, true, FILE_A, null);
        assertEquals(EXPECTED_L1_DUP_ALL, normalizeLineEndings2(actual));

    }

    /**
     * Uniq from file is duplicate n count return correct.
     *
     * @throws AbstractApplicationException the abstract application exception,IOException
     */

    @Test
    void  uniqFromFile_IsDuplicateNCount_ReturnCorrect() throws AbstractApplicationException{

        String actual = uniqApplication.uniqFromFile(true, true, false, FILE_A, null);
        assertEquals(EXPECTED_L1_DUP_AND_COUNT, normalizeLineEndings2(actual));

    }

    /**
     * Uniq from file is count n duplicate all throw exception.
     * @throws IOException
     */
    @Test
    void  uniqFromFile_IsCountNDuplicateAll_ThrowException() {

        Throwable exp = assertThrows(UniqueException.class, () -> uniqApplication.uniqFromFile(true, false, true, FILE_A, null));
        String expected = UNIQ_PREFIX + INCOMPAT_FLAG_MSG;
        assertEquals(expected, exp.getMessage());

    }

    /**
     * Uniq from file all option throw exception.
     * @throws IOException
     */

    @Test
    void  uniqFromFile_AllOption_ThrowException() {

        Throwable exp = assertThrows(UniqueException.class, () -> uniqApplication.uniqFromFile(true, true, true, FILE_A, null));
        String expected = UNIQ_PREFIX + INCOMPAT_FLAG_MSG;
        assertEquals(expected, exp.getMessage());

    }
    /**
     * Uniq from file no option to output file should hae default behaviour.
     * @throws IOException
     */
    @Test
    void  uniqFromFile_ToOutputFile_WriteCorrect() throws IOException, AbstractApplicationException{

        String output = uniqApplication.uniqFromFile(false, false, false, FILE_A, FILE_B);
        String actual = readString(Path.of(FILE_PATH_B));
        assertEquals(EXPECTED_L1, normalizeLineEndings2(actual));
        assertEquals(EXPECTED_L1, normalizeLineEndings2(output));

    }

    /**
     * Uniq from stdin no option return correct.
     *
     * @throws AbstractApplicationException the abstract application exception
     */

    @Test
    void  uniqFromStdin_NoOption_ReturnCorrect() throws AbstractApplicationException{
            String argument = STDIN_1;
            byte[] bytes = argument.getBytes();
            InputStream inputStream = new ByteArrayInputStream(bytes);
            String actual = uniqApplication.uniqFromStdin(false, false, false, inputStream, null);
            assertEquals(EXPECTED_L1, normalizeLineEndings2(actual));

    }

    /**
     * Uniq from stdin is count return correct.
     *
     * @throws AbstractApplicationException the abstract application exception
     */

    @Test
    void  uniqFromStdin_IsCount_ReturnCorrect() throws AbstractApplicationException{
            String argument = STDIN_1;
            byte[] bytes = argument.getBytes();
            InputStream inputStream = new ByteArrayInputStream(bytes);
            String actual = uniqApplication.uniqFromStdin(true, false, false, inputStream, null);
            assertEquals(EXPECTED_L1_COUNT, normalizeLineEndings2(actual));

    }

    /**
     * Uniq from stdin is duplicate return correct.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void  uniqFromStdin_IsDuplicate_ReturnCorrect() throws AbstractApplicationException{
            String argument = STDIN_1;
            byte[] bytes = argument.getBytes();
            InputStream inputStream = new ByteArrayInputStream(bytes);
            String actual = uniqApplication.uniqFromStdin(false, true, false, inputStream, null);
            assertEquals(EXPECTED_L1_DUP, normalizeLineEndings2(actual));


    }

    /**
     * Uniq from stdin is duplicate all return correct.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void  uniqFromStdin_IsDuplicateAll_ReturnCorrect() throws AbstractApplicationException{
            String argument = STDIN_1;
            byte[] bytes = argument.getBytes();
            InputStream inputStream = new ByteArrayInputStream(bytes);
            String actual = uniqApplication.uniqFromStdin(false, false, true, inputStream, null);
            assertEquals(EXPECTED_L1_DUP_ALL, normalizeLineEndings2(actual));

    }

    /**
     * Uniq from stdin is duplicate n duplicate all return correct.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void  uniqFromStdin_IsDuplicateNDuplicateAll_ReturnCorrect() throws AbstractApplicationException {
            String argument = STDIN_1;
            byte[] bytes = argument.getBytes();
            InputStream inputStream = new ByteArrayInputStream(bytes);
            String actual = uniqApplication.uniqFromStdin(false, true, true, inputStream, null);
            assertEquals(EXPECTED_L1_DUP_ALL, normalizeLineEndings2(actual));

    }

    /**
     * Uniq from stdin is duplicate n count return correct.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void  uniqFromStdin_IsDuplicateNCount_ReturnCorrect() throws AbstractApplicationException {
            String argument = STDIN_1;
            byte[] bytes = argument.getBytes();
            InputStream inputStream = new ByteArrayInputStream(bytes);
            String actual = uniqApplication.uniqFromStdin(true, true, false, inputStream, null);
            assertEquals(EXPECTED_L1_DUP_AND_COUNT, normalizeLineEndings2(actual));

    }
    /**
     * Uniq from stdin all option to file return correcct output
     *
     * @throws AbstractApplicationException the abstract application exception, IOException
     */
    @Test
    void  uniqFromStdin_NoOptionToFile_ReturnCorrect() throws AbstractApplicationException, IOException{
            String argument = STDIN_1;
            byte[] bytes = argument.getBytes();
            InputStream inputStream = new ByteArrayInputStream(bytes);
            String actual = uniqApplication.uniqFromStdin(false, false, false, inputStream, FILE_B);
            String fileContent = readString(Path.of(FILE_PATH_B));
            assertEquals(EXPECTED_L1, normalizeLineEndings2(actual));
            assertEquals(EXPECTED_L1, normalizeLineEndings2(fileContent));

    }
    /**
     * Run no option file return correct.
     *
     * @throws AbstractApplicationException the abstract application exception
     */

    @Test
    void  run_NoOptionFile_ReturnCorrect() throws AbstractApplicationException {
        InputStream inputStream = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] args = {FILE_A};
        uniqApplication.run(args, inputStream, outputStream);
        assertEquals(EXPECTED_L1 + NEW_LINE, normalizeLineEndings2(outputStream.toString()));
    }
    /**
     * Run no option file interleaving no duplicate in adj lines return correct.
     *
     * @throws AbstractApplicationException the abstract application exception
     */

    @Test
    void  run_NoOptionFileNoAdjDupLine_ReturnExpected() throws AbstractApplicationException {
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] args = {FILE_B};
        uniqApplication.run(args, System.in, outputStream);
        assertEquals(EXPECTED_L2 , normalizeLineEndings2(outputStream.toString()));
    }

    /**
     * Run is count file return correct.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void  run_IsCountFile_ReturnCorrect() throws AbstractApplicationException{
        InputStream inputStream = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] args = {"-c", FILE_A};
        uniqApplication.run(args, inputStream, outputStream);
        assertEquals(EXPECTED_L1_COUNT + NEW_LINE , normalizeLineEndings2(outputStream.toString()));
    }
    /**
     * Run is count file with interleaving no duplicate in adj lines return correct.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void  run_IsCountFileNoAdjDup_ReturnCorrect() throws AbstractApplicationException{
        InputStream inputStream = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] args = {"-c", FILE_B};
        uniqApplication.run(args, inputStream, outputStream);
        assertEquals(EXPECTED_L2_COUNT , normalizeLineEndings2(outputStream.toString()));
    }

    /**
     * Run is duplicate file return correct.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void  run_IsDuplicateFile_ReturnCorrect() throws AbstractApplicationException{
        InputStream inputStream = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] args = {"-d", FILE_A};
        uniqApplication.run(args, inputStream, outputStream);
        assertEquals(EXPECTED_L1_DUP + NEW_LINE, normalizeLineEndings2(outputStream.toString()));

    }
    /**
     * Run is duplicate file with interleaving non adj dup lines return correct.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void  run_IsDuplicateFileNoAdjDup_ReturnCorrect() throws AbstractApplicationException{
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] args = {"-d", FILE_B};
        uniqApplication.run(args, System.in, outputStream);
        assertEquals(EXPECTED_L2_DUP + System.lineSeparator() , outputStream.toString());

    }

    /**
     * Run is duplicate all file return correct.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void  run_IsDuplicateAllFile_ReturnCorrect() throws AbstractApplicationException{
        InputStream inputStream = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] args = {"-D", FILE_A};
        uniqApplication.run(args, inputStream, outputStream);
        assertEquals(EXPECTED_L1_DUP_ALL + NEW_LINE, normalizeLineEndings2(outputStream.toString()));
    }
    /**
     * Run is duplicate all file with interleaving no Dup Adj lines return correct.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void  run_IsDuplicateAllFileNoAdjDup_ReturnCorrect() throws AbstractApplicationException{
        InputStream inputStream = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] args = {"-D", FILE_B};
        uniqApplication.run(args, inputStream, outputStream);
        assertEquals(EXPECTED_L2_DUP_ALL + System.lineSeparator(), outputStream.toString());
    }

    /**
     * Run is duplicate n duplicate all file return correct.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void  run_IsDuplicateNDuplicateAllFile_ReturnCorrect() throws AbstractApplicationException {
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] args = {"-D", "-d", FILE_A};
        uniqApplication.run(args, System.in, outputStream);
        assertEquals(EXPECTED_L1_DUP_ALL + NEW_LINE, normalizeLineEndings2(outputStream.toString()));
    }

    /**
     * Run is duplicate n count file return correct.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void  run_IsDuplicateNCountFile_ReturnCorrect() throws AbstractApplicationException {
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] args = {"-c", "-d", FILE_A};
        uniqApplication.run(args, System.in, outputStream);
        assertEquals(EXPECTED_L1_DUP_AND_COUNT + NEW_LINE, normalizeLineEndings2(outputStream.toString()));
    }

    /**
     * Run is count n duplicate all file throw exception.
     */
    @Test
    void  run_IsCountNDuplicateAllFile_ThrowException() throws InvalidArgsException {
        String[] args = {"-D" , "-c", FILE_A};
        OutputStream outputStream = new ByteArrayOutputStream();
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
    void  run_AllOptionFile_ThrowException() throws InvalidArgsException{
        String[] args = {"-D" , "-c", "-d", FILE_A};
        OutputStream outputStream = new ByteArrayOutputStream();
        List<String> fileNames = Arrays.asList(FILE_A);
        Throwable exp = assertThrows(UniqueException.class, () -> uniqApplication.run(args, System.in, outputStream));
        String expected = UNIQ_PREFIX + INCOMPAT_FLAG_MSG;
        assertEquals(expected, exp.getMessage());
    }
    /**
     * Run no option to output file throw exception.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void  run_NoOptionFileToOutFile_ThrowException() throws InvalidArgsException, AbstractApplicationException, IOException{
        String[] args = {FILE_A, FILE_B};
        OutputStream outputStream = new ByteArrayOutputStream();
        uniqApplication.run(args, System.in, outputStream);
        String outputFile = readString(Path.of(FILE_PATH_B));
        assertEquals(EXPECTED_L1, normalizeLineEndings2(outputFile));
    }
    /**
     * Run no option from stdin return correct.
     *
     * @throws AbstractApplicationException the abstract application exception
     */

    @Test
    void  run_NoOptionStdin_ReturnCorrect() throws AbstractApplicationException{
        InputStream inputStream = new ByteArrayInputStream(STDIN_1.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] args = {"-"};
        uniqApplication.run(args, inputStream, outputStream);
        assertEquals(EXPECTED_L1 + NEW_LINE, normalizeLineEndings2(outputStream.toString()));
    }

    /**
     * Run is count file stdin return correct.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void  run_IsCountStdin_ReturnCorrect() throws AbstractApplicationException{
        InputStream inputStream = new ByteArrayInputStream(STDIN_1.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] args = {"-c", "-"};
        uniqApplication.run(args, inputStream, outputStream);
        assertEquals(EXPECTED_L1_COUNT + NEW_LINE, normalizeLineEndings2(outputStream.toString()));
    }

    /**
     * Run is duplicate file stdin return correct.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void  run_IsDuplicateStdin_ReturnCorrect() throws AbstractApplicationException{
        InputStream inputStream = new ByteArrayInputStream(STDIN_1.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] args = {"-d","-"};
        uniqApplication.run(args, inputStream, outputStream);
        assertEquals(EXPECTED_L1_DUP + NEW_LINE, normalizeLineEndings2(outputStream.toString()));

    }

    /**
     * Run is duplicate all file stdin return correct.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void  run_IsDuplicateAllStdin_ReturnCorrect() throws AbstractApplicationException{
        InputStream inputStream = new ByteArrayInputStream(STDIN_1.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] args = {"-D","-"};
        uniqApplication.run(args, inputStream, outputStream);
        assertEquals(EXPECTED_L1_DUP_ALL + NEW_LINE, normalizeLineEndings2(outputStream.toString()));

    }

    /**
     * Run is duplicate n duplicate all file stdin return correct.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void  run_IsDuplicateNDuplicateAllStdin_ReturnCorrect() throws AbstractApplicationException {
        InputStream inputStream = new ByteArrayInputStream(STDIN_1.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] args = {"-d","-D","-"};
        uniqApplication.run(args, inputStream, outputStream);
        assertEquals(EXPECTED_L1_DUP_ALL + NEW_LINE, normalizeLineEndings2(outputStream.toString()));
    }

    /**
     * Run is duplicate n count file stdin return correct.
     *
     * @throws AbstractApplicationException the abstract application exception, invalidargsException
     */
    @Test
    void  run_IsDuplicateNCountStdin_ReturnCorrect() throws AbstractApplicationException {
        InputStream inputStream = new ByteArrayInputStream(STDIN_1.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] args = {"-c","-d","-"};
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
    void  run_invalidFlag_ThrowException(String flag) throws InvalidArgsException{
        String[] args = {flag, FILE_A};
        OutputStream outputStream = new ByteArrayOutputStream();
        Throwable exp = assertThrows(UniqueException.class, () -> uniqApplication.run(args, System.in, outputStream));
        String expected = UNIQ_PREFIX + ILLEGAL_FLAG_MSG + flag.replace("-", "");
        assertEquals(expected, exp.getMessage());
    }



}
