package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import sg.edu.nus.comp.cs4218.Environment;
import org.mockito.MockedStatic;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CdException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;


/**
 * Tests Functionality of cd application
 */
public class CdApplicationTest {
    private String originalDirectory;
    /**
     * The File path.
     */
    static final String FILE_PATH = "CdApplicationTest";
    /**
     * The Subdir string.
     */
    static final String SUBDIR_STRING = "subdir";
    static final String UNEXECUTABLE = "unexecutable";

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() throws IOException {
        originalDirectory = Environment.currentDirectory;
        Path cdResourcePath = Paths.get("src", "test", "resources", "app", FILE_PATH);
        Environment.currentDirectory = cdResourcePath.toAbsolutePath().toString();
        Files.createDirectory(Path.of(cdResourcePath + File.separator +  UNEXECUTABLE));
    }

    /**
     * Tear down.
     */
    @AfterEach
    void tearDown() throws IOException{
        Path cdResourcePath = Paths.get("src", "test", "resources", "app", FILE_PATH);
        Files.deleteIfExists(Path.of(cdResourcePath + File.separator + UNEXECUTABLE));
        Environment.currentDirectory = originalDirectory;
    }

    /**
     * Change to directory relative path current dir changes.
     *
     * @param fileArg the file arg
     * @throws AbstractApplicationException the abstract application exception
     */
    @ParameterizedTest
    @ValueSource(strings = {"subdir/.", "./subdir", SUBDIR_STRING, "../CdApplicationTest/subdir"})
    void changeToDirectory_RelativePath_CurrentDirChanges(String fileArg)
            throws AbstractApplicationException {
        CdApplication cdApplication = new CdApplication();
        cdApplication.changeToDirectory(fileArg);
        assert Environment.currentDirectory.endsWith(SUBDIR_STRING);
    }

    /**
     * Change to directory absolute path current dir changes.
     *
     * @param fileArg the file arg
     * @throws AbstractApplicationException the abstract application exception
     */
    @ParameterizedTest
    @ValueSource(strings = {"/subdir/.", "/./subdir", "/subdir", "/../CdApplicationTest/subdir"})
    void changeToDirectory_AbsolutePath_CurrentDirChanges(String fileArg)
            throws AbstractApplicationException {
        CdApplication cdApplication = new CdApplication();
        cdApplication.changeToDirectory(Environment.currentDirectory + fileArg);
        assert Environment.currentDirectory.endsWith(SUBDIR_STRING);
    }

    /**
     * Change to directory cd to file relative error message and current dir unchanged.
     *
     * @param fileArg the file arg
     */
    @ParameterizedTest
    @ValueSource(strings = {"CdFile.txt", "subdir/subDirFile.txt"})
    void changeToDirectory_CdToFileRelative_ErrorMessageAndCurrentDirUnchanged(String fileArg) {
        CdApplication cdApplication = new CdApplication();
        Throwable exp = assertThrows(CdException.class,
                () -> {cdApplication.changeToDirectory(fileArg);});
        assertEquals(new CdException(ERR_IS_NOT_DIR + " " + fileArg).getMessage(),
                exp.getMessage());
        assert Environment.currentDirectory.endsWith(FILE_PATH);
    }

    /**
     * Change to directory cd to file absolute error message and current dir unchanged.
     *
     * @param fileArg the file arg
     */
    @ParameterizedTest
    @ValueSource(strings = {"/CdFile.txt", "/subdir/subDirFile.txt"})
    void changeToDirectory_CdToFileAbsolute_ErrorMessageAndCurrentDirUnchanged(String fileArg) {
        CdApplication cdApplication = new CdApplication();
        Throwable exp = assertThrows(CdException.class,
                () -> {cdApplication.changeToDirectory(Environment.currentDirectory + fileArg);});
        assertEquals(new CdException(ERR_IS_NOT_DIR + " " + Environment.currentDirectory + fileArg).getMessage(),
                exp.getMessage());
        assert Environment.currentDirectory.endsWith(FILE_PATH);
    }

    /**
     * Run too many args error message and current dir unchanged.
     */
    @Test
    void run_TooManyArgs_ErrorMessageAndCurrentDirUnchanged() {
        CdApplication cdApplication = new CdApplication();
        String[] args = {"arg1", "arg2"};
        Throwable exp = assertThrows(AbstractApplicationException.class,
                () -> {cdApplication.run(args, System.in, System.out);});
        assertEquals(new CdException(ERR_TOO_MANY_ARGS).getMessage(), exp.getMessage());
        assert Environment.currentDirectory.endsWith(FILE_PATH);
    }

    /**
     * Run null args error message and current dir unchanged.
     */
    @Test
    void run_NullArgs_ErrorMessageAndCurrentDirUnchanged() {
        CdApplication cdApplication = new CdApplication();
        String[] args = null;
        Throwable exp = assertThrows(AbstractApplicationException.class,
                () -> {cdApplication.run(args, System.in, System.out);});
        assertEquals(new CdException(ERR_NULL_ARGS).getMessage(), exp.getMessage());
        assert Environment.currentDirectory.endsWith(FILE_PATH);
    }

    /**
     * Run cd to relative directory current dir changes.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void run_CdToRelativeDirectory_CurrentDirChanges()
            throws AbstractApplicationException {
        CdApplication cdApplication = new CdApplication();
        String[] args = {SUBDIR_STRING};
        cdApplication.run(args, System.in, System.out);
        assert Environment.currentDirectory.endsWith(SUBDIR_STRING);
    }

    /**
     * Run cd with no args current dir is the same.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    void run_CdIntoBlankPath_Success() throws Exception {
        CdApplication cdApplication = new CdApplication();
        String[] args = {};
        String currDirectory = Environment.currentDirectory;
        cdApplication.run(args, System.in, System.out);
        assertTrue(Environment.currentDirectory.equals(currDirectory));
    }

    /**
     * Run cd to non existent directory error message and current dir unchanged.
     */
    @Test
    void run_CdToNonExistentDirectory_ErrorMessageAndCurrentDirUnchanged() {
        CdApplication cdApplication = new CdApplication();
        String[] args = {"nonexistent"};
        Throwable exp = assertThrows(AbstractApplicationException.class,
                () -> {cdApplication.run(args, System.in, System.out);});
        assertEquals(new CdException(ERR_FILE_NOT_FND + " " + args[0]).getMessage(),
                exp.getMessage());
        assert Environment.currentDirectory.endsWith(FILE_PATH);
    }

    /**
     * Run cd with flags error message and current dir unchanged.
     *
     * @param flag the flag
     */
    @ParameterizedTest
    @ValueSource(strings = {"-c", "-l"})
    void run_CdWithFlags_ErrorMessageAndCurrentDirUnchanged(String flag) {
        CdApplication cdApplication = new CdApplication();
        String[] args = {flag, SUBDIR_STRING};
        Throwable exp = assertThrows(AbstractApplicationException.class,
                () -> {cdApplication.run(args, System.in, System.out);});
        assertEquals(new CdException(ERR_TOO_MANY_ARGS).getMessage(), exp.getMessage());
        assert Environment.currentDirectory.endsWith(FILE_PATH);
    }

    /**
     * Run null input stream error message and current dir unchanged.
     */
    @Test
    void run_NullInputStream_ErrorMessageAndCurrentDirUnchanged() {
        CdApplication cdApplication = new CdApplication();
        String[] args = {SUBDIR_STRING};
        Throwable exp = assertThrows(AbstractApplicationException.class,
                () -> {cdApplication.run(args, null, System.out);});
        assertEquals(new CdException(ERR_NULL_STREAMS).getMessage(), exp.getMessage());
        assert Environment.currentDirectory.endsWith(FILE_PATH);
    }

    /**
     * Run null output stream error message and current dir unchanged.
     */
    @Test
    void run_NullOutputStream_ErrorMessageAndCurrentDirUnchanged() {
        CdApplication cdApplication = new CdApplication();
        String[] args = {SUBDIR_STRING};
        Throwable exp = assertThrows(AbstractApplicationException.class,
                () -> {cdApplication.run(args, System.in, null);});
        assertEquals(new CdException(ERR_NULL_STREAMS).getMessage(), exp.getMessage());
        assert Environment.currentDirectory.endsWith(FILE_PATH);
    }
    /**
     * Get normalized Absoulte path of a file pathstring that is unexecutable throw exception
     */
    @Test
    void getNormalizedAbsolutePath_Unexecutable_ThrowException(){
        CdApplication cdApplication = new CdApplication();
        String args = "unexecutable";
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.exists(any())).thenReturn(true);
            mockedFiles.when(() -> Files.isDirectory(any())).thenReturn(true);
            mockedFiles.when(() -> Files.isExecutable(any())).thenReturn(false);
            Throwable exp = assertThrows(CdException.class, () -> cdApplication.getNormalizedAbsolutePath(args));
            assertEquals(new CdException(ERR_NO_PERM + " " + args).getMessage(), exp.getMessage());
        }

    }
    /**
     * Get normalized Absoulte path of a valid file
     */
    @Test
    void getNormalizedAbsolutePath_Valid_ReturnPath() throws AbstractApplicationException{
        CdApplication cdApplication = new CdApplication();
        String args = "unexecutable";
        String actual = cdApplication.getNormalizedAbsolutePath(args);
        assertEquals(Path.of(Environment.currentDirectory + File.separator + UNEXECUTABLE).toString(), actual);


    }
}
