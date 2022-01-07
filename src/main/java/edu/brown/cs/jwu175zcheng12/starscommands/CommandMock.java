package edu.brown.cs.jwu175zcheng12.starscommands;

import edu.brown.cs.jwu175zcheng12.csvdataset.MockPersonDataset;
import edu.brown.cs.jwu175zcheng12.repl.GeneralREPL;
import edu.brown.cs.jwu175zcheng12.repl.GeneralREPLCommand;

/**
 * A class which represents the "mock" command for the Stars 1 project.
 *
 * Accepted syntax:
 * - mock csv_file_name
 */
public class CommandMock implements GeneralREPLCommand {

  private final MockPersonDataset workingMockPersonDataset;

  /**
   * The constructor for this class. Note that the MockPersonDataset is totally
   * mutable, as is intended.
   *
   * @param workingMockPersonDataset the MockPersonDataset instance to work with.
   */
  public CommandMock(MockPersonDataset workingMockPersonDataset) {
    this.workingMockPersonDataset = workingMockPersonDataset;
  }
  @Override
  public String getCommandName() {
    return "mock";
  }

  @Override
  public void executeCommand(String[] argArray) {
    // Check for appropriate syntax
    if (argArray.length == 2 && argArray[1] != null) {
      // Parse inputs in 'mock csv_file_name' format
      String filename = argArray[1];
      workingMockPersonDataset.loadData(filename, true);
    } else {
      GeneralREPL.printError("Malformed input for mock command.");
    }
  }
}
