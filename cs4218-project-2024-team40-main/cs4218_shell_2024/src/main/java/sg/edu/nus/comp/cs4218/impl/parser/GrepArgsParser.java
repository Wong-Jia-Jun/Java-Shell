package sg.edu.nus.comp.cs4218.impl.parser;

import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;

import java.util.HashSet;
import java.util.Set;

/**
 * Parses arguments for the `grep` command.
 */
public class GrepArgsParser extends ArgsParser {
    public static final String ILLEGAL_NO_PAT = "Missing pattern. A pattern must be specified.";
    private final static char FLAG_IS_IGNORE = 'i';
    private final static char FLAG_IS_COUNT = 'c';
    private final static char FLAG_IS_PRNT_FILE = 'H';


    /**
     * Creates a new GrepArgsParser instance.
     */
    public GrepArgsParser() {
        super();
        legalFlags.add(FLAG_IS_IGNORE);
        legalFlags.add(FLAG_IS_COUNT);
        legalFlags.add(FLAG_IS_PRNT_FILE);
    }

    /**
     * Checks for the existence of illegal flags. Presence of any illegal flags would result in an exception.
     *
     * @throws InvalidArgsException
     */
    @Override
    protected void validateArgs() throws InvalidArgsException {
        Set<Character> illegalFlags = new HashSet<>(flags);
        illegalFlags.removeAll(legalFlags);

        for (Character flag : illegalFlags) {
            String exceptionMessage = ILLEGAL_FLAG_MSG + flag;
            throw new InvalidArgsException(exceptionMessage);
        }

        if (nonFlagArgs.isEmpty()) {
            throw new InvalidArgsException(ILLEGAL_NO_PAT);
        }
    }

    /**
     * Checks if the ignore case flag is set.
     *
     * @return True if the ignore case flag is set, false otherwise.
     */
    public Boolean isIgnoreCase() {
        return flags.contains(FLAG_IS_IGNORE);
    }

    /**
     * Checks if the count flag is set.
     *
     * @return True if the count flag is set, false otherwise.
     */
    public Boolean isCount() {
        return flags.contains(FLAG_IS_COUNT);
    }

    /**
     * Checks if the print file name flag is set.
     *
     * @return True if the print file name flag is set, false otherwise.
     */
    public Boolean isPrintFileName() {
        return flags.contains(FLAG_IS_PRNT_FILE);
    }

    /**
     * Gets the pattern to search for.
     *
     * @return The search pattern, or null if not provided.
     */
    public String getPattern() {
        return nonFlagArgs.isEmpty() ? null : nonFlagArgs.get(0);
    }

    /**
     * Gets the file names to search in.
     *
     * @return An array of file names, or an empty array if none provided.
     */
    public String[] getFileNames() {
        if (nonFlagArgs.size() <= 1) {
            return new String[0];
        } else {
            return nonFlagArgs.subList(1, nonFlagArgs.size()).toArray(new String[0]);
        }
    }

    /**
     * Checks if the grep command is to be executed on stdin.
     * @return
     */
    public boolean isGrepFromStdin() {
        return nonFlagArgs.size() == 1;
    }

    /**
     * Checks if the grep command is to be executed on files.
     * @return
     */
    public boolean isGrepFromFiles() {
        return nonFlagArgs.size() > 1 && !nonFlagArgs.subList(1, nonFlagArgs.size()).contains("-");
    }

    /**
     * Checks if the grep command is to be executed on files and stdin.
     * @return
     */
    public boolean isGrepFromFilesAndStdin() {
        return nonFlagArgs.size() > 1 && !isGrepFromFiles();
    }
}
