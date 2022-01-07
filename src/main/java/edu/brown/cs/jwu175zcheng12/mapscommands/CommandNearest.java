package edu.brown.cs.jwu175zcheng12.mapscommands;

import edu.brown.cs.jwu175zcheng12.maps.MapDatabase;
import edu.brown.cs.jwu175zcheng12.maps.Node;
import edu.brown.cs.jwu175zcheng12.repl.GeneralREPLCommand;

import static edu.brown.cs.jwu175zcheng12.repl.GeneralREPL.print;
import static edu.brown.cs.jwu175zcheng12.repl.GeneralREPL.printError;

/**
 * A class which represents the "nearest" command for the Maps 1 and 2 project.
 *
 * Accepted syntax:
 * - nearest latitude longitude
 */
public class CommandNearest implements GeneralREPLCommand {

  private final MapDatabase workingMapDatabase;
  private static final int NINETY = 90;

  /**
   * The constructor for this class. Note that the workingMapDatabase is
   * totally mutable, as is intended---we want workingMapDatabase to refer to
   * the same MapDatabase that the other commands are using, if not any changes
   * we make here won't be reflected by those commands.
   *
   * @param workingMapDatabase the MapDatabase to work with.
   */
  public CommandNearest(MapDatabase workingMapDatabase) {
    this.workingMapDatabase = workingMapDatabase;
  }

  @Override
  public String getCommandName() {
    return "nearest";
  }

  @Override
  public void executeCommand(String[] argArray) {
    double lat;
    double lon;
    // Check for appropriate syntax
    if (argArray.length == 3 && argArray[1] != null && argArray[2] != null) {
      // Parse inputs in 'nearest latitude longitude' format
      try {
        lat = Double.parseDouble(argArray[1]);
        lon = Double.parseDouble(argArray[2]);
      } catch (NumberFormatException error) {
        printError("Bad number format.");
        return;
      }
      // Check that angles are appropriate
      if (lat > NINETY || lat < -NINETY) {
        printError("Latitude passed to nearest command must be in range [-90, 90].");
        return;
      }
      if (lon > NINETY * 2 || lon < -NINETY * 2) {
        printError("Longitude passed to nearest command must be in range [-180, 180].");
        return;
      }
      // Get results
      Node resultNode = workingMapDatabase.getNearestNode(lat, lon);
      if (resultNode == null) {
        printError("No Node found.");
        return;
      }
      // Print
      print(resultNode.getNodeId());
    } else {
      printError("Malformed input for nearest command.");
    }
  }
}
