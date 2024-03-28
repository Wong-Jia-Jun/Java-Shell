package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.CatInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CatException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.parser.CatArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

/**
 * The cat command concatenates the content of given files and prints on the standard output.
 * If no files are specified, it reads from standard input.
 * Command Format - cat [OPTIONS] [FILE]
 * FILE - Name of the file or '-' for standard input. If no files are specified, use standard input.
 * OPTIONS
 * -n : number all output lines
 */
@SuppressWarnings("PMD.GodClass")
public class CatApplication implements CatInterface {
    
    public static final String LINE_NUM_PREFIX = "     ";
    public static final String LINE_NUM_SUFFIX = "  ";
    public  OutputStream stdout;
    private final CatArgsParser catArgsParser;

    public CatApplication(CatArgsParser catArgsParser) {
        this.catArgsParser = catArgsParser;
    }

    public void setStdout(OutputStream stdout) {
        this.stdout = stdout;
    }

    /**
     * Runs the cat application with the specified arguments.
     *
     * @param args   Array of arguments for the application. Each array element is the path to a
     *               file. If no files are specified stdin is used.
     * @param stdin  An InputStream. The input for the command is read from this InputStream if no
     *               files are specified.
     * @param stdout An OutputStream. The output of the command is written to this OutputStream.
     * @throws CatException If the file(s) specified do not exist or are unreadable.
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws AbstractApplicationException {
        if (stdin == null || stdout == null) {
            throw new CatException(ERR_NULL_STREAMS);
        }
        setStdout(stdout);
        if (args == null) {
            throw new CatException(ERR_NULL_ARGS);
        }
        try {
            catArgsParser.parse(args);
        } catch (InvalidArgsException e) {
            throw new CatException(e);
        }
        StringBuilder output = new StringBuilder();
        try {
            if (catArgsParser.isCatStdin()) {
                output.append(catStdin(catArgsParser.isShowLineNumber(), stdin));
                stdout.write(output.toString().getBytes());
            } else if (catArgsParser.isCatFileAndStdin()) {
                output.append(catFileAndStdin(catArgsParser.isShowLineNumber(), stdin,
                        catArgsParser.getFileNames().toArray(new String[0])));
                stdout.write(output.toString().getBytes());
            } else if (catArgsParser.isCatFiles()) {
                output.append(catFiles(catArgsParser.isShowLineNumber(),
                        catArgsParser.getFileNames().toArray(new String[0])));
                stdout.write(output.toString().getBytes());
            } else {
                // This shouldn't happen but just put this for defensive programming
                throw new CatException(ERR_GENERAL);
            }
        } catch (IOException e) {
            throw new CatException(ERR_IO_EXCEPTION, e);
        }
    }

    /**
     * The function `addLineNumber` appends a line number with prefixes and suffixes to a
     * `StringBuilder` output.
     * 
     * @param output The `output` parameter is a `StringBuilder` object that represents the text
     * content to which a line number will be added.
     * @param lineNumber The `lineNumber` parameter is an integer value representing the line number
     * that you want to append to the output StringBuilder.
     * @return The method is returning a `StringBuilder` object after appending the line number with
     * prefixes and suffixes to the existing `StringBuilder` output.
     */
    private StringBuilder addLineNumber(StringBuilder output, int lineNumber) {
        return output.append(LINE_NUM_PREFIX).append(lineNumber).append(LINE_NUM_SUFFIX);
    }

    /**
     * This Java function reads the content of files specified by their file names, optionally adding
     * line numbers to each line, and returns the concatenated content as a string.
     * 
     * @param isLineNumber The `isLineNumber` parameter in the `catFiles` method is a boolean flag that
     * determines whether line numbers should be included in the output. If `isLineNumber` is `true`,
     * line numbers will be added to each line of the file content. If `isLineNumber` is `false`,
     * @return The method `catFiles` returns a `String` containing the contents of the files specified
     * in the `fileName` parameter. The contents can be concatenated with line numbers if the
     * `isLineNumber` parameter is set to `true`.
     * @throws CatException when null/empty file name or when error reading file or io exceptions
     */
    @Override
    public String catFiles(Boolean isLineNumber, String... fileName) throws AbstractApplicationException {
        if (fileName == null) {
            throw new CatException(ERR_NULL_ARGS);
        }
        if (fileName.length == 0) {
            throw new CatException(ERR_NO_ARGS);
        }
        StringBuilder result = new StringBuilder();
        for (String file : fileName) {
            if (StringUtils.isBlank(file)) {
                result.append(new CatException(ERR_NO_FILE_ARGS).getMessage()).append(StringUtils.STRING_NEWLINE);
                continue;
            }
            Path filePath;
            try {
                filePath = IOUtils.resolveFilePath(file);
            } catch (IOException e) {
                result.append(new CatException(ERR_FILE_NOT_FND, file).getMessage()).append(StringUtils.STRING_NEWLINE);
                continue;
            }
            if (!Files.exists(filePath)) {
                result.append(new CatException(ERR_FILE_NOT_FND, file).getMessage()).append(StringUtils.STRING_NEWLINE);
                continue;
            }
            if (Files.isDirectory(filePath)) {
                result.append(new CatException(ERR_IS_DIR, file).getMessage()).append(StringUtils.STRING_NEWLINE);
                continue;
            }
            try (InputStream inputStream = IOUtils.openInputStream(file)){
                List<String> lines = IOUtils.getLinesFromInputStream(inputStream);
                int lineNumber = 1;
                for (String line : lines) {
                    if (isLineNumber) {
                        result = addLineNumber(result, lineNumber);
                        result.append(line).append(StringUtils.STRING_NEWLINE);
                        lineNumber++;
                    } else {
                        result.append(line).append(StringUtils.STRING_NEWLINE);
                    }
                }
            } catch (ShellException | IOException e) {
                result.append(new CatException(file, e).getMessage()).append(StringUtils.STRING_NEWLINE);
            }
        }
        return result.toString();
    }

    /**
     * This Java function reads input from a given InputStream, optionally adding line numbers to each
     * line, and returns the concatenated result as a String.
     * 
     * @param isLineNumber The `isLineNumber` parameter is a boolean flag that determines whether line
     * numbers should be included in the output. If `isLineNumber` is `true`, line numbers will be
     * added to each line of input. If `isLineNumber` is `false`, line numbers will not be included in
     * the output
     * @param stdin The `stdin` parameter in the `catStdin` method represents the standard input stream
     * from which the method reads input. This input stream can be provided by the user or another
     * program and typically contains text or data that the method will process.
     * @return The method `catStdin` is returning a `String` that contains the contents read from the
     * `InputStream stdin`. The content may or may not include line numbers based on the value of the
     * `isLineNumber` parameter.
     * @throws  CatException when io exception or null inputstream
     */
    @Override
    public String catStdin(Boolean isLineNumber, InputStream stdin) throws AbstractApplicationException {
        if (stdin == null) {
            throw new CatException(ERR_NULL_STREAMS);
        }
        StringBuilder result = new StringBuilder();
        try {
            List<String> lines = IOUtils.getLinesFromInputStream(stdin);
            int lineNumber = 1;
            for (String line : lines) {
                if (isLineNumber) {
                    result = addLineNumber(result, lineNumber);
                    result.append(line).append(StringUtils.STRING_NEWLINE);
                    lineNumber++;
                } else {
                    result.append(line).append(StringUtils.STRING_NEWLINE);
                }
            }
        } catch (IOException e) {
            throw new CatException(ERR_IO_EXCEPTION, e);
        }
        return result.toString();
    }

    /**
     * The function `catFileAndStdin` reads and concatenates the contents of files and standard input if '-'
     * is in arguments, optionally displaying line numbers.
     * 
     * @param isLineNumber The `isLineNumber` parameter is a boolean flag that determines whether line
     * numbers should be displayed when reading and concatenating files. If `isLineNumber` is set to
     * `true`, line numbers will be included in the output; if set to `false`, line numbers will not be
     * included.
     * @param stdin The `stdin` parameter in the `catFileAndStdin` method is an `InputStream` that
     * represents the standard input stream. It is used to read input from the console or another
     * source.
     * @return The method `catFileAndStdin` returns a `String` containing the concatenated contents of
     * the contents of a file after the last stdin input, if any. All contents before this is printed already.
     * @throws CatException when null streams, No files or error writing to stream/IO exception
     */
    @Override
    public String catFileAndStdin(Boolean isLineNumber, InputStream stdin, String... fileName)
            throws AbstractApplicationException {
        if (fileName == null) {
            throw new CatException(ERR_NULL_ARGS);
        }
        if (stdin == null) {
            throw new CatException(ERR_NULL_STREAMS);
        }
        if (fileName.length == 0) {
            throw new CatException(ERR_NO_ARGS);
        }
        if (stdout == null) {
            throw new CatException(ERR_NULL_STREAMS);
        }

        StringBuilder result = new StringBuilder();
        for (String file : fileName) {
            try {
                if ("-".equals(file)) {
                    // Print file contents before stdin
                    stdout.write(result.toString().getBytes());
                    result = new StringBuilder();

                    result.append(catStdin(isLineNumber, stdin));
                } else {
                    result.append(catFiles(isLineNumber, file));
                }
            } catch (IOException e) {
                throw new CatException(ERR_WRITE_STREAM);//NOPMD - throw here to get caught in SHell impl and show excpt msg
            }
        }
        return result.toString();
    }
}
