package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.PasteInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.PasteException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.parser.PasteArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

@SuppressWarnings("PMD.GodClass")
public class PasteApplication implements PasteInterface {
    private static final char TAB = '\t';
    private final PasteArgsParser pasteArgsParser; //NOPMD - unimplemented
    public PasteApplication(PasteArgsParser pasteArgsParser){
        this.pasteArgsParser = pasteArgsParser;
    }

    /**
     * Runs the paste application with the specified arguments.
     *
     * @param args  Array of arguments for the application.
     * @param stdin InputStream for the application.
     * @param stdout OutputStream for the application.
     * @throws AbstractApplicationException If an exception happens during the execution of paste.
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws AbstractApplicationException {//NOPMD - unimplemented
        if (stdin == null || stdout == null) {
            throw new PasteException(ERR_NULL_STREAMS);
        }
        if (args == null) {
            throw new PasteException(ERR_NULL_ARGS);
        }
        try {
            pasteArgsParser.parse(args);
        } catch (InvalidArgsException e) {
            throw new PasteException(e);
        }
        Boolean isSerial = pasteArgsParser.isSerial();
        try {
            StringBuilder result = new StringBuilder();
            if (pasteArgsParser.isPasteStdin()) {
                result.append(mergeStdin(isSerial, stdin));
            } else if (pasteArgsParser.isPasteFiles()) {
                result.append(mergeFile(isSerial, pasteArgsParser.getFileNames().toArray(new String[0])));
            } else if (pasteArgsParser.isPasteFileAndStdin()) {
                result.append(mergeFileAndStdin(isSerial, stdin, pasteArgsParser.getFileNames().toArray(new String[0])));
            } else {
                // This shouldn't happen but just put this for defensive programming
                throw new PasteException(ERR_GENERAL);
            }
            stdout.write(result.toString().getBytes());
            if (result.length() > 0) {
                stdout.write(STRING_NEWLINE.getBytes());
            }
        } catch (IOException e) {
            throw new PasteException(e);
        }
    }

    /**
     * Performs paste operation on the given input stream and writes the result to the output stream.
     * @param isSerial Paste one file at a time instead of in parallel
     * @param stdin InputStream containing arguments from Stdin
     * @return
     * @throws AbstractApplicationException
     */
    @Override
    public String mergeStdin(Boolean isSerial, InputStream stdin) throws AbstractApplicationException{
        if (stdin == null) {
            throw new PasteException(ERR_NULL_STREAMS);
        }
        try {
            List<String> lines = IOUtils.getLinesFromInputStream(stdin);
            if (isSerial) {
                return mergeLinesSerial(lines);
            } else {
                return mergeLinesNonSerial(lines);
            }
        } catch (IOException e) {
            throw new PasteException(e);
        }
    }

    /**
     * Performs paste operation on the given files and returns the result.
     * @param isSerial Paste one file at a time instead of in parallel
     * @param fileName Array of file names to be read and merged (not including "-" for reading from stdin)
     * @return
     * @throws AbstractApplicationException
     */
    @Override
    public String mergeFile(Boolean isSerial, String... fileName) throws AbstractApplicationException{
        if (fileName == null) {
            throw new PasteException(ERR_NULL_ARGS);
        }
        if (fileName.length == 0) {
            throw new PasteException(ERR_NO_ARGS);
        }
        List[] lines = new List[fileName.length];
        for (int i = 0; i < fileName.length; i++) {
            if (StringUtils.isBlank(fileName[i])) {
                throw new PasteException(ERR_NO_FILE_ARGS);
            }
            Path filePath;
            try {
                filePath = IOUtils.resolveFilePath(fileName[i]);
            } catch (IOException e) {
                throw new PasteException(fileName[i], ERR_FILE_NOT_FND); //NOPMD - throw here to get caught in ShellImpl and show msg
            }
            if (!Files.exists(filePath)) {
                throw new PasteException(fileName[i], ERR_FILE_NOT_FND);
            }
            if (Files.isDirectory(filePath)) {
                throw new PasteException(fileName[i], ERR_IS_DIR_FILE);
            }
            try (InputStream inputStream = IOUtils.openInputStream(fileName[i])) {
                lines[i] = IOUtils.getLinesFromInputStream(inputStream);
            } catch (ShellException | IOException e) {
                throw new PasteException(e);
            }
        }
        if (isSerial) {
            return mergeLinesSerial(lines);
        } else {
            return mergeLinesNonSerial(lines);
        }
    }

    /**
     * Performs paste operation on the given input stream and files and returns the result.
     * Open stdin once and get stdin input if non serial, apply each line to each '-' in output,
     * else open stdin for each file and its behavior is like ubuntu shell
     * @param isSerial Paste one file at a time instead of in parallel
     * @param stdin    InputStream containing arguments from Stdin
     * @param fileName Array of file names to be read and merged (including "-" for reading from stdin)
     * @return
     * @throws AbstractApplicationException
     */
    @Override
    public String mergeFileAndStdin(Boolean isSerial, InputStream stdin, String... fileName) throws AbstractApplicationException { //NOPMD - execesiveMethodLength
        if (fileName == null) {
            throw new PasteException(ERR_NULL_ARGS);
        }
        if (stdin == null) {
            throw new PasteException(ERR_NULL_STREAMS);
        }
        if (fileName.length == 0) {
            throw new PasteException(ERR_NO_ARGS);
        }

        List[] lines = new List[fileName.length];
        List<List<String>> stdinLineRefs = new ArrayList<>();
        for (int i = 0; i < fileName.length; i++) {
            if (StringUtils.isBlank(fileName[i])) {
                throw new PasteException(ERR_NO_FILE_ARGS);
            }

            if ("-".equals(fileName[i])) {
                if (isSerial) {
                    try {
                        lines[i] = IOUtils.getLinesFromInputStream(stdin);
                    } catch (IOException e) {
                        throw new PasteException(e);
                    }
                } else {
                    lines[i] = new ArrayList<String>();
                    stdinLineRefs.add((List<String>) lines[i]);
                }
                continue;
            }
            Path filePath;
            try {
                filePath = IOUtils.resolveFilePath(fileName[i]);
            } catch (IOException e) {
                throw new PasteException(fileName[i], ERR_FILE_NOT_FND); //NOPMD - throw here to get caught in ShellImpl and show msg
            }
            if (!Files.exists(filePath)) {
                throw new PasteException(fileName[i], ERR_FILE_NOT_FND);
            }
            if (Files.isDirectory(filePath)) {
                throw new PasteException(fileName[i], ERR_IS_DIR_FILE);
            }
            try (InputStream inputStream = IOUtils.openInputStream(fileName[i])) {
                lines[i] = IOUtils.getLinesFromInputStream(inputStream);
            } catch (ShellException | IOException e) {
                throw new PasteException(e);
            }
        }
        if (isSerial) {
            return mergeLinesSerial(lines);
        } else {
            List<String> stdinLines = null;
            try {
                stdinLines = IOUtils.getLinesFromInputStream(stdin);
            } catch (IOException e) {
                throw new PasteException(e);
            }
            int stdinLinesIndex = 0;
            while (stdinLinesIndex < stdinLines.size()) {
                for (List<String> list : stdinLineRefs) {
                    if (stdinLinesIndex >= stdinLines.size()) {
                        break;
                    }
                    list.add(stdinLines.get(stdinLinesIndex));
                    stdinLinesIndex++;
                }
            }
            return mergeLinesNonSerial(lines);
        }
    }

    /**
     * Non serial- inputA.line1 + \t + inputB.line1 + \t + inputC.line1 + \n...
     *             inputA.line2 + \t + inputB.line2 + \t + inputC.line2 + \n...
     * @param arrayOfLines arrayOfLines[i] = lists of lines from input i
     * @return merged lines
     */
    @SafeVarargs
    private String mergeLinesNonSerial(List<String>... arrayOfLines) {
        int maxLength = 0;
        for (List<String> lines : arrayOfLines) {
            if (lines.size() > maxLength) {
                maxLength = lines.size();
            }
        }
        StringBuilder result = new StringBuilder();
        if (maxLength == 0) { // If all inputs are empty, still seperate by tabs
            for (int i = 0; i < arrayOfLines.length; i++) {
                if (i < arrayOfLines.length - 1) {
                    result.append(TAB);
                } else {
                    return result.toString();
                }
            }
        }
        for (int i = 0; i < maxLength; i++) {
            for (int j = 0; j < arrayOfLines.length; j++) {
                if (i < arrayOfLines[j].size()) {
                    result.append(arrayOfLines[j].get(i));
                }
                if (j < arrayOfLines.length - 1) {
                    result.append(TAB);
                }
            }
            if (i < maxLength - 1) {
                result.append(STRING_NEWLINE);
            }
        }
        return result.toString();
    }

    /**
     * Serial- inputA.line1 + \n + inputA.line2 + \n + inputA.line3 + \n...
     *         inputB.line1 + \n + inputB.line2 + \n + inputB.line3 + \n...
     * @param arrayOfLines arrayOfLines[i] = lists of lines from input i
     * @return merged lines
     */
    @SafeVarargs
    private String mergeLinesSerial(List<String>... arrayOfLines) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < arrayOfLines.length; i++) {
            List<String> lines = arrayOfLines[i];
            for (int j = 0; j < lines.size(); j++) {
                result.append(lines.get(j));
                if (j < lines.size() - 1) {
                    result.append(TAB);
                }
            }
            if (i < arrayOfLines.length - 1) {
                result.append(STRING_NEWLINE);
            }
        }
        return result.toString();
    }

}
