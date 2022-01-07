package edu.brown.cs.jwu175zcheng12.maps;

import com.google.common.collect.ImmutableMap;
import edu.brown.cs.jwu175zcheng12.kdtree.KDTree;
import edu.brown.cs.jwu175zcheng12.repl.GeneralPair;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static edu.brown.cs.jwu175zcheng12.repl.GeneralREPL.printError;

/**
 * A class which represents a database of Nodes and Ways.
 */
public class MapDatabase {

  // CheckStyle catches magic numbers...
  private static final int SEVEN = 7;
  private static final int EIGHT = 8;
  private static final int TEN = 10;
  private static final int ELEVEN = 11;
  private static final int INTERVAL_POW = 2;
  private static final BigDecimal INTERVAL = new BigDecimal("0.01");

  private Connection conn = null;
  private String dbPath = null;

  private final Map<String, Node> tNodeHashMap = new HashMap<>();
  private final Map<String, Way> wayHashMap = new HashMap<>();
  private KDTree<Node> nodeKDTree = null;

  // ----------------------------- Constructors ----------------------------

  /**
   * The constructor for this class. It has no input arguments.
   */
  public MapDatabase() {
  }

  /**
   * Another constructor for this class. This constructor offers the ability to
   * set the databasePath and fill up the nodeHashMap and nodeKDTree
   * immediately.
   *
   * @param dbPath the path to the database
   * @throws SQLException           whenever one of the SQL commands sent by
   *                                Java is malformed
   * @throws ClassNotFoundException when the JDBC driver is not registered
   */
  public MapDatabase(String dbPath) throws SQLException, ClassNotFoundException {
    // Set up this MapDatabase
    this.setUpMapDatabase(dbPath);
  }

  // ------------------------ Connection and Caching -----------------------

  /**
   * A function which sets up the MapDatabase with a new databasePath by
   * clearing all cached data, setting up the database connection, and caching
   * the new data (Nodes only).
   *
   * @param inputDatabasePath the path to the database
   * @throws SQLException           whenever one of the SQL commands sent by
   *                                Java is malformed
   * @throws ClassNotFoundException when the JDBC driver is not registered
   */
  public void setUpMapDatabase(String inputDatabasePath)
      throws ClassNotFoundException, SQLException {
    if (inputDatabasePath == null) {
      throw new IllegalArgumentException(
          "Cannot setupDatabaseConnection with null databasePath.");
    }
    // Clear cached data
    this.clearCachedData();
    // Set this.dbPath
    this.dbPath = inputDatabasePath;
    // Create connection
    try {
      // Setup SQL Driver Manager Class
      Class.forName("org.sqlite.JDBC");
      String urlToDB = "jdbc:sqlite:" + dbPath;
      this.conn = DriverManager.getConnection(urlToDB);
    } catch (ClassNotFoundException e) {
      printError("Failed to register JDBC driver");
      throw e;
    }
    // Enforce Foreign Keys during Operations
    try (Statement stat = this.conn.createStatement()) {
      stat.executeUpdate("PRAGMA foreign_keys = ON;");
    } catch (SQLException e) {
      printError("SQL Failed on setup: " + e.getMessage());
      throw e;
    }
    // Create and fill nodeHashMap and KDTree
    this.fillNodeHashMapAndKDTree();
  }

  /**
   * A helper function which fills the nodeHashMap and KDTree in this
   * MapDatabase with all the traversable Nodes from the database.
   *
   * @throws SQLException whenever one of the SQL commands sent by Java is
   *                      malformed. Usually if the connection is to a nonsense,
   *                      non-.sqlite3 file.
   */
  private void fillNodeHashMapAndKDTree() throws SQLException {
    // Prepare for creation of KDTree
    List<Node> tempKDTreeList = new ArrayList<>();
    // Attempt to query database for Nodes, and fill the Node data structures
    // Use Try-with-resources
    try (
        PreparedStatement prep = this.conn.prepareStatement(
            "SELECT node.id, node.latitude, node.longitude FROM way "
                + "INNER JOIN node ON (way.start == node.id OR way.end == node.id) "
                + "WHERE (way.type != '' AND way.type != 'unclassified');")
    ) {
      // Nested Try-with-resources necessary
      try (ResultSet rs = prep.executeQuery()) {
        // Iterate over returned Nodes
        while (rs.next()) {
          // Extract data
          String nodeId = rs.getString(1);
          double nodeLatitude = rs.getDouble(2);
          double nodeLongitude = rs.getDouble(3);
          // Skip if nodeId already used: violates uniqueness rule
          if (tNodeHashMap.containsKey(nodeId)) {
            continue;
          }
          // Create Node
          Node newNode = new Node(nodeId, nodeLatitude,
              nodeLongitude, this);
          // Put Node in HashMap and tempKDTreeList
          this.tNodeHashMap.put(nodeId, newNode);
          tempKDTreeList.add(newNode);
        }
      }
    } catch (SQLException e) {
      printError("SQL Exception in fillNodeHashMapAndKDTree: "
          + e.getMessage());
      throw e;
    }
    // Set nodeKDTree
    this.nodeKDTree = new KDTree<>(2, tempKDTreeList);
  }

  /**
   * A helper function which clears the nodeHashMap and KDTree in this
   * MapDatabase, effectively clearing all cached information about the
   * database.
   */
  private void clearCachedData() {
    // Clear nodeHashMap and wayHashMap
    this.tNodeHashMap.clear();
    this.wayHashMap.clear();
    // Set nodeKDTree to null
    this.nodeKDTree = null;
  }

  // ------------------------- Searching for Nodes -------------------------

  /**
   * A function which searches the nodeHashMap for a Node which has nodeId
   * equal to searchNodeId. If multiple Nodes have the same nodeId, only one
   * Node will be returned. If no such Node is found, null will be returned.
   *
   * @param searchNodeId the nodeId of the Node to be returned
   * @return the first (and assumed only) Node object in nodeHashMap which has
   * nodeId equal to searchNodeId. Returns null if no such Node is found.
   */
  public Node getNodeById(String searchNodeId) {
    if (this.dbPath == null || this.conn == null || this.nodeKDTree == null) {
      printError("No map data loaded.");
      return null;
    }
    if (!this.tNodeHashMap.containsKey(searchNodeId)) {
      printError("No such Node (ID = \"" + searchNodeId
          + "\") in loaded data.");
      return null;
    }
    return this.tNodeHashMap.get(searchNodeId);
  }

  /**
   * A function which uses the nodeKDTree to get the nearest traversable Node
   * to the input point, given by coordinates = [latitude, longitude]. If no
   * Node is found, null will be returned.
   *
   * @param latitude  a double, the latitude of the search point
   * @param longitude a double, the longitude of the search point
   * @return the nearest traversable Node to that location. Returns null if no
   * Node is found (which implies that there are no Nodes in this database).
   */
  public Node getNearestNode(double latitude, double longitude) {
    if (this.dbPath == null || this.conn == null || this.nodeKDTree == null) {
      printError("No map data loaded.");
      return null;
    }
    List<Node> ans = this.nodeKDTree
        .findKNearestNeighbors(1, new Double[]{latitude, longitude});
    if (ans.size() == 0) {
      printError("No Nodes in loaded data.");
      return null;
    }
    return ans.get(0);
  }

  /**
   * A function which gets the intersection Node between two ways, given their
   * names. If they do not intersect at all, this function returns null.
   *
   * @param way1Name the first way's name
   * @param way2Name the second way's name
   * @return the first (and assumed only) Node of intersection between way1 and
   * way2. If both Ways share two Nodes, only one will be returned. If both Ways
   * share no Nodes, null will be returned.
   */
  public Node getIntersectionNode(String way1Name, String way2Name) {
    if (this.dbPath == null || this.conn == null) {
      printError("No map data loaded.");
      return null;
    }
    if (way1Name == null || way2Name == null) {
      printError("Way names cannot be null.");
      return null;
    }
    if (way1Name.equals("") || way2Name.equals("")) {
      printError("Way names cannot be empty Strings.");
      return null;
    }
    // Use Try-with-resources
    try (PreparedStatement prep = this.conn.prepareStatement(
        "SELECT "
            + "CASE WHEN W1.start = W2.end THEN W1.start "
            + "WHEN W1.end = W2.start THEN W1.end "
            + "WHEN W1.start = W2.start THEN W1.start "
            + "WHEN W1.end = W2.end THEN W1.end "
            + "ELSE ? "
            + "END AS nodeid "
            + "FROM Way AS W1, Way AS W2 "
            + "WHERE W1.name = ? AND W2.name = ? AND nodeid != 'NONE' "
            + "LIMIT 1;")) {
      prep.setString(1, "NONE");
      prep.setString(2, way1Name);
      prep.setString(3, way2Name);
      // Nested Try-with-resources necessary
      try (ResultSet rs = prep.executeQuery()) {
        rs.next();
        String nodeId = rs.getString(1);
        // Check if the idNodeMap contains a node with this id,
        // i.e. check if the node with this id is traversable
        if (!this.tNodeHashMap.containsKey(nodeId)) {
          return null;
        }
        return this.tNodeHashMap.get(nodeId);
      }
    } catch (SQLException e) {
      if ("ResultSet closed".equals(e.getMessage())) {
        printError("No intersection between ways (way1Name = \"" + way1Name
            + "\", way2Name = \"" + way2Name + "\").");
      } else {
        printError("SQL Exception when searching database for intersection: "
                + e.getMessage());
      }
      return null;
    }
  }

  // -------------------------- Searching for Ways -------------------------

  /**
   * A function which searches the database and returns all the Ways that lie
   * inside a bounding box. The box has northwest point (lat1, lon1) and
   * southeast point (lat2, lon2).
   * <p>
   * Only traversable ways, i.e. Ways, are returned!
   * <p>
   * Note: "Inside" means that at least one end of the way lies within the
   * bounding box (inclusive).
   * Note: For this project, a point being "northwest" means it has greater
   * latitude and lower longitude than the southeast point.
   * <p>
   * Effects: also adds any traversable Ways it finds to the wayHashMap, if not
   * already in there.
   *
   * @param lat1 the latitude of the northwest point of the bounding box
   * @param lon1 the longitude of the northwest point of the bounding box
   * @param lat2 the latitude of the southeast point of the bounding box
   * @param lon2 the longitude of the southeast point of the bounding box
   * @return a list of Ways that lie inside the bounding box; traversable only.
   * Returns null if no map data is loaded, or an SQL exception is encountered.
   */
  public List<Way> getWaysInBox(double lat1, double lon1,
                                double lat2, double lon2) {
    List<String> wayIds = getWayIdsInBox(lat1, lon1, lat2, lon2);
    if (wayIds == null) {
      return null;
    }
    List<Way> results = new ArrayList<>();
    for (String wayId : wayIds) {
      if (wayHashMap.containsKey(wayId)) {
        results.add(wayHashMap.get(wayId));
      }
    }
    return results;
  }

  /**
   * A function which searches the database and returns all the wayIds of ways
   * that lie inside a bounding box. The box has northwest point (lat1, lon1)
   * and southeast point (lat2, lon2).
   * <p>
   * Both traversable and non-traversable ways are returned!
   * <p>
   * Note: "Inside" means that at least one end of the way lies within the
   * bounding box (inclusive).
   * Note: For this project, a point being "northwest" means it has greater
   * latitude and lower longitude than the southeast point.
   * <p>
   * Effects: also adds any traversable Ways it finds to the wayHashMap, if not
   * already in there.
   *
   * @param lat1 the latitude of the northwest point of the bounding box
   * @param lon1 the longitude of the northwest point of the bounding box
   * @param lat2 the latitude of the southeast point of the bounding box
   * @param lon2 the longitude of the southeast point of the bounding box
   * @return an alphabetically-sorted list of wayIds representing ways that lie
   * inside the bounding box, both traversable and non-traversable. Returns null
   * if no map data is loaded, or an SQL exception is encountered.
   */
  public List<String> getWayIdsInBox(double lat1, double lon1,
                                     double lat2, double lon2) {
    if (this.dbPath == null || this.conn == null || this.nodeKDTree == null) {
      printError("No map data loaded.");
      return null;
    }
    if (lat1 < lat2 || lon1 > lon2) {
      printError("Check your coordinates. The bounding box must be defined by"
          + " its northwest point first, then its southeast point.");
      return null;
    }
    List<String> wayIdsInBox = new ArrayList<>();
    // Use Try-with-resources
    try (PreparedStatement prep = this.conn.prepareStatement(
        "SELECT * FROM way "
            + "INNER JOIN node s ON way.start == s.id "
            + "INNER JOIN node e ON way.end == e.id "
            + "WHERE (s.latitude <= ? AND s.latitude >= ? "
            + "AND s.longitude >= ? AND s.longitude <= ?) "
            + "OR (e.latitude <= ? AND e.latitude >= ? "
            + "AND e.longitude >= ? AND e.longitude <= ?) "
            + "ORDER BY way.id;")) {
      prep.setDouble(1, lat1);
      prep.setDouble(2, lat2);
      prep.setDouble(3, lon1);
      prep.setDouble(4, lon2);
      prep.setDouble(5, lat1);
      prep.setDouble(6, lat2);
      prep.setDouble(SEVEN, lon1);
      prep.setDouble(EIGHT, lon2);
      // Nested Try-with-resources necessary
      try (ResultSet rs = prep.executeQuery()) {
        while (rs.next()) {
          String wayId = rs.getString(1);
          String name = rs.getString(2);
          String type = rs.getString(3);
          String startNodeID = rs.getString(4);
          String endNodeID = rs.getString(5);
          // If way is traversable and not already in wayHashMap
          if (tNodeHashMap.containsKey(startNodeID)
              && tNodeHashMap.containsKey(endNodeID)
              && !wayHashMap.containsKey(wayId)
              && !type.equals("")
              && !type.equals("unclassified")) {
            // Create Way and add to wayHashMap
            this.wayHashMap.put(wayId,
                new Way(wayId, tNodeHashMap.get(startNodeID),
                    tNodeHashMap.get(endNodeID), name, type));
          }
          // Add wayId to wayIdsInBox
          wayIdsInBox.add(wayId);
        }
      }
    } catch (SQLException e) {
      printError("SQL Exception when finding ways in bounding box (NorthWest = ["
          + lat1 + ", " + lon1 + "] and SouthEast = ["
          + lat2 + ", " + lon2 + "]: " + e.getMessage());
      return null;
    }
    // Return
    return wayIdsInBox;
  }

  /**
   * A function which searches the database and returns two things:
   * (1) a hashmap of wayIds to hashmaps which contain information about that
   * way (the starting latitude and longitude, the ending latitude and
   * longitude, the type of the way, and the name of the way), and
   * (2) a hashmap of hashmaps of lists of Strings (wayIds), representing the
   * cells of the map for the GUI. The key of the outer hashmap is the latitude
   * of the top left point of a specific cell. This is mapped to an inner
   * hashmap where the key is the longitude of the top left point of a specific
   * cell. Finally, this is mapped to a list of all the WayIDs which are
   * contained in that specific cell. As demonstrated above, cells are
   * represented by their northwest corner, whose latitude is the first index
   * and longitude is the second.
   * <p>
   * It is assumed that lat1, lon1, lat2, and lon2 all lie perfectly on the grid
   * defined by our step! i.e. if INTERVAL = 0.01, then these must be multiples
   * of 0.01.
   * <p>
   * Both traversable and non-traversable ways are returned!
   *
   * @param lat1 the latitude of the northwest point of the bounding box
   * @param lon1 the longitude of the northwest point of the bounding box
   * @param lat2 the latitude of the southeast point of the bounding box
   * @param lon2 the longitude of the southeast point of the bounding box
   * @return A pair of information - the first value being a map containing
   * all way information, the second value containing information about which
   * ways are included in each cell.
   */
  public GeneralPair<Map<String, Object>, Map<String, Map<String, List<String>>>>
        getWaysAndCellsForGUI(BigDecimal lat1, BigDecimal lon1,
                              BigDecimal lat2, BigDecimal lon2) {
    if (this.dbPath == null || this.conn == null || this.nodeKDTree == null) {
      printError("No map data loaded.");
      return null;
    }
    if (lat1.compareTo(lat2) <= 0 || lon1.compareTo(lon2) >= 0) {
      printError("Check your coordinates. The bounding box must be defined by"
          + " its northwest point first, then its southeast point.");
      return null;
    }

    // Prepare return variables
    Map<String, Object> ways = new HashMap<>();
    Map<String, Map<String, List<String>>> cells = new HashMap<>();
    initializeCellsWithEmptyLists(cells, lat1, lon1, lat2, lon2);
    // Use Try-with-resources
    try (PreparedStatement prep = this.conn.prepareStatement(
        "SELECT * FROM way "
            + "INNER JOIN node s ON way.start == s.id "
            + "INNER JOIN node e ON way.end == e.id "
            + "WHERE (s.latitude <= ? AND s.latitude >= ? "
            + "AND s.longitude >= ? AND s.longitude <= ?) "
            + "OR (e.latitude <= ? AND e.latitude >= ? "
            + "AND e.longitude >= ? AND e.longitude <= ?);")) {
      prep.setBigDecimal(1, lat1);
      prep.setBigDecimal(2, lat2);
      prep.setBigDecimal(3, lon1);
      prep.setBigDecimal(4, lon2);
      prep.setBigDecimal(5, lat1);
      prep.setBigDecimal(6, lat2);
      prep.setBigDecimal(SEVEN, lon1);
      prep.setBigDecimal(EIGHT, lon2);
      // Nested Try-with-resources necessary
      try (ResultSet rs = prep.executeQuery()) {
        while (rs.next()) {
          String wayId = rs.getString(1);
          String wayName = rs.getString(2);
          String wayType = rs.getString(3);
          BigDecimal startLat = rs.getBigDecimal(SEVEN);
          BigDecimal startLon = rs.getBigDecimal(EIGHT);
          BigDecimal endLat = rs.getBigDecimal(TEN);
          BigDecimal endLon = rs.getBigDecimal(ELEVEN);
          if (!ways.containsKey(wayId)) {
            Map<String, Object> wayObject = ImmutableMap.<String, Object>builder()
                .put("startLat", startLat)
                .put("startLon", startLon)
                .put("endLat", endLat)
                .put("endLon", endLon)
                .put("type", wayType)
                .put("name", wayName)
                .build();
            ways.put(wayId, wayObject);
          }
          addWayToCell(wayId, cells, roundUpLat(startLat), roundDownLon(startLon));
          addWayToCell(wayId, cells, roundUpLat(endLat), roundDownLon(endLon));
        }
      }
    } catch (SQLException e) {
      printError("SQL Exception when finding ways in bounding box (NorthWest = ["
          + lat1 + ", " + lon1 + "] and SouthEast = ["
          + lat2 + ", " + lon2 + "]: " + e.getMessage());
      return null;
    }
    // Return
    return new GeneralPair<>(ways, cells);
  }

  /**
   * A function which searches the database to get all the Ways that lead in/out
   * from a given Node by doing a database query. The Node is assumed to already
   * be in our tNodeHashMap (and nodeKDTree, but that's not used).
   * <p>
   * Only traversable ways, i.e. Ways, are returned!
   * <p>
   * Effects: also adds any traversable Ways it finds to the wayHashMap, if not
   * already in there.
   *
   * @param nodeId a String, the Id of this Node
   * @param out    a boolean, whether the requested Ways are those that lead out
   *               of the Node (true), or lead into the Node (false)
   * @return a Set of Ways which leads out/in of this Node. Returns null if no
   * map data is loaded, or an SQL exception is encountered.
   */
  public Set<Way> getWaysInOrOutOfNode(String nodeId, boolean out) {
    if (this.dbPath == null || this.conn == null || this.nodeKDTree == null) {
      printError("No map data loaded.");
      return null;
    }
    if (!this.tNodeHashMap.containsKey(nodeId)) {
      printError("No such Node (ID = \"" + nodeId
          + "\") in loaded data; cannot find ways in/out of Node.");
      return null;
    }
    Set<Way> result = new HashSet<>();
    // Decide in or out
    String sqlString = "SELECT * FROM way "
        + "WHERE end = ? AND way.type != '' AND way.type != 'unclassified';";
    if (out) {
      sqlString = "SELECT * FROM way "
          + "WHERE start = ? AND way.type != '' AND way.type != 'unclassified';";
    }
    // Use Try-with-resources
    try (PreparedStatement prep = this.conn.prepareStatement(sqlString)) {
      prep.setString(1, nodeId);
      // Nested Try-with-resources necessary
      try (ResultSet rs = prep.executeQuery()) {
        while (rs.next()) {
          String wayId = rs.getString(1);
          String name = rs.getString(2);
          String type = rs.getString(3);
          String startNodeID = rs.getString(4);
          String endNodeID = rs.getString(5);
          // If way is already in wayHashMap
          if (wayHashMap.containsKey(wayId)) {
            // Add to results
            result.add(wayHashMap.get(wayId));
          } else {
            // Create Way
            Way newWay = new Way(wayId, tNodeHashMap.get(startNodeID),
                tNodeHashMap.get(endNodeID), name, type);
            // Add to wayHashMap and results
            this.wayHashMap.put(wayId, newWay);
            result.add(newWay);
          }
        }
      }
    } catch (SQLException e) {
      printError("SQL Exception when searching database for Ways "
          + "leading out/in of Node (ID = \""
          + nodeId + "\"): " + e.getMessage());
      return null;
    }
    // Return
    return result;
  }

  /**
   * A function which searches the wayHashMap, then the database if not found,
   * for a way which has wayId equal to searchWayId. If multiple Ways have the
   * same wayId, only one Way will be returned. If no such Way is found, null
   * will be returned.
   * <p>
   * Only traversable ways, i.e. Ways, are returned! No use case for returning a
   * non-traversable one, anyway.
   *
   * @param searchWayId the wayId of the Way to be returned
   * @return the first (and assumed only) Way in the database which has
   * wayId equal to searchWayId. Returns null if no map data is loaded, no such
   * Way is found, or an SQL exception is encountered.
   */
  public Way getWayById(String searchWayId) {
    if (this.dbPath == null || this.conn == null || this.nodeKDTree == null) {
      printError("No map data loaded.");
      return null;
    }
    if (this.wayHashMap.containsKey(searchWayId)) {
      return this.wayHashMap.get(searchWayId);
    }
    // Use Try-with-resources
    try (PreparedStatement prep = this.conn.prepareStatement(
        "SELECT * FROM way WHERE id = ?;")) {
      prep.setString(1, searchWayId);
      // Nested Try-with-resources necessary
      try (ResultSet rs = prep.executeQuery()) {
        // Grabs the next one that fits and grabs all of the Way Data
        rs.next();
        String wayId = rs.getString(1);
        String name = rs.getString(2);
        String type = rs.getString(3);
        String startNodeID = rs.getString(4);
        String endNodeID = rs.getString(5);
        // Creates the new Way (definitely not in wayHashMap at this point)
        Way newWay = new Way(wayId, getNodeById(startNodeID),
            getNodeById(endNodeID), name, type);
        // Add to wayHashMap and return newWay
        this.wayHashMap.put(wayId, newWay);
        return newWay;
      }
    } catch (SQLException e) {
      if ("ResultSet closed".equals(e.getMessage())) {
        printError("No such Way (ID = \"" + searchWayId
            + "\") in database.");
      } else {
        printError(
            "SQL Exception when searching database for Way (ID = \""
                + searchWayId + "\"): " + e.getMessage());
      }
      return null;
    }
  }

  /**
   * A function which searches the database for a Way which has name equal to
   * searchWayName. If multiple Ways have the same name, only one Way will be
   * returned. If no such Way is found, null will be returned.
   * <p>
   * Only traversable ways, i.e. Ways, are returned! No use case for returning a
   * non-traversable one, anyway.
   *
   * @param searchWayName the name of the Way to be returned
   * @return the first (and assumed only) Way in the database which has
   * name equal to searchWayName. Returns null if no map data is loaded, no such
   * Way is found, or an SQL exception is encountered.
   */
  public Way getWayByName(String searchWayName) {
    if (this.dbPath == null || this.conn == null || this.nodeKDTree == null) {
      printError("No map data loaded.");
      return null;
    }
    // Use Try-with-resources
    try (PreparedStatement prep = this.conn.prepareStatement(
        "SELECT * FROM way WHERE name = ?;")) {
      prep.setString(1, searchWayName);
      // Nested Try-with-resources necessary
      try (ResultSet rs = prep.executeQuery()) {
        // Grabs the next one that fits and grabs all of the Way Data
        rs.next();
        String wayId = rs.getString(1);
        String name = rs.getString(2);
        String type = rs.getString(3);
        String startNodeID = rs.getString(4);
        String endNodeID = rs.getString(5);
        // If way is already in wayHashMap
        if (wayHashMap.containsKey(wayId)) {
          return this.wayHashMap.get(wayId);
        }
        // Create Way
        Way newWay = new Way(wayId, tNodeHashMap.get(startNodeID),
            tNodeHashMap.get(endNodeID), name, type);
        // Add to wayHashMap and return newWay
        this.wayHashMap.put(wayId, newWay);
        return newWay;
      }
    } catch (SQLException e) {
      if ("ResultSet closed".equals(e.getMessage())) {
        printError("No such Way (Name = \"" + searchWayName
            + "\") in database.");
      } else {
        printError(
            "SQL Exception when searching database for Way (Name = \""
                + searchWayName + "\"): " + e.getMessage());
      }
      return null;
    }
  }

  // --------------------------- Helper Functions --------------------------

  /**
   * A private helper function which rounds BigDecimal values up to the
   * nearest hundredth (or whatever interval).
   *
   * @param lat a BigDecimal to be rounded.
   * @return lat rounded up to the nearest hundredth.
   */
  private static String roundUpLat(BigDecimal lat) {
    return lat.setScale(INTERVAL_POW, RoundingMode.CEILING)
        .stripTrailingZeros().toPlainString();
  }

  /**
   * A private helper function which rounds BigDecimal values down to the
   * nearest hundredth (or whatever interval).
   *
   * @param lon a BigDecimal to be rounded.
   * @return lon rounded down to the nearest hundredth.
   */
  private static String roundDownLon(BigDecimal lon) {
    return lon.setScale(INTERVAL_POW, RoundingMode.FLOOR)
        .stripTrailingZeros().toPlainString();
  }

  /**
   * A private helper function which initializes the cells to be returned in
   * getWaysAndCellsForGUI() with empty lists.
   *
   * @param cells the cells hashmap
   * @param lat1  the latitude of the northwest point of the bounding box
   * @param lon1  the longitude of the northwest point of the bounding box
   * @param lat2  the latitude of the southeast point of the bounding box
   * @param lon2  the longitude of the southeast point of the bounding box
   */
  private void initializeCellsWithEmptyLists(Map<String, Map<String, List<String>>> cells,
                                             BigDecimal lat1, BigDecimal lon1,
                                             BigDecimal lat2, BigDecimal lon2) {
    for (BigDecimal lat = lat1; lat.compareTo(lat2) > 0; lat = lat.subtract(INTERVAL)) {
      String latString = lat.stripTrailingZeros().toPlainString();
      cells.put(latString, new HashMap<>());
      for (BigDecimal lon = lon1; lon.compareTo(lon2) < 0; lon = lon.add(INTERVAL)) {
        String lonString = lon.stripTrailingZeros().toPlainString();
        cells.get(latString).put(lonString, new ArrayList<>());
      }
    }
  }

  /**
   * A PRIVATE (only public for the sake of JUnit testing) helper function which
   * puts a wayId into the correct cell, given the closest northwest grid
   * intersection point (i.e. the cell's indices).
   * Remember that both ends of a way count to its placement in a cell!
   *
   * @param wayId      a String, the Id of any way, traversable or otherwise
   * @param cells      the cells hashmap
   * @param lat1String the latitude of the cell this way is in
   * @param lon1String the longitude of the cell this way is in
   */
  public void addWayToCell(String wayId,
                           Map<String, Map<String, List<String>>> cells,
                           String lat1String, String lon1String) {
    if (!cells.containsKey(lat1String)) {
      return;
    }
    if (!cells.get(lat1String).containsKey(lon1String)) {
      return;
    }
    cells.get(lat1String).get(lon1String).add(wayId);
  }
}
