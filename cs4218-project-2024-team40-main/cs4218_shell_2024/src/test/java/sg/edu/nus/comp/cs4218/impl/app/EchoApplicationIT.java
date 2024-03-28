package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.*;
import sg.edu.nus.comp.cs4218.exception.EchoException;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration Tests for EchoApplication.
 */
public class EchoApplicationIT { //NOPMD -suppressed ClassNamingConventions - As per Teaching Team's guidelines
    public static final String HELLO = "Hello";

    private EchoApplication echoApplication;
    private ByteArrayOutputStream outputStream;

    @BeforeEach
    void setUp() {
        echoApplication = new EchoApplication();
        outputStream = new ByteArrayOutputStream();
    }

    @Test
    void run_EmptyString_ResultsInNewLine() throws Exception {
        String[] args = {""};
        echoApplication.run(args, System.in, outputStream);
        assertEquals(System.lineSeparator(), outputStream.toString());
    }

    @Test
    void run_SingleWord_PrintsWord() throws Exception {
        String[] args = {HELLO};
        echoApplication.run(args, System.in, outputStream);
        assertEquals(HELLO + System.lineSeparator(), outputStream.toString());
    }

    @Test
    void run_MultipleWords_PrintsWordsSeparatedBySpaces() throws Exception {
        String[] args = {HELLO, "World!"};
        echoApplication.run(args, System.in, outputStream);
        assertEquals("Hello World!" + System.lineSeparator(), outputStream.toString());
    }

    @Test
    void run_SpecialCharacters_HandledCorrectly() throws Exception {
        String[] args = {"!@#$%", "^&*()"};
        echoApplication.run(args, System.in, outputStream);
        assertEquals("!@#$% ^&*()" + System.lineSeparator(), outputStream.toString());
    }

    @Test
    void run_WithNoArgs_ResultsInOnlyNewLine() throws Exception {
        String[] args = {};
        echoApplication.run(args, System.in, outputStream);
        assertEquals(System.lineSeparator(), outputStream.toString());
    }

    @Test
    void run_WithNullArgs_ThrowsException() {
        String[] args = null;
        EchoException exception = assertThrows(EchoException.class, () ->
                echoApplication.run(args, System.in, outputStream));
        assertEquals("echo: Null arguments" , exception.getMessage());
    }

    @Test
    void run_WithNullOutputStream_ThrowsException() {
        String[] args = {HELLO};
        EchoException exception = assertThrows(EchoException.class, () ->
                echoApplication.run(args, System.in, null));
        assertEquals("echo: Null Pointer Exception" , exception.getMessage());
    }

    @Test
    void run_QuotesAndSpecialCharacters_Success() throws Exception {
        String[] args = {"\"Double Quotes\"", "'Single Quotes'", "\\", "\nNewline", "\tTab"};
        echoApplication.run(args, System.in, outputStream);
        String expectedOutput = "\"Double Quotes\" 'Single Quotes' \\ \nNewline \tTab" + System.lineSeparator();
        assertEquals(expectedOutput, outputStream.toString());
    }

    @Test
    void run_EnvironmentVariables_Success() throws Exception {
        String[] args = {"Path:", "$PATH"};
        echoApplication.run(args, System.in, outputStream);
        assertTrue(outputStream.toString().contains("Path: $PATH" + System.lineSeparator()));
    }

    @Test
    void run_LargeAmountOfData_Works() throws Exception {
        StringBuilder largeString = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            largeString.append("data");
            if (i < 9999) {
                largeString.append(' ');
            }
        }
        String[] args = {largeString.toString()};
        echoApplication.run(args, System.in, outputStream);
        assertEquals(largeString + System.lineSeparator(), outputStream.toString());
    }

    @Test
    void run_ToFileAndReadBack_Success() throws Exception {
        String[] args = {"Echo", "this", "to", "a", "file"};
        File tempFile = File.createTempFile("echoTest", ".txt");
        tempFile.deleteOnExit();
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            echoApplication.run(args, System.in, fos);
        }

        StringBuilder fileContent = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(tempFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                fileContent.append(line);
            }
        }

        assertEquals("Echo this to a file", fileContent.toString());
    }

    @Test
    void run_WithInputStream_Success() throws Exception {
        String input = "This input should be ignored";
        InputStream stdin = new ByteArrayInputStream(input.getBytes());
        String[] args = {"Echo", "from", "args"};
        echoApplication.run(args, stdin, outputStream);
        assertEquals("Echo from args" + System.lineSeparator(), outputStream.toString());
    }

    @Test
    void run_WithEscapedCharacters_Success() throws Exception {
        String[] args = {"Line1\\nLine2", "Tab\\tSeparated"};
        echoApplication.run(args, System.in, outputStream);
        assertEquals("Line1\\nLine2 Tab\\tSeparated" + System.lineSeparator(), outputStream.toString());
    }

}
