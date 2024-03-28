package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.RmException;
import sg.edu.nus.comp.cs4218.impl.parser.RmArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static sg.edu.nus.comp.cs4218.impl.parser.ArgsParser.ILLEGAL_FLAG_MSG;
import static sg.edu.nus.comp.cs4218.impl.parser.RmArgsParser.FLAG_IS_EMPTY_DIR;
import static sg.edu.nus.comp.cs4218.impl.parser.RmArgsParser.FLAG_IS_RECURSIVE;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;


/**
 * Tests Functionality of rm application
 */
public class RmApplicationTest {
    private RmApplication rmApplication;
    private RmArgsParser mockedParser;
    private static final String FILE_NAME = "testFile.txt";
    private static final Path FILE_PATH = Path.of(FILE_NAME);
    private static final String EMPTY_DIR_NAME = "testEmptyDir";
    private static final Path EMPTY_DIR_PATH = Path.of(EMPTY_DIR_NAME);
    private static final String DIR_NAME = "testDir";
    private static final Path DIR_PATH = Path.of(DIR_NAME);
    private static final String DIR_FILE_NAME = "innerTestFile.txt";
    private static final Path DIR_FILE_PATH = Path.of( DIR_PATH + "/" + DIR_FILE_NAME);
    private static final String RM_PREFIX = "rm: ";
    private static final String IO_EXP_MSG = "IO Exception";
    private final String RECURSIVE_FLAG = "-" + FLAG_IS_RECURSIVE;
    private final String EMPTY_FOLDER_FLAG = "-" + FLAG_IS_EMPTY_DIR;

    /**
     * Init.
     */
    @BeforeAll
    static void init() throws IOException {
        // Remove IOUtils.resolveFilePath static dependency
        try (MockedStatic<IOUtils> mockedUtils = mockStatic(IOUtils.class)) {
            mockedUtils.when(() -> IOUtils.resolveFilePath(FILE_NAME)).thenReturn(FILE_PATH);
            assertEquals(FILE_PATH, IOUtils.resolveFilePath(FILE_NAME));
            mockedUtils.when(() -> IOUtils.resolveFilePath(EMPTY_DIR_NAME)).thenReturn(EMPTY_DIR_PATH);
            assertEquals(EMPTY_DIR_PATH, IOUtils.resolveFilePath(EMPTY_DIR_NAME));
            mockedUtils.when(() -> IOUtils.resolveFilePath(DIR_NAME)).thenReturn(DIR_PATH);
            assertEquals(DIR_PATH, IOUtils.resolveFilePath(DIR_NAME));
        }
    }

    /**
     * Sets .
     *
     * @throws IOException the io exception
     */
    @BeforeEach
    void setup() throws IOException {
            mockedParser = mock(RmArgsParser.class);
            when(mockedParser.isEmptyFolder()).thenReturn(false);
            when(mockedParser.isRecursive()).thenReturn(false);
            rmApplication = new RmApplication(mockedParser);
            Files.createDirectory(EMPTY_DIR_PATH);
            Files.createDirectory(DIR_PATH);
            Files.createFile(DIR_FILE_PATH);
            Files.createFile(FILE_PATH);
    }

    /**
     * Teardown.
     *
     * @throws IOException the io exception
     */
    @AfterEach
    void teardown() throws IOException {
        Files.deleteIfExists(FILE_PATH);
        Files.deleteIfExists(DIR_FILE_PATH);
        Files.deleteIfExists(EMPTY_DIR_PATH);
        Files.deleteIfExists(DIR_PATH);
    }

    private String formatExceptionMessage(String pathname, String message) {
        return String.format("rm: cannot remove '%s': %s", pathname, message);
    }

    /**
     * Remove null filename should throw rm exception.
     */
    /* remove */
    @Test
    void remove_NullFilename_ShouldThrowRmException() {
        Throwable thrown = assertThrows(RmException.class, () -> rmApplication.remove(false, false, null));
        assertEquals(RM_PREFIX + ERR_GENERAL, thrown.getMessage());
    }

    /**
     * Remove invalid filename should throw rm exception.
     */
    @Test
    void remove_InvalidFilename_ShouldThrowRmException() {
        String filename = "test.txt";
        Throwable thrown = assertThrows(RmException.class, () -> rmApplication.remove(false, false, filename));
        assertEquals(formatExceptionMessage(filename, ERR_FILE_NOT_FND), thrown.getMessage());
    }

    /**
     * Remove valid file with no flags should delete file.
     *
     * @throws RmException the rm exception
     */
    @Test
    void remove_ValidFileWithNoFlags_ShouldDeleteFile() throws RmException {
        assertTrue(Files.exists(FILE_PATH));
        rmApplication.remove(false, false, String.valueOf(FILE_PATH));
        assertFalse(Files.exists(FILE_PATH));
    }

    /**
     * Remove empty directory with no flags should throw rm exception.
     */
    @Test
    void remove_EmptyDirectoryWithNoFlags_ShouldThrowRmException() {
        assertTrue(Files.exists(EMPTY_DIR_PATH));
        Throwable thrown = assertThrows(RmException.class, () -> rmApplication.remove(false, false, EMPTY_DIR_NAME));
        assertEquals(formatExceptionMessage(EMPTY_DIR_NAME, ERR_IS_DIR), thrown.getMessage());
    }

    /**
     * Remove empty directory with empty flags should delete directory.
     *
     * @throws RmException the rm exception
     */
    @Test
    void remove_EmptyDirectoryWithEmptyFlags_ShouldDeleteDirectory() throws RmException {
        assertTrue(Files.exists(EMPTY_DIR_PATH));
        rmApplication.remove(true, false, EMPTY_DIR_NAME);
        assertFalse(Files.exists(EMPTY_DIR_PATH));
    }

    /**
     * Remove empty directory with recursive flags should delete directory.
     *
     * @throws RmException the rm exception
     */
    @Test
    void remove_EmptyDirectoryWithRecursiveFlags_ShouldDeleteDirectory() throws RmException {
        assertTrue(Files.exists(EMPTY_DIR_PATH));
        rmApplication.remove(false, true, EMPTY_DIR_NAME);
        assertFalse(Files.exists(EMPTY_DIR_PATH));
    }

    /**
     * Remove empty directory with recursive and empty flags should delete directory.
     *
     * @throws RmException the rm exception
     */
    @Test
    void remove_EmptyDirectoryWithRecursiveAndEmptyFlags_ShouldDeleteDirectory() throws RmException {
        assertTrue(Files.exists(EMPTY_DIR_PATH));
        rmApplication.remove(true, true, EMPTY_DIR_NAME);
        assertFalse(Files.exists(EMPTY_DIR_PATH));
    }

    /**
     * Remove file directory with no flags should throw rm exception.
     */
    @Test
    void remove_FileDirectoryWithNoFlags_ShouldThrowRmException() {
        assertTrue(Files.exists(DIR_PATH));
        Throwable thrown = assertThrows(RmException.class, () -> rmApplication.remove(false, false, DIR_NAME));
        assertEquals(formatExceptionMessage(DIR_NAME, ERR_IS_DIR), thrown.getMessage());
    }

    /**
     * Remove file directory with empty flags should throw rm exception.
     */
    @Test
    void remove_FileDirectoryWithEmptyFlags_ShouldThrowRmException()  {
        assertTrue(Files.exists(DIR_PATH));
        Throwable thrown = assertThrows(RmException.class, () -> rmApplication.remove(true, false, DIR_NAME));
        assertEquals(formatExceptionMessage(DIR_NAME, ERR_DIR_NOT_EMPTY), thrown.getMessage());
    }

    /**
     * Remove file directory and is recursive list throws IO exception should throw rm exception
     */
    @Test
    void remove_FileDirectorRecursiveIOExceptionOnList_ShouldThrowRmException()  {
        assertTrue(Files.exists(DIR_PATH));
        try (
                MockedStatic<Files> mockedFiles = mockStatic(Files.class);
                MockedStatic<IOUtils> mockedUtils = mockStatic(IOUtils.class)
        ) {
            mockedFiles.when(() -> Files.delete(any(Path.class)))
                    .thenThrow(new IOException(IO_EXP_MSG));
            File mockFile = mock(File.class);
            when(mockFile.exists()).thenReturn(true);
            when(mockFile.isDirectory()).thenReturn(true);
            when(mockFile.canRead()).thenReturn(true);
            when(mockFile.list()).thenReturn(new String[]{});
            Path mockPath = mock(Path.class);
            when(mockPath.toFile()).thenReturn(mockFile);
            when(mockPath.toString()).thenReturn(DIR_NAME);
            mockedUtils.when(() -> IOUtils.resolveFilePath(anyString()))
                    .thenReturn(mockPath);
            Throwable thrown = assertThrows(RmException.class, () -> rmApplication.remove(
                    false, true, DIR_NAME));
            assertEquals(formatExceptionMessage(mockPath.toString(), IO_EXP_MSG), thrown.getMessage());
        }
    }

    /**
     * Remove file directory and is empty folder list throws IO exception should throw rm exception
     */
    @Test
    void remove_FileDirectorIsEmptyIOExceptionOnList_ShouldThrowRmException()  {
        assertTrue(Files.exists(DIR_PATH));
        try (
                MockedStatic<Files> mockedFiles = mockStatic(Files.class);
                MockedStatic<IOUtils> mockedUtils = mockStatic(IOUtils.class)
        ) {
            mockedFiles.when(() -> Files.delete(any(Path.class)))
                    .thenThrow(new IOException(IO_EXP_MSG));
            File mockFile = mock(File.class);
            when(mockFile.exists()).thenReturn(true);
            when(mockFile.isDirectory()).thenReturn(true);
            when(mockFile.canRead()).thenReturn(true);
            when(mockFile.list()).thenReturn(new String[]{});
            Path mockPath = mock(Path.class);
            when(mockPath.toFile()).thenReturn(mockFile);
            when(mockPath.toString()).thenReturn(DIR_NAME);
            mockedUtils.when(() -> IOUtils.resolveFilePath(anyString()))
                    .thenReturn(mockPath);
            Throwable thrown = assertThrows(RmException.class, () -> rmApplication.remove(
                    true, false, DIR_NAME));
            assertEquals(formatExceptionMessage(mockPath.toString(), IO_EXP_MSG), thrown.getMessage());
        }
    }

    /**
     * Remove file directory that cannot be read should throw rm exception.
     */
    @Test
    void remove_FileDirectoryNodeCannotRead_ShouldThrowRmException()  {
        assertTrue(Files.exists(DIR_PATH));
        try (MockedStatic<IOUtils> mockedUtils = mockStatic(IOUtils.class)) {
            File mockFile = mock(File.class);
            when(mockFile.exists()).thenReturn(true);
            when(mockFile.isDirectory()).thenReturn(false);
            when(mockFile.canRead()).thenReturn(false);
            Path mockPath = mock(Path.class);
            when(mockPath.toFile()).thenReturn(mockFile);
            mockedUtils.when(() -> IOUtils.resolveFilePath(anyString()))
                    .thenReturn(mockPath);
            Throwable thrown = assertThrows(RmException.class, () -> rmApplication.remove(
                    false, false, DIR_NAME));
            assertEquals(formatExceptionMessage(DIR_NAME, ERR_NO_PERM), thrown.getMessage());
        }
    }

    /**
     * Remove file directory that cannot be deleted should throw rm exception.
     */
    @Test
    void remove_FileDirectoryNodeCannotDelete_ShouldThrowRmException()  {
        assertTrue(Files.exists(DIR_PATH));
        try (
                MockedStatic<IOUtils> mockedUtils = mockStatic(IOUtils.class);
                MockedStatic<Files> mockedFiles = mockStatic(Files.class)
        ) {
            File mockFile = mock(File.class);
            when(mockFile.exists()).thenReturn(true);
            when(mockFile.isDirectory()).thenReturn(false);
            when(mockFile.canRead()).thenReturn(true);
            Path mockPath = mock(Path.class);
            when(mockPath.toFile()).thenReturn(mockFile);
            mockedUtils.when(() -> IOUtils.resolveFilePath(anyString()))
                    .thenReturn(mockPath);
            mockedFiles.when(() -> Files.delete(mockPath))
                    .thenThrow(new IOException(IO_EXP_MSG));
            Throwable thrown = assertThrows(RmException.class, () -> rmApplication.remove(
                    false, false, DIR_NAME));
            assertEquals(formatExceptionMessage(DIR_NAME, IO_EXP_MSG), thrown.getMessage());
        }
    }

    /**
     * Remove file directory with recursive flags should delete directory.
     *
     * @throws RmException the rm exception
     */
    @Test
    void remove_FileDirectoryWithRecursiveFlags_ShouldDeleteDirectory() throws RmException {
        assertTrue(Files.exists(DIR_PATH));
        rmApplication.remove(false, true, DIR_NAME);
        assertFalse(Files.exists(DIR_PATH));
    }

    /**
     * Remove file directory with recursive and empty flags should delete directory.
     *
     * @throws RmException the rm exception
     */
    @Test
    void remove_FileDirectoryWithRecursiveAndEmptyFlags_ShouldDeleteDirectory() throws RmException {
        assertTrue(Files.exists(DIR_PATH));
        rmApplication.remove(true, true, DIR_NAME);
        assertFalse(Files.exists(DIR_PATH));
    }

    /* run */

    /**
     * Run null stdin should throw rm exception.
     */
    @Test
    void run_NullStdin_ShouldThrowRmException() {
        String[] args = {""};
        Throwable thrown = assertThrows(RmException.class, () -> rmApplication.run(args, null, System.out));
        assertEquals(RM_PREFIX + ERR_NULL_STREAMS, thrown.getMessage());
    }

    /**
     * Run null stdout should throw rm exception.
     */
    @Test
    void run_NullStdout_ShouldThrowRmException() {
        String[] args = {""};
        Throwable thrown = assertThrows(RmException.class, () -> rmApplication.run(args, System.in, null));
        assertEquals(RM_PREFIX + ERR_NULL_STREAMS, thrown.getMessage());
    }

    /**
     * Run null args should throw rm exception.
     */
    @Test
    void run_NullArgs_ShouldThrowRmException() {
        Throwable thrown = assertThrows(RmException.class, () -> rmApplication.run(null, System.in, System.out));
        assertEquals(RM_PREFIX + ERR_NULL_ARGS, thrown.getMessage());
    }

    /**
     * Run invalid args should throw rm exception.
     *
     * @throws InvalidArgsException the invalid args exception
     */
    @Test
    void run_InvalidArgs_ShouldThrowRmException() throws InvalidArgsException {
        String[] args = {"-f"};
        doThrow(new InvalidArgsException(ILLEGAL_FLAG_MSG + args[0])).when(mockedParser).parse(isA(String.class));
        Throwable thrown = assertThrows(RmException.class, () -> rmApplication.run(args, System.in, System.out));
        assertEquals(RM_PREFIX + ILLEGAL_FLAG_MSG + args[0], thrown.getMessage());
    }

    /**
     * Run invalid file should fail to std out.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     */
    @Test
    void run_InvalidFile_ShouldFailToStdOut() throws AbstractApplicationException, IOException {
        OutputStream outputStream = new ByteArrayOutputStream();
        String filename = "notAFile.dir";
        String[] args = {filename};
        when(mockedParser.getFileNames()).thenReturn(List.of(args));
        rmApplication.run(args, System.in, outputStream);
        assertEquals(formatExceptionMessage(filename, ERR_FILE_NOT_FND) + System.lineSeparator(), outputStream.toString());
        outputStream.close();
    }

    /**
     * Run invalid directory should fail to std out.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     */
    @Test
    void run_InvalidDirectory_ShouldFailToStdOut() throws AbstractApplicationException, IOException {
        OutputStream outputStream = new ByteArrayOutputStream();
        String dirName = "resources";
        String[] args = {dirName};
        when(mockedParser.getFileNames()).thenReturn(List.of(args));
        rmApplication.run(args, System.in, outputStream);
        assertEquals(formatExceptionMessage(dirName, ERR_FILE_NOT_FND) + System.lineSeparator(), outputStream.toString());
        outputStream.close();
    }


    /**
     * Run valid file should delete file.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     */
    @Test
    void run_ValidFile_ShouldDeleteFile() throws AbstractApplicationException, IOException {
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] args = {FILE_NAME};
        when(mockedParser.getFileNames()).thenReturn(List.of(args));
        assertTrue(Files.exists(FILE_PATH));
        rmApplication.run(args, System.in, outputStream);
        assertEquals(System.lineSeparator(), outputStream.toString());
        assertFalse(Files.exists(FILE_PATH));
        outputStream.close();
    }

    /**
     * Run cannot write to stdout should throw RmException.
     *
     * @throws IOException                  the io exception
     */
    @Test
    @SuppressWarnings("PMD.CloseResource")
    void run_ValidFileButIoException_ShouldThrowException() throws IOException {
        OutputStream outputStream = mock(OutputStream.class);
        doThrow(new IOException()).when(outputStream).write(any());
        String[] args = {FILE_NAME};
        when(mockedParser.getFileNames()).thenReturn(List.of(args));
        assertTrue(Files.exists(FILE_PATH));
        Throwable thrown = assertThrows(RmException.class, () -> rmApplication.run(args, System.in, outputStream));
        assertEquals(RM_PREFIX + ERR_WRITE_STREAM, thrown.getMessage());
    }

    /**
     * Run empty directory with no flag should fail to std out.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     */
    @Test
    void run_EmptyDirectoryWithNoFlag_ShouldFailToStdOut() throws AbstractApplicationException, IOException {
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] args = {EMPTY_DIR_NAME};
        when(mockedParser.getFileNames()).thenReturn(List.of(args));
        assertTrue(Files.exists(EMPTY_DIR_PATH));
        rmApplication.run(args, System.in, outputStream);
        assertEquals(formatExceptionMessage(EMPTY_DIR_NAME, ERR_IS_DIR) + System.lineSeparator(), outputStream.toString());
        assertTrue(Files.exists(EMPTY_DIR_PATH));
        outputStream.close();
    }

    /**
     * Run empty directory with empty flag should delete directory.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     */
    @Test
    void run_EmptyDirectoryWithEmptyFlag_ShouldDeleteDirectory() throws AbstractApplicationException, IOException {
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] args = {EMPTY_FOLDER_FLAG, EMPTY_DIR_NAME};
        when(mockedParser.getFileNames()).thenReturn(List.of(args[1]));
        when(mockedParser.isEmptyFolder()).thenReturn(true);
        assertTrue(Files.exists(EMPTY_DIR_PATH));
        rmApplication.run(args, System.in, outputStream);
        assertEquals(System.lineSeparator(), outputStream.toString());
        assertFalse(Files.exists(EMPTY_DIR_PATH));
        outputStream.close();
    }

    /**
     * Run empty directory with recursive flag should delete directory.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     */
    @Test
    void run_EmptyDirectoryWithRecursiveFlag_ShouldDeleteDirectory() throws AbstractApplicationException, IOException {
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] args = {RECURSIVE_FLAG, EMPTY_DIR_NAME};
        when(mockedParser.getFileNames()).thenReturn(List.of(args[1]));
        when(mockedParser.isRecursive()).thenReturn(true);
        assertTrue(Files.exists(EMPTY_DIR_PATH));
        rmApplication.run(args, System.in, outputStream);
        assertEquals(System.lineSeparator(), outputStream.toString());
        assertFalse(Files.exists(EMPTY_DIR_PATH));
        outputStream.close();
    }

    /**
     * Run file directory with no flag should throw rm exception.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     */
    @Test
    void run_FileDirectoryWithNoFlag_ShouldThrowRmException() throws AbstractApplicationException, IOException {
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] args = {DIR_NAME};
        when(mockedParser.getFileNames()).thenReturn(List.of(args));
        assertTrue(Files.exists(DIR_PATH));
        rmApplication.run(args, System.in, outputStream);
        assertEquals(formatExceptionMessage(DIR_NAME, ERR_IS_DIR) + System.lineSeparator(), outputStream.toString());
        assertTrue(Files.exists(DIR_PATH));
        outputStream.close();
    }

    /**
     * Run file directory with empty flag should throw rm exception.
     *
     * @throws IOException                  the io exception
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void run_FileDirectoryWithEmptyFlag_ShouldThrowRmException() throws IOException, AbstractApplicationException {
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] args = {EMPTY_FOLDER_FLAG, DIR_NAME};
        when(mockedParser.getFileNames()).thenReturn(List.of(args[1]));
        when(mockedParser.isEmptyFolder()).thenReturn(true);
        assertTrue(Files.exists(DIR_PATH));
        rmApplication.run(args, System.in, outputStream);
        assertEquals(formatExceptionMessage(DIR_NAME, ERR_DIR_NOT_EMPTY) + System.lineSeparator(), outputStream.toString());
        assertTrue(Files.exists(DIR_PATH));
        outputStream.close();
    }

    /**
     * Run file directory with recursive flag should delete directory.
     *
     * @throws IOException                  the io exception
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void run_FileDirectoryWithRecursiveFlag_ShouldDeleteDirectory() throws IOException, AbstractApplicationException {
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] args = {RECURSIVE_FLAG, DIR_NAME};
        when(mockedParser.getFileNames()).thenReturn(List.of(args[1]));
        when(mockedParser.isRecursive()).thenReturn(true);
        assertTrue(Files.exists(DIR_PATH));
        assertTrue(Files.exists(DIR_FILE_PATH));
        rmApplication.run(args, System.in, outputStream);
        assertEquals(System.lineSeparator(), outputStream.toString());
        assertFalse(Files.exists(DIR_PATH));
        assertFalse(Files.exists(DIR_FILE_PATH));
        outputStream.close();
    }

}
