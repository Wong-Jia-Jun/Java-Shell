package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;
/**
 * Parses arguments for the `cat` command.
 */
public class CatArgsParser extends ArgsParser {
    public static final char FLAG_SHOW_LINE_NO = 'n';

    public CatArgsParser() {
        super();
        legalFlags.add(FLAG_SHOW_LINE_NO);
    }
    /**
     * Checks if the line numbers should be shown.
     *
     * @return True if the `-n` flag is present, false otherwise.
     */
    public Boolean isShowLineNumber() {
        return flags.contains(FLAG_SHOW_LINE_NO);
    }
    /**
     * Checks if the command is to read from stdin.
     *
     * @return True if no file names are provided, false otherwise.
     */
    public Boolean isCatStdin() {
        return nonFlagArgs.isEmpty();
    }
    /**
     * Checks if the command is to read from both files and stdin.
     *
     * @return True if both file names and `-` (stdin) are provided, false otherwise.
     */
    public Boolean isCatFileAndStdin() {
        return !nonFlagArgs.isEmpty() && nonFlagArgs.contains("-");
    }

    /**
     * Checks if the command is to read from files only.
     *
     * @return True if file names are provided and stdin is not included, false otherwise.
     */
    public Boolean isCatFiles() {
        return !nonFlagArgs.isEmpty() && !isCatFileAndStdin();
    }

    /**
     * Gets the list of file names provided in the arguments.
     *
     * @return A list of file names.
     */
    public List<String> getFileNames() {
        return nonFlagArgs;
    }
}
