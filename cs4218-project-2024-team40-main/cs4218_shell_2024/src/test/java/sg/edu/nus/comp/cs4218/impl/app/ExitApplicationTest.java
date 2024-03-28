package sg.edu.nus.comp.cs4218.impl.app;

import com.ginsberg.junit.exit.ExpectSystemExit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests Functionality of exit application.
 * java.lang.System is not mocked as it is a core part of the Java runtime environment.
 */
public class ExitApplicationTest {
    private ExitApplication exitApplication;
    @BeforeEach
    void setup() {
        exitApplication = new ExitApplication();
    }

    /**
     * Run exit command system exit.
     */
    @Test
    @ExpectSystemExit
    void run_ExitCommand_SystemExit() {
        String[] args = new String[0];
        exitApplication.run(args, System.in, System.out);
    }

    /**
     * Terminate execution exit command system exit.
     */
    @Test
    @ExpectSystemExit
    void terminateExecution_ExitCommand_SystemExit() {
        exitApplication.terminateExecution();
    }
}
