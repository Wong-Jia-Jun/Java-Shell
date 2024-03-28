package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

/**
 * Parses arguments for the `ls` command.
 */
public class LsArgsParser extends ArgsParser {


    private final static char FLAG_IS_RECURSIVE = 'R';


    private final static char FLAG_IS_SORT_EXT = 'X';

    public LsArgsParser() {
        super();
        legalFlags.add(FLAG_IS_RECURSIVE);
        legalFlags.add(FLAG_IS_SORT_EXT);
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
     * Checks if the sort by extension flag is set.
     *
     * @return True if the sort by extension flag is set, false otherwise.
     */
    public Boolean isSortByExt() {
        return flags.contains(FLAG_IS_SORT_EXT);
    }

    /**
     * Gets the directories to list.
     *
     * @return A list of directory paths.
     */
    public List<String> getDirectories() {
        return nonFlagArgs;
    }
}

