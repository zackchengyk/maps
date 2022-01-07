package edu.brown.cs.jwu175zcheng12.mapscommands;

import edu.brown.cs.jwu175zcheng12.maps.MapDatabase;
import edu.brown.cs.jwu175zcheng12.repl.GeneralREPLCommand;

import java.util.List;

import static edu.brown.cs.jwu175zcheng12.repl.GeneralREPL.print;
import static edu.brown.cs.jwu175zcheng12.repl.GeneralREPL.printError;

/**
 * A class which represents the "ways" command for the Maps 1 and 2 project.
 *
 * Accepted syntax:
 * - ways lat1 lon1 lat2 lon2
 */
public class CommandWays implements GeneralREPLCommand {

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
  public CommandWays(MapDatabase workingMapDatabase) {
    this.workingMapDatabase = workingMapDatabase;
  }

  @Override
  public String getCommandName() {
    return "ways";
  }

  @Override
  public void executeCommand(String[] argArray) {
    double lat1;
    double lon1;
    double lat2;
    double lon2;
    // Check for appropriate syntax
    if (argArray.length == 5 && argArray[1] != null
        && argArray[2] != null && argArray[3] != null
        && argArray[4] != null) {
      // Parse inputs in 'ways lat1 lon1 lat2 lon2' format
      try {
        lat1 = Double.parseDouble(argArray[1]);
        lon1 = Double.parseDouble(argArray[2]);
        lat2 = Double.parseDouble(argArray[3]);
        lon2 = Double.parseDouble(argArray[4]);
      } catch (NumberFormatException error) {
        printError("Bad number format.");
        return;
      }
      // Check that angles are appropriate
      if (lat1 > NINETY || lat1 < -NINETY
          || lat2 > NINETY || lat2 < -NINETY) {
        printError("Latitudes passed to ways command must be in range [-90, 90].");
        return;
      }
      if (lon1 > NINETY * 2 || lon1 < -NINETY * 2
          || lon2 > NINETY * 2 || lon2 < -NINETY * 2) {
        printError("Longitude passed to ways command must be in range [-180, 180].");
        return;
      }
      // Get results
      List<String> wayIds = workingMapDatabase.getWayIdsInBox(lat1, lon1, lat2, lon2);
      if (wayIds == null || wayIds.size() == 0) {
        printError("No Ways found.");
        return;
      }
      // Print
      for (String wayId : wayIds) {
        print(wayId);
      }
    } else {
      printError("Malformed input for ways command.");
    }
  }
}
