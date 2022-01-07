package edu.brown.cs.jwu175zcheng12.maps;

import com.google.common.collect.ImmutableMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static edu.brown.cs.jwu175zcheng12.repl.GeneralREPL.printError;

/**
 * thread that continuously communicates with checkin server.
 */
public final class CheckinThread extends Thread {
  private long last = 0;
  private Map<Double, UserCheckin> checkins;
  private boolean pause = false;
  static final long MS_CONVERSION = 1000;
  private Connection dbConn = null;

  /**
   * The constructor for this class.
   *
   * @param dbPath the path to the sqlite db to be used for our checkins
   */
  public CheckinThread(String dbPath) {
    checkins = Collections.synchronizedMap(new HashMap<>());
    // Create connection
    try {
      // Setup SQL Driver Manager Class
      Class.forName("org.sqlite.JDBC");
      String urlToDB = "jdbc:sqlite:" + dbPath;
      this.dbConn = DriverManager.getConnection(urlToDB);
    } catch (ClassNotFoundException | SQLException e) {
      printError("SQL or ClassNotFound Exception in CheckinThread constructor: "
          + e.getMessage());
    }
    // Clear database
    try (
        PreparedStatement prep = this.dbConn.prepareStatement(
            "DELETE FROM checkins WHERE true;")
    ) {
      prep.executeUpdate();
    } catch (SQLException e) {
      printError("SQL Exception in CheckinThread run() method: "
          + e.getMessage());
    }
  }

  /**
   * runs the thread by querying the url for information on user checkins.
   */
  public synchronized void run() {
    List<List<String>> updates = null;

    long lastSec = 0;

    while (true) {
      long sec = System.currentTimeMillis() / MS_CONVERSION;
      if (sec != lastSec && !pause) {
        try {
          updates = this.update();
        } catch (IOException e) {
          e.printStackTrace();
        }

        if (updates != null && !updates.isEmpty()) {
          // Use Try-with-resources
          try (
              PreparedStatement prep = this.dbConn.prepareStatement(
                  "INSERT INTO checkins VALUES (?, ?, ?, ?, ?);")
          ) {
            // Iterate over new updates
            for (List<String> el : updates) {
              double timestamp = Double.parseDouble(el.get(0));
              int id = Integer.parseInt(el.get(1));
              String name = el.get(2);
              double lat = Double.parseDouble(el.get(3));
              double lon = Double.parseDouble(el.get(4));

              // Put in concurrent hashmap
              UserCheckin uc = new UserCheckin(id, name, timestamp, lat, lon);
              checkins.put(timestamp, uc);

              // Add to prepared statement batch
              prep.setInt(1, id);
              prep.setString(2, name);
              prep.setDouble(3, timestamp);
              prep.setDouble(4, lat);
              prep.setDouble(5, lon);
              prep.addBatch();
            }
            // Finally execute batch
            prep.executeBatch();
          } catch (SQLException e) {
            printError("SQL Exception in CheckinThread run() method: "
                + e.getMessage());
          }
        }
        lastSec = sec;
      }
    }
  }

  private synchronized List<List<String>> update() throws IOException {
    URL serverURL = new URL("http://localhost:8080?last=" + last);
    last = Instant.now().getEpochSecond();

    HttpURLConnection conn = (HttpURLConnection) serverURL.openConnection();
    conn.setRequestMethod("GET");

    Pattern pattern = Pattern.compile(
        "\\[(.*?)\\, (.*?)\\, \"(.*?)\", (.*?)\\, (.*?)\\]");
    String line;
    List<List<String>> output = new ArrayList<>();

    // Use try-with-resources
    try (
        BufferedReader br = new BufferedReader(
            new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))
    ) {
      while ((line = br.readLine()) != null) {
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
          List<String> data = new ArrayList<>();
          String parsedTimestamp = matcher.group(1);
          if (parsedTimestamp.charAt(0) == '[') {
            data.add(parsedTimestamp.substring(1));
          } else {
            data.add(parsedTimestamp);
          }
          data.add(matcher.group(2));
          data.add(matcher.group(3));
          data.add(matcher.group(4));
          data.add(matcher.group(5));
          output.add(data);
        }
      }
    } catch (ConnectException ignored) {
      // printInfo("Connection refused. Check that the checkin server is running!");
    }

    return output;
  }

  /**
   * gets the latest checkin updates. Refreshes hashmap so only new
   * checkin updates are returned next time.
   *
   * @return map from a string to a double of timestamps to checkin objects
   */
  public Map<Double, UserCheckin> getLatestCheckins() {
    pause = true;
    Map<Double, UserCheckin> temp = checkins;
    checkins = Collections.synchronizedMap(new HashMap<>());
    pause = false;
    return temp;
  }

  /**
   * A function which gets the all the checkins for a single user and returns it
   * as a list of Objects (for frontend).
   *
   * @param id an int, the id of the user whose data is being requested
   * @return a list of Objects representing the checkins of one user
   */
  public List<Object> getOneUsersCheckinsHandlerForGUI(int id) {
    pause = true;
    List<Object> oneUsersCheckinsList = new ArrayList<>();
    // Use Try-with-resources
    try (
        PreparedStatement prep = this.dbConn.prepareStatement(
            "SELECT * FROM checkins WHERE id = ?;")
    ) {
      prep.setInt(1, id);
      // Nested Try-with-resources necessary
      try (ResultSet rs = prep.executeQuery()) {
        while (rs.next()) {
          Map<String, Object> checkinInfo = ImmutableMap.<String, Object>builder()
              .put("id", rs.getInt(1))
              .put("name", rs.getString(2))
              .put("timestamp", rs.getDouble(3))
              .put("latitude", rs.getDouble(4))
              .put("longitude", rs.getDouble(5))
              .build();
          oneUsersCheckinsList.add(checkinInfo);
        }
      }
    } catch (SQLException e) {
      printError("SQL Exception in CheckinThread getOneUsersCheckinsHandlerForGUI() method: "
          + e.getMessage());
    }
    // Return
    pause = false;
    return oneUsersCheckinsList;
  }

  /**
   * A function which deletes all the checkins for a single user.
   *
   * @param id an int, the id of the user whose data is being requested
   */
  public void deleteUserData(int id) {
    pause = true;
    try (
        PreparedStatement prep = this.dbConn.prepareStatement(
            "DELETE FROM checkins WHERE id = ?;")
    ) {
      prep.setInt(1, id);
      // Nested Try-with-resources necessary
      prep.executeQuery();
    } catch (SQLException e) {
      printError("SQL Exception in CheckinThread getOneUsersCheckinsHandlerForGUI() method: "
          + e.getMessage());
    }
    // Return
    pause = false;
  }
}
