package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.app.LsInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.LsException;
import sg.edu.nus.comp.cs4218.impl.parser.LsArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.*;

/**
 * Implementation of the 'ls' command application.
 */
@SuppressWarnings("PMD.GodClass")
public class LsApplication implements LsInterface {
    private final LsArgsParser parser;
    private final static String PATH_CURR_DIR = STRING_CURR_DIR + CHAR_FILE_SEP;

    public LsApplication(LsArgsParser parser) {
        this.parser = parser;
    }

    /**
     * Lists the content of the specified folder(s) with optional recursive and sorting options.
     *
     * @param isRecursive Indicates whether to list content recursively.
     * @param isSortByExt Indicates whether to sort content by file extension.
     * @param folderName  The name(s) of the folder(s) to list.
     * @return The content of the specified folder(s) formatted as a string.
     * @throws AbstractApplicationException If an error occurs during the listing process.
     */

    @Override
    public String listFolderContent(Boolean isRecursive, Boolean isSortByExt,
                                    String... folderName) throws AbstractApplicationException {
        if (folderName == null) {
            throw new LsException(ERR_NULL_ARGS);
        }
        if (folderName.length == 0 && !isRecursive) {
            return listCwdContent(isSortByExt);
        }
        if (folderName.length == 1 && !isRecursive) { // Check edge case where only arg is cur dir
            Path argPath = resolvePath(folderName[0]);
            if (argPath.equals(Paths.get(Environment.currentDirectory))) {
                return listCwdContent(isSortByExt);
            }
        }

        List<Path> paths;
        if (folderName.length == 0 && isRecursive) {
            String[] directories = new String[1];
            directories[0] = Environment.currentDirectory;
            paths = resolvePaths(directories);
        } else {
            paths = resolvePaths(folderName);
        }

        return buildResult(paths, isRecursive, isSortByExt, 0).trim();
    }
    /**
     * Runs the 'ls' command application with the specified arguments.
     *
     * @param args   The command-line arguments.
     * @param stdin  The input stream, not used.
     * @param stdout The output stream to write the result.
     * @throws LsException If an error occurs during execution.
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout)
            throws AbstractApplicationException {
        if (args == null) {
            throw new LsException(ERR_NULL_ARGS);
        }

        if (stdin == null) {
            throw new LsException(ERR_NO_ISTREAM);
        }
        if (stdout == null) {
            throw new LsException(ERR_NO_OSTREAM);
        }

        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new LsException(e.getMessage());//NOPMD - throw here to get caught in SHell impl and show excpt msg
        }

        Boolean recursive = parser.isRecursive();
        Boolean sortByExt = parser.isSortByExt();
        String[] directories = parser.getDirectories()
                .toArray(new String[parser.getDirectories().size()]);
        String result = listFolderContent(recursive, sortByExt, directories);

        try {
            stdout.write(result.getBytes());
            stdout.write(StringUtils.STRING_NEWLINE.getBytes());
        } catch (IOException e) {
            throw new LsException(ERR_WRITE_STREAM);//NOPMD - throw here to get caught in SHell impl and show excpt msg
        }
    }

    /**
     * Lists the content of the current working directory. does not account for recursive mode in cwd
     *
     * @param isSortByExt Indicates whether to sort content by file extension.
     * @return The content of the current working directory formatted as a string.
     * @throws AbstractApplicationException If an error occurs during the listing process.
     */
    private String listCwdContent(Boolean isSortByExt) throws AbstractApplicationException {
        String cwd = Environment.currentDirectory;
        try {
            return formatContents(getContents(Paths.get(cwd)), isSortByExt);
        } catch (InvalidDirectoryException | NonExistentDirectoryException e) {
            // Shouldn't happen as we are using the current directory
            throw new LsException("Unexpected error occurred!");//NOPMD - throw here to get caught in SHell impl and show excpt msg
        }
    }

    /**
     * Builds the resulting string to be written into the output stream.
     * <p>
     * NOTE: This is recursively called if user wants recursive mode.
     *
     * @param paths         - list of java.nio.Path objects to list
     * @param isRecursive   - recursive mode, repeatedly ls the child directories
     * @param isSortByExt - sorts folder contents alphabetically by file extension (characters after the last ‘.’ (without quotes)). Files with no extension are sorted first.
     * @param depth - we only care if depth = 0, if there is a non dir file, we want to print them
     * @return String to be written to output stream.
     */
    private String buildResult(List<Path> paths, Boolean isRecursive, Boolean isSortByExt, int depth) {
        StringBuilder result = new StringBuilder();
        StringBuilder depth0Files = new StringBuilder();
        for (Path path : paths) {
            try {
                if (depth == 0 && !Files.isDirectory(path) && Files.exists(path)) {
                    // If user inputed files in the original ls cmd arg, the file should be printed also
                    depth0Files.append(path.getFileName().toString());
                    depth0Files.append(CHAR_SPACE);
                    continue;
                }
                List<Path> contents = getContents(path);
                String formatted = formatContents(contents, isSortByExt);
                String relativePath = getRelativeToCwd(path).toString();
                result.append(StringUtils.isBlank(relativePath) ? PATH_CURR_DIR : relativePath);
                result.append(':').append(STRING_NEWLINE);
                result.append(formatted);

                if (!formatted.isEmpty()) {
                    // Empty directories should not have an additional new line
                    result.append(STRING_NEWLINE);
                }
                result.append(STRING_NEWLINE);

                // RECURSE!
                if (isRecursive) {
                    result.append(buildResult(contents, isRecursive, isSortByExt, depth + 1));
                }
            } catch (InvalidDirectoryException e) {
                // This is thrown when getContents is called on a file
                // Case 1: not isRecursive - Users allowed to ls actual files, handled in if depth == 0..., this wont get thrown
                // Case 2: isRecursive, depth = 0 - Users allowed to ls actual files, handled in if depth == 0..., this wont get thown
                // Case 3: isRecursive, depth > 0 - Files should be ignored in recursive mode
            } catch (NonExistentDirectoryException e) {
                result.append(e.getMessage());
                result.append(STRING_NEWLINE);
            }
        }
        if (depth == 0 && depth0Files.length() > 0) {
            depth0Files.append(STRING_NEWLINE);
            depth0Files.append(STRING_NEWLINE);
            result.insert(0, depth0Files);
        }
        return result.toString();
    }

    /**
     * Formats the contents of a directory into a single string.
     *
     * @param contents    - list of items in a directory
     * @param isSortByExt - sorts folder contents alphabetically by file extension (characters after the last ‘.’ (without quotes)). Files with no extension are sorted first.
     * @return
     */
    private String formatContents(List<Path> contents, Boolean isSortByExt) {
        List<String> fileNames = new ArrayList<>();
        for (Path path : contents) {
            fileNames.add(path.getFileName().toString());
        }

        if (isSortByExt) {
            Collections.sort(fileNames, (o1, o2) -> {
                String ext1 = o1.contains(".") ? o1.substring(o1.lastIndexOf(".") + 1) : "";
                String ext2 = o2.contains(".") ? o2.substring(o2.lastIndexOf(".") + 1) : "";
                // If both files have no extension, sort by file name
                if (ext1.isEmpty() && ext2.isEmpty()) {
                    return o1.compareTo(o2);
                }
                return ext1.compareTo(ext2);
            });
        } else {
            Collections.sort(fileNames);
        }

        StringBuilder result = new StringBuilder();
        for (String fileName : fileNames) {
            result.append(fileName);
            result.append(STRING_NEWLINE);
        }

        return result.toString().trim();
    }

    /**
     * Gets the contents in a single specified directory.
     *
     * @param directory
     * @return List of files + directories in the passed directory.
     */
    private List<Path> getContents(Path directory)
            throws InvalidDirectoryException, NonExistentDirectoryException {
        if (!Files.exists(directory)) {
            throw new NonExistentDirectoryException(getRelativeToCwd(directory).toString());
        }

        if (!Files.isDirectory(directory)) {
            throw new InvalidDirectoryException(getRelativeToCwd(directory).toString());
        }

        List<Path> result = new ArrayList<>();
        File pwd = directory.toFile();
        for (File f : pwd.listFiles()) {
            if (!f.isHidden()) {
                result.add(f.toPath());
            }
        }

        result.sort(Comparator.comparing(Path::toString));

        return result;
    }

    /**
     * Resolve all paths given as arguments into a list of Path objects for easy path management.
     *
     * @param directories
     * @return List of java.nio.Path objects
     */
    private List<Path> resolvePaths(String... directories) throws LsException {
        List<Path> paths = new ArrayList<>();
        for (int i = 0; i < directories.length; i++) {
            paths.add(resolvePath(directories[i]));
        }
        return paths;
    }

    /**
     * Converts a String into a java.nio.Path objects. Also resolves if the current path provided
     * is an absolute path.
     *
     * @param directory
     * @return
     */
    private Path resolvePath(String directory) throws LsException {
        Path path = new File(directory).toPath();
        return path.isAbsolute() ? path.normalize() : Paths.get(Environment.currentDirectory, directory).normalize();
    }

    /**
     * Converts a path to a relative path to the current directory.
     *
     * @param path
     * @return
     */
    private Path getRelativeToCwd(Path path) {
        return Paths.get(Environment.currentDirectory).relativize(path);
    }

    private class InvalidDirectoryException extends Exception {
        InvalidDirectoryException(String directory) {
            super(String.format("ls: cannot access '%s': Not a directory", directory));
        }
    }

    private class NonExistentDirectoryException extends Exception {
        NonExistentDirectoryException(String directory) {
            super(String.format("ls: cannot access '%s': No such file or directory", directory));
        }
    }
}
