package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

/**
 * This class is Tee application's parser which encapsulates its own parsing details and
 * information  .
 */
public class TeeArgsParser extends ArgsParser{
    public static final char FLAG_IS_APPEND = 'a';



    public TeeArgsParser() {
        super();

        legalFlags.add(FLAG_IS_APPEND);

    }
    /**
     * Checks for the existence of legal option append flags.
     * @return boolean whether is Append option present
     */
    public Boolean isAppend() {
        return flags.contains(FLAG_IS_APPEND);
    }


    /**
     * Return nonFlagArgs which are arguements that are not flag related after being parsed.
     * @return boolean whether is Append option present
     */
    public List<String> getFileNames() {
        return nonFlagArgs;
    }

}
