package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.WcInterface;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.exception.WcException;
import sg.edu.nus.comp.cs4218.impl.parser.WcArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

/**
 * Implementation of the 'wc' command application.
 */
@SuppressWarnings("PMD.GodClass")
public class WcApplication implements WcInterface {
    private static final String TAB = "\t";
    private static final int LINES_INDEX = 0;
    private static final int WORDS_INDEX = 1;
    private static final int BYTES_INDEX = 2;
    public static final String WC_PREFIX = "wc: ";
    private final WcArgsParser parser;

    public WcApplication(WcArgsParser parser) {
        this.parser = parser;
    }

    /**
     * Runs the wc application with the specified arguments.
     *
     * @param args   Array of arguments for the application. Each array element is the path to a
     *               file. If no files are specified stdin is used.
     * @param stdin  An InputStream. The input for the command is read from this InputStream if no
     *               files are specified.
     * @param stdout An OutputStream. The output of the command is written to this OutputStream.
     * @throws WcException If an error occurs during the execution, including null input stream or IO exceptions.
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout)
            throws WcException {
        // Format: wc [-clw] [FILES]
        if (stdout == null || stdin == null) {
            throw new WcException(ERR_NULL_STREAMS);
        }
        if (args == null) {
            throw new WcException(ERR_NULL_ARGS);
        }
        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new WcException(e);
        }
        String result;
        boolean onlySingleDash = parser.getFileNames().stream().anyMatch("-"::equals) && parser.getFileNames().size() == 1;
        boolean containsDash = parser.getFileNames().stream().anyMatch("-"::equals);
        try {
            if (onlySingleDash || parser.getFileNames().isEmpty()) {
                result = countFromStdin(parser.isByteCount(), parser.isLineCount(), parser.isWordCount(), stdin);
                if (onlySingleDash) {
                    result = result + " -";
                }
            } else if (containsDash) {
                result = countFromFileAndStdin(parser.isByteCount(), parser.isLineCount(), parser.isWordCount(), stdin, parser.getFileNames().toArray(new String[0]));
            } else {
                result = countFromFiles(parser.isByteCount(), parser.isLineCount(), parser.isWordCount(), parser.getFileNames().toArray(new String[0]));
            }
        } catch (Exception e) {
            // Will never happen
            throw new WcException(ERR_GENERAL);//NOPMD - throw here to get caught in SHell impl and show excpt msg
        }
        try {
            stdout.write(result.getBytes());
            stdout.write(STRING_NEWLINE.getBytes());
        } catch (IOException e) {
            throw new WcException(ERR_WRITE_STREAM);//NOPMD - throw here to get caught in SHell impl and show excpt msg
        }
    }

    /**
     * Returns string containing the number of lines, words, and bytes in input files.
     *
     * @param isBytes  Boolean option to count the number of Bytes.
     * @param isLines  Boolean option to count the number of lines.
     * @param isWords  Boolean option to count the number of words.
     * @param fileName Array of String of file names.
     * @throws WcException If an error occurs during the counting process.
     */
    @Override
    public String countFromFiles(Boolean isBytes, Boolean isLines, Boolean isWords, //NOPMD
                                 String... fileName) throws WcException {
        if (fileName == null) {
            throw new WcException(ERR_GENERAL);
        }
        List<String> result = new ArrayList<>();
        long[] totalCount = new long[3];
        for (String file : fileName) {
            long[] count = new long[3];
            if (!readFromFile(result, file, count)) {
                continue;
            }

            if (fileName.length == result.size()) { // Single file and exception received
                continue;
            }

            // Update total count
            totalCount[0] += count[0];
            totalCount[1] += count[1];
            totalCount[2] += count[2];

            // Format all output: " %7d %7d %7d %s"
            // Output in the following order: lines words bytes filename
            StringBuilder stringBuild = new StringBuilder();
            // fixed bug: no arguments should show all values
            appendWordCount(stringBuild, count, isLines, isWords, isBytes);
            stringBuild.append(String.format(" %s", file));
            result.add(stringBuild.toString());
        }

        // Print cumulative counts for all the files
        if (fileName.length > 1) {
            StringBuilder stringBuild = new StringBuilder(); //NOPMD
            // fixed bug: no arguments should show all values
            appendWordCount(stringBuild, totalCount, isLines, isWords, isBytes);
            stringBuild.append(" total");
            result.add(stringBuild.toString());
        }
        return String.join(STRING_NEWLINE, result);
    }

    /**
     * Returns string containing the number of lines, words, and bytes in standard input.
     *
     * @param isBytes Boolean option to count the number of Bytes.
     * @param isLines Boolean option to count the number of lines.
     * @param isWords Boolean option to count the number of words.
     * @param stdin   InputStream containing arguments from Stdin.
     * @throws WcException If an error occurs during the counting process.
     */
    @Override
    public String countFromStdin(Boolean isBytes, Boolean isLines, Boolean isWords,
                                 InputStream stdin) throws WcException {
        if (stdin == null) {
            throw new WcException(ERR_NULL_STREAMS);
        }
        long[] count = getCountReport(stdin); // lines words bytes;
        StringBuilder stringBuild = new StringBuilder(); //NOPMD
        appendWordCount(stringBuild, count, isLines, isWords, isBytes);
        return stringBuild.toString();
    }

    /**
     * Returns string containing the number of lines, words, and bytes in input files and standard input
     *
     * @param isBytes  Boolean option to count the number of Bytes
     * @param isLines  Boolean option to count the number of lines
     * @param isWords  Boolean option to count the number of words
     * @param stdin    InputStream containing arguments from Stdin
     * @param fileName Array of String of file names
     * @throws WcException If an error occurs during the counting process, including null input stream or IO exceptions.
     */
    @Override
    public String countFromFileAndStdin(Boolean isBytes, Boolean isLines, Boolean isWords, //NOPMD - suppressed ExcessiveMethodLength - original method name from teaching team
                                        InputStream stdin, String... fileName) throws WcException {
        if (stdin == null) {
            throw new WcException(ERR_NULL_STREAMS);
        }
        if (fileName == null) {
            throw new WcException(ERR_GENERAL);
        }
        boolean isFirstDash = true;
        List<long []> counts = new LinkedList<>();
        long[] stdinCount = new long[3];
        long[] totalCount = new long[3];
        List<String> result = new ArrayList<>();
        for (String file : fileName) {
            long[] count;
            if ("-".equals(file)) {
                count = getCountReport(stdin); // lines words bytes;
                for (int i = 0; i < count.length; i++) {
                    stdinCount[i] += count[i]; // store stdin in separate array
                }
                if (isFirstDash) {
                    counts.add(stdinCount);
                    isFirstDash = false;
                } else {
                    counts.add(new long[3]);
                }
            } else {
                count = new long[3];
                if (!readFromFile(result, file, count)) {
                    continue;
                }
                counts.add(count);
            }

            // Update total count
            totalCount[0] += count[0];
            totalCount[1] += count[1];
            totalCount[2] += count[2];
        }

        // Print all counts
        int fileNumber = 0;
        while (!counts.isEmpty()) {
            long[] finalCount = counts.remove(0);
            StringBuilder stringBuild = new StringBuilder(); //NOPMD
            // fixed bug: no arguments should show all values
            appendWordCount(stringBuild, finalCount, isLines, isWords, isBytes);
            stringBuild.append(String.format(" %s", fileName[fileNumber]));
            result.add(stringBuild.toString());
            fileNumber++;
        }


        // Print cumulative counts for all the files and stdin
        if (fileName.length > 1) {
            StringBuilder stringBuild = new StringBuilder(); //NOPMD
            // fixed bug: no arguments should show all values
            appendWordCount(stringBuild, totalCount, isLines, isWords, isBytes);
            stringBuild.append(" total");
            result.add(stringBuild.toString());
        }

        return String.join(STRING_NEWLINE, result);
    }

    /**
     * Calculates a count report of lines, words, and bytes from the provided input stream.
     *
     * @param input The input stream from which to calculate the count report.
     * @return An array of long values containing the count report. The array has three elements:
     *         - Index 0: Number of lines.
     *         - Index 1: Number of words.
     *         - Index 2: Number of bytes.
     * @throws WcException If an error occurs during the counting process, including null input stream or IO exceptions.
     */
    public long[] getCountReport(InputStream input) throws WcException {
        if (input == null) {
            throw new WcException(ERR_NULL_STREAMS);
        }
        long[] result = new long[3]; // lines, words, bytes

        byte[] data = new byte[1024];
        int inRead;
        boolean inWord = false;
        try {
            while ((inRead = input.read(data, 0, data.length)) != -1) {
                for (int i = 0; i < inRead; ++i) {
                    if (Character.isWhitespace(data[i])) {
                        // Use <newline> character here. (Ref: UNIX)
                        if (data[i] == '\n') {
                            ++result[LINES_INDEX];
                        }
                        if (inWord) {
                            ++result[WORDS_INDEX];
                        }

                        inWord = false;
                    } else {
                        inWord = true;
                    }
                }
                result[BYTES_INDEX] += inRead;
            }
            if (inWord) {
                ++result[WORDS_INDEX]; // To handle last word
            }
        } catch (IOException e) {
            throw new WcException(ERR_IO_EXCEPTION); //NOPMD - throw here to get caught in ShellImpl and show msg
        }

        return result;
    }

    /**
     * Appends Word Count to a StringBuilder with the number of lines, words, and bytes.
     *
     * @param stringBuilder      StringBuilder which forms the required string.
     * @param count   Long[] of values for number of bytes, lines and words.
     * @param isBytes Boolean option to count the number of Bytes.
     * @param isLines Boolean option to count the number of lines.
     * @param isWords Boolean option to count the number of words.
     */
    private void appendWordCount(StringBuilder stringBuilder, long[] count, boolean isLines, boolean isWords, boolean isBytes) {
        boolean noArguments = !isLines && !isWords && !isBytes;
        if (noArguments) {
            stringBuilder.append(TAB).append(count[0]);
            stringBuilder.append(TAB).append(count[1]);
            stringBuilder.append(TAB).append(count[2]);
        } else {
            if (isLines) {
                stringBuilder.append(TAB).append(count[0]);
            }
            if (isWords) {
                stringBuilder.append(TAB).append(count[1]);
            }
            if (isBytes) {
                stringBuilder.append(TAB).append(count[2]);
            }
        }
    }

    /**
     * Reads the content from the specified file, calculates the count report of lines, words, and bytes,
     * and updates the provided count array with the calculated values.
     *
     * @param result The list to store any error messages or additional information.
     * @param file   The path to the file to be read.
     * @param count  An array to store the count report. It must have a length of 3, where:
     *               - Index 0: Number of lines.
     *               - Index 1: Number of words.
     *               - Index 2: Number of bytes.
     * @return true if file was successfully read
     * @throws WcException If an error occurs during file reading, including file not found, permission issues,
     *                     or if there are any errors during the counting process.
     */
    private boolean readFromFile(List<String> result, String file, long... count) throws WcException {
        File node;
        try {
            node = IOUtils.resolveFilePath(file).toFile();
        } catch (IOException e) {
            result.add(WC_PREFIX + ERR_FILE_NOT_FND);
            return false;
        }
        if (!node.exists()) {
            result.add(WC_PREFIX + ERR_FILE_NOT_FND);
            return false;
        }
        if (node.isDirectory()) {
            result.add(WC_PREFIX + ERR_IS_DIR);
            return false;
        }
        if (!node.canRead()) {
            result.add(WC_PREFIX + ERR_NO_PERM);
            return false;
        }

        try (InputStream input = IOUtils.openInputStream(file)){
            long[] tempCount = getCountReport(input); // lines words bytes
            System.arraycopy(tempCount, 0, count, 0, tempCount.length);
        } catch (IOException | ShellException e) {
            throw new WcException(e);
        }
        return true;
    }
}
