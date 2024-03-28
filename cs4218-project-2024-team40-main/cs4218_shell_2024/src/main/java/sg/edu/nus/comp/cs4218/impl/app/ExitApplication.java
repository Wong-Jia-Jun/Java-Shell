package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.ExitInterface;

import java.io.InputStream;
import java.io.OutputStream;
/**
 * Implementation of the {@link ExitInterface} for exiting shell
 * This class provides methods to exit the shell or terminate program
 */
public class ExitApplication implements ExitInterface {
    /**
     * Runs the exit application.
     *
     * @param args   Array of arguments for the application, not used.
     * @param stdin  An InputStream, not used.
     * @param stdout An OutputStream, not used.
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) {
        // Format: exit
        terminateExecution();
    }

    /**
     * Terminate shell.
     */
    @Override
    public void terminateExecution() {
        System.exit(0);
    }
}
