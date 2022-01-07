package edu.brown.cs.jwu175zcheng12.maps;

import edu.brown.cs.jwu175zcheng12.repl.GeneralPair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;

import static org.junit.Assert.*;

/**
 * A class which tests the MapDatabase class.
 */
public class MapDatabaseTest {

  private MapDatabase smallMapsDb;
  private MapDatabase emptyMapsDb;
  private MapDatabase tiedMapsDb;
  private MapDatabase nonTraversableDb;

  /**
   * Sets up the MapDatabases used for testing.
   */
  @Before
  public void setUp() {
    try {
      smallMapsDb = new MapDatabase("data/maps/smallMaps.sqlite3");
      emptyMapsDb = new MapDatabase("data/maps/empty.sqlite3");
      tiedMapsDb = new MapDatabase("data/maps/tied.sqlite3");
      nonTraversableDb = new MapDatabase("data/maps/smallMapsWithNontraversables.sqlite3");
    } catch (SQLException | ClassNotFoundException ignored) {
    }
  }

  /**
   * Tears Down the MapDatabases used for testing.
   */
  @After
  public void tearDown() {
    smallMapsDb = null;
    emptyMapsDb = null;
    tiedMapsDb = null;
    nonTraversableDb = null;
  }

  /**
   * Tests the Throwable Errors/Exceptions of the MapDatabases
   */
  @Test
  public void testSetup() {
    IllegalArgumentException nullPath = assertThrows(
        IllegalArgumentException.class,
        () -> new MapDatabase(null));
    assertTrue(nullPath.getMessage().contains("Cannot setupDatabaseConnection with null databasePath."));
    SQLException badPath = assertThrows(
        SQLException.class,
        () -> new MapDatabase("/bad/path/to/database"));
    assertTrue(badPath.getMessage().contains("path to '/bad/path/to/database': '/bad' does not exist"));
  }

  /**
   * Tests the getNearestNode() function.
   */
  @Test
  public void testGetNearestNode() {
    setUp();
    // Test Null Data case
    assertNull(new MapDatabase().getNearestNode(0.0, 0.0));

    // Test Empty Data case
    assertNull(emptyMapsDb.getNearestNode(0.1, 1.0));

    // Test Get Nearest Node
    assertEquals(smallMapsDb.getNearestNode(0.0, 0.0),
        new Node("/n/0", 41.82, -71.4));
    assertEquals(smallMapsDb.getNearestNode(0.0, -100.0),
        new Node("/n/3", 41.82, -71.4003));

    // Test Tied Nodes [Random]
    List<Node> possibleTiedNodes = new ArrayList<>();
    possibleTiedNodes.add(new Node("/n/0", 10.0, 20.0));
    possibleTiedNodes.add(new Node("/n/1", 10.0, 20.0));
    possibleTiedNodes.add(new Node("/n/2", 10.0, 20.0));
    assertTrue(possibleTiedNodes.contains(tiedMapsDb.getNearestNode(10.0, 19.0)));
    tearDown();
  }

  /**
   * Tests the getNodeById() function.
   */
  @Test
  public void testGetNodeById() {
    setUp();
    // Tests that you have to load the data first
    assertNull(new MapDatabase().getNodeById("Node"));

    // Test getNodeById Querying HashMap
    assertEquals(new Node("/n/0", 0., 0.),
        smallMapsDb.getNodeById("/n/0"));
    assertEquals(new Node("/n/1", 0., 0.),
        smallMapsDb.getNodeById("/n/1"));
    assertEquals(new Node("/n/5", 0., 0.),
        smallMapsDb.getNodeById("/n/5"));

    // Tests that invalid nodeId prints error
    assertNull(smallMapsDb.getNodeById("/n/not_a_real_node"));
    assertNull(emptyMapsDb.getNodeById("/n/there_are_no_nodes"));

    tearDown();
  }

  /**
   * Tests the getIntersectionNode function.
   */
  @Test
  public void testGetIntersectionNode() {
    setUp();

    // Test failure before setup
    MapDatabase testDb = new MapDatabase();
    assertNull(testDb.getIntersectionNode(null, null));

    // Null Cases return null
    assertNull(smallMapsDb.getIntersectionNode(null, null));
    assertNull(smallMapsDb.getIntersectionNode("", ""));
    assertNull(smallMapsDb.getIntersectionNode("a", ""));
    assertNull(smallMapsDb.getIntersectionNode("", "b"));
    assertNull(smallMapsDb.getIntersectionNode(null, "/w/0"));
    assertNull(smallMapsDb.getIntersectionNode("/w/0", null));
    assertNull(smallMapsDb.getIntersectionNode("/w/0", "/w/not/a/way"));

    // Tests all possible Intersections
    assertEquals(
        smallMapsDb.getIntersectionNode("Chihiro Ave", "Radish Spirit Blvd"),
        smallMapsDb.getNodeById("/n/0"));
    assertEquals(
        smallMapsDb.getIntersectionNode("Kamaji Pl", "Yubaba St"),
        smallMapsDb.getNodeById("/n/5"));
    Node test = smallMapsDb.getIntersectionNode("Chihiro Ave", "Chihiro Ave");
    List<Node> possibleValues = Arrays.asList(smallMapsDb.getNodeById("/n/0"),
        smallMapsDb.getNodeById("/n/1"),
        smallMapsDb.getNodeById("/n/2"));
    assertTrue(possibleValues.contains(test));
    assertEquals(
        smallMapsDb.getIntersectionNode("Chihiro Ave", "Kamaji Pl"),
        smallMapsDb.getNodeById("/n/2"));

    // No Intersection also returns null
    assertNull(
        smallMapsDb.getIntersectionNode("Radish Spirit Blvd", "Kamaji Pl"));

    tearDown();
  }

  /**
   * Tests the getWayById() function.
   */
  @Test
  public void testGetWayById() {
    setUp();
    // Tests that data must be loaded first
    assertNull(new MapDatabase().getWayById("/w/not_loaded"));

    // Test getWayById Querying Database
    Node n0 = smallMapsDb.getNodeById("/n/0");
    Node n1 = smallMapsDb.getNodeById("/n/1");
    Node n4 = smallMapsDb.getNodeById("/n/4");
    Node n5 = smallMapsDb.getNodeById("/n/5");
    assertEquals(new Way("/w/0", n0, n1, "Chihiro Ave", "residential"),
        smallMapsDb.getWayById("/w/0"));
    assertEquals(new Way("/w/3", n1, n4, "Sootball Ln", "residential"),
        smallMapsDb.getWayById("/w/3"));
    assertEquals(new Way("/w/6", n4, n5, "Yubaba St", "residential"),
        smallMapsDb.getWayById("/w/6"));

    // Tests that invalid wayId prints error
    assertNull(smallMapsDb.getWayById("/w/not_a_real_way"));
    assertNull(emptyMapsDb.getWayById("/w/there_are_no_ways"));
    tearDown();
  }

  /**
   * Tests the getWayByName() function.
   */
  @Test
  public void testGetWayByName() {
    setUp();
    // Tests that data must be loaded first
    assertNull(new MapDatabase().getWayByName("Nothing_Loaded"));

    // Test getWayByName Querying Database
    Node n0 = smallMapsDb.getNodeById("/n/0");
    Node n1 = smallMapsDb.getNodeById("/n/1");
    Node n2 = smallMapsDb.getNodeById("/n/2");
    Node n3 = smallMapsDb.getNodeById("/n/3");
    Node n4 = smallMapsDb.getNodeById("/n/4");
    Node n5 = smallMapsDb.getNodeById("/n/5");

    HashSet<Way> chihiroAveWays = new HashSet<>();
    chihiroAveWays.add(new Way("/w/0", n0, n1, "Chihiro Ave", "residential"));
    chihiroAveWays.add(new Way("/w/1", n1, n2, "Chihiro Ave", "residential"));
    assertTrue(chihiroAveWays.contains(smallMapsDb.getWayByName("Chihiro Ave")));

    assertEquals(new Way("/w/3", n1, n4, "Sootball Ln", "residential"),
        smallMapsDb.getWayByName("Sootball Ln"));

    HashSet<Way> yuBabaStWays = new HashSet<>();
    yuBabaStWays.add(new Way("/w/5", n3, n4, "Yubaba St", "residential"));
    yuBabaStWays.add(new Way("/w/6", n4, n5, "Yubaba St", "residential"));
    assertTrue(yuBabaStWays.contains(smallMapsDb.getWayByName("Yubaba St")));

    // Tests that invalid wayName prints error
    assertNull(smallMapsDb.getWayByName("Not_in_DB"));
    assertNull(emptyMapsDb.getWayByName("DB_is_empty"));
    tearDown();
  }

  /**
   * Tests that the BoundingBox captures the right Ways.
   */
  @Test
  public void testGetWaysInBox() {
    setUp();
    // No Nodes in Bounding Box when not loaded
    assertNull(new MapDatabase().getWaysInBox(10.0, -10.0, -10.0, 10.0));

    // Empty ArrayList when empty
    List<Way> emptyWays = new ArrayList<>();
    assertEquals(emptyMapsDb.getWaysInBox(10.0, -10.0, -10.0, 10.0), emptyWays);

    // Returns null if bounding box northwest is not northwest
    assertNull(emptyMapsDb.getWaysInBox(1, 1, 0, 0));

    // Inclusive Bounds with ASC Order By ID
    List<Way> smallMapsInclusive = new ArrayList<>();
    smallMapsInclusive.add(smallMapsDb.getWayById("/w/0"));
    smallMapsInclusive.add(smallMapsDb.getWayById("/w/2"));
    assertEquals(smallMapsDb.getWaysInBox(41.82, -71.4, -10.0, 10.0), smallMapsInclusive);
    List<Way> tiedMapsInclusive = new ArrayList<>();
    tiedMapsInclusive.add(tiedMapsDb.getWayById("/w/0"));
    tiedMapsInclusive.add(tiedMapsDb.getWayById("/w/1"));
    tiedMapsInclusive.add(tiedMapsDb.getWayById("/w/2"));
    tiedMapsInclusive.add(tiedMapsDb.getWayById("/w/3"));
    tiedMapsInclusive.add(tiedMapsDb.getWayById("/w/4"));
    assertEquals(tiedMapsDb.getWaysInBox(10.0, -10.0, -10.0, 20.0), tiedMapsInclusive);

    // Must be Bounded Both Ways
    // Neither
    assertEquals(smallMapsDb.getWaysInBox(10.0, -10.0, -10.0, 10.0), emptyWays);
    assertEquals(tiedMapsDb.getWaysInBox(9.0, -10.0, -10.0, 10.0), emptyWays);

    // North-South Bound only
    assertEquals(smallMapsDb.getWaysInBox(42.0, -10.0, 40.0, 10.0), emptyWays);
    assertEquals(tiedMapsDb.getWaysInBox(15.0, -10.0, -5.0, 10.0), emptyWays);

    // East-West Bound only
    assertEquals(smallMapsDb.getWaysInBox(10.0, -72.0, -10.0, -70.0), emptyWays);
    assertEquals(tiedMapsDb.getWaysInBox(9.0, -10.0, -5.0, 20.0), emptyWays);

    // General Has Nodes in Bounding Box and ASC Order by ID
    // 42 -72 41.8 -71.3
    List<Way> smallMapsAllWays = new ArrayList<>();
    smallMapsAllWays.add(smallMapsDb.getWayById("/w/0"));
    smallMapsAllWays.add(smallMapsDb.getWayById("/w/1"));
    smallMapsAllWays.add(smallMapsDb.getWayById("/w/2"));
    smallMapsAllWays.add(smallMapsDb.getWayById("/w/3"));
    smallMapsAllWays.add(smallMapsDb.getWayById("/w/4"));
    smallMapsAllWays.add(smallMapsDb.getWayById("/w/5"));
    smallMapsAllWays.add(smallMapsDb.getWayById("/w/6"));
    assertEquals(smallMapsDb.getWaysInBox(42.0, -72.0, 41.8, -71.3), smallMapsAllWays);
    tearDown();
  }

  /**
   * Tests that we collect the right ways Out of Node.
   */
  @Test
  public void testGetWaysOutOfNode() {
    setUp();
    // Data is not loaded
    assertNull(new MapDatabase().getWaysInOrOutOfNode("Not_loaded", true));

    // Node doesnt exist in Data
    assertNull(emptyMapsDb.getWaysInOrOutOfNode("No_nodes", true));
    assertNull(smallMapsDb.getWaysInOrOutOfNode("Not_in_DB", true));

    // No ways out
    HashSet<Way> emptySet = new HashSet<>();
    assertEquals(smallMapsDb.getWaysInOrOutOfNode("/n/5", true), emptySet);
    assertEquals(tiedMapsDb.getWaysInOrOutOfNode("/n/5", true), emptySet);

    // Has ways out
    HashSet<Way> smallMapOut = new HashSet<>();
    smallMapOut.add(smallMapsDb.getWayById("/w/0"));
    smallMapOut.add(smallMapsDb.getWayById("/w/2"));
    assertEquals(smallMapsDb.getWaysInOrOutOfNode("/n/0", true), smallMapOut);
    assertEquals(tiedMapsDb.getWaysInOrOutOfNode("/n/0", true), smallMapOut);
    tearDown();
  }

  /**
   * Tests that we collect the right ways Into the Node.
   */
  @Test
  public void testGetWaysIntoTheNode() {
    setUp();
    // Data is not loaded
    assertNull(new MapDatabase().getWaysInOrOutOfNode("Not_loaded", false));

    // Node doesnt exist in Data
    assertNull(emptyMapsDb.getWaysInOrOutOfNode("No_nodes", false));
    assertNull(smallMapsDb.getWaysInOrOutOfNode("Not_in_DB", false));

    // No ways in
    HashSet<Way> emptySet = new HashSet<>();
    assertEquals(smallMapsDb.getWaysInOrOutOfNode("/n/0", false), emptySet);
    assertEquals(tiedMapsDb.getWaysInOrOutOfNode("/n/0", false), emptySet);

    // Has ways in
    HashSet<Way> smallMapOut = new HashSet<>();
    smallMapOut.add(smallMapsDb.getWayById("/w/4"));
    smallMapOut.add(smallMapsDb.getWayById("/w/6"));
    assertEquals(smallMapsDb.getWaysInOrOutOfNode("/n/5", false), smallMapOut);
    assertEquals(tiedMapsDb.getWaysInOrOutOfNode("/n/5", false), smallMapOut);
    tearDown();
  }

  /**
   * Tests that we collect the right ways to send to the GUI.
   */
  @Test
  public void testGetWaysAndCellsForGUI() {
    setUp();

    // Test helper
    MapDatabase testDb = new MapDatabase();
    Map<String, Map<String, List<String>>> cells = new HashMap<>();
    cells.put("40", new HashMap<>());
    cells.get("40").put("70", new ArrayList<>());
    cells.get("40").get("70").add("myWayId 1");
    cells.get("40").get("70").add("myWayId 2");
    assertEquals(2, cells.get("40").get("70").size());
    assertTrue(cells.get("40").get("70").contains("myWayId 1"));
    assertTrue(cells.get("40").get("70").contains("myWayId 2"));
    testDb.addWayToCell("myWayId 3", cells, "30", "70"); // Does nothing
    assertEquals(2, cells.get("40").get("70").size());
    testDb.addWayToCell("myWayId 3", cells, "40", "80"); // Does nothing
    assertEquals(2, cells.get("40").get("70").size());
    testDb.addWayToCell("myWayId 3", cells, "40", "70"); // Does something!
    assertEquals(3, cells.get("40").get("70").size());
    assertTrue(cells.get("40").get("70").contains("myWayId 3"));

    // No Nodes in Bounding Box when not loaded
    assertNull(new MapDatabase().getWaysAndCellsForGUI(BigDecimal.valueOf(10.0),
        BigDecimal.valueOf(-10.0), BigDecimal.valueOf(-10.0), BigDecimal.valueOf(10.0)));

    // Empty ArrayList when empty
    GeneralPair<Map<String, Object>, Map<String, Map<String, List<String>>>>
        response = emptyMapsDb.getWaysAndCellsForGUI(BigDecimal.valueOf(10.0), BigDecimal.valueOf(-10.0),
        BigDecimal.valueOf(-10.0), BigDecimal.valueOf(10.0));
    assertTrue(response.getFirst().isEmpty());

    // Returns null if bounding box northwest is not northwest
    assertNull(emptyMapsDb.getWaysAndCellsForGUI(BigDecimal.ONE, BigDecimal.ONE,
        BigDecimal.ZERO, BigDecimal.ZERO));

    response = nonTraversableDb.getWaysAndCellsForGUI(BigDecimal.valueOf(41.8206),
        BigDecimal.valueOf(-71.4003), BigDecimal.valueOf(41.82), BigDecimal.valueOf(-71.4));
    List<Way> traversables = nonTraversableDb
        .getWaysInBox(41.8206, -71.4003, 41.82, -71.4);
    assertTrue(response.getFirst().size() > traversables.size());
    assertEquals(response.getFirst().size(), 7);
    assertEquals(response.getSecond().size(), 1);
    System.out.println("RESPONSE " + response.getSecond());
    assertTrue(response.getSecond().containsKey("41.8206"));
    assertEquals(response.getSecond().get("41.8206").size(), 1);

    response = smallMapsDb.getWaysAndCellsForGUI(BigDecimal.valueOf(41.8206),
        BigDecimal.valueOf(-71.4003), BigDecimal.valueOf(41.82), BigDecimal.valueOf(-71.4));
    traversables = smallMapsDb.getWaysInBox(41.8206, -71.4003, 41.82, -71.4);
    assertEquals(response.getFirst().size(), traversables.size());
    assertEquals(response.getSecond().size(), 1);
    assertTrue(response.getSecond().containsKey("41.8206"));
    assertEquals(response.getSecond().get("41.8206").size(), 1);

    tearDown();
  }
}
