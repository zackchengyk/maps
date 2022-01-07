package edu.brown.cs.jwu175zcheng12.repl;

/**
 * An interface which represents a REPL command.
 * It contains both the name (hardly used) and the method which
 * actually does what the command should do.
 */
public interface GeneralREPLCommand {

  /**
   * A function which gets the name of this command.
   *
   * @return the name of this command
   */
  String getCommandName();

  /**
   * A function which performs the task associated with this command.
   *
   * @param argArray a String array, representing the arguments to this function
   */
  void executeCommand(String[] argArray);
}
