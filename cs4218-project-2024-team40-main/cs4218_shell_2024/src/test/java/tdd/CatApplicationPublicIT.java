package tdd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.CHAR_FILE_SEP;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;

import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CatException;
import sg.edu.nus.comp.cs4218.impl.app.CatApplication;
import sg.edu.nus.comp.cs4218.impl.parser.CatArgsParser;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;

public class CatApplicationPublicIT { //NOPMD
    private static final String TEMP = "temp-cat";
    private static final String DIR = "dir";
    public static final String ERR_IS_DIR = String.format("cat: %s: This is a directory", Paths.get(TEMP, DIR));
    private static final String TEXT_ONE = "Test line 1" + STRING_NEWLINE + "Test line 2" + STRING_NEWLINE +
            "Test line 3";
    private static final String TEXT_ONE_LINE = "     1  Test line 1" + STRING_NEWLINE +
            "     2  Test line 2" + STRING_NEWLINE +
            "     3  Test line 3";
    private static final String TEXT_1 = "Test line 1.1" + STRING_NEWLINE + "Test line 1.2" + STRING_NEWLINE + "Test line 1.3";
    private static final String TEXT_2 = "Test line 2.1" + STRING_NEWLINE + "Test line 2.2";
    private static final String TEXT_1_2 = "Test line 1.1" + STRING_NEWLINE + "Test line 1.2" + STRING_NEWLINE +
            "Test line 1.3" + STRING_NEWLINE + "Test line 2.1" + STRING_NEWLINE + "Test line 2.2";
    private static final String TEXT_1_2_LINE = "     1  Test line 1.1" + STRING_NEWLINE +
            "     2  Test line 1.2" + STRING_NEWLINE +
            "     3  Test line 1.3" + STRING_NEWLINE +
            "     1  Test line 2.1" + STRING_NEWLINE +
            "     2  Test line 2.2";
    private static final String FILE_A = "fileA.txt";
    private static final String FILE_B = "fileB.txt";
    private static final String FILE_C = "fileC.txt";
    private static final String FILE_D = "fileD.txt";
    private static final String FILE_E = "fileE.txt";
    private static final String FILE_F = "fileF.txt";
    private static final String FILE_G = "fileG.txt";
    private static final String FILE_H = "fileH.txt";
    private static final String FILE_I = "fileI.txt";
    private static final String FILE_J = "fileJ.txt";
    private static final String FILE_K = "fileK.txt";
    private static final String FILE_L = "fileL.txt";
    private static final String FILE_M = "fileM.txt";
    private static final String FILE_N = "fileN.txt";
    private static final String FILE_O = "fileO.txt";
    private static final String NON_EXST_FILE = "nonexistent_file.txt";
    private static final Deque<Path> FILES = new ArrayDeque<>();
    public static final String ERR_NO_SUCH_FILE = "cat: %s: No such file or directory";
    private static Path tempPath;
    private static Path dirPath;

    private CatApplication catApplication;

    @BeforeEach
    void setUp() {
        catApplication = new CatApplication(new CatArgsParser());
    }

    @BeforeAll
    static void createTemp() throws IOException, NoSuchFieldException, IllegalAccessException {
        String initialDir = TestEnvironmentUtil.getCurrentDirectory();
        tempPath = Paths.get(initialDir, TEMP);
        dirPath = Paths.get(TestEnvironmentUtil.getCurrentDirectory(), TEMP + CHAR_FILE_SEP + DIR);
        Files.createDirectory(tempPath);
        Files.createDirectory(dirPath);
    }

    @AfterAll
    static void deleteFiles() throws IOException {
        for (Path file : FILES) {
            Files.deleteIfExists(file);
        }
        Files.deleteIfExists(dirPath);
        Files.delete(tempPath);
    }

    private void createFile(String name, String text) throws IOException {
        Path path = tempPath.resolve(name);
        Files.createFile(path);
        Files.write(path, text.getBytes(StandardCharsets.UTF_8));
        FILES.push(path);
    }

    private String[] toArgs(String flag, String... files) {
        List<String> args = new ArrayList<>();
        if (!flag.isEmpty()) {
            args.add("-" + flag);
        }
        for (String file : files) {
            if ("-".equals(file)) {
                args.add(file);
            } else {
                args.add(Paths.get(TEMP, file).toString());
            }
        }
        return args.toArray(new String[0]);
    }

    //catStdin cases
    @Test
    void run_SingleStdinNoFlag_DisplaysStdinContents() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream inputStream = new ByteArrayInputStream(TEXT_ONE.getBytes(StandardCharsets.UTF_8));
        catApplication.run(toArgs(""), inputStream, output);
        assertEquals((TEXT_ONE + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleStdinFlag_DisplaysNumberedStdinContents() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String expectedText = TEXT_ONE_LINE;
        InputStream inputStream = new ByteArrayInputStream(TEXT_ONE.getBytes(StandardCharsets.UTF_8));
        catApplication.run(toArgs("n"), inputStream, output);
        assertEquals((expectedText + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleStdinDashNoFlag_DisplaysStdinContents() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream inputStream = new ByteArrayInputStream(TEXT_ONE.getBytes(StandardCharsets.UTF_8));
        catApplication.run(toArgs("", "-"), inputStream, output);
        assertEquals((TEXT_ONE + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleStdinDashFlag_DisplaysNumberedStdinContents() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String expectedText = TEXT_ONE_LINE;
        InputStream inputStream = new ByteArrayInputStream(TEXT_ONE.getBytes(StandardCharsets.UTF_8));
        catApplication.run(toArgs("n", "-"), inputStream, output);
        assertEquals((expectedText + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleEmptyStdinNoFlag_DisplaysEmpty() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String text = "";
        InputStream inputStream = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
        catApplication.run(toArgs(""), inputStream, output);
        assertEquals((text), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleEmptyStdinFlag_DisplaysEmpty() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String text = "";
        InputStream inputStream = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
        catApplication.run(toArgs("n"), inputStream, output);
        assertEquals((text), output.toString(StandardCharsets.UTF_8));
    }

    //catFiles cases
    @Test
    void run_NonexistentFileNoFlag_DisplaysErrMsg() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String nonExistFile = NON_EXST_FILE;
        catApplication.run(toArgs("", nonExistFile), System.in, output);
        assertEquals(String.format(ERR_NO_SUCH_FILE, Paths.get(TEMP, nonExistFile)) + STRING_NEWLINE,
                output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_DirectoryNoFlag_DisplaysErrMsg() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        catApplication.run(toArgs("", DIR), System.in, output);
        assertEquals(ERR_IS_DIR + STRING_NEWLINE,
                output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleFileNoFlag_DisplaysFileContents() throws AbstractApplicationException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = FILE_A;
        String text = TEXT_ONE;
        createFile(fileName, text);
        catApplication.run(toArgs("", fileName), System.in, output);
        assertEquals((text + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleFileFlag_DisplaysNumberedFileContents() throws AbstractApplicationException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = FILE_B;
        String expectedText = TEXT_ONE_LINE;
        createFile(fileName, TEXT_ONE);
        catApplication.run(toArgs("n", fileName), System.in, output);
        assertEquals((expectedText + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleEmptyFileNoFlag_DisplaysEmpty() throws AbstractApplicationException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = FILE_C;
        String text = "";
        createFile(fileName, text);
        catApplication.run(toArgs("", fileName), System.in, output);
        assertEquals((text), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleEmptyFileFlag_DisplaysEmpty() throws AbstractApplicationException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = FILE_D;
        String text = "";
        createFile(fileName, text);
        catApplication.run(toArgs("n", fileName), System.in, output);
        assertEquals((text), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleFileUnknownFlag_ThrowsException() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = FILE_E;
        createFile(fileName, TEXT_ONE);
        assertThrows(CatException.class, () -> catApplication.run(toArgs("a", fileName), System.in, output));
    }

    @Test
    void run_MultipleFilesNoFlag_DisplaysCatFileContents() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName1 = FILE_F;
        String fileName2 = FILE_G;
        String text1 = TEXT_1;
        String text2 = TEXT_2;
        String expectedText = TEXT_1_2;
        createFile(fileName1, text1);
        createFile(fileName2, text2);
        catApplication.run(toArgs("", fileName1, fileName2), System.in, output);
        assertEquals((expectedText + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_MultipleFilesFlag_DisplaysNumberedCatFileContents() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName1 = FILE_H;
        String fileName2 = FILE_I;
        String text1 = TEXT_1;
        String text2 = TEXT_2;
        String expectedText = TEXT_1_2_LINE;
        createFile(fileName1, text1);
        createFile(fileName2, text2);
        catApplication.run(toArgs("n", fileName1, fileName2), System.in, output);
        assertEquals((expectedText + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_MultipleEmptyFilesNoFlag_DisplaysEmpty() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName1 = FILE_J;
        String fileName2 = FILE_K;
        String text = "";
        createFile(fileName1, text);
        createFile(fileName2, text);
        catApplication.run(toArgs("", fileName1, fileName2), System.in, output);
        assertEquals((text), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_MultipleEmptyFilesFlag_DisplaysEmpty() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName1 = FILE_L;
        String fileName2 = FILE_M;
        String text = "";
        createFile(fileName1, text);
        createFile(fileName2, text);
        catApplication.run(toArgs("n", fileName1, fileName2), System.in, output);
        assertEquals((text), output.toString(StandardCharsets.UTF_8));
    }

    //catFilesAndStdin cases
    @Test
    void run_SingleStdinNonexistentFileNoFlag_DisplaysErrMsg() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream inputStream = new ByteArrayInputStream(TEXT_ONE.getBytes(StandardCharsets.UTF_8));
        String nonExistFile = NON_EXST_FILE;
        catApplication.run(toArgs("", nonExistFile), inputStream, output);
        assertEquals(String.format(ERR_NO_SUCH_FILE, Paths.get(TEMP, nonExistFile)) + STRING_NEWLINE,
                output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleStdinDirectoryNoFlag_ThrowsException() throws Exception  {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream inputStream = new ByteArrayInputStream(TEXT_ONE.getBytes(StandardCharsets.UTF_8));
        catApplication.run(toArgs("", DIR), inputStream, output);
        assertEquals(ERR_IS_DIR + STRING_NEWLINE, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleStdinDashSingleFileNoFlag_DisplaysCatStdinFileContents() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String stdinText = TEXT_1;
        InputStream inputStream = new ByteArrayInputStream(stdinText.getBytes(StandardCharsets.UTF_8));
        String fileName = FILE_N;
        String fileText = TEXT_2;
        createFile(fileName, fileText);
        String expectedText = TEXT_1_2;
        catApplication.run(toArgs("", "-", fileName), inputStream, output);
        assertEquals((expectedText + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleFileSingleStdinDashNoFlag_DisplaysCatFileStdinContents() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileText = TEXT_1;
        String fileName = FILE_O;
        createFile(fileName, fileText);
        String stdinText = TEXT_2;
        InputStream inputStream = new ByteArrayInputStream(stdinText.getBytes(StandardCharsets.UTF_8));
        String expectedText = TEXT_1_2;
        catApplication.run(toArgs("", fileName, "-"), inputStream, output);
        assertEquals((expectedText + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }
}

