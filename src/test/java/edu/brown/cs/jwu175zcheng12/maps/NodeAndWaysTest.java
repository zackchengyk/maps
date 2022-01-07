package edu.brown.cs.jwu175zcheng12.maps;

import edu.brown.cs.jwu175zcheng12.stars.Star;
import junit.framework.AssertionFailedError;
import org.junit.Test;

import java.sql.SQLException;
import java.util.*;

import static edu.brown.cs.jwu175zcheng12.repl.GeneralREPL.printError;
import static org.junit.Assert.*;

/**
 * A class which tests the Node and Ways classes.
 */
public class NodeAndWaysTest {

  // -------------------------------- Basic --------------------------------

  /**
   * Tests various basic methods
   */
  @Test
  public void testBasicMethods() {

    // Set up
    Node n0 = new Node("0", -60., 0.);
    Node n1 = new Node("maps_2_sus", 60., 180.);
    Node n2 = new Node("/n/2_lol_what", -60., 180.);
    Way way01 = new Way("Way 01", n0, n1, "Way 01's name", "Way 01's type");

    // Tests exception-throwing for bad construction
    assertThrows(IllegalArgumentException.class, () ->
        new Node(null, -60., 180.));
    assertThrows(IllegalArgumentException.class, () ->
        new Node("null coordinates", null, 180.));
    assertThrows(IllegalArgumentException.class, () ->
        new Node("null coordinates", -60., null));
    assertThrows(IllegalArgumentException.class, () ->
        new Way(null, n0, n1, "name", "type"));
    assertThrows(IllegalArgumentException.class, () ->
        new Way("null start", null, n1, "name", "type"));
    assertThrows(IllegalArgumentException.class, () ->
        new Way("null end", n0, null, "name", "type"));
    assertThrows(IllegalArgumentException.class, () ->
        new Way("null name", n0, n1, null, "type"));
    assertThrows(IllegalArgumentException.class, () ->
        new Way("null type", n0, n1, "name", null));
    assertThrows(IllegalArgumentException.class, () ->
        new Way("empty string type", n0, n1, "name", ""));
    assertThrows(IllegalArgumentException.class, () ->
        new Way("unclassified type", n0, n1, "name", "unclassified"));

    // Tests Node.getNodeId
    assertEquals("0", n0.getNodeId());
    assertEquals("maps_2_sus", n1.getNodeId());
    assertEquals("/n/2_lol_what", n2.getNodeId());

    // Tests Node.toString
    assertEquals("Node{nodeId=\"" + n0.getNodeId() + "\"}", n0.toString());
    assertEquals("Node{nodeId=\"" + n1.getNodeId() + "\"}", n1.toString());
    assertEquals("Node{nodeId=\"" + n2.getNodeId() + "\"}", n2.toString());

    // Tests Node.equals
    assertFalse(n0.equals(null));
    Star star = new Star(1, "Star One", 1., 2., 3.);
    assertFalse(n0.equals(star));

    // Tests Way.getNodeId
    assertEquals("Way 01", way01.getWayId());

    // Tests Way.toString
    assertEquals("Way{"
        + "wayId=\"Way 01\""
        + ", start=" + n0.toString()
        + ", end=" + n1.toString()
        + ", name=\"Way 01's name\""
        + ", type=\"Way 01's type\"}", way01.toString());

    // Tests Way.equals
    assertFalse(way01.equals(null));
    assertFalse(way01.equals(star));
    Way way01Fake1 = new Way("different Id", n0, n1, "Way 01's name", "Way 01's type");
    Way way01Fake2 = new Way("Way 01", n2, n1, "Way 01's name", "Way 01's type");
    Way way01Fake3 = new Way("Way 01", n0, n2, "Way 01's name", "Way 01's type");
    Way way01Fake4 = new Way("Way 01", n0, n1, "different name", "Way 01's type");
    Way way01Fake5 = new Way("Way 01", n0, n1, "Way 01's name", "different type");
    assertNotEquals(way01, way01Fake1);
    assertNotEquals(way01, way01Fake2);
    assertNotEquals(way01, way01Fake3);
    assertNotEquals(way01, way01Fake4);
    assertNotEquals(way01, way01Fake5);
  }

  /**
   * Tests the getters and setters for Node's waysOut
   */
  @Test
  public void testGetAndSetWaysOut() {

    // Set up
    Node n0 = new Node("0", -60., 0.);
    Node n1 = new Node("1", 60., 180.);
    Node n2 = new Node("2", -60., 180.);
    Way way01 = new Way("0-1", n0, n1, "0-1 ln", "type");
    Way way02 = new Way("0-2", n0, n2, "0-2 ln", "type");
    Way way12 = new Way("1-2", n1, n2, "1-2 ln", "type");

    // Tests exception throwing if attempting to getWaysOut before calling
    // setWaysOut or specifying any MapDatabase
    assertThrows(UnsupportedOperationException.class, n0::getWaysOut);

    // Tests exception throwing if attempting to put a bad Way into waysOut
    assertThrows(IllegalArgumentException.class, () -> {
      n1.setWaysOut(new HashSet<>(Arrays.asList(way02, way12)));
    });

    // Tests that when exception is thrown during setWaysOut, none are put in
    assertThrows(UnsupportedOperationException.class, n1::getWaysOut);

    // Standard setting
    n0.setWaysOut(Collections.emptyList());
    n1.setWaysOut(Collections.singletonList(way12));
    n2.setWaysOut(Collections.emptyList());
    assertSameContents(Collections.emptyList(), n0.getWaysOut());
    assertSameContents(Collections.singletonList(way12), n1.getWaysOut());
    assertSameContents(Collections.emptyList(), n2.getWaysOut());

    // Tests setWaysOut overriding
    n0.setWaysOut(Arrays.asList(way01, way02));
    assertSameContents(Arrays.asList(way02, way01), n0.getWaysOut());
  }

  // ------------------------ Database Interactions ------------------------

  /**
   * Tests Nodes' and Ways' interactions with a MapDatabase
   */
  @Test
  public void testWithDatabase() {

    // Do tests in a try block
    try {
      // Set up
      MapDatabase workingMapDatabase = new MapDatabase();
      workingMapDatabase.setUpMapDatabase("data/maps/smallMaps.sqlite3");

      // Tests exception-throwing for bad construction
      assertThrows(IllegalArgumentException.class, () ->
          new Node(null, -60., 180., workingMapDatabase));
      assertThrows(IllegalArgumentException.class, () ->
          new Node("bad coordinates", null, 180., workingMapDatabase));
      assertThrows(IllegalArgumentException.class, () ->
          new Node("bad coordinates", -60., null, workingMapDatabase));
      assertThrows(IllegalArgumentException.class, () ->
          new Node("bad coordinates", -60., 180., null));

      // Get various Nodes and Ways, maximizing test coverage (lol)
      Node n0 = workingMapDatabase.getNodeById("/n/0");
      Way chihiroAve = workingMapDatabase.getWayByName("Chihiro Ave");
      Node n2 = workingMapDatabase.getIntersectionNode("Chihiro Ave", "Kamaji Pl");
      Node n3 = workingMapDatabase.getNearestNode(41.82, -71.4003);

      // Call getWaysInABox around Node n0
      List<Way> radishAndChihiro = workingMapDatabase.getWaysInBox(41.82001,
          -71.4001, 41.81999, -71.3999);

      // Ensure that getWaysInABox understands to remove the same Chihiro Ave
      assertEquals(2, radishAndChihiro.size());
      assertTrue(radishAndChihiro.remove(chihiroAve));
      assertEquals(1, radishAndChihiro.size());

      // Ensure that getWaysInABox returned the correct two results
      Way radishBlvd = radishAndChihiro.get(0);
      assertEquals("Radish Spirit Blvd", radishBlvd.getName());

      // Ensure Ways are EQUAL and SAME, unless completely made up (NOT same)
      Way radishBlvdById = workingMapDatabase.getWayById("/w/2");
      Way radishBlvdByName = workingMapDatabase.getWayByName("Radish Spirit Blvd");
      Way radishBlvdMadeUp = new Way("/w/2", n0, n3, "Radish Spirit Blvd", "residential");
      assertEquals(radishBlvd, radishBlvdById);
      assertEquals(radishBlvd, radishBlvdByName);
      assertEquals(radishBlvd, radishBlvdMadeUp);
      assertSame(radishBlvd, radishBlvdById);
      assertSame(radishBlvd, radishBlvdByName);
      assertNotSame(radishBlvd, radishBlvdMadeUp);

      // Test getWaysOut querying the database
      Collection<Way> n0_ways = n0.getWaysOut();
      assertSameContents(Arrays.asList(chihiroAve, radishBlvd), n0_ways);

    } catch (UnsupportedOperationException | ClassNotFoundException | SQLException error) {
      fail();
    }
  }

  // ----------------------------- Pathfinding -----------------------------

  /**
   * Tests the dijkstraPath and aStarPath method with various edge cases:
   * <p>
   *      0         (single Node)
   * <p>
   *      0  1      (two disconnected Nodes)
   * <p>
   *      0 -> 1    (two one-way-connected Nodes)
   * <p>
   *      0(0')     (two disconnected Nodes occupying the same location)
   */
  @Test
  public void testPathfindingEdgeCases() {

    // Set up all of the relevant Nodes and Ways
    Node n0 = new Node("0", -60., 0.);
    Node n0Prime = new Node("0'", -60., 0.);
    Node n1 = new Node("1", 60., 180.);
    Way way00Prime = new Way("0-0'", n0, n0Prime, "Around the World", "NotBlank");
    Way way01 = new Way("0-1", n0, n1, "Around the World", "NotUnTraversable");

    // Set up [Single Node]
    n0.setWaysOut(Collections.emptyList());

    // Get results [Single Node]
    List<Way> n0_to_n0 = n0.dijkstraPath(n0);
    List<Way> n0_to_n0_A_star = n0.aStarPath(n0);
    List<Way> n0_to_nowhere = n0.dijkstraPath(n1);
    List<Way> n0_to_nowhere_A_star = n0.aStarPath(n1);

    // Set up [Multiple Nodes no Ways]
    n1.setWaysOut(Collections.emptyList());

    // Get results [Multiple Nodes no Ways]
    List<Way> n0_to_nowhere_n1 = n0.dijkstraPath(n1);
    List<Way> n0_to_nowhere_n1_A_star = n0.aStarPath(n1);

    // Set up again [Single Directed Edge]
    n0.setWaysOut(Arrays.asList(way01, way01));
    n1.setWaysOut(Collections.emptyList());

    // Get results again [Single Directed Edge]
    List<Way> n0_to_n0_again = n0.dijkstraPath(n0);
    List<Way> n0_to_n0_again_A_star = n0.aStarPath(n0);
    List<Way> n0_to_n1 = n0.dijkstraPath(n1);
    List<Way> n0_to_n1_A_star = n0.aStarPath(n1);

    // Set up again [Nodes share position]
    n0.setWaysOut(Arrays.asList(way00Prime));
    n1.setWaysOut(Collections.emptyList());
    n0Prime.setWaysOut(Collections.emptyList());

    // Get results again [Nodes share position]
    List<Way> n0_to_n0Prime = n0.dijkstraPath(n0Prime);

    // Set up again [Directed edges are one-way]
    n0.setWaysOut(Arrays.asList(way01));
    n1.setWaysOut(Collections.emptyList());

    // Get results again [Directed edges are one-way]
    List<Way> n1_to_nowhere_n0 = n1.dijkstraPath(n0);

    // Tests correct paths returned
    assertEquals(Collections.emptyList(), n0_to_n0);
    assertEquals(Collections.emptyList(), n0_to_n0_A_star);
    assertNull(n0_to_nowhere);
    assertNull(n0_to_nowhere_A_star);
    assertNull(n0_to_nowhere_n1);
    assertNull(n0_to_nowhere_n1_A_star);
    assertEquals(Collections.emptyList(), n0_to_n0_again);
    assertEquals(Collections.emptyList(), n0_to_n0_again_A_star);
    assertEquals(Collections.singletonList(way01), n0_to_n1);
    assertEquals(Collections.singletonList(way01), n0_to_n1_A_star);
    assertEquals(Arrays.asList(way00Prime), n0_to_n0Prime);
    assertNull(n1_to_nowhere_n0);

    // Included Ways Out must be Traversable and Start with right node
    IllegalArgumentException badStart = assertThrows(
        IllegalArgumentException.class,
        () -> n1.setWaysOut(Arrays.asList(way01)));
    assertTrue(badStart.getMessage().contains("Cannot include Way"));

    IllegalArgumentException badTraversable1 = assertThrows(
        IllegalArgumentException.class,
        () -> n0.setWaysOut(Collections.singletonList(
            new Way("id", n0, n1, "hi", ""))));
    assertTrue(badTraversable1.getMessage().contains(
        "The Way's type cannot be the empty string or \"unclassified\"."));

    IllegalArgumentException badTraversable2 = assertThrows(
        IllegalArgumentException.class,
        () -> n0.setWaysOut(Collections.singletonList(new Way("id", n0, n1, "hi", "unclassified"))));
    assertTrue(badTraversable2.getMessage().contains(
        "The Way's type cannot be the empty string or \"unclassified\"."));
  }

  /**
   * Tests the dijkstraPath and aStarPath method with a linear graph:
   * <p>
   * "weird" <- 0 -> 1 -> 2 -> 3 -> 4
   */
  @Test
  public void testPathfindingLinear() {

    // Set up
    Node nWeird = new Node("weird", -0.51, 5.34584);
    Node n0 = new Node("0", 0., 0.);
    Node n1 = new Node("1", 0., 1.);
    Node n2 = new Node("2", 0., 2.);
    Node n3 = new Node("3", 0., 3.);
    Node n4 = new Node("4", 0., 4.);
    Way way01 = new Way("0-1", n0, n1, "0-1 st", "type");
    Way way12 = new Way("1-2", n1, n2, "1-2 st", "type");
    Way way23 = new Way("2-3", n2, n3, "2-3 st", "type");
    Way way34 = new Way("3-4", n3, n4, "3-4 st", "type");
    Way way0weird = new Way("0-weird", n0, nWeird, "0-weird st", "type");
    nWeird.setWaysOut(Collections.emptyList());
    n0.setWaysOut(Arrays.asList(way0weird, way01));
    n1.setWaysOut(Collections.singletonList(way12));
    n2.setWaysOut(Collections.singletonList(way23));
    n3.setWaysOut(Collections.singletonList(way34));
    n4.setWaysOut(Collections.emptyList());

    // Get results
    List<Way> n0_to_nWeird = n0.dijkstraPath(nWeird);
    List<Way> n0_to_n4 = n0.dijkstraPath(n4);
    List<Way> n1_to_n4 = n1.dijkstraPath(n4);
    List<Way> n2_to_n4 = n2.dijkstraPath(n4);
    List<Way> n3_to_n4 = n3.dijkstraPath(n4);
    List<Way> n4_to_n3 = n4.dijkstraPath(n3);
    List<Way> n1_to_nWeird = n1.dijkstraPath(nWeird);
    List<Way> n0_to_nWeird_A_star = n0.aStarPath(nWeird);
    List<Way> n0_to_n4_A_star = n0.aStarPath(n4);
    List<Way> n1_to_n4_A_star = n1.aStarPath(n4);
    List<Way> n2_to_n4_A_star = n2.aStarPath(n4);
    List<Way> n3_to_n4_A_star = n3.aStarPath(n4);
    List<Way> n4_to_n3_A_star = n4.aStarPath(n3);
    List<Way> n1_to_nWeird_A_star = n1.aStarPath(nWeird);

    // Tests correct paths returned
    assertEquals(Collections.singletonList(way0weird), n0_to_nWeird);
    assertEquals(Arrays.asList(way01, way12, way23, way34), n0_to_n4);
    assertEquals(Arrays.asList(way12, way23, way34), n1_to_n4);
    assertEquals(Arrays.asList(way23, way34), n2_to_n4);
    assertEquals(Collections.singletonList(way34), n3_to_n4);
    assertEquals(Collections.singletonList(way0weird), n0_to_nWeird_A_star);
    assertEquals(Arrays.asList(way01, way12, way23, way34), n0_to_n4_A_star);
    assertEquals(Arrays.asList(way12, way23, way34), n1_to_n4_A_star);
    assertEquals(Arrays.asList(way23, way34), n2_to_n4_A_star);
    assertEquals(Collections.singletonList(way34), n3_to_n4_A_star);

    // Tests no paths returned
    assertNull(n4_to_n3);
    assertNull(n1_to_nWeird);
    assertNull(n4_to_n3_A_star);
    assertNull(n1_to_nWeird_A_star);
  }

  /**
   * Tests the dijkstraPath and aStarPath method with a simple graph:
   * <p>
   *      D ----- > E
   *      |        ^
   *      |       /
   *      |     C
   *      |   ^ ^
   *      | /   |
   *      A --> B
   **/
  @Test
  public void testPathfindingSimple() {

    // Set up
    Node nA = new Node("A", 0.0, 0.0);
    Node nB = new Node("B", 0.0, 0.1);
    Node nC = new Node("C", 0.1, 0.1);
    Node nD = new Node("D", 0.2, 0.0);
    Node nE = new Node("E", 0.2, 0.2);
    Way wayAB = new Way("A-B", nA, nB, "A-B st", "type");
    Way wayBC = new Way("B-C", nB, nC, "B-C st", "type");
    Way wayAC = new Way("A-C", nA, nC, "A-C st", "type");
    Way wayAD = new Way("A-D", nA, nD, "A-D st", "type");
    Way wayCE = new Way("C-E", nC, nE, "C-E st", "type");
    Way wayDE = new Way("D-E", nD, nE, "D-E st", "type");
    nA.setWaysOut(Arrays.asList(wayAB, wayAC, wayAD));
    nB.setWaysOut(Collections.singletonList(wayBC));
    nC.setWaysOut(Collections.singletonList(wayCE));
    nD.setWaysOut(Collections.singletonList(wayDE));
    nE.setWaysOut(Collections.emptyList());

    // Get results
    List<Way> nA_to_nA = nA.dijkstraPath(nA);
    List<Way> nA_to_nE = nA.dijkstraPath(nE);
    List<Way> nE_to_nA = nE.dijkstraPath(nA);
    List<Way> nA_to_nA_A_star = nA.aStarPath(nA);
    List<Way> nA_to_nE_A_star = nA.aStarPath(nE);
    List<Way> nE_to_nA_A_star = nE.aStarPath(nA);

    // Tests correct paths returned
    assertEquals(Collections.emptyList(), nA_to_nA);
    assertEquals(Arrays.asList(wayAC, wayCE), nA_to_nE);
    assertEquals(Collections.emptyList(), nA_to_nA_A_star);
    assertEquals(Arrays.asList(wayAC, wayCE), nA_to_nE_A_star);

    // Tests no paths returned
    assertNull(nE_to_nA);
    assertNull(nE_to_nA_A_star);
  }

  /**
   * Tests that the dijkstraPath and aStarPath method doesn't run infinitely on loops
   * Both when there is and isn't an answer
   * <p> HAS NO ANSWER
   *      A ---> B
   *      ^     /
   *      |    /
   *      |   /
   *      |  /
   *      | /
   *      |v
   *      C     D
   * <p> HAS AN ANSWER
   *      A ---> B
   *      ^     /
   *      |    /
   *      |   /
   *      |  /
   *      | /
   *      |v
   *      C --> D
   **/
  @Test
  public void testPathfindingLoop() {

    // Set up [No Answer Case]
    Node nA = new Node("A", 0.0, 1.0);
    Node nB = new Node("B", 1.0, 1.0);
    Node nC = new Node("C", 0.0, 0.0);
    Node nD = new Node("D", 1.0, 0.0);
    Way wayAB = new Way("A-B", nA, nB, "A-B st", "type");
    Way wayBC = new Way("B-C", nB, nC, "B-C st", "type");
    Way wayCA = new Way("C-A", nC, nA, "C-A st", "type");
    Way wayCD = new Way("C-D", nC, nD, "C-D st", "type");
    nA.setWaysOut(Collections.singletonList(wayAB));
    nB.setWaysOut(Collections.singletonList(wayBC));
    nC.setWaysOut(Collections.singletonList(wayCA));
    nD.setWaysOut(Collections.emptyList());

    // Get results [No Answer Case]
    List<Way> nA_to_nD_null = nA.dijkstraPath(nD);
    List<Way> nB_to_nD_null = nB.dijkstraPath(nD);
    List<Way> nC_to_nD_null = nC.dijkstraPath(nD);
    List<Way> nA_to_nD_AStar_null = nA.aStarPath(nD);
    List<Way> nB_to_nD_AStar_null = nB.aStarPath(nD);
    List<Way> nC_to_nD_AStar_null = nC.aStarPath(nD);

    // Set Up Again [Has Answer Case]
    nC.setWaysOut(Arrays.asList(wayCA, wayCD));

    // Get Results [Has Answer Case]
    List<Way> nA_to_nD = nA.dijkstraPath(nD);
    List<Way> nB_to_nD = nB.dijkstraPath(nD);
    List<Way> nC_to_nD = nC.dijkstraPath(nD);
    List<Way> nA_to_nD_AStar = nA.aStarPath(nD);
    List<Way> nB_to_nD_AStar = nB.aStarPath(nD);
    List<Way> nC_to_nD_AStar = nC.aStarPath(nD);

    // Tests correct paths returned
    assertNull(nA_to_nD_null);
    assertNull(nB_to_nD_null);
    assertNull(nC_to_nD_null);
    assertNull(nA_to_nD_AStar_null);
    assertNull(nB_to_nD_AStar_null);
    assertNull(nC_to_nD_AStar_null);
    assertEquals(nA_to_nD, Arrays.asList(wayAB, wayBC, wayCD));
    assertEquals(nB_to_nD, Arrays.asList(wayBC, wayCD));
    assertEquals(nC_to_nD, Arrays.asList(wayCD));
    assertEquals(nA_to_nD_AStar, Arrays.asList(wayAB, wayBC, wayCD));
    assertEquals(nB_to_nD_AStar, Arrays.asList(wayBC, wayCD));
    assertEquals(nC_to_nD_AStar, Arrays.asList(wayCD));
  }

  /**
   * Tests the dijkstraPath and aStarPath method with multiple Shortest Paths :
   * <p>
   * <p>
   *      B ---> D
   *      ^      ^
   *      |      |
   *      A ---> C
   **/
  @Test
  public void testPathfindingMultiple() {

    // Set up [Multiple Short Paths]
    Node nA = new Node("A", 0.0, 0.0);
    Node nB = new Node("B", 0.0, 3.0);
    Node nC = new Node("C", 3.0, 0.0);
    Node nD = new Node("D", 3.0, 3.0);
    Way wayAB = new Way("A-B", nA, nB, "A-B st", "type");
    Way wayBD = new Way("B-D", nB, nD, "B-D st", "type");
    Way wayCD = new Way("C-D", nC, nD, "C-D st", "type");
    Way wayAC = new Way("A-C", nA, nC, "A-C st", "type");
    nA.setWaysOut(Arrays.asList(wayAB, wayAC));
    nB.setWaysOut(Collections.singletonList(wayBD));
    nC.setWaysOut(Collections.singletonList(wayCD));
    nD.setWaysOut(Collections.emptyList());

    // Get results [No Answer Case]
    List<Way> nA_to_nD = nA.dijkstraPath(nD);
    List<Way> nA_to_nD_AStar = nA.aStarPath(nD);
    List<List<Way>> possibilities = Arrays.asList(Arrays.asList(wayAB, wayBD), Arrays.asList(wayAC, wayCD));
    assertTrue(possibilities.contains(nA_to_nD));
    assertTrue(possibilities.contains(nA_to_nD_AStar));
  }

  /**
   * A helper function which just does an assert for content equality, but
   * not for order.
   *
   * @param expectedAns a Collection of Ways which is to be checked
   * @param actualAns   a Collection of Ways which is to be checked
   */
  private void assertSameContents(Collection<Way> expectedAns,
                                  Collection<Way> actualAns) {
    assertTrue(expectedAns.size() == actualAns.size()
        && expectedAns.containsAll(actualAns)
        && actualAns.containsAll(expectedAns));
  }
}
