package ef2test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.impl.parser.LsArgsParser;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests Functionality of ls args parser
 */
public class LsArgsParserTest {
    /**
     * The constant TEST.
     */
    public static final String TEST = "test";
    /**
     * The constant FILES.
     */
    public static final String FILES = "files";
    private LsArgsParser lsArgsParser;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        lsArgsParser = new LsArgsParser();
    }

    /**
     * Gets is recursive args.
     *
     * @return the is recursive args
     */
    static Stream<Arguments> getIsRecursiveArgs() {
        return Stream.of(
                Arguments.of((Object) new String[]{"-R"}),
                Arguments.of((Object) new String[]{"-R", "-X", FILES}),
                Arguments.of((Object) new String[]{"-R", "*"})
        );
    }

    /**
     * Gets not is recursive args.
     *
     * @return the not is recursive args
     */
    static Stream<Arguments> getNotIsRecursiveArgs() {
        return Stream.of(
                Arguments.of((Object) new String[]{"-X"}),
                Arguments.of((Object) new String[]{"-X", FILES}),
                Arguments.of((Object) new String[]{"-X", "*"}),
                Arguments.of((Object) new String[]{FILES}),
                Arguments.of((Object) new String[]{})
        );
    }

    /**
     * The type Is recursive tests.
     */
    @Nested
    class IsRecursiveTests {
        /**
         * Is recursive contains r flag returns true.
         *
         * @param args the args
         * @throws InvalidArgsException the invalid args exception
         */
        @ParameterizedTest
        @MethodSource("ef2test.LsArgsParserTest#getIsRecursiveArgs")
        void isRecursive_ContainsRFlag_ReturnsTrue(String... args) throws InvalidArgsException {
            lsArgsParser.parse(args);
            assertTrue(lsArgsParser.isRecursive());
        }

        /**
         * Is recursive does not contain r flag returns false.
         *
         * @param args the args
         * @throws InvalidArgsException the invalid args exception
         */
        @ParameterizedTest
        @MethodSource("ef2test.LsArgsParserTest#getNotIsRecursiveArgs")
        void isRecursive_DoesNotContainRFlag_ReturnsFalse(String... args) throws InvalidArgsException {
            lsArgsParser.parse(args);
            assertFalse(lsArgsParser.isRecursive());
        }
    }

    /**
     * Gets is sort by ext args.
     *
     * @return the is sort by ext args
     */
    static Stream<Arguments> getIsSortByExtArgs() {
        return Stream.of(
                Arguments.of((Object) new String[]{"-X"}),
                Arguments.of((Object) new String[]{"-X", FILES}),
                Arguments.of((Object) new String[]{"-X", "*"})
        );
    }

    /**
     * Gets not is sort by ext args.
     *
     * @return the not is sort by ext args
     */
    static Stream<Arguments> getNotIsSortByExtArgs() {
        return Stream.of(
                Arguments.of((Object) new String[]{"-R"}),
                Arguments.of((Object) new String[]{"-R", "-X", FILES}),
                Arguments.of((Object) new String[]{"-R", "*"}),
                Arguments.of((Object) new String[]{FILES}),
                Arguments.of((Object) new String[]{})
        );
    }

    /**
     * The type Is sort by ext tests.
     */
    @Nested
    class IsSortByExtTests {
        /**
         * Is sort by ext contains x flag returns true.
         *
         * @throws InvalidArgsException the invalid args exception
         */
        @ParameterizedTest
        @MethodSource("ef2test.LsArgsParserTest#getIsSortByExtArgs")
        void isSortByExt_ContainsXFlag_ReturnsTrue() throws InvalidArgsException {
            String[] args = {"-X"};
            lsArgsParser.parse(args);
            assertTrue(lsArgsParser.isSortByExt());
        }

        /**
         * Is sort by ext does not contain x flag returns false.
         *
         * @throws InvalidArgsException the invalid args exception
         */
        @ParameterizedTest
        @MethodSource("ef2test.LsArgsParserTest#getNotIsSortByExtArgs")
        void isSortByExt_DoesNotContainXFlag_ReturnsFalse() throws InvalidArgsException {
            String[] args = {"-R"};
            lsArgsParser.parse(args);
            assertFalse(lsArgsParser.isSortByExt());
        }
    }

    /**
     * Gets directories no args returns empty list.
     *
     * @throws InvalidArgsException the invalid args exception
     */
    @Test
    void getDirectories_NoArgs_ReturnsEmptyList() throws InvalidArgsException {
        String[] args = {};
        lsArgsParser.parse(args);
        assertTrue(lsArgsParser.getDirectories().isEmpty());
    }

    /**
     * Gets has files args.
     *
     * @return the has files args
     */
    static Stream<Arguments> getHasFilesArgs() {
        return Stream.of(
                Arguments.of((Object) new String[]{"-X", TEST}),
                Arguments.of((Object) new String[]{"-X", "-R", TEST}),
                Arguments.of((Object) new String[]{TEST})
        );
    }

    /**
     * Gets directories has files returns list.
     *
     * @param args the args
     * @throws InvalidArgsException the invalid args exception
     */
    @ParameterizedTest
    @MethodSource("ef2test.LsArgsParserTest#getHasFilesArgs")
    void getDirectories_HasFiles_ReturnsList(String... args) throws InvalidArgsException {
        lsArgsParser.parse(args);
        assertEquals(Arrays.asList(TEST), lsArgsParser.getDirectories());
    }
}
