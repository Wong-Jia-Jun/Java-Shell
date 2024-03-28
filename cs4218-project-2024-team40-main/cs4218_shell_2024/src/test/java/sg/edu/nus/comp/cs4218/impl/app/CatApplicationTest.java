package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CatException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.parser.CatArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;


/**
 * Tests Functionality of cat application
 */
public class CatApplicationTest {
    /**
     * The type Positive tests.
     */
    @Nested
    class PositiveTests {
        /**
         * The Cat args parser.
         */
        private CatArgsParser catArgsParser;
        /**
         * The Cat application.
         */
        private CatApplication catApplication;
        /**
         * The Input stream.
         */
        private InputStream inputStream;
        /**
         * The Output stream.
         */
        private OutputStream outputStream;
        /**
         * The Output.
         */
        private static final String OUTPUT = "output";
        /**
         * The Line 1.
         */
        private static final String LINE_1 = "line1";
        /**
         * The Line 2.
         */
        private static final String LINE_2 = "line2";
        /**
         * The Cat files.
         */
        private static final String CAT_FILES = "catFiles";
        /**
         * The Cat stdin.
         */
        private static final String CAT_STDIN = "catStdin";
        /**
         * The File 1.
         */
        private static final String FILE_1 = "file1";
        /**
         * The File 2.
         */
        private static final String FILE_2 = "file2";

        /**
         * Sets up.
         */
        @BeforeEach
        void setUp() {
            catArgsParser = mock(CatArgsParser.class);
            catApplication = new CatApplication(catArgsParser);
            inputStream = mock(InputStream.class);
            outputStream = mock(OutputStream.class);
            catApplication.setStdout(outputStream);
        }

        /**
         * Run call cat stdin prints to stdout.
         *
         * @throws AbstractApplicationException the abstract application exception
         */
        @Test
        void run_CallCatStdin_PrintsToStdout() throws AbstractApplicationException {
            when(catArgsParser.isCatStdin()).thenReturn(true);
            CatApplication spyApplication = spy(new CatApplication(catArgsParser));
            doReturn(OUTPUT).when(spyApplication).catStdin(anyBoolean(), any(InputStream.class));
            OutputStream outputStream = new ByteArrayOutputStream();
            spyApplication.run(new String[]{"-"}, inputStream, outputStream);
            assertEquals(OUTPUT, outputStream.toString());
        }

        /**
         * Run call cat file and stdin prints to stdout.
         *
         * @throws AbstractApplicationException the abstract application exception
         */
        @Test
        void run_CallCatFileAndStdin_PrintsToStdout() throws AbstractApplicationException {
            when(catArgsParser.isCatFileAndStdin()).thenReturn(true);
            CatApplication spyApplication = spy(new CatApplication(catArgsParser));
            doReturn(OUTPUT).when(spyApplication).catFileAndStdin(
                    anyBoolean(), any(InputStream.class), any(String[].class));
            OutputStream outputStream = new ByteArrayOutputStream();
            spyApplication.run(new String[]{"-"}, inputStream, outputStream);
            assertEquals(OUTPUT, outputStream.toString());
        }

        /**
         * Run call cat files prints to stdout.
         *
         * @throws AbstractApplicationException the abstract application exception
         */
        @Test
        void run_CallCatFiles_PrintsToStdout() throws AbstractApplicationException {
            when(catArgsParser.isCatFiles()).thenReturn(true);
            CatApplication spyApplication = spy(new CatApplication(catArgsParser));
            doReturn(OUTPUT).when(spyApplication).catFiles(anyBoolean(), any(String[].class));
            OutputStream outputStream = new ByteArrayOutputStream();
            spyApplication.run(new String[]{"-"}, inputStream, outputStream);
            assertEquals(OUTPUT, outputStream.toString());
        }

        /**
         * Cat files succesful read returns string.
         *
         * @param isLine the is line
         * @throws AbstractApplicationException the abstract application exception
         */
        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void catFiles_SuccesfulRead_ReturnsString(boolean isLine) throws AbstractApplicationException {
            try (
                    MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class);
                    MockedStatic<Files> mockedFiles = mockStatic(Files.class)
            ) {
                mockedFiles.when(() -> Files.exists(any())).thenReturn(true);
                mockedFiles.when(() -> Files.isDirectory(any())).thenReturn(false);
                mockedStatic.when(() -> IOUtils.openInputStream(anyString()))
                        .thenReturn(inputStream);
                mockedStatic.when(() -> IOUtils.getLinesFromInputStream(inputStream))
                        .thenReturn(List.of(LINE_1, LINE_2));
                String result = catApplication.catFiles(isLine, FILE_1, "file2");
                String expected = (isLine ? CatApplication.LINE_NUM_PREFIX + "1" + CatApplication.LINE_NUM_SUFFIX : "")
                        + LINE_1 + System.lineSeparator() + (isLine ? CatApplication.LINE_NUM_PREFIX + "2"
                        + CatApplication.LINE_NUM_SUFFIX : "") + LINE_2 + System.lineSeparator();
                assertEquals(expected + expected, result);
            }
        }

        /**
         * Cat stdin succesful read returns string.
         *
         * @param isLine the is line
         * @throws AbstractApplicationException the abstract application exception
         */
        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void catStdin_SuccesfulRead_ReturnsString(boolean isLine) throws AbstractApplicationException {
            try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
                mockedStatic.when(() -> IOUtils.getLinesFromInputStream(inputStream))
                        .thenReturn(List.of(LINE_1, LINE_2));
                String result = catApplication.catStdin(isLine, inputStream);
                String expected = (isLine ? CatApplication.LINE_NUM_PREFIX + "1" + CatApplication.LINE_NUM_SUFFIX : "")
                        + LINE_1 + System.lineSeparator() + (isLine ? CatApplication.LINE_NUM_PREFIX + "2"
                        + CatApplication.LINE_NUM_SUFFIX : "") + LINE_2 + System.lineSeparator();
                assertEquals(expected, result);
            }
        }

        /**
         * Cat file and stdin dash then file returns string and does not print.
         *
         * @throws AbstractApplicationException the abstract application exception
         * @throws IOException                  the io exception
         */
        @Test
        void catFileAndStdin_DashThenFile_ReturnsStringAndDoesNotPrint()
                throws AbstractApplicationException, IOException {
            CatApplication spyApplication = spy(new CatApplication(catArgsParser));
            spyApplication.setStdout(outputStream);
            doReturn(CAT_FILES + System.lineSeparator()).when(spyApplication).catFiles(anyBoolean(),
                    any(String[].class));
            doReturn(CAT_STDIN + System.lineSeparator()).when(spyApplication).catStdin(anyBoolean(),
                    any(InputStream.class));
            String result = spyApplication.catFileAndStdin(true, inputStream, "-", FILE_1);
            verify(outputStream, times(1)).write(new byte[0]);
            String expected = CAT_STDIN + System.lineSeparator() + CAT_FILES + System.lineSeparator();
            assertEquals(expected, result);
        }

        /**
         * Cat file and stdin file then dash prints file and return string.
         *
         * @throws AbstractApplicationException the abstract application exception
         * @throws IOException                  the io exception
         */
        @Test
        void catFileAndStdin_FileThenDash_PrintsFileAndReturnString() throws AbstractApplicationException, IOException {
            CatApplication spyApplication = spy(new CatApplication(catArgsParser));
            spyApplication.setStdout(outputStream);
            doReturn(CAT_FILES + System.lineSeparator()).when(spyApplication).catFiles(anyBoolean(),
                    any(String[].class));
            doReturn(CAT_STDIN + System.lineSeparator()).when(spyApplication).catStdin(anyBoolean(),
                    any(InputStream.class));
            String result = spyApplication.catFileAndStdin(false, inputStream, FILE_1, "-");
            verify(outputStream, times(1))
                    .write((CAT_FILES + System.lineSeparator()).getBytes());
            String expected = CAT_STDIN + System.lineSeparator();
            assertEquals(expected, result);
        }
    }

    /**
     * The type Negative tests.
     */
    @Nested
    class NegativeTests {
        /**
         * The Cat args parser.
         */
        private CatArgsParser catArgsParser;
        /**
         * The Cat application.
         */
        private CatApplication catApplication;
        /**
         * The Input stream.
         */
        private InputStream inputStream;
        /**
         * The Output stream.
         */
        private OutputStream outputStream;
        /**
         * The File 1.
         */
        private static final String FILE_1 = "file1";
        /**
         * The Test string.
         */
        private static final String TEST_STRING = "test";
        /**
         * The Mock string.
         */
        private static final String MOCK_STRING = "mock";
        /**
         * The Closed string.
         */
        private static final String CLOSED_STRING = "closed";
        /**
         * The Err strm closed message.
         */
        private static final String ERR_STRM_CLOSED = "Stream closed";

        /**
         * Sets up.
         */
        @BeforeEach
        void setUp() {
            catArgsParser = mock(CatArgsParser.class);
            catApplication = new CatApplication(catArgsParser);
            inputStream = mock(InputStream.class);
            outputStream = mock(OutputStream.class);
            catApplication.setStdout(outputStream);
        }

        /**
         * Run null args throws exception.
         */
        @Test
        void run_NullArgs_ThrowsException() {
            Throwable exp = assertThrows(CatException.class, () -> {
                catApplication.run(null, inputStream, outputStream);
            });
            assertEquals(new CatException(ERR_NULL_ARGS).getMessage(), exp.getMessage());
        }

        /**
         * Run null input stream throws exception.
         */
        @Test
        void run_NullInputStream_ThrowsException() {
            Throwable exp = assertThrows(CatException.class, () -> {
                catApplication.run(new String[]{FILE_1}, null, outputStream);
            });
            assertEquals(new CatException(ERR_NULL_STREAMS).getMessage(), exp.getMessage());
        }

        /**
         * Run null output stream throws exception.
         */
        @Test
        void run_NullOutputStream_ThrowsException() {
            Throwable exp = assertThrows(CatException.class, () -> {
                catApplication.run(new String[]{FILE_1}, inputStream, null);
            });
            assertEquals(new CatException(ERR_NULL_STREAMS).getMessage(), exp.getMessage());
        }

        /**
         * Run invalid args parsed throws exception.
         *
         * @throws InvalidArgsException the invalid args exception
         */
        @Test
        void run_InvalidArgsParsed_ThrowsException() throws InvalidArgsException {
            doThrow(new InvalidArgsException(TEST_STRING)).when(catArgsParser).parse(any());
            Throwable exp = assertThrows(CatException.class, () -> {
                catApplication.run(new String[]{MOCK_STRING}, inputStream, outputStream);
            });
            assertEquals(new CatException(TEST_STRING).getMessage(), exp.getMessage());
        }

        /**
         * Run closed output stream throws exception.
         *
         * @throws IOException                  the io exception
         * @throws AbstractApplicationException the abstract application exception
         */
        @Test
        void run_ClosedOutputStream_ThrowsException() throws IOException, AbstractApplicationException {
            doThrow(new IOException(CLOSED_STRING)).when(outputStream).write(any());
            when(catArgsParser.isCatStdin()).thenReturn(true);
            CatApplication spyCatApplication = spy(new CatApplication(catArgsParser));
            doReturn("output").when(spyCatApplication).catStdin(anyBoolean(), any(InputStream.class));
            Throwable exp = assertThrows(CatException.class, () -> {
                spyCatApplication.run(new String[]{MOCK_STRING}, inputStream, outputStream);
            });
            assertEquals(new CatException(ERR_IO_EXCEPTION, new IOException(CLOSED_STRING)).getMessage(), exp.getMessage());
        }

        /**
         * Run cat stdin throws exception propagates exception.
         *
         * @throws AbstractApplicationException the abstract application exception
         */
        @Test
        void run_catStdinThrowsException_PropagatesException() throws AbstractApplicationException {
            when(catArgsParser.isCatStdin()).thenReturn(true);
            CatApplication spyApplication = spy(new CatApplication(catArgsParser));
            doThrow(new CatException(TEST_STRING)).when(spyApplication).catStdin(anyBoolean(), any(InputStream.class));
            Throwable thrown = assertThrows(CatException.class, () -> {
                spyApplication.run(new String[]{MOCK_STRING}, inputStream, outputStream);
            });
            assertEquals(new CatException(TEST_STRING).getMessage(),
                    thrown.getMessage());
        }

        /**
         * Run cat files and stdin throws exception propagates exception.
         *
         * @throws AbstractApplicationException the abstract application exception
         */
        @Test
        void run_catFilesAndStdinThrowsException_PropagatesException() throws AbstractApplicationException {
            when(catArgsParser.isCatFileAndStdin()).thenReturn(true);
            CatApplication spyApplication = spy(new CatApplication(catArgsParser));
            doThrow(new CatException(TEST_STRING)).when(spyApplication).catFileAndStdin(anyBoolean(),
                    any(InputStream.class), any(String[].class));
            Throwable thrown = assertThrows(CatException.class, () -> {
                spyApplication.run(new String[]{MOCK_STRING}, inputStream, outputStream);
            });
            assertEquals(new CatException(TEST_STRING).getMessage(),
                    thrown.getMessage());
        }

        /**
         * Run cat files throws exception propagates exception.
         *
         * @throws AbstractApplicationException the abstract application exception
         */
        @Test
        void run_catFilesThrowsException_PropagatesException() throws AbstractApplicationException {
            when(catArgsParser.isCatFiles()).thenReturn(true);
            CatApplication spyApplication = spy(new CatApplication(catArgsParser));
            doThrow(new CatException(TEST_STRING)).when(spyApplication).catFiles(anyBoolean(), any(String[].class));
            Throwable thrown = assertThrows(CatException.class, () -> {
                spyApplication.run(new String[]{MOCK_STRING}, inputStream, outputStream);
            });
            assertEquals(new CatException(TEST_STRING).getMessage(),
                    thrown.getMessage());
        }

        /**
         * Cat files null file name throws exception.
         *
         * @param isLine the is line
         */
        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void catFiles_NullFileName_ThrowsException(boolean isLine) {
            Throwable exp = assertThrows(CatException.class, () -> {
                catApplication.catFiles(isLine, (String[]) null);
            });
            assertEquals(new CatException(ERR_NULL_ARGS).getMessage(), exp.getMessage());
        }

        /**
         * Cat files empty files throws exception.
         *
         * @param isLine the is line
         */
        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void catFiles_EmptyFiles_ThrowsException(boolean isLine) {
            Throwable exp = assertThrows(CatException.class, () -> {
                catApplication.catFiles(isLine, new String[0]);
            });
            assertEquals(new CatException(ERR_NO_ARGS).getMessage(), exp.getMessage());
        }

        /**
         * Cat files blank string prints exception.
         *
         * @param isLine the is line
         */
        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void catFiles_BlankFileName_PrintsException(boolean isLine) throws AbstractApplicationException {
            String result = catApplication.catFiles(isLine, "");
            assertEquals(new CatException(ERR_NO_FILE_ARGS).getMessage() + STRING_NEWLINE, result);
        }

        /**
         * Cat files cannot open input stream returns exception text.
         *
         * @param isLine the is line
         */
        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void catFiles_CannotOpenInputStream_ReturnsException(boolean isLine) throws AbstractApplicationException {
            try (
                    MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class);
                    MockedStatic<Files> mockedFiles = mockStatic(Files.class)
            ) {
                mockedFiles.when(() -> Files.exists(any())).thenReturn(true);
                mockedFiles.when(() -> Files.isDirectory(any())).thenReturn(false);
                mockedStatic.when(() -> IOUtils.openInputStream(anyString()))
                        .thenThrow(new ShellException(ERR_STRM_CLOSED));
                String result = catApplication.catFiles(isLine, "nonexistent");

                assertEquals(new CatException("nonexistent", new ShellException(ERR_STRM_CLOSED)).getMessage() + STRING_NEWLINE,
                        result);
            }
        }

        /**
         * Cat files cannot get lines from input stream retuns exception.
         *
         * @param isLine the is line
         */
        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void catFiles_CannotGetLinesFromInputStream_ReturnsException(boolean isLine) throws AbstractApplicationException {
            try (
                    MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class);
                    MockedStatic<Files> mockedFiles = mockStatic(Files.class)
            ) {
                mockedFiles.when(() -> Files.exists(any())).thenReturn(true);
                mockedFiles.when(() -> Files.isDirectory(any())).thenReturn(false);
                mockedStatic.when(() -> IOUtils.getLinesFromInputStream(any()))
                        .thenThrow(new IOException(ERR_STRM_CLOSED));

                String result = catApplication.catFiles(isLine, "test");

                assertEquals(new CatException("test", new IOException(ERR_STRM_CLOSED)).getMessage() + STRING_NEWLINE,
                        result);
            }
        }

        /**
         * Cat stdin null input stream throws exception.
         *
         * @param isLine the is line
         */
        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void catStdin_NullInputStream_ThrowsException(boolean isLine) {
            Throwable exp = assertThrows(CatException.class, () -> {
                catApplication.catStdin(isLine, null);
            });
            assertEquals(new CatException(ERR_NULL_STREAMS).getMessage(), exp.getMessage());
        }

        /**
         * Cat stdin cannot get lines from stdin throws exception.
         *
         * @param isLine the is line
         */
        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void catStdin_CannotGetLinesFromStdin_ThrowsException(boolean isLine) {
            try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
                mockedStatic.when(() -> IOUtils.getLinesFromInputStream(any()))
                        .thenThrow(new IOException(ERR_STRM_CLOSED));
                Throwable exp = assertThrows(CatException.class, () -> {
                    catApplication.catStdin(isLine, inputStream);
                });
                assertEquals(new CatException(ERR_IO_EXCEPTION, new IOException(ERR_STRM_CLOSED)).getMessage(), exp.getMessage());
            }
        }

        /**
         * Cat file and stdin null files throws exception.
         *
         * @param isLine the is line
         */
        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void catFileAndStdin_NullFiles_ThrowsException(boolean isLine) {
            Throwable exp = assertThrows(CatException.class, () -> {
                catApplication.catFileAndStdin(isLine, inputStream, (String[]) null);
            });
            assertEquals(new CatException(ERR_NULL_ARGS).getMessage(), exp.getMessage());
        }

        /**
         * Cat file and stdin empty files throws exception.
         *
         * @param isLine the is line
         */
        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void catFileAndStdin_EmptyFiles_ThrowsException(boolean isLine) {
            Throwable exp = assertThrows(CatException.class, () -> {
                catApplication.catFileAndStdin(isLine, inputStream, new String[0]);
            });
            assertEquals(new CatException(ERR_NO_ARGS).getMessage(), exp.getMessage());
        }

        /**
         * Cat file and stdin null input stream throws exception.
         *
         * @param isLine the is line
         */
        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void catFileAndStdin_NullInputStream_ThrowsException(boolean isLine) {
            Throwable exp = assertThrows(CatException.class, () -> {
                catApplication.catFileAndStdin(isLine, null, FILE_1);
            });
            assertEquals(new CatException(ERR_NULL_STREAMS).getMessage(), exp.getMessage());
        }

        /**
         * Cat file and stdin null output stream throws exception.
         *
         * @param isLine the is line
         */
        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void catFileAndStdin_NullOutputStream_ThrowsException(boolean isLine) {
            catApplication.setStdout(null);
            Throwable exp = assertThrows(CatException.class, () -> {
                catApplication.catFileAndStdin(isLine, inputStream, FILE_1);
            });
            assertEquals(new CatException(ERR_NULL_STREAMS).getMessage(), exp.getMessage());
        }

        /**
         * Cat file and stdin cannot write to output stream throws exception.
         *
         * @param isLine the is line
         * @throws IOException the io exception
         */
        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void catFileAndStdin_CannotWriteToOutputStream_ThrowsException(boolean isLine) throws IOException {
            doThrow(new IOException(CLOSED_STRING)).when(outputStream).write(any());
            Throwable exp = assertThrows(CatException.class, () -> {
                catApplication.catFileAndStdin(isLine, inputStream, "-");
            });
            assertEquals(new CatException(ERR_WRITE_STREAM).getMessage(), exp.getMessage());
        }
    }
}
