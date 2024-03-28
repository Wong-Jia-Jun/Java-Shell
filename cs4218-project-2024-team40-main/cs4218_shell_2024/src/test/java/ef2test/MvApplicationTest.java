package ef2test;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.MvException;
import sg.edu.nus.comp.cs4218.impl.app.MvApplication;
import sg.edu.nus.comp.cs4218.impl.parser.MvArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.fileSeparator;

public class MvApplicationTest {
    public static final String INVALID_SOURCE = "notADirectory";
    private MvApplication application;
    private static final String TEMP = "temp-mv" + File.separator;
    private static final String TEXT_A = "textA.txt";
    private static final String TEXT_A_PATH = TEMP + TEXT_A;
    private static final String TEXT_B = "textB.txt";
    private static final String TEXT_B_PATH = TEMP + TEXT_B;
    private static final String FOLDER = "folder" + File.separator;
    private static final String FOLDER_PATH = TEMP + FOLDER;
    private static final String MOVED_TEXT_TXT = "movedText.txt";
    private static final String MOVED_TXT_PATH = TEMP + MOVED_TEXT_TXT;
    private static final String TARGET_FOLDER = "targetFolder" + File.separator;
    private static final String TARGET_FDR_PATH = TEMP + TARGET_FOLDER;
    private static final String DIR_TEXT_A = "textA.txt";
    private static final String DIR_TEXT_A_PATH = TARGET_FDR_PATH + DIR_TEXT_A;
    private static final String MV_PREFIX = "mv: ";
    private static final String MV_MESSAGE = MV_PREFIX + "cannot stat ";

    void createAndWriteFile(String filePath) throws Exception {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("Content inside " + filePath);
        }
    }

    void createFolder(String folderPath) throws Exception {
        Path path = Paths.get(folderPath);
        Files.createDirectories(path);
    }

    void createMvResourcesAndFolders() throws Exception {
        createFolder(TEMP);
        createFolder(TARGET_FDR_PATH);
        createFolder(FOLDER_PATH);
    }

    private String getExceptionFormat(String str) {
        return String.format("'%s': ", str);
    }

    @BeforeEach
    void setup() throws Exception {
        MvArgsParser parser = mock(MvArgsParser.class); // This is only used in run() method, solely for instantiation
        application = new MvApplication(parser);
        createMvResourcesAndFolders();
        createAndWriteFile(TEXT_A_PATH);
        createAndWriteFile(TEXT_B_PATH);
        createAndWriteFile(DIR_TEXT_A_PATH);

        // Remove IOUtils.resolveFilePath static dependency
        try (MockedStatic<IOUtils> mockedUtils = mockStatic(IOUtils.class)) {
            Path textA = Path.of(TEXT_A_PATH);
            mockedUtils.when(() -> IOUtils.resolveFilePath(TEXT_A_PATH)).thenReturn(textA);
            assertEquals(textA, IOUtils.resolveFilePath(TEXT_A_PATH));
            Path textB = Path.of(TEXT_B_PATH);
            mockedUtils.when(() -> IOUtils.resolveFilePath(TEXT_B_PATH)).thenReturn(textB);
            assertEquals(textB, IOUtils.resolveFilePath(TEXT_B_PATH));
            Path movedText = Path.of(MOVED_TXT_PATH);
            mockedUtils.when(() -> IOUtils.resolveFilePath(MOVED_TXT_PATH)).thenReturn(movedText);
            assertEquals(movedText, IOUtils.resolveFilePath(MOVED_TXT_PATH));
            Path targetFdr = Path.of(TARGET_FDR_PATH);
            mockedUtils.when(() -> IOUtils.resolveFilePath(TARGET_FDR_PATH)).thenReturn(targetFdr);
            assertEquals(targetFdr, IOUtils.resolveFilePath(TARGET_FDR_PATH));
            Path folder = Path.of(FOLDER_PATH);
            mockedUtils.when(() -> IOUtils.resolveFilePath(FOLDER_PATH)).thenReturn(folder);
            assertEquals(folder, IOUtils.resolveFilePath(FOLDER_PATH));
            Path invalidSource = Path.of(INVALID_SOURCE);
            mockedUtils.when(() -> IOUtils.resolveFilePath(INVALID_SOURCE)).thenReturn(invalidSource);
            assertEquals(invalidSource, IOUtils.resolveFilePath(INVALID_SOURCE));
        }
    }

    @AfterEach
    void tearDown() {
        try (Stream<Path> pathStream = Files.walk(Paths.get(TEMP))) {
            pathStream.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(file -> {
                if (!file.delete()) {
                    System.err.println("Failed to delete file: " + file.getAbsolutePath());
                }
            });
        } catch (IOException e) {
            System.err.println("Error walking through directory: " + e.getMessage());
        }
    }

    /**
     * mv src file to dest file invalid source and valid target should return error string.
     */
    @Test
    void mvSrcFileToDestFile_InvalidSourceAndValidTarget_ShouldReturnErrorString() throws MvException {
        String result = application.mvSrcFileToDestFile(false, "hello.text", "");
        assertEquals(MV_MESSAGE + getExceptionFormat("hello.text") + ERR_FILE_NOT_FND, result);
    }

    /**
     * mv src file to dest file target is a directory should return error string.
     */
    @Test
    void mvSrcFileToDestFile_SrcFileIsADirectory_ShouldReturnErrorString() throws MvException {
        String result = application.mvSrcFileToDestFile(false, TARGET_FDR_PATH, TEXT_A_PATH);
        assertEquals(String.format("mv: cannot overwrite non-directory '%s' with directory '%s'", TEXT_A_PATH, TARGET_FDR_PATH), result);
    }

    /**
     * mv src file to dest file option given and existing target should return error string.
     */
    @Test
    void mvSrcFileToDestFile_OptionNotGivenAndExistingTarget_ShouldReturnErrorString() throws MvException {
        String result = application.mvSrcFileToDestFile(false, TEXT_A_PATH, TEXT_B_PATH);
        assertEquals(MV_MESSAGE + getExceptionFormat(TEXT_B_PATH) + ERR_FILE_EXISTS, result);
    }

    @Test
    void mvSrcFileToDestFile_NoOptionGivenAndValidFile_FileRenamed() throws Exception {
        File source = new File(TEXT_A_PATH);
        List<String> sourceContent = Files.readAllLines(source.toPath());

        String output = application.mvSrcFileToDestFile(false, TEXT_A_PATH, MOVED_TXT_PATH);
        File target = new File(MOVED_TXT_PATH);
        List<String> targetContent = Files.readAllLines(target.toPath());

        assertTrue(target.exists());
        assertFalse(source.exists());
        assertEquals(sourceContent, targetContent);
        assertEquals(output, null);
    }

    @Test
    void mvSrcFileToDestFile_ValidFile_FileRenamed() throws Exception {
        File source = new File(TEXT_A_PATH);
        List<String> sourceContent = Files.readAllLines(source.toPath());

        application.mvSrcFileToDestFile(true, TEXT_A_PATH, MOVED_TXT_PATH);
        File target = new File(MOVED_TXT_PATH);
        List<String> targetContent = Files.readAllLines(target.toPath());

        assertTrue(target.exists());
        assertFalse(source.exists());
        assertEquals(sourceContent, targetContent);
    }

    @Test
    void mvSrcFileToDestFile_ValidFolder_FolderRenamed() throws Exception {
        File source = new File(FOLDER_PATH);

        application.mvSrcFileToDestFile(true, FOLDER_PATH, MOVED_TXT_PATH);
        File target = new File(MOVED_TXT_PATH);

        assertTrue(target.exists());
        assertTrue(target.isDirectory());
        assertFalse(source.exists());
    }
    @Test
    void mvSrcFileToDestFile_SameSrcDest_NothingHappens() throws Exception {
        File source = new File(FOLDER_PATH);
        String output = application.mvSrcFileToDestFile(true, FOLDER_PATH, FOLDER_PATH);
        assertEquals(output, null);
        assertTrue(source.exists());
    }
    @Test
    void mvSrcFileToDestFile_ErrorMovingFile_NothingHappens() throws Exception {
        try (MockedStatic<Files> mockedUtils = mockStatic(Files.class)) {
            mockedUtils.when(() -> Files.exists(Path.of(Environment.currentDirectory + File.separator + TEXT_A_PATH))).thenReturn(true);
            mockedUtils.when(() -> Files.isDirectory(any())).thenReturn(false);
            mockedUtils.when(() -> Files.isRegularFile(any())).thenReturn(false);
            mockedUtils.when(() -> Files.exists(Path.of(Environment.currentDirectory + File.separator + MOVED_TEXT_TXT))).thenReturn(false);
            mockedUtils.when(() -> Files.move(any(), any())).thenThrow(IOException.class);
            assertThrows(MvException.class, () ->application.mvSrcFileToDestFile(false, TEXT_A_PATH, MOVED_TXT_PATH));
        }
    }

    @Test
    void mvSrcFileToDestFile_ErrorDeleteFile_NothingHappens() {
        try (MockedStatic<Files> mockedUtils = mockStatic(Files.class)) {
            mockedUtils.when(() -> Files.exists(any())).thenReturn(true);
            mockedUtils.when(() -> Files.isDirectory(any())).thenReturn(false);
            mockedUtils.when(() -> Files.isRegularFile(any())).thenReturn(false);
            mockedUtils.when(() -> Files.deleteIfExists(any())).thenThrow(IOException.class);
            assertThrows(MvException.class, ()-> application.mvSrcFileToDestFile(true, FOLDER_PATH, MOVED_TXT_PATH));
        }
    }
    /**
     * Mv files to folder valid source and invalid directory should return error string.
     */
    @Test
    void mvFilesToFolder_ValidSourceAndInvalidDirectory_ShouldReturnErrorString() throws MvException {
        File target = new File(TEXT_A_PATH);
        String result = application.mvFilesToFolder(false, INVALID_SOURCE, TEXT_A_PATH);

        assertEquals(MV_MESSAGE + getExceptionFormat(INVALID_SOURCE) + ERR_IS_NOT_DIR, result);
        assertTrue(target.exists());
    }
    /**
     * Mv files to folder error deleting file should throw exception.
     */
    @Test
    void mvFilesToFolder_ErrorDeleteFile_ThrowException()  {
        try (MockedStatic<Files> mockedUtils = mockStatic(Files.class)) {
            mockedUtils.when(() -> Files.isDirectory(any())).thenReturn(true);
            mockedUtils.when(() -> Files.exists(Path.of(Environment.currentDirectory + File.separator + TEXT_A_PATH))).thenReturn(true);
            mockedUtils.when(() -> Files.deleteIfExists(any())).thenThrow(IOException.class);
            assertThrows(MvException.class, () -> application.mvFilesToFolder(true, TARGET_FDR_PATH, TEXT_A_PATH));

        }
    }
    /**
     * Mv files to folder error moving file should throw exception.
     */
    @Test
    void mvFilesToFolder_ErrorMovingtoDest_ThrowException() throws MvException {
        try (MockedStatic<Files> mockedUtils = mockStatic(Files.class)) {
            mockedUtils.when(() -> Files.isDirectory(any())).thenReturn(true);
            mockedUtils.when(() -> Files.exists(Path.of(Environment.currentDirectory + File.separator + TEXT_A_PATH))).thenReturn(true);
            mockedUtils.when(() -> Files.exists(Path.of(Environment.currentDirectory + File.separator + TARGET_FDR_PATH).resolve(Path.of(TEXT_A_PATH).getFileName()))).thenReturn(false);
            mockedUtils.when(() -> Files.move(any(), any())).thenThrow(IOException.class);
            assertThrows(MvException.class, () -> application.mvFilesToFolder(false, TARGET_FDR_PATH, TEXT_A_PATH));

        }
    }

    /**
     * Mv files to folder valid source and existing file in directory and option given should return error string.
     */
    @Test
    void mvFilesToFolder_ValidSourceAndExistingFileInDirectoryAndOptionNotGiven_ShouldReturnErrorString() throws MvException {
        File target = new File(TEXT_A_PATH);
        String result = application.mvFilesToFolder(false, TARGET_FDR_PATH, TEXT_A_PATH);
        assertEquals(MV_MESSAGE + getExceptionFormat(TEXT_A_PATH) + ERR_FILE_EXISTS, result);

        assertTrue(target.exists());
    }

    /**
     * Mv files to folder valid source and existing file in directory should replace files.
     */
    @Test
    void mvFilesToFolder_ValidSourceAndExistingFileInDirectory_ShouldReplaceFiles() throws MvException, IOException {
        File source = new File(TEXT_A_PATH);
        List<String> sourceContent = Files.readAllLines(source.toPath());
        String result = application.mvFilesToFolder(true, TARGET_FDR_PATH, TEXT_A_PATH);
        File target = new File(TARGET_FDR_PATH + TEXT_A);
        List<String> targetContent = Files.readAllLines(target.toPath());

        assertEquals("", result);
        assertFalse(source.exists());
        assertTrue(target.exists());
        assertEquals(sourceContent, targetContent);
    }


    /**
     * Mv files to folder valid and invalids sources should move valid files and print exceptions to stdout.
     */
    @Test
    void mvFilesToFolder_ValidAndInvalidsSources_ShouldMoveValidFilesAndPrintExceptionsToStdout() throws MvException, IOException {
        File source = new File(TEXT_B_PATH);
        List<String> sourceContent = Files.readAllLines(source.toPath());

        String[] filenames = {TEXT_B_PATH, INVALID_SOURCE, FOLDER_PATH};
        String result = application.mvFilesToFolder(false, TARGET_FDR_PATH, filenames);
        File target = new File(TARGET_FDR_PATH + TEXT_B);
        List<String> targetContent = Files.readAllLines(target.toPath());

        assertEquals(MV_MESSAGE + getExceptionFormat(INVALID_SOURCE) + ERR_FILE_NOT_FND, result);
        assertTrue(target.exists());
        assertFalse(source.exists());
        assertEquals(sourceContent, targetContent);
    }

    /**
     * Mv files to folder valid and invalids sources and existing file in directory should move valid files and print exceptions to stdout.
     */
    @Test
    void mvFilesToFolder_ValidAndInvalidsSourcesAndExistingFileInDirectory_ShouldMoveValidFilesAndPrintExceptionsToStdout() throws MvException, IOException {
        File source = new File(TEXT_B_PATH);
        List<String> sourceContent = Files.readAllLines(source.toPath());
        String[] filenames = {TEXT_A_PATH, INVALID_SOURCE, TARGET_FDR_PATH, TEXT_B_PATH};
        Path expectedPathB = Path.of(TARGET_FDR_PATH + File.separator + TEXT_B);
        String result = application.mvFilesToFolder(false, TARGET_FDR_PATH, filenames);
        List<String> expectedResult = List.of(MV_MESSAGE + getExceptionFormat(TEXT_A_PATH) + ERR_FILE_EXISTS,
                MV_MESSAGE + getExceptionFormat(INVALID_SOURCE) + ERR_FILE_NOT_FND);
        File target = new File(TARGET_FDR_PATH + TEXT_B);
        List<String> targetContent = Files.readAllLines(target.toPath());

        assertEquals(String.join(STRING_NEWLINE, expectedResult), result);
        assertTrue(Files.exists(Path.of(TEXT_A_PATH)));
        assertTrue(Files.exists(expectedPathB));
        assertEquals(sourceContent, targetContent);
    }

    @Test
    void mvFilesToFolder_SourceFileNotExisting_FileMoved() throws Exception {
        File source = new File(TEXT_A_PATH);
        List<String> sourceContent = Files.readAllLines(source.toPath());

        application.mvFilesToFolder(true, TARGET_FDR_PATH, TEXT_A_PATH);
        File target = new File(TARGET_FDR_PATH + TEXT_A);
        List<String> targetContent = Files.readAllLines(target.toPath());

        assertTrue(target.exists());
        assertFalse(source.exists());
        assertEquals(sourceContent, targetContent);
    }

    @Test
    void mvFilesToFolder_SourceFolderNotExisting_FolderMoved() throws Exception {
        File source = new File(FOLDER_PATH);

        application.mvFilesToFolder(true, TARGET_FDR_PATH, FOLDER_PATH);
        File target = new File(TARGET_FDR_PATH + FOLDER);

        assertTrue(target.exists());
        assertTrue(target.isDirectory());
        assertFalse(source.exists());
    }

    @Test
    void mvFilesToFolder_MultipleSourceFile_FilesMoved() throws Exception {
        File sourceA = new File(TEXT_A_PATH);
        List<String> sourceAContent = Files.readAllLines(sourceA.toPath());
        File sourceB = new File(TEXT_B_PATH);
        List<String> sourceBContent = Files.readAllLines(sourceB.toPath());

        application.mvFilesToFolder(true, TARGET_FDR_PATH, TEXT_A_PATH, TEXT_B_PATH);
        File targetA = new File(TARGET_FDR_PATH + TEXT_A);
        List<String> targetAContent = Files.readAllLines(targetA.toPath());
        File targetB = new File(TARGET_FDR_PATH + TEXT_B);
        List<String> targetBContent = Files.readAllLines(targetB.toPath());

        assertTrue(targetA.exists());
        assertTrue(targetB.exists());
        assertFalse(sourceA.exists());
        assertFalse(sourceB.exists());
        assertEquals(sourceAContent, targetAContent);
        assertEquals(sourceBContent, targetBContent);
    }

}
