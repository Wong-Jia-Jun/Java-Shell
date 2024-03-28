package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.SortException;
import sg.edu.nus.comp.cs4218.impl.parser.SortArgsParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FND;


/**
 * Integration Tests of Functionality of sort application
 */
public class SortApplicationIT { //NOPMD - suppressed ClassNamingConventions - As per Teaching Team's guidelines

    private SortApplication sortApp;
    private ByteArrayOutputStream outputStream;
    private static final String APPLE = "apple";
    private static final String BANANA = "banana";
    private static final String CARROT = "carrot";

    /**
     * The Temp directory.
     */
    @TempDir
    Path tempDirectory;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        sortApp = new SortApplication(new SortArgsParser());
        outputStream = new ByteArrayOutputStream();
    }

    /**
     * Tear down.
     *
     * @throws IOException the io exception
     */
    @AfterEach
    void tearDown() throws IOException {
        outputStream.close();
    }

    /**
     * Run sort from files sorts content correctly.
     *
     * @throws Exception the exception
     */
    @Test
    void run_SortFromFiles_SortsContentCorrectly() throws Exception {
        Path file1 = tempDirectory.resolve("test1.txt");
        Files.writeString(file1, BANANA + System.lineSeparator() + APPLE + System.lineSeparator() + CARROT);

        String[] args = {file1.toString()};
        sortApp.run(args, System.in, outputStream);
        assertEquals(APPLE + System.lineSeparator() + BANANA + System.lineSeparator() + CARROT
                + System.lineSeparator(), outputStream.toString());
    }

    /**
     * Run sort from stdin sorts content correctly.
     *
     * @throws Exception the exception
     */
    @Test
    void run_SortFromStdin_SortsContentCorrectly() throws Exception {
        String inputContent = BANANA + System.lineSeparator() + APPLE + System.lineSeparator() + CARROT;
        InputStream inputStream = new java.io.ByteArrayInputStream(inputContent.getBytes());

        String[] args = {};
        sortApp.run(args, inputStream, outputStream);
        assertEquals(APPLE + System.lineSeparator() + BANANA + System.lineSeparator() + CARROT
                + System.lineSeparator(), outputStream.toString());
    }

    /**
     * Sort files and stdin invalid file string returns exception text. (Invalid for java `Path.resolve`)
     * @throws AbstractApplicationException
     */
    @Test
    void sortFromFiles_InvalidFileString_ReturnsExceptionText() {
        String invalidFile = "invalidFile:";
        Throwable exp = assertThrows(SortException.class, () -> sortApp.sortFromFiles(false, false, false, invalidFile));
        assertEquals(new SortException(invalidFile, ERR_FILE_NOT_FND).getMessage(), exp.getMessage());
    }

}
