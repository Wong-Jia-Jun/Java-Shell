package ef2test;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.impl.util.RegexArgument;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for functionality of the {@link RegexArgument} class, specifically its
 * ability to expand file paths containing glob patterns.
 */
public class RegexArgumentTest {
    private RegexArgument regexArgument;
    private static final String FILE_NAME_A = "testFileA.txt";
    private static final Path FILE_PATH_A = Path.of(FILE_NAME_A);
    private static final String EMPTY_DIR_NAME = "testEmptyDir";
    private static final Path EMPTY_DIR_PATH = Path.of(EMPTY_DIR_NAME);
    private static final String FILE_NAME_B = "testFileB.txt";
    private static final Path FILE_PATH_B = Path.of(FILE_NAME_B);
    private static final String DIR_NAME = "testFile";
    private static final Path DIR_PATH = Path.of(DIR_NAME);
    private static final String DIR_FILE_NAME_A = "testFileA.txt";
    private static final Path DIR_FILE_PATH_A = Path.of( DIR_PATH + "/" + DIR_FILE_NAME_A);
    private static final String DIR_FILE_NAME_B = "testFileB.docx";
    private static final Path DIR_FILE_PATH_B = Path.of( DIR_PATH + "/" + DIR_FILE_NAME_B);

    /**
     * Sets up testing environment.
     *
     * @throws IOException the io exception
     */
    @BeforeEach
    void setup() throws IOException {
        Files.createDirectory(EMPTY_DIR_PATH);
        Files.createDirectory(DIR_PATH);
        Files.createFile(FILE_PATH_A);
        Files.createFile(FILE_PATH_B);
        Files.createFile(DIR_FILE_PATH_A);
        Files.createFile(DIR_FILE_PATH_B);
        regexArgument = new RegexArgument();
    }

    /**
     * Teardown testing environment.
     *
     * @throws IOException the io exception
     */
    @AfterEach
    void teardown() throws IOException {
        Files.deleteIfExists(FILE_PATH_A);
        Files.deleteIfExists(FILE_PATH_B);
        Files.deleteIfExists(FILE_PATH_B);
        Files.deleteIfExists(DIR_FILE_PATH_A);
        Files.deleteIfExists(DIR_FILE_PATH_B);
        Files.deleteIfExists(EMPTY_DIR_PATH);
        Files.deleteIfExists(DIR_PATH);
        regexArgument = null;
    }

    /**
     * Tests the behavior of {@link RegexArgument#globFiles()} when no matching paths are found.
     */
    @Test
    void globFiles_NoMatchingPaths_ReturnOriginalArgument() {
        for (char c : EMPTY_DIR_NAME.toCharArray()) {
            regexArgument.append(c);
        }
        regexArgument.append('/');
        regexArgument.appendAsterisk();
        List<String> expectedResult = new LinkedList<>();
        expectedResult.add(EMPTY_DIR_NAME + "/*");
        List<String> result = regexArgument.globFiles();
        assertEquals(expectedResult, result);
        assertTrue(Files.exists(EMPTY_DIR_PATH));
        assertEquals(0, Objects.requireNonNull(new File(EMPTY_DIR_NAME).listFiles()).length);
    }

    /**
     * Tests the ability of {@link RegexArgument#globFiles()} to match files based on extensions.
     */
    @Test
    void globFiles_MatchingFilesByExtension_ReturnOnlyMatchingFiles() {
        for (char c : DIR_NAME.toCharArray()) {
            regexArgument.append(c);
        }
        regexArgument.append('/');
        regexArgument.appendAsterisk();
        String matchingRegex = ".txt";
        for (char c : matchingRegex.toCharArray()) {
            regexArgument.append(c);
        }
        List<String> expectedResult = new LinkedList<>();
        expectedResult.add(DIR_NAME + File.separator + DIR_FILE_NAME_A);
        List<String> result = regexArgument.globFiles();
        assertTrue(Files.exists(DIR_FILE_PATH_A));
        assertTrue(Files.exists(DIR_FILE_PATH_B));
        assertEquals(expectedResult, result);
        assertEquals(1, expectedResult.size());
    }

    /**
     * Tests the ability of {@link RegexArgument#globFiles()} to match files based on partial paths.
     */
    @Test
    void globFiles_MatchingFilesByPartialPath_ReturnOnlyMatchingFiles() {
        for (char c : DIR_NAME.toCharArray()) {
            regexArgument.append(c);
        }
        regexArgument.append('/');
        regexArgument.appendAsterisk();
        List<String> expectedResult = new LinkedList<>();
        expectedResult.add(DIR_NAME + File.separator + DIR_FILE_NAME_A);
        expectedResult.add(DIR_NAME + File.separator + DIR_FILE_NAME_B);
        List<String> result = regexArgument.globFiles();
        assertTrue(Files.exists(DIR_FILE_PATH_A));
        assertTrue(Files.exists(DIR_FILE_PATH_B));
        assertEquals(expectedResult, result);
        assertEquals(2, expectedResult.size());
    }

    /**
     * Tests the ability of {@link RegexArgument#globFiles()} to match both files and directories.
     */
    @Test
    void globFiles_MatchingFilesAndDirectories_ReturnListOfPaths() {
        for (char c : "test".toCharArray()) {
            regexArgument.append(c);
        }
        regexArgument.appendAsterisk();
        List<String> expectedResult = new LinkedList<>();
        expectedResult.add(EMPTY_DIR_NAME);
        expectedResult.add(DIR_NAME);
        expectedResult.add(FILE_NAME_A);
        expectedResult.add(FILE_NAME_B);
        List<String> result = regexArgument.globFiles();
        assertTrue(Files.exists(DIR_FILE_PATH_A));
        assertTrue(Files.exists(DIR_FILE_PATH_B));
        assertEquals(expectedResult, result);
        assertEquals(4, expectedResult.size());
    }
}