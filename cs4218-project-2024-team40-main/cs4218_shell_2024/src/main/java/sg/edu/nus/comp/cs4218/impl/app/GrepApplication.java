package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.GrepInterface;
import sg.edu.nus.comp.cs4218.exception.*;
import sg.edu.nus.comp.cs4218.impl.parser.GrepArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
@SuppressWarnings("PMD.GodClass")
public class GrepApplication implements GrepInterface {
    public static final String EMPTY_PATTERN = "Pattern should not be empty.";

    private final GrepArgsParser grepArgsParser;
    private OutputStream outputStream;

    public GrepApplication(GrepArgsParser grepArgsParser) {
        this.grepArgsParser = grepArgsParser;
    }

    /**
     * Sets the output stream for the application.
     * @param outputStream
     */
    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    /**
     * Runs the grep application with the specified arguments.
     * @param args
     * @param stdin
     * @param stdout
     * @throws AbstractApplicationException
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws AbstractApplicationException {
        if (stdin == null || stdout == null) {
            throw new GrepException(ERR_NULL_STREAMS);
        }
        setOutputStream(stdout);
        if (args == null) {
            throw new GrepException(ERR_NULL_ARGS);
        }
        try {
            grepArgsParser.parse(args);
        } catch (InvalidArgsException e) {
            throw new GrepException(e);
        }
        StringBuilder output = new StringBuilder();
        if (grepArgsParser.isGrepFromStdin()) {
            output.append(grepFromStdin(grepArgsParser.getPattern(), grepArgsParser.isIgnoreCase(), grepArgsParser.isCount(), grepArgsParser.isPrintFileName(), stdin));
        } else if (grepArgsParser.isGrepFromFiles()) {
            output.append(grepFromFiles(grepArgsParser.getPattern(), grepArgsParser.isIgnoreCase(), grepArgsParser.isCount(), grepArgsParser.isPrintFileName(), grepArgsParser.getFileNames()));
        } else if (grepArgsParser.isGrepFromFilesAndStdin()) {
            output.append(grepFromFileAndStdin(grepArgsParser.getPattern(), grepArgsParser.isIgnoreCase(), grepArgsParser.isCount(), grepArgsParser.isPrintFileName(), stdin, grepArgsParser.getFileNames()));
        } else {
            // Should not reach here
            throw new GrepException(ERR_GENERAL);
        }
        try {
            output.append(STRING_NEWLINE);
            stdout.write(output.toString().getBytes());
        } catch (IOException e) {
            throw new GrepException(e);
        }
    }

    /**
     * Returns the result of running grep with the specified arguments on the specified input stream.
     * @param pattern           String specifying a regular expression in JAVA format
     * @param isCaseInsensitive Boolean option to perform case insensitive matching
     * @param isCountLines      Boolean option to only write out a count of matched lines
     * @param isPrefixFileName  Boolean option to print file name with output lines
     * @param stdin             InputStream containing arguments from Stdin
     * @return
     * @throws AbstractApplicationException
     */
    @Override
    public String grepFromStdin(String pattern, Boolean isCaseInsensitive, Boolean isCountLines, Boolean isPrefixFileName, InputStream stdin) throws AbstractApplicationException {
        if (stdin == null) {
            throw new GrepException(ERR_NULL_STREAMS);
        }
        if (StringUtils.isBlank(pattern)) {
            throw new GrepException(EMPTY_PATTERN);
        }
        try {
            List<String> lines = IOUtils.getLinesFromInputStream(stdin);
            return getGrepResultsFromOneLines(isCaseInsensitive, isCountLines, isPrefixFileName, pattern, lines, "(standard input)");
        } catch (IOException e) {
            throw new GrepException(e);
        }
    }

    /**
     * Returns the result of running grep with the specified arguments on the specified files.
     * @param pattern           String specifying a regular expression in JAVA format
     * @param isCaseInsensitive Boolean option to perform case insensitive matching
     * @param isCountLines      Boolean option to only write out a count of matched lines
     * @param isPrefixFileName  Boolean option to print file name with output lines
     * @param fileNames         Array of file names (not including "-" for reading from stdin)
     * @return
     * @throws AbstractApplicationException
     */
    @Override
    public String grepFromFiles(String pattern, Boolean isCaseInsensitive, Boolean isCountLines, Boolean isPrefixFileName, String... fileNames) throws AbstractApplicationException {
        if (fileNames == null || fileNames.length == 0) {
            throw new GrepException(ERR_NULL_ARGS);
        }
        if (StringUtils.isBlank(pattern)) {
            throw new GrepException(EMPTY_PATTERN);
        }

        StringJoiner results = new StringJoiner(STRING_NEWLINE);
        boolean shouldHavePrefix = isPrefixFileName || fileNames.length > 1;
        for (int i = 0; i < fileNames.length; i++) {
            if (StringUtils.isBlank(fileNames[i])) {
                results.add(new GrepException(ERR_NO_FILE_ARGS).getMessage());
                continue;
            }
            Path filePath;
            try {
                filePath = IOUtils.resolveFilePath(fileNames[i]);
            } catch (IOException e) {
                results.add(new GrepException(fileNames[i], ERR_FILE_NOT_FND).getMessage());
                continue;
            }
            if (!Files.exists(filePath)) {
                results.add(new GrepException(fileNames[i], ERR_FILE_NOT_FND).getMessage());
                continue;
            }
            if (Files.isDirectory(filePath)) {
                results.add(new GrepException(fileNames[i], ERR_IS_DIR).getMessage());
                continue;
            }
            try (InputStream inputStream = IOUtils.openInputStream(fileNames[i])) {
                List<String> lines = IOUtils.getLinesFromInputStream(inputStream);
                String grepResults = getGrepResultsFromOneLines(isCaseInsensitive, isCountLines, shouldHavePrefix, pattern, lines, fileNames[i]);
                if (!StringUtils.isBlank(grepResults)) {
                    results.add(grepResults);
                }
            } catch (IOException | ShellException e) {
                results.add(new GrepException(fileNames[i], e).getMessage());
            }
        }
        return results.toString();
    }

    /**
     * Returns the result of running grep with the specified arguments on the specified files and input stream.
     * @param pattern           String specifying a regular expression in JAVA format
     * @param isCaseInsensitive Boolean option to perform case insensitive matching
     * @param isCountLines      Boolean option to only write out a count of matched lines
     * @param isPrefixFileName  Boolean option to print file name with output lines
     * @param stdin             InputStream containing arguments from Stdin
     * @param fileNames         Array of file names (including "-" for reading from stdin)
     * @return
     * @throws AbstractApplicationException
     */
    @Override
    public String grepFromFileAndStdin(String pattern, Boolean isCaseInsensitive, Boolean isCountLines, Boolean isPrefixFileName, InputStream stdin, String... fileNames) throws AbstractApplicationException {
        if (fileNames == null || fileNames.length == 0) {
            throw new GrepException(ERR_NULL_ARGS);
        }
        if (StringUtils.isBlank(pattern)) {
            throw new GrepException(EMPTY_PATTERN);
        }
        if (stdin == null) {
            throw new GrepException(ERR_NULL_STREAMS);
        }

        StringJoiner results = new StringJoiner(STRING_NEWLINE);
        boolean shouldHavePrefix = isPrefixFileName || fileNames.length > 1;
        for (String fileName : fileNames) {
            if ("-".equals(fileName)) {
                try {
                    String maybeNewline = results.toString().isEmpty() ? "" : STRING_NEWLINE;
                    outputStream.write((results + maybeNewline).getBytes());
                    results = new StringJoiner(STRING_NEWLINE);
                    String stdinResults  = grepFromStdin(pattern, isCaseInsensitive, isCountLines, shouldHavePrefix, stdin);
                    if (!StringUtils.isBlank(stdinResults)) {
                        results.add(stdinResults);
                    }
                } catch (IOException e) {
                    throw new GrepException(e);
                }
            } else {
                String fileResult = grepFromFiles(pattern, isCaseInsensitive, isCountLines, shouldHavePrefix, fileName);
                results.add(fileResult);
            }
        }
        return results.toString();
    }

    private String getGrepResultsFromOneLines(boolean isCaseInsensitive, boolean isCountLines, boolean isPrefixFileName, String pattern, List<String> lines, String fileName)
            throws AbstractApplicationException{
        Pattern compiledPattern;
        try {
            if (isCaseInsensitive) {
                compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            } else {
                compiledPattern = Pattern.compile(pattern);
            }
        } catch (IllegalArgumentException e) {
            throw new GrepException(e);
        }
        StringJoiner stringJoiner = new StringJoiner(STRING_NEWLINE);
        int count = 0;
        for (String line : lines) {
            Matcher matcher = compiledPattern.matcher(line);
            if (matcher.find()) { // match
                String linePrefix = isPrefixFileName ? fileName + ":" : "";
                stringJoiner.add(linePrefix + line);
                count++;
            }
        }
        if (isCountLines) {
            return isPrefixFileName ? fileName + ":" + count : Integer.toString(count);
        } else {
            return stringJoiner.toString();
        }
    }
}
