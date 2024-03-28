package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedOutputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.EchoException;


/**
 * Tests Functionality of echo application
 */
class EchoApplicationTest {
    private static final String[] EMPTY = {""};
    private static final String[] SPACE = {" "};
    private static final String[] STRING_1 = {"ABC", "123"};
    private static final String[] STRING_2 = {"0", "1", "abc"};
    private static final String[] STRING_3 = {"!@#$%^&*()_+{}|:<>?.,/~"};
    private static final String[] STRING_4 = null;
    private static final String[] STRING_5 = {"'Travel time Singapore -> Paris is 13h and 15`'"};
    private static final String ECHO_PREFIX = "echo: ";
    private EchoApplication app;

    /**
     * Sets .
     */
    @BeforeEach
    void setup() {
        app = new EchoApplication();
    }

    /**
     * Construct result blank return blank.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
// echoApplication Class takes in string objects already so quoting does not matter, processing happens before it passed to this
    @Test
    public void constructResult_Blank_ReturnBlank() throws AbstractApplicationException {

        assertEquals(STRING_NEWLINE , app.constructResult(EMPTY));
        assertEquals(" " + System.lineSeparator() , app.constructResult(SPACE));

    }

    /**
     * Construct result alphanumeric without double quotes return respective.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    public void constructResult_AlphanumericWithoutDoubleQuotes_ReturnRespective() throws AbstractApplicationException {

        assertEquals("ABC 123" + System.lineSeparator(), app.constructResult(STRING_1));
        assertEquals("0 1 abc" + System.lineSeparator(), app.constructResult(STRING_2));

    }

    /**
     * Construct result special characters should write as is.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    public void constructResult_SpecialCharacters_ShouldWriteAsIs() throws AbstractApplicationException {

        assertEquals("!@#$%^&*()_+{}|:<>?.,/~" + System.lineSeparator(), app.constructResult(STRING_3));

    }


    /**
     * Construct result null throws exception.
     */
    @Test
    public void constructResult_Null_ThrowsException() {
        Throwable exp = assertThrows(EchoException.class, () -> app.constructResult(STRING_4));
        assertEquals(ECHO_PREFIX + ERR_NULL_ARGS, exp.getMessage());
    }

    /**
     * Run null output stream throws exception.
     */
    @Test
    public void run_NullOutputStream_ThrowsException() {
        Throwable error = assertThrows(EchoException.class, () -> app.run(STRING_1, System.in, null));
        assertEquals(ECHO_PREFIX + ERR_NULL_STREAMS, error.getMessage());
    }
    /**
     * Run null input stream throws exception.
     */
    @Test
    public void run_NullInputStream_ThrowsException() {
        Throwable error = assertThrows(EchoException.class, () -> app.run(STRING_1, null, System.out));
        assertEquals(ECHO_PREFIX + ERR_NULL_STREAMS, error.getMessage());
    }

    /**
     * Run runtime io exception throws exception.
     *
     * @throws IOException the io exception
     */
    @Test
    public void run_RuntimeIOException_ThrowsException() throws IOException {
        try (OutputStream out = new PipedOutputStream()) {
            out.close();
            Throwable error = assertThrows(EchoException.class, () -> app.run(STRING_1, System.in, out));
            assertEquals(ECHO_PREFIX + ERR_IO_EXCEPTION, error.getMessage());
        }
    }

    /**
     * Run double quoted disable special char.
     *
     * @throws AbstractApplicationException the abstract application exception
     */
    @Test
    public void run_DoubleQuoted_DisableSpecialChar() throws AbstractApplicationException {
            OutputStream out = new ByteArrayOutputStream();
            app.run( STRING_5, System.in, out);
            assertEquals("'Travel time Singapore -> Paris is 13h and 15`'" + System.lineSeparator(), out.toString());
        }
    }

