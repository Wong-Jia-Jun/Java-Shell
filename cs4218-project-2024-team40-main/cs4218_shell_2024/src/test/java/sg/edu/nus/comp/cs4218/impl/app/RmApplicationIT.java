package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.RmException;
import sg.edu.nus.comp.cs4218.impl.parser.RmArgsParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.parser.RmArgsParser.FLAG_IS_EMPTY_DIR;
import static sg.edu.nus.comp.cs4218.impl.parser.RmArgsParser.FLAG_IS_RECURSIVE;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;


/**
 * Integration Tests of RmApplication with RmArgsParser and IOUtils
 */
public class RmApplicationIT { //NOPMD - suppressed ClassNamingConventions - As per Teaching Team's guidelines
    private RmApplication rmApplication;
    private static final String FILE_NAME = "testFile.txt";
    private static final Path FILE_PATH = Path.of(FILE_NAME);
    private static final String EMPTY_DIR_NAME = "testEmptyDir";
    private static final Path EMPTY_DIR_PATH = Path.of(EMPTY_DIR_NAME);
    private static final String DIR_NAME = "testDir";
    private static final Path DIR_PATH = Path.of(DIR_NAME);
    private static final String DIR_FILE_NAME = "innerTestFile.txt";
    private static final Path DIR_FILE_PATH = Path.of(DIR_PATH + "/" + DIR_FILE_NAME);
    private static final String RM_PREFIX = "rm: ";
    private final String RECURSIVE_FLAG = "-" + FLAG_IS_RECURSIVE;
    private final String EMPTY_FOLDER_FLAG = "-" + FLAG_IS_EMPTY_DIR;

    /**
     * Setup the test environment
     *
     * @throws IOException the io exception
     */
    @BeforeEach
    void setup() throws IOException {
        rmApplication = new RmApplication(new RmArgsParser());
        Files.createDirectory(EMPTY_DIR_PATH);
        Files.createDirectory(DIR_PATH);
        Files.createFile(DIR_FILE_PATH);
        Files.createFile(FILE_PATH);
    }

    /**
     * Teardown the test environment
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
        Throwable thrown = assertThrows(RmException.class, () -> rmApplication.remove(false, false, (String[]) null));
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
    void remove_FileDirectoryWithEmptyFlags_ShouldThrowRmException() {
        assertTrue(Files.exists(DIR_PATH));
        Throwable thrown = assertThrows(RmException.class, () -> rmApplication.remove(true, false, DIR_NAME));
        assertEquals(formatExceptionMessage(DIR_NAME, ERR_DIR_NOT_EMPTY), thrown.getMessage());
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
    void remove_FileDirectoryWithRecursiveAndEmptyFlags_ShouldDeleteDirectory() throws  RmException {
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
     * Run invalid file should fail to std out.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     */
    @ParameterizedTest
    @ValueSource(strings = {"resources", "resources:"})
    void run_InvalidFile_ShouldFailToStdOut(String str) throws AbstractApplicationException, IOException {
        OutputStream outputStream = new ByteArrayOutputStream();
        String[] args = {str};
        rmApplication.run(args, System.in, outputStream);
        assertEquals(formatExceptionMessage(str, ERR_FILE_NOT_FND) + System.lineSeparator(), outputStream.toString());
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
        assertTrue(Files.exists(FILE_PATH));
        rmApplication.run(args, System.in, outputStream);
        assertEquals(System.lineSeparator(), outputStream.toString());
        assertFalse(Files.exists(FILE_PATH));
        outputStream.close();
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
        assertTrue(Files.exists(DIR_PATH));
        assertTrue(Files.exists(DIR_FILE_PATH));
        rmApplication.run(args, System.in, outputStream);
        assertEquals(System.lineSeparator(), outputStream.toString());
        assertFalse(Files.exists(DIR_PATH));
        assertFalse(Files.exists(DIR_FILE_PATH));
        outputStream.close();
    }

}
