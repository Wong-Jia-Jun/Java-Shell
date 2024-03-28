package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import sg.edu.nus.comp.cs4218.exception.*;
import sg.edu.nus.comp.cs4218.impl.parser.CutArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.RangeHelper;
import sg.edu.nus.comp.cs4218.impl.util.RangeHelperFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;


/**
 * Tests Functionality of cut application
 */
public class CutApplicationTest {
    private final String newLine = System.getProperty("line.separator");
    private static final String EXPECTED_ARG = "abc";
    private static final String EXPECTED_CHAR = "£€\uD800\uDF48";
    private static final String EXPECTED_OUTPUT = "expected output";
    private static final String FILE = "file";
    private static final String MOCK = "mock";

    /**
     * The type Positive tests.
     */
    @Nested
    class PositiveTests {
        private CutArgsParser cutArgsParser;
        private CutApplication cutApplication;
        private RangeHelperFactory rangeHelpFact;
        private RangeHelper rangeHelper;
        private InputStream inputStream;
        private OutputStream outputStream;

        private String getExpectedLineChar(String args) {
            if (args.equals(EXPECTED_ARG)) {
                return EXPECTED_ARG + newLine;
            } else if (args.isEmpty()) {
                return newLine;
            } else if (args.equals(EXPECTED_CHAR)) {
                return EXPECTED_CHAR + newLine;
            } else {
                return null;
            }
        }

        private String getExpectedLineByte(String args) {
            if (args.equals(EXPECTED_ARG)) {
                return EXPECTED_ARG + newLine;
            } else if (args.isEmpty()) {
                return newLine;
            } else if (args.equals(EXPECTED_CHAR)) {
                byte[] bytes = new byte[]{(byte) 0xC2, (byte) 0xA3, (byte) 0xE2};
                String result = new String(bytes, StandardCharsets.UTF_8);
                return result + newLine;
            } else {
                return null;
            }
        }

        /**
         * Sets up.
         *
         * @throws RangeHelperException the range helper exception
         */
        @BeforeEach
        void setUp() throws RangeHelperException {
            this.cutArgsParser = mock(CutArgsParser.class);
            this.rangeHelpFact = mock(RangeHelperFactory.class);
            this.rangeHelper = mock(RangeHelper.class);
            when(this.rangeHelpFact.createRangeHelper(any())).thenReturn(this.rangeHelper);
            when(rangeHelper.contains(1)).thenReturn(true);
            when(rangeHelper.contains(2)).thenReturn(true);
            when(rangeHelper.contains(3)).thenReturn(true);
            when(rangeHelper.isEmpty()).thenReturn(false);
            this.cutApplication = new CutApplication(cutArgsParser, rangeHelpFact);
            this.inputStream = mock(InputStream.class);
            this.outputStream = mock(OutputStream.class);
            this.cutApplication.setStdIn(inputStream);
            this.cutApplication.setStdOut(outputStream);
        }

        /**
         * Run call cut stdin writes to output stream.
         *
         * @throws AbstractApplicationException the abstract application exception
         */
        @Test
        void run_CallCutStdin_WritesToOutputStream() throws AbstractApplicationException {
            when(cutArgsParser.isCutStdin()).thenReturn(true);
            CutApplication spyApplication = spy(new CutApplication(cutArgsParser, rangeHelpFact));
            doReturn(EXPECTED_OUTPUT).when(spyApplication).cutFromStdin(anyBoolean(), anyBoolean(), any(),
                    any(InputStream.class));
            OutputStream outputStream = new ByteArrayOutputStream();
            spyApplication.run(new String[]{"-c", "1"}, inputStream, outputStream);
            assertEquals(EXPECTED_OUTPUT, outputStream.toString());
        }

        /**
         * Run call cut files writes to output stream.
         *
         * @throws AbstractApplicationException the abstract application exception
         * @throws RangeHelperException         the range helper exception
         */
        @Test
        void run_CallCutFiles_WritesToOutputStream() throws AbstractApplicationException, RangeHelperException {
            when(cutArgsParser.isCutStdin()).thenReturn(false);
            when(cutArgsParser.isCutFiles()).thenReturn(true);
            when(cutArgsParser.isCutByByte()).thenReturn(true);
            when(cutArgsParser.getFiles()).thenReturn(new String[]{FILE});
            when(rangeHelpFact.createRangeHelper(any())).thenReturn(rangeHelper);
            CutApplication spyApplication = spy(new CutApplication(cutArgsParser, rangeHelpFact));
            doReturn(EXPECTED_OUTPUT).when(spyApplication).cutFromFiles(anyBoolean(), anyBoolean(), any(),
                    anyString());
            OutputStream outputStream = new ByteArrayOutputStream();
            spyApplication.run(new String[]{"-c", "1"}, inputStream, outputStream);
            assertEquals(EXPECTED_OUTPUT, outputStream.toString());
        }

        /**
         * Cut from line cut by char returns cut line.
         *
         * @param line the line
         */
        @ParameterizedTest
        @ValueSource(strings = {EXPECTED_ARG, "", EXPECTED_CHAR})
        void cutFromLine_CutByChar_ReturnsCutLine(String line) {
            String result = cutApplication.cutFromLine(line, true, false, rangeHelper);
            assertEquals(getExpectedLineChar(line), result);
        }

        /**
         * Cut from line cut by byte returns cut line.
         *
         * @param line the line
         */
        @ParameterizedTest
        @ValueSource(strings = {EXPECTED_ARG, "", EXPECTED_CHAR})
        void cutFromLine_CutByByte_ReturnsCutLine(String line) {
            String result = cutApplication.cutFromLine(line, false, true, rangeHelper);
            assertEquals(getExpectedLineByte(line), result);
        }

        /**
         * Cut from stdin cut by char successful returns cut output.
         *
         * @param line the line
         * @throws RangeHelperException         the range helper exception
         * @throws AbstractApplicationException the abstract application exception
         */
        @ParameterizedTest
        @ValueSource(strings = {EXPECTED_ARG, "", EXPECTED_CHAR})
        void cutFromStdin_CutByCharSuccessful_ReturnsCutOutput(String line) throws RangeHelperException, AbstractApplicationException {
            try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
                mockedStatic.when(() -> IOUtils.getLinesFromInputStream(any(InputStream.class)))
                        .thenReturn(List.of(line));
                String output = cutApplication.cutFromStdin(true, false,
                        List.of(new int[]{1, 3}), inputStream);
                assertEquals(getExpectedLineChar(line), output);
            }
        }

        /**
         * Cut from stdin cut by byte successful returns cut output.
         *
         * @param line the line
         * @throws AbstractApplicationException the abstract application exception
         */
        @ParameterizedTest
        @ValueSource(strings = {EXPECTED_ARG, "", EXPECTED_CHAR})
        void cutFromStdin_CutByByteSuccessful_ReturnsCutOutput(String line) throws AbstractApplicationException {
            try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
                mockedStatic.when(() -> IOUtils.getLinesFromInputStream(any(InputStream.class)))
                        .thenReturn(List.of(line));
                String output = cutApplication.cutFromStdin(false, true,
                        List.of(new int[]{1, 3}), inputStream);
                assertEquals(getExpectedLineByte(line), output);
            }
        }

        /**
         * Cut from files only dash prints nothing and returns.
         *
         * @param flag the flag
         * @throws AbstractApplicationException the abstract application exception
         */
        @ParameterizedTest
        @ValueSource(strings = {"-c", "-b"})
        void cutFromFiles_OnlyDash_PrintsNothingAndReturns(String flag)
               throws AbstractApplicationException {
            CutApplication spyApplication = spy(new CutApplication(cutArgsParser, rangeHelpFact));
            doReturn(EXPECTED_OUTPUT).when(spyApplication).cutFromStdin(anyBoolean(), anyBoolean(), any(),
                    any(InputStream.class));
            spyApplication.setStdIn(inputStream);
            spyApplication.setStdOut(outputStream);
            String output;
            if ("-c".equals(flag)) {
                output = spyApplication.cutFromFiles(true, false, List.of(new int[]{1, 3}),
                        "-");
            } else {
                output = spyApplication.cutFromFiles(false, true, List.of(new int[]{1, 3}),
                        "-");
            }
            assertEquals(EXPECTED_OUTPUT, output);
        }

        /**
         * Cut from files only files never prints and returns.
         *
         * @param flag the flag
         * @throws AbstractApplicationException the abstract application exception
         */
        @ParameterizedTest
        @ValueSource(strings = {"-c", "-b"})
        void cutFromFiles_OnlyFiles_NeverPrintsAndReturns(String flag)
                throws AbstractApplicationException {
            try (
                    MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class);
                    MockedStatic<Files> mockedFiles = mockStatic(Files.class)
            ) {
                mockedFiles.when(() -> Files.exists(any())).thenReturn(true);
                mockedFiles.when(() -> Files.isDirectory(any())).thenReturn(false);
                mockedStatic.when(() -> IOUtils.openInputStream(anyString()))
                        .thenReturn(inputStream);
                mockedStatic.when(() -> IOUtils.getLinesFromInputStream(any(InputStream.class)))
                        .thenReturn(List.of("example input", "hi"));
                String output;
                if ("-c".equals(flag)) {
                    output = cutApplication.cutFromFiles(true, false, List.of(new int[]{1, 3}),
                            FILE);
                } else {
                    output = cutApplication.cutFromFiles(false, true, List.of(new int[]{1, 3}),
                            FILE);
                }
                assertEquals("exa" + newLine + "hi" + newLine, output);
                verify(outputStream, never()).write(any(byte[].class));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Cut from files file then dash prints file output then returns stdin content.
         *
         * @param flag the flag
         * @throws AbstractApplicationException the abstract application exception
         */
        @ParameterizedTest
        @ValueSource(strings = {"-c", "-b"})
        void cutFromFiles_FileThenDash_PrintsFileOutputThenReturnsStdinContent(String flag)
                throws AbstractApplicationException {
            try (
                    MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class);
                    MockedStatic<Files> mockedFiles = mockStatic(Files.class)
            ) {
                mockedFiles.when(() -> Files.exists(any())).thenReturn(true);
                mockedFiles.when(() -> Files.isDirectory(any())).thenReturn(false);
                mockedStatic.when(() -> IOUtils.getLinesFromInputStream(any(InputStream.class)))
                        .thenReturn(List.of("example input", "hi"));
                mockedStatic.when(() -> IOUtils.openInputStream(anyString()))
                        .thenReturn(inputStream);
                String output;
                if ("-c".equals(flag)) {
                    output = cutApplication.cutFromFiles(true, false, List.of(new int[]{1, 3}),
                            new String[] {FILE, "-"});
                } else {
                    output = cutApplication.cutFromFiles(false, true, List.of(new int[]{1, 3}),
                            new String[] {FILE, "-"});
                }
                assertEquals("exa" + newLine + "hi" + newLine, output);
                String expectedOutput = "exa" + System.getProperty("line.separator")
                        + "hi" + System.getProperty("line.separator");
                verify(outputStream).write(expectedOutput.getBytes());
                verify(outputStream, times(1)).write(any(byte[].class));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * The type Null and invalid args tests.
     */
    @Nested
    class NullAndInvalidArgsTests {
        /**
         * The Cut args parser.
         */
        CutArgsParser cutArgsParser;
        /**
         * The Cut application.
         */
        CutApplication cutApplication;
        /**
         * The Range help fact.
         */
        RangeHelperFactory rangeHelpFact; //NOPMD - suppressed LongVariable - Long Variable needed to specify use
        /**
         * The Range helper.
         */
        RangeHelper rangeHelper;
        /**
         * The Input stream.
         */
        InputStream inputStream;
        /**
         * The Output stream.
         */
        OutputStream outputStream;
        private static final String FILE_STRING = "file";
        private static final String EXPECTED_OUTPUT = "expected output";
        private static final String TEST_STRING = "test";

        /**
         * Sets up.
         *
         * @throws RangeHelperException the range helper exception
         */
        @BeforeEach
        void setUp() throws RangeHelperException {
            this.cutArgsParser = mock(CutArgsParser.class);
            this.rangeHelpFact = mock(RangeHelperFactory.class);
            this.rangeHelper = mock(RangeHelper.class);
            when(this.rangeHelpFact.createRangeHelper(any())).thenReturn(this.rangeHelper);
            this.cutApplication = new CutApplication(cutArgsParser, rangeHelpFact);
            this.inputStream = mock(InputStream.class);
            this.outputStream = mock(OutputStream.class);
            this.cutApplication.setStdIn(inputStream);
            this.cutApplication.setStdOut(outputStream);
        }

        /**
         * Run null args throws exception.
         */
        @Test
        void run_NullArgs_ThrowsException() {
            Throwable exp = assertThrows(CutException.class, () -> {
                cutApplication.run(null, inputStream, outputStream);
            });
            assertEquals(new CutException(ERR_NULL_ARGS).getMessage(), exp.getMessage());
        }

        /**
         * Run null input stream throws exception.
         */
        @Test
        void run_NullInputStream_ThrowsException() {
            Throwable exp = assertThrows(CutException.class, () -> {
                cutApplication.run(new String[]{"-c", "1"}, null, outputStream);
            });
            assertEquals(new CutException(ERR_NULL_STREAMS).getMessage(), exp.getMessage());
        }

        /**
         * Run null output stream throws exception.
         */
        @Test
        void run_NullOutputStream_ThrowsException() {
            Throwable exp = assertThrows(CutException.class, () -> {
                cutApplication.run(new String[]{"-c", "1"}, inputStream, null);
            });
            assertEquals(new CutException(ERR_NULL_STREAMS).getMessage(), exp.getMessage());
        }

        /**
         * Run invalid args parsed throws exception.
         *
         * @throws InvalidArgsException the invalid args exception
         */
        @Test
        void run_InvalidArgsParsed_ThrowsException() throws InvalidArgsException {
            doThrow(new InvalidArgsException(TEST_STRING)).when(cutArgsParser).parse(any());
            Throwable thrown = assertThrows(CutException.class, () -> {
                cutApplication.run(new String[]{MOCK}, inputStream, outputStream);
            });
            assertEquals(new CutException(TEST_STRING).getMessage(),
                    thrown.getMessage());
        }

        /**
         * Run invalid ranges throws exception.
         *
         * @throws RangeHelperException the range helper exception
         */
        @Test
        void run_InvalidRanges_ThrowsException() throws RangeHelperException {
            when(this.rangeHelpFact.createRangeHelper(any())).thenThrow(new RangeHelperException(TEST_STRING));
            Throwable thrown = assertThrows(CutException.class, () -> {
                cutApplication.run(new String[]{"-c", "1-2-3"}, inputStream, outputStream);
            });
            assertEquals(new CutException(new RangeHelperException(TEST_STRING).getMessage()).getMessage(),
                    thrown.getMessage());
        }

        /**
         * Run closed output stream throws exception.
         *
         * @throws IOException                  the io exception
         * @throws AbstractApplicationException the abstract application exception
         */
        @Test
        void run_ClosedOutputStream_ThrowsException() throws IOException, AbstractApplicationException {
            doThrow(new IOException("closed")).when(outputStream).write(any());
            when(cutArgsParser.isCutStdin()).thenReturn(true);
            CutApplication spyApplication = spy(new CutApplication(cutArgsParser, rangeHelpFact));
            doReturn(EXPECTED_OUTPUT).when(spyApplication).cutFromStdin(anyBoolean(), anyBoolean(), any(),
                    any(InputStream.class));
            Throwable thrown = assertThrows(CutException.class, () -> {
                spyApplication.run(new String[]{MOCK}, inputStream, outputStream);
            });
            assertEquals(new CutException(ERR_IO_EXCEPTION).getMessage(),
                    thrown.getMessage());
        }

        /**
         * Run cut stdin throws exception propagates exception.
         *
         * @throws AbstractApplicationException the abstract application exception
         */
        @Test
        void run_CutStdinThrowsException_PropagatesException() throws AbstractApplicationException {
            when(cutArgsParser.isCutStdin()).thenReturn(true);
            CutApplication spyApplication = spy(new CutApplication(cutArgsParser, rangeHelpFact));
            doThrow(new CutException(TEST_STRING)).when(spyApplication).cutFromStdin(anyBoolean(), anyBoolean(), any(),
                    any(InputStream.class));
            Throwable thrown = assertThrows(CutException.class, () -> {
                spyApplication.run(new String[]{MOCK}, inputStream, outputStream);
            });
            assertEquals(new CutException(TEST_STRING).getMessage(),
                    thrown.getMessage());
        }

        /**
         * Run cut from files throws exception propagates exception.
         *
         * @throws AbstractApplicationException the abstract application exception
         */
        @Test
        void run_CutFromFilesThrowsException_PropagatesException() throws AbstractApplicationException {
            when(cutArgsParser.isCutFiles()).thenReturn(true);
            when(cutArgsParser.getFiles()).thenReturn(new String[]{MOCK});
            CutApplication spyApplication = spy(new CutApplication(cutArgsParser, rangeHelpFact));
            doThrow(new CutException(TEST_STRING)).when(spyApplication).cutFromFiles(anyBoolean(), anyBoolean(), any(),
                    any(String[].class));
            Throwable thrown = assertThrows(CutException.class, () -> {
                spyApplication.run(new String[]{MOCK}, inputStream, outputStream);
            });
            assertEquals(new CutException(TEST_STRING).getMessage(),
                    thrown.getMessage());
        }

        /**
         * Cut from files both flags throws exception.
         */
        @Test
        void cutFromFiles_BothFlags_ThrowsException() {
            ArrayList<int[]> ranges = new ArrayList<>();
            Throwable exp = assertThrows(CutException.class, () -> {
                cutApplication.cutFromFiles(true, true, ranges, FILE_STRING);
            });
            assertEquals(new CutException(CutArgsParser.ILLEGAL_BOTH_FLAG).getMessage(),
                    exp.getMessage());
        }

        /**
         * Cut from files null streams throws exception.
         *
         * @param stream the stream
         */
        @ParameterizedTest
        @ValueSource(strings = {"input", "output", "both"})
        void cutFromFiles_NullStreams_ThrowsException(String stream) {
            if (Objects.equals(stream, "input")) {
                cutApplication.setStdIn(null);
            }
            else if (Objects.equals(stream, "output")) {
                cutApplication.setStdOut(null);
            }
            else {
                cutApplication.setStdIn(null);
                cutApplication.setStdOut(null);
            }
            Throwable exp = assertThrows(CutException.class, () -> {
                cutApplication.cutFromFiles(true, false, new ArrayList<>(), FILE_STRING);
            });
            assertEquals(new CutException(ERR_NULL_STREAMS).getMessage(), exp.getMessage());
        }

        /**
         * Cut from files null files throws exception.
         */
        @Test
        void cutFromFiles_NullFiles_ThrowsException() {
            Throwable exp = assertThrows(CutException.class, () -> {
                cutApplication.cutFromFiles(true, false, new ArrayList<>(), null);
            });
            assertEquals(new CutException(ERR_NULL_ARGS).getMessage(), exp.getMessage());
        }

        /**
         * Cut from files null ranges throws exception.
         */
        @Test
        void cutFromFiles_NullRanges_ThrowsException() {
            Throwable exp = assertThrows(CutException.class, () -> {
                cutApplication.cutFromFiles(true, false, null, FILE_STRING);
            });
            assertEquals(new CutException(ERR_NULL_ARGS).getMessage(), exp.getMessage());
        }

        /**
         * Cut from files empty files or range throws exception.
         *
         * @param arg the arg
         */
        @ParameterizedTest
        @ValueSource(strings = {"fileName", "ranges"})
        void cutFromFiles_EmptyFilesOrRange_ThrowsException(String arg) {
            if (Objects.equals(arg, "fileName")) {
                ArrayList<int[]> ranges = new ArrayList<>();
                ranges.add(new int[]{1, 2});
                Throwable exp = assertThrows(CutException.class, () -> {
                    cutApplication.cutFromFiles(true, false, ranges, new String[0]);
                });
                assertEquals(new CutException(ERR_NO_ARGS).getMessage(), exp.getMessage());
            } else {
                Throwable exp = assertThrows(CutException.class, () -> {
                    String[] files = new String[]{FILE_STRING};
                    cutApplication.cutFromFiles(true, false, new ArrayList<>(), files);
                });
                assertEquals(new CutException(ERR_NO_ARGS).getMessage(), exp.getMessage());
            }
        }

        /**
         * Cut from files just files and invalid ranges throws exception.
         *
         * @throws RangeHelperException the range helper exception
         */
        @Test
        void cutFromFiles_JustFilesAndInvalidRanges_ThrowsException() throws RangeHelperException {
            when(this.rangeHelpFact.createRangeHelper(any())).thenThrow(new RangeHelperException(""));
            ArrayList<int[]> ranges = new ArrayList<>();
            ranges.add(new int[]{1, 2});
            Throwable exp = assertThrows(CutException.class, () -> {
                cutApplication.cutFromFiles(true, false, ranges, FILE_STRING);
            });
            assertEquals(new CutException(new RangeHelperException("").getMessage()).getMessage(), exp.getMessage());
        }

        /**
         * Cut from files stdout closed throws exception.
         *
         * @throws IOException the io exception
         */
        @Test
        void cutFromFiles_StdoutClosed_ThrowsException() throws IOException {
            doThrow(new IOException("Mock")).when(outputStream).write(any());
            ArrayList<int[]> ranges = new ArrayList<>();
            ranges.add(new int[]{1, 2});
            Throwable exp = assertThrows(CutException.class, () -> {
                cutApplication.cutFromFiles(true, false, ranges, "-");
            });
            assertEquals(new CutException("Mock").getMessage(), exp.getMessage());
        }

        /**
         * Cut from files cut from file, file doesn't exist, prints exception.
         */
        @Test
        void cutFromFiles_CutFromFileCantFindFile_PrintsException() throws AbstractApplicationException {
            try (
                    MockedStatic<Files> mockedFiles = mockStatic(Files.class)
            ) {
                mockedFiles.when(() -> Files.exists(any())).thenReturn(false);
                String results = cutApplication.cutFromFiles(true, false, List.of(new int[]{1, 1}),
                        FILE_STRING);
                assertEquals(new CutException(ERR_FILE_NOT_FND, FILE_STRING).getMessage() + newLine,
                        results);
            }
        }

        /**
         * Cut from files cut from file, file is a directory, prints exception.
         */
        @Test
        void cutFromFiles_CutFromFileIsDirectory_PrintsException() throws AbstractApplicationException {
            try (
                    MockedStatic<Files> mockedFiles = mockStatic(Files.class)
            ) {
                mockedFiles.when(() -> Files.exists(any())).thenReturn(true);
                mockedFiles.when(() -> Files.isDirectory(any())).thenReturn(true);
                String results = cutApplication.cutFromFiles(true, false, List.of(new int[]{1, 1}),
                        FILE_STRING);
                assertEquals(new CutException(ERR_IS_DIR, FILE_STRING).getMessage() + newLine,
                        results);
            }
        }

        /**
         * Cut from files cut from file cannot open files throws exception.
         */
        @Test
        void cutFromFiles_CutFromFileCannotOpenFiles_PrintsException() throws AbstractApplicationException {
            try (
                    MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class);
                    MockedStatic<Files> mockedFiles = mockStatic(Files.class)
            ) {
                mockedFiles.when(() -> Files.exists(any())).thenReturn(true);
                mockedFiles.when(() -> Files.isDirectory(any())).thenReturn(false);
                mockedStatic.when(() -> IOUtils.openInputStream(anyString()))
                        .thenThrow(new ShellException(TEST_STRING));
                String results = cutApplication.cutFromFiles(true, false, List.of(new int[]{1, 1}),
                        FILE_STRING);
                assertEquals(new CutException(ERR_READING_FILE, new ShellException(TEST_STRING)).getMessage() + newLine,
                        results);
            }
        }

        /**
         * Cut from files cut from file cannot get lines throws exception.
         */
        @Test
        void cutFromFiles_CutFromFileCannotGetLines_ThrowsException() throws AbstractApplicationException {
            try (
                    MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class);
                    MockedStatic<Files> mockedFiles = mockStatic(Files.class)
            ) {
                mockedFiles.when(() -> Files.exists(any())).thenReturn(true);
                mockedFiles.when(() -> Files.isDirectory(any())).thenReturn(false);
                mockedStatic.when(() -> IOUtils.openInputStream(anyString()))
                        .thenReturn(inputStream);
                mockedStatic.when(() -> IOUtils.getLinesFromInputStream(any(InputStream.class)))
                        .thenThrow(new IOException(TEST_STRING));
                String results = cutApplication.cutFromFiles(true, false, List.of(new int[]{1, 1}),
                        FILE_STRING);
                assertEquals(new CutException(ERR_IO_EXCEPTION, new IOException(TEST_STRING)).getMessage() + newLine,
                        results);
            }
        }

        /**
         * Cut from stdin both flags throws exception.
         */
        @Test
        void cutFromStdin_BothFlags_ThrowsException() {
            ArrayList<int[]> ranges = new ArrayList<>();
            ranges.add(new int[]{1, 2});
            Throwable exp = assertThrows(CutException.class, () -> {
                cutApplication.cutFromStdin(true, true, ranges, inputStream);
            });
            assertEquals(new CutException(CutArgsParser.ILLEGAL_BOTH_FLAG).getMessage(),
                    exp.getMessage());
        }

        /**
         * Cut from stdin null streams throws exception.
         */
        @Test
        void cutFromStdin_NullStreams_ThrowsException() {
            Throwable exp = assertThrows(CutException.class, () -> {
                cutApplication.cutFromStdin(true, false, new ArrayList<>(), null);
            });
            assertEquals(new CutException(ERR_NULL_STREAMS).getMessage(), exp.getMessage());
        }

        /**
         * Cut from stdin null ranges throws exception.
         */
        @Test
        void cutFromStdin_NullRanges_ThrowsException() {
            Throwable exp = assertThrows(CutException.class, () -> {
                cutApplication.cutFromStdin(true, false, null, inputStream);
            });
            assertEquals(new CutException(ERR_NULL_ARGS).getMessage(), exp.getMessage());
        }

        /**
         * Cut from stdin io utils throws io exception throws exception.
         *
         * @throws RangeHelperException the range helper exception
         */
        @Test
        void cutFromStdin_IOUtilsThrowsIOException_ThrowsException() throws RangeHelperException {
            when(rangeHelpFact.createRangeHelper(any())).thenReturn(rangeHelper);
            try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
                mockedStatic.when(() -> IOUtils.getLinesFromInputStream(any(InputStream.class)))
                        .thenThrow(new IOException("Stream closed"));

                Throwable exp = assertThrows(CutException.class, () -> cutApplication.cutFromStdin(true,
                                false, List.of(new int[]{1, 2}), inputStream));
                assertEquals(new CutException("Stream closed").getMessage(), exp.getMessage());
            }
        }

        /**
         * Cut from stdin Range helper throws exception.
         *
         * @throws RangeHelperException the range helper exception
         */
        @Test
        void cutFromStdin_RangeHelperThrowsException_ThrowsException() throws RangeHelperException {
            try (MockedStatic<IOUtils> mockedStatic = mockStatic(IOUtils.class)) {
                mockedStatic.when(() -> IOUtils.getLinesFromInputStream(any(InputStream.class)))
                        .thenReturn(List.of("example input", "hi"));
                when(rangeHelpFact.createRangeHelper(anyList())).thenThrow(new RangeHelperException(TEST_STRING));
                Throwable exp = assertThrows(CutException.class, () -> {
                    cutApplication.cutFromStdin(true, false, List.of(new int[]{1, 2}), inputStream);
                });
                assertEquals(new CutException(new RangeHelperException(TEST_STRING).getMessage())
                        .getMessage(), exp.getMessage());
            }
        }
    }
}
