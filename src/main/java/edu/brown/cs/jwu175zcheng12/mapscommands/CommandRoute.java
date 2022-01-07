package edu.brown.cs.jwu175zcheng12.mapscommands;

import edu.brown.cs.jwu175zcheng12.maps.MapDatabase;
import edu.brown.cs.jwu175zcheng12.maps.Node;
import edu.brown.cs.jwu175zcheng12.maps.Way;
import edu.brown.cs.jwu175zcheng12.repl.GeneralREPLCommand;

import java.util.List;

import static edu.brown.cs.jwu175zcheng12.repl.GeneralREPL.print;
import static edu.brown.cs.jwu175zcheng12.repl.GeneralREPL.printError;

/**
 * A class which represents the "route" command for the Maps 1 and 2 project.
 *
 * Accepted syntax:
 * - route  startLat startLon  endLat endLon
 * - route  "st1" "cross-st1"  "st2" "cross-st2"
 */
public class CommandRoute implements GeneralREPLCommand {

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
  public CommandRoute(MapDatabase workingMapDatabase) {
    this.workingMapDatabase = workingMapDatabase;
  }

  @Override
  public String getCommandName() {
    return "route";
  }

  @Override
  public void executeCommand(String[] argArray) {
    // Check for appropriate syntax
    if (argArray.length == 5 && argArray[1] != null
        && argArray[2] != null && argArray[3] != null
        && argArray[4] != null) {
      Node start;
      Node end;
      try {
        // Parse inputs in 'route startLat startLon endLat endLon' format
        double startLat = Double.parseDouble(argArray[1]);
        double startLon = Double.parseDouble(argArray[2]);
        double endLat = Double.parseDouble(argArray[3]);
        double endLon = Double.parseDouble(argArray[4]);
        // Check that angles are appropriate
        if (startLat > NINETY || startLat < -NINETY
            || endLat > NINETY || endLat < -NINETY) {
          printError("Latitude passed to route command must be in range [-90, 90].");
          return;
        }
        if (startLon > NINETY * 2 || startLon < -NINETY * 2
            || endLon > NINETY * 2 || endLon < -NINETY * 2) {
          printError("Longitude passed to route command must be in range [-180, 180].");
          return;
        }
        // Get start and end Node
        start = workingMapDatabase.getNearestNode(startLat, startLon);
        end = workingMapDatabase.getNearestNode(endLat, endLon);
      } catch (NumberFormatException error) {
        // Parse inputs in 'route st1 cst1 st2 cst2' format
        String st1 = argArray[1];
        String cst1 = argArray[2];
        String st2 = argArray[3];
        String cst2 = argArray[4];
        // Get start and end Node
        start = workingMapDatabase.getIntersectionNode(st1, cst1);
        end = workingMapDatabase.getIntersectionNode(st2, cst2);
      }
      if (start == null || end == null) {
        printError("At least one street + cross-street pair does not intersect."
            + "Therefore, no route was found.");
        return;
      }
      if (start.equals(end)) {
        printError("Already there!");
        return;
      }
      // Get path
      List<Way> pathWays = start.aStarPath(end);
      // If no path, print "-/-" message
      if (pathWays == null) {
        print(start.getNodeId() + " -/- " + end.getNodeId());
        return;
      }
      // Print path
      for (Way pathWay : pathWays) {
        print(pathWay.getStart().getNodeId() + " -> "
            + pathWay.getEnd().getNodeId() + " : " + pathWay.getWayId());
      }
    } else {
      printError("Malformed input for route command.");
    }
  }
}
