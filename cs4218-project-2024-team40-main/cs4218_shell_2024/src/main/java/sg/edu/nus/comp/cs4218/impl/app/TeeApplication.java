package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.app.TeeInterface;
import sg.edu.nus.comp.cs4218.impl.parser.TeeArgsParser;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.TeeException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;



import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.List;
import java.util.Arrays;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
/**
 * Implementation of the 'tee' command application.
 */
public class TeeApplication implements TeeInterface {
    private final TeeArgsParser teeArgsParser;

    public TeeApplication(TeeArgsParser teeArgsParser) {
        this.teeArgsParser = teeArgsParser;
    }
    /**
     * Runs the mkdir application with the specified arguments.
     *
     * @param args   Array of arguments for the application. Each array element is the path to a
     *               file.
     * @param stdin  An InputStream. The input for the command is read from this InputStream
     * @param stdout An OutputStream. The output of the command is written to this OutputStream.
     * @throws TeeException when stdin or stdout is null, when args have invalid format or when there is trouble writing
     * to stdout
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws AbstractApplicationException {
        boolean isAppend;
        if (stdin == null) {
            throw new TeeException(ERR_NO_ISTREAM);
        }
        if (stdout == null) {
            throw new TeeException(ERR_NO_OSTREAM);
        }
        try {
            teeArgsParser.parse(args);
        } catch (InvalidArgsException e) {
            throw new TeeException(e.getMessage());//NOPMD - throw here to get caught in SHell impl and show excpt msg
        }
        isAppend = teeArgsParser.isAppend();
        List<String> files = teeArgsParser.getFileNames();
        String[] filenames = files.toArray(new String[0]);
        String outputToWrite = teeFromStdin(isAppend, stdin, filenames);
        try {
            stdout.write(outputToWrite.getBytes());
            stdout.write(System.lineSeparator().getBytes());
        } catch (IOException e) {
            throw new TeeException(ERR_IO_EXCEPTION);//NOPMD - throw here to get caught in SHell impl and show excpt msg
        }


    }
    /**
     * Reads from standard input and write to both the standard output and files
     *
     * @param isAppend Boolean option to append the standard input to the contents of the input files
     * @param stdin    InputStream containing arguments from Stdin
     * @param fileName Array of String of file names
     * @return String input we got from inputstream that we wish to write to outputstream
     * @throws TeeException if have null valued file names, missing isAppend/stdin arg
     */
    @Override
    public String teeFromStdin(Boolean isAppend, InputStream stdin, String... fileName) throws AbstractApplicationException{
        //can have no fileNames arg but cannot have null value
        if (fileName != null && Arrays.stream(fileName).anyMatch(fileNames -> fileNames == null)) {
            throw new TeeException(ERR_NULL_ARGS);
        }
        if(isAppend == null){
            throw new TeeException(ERR_NULL_ARGS);
        }
        if (stdin == null) {
            throw new TeeException(ERR_NO_ISTREAM);
        }
        List<String> input = getInputStream(stdin);
        if (fileName != null ){
            for (String file : fileName) {
                if(!"-".equals(file)) {
                    teeToFile(isAppend, input, file);
                }
            }
        }


        return  String.join(System.lineSeparator(), input);
    };

    /**
     * Handles the writing of content to file if a file arguement is provided in tee Command
     *
     * @param isAppend Boolean option to append the standard input to the contents of the input files
     * @param content    InputStream containing arguments from Stdin
     * @param fileName Array of String of file names
     * @return
     * @throws TeeException if no filename specified, file is a directory, no write permission to file, invalid filepath
     */
    void teeToFile(boolean isAppend, List<String> content, String fileName) throws TeeException {
        Path currentDir = Paths.get(Environment.currentDirectory);

        if (fileName.isEmpty()) {
            throw new TeeException(ERR_FILE_NOT_FND);
        }

        Path filePath = currentDir.resolve(fileName);

        if (Files.isDirectory(filePath)) {
            throw new TeeException(ERR_IS_DIR);
        }

        if (Files.exists(filePath) && !Files.isWritable(filePath)) {
            throw new TeeException(ERR_NO_PERM);
        }

        try {
            Files.write(filePath, content, CREATE, WRITE, isAppend ? APPEND : TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new TeeException(ERR_WRITING_FILE);//NOPMD - throw here to get caught in SHell impl and show excpt msg
        }
    }
    /**
     * Handles retrieveing of content from input stream
     *
     * @param stdin
     * @return List<String> , the list of lines read from inputstream
     * @throws TeeException if error reading inputstream, or null inputsream
     */
    List<String> getInputStream(InputStream stdin) throws TeeException {
        try {

            if (stdin == null) {
                throw new TeeException(ERR_NULL_STREAMS);
            }

            return IOUtils.getLinesFromInputStream(stdin);
        }
        catch (IOException e){
            throw new TeeException(ERR_READ_STREAM);//NOPMD - throw here to get caught in SHell impl and show excpt msg
        }

    }
}
