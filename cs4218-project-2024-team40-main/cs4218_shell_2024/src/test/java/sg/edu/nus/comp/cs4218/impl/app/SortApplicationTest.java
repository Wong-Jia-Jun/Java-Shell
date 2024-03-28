package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.exception.SortException;
import sg.edu.nus.comp.cs4218.impl.parser.SortArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

/**
 * Tests Functionality of sort application
 */
public class SortApplicationTest { //NOPMD - suppressed UseUtilityClass - Test Methods are nested for readability
    private static final String MOCK = "mock";
    private static final String MOCK_MISSPELL = "moCk";
    private static final List<String> MOCK_INPUT_ALL = List.of(MOCK, MOCK_MISSPELL, "B2", "B1", "-2",
            "1", "10test", STRING_NEWLINE, "12", "#hi", "£", "€");
    private static final List<String> MOCK_INPUT_1_LINE = List.of(MOCK);
    private static final List<String> MOCK_INPUT_0_LINE = List.of();
    /**
     * The constant RANDOM_NUMBER.
     */
    public static final String RANDOM_NUMBER = "2147483648";
    /**
     * The constant RANDOM_NUMBER_2.
     */
    public static final String RANDOM_NUMBER_2 = "-2147483650";
    /**
     * The constant TEST_INPUT.
     */
    public static final String TEST_INPUT = "1AA";
    /**
     * The constant TEST_INPUT_2.
     */
    public static final String TEST_INPUT_2 = "1aA";
    /**
     * The constant MOCK_CAPITAL.
     */
    public static final String MOCK_CAPITAL = "MOCK";
    /**
     * The constant TEST.
     */
    public static final String TEST = "test";

    /**
     * The type Positive run tests.
     */
    @Nested
    class PositiveRunTests {
        private SortArgsParser sortArgsParser;
        private InputStream inputStream;
        private OutputStream outputStream;

        /**
         * Sets up.
         */
        @BeforeEach
        void setUp() {
            sortArgsParser = mock(SortArgsParser.class);
            inputStream = mock(InputStream.class);
            outputStream = mock(OutputStream.class);
        }

        /**
         * Run successful sort from stdin prints to output stream.
         *
         * @throws AbstractApplicationException the abstract application exception
         * @throws IOException                  the io exception
         */
        @Test
        void run_SuccessfulSortFromStdin_PrintsToOutputStream() throws AbstractApplicationException, IOException {
            SortApplication spyApplication = spy(new SortApplication(sortArgsParser));
            doReturn(MOCK).when(spyApplication).sortFromStdin(anyBoolean(), anyBoolean(), anyBoolean(),
                    any(InputStream.class));
            when(sortArgsParser.isSortFromStdin()).thenReturn(true);
            spyApplication.run(new String[]{MOCK}, inputStream, outputStream);
            verify(spyApplication).sortFromStdin(anyBoolean(), anyBoolean(), anyBoolean(), any(InputStream.class));
            verify(outputStream, times(1)).write(MOCK.getBytes());
            verify(outputStream, times(1)).write(STRING_NEWLINE.getBytes());
        }

        /**
         * Run successful sort from files prints to output stream.
         *
         * @throws AbstractApplicationException the abstract application exception
         * @throws IOException                  the io exception
         */
        @Test
        void run_SuccessfulSortFromFiles_PrintsToOutputStream() throws AbstractApplicationException, IOException {
            SortApplication spyApplication = spy(new SortApplication(sortArgsParser));
            doReturn(MOCK).when(spyApplication).sortFromFiles(anyBoolean(), anyBoolean(), anyBoolean(),
                    any(String[].class));
            when(sortArgsParser.isSortFromFiles()).thenReturn(true);
            spyApplication.run(new String[]{MOCK}, inputStream, outputStream);
            verify(spyApplication).sortFromFiles(anyBoolean(), anyBoolean(), anyBoolean(), any(String[].class));
            verify(outputStream, times(1)).write(MOCK.getBytes());
            verify(outputStream, times(1)).write(STRING_NEWLINE.getBytes());
        }
    }

    /**
     * Sort input provider stream.
     *
     * @return the stream
     */
    static Stream<Arguments> sortInputProvider() {
        return java.util.stream.Stream.of(
                Arguments.of((Object) MOCK_INPUT_ALL),
                Arguments.of((Object) MOCK_INPUT_1_LINE),
                Arguments.of((Object) MOCK_INPUT_0_LINE)
        );
    }

    /**
     * The type Positive sort from files test.
     */
    @Nested
    class PositiveSortFromFilesTest {
        private SortArgsParser sortArgsParser;
        private InputStream inputStream;
        private SortApplication sortApplication;
        private MockedStatic<IOUtils> mockedStaticFiles;

        /**
         * Sets up.
         */
        @BeforeEach
        void setUp() {
            sortArgsParser = mock(SortArgsParser.class);
            inputStream = mock(InputStream.class);
            sortApplication = new SortApplication(sortArgsParser);
            mockedStaticFiles = mockStatic(IOUtils.class);
            Path mockPath = mock(Path.class);
            File mockFile = mock(File.class);
            when(mockFile.exists()).thenReturn(true);
            when(mockFile.isDirectory()).thenReturn(false);
            when(mockFile.canRead()).thenReturn(true);
            when(mockPath.toFile()).thenReturn(mockFile);
            mockedStaticFiles.when(() -> IOUtils.resolveFilePath(anyString()))
                    .thenReturn(mockPath);
            mockedStaticFiles.when(() -> IOUtils.openInputStream(anyString()))
                    .thenReturn(inputStream);
        }

        /**
         * Tear down.
         */
        @AfterEach
        void tearDown() {
            mockedStaticFiles.close();
        }

        private String getSortFilesNoFlagsExpected(List<String> input) {
            if (input.equals(MOCK_INPUT_0_LINE)) {
                return "";
            } else if (input.equals(MOCK_INPUT_1_LINE)) {
                return MOCK;
            } else {
                return STRING_NEWLINE + "#hi" + STRING_NEWLINE + "-2" + STRING_NEWLINE + "1"
                        + STRING_NEWLINE + "10test" + STRING_NEWLINE + "12" + STRING_NEWLINE + "B1" + STRING_NEWLINE
                        + "B2" + STRING_NEWLINE + MOCK_MISSPELL + STRING_NEWLINE + MOCK + STRING_NEWLINE
                        + "£" + STRING_NEWLINE + "€";

            }
        }

        /**
         * Sort from files sort with no flags returns sorted string.
         *
         * @param args the args
         * @throws AbstractApplicationException the abstract application exception
         */
        @ParameterizedTest
        @MethodSource("sg.edu.nus.comp.cs4218.impl.app.SortApplicationTest#sortInputProvider")
        void sortFromFiles_SortWithNoFlags_ReturnsSortedString(List<String> args) throws AbstractApplicationException {
            mockedStaticFiles.when(() -> IOUtils.getLinesFromInputStream(any(InputStream.class)))
                    .thenReturn(args);
            String result = sortApplication.sortFromFiles(false, false,
                    false, new String[]{MOCK});
            String expected = getSortFilesNoFlagsExpected(args);
            assertEquals(expected, result);
        }

        /**
         * Sort from files n and rf lag returns sorted string.
         *
         * @throws AbstractApplicationException the abstract application exception
         */
        @Test
        void sortFromFiles_NAndRFLag_ReturnsSortedString() throws AbstractApplicationException {
            List<String> input = List.of("-2", MOCK, "-a", "Hi", "1A", "1", "1B", RANDOM_NUMBER);
            mockedStaticFiles.when(() -> IOUtils.getLinesFromInputStream(any(InputStream.class)))
                    .thenReturn(input);
            SortApplication sortApplication = new SortApplication(sortArgsParser);
            String result = sortApplication.sortFromFiles(true, true,
                    false, new String[]{MOCK});
            String expected = RANDOM_NUMBER + STRING_NEWLINE + "1B" + STRING_NEWLINE + "1A" + STRING_NEWLINE
                    + "1" + STRING_NEWLINE + MOCK + STRING_NEWLINE + "Hi" + STRING_NEWLINE + "-a"
                    + STRING_NEWLINE + "-2";
            assertEquals(expected, result);
        }

        /**
         * Sort from files n and f flag returns sorted string.
         *
         * @throws AbstractApplicationException the abstract application exception
         */
        @Test
        void sortFromFiles_NAndFFlag_ReturnsSortedString() throws AbstractApplicationException {
            List<String> input = List.of(RANDOM_NUMBER_2, MOCK_MISSPELL, MOCK, "1A", "1a", "1B", "0");
            mockedStaticFiles.when(() -> IOUtils.getLinesFromInputStream(any(InputStream.class)))
                    .thenReturn(input);
            SortApplication sortApplication = new SortApplication(sortArgsParser);
            String result = sortApplication.sortFromFiles(true, false,
                    true, new String[]{MOCK});
            String expected = RANDOM_NUMBER_2 + STRING_NEWLINE + "0" + STRING_NEWLINE + MOCK_MISSPELL + STRING_NEWLINE + MOCK
                    + STRING_NEWLINE + "1A" + STRING_NEWLINE + "1a" + STRING_NEWLINE + "1B" ;
            assertEquals(expected, result);
        }

        /**
         * Sort from files r and f flag returns sorted string stable sorted.
         *
         * @throws AbstractApplicationException the abstract application exception
         */
        @Test
        void sortFromFiles_RAndFFlag_ReturnsSortedStringStableSorted() throws AbstractApplicationException {
            List<String> input = List.of("1A", TEST_INPUT, "1a", TEST_INPUT_2, MOCK_MISSPELL, MOCK, MOCK_CAPITAL);
            mockedStaticFiles.when(() -> IOUtils.getLinesFromInputStream(any(InputStream.class)))
                    .thenReturn(input);
            SortApplication sortApplication = new SortApplication(sortArgsParser);
            String result = sortApplication.sortFromFiles(false, true,
                    true, new String[]{MOCK});
            String expected = MOCK_CAPITAL + STRING_NEWLINE + MOCK + STRING_NEWLINE + MOCK_MISSPELL + STRING_NEWLINE
                    + TEST_INPUT_2 + STRING_NEWLINE + TEST_INPUT + STRING_NEWLINE + "1a" + STRING_NEWLINE + "1A";
            assertEquals(expected, result);
        }
    }

    /**
     * The type Positive sort from stdin test.
     */
    @Nested
    class PositiveSortFromStdinTest {
        private SortArgsParser sortArgsParser;
        private InputStream inputStream;
        private SortApplication sortApplication;
        private MockedStatic<IOUtils> mockedStaticStdin;

        /**
         * Sets up.
         */
        @BeforeEach
        void setUp() {
            sortArgsParser = mock(SortArgsParser.class);
            inputStream = mock(InputStream.class);
            sortApplication = new SortApplication(sortArgsParser);
            mockedStaticStdin = mockStatic(IOUtils.class);
        }

        /**
         * Tear down.
         */
        @AfterEach
        void tearDown() {
            mockedStaticStdin.close();
        }

        private String getSortStdinNoFlagsExpected(List<String> input) {
            if (input.equals(MOCK_INPUT_0_LINE)) {
                return "";
            } else if (input.equals(MOCK_INPUT_1_LINE)) {
                return MOCK;
            } else {
                return STRING_NEWLINE + "#hi" + STRING_NEWLINE + "-2" + STRING_NEWLINE + "1"
                        + STRING_NEWLINE + "10test" + STRING_NEWLINE + "12" + STRING_NEWLINE + "B1" + STRING_NEWLINE
                        + "B2" + STRING_NEWLINE + MOCK_MISSPELL + STRING_NEWLINE + MOCK + STRING_NEWLINE
                        + "£" + STRING_NEWLINE + "€";

            }
        }

        /**
         * Sort from stdin sort with no flags returns sorted string.
         *
         * @param args the args
         * @throws AbstractApplicationException the abstract application exception
         */
        @ParameterizedTest
        @MethodSource("sg.edu.nus.comp.cs4218.impl.app.SortApplicationTest#sortInputProvider")
        void sortFromStdin_SortWithNoFlags_ReturnsSortedString(List<String> args) throws AbstractApplicationException {
            mockedStaticStdin.when(() -> IOUtils.getLinesFromInputStream(any(InputStream.class)))
                    .thenReturn(args);
            String result = sortApplication.sortFromStdin(false, false,
                    false, inputStream);
            String expected = getSortStdinNoFlagsExpected(args);
            assertEquals(expected, result);
        }

        /**
         * Sort from stdin n and rf lag returns sorted string.
         *
         * @throws AbstractApplicationException the abstract application exception
         */
        @Test
        void sortFromStdin_NAndRFLag_ReturnsSortedString() throws AbstractApplicationException {
            List<String> input = List.of("-2", MOCK, "-a", "Hi", "1A", "1", "1B", RANDOM_NUMBER);
            mockedStaticStdin.when(() -> IOUtils.getLinesFromInputStream(any(InputStream.class)))
                    .thenReturn(input);
            SortApplication sortApplication = new SortApplication(sortArgsParser);
            String result = sortApplication.sortFromStdin(true, true,
                    false, inputStream);
            String expected = RANDOM_NUMBER + STRING_NEWLINE + "1B" + STRING_NEWLINE + "1A" + STRING_NEWLINE
                    + "1" + STRING_NEWLINE + MOCK + STRING_NEWLINE + "Hi" + STRING_NEWLINE + "-a"
                    + STRING_NEWLINE + "-2";
            assertEquals(expected, result);
        }

        /**
         * Sort from stdin n and f flag returns sorted string.
         *
         * @throws AbstractApplicationException the abstract application exception
         */
        @Test
        void sortFromStdin_NAndFFlag_ReturnsSortedString() throws AbstractApplicationException {
            List<String> input = List.of(RANDOM_NUMBER_2, MOCK_MISSPELL, MOCK, "1A", "1a", "1B", "0");
            mockedStaticStdin.when(() -> IOUtils.getLinesFromInputStream(any(InputStream.class)))
                    .thenReturn(input);
            SortApplication sortApplication = new SortApplication(sortArgsParser);
            String result = sortApplication.sortFromStdin(true, false,
                    true, inputStream);
            String expected = RANDOM_NUMBER_2 + STRING_NEWLINE + "0" + STRING_NEWLINE + MOCK_MISSPELL + STRING_NEWLINE + MOCK
                    + STRING_NEWLINE + "1A" + STRING_NEWLINE + "1a" + STRING_NEWLINE + "1B" ;
            assertEquals(expected, result);
        }

        /**
         * Sort from stdin r and f flag returns sorted string stable sorted.
         *
         * @throws AbstractApplicationException the abstract application exception
         */
        @Test
        void sortFromStdin_RAndFFlag_ReturnsSortedStringStableSorted() throws AbstractApplicationException {
            List<String> input = List.of("1A", TEST_INPUT, "1a", TEST_INPUT_2, MOCK_MISSPELL, MOCK, MOCK_CAPITAL);
            mockedStaticStdin.when(() -> IOUtils.getLinesFromInputStream(any(InputStream.class)))
                    .thenReturn(input);
            SortApplication sortApplication = new SortApplication(sortArgsParser);
            String result = sortApplication.sortFromStdin(false, true,
                    true, inputStream);
            String expected = MOCK_CAPITAL + STRING_NEWLINE + MOCK + STRING_NEWLINE + MOCK_MISSPELL + STRING_NEWLINE
                    + TEST_INPUT_2 + STRING_NEWLINE + TEST_INPUT + STRING_NEWLINE + "1a" + STRING_NEWLINE + "1A";
            assertEquals(expected, result);
        }

    }

    /**
     * The type Negative tests.
     */
    @Nested
    class NegativeTests {
        private SortArgsParser sortArgsParser;
        private InputStream inputStream;
        private OutputStream outputStream;
        private SortApplication sortApplication;

        /**
         * Sets up.
         */
        @BeforeEach
        void setUp() {
            sortArgsParser = mock(SortArgsParser.class);
            inputStream = mock(InputStream.class);
            outputStream = mock(OutputStream.class);
            sortApplication = new SortApplication(sortArgsParser);
        }

        /**
         * Run null stdin throw sort exception.
         */
        @Test
        void run_NullStdin_ThrowSortException() {
            Throwable thrown = assertThrows(SortException.class, () -> {
               sortApplication.run(new String[0], null, outputStream);
            });
            assertEquals(new SortException(ERR_NULL_STREAMS).getMessage(),
                    thrown.getMessage());
        }

        /**
         * Run null stdout throw sort exception.
         */
        @Test
        void run_NullStdout_ThrowSortException() {
            Throwable thrown = assertThrows(SortException.class, () -> {
                sortApplication.run(new String[0], inputStream, null);
            });
            assertEquals(new SortException(ERR_NULL_STREAMS).getMessage(),
                    thrown.getMessage());
        }

        /**
         * Run null args throw sort exception.
         */
        @Test
        void run_NullArgs_ThrowSortException() {
            Throwable thrown = assertThrows(SortException.class, () -> {
                sortApplication.run(null, inputStream, outputStream);
            });
            assertEquals(new SortException(ERR_NULL_ARGS).getMessage(),
                    thrown.getMessage());
        }

        /**
         * Run invalid args parsed throw sort exception.
         *
         * @throws InvalidArgsException the invalid args exception
         */
        @Test
        void run_InvalidArgsParsed_ThrowSortException() throws InvalidArgsException {
            doThrow(new InvalidArgsException(TEST)).when(sortArgsParser).parse(any());
            Throwable thrown = assertThrows(SortException.class, () -> {
                sortApplication.run(new String[]{MOCK}, inputStream, outputStream);
            });
            assertEquals(new SortException(TEST).getMessage(),
                    thrown.getMessage());
        }

        /**
         * Run sort from stdin throws exception propagates exception.
         *
         * @throws AbstractApplicationException the abstract application exception
         */
        @Test
        void run_sortFromStdinThrowsException_PropagatesException() throws AbstractApplicationException {
            when(sortArgsParser.isSortFromStdin()).thenReturn(true);
            SortApplication spyApplication = spy(new SortApplication(sortArgsParser));
            doThrow(new SortException(TEST)).when(spyApplication).sortFromStdin(anyBoolean(),
                    anyBoolean(), anyBoolean(), any(InputStream.class));
            Throwable thrown = assertThrows(SortException.class, () -> {
                spyApplication.run(new String[]{MOCK}, inputStream, outputStream);
            });
            assertEquals(new SortException(TEST).getMessage(),
                    thrown.getMessage());
        }

        /**
         * Run sort from files throws exception propagates exception.
         *
         * @throws AbstractApplicationException the abstract application exception
         */
        @Test
        void run_sortFromFilesThrowsException_PropagatesException() throws AbstractApplicationException {
            when(sortArgsParser.isSortFromFiles()).thenReturn(true);
            SortApplication spyApplication = spy(new SortApplication(sortArgsParser));
            doThrow(new SortException(TEST)).when(spyApplication).sortFromFiles(anyBoolean(),
                    anyBoolean(), anyBoolean(), any(String[].class));
            Throwable thrown = assertThrows(SortException.class, () -> {
                spyApplication.run(new String[]{MOCK}, inputStream, outputStream);
            });
            assertEquals(new SortException(TEST).getMessage(),
                    thrown.getMessage());
        }

        /**
         * Run output stream closed throws exception.
         *
         * @throws IOException the io exception
         */
        @Test
        void run_OutputStreamClosed_ThrowsException() throws IOException {
            try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
                mockedStatic.when(() -> IOUtils.getLinesFromInputStream(any(InputStream.class)))
                        .thenReturn(List.of(MOCK));
                doThrow(new IOException("closed")).when(outputStream).write(any());
                when(sortArgsParser.isSortFromStdin()).thenReturn(true);
                Throwable thrown = assertThrows(SortException.class, () -> {
                    sortApplication.run(new String[]{MOCK}, inputStream, outputStream);
                });
                assertEquals(new SortException(ERR_WRITE_STREAM).getMessage(),
                        thrown.getMessage());
            }
        }

        /**
         * Sort from files null file names throws exception.
         */
        @Test
        void sortFromFiles_NullFileNames_ThrowsException() {
            Throwable thrown = assertThrows(SortException.class, () -> {
                sortApplication.sortFromFiles(false, false, false,
                        null);
            });
            assertEquals(new SortException(ERR_NULL_ARGS).getMessage(),
                    thrown.getMessage());
        }

        /**
         * Sort from files empty file names throws exception.
         */
        @Test
        void sortFromFiles_EmptyFileNames_ThrowsException() {
            Throwable thrown = assertThrows(SortException.class, () -> {
                sortApplication.sortFromFiles(false, false, false,
                        new String[0]);
            });
            assertEquals(new SortException(ERR_NULL_ARGS).getMessage(),
                    thrown.getMessage());
        }

        /**
         * Sort from files file doesnt exist throws exception.
         */
        @Test
        void sortFromFiles_FileDoesntExist_ThrowsException() {
            try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
                File mockFile = mock(File.class);
                when(mockFile.exists()).thenReturn(false);
                Path mockPath = mock(Path.class);
                when(mockPath.toFile()).thenReturn(mockFile);
                mockedStatic.when(() -> IOUtils.resolveFilePath(anyString()))
                        .thenReturn(mockPath);
                Throwable thrown = assertThrows(SortException.class, () -> {
                    sortApplication.sortFromFiles(false, false, false,
                            MOCK);
                });
                assertEquals(new SortException(MOCK, ERR_FILE_NOT_FND).getMessage(),
                        thrown.getMessage());
            }
        }

        /**
         * Sort from files file is dir throws exception.
         */
        @Test
        void sortFromFiles_FileIsDir_ThrowsException() {
            try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
                File mockFile = mock(File.class);
                when(mockFile.exists()).thenReturn(true);
                when(mockFile.isDirectory()).thenReturn(true);
                Path mockPath = mock(Path.class);
                when(mockPath.toFile()).thenReturn(mockFile);
                mockedStatic.when(() -> IOUtils.resolveFilePath(anyString()))
                        .thenReturn(mockPath);
                Throwable thrown = assertThrows(SortException.class, () -> {
                    sortApplication.sortFromFiles(false, false, false,
                            MOCK);
                });
                assertEquals(new SortException(MOCK, ERR_IS_DIR).getMessage(),
                        thrown.getMessage());
            }
        }

        /**
         * Sort from files file cant read throws exception.
         */
        @Test
        void sortFromFiles_FileCantRead_ThrowsException() {
            try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
                File mockFile = mock(File.class);
                when(mockFile.exists()).thenReturn(true);
                when(mockFile.isDirectory()).thenReturn(false);
                when(mockFile.canRead()).thenReturn(false);
                Path mockPath = mock(Path.class);
                when(mockPath.toFile()).thenReturn(mockFile);
                mockedStatic.when(() -> IOUtils.resolveFilePath(anyString()))
                        .thenReturn(mockPath);
                Throwable thrown = assertThrows(SortException.class, () -> {
                    sortApplication.sortFromFiles(false, false, false,
                            MOCK);
                });
                assertEquals(new SortException(MOCK, ERR_NO_PERM).getMessage(),
                        thrown.getMessage());
            }
        }

        /**
         * Sort from files io utils cannot open file throws exception.
         */
        @Test
        void sortFromFiles_IOUtilsCannotOpenFile_ThrowsException() {
            try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
                mockedStatic.when(() -> IOUtils.openInputStream(anyString()))
                        .thenThrow(new ShellException(TEST));
                File mockFile = mock(File.class);
                when(mockFile.exists()).thenReturn(true);
                when(mockFile.isDirectory()).thenReturn(false);
                when(mockFile.canRead()).thenReturn(true);
                Path mockPath = mock(Path.class);
                when(mockPath.toFile()).thenReturn(mockFile);
                mockedStatic.when(() -> IOUtils.resolveFilePath(anyString()))
                        .thenReturn(mockPath);
                Throwable thrown = assertThrows(SortException.class, () -> {
                    sortApplication.sortFromFiles(false, false, false,
                            MOCK);
                });
                assertEquals(new SortException(ERR_READING_FILE).getMessage(),
                        thrown.getMessage());
            }
        }

        /**
         * Sort from files io utils cannot get lines throws exception.
         */
        @Test
        void sortFromFiles_IOUtilsCannotGetLines_ThrowsException() {
            try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
                mockedStatic.when(() -> IOUtils.openInputStream(anyString()))
                        .thenReturn(inputStream);
                mockedStatic.when(() -> IOUtils.getLinesFromInputStream(any(InputStream.class)))
                        .thenThrow(new IOException(TEST));
                File mockFile = mock(File.class);
                when(mockFile.exists()).thenReturn(true);
                when(mockFile.isDirectory()).thenReturn(false);
                when(mockFile.canRead()).thenReturn(true);
                Path mockPath = mock(Path.class);
                when(mockPath.toFile()).thenReturn(mockFile);
                mockedStatic.when(() -> IOUtils.resolveFilePath(anyString()))
                        .thenReturn(mockPath);
                Throwable thrown = assertThrows(SortException.class, () -> {
                    sortApplication.sortFromFiles(false, false, false,
                            MOCK);
                });
                assertEquals(new SortException(ERR_IO_EXCEPTION).getMessage(),
                        thrown.getMessage());
            }
        }

        /**
         * Sort from files io utils cannot close file throws exception.
         */
        @Test
        void sortFromFiles_IOUtilsCannotCloseFile_ThrowsException() {
            try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
                mockedStatic.when(() -> IOUtils.openInputStream(anyString()))
                        .thenReturn(inputStream);
                mockedStatic.when(() -> IOUtils.getLinesFromInputStream(any(InputStream.class)))
                        .thenReturn(List.of(MOCK));
                mockedStatic.when(() -> IOUtils.closeInputStream(any(InputStream.class))
                ).thenThrow(new ShellException(TEST));
                File mockFile = mock(File.class);
                when(mockFile.exists()).thenReturn(true);
                when(mockFile.isDirectory()).thenReturn(false);
                when(mockFile.canRead()).thenReturn(true);
                Path mockPath = mock(Path.class);
                when(mockPath.toFile()).thenReturn(mockFile);
                mockedStatic.when(() -> IOUtils.resolveFilePath(anyString()))
                        .thenReturn(mockPath);
                Throwable thrown = assertThrows(SortException.class, () -> {
                    sortApplication.sortFromFiles(false, false, false,
                            MOCK);
                });
                assertEquals(new SortException(ERR_CLOSE_STREAMS).getMessage(),
                        thrown.getMessage());
            }
        }

        /**
         * Sort from stdin null stdin throws exception.
         */
        @Test
        void sortFromStdin_NullStdin_ThrowsException() {
            Throwable thrown = assertThrows(SortException.class, () -> {
                sortApplication.sortFromStdin(false, false, false,
                        null);
            });
            assertEquals(new SortException(ERR_NULL_STREAMS).getMessage(),
                    thrown.getMessage());
        }

        /**
         * Sort from stdin io utils cannot get lines throws exception.
         */
        @Test
        void sortFromStdin_IOUtilsCannotGetLines_ThrowsException() {
            try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
                mockedStatic.when(() -> IOUtils.getLinesFromInputStream(any(InputStream.class)))
                        .thenThrow(new IOException(TEST));
                Throwable thrown = assertThrows(SortException.class, () -> {
                    sortApplication.sortFromStdin(false, false, false,
                            inputStream);
                });
                assertEquals(new SortException(ERR_IO_EXCEPTION).getMessage(),
                        thrown.getMessage());
            }
        }
    }
}
