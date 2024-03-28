package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.UniqInterface;
import sg.edu.nus.comp.cs4218.exception.*;
import sg.edu.nus.comp.cs4218.impl.parser.UniqArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
/**
 * Implementation of the 'uniq' command application.
 */
@SuppressWarnings("PMD.GodClass")
public class UniqApplication implements UniqInterface {

    private final UniqArgsParser uniqArgsParser;
    InputStream stdin;
    private final static String TAB = "\t";

    public UniqApplication(UniqArgsParser parser) {
        this.uniqArgsParser = parser;
    }

    /**
     * Runs the unique application with the specified arguments.
     *
     * @param args   Array of arguments for the application. Each array element is the path to a
     *               file or stdin. If no files are specified stdin is used.
     * @param stdin  An InputStream. The input for the command is read from this InputStream if no
     *               files are specified.
     * @param stdout An OutputStream. The output of the command is written to this OutputStream.
     * @throws UniqueException If an error occurs during the execution, including null input stream , IO exceptions,
     * invalid files
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws AbstractApplicationException{
        String output;
        this.stdin = stdin;

        if (stdout == null || stdin == null) {
            throw new UniqueException(ERR_NULL_STREAMS);
        }
        try {
            uniqArgsParser.parse(args);
            // Can only take in 1 input file and 1 outputfile
            if (uniqArgsParser.getFileNames().size() > 2) {
                throw new UniqueException(ERR_EXTRA_OPERAND + String.format("'%s'", uniqArgsParser.getFileNames().get(2)));
            }
        } catch (InvalidArgsException e) {
            throw new UniqueException(e.getMessage());//NOPMD - throw here to get caught in SHell impl and show excpt msg
        }
        boolean isCount = uniqArgsParser.isCount();
        boolean isRepeated = uniqArgsParser.isRepeated();
        boolean isAllRepeated = uniqArgsParser.isAllRepeated();
        try {
             output = uniqHelper(isCount, isRepeated, isAllRepeated, stdin,
                    uniqArgsParser.getInputFile(), uniqArgsParser.getOutputFile());
        }
            catch (IOException e){
                output = new UniqueException(ERR_FILE_NOT_FND).getMessage();
            }

            if (uniqArgsParser.getOutputFile() != null) {
                return;
            }
            try {
                stdout.write(output.getBytes());
                stdout.write(System.lineSeparator().getBytes());
            } catch (IOException e) {
                throw new UniqueException(ERR_WRITE_STREAM);//NOPMD - throw here to get caught in shell impl
            }




    }
     private String uniqHelper(boolean isCount, boolean isRepeated, boolean isAllRepeated, InputStream stdin,
                                String inputFilename, String outputFileName) throws AbstractApplicationException, IOException {
        if (inputFilename == null || inputFilename.equals("-")) {
            return uniqFromStdin(isCount, isRepeated, isAllRepeated, stdin, outputFileName);
        } else {
            return uniqFromFile(isCount, isRepeated, isAllRepeated, inputFilename, outputFileName);
        }
    }
    /**
     * Filters adjacent matching lines from INPUT_FILE or standard input and writes to an OUTPUT_FILE or to standard output.
     *
     * @param isCount        Boolean option to prefix lines by the number of occurrences of adjacent duplicate lines
     * @param isRepeated     Boolean option to print only duplicate lines, one for each group
     * @param isAllRepeated  Boolean option to print all duplicate lines (takes precedence if isRepeated is set to true)
     * @param inputFileName  of path to input file
     * @param outputFileName of path to output file (if any)
     * @throws Exception
     */
    @Override
    public String uniqFromFile(Boolean isCount, Boolean isRepeated, Boolean isAllRepeated, String inputFileName, String outputFileName) throws AbstractApplicationException{
        if(inputFileName == null || isCount == null || isRepeated == null || isAllRepeated == null) {
            throw new UniqueException(ERR_NULL_ARGS);
        }
        List<String> fileLines = new ArrayList<>();
        String output;
        try{
            File node = IOUtils.resolveFilePath(inputFileName).toFile();
            if (!node.exists()) {
                String errorMessage = String.format("%s: %s", inputFileName, ERR_FILE_NOT_FND);
                throw new UniqueException(errorMessage);
            }
            if (node.isDirectory()) {
                String errorMessage = String.format("error reading '%s' %s", inputFileName, ERR_IS_DIR_FILE);
                throw new UniqueException(errorMessage);
            }
            if (!node.canRead()) {
                throw new UniqueException(ERR_NO_PERM);
            }
        }
        catch (IOException e){
            throw new UniqueException(ERR_FILE_NOT_FND); //NOPMD - caught in shellimpl
        }
        try (InputStream input = IOUtils.openInputStream(inputFileName)) {
            fileLines.addAll(IOUtils.getLinesFromInputStream(input));
            IOUtils.closeInputStream(input);
            output = uniqInput(isCount, isRepeated, isAllRepeated, fileLines, outputFileName);
            if (outputFileName != null) {
                uniqToFile(output, outputFileName);

            }
        }
        catch (ShellException e) {
            output = new UniqueException(ERR_CLOSE_STREAMS).getMessage();
        }
        catch (IOException e) {
            output = new UniqueException(ERR_IO_EXCEPTION).getMessage();
        }



        return  output;
    }
    /**
     * Filters adjacent matching lines from INPUT_FILE or standard input and writes to an OUTPUT_FILE or to standard output.
     *
     * @param isCount       Boolean option to prefix lines by the number of occurrences of adjacent duplicate lines
     * @param isRepeated    Boolean option to print only duplicate lines, one for each group
     * @param isAllRepeated Boolean option to print all duplicate lines (takes precedence if isRepeated is set to true)
     * @param stdin         InputStream containing arguments from Stdin
     * @param outputFileName of path to output file (if any)
     * @throws Exception
     */
    @Override
    public String uniqFromStdin(Boolean isCount, Boolean isRepeated, Boolean isAllRepeated, InputStream stdin, String outputFileName) throws AbstractApplicationException{
        List<String> lines = new ArrayList<>();
        String output;
        if (stdin == null || isCount == null || isRepeated == null || isAllRepeated == null) {
            throw new UniqueException(ERR_NULL_ARGS);
        }
        try {
             lines.addAll(IOUtils.getLinesFromInputStream(stdin));
            output = uniqInput(isCount, isRepeated, isAllRepeated, lines, outputFileName);
            if (outputFileName != null) {
                uniqToFile(output, outputFileName);
            }
        }
        catch (IOException e){
            output = new UniqueException(ERR_IO_EXCEPTION).getMessage();
        }



        return output;
    }
    public String uniqInput(Boolean isCount, Boolean isRepeated, Boolean isAllRepeated, List<String> input, String outputFileName) throws UniqueException{//NOPMD - for processing uniq lines
        String output = "";
        List<String> lines = new ArrayList<>();
        List<Integer> count = new ArrayList<>();
        int counter = 0;
        String current = "";
        for (String s : input) {
            if (current.isEmpty()) {
                current = s;
                counter = 1;
                continue;
            }

            if (current.equals(s)) {
                counter += 1;
            } else {
                lines.add(current);
                count.add(counter);
                current = s;
                counter = 1;
            }
        }
        lines.add(current);
        count.add(counter);

        // different combination of flags, -D takes precedence
        if (isAllRepeated) {
            if (isCount) {
                throw new UniqueException("printing all duplicated lines and repeat counts is meaningless");
            } else {
                for (int i = 0; i < lines.size(); i++) {
                    if (count.get(i) > 1) {
                        for (int j = 0; j < count.get(i); j++) {
                            output += lines.get(i) + System.lineSeparator();
                        }
                    }
                }
            }
        } else if (isRepeated) {
            if (isCount) {
                for (int i = 0; i < lines.size(); i++) {
                    if (count.get(i) > 1) {
                        output += TAB + count.get(i) + " " + lines.get(i) + System.lineSeparator();
                    }
                }
            } else {
                for (int i = 0; i < lines.size(); i++) {
                    if (count.get(i) > 1) {
                        output += lines.get(i) + System.lineSeparator();
                    }
                }
            }
        } else if (isCount) {
            for (int i = 0; i < lines.size(); i++) {
                output += TAB + count.get(i) + " " + lines.get(i) + System.lineSeparator();
            }
        } else { // default format
            for (int i = 0; i < lines.size(); i++) {
                output += lines.get(i) + System.lineSeparator();
            }
        }

        return output.length() > 0 ? output.substring(0, output.length() - System.lineSeparator().length()) : output;
    }
    public void uniqToFile(String content, String fileName) throws UniqueException, IOException {

            Path filePath = IOUtils.resolveFilePath(fileName);

            if (Files.isDirectory(filePath)) {
                throw new UniqueException(ERR_IS_DIR);
            }

            if (Files.exists(filePath) && !Files.isWritable(filePath)) {
                throw new UniqueException(ERR_NO_PERM);
            }

            try {
                if (!Files.exists(filePath)) {
                    Files.createFile(filePath);
                }
                Files.write(filePath, content.getBytes(), CREATE, WRITE, TRUNCATE_EXISTING);
            } catch (IOException e) {
                throw new UniqueException(ERR_WRITING_FILE);//NOPMD - throw here to get caught in shell impl
            }

    }
}
