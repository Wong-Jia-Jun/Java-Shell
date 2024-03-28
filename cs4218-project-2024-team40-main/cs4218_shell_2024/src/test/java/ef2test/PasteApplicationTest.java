package ef2test;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.PasteException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.app.PasteApplication;
import sg.edu.nus.comp.cs4218.impl.parser.PasteArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;


/**
 * Tests Functionality of paste application
 */
@SuppressWarnings("PMD.CloseResource")
public class PasteApplicationTest {
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
    private static final String[] LINES1 = {"1", "2", "3", "4"};
    private static final String[] LINES2 = {"A", "B", "C", "D"};
    private static final String[] LINES3 = {"hello", "world"};
    private static final String SINGLE_ARG = "Single Arg";
    private static final String MULTIPLE_ARG = "1" + STRING_NEWLINE + "2" + STRING_NEWLINE;
    private static final String MTL_ARG_RES = "1" + STRING_NEWLINE + "2";
    private static final String MTL_ARG_S_RES = "1\t2";
    private static final String SINGLE_ARG_L2 = "Single Arg\t1" + STRING_NEWLINE + "\t2" + STRING_NEWLINE + "\t3" + STRING_NEWLINE + "\t4";
    private static final String L1_EXPECTED = "1" + STRING_NEWLINE + "2" + STRING_NEWLINE + "3" + STRING_NEWLINE + "4";
    private static final String L1_SERIAL = "1\t2\t3\t4";
    private static final String SINGLE_L2_SERIAL = "Single Arg" + STRING_NEWLINE + L1_SERIAL;
    private static final String L1_L2_MERGE_DEF = "1\tA" + STRING_NEWLINE
                                                        + "2\tB" + STRING_NEWLINE
                                                        + "3\tC" + STRING_NEWLINE
                                                        + "4\tD";
    private static final String L1_L2_SERIAL = "1\t2\t3\t4" + STRING_NEWLINE + "A\tB\tC\tD";
    private static final String TEST_STR = "Test";
    private static final String BLANK_FILE = "";
    private static final String ILLEGAL_ARG = "-a";
    private static final String D_F_D_S_RES = "1\t2\t3\t4" + STRING_NEWLINE + "A\tB\tC\tD" + STRING_NEWLINE + "hello\tworld";
    private static final String D_F_D_RES = "1\tA\t2" + STRING_NEWLINE + "3\tB\t4" + STRING_NEWLINE + "\tC\t" + STRING_NEWLINE + "\tD\t";

    /**
     * Sets each.
     *
     * @throws IOException the io exception
     */
    @BeforeEach
    public void setupEach() {
        this.pasteParser = mock(PasteArgsParser.class);
        this.pasteApplication = new PasteApplication(pasteParser);
    }

    /**
     * Merge stdin stdin null throw exception.
     */
    /* mergeStdin */
    @Test
    void mergeStdin_StdinNull_ThrowException() {
        Throwable exp = assertThrows(PasteException.class ,() -> pasteApplication.mergeStdin(false,null ));
        assertEquals(new PasteException(ERR_NULL_STREAMS).getMessage(), exp.getMessage());
    }

    @Test
    void mergeStdin_CannotGetLinesFromInputStream_ThrowException() {
        try (MockedStatic<IOUtils> mockedIo = mockStatic(IOUtils.class)) {
            InputStream input = mock(InputStream.class);
            mockedIo.when(() -> IOUtils.getLinesFromInputStream(any()))
                    .thenThrow(new IOException(TEST_STR));
            Throwable exp = assertThrows(PasteException.class, () -> pasteApplication.mergeStdin(false, input));
            assertEquals(new PasteException(new IOException(TEST_STR)).getMessage(), exp.getMessage());
        }
    }

    /**
     * Merge stdin one stdin arg returns stdin arg string.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void mergeStdin_OneStdinArg_EchoStdinArg() throws AbstractApplicationException {
        byte[] bytes = SINGLE_ARG.getBytes();
        InputStream inputStream = new ByteArrayInputStream(bytes);
        String actual = pasteApplication.mergeStdin(false, inputStream);
        assertEquals(SINGLE_ARG, actual);
    }

    /**
     * Merge stdin one stdin arg option echo stdin arg.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void mergeStdin_OneStdinArgOption_EchoStdinArg() throws  AbstractApplicationException {
        byte[] bytes = SINGLE_ARG.getBytes();
        InputStream inputStream = new ByteArrayInputStream(bytes);
        String actual = pasteApplication.mergeStdin(true, inputStream);
        assertEquals(SINGLE_ARG, actual);
    }

    /**
     * Merge stdin multiple stdin arg arg sep by tab.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void mergeStdin_MultipleStdinArg_ArgSepByTab() throws AbstractApplicationException {
        byte[] bytes = MULTIPLE_ARG.getBytes();
        InputStream inputStream = new ByteArrayInputStream(bytes);
        String actual = pasteApplication.mergeStdin(false, inputStream);
        assertEquals(MTL_ARG_RES, actual);
    }

    /**
     * Merge stdin multiple stdin arg option arg sep by new line.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void mergeStdin_MultipleStdinArgOption_ArgSepByNewLine() throws  AbstractApplicationException{
        byte[] bytes = MULTIPLE_ARG.getBytes();
        InputStream inputStream = new ByteArrayInputStream(bytes);
        String actual = pasteApplication.mergeStdin(true, inputStream);
        assertEquals(MTL_ARG_S_RES, actual);

    }

    /**
     * Merge file null file name throw exception.
     */
    @Test
    void mergeFile_NullFileName_ThrowException() {
        Throwable exp = assertThrows(PasteException.class, ()-> pasteApplication.mergeFile(false, null));
        assertEquals(new PasteException(ERR_NULL_ARGS).getMessage(), exp.getMessage());

    }

    /**
     * Merge length 0 file names throw exception.
     */
    @Test
    void mergeFile_Length0FileName_ThrowException() {
        String[] files = {};
        Throwable exp = assertThrows(PasteException.class, ()-> pasteApplication.mergeFile(false, files));
        assertEquals(new PasteException(ERR_NO_ARGS).getMessage(), exp.getMessage());

    }

    /**
     * Merge blank file name in file names throw exception.
     */
    @Test
    void mergeFile_BlankFileName_ThrowException() {
        String[] files = {BLANK_FILE};
        Throwable exp = assertThrows(PasteException.class, ()-> pasteApplication.mergeFile(false, files));
        assertEquals(new PasteException(ERR_NO_FILE_ARGS).getMessage(), exp.getMessage());

    }

    /**
     * Merge file non exist file name throw exception.
     */
    @Test
    void mergeFile_NonExistFileName_ThrowException() {
        String[] files = {FILE_PATH_INVALID};
        try (MockedStatic<IOUtils> mockedIo = mockStatic(IOUtils.class)) {
            mockedIo.when(() -> IOUtils.resolveFilePath(any()))
                    .thenReturn(Paths.get(FILE_PATH_INVALID));
            Throwable exp = assertThrows(PasteException.class, ()-> pasteApplication.mergeFile(false, files));
            assertEquals(new PasteException(FILE_PATH_INVALID, ERR_FILE_NOT_FND).getMessage(), exp.getMessage());
        }
    }

    /**
     * Merge file input file is dir throw exception.
     */
    @Test
    void mergeFile_InputFileIsDir_ThrowException() {
        String[] files = {FOLDER1_PATH};
        try (
                MockedStatic<IOUtils> mockedIo = mockStatic(IOUtils.class);
                MockedStatic<Files> mockedFiles = mockStatic(Files.class)
        ) {
            mockedIo.when(() -> IOUtils.resolveFilePath(any()))
                    .thenReturn(Paths.get(FOLDER1_PATH));
            mockedFiles.when(() -> Files.exists(any()))
                    .thenReturn(true);
            mockedFiles.when(() -> Files.isDirectory(any()))
                    .thenReturn(true);
            Throwable exp = assertThrows(PasteException.class, ()-> pasteApplication.mergeFile(false, files));
            assertEquals(new PasteException(FOLDER1_PATH, ERR_IS_DIR_FILE).getMessage(), exp.getMessage());
        }
    }

    /**
     * Merge file cannot open input stream throw exception.
     */
    @Test
    void mergeFile_CannotOpenInputStream_ThrowException() {
        String[] files = {FILE_PATH_A};
        try (
                MockedStatic<IOUtils> mockedIo = mockStatic(IOUtils.class);
                MockedStatic<Files> mockedFiles = mockStatic(Files.class)
        ) {
            mockedIo.when(() -> IOUtils.resolveFilePath(any()))
                    .thenReturn(Paths.get(FOLDER1_PATH));
            mockedFiles.when(() -> Files.exists(any()))
                    .thenReturn(true);
            mockedFiles.when(() -> Files.isDirectory(any()))
                    .thenReturn(false);
            mockedIo.when(() -> IOUtils.openInputStream(any()))
                    .thenThrow(new ShellException(TEST_STR));
            Throwable exp = assertThrows(PasteException.class, ()-> pasteApplication.mergeFile(false, files));
            assertEquals(new PasteException(new ShellException(TEST_STR)).getMessage(), exp.getMessage());
        }
    }

    /**
     * Merge file cannot get lines from stream throw exception.
     */
    @Test
    void mergeFile_CannotGetLinesFromStream_ThrowException() {
        String[] files = {FILE_PATH_A};
        try (
                MockedStatic<IOUtils> mockedIo = mockStatic(IOUtils.class);
                MockedStatic<Files> mockedFiles = mockStatic(Files.class)
        ) {
            mockedIo.when(() -> IOUtils.resolveFilePath(any()))
                    .thenReturn(Paths.get(FOLDER1_PATH));
            mockedFiles.when(() -> Files.exists(any()))
                    .thenReturn(true);
            mockedFiles.when(() -> Files.isDirectory(any()))
                    .thenReturn(false);
            mockedIo.when(() -> IOUtils.openInputStream(any()))
                    .thenReturn(mock(InputStream.class));
            mockedIo.when(() -> IOUtils.getLinesFromInputStream(any()))
                    .thenThrow(new IOException(TEST_STR));
            Throwable exp = assertThrows(PasteException.class, ()-> pasteApplication.mergeFile(false, files));
            assertEquals(new PasteException(new IOException(TEST_STR)).getMessage(), exp.getMessage());
        }
    }

    /**
     * Merge file one file arg echo stdin arg.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void mergeFile_OneFileArg_EchoStdinArg() throws AbstractApplicationException {
        try (
                MockedStatic<IOUtils> mockedIo = mockStatic(IOUtils.class);
                MockedStatic<Files> mockedFiles = mockStatic(Files.class)
        ) {
            mockedFiles.when(() -> Files.exists(any()))
                    .thenReturn(true);
            mockedFiles.when(() -> Files.isDirectory(any()))
                    .thenReturn(false);
            mockedIo.when(() -> IOUtils.resolveFilePath(any()))
                    .thenReturn(Paths.get(FILE_PATH_A));
            mockedIo.when(() -> IOUtils.openInputStream(any()))
                    .thenReturn(mock(InputStream.class));
            mockedIo.when(() -> IOUtils.getLinesFromInputStream(any()))
                    .thenReturn(Arrays.asList(LINES1));
            String[] files = {FILE_PATH_A};
            String output = pasteApplication.mergeFile(false, files);
            assertEquals(L1_EXPECTED, output);
        }
    }

    /**
     * Merge file one stdin arg option sep by tab.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void mergeFile_OneStdinArgOption_SepByTab() throws AbstractApplicationException {
        try (
                MockedStatic<IOUtils> mockedIo = mockStatic(IOUtils.class);
                MockedStatic<Files> mockedFiles = mockStatic(Files.class)
        ) {
            mockedFiles.when(() -> Files.exists(any()))
                    .thenReturn(true);
            mockedFiles.when(() -> Files.isDirectory(any()))
                    .thenReturn(false);
            mockedIo.when(() -> IOUtils.resolveFilePath(any()))
                    .thenReturn(Paths.get(FILE_PATH_A));
            mockedIo.when(() -> IOUtils.openInputStream(any()))
                    .thenReturn(mock(InputStream.class));
            mockedIo.when(() -> IOUtils.getLinesFromInputStream(any()))
                    .thenReturn(Arrays.asList(LINES1));
            String[] files = {FILE_PATH_A};
            String output = pasteApplication.mergeFile(true, files);
            assertEquals(L1_SERIAL, output);
        }
    }

    /**
     * MergeFileAndStdin null fileName throws exception
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void mergeFileAndStdin_NullFileName_ThrowException() {
        InputStream inputStream = mock(InputStream.class);
        String[] files = null;
        Throwable exp = assertThrows(PasteException.class, () -> pasteApplication.mergeFileAndStdin(false, inputStream, files));
        assertEquals(new PasteException(ERR_NULL_ARGS).getMessage(), exp.getMessage());
    }

    /**
     * Merge file and stdin null stdin throw exception.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void mergeFileAndStdin_NullStdin_ThrowException() {
        String[] files = {FILE_PATH_A};
        Throwable exp = assertThrows(PasteException.class, () -> pasteApplication.mergeFileAndStdin(false, null, files));
        assertEquals(new PasteException(ERR_NULL_STREAMS).getMessage(), exp.getMessage());
    }

    /**
     * Merge file and stdin length 0 file names throw exception.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void mergeFileAndStdin_Length0FileNames_ThrowException() {
        String[] files = {};
        InputStream inputStream = mock(InputStream.class);
        Throwable exp = assertThrows(PasteException.class, () -> pasteApplication.mergeFileAndStdin(false, inputStream, files));
        assertEquals(new PasteException(ERR_NO_ARGS).getMessage(), exp.getMessage());
    }

    /**
     * Merge file and stdin blank file name in file names throw exception.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void mergeFileAndStdin_EmptyFileName_ThrowsException() {
        String[] files = {BLANK_FILE};
        InputStream inputStream = mock(InputStream.class);
        Throwable exp = assertThrows(PasteException.class, () -> pasteApplication.mergeFileAndStdin(false, inputStream, files));
        assertEquals(new PasteException(ERR_NO_FILE_ARGS).getMessage(), exp.getMessage());
    }

    /**
     * Merge file and stdin cannot open stdin throw exception.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void mergeFileAndStdin_CannotGetLinesFromInputStream_ThrowException() {
        try (MockedStatic<IOUtils> mockedIo = mockStatic(IOUtils.class)) {
            InputStream input = mock(InputStream.class);
            mockedIo.when(() -> IOUtils.getLinesFromInputStream(any()))
                    .thenThrow(new IOException(TEST_STR));
            String[] files = {STDIN_ARG};
            Throwable exp = assertThrows(PasteException.class, () -> pasteApplication.mergeFileAndStdin(false, input, files));
            assertEquals(new PasteException(new IOException(TEST_STR)).getMessage(), exp.getMessage());
        }
    }

    /**
     * Merge file and stdin cannot open stdin serial throw exception.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void mergeFileAndStdin_CannotGetLinesFromInputStreamSerial_ThrowException() {
        try (MockedStatic<IOUtils> mockedIo = mockStatic(IOUtils.class)) {
            InputStream input = mock(InputStream.class);
            mockedIo.when(() -> IOUtils.getLinesFromInputStream(any()))
                    .thenThrow(new IOException(TEST_STR));
            String[] files = {STDIN_ARG};
            Throwable exp = assertThrows(PasteException.class, () -> pasteApplication.mergeFileAndStdin(true, input, files));
            assertEquals(new PasteException(new IOException(TEST_STR)).getMessage(), exp.getMessage());
        }
    }

    /**
     * Merge file and stdin non exist file name throw exception.
     */
    @Test
    void mergeFileAndStdin_NonExistFileName_ThrowException() {
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.exists(any()))
                    .thenReturn(false);
            String[] files = {MISSING_FILE_PATH};
            InputStream inputStream = mock(InputStream.class);
            Throwable exp = assertThrows(PasteException.class, () -> pasteApplication.mergeFileAndStdin(false, inputStream, files));
            assertEquals(new PasteException(MISSING_FILE_PATH, ERR_FILE_NOT_FND).getMessage(), exp.getMessage());
        }
    }

    /**
     * Merge file and stdin input file is dir throw exception.
     */
    @Test
    void mergeFileAndStdin_InputFileIsDir_ThrowException() {
        try (
                MockedStatic<IOUtils> mockedIo = mockStatic(IOUtils.class);
                MockedStatic<Files> mockedFiles = mockStatic(Files.class)
        ) {
            mockedIo.when(() -> IOUtils.resolveFilePath(any()))
                    .thenReturn(Paths.get(FOLDER1_PATH));
            mockedFiles.when(() -> Files.exists(any()))
                    .thenReturn(true);
            mockedFiles.when(() -> Files.isDirectory(any()))
                    .thenReturn(true);
            String[] files = {FOLDER1_PATH};
            InputStream inputStream = mock(InputStream.class);
            Throwable exp = assertThrows(PasteException.class, () -> pasteApplication.mergeFileAndStdin(false, inputStream, files));
            assertEquals(new PasteException(FOLDER1_PATH, ERR_IS_DIR_FILE).getMessage(), exp.getMessage());
        }
    }

    /**
     * Merge file and stdin cannot open file input stream throw exception.
     */
    @Test
    void mergeFileAndStdin_CannotOpenFileInputStream_ThrowException() {
        try (
                MockedStatic<IOUtils> mockedIo = mockStatic(IOUtils.class);
                MockedStatic<Files> mockedFiles = mockStatic(Files.class)
        ) {
            mockedIo.when(() -> IOUtils.resolveFilePath(any()))
                    .thenReturn(Paths.get(FOLDER1_PATH));
            mockedFiles.when(() -> Files.exists(any()))
                    .thenReturn(true);
            mockedFiles.when(() -> Files.isDirectory(any()))
                    .thenReturn(false);
            mockedIo.when(() -> IOUtils.openInputStream(any()))
                    .thenThrow(new ShellException(TEST_STR));
            String[] files = {FILE_PATH_A};
            InputStream inputStream = mock(InputStream.class);
            Throwable exp = assertThrows(PasteException.class, () -> pasteApplication.mergeFileAndStdin(false, inputStream, files));
            assertEquals(new PasteException(new ShellException(TEST_STR)).getMessage(), exp.getMessage());
        }
    }

    /**
     * Merge file and stdin cannot get lines from file stream throw exception.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void mergeFileAndStdin_CannotGetLinesFromFileStream_ThrowException() {
        try (
                MockedStatic<IOUtils> mockedIo = mockStatic(IOUtils.class);
                MockedStatic<Files> mockedFiles = mockStatic(Files.class)
        ) {
            mockedIo.when(() -> IOUtils.resolveFilePath(any()))
                    .thenReturn(Paths.get(FOLDER1_PATH));
            mockedFiles.when(() -> Files.exists(any()))
                    .thenReturn(true);
            mockedFiles.when(() -> Files.isDirectory(any()))
                    .thenReturn(false);
            mockedIo.when(() -> IOUtils.openInputStream(any()))
                    .thenReturn(mock(InputStream.class));
            mockedIo.when(() -> IOUtils.getLinesFromInputStream(any()))
                    .thenThrow(new IOException(TEST_STR));
            String[] files = {FILE_PATH_A};
            InputStream inputStream = mock(InputStream.class);
            Throwable exp = assertThrows(PasteException.class, () -> pasteApplication.mergeFileAndStdin(false, inputStream, files));
            assertEquals(new PasteException(new IOException(TEST_STR)).getMessage(), exp.getMessage());
        }
    }

    /**
     * Merge file and stdin stdin one file same len lines sep by tab.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void mergeFileAndStdin_StdinOneFileSameLen_LinesSepByTab() throws AbstractApplicationException {
        try (
                MockedStatic<IOUtils> mockedIo = mockStatic(IOUtils.class);
                MockedStatic<Files> mockedFiles = mockStatic(Files.class)
        ) {
            mockedFiles.when(() -> Files.exists(any()))
                    .thenReturn(true);
            mockedFiles.when(() -> Files.isDirectory(any()))
                    .thenReturn(false);
            mockedIo.when(() -> IOUtils.resolveFilePath(any()))
                    .thenReturn(Paths.get(FILE_PATH_B));
            mockedIo.when(() -> IOUtils.openInputStream(any()))
                    .thenReturn(mock(InputStream.class));
            mockedIo.when(() -> IOUtils.getLinesFromInputStream(any()))
                    .thenReturn(Arrays.asList(LINES2)) // File input stream reaad first when not serial
                    .thenReturn(Arrays.asList(LINES1));
            String[] files = {STDIN_ARG, FILE_PATH_B};
            String output = pasteApplication.mergeFileAndStdin(false, mock(InputStream.class), files);
            assertEquals(L1_L2_MERGE_DEF, output);
        }
    }

    /**
     * Merge file and stdin stdin one file diff len lines sep by tab.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    // where number of lines in File > or < number of lines of stdin
    @Test
    void mergeFileAndStdin_StdinOneFileDiffLen_LinesSepByTab() throws AbstractApplicationException {
        try (
                MockedStatic<IOUtils> mockedIo = mockStatic(IOUtils.class);
                MockedStatic<Files> mockedFiles = mockStatic(Files.class)
        ) {
            mockedFiles.when(() -> Files.exists(any()))
                    .thenReturn(true);
            mockedFiles.when(() -> Files.isDirectory(any()))
                    .thenReturn(false);
            mockedIo.when(() -> IOUtils.resolveFilePath(any()))
                    .thenReturn(Paths.get(FILE_PATH_A));
            mockedIo.when(() -> IOUtils.openInputStream(any()))
                    .thenReturn(mock(InputStream.class));
            mockedIo.when(() -> IOUtils.getLinesFromInputStream(any()))
                    .thenReturn(Arrays.asList(LINES1)) // this is for file
                    .thenReturn(Arrays.asList(SINGLE_ARG)); // this is for stdin
            String[] files = {STDIN_ARG, FILE_PATH_B};
            String output = pasteApplication.mergeFileAndStdin(false, mock(InputStream.class), files);
            assertEquals(SINGLE_ARG_L2, output);
        }
    }

    /**
     * Merge file and stdin stdin one file option diff len 1 file at a time.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void mergeFileAndStdin_StdinOneFileOptionDiffLen_1FileAtATime()throws AbstractApplicationException {
        try (
                MockedStatic<IOUtils> mockedIo = mockStatic(IOUtils.class);
                MockedStatic<Files> mockedFiles = mockStatic(Files.class)
        ) {
            mockedFiles.when(() -> Files.exists(any()))
                    .thenReturn(true);
            mockedFiles.when(() -> Files.isDirectory(any()))
                    .thenReturn(false);
            mockedIo.when(() -> IOUtils.resolveFilePath(any()))
                    .thenReturn(Paths.get(FILE_PATH_A));
            mockedIo.when(() -> IOUtils.openInputStream(any()))
                    .thenReturn(mock(InputStream.class));
            mockedIo.when(() -> IOUtils.getLinesFromInputStream(any()))
                    .thenReturn(Arrays.asList(SINGLE_ARG))
                    .thenReturn(Arrays.asList(LINES1));
            String[] files = {STDIN_ARG, FILE_PATH_A};
            String output = pasteApplication.mergeFileAndStdin(true, mock(InputStream.class), files);
            assertEquals(SINGLE_L2_SERIAL, output);
        }
    }

    /**
     * Merge file and stdin stdin one file option same len serial.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    //BUG!!!
    @Test
    void mergeFileAndStdin_StdinOneFileOptionSameLen_1FileAtATime() throws AbstractApplicationException {
        try(
                MockedStatic<IOUtils> mockedIo = mockStatic(IOUtils.class);
                MockedStatic<Files> mockedFiles = mockStatic(Files.class)
        ){
            mockedFiles.when(() -> Files.exists(any()))
                    .thenReturn(true);
            mockedFiles.when(() -> Files.isDirectory(any()))
                    .thenReturn(false);
            mockedIo.when(() -> IOUtils.resolveFilePath(any()))
                    .thenReturn(Paths.get(FILE_PATH_B));
            mockedIo.when(() -> IOUtils.openInputStream(any()))
                    .thenReturn(mock(InputStream.class));
            mockedIo.when(() -> IOUtils.getLinesFromInputStream(any()))
                    .thenReturn(Arrays.asList(LINES1))
                    .thenReturn(Arrays.asList(LINES2));
            String[] files = {STDIN_ARG, FILE_PATH_B};
            String output = pasteApplication.mergeFileAndStdin(true, mock(InputStream.class), files);
            assertEquals(L1_L2_SERIAL, output);
        }
    }

    /**
     * Merge file and stdin dash, file and dash non serial, should return merged.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void mergeFileAndStdin_DashFileDashNotSerial_ReturnsMerged() throws AbstractApplicationException {
        try (
                MockedStatic<IOUtils> mockedIo = mockStatic(IOUtils.class);
                MockedStatic<Files> mockedFiles = mockStatic(Files.class)
        ) {
            mockedFiles.when(() -> Files.exists(any()))
                    .thenReturn(true);
            mockedFiles.when(() -> Files.isDirectory(any()))
                    .thenReturn(false);
            mockedIo.when(() -> IOUtils.resolveFilePath(any()))
                    .thenReturn(Paths.get(FILE_PATH_A));
            mockedIo.when(() -> IOUtils.openInputStream(any()))
                    .thenReturn(mock(InputStream.class));
            mockedIo.when(() -> IOUtils.getLinesFromInputStream(any()))
                    .thenReturn(Arrays.asList(LINES2)) // Files inputstream get read first
                    .thenReturn(Arrays.asList(LINES1)); // Stdin inputstream
            String[] files = {STDIN_ARG, FILE_PATH_A, STDIN_ARG};
            String output = pasteApplication.mergeFileAndStdin(false, mock(InputStream.class), files);
            assertEquals(D_F_D_RES, output);
        }
    }

    /**
     * Merge file and stdin dash, file and dash serial, should return merged.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void mergeFileAndStdin_DashFileDashSerial_ReturnsMerged() throws AbstractApplicationException {
        try (
                MockedStatic<IOUtils> mockedIo = mockStatic(IOUtils.class);
                MockedStatic<Files> mockedFiles = mockStatic(Files.class)
        ) {
            mockedFiles.when(() -> Files.exists(any()))
                    .thenReturn(true);
            mockedFiles.when(() -> Files.isDirectory(any()))
                    .thenReturn(false);
            mockedIo.when(() -> IOUtils.resolveFilePath(any()))
                    .thenReturn(Paths.get(FILE_PATH_A));
            mockedIo.when(() -> IOUtils.openInputStream(any()))
                    .thenReturn(mock(InputStream.class));
            mockedIo.when(() -> IOUtils.getLinesFromInputStream(any()))
                    .thenReturn(Arrays.asList(LINES1))
                    .thenReturn(Arrays.asList(LINES2))
                    .thenReturn(Arrays.asList(LINES3));
            String[] files = {STDIN_ARG, FILE_PATH_A, STDIN_ARG};
            String output = pasteApplication.mergeFileAndStdin(true, mock(InputStream.class), files);
            assertEquals(D_F_D_S_RES, output);
        }
    }

    /**
     * Merge file and stdin non serial get from stdin less lines than dashes merge correctly.
     * Checks that in the logic that assigns the list of lines for each '-' will break if the number of stdin input lines less
     * than dashes in args
     * @throws AbstractApplicationException
     */
    @Test
    void mergeFileAndStdin_NonSerialAndGetFromStdinLessLinesThanDashes_MergeCorrectly() throws AbstractApplicationException {
        try (
                MockedStatic<IOUtils> mockedIo = mockStatic(IOUtils.class);
                MockedStatic<Files> mockedFiles = mockStatic(Files.class)
        ) {
            mockedIo.when(() -> IOUtils.openInputStream(any()))
                    .thenReturn(mock(InputStream.class));
            mockedIo.when(() -> IOUtils.getLinesFromInputStream(any()))
                    .thenReturn(Arrays.asList(LINES1));
            String[] files = {STDIN_ARG, STDIN_ARG, STDIN_ARG, STDIN_ARG, STDIN_ARG};
            String output = pasteApplication.mergeFileAndStdin(false, mock(InputStream.class), files);
            assertEquals(L1_SERIAL + '\t', output);
        }
    }

    /**
     * Run input or output stream is null throws exception
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void run_StreamsNull_ThrowsException(boolean isStdinNull) {
        InputStream inputStream = isStdinNull ? null : mock(InputStream.class);
        OutputStream outputStream = isStdinNull ? mock(OutputStream.class) : null;
        String[] args = {FILE_PATH_A, FILE_PATH_B};
        Throwable exp = assertThrows(PasteException.class, () -> pasteApplication.run(args, inputStream, outputStream));
        assertEquals(new PasteException(ERR_NULL_STREAMS).getMessage(), exp.getMessage());
    }

    /**
     * Run null args in throw exception.
     */
    @Test
    void run_NullArgs_ThrowException() {
        InputStream inputStream = mock(InputStream.class);
        OutputStream outputStream = mock(OutputStream.class);
        Throwable exp = assertThrows(PasteException.class, ()-> pasteApplication.run(null, inputStream, outputStream));
        assertEquals(new PasteException(ERR_NULL_ARGS).getMessage(), exp.getMessage());
    }

    /**
     * Run illegal args parsed throws exception.
     *
     * @throws InvalidArgsException the invalid args exception
     */
    @Test
    void run_IllegalArgsParsed_ThrowsException() throws InvalidArgsException {
        InputStream inputStream = mock(InputStream.class);
        OutputStream outputStream = mock(OutputStream.class);
        String[] args = {ILLEGAL_ARG, FILE_PATH_A, FILE_PATH_B};
        doThrow(new InvalidArgsException(TEST_STR)).when(pasteParser).parse(args);
        Throwable exp = assertThrows(PasteException.class, ()-> pasteApplication.run(args, inputStream, outputStream));
        assertEquals(new PasteException(new InvalidArgsException(TEST_STR)).getMessage(), exp.getMessage());
    }

    /**
     * Run cannot merge file throw exception.
     *
     * @throws IOException
     * @throws AbstractApplicationException
     */
    @Test
    void run_CannotWriteToOutputStream_ThrowsException() throws IOException, AbstractApplicationException {
        InputStream inputStream = mock(InputStream.class);
        OutputStream outputStream = mock(OutputStream.class);
        doThrow(new IOException(TEST_STR)).when(outputStream).write(any());
        String[] args = {};
        when(pasteParser.isPasteStdin()).thenReturn(true);
        PasteApplication spyApp = spy(pasteApplication);
        doReturn(TEST_STR).when(spyApp).mergeStdin(anyBoolean(), any(InputStream.class));
        Throwable exp = assertThrows(PasteException.class, ()-> spyApp.run(args, inputStream, outputStream));
        assertEquals(new PasteException(new IOException(TEST_STR)).getMessage(), exp.getMessage());
    }

    /**
     * Run args is paste files merge file called.
     *
     * @throws AbstractApplicationException
     */
    @Test
    void run_ArgsIsPasteStdin_PasteStdinCalled() throws AbstractApplicationException, IOException {
        InputStream inputStream = mock(InputStream.class);
        OutputStream outputStream = mock(OutputStream.class);
        String[] args = {};
        when(pasteParser.isPasteStdin()).thenReturn(true);
        PasteApplication spyApp = spy(pasteApplication);
        doReturn(TEST_STR).when(spyApp).mergeStdin(anyBoolean(), any(InputStream.class));
        spyApp.run(args, inputStream, outputStream);
        verify(spyApp, times(1)).mergeStdin(anyBoolean(), any(InputStream.class));
        verify(outputStream, times(1)).write(TEST_STR.getBytes());
        verify(outputStream, times(1)).write(STRING_NEWLINE.getBytes());
    }

    /**
     * Run args is paste files merge file called.
     *
     * @throws AbstractApplicationException
     */
    @Test
    void run_ArgsIsPasteFiles_MergeFileCalled() throws AbstractApplicationException, IOException {
        InputStream inputStream = mock(InputStream.class);
        OutputStream outputStream = mock(OutputStream.class);
        String[] args = {FILE_PATH_A, FILE_PATH_B};
        when(pasteParser.isPasteFiles()).thenReturn(true);
        PasteApplication spyApp = spy(pasteApplication);
        doReturn(TEST_STR).when(spyApp).mergeFile(anyBoolean(), any(String[].class));
        spyApp.run(args, inputStream, outputStream);
        verify(spyApp, times(1)).mergeFile(anyBoolean(), any(String[].class));
        verify(outputStream, times(1)).write(TEST_STR.getBytes());
        verify(outputStream, times(1)).write(STRING_NEWLINE.getBytes());
    }

    /**
     * Run args is paste file and stdin merge file and stdin called.
     *
     * @throws AbstractApplicationException
     */
    @Test
    void run_ArgsIsPasteFileAndStdin_MergeFileAndStdinCalled() throws AbstractApplicationException, IOException {
        InputStream inputStream = mock(InputStream.class);
        OutputStream outputStream = mock(OutputStream.class);
        String[] args = {FILE_PATH_A, STDIN_ARG};
        when(pasteParser.isPasteFileAndStdin()).thenReturn(true);
        PasteApplication spyApp = spy(pasteApplication);
        doReturn(TEST_STR).when(spyApp).mergeFileAndStdin(anyBoolean(), any(InputStream.class), any(String[].class));
        spyApp.run(args, inputStream, outputStream);
        verify(spyApp, times(1)).mergeFileAndStdin(anyBoolean(), any(InputStream.class), any(String[].class));
        verify(outputStream, times(1)).write(TEST_STR.getBytes());
        verify(outputStream, times(1)).write(STRING_NEWLINE.getBytes());
    }
}
