package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.RmInterface;
import sg.edu.nus.comp.cs4218.exception.*;
import sg.edu.nus.comp.cs4218.impl.parser.RmArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.fileSeparator;

/**
 * Implementation of the 'rm' command application.
 */
public class RmApplication implements RmInterface {

    private final RmArgsParser parser;
    public RmApplication (RmArgsParser parser) {
        this.parser = parser;
    }

    /**
     * Runs the rm application with the specified arguments.
     *
     * @param args   Array of arguments for the application.
     * @param stdin  An InputStream.
     * @param stdout An OutputStream.
     * @throws RmException When stdout or stdin is null, or when args is null, or when args are in invalid format.
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws AbstractApplicationException {
        if (stdout == null || stdin == null) {
            throw new RmException(ERR_NULL_STREAMS);
        }
        if (args == null) {
            throw new RmException(ERR_NULL_ARGS);
        }
        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new RmException(e);
        }
        List<String> results = new ArrayList<>();
        List<String> filenames = parser.getFileNames();
        for (String file : filenames) {
            try {
                remove(parser.isEmptyFolder(), parser.isRecursive(), file);
            } catch (RmException e) {
                results.add(e.getMessage());
            }
        }
        try {
            for (String result : results) {
                stdout.write(result.getBytes());
            }
            stdout.write(STRING_NEWLINE.getBytes());
        } catch (IOException e) {
            throw new RmException(ERR_WRITE_STREAM);//NOPMD - throw here to get caught in SHell impl and show excpt msg
        }
    }

    /**
     * Removes the specified files or directories.
     *
     * @param isEmptyFolder Flag indicating if the folder to be removed is empty.
     * @param isRecursive   Flag indicating if the removal should be recursive.
     * @param fileName      Array of file names or directory paths to be removed.
     * @throws RmException When an error occurs during the removal process.
     */
    @Override
    public void remove(Boolean isEmptyFolder, Boolean isRecursive, String... fileName) throws RmException {
        if (fileName == null) {
            throw new RmException(ERR_GENERAL);
        }
        for (String file : fileName) {
            Path path;
            try {
                path = IOUtils.resolveFilePath(file);
            } catch (IOException e) {
                throw new RmException(file, ERR_FILE_NOT_FND); //NOPMD - throw here to get caught in ShellImpl and show msg
            }
            File node = path.toFile();
            if (!node.exists()) {
                throw new RmException(file, ERR_FILE_NOT_FND);
            }
            if (!node.canRead()) {
                throw new RmException(file, ERR_NO_PERM);
            }
            try {
                if (node.isDirectory()) {
                    if (isRecursive) { // If isRecursive, recursively call remove on child directories and delete files
                        for (String innerFile : Objects.requireNonNull(node.list())) {
                            remove(false, true, path + fileSeparator() + innerFile);
                        }
                        Files.delete(path);
                    } else if (isEmptyFolder && Objects.requireNonNull(node.list()).length == 0) {
                        Files.delete(path);
                    } else { // Is a directory, but no flags given
                        throw new RmException(file, isEmptyFolder ? ERR_DIR_NOT_EMPTY : ERR_IS_DIR);
                    }
                } else {
                    Files.delete(path);
                }
            } catch (IOException e) {
                throw new RmException(file, e);
            }
        }
    }
}
