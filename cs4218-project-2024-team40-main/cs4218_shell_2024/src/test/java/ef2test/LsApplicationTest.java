package ef2test;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.LsException;
import sg.edu.nus.comp.cs4218.impl.app.LsApplication;
import sg.edu.nus.comp.cs4218.impl.parser.LsArgsParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

/**
 * Tests Functionality of ls application
 */
public class LsApplicationTest {

    public static final String TEST = "test";

    public static final String VALID = "valid";

    public static final String VALID_1 = "valid1";
    private static final String RESOURCE_DIR = Paths.get("src", TEST, "resources", "app",
            "LsApplicationTest").toAbsolutePath().toString();

    public static final String VALID_1_FILE = "valid1.txt";
    public static final String VALID_1_PREFIX = "valid1:";

    public static final String A_A = "a.a";

    public static final String A_B = "a.b";

    public static final String VALID_FILE_TXT = "validFile.txt";

    public static final String VALID_2 = "valid2";

    public static final String VALID_FILE_2_TXT = "validFile2.txt";

    public static final String B_A = "b.a";

    public static final String VALID_PREFIX = "valid:";

    public static final String VALID_FILE_3_TXT = "validFile3.txt";

    public static final String VALID_1_2_PREFIX = "valid" + File.separator + "valid2:";

    public static final String INVALID = "invalid";
    public static final String CUR_DIR_DOT = ".";
    public static final String ROOT_FILES = "a.a" + STRING_NEWLINE + "a.b" + STRING_NEWLINE + "valid" + STRING_NEWLINE
            + "valid1" + STRING_NEWLINE + "validFile.txt";
    private LsApplication lsApplication;
    private LsArgsParser lsArgsParser;
    private InputStream inputStream;
    private OutputStream outputStream;
    private String originalDirectory;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        lsArgsParser = mock(LsArgsParser.class);
        lsApplication = new LsApplication(lsArgsParser);
        inputStream = mock(InputStream.class);
        outputStream = mock(OutputStream.class);
        originalDirectory = Environment.currentDirectory;
        Environment.currentDirectory = RESOURCE_DIR;
    }

    /**
     * Tear down.
     */
    @AfterEach
    void tearDown() {
        Environment.currentDirectory = originalDirectory;
    }

    /**
     * Run no errors prints to stdout.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     */
    @Test
    void run_NoErrors_PrintsToStdout() throws AbstractApplicationException, IOException {
        doReturn(List.of(TEST)).when(lsArgsParser).getDirectories();
        LsApplication spyApplication = spy(new LsApplication(lsArgsParser));
        doReturn(TEST).when(spyApplication).listFolderContent(anyBoolean(), anyBoolean(),
                any(String[].class));
        String[] args = new String[0];
        spyApplication.run(args, inputStream, outputStream);
        verify(outputStream, times(1)).write(TEST.getBytes());
    }

    static Stream<Arguments> oneFileAndIsCurDirEdgeCaseProvider() {
        return Stream.of(
                Arguments.of((Object) new String[]{CUR_DIR_DOT}),
                Arguments.of((Object) new String[]{RESOURCE_DIR})
        );
    }

    /**
     * List folder content one file and is cur dir prints file, without starting with './: or [CURDIR]/:'
     * @param args
     * @throws AbstractApplicationException
     */
    @ParameterizedTest
    @MethodSource("oneFileAndIsCurDirEdgeCaseProvider")
    void listFolderContent_OneFileAndIsCurDir_PrintsFile(String... args) throws AbstractApplicationException {
        String result = lsApplication.listFolderContent(false, false, args);
        assertEquals(ROOT_FILES, result);
    }

    /**
     * The type Absolute and relative file path test.
     */
    @Nested
    class AbsoluteAndRelativeFilePathTest {
        /**
         * List folder content absolute file path prints file.
         *
         * @throws AbstractApplicationException the abstract application exception
         */
        @Test
        void listFolderContent_AbsoluteFilePath_PrintsFile() throws AbstractApplicationException {
            String path = Paths.get("src", TEST, "resources", "app",
                    "LsApplicationTest", VALID_1).toAbsolutePath().toString();
            String result = lsApplication.listFolderContent(false, false, path);
            assertEquals(VALID_1_PREFIX + System.lineSeparator() + VALID_1_FILE, result);
        }

        /**
         * List folder content relative file path prints file.
         *
         * @throws AbstractApplicationException the abstract application exception
         */
        @Test
        void listFolderContent_RelativeFilePath_PrintsFile() throws AbstractApplicationException {
            String result = lsApplication.listFolderContent(false, false, VALID_1);
            assertEquals(VALID_1_PREFIX + System.lineSeparator() + VALID_1_FILE, result);
        }
    }

    /**
     * The type E flag positive tests.
     */
    @Nested
    class EFlagPositiveTests {
        /**
         * List folder content no folder names prints cwd content sorted by ext.
         *
         * @throws AbstractApplicationException the abstract application exception
         */
        @Test
        void listFolderContent_NoFolderNames_PrintsCwdContentSortedByExt() throws AbstractApplicationException {
            String result = lsApplication.listFolderContent(false, true);
            assertEquals(VALID + System.lineSeparator() + VALID_1 + System.lineSeparator() + A_A
                    + System.lineSeparator() + A_B + System.lineSeparator() + VALID_FILE_TXT, result);
        }

        /**
         * List folder content one valid folder name prints folder content sorted by ext.
         *
         * @throws AbstractApplicationException the abstract application exception
         */
        @Test
        void listFolderContent_OneValidFolderName_PrintsFolderContentSortedByExt() throws AbstractApplicationException {
            String result = lsApplication.listFolderContent(false, true, VALID);
            assertEquals(VALID_PREFIX + System.lineSeparator() + VALID_2 + System.lineSeparator() + B_A
                    + System.lineSeparator() + A_B + System.lineSeparator() + VALID_FILE_2_TXT, result);
        }

        /**
         * List folder content multiple valid folder names prints folder content sorted by ext.
         *
         * @throws AbstractApplicationException the abstract application exception
         */
        @Test
        void listFolderContent_MultipleValidFolderNames_PrintsFolderContentSortedByExt()
                throws AbstractApplicationException {
            String result = lsApplication.listFolderContent(false, true, VALID,
                    VALID_1);
            assertEquals(VALID_PREFIX + System.lineSeparator() + VALID_2 + System.lineSeparator() + B_A
                    + System.lineSeparator() + A_B + System.lineSeparator() + VALID_FILE_2_TXT
                    + System.lineSeparator() + System.lineSeparator() + VALID_1_PREFIX + System.lineSeparator()
                    + VALID_1_FILE, result);
        }

        /**
         * List folder content directory is file prints file.
         *
         * @throws AbstractApplicationException the abstract application exception
         */
        @Test
        void listFolderContent_DirectoryIsFile_PrintsFile() throws AbstractApplicationException {
            String result = lsApplication.listFolderContent(false, true,
                    VALID_FILE_TXT);
            assertEquals(String.format(VALID_FILE_TXT, VALID_FILE_TXT), result);
        }
    }

    /**
     * The type Recursive flag positive tests.
     */
    @Nested
    class RecursiveFlagPositiveTests {
        /**
         * List folder content no folder names prints cwd content recursive.
         *
         * @throws AbstractApplicationException the abstract application exception
         */
        @Test
        void listFolderContent_NoFolderNames_PrintsCwdContentRecursive() throws AbstractApplicationException {
            String result = lsApplication.listFolderContent(true, false);
            assertEquals("." + File.separator + ":" + System.lineSeparator() + A_A + System.lineSeparator() + A_B
                    + System.lineSeparator() + VALID + System.lineSeparator() + VALID_1 + System.lineSeparator()
                    + VALID_FILE_TXT + System.lineSeparator() + System.lineSeparator() + VALID_PREFIX
                    + System.lineSeparator() + A_B + System.lineSeparator()
                    + B_A + System.lineSeparator() + VALID_2 + System.lineSeparator() + VALID_FILE_2_TXT
                    + System.lineSeparator() + System.lineSeparator() + VALID_1_2_PREFIX + System.lineSeparator()
                    + VALID_FILE_3_TXT + System.lineSeparator() + System.lineSeparator() + VALID_1_PREFIX
                    + System.lineSeparator() + VALID_1_FILE, result);
        }

        /**
         * List folder content one valid folder name prints folder content recursive.
         *
         * @throws AbstractApplicationException the abstract application exception
         */
        @Test
        void listFolderContent_OneValidFolderName_PrintsFolderContentRecursive() throws AbstractApplicationException {
            String result = lsApplication.listFolderContent(true, false, VALID);
            assertEquals(VALID_PREFIX
                    + System.lineSeparator() + A_B + System.lineSeparator()
                    + B_A + System.lineSeparator() + VALID_2 + System.lineSeparator() + VALID_FILE_2_TXT
                    + System.lineSeparator() + System.lineSeparator() + VALID_1_2_PREFIX + System.lineSeparator()
                    + VALID_FILE_3_TXT, result);
        }

        /**
         * List folder content multiple valid folder names prints folder content recursive.
         *
         * @throws AbstractApplicationException the abstract application exception
         */
        @Test
        void listFolderContent_MultipleValidFolderNames_PrintsFolderContentRecursive() throws AbstractApplicationException {
            String result = lsApplication.listFolderContent(true, false, VALID,
                    VALID_1);
            assertEquals(VALID_PREFIX + System.lineSeparator() + A_B + System.lineSeparator()
                    + B_A + System.lineSeparator() + VALID_2 + System.lineSeparator() + VALID_FILE_2_TXT
                    + System.lineSeparator() + System.lineSeparator() + VALID_1_2_PREFIX + System.lineSeparator()
                    + VALID_FILE_3_TXT + System.lineSeparator() + System.lineSeparator() + VALID_1_PREFIX
                    + System.lineSeparator() + VALID_1_FILE, result);
        }

        /**
         * List folder content directory is file prints file.
         *
         * @throws AbstractApplicationException the abstract application exception
         */
        @Test
        void listFolderContent_DirectoryIsFile_PrintsFile() throws AbstractApplicationException {
            String result = lsApplication.listFolderContent(true, false,
                    VALID_FILE_TXT);
            assertEquals(String.format(VALID_FILE_TXT, VALID_FILE_TXT), result);
        }
    }

    /**
     * The type No flags positive tests.
     */
    @Nested
    class NoFlagsPositiveTests {
        /**
         * List folder content no folder names prints cwd content.
         *
         * @throws AbstractApplicationException the abstract application exception
         */
        @Test
        void listFolderContent_NoFolderNames_PrintsCwdContent() throws AbstractApplicationException {
            String result = lsApplication.listFolderContent(false, false);
            assertEquals(A_A + System.lineSeparator() + A_B + System.lineSeparator() + VALID
                    + System.lineSeparator() + VALID_1 + System.lineSeparator() + VALID_FILE_TXT, result);
        }

        /**
         * List folder content one valid folder name prints folder content.
         *
         * @throws AbstractApplicationException the abstract application exception
         */
        @Test
        void listFolderContent_OneValidFolderName_PrintsFolderContent() throws AbstractApplicationException {
            String result = lsApplication.listFolderContent(false, false, VALID);
            assertEquals(VALID_PREFIX + System.lineSeparator() + A_B + System.lineSeparator() + B_A
                    + System.lineSeparator() + VALID_2 + System.lineSeparator() + VALID_FILE_2_TXT, result);
        }

        /**
         * List folder content multiple valid folder names prints folder content.
         *
         * @throws AbstractApplicationException the abstract application exception
         */
        @Test
        void listFolderContent_MultipleValidFolderNames_PrintsFolderContent() throws AbstractApplicationException {
            String result = lsApplication.listFolderContent(false, false, VALID,
                    VALID_1);
            assertEquals(VALID_PREFIX + System.lineSeparator() + A_B + System.lineSeparator() + B_A
                    + System.lineSeparator() + VALID_2 + System.lineSeparator() + VALID_FILE_2_TXT
                    + System.lineSeparator() + System.lineSeparator() + VALID_1_PREFIX + System.lineSeparator()
                    + VALID_1_FILE, result);
        }

        /**
         * List folder content invalid file path with valid file path print error then print success.
         *
         * @throws AbstractApplicationException the abstract application exception
         */
        @Test
        void listFolderContent_InvalidFilePathWithValidFilePath_PrintErrorThenPrintSuccess()
                throws AbstractApplicationException {
            String result = lsApplication.listFolderContent(false, false,
                    INVALID, VALID);
            assertEquals(String.format("ls: cannot access '%s': No such file or directory", INVALID)
                    + System.lineSeparator() + VALID_PREFIX + System.lineSeparator() + A_B + System.lineSeparator()
                    + B_A + System.lineSeparator() + VALID_2 + System.lineSeparator() + VALID_FILE_2_TXT, result);
        }
    }

    /**
     * The type Negative tests.
     */
    @Nested
    class NegativeTests {
        /**
         * Run null args throw ls exception.
         */
        @Test
        void run_NullArgs_ThrowLsException() {
            Throwable thrown = assertThrows(LsException.class, () -> lsApplication.run(null, inputStream,
                    outputStream));
            assertEquals(new LsException(ERR_NULL_ARGS).getMessage(), thrown.getMessage());
        }

        /**
         * Run null input stream throw ls exception.
         */
        @Test
        void run_NullInputStream_ThrowLsException() {
            String[] args = new String[0];
            Throwable thrown = assertThrows(LsException.class, () -> lsApplication.run(args, null,
                    outputStream));
            assertEquals(new LsException(ERR_NO_ISTREAM).getMessage(), thrown.getMessage());
        }

        /**
         * Run null output stream throw ls exception.
         */
        @Test
        void run_NullOutputStream_ThrowLsException() {
            String[] args = new String[0];
            Throwable thrown = assertThrows(LsException.class, () -> lsApplication.run(args, inputStream,
                    null));
            assertEquals(new LsException(ERR_NO_OSTREAM).getMessage(), thrown.getMessage());
        }

        /**
         * Run ls parser invalid args throw ls exception.
         *
         * @throws InvalidArgsException the invalid args exception
         */
        @Test
        void run_LsParserInvalidArgs_ThrowLsException() throws InvalidArgsException {
            doThrow(new InvalidArgsException(TEST)).when(lsArgsParser).parse(any(String[].class));
            String[] args = new String[0];
            Throwable thrown = assertThrows(LsException.class, () -> lsApplication.run(args, inputStream,
                    outputStream));
            assertEquals(new LsException(TEST).getMessage(), thrown.getMessage());
        }

        /**
         * Run cannot write to stdout throw ls exception.
         *
         * @throws Exception the exception
         */
        @Test
        void run_CannotWriteToStdout_ThrowLsException() throws Exception {
            doThrow(new IOException()).when(outputStream).write(any(byte[].class));
            LsApplication spyApplication = spy(new LsApplication(lsArgsParser));
            doReturn(TEST).when(spyApplication).listFolderContent(anyBoolean(), anyBoolean(),
                    any(String[].class));
            String[] args = new String[0];
            Throwable thrown = assertThrows(LsException.class, () -> spyApplication.run(args, inputStream,
                    outputStream));
            assertEquals(new LsException(ERR_WRITE_STREAM).getMessage(), thrown.getMessage());
        }

        /**
         * List folder content null folder name throw ls exception.
         */
        @Test
        void listFolderContent_NullFolderName_ThrowLsException() {
            Throwable thrown = assertThrows(LsException.class, () -> lsApplication.listFolderContent(
                    false, false, (String[]) null));
            assertEquals(new LsException(ERR_NULL_ARGS).getMessage(), thrown.getMessage());
        }

        /**
         * List folder content invalid folder name prints invalid dir exception.
         *
         * @throws AbstractApplicationException the abstract application exception
         */
        @Test
        void listFolderContent_InvalidFolderName_PrintsInvalidDirException() throws AbstractApplicationException {
            String result = lsApplication.listFolderContent(false, false, INVALID);
            assertEquals(String.format("ls: cannot access '%s': No such file or directory", INVALID), result);
        }
    }
}
