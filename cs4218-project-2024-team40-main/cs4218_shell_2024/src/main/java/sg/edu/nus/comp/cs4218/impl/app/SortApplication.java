package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.SortInterface;
import sg.edu.nus.comp.cs4218.exception.*;
import sg.edu.nus.comp.cs4218.impl.parser.SortArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.*;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
@SuppressWarnings("PMD.GodClass")
/**
 * Implementation of the 'sort' command application.
 */
public class SortApplication implements SortInterface {
    private final SortArgsParser sortArgsParser;
    /**
     * Inner class to define custom sorting behavior similar to shell sorting.
     */
    private class SortLikeShell implements Comparator<String> {
        private final boolean isCaseIndependent;
        private final boolean isFirstWordNumber;

        public SortLikeShell(boolean isCaseIndependent, boolean isFirstWordNumber) {
            this.isCaseIndependent = isCaseIndependent;
            this.isFirstWordNumber = isFirstWordNumber;
        }
        /**
         * Compares two strings based on custom sorting behavior.
         *
         * @param str1 The first string to compare.
         * @param str2 The second string to compare.
         * @return An integer representing the comparison result.
         */
        @Override
        public int compare(String str1, String str2) {
            String temp1 = isCaseIndependent ? str1.toLowerCase(Locale.ENGLISH) : str1;
            String temp2 = isCaseIndependent ? str2.toLowerCase(Locale.ENGLISH) : str2;
            int compareResult = 0;
            // Extract the first group of numbers if possible.
            if (isFirstWordNumber && !temp1.isEmpty() && !temp2.isEmpty()) {
                String chunk1 = getChunk(temp1);
                String chunk2 = getChunk(temp2);

                boolean isChunk1Number = checkIfChunkIsNumber(chunk1);
                boolean isChunk2Number = checkIfChunkIsNumber(chunk2);
                if (!isChunk1Number && !isChunk2Number) { //NOPMD - valid for now for convenience if not can change to positive
                    compareResult = temp1.compareTo(temp2);
                } else if (isChunk1Number && isChunk2Number) {
                    BigInteger int1 = new BigInteger(chunk1);
                    BigInteger int2 = new BigInteger(chunk2);
                    compareResult = int1.equals(int2) ? temp1.compareTo(temp2) : int1.compareTo(int2);
                } else {
                    BigInteger int1 = isChunk1Number ? new BigInteger(chunk1) : BigInteger.ZERO;
                    BigInteger int2 = isChunk2Number ? new BigInteger(chunk2) : BigInteger.ZERO;
                    compareResult = int1.equals(int2) ? temp1.compareTo(temp2) : int1.compareTo(int2);
                }
            } else {
                compareResult = temp1.compareTo(temp2);
            }

            return compareResult;
        }
    }

    public SortApplication(SortArgsParser sortArgsParser) {
        this.sortArgsParser = sortArgsParser;
    }

    /**
     * Runs the sort application with the specified arguments.
     *
     * @param args   Array of arguments for the application. Each array element is the path to a
     *               file. If no files are specified stdin is used.
     * @param stdin  An InputStream. The input for the command is read from this InputStream if no
     *               files are specified.
     * @param stdout An OutputStream. The output of the command is written to this OutputStream.
     * @throws SortException
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws AbstractApplicationException {
        // Format: sort [-nrf] [FILES]
        if (stdin == null || stdout == null) {
            throw new SortException(ERR_NULL_STREAMS);
        }
        if (args == null) {
            throw new SortException(ERR_NULL_ARGS);
        }
        try {
            sortArgsParser.parse(args);
        } catch (InvalidArgsException e) {
            throw new SortException(e);
        }
        StringBuilder output = new StringBuilder();
        if (sortArgsParser.isSortFromStdin()) {
            output.append(sortFromStdin(sortArgsParser.isFirstWordNumber(), sortArgsParser.isReverseOrder(),
                    sortArgsParser.isCaseIndependent(), stdin));
        } else if (sortArgsParser.isSortFromFiles()) {
            String[] files = sortArgsParser.getFileNames().toArray(new String[0]);
            output.append(sortFromFiles(sortArgsParser.isFirstWordNumber(), sortArgsParser.isReverseOrder(),
                    sortArgsParser.isCaseIndependent(), files));
        } else {
            // This shouldn't happen but just put this for defensive programming
            throw new SortException(ERR_GENERAL);
        }

        try {
            if (!output.toString().isEmpty()) {
                stdout.write(output.toString().getBytes());
                stdout.write(STRING_NEWLINE.getBytes());
            }
        } catch (IOException e) {
            throw new SortException(ERR_WRITE_STREAM); //NOPMD - throw exception here to be caught in shelimpl to print excpt msg
        }
    }

    /**
     * Returns string containing the orders of the lines of the specified file
     *
     * @param isFirstWordNumber Boolean option to treat the first word of a line as a number
     * @param isReverseOrder    Boolean option to sort in reverse order
     * @param isCaseIndependent Boolean option to perform case-independent sorting
     * @param fileNames         Array of String of file names
     * @throws Exception
     */
    @Override
    public String sortFromFiles(Boolean isFirstWordNumber, Boolean isReverseOrder, Boolean isCaseIndependent,
                                String... fileNames) throws AbstractApplicationException {
        if (fileNames == null || fileNames.length == 0) {
            throw new SortException(ERR_NULL_ARGS);
        }
        List<String> lines = new ArrayList<>();
        for (String file : fileNames) {
            Path filePath;
            try {
                filePath = IOUtils.resolveFilePath(file);
            } catch (IOException e) {
                throw new SortException(file, ERR_FILE_NOT_FND); //NOPMD - throw here to get caught in ShellImpl and show msg
            }
            File node = filePath.toFile();
            if (!node.exists()) {
                throw new SortException(file, ERR_FILE_NOT_FND);
            }
            if (node.isDirectory()) {
                throw new SortException(file, ERR_IS_DIR);
            }
            if (!node.canRead()) {
                throw new SortException(file, ERR_NO_PERM);
            }
            InputStream input ; //NOPMD - closed in IOUtils.closeInputStream(input) below

            try {
                input = IOUtils.openInputStream(file);
            } catch (ShellException e) {
                throw new SortException(ERR_READING_FILE);//NOPMD - throw here to get caught in SHell impl and show excpt msg
            }
            try {
                lines.addAll(IOUtils.getLinesFromInputStream(input));
            } catch (IOException e) {
                throw new SortException(ERR_IO_EXCEPTION);//NOPMD - throw here to get caught in SHell impl and show excpt msg
            }
            try {
                IOUtils.closeInputStream(input);
            } catch (ShellException e) {
                throw new SortException(ERR_CLOSE_STREAMS);//NOPMD - throw here to get caught in SHell impl and show excpt msg
            }

        }
        lines = sortInputString(isFirstWordNumber, isReverseOrder, isCaseIndependent, lines);
        return convertSortedLinesToString(lines);
    }

    /**
     * Returns string containing the orders of the lines from the standard input
     *
     * @param isFirstWordNumber Boolean option to treat the first word of a line as a number
     * @param isReverseOrder    Boolean option to sort in reverse order
     * @param isCaseIndependent Boolean option to perform case-independent sorting
     * @param stdin             InputStream containing arguments from Stdin
     * @throws Exception
     */
    @Override
    public String sortFromStdin(Boolean isFirstWordNumber, Boolean isReverseOrder, Boolean isCaseIndependent,
                                InputStream stdin) throws AbstractApplicationException {
        if (stdin == null) {
            throw new SortException(ERR_NULL_STREAMS);
        }
        List<String> lines;
        try {
            lines = IOUtils.getLinesFromInputStream(stdin);
        } catch (IOException e) {
            throw new SortException(ERR_IO_EXCEPTION);//NOPMD - throw here to get caught in SHell impl and show excpt msg
        }
        lines = sortInputString(isFirstWordNumber, isReverseOrder, isCaseIndependent, lines);
        return convertSortedLinesToString(lines);
    }

    /**
     * Sorts the input ArrayList based on the given conditions. Invoking this function will mutate the ArrayList.
     *
     * @param isFirstWordNumber Boolean option to treat the first word of a line as a number
     * @param isReverseOrder    Boolean option to sort in reverse order
     * @param isCaseIndependent Boolean option to perform case-independent sorting
     * @param input             ArrayList of Strings of lines
     */
    private List<String> sortInputString(Boolean isFirstWordNumber, Boolean isReverseOrder, Boolean isCaseIndependent,
                                         List<String> input) {
        List<String> modifiableList = new ArrayList<>(input);
        Collections.sort(modifiableList, new SortLikeShell(isCaseIndependent, isFirstWordNumber));
        if (isReverseOrder) {
            Collections.reverse(modifiableList);
        }
        return modifiableList;
    }

    /**
     * Extracts a chunk of numbers or non-numbers from str starting from index 0.
     * If the first character is a digit, it will extract a chunk of numbers.
     * If the first character is not a digit, it will return the original string.
     *
     * @param str Input string to read from
     */
    private String getChunk(String str) {
        int startIndexLocal = 0;
        StringBuilder chunk = new StringBuilder();
        final int strLen = str.length();
        char chr = str.charAt(startIndexLocal++);
        chunk.append(chr);
        final boolean extractDigit = checkIfChunkIsNumber(str);
        if (!extractDigit) {
            return chunk.toString();
        }
        while (startIndexLocal < strLen) {
            chr = str.charAt(startIndexLocal++);
            if (!Character.isDigit(chr)) {
                break;
            }
            chunk.append(chr);
        }
        return chunk.toString();
    }
    /**
     * Checks if the given string represents a number.
     *
     * @param str The string to check.
     * @return True if the string represents a number, false otherwise.
     */
    private boolean checkIfChunkIsNumber(String str) {
        char chr = str.charAt(0);
        return Character.isDigit(chr)
                || (chr == '-' && str.length() > 1 && Character.isDigit(str.charAt(1)));
    }
    /**
     * Converts the sorted lines into a single string.
     *
     * @param lines The list of sorted lines.
     * @return The sorted lines as a single string.
     */
    private String convertSortedLinesToString(List<String> lines) {
        String result = "";
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.endsWith(STRING_NEWLINE)) {
                result += line;
            } else {
                result += line;
                if (i != lines.size() - 1) {
                    result += STRING_NEWLINE;
                }
            }
        }
        return result;
    }
}
