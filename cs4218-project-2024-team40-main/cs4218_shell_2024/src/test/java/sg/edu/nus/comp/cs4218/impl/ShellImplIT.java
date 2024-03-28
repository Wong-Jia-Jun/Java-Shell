package sg.edu.nus.comp.cs4218.impl;

import com.ginsberg.junit.exit.ExpectSystemExit;
import org.junit.jupiter.api.*;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.ShellException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_APP;
import static sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil.setCurrentDirectory;

/**
 * Integration tests for functionality of the {@link ShellImpl} class
 */
class ShellImplIT { //NOPMD - suppressed ClassNamingConventions - As per teaching team's guidelines
    public static final String EXIT = "exit\n";
    public static final String APP = "app";
    public static final String CAT = "cat";
    public static final String ECHO = "echo";
    public static final String CUT = "cut";
    public static final String SRC = "src/test/resources";
    private static final String HELLO = "Hello\n";
    private static final String NEW_LINE = "\n";
    private static final String FROM = "From\n";
    private static final String CAT_INPUT = "CatTest.txt\n";
    private static final String SYMBOLS = "£И€𐍈\n";
    public static final String CAT_APP_TEST = "CatApplicationTest";
    public static final String DIR = "dir";
    public static final String CRLF_TXT = "catTestCrlf.txt";
    public static final String SHELL_IMPL = "ShellImplTest";
    private ShellImpl shell;
    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;
    private PipedOutputStream pipedOut;
    private ByteArrayOutputStream testOutContent;
    private static String srcDirectory;

    @BeforeAll
    static void createEnvironment() {
        srcDirectory = String.copyValueOf(Environment.currentDirectory.toCharArray());
    }

    /**
     * Sets up testing environment.
     *
     * @throws IOException the io exception
     */
    @BeforeEach
    void setUp() throws IOException, InterruptedException {
    	pipedOut = new PipedOutputStream();
        PipedInputStream pipedIn = new PipedInputStream(pipedOut); //NOPMD - suppressed CloseResource - resource used in testing
    	System.setIn(pipedIn);
    	testOutContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(testOutContent));
        shell = new ShellImpl();

        Thread shellThread = new Thread(() -> {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                shell.run(reader);
            } catch (Exception e) {
                System.out.println("Error in shell: " + e.getMessage());
            }
        });

        shellThread.start();

        // Clears previous output
        writeToShell(NEW_LINE);
        testOutContent.reset();
    }
    
    private void writeToShell(String command) throws IOException, InterruptedException {
    	pipedOut.write(command.getBytes());
        pipedOut.flush();

        // Add some form of synchronization/wait here to allow the shell to process the command
        Thread.sleep(100);
    }

    private String normalizeLineEndings(String input) {
        return input.replace("\r\n", NEW_LINE);
    }

    /**
     * Restore streams and original test environment.
     */
    @AfterEach
    void restoreStreams() throws NoSuchFieldException, IllegalAccessException {
        System.setOut(originalOut); // Restore System.out
        System.setIn(originalIn); // Restore System.in
        setCurrentDirectory(srcDirectory); // Restore original directory
    }

    /**
     * Run invalid command should print invalid command to stdout and continue.
     *
     * @throws IOException          the io exception
     * @throws InterruptedException the interrupted exception
     */
    @Test
    @ExpectSystemExit
    void run_InvalidCommand_ShouldPrintInvalidCommandToStdout() throws IOException, InterruptedException {
    	writeToShell("invalidCommand\n");
        String output = normalizeLineEndings(testOutContent.toString().trim());
        assertEquals(new ShellException("invalidCommand" + ": " + ERR_INVALID_APP).getMessage() + NEW_LINE
        		+ Environment.currentDirectory + ">", output);
        writeToShell(EXIT);
    }
    //CatCommands
    /**
     * Run cat command should print expected output.
     *
     * @throws IOException          the io exception
     * @throws InterruptedException the interrupted exception
     */
    @Test
    @ExpectSystemExit
    void run_CatCommand_ShouldPrintExpectedOutput() throws IOException, InterruptedException {
        String testFileDir = Paths.get(SRC, APP, CAT_APP_TEST, DIR, CRLF_TXT)
                .toAbsolutePath().toString();
        String expectedContent = HELLO
                + NEW_LINE
                + FROM
                + CAT_INPUT
                + SYMBOLS;
        writeToShell(String.format("cat %s\n", testFileDir));
        String output = normalizeLineEndings(testOutContent.toString().trim());
        assertEquals(expectedContent
                + Environment.currentDirectory + ">", output);
        writeToShell(EXIT);
    }
    /**
     * Run cat and cut command integrate should print expected output.
     *
     * @throws IOException          the io exception
     * @throws InterruptedException the interrupted exception
     */
    @Test
    @ExpectSystemExit
    void run_CatCutCommand_ShouldPrintExpectedOutput() throws IOException, InterruptedException {
        String testFileDir = Paths.get(SRC, APP, CAT_APP_TEST, DIR, CRLF_TXT)
                .toAbsolutePath().toString();
        String expectedContent = "He\n"
                + NEW_LINE
                + "Fr\n"
                + "Ca\n"
                + "£И\n";
        writeToShell(String.format("cat %s | cut -c 1,2\n", testFileDir));
        String output = normalizeLineEndings(testOutContent.toString().trim());
        assertEquals(expectedContent
                + Environment.currentDirectory + ">", output);
        writeToShell(EXIT);
    }
    /**
     * Run cat and cut command integrate should print expected output.
     *
     * @throws IOException          the io exception
     * @throws InterruptedException the interrupted exception
     */
    @Test
    @ExpectSystemExit
    void run_CutCatCommand_ShouldPrintExpectedOutput() throws IOException, InterruptedException {
        String testFileDir = Paths.get(SRC, APP, CAT_APP_TEST, DIR, CRLF_TXT)
                .toAbsolutePath().toString();
        String expectedContent = "He\n"
                + NEW_LINE
                + "Fr\n"
                + "Ca\n"
                + "£И\n";
        writeToShell(String.format("cut -c 1,2 %s | cat\n", testFileDir));
        String output = normalizeLineEndings(testOutContent.toString().trim());
        assertEquals(expectedContent
                + Environment.currentDirectory + ">", output);
        writeToShell(EXIT);
    }
    /**
     * Run cat and sort command integrate should print expected output.
     *
     * @throws IOException          the io exception
     * @throws InterruptedException the interrupted exception
     */
    @Test
    @ExpectSystemExit
    void run_CatSortCommand_ShouldPrintExpectedOutput() throws IOException, InterruptedException {
        String testFileDir = Paths.get(SRC, APP, CAT_APP_TEST, DIR, CRLF_TXT)
                .toAbsolutePath().toString();
        String expectedContent = CAT_INPUT
                + FROM
                + HELLO
                + SYMBOLS;
        writeToShell(String.format("cat %s | sort\n", testFileDir));
        String output = normalizeLineEndings(testOutContent.toString().trim());
        assertEquals(expectedContent
                + Environment.currentDirectory + ">", output);
        writeToShell(EXIT);
    }
    /**
     * Run cat and tee command integrate should print expected output.
     *
     * @throws IOException          the io exception
     * @throws InterruptedException the interrupted exception
     */
    @Test
    @ExpectSystemExit
    void run_CatTeeCommand_ShouldPrintExpectedOutput() throws IOException, InterruptedException {
        String testFileDir = Paths.get(SRC, APP, CAT_APP_TEST, DIR, CRLF_TXT)
                .toAbsolutePath().toString();
        String expectedContent = HELLO
                + NEW_LINE
                + FROM
                + CAT_INPUT
                + SYMBOLS;
        writeToShell(String.format("cat %s | tee output.txt \n", testFileDir));
        String output = normalizeLineEndings(testOutContent.toString().trim());
        String fileContent = normalizeLineEndings(String.join(System.lineSeparator(),Files.readAllLines(Path.of(srcDirectory, "output.txt"))));
        assertEquals(expectedContent
                + Environment.currentDirectory + ">", output);
        assertEquals(expectedContent.trim(), fileContent);
        writeToShell(EXIT);
    }

    /**
     * Run cd command should print expected output.
     *
     * @throws IOException          the io exception
     * @throws InterruptedException the interrupted exception
     */
    @Test
    @ExpectSystemExit
    void run_CdCommand_ShouldPrintExpectedOutput() throws IOException, InterruptedException {
        String testFileDir = Paths.get(SRC,APP, "CdApplicationTest")
                .toAbsolutePath().toString();

        writeToShell(String.format("cd %s\n", testFileDir));
        String output = normalizeLineEndings(testOutContent.toString().trim());
        assertEquals(Environment.currentDirectory + ">", output);
        writeToShell(EXIT);
    }

    /**
     * Run echo command should print expected output.
     *
     * @throws IOException          the io exception
     * @throws InterruptedException the interrupted exception
     */
    @Test
    @ExpectSystemExit
    void run_EchoCommand_ShouldPrintExpectedOutput() throws IOException, InterruptedException {
    	writeToShell("echo 'Hello World'\n");
        String output = normalizeLineEndings(testOutContent.toString().trim());
        assertEquals("Hello World\n"
                + Environment.currentDirectory + ">", output);
        writeToShell(EXIT);
    }

    /**
     * Run ls command using pipe should print expected output.
     *
     * @throws IOException          the io exception
     * @throws InterruptedException the interrupted exception
     */
    @Test
    @ExpectSystemExit
    void run_LsCommand_ShouldPrintExpectedOutput() throws IOException, InterruptedException {
        writeToShell("ls ./src\n");
        String output = normalizeLineEndings(testOutContent.toString().trim());
        assertEquals( "src:\nmain\ntest\n" + Environment.currentDirectory + ">", output);
        writeToShell(EXIT);
    }
    /**
     * Run rm with Cd command  should delete created file
     *
     * @throws IOException          the io exception
     * @throws InterruptedException the interrupted exception
     */
    @Test
    @ExpectSystemExit
    void run_RmCdCommand_ShouldPrintExpectedOutput() throws IOException, InterruptedException {
        Path testFile = Path.of(SRC,SHELL_IMPL, "rm.txt");
        Path testDir = Path.of(SRC,SHELL_IMPL);
        Files.deleteIfExists(testFile);
        Files.createFile(testFile);
        assertTrue(Files.exists(testFile));
        writeToShell(String.format("cd %s ; rm %s \n", testDir, "rm.txt"));
        assertTrue(!Files.exists(testFile));
        writeToShell(EXIT);
    }

    /**
     * Run ls and grep command using pipe should print expected output.
     *
     * @throws IOException          the io exception
     * @throws InterruptedException the interrupted exception
     */
    @Test
    @ExpectSystemExit
    void run_PipeLsIntoGrep_ShouldPrintExpectedOutput() throws IOException, InterruptedException {
        String testFileDir = Paths.get(SRC, APP, "LsApplicationTest")
                .toAbsolutePath().toString();
        String expectedContent = "valid\n" + "valid1\n" + "validFile.txt\n";
        writeToShell(String.format("ls %s | grep 'valid'\n", testFileDir));
        String output = normalizeLineEndings(testOutContent.toString().trim());
        assertEquals( expectedContent
                + Environment.currentDirectory + ">", output);
        writeToShell(EXIT);
    }

    /**
     * Run ls and grep command using pipe should print expected output.
     *
     * @throws IOException          the io exception
     * @throws InterruptedException the interrupted exception
     */
    @Test
    @ExpectSystemExit
    void run_PipeCatIntoWc_ShouldPrintExpectedOutput() throws IOException, InterruptedException {
        String testFileDir = Paths.get(SRC, APP, "WcApplicationTest")
                .toAbsolutePath().toString();
        File inputFile = new File(testFileDir + "/long_sample_text.txt");
        String expectedContent = "100\n";
        writeToShell(String.format("cat %s | wc -w\n", inputFile));
        String output = normalizeLineEndings(testOutContent.toString().trim());
        assertEquals( expectedContent
                + Environment.currentDirectory + ">", output);
        writeToShell(EXIT);
    }

    /**
     * Run cat command with redirect should print expected output.
     *
     * @throws IOException          the io exception
     * @throws InterruptedException the interrupted exception
     */
    @Test
    @ExpectSystemExit
    void run_RedirectFileIntoCat_ShouldPrintExpectedOutput() throws IOException, InterruptedException {
        String testFileDir = Paths.get(SRC,SHELL_IMPL)
                .toAbsolutePath().toString();
        String expectedContent = "Content for file 1.\n" + "HI.\n" + "WORLD.\n";
        writeToShell(String.format("cat < %s/IOTestFile1.txt\n", testFileDir));
        String output = normalizeLineEndings(testOutContent.toString().trim());
        assertEquals( expectedContent
                + Environment.currentDirectory + ">", output);
        writeToShell(EXIT);
    }

    /**
     * Run cat and grep command using pipe and redirect should print expected output.
     *
     * @throws IOException          the io exception
     * @throws InterruptedException the interrupted exception
     */
    @Test
    @ExpectSystemExit
    void run_PipeCatRedirectGrep_ShouldPrintExpectedOutput() throws IOException, InterruptedException {
        String testFileDir = Paths.get(SRC, APP, "WcApplicationTest")
                .toAbsolutePath().toString();
        String outputDir = Paths.get(SRC,  SHELL_IMPL)
                .toAbsolutePath().toString();
        File inputFile = new File(testFileDir + "/medium_sample_text.txt");
        File filteredOutput = new File(outputDir + "/filteredOutput.txt");

        // Clear file
        try (FileWriter writer = new FileWriter(filteredOutput)) {
            writer.write("");
        }

        writeToShell(String.format("cat %s | grep 'the' > %s\n", inputFile, filteredOutput));
        String output = normalizeLineEndings(testOutContent.toString().trim());

        List<String> result = Files.readAllLines(filteredOutput.toPath());
        List<String> expectedResult = Files.readAllLines(inputFile.toPath());
        // Verify only the second and third line was copied
        assertEquals(2, result.size());
        assertEquals(result.get(0), expectedResult.get(1));
        assertEquals(result.get(1), expectedResult.get(2));
        assertEquals(Environment.currentDirectory + ">", output);

        writeToShell(EXIT);
    }

    /**
     * Run echo, cat, cd, grep, ls and wc commands.
     * Uses semicolon, command substitution, piping, redirecting and globbing.
     * Print expected output.
     *
     * @throws IOException          the io exception
     * @throws InterruptedException the interrupted exception
     */
    @Test
    @ExpectSystemExit
    void run_AllShellSpecifications_ShouldPrintExpectedOutput() throws IOException, InterruptedException {
        String testFileDir = Paths.get(SRC, APP, "WcApplicationTest")
                .toAbsolutePath().toString();
        String outputDir = Paths.get(SRC,  SHELL_IMPL)
                .toAbsolutePath().toString();
        File filteredOutput = new File(outputDir + "/filteredOutput.txt");

        writeToShell(String.format("cd %s; wc -w `ls *` > %s;" +
                        " echo \"The word count for all the texts is `cat %s | grep 'total'`\" \n",
                testFileDir, filteredOutput, filteredOutput));

        String output = normalizeLineEndings(testOutContent.toString().trim());
        List<String> result = Files.readAllLines(filteredOutput.toPath());
        assertEquals("The word count for all the texts is \t120 total\n" +
                Environment.currentDirectory + ">", output);
        // Note +1 is due to the additional "... total" in the output
        assertEquals(Files.list(Path.of(testFileDir)).count() + 1, result.size());
        writeToShell(EXIT);
    }

    /**
     * Run multiple application and one has error should print error and output and continue.
     *
     * @throws IOException          the io exception
     * @throws InterruptedException the interrupted exception
     */
    @Test
    @ExpectSystemExit
    void run_MultipleApplicationAndOneHasError_ShouldPrintErrorAndContinue() throws IOException, InterruptedException {
    	writeToShell("invalidCommand\n");
        writeToShell("ls ./src\n");
        String output = normalizeLineEndings(testOutContent.toString().trim());
        assertEquals( new ShellException("invalidCommand" + ": " + ERR_INVALID_APP).getMessage() + NEW_LINE +
                Environment.currentDirectory + "> src:\nmain\ntest\n" + Environment.currentDirectory + ">", output);
        writeToShell(EXIT);
    }
}
