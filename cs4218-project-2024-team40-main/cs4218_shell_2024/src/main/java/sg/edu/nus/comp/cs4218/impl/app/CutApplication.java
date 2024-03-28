package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.CutInterface;
import sg.edu.nus.comp.cs4218.exception.*;

import sg.edu.nus.comp.cs4218.impl.parser.CutArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.RangeHelper;
import sg.edu.nus.comp.cs4218.impl.util.RangeHelperFactory;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
@SuppressWarnings("PMD.GodClass")
/**
 * Implementation of the {@link CutInterface} for cutting sections from lines of text.
 * This class provides methods to cut sections from files, standard input, and individual lines.
 */
public class CutApplication implements CutInterface {
    public OutputStream stdOut;
    public InputStream stdIn;
    private final CutArgsParser cutArgsParser;
    private final RangeHelperFactory rangeHelpFact;
    public CutApplication(CutArgsParser cutArgsParser, RangeHelperFactory rangeHelpFact) {
        this.cutArgsParser = cutArgsParser;
        this.rangeHelpFact = rangeHelpFact;
    }

    /**
     * Cuts sections from a file based on the provided options.
     *
     * @param file       The path to the input file.
     * @param isCharPo   Indicates whether cutting is by character position.
     * @param isBytePo   Indicates whether cutting is by byte position.
     * @param ranges     The ranges to cut from each line.
     * @return The result after cutting sections from the file.
     */
    private String cutFromFile(String file, Boolean isCharPo, Boolean isBytePo, RangeHelper ranges) {
        StringBuilder result = new StringBuilder();
        Path filePath;
        try {
            filePath = IOUtils.resolveFilePath(file);
        } catch (IOException e) {
            result.append(new CutException(ERR_FILE_NOT_FND, file).getMessage());
            result.append(StringUtils.STRING_NEWLINE);
            return result.toString();
        }
        if (!Files.exists(filePath)) {
            result.append(new CutException(ERR_FILE_NOT_FND, file).getMessage());
            result.append(StringUtils.STRING_NEWLINE);
            return result.toString();
        }
        if (Files.isDirectory(filePath)) {
            result.append(new CutException(ERR_IS_DIR, file).getMessage());
            result.append(StringUtils.STRING_NEWLINE);
            return result.toString();
        }
        try (InputStream inputStream = IOUtils.openInputStream(file)){

            List<String> lines = IOUtils.getLinesFromInputStream(inputStream);
            for (String line : lines) {
                result.append(cutFromLine(line, isCharPo, isBytePo, ranges));
            }
        } catch (ShellException e) {
            result.append(new CutException(ERR_READING_FILE, e).getMessage());
            result.append(StringUtils.STRING_NEWLINE);
        } catch (IOException e) {
            result.append(new CutException(ERR_IO_EXCEPTION, e).getMessage());
            result.append(StringUtils.STRING_NEWLINE);
        }
        return result.toString();
    }

    /**
     * Cuts sections from a line based on the provided options.
     *
     * @param line     The input line.
     * @param isCharPo Indicates whether cutting is by character position.
     * @param isBytePo Indicates whether cutting is by byte position.
     * @param ranges   The ranges to cut from the line.
     * @return The result after cutting sections from the line.
     */
    public String cutFromLine(String line, Boolean isCharPo, Boolean isBytePo, RangeHelper ranges) {
        StringBuilder result = new StringBuilder();
        if (isCharPo) {
            AtomicInteger atomicI = new AtomicInteger();
            line.codePoints().forEach(codePoint -> {
                if (ranges.contains(atomicI.get() + 1)) {
                    result.appendCodePoint(codePoint);
                }
                atomicI.addAndGet(1);
            });
        } else if (isBytePo) {
            byte[] bytes = line.getBytes(StandardCharsets.UTF_8);
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            for (int i = 0; i < bytes.length; i++) {
                if (ranges.contains(i + 1)) {
                    byteStream.write(bytes[i]);
                }
            }
            result.append(byteStream.toString(StandardCharsets.UTF_8));
        }
        if (result.length() == 0 || result.charAt(result.length() - 1) != '\n') {
            result.append(StringUtils.STRING_NEWLINE);
        }
        return result.toString();
    }
    /**
     * Sets the standard input stream for this application.
     *
     * @param stdIn The standard input stream to set.
     */
    public void setStdIn(InputStream stdIn) {
        this.stdIn = stdIn;
    }
    /**
     * Sets the standard output stream for this application.
     *
     * @param stdOut The standard output stream to set.
     */
    public void setStdOut(OutputStream stdOut) {
        this.stdOut = stdOut;
    }

    /**
     * Runs the cut application with the specified arguments.
     *
     * @param args   The command-line arguments.
     * @param stdin  The standard input stream.
     * @param stdout The standard output stream.
     * @throws AbstractApplicationException If an error occurs during application execution.
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws AbstractApplicationException {
        if (stdin == null || stdout == null) {
            throw new CutException(ERR_NULL_STREAMS);
        }
        setStdIn(stdin);
        setStdOut(stdout);
        if (args == null) {
            throw new CutException(ERR_NULL_ARGS);
        }
        try {
            cutArgsParser.parse(args);
        } catch (InvalidArgsException e) {
            throw new CutException(e);
        }
        StringBuilder output = new StringBuilder();
        RangeHelper rangeHelper;
        try {
             rangeHelper = rangeHelpFact.createRangeHelper(cutArgsParser.getRanges());
        } catch (RangeHelperException e) {
            throw new CutException(e);
        }
        try {
            if (cutArgsParser.isCutStdin()) {
                output.append(cutFromStdin(cutArgsParser.isCutByChar(), cutArgsParser.isCutByByte(),
                        rangeHelper, stdin));
                stdout.write(output.toString().getBytes());
            } else if (cutArgsParser.isCutFiles()) {
                output.append(cutFromFiles(cutArgsParser.isCutByChar(), cutArgsParser.isCutByByte(),
                        rangeHelper, cutArgsParser.getFiles()));
                stdout.write(output.toString().getBytes());
            } else {
                // This shouldn't happen but just put this for defensive programming
                throw new CutException(ERR_GENERAL);
            }
        } catch (IOException e) {
            throw new CutException(ERR_IO_EXCEPTION);//NOPMD - throw here to get caught in SHell impl and show excpt msg
        }
    }
    /**
     * Cuts out selected portions of each line
     *
     * @param isCharPo Boolean option to cut by character position
     * @param isBytePo Boolean option to cut by byte position
     * @param ranges   List of 2-element arrays containing the start and end indices for cut.
     *                 For instance, cutting on the first column would be represented using a [1,1] array.
     * @param fileName Array of String of file names
     * @return
     * @throws Exception
     */

    @Override
    public String cutFromFiles(Boolean isCharPo, Boolean isBytePo, List<int[]> ranges, String... fileName)
            throws AbstractApplicationException {
        if (isBytePo && isCharPo) {
            throw new CutException(CutArgsParser.ILLEGAL_BOTH_FLAG);
        }
        if (this.stdOut == null || this.stdIn == null) {
            throw new CutException(ERR_NULL_STREAMS);
        }
        if (fileName == null || ranges == null) {
            throw new CutException(ERR_NULL_ARGS);
        }
        if (fileName.length == 0 || ranges.isEmpty()) {
            throw new CutException(ERR_NO_ARGS);
        }

        StringBuilder result = new StringBuilder();
        for (String file : fileName) {
            if ("-".equals(file)) {
                try {
                    this.stdOut.write(result.toString().getBytes());
                    result = new StringBuilder();
                    result.append(cutFromStdin(isCharPo, isBytePo, ranges, this.stdIn));
                } catch (IOException e) {
                    throw new CutException(e);
                }
                continue;
            }
            try {
                RangeHelper rangeHelper = rangeHelpFact.createRangeHelper(ranges);
                result.append(cutFromFile(file, isCharPo, isBytePo, rangeHelper));
            } catch (RangeHelperException e) {
                throw new CutException(e);
            }
        }
        return result.toString();
    }
    /**
     * Cuts sections from standard input based on the provided options and ranges.
     *
     * @param isCharPo Indicates whether cutting is by character position.
     * @param isBytePo Indicates whether cutting is by byte position.
     * @param ranges   The ranges to cut from each input line.
     * @param stdin    The standard input stream.
     * @return The result after cutting sections from standard input.
     * @throws CutException If an error occurs during input reading or cutting.
     */
    @Override
    public String cutFromStdin(Boolean isCharPo, Boolean isBytePo, List<int[]> ranges, InputStream stdin)
            throws AbstractApplicationException {
        if (stdin == null) {
            throw new CutException(ERR_NULL_STREAMS);
        }
        if (ranges == null) {
            throw new CutException(ERR_NULL_ARGS);
        }
        if (isBytePo && isCharPo) {
            throw new CutException(CutArgsParser.ILLEGAL_BOTH_FLAG);
        }
        StringBuilder result = new StringBuilder();
        try {
            List<String> lines = IOUtils.getLinesFromInputStream(stdin);
            for (String line : lines) {
                try {
                    RangeHelper rangeHelper = rangeHelpFact.createRangeHelper(ranges);
                    result.append(cutFromLine(line, isCharPo, isBytePo, rangeHelper));
                } catch (RangeHelperException e) {
                    throw new CutException(e);
                }
            }
        } catch (IOException e) {
            throw new CutException(e);
        }
        return result.toString();
    }
}
