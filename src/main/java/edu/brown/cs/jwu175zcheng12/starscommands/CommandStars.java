package edu.brown.cs.jwu175zcheng12.starscommands;

import edu.brown.cs.jwu175zcheng12.csvdataset.StarDataset;
import edu.brown.cs.jwu175zcheng12.repl.GeneralREPL;
import edu.brown.cs.jwu175zcheng12.repl.GeneralREPLCommand;

/**
 * A class which represents the "stars" command for the Stars 1 project.
 *
 * Accepted syntax:
 * - stars csv_file_name
 */
public class CommandStars implements GeneralREPLCommand {

  private final StarDataset workingStarDataset;

  /**
   * The constructor for this class. Note that the StarDataset is totally
   * mutable, as is intended---we want workingStarDataset to refer to the same
   * StarDataset that the other commands are using, if not any changes we make
   * here won't be reflected by those commands.
   *
   * @param workingStarDataset the StarDataset instance to work with.
   */
  public CommandStars(StarDataset workingStarDataset) {
    this.workingStarDataset = workingStarDataset;
  }

  @Override
  public String getCommandName() {
    return "stars";
  }

  @Override
  public void executeCommand(String[] argArray) {
    // Check for appropriate syntax
    if (argArray.length == 2 && argArray[1] != null) {
      // Parse inputs in 'stars csv_file_name' format
      String filename = argArray[1];
      workingStarDataset.loadData(filename, true);
    } else {
      GeneralREPL.printError("Malformed input for stars command.");
    }
  }
}
