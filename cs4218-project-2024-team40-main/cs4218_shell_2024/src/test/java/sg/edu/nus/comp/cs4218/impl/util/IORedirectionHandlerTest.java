
package sg.edu.nus.comp.cs4218.impl.util;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;


import java.io.*;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.mockito.MockedStatic;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_REDIR_INPUT;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_REDIR_OUTPUT;

/**
 * The type Io redirection handler test.
 */
public class IORedirectionHandlerTest {
    /**
     * The Io redirect handler.
     */
// mock arguement resolver, IO Utils
    IORedirectionHandler ioRedirectHandler;
    /**
     * The Argument resolver.
     */
    ArgumentResolver argumentResolver;

    private InputStream stdin;
    private OutputStream stdout;
    private static final String BASE_DIR = Environment.currentDirectory;
    private static final String TEST_RESOURCE_DIR = "src/test/resources/app";
    private static final String TEST_DIR = Environment.currentDirectory + File.separator + TEST_RESOURCE_DIR + File.separator + "IORedirectionHandlerTest";
    private static final Path TEST_PATH = Path.of(TEST_DIR);
    private static final String FILE_1 = "file1.txt";
    private static final String FILE_2 = "file2.txt";
    private static final String NON_EXIST_FILE = "nonExist.txt"; // does not exist
    private static final Path PATH_1 = Path.of(TEST_DIR, FILE_1);
    private static final Path PATH_2 = Path.of(TEST_DIR, FILE_2);
    private static final Path PATH_3 = Path.of(TEST_DIR, NON_EXIST_FILE);
    private static final String SHELL_PREFIX = "shell: ";

    private static void deleteDirectory(File directory) {
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
    private static Path resolveFilePath(String fileName) {
    Path currentDirectory = Paths.get(Environment.currentDirectory);
    return currentDirectory.resolve(fileName);
    }

    /**
     * Sets before all.
     *
     * @throws IOException the io exception
     */
    @BeforeAll
    static void setupBeforeAll() throws IOException {
    if (Files.notExists(TEST_PATH)) {
        Files.createDirectory(TEST_PATH);
    }
    if (Files.notExists(PATH_1)) {
        Files.createFile(PATH_1);
    }
    if (Files.notExists(PATH_2)) {
        Files.createFile(PATH_2);
    }
    Environment.currentDirectory = TEST_DIR;
    }

    /**
     * Sets .
     */
    @BeforeEach
    void setup() {
    argumentResolver = mock(ArgumentResolver.class);

    stdout = new ByteArrayOutputStream();
    stdin = new ByteArrayInputStream("".getBytes());
    }


    /**
     * Tear down after all.
     */
    @AfterAll
    static void tearDownAfterAll() {
    Environment.currentDirectory = BASE_DIR;
    deleteDirectory(new File(TEST_DIR));
    }


    /**
     * Is redir operator redir operator return true.
     *
     * @param chr the chr
     */
    @ParameterizedTest
    @ValueSource(strings = {">", "<"})
    void isRedirOperator_RedirOperator_ReturnTrue(String chr){
    ioRedirectHandler = new IORedirectionHandler(List.of(FILE_1, FILE_2), stdin, stdout, argumentResolver);
    assertTrue(() -> ioRedirectHandler.isRedirOperator(chr));
    }

    /**
     * Is redir operator non redir operator return true.
     *
     * @param chr the chr
     */
    @ParameterizedTest
    @ValueSource(strings = {"a.txt", "b.txt", "!", "*", "?"})
    void isRedirOperator_NonRedirOperator_ReturnTrue(String chr){
    ioRedirectHandler = new IORedirectionHandler(List.of(FILE_1, FILE_2), stdin, stdout, argumentResolver);
    assertFalse(ioRedirectHandler.isRedirOperator(chr));
    }

    /**
     * Extract redir options valid no redir return all file.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws ShellException               the shell exception
     * @throws IOException                  the io exception
     */
    @Test
    void extractRedirOptions_ValidNoRedir_ReturnAllFile() throws AbstractApplicationException, ShellException, IOException {
        // mock RegexArgument

        ioRedirectHandler = new IORedirectionHandler(List.of(FILE_1, FILE_2), stdin, stdout, argumentResolver);
        when(argumentResolver.resolveOneArgument(FILE_1, new RegexArgument())).thenReturn(List.of(FILE_1));
        when(argumentResolver.resolveOneArgument(FILE_2, new RegexArgument())).thenReturn(List.of(FILE_2));
        try(MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
            mockedStatic.when(() -> IOUtils.openInputStream(any(String.class)))
                    .thenReturn(new FileInputStream(resolveFilePath(FILE_2).toString()));
        }
        assertDoesNotThrow(() -> ioRedirectHandler.extractRedirOptions());
        List<String> noRedirArgsList = ioRedirectHandler.getNoRedirArgsList();
        List<String> expected = Arrays.asList(FILE_1,FILE_2);
        assertEquals(expected, noRedirArgsList);

    }

    /**
     * Extract redir options valid input return file before 1 st redir.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws ShellException               the shell exception
     * @throws IOException                  the io exception
     */
    @Disabled
    @Test
    void extractRedirOptions_ValidInput_ReturnFileBefore1stRedir() throws AbstractApplicationException, ShellException, IOException {
    ioRedirectHandler = new IORedirectionHandler(List.of(FILE_1, String.valueOf(CHAR_REDIR_INPUT), FILE_2), stdin, stdout, argumentResolver);
    when(argumentResolver.resolveOneArgument(FILE_1, new RegexArgument())).thenReturn(List.of(FILE_1));
    when(argumentResolver.resolveOneArgument(FILE_2, new RegexArgument())).thenReturn(List.of(FILE_2));
    when(argumentResolver.resolveOneArgument(String.valueOf(CHAR_REDIR_INPUT), new RegexArgument())).thenReturn(List.of(String.valueOf(CHAR_REDIR_INPUT)));
    try(MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
        mockedStatic.when(() -> IOUtils.openInputStream(any(String.class)))
                .thenReturn(new FileInputStream(resolveFilePath(FILE_2).toString()));
    }
    assertDoesNotThrow(() -> ioRedirectHandler.extractRedirOptions());
    List<String> noRedirArgsList = ioRedirectHandler.getNoRedirArgsList();
    List<String> expected = List.of(FILE_1);
    assertEquals(expected, noRedirArgsList);

    }

    /**
     * Extract redir options valid output not exist create output file and return file before 1 st redir.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws ShellException               the shell exception
     * @throws IOException                  the io exception
     */
    @Disabled
    @Test
    void extractRedirOptions_ValidOutputNotExist_CreateOutputFileAndReturnFileBefore1stRedir() throws AbstractApplicationException, ShellException, IOException {
    ioRedirectHandler = new IORedirectionHandler(List.of(FILE_1, String.valueOf(CHAR_REDIR_OUTPUT), NON_EXIST_FILE), stdin, stdout, argumentResolver);
    when(argumentResolver.resolveOneArgument(FILE_1, new RegexArgument())).thenReturn(List.of(FILE_1));
    when(argumentResolver.resolveOneArgument(NON_EXIST_FILE, new RegexArgument())).thenReturn(List.of(NON_EXIST_FILE));
    when(argumentResolver.resolveOneArgument(String.valueOf(CHAR_REDIR_OUTPUT), new RegexArgument())).thenReturn(List.of(String.valueOf(CHAR_REDIR_OUTPUT)));
    try(MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
        mockedStatic.when(() -> IOUtils.openOutputStream(NON_EXIST_FILE))
                .thenReturn(new FileOutputStream(resolveFilePath(NON_EXIST_FILE).toString()));
    }
    assertDoesNotThrow(() -> ioRedirectHandler.extractRedirOptions());
    List<String> noRedirArgsList = ioRedirectHandler.getNoRedirArgsList();
    List<String> expected = Arrays.asList(FILE_1);
    assertEquals(expected, noRedirArgsList);
    assertTrue(Files.exists(PATH_3));
    }

    /**
     * Extract redir options nullargs throw exception.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws ShellException               the shell exception
     * @throws IOException                  the io exception
     */
    @Test
    void extractRedirOptions_Nullargs_ThrowException() throws AbstractApplicationException, ShellException, IOException {
        ioRedirectHandler = new IORedirectionHandler(null, stdin, stdout, argumentResolver);
        when(argumentResolver.resolveOneArgument(FILE_1, new RegexArgument())).thenReturn(List.of(FILE_1));
        when(argumentResolver.resolveOneArgument(FILE_2, new RegexArgument())).thenReturn(List.of(FILE_2));
        when(argumentResolver.resolveOneArgument(String.valueOf(CHAR_REDIR_INPUT), new RegexArgument())).thenReturn(List.of(String.valueOf(CHAR_REDIR_INPUT)));
        try(MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
            mockedStatic.when(() -> IOUtils.openInputStream(any(String.class)))
                    .thenReturn(new FileInputStream(resolveFilePath(FILE_2).toString()));
        }
        Throwable exp = assertThrows(ShellException.class, () -> ioRedirectHandler.extractRedirOptions());
        assertEquals(SHELL_PREFIX + ERR_SYNTAX, exp.getMessage());

    }

    /**
     * Extract redir options valid input output return file before 1 st redir.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws ShellException               the shell exception
     * @throws IOException                  the io exception
     */
    @Disabled
    @Test
    void extractRedirOptions_ValidInputOutput_ReturnFileBefore1stRedir() throws AbstractApplicationException, ShellException, IOException {
        ioRedirectHandler = new IORedirectionHandler(List.of(FILE_1, String.valueOf(CHAR_REDIR_INPUT), FILE_2, String.valueOf(CHAR_REDIR_OUTPUT), FILE_1), stdin, stdout, argumentResolver);
        when(argumentResolver.resolveOneArgument(FILE_1, new RegexArgument())).thenReturn(List.of(FILE_1));
        when(argumentResolver.resolveOneArgument(FILE_2, new RegexArgument())).thenReturn(List.of(FILE_2));
        when(argumentResolver.resolveOneArgument(String.valueOf(CHAR_REDIR_INPUT), new RegexArgument())).thenReturn(List.of(String.valueOf(CHAR_REDIR_INPUT)));
        when(argumentResolver.resolveOneArgument(String.valueOf(CHAR_REDIR_OUTPUT), new RegexArgument())).thenReturn(List.of(String.valueOf(CHAR_REDIR_OUTPUT)));
        try(MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
            mockedStatic.when(() -> IOUtils.openInputStream(any(String.class)))
                    .thenReturn(new FileInputStream(resolveFilePath(FILE_2).toString()));
            mockedStatic.when(() -> IOUtils.openOutputStream(any(String.class)))
                    .thenReturn(new FileOutputStream(resolveFilePath(FILE_1).toString()));
        }
        assertDoesNotThrow(() -> ioRedirectHandler.extractRedirOptions());
        List<String> noRedirArgsList = ioRedirectHandler.getNoRedirArgsList();
        List<String> expected = List.of(FILE_1);
        assertEquals(expected, noRedirArgsList);

    }

    /**
     * Extract redir options file have special char throw exception.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws ShellException               the shell exception
     * @throws IOException                  the io exception
     */
    @Test
    void extractRedirOptions_FileHaveSpecialChar_ThrowException() throws AbstractApplicationException, ShellException, IOException {
        ioRedirectHandler = new IORedirectionHandler(List.of(FILE_1, String.valueOf(CHAR_REDIR_OUTPUT), NON_EXIST_FILE), stdin, stdout, argumentResolver);
        when(argumentResolver.resolveOneArgument(any(), any())).thenReturn(List.of(FILE_1, "`"));
//
        Throwable exp = assertThrows(ShellException.class, () -> ioRedirectHandler.extractRedirOptions());
        assertEquals(SHELL_PREFIX + ERR_SYNTAX, exp.getMessage());

    }

    /**
     * Extract redir options conseq redir out throw exception.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws ShellException               the shell exception
     * @throws IOException                  the io exception
     */
    @Test
    void extractRedirOptions_ConseqRedirOut_ThrowException() throws AbstractApplicationException, ShellException, IOException {
        ioRedirectHandler = new IORedirectionHandler(List.of(FILE_1, String.valueOf(CHAR_REDIR_OUTPUT),String.valueOf(CHAR_REDIR_OUTPUT), FILE_2), stdin, stdout, argumentResolver);
        when(argumentResolver.resolveOneArgument(FILE_1, new RegexArgument())).thenReturn(List.of(FILE_1));
        when(argumentResolver.resolveOneArgument(FILE_2, new RegexArgument())).thenReturn(List.of(FILE_2));
        when(argumentResolver.resolveOneArgument(String.valueOf(CHAR_REDIR_OUTPUT), new RegexArgument())).thenReturn(List.of(String.valueOf(CHAR_REDIR_OUTPUT)));
        try(MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
            mockedStatic.when(() -> IOUtils.openInputStream(any(String.class)))
                    .thenReturn(new FileInputStream(resolveFilePath(FILE_2).toString()));
        }
        Throwable exp = assertThrows(ShellException.class, () -> ioRedirectHandler.extractRedirOptions());
        assertEquals(SHELL_PREFIX + ERR_SYNTAX, exp.getMessage());

    }

    /**
     * Extract redir options conseq redir in throw exception.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws ShellException               the shell exception
     * @throws IOException                  the io exception
     */
    @Test
    void extractRedirOptions_ConseqRedirIn_ThrowException() throws AbstractApplicationException, ShellException, IOException {
        ioRedirectHandler = new IORedirectionHandler(List.of(FILE_1, String.valueOf(CHAR_REDIR_INPUT),String.valueOf(CHAR_REDIR_INPUT), FILE_2), stdin, stdout, argumentResolver);
        when(argumentResolver.resolveOneArgument(FILE_1, new RegexArgument())).thenReturn(List.of(FILE_1));
        when(argumentResolver.resolveOneArgument(FILE_2, new RegexArgument())).thenReturn(List.of(FILE_2));
        when(argumentResolver.resolveOneArgument(String.valueOf(CHAR_REDIR_INPUT),new RegexArgument())).thenReturn(List.of(String.valueOf(CHAR_REDIR_INPUT)));
        try(MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
            mockedStatic.when(() -> IOUtils.openInputStream(any(String.class)))
                    .thenReturn(new FileInputStream(resolveFilePath(FILE_2).toString()));
        }
        Throwable exp = assertThrows(ShellException.class, () -> ioRedirectHandler.extractRedirOptions());
        assertEquals(SHELL_PREFIX + ERR_SYNTAX, exp.getMessage());

    }


}