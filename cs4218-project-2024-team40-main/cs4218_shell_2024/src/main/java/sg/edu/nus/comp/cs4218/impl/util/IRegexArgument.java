package sg.edu.nus.comp.cs4218.impl.util;

import java.util.List;

public interface IRegexArgument {
    void append(char chr);
    void appendAsterisk();
    void merge(String str);
    void merge(IRegexArgument other) ;
    List<String> globFiles();
    boolean isEmpty();
}
