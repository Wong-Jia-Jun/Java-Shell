package sg.edu.nus.comp.cs4218.impl.parser;

import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
/**
 * Parses arguments for the `cut` command.
 */
public class CutArgsParser extends ArgsParser {

    public static final char FLAG_CUT_BY_CHAR = 'c';


    public static final char FLAG_CUT_BY_BYTE = 'b';

    public static final String ILLEGAL_MSS_FLAG = "Either -c or -b flag must be present";


    public static final String ILLEGAL_BOTH_FLAG = "Cannot use both -c and -b flags together";


    public static final String ILLEGAL_RANGE_MSG = "Invalid byte/character position";

    public static final String ILLEGAL_ONE_INDEX = " byte/character positions are numbered from 1";

    public static final String ILLEGAL_DCS_RANGE = "Invalid decreasing range";

    public CutArgsParser() {
        super();
        legalFlags.add(FLAG_CUT_BY_CHAR);
        legalFlags.add(FLAG_CUT_BY_BYTE);
    }

    /**
     * Checks if cutting by character is specified.
     *
     * @return True if the `-c` flag is present, false otherwise.
     */
    public Boolean isCutByChar() {
        return flags.contains(FLAG_CUT_BY_CHAR);
    }

    /**
     * Checks if cutting by byte is specified.
     *
     * @return True if the `-b` flag is present, false otherwise.
     */
    public Boolean isCutByByte() {
        return flags.contains(FLAG_CUT_BY_BYTE);
    }

    /**
     * Checks if standard input is to be cut.
     *
     * @return True if only one argument is provided, false otherwise.
     */
    public Boolean isCutStdin() {
        return nonFlagArgs.size() == 1;
    }

    /**
     * Checks if files are to be cut.
     *
     * @return True if more than one argument is provided, false otherwise.
     */
    public Boolean isCutFiles() {
        return nonFlagArgs.size() != 1;
    }

    /**
     * Gets the file names to be cut.
     *
     * @return An array of file names.
     */
    public String[] getFiles() {
        String[] files = new String[nonFlagArgs.size() - 1];
        return nonFlagArgs.subList(1, nonFlagArgs.size()).toArray(files);
    }

    /**
     * Gets the specified ranges to cut.
     *
     * @return A list of integer arrays representing the ranges.
     */
    public List<int[]> getRanges() {
        String ranges = nonFlagArgs.get(0);
        List<int[]> rangesList = new ArrayList<>();
        String[] rangeStrings = ranges.split(",");
        for (String range : rangeStrings) {
            String[] rangeLimits = range.split("-");
            if (rangeLimits.length == 1) {
                rangesList.add(new int[]{Integer.parseInt(rangeLimits[0]), Integer.parseInt(rangeLimits[0])});
            } else {
                rangesList.add(new int[]{Integer.parseInt(rangeLimits[0]), Integer.parseInt(rangeLimits[1])});
            }
        }
        return rangesList;
    }

    /**
     * Validates the provided arguments.
     *
     * @throws InvalidArgsException if the arguments are invalid.
     */
    @Override
    protected void validateArgs() throws InvalidArgsException {
        Set<Character> illegalFlags = new HashSet<>(flags);
        illegalFlags.removeAll(legalFlags);

        // construct exception message with the first illegal flag encountered
        for (Character flag : illegalFlags) {
            String exceptionMessage = ILLEGAL_FLAG_MSG + flag;
            throw new InvalidArgsException(exceptionMessage);
        }

        // Check for at least one of -c or -b flags
        if (!flags.contains(FLAG_CUT_BY_CHAR) && !flags.contains(FLAG_CUT_BY_BYTE)) {
            throw new InvalidArgsException(ILLEGAL_MSS_FLAG);
        }

        // Check for the presence of both -c and -b flags
        if (flags.contains(FLAG_CUT_BY_CHAR) && flags.contains(FLAG_CUT_BY_BYTE)) {
            throw new InvalidArgsException(ILLEGAL_BOTH_FLAG);
        }

        // Check ranges is valid
        String ranges = nonFlagArgs.get(0);
        String regex = "^(\\d+(-\\d+)?)(,\\d+(-\\d+)?)*$";
        Pattern pattern = Pattern.compile(regex);
        if (!pattern.matcher(ranges).matches()) {
            throw new InvalidArgsException(ILLEGAL_RANGE_MSG + " " + ranges);
        }

        // Check for invalid ranges
        List<int[]> rangesList = getRanges();
        for (int[] range : rangesList) {
            if (range[0] < 1 || range[1] < 1) {
                throw new InvalidArgsException(ILLEGAL_ONE_INDEX + " " + ranges);
            }
            if (range[0] > range[1]) {
                throw new InvalidArgsException(ILLEGAL_DCS_RANGE + " " + ranges);
            }
        }
    }
}