package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.GrepException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.parser.GrepArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class GrepApplicationTest {
    private GrepArgsParser grepArgsParser;
    private GrepApplication grepApplication;
    private InputStream inputStream;
    private OutputStream outputStream;
    private static final String INVALID = "invalid";
    private static final String TEST_STR = "test";
    private static final String STDIN_ARG = "-";
    @BeforeEach
    void setup() {
        grepArgsParser = mock(GrepArgsParser.class);
        grepApplication = new GrepApplication(grepArgsParser);
        inputStream = mock(InputStream.class);
        outputStream = mock(OutputStream.class);
    }

    /**
     * Run with null args should throw exception
     */
    @Test
    void run_NullArgs_ThrowsException() {
        Throwable exp = assertThrows(GrepException.class, () -> grepApplication.run(null, inputStream, outputStream));
        assertEquals(new GrepException(ERR_NULL_ARGS).getMessage(), exp.getMessage());
    }

    /**
     * Run with invalid args from parser should throw exception
     * @throws InvalidArgsException
     */
    @Test
    void run_InvalidArgsFromParser_ThrowsException() throws InvalidArgsException {
        doThrow(new InvalidArgsException(TEST_STR)).when(grepArgsParser).parse(any());
        Throwable exp = assertThrows(GrepException.class, () -> grepApplication.run(new String[]{STDIN_ARG + INVALID}, inputStream, outputStream));
        assertEquals(new GrepException(new InvalidArgsException(TEST_STR)).getMessage(), exp.getMessage());
    }

    @Test
    void run_CannotWriteToStdout_ThrowsException() throws IOException, AbstractApplicationException {
        doThrow(new IOException(TEST_STR)).when(outputStream).write(any());
        doReturn(true).when(grepArgsParser).isGrepFromStdin();
        doReturn(TEST_STR).when(grepArgsParser).getPattern();
        GrepApplication spyApp = spy(grepApplication);
        doReturn(TEST_STR).when(spyApp).grepFromStdin(anyString(), any(), any(), any(), any(InputStream.class));
        Throwable exp = assertThrows(GrepException.class, () -> spyApp.run(new String[]{TEST_STR}, inputStream, outputStream));
        assertEquals(new GrepException(new IOException(TEST_STR)).getMessage(), exp.getMessage());
    }

    @Test
    void grepFromStdin_NullStdin_ThrowsException() {
        Throwable exp = assertThrows(GrepException.class, () -> grepApplication.grepFromStdin(TEST_STR, false, false, false, null));
        assertEquals(new GrepException(ERR_NULL_STREAMS).getMessage(), exp.getMessage());
    }

    @Test
    void grepFromStdin_EmptyPattern_ThrowsException() throws IOException {
        Throwable exp = assertThrows(GrepException.class, () -> grepApplication.grepFromStdin("", false, false, false, inputStream));
        assertEquals(new GrepException(GrepApplication.EMPTY_PATTERN).getMessage(), exp.getMessage());
    }

    @Test
    void grepFromStdin_CannotReadStdin_ThrowsException() throws IOException {
        try (MockedStatic<IOUtils> mockedIo = mockStatic(IOUtils.class)) {
            mockedIo.when(() -> IOUtils.getLinesFromInputStream(any(InputStream.class))).thenThrow(new IOException(TEST_STR));
            Throwable exp = assertThrows(GrepException.class, () -> grepApplication.grepFromStdin(TEST_STR, false, false, false, inputStream));
            assertEquals(new GrepException(new IOException(TEST_STR)).getMessage(), exp.getMessage());
        }
    }

    @Test
    void grepFromFiles_BlankFileInFileNames_ReturnsExceptionString() throws AbstractApplicationException {
        String res = grepApplication.grepFromFiles(TEST_STR, false, false, false, new String[]{""});
        assertEquals(new GrepException(ERR_NO_FILE_ARGS).getMessage(), res);
    }

    @Test
    void grepFromFiles_IsDirectory_ReturnsExceptionString() throws AbstractApplicationException {
        try (
                MockedStatic<IOUtils> mockedIo = mockStatic(IOUtils.class);
                MockedStatic<Files> mockedFiles = mockStatic(Files.class)
        ) {
            mockedIo.when(() -> IOUtils.resolveFilePath(anyString())).thenReturn(mock(Path.class));
            mockedFiles.when(() -> Files.exists(any(Path.class))).thenReturn(true);
            mockedFiles.when(() -> Files.isDirectory(any(Path.class))).thenReturn(true);
            String res = grepApplication.grepFromFiles(TEST_STR, false, false, false, TEST_STR);
            assertEquals(new GrepException(TEST_STR, ERR_IS_DIR).getMessage(), res);
        }
    }

    @Test
    void grepFromFiles_CannotOpenFileInputStream_ReturnsExceptionString() throws AbstractApplicationException {
        try (
                MockedStatic<IOUtils> mockedIo = mockStatic(IOUtils.class);
                MockedStatic<Files> mockedFiles = mockStatic(Files.class)
        ) {
            mockedIo.when(() -> IOUtils.resolveFilePath(anyString())).thenReturn(mock(Path.class));
            mockedFiles.when(() -> Files.exists(any(Path.class))).thenReturn(true);
            mockedFiles.when(() -> Files.isDirectory(any(Path.class))).thenReturn(false);
            mockedIo.when(() -> IOUtils.openInputStream(anyString())).thenThrow(new ShellException(TEST_STR));
            String res = grepApplication.grepFromFiles(TEST_STR, false, false, false, TEST_STR);
            assertEquals(new GrepException(TEST_STR, new ShellException(TEST_STR)).getMessage(), res);
        }
    }

    @Test
    void grepFromFileAndStdin_CannotWriteToStdout_ThrowsException() throws IOException {
        grepApplication.setOutputStream(outputStream);
        doThrow(new IOException(TEST_STR)).when(outputStream).write(any());
        Throwable exp = assertThrows(GrepException.class, () -> grepApplication.grepFromFileAndStdin(TEST_STR, false, false, false, inputStream, STDIN_ARG));
        assertEquals(new GrepException(new IOException(TEST_STR)).getMessage(), exp.getMessage());
    }

    @Test
    void run_IsGrepFromStdin_CallsGrepFromStdin() throws AbstractApplicationException, IOException {
        doReturn(true).when(grepArgsParser).isGrepFromStdin();
        doReturn(TEST_STR).when(grepArgsParser).getPattern();
        GrepApplication spyApp = spy(grepApplication);
        doReturn(TEST_STR).when(spyApp).grepFromStdin(anyString(), anyBoolean(), anyBoolean(), anyBoolean(), any(InputStream.class));
        spyApp.run(new String[]{TEST_STR}, inputStream, outputStream);
        verify(spyApp).grepFromStdin(TEST_STR, false, false, false, inputStream);
    }

    @Test
    void run_IsGrepFromFiles_CallsGrepFromFiles() throws AbstractApplicationException {
        doReturn(true).when(grepArgsParser).isGrepFromFiles();
        doReturn(TEST_STR).when(grepArgsParser).getPattern();
        doReturn(new String[]{TEST_STR}).when(grepArgsParser).getFileNames();
        GrepApplication spyApp = spy(grepApplication);
        doReturn(TEST_STR).when(spyApp).grepFromFiles(anyString(), anyBoolean(), anyBoolean(), anyBoolean(), any());
        spyApp.run(new String[]{TEST_STR}, inputStream, outputStream);
        verify(spyApp).grepFromFiles(TEST_STR, false, false, false, new String[]{TEST_STR});
    }

    @Test
    void run_IsGrepFromFilesAndStdin_CallsGrepFromFileAndStdin() throws AbstractApplicationException, IOException {
        doReturn(true).when(grepArgsParser).isGrepFromFilesAndStdin();
        doReturn(TEST_STR).when(grepArgsParser).getPattern();
        doReturn(new String[]{TEST_STR}).when(grepArgsParser).getFileNames();
        GrepApplication spyApp = spy(grepApplication);
        doReturn(TEST_STR).when(spyApp).grepFromFileAndStdin(anyString(), anyBoolean(), anyBoolean(), anyBoolean(), any(InputStream.class), anyString());
        spyApp.run(new String[]{TEST_STR}, inputStream, outputStream);
        verify(spyApp).grepFromFileAndStdin(TEST_STR, false, false, false, inputStream, TEST_STR);
    }

    @Test
    void grepFromFileAndStdin_FileThenStdin_PrintsAndReturnsOutput() throws AbstractApplicationException, IOException {
        GrepApplication spyApp = spy(grepApplication);
        doReturn(TEST_STR).when(spyApp).grepFromFiles(anyString(), anyBoolean(), anyBoolean(), anyBoolean(), any());
        doReturn(TEST_STR).when(spyApp).grepFromStdin(anyString(), anyBoolean(), anyBoolean(), anyBoolean(), any(InputStream.class));
        spyApp.setOutputStream(outputStream);
        String res = spyApp.grepFromFileAndStdin(TEST_STR, false, false, false, inputStream, TEST_STR, STDIN_ARG);
        assertEquals(TEST_STR, res);
        verify(outputStream).write((TEST_STR + STRING_NEWLINE).getBytes());
    }

    @Test
    void grepFromFileAndStdin_StdinThenFile_PrintsEmptyStringAndReturnsOutput() throws AbstractApplicationException, IOException {
        GrepApplication spyApp = spy(grepApplication);
        doReturn(TEST_STR).when(spyApp).grepFromFiles(anyString(), anyBoolean(), anyBoolean(), anyBoolean(), any());
        doReturn(TEST_STR).when(spyApp).grepFromStdin(anyString(), anyBoolean(), anyBoolean(), anyBoolean(), any(InputStream.class));
        spyApp.setOutputStream(outputStream);
        String res = spyApp.grepFromFileAndStdin(TEST_STR, false, false, false, inputStream, STDIN_ARG, TEST_STR);
        assertEquals(TEST_STR + STRING_NEWLINE + TEST_STR, res);
        verify(outputStream).write(("").getBytes());
    }
}
