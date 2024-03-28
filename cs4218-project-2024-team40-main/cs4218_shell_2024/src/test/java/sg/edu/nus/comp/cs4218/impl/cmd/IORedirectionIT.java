package sg.edu.nus.comp.cs4218.impl.cmd;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;
import sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandlerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

public class IORedirectionIT {
    private static final String BASE_DIR = Environment.currentDirectory;
    private static final String TEST_RESOURCE_DIR = "src/test/resources/app";
    private static final String TEST_DIR = Environment.currentDirectory + File.separator + TEST_RESOURCE_DIR + File.separator + "IORedirectionIT";
    private static final String FILE_1 = "File1.txt";
    private static final String PATH_FILE1 =  TEST_DIR + File.separator + FILE_1;



    private static final String OUTPUT_FILE_1 = "outputFile1.txt";
    private static final String PATH_OUT_FILE_1 =  TEST_DIR + File.separator + OUTPUT_FILE_1;
    private static final String OUTPUT_FILE_2 = "outputFile2.txt";
    private static final String PATH_OUT_FILE_2  =  TEST_DIR + File.separator +  OUTPUT_FILE_2;
    private static final String FILE_1_CONTENT = "Content for file 1."
            + System.lineSeparator() + "HI."
            + System.lineSeparator() + "WORLD.";
    private static final String INPUT_REDIR_CHAR = "<";
    private static final String OUTPUT_REDIR_CHAR = ">";
    private static final String FILE_NOT_EXIST = "testFileNotExist.txt";
    private static final String FILENAME1 = TEST_DIR + File.separator +"IOTestFile1.txt";

    private static final String FOLDER1 = "IOTestFolder1";
    private static final String PATH_FOLDER1 = TEST_DIR + File.separator + "IOTestFolder1";

    private static ApplicationRunner appRunner;
    private static InputStream inputStream;
    private static ByteArrayOutputStream outputStream;
    private static CallCommand callCommand;
    private static ArgumentResolver argumentResolver = new ArgumentResolver();
    private static IORedirectionHandlerFactory ioRedirFact = new IORedirectionHandlerFactory();
    private static final String SHELL_EXCT_PREFIX = "shell: ";
    private static final String ECHO_CMD = "echo";
    private static final String CAT_CMD = "cat";
    private static final String RANDOM_INPUT = "123";

    public static String readFromFile(String fileName) throws IOException {
        StringBuilder result = new StringBuilder();
        try(FileReader fileReader = new FileReader(fileName);
        BufferedReader reader = new BufferedReader(fileReader)) {
            String currentLine = reader.readLine();
            while (currentLine != null) {
                result.append(currentLine);
                result.append(System.lineSeparator());
                currentLine = reader.readLine();
            }
        }

        if (result.length() == 0) {
            return "";
        } else {
            return result.toString().trim();
        }

    }

    @BeforeAll
    static void setUp() throws IOException {
        appRunner = new ApplicationRunner();
        inputStream = Mockito.mock(InputStream.class);
        if (Files.notExists(Path.of(TEST_DIR))) {
                Files.createDirectory(Path.of(TEST_DIR));
            }
        File mockFile = new File(PATH_FILE1);
        mockFile.createNewFile();
        File file = new File(PATH_FOLDER1);
        file.mkdir();
        Environment.currentDirectory = TEST_DIR;
        try(PrintWriter printWriter = new PrintWriter(FILENAME1);
        BufferedWriter writer1 = new BufferedWriter(printWriter)){
            writer1.write(FILE_1_CONTENT);
            writer1.flush();
        }


    }
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

    @AfterAll
    static void tearDown() throws IOException {
        inputStream.close();
        outputStream.close();
        Environment.currentDirectory = BASE_DIR;
        deleteDirectory(new File(TEST_DIR));


    }

    @AfterEach
    void tearDownAfterEach() throws IOException {
        inputStream.close();
        outputStream.close();
        File file1 = new File(PATH_OUT_FILE_1);
        File file2 = new File(PATH_OUT_FILE_2);
        if(file1.exists()){
            file1.delete();
        }
        if(file2.exists()){
            file2.delete();
        }



    }

    @BeforeEach
    void setUpBeforeEach() {
       outputStream = new ByteArrayOutputStream();
    }

    //test for input only

    @Test
    void callCommandEval_NoInputFileEcho_ThrowException() throws ShellException {
        List<String> argsList = List.of(ECHO_CMD, INPUT_REDIR_CHAR);
        callCommand = new CallCommand(argsList, appRunner, argumentResolver, ioRedirFact);
        assertThrows(NoSuchElementException.class, () -> callCommand.evaluate(inputStream, outputStream));
    }

    @Test
    void callCommandEval_NonExistInputFileEcho_ThrowException() throws ShellException {
        List<String> argsList = List.of(ECHO_CMD," 1", INPUT_REDIR_CHAR,FILE_NOT_EXIST);
        callCommand = new CallCommand(argsList, appRunner, argumentResolver, ioRedirFact);
        Throwable exp = assertThrows(ShellException.class, () -> callCommand.evaluate(inputStream, outputStream));
        String expected = SHELL_EXCT_PREFIX + ERR_FILE_NOT_FND;
        assertEquals(expected, exp.getMessage());
    }

    @Test
    void callCommandEval_InputFileIsDir_ThrowException() throws ShellException{
        List<String> argsList = List.of(ECHO_CMD," 1", INPUT_REDIR_CHAR,FOLDER1);
        callCommand = new CallCommand(argsList, appRunner, argumentResolver, ioRedirFact);
        Throwable exp = assertThrows(ShellException.class, () -> callCommand.evaluate(inputStream, outputStream));
        String expected = SHELL_EXCT_PREFIX + ERR_FILE_NOT_FND;
        assertEquals(expected, exp.getMessage());
    }

    @Test
    void callCommandEval_InvalidCommandIn_ThrowException() throws ShellException{
        List<String> argsList = List.of("command", INPUT_REDIR_CHAR, FILENAME1);
        callCommand = new CallCommand(argsList, appRunner, argumentResolver, ioRedirFact);
        Throwable exp = assertThrows(ShellException.class, () -> callCommand.evaluate(inputStream, outputStream));
        String expected = SHELL_EXCT_PREFIX + "command: " + ERR_INVALID_APP;
        assertEquals(expected, exp.getMessage());
    }


    @Test
    void callCommandEval_InputRedirectionCat_PrintContent() throws ShellException, IOException, AbstractApplicationException {
        List<String> argsList = List.of(CAT_CMD, INPUT_REDIR_CHAR, FILENAME1);
        callCommand = new CallCommand(argsList, appRunner, argumentResolver, ioRedirFact);
        callCommand.evaluate(inputStream, outputStream);
        String expectedOutput = FILE_1_CONTENT + System.lineSeparator();
        assertEquals(expectedOutput, outputStream.toString());
    }

    @Test
    void callCommandEval_ConsecInputRedirectionCat_ThrowException() throws ShellException {
        List<String> argsList = List.of(CAT_CMD, INPUT_REDIR_CHAR, INPUT_REDIR_CHAR, FILENAME1);
        callCommand = new CallCommand(argsList, appRunner, argumentResolver, ioRedirFact);
        Throwable exp = assertThrows(ShellException.class, () -> callCommand.evaluate(inputStream, outputStream));
        String expected = SHELL_EXCT_PREFIX + ERR_SYNTAX;
        assertEquals(expected, exp.getMessage());
    }

    @Test
    void callCommandEval_NonConsecInputRedirectionCat1_CatLast() throws ShellException, IOException, AbstractApplicationException {
        //File_1 empty , FileName1 has content so shld have cat content
        List<String> argsList = List.of(CAT_CMD, INPUT_REDIR_CHAR,PATH_FILE1, INPUT_REDIR_CHAR, FILENAME1);
        callCommand = new CallCommand(argsList, appRunner, argumentResolver, ioRedirFact);
        callCommand.evaluate(inputStream, outputStream);
        String expectedOutput = FILE_1_CONTENT + System.lineSeparator();
        assertEquals(expectedOutput, outputStream.toString());
    }

    //opposite of above so expected shld be empty
    @Test
    void callCommandEval_NonConsecInputRedirectionCat2_CatLast() throws ShellException, IOException, AbstractApplicationException {

        List<String> argsList = List.of(CAT_CMD, INPUT_REDIR_CHAR,FILENAME1, INPUT_REDIR_CHAR, PATH_FILE1);
        callCommand = new CallCommand(argsList, appRunner, argumentResolver, ioRedirFact);
        callCommand.evaluate(inputStream, outputStream);
        String expectedOutput =  "";
        assertEquals(expectedOutput, outputStream.toString());
    }


    //test for output only
    @Test
    void callCommandEval_NoOutputFileEcho_ThrowException() throws  ShellException{
        List<String> argsList = List.of(ECHO_CMD, OUTPUT_REDIR_CHAR);
        callCommand = new CallCommand(argsList, appRunner, argumentResolver, ioRedirFact);
        assertThrows(NoSuchElementException.class, () -> callCommand.evaluate(inputStream, outputStream));
    }

    @Test
    void callCommandEval_InvalidCommandOut_ThrowException() throws ShellException{
        List<String> argsList = List.of("command", OUTPUT_REDIR_CHAR, OUTPUT_FILE_2);
        callCommand = new CallCommand(argsList, appRunner, argumentResolver, ioRedirFact);
        Throwable exp = assertThrows(ShellException.class, () -> callCommand.evaluate(inputStream, outputStream));
        String expected = SHELL_EXCT_PREFIX + "command: " + ERR_INVALID_APP;
        assertEquals(expected, exp.getMessage());
    }

    @Test
    void callCommandEval_RedirectOutSingleFileEcho_RedirImmediateFileOnly() throws Exception {
        String contentToEcho = "hello world";
        List<String> argsList = List.of(ECHO_CMD, contentToEcho, OUTPUT_REDIR_CHAR,
                OUTPUT_FILE_2);
        callCommand = new CallCommand(argsList, appRunner, argumentResolver, ioRedirFact);
        callCommand.evaluate(inputStream, outputStream);
        String expectedOutput = contentToEcho ;
        String output = readFromFile(PATH_OUT_FILE_2);
        assertEquals(expectedOutput, output);

    }

    @Test
    void callCommandEval_RedirectOutMultipleFileEcho_RedirImmediateFileOnly() throws Exception {
        String contentToEcho = "hello world";
        List<String> argsList = List.of(ECHO_CMD, contentToEcho, OUTPUT_REDIR_CHAR,
               OUTPUT_FILE_2, OUTPUT_FILE_1);
        callCommand = new CallCommand(argsList, appRunner, argumentResolver, ioRedirFact);
        callCommand.evaluate(inputStream, outputStream);
        String expectedOutput = contentToEcho + " " + OUTPUT_FILE_1;
        String output = readFromFile(PATH_OUT_FILE_2);
        assertEquals(expectedOutput, output);
        // Path_output_1 just taken as echo arg so not created
        File file = new File(PATH_OUT_FILE_1);
        assertFalse(file.exists());

    }

    @Test
    void callCommandEval_ConsecOutputRedirectionEcho_ThrowException() throws ShellException {
        List<String> argsList = List.of(ECHO_CMD, OUTPUT_REDIR_CHAR, OUTPUT_REDIR_CHAR, OUTPUT_FILE_2);
        callCommand = new CallCommand(argsList, appRunner, argumentResolver, ioRedirFact);
        Throwable exp = assertThrows(ShellException.class, () -> callCommand.evaluate(inputStream, outputStream));
        String expected = SHELL_EXCT_PREFIX + ERR_SYNTAX;
        assertEquals(expected, exp.getMessage());

    }

    //both output files still created but first one is not written to
    @Test
    void callCommandEval_NonConsecOutputRedirectionEcho1_CatLast() throws ShellException, IOException, AbstractApplicationException {
        //File_1 empty , FileName1 has content so shld have cat content
        List<String> argsList = List.of(ECHO_CMD, RANDOM_INPUT, OUTPUT_REDIR_CHAR,OUTPUT_FILE_1, OUTPUT_REDIR_CHAR, OUTPUT_FILE_2);
        callCommand = new CallCommand(argsList, appRunner, argumentResolver, ioRedirFact);
        callCommand.evaluate(inputStream, outputStream);
        String output1 = readFromFile(PATH_OUT_FILE_1);
        String output2 = readFromFile(PATH_OUT_FILE_2);
        String expectedOutput1 = "" ;
        String expectedOutput2 = RANDOM_INPUT;
        assertEquals(expectedOutput1, output1 );
        assertEquals(expectedOutput2, output2 );
    }

    //opposite of above
    @Test
    void callCommandEval_NonConsecOutputRedirectionEcho2_CatLast() throws ShellException, IOException, AbstractApplicationException {
        //File_1 empty , FileName1 has content so shld have cat content
        List<String> argsList = List.of(ECHO_CMD, RANDOM_INPUT, OUTPUT_REDIR_CHAR,PATH_OUT_FILE_2, OUTPUT_REDIR_CHAR, OUTPUT_FILE_1);
        callCommand = new CallCommand(argsList, appRunner, argumentResolver, ioRedirFact);
        callCommand.evaluate(inputStream, outputStream);
        String output1 = readFromFile(PATH_OUT_FILE_1);
        String output2 = readFromFile(PATH_OUT_FILE_2);
        String expectedOutput1 = RANDOM_INPUT ;
        String expectedOutput2 = "";
        assertEquals(expectedOutput1, output1 );
        assertEquals(expectedOutput2, output2 );
    }
//Test with both input output redir

    @Test
    void callCommandEval_ConsecRedir_ThrowException() throws ShellException {
        List<String> argsList = List.of(CAT_CMD,  INPUT_REDIR_CHAR,
                OUTPUT_REDIR_CHAR, OUTPUT_FILE_1);
        callCommand = new CallCommand(argsList, appRunner, argumentResolver, ioRedirFact);
        assertThrows(ShellException.class, () -> callCommand.evaluate(inputStream, outputStream));
    }

    @Test
    void callCommandEval_ConsecRedirInOutCat_CorrectBehaviour() throws Exception {
        List<String> argsList = List.of(CAT_CMD,
                INPUT_REDIR_CHAR, FILENAME1, OUTPUT_REDIR_CHAR, OUTPUT_FILE_1);
        callCommand = new CallCommand(argsList, appRunner, argumentResolver, ioRedirFact);
        callCommand.evaluate(inputStream, outputStream);
        String outputFromFile1 = readFromFile(PATH_OUT_FILE_1);
        String expectedOutput = FILE_1_CONTENT;
        assertEquals(expectedOutput, outputFromFile1);
    }
}