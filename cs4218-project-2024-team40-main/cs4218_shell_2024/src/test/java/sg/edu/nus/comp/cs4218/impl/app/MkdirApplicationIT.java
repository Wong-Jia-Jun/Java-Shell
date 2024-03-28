package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.MkdirException;
import sg.edu.nus.comp.cs4218.impl.parser.MkdirArgsParser;


import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;


import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static org.junit.jupiter.api.Assertions.*;


/**
 * Integration Tests of  Functionality of mkdir application
 */
public class MkdirApplicationIT { //NOPMD - suppressed ClassNamingConventions - As per Teaching Team's guidelines
    private static final Path BASE_PATH = Paths.get(Environment.currentDirectory);
    private static final String TEST_FOLDER = "mkDirTestFolder";
    private static Path testPath;
    private static final String SINGLE_DIRECTORY = "Single";
    private static final String SINGLE_DIRECTORY2 = "Single2";
    private static final String SINGLE_PARENT_DIR = "Single" + File.separator + "Child";
    private static final String MKDIR_PREFIX = "mkdir: ";
    /**
     * The constant ILLEGAL_FLAG_MSG.
     */
    public static final String ILLEGAL_FLAG_MSG = "illegal option -- ";


    /**
     * Setup.
     */
    @BeforeAll
    static public void setup(){
        testPath = BASE_PATH.resolve(TEST_FOLDER);
        testPath.toFile().mkdir();
    }

    /**
     * Teardown.
     */
// Not sure if there is a more efficient way to do this but have to recursively delete all subdirectories deleting
    // parent file does not work
    @AfterEach
    public void teardown() {
        File[] contents = testPath.toFile().listFiles();
        if (contents != null) {
            for (File file : contents) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }

    }

    /**
     * Teardown testdir.
     */
    @AfterAll
    public static void teardownTestdir(){
        testPath.toFile().delete();
    }

    /**
     * Delete directory.
     *
     * @param directory the directory
     */
    public void deleteDirectory(File directory){
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
    /**
     * Run null stdin throw exception.
     *
     */
    @Test
    public void run_NullStdin_ThrowException() {
        String path = testPath.toString() + File.separator + SINGLE_DIRECTORY;
        MkdirApplication mkdirApplication = new MkdirApplication(new MkdirArgsParser());
        Throwable exp = assertThrows(MkdirException.class, () ->  mkdirApplication.run(new String[] {path}, null, System.out));
        assertEquals(MKDIR_PREFIX + ERR_NULL_STREAMS, exp.getMessage());

    }
    /**
     * * Run null stdout throw exception.
     */
    @Test
    public void run_NullStdout_ThrowException() throws AbstractApplicationException, IOException, InvalidArgsException {
        String path = testPath.toString() + File.separator + SINGLE_DIRECTORY;
        MkdirApplication mkdirApplication = new MkdirApplication(new MkdirArgsParser());
        Throwable exp = assertThrows(MkdirException.class, () ->  mkdirApplication.run(new String[] {path}, System.in, null));
        assertEquals(MKDIR_PREFIX + ERR_NULL_STREAMS, exp.getMessage());

    }
    /**
     * Run non existent single dir create dir.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
//Mkdir run tests
    @Test
    public void run_NonExistentSingleDIr_CreateDir() throws AbstractApplicationException{
        String path = testPath.toString() + File.separator + SINGLE_DIRECTORY;
        MkdirApplication mkdirApp = new MkdirApplication(new MkdirArgsParser());
        mkdirApp.run(new String[] {path}, System.in, System.out );
        File expectedFile = new File(path);
        assertTrue(expectedFile.isDirectory());

    }

    /**
     * Run multiple non existent single d ir create dirs.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    public void run_MultipleNonExistentSingleDIr_CreateDirs() throws AbstractApplicationException{
        String path1 = testPath.toString() + File.separator + SINGLE_DIRECTORY;
        String path2 = testPath.toString() + File.separator + SINGLE_DIRECTORY2;
        MkdirApplication mkdirApp = new MkdirApplication(new MkdirArgsParser());
        mkdirApp.run(new String[] {path1, path2}, System.in, System.out );
        File expectedFile1 = new File(path1);
        File expectedFile2 = new File(path2);
        assertTrue(expectedFile1.isDirectory());
        assertTrue(expectedFile2.isDirectory());

    }

    /**
     * Run existent duplicate single d ir throw exception.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     */
    @Test
    public void run_ExistentDuplicateSingleDIr_ThrowException() throws AbstractApplicationException, IOException {
        String path = testPath.toString() + File.separator + SINGLE_DIRECTORY;
        MkdirApplication mkdirApp = new MkdirApplication(new MkdirArgsParser());
        File existentFile = new File(path);
        existentFile.createNewFile();
        Throwable exp = assertThrows(MkdirException.class, () -> mkdirApp.run(new String[]{path}, System.in, System.out));
        assertEquals(MKDIR_PREFIX + "cannot create directory '" + path + "': " + ERR_FILE_EXISTS, exp.getMessage());
    }

    /**
     * Run multi level dir parent exists multi lvl dir created.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    public void run_MultiLevelDirParentExists_MultiLvlDirCreated() throws AbstractApplicationException{
        String folderPath1 = testPath.toString() + File.separator + SINGLE_DIRECTORY;
        String folderPath2 = testPath.toString() + File.separator + SINGLE_DIRECTORY2;
        MkdirApplication mkdirApp = new MkdirApplication(new MkdirArgsParser());
        mkdirApp.run(new String[] {folderPath1, folderPath2}, System.in, System.out );
        File expectedFile1 = new File(folderPath1);
        File expectedFile2 = new File(folderPath2);
        assertTrue(expectedFile1.isDirectory());
        assertTrue(expectedFile2.isDirectory());


    }

    /**
     * Run multi level dir parent doesnt exists no flag throw exception.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    public void run_MultiLevelDirParentDoesntExistsNoFlag_ThrowException() throws AbstractApplicationException{

        String folderPath1 = testPath.toString() + File.separator + SINGLE_DIRECTORY;
        String folderPath2 = testPath.toString() + File.separator + SINGLE_PARENT_DIR;
        MkdirApplication mkdirApp = new MkdirApplication(new MkdirArgsParser());
        File expectedFile1 = new File(folderPath1);
        Throwable exp = assertThrows(MkdirException.class, () -> mkdirApp.run(new String[] {folderPath2}, System.in, System.out ));
        assertTrue(!expectedFile1.isDirectory());
        assertEquals((MKDIR_PREFIX + "cannot create directory '" + folderPath2 + "': No such file or directory"), exp.getMessage());
    }

    /**
     * Run multi level dir parent doesnt exists with flag create dirs.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    public void run_MultiLevelDirParentDoesntExistsWithFlag_CreateDirs() throws AbstractApplicationException{
        String folderPath1 = testPath.toString() + File.separator + SINGLE_DIRECTORY;
        String folderPath2 = testPath.toString() + File.separator + SINGLE_PARENT_DIR;
        String flag = "-p";
        MkdirApplication mkdirApp = new MkdirApplication(new MkdirArgsParser());
        File expectedFile1 = new File(folderPath1);
        File expectedFile2 = new File(folderPath2);

        mkdirApp.run(new String[] {flag,folderPath2}, System.in, System.out );
        assertTrue(expectedFile1.isDirectory());
        assertTrue(expectedFile2.isDirectory());

    }

    /**
     * Run null arguement throw exception.
     */
    @Test
    public void run_NullArguement_ThrowException() {
        MkdirApplication mkdirApp = new MkdirApplication(new MkdirArgsParser());
        Throwable exp = assertThrows(MkdirException.class, () ->  mkdirApp.run(new String[] {}, System.in, System.out));
        assertEquals(MKDIR_PREFIX + ERR_MISSING_ARG, exp.getMessage());
    }

    /**
     * Run invalid flag throw exception.
     *
     * @param flag the flag
     */
    @ParameterizedTest
    @ValueSource(strings = {"-a", "-b", "-c", "-d", "-e", "-f", "-g", "-h"})
    public void run_invalidFlag_ThrowException(String flag) {
        String path = testPath.toString() + File.separator + SINGLE_DIRECTORY;
        MkdirApplication mkdirApp = new MkdirApplication(new MkdirArgsParser());
        Throwable exp = assertThrows(MkdirException.class, () ->  mkdirApp.run(new String[] {flag, path}, System.in, System.out));
        assertEquals(MKDIR_PREFIX + ILLEGAL_FLAG_MSG + flag.replace("-", "") , exp.getMessage());
    }
}
