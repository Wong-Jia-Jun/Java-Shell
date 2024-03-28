package sg.edu.nus.comp.cs4218.impl.cmd;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;;
import sg.edu.nus.comp.cs4218.exception.WcException;;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandler;
import sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandlerFactory;


import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

public class CallCommandITest {


    /**
     *  in each test check for  what are the expected
     *          List<String> noRedirArgsList = redirHandler.getNoRedirArgsList();
     *         InputStream inputStream = redirHandler.getInputStream(); //NOPMD - dont want to close right after as shell wants to keep taking input
     *         OutputStream outputStream = redirHandler.getOutputStream();//NOPMD - dont want to close right after as shell wants to keep taking input
     *         List<String> parsedArgsList = argumentResolver.parseArguments(noRedirArgsList);
     */
    private static List<String> contents = new ArrayList<>();
    private static final String ORIGINAL_DIR = Environment.currentDirectory;
     private static final String RESOURCES_PATH = Path.of("src", "test", "resources").toString();
    private static final String TEST_DIR = Environment.currentDirectory + File.separator + RESOURCES_PATH + File.separator + "CallCommandIT";
    private static final Path TEST_PATH = Path.of(TEST_DIR);

    private static final String FILE_1 = "file1.txt";
    private static final String FILE_2 = "file2.txt";
    private static final String SUB_DIR = "subdir";
    private static final String ECHO_CMD = "echo";
    private static final Path SUB_DIR_PATH = Path.of(TEST_DIR + File.separator + SUB_DIR);

    private final Path file1 = Path.of(TEST_DIR, FILE_1);
    private final Path file2 = Path.of(TEST_DIR, FILE_2);
    private final InputStream stdin = System.in;
    private final OutputStream stdout = new ByteArrayOutputStream();
    private CallCommand command;
    private static void deleteDirectory(File directory) {
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
     * Write to file.
     *
     * @param file    the file
     * @param content the content
     * @throws IOException the io exception
     */
    public static void writeToFile(Path file, String... content) throws IOException {
        contents = new ArrayList<>();
        for (String line : content) {
            contents.add(line);
        }
        Files.write(file, contents, WRITE, TRUNCATE_EXISTING);

    }
    @BeforeAll
    static void setupBeforeAll() throws IOException {
        TEST_PATH.toFile().mkdir();
        Environment.currentDirectory = TEST_DIR;
    }

    @AfterAll
    static void tearDownAfterAll() throws IOException {
        deleteDirectory(TEST_PATH.toFile());
        Environment.currentDirectory = ORIGINAL_DIR;
    }




    @BeforeEach
    void setUp() throws IOException {
        TEST_PATH.toFile().mkdir();
        SUB_DIR_PATH.toFile().mkdir();
        if (Files.notExists(file1)) {
            Files.createFile(file1);
        }
        if (Files.notExists(file2)) {
            Files.createFile(file2);
        }
        writeToFile(file1, "file1.txt");
        writeToFile(file2, "file2.txt");
    }

    @AfterEach
    void tearDown()  {
       deleteDirectory(TEST_PATH.toFile());
    }

    private void buildCommand(List<String> argList) throws ShellException {
        ApplicationRunner appRunner = new ApplicationRunner();
        command = new CallCommand(argList, appRunner, new ArgumentResolver(), new IORedirectionHandlerFactory());
    }


    @Test
    @DisplayName("echo hello > file1.txt")
    public void evaluate_EchoCommandWithIORedirectOut_CommandExecuted() throws ShellException, AbstractApplicationException, IOException {
            buildCommand(List.of(ECHO_CMD, "hello", ">", FILE_1));
            command.evaluate(stdin, stdout);
            List<String> output = Files.readAllLines(file1);
            assertEquals("hello", output.get(0));
    }

    @Test
    @DisplayName("ls -X .")
    public void evaluate_LsCommandCwd_CommandExecuted() throws ShellException, AbstractApplicationException, IOException  {
            buildCommand(List.of("ls", "-X", "."));
            command.evaluate(stdin, stdout);
            String output = stdout.toString();
            assertEquals(SUB_DIR + System.lineSeparator() + FILE_1 + System.lineSeparator()+ FILE_2 + System.lineSeparator(), output);
    }
    //will not have new line in std out here
    @Test
    @DisplayName("echo `ls`")
    public void evaluate_EchoCommandWithCommandSubLs_CommandExecuted() throws ShellException, AbstractApplicationException, IOException{
            buildCommand(List.of(ECHO_CMD, "`ls`"));
            command.evaluate(stdin, stdout);
            String output = stdout.toString();
            assertEquals(FILE_1 +
                    FILE_2 + SUB_DIR + System.lineSeparator(), output);
    }
    @Test
    void evaluate_EchoApplicationWithSingleQuote_DisableSpecialChar() throws FileNotFoundException, AbstractApplicationException, ShellException {
        buildCommand(List.of(ECHO_CMD,  "\'Travel time Singapore -> Paris is 13h and 15`\' "));
        command.evaluate(stdin,stdout);
        String output = stdout.toString();
        String expected = "Travel time Singapore -> Paris is 13h and 15` ";
        assertEquals(expected + System.lineSeparator(), output);
    }

    @Test
    void evaluate_EchoApplicationWithDoubleQuoting_BackQuoteNotDisabled() throws FileNotFoundException, AbstractApplicationException, ShellException {
        buildCommand(List.of(ECHO_CMD,  "\"This is space:`echo \" \"`.\"" ));
        command.evaluate(stdin,stdout);
        String output = stdout.toString();
        String expected = "This is space: .";
        assertEquals(expected + System.lineSeparator(), output);
    }
    @Test
    @DisplayName("cat *.txt")
    public void evaluate_CatCommandWithGlobbing_CommandExecuted() throws ShellException, AbstractApplicationException, IOException {
            buildCommand(List.of("cat", "*.txt"));
            command.evaluate(stdin, stdout);
            String output = stdout.toString();
            assertEquals(FILE_1 + System.lineSeparator()+ FILE_2 + System.lineSeparator(), output);
    }
    @Test
    @DisplayName("cat < 1.txt")
    public void evaluate_CatCommandWithIOInput_CommandExecuted() throws ShellException, AbstractApplicationException, IOException  {
            buildCommand(List.of("cat", "<", FILE_1));

            command.evaluate(stdin, stdout);

            String output = stdout.toString();
            assertEquals(FILE_1  + System.lineSeparator(), output);
    }

    @Test
    @DisplayName("uniq -c file1.txt")
    public void evaluate_UniqCommand_CommandExecuted() throws ShellException, AbstractApplicationException, IOException {
            String expected1 = "Hello World";
            String expected2 = "Alice";
            Files.writeString(file1, String.join(System.lineSeparator(), expected1, expected1,
                    expected2, expected2));
            buildCommand(List.of("uniq", "-c", FILE_1));
            command.evaluate(stdin, stdout);
            String output = stdout.toString();
            assertEquals("\t2 " + expected1 + System.lineSeparator()+ "\t2 " + expected2 + System.lineSeparator(), output);
    }
    @Test
    @DisplayName("cd subdir")
    public void evaluate_CdCommand_CommandExecuted() throws ShellException, AbstractApplicationException, IOException {
        buildCommand(List.of("cd", SUB_DIR));
        command.evaluate(stdin, stdout);
        assert  Environment.currentDirectory.endsWith("subdir");
    }
    @Test
    public void evaluate_InvalidApp_ThrowsShellException() {
       Throwable exp = assertThrows(ShellException.class, () -> {
            // Invalid command
            buildCommand(List.of("lsa"));
            command.evaluate(stdin, stdout);
        });
       assertEquals("shell: lsa: Invalid app", exp.getMessage());
    }

    @Test
    public void evaluate_InvalidAppOptions_ThrowsAppException() {
        Throwable exp = assertThrows(WcException.class, () -> {
            buildCommand(List.of("wc", "-X", FILE_1));

            command.evaluate(stdin, stdout);
        });
        assertEquals("wc: illegal option -- X", exp.getMessage());
    }
    @Test
    public void evaluate_NullStdin_ThrowsShellException() {
        Throwable exp = assertThrows(ShellException.class, () -> {
            buildCommand(List.of("wc", FILE_1));
            command.evaluate(null, stdout);
        });
        assertEquals("shell: Null Pointer Exception", exp.getMessage());
    }
    @Test
    public void evaluate_NullStdout_ThrowsShellException() {
        Throwable exp = assertThrows(ShellException.class, () -> {
            buildCommand(List.of("wc", FILE_1));
            command.evaluate(stdin, null);
        });
        assertEquals("shell: Null Pointer Exception", exp.getMessage());
    }
    @Test
    public void evaluate_NullStdoutnStdin_ThrowsShellException() {
        Throwable exp = assertThrows(ShellException.class, () -> {
            buildCommand(List.of("wc", FILE_1));
            command.evaluate(null, null);
        });
        assertEquals("shell: Null Pointer Exception", exp.getMessage());
    }
}