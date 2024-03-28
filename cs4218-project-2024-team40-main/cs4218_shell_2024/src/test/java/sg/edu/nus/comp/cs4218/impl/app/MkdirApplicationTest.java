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
import java.util.ArrayList;
import java.util.List;


import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


/**
 * Tests Functionality of mkdir application
 */
public class MkdirApplicationTest {
    /**
     * The Mkdir args parser.
     */
    MkdirArgsParser mkdirArgsParser;
    /**
     * The Mkdir application.
     */
    MkdirApplication mkdirApplication;

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
     * Setup each.
     */
    @BeforeEach
    public void setupEach(){
        this.mkdirArgsParser = mock(MkdirArgsParser.class);
        this.mkdirApplication = new MkdirApplication(mkdirArgsParser);

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
     * Create folder non existent single dir single dir created.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
//CreateFolder tests
    @Test
    public void createFolder_NonExistentSingleDir_SingleDirCreated() throws AbstractApplicationException{
        Path folderPath = testPath.resolve(SINGLE_DIRECTORY);
        mkdirApplication.createFolder(folderPath.toString());
        assertTrue(folderPath.toFile().isDirectory());


    }

    /**
     * Create folder dir alr exists throw exception.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    public void createFolder_DirAlrExists_ThrowException() throws AbstractApplicationException{
        Path folderPath = testPath.resolve(SINGLE_DIRECTORY);
        mkdirApplication.createFolder(folderPath.toString());
        assertTrue(folderPath.toFile().isDirectory());
        Throwable exp = assertThrows(MkdirException.class, () -> mkdirApplication.createFolder(folderPath.toString()));
        assertEquals(new MkdirException("cannot create directory '" + mkdirApplication.getFilename() +  "': " + ERR_FILE_EXISTS).getMessage(),
                exp.getMessage());
    }

    /**
     * Create folder multiple single dir multiple dir created.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    public void createFolder_MultipleSingleDir_MultipleDirCreated() throws AbstractApplicationException{
        Path folderPath1 = testPath.resolve(SINGLE_DIRECTORY);
        Path folderPath2 = testPath.resolve(SINGLE_DIRECTORY2);
        mkdirApplication.createFolder(folderPath1.toString(),folderPath2.toString());
        assertTrue(folderPath1.toFile().isDirectory());
        assertTrue(folderPath2.toFile().isDirectory());

    }

    /**
     * Create folder multi level dir parent exists multi lvl dir created.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    public void createFolder_MultiLevelDirParentExists_MultiLvlDirCreated() throws AbstractApplicationException{
        Path folderPath1 = testPath.resolve(SINGLE_DIRECTORY);
        Path folderPath2 = testPath.resolve(SINGLE_PARENT_DIR);

        mkdirApplication.createFolder(folderPath1.toString(), folderPath2.toString());
        assertTrue(folderPath1.toFile().isDirectory());
        assertTrue(folderPath2.toFile().isDirectory());


    }

    /**
     * Create folder multi level dir parent does not exists throw exception.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    public void createFolder_MultiLevelDirParentDoesNotExists_ThrowException() throws AbstractApplicationException{;
        Path folderPath = testPath.resolve(SINGLE_PARENT_DIR);
        Throwable exp = assertThrows(MkdirException.class, () -> mkdirApplication.createFolder(folderPath.toString()));
        // different from run exception msg since will nto be able to create file here
        assertEquals(MKDIR_PREFIX + "cannot create directory " + "'null': " + ERR_FILE_NOT_FND, exp.getMessage());


    }


    /**
     * Run non existent single dir create dir.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws InvalidArgsException         the invalid args exception
     */
//Mkdir run tests
    @Test
    public void run_NonExistentSingleDir_CreateDir() throws AbstractApplicationException, InvalidArgsException{
        String path = testPath.toString() + File.separator + SINGLE_DIRECTORY;
        List<String> files = new ArrayList<>();
        files.add(path);
        doNothing().when(mkdirArgsParser).parse(any(String[].class));
        when(mkdirArgsParser.isCreateMissingParent()).thenReturn(false);
        when(mkdirArgsParser.getFileNames()).thenReturn(files);
        mkdirApplication.run(new String[] {path}, System.in, System.out );
        File expectedFile = new File(path);
        assertTrue(expectedFile.isDirectory());
    }


    /**
     * Run multiple non existent single dir create dirs.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws InvalidArgsException         the invalid args exception
     */
    @Test
    public void run_MultipleNonExistentSingleDir_CreateDirs() throws AbstractApplicationException, InvalidArgsException{
        String path1 = testPath.toString() + File.separator + SINGLE_DIRECTORY;
        String path2 = testPath.toString() + File.separator + SINGLE_DIRECTORY2;
        List<String> files = new ArrayList<>();
        files.add(path1);
        files.add(path2);
        when(mkdirArgsParser.isCreateMissingParent()).thenReturn(false);
        when(mkdirArgsParser.getFileNames()).thenReturn(files);
        mkdirApplication.run(new String[] {path1, path2}, System.in, System.out );
        File expectedFile1 = new File(path1);
        File expectedFile2 = new File(path2);
        assertTrue(expectedFile1.isDirectory());
        assertTrue(expectedFile2.isDirectory());
    }

    /**
     * Run existent duplicate single dir throw exception.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     * @throws InvalidArgsException         the invalid args exception
     */
    @Test
    public void run_ExistentDuplicateSingleDir_ThrowException() throws AbstractApplicationException, IOException, InvalidArgsException{
        String path = testPath.toString() + File.separator + SINGLE_DIRECTORY;
        List<String> files = new ArrayList<>();
        files.add(path);
        File existentFile = new File(path);
        existentFile.createNewFile();
        when(mkdirArgsParser.isCreateMissingParent()).thenReturn(false);
        when(mkdirArgsParser.getFileNames()).thenReturn(files);
        Throwable exp = assertThrows(MkdirException.class, () ->  mkdirApplication.run(new String[] {path}, System.in, System.out));
        assertEquals(MKDIR_PREFIX + "cannot create directory '" + path + "': " + ERR_FILE_EXISTS, exp.getMessage());

    }

    /**
     * Run multi level dir parent exists multi lvl dir created.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws InvalidArgsException         the invalid args exception
     */
    @Test
    public void run_MultiLevelDirParentExists_MultiLvlDirCreated() throws AbstractApplicationException, InvalidArgsException{
        String folderPath1 = testPath.toString() + File.separator + SINGLE_DIRECTORY;
        String folderPath2 = testPath.toString() + File.separator + SINGLE_DIRECTORY2;
        List<String> files = new ArrayList<>();
        files.add(folderPath1);
        files.add(folderPath2);
        when(mkdirArgsParser.isCreateMissingParent()).thenReturn(false);
        when(mkdirArgsParser.getFileNames()).thenReturn(files);
        mkdirApplication.run(new String[] {folderPath1, folderPath2}, System.in, System.out );
        File expectedFile1 = new File(folderPath1);
        File expectedFile2 = new File(folderPath2);
        assertTrue(expectedFile1.isDirectory());
        assertTrue(expectedFile2.isDirectory());

    }

    /**
     * Run multi level dir parent doesnt exists no flag throw exception.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws InvalidArgsException         the invalid args exception
     */
    @Test
    public void run_MultiLevelDirParentDoesntExistsNoFlag_ThrowException() throws AbstractApplicationException, InvalidArgsException{
        String folderPath1 = testPath.toString() + File.separator + SINGLE_DIRECTORY;
        String folderPath2 = testPath.toString() + File.separator + SINGLE_PARENT_DIR;
        List<String> files = new ArrayList<>();
        files.add(folderPath2);
        when(mkdirArgsParser.isCreateMissingParent()).thenReturn(false);
        when(mkdirArgsParser.getFileNames()).thenReturn(files);
        File expectedFile1 = new File(folderPath1);
        Throwable exp = assertThrows(MkdirException.class, () -> mkdirApplication.run(new String[] {folderPath2}, System.in, System.out ));
        assertTrue(!expectedFile1.isDirectory());
        assertEquals((MKDIR_PREFIX + "cannot create directory '" + folderPath2 + "': No such file or directory"), exp.getMessage());



    }

    /**
     * Run multi level dir parent doesnt exists with flag create dirs.
     *
     * @throws AbstractApplicationException the abstract application exception
     * @throws InvalidArgsException         the invalid args exception
     */
    @Test
    public void run_MultiLevelDirParentDoesntExistsWithFlag_CreateDirs() throws AbstractApplicationException, InvalidArgsException{
        String folderPath1 = testPath.toString() + File.separator + SINGLE_DIRECTORY;
        String folderPath2 = testPath.toString() + File.separator + SINGLE_PARENT_DIR;
        String flag = "-p";
        File expectedFile1 = new File(folderPath1);
        File expectedFile2 = new File(folderPath2);
        List<String> files = new ArrayList<>();
        files.add(folderPath2);
        when(mkdirArgsParser.isCreateMissingParent()).thenReturn(true);
        when(mkdirArgsParser.getFileNames()).thenReturn(files);
        mkdirApplication.run(new String[] {flag,folderPath2}, System.in, System.out );
        assertTrue(expectedFile1.isDirectory());
        assertTrue(expectedFile2.isDirectory());

    }

    /**
     * Run no files throw exception.
     */
    @Test
    public void run_NoFiles_ThrowException() {
        List<String> files = new ArrayList<>();
        when(mkdirArgsParser.isCreateMissingParent()).thenReturn(false);
        when(mkdirArgsParser.getFileNames()).thenReturn(files);
        Throwable exp = assertThrows(MkdirException.class, () ->  mkdirApplication.run(new String[] {}, System.in, System.out));
        assertEquals(MKDIR_PREFIX + ERR_MISSING_ARG, exp.getMessage());
    }

    /**
     * Run null arguement throw exception.
     */
    @Test
    public void run_NullArguement_ThrowException() {
        when(mkdirArgsParser.isCreateMissingParent()).thenReturn(false);
        when(mkdirArgsParser.getFileNames()).thenReturn(null);
        Throwable exp = assertThrows(MkdirException.class, () ->  mkdirApplication.run(null, System.in, System.out));
        assertEquals(MKDIR_PREFIX + ERR_MISSING_ARG, exp.getMessage());
    }


    /**
     * Run illegal flag throw exception.
     *
     * @param flag the flag
     * @throws InvalidArgsException the invalid args exception
     */
    @ParameterizedTest
    @ValueSource(strings = { "-b", "-c", "-d", "-e", "-f", "-g", "-h"})
    void run_IllegalFlag_ThrowException(String flag) throws InvalidArgsException{
        String folderPath1 = testPath.toString() + File.separator + SINGLE_DIRECTORY;
        String [] args = new String[]{flag,folderPath1};
        List<String> files = new ArrayList<>();
        files.add(folderPath1);
        doThrow(new InvalidArgsException(ILLEGAL_FLAG_MSG + flag)).when(mkdirArgsParser).parse(args);
        when(mkdirArgsParser.isCreateMissingParent()).thenReturn(false);
        when(mkdirArgsParser.getFileNames()).thenReturn(files);
        Throwable exp = assertThrows(MkdirException.class, () -> mkdirApplication.run(args, System.in, System.out));
        //since parser is mocked non illegalflags will still have "-b" so no need to replace it in expected
        assertEquals(MKDIR_PREFIX + ILLEGAL_FLAG_MSG + flag, exp.getMessage());

    }


}
