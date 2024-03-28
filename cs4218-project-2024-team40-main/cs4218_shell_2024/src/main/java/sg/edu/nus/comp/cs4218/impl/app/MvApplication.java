package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.MvInterface;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.MvException;
import sg.edu.nus.comp.cs4218.impl.parser.MvArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class MvApplication implements MvInterface {
    private final MvArgsParser parser;

    public MvApplication(MvArgsParser parser) {
        this.parser = parser;
    }

    private String getExceptionFormat(String filename, String message) {
        return String.format("mv: cannot stat '%s': %s", filename, message);
    }

    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws MvException {
        if (stdout == null || stdin == null) {
            throw new MvException(ERR_NULL_STREAMS);
        }
        if (args == null) {
            throw new MvException(ERR_NULL_ARGS);
        }
        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new MvException(e);
        }
        int fileCount = parser.getFileNames().size();
        if (fileCount < 2) {
            throw new MvException(ERR_NO_ARGS);
        }
        String result;
        // Verify if last file is a directory or a filename
        String target = parser.getFileNames().get(fileCount - 1);
        Path targetPath;
        try {
            targetPath = IOUtils.resolveFilePath(target);
            if (Files.isDirectory(targetPath)) {
                result = mvFilesToFolder(parser.isOverwrite(), target, parser.getFileNames().toArray(new String[0]));
            } else if (fileCount == 2) {
                result = mvSrcFileToDestFile(parser.isOverwrite(), parser.getFileNames().get(0), target);
            } else {
                result = new MvException(ERR_TOO_MANY_ARGS).getMessage();
            }
        } catch (IOException e) {
            result = getExceptionFormat(target, ERR_FILE_NOT_FND);
        }
        try {
            if (result != null) {
                stdout.write(result.getBytes());
            }
            stdout.write(STRING_NEWLINE.getBytes());
        } catch (IOException e) {
            throw new MvException(ERR_WRITE_STREAM);//NOPMD - throw here to get caught in SHell impl and show excpt msg
        }
    }

    @Override
    public String mvSrcFileToDestFile(Boolean isOverwrite, String srcFile, String destFile) throws MvException {

        Path target;
        try {
            target = IOUtils.resolveFilePath(destFile);
        } catch (IOException e) {
            return getExceptionFormat(destFile, ERR_INVALID_FILE);
        }
        Path source;
        try {
            source = IOUtils.resolveFilePath(srcFile);
        } catch (IOException e) {
            return getExceptionFormat(srcFile, ERR_FILE_NOT_FND);
        }

        if (target.equals(source)) {
            return null;
        }

        // Verify Files exist
        if (!Files.exists(source)) {
            return getExceptionFormat(srcFile, ERR_FILE_NOT_FND);
        }
        // Verify Src is not a Directory and Dest is not a File
        if (Files.isDirectory(source) && Files.isRegularFile(target)) {
            return String.format("mv: cannot overwrite non-directory '%s' with directory '%s'", destFile, srcFile);
        }

        if (isOverwrite) {
            // Delete destFile and rename srcFile
            try {
                Files.deleteIfExists(target);
                Files.move(source, target);
            } catch (IOException e) {
                throw new MvException(ERR_IO_EXCEPTION); //NOPMD - throw here to get caught in ShellImpl and show msg
            }
        } else {
            // If exists, throw exception
            if (Files.exists(target)) {
                return getExceptionFormat(destFile, ERR_FILE_EXISTS);
            } else {
                try {
                    Files.move(source, target);
                } catch (IOException e) {
                    throw new MvException(ERR_IO_EXCEPTION); //NOPMD - throw here to get caught in ShellImpl and show msg
                }
            }
        }
        return null;
    }

    @Override
    public String mvFilesToFolder(Boolean isOverwrite, String destFolder, String... fileName) throws MvException {
        Path dest;
        try {
            dest = IOUtils.resolveFilePath(destFolder);
        } catch (IOException e) {
           return getExceptionFormat(destFolder, ERR_IS_NOT_DIR);
        }

        // Verify directory
        if (!Files.isDirectory(dest)) {
            return getExceptionFormat(destFolder, ERR_IS_NOT_DIR);
        }

        List<String> result = new ArrayList<>();
        for (String file : fileName) {
            Path src;
            try {
                src = IOUtils.resolveFilePath(file);
            } catch (IOException e) {
                result.add(getExceptionFormat(file, ERR_FILE_NOT_FND));
                continue;
            }
            // Verify file exists
            if (!Files.exists(src)) {
                result.add(getExceptionFormat(file, ERR_FILE_NOT_FND));
                continue;
            }
            // If file is folder
            if (src.equals(dest)) {
                continue;
            }

            try {
                if (isOverwrite) {
                    Files.deleteIfExists(dest.resolve(src.getFileName()));
                    Files.move(src, dest.resolve(src.getFileName()));
                } else if (Files.exists(dest.resolve(src.getFileName()))) {
                        result.add(getExceptionFormat(file, ERR_FILE_EXISTS));
                } else {
                    Files.move(src, dest.resolve(src.getFileName()));
                }
            } catch (IOException e) {
                throw new MvException(ERR_IO_EXCEPTION); //NOPMD - throw here to get caught in ShellImpl and show msg
            }
        }
        return String.join(STRING_NEWLINE, result);
    }
}
