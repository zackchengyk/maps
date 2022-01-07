package edu.brown.cs.jwu175zcheng12.mapscommands;

import edu.brown.cs.jwu175zcheng12.maps.MapDatabase;

import edu.brown.cs.jwu175zcheng12.repl.GeneralREPLCommand;

import java.sql.SQLException;

import static edu.brown.cs.jwu175zcheng12.repl.GeneralREPL.print;
import static edu.brown.cs.jwu175zcheng12.repl.GeneralREPL.printError;

/**
 * A class which represents the "map" command for the Maps 1 and 2 project.
 *
 * Accepted syntax:
 * - map path/to/database
 */
public class CommandMap implements GeneralREPLCommand {

  private final MapDatabase workingMapDatabase;

  /**
   * The constructor for this class. Note that the workingMapDatabase is
   * totally mutable, as is intended---we want workingMapDatabase to refer to
   * the same MapDatabase that the other commands are using, if not any changes
   * we make here won't be reflected by those commands.
   *
   * @param workingMapDatabase the MapDatabase to work with.
   */
  public CommandMap(MapDatabase workingMapDatabase) {
    this.workingMapDatabase = workingMapDatabase;
  }

  @Override
  public String getCommandName() {
    return "map";
  }

  @Override
  public void executeCommand(String[] argArray) {
    // Check for appropriate syntax
    if (argArray.length == 2 && argArray[1] != null) {
      // Parse inputs in 'maps path/to/database' format
      String pathToDatabase = argArray[1];
      // Set up workingMapDatabase to work with new database
      try {
        workingMapDatabase.setUpMapDatabase(pathToDatabase);
      } catch (UnsupportedOperationException | ClassNotFoundException error) {
        return;
      } catch (SQLException e) {
        printError("Error Code: " + e.getErrorCode());
        printError("Error Message: " + e.getMessage());
        Throwable throwable = e.getCause();
        while (throwable != null) {
          printError("Cause of Error: " + throwable);
          throwable = throwable.getCause();
        }
        return;
      }
      // Print
      print("map set to " + pathToDatabase);
    } else {
      printError("Malformed input for map command.");
    }
  }
}
