package sg.edu.nus.comp.cs4218.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ginsberg.junit.exit.ExpectSystemExit;

import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;

import java.io.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_APP;

/**
 * The type Shell impl test.
 */
class ShellImplTest {
    /**
     * The constant EXIT.
     */
    public static final String EXIT = "exit\n";
    /**
     * The constant COMMAND_SUCCESS.
     */
    public static final String COMMAND_SUCCESS = "> Command executed successfully\n";
    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;
    private ShellImplMock shell;
    private PipedOutputStream pipedOut;
    private ByteArrayOutputStream testOutContent;

    /**
     * The type Shell impl mock.
     */
    static class ShellImplMock extends ShellImpl {
    	private boolean shouldFailStub = false;
        private boolean cmdThrowExcept = false;

        /**
         * Configure to throw exception.
         */
        public void configureToThrowException() {
            cmdThrowExcept = true;
        }

        /**
         * Configure to not throw exception.
         */
        public void configureToNotThrowException() {
            cmdThrowExcept = false;
        }

        /**
         * Configure to execute without success stub.
         */
        public void configureToExecuteWithoutSuccessStub() {
        	shouldFailStub = true;
        }

        @Override
        public void parseAndEvaluate(String commandString, OutputStream stdout)
                throws AbstractApplicationException, ShellException, FileNotFoundException {
        	if (shouldFailStub) {
        		super.parseAndEvaluate(commandString, stdout);
        		return;
        	}
        	
            if (cmdThrowExcept) {
                throw new ApplicationExceptionStub("Application exception");
            } else {
                Command successfulCommand = new SuccessfulCommandStub();
                successfulCommand.evaluate(System.in, stdout);
            }
        }
    }

    /**
     * The type Successful command stub.
     */
    static class SuccessfulCommandStub implements Command {
        @Override
        public void evaluate(InputStream stdin, OutputStream stdout) {
            // Simulate doing something successful
            String successMessage = "Command executed successfully\n";
            try {
                stdout.write(successMessage.getBytes());
            } catch (IOException e) {
                // Handle unexpected IOException
            }
        }

        @Override
        public void terminate() {
            // Do nothing
        }
    }

    /**
     * The type Application exception stub.
     */
    static class ApplicationExceptionStub extends AbstractApplicationException {
        /**
         * Instantiates a new Application exception stub.
         *
         * @param message the message
         */
        public ApplicationExceptionStub(String message) {
            super(message);
        }
    }

    /**
     * Sets up.
     *
     * @throws IOException the io exception
     */
    @BeforeEach
    void setUp() throws IOException {
    	pipedOut = new PipedOutputStream();
        PipedInputStream pipedIn = new PipedInputStream(pipedOut); //NOPMD - suppressed CloseResource - resource used in testing
    	System.setIn(pipedIn);
    	testOutContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(testOutContent));
        shell = new ShellImplMock();
        shell.configureToNotThrowException();
        
        Thread shellThread = new Thread(() -> {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                shell.run(reader);
            } catch (Exception e) {
                System.out.println("Error in shell: " + e.getMessage());
            }
        });

        shellThread.start();
    }
    
    private void writeToShell(String command) throws IOException, InterruptedException {
    	pipedOut.write(command.getBytes());
        pipedOut.flush();

        // Add some form of synchronization/wait here to allow the shell to process the command
        Thread.sleep(100);
    }
    
    private String normalizeLineEndings(String input) {
        return input.replace("\r\n", "\n");
    }

    /**
     * Restore streams.
     */
    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut); // Restore System.out
        System.setIn(originalIn); // Restore System.in
    }

    /**
     * Run invalid command should print invalid command to stdout and continue.
     *
     * @throws IOException          the io exception
     * @throws InterruptedException the interrupted exception
     */
    @Test
    @ExpectSystemExit
    void run_InvalidCommand_ShouldPrintInvalidCommandToStdoutAndContinue()
    		throws IOException, InterruptedException {
    	shell.configureToExecuteWithoutSuccessStub();
    	writeToShell("invalidCommand\n");

        // Now check the output
        String output = normalizeLineEndings(testOutContent.toString().trim());
        assertEquals(Environment.currentDirectory + "> "
                + new ShellException("invalidCommand" + ": " + ERR_INVALID_APP).getMessage() + "\n"
        		+ Environment.currentDirectory + ">", output);
        writeToShell(EXIT);
    }

    /**
     * Run one successful command should print success command output.
     *
     * @throws IOException          the io exception
     * @throws InterruptedException the interrupted exception
     */
    @Test
    @ExpectSystemExit
    void run_OneSuccessfulCommand_ShouldPrintSuccessCommandOutput()
    		throws IOException, InterruptedException {
    	writeToShell("successfulCommand\n");
        String output = normalizeLineEndings(testOutContent.toString().trim());
        assertEquals(Environment.currentDirectory + COMMAND_SUCCESS
                + Environment.currentDirectory + ">", output);
        shell.configureToExecuteWithoutSuccessStub();
        writeToShell(EXIT);
    }

    /**
     * Run multiple successful command should print success command outputs.
     *
     * @throws IOException          the io exception
     * @throws InterruptedException the interrupted exception
     */
    @Test
    @ExpectSystemExit
    void run_MultipleSuccessfulCommand_ShouldPrintSuccessCommandOutputs()
    		throws IOException, InterruptedException {
    	writeToShell("successfulCommand\n");
    	writeToShell("successfulCommand\n");
        String output = normalizeLineEndings(testOutContent.toString().trim());
        assertEquals(Environment.currentDirectory + COMMAND_SUCCESS
                + Environment.currentDirectory + COMMAND_SUCCESS
                + Environment.currentDirectory + ">", output);
        shell.configureToExecuteWithoutSuccessStub();
        writeToShell(EXIT);
    }

    /**
     * Run one application and has error should print error and continue.
     *
     * @throws IOException          the io exception
     * @throws InterruptedException the interrupted exception
     */
    @Test
    @ExpectSystemExit
    void run_OneApplicationAndHasError_ShouldPrintErrorAndContinue()
    		throws IOException, InterruptedException {
    	shell.configureToThrowException();
    	writeToShell("exceptionCommand\n");
        String output = normalizeLineEndings(testOutContent.toString().trim());
        assertEquals(Environment.currentDirectory + "> Application exception\n"
                + Environment.currentDirectory + ">", output);
        shell.configureToExecuteWithoutSuccessStub();
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
    void run_MultipleApplicationAndOneHasError_ShouldPrintErrorAndOutputAndContinue()
    		throws IOException, InterruptedException {
    	shell.configureToThrowException();
    	writeToShell("exceptionCommand\n");
        shell.configureToNotThrowException();
        writeToShell("successCommand\n");
        String output = normalizeLineEndings(testOutContent.toString().trim());
        assertEquals(Environment.currentDirectory + "> Application exception\n"
                + Environment.currentDirectory + COMMAND_SUCCESS
                + Environment.currentDirectory + ">", output);
        shell.configureToExecuteWithoutSuccessStub();
        writeToShell(EXIT);
    }
}
