package sg.edu.nus.comp.cs4218.impl.util;

import java.io.File;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The type String utils test.
 */
public class StringUtilsTest {

    /**
     * The constant OS_NAME.
     */
    public static final String OS_NAME = "os.name";
    /**
     * The constant JAVA.
     */
    public static final String JAVA = "Java";

    /**
     * File separator windows returns windows file separator.
     */
    @Test
    void fileSeparator_Windows_ReturnsWindowsFileSeparator() {
        String osName = System.getProperty(OS_NAME);
        System.setProperty(OS_NAME, "Windows 11");
        assertEquals("\\" + File.separator, StringUtils.fileSeparator());
        System.setProperty(OS_NAME, osName);
    }

    /**
     * File separator linux returns linux file separator.
     */
    @Test
    void fileSeparator_Linux_ReturnsLinuxFileSeparator() {
        String osName = System.getProperty(OS_NAME);
        System.setProperty(OS_NAME, "Linux");
        assertEquals(File.separator, StringUtils.fileSeparator());
        System.setProperty(OS_NAME, osName);
    }

    /**
     * Is blank blank strings returns true.
     *
     * @param str the str
     */
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"", "   "})
    void isBlank_BlankStrings_ReturnsTrue(String str) {
        assertEquals(true, StringUtils.isBlank(str));
    }

    /**
     * Is blank non blank strings returns false.
     *
     * @param str the str
     */
    @ParameterizedTest
    @ValueSource(strings = {"a", "  a", "a  ", "  a  "})
    void isBlank_NonBlankStrings_ReturnsFalse(String str) {
        assertEquals(false, StringUtils.isBlank(str));
    }

    /**
     * Multiply char positive number returns string.
     *
     * @param num the num
     */
    @ParameterizedTest
    @ValueSource(ints = {1, 5, 1000})
    void multiplyChar_PositiveNumber_ReturnsString(int num) {
        char character = 'a';
        StringBuilder stringBuilder = new StringBuilder(num);
        for (int i = 0; i < num; i++) {
            stringBuilder.append(character);
        }
        String expected = stringBuilder.toString();

        assertEquals(expected, StringUtils.multiplyChar(character, num));
    }

    /**
     * Multiply char less than or equal zero returns empty string.
     *
     * @param num the num
     */
    @ParameterizedTest
    @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    void multiplyChar_LessThanOrEqualZero_ReturnsEmptyString(int num) {
        char character = 'a';
        assertEquals("", StringUtils.multiplyChar(character, num));
    }

    /**
     * Multiply char non printable character returns correct string.
     */
    @Test
    void multiplyChar_NonPrintableCharacter_ReturnsCorrectString() {
        assertEquals("\n\n", StringUtils.multiplyChar('\n', 2));
    }

    /**
     * Tokenize with normal string returns tokens.
     */
    @Test
    public void tokenize_WithNormalString_ReturnsTokens() {
        String input = "Hello world from Java";
        String[] expected = {"Hello", "world", "from", JAVA};
        assertArrayEquals(expected, StringUtils.tokenize(input));
    }

    /**
     * Tokenize with extra spaces returns tokens.
     */
    @Test
    public void tokenize_WithExtraSpaces_ReturnsTokens() {
        String input = "  Hello   world  from  Java  ";
        String[] expected = {"Hello", "world", "from", JAVA};
        assertArrayEquals(expected, StringUtils.tokenize(input));
    }

    /**
     * Tokenize with single word returns single token.
     */
    @Test
    public void tokenize_WithSingleWord_ReturnsSingleToken() {
        String input = JAVA;
        String[] expected = {JAVA};
        assertArrayEquals(expected, StringUtils.tokenize(input));
    }

    /**
     * Tokenize empty string returns empty array.
     */
    @Test
    public void tokenize_EmptyString_ReturnsEmptyArray() {
        String input = "";
        String[] expected = {};
        assertArrayEquals(expected, StringUtils.tokenize(input));
    }

    /**
     * Tokenize null string returns empty array.
     */
    @Test
    public void tokenize_NullString_ReturnsEmptyArray() {
        String input = null;
        String[] expected = {};
        assertArrayEquals(expected, StringUtils.tokenize(input));
    }

    /**
     * Tokenize string with only spaces returns empty array.
     */
    @Test
    public void tokenize_StringWithOnlySpaces_ReturnsEmptyArray() {
        String input = "     ";
        String[] expected = {};
        assertArrayEquals(expected, StringUtils.tokenize(input));
    }

    /**
     * Tokenize string with tabs and newlines returns tokens.
     */
    @Test
    public void tokenize_StringWithTabsAndNewlines_ReturnsTokens() {
        String input = "\tHello\nworld\r\nfrom\nJava\t";
        String[] expected = {"Hello", "world", "from", JAVA};
        assertArrayEquals(expected, StringUtils.tokenize(input));
    }

    /**
     * Is number positive integer returns true.
     */
    @Test
    void isNumber_PositiveInteger_ReturnsTrue() {
        assertTrue(StringUtils.isNumber("123"));
    }

    /**
     * Is number negative integer returns true.
     */
    @Test
    void isNumber_NegativeInteger_ReturnsTrue() {
        assertTrue(StringUtils.isNumber("-456"));
    }

    /**
     * Is number large integer returns true.
     */
    @Test
    void isNumber_LargeInteger_ReturnsTrue() {
        assertTrue(StringUtils.isNumber("1234567890123456789012345678901234567890"));
    }

    /**
     * Is number zero returns true.
     */
    @Test
    void isNumber_Zero_ReturnsTrue() {
        assertTrue(StringUtils.isNumber("0"));
    }

    /**
     * Is number positive decimal returns false.
     */
    @Test
    void isNumber_PositiveDecimal_ReturnsFalse() {
        assertFalse(StringUtils.isNumber("123.45"));
    }

    /**
     * Is number negative decimal returns false.
     */
    @Test
    void isNumber_NegativeDecimal_ReturnsFalse() {
        assertFalse(StringUtils.isNumber("-123.45"));
    }

    /**
     * Is number alphabetic string returns false.
     */
    @Test
    void isNumber_AlphabeticString_ReturnsFalse() {
        assertFalse(StringUtils.isNumber("abc"));
    }

    /**
     * Is number alphanumeric string returns false.
     */
    @Test
    void isNumber_AlphanumericString_ReturnsFalse() {
        assertFalse(StringUtils.isNumber("123abc456"));
    }

    /**
     * Is number empty string returns false.
     */
    @Test
    void isNumber_EmptyString_ReturnsFalse() {
        assertFalse(StringUtils.isNumber(""));
    }

    /**
     * Is number with spaces returns false.
     */
    @Test
    void isNumber_WithSpaces_ReturnsFalse() {
        assertFalse(StringUtils.isNumber("  "));
    }

    /**
     * Is number with null returns false.
     */
    @Test
    void isNumber_WithNull_ReturnsFalse() {
        assertFalse(StringUtils.isNumber(null));
    }

    /**
     * Is number only plus minus signs returns false.
     */
    @Test
    void isNumber_OnlyPlusMinusSigns_ReturnsFalse() {
        assertFalse(StringUtils.isNumber("-"));
        assertFalse(StringUtils.isNumber("+"));
    }

    /**
     * Is number number containing spaces returns false.
     */
    @Test
    void isNumber_NumberContainingSpaces_ReturnsFalse() {
        assertFalse(StringUtils.isNumber("123 456"));
    }

    /**
     * Is number special characters returns false.
     */
    @Test
    void isNumber_SpecialCharacters_ReturnsFalse() {
        assertFalse(StringUtils.isNumber("$%^&"));
    }
}
