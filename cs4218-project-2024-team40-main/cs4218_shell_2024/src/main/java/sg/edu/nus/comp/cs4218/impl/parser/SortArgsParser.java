package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;
/**
 * Parses arguments for the `sort` command.
 */
public class SortArgsParser extends ArgsParser {


    public static final char FLAG_IS_FIRST_NUM = 'n';

    public static final char FLAG_IS_REV_ORDER = 'r';

    public static final char FLAG_IS_NO_CASE = 'f';

    /**
     * Constructs a new `SortArgsParser` object and sets up legal flags.
     */
    public SortArgsParser() {
        super();
        legalFlags.add(FLAG_IS_FIRST_NUM);
        legalFlags.add(FLAG_IS_REV_ORDER);
        legalFlags.add(FLAG_IS_NO_CASE);
    }

    /**
     * Checks if the first word of each line should be treated as a number.
     *
     * @return True if the first word of each line should be treated as a number, false otherwise.
     */
    public Boolean isFirstWordNumber() {
        return flags.contains(FLAG_IS_FIRST_NUM);
    }

    /**
     * Checks if sorting should be done in reverse order.
     *
     * @return True if sorting should be done in reverse order, false otherwise.
     */
    public Boolean isReverseOrder() {
        return flags.contains(FLAG_IS_REV_ORDER);
    }

    /**
     * Checks if case-insensitive sorting should be performed.
     *
     * @return True if case-insensitive sorting should be performed, false otherwise.
     */
    public boolean isCaseIndependent() {
        return flags.contains(FLAG_IS_NO_CASE);
    }

    /**
     * Checks if sorting is to be performed from standard input.
     *
     * @return True if sorting is to be performed from standard input, false otherwise.
     */
    public boolean isSortFromStdin() {
        return nonFlagArgs.isEmpty();
    }

    /**
     * Checks if sorting is to be performed from files.
     *
     * @return True if sorting is to be performed from files, false otherwise.
     */
    public boolean isSortFromFiles() {
        return !nonFlagArgs.isEmpty();
    }

    /**
     * Gets the list of file names to sort.
     *
     * @return A list of file names.
     */
    public List<String> getFileNames() {
        return nonFlagArgs;
    }
}
    