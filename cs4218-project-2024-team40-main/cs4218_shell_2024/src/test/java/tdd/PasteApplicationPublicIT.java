package tdd;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR_FILE;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.CHAR_FILE_SEP;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

import sg.edu.nus.comp.cs4218.exception.PasteException;
import sg.edu.nus.comp.cs4218.impl.app.PasteApplication;
import sg.edu.nus.comp.cs4218.impl.parser.PasteArgsParser;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;
import sg.edu.nus.comp.cs4218.testutils.TestStringUtils;

import javax.print.DocFlavor;

public class PasteApplicationPublicIT { //NOPMD
    private static final String TEMP = "temp-paste";
    private static final String DIR = "dir";
    private static final String TEST_LINE = "Test line 1" + STRING_NEWLINE + "Test line 2" + STRING_NEWLINE + "Test line 3";
    public static final String EXPECTED_TEXT = "Test line 1\tTest line 2\tTest line 3";
    private static final String ERR_NO_SUCH_FILE = "paste: %s: No such file or directory";
    private static final String TEXT_1 = "Test line 1.1" + STRING_NEWLINE + "Test line 1.2" + STRING_NEWLINE + "Test line 1.3";
    private static final String TEXT_2 = "Test line 2.1" + STRING_NEWLINE + "Test line 2.2";
    private static final String EXPECTED_1_2 = "Test line 1.1\tTest line 2.1" + STRING_NEWLINE + "Test line 1.2\tTest line 2.2" + STRING_NEWLINE + "Test line 1.3\t";
    private static final String EXPECTED_1_2_S = "Test line 1.1\tTest line 1.2\tTest line 1.3" + STRING_NEWLINE + "Test line 2.1\tTest line 2.2";
    private static final Deque<Path> FILES = new ArrayDeque<>();
    private static Path tempPath;
    private static Path dirPath;

    private PasteApplication pasteApplication;

    @BeforeEach
    void setUp() {
        pasteApplication = new PasteApplication(new PasteArgsParser());
    }

    @BeforeAll
    static void createTemp() throws NoSuchFieldException, IllegalAccessException, IOException {
        tempPath = Paths.get(TestEnvironmentUtil.getCurrentDirectory(), TEMP);
        Files.createDirectory(tempPath);
        dirPath = Paths.get(TestEnvironmentUtil.getCurrentDirectory(), TEMP + TestStringUtils.CHAR_FILE_SEP + DIR);
        Files.createDirectory(dirPath);
    }

    @AfterAll
    static void deleteFiles() throws IOException {
        for (Path file : FILES) {
            Files.deleteIfExists(file);
        }
        Files.delete(dirPath);
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

    @Test
    void run_SingleStdinNullStdout_ThrowsException() {
        InputStream inputStream = new ByteArrayInputStream(TEST_LINE.getBytes(StandardCharsets.UTF_8));
        assertThrows(PasteException.class, () -> pasteApplication.run(toArgs(""), inputStream, null));
    }

    @Test
    void run_NullStdinNullFilesNoFlag_ThrowsException() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assertThrows(PasteException.class, () -> pasteApplication.run(toArgs(""), null, output));
    }

    @Test
    void run_NullStdinNullFilesFlag_ThrowsException() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assertThrows(PasteException.class, () -> pasteApplication.run(toArgs("n"), null, output));
    }

    //mergeStdin cases
    @Test
    void run_SingleStdinNoFlag_DisplaysStdinContents() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream inputStream = new ByteArrayInputStream(TEST_LINE.getBytes(StandardCharsets.UTF_8));
        pasteApplication.run(toArgs(""), inputStream, output);
        assertEquals((TEST_LINE + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }


    @Test
    void run_SingleStdinFlag_DisplaysNonParallelStdinContents() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream inputStream = new ByteArrayInputStream(TEST_LINE.getBytes(StandardCharsets.UTF_8));
        pasteApplication.run(toArgs("s"), inputStream, output);
        assertEquals((EXPECTED_TEXT + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleStdinDashNoFlag_DisplaysStdinContents() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream inputStream = new ByteArrayInputStream(TEST_LINE.getBytes(StandardCharsets.UTF_8));
        pasteApplication.run(toArgs("", "-"), inputStream, output);
        assertEquals((TEST_LINE + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleStdinDashFlag_DisplaysNonParallelStdinContents() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream inputStream = new ByteArrayInputStream(TEST_LINE.getBytes(StandardCharsets.UTF_8));
        pasteApplication.run(toArgs("s", "-"), inputStream, output);
        assertEquals((EXPECTED_TEXT + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleEmptyStdinNoFlag_DisplaysEmpty() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String text = "";
        InputStream inputStream = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
        pasteApplication.run(toArgs(""), inputStream, output);

        assertEquals(text, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleEmptyStdinFlag_DisplaysEmpty() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String text = "";
        InputStream inputStream = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
        pasteApplication.run(toArgs("s"), inputStream, output);
        assertEquals(text, output.toString(StandardCharsets.UTF_8));
    }

    //mergeFiles cases
    @Test
    void run_NonexistentFileNoFlag_ThrowsException() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String noExstFile = "nonexistent_file.txt";
        Throwable exception = assertThrows(PasteException.class, () -> pasteApplication.run(toArgs("", noExstFile), System.in, output));
        assertEquals(String.format(ERR_NO_SUCH_FILE, TEMP + CHAR_FILE_SEP + noExstFile), exception.getMessage());
    }

    @Test
    void run_DirectoryNoFlag_DisplaysEmpty() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Throwable exp = assertThrows(PasteException.class, () -> pasteApplication.run(toArgs("", DIR), System.in, output));
        assertEquals(new PasteException(TEMP + File.separator + DIR, ERR_IS_DIR_FILE).getMessage(), exp.getMessage());
    }

    @Test
    void run_SingleFileNoFlag_DisplaysFileContents() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileA.txt";
        String text = TEST_LINE;
        createFile(fileName, text);
        pasteApplication.run(toArgs("", fileName), System.in, output);
        assertEquals((text + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleFileFlag_DisplaysNonParallelFileContents() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileB.txt";
        createFile(fileName, TEST_LINE);
        pasteApplication.run(toArgs("s", fileName), System.in, output);
        assertEquals((EXPECTED_TEXT + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleEmptyFileNoFlag_DisplaysEmpty() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileC.txt";
        String text = "";
        createFile(fileName, text);
        pasteApplication.run(toArgs("", fileName), System.in, output);
        assertEquals(text, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleEmptyFileFlag_DisplaysEmpty() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileD.txt";
        String text = "";
        createFile(fileName, text);
        pasteApplication.run(toArgs("s", fileName), System.in, output);
        assertEquals(text, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleFileUnknownFlag_Throws() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileE.txt";
        createFile(fileName, TEST_LINE);
        assertThrows(PasteException.class, () -> pasteApplication.run(toArgs("a", fileName), System.in, output));
    }

    @Test
    void run_MultipleFilesNoFlag_DisplaysMergedFileContents() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName1 = "fileF.txt";
        String fileName2 = "fileG.txt";
        createFile(fileName1, TEXT_1);
        createFile(fileName2, TEXT_2);
        pasteApplication.run(toArgs("", fileName1, fileName2), System.in, output);
        assertEquals((EXPECTED_1_2 + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_MultipleFilesFlag_DisplaysNonParallelMergedFileContents() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName1 = "fileH.txt";
        String fileName2 = "fileI.txt";
        createFile(fileName1, TEXT_1);
        createFile(fileName2, TEXT_2);
        pasteApplication.run(toArgs("s", fileName1, fileName2), System.in, output);
        assertEquals((EXPECTED_1_2_S + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_MultipleEmptyFilesNoFlag_DisplaysEmpty() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName1 = "fileJ.txt";
        String fileName2 = "fileK.txt";
        String text = "";
        createFile(fileName1, text);
        createFile(fileName2, text);
        pasteApplication.run(toArgs("", fileName1, fileName2), System.in, output);
        assertEquals("\t" + STRING_NEWLINE, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_MultipleEmptyFilesFlag_DisplaysEmpty() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName1 = "fileL.txt";
        String fileName2 = "fileM.txt";
        String text = "";
        createFile(fileName1, text);
        createFile(fileName2, text);
        pasteApplication.run(toArgs("s", fileName1, fileName2), System.in, output);
        assertEquals(STRING_NEWLINE + STRING_NEWLINE, output.toString(StandardCharsets.UTF_8));
    }

    //mergeFilesAndStdin cases
    @Test
    void run_SingleStdinNonexistentFileNoFlag_ThrowsException() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String stdinText = TEXT_1;
        InputStream inputStream = new ByteArrayInputStream(stdinText.getBytes(StandardCharsets.UTF_8));
        String noExstFile = "nonexistent_file.txt";
        Throwable exception = assertThrows(PasteException.class, () -> pasteApplication.run(toArgs("", noExstFile), inputStream, output));
        assertEquals(String.format(ERR_NO_SUCH_FILE, TEMP + CHAR_FILE_SEP + noExstFile), exception.getMessage());
    }

    @Test
    void run_SingleStdinDirectoryNoFlag_DisplaysMergedStdinFileContents() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String stdinText = TEXT_1;
        InputStream inputStream = new ByteArrayInputStream(stdinText.getBytes(StandardCharsets.UTF_8));
        String expectedText = TEXT_1;
        pasteApplication.run(toArgs("", "-"), inputStream, output);
        assertEquals(expectedText + STRING_NEWLINE, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleStdinDashSingleFileNoFlag_DisplaysMergedStdinFileContents() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String stdinText = TEXT_1;
        InputStream inputStream = new ByteArrayInputStream(stdinText.getBytes(StandardCharsets.UTF_8));
        String fileName = "fileN.txt";
        String fileText = TEXT_2;
        createFile(fileName, fileText);
        pasteApplication.run(toArgs("", "-", fileName), inputStream, output);
        assertEquals((EXPECTED_1_2 + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleFileSingleStdinDashNoFlag_DisplaysNonParallelMergedFileStdinContents() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileText = TEXT_1;
        String fileName = "fileO.txt";
        createFile(fileName, fileText);
        String stdinText = TEXT_2;
        InputStream inputStream = new ByteArrayInputStream(stdinText.getBytes(StandardCharsets.UTF_8));
        pasteApplication.run(toArgs("", fileName, "-"), inputStream, output);
        assertEquals((EXPECTED_1_2 + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }
}
