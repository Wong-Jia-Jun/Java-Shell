package tdd;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.MkdirException;
import sg.edu.nus.comp.cs4218.impl.app.MkdirApplication;
import sg.edu.nus.comp.cs4218.impl.parser.MkdirArgsParser;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;


public class MkdirApplicationPublicIT {//NOPMD -suppressed ClassNamingConventions - As per Teaching Team's guidelines
    MkdirApplication mkdirApplication;
    private final  static  String PATH_TEST_DIR = "TestResources" + File.separator + "mkdirTestDir" + File.separator;
    String tempDir = PATH_TEST_DIR + "mkdirTest";
    String tempDir2 = PATH_TEST_DIR + "mkdirTest2";
    String tempParent = PATH_TEST_DIR + "mkdirTestParent";
    String tempChild = tempParent + File.separator + "mkdirTestChild";
    @BeforeAll
    static void setUpAll() throws NoSuchFieldException, IllegalAccessException {
        String originalPath = TestEnvironmentUtil.getCurrentDirectory();
        String testDirPath = originalPath + File.separator + PATH_TEST_DIR;
        new File(testDirPath).mkdirs();
    }


    @BeforeEach
    void setUp() {
        mkdirApplication = new MkdirApplication(new MkdirArgsParser());
        deleteDirectory(null, new File(PATH_TEST_DIR).listFiles());
    }

    @AfterEach
    void tearDown() throws IOException {
        File file = new File(PATH_TEST_DIR + File.separator + "EmptyFileForGitTracking.txt");
        file.createNewFile();
    }
    @AfterAll
    static void tearDownAll() throws NoSuchFieldException, IllegalAccessException {
        String originalPath = TestEnvironmentUtil.getCurrentDirectory();
        String testDirPath = originalPath + File.separator + PATH_TEST_DIR;
        deleteDirectory(new File(testDirPath), new File(testDirPath).listFiles());
    }

    public static void deleteDirectory(File directory, File... files) {
        if (null != files) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i], files[i].listFiles());
                } else {
                    files[i].delete();
                }
            }
        }
        if (directory != null) {
            directory.delete();
        }
    }

    @Test
    void run_NullInput_ThrowsException() {
        assertThrows(MkdirException.class, () -> {
            mkdirApplication.run(null, System.in, System.out);
        });
    }

    @Test
    void run_EmptyInput_Success() throws Exception {
        String[] args = new String[0];
        assertThrows(MkdirException.class, () -> {
            mkdirApplication.run(args, System.in, System.out);
        });
    }


    @Test
    void run_OneNewDirectoryInput_Success() throws Exception {
        String[] args = new String[1];
        args[0] = tempDir;
        mkdirApplication.run(args, System.in, System.out);
        assertTrue(new File(tempDir).exists());
    }


    @Test
    void run_TwoNewDirectoryInput_Success() throws Exception {
        String[] args = new String[2];
        args[0] = tempDir;
        args[1] = tempDir2;
        mkdirApplication.run(args, System.in, System.out);
        assertTrue(new File(tempDir).exists());
        assertTrue(new File(tempDir2).exists());
    }

    @Test
    void run_DirectoryInDirectoryInput_Success() throws Exception {
        String[] args = new String[2];
        args[0] = tempParent;
        args[1] = tempChild;
        mkdirApplication.run(args, System.in, System.out);
        assertTrue(new File(tempChild).exists());
    }
}
