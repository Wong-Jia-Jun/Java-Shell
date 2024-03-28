package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

/**
 * Parses arguments for the `wc` command.
 */
public class WcArgsParser extends ArgsParser {

    /**
     * Flag indicating to count bytes.
     */
    public static final char FLAG_IS_BYTE = 'c';

    /**
     * Flag indicating to count lines.
     */
    public static final char FLAG_IS_LINE = 'l';

    /**
     * Flag indicating to count words.
     */
    public static final char FLAG_IS_WORD = 'w';

    /**
     * Constructs a new `WcArgsParser` object and sets up legal flags.
     */
    public WcArgsParser() {
        super();
        legalFlags.add(FLAG_IS_BYTE);
        legalFlags.add(FLAG_IS_LINE);
        legalFlags.add(FLAG_IS_WORD);
    }

    /**
     * Checks if byte count is requested.
     *
     * @return True if byte count is requested, false otherwise.
     */
    public Boolean isByteCount() {
        return flags.contains(FLAG_IS_BYTE);
    }

    /**
     * Checks if line count is requested.
     *
     * @return True if line count is requested, false otherwise.
     */
    public Boolean isLineCount() {
        return flags.contains(FLAG_IS_LINE);
    }

    /**
     * Checks if word count is requested.
     *
     * @return True if word count is requested, false otherwise.
     */
    public Boolean isWordCount() {
        return flags.contains(FLAG_IS_WORD);
    }
    /**
     * Return nonFlagArgs which are arguements that are not flag related after being parsed.
     * @return boolean whether is Append option present
     */
    public List<String> getFileNames() {
        return nonFlagArgs;
    }
}
