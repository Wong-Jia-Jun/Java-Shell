package sg.edu.nus.comp.cs4218.impl.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Application;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.app.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_APP;

public class ApplicationRunnerIT { //NOPMD - suppressed ClassNamingConventions - As per Teaching Team's guidelines
    private final static String APP_LS = "ls";
    private final static String APP_WC = "wc";
    private final static String APP_ECHO = "echo";
    private final static String APP_GREP = "grep";
    private final static String APP_CD = "cd";
    private final static String APP_CAT = "cat";
    private final static String APP_CUT = "cut";
    private final static String APP_MKDIR = "mkdir";
    private final static String APP_TEE = "tee";
    private static final String APP_SORT = "sort";
    private final static String APP_MV = "mv";
    private final static String APP_PASTE = "paste";
    private final static String APP_RM = "rm";
    private final static String APP_UNIQ = "uniq";
    private final static String APP_INVALID = "INVALIDAPP";
    private final static String STDIN_INPUT = "test";
    private final static String CD_ARGS = ".";
    private final static String[] CUT_ARGS = new String[]{"-c", "1"};
    private final static String INVALID_MSG = "shell: INVALIDAPP: ";
    private final static String DIR_NAME = "mkDirAppRunnerTestDir";
    private final static String FILE_1_NAME = "file1.txt";
    private final static String FILE_2_NAME = "file2.txt";
    private Path directoryPath;
    private ApplicationRunner applicationRunner;
    private InputStream inputStream;
    private OutputStream outputStream;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() throws IOException{
        applicationRunner = new ApplicationRunner();
        inputStream = mock(InputStream.class);
        outputStream = mock(OutputStream.class);
        directoryPath = Paths.get(DIR_NAME);
        Files.createFile(Paths.get(FILE_1_NAME));
        Files.createFile(Paths.get(FILE_2_NAME));
    }

    /**
     * Tear down.
     *
     * @throws IOException the io exception
     */
    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(directoryPath);
        Files.deleteIfExists(Paths.get(FILE_1_NAME));
        Files.deleteIfExists(Paths.get(FILE_2_NAME));

    }

    /**
     * This test method checks if running the ls application returns an instance of LsApplication.
     */
    @Test
    void runApp_LsApp_RunsLsApplication() throws AbstractApplicationException, ShellException {
        Application application = applicationRunner.runApp(APP_LS, new String[]{}, inputStream, outputStream);
        assertEquals(LsApplication.class, application.getClass());
    }

    /**
     * This test function checks if the WcApplication runs successfully when executed by the
     * application runner.
     */
    @Test
    void runApp_WcApp_RunsWcApplication() throws AbstractApplicationException, ShellException {
        inputStream = new ByteArrayInputStream(STDIN_INPUT.getBytes());
        Application application = applicationRunner.runApp(APP_WC, new String[]{}, inputStream, outputStream);
        assertEquals(WcApplication.class, application.getClass());
    }

    /**
     * This test function checks if the EchoApplication runs successfully using the applicationRunner.
     */
    @Test
    void runApp_EchoApp_RunsEchoApplication() throws AbstractApplicationException, ShellException {
        Application application = applicationRunner.runApp(APP_ECHO, new String[]{}, inputStream, outputStream);
        assertEquals(EchoApplication.class, application.getClass());
    }

    /**
     * This test function checks if the Grep application runs successfully.
     */
    @Test
    void runApp_GrepApp_RunsGrepApplication() throws AbstractApplicationException, ShellException {
        inputStream = new ByteArrayInputStream(STDIN_INPUT.getBytes());
        Application application = applicationRunner.runApp(APP_GREP, new String[]{STDIN_INPUT}, inputStream, outputStream);
        assertEquals(GrepApplication.class, application.getClass());
    }

    /**
     * This test function checks if running the Cd application through the application runner results
     * in the CdApplication class being instantiated.
     */
    @Test
    void runApp_CdApp_RunsCdApplication() throws AbstractApplicationException, ShellException {
        Application application = applicationRunner.runApp(APP_CD, new String[]{CD_ARGS}, inputStream, outputStream);
        assertEquals(CdApplication.class, application.getClass());
    }

   /**
    * This test function verifies that running the Cat application in a Java program results in the
    * CatApplication class being instantiated.
    */
    @Test
    void runApp_CatApp_RunsCatApplication() throws AbstractApplicationException, ShellException {
        inputStream = new ByteArrayInputStream(STDIN_INPUT.getBytes());
        Application application = applicationRunner.runApp(APP_CAT, new String[]{}, inputStream, outputStream);
        assertEquals(CatApplication.class, application.getClass());
    }

    /**
     * This test function verifies that running the "cut" application results in the correct
     * application class being instantiated.
     */
    @Test
    void runApp_CutApp_RunsCutApplication() throws AbstractApplicationException, ShellException {
        inputStream = new ByteArrayInputStream(STDIN_INPUT.getBytes());
        Application application = applicationRunner.runApp(APP_CUT, CUT_ARGS, inputStream, outputStream);
        assertEquals(CutApplication.class, application.getClass());
    }

    /**
     * This test function checks if running the Mkdir application creates a MkdirApplication instance.
     */
    @Test
    void runApp_MkdirApp_RunsMkdirApplication() throws AbstractApplicationException, ShellException {
        Application application = applicationRunner.runApp(APP_MKDIR, new String[]{DIR_NAME}, inputStream, outputStream);
        assertEquals(MkdirApplication.class, application.getClass());
    }

    /**
     * This test function checks if running the Tee application using the application runner results in
     * the TeeApplication class being instantiated.
     */
    @Test
    void runApp_TeeApp_RunsTeeApplication() throws AbstractApplicationException, ShellException {
        inputStream = new ByteArrayInputStream(STDIN_INPUT.getBytes());
        Application application = applicationRunner.runApp(APP_TEE, new String[]{}, inputStream, outputStream);
        assertEquals(TeeApplication.class, application.getClass());
    }

   /**
    * This test function checks if the SortApplication runs successfully when executed.
    */
    @Test
    void runApp_SortApp_RunsSortApplication() throws AbstractApplicationException, ShellException {
        inputStream = new ByteArrayInputStream(STDIN_INPUT.getBytes());
        Application application = applicationRunner.runApp(APP_SORT, new String[]{}, inputStream, outputStream);
        assertEquals(SortApplication.class, application.getClass());
    }

    /**
     * This test function checks if running the "rm" application returns an instance of the
     * RmApplication class.
     */
    @Test
    void runApp_RmApp_RunsRmApplication() throws AbstractApplicationException, ShellException {
        Application application = applicationRunner.runApp(APP_RM, new String[]{DIR_NAME}, inputStream, outputStream);
        assertEquals(RmApplication.class, application.getClass());
    }

    /**
     * This test function checks if running the "uniq" application returns an instance of the
     * RmApplication class.
     */
    @Test
    void runApp_UniqApp_RunsUniqApplication() throws AbstractApplicationException, ShellException {
        Application application = applicationRunner.runApp(APP_UNIQ, new String[]{FILE_1_NAME, FILE_2_NAME}, inputStream, outputStream);
        assertEquals(UniqApplication.class, application.getClass());
    }


    /**
     * This test function checks if running the "mv" application returns an instance of the
     * MvApplication class.
     */
    @Test
    void runApp_MvApp_RunsMvApplication() throws AbstractApplicationException, ShellException {
        Application application = applicationRunner.runApp(APP_MV, new String[]{DIR_NAME, DIR_NAME}, inputStream, outputStream);
        assertEquals(MvApplication.class, application.getClass());
    }

    /**
     * This test function checks if running the "paste" application returns an instance of the
     * PasteApplication class.
     */
    @Test
    void runApp_PasteApp_RunsPasteApplication() throws AbstractApplicationException, ShellException {
        inputStream = new ByteArrayInputStream(STDIN_INPUT.getBytes());
        Application application = applicationRunner.runApp(APP_PASTE, new String[]{}, inputStream, outputStream);
        assertEquals(PasteApplication.class, application.getClass());
    }

    /**
     * This test method checks if running an invalid application throws a ShellException with the
     * expected error message.
     */
    @Test
    void runApp_InvalidApp_ThrowsShellException() {
        Throwable thrown = assertThrows(ShellException.class, () -> {
            applicationRunner.runApp(APP_INVALID, new String[]{}, inputStream, outputStream);
        });
        assertEquals(INVALID_MSG + ERR_INVALID_APP, thrown.getMessage());
    }
}
