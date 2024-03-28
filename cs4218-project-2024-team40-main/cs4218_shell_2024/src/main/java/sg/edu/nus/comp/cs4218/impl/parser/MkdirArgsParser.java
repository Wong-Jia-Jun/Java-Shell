package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

/**
 * This class is Tee application's parser which encapsulates its own parsing details and
 * information  .
 */
public class MkdirArgsParser extends ArgsParser {

    public static final char FLAG_IS_MISS_PRNT = 'p';



    public MkdirArgsParser() {
        super();
        legalFlags.add(FLAG_IS_MISS_PRNT);

    }
    /**
     * Checks for the existence of legal option -p flags.
     * @return boolean whether is Append option present
     */
    public Boolean isCreateMissingParent() {
        return flags.contains(FLAG_IS_MISS_PRNT);
    }


    /**
     * Return nonFlagArgs which are arguements that are not flag related after being parsed.
     * @return boolean whether is Append option present
     */
    public List<String> getFileNames() {
        return nonFlagArgs;
    }


}
