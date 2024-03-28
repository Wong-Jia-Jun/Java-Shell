package tdd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CutException;
import sg.edu.nus.comp.cs4218.impl.app.CutApplication;
import sg.edu.nus.comp.cs4218.impl.parser.CutArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.RangeHelperFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FND;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

public class CutApplicationPublicIT { //NOPMD
    public static final String CHAR_FLAG = "-c";
    public static final String BYTE_FLAG = "-b";
    public static final String TEST_RANGE = "1-3";
    private static final String TEST_STRING = "hello world";
    private static final String RES_FULL = "hel";
    private static final String SPLIT_STR_1 = "hello";
    private static final String SPLIT_STR_2 = "world";
    private static final String RES_SPT_1 = "hel";
    private static final String RES_SPT_2 = "wor";
    private static final String INVALID_FILE = "invalidFile";


    CutApplication cutApplication;

    private String joinStringsByLineSeparator(String... strs) {
        return String.join(STRING_NEWLINE, strs);
    }

    private InputStream generateInputStreamFromStrings(String... strs) {
        return new ByteArrayInputStream(joinStringsByLineSeparator(strs).getBytes(StandardCharsets.UTF_8));
    }

    @BeforeEach
    public void setUp() {
        cutApplication = new CutApplication(new CutArgsParser(), new RangeHelperFactory());
    }


    @Test
    void run_SingleLineByCharRange_ReturnCutByLine() throws Exception {
        String[] argList = new String[]{CHAR_FLAG, TEST_RANGE};
        InputStream stdin = generateInputStreamFromStrings(TEST_STRING); //NOPMD
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        cutApplication.run(argList, stdin, output);
        assertEquals(RES_FULL + STRING_NEWLINE, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleLineByByteRange_ReturnCutByByte() throws Exception {
        String[] argList = new String[]{BYTE_FLAG, TEST_RANGE};
        InputStream stdin = generateInputStreamFromStrings(TEST_STRING); //NOPMD
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        cutApplication.run(argList, stdin, output);
        assertEquals(RES_FULL + STRING_NEWLINE, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_MultipleLinesByCharRange_ReturnCutContentAtEachLineByByte() throws Exception {
        String[] argList = new String[]{CHAR_FLAG, TEST_RANGE};
        InputStream stdin = generateInputStreamFromStrings(SPLIT_STR_1, SPLIT_STR_2); //NOPMD
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        cutApplication.run(argList, stdin, output);
        assertEquals(RES_SPT_1 + STRING_NEWLINE + RES_SPT_2 + STRING_NEWLINE, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_MultipleLinesByByteRange_ReturnCutContentAtEachLineByByte() throws Exception {
        String[] argList = new String[]{BYTE_FLAG, TEST_RANGE};
        InputStream stdin = generateInputStreamFromStrings(SPLIT_STR_1, SPLIT_STR_2); //NOPMD
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        cutApplication.run(argList, stdin, output);
        assertEquals(RES_SPT_1 + STRING_NEWLINE + RES_SPT_2 + STRING_NEWLINE, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_NotFoundFile_PrintsException() throws AbstractApplicationException {
        String[] argList = new String[]{BYTE_FLAG, TEST_RANGE, INVALID_FILE};
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        cutApplication.run(argList, System.in, output);
        assertEquals(new CutException(ERR_FILE_NOT_FND, INVALID_FILE).getMessage() + STRING_NEWLINE, output.toString(StandardCharsets.UTF_8));
    }

}
