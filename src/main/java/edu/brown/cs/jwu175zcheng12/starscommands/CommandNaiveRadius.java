package edu.brown.cs.jwu175zcheng12.starscommands;

import edu.brown.cs.jwu175zcheng12.repl.GeneralREPL;
import edu.brown.cs.jwu175zcheng12.repl.GeneralREPLCommand;
import edu.brown.cs.jwu175zcheng12.stars.Star;
import edu.brown.cs.jwu175zcheng12.csvdataset.StarDataset;

import java.util.List;

/**
 * A class which represents the "naive_radius" command for the Stars 1 project.
 *
 * Accepted syntax:
 * - naive_radius k x y z,
 * - naive_radius k "star_name",
 */
public class CommandNaiveRadius implements GeneralREPLCommand {

  private final StarDataset workingStarDataset;

  /**
   * The constructor for this class. Note that the StarDataset is totally
   * mutable, as is intended---we want workingStarDataset to refer to the same
   * StarDataset that the other commands, especially CommandStars, are using.
   *
   * @param workingStarDataset the StarData instance to work with.
   */
  public CommandNaiveRadius(StarDataset workingStarDataset) {
    this.workingStarDataset = workingStarDataset;
  }

  @Override
  public String getCommandName() {
    return "naive_radius";
  }

  @Override
  public void executeCommand(String[] argArray) {
    double r;
    double x;
    double y;
    double z;
    // Check for appropriate syntax
    if (argArray.length == 5 && argArray[1] != null
        && argArray[2] != null && argArray[3] != null
        && argArray[4] != null) {
      // Parse inputs in 'naive_radius r x y z' format
      try {
        r = Double.parseDouble(argArray[1]);
        if (r < 0) {
          GeneralREPL.printError(
              "Radius must be non-negative for naive_radius command.");
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
      List<Star> naiveRadiusStars = workingStarDataset.naiveRadius(r, x, y, z);
      // Print results
      for (Star naiveRadiusStar : naiveRadiusStars) {
        GeneralREPL.print(Integer.toString(naiveRadiusStar.getStarId()));
      }
    } else if (argArray.length == 3 && argArray[1] != null
        && argArray[2] != null) {
      // Parse inputs in 'naive_radius r "star_name"' format
      try {
        r = Double.parseDouble(argArray[1]);
        if (r < 0) {
          GeneralREPL.printError("Radius must be non-negative.");
          return;
        }
      } catch (NumberFormatException error) {
        GeneralREPL.printError("Bad number format.");
        return;
      }
      // Find corresponding star
      Star refStar = workingStarDataset.getStarByName(argArray[2]);
      if (refStar == null) {
        GeneralREPL.printError("Star \"" + argArray[2] + "\" not found.");
        return;
      }
      // Execute task
      List<Star> naiveRadiusStars = workingStarDataset.naiveRadius(r, refStar);
      // Print results
      for (Star naiveRadiusStar : naiveRadiusStars) {
        GeneralREPL.print(Integer.toString(naiveRadiusStar.getStarId()));
      }
    } else {
      GeneralREPL.printError("Malformed input for naive_radius command.");
    }
  }
}
