package tdd;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Deque;

import sg.edu.nus.comp.cs4218.impl.app.UniqApplication;
import sg.edu.nus.comp.cs4218.impl.parser.UniqArgsParser;
import sg.edu.nus.comp.cs4218.exception.UniqueException;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

public class UniqApplicationPublicIT { //NOPMD -suppressed ClassNamingConventions - As per Teaching Team's guidelines
    private static final String INPUT_FILE_TXT = "input_file.txt";
    private static final String OUTPUT_FILE_TXT = "output_file.txt";
    private static final String HELLO_WORD_STR = "Hello World";
    private static final String ALICE_STR = "Alice";
    private static final String BOB_STR = "Bob";
    private static final Deque<Path>FILES = new ArrayDeque<>();
    private static Path currPath;

    private UniqApplication uniqApplication;

    private static final String TEST_INPUT = HELLO_WORD_STR + STRING_NEWLINE +
            HELLO_WORD_STR + STRING_NEWLINE +
            ALICE_STR + STRING_NEWLINE +
            ALICE_STR + STRING_NEWLINE +
            BOB_STR + STRING_NEWLINE +
            ALICE_STR + STRING_NEWLINE +
            BOB_STR + STRING_NEWLINE;

    private static final String WITHOUT_F_INPUT = HELLO_WORD_STR + STRING_NEWLINE +
            ALICE_STR + STRING_NEWLINE +
            BOB_STR + STRING_NEWLINE +
            ALICE_STR + STRING_NEWLINE +
            BOB_STR + STRING_NEWLINE;

    private static final String COUNT_FLAG_OUT = "\t2 Hello World" + STRING_NEWLINE +
            "\t2 Alice" + STRING_NEWLINE +
            "\t1 Bob" + STRING_NEWLINE +
            "\t1 Alice" + STRING_NEWLINE +
            "\t1 Bob" + STRING_NEWLINE;

    private static final String DUP_FLAG_OUT = HELLO_WORD_STR + STRING_NEWLINE +
            ALICE_STR + STRING_NEWLINE;

    private static final String ALL_DUP_OUT = HELLO_WORD_STR + STRING_NEWLINE +
            HELLO_WORD_STR + STRING_NEWLINE +
            ALICE_STR + STRING_NEWLINE +
            ALICE_STR + STRING_NEWLINE;

    private static final String COUNT_DUP_OUT = "\t2 Hello World" + STRING_NEWLINE +
            "\t2 Alice" + STRING_NEWLINE;

    @BeforeAll
    static void setUp() throws NoSuchFieldException, IllegalAccessException {
        currPath = Paths.get(TestEnvironmentUtil.getCurrentDirectory());
    }

    @BeforeEach
    void init() {
        uniqApplication = new UniqApplication(new UniqArgsParser());
    }

    @AfterEach
    void deleteTemp() throws IOException {
        for (Path file :FILES) {
            Files.deleteIfExists(file);
        }
    }

    private Path createFile(String name) throws IOException {
        Path path = currPath.resolve(name);
        Files.createFile(path);
       FILES.push(path);
        return path;
    }

    private void writeToFile(Path path, String content) throws IOException {
        Files.write(path, content.getBytes());
    }

    @Test
    void run_NoFilesWithoutFlag_ReadsFromInputAndDisplaysAdjacentLines() throws Exception {
        String[] args = {};
        InputStream stdin = new ByteArrayInputStream(TEST_INPUT.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        uniqApplication.run(args, stdin, outputStream);
        assertEquals(WITHOUT_F_INPUT, outputStream.toString());
    }

    @Test
    void run_NoFilesWithCountFlag_ReadsFromInputAndDisplaysCountOfAdjacentLines() throws Exception {
        String[] args = {"-c"};
        InputStream stdin = new ByteArrayInputStream(TEST_INPUT.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        uniqApplication.run(args, stdin, outputStream);
        assertEquals(COUNT_FLAG_OUT, outputStream.toString());
    }

    @Test
    void run_NoFilesWithDuplicateFlag_ReadsFromInputAndDisplaysRepeatedAdjacentLinesOnlyOnce() throws Exception {
        String[] args = {"-d"};
        InputStream stdin = new ByteArrayInputStream(TEST_INPUT.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        uniqApplication.run(args, stdin, outputStream);
        assertEquals(DUP_FLAG_OUT, outputStream.toString());
    }

    @Test
    void run_NoFilesWithAllDuplicateFlag_ReadsFromInputAndDisplaysRepeatedAdjacentLinesRepeatedly() throws Exception {
        String[] args = {"-D"};
        InputStream stdin = new ByteArrayInputStream(TEST_INPUT.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        uniqApplication.run(args, stdin, outputStream);
        assertEquals(ALL_DUP_OUT, outputStream.toString());
    }

    @Test
    void run_NoFilesWithDuplicateAndAllDuplicateFlags_ReadsFromInputAndDisplaysRepeatedAdjacentLinesRepeatedly() throws Exception {
        String[] args = {"-d", "-D"};
        InputStream stdin = new ByteArrayInputStream(TEST_INPUT.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        uniqApplication.run(args, stdin, outputStream);
        assertEquals(ALL_DUP_OUT, outputStream.toString());
    }

    @Test
    void run_NoFilesWithCountAndDuplicateFlags_ReadsFromInputAndDisplaysCountOfRepeatedAdjacentLinesOnlyOnce() throws Exception {
        String[] args = {"-c", "-d"};
        InputStream stdin = new ByteArrayInputStream(TEST_INPUT.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        uniqApplication.run(args, stdin, outputStream);
        assertEquals(COUNT_DUP_OUT, outputStream.toString());
    }

    @Test
    void run_NoFilesWithUnknownFlag_Throws() {
        String[] args = {"-x"};
        InputStream stdin = new ByteArrayInputStream(TEST_INPUT.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertThrows(UniqueException.class, () -> uniqApplication.run(args, stdin, outputStream));
    }

    @Test
    void run_NonemptyInputFile_ReadsFileAndDisplaysAdjacentLines() throws Exception {
        Path inputPath = createFile(INPUT_FILE_TXT);
        writeToFile(inputPath, TEST_INPUT);
        String[] args = {INPUT_FILE_TXT};
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        uniqApplication.run(args, stdin, outputStream);
        assertEquals(WITHOUT_F_INPUT, outputStream.toString());
    }

    @Test
    void run_EmptyInputFile_ReadsFileAndDisplaysNewline() throws Exception {
        createFile(INPUT_FILE_TXT);
        String[] args = {INPUT_FILE_TXT};
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        uniqApplication.run(args, stdin, outputStream);
        assertEquals(STRING_NEWLINE, outputStream.toString());
    }

    @Test
    void run_NonexistentInputFile_Throws() {
        String[] args = {"nonexistent_file.txt"};
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertThrows(UniqueException.class, () -> uniqApplication.run(args, stdin, outputStream));
    }

    @Test
    void run_InputFileToOutputFile_DisplaysNewlineAndOverwritesOutputFile() throws Exception {
        Path inputPath = createFile(INPUT_FILE_TXT);
        Path outputPath = createFile(OUTPUT_FILE_TXT);
        writeToFile(inputPath, TEST_INPUT);
        writeToFile(outputPath, "This is the output file.");
       FILES.push(outputPath);
        String[] args = {INPUT_FILE_TXT, OUTPUT_FILE_TXT};
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        uniqApplication.run(args, stdin, outputStream);
        assertEquals("", outputStream.toString());
        assertArrayEquals((WITHOUT_F_INPUT.substring(0, WITHOUT_F_INPUT.length() - STRING_NEWLINE.length())).getBytes(), Files.readAllBytes(outputPath) );
    }

    @Test
    void run_InputFileToNonexistentOutputFile_DisplaysNewlineAndCreatesOutputFile() throws Exception {
        Path inputPath = createFile(INPUT_FILE_TXT);
        writeToFile(inputPath, TEST_INPUT);
        String[] args = {INPUT_FILE_TXT, OUTPUT_FILE_TXT};
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        uniqApplication.run(args, stdin, outputStream);
        Path outputPath = currPath.resolve(OUTPUT_FILE_TXT);
       FILES.push(outputPath);
        assertEquals("", outputStream.toString());
        assertTrue(Files.exists(outputPath));
        assertArrayEquals((WITHOUT_F_INPUT.substring(0, WITHOUT_F_INPUT.length() - STRING_NEWLINE.length())).getBytes(), Files.readAllBytes(outputPath));
    }

    @Test
    void run_NonexistentInputFileToOutputFile_Throws() throws IOException {
        Path outputPath = createFile(OUTPUT_FILE_TXT);
        writeToFile(outputPath, "This is the output file.");
       FILES.push(outputPath);
        String[] args = {INPUT_FILE_TXT, OUTPUT_FILE_TXT};
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertThrows(UniqueException.class, () -> uniqApplication.run(args, stdin, outputStream));
    }

    @Test
    void run_NonexistentInputFileToNonexistentOutputFile_Throws() {
        String[] args = {INPUT_FILE_TXT, OUTPUT_FILE_TXT};
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertThrows(UniqueException.class, () -> uniqApplication.run(args, stdin, outputStream));
    }
}
