package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;
/**
 * This class is Uniq application's parser which encapsulates its own parsing details and
 * information  .
 */
public class UniqArgsParser extends ArgsParser {
    public static final char FLAG_IS_COUNT = 'c';
    public static final char FLAG_IS_REPEATED = 'd';
    public static final char FLAG_IS_ALL_RPT = 'D';


    public UniqArgsParser() {
        super();
        legalFlags.add(FLAG_IS_COUNT);
        legalFlags.add(FLAG_IS_REPEATED);
        legalFlags.add(FLAG_IS_ALL_RPT);


    }
    /**
     * Checks for the existence of legal option -c flags.
     * @return boolean whether is Append option present
     */
    public Boolean isCount() {
        return flags.contains(FLAG_IS_COUNT);
    }
    /**
     * Checks for the existence of legal option -d flags.
     * @return boolean whether is Append option present
     */
    public Boolean isRepeated() {
        return flags.contains(FLAG_IS_REPEATED);
    }
    /**
     * Checks for the existence of legal option -D flags.
     * @return boolean whether is Append option present
     */
    public Boolean isAllRepeated() {
        return flags.contains(FLAG_IS_ALL_RPT);
    }


    /**
     * Return nonFlagArgs which are arguements that are not flag related after being parsed.
     * @return boolean whether is Append option present
     */
    public List<String> getFileNames() {
        return nonFlagArgs;
    }

    /**
     * Return inputfile which is first/0th index in nonFlag arg else return null if non exist
     * @return String value of input file
     */
    public String getInputFile() {
        if (nonFlagArgs.size() == 0) {
            return null;
        }
        return nonFlagArgs.get(0);
    }
    /**
     * Return output file which is second/1st index in nonFlag arg else return null if non exist
     * @return String value of output file
     */
    public String getOutputFile() {
        if (nonFlagArgs.size() <= 1) {
            return null;
        }
        return nonFlagArgs.get(1);
    }

}
