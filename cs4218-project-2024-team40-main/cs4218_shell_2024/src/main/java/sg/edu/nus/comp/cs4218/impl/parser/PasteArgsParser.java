package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;
/**
 * This class is Paste application's parser which encapsulates its own parsing details and
 * information  .
 */
public class PasteArgsParser extends  ArgsParser {
    public static final char FLAG_IS_SERIAL = 's';



    public PasteArgsParser() {
        super();
        legalFlags.add(FLAG_IS_SERIAL);

    }
    /**
     * Checks for the existence of legal option -s flags.
     * @return boolean whether is Append option present
     */
    public Boolean isSerial() {
        return flags.contains(FLAG_IS_SERIAL);
    }

    /**
     * Checks if the command is to read from stdin.
     *
     * @return True if no file names are provided, false otherwise.
     */
    public Boolean isPasteStdin() {
        return nonFlagArgs.isEmpty();
    }

    /**
     * Checks if the command is to read from both files and stdin.
     *
     * @return True if both file names and `-` (stdin) are provided, false otherwise.
     */
    public Boolean isPasteFileAndStdin() {
        return !nonFlagArgs.isEmpty() && nonFlagArgs.contains("-");
    }

    /**
     * Checks if the command is to read from files only.
     *
     * @return True if file names are provided and stdin is not included, false otherwise.
     */
    public Boolean isPasteFiles() {
        return !nonFlagArgs.isEmpty() && !isPasteFileAndStdin();
    }

    /**
     * Return nonFlagArgs which are arguements that are not flag related after being parsed.
     * @return boolean whether is Append option present
     */
    public List<String> getFileNames() {
        return nonFlagArgs;
    }

}
