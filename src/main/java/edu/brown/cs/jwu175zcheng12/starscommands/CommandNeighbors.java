package edu.brown.cs.jwu175zcheng12.starscommands;

import edu.brown.cs.jwu175zcheng12.csvdataset.StarDataset;
import edu.brown.cs.jwu175zcheng12.repl.GeneralREPL;
import edu.brown.cs.jwu175zcheng12.repl.GeneralREPLCommand;
import edu.brown.cs.jwu175zcheng12.stars.Star;

import java.util.List;

/**
 * A class which represents the "neighbors" command for
 * the Stars 2 project.
 *
 * Accepted syntax:
 * - neighbors k x y z
 * - neighbors k "star_name"
 */
public class CommandNeighbors implements GeneralREPLCommand {

  private final StarDataset workingStarDataset;

  /**
   * The constructor for this class. Note that the StarDataset is totally
   * mutable, as is intended---we want workingStarDataset to refer to the same
   * StarDataset that the other commands, especially CommandStars, are using.
   *
   * @param workingStarDataset the StarData instance to work with.
   */
  public CommandNeighbors(StarDataset workingStarDataset) {
    this.workingStarDataset = workingStarDataset;
  }

  @Override
  public String getCommandName() {
    return "neighbors";
  }

  @Override
  public void executeCommand(String[] argArray) {
    int k;
    double x;
    double y;
    double z;
    // Check for appropriate syntax
    if (argArray.length == 5 && argArray[1] != null
        && argArray[2] != null && argArray[3] != null
        && argArray[4] != null) {
      // Parse inputs in 'naive_neighbors k x y z' format
      try {
        k = Integer.parseInt(argArray[1]);
        if (k < 0) {
          GeneralREPL.printError("Number of nearest neighbors to be found "
              + "must be non-negative.");
          return;
        }
        x = Double.parseDouble(argArray[2]);
        y = Double.parseDouble(argArray[3]);
        z = Double.parseDouble(argArray[4]);
      } catch (NumberFormatException error) {
        GeneralREPL.printError("Bad number format.");
        return;
      }
      // Execute task
      List<Star> neighborStars =
          workingStarDataset.neighbors(k, x, y, z);
      // Print results
      for (Star neighborStar : neighborStars) {
        GeneralREPL.print(Integer.toString(neighborStar.getStarId()));
      }
    } else if (argArray.length == 3 && argArray[1] != null
        && argArray[2] != null) {
      // Parse inputs in 'neighbors k "star_name"' format
      try {
        k = Integer.parseInt(argArray[1]);
        if (k < 0) {
          GeneralREPL.printError("Number of nearest neighbors to be found "
              + "must be non-negative.");
          return;
        }
      } catch (NumberFormatException error) {
        GeneralREPL.printError("Bad number format.");
        return;
      }
      // Find corresponding star
      Star star = workingStarDataset.getStarByName(argArray[2]);
      if (star == null) {
        GeneralREPL.printError("Star \"" + argArray[2] + "\" not found.");
        return;
      }
      // Execute task
      List<Star> neighborStars = workingStarDataset.neighbors(k, star);
      // Print results
      for (Star neighborStar : neighborStars) {
        GeneralREPL.print(Integer.toString(neighborStar.getStarId()));
//        printInfo("\"" + neighborStar.getProperName() + "\" is "
//            + neighborStar.getDistanceFrom(star) + " distance away.");
      }
    } else {
      GeneralREPL.printError("Malformed input for neighbors command.");
    }
  }
}
