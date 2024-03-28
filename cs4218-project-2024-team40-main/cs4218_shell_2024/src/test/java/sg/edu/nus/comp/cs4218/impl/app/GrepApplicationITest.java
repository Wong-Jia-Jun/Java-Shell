package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.*;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.impl.parser.GrepArgsParser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GrepApplicationITest {
    public static final String SUFFIX_TXT = ".txt";
    public static final String GREP = "Grep";
    private static Path testDirectory;
    private GrepApplication grepApplication;
    private ByteArrayOutputStream outputStream;

    @BeforeAll
    static void setupAll() throws Exception {
        testDirectory = Files.createTempDirectory(Paths.get(System.getProperty("java.io.tmpdir")), "grepTest");
    }

    @BeforeEach
    void setUp() {
        GrepArgsParser grepArgsParser = new GrepArgsParser();
        grepApplication = new GrepApplication(grepArgsParser);
        outputStream = new ByteArrayOutputStream();
    }

    @AfterEach
    void tearDown() throws Exception {
        outputStream.close();
    }

    @AfterAll
    static void cleanUpAll() throws Exception {
        Files.walk(testDirectory)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    /**
     * Test that grep matches a simple pattern in a single file.
     * @throws Exception if an error occurs during testing
     */
    @Test
    void run_SimplePatternInSingleFile_Matches() throws Exception {
        Path testFile = Files.createTempFile(testDirectory, "testFile", SUFFIX_TXT);
        String content = "Java\nGrep\nTest\n";
        Files.writeString(testFile, content);

        String[] args = {GREP, testFile.toString()};
        InputStream stdin = new ByteArrayInputStream(new byte[0]);
        grepApplication.run(args, stdin, outputStream);

        String output = outputStream.toString();
        assertTrue(output.contains(GREP));
        assertFalse(output.contains("Java"));
        assertFalse(output.contains("Test"));

        Files.deleteIfExists(testFile);
    }

    /**
     * Test that grep performs case insensitive matching.
     * @throws Exception if an error occurs during testing
     */
    @Test
    void run_CaseInsensitiveMatching_Performs() throws Exception {
        Path testFile = Files.createTempFile(testDirectory, "caseTest", SUFFIX_TXT);
        Files.writeString(testFile, "Java\nGrep\nTest\n");

        String[] args = {"-i", "grep", testFile.toString()};
        InputStream stdin = new ByteArrayInputStream(new byte[0]);
        grepApplication.run(args, stdin, outputStream);

        String output = outputStream.toString();
        assertTrue(output.contains(GREP));

        Files.deleteIfExists(testFile);
    }

    /**
     * Test that grep counts the number of matching lines.
     * @throws Exception if an error occurs during testing
     */

    @Test
    void run_CountMatchingLines_Counts() throws Exception {
        Path testFile = Files.createTempFile(testDirectory, "countTest", SUFFIX_TXT);
        Files.writeString(testFile, "Java\nGrep\nTest\nGrep\n");

        String[] args = {"-c", GREP, testFile.toString()};
        InputStream stdin = new ByteArrayInputStream(new byte[0]);
        grepApplication.run(args, stdin, outputStream);

        String output = outputStream.toString().trim();
        assertEquals("2", output);

        Files.deleteIfExists(testFile);
    }

    /**
     * Test that grep prefixes output lines with the file name when searching multiple files.
     * @throws Exception if an error occurs during testing
     */
    @Test
    void run_PrefixesOutputWithFileName_WhenSearchingMultipleFiles() throws Exception {
        Path testFile1 = Files.createTempFile(testDirectory, "multiFile1", SUFFIX_TXT);
        Path testFile2 = Files.createTempFile(testDirectory, "multiFile2", SUFFIX_TXT);
        Files.writeString(testFile1, "Match here\nNo match\n");
        Files.writeString(testFile2, "Another match\n");

        String[] args = {"-H", "Match", testFile1.toString(), testFile2.toString()};
        InputStream stdin = new ByteArrayInputStream(new byte[0]);
        grepApplication.run(args, stdin, outputStream);

        String output = outputStream.toString();
        assertTrue(output.contains(testFile1.getFileName().toString() + ":Match here"));
        Files.deleteIfExists(testFile1);
        Files.deleteIfExists(testFile2);
    }

    /**
     * Test that grep correctly matches complex patterns involving special characters.
     * @throws Exception if an error occurs during testing
     */
    @Test
    void run_ComplexPatternWithSpecialCharacters_MatchesExpected() throws Exception {
        Path testFile = Files.createTempFile(testDirectory, "complexPattern", SUFFIX_TXT);
        Files.writeString(testFile, "Line 1: Value=100\nLine 2: Value=200\n");

        String[] args = {"Value=100", testFile.toString()};
        InputStream stdin = new ByteArrayInputStream(new byte[0]);
        grepApplication.run(args, stdin, outputStream);

        String output = outputStream.toString();
        assertTrue(output.contains("Value=100"));
        assertFalse(output.contains("Value=200"));

        Files.deleteIfExists(testFile);
    }

    /**
     * Test that grep can match patterns from stdin.
     * @throws Exception if an error occurs during testing
     */
    @Test
    void run_StdinInput_MatchesExpectedPattern() throws Exception {
        String inputData = "Stdin line 1\nMatching line from stdin\n";
        InputStream stdin = new ByteArrayInputStream(inputData.getBytes());
        String[] args = {"Matching"};

        grepApplication.run(args, stdin, outputStream);

        String output = outputStream.toString();
        assertTrue(output.contains("Matching line from stdin"));
        assertFalse(output.contains("Stdin line 1"));
    }

    /**
     * Test that grep throws an exception for invalid regular expression patterns.
     * @throws Exception if an error occurs during testing
     */
    @Test
    void run_InvalidPatternSyntax_ThrowsPatternSyntaxException() throws Exception {
        Path testFile = Files.createTempFile(testDirectory, "invalidPattern", SUFFIX_TXT);
        Files.writeString(testFile, "Sample text\nAnother line\n");
        InputStream stdin = new ByteArrayInputStream(new byte[0]);

        String[] args = {"[Invalid[", testFile.toString()};
        Exception exception = assertThrows(AbstractApplicationException.class, () -> {
            grepApplication.run(args, stdin, outputStream);
        });

        assertTrue(exception.getMessage().contains("Unclosed"));
        Files.deleteIfExists(testFile);
    }
}