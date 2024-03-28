package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.app.CdInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CdException;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

/**
 * Implementation of the {@link CdInterface} for changing the current directory.
 * This class provides methods to change the current directory and run the cd application.
 */
public class CdApplication implements CdInterface {

    /**
     * Changes the current directory to the specified path.
     *
     * @param path The path to change to.
     * @throws AbstractApplicationException If an error occurs during directory change.
     */
    @Override
    public void changeToDirectory(String path) throws AbstractApplicationException {
        Environment.currentDirectory = getNormalizedAbsolutePath(path);
    }

    /**
     * Runs the cd application with the specified arguments.
     * Assumption: The application must take in one argument (cd without args is not supported).
     *
     * @param args   Array of arguments for the application.
     * @param stdin  An InputStream, not used.
     * @param stdout An OutputStream, not used.
     * @throws CdException If there are errors in the arguments or during directory change.
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout)
            throws AbstractApplicationException {
        if (args == null) {
            throw new CdException(ERR_NULL_ARGS);
        }

        if (args.length > 1) {
            throw new CdException(ERR_TOO_MANY_ARGS);
        }

        if (stdin == null || stdout == null) {
            throw new CdException(ERR_NULL_STREAMS);
        }
        if (args.length != 0) {
            changeToDirectory(args[0]);
        }
    }

    /**
     * Retrieves the normalized absolute path of the given input path string.
     *
     * @param pathStr The input path string.
     * @return The normalized absolute path as a string.
     * @throws CdException If no arguments are provided, the file is not found, or the path is not a directory.
     * @throws AbstractApplicationException If an error occurs during path normalization.
     */
     String getNormalizedAbsolutePath(String pathStr) throws AbstractApplicationException {
        Path path = new File(pathStr).toPath();
        if (!path.isAbsolute()) {
            path = Paths.get(Environment.currentDirectory, pathStr);
        }

        if (!Files.exists(path)) {
            throw new CdException(ERR_FILE_NOT_FND + " " + pathStr);
        }

        if (!Files.isDirectory(path)) {
            throw new CdException(ERR_IS_NOT_DIR + " " + pathStr);
        }

        if (!Files.isExecutable(path)) {
            throw new CdException(ERR_NO_PERM + " " + pathStr);
        }

        return path.normalize().toString();
    }
}

