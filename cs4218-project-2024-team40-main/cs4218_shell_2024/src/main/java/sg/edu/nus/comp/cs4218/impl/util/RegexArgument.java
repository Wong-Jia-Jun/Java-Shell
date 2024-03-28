package sg.edu.nus.comp.cs4218.impl.util;

import sg.edu.nus.comp.cs4218.Environment;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_ASTERISK;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FILE_SEP;

@SuppressWarnings("PMD.AvoidStringBufferField")
public final class RegexArgument implements IRegexArgument {
    private StringBuilder plaintext;
    private StringBuilder regex;
    private boolean isRegexFlag;

    public RegexArgument() {
        this.plaintext = new StringBuilder();
        this.regex = new StringBuilder();
        this.isRegexFlag = false;
    }

    public RegexArgument(String str) {
        this();
        merge(str);
    }



    public void append(char chr) {
        plaintext.append(chr);
        regex.append(Pattern.quote(String.valueOf(chr)));
    }

    public void appendAsterisk() {
        plaintext.append(CHAR_ASTERISK);
        regex.append(String.format("[^%s]*", StringUtils.fileSeparator()));
        isRegexFlag = true;
    }

    public void merge(IRegexArgument other) {
        if (other instanceof RegexArgument) {
            RegexArgument arg = (RegexArgument) other;
            plaintext.append(arg.plaintext);
            regex.append(arg.regex);
            isRegexFlag = isRegexFlag || arg.isRegexFlag;
        } else {
            throw new UnsupportedOperationException("merge called with different class");
        }
    }

    public void merge(String str) {
        plaintext.append(str);
        regex.append(Pattern.quote(str));
    }

    public List<String> globFiles() {
        List<String> globbedFiles = new LinkedList<>();

        if (isRegexFlag) {
            String dir = "";
            String tokens[] = plaintext.toString().replaceAll("\\\\", "/").split("/");
            for (int i = 0; i < tokens.length - 1; i++) {
                dir += tokens[i] + File.separator;
            }

            // fixed bug here: moved pattern compilation to after tokenization -> regex only uses last string after "\"
            if (tokens.length > 1) {
                char[] matchingRegex = tokens[tokens.length - 1].toCharArray();
                regex.setLength(0); // clear previous regex
                for (char c : matchingRegex) {
                    if (Objects.equals(c, '*')) {
                        regex.append(String.format("[^%s]*", StringUtils.fileSeparator()));
                    } else {
                        regex.append(Pattern.quote(String.valueOf(c)));
                    }
                }
            }
            Pattern regexPattern = Pattern.compile(regex.toString());


            File currentDir = Paths.get(Environment.currentDirectory + File.separator + dir).toFile();

            for (String candidate : currentDir.list()) {
                if (regexPattern.matcher(candidate).matches()) {
                    globbedFiles.add(dir + candidate);
                }
            }

            Collections.sort(globbedFiles);
        }

        if (globbedFiles.isEmpty()) {
            globbedFiles.add(plaintext.toString());
        }

        return globbedFiles;
    }




    public boolean isEmpty() {
        return plaintext.length() == 0;
    }

    public String toString() {
        return plaintext.toString();
    }
}
