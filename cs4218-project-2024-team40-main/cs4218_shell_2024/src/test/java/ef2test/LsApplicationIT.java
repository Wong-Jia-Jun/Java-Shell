package ef2test;

import org.junit.jupiter.api.*;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.impl.app.LsApplication;
import sg.edu.nus.comp.cs4218.impl.parser.LsArgsParser;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;
import sg.edu.nus.comp.cs4218.testutils.TestStringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.CHAR_FILE_SEP;

public class LsApplicationIT { //NOPMD
    private static final String INVALID_DIR = "invalid";
    private static final String ERR_MSG = "ls: cannot access 'temp-ls" + File.separator + "invalid': No such file or directory" + STRING_NEWLINE;
    private static final String TEMP = "temp-ls";
    private static final String SUBFOLDER_A = "subfolderA";
    private static final String TXT_IN_SUBFDR_A = "file_in_subfolderA.txt";
    private static final String JPG_IN_SUBFDR_A = "image_in_subfolderA.jpg";
    private static final String DOC_IN_SUBFDR_A = "document_in_subfolderA.doc";
    private static final String SUBFOLDER_B = "subfolderB";
    private static final String TXT_IN_SUBFDR_B = "file_in_subfolderB.txt";
    private static final String JPG_IN_SUBFDR_B = "image_in_subfolderB.jpg";
    private static final String DOC_IN_SUBFDR_B = "document_in_subfolderB.doc";
    private static final String FILE_A_TXT = "fileA.txt";
    private static final String FIRST_FOLDER = "first_folder";
    private static final String SECOND_FOLDER = "second_folder";
    private static final String TXT_IN_FIRST_FDR = "file_in_first_folder.txt";
    private static final String JPG_IN_FIRST_FDR = "image_in_first_folder.jpg";
    private static final String DOC_IN_FIRST_FDR = "document_in_first_folder.doc";
    private static final String TXT_IN_SECOND_FDR = "file_in_second_folder.txt";
    private static final String JPG_IN_SECOND_FDR = "image_in_second_folder.jpg";
    private static final String DOC_IN_SECOND_FDR = "document_in_second_folder.doc";
    private static final String COLON = ":";

    private static final Deque<Path> FILES = new ArrayDeque<>();

    private static Path tempPath;
    private LsApplication lsApplication;

    @BeforeAll
    static void setUp() throws NoSuchFieldException, IllegalAccessException {
        Path originalPath = Paths.get(TestEnvironmentUtil.getCurrentDirectory());
        tempPath = Paths.get(originalPath.toString(), TEMP);
    }

    @BeforeEach
    void init() throws IOException {
        lsApplication = new LsApplication(new LsArgsParser());
        Files.createDirectory(tempPath);
    }

    @AfterEach
    void deleteTemp() throws IOException {
        for (Path file : FILES) {
            Files.deleteIfExists(file);
        }
        Files.delete(tempPath);
    }

    private String[] toArgs(String flag, String... files) {
        List<String> args = new ArrayList<>();
        if (!flag.isEmpty()) {
            args.add("-" + flag);
        }
        for (String file : files) {
            args.add(Paths.get(TEMP, file).toString());
        }
        return args.toArray(new String[0]);
    }

    private void createFile(String name) throws IOException {
        createFile(name, tempPath);
    }

    private Path createDirectory(String folder) throws IOException {
        return createDirectory(folder, tempPath);
    }

    private void createFile(String name, Path inPath) throws IOException {
        Path path = inPath.resolve(name);
        Files.createFile(path);
        FILES.push(path);
    }

    private Path createDirectory(String folder, Path inPath) throws IOException {
        Path path = inPath.resolve(folder);
        Files.createDirectory(path);
        FILES.push(path);
        return path;
    }

    private ByteArrayOutputStream createDoublyNestedFolders() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Path firstDir = createDirectory(FIRST_FOLDER);
        createFile(TXT_IN_FIRST_FDR, firstDir);
        createFile(JPG_IN_FIRST_FDR, firstDir);
        createFile(DOC_IN_FIRST_FDR, firstDir);

        Path dirA = createDirectory(SUBFOLDER_A, firstDir);
        createFile(TXT_IN_SUBFDR_A, dirA);
        createFile(JPG_IN_SUBFDR_A, dirA);
        createFile(DOC_IN_SUBFDR_A, dirA);

        Path secondDir = createDirectory(SECOND_FOLDER);
        createFile(TXT_IN_SECOND_FDR, secondDir);
        createFile(JPG_IN_SECOND_FDR, secondDir);
        createFile(DOC_IN_SECOND_FDR, secondDir);

        Path dirB = createDirectory(SUBFOLDER_B, secondDir);
        createFile(TXT_IN_SUBFDR_B, dirB);
        createFile(JPG_IN_SUBFDR_B, dirB);
        createFile(DOC_IN_SUBFDR_B, dirB);
        return output;
    }

    @Test
    void run_RecursiveFlagWithSingleInvalidArg_PrintsError() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        lsApplication.run(toArgs("R", INVALID_DIR), System.in, output);
        assertEquals(ERR_MSG, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_RecursiveFlagWithInvalidArgsAndValidFileAndValidDir_PrintsErrorAndValidFileAndValidDir() throws AbstractApplicationException, IOException {
        ByteArrayOutputStream output = createDoublyNestedFolders();
        lsApplication.run(toArgs("R", FIRST_FOLDER, INVALID_DIR, SECOND_FOLDER), System.in, output);
        assertEquals((TEMP + CHAR_FILE_SEP + FIRST_FOLDER + COLON + TestStringUtils.STRING_NEWLINE + DOC_IN_FIRST_FDR + TestStringUtils.STRING_NEWLINE +
                TXT_IN_FIRST_FDR + TestStringUtils.STRING_NEWLINE + JPG_IN_FIRST_FDR + TestStringUtils.STRING_NEWLINE + SUBFOLDER_A + TestStringUtils.STRING_NEWLINE +
                TestStringUtils.STRING_NEWLINE + TEMP + CHAR_FILE_SEP + FIRST_FOLDER + CHAR_FILE_SEP + SUBFOLDER_A + COLON + TestStringUtils.STRING_NEWLINE +
                DOC_IN_SUBFDR_A + TestStringUtils.STRING_NEWLINE + TXT_IN_SUBFDR_A + TestStringUtils.STRING_NEWLINE + JPG_IN_SUBFDR_A +
                TestStringUtils.STRING_NEWLINE + TestStringUtils.STRING_NEWLINE + ERR_MSG + TEMP + CHAR_FILE_SEP + SECOND_FOLDER + COLON + TestStringUtils.STRING_NEWLINE + DOC_IN_SECOND_FDR +
                TestStringUtils.STRING_NEWLINE + TXT_IN_SECOND_FDR + TestStringUtils.STRING_NEWLINE + JPG_IN_SECOND_FDR + TestStringUtils.STRING_NEWLINE + SUBFOLDER_B +
                TestStringUtils.STRING_NEWLINE + TestStringUtils.STRING_NEWLINE + TEMP + CHAR_FILE_SEP + SECOND_FOLDER + CHAR_FILE_SEP + SUBFOLDER_B + COLON + TestStringUtils.STRING_NEWLINE +
                DOC_IN_SUBFDR_B + TestStringUtils.STRING_NEWLINE + TXT_IN_SUBFDR_B + TestStringUtils.STRING_NEWLINE + JPG_IN_SUBFDR_B +
                TestStringUtils.STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_LsOnFile_PrintsFileName() throws AbstractApplicationException, IOException {
        createFile(FILE_A_TXT);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        lsApplication.run(toArgs("", FILE_A_TXT), System.in, output);
        assertEquals(FILE_A_TXT + STRING_NEWLINE, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_LsNoFlags_PrintsLsSortedAlphabetically() throws AbstractApplicationException, IOException {
        String oldEnv = Environment.currentDirectory;
        Environment.currentDirectory = tempPath.toString();
        createFile(FILE_A_TXT);
        createFile(TXT_IN_FIRST_FDR);
        createFile(JPG_IN_FIRST_FDR);
        createFile(DOC_IN_FIRST_FDR);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        lsApplication.run(toArgs(""), System.in, output);
        assertEquals(DOC_IN_FIRST_FDR + STRING_NEWLINE + FILE_A_TXT + STRING_NEWLINE + TXT_IN_FIRST_FDR + STRING_NEWLINE + JPG_IN_FIRST_FDR + STRING_NEWLINE, output.toString(StandardCharsets.UTF_8));
        Environment.currentDirectory = oldEnv;
    }

    @Test
    void run_CompareExtensionOnlyFolders_PrintsLsSortedAlphabetically() throws AbstractApplicationException, IOException {
        String oldEnv = Environment.currentDirectory;
        Environment.currentDirectory = tempPath.toString();
        createDoublyNestedFolders();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        lsApplication.run(toArgs("X"), System.in, output);
        assertEquals(FIRST_FOLDER + STRING_NEWLINE + SECOND_FOLDER + STRING_NEWLINE, output.toString(StandardCharsets.UTF_8));
        Environment.currentDirectory = oldEnv;
    }
}
