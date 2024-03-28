package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

/**
 * Parses arguments for the `rm` command.
 */
public class RmArgsParser extends ArgsParser {

    public static final char FLAG_IS_RECURSIVE = 'r';

    public static final char FLAG_IS_EMPTY_DIR = 'd';

    public RmArgsParser() {
        super();
        legalFlags.add(FLAG_IS_RECURSIVE);
        legalFlags.add(FLAG_IS_EMPTY_DIR);
    }

    /**
     * Checks if the recursive flag is set.
     *
     * @return True if the recursive flag is set, false otherwise.
     */
    public Boolean isRecursive() {
        return flags.contains(FLAG_IS_RECURSIVE);
    }

    /**
     * Checks if the empty folder flag is set.
     *
     * @return True if the empty folder flag is set, false otherwise.
     */
    public Boolean isEmptyFolder() {
        return flags.contains(FLAG_IS_EMPTY_DIR);
    }

    /**
     * Gets the list of file names to remove.
     *
     * @return A list of file names.
     */
    public List<String> getFileNames() {
        return nonFlagArgs;
    }
}

