package edu.brown.cs.jwu175zcheng12.maps;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.*;

/**
 * A class which generates Model Based tests of both A* and Dijkstra Tests
 */
public class ModelBasedTest {
  /**
   * Uses Model Based Testing to test if dijkstra returns a same length path as A star!
   */
  @Test
  public void modelBasedTesting() {
    System.out.println("Lots of Small Graph Tests");
    assertTrue(dijkstraAStarModelBasedTesting(25, 15, -1000.0, 1000.0, false));
    assertTrue(dijkstraAStarModelBasedTesting(25, 15, -1000.0, 1000.0, true));
    System.out.println("A Couple of Medium Sized Graphs");
    assertTrue(dijkstraAStarModelBasedTesting(10, 50, -1000.0, 1000.0, false));
    assertTrue(dijkstraAStarModelBasedTesting(10, 50, -1000.0, 1000.0, true));
    System.out.println("One Particularly Chunky Boi");
    assertTrue(dijkstraAStarModelBasedTesting(1, 100, -1000.0, 1000.0, false));
    assertTrue(dijkstraAStarModelBasedTesting(1, 100, -1000.0, 1000.0, true));
  }


  // -------------------------- Random Generation Helper Functions -------------------------

  /**
   * Generates [Size] random Nodes collected into a list with a random latitude and longitude
   * each in the given lower bound and upper bound range.
   *
   * @param size       - The number or Random Nodes to be collected
   * @param lowerBound - The minimum value that latitude and longitude can be
   * @param upperBound - The maximum value that latitude and longitude can be
   * @return a List of random Nodes with their positions in the range of lower - upper bound
   */
  public List<Node> generateRandomNodes(int size, double lowerBound, double upperBound) {
    List<Node> randomNodes = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      String nodeId = "/n/" + i;
      double latitude = ThreadLocalRandom.current().nextDouble(lowerBound, upperBound);
      double longitude = ThreadLocalRandom.current().nextDouble(lowerBound, upperBound);
      randomNodes.add(new Node(nodeId, latitude, longitude));
    }
    return randomNodes;
  }

  /**
   * Generates [Size] random Ways collected into a list with a random latitude and longitude
   * each in the given lower bound and upper bound range, Sets the appropriate waysOut for
   * each Node.
   *
   * @param nodes- The list of possible Nodes to create a directed Way with
   * @param allWays - Overrides random way selection and creates a way between all nodes
   */
  public void initializeRandomWays(List<Node> nodes, Boolean allWays) {
    int size = nodes.size();
    for (int n1idx = 0; n1idx < size; n1idx++) {
      for (int n2idx = 0; n2idx < size; n2idx++) {
        Set<Way> waysOut = new HashSet<>();
        Node start = nodes.get(n1idx);
        if (n1idx != n2idx && (allWays || ThreadLocalRandom.current().nextBoolean())) {
          String wayId = "/w/" + n1idx + "." + n2idx;
          Node end = nodes.get(n2idx);
          String name = "Way: " + n1idx + " -> " + n2idx;
          String type = "Pathing";
          waysOut.add(new Way(wayId, start, end, name, type));
        }
        start.setWaysOut(waysOut);
      }
    }
  }

  // -------------------------- Model Based Testing Helper Functions -------------------------

  /**
   * Checks to see if the first Path of Ways has the same "Travel Distance" as the second path
   *
   * @param path1 - The First Path of Ways
   * @param path2 - The Second Path of Ways
   * @return a boolean declaring whether or not the first path has the same total distance as
   * the second path
   */
  public Boolean pathDistanceIsValid(List<Way> path1, List<Way> path2) {
    double TOLERANCE = 0.0000000000000001;
    if (path1 == null && path2 == null) {
      return true;
    } else if (path1 == null || path2 == null) {
      return false;
    } else {
      double path1Dist = path1.stream()
        .map(way -> way.euclideanWayLength())
        .reduce(0.0, (w1, w2) -> w1 + w2);
      double path2Dist = path2.stream()
        .map(way -> way.euclideanWayLength())
        .reduce(0.0, (w1, w2) -> w1 + w2);
      return Math.abs(path1Dist - path2Dist) < TOLERANCE;
    }
  }

  /**
   * Implements Model Based Testing comparing the results of A* Search with the results of
   * Dijkstra Search by checking if the distance of the paths between A* and Dijkstra is
   * the same.
   * @param numTests - The number of random tests to run
   * @param nodeSize - The number of Nodes in the graph
   * @param lowerBound - The minimum value that latitude and longitude can be
   * @param upperBound - The maximum value that latitude and longitude can be
   * @param allWays - Overrides random way selection and creates a way between all nodes
   */
  public Boolean dijkstraAStarModelBasedTesting(int numTests, int nodeSize, double lowerBound,
                                                double upperBound, Boolean allWays) {
    for (int testNum = 1; testNum < numTests + 1; testNum++) {
      List<Node> nodes = generateRandomNodes(nodeSize, lowerBound, upperBound);
      initializeRandomWays(nodes, allWays);
      for (Node n1 : nodes) {
        for (Node n2 : nodes) {
          List<Way> dijkstraPath = n1.dijkstraPath(n2);
          List<Way> aStarPath = n1.aStarPath(n2);
          if (!pathDistanceIsValid(dijkstraPath, aStarPath)) {
            throw new Error("Mismatched Path: " + dijkstraPath + ", " + aStarPath);
          }
          System.out.print(
            "Processing Test #" + testNum
              + ", Path: (" + n1.getNodeId()
              + " -> " + n2.getNodeId() + ")\r");
        }
      }
    }
    return true;
  }
}
