package edu.brown.cs.jwu175zcheng12.repl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/**
 * A class which represents a REPL, with a given a set of commands.
 */
public final class GeneralREPL {

  private final BufferedReader reader;
  private final String tokenizingRegex;
  private final Map<String, GeneralREPLCommand> allCommands =
      new HashMap<>();

  /**
   * The constructor for this class. Every field is a final immutable object
   * or primitive, so no copying is necessary.
   *
   * @param reader the Java.IO.Reader to be passed to the BufferedReader
   * @param regex  a String, the regex to be used when tokenizing inputs
   */
  public GeneralREPL(Reader reader, String regex) {
    this.reader = new BufferedReader(reader);
    this.tokenizingRegex = regex;
  }

  /**
   * A function which adds one GeneralREPLCommand to allCommands, the HashMap
   * of commands which this REPL must support.
   *
   * @param command a GeneralREPLCommand to be added to the allCommands HashMap
   */
  public void registerCommand(GeneralREPLCommand command) {
    allCommands.put(command.getCommandName(), command);
  }

  /**
   * A function which starts the REPL associated with this GeneralREPL.
   */
  public void startREPL() {
    boolean output = true;
    while (output) {
      output = this.readEvalPrint();
    }
  }

  /**
   * A function which is called once per REPL loop. It reads in input from
   * the reader created when this GeneralREPL class instance was constructed,
   * checks to see if it corresponds with a registered command, and executes
   * that command if need be.
   * <p>
   * If EOF is read, the function returns false.
   *
   * @return a boolean indicating whether to continue the REPL's looping
   */
  private boolean readEvalPrint() {
    try {
      // Get input from console
      String inputString = reader.readLine();
      // Check if EOF was returned
      if (inputString == null) {
        return false;
      }
      // Check for mismatched delimiters
      long quoteCount = inputString.chars().filter(x -> x == '"').count();
      if (quoteCount % 2 != 0) {
        printError("Mismatched delimiters. Check for missing quotation marks.");
        return true;
      }
      // Tokenize
      String[] inputStringArray = inputString.split(this.tokenizingRegex);
      // Check if there was any input at all
      if (inputStringArray.length == 0) {
        // Print nothing
        return true;
      }
      // Check if the command exists
      if (!allCommands.containsKey(inputStringArray[0])) {
        printError("Command not recognized.");
        return true;
      }
      // Run the appropriate command
      allCommands.get(inputStringArray[0]).executeCommand(inputStringArray);
      return true;
    } catch (IOException error) {
      printError("IOException returned when trying to read "
          + "console input.");
      printError("Error message was: \"" + error.getMessage() + "\"");
      return false;
    }
  }

  /**
   * A function that prints a string, given by messageString, to the
   * standard output stream.
   *
   * @param messageString the message to be printed
   */
  public static void print(String messageString) {
    System.out.println(messageString);
  }

  /**
   * A function that prints a string, given by messageString, to the
   * standard output stream, prefixed by "INFO: ". Used for debugging only.
   *
   * @param infoString the message to be printed
   */
  public static void printInfo(String infoString) {
    System.out.println("INFO: " + infoString);
  }

  /**
   * A function that prints an error message, given by errorString, to the
   * standard error output stream, prefixed by "ERROR: ".
   *
   * @param errorString the message to be printed after "ERROR: "
   */
  public static void printError(String errorString) {
    System.err.println("ERROR: " + errorString);
  }
}
