package sg.edu.nus.comp.cs4218.impl.util;

import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.*;
/**
 * Utility class for resolving shell command arguments including quoting, globbing, and command substitution.
 */
@SuppressWarnings("PMD.ExcessiveMethodLength")// - May split up method if time allows
public class ArgumentResolver {

    private final ApplicationRunner applicationRunner;

    public ArgumentResolver() {
        applicationRunner = new ApplicationRunner();
    }
    /**
     * Retrieves the ApplicationRunner instance associated with this ArgumentResolver.
     *
     * @return The ApplicationRunner instance.
     */
    public ApplicationRunner getAppRunner() {
        return applicationRunner;
    }

    /**
     * Handle quoting + globing + command substitution for a list of arguments.
     *
     * @param argsList The original list of arguments.
     * @return The list of parsed arguments.
     * @throws ShellException If any of the arguments have an invalid syntax.
     */
    public List<String> parseArguments(List<String> argsList) throws AbstractApplicationException, ShellException, FileNotFoundException {
        List<String> parsedArgsList = new LinkedList<>();
        for (String arg : argsList) {
            parsedArgsList.addAll(resolveOneArgument(arg, makeRegexArgument()));
        }
        return parsedArgsList;
    }

    /**
     * Unwraps single and double quotes from one argument.
     * Performs globing when there are unquoted asterisks.
     * Performs command substitution.
     * <p>
     * Single quotes disable the interpretation of all special characters.
     * Double quotes disable the interpretation of all special characters, except for back quotes.
     *
     * @param arg String containing one argument.
     * @return A list containing one or more parsed args, depending on the outcome of the parsing.
     */
    public List<String> resolveOneArgument(String arg, IRegexArgument parsedArg) throws AbstractApplicationException, ShellException, FileNotFoundException {
        Stack<Character> unmatchedQuotes = new Stack<>();
        LinkedList<IRegexArgument> parsedArgsSegment = new LinkedList<>();
        StringBuilder subCommand = new StringBuilder();

        for (int i = 0; i < arg.length(); i++) {
            char chr = arg.charAt(i);

            if (chr == CHAR_BACK_QUOTE) {
                if (unmatchedQuotes.isEmpty() || unmatchedQuotes.peek() == CHAR_DOUBLE_QUOTE) {
                    // start of command substitution
                    if (!parsedArg.isEmpty()) {
                        appendParsedArgIntoSegment(parsedArgsSegment, parsedArg);
                        parsedArg = makeRegexArgument(); //NOPMD - valid assignment
                    }

                    unmatchedQuotes.add(chr);

                } else if (unmatchedQuotes.peek() == chr) {
                    // end of command substitution
                    unmatchedQuotes.pop();

                    // evaluate subCommand and get the output
                    String subCommandOutput = this.evaluateSubCommand(subCommand.toString());
                    subCommand.setLength(0); // Clear the previous subCommand registered

                    // check if back quotes are nested
                    if (unmatchedQuotes.isEmpty()) {
                        List<IRegexArgument> subOutputSegment = Stream
                                .of(StringUtils.tokenize(subCommandOutput))
                                .map(this::makeRegexArgument)
                                .collect(Collectors.toList());

                        // append the first token to the previous parsedArg
                        // e.g. arg: abc`1 2 3`xyz`4 5 6` (contents in `` is after command sub)
                        // expected: [abc1, 2, 3xyz4, 5, 6]
                        // fixed bug here
                        if (!subOutputSegment.isEmpty()) {
                            IRegexArgument firstOutputArg = subOutputSegment.remove(0);
                            appendParsedArgIntoSegment(parsedArgsSegment, firstOutputArg);
                        }
                            parsedArgsSegment.addAll(subOutputSegment);

                    } else {
                        // don't tokenize subCommand output
                        appendParsedArgIntoSegment(parsedArgsSegment,
                                makeRegexArgument(subCommandOutput));
                    }
                } else {
                    // ongoing single quote
                    parsedArg.append(chr);
                }
            } else if (chr == CHAR_SINGLE_QUOTE || chr == CHAR_DOUBLE_QUOTE) {
                if (unmatchedQuotes.isEmpty()) {
                    // start of quote
                    unmatchedQuotes.add(chr);
                } else if (unmatchedQuotes.peek() == chr) {
                    // end of quote
                    unmatchedQuotes.pop();

                    // make sure parsedArgsSegment is not empty
                    appendParsedArgIntoSegment(parsedArgsSegment, makeRegexArgument());
                } else if (unmatchedQuotes.peek() == CHAR_BACK_QUOTE) {
                    // ongoing back quote: add chr to subCommand
                    subCommand.append(chr);
                } else {
                    // ongoing single/double quote
                    parsedArg.append(chr);
                }
            } else if (chr == CHAR_ASTERISK) {
                if (unmatchedQuotes.isEmpty()) {
                    // each unquoted * matches a (possibly empty) sequence of non-slash chars
                    parsedArg.appendAsterisk();
                } else if (unmatchedQuotes.peek() == CHAR_BACK_QUOTE) {
                    // ongoing back quote: add chr to subCommand
                    subCommand.append(chr);
                } else {
                    // ongoing single/double quote
                    parsedArg.append(chr);
                }
            } else {
                if (unmatchedQuotes.isEmpty()) {
                    // not a special character
                    parsedArg.append(chr);
                } else if (unmatchedQuotes.peek() == CHAR_BACK_QUOTE) {
                    // ongoing back quote: add chr to subCommand
                    subCommand.append(chr);
                } else {
                    // ongoing single/double quote
                    parsedArg.append(chr);
                }
            }
        }

        if (!parsedArg.isEmpty()) {
            appendParsedArgIntoSegment(parsedArgsSegment, parsedArg);
        }

        // perform globing
        return parsedArgsSegment.stream()
                .flatMap(regexArgument -> regexArgument.globFiles().stream())
                .collect(Collectors.toList());
    }
    /**
     * Creates a new instance of IRegexArgument.
     *
     * @return A new instance of IRegexArgument.
     */
    public IRegexArgument makeRegexArgument() {
        return new RegexArgument();
    }
    /**
     * Creates a new instance of IRegexArgument with the specified string.
     *
     * @param str The string to initialize the instance with.
     * @return A new instance of IRegexArgument.
     */
    public IRegexArgument makeRegexArgument(String str) {
        return new RegexArgument(str);
    }
    /**
     * Evaluates a subcommand and returns its output.
     *
     * @param commandString The string representing the subcommand.
     * @return The output of the subcommand.
     * @throws AbstractApplicationException If an application exception occurs.
     * @throws ShellException               If a shell-related exception occurs.
     * @throws FileNotFoundException       If a file specified in the command is not found.
     */
    public String evaluateSubCommand(String commandString) throws AbstractApplicationException, ShellException, FileNotFoundException {
        if (StringUtils.isBlank(commandString)) {
            return "";
        }

        OutputStream outputStream = new ByteArrayOutputStream();
        Command command = CommandBuilder.parseCommand(commandString, getAppRunner());
        command.evaluate(System.in, outputStream);

        // replace newlines with spaces
        // fixed bug here: This adds a space to the output
        return outputStream.toString().replace(STRING_NEWLINE, "");
    }

    /**
     * Append current parsedArg to the last parsedArg in parsedArgsSegment.
     * If parsedArgsSegment is empty, then just add current parsedArg.
     */
    private void appendParsedArgIntoSegment(LinkedList<IRegexArgument> parsedArgsSegment,
                                            IRegexArgument parsedArg) {
        if (parsedArgsSegment.isEmpty()) {
            parsedArgsSegment.add(parsedArg);
        } else {
            IRegexArgument lastParsedArg = parsedArgsSegment.removeLast();
            parsedArgsSegment.add(lastParsedArg);
            lastParsedArg.merge(parsedArg);
        }
    }
}
