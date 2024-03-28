package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.Application;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.app.MkdirInterface;
import sg.edu.nus.comp.cs4218.exception.*;
import sg.edu.nus.comp.cs4218.impl.parser.MkdirArgsParser;
import sg.edu.nus.comp.cs4218.impl.parser.WcArgsParser;


import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

/**
 * Implementation of the 'mkdir' command application.
 */
public class MkdirApplication implements MkdirInterface {
    private boolean isMissingParent;
    private String filename;
    private final MkdirArgsParser mkdirArgsParser;

    public MkdirApplication(MkdirArgsParser mkdirArgsParser) {
        this.mkdirArgsParser = mkdirArgsParser;
    }
    /**
     * Runs the mkdir application with the specified arguments.
     *
     * @param args   Array of arguments for the application. Each array element is the path to a
     *               file.
     * @param stdin  An InputStream. Not used
     * @param stdout An OutputStream. Not used
     * @throws MkdirException when args is null or empty, or when args are in invalid format
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws AbstractApplicationException {
        if (stdout == null || stdin == null) {
            throw new MkdirException(ERR_NULL_STREAMS);
        }
        if (args == null || args.length == 0) {
            throw new MkdirException(ERR_MISSING_ARG);
        }

        Path currentDir = Paths.get(Environment.currentDirectory);


        try {
            mkdirArgsParser.parse(args);
        } catch (InvalidArgsException e) {
            throw new MkdirException(e.getMessage());//NOPMD - throw here to get caught in SHell impl and show excpt msg
        }
        if (mkdirArgsParser.isCreateMissingParent()){
            setCreateParents();
        }
        List<String> parsedFilePaths = mkdirArgsParser.getFileNames();
        for (String arg : parsedFilePaths) {
            filename = arg;
            Path folderPath = Paths.get(arg);
            if(!folderPath.isAbsolute()) {
                folderPath = currentDir.resolve(folderPath);
            }
            createFolder(folderPath.toString());
        }


    }
    /**
     * Create folder from the given folder names. Do nothing if folder already exists. If folder
     * name is a path format, create the folder that satisfies the path specification.
     *
     * @param folderName Array of string of folder names to be created
     * @throws MkdirException   when -p option not present and folder/directory created already exists or intermediate
     * parent does not exist
     */
    @Override
    public void createFolder(String... folderName) throws AbstractApplicationException {
        for (String folderPath : folderName) {
            File directory = new File(folderPath);
            if (isMissingParent) {
                directory.mkdirs();

            } else {
                if(!directory.mkdir()){
                    if(directory.exists()) {
                        throw new MkdirException("cannot create directory '" + this.getFilename() + "': " + ERR_FILE_EXISTS );
                    }
                    else {
                        throw new MkdirException("cannot create directory '" + this.getFilename() + "': No such file or directory");
                    }
                }
            }
        }
    }
    /**
     * Sets the flag to create missing parents.
     */
    public void setCreateParents() {
        this.isMissingParent = true;
    }
    /**
     * Retrieves the filename.
     *
     * @return The filename.
     */
    public String getFilename() {
        return filename;
    }
}
