package sg.edu.nus.comp.cs4218.impl;

import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.Shell;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.CommandBuilder;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class ShellImpl implements Shell {

    /**
     * Main method for the Shell Interpreter program.
     *
     * @param args List of strings arguments, unused.
     */
    public static void main(String... args) {
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            ShellImpl shell = new ShellImpl();
            shell.run(reader);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }


    /**
     * Runs the main logic loop for the Shell Interpreter.
     *
     * @param reader The BufferedReader used to read input commands.
     */
    public void run(BufferedReader reader) {

        while (true) {
            try {
                String currentDirectory = Environment.currentDirectory;
                String commandString;

                System.out.print(currentDirectory + "> ");
                commandString = reader.readLine();

                if (!StringUtils.isBlank(commandString)) {
                    this.parseAndEvaluate(commandString, System.out);
                }
            }
                catch(IOException e){
                    System.out.println(e.getMessage());
                    return; // Streams are closed, terminate process
                }

                catch(Exception e){
                    System.out.println(e.getMessage());
                }

    }}

    @Override
    public void parseAndEvaluate(String commandString, OutputStream stdout)
            throws AbstractApplicationException, ShellException, FileNotFoundException {
        Command command = CommandBuilder.parseCommand(commandString, new ApplicationRunner());

        command.evaluate(System.in, stdout);
    }
}
