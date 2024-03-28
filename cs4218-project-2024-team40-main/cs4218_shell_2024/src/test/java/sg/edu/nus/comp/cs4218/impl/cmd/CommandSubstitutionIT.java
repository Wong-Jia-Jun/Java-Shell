package sg.edu.nus.comp.cs4218.impl.cmd;

import org.junit.jupiter.api.*;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.Command;

import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.CommandBuilder;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.*;
import java.util.List;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration Tests of Command subs
 */
public class CommandSubstitutionIT {
    /**
     * The Current out.
     */
    ByteArrayOutputStream currentOut;
    private static final String BASE_PATH = Environment.currentDirectory;
    private static final String TEST_FOLDER_NAME = "commandSubsTestFolder";
    private static final String TEST_RESOURCE_DIR = "src/test/resources/app";
    private static final String TEST_DIR = Environment.currentDirectory + File.separator + TEST_RESOURCE_DIR + File.separator + TEST_FOLDER_NAME;
    private static final String TEST_PATH = BASE_PATH + File.separator + TEST_FOLDER_NAME;
    private static final String TEST_FILE1 = "testfile1";
    private static final String TEST_FILE2 = "testfile2";
    private static final String TEST_FOLDER = "testfolder";

    /**
     * Sets up.
     *
     * @throws IOException the io exception
     */
    @BeforeAll
    static void setUp() throws IOException {
        Environment.currentDirectory = TEST_PATH;
        Files.createDirectories(Paths.get(TEST_PATH + File.separator + TEST_FOLDER));
        Files.createFile(Paths.get(TEST_PATH + File.separator + TEST_FILE1));
        Files.createFile(Paths.get(TEST_PATH + File.separator + TEST_FILE2));


    }

    /**
     * Delete dir.
     *
     * @param file the file
     */
    public static void deleteDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDir(f);
            }
        }
        file.delete();
    }

    /**
     * Tear down.
     */
    @AfterAll
    static void tearDown() {
        Environment.currentDirectory = BASE_PATH;
        deleteDir(new File(TEST_PATH));
    }

    /**
     * Sets up each.
     */
    @BeforeEach
    void setUpEach() {
        currentOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(currentOut));
    }

    /**
     * Command substitution test nested sub return correct output.
     *
     * @throws ShellException               the shell exception
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     */
    @Test
    void commandSubstitutionTest_NestedSub_ReturnCorrectOutput() throws ShellException, AbstractApplicationException, IOException {
        String inputString = "echo `echo `echo hello world` `";
        Command command = CommandBuilder.parseCommand(inputString, new ApplicationRunner());
        command.evaluate(System.in, System.out);
        final String standardOutput = currentOut.toString();
        assertEquals("echo hello world" + System.lineSeparator(), standardOutput);
    }

    /**
     * Command substitution test basic echo return correct output.
     *
     * @throws ShellException               the shell exception
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     */
    @Test
    void commandSubstitutionTest_BasicEcho_ReturnCorrectOutput() throws ShellException, AbstractApplicationException, IOException {
        String inputString = "echo `echo hello world`";
        Command command = CommandBuilder.parseCommand(inputString, new ApplicationRunner());
        command.evaluate(System.in, System.out);
        final String standardOutput = currentOut.toString();
        assertEquals("hello world" + System.lineSeparator(), standardOutput);
    }

    /**
     * Command substitution test basic echo trailing new line new line deleted.
     *
     * @throws ShellException               the shell exception
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     */
    @Disabled
    @Test
    void commandSubstitutionTest_BasicEchoTrailingNewLine_NewLineDeleted() throws ShellException, AbstractApplicationException, IOException {
        String commandWthNewLine= "echo hello world\n" ;
        String inputString = "echo `" + commandWthNewLine +"`";
        Command command = CommandBuilder.parseCommand(inputString, new ApplicationRunner());
        command.evaluate(System.in, System.out);
        final String standardOutput = currentOut.toString();
        assertEquals("hello world" + System.lineSeparator(), standardOutput);
    }

    /**
     * Command substitution test single quote return correct output.
     *
     * @throws ShellException               the shell exception
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     */
    @Test
    void commandSubstitutionTest_SingleQuote_ReturnCorrectOutput() throws ShellException, AbstractApplicationException, IOException  {
        String inputString = "echo `echo ‘quote is not interpreted as special character’`";
        Command command = CommandBuilder.parseCommand(inputString, new ApplicationRunner());
        command.evaluate(System.in, System.out);
        final String standardOutput = currentOut.toString();
        assertEquals("‘quote is not interpreted as special character’" + System.lineSeparator(), standardOutput);
    }

    /**
     * Command substitution test enclosed in single quote not substituted.
     *
     * @throws ShellException               the shell exception
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     */
    @Test
    void commandSubstitutionTest_EnclosedInSingleQuote_NotSubstituted() throws ShellException, AbstractApplicationException, IOException  {
        String inputString = "echo '`echo enclosed in single quote not CMD Sub`'";
        Command command = CommandBuilder.parseCommand(inputString, new ApplicationRunner());
        command.evaluate(System.in, System.out);
        final String standardOutput = currentOut.toString();
        assertEquals("`echo enclosed in single quote not CMD Sub`" + System.lineSeparator(), standardOutput);
    }

    /**
     * Command substitution test redirection return correct output.
     *
     * @throws ShellException               the shell exception
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     */
//ls depends on directory user currently in not sure abt outptt in file yet
    @Disabled
    @Test
    void commandSubstitutionTest_Redirection_ReturnCorrectOutput() throws ShellException, AbstractApplicationException, IOException {
        String inputString = "echo files in `ls` > 1.txt";
        Command command = CommandBuilder.parseCommand(inputString, new ApplicationRunner());
        command.evaluate(System.in, System.out);
        final String standardOutput = currentOut.toString();
        String expected = "";
        List<String> lscontent = IOUtils.getLinesFromInputStream(new FileInputStream(""));
        assertEquals(expected, standardOutput);
    }

    /**
     * Command substitution test invalid command throw exception.
     *
     * @throws ShellException               the shell exception
     * @throws AbstractApplicationException the abstract application exception
     * @throws IOException                  the io exception
     */
    @Test
    void commandSubstitutionTest_InvalidCommand_ThrowException() throws ShellException, AbstractApplicationException, IOException  {
        String inputString = "echo `invalidCommand’`";
        Command command = CommandBuilder.parseCommand(inputString, new ApplicationRunner());
        assertThrows(ShellException.class , () -> command.evaluate(System.in, System.out));
    }


}
