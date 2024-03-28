package sg.edu.nus.comp.cs4218.impl.util;


/**
 * Utility class to hold all error/ exception messages used when some exception is caught and to be displayed
 */
public class ErrorConstants {

    // Streams related
    public static final String ERR_WRITE_STREAM = "Could not write to output stream";
    public static final String ERR_NULL_STREAMS = "Null Pointer Exception";
    public static final String ERR_CLOSE_STREAMS = "Unable to close streams";
    public static final String ERR_MULT_STREAMS = "Multiple streams provided";
    public static final String ERR_STREAM_CLOSED = "Stream is closed";
    public static final String ERR_NO_OSTREAM = "OutputStream not provided";
    public static final String ERR_NO_ISTREAM = "InputStream not provided";
    public static final String ERR_NO_INPUT = "No InputStream and no filenames";
    public static final String ERR_NO_FILE_ARGS = "No files provided";
    public static final String ERR_READ_STREAM = "Could not read from input stream";

    // Arguments related
    public static final String ERR_MISSING_ARG = "Missing Argument";
    public static final String ERR_NO_ARGS = "Insufficient arguments";
    public static final String ERR_NULL_ARGS = "Null arguments";
    public static final String ERR_TOO_MANY_ARGS = "Too many arguments";
    public static final String ERR_EXTRA_OPERAND = "extra operand";
    public static final String ERR_INVALID_FLAG = "Invalid flag option supplied";
    public static final String ERR_BAD_REGEX = "Invalid pattern";

    // Files and folders related
    public static final String ERR_FILE_NOT_FND = "No such file or directory";
    public static final String ERR_READING_FILE = "Could not read file";
    public static final String ERR_IS_DIR = "This is a directory";
    public static final String ERR_IS_NOT_DIR = "Not a directory";
    public static final String ERR_NO_PERM = "Permission denied";
    public static final String ERR_WRITING_FILE = "Could not write to file";

    public static final String ERR_IS_DIR_FILE = "Is a directory";

    public static final String ERR_DIR_NOT_EMPTY = "Directory not empty";


    // `date` related
    public static final String ERR_FORMAT_PREFIX = "Invalid format. Date format must start with '+'";
    public static final String ERR_FORMAT_FIELD = "Invalid format. Missing or unknown character after '%'";
    public static final String ERR_MISSING_FIELD = "Invalid format";

    // `find` related
    public static final String ERR_INVALID_FILE = "Invalid Filename";
    public static final String ERR_NAME_FLAG = "Paths must precede -name";

    // `sed` related
    public static final String ERR_NO_REP_RULE = "No replacement rule supplied";
    public static final String ERR_REP_RULE = "Invalid replacement rule";
    public static final String ERR_INVALID_REP_X = "X needs to be a number greater than 0";
    public static final String ERR_INVALID_REGEX = "Invalid regular expression supplied";
    public static final String ERR_EMPTY_REGEX = "Regular expression cannot be empty";

    // `grep` related
    public static final String ERR_NO_REGEX = "No regular expression supplied";

    // `mkdir` related
    public static final String ERR_NO_FOLDERS = "No folder name s are supplied";
    public static final String ERR_ALR_EXISTS = "File or directory already exists";
    public static final String ERR_TOP_MISSING = "Top level folders do not exist";

    // `mv` related
    public static final String ERR_FILE_EXISTS = "File already exists";

    // General constants
    public static final String ERR_INVALID_APP = "Invalid app";
    public static final String ERR_NOT_SUPPORTED = "Not supported yet";
    public static final String ERR_SYNTAX = "Invalid syntax";
    public static final String ERR_GENERAL = "Exception Caught";
    public static final String ERR_IO_EXCEPTION = "IOException";

}
