package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

/**
 * Parses arguments for the `mv` command.
 */
public class MvArgsParser extends ArgsParser {

    /**
     * Flag indicating to count bytes.
     */
    public static final char FLAG_AVOID_OVERWR = 'n';

    /**
     * Constructs a new `WcArgsParser` object and sets up legal flags.
     */
    public MvArgsParser() {
        super();
        legalFlags.add(FLAG_AVOID_OVERWR);
    }

    /**
     * Checks if byte count is requested.
     *
     * @return True if byte count is requested, false otherwise.
     */
    public Boolean isOverwrite() {
        return !flags.contains(FLAG_AVOID_OVERWR);
    }

    /**
     * Return nonFlagArgs which are arguements that are not flag related after being parsed.
     * @return boolean whether is Append option present
     */
    public List<String> getFileNames() {
        return nonFlagArgs;
    }
}
