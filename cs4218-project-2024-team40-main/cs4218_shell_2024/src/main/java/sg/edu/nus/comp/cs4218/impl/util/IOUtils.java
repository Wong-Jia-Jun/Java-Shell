package sg.edu.nus.comp.cs4218.impl.util;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.ShellException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

@SuppressWarnings("PMD.PreserveStackTrace")
/**
 * The IOUtils class provides utility methods for input and output operations.
 * It includes methods for opening and closing input and output streams, resolving file paths,
 * and reading lines from input streams.
 */
public final class IOUtils {
    private IOUtils() {
    }

    /**
     * Open an inputStream based on the file name.
     *
     * @param fileName String containing file name.
     * @return InputStream of file opened.
     * @throws ShellException If file destination is inaccessible.
     */
    public static InputStream openInputStream(String fileName) throws ShellException {
        String resolvedFileName;
        FileInputStream fileInputStream;
        try {
            resolvedFileName = resolveFilePath(fileName).toString();
            fileInputStream = new FileInputStream(new File(resolvedFileName));
        } catch (IOException e) {
            throw new ShellException(ERR_FILE_NOT_FND);
        }

        return fileInputStream;
    }

    /**
     * Open an outputStream based on the file name.
     *
     * @param fileName String containing file name.
     * @return OutputStream of file opened.
     * @throws ShellException If file destination is inaccessible.
     */
    public static OutputStream openOutputStream(String fileName) throws ShellException {
        File file;
        FileOutputStream fileOutputStream;
        try {
            file = IOUtils.resolveFilePath(fileName).toFile();
            if (file.isDirectory()){
                throw new ShellException(ERR_IS_DIR);
            }
            fileOutputStream = new FileOutputStream(file);
        } catch (IOException e) {
            throw new ShellException(ERR_FILE_NOT_FND);
        }
        return fileOutputStream;
    }

    /**
     * Close an inputStream. If inputStream provided is System.in or null, it will be ignored.
     *
     * @param inputStream InputStream to be closed.
     * @throws ShellException If inputStream cannot be closed successfully.
     */
    public static void closeInputStream(InputStream inputStream) throws ShellException {
        if (inputStream == null || inputStream.equals(System.in) ) {
            return;
        }

        try {
            inputStream.close();
        } catch (IOException e) {
            throw new ShellException(ERR_CLOSE_STREAMS);
        }
    }

    /**
     * Close an outputStream. If outputStream provided is System.out or null, it will be ignored.
     *
     * @param outputStream OutputStream to be closed.
     * @throws ShellException If outputStream cannot be closed successfully.
     */
    public static void closeOutputStream(OutputStream outputStream) throws ShellException {
        if (outputStream == null || outputStream.equals(System.out) ) {
            return;
        }

        try {
            outputStream.close();
        } catch (IOException e) {
            throw new ShellException(ERR_CLOSE_STREAMS);
        }
    }

    /**
     * Returns filepath based on the given filename.
     *
     * @param fileName      a string containing the target filename.
     * @throws IOException  if error converting filename to path.
     */
    public static Path resolveFilePath(String fileName) throws IOException {
        Path currentDirectory = Paths.get(Environment.currentDirectory);
        // fixed bug: unchecked exception from Path.resolve() would propagate upwards, causing unexpected exceptions
        Path filePath;
        try {
            filePath = currentDirectory.resolve(fileName);
        } catch (InvalidPathException e) {
            throw new IOException("Failed to convert " + fileName + " to Path.");
        }
        return filePath;
    }

    /**
     * Returns a list of lines based on the given InputStream.
     *
     * @param input InputStream containing arguments from System.in or FileInputStream
     * @throws Exception if error reading from input stream
     */
    public static List<String> getLinesFromInputStream(InputStream input) throws IOException{

        List<String> output = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input,
                StandardCharsets.UTF_8));
        String line;

        while ((line = reader.readLine()) != null) {
            output.add(line);
        }
        return output;
    }
}
