package ef2test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.impl.parser.UniqArgsParser;
import sg.edu.nus.comp.cs4218.impl.parser.WcArgsParser;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Tests Functionality of uniqArgsParser
 */
public class UniqArgsParserTest {
    private UniqArgsParser uniqArgsParser;
    private static final String FILE_1 = "file1.txt";
    private static final String FILE_2 = "file2.txt";

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        uniqArgsParser = new UniqArgsParser();
    }

    /**
     * Is methods no options with file return all false.
     *
     * @throws InvalidArgsException the invalid args exception
     */
    @Test
    void isMethods_NoOptionsWithFile_ReturnAllFalse() throws InvalidArgsException {
        String[] args = {FILE_1};
        uniqArgsParser.parse(args);
        assertFalse(uniqArgsParser.isCount());
        assertFalse(uniqArgsParser.isRepeated());
        assertFalse(uniqArgsParser.isAllRepeated());
    }

    /**
     * Gets files names no options with file correct non flags.
     *
     * @throws InvalidArgsException the invalid args exception
     */
    @Test
    void getFilesNames_NoOptionsWithFile_CorrectNonFlags() throws InvalidArgsException {
        String[] args = {FILE_1};
        List<String> expected = Arrays.asList(args);
        uniqArgsParser.parse(args);
        assertEquals(expected,uniqArgsParser.getFileNames());
    }

    /**
     * Is methods count options with file only count return true.
     *
     * @throws InvalidArgsException the invalid args exception
     */
    @Test
    void isMethods_CountOptionsWithFile_OnlyCountReturnTrue() throws InvalidArgsException {
        String[] args = {"-c", FILE_1};
        uniqArgsParser.parse(args);
        assertTrue(uniqArgsParser.isCount());
        assertFalse(uniqArgsParser.isRepeated());
        assertFalse(uniqArgsParser.isAllRepeated());
    }

    /**
     * Gets file names count options with file correct non flags.
     *
     * @throws InvalidArgsException the invalid args exception
     */
    @Test
    void getFileNames_CountOptionsWithFile_CorrectNonFlags() throws InvalidArgsException {
        String[] args = {"-c", FILE_1};
        uniqArgsParser.parse(args);
        assertEquals(List.of(FILE_1),uniqArgsParser.getFileNames());
    }

    /**
     * Is methods duplicate options with file only duplicate return true.
     *
     * @throws InvalidArgsException the invalid args exception
     */
    @Test
    void isMethods_DuplicateOptionsWithFile_OnlyDuplicateReturnTrue() throws InvalidArgsException {
        String[] args = {"-d",FILE_1};
        uniqArgsParser.parse(args);
        assertFalse(uniqArgsParser.isCount());
        assertTrue(uniqArgsParser.isRepeated());
        assertFalse(uniqArgsParser.isAllRepeated());
    }

    /**
     * Gets files names duplicate options with file correct non flags.
     *
     * @throws InvalidArgsException the invalid args exception
     */
    @Test
    void getFilesNames_DuplicateOptionsWithFile_CorrectNonFlags() throws InvalidArgsException {
        String[] args = {"-d",FILE_1};
        List<String> expected = new ArrayList<>();
        expected.add(FILE_1);
        uniqArgsParser.parse(args);
        assertEquals(expected,uniqArgsParser.getFileNames());
    }

    /**
     * Is methods all duplicate options with file only all duplicate return true.
     *
     * @throws InvalidArgsException the invalid args exception
     */
    @Test
    void isMethods_AllDuplicateOptionsWithFile_OnlyAllDuplicateReturnTrue() throws InvalidArgsException {
        String[] args = {"-D",FILE_1};
        uniqArgsParser.parse(args);
        assertFalse(uniqArgsParser.isCount());
        assertFalse(uniqArgsParser.isRepeated());
        assertTrue(uniqArgsParser.isAllRepeated());
    }

    /**
     * Gets file names all duplicate options with file correct non flags.
     *
     * @throws InvalidArgsException the invalid args exception
     */
    @Test
    void getFileNames_AllDuplicateOptionsWithFile_CorrectNonFlags() throws InvalidArgsException {
        String[] args = {"-D",FILE_1};
        uniqArgsParser.parse(args);
        assertEquals(List.of(FILE_1), uniqArgsParser.getFileNames());
    }

    /**
     * Is methods all options with file all return true.
     *
     * @throws InvalidArgsException the invalid args exception
     */
    @Test
    void isMethods_AllOptionsWithFile_AllReturnTrue() throws InvalidArgsException {
        String[] args = {"-c", "-d", "-D",FILE_1};
        uniqArgsParser.parse(args);
        assertTrue(uniqArgsParser.isCount());
        assertTrue(uniqArgsParser.isRepeated());
        assertTrue(uniqArgsParser.isAllRepeated());
    }

    /**
     * Gets file names all options with file correct non f lag.
     *
     * @throws InvalidArgsException the invalid args exception
     */
    @Test
    void getFileNames_AllOptionsWithFile_CorrectNonFLag() throws InvalidArgsException {
        String[] args = {"-c", "-d", "-D",FILE_1};
        uniqArgsParser.parse(args);
        assertEquals(List.of(FILE_1), uniqArgsParser.getFileNames());
    }


    /**
     * Is methods count option no files only count true.
     *
     * @throws InvalidArgsException the invalid args exception
     */
    @Test
    void isMethods_CountOptionNoFiles_OnlyCountTrue() throws InvalidArgsException {
        String[] args = {"-c"};
        uniqArgsParser.parse(args);
        assertTrue(uniqArgsParser.isCount());
        assertFalse(uniqArgsParser.isRepeated());
        assertFalse(uniqArgsParser.isAllRepeated());
    }

    /**
     * Gets file names count option no files correct non f lags.
     *
     * @throws InvalidArgsException the invalid args exception
     */
    @Test
    void getFileNames_CountOptionNoFiles_CorrectNonFLags() throws InvalidArgsException {
        String[] args = {"-c"};
        List<String> expected = new ArrayList<>();
        uniqArgsParser.parse(args);
        assertEquals(expected, uniqArgsParser.getFileNames());
    }

    /**
     * Is methods count option file stdin only count true.
     *
     * @throws InvalidArgsException the invalid args exception
     */
    @Test
    void isMethods_CountOptionFileStdin_OnlyCountTrue() throws InvalidArgsException {
        String[] args = {"-c", "-", FILE_1};
        uniqArgsParser.parse(args);
        assertTrue(uniqArgsParser.isCount());
        assertFalse(uniqArgsParser.isRepeated());
        assertFalse(uniqArgsParser.isAllRepeated());
    }

    /**
     * Gets file names count option file stdin correct non flags args.
     *
     * @throws InvalidArgsException the invalid args exception
     */
    @Test
    void getFileNames_CountOptionFileStdin_CorrectNonFLags() throws InvalidArgsException {
        String[] args = {"-c","-", FILE_1};
        uniqArgsParser.parse(args);
        assertEquals(List.of("-", FILE_1), uniqArgsParser.getFileNames());
    }
    /**
     * Gets input file from non flag arg which has only 1 arg
     *
     * @throws InvalidArgsException the invalid args exception
     */
    @Test
    void getInputFile_OneFileinNonFlag_ReturnInputFile() throws InvalidArgsException {
        String[] args = {FILE_1};
        uniqArgsParser.parse(args);
        assertEquals(FILE_1, uniqArgsParser.getInputFile());
    }
    /**
     * Gets input file from non flag arg which is empty
     *
     * @throws InvalidArgsException the invalid args exception
     */
    @Test
    void getInputFile_EmptyNonFlag_ReturnNull() throws InvalidArgsException {
        String[] args = {};
        uniqArgsParser.parse(args);
        assertEquals(null, uniqArgsParser.getInputFile());
    }
    /**
     * Gets input file from non flag arg which has 2 args
     *
     * @throws InvalidArgsException the invalid args exception
     */
    @Test
    void getInputFile_TwoFileNonFlag_ReturnInputFile() throws InvalidArgsException {
        String[] args = {FILE_1, FILE_2};
        uniqArgsParser.parse(args);
        assertEquals(FILE_1, uniqArgsParser.getInputFile());
    }

    /**
     * Gets input file from non flag arg which has only 1 arg
     *
     * @throws InvalidArgsException the invalid args exception
     */
    @Test
    void getOutputFile_OneFileinNonFlag_ReturnNull() throws InvalidArgsException {
        String[] args = {FILE_1};
        uniqArgsParser.parse(args);
        assertEquals(null, uniqArgsParser.getOutputFile());
    }
    /**
     * Gets input file from non flag arg which is empty
     *
     * @throws InvalidArgsException the invalid args exception
     */
    @Test
    void getOutputFile_EmptyNonFlag_ReturnNull() throws InvalidArgsException {
        String[] args = {};
        uniqArgsParser.parse(args);
        assertEquals(null, uniqArgsParser.getOutputFile());
    }
    /**
     * Gets input file from non flag arg which has 2 args
     *
     * @throws InvalidArgsException the invalid args exception
     */
    @Test
    void getOutputFile_TwoFileNonFlag_ReturnOutputFile() throws InvalidArgsException {
        String[] args = {FILE_1, FILE_2};
        uniqArgsParser.parse(args);
        assertEquals(FILE_2, uniqArgsParser.getOutputFile());
    }
}
