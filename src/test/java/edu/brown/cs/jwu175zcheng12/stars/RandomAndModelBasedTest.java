package edu.brown.cs.jwu175zcheng12.stars;

import edu.brown.cs.jwu175zcheng12.csvdataset.StarDataset;

import edu.brown.cs.jwu175zcheng12.kdtree.KDTree;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * A class which tests the KDTree class, as well as the StarDataset's naive and
 * non-naive methods.
 */
public class RandomAndModelBasedTest {

  private static final double EPS = 0.001;
  private static final Double MAX = 10000000000000.;
  private static final Double MIN = -10000000000000.;
  // Generates ( Math.pow(2, LAYERS_IN_TREE) - 1 ) stars, which corresponds to
  // a tree of LAYERS_IN_TREE layers to run tests on
  private static final int LAYERS_IN_TREE = 15;
  // Given ( Math.pow(2, LAYERS_IN_TREE) - 1 ) stars, run NUM_MODEL_TESTS
  // random comparisons between the naive and non-naive approaches
  private static final int NUM_MODEL_TESTS = 20;

  private static final Random R = new Random();

  /**
   * Tests the correctness of a randomly-generated KDTree built with a StarList of
   * a random size, with coordinates of 3 dimensions bounded between lowerBound and
   * upperBound for both radiusSearch and kNearestNeighbors using Model Based Testing
   * based on distance testing.
   */
  @Test
  public void distanceRandomKDTreeTest() {
    int randomsize = new Random().nextInt(25);
    assertTrue(KNNModelBasedTester(randomsize, 3, -100, 100, 100));
    assertTrue(radiusMBTester(randomsize, 3, -100, 100, 100));
  }

  /**
   * Tests the correctness of a randomly-generated KDTree with n layers
   * and checks the output KDTree randomly. Also performs model testing against
   * the naive approach.
   */
  @Test
  public void bigRandomKDTreeTest() {

    // Create a list, map, and tree with n layers, of stars
    int n = LAYERS_IN_TREE;
    int starListSize = (int) Math.pow(2, n) - 1;
    Map<String, Star> starHashMap = new HashMap<>(starListSize);
    List<Star> starList = generateStarListAndFillMap(starListSize,
        starHashMap);
    KDTree<Star> tree = new KDTree<>(3, starList);
    System.out.println("Generated a list of Stars of size = " + starListSize
        + " which was made into to a KDTree with layers [0, " + (n - 1) + "].");

    // Prepare K-D tree invariant test ("left is less, right is more");
    int numNodesToTestFrom = (int) Math.pow(2, n - 2);
    System.out.println(numNodesToTestFrom + " randomly selected nodes in this "
        + "KDTree will be tested for correctness.");
    int maxTestsAtEachNode = (int) Math.pow(2, n) - 2;
    System.out.println("Each will be compared to " + maxTestsAtEachNode
        + " or however many descendant nodes it has (whichever is fewer) "
        + "randomly selected descendant nodes.");

    // Conduct K-D tree invariant test
    for (int i = 0; i < numNodesToTestFrom; i++) {
      System.out.print("Testing nodes... (" + i + " / "
          + numNodesToTestFrom + ")\r");
      // Get a random node to test from
      String pre = generateLRSequence(n - 1, "");
      // Decide the number of random tests to conduct at that node
      int numDescendants = (int) Math.pow(2, n - pre.length()) - 2;
      int numTests = Math.min(maxTestsAtEachNode, numDescendants);
      // Conduct the tests at that node
      testNode(numTests, tree, n, pre);
    }
    System.out.println("Completed " + numNodesToTestFrom + " KDTree node tests."
        + "                                ");

    // Prepare model tests by creating StarDataset from starList, starHashmap,
    // and tree
    StarDataset starDataset = new StarDataset();
    starDataset.setStarListHashAndTree(starList, starHashMap, tree);

    // Conduct model tests
    int numModelTestsToRun = NUM_MODEL_TESTS;
    System.out.println(numModelTestsToRun + " model tests will be run, testing "
        + "naiveNeighbor() against neighbor(), and naiveRadius() against "
        + "radius().");
    for (int j = 0; j < numModelTestsToRun; j++) {
      System.out.print("Running model tests... (" + j + " / "
          + numModelTestsToRun + ")\r");
      // Randomly select input radius and number of neighbors, with a 4%-ish
      // chance that k or r will be more than the value needed to return ALL the
      // available stars, to test more comprehensively
      double r = (MAX - MIN) * 1.05 * R.nextDouble();
      int k = R.nextInt((int) Math.round(starListSize * 1.05));
      // Get results
      if (R.nextInt(2) == 1) {
        // Randomly select a Star in the dataset
        Star refStar = starDataset.getStarByName("Star "
            + R.nextInt(starListSize));
        // Find Stars in radius
        List<Star> result1 = starDataset.radius(r, refStar);
        List<Star> result2 = starDataset.naiveRadius(r, refStar);
        // Find neighbors
        List<Star> result3 = starDataset.neighbors(k, refStar);
        List<Star> result4 = starDataset.naiveNeighbors(k, refStar);
        // Check results of radius
        compareRadiusResults(result1, result2, r, refStar,
            null, null, null, starDataset, starListSize);
        // Check results of neighbors (but remember that k might be more than
        // the total number of stars)
        compareNeighborsResults(result3, result4, k, refStar,
            null, null, null, starDataset, starListSize);
      } else {
        // Randomly select coordinates, mostly within the area with stars
        double x = MIN + (MAX - MIN) * 1.5 * R.nextDouble();
        double y = MIN + (MAX - MIN) * 1.5 * R.nextDouble();
        double z = MIN + (MAX - MIN) * 1.5 * R.nextDouble();
        // Find Stars in radius
        List<Star> result1 = starDataset.radius(r, x, y, z);
        List<Star> result2 = starDataset.naiveRadius(r, x, y, z);
        // Find neighbors
        List<Star> result3 = starDataset.neighbors(k, x, y, z);
        List<Star> result4 = starDataset.naiveNeighbors(k, x, y, z);
        // Check results of radius
        compareRadiusResults(result1, result2, r, null, x, y, z,
            starDataset, starListSize);
        // Check results of neighbors (but remember that k might be more than
        // the total number of stars)
        compareNeighborsResults(result3, result4, k, null, x, y, z,
            starDataset, starListSize);
      }
    }
    System.out.println("Completed " + numModelTestsToRun + " model tests."
        + "                                ");
  }

  // -------------------------- Helper Functions ---------------------------

  private List<Star> generateStarListAndFillMap(int starListSize,
                                                Map<String, Star> map) {
    List<Star> starList = new ArrayList<>(starListSize);
    for (int i = 0; i < starListSize; i++) {
      System.out.print("Generating random stars... (" + i + " / "
          + starListSize + ")\r");
      // Generate a random star and add it to the list
      double x = MIN + (MAX - MIN) * R.nextDouble();
      double y = MIN + (MAX - MIN) * R.nextDouble();
      double z = MIN + (MAX - MIN) * R.nextDouble();
      Star star = new Star(i, "Star " + i, x, y, z);
      starList.add(star);
      map.put("Star " + i, star);
    }
    System.out.println("Generated " + starListSize + " stars. Now beginning "
        + "KDTree formation.                                                ");
    return starList;
  }

  private String generateLRSequence(int maxLength, String pre) {
    StringBuilder sb = new StringBuilder(maxLength);
    sb.append(pre);
    int maxAdditionalLength = maxLength - pre.length();
    int additionalLength = R.nextInt(maxAdditionalLength + 1);
    for (int k = 0; k < additionalLength; k++) {
      if (R.nextInt(2) == 1) {
        sb.append("R");
      } else {
        sb.append("L");
      }
    }
    return sb.toString();
  }

  private void testNode(int numTests, KDTree<Star> tree,
                        int n, String pre) {
    Star rootStar = tree.getTree().getLR(pre);
    for (int j = 0; j < numTests; j++) {
      String path = generateLRSequence(n - 1, pre);
      Star testStar = null;
      try {
        testStar = tree.getTree().getLR(path);
      } catch (NoSuchElementException error) {
        System.err.println("Pre = " + pre);
        System.err.println("Path = " + path);
        System.err.println("Tree = ");
        System.err.println(tree.toString());
      }
      if (rootStar == testStar) {
        return;
      } else if (path.charAt(pre.length()) == 'R') {
        assertNotNull(testStar);
        assertTrue(
            Double.compare(
                testStar.getNthCoordinate(pre.length() % 3),
                rootStar.getNthCoordinate(pre.length() % 3)) >= 0);
      } else {
        assertNotNull(testStar);
        assertTrue(
            Double.compare(
                testStar.getNthCoordinate(pre.length() % 3),
                rootStar.getNthCoordinate(pre.length() % 3)) < 0);
      }
    }
  }

  private void compareRadiusResults(List<Star> result1, List<Star> result2,
                                    Double r, Star refStar,
                                    Double x, Double y, Double z,
                                    StarDataset starDataset,
                                    int starListSize) {
    int usingRefStar = refStar != null ? 1 : 0;
    assert usingRefStar != 0 || (x != null) && (y != null) && (z != null);
    // Check same size
    assertEquals(result1.size(), result2.size());
    // Iterate over results
    double previousDist = 0;
    for (int l = 0; l < result1.size(); l++) {
      // Confirm that both results are identical
      assertEquals(result1.get(l), result2.get(l));
      // Confirm that stars exist in the dataset
      Assert.assertEquals(result1.get(l),
          starDataset.getStarByName(result1.get(l).getProperName()));
      // Confirm that stars are further away than the last one
      if (usingRefStar == 1) {
        assertTrue(result1.get(l).getDistanceFrom(refStar) >= previousDist);
        previousDist = result1.get(l).getDistanceFrom(refStar);
      } else {
        assertTrue(result1.get(l).getDistanceFrom(x, y, z) >= previousDist);
        previousDist = result1.get(l).getDistanceFrom(x, y, z);
      }
      // Confirm that stars are closer than r
      assertTrue(previousDist < r);
    }
  }

  private void compareNeighborsResults(List<Star> result3, List<Star> result4,
                                       int k, Star refStar,
                                       Double x, Double y, Double z,
                                       StarDataset starDataset,
                                       int starListSize) {
    int usingRefStar = refStar != null ? 1 : 0;
    assert usingRefStar != 0 || (x != null) && (y != null) && (z != null);
    // Check same and correct size
    assertEquals(Math.min(k, starListSize - usingRefStar), result3.size());
    assertEquals(result3.size(), result4.size());
    // Iterate over results
    double previousDist = 0;
    boolean randomTailHasBegun = false;
    for (int l = 0; l < result3.size(); l++) {
      // Confirm that stars exist in the dataset
      Assert.assertEquals(result3.get(l),
          starDataset.getStarByName(result3.get(l).getProperName()));
      Assert.assertEquals(result4.get(l),
          starDataset.getStarByName(result4.get(l).getProperName()));
      // Confirm that stars are further away than the last one
      if (usingRefStar == 1) {
        assertTrue(result3.get(l).getDistanceFrom(refStar) >= previousDist);
        previousDist = result3.get(l).getDistanceFrom(refStar);
      } else {
        assertTrue(result3.get(l).getDistanceFrom(x, y, z) >= previousDist);
        previousDist = result3.get(l).getDistanceFrom(x, y, z);
      }
      // If by now we're at the random tail, just compare distance
      if (randomTailHasBegun || result3.get(l) != result4.get(l)) {
        randomTailHasBegun = true;
        if (usingRefStar == 1) {
          assertEquals(result3.get(l).getDistanceFrom(refStar),
              result4.get(l).getDistanceFrom(refStar), EPS);
        } else {
          assertEquals(result3.get(l).getDistanceFrom(x, y, z),
              result4.get(l).getDistanceFrom(x, y, z), EPS);
        }
      }
      // Else, they are certain to be the same due to that condition above
    }
  }

  // Generates Random Coordinates of a certain dimension
  public Double[] generateRandomCoordinates(int dim, double minBound, double maxBound) {
    Double[] randomCoords = new Double[dim];
    double randomMultiplier = new Random().nextDouble();

    for (int j = 0; j < dim; j++) {
      randomCoords[j] = randomMultiplier * (maxBound - minBound) + minBound;
    }
    return randomCoords;
  }

  // Generates List of stars at random coordinates,name and id is the index to ensure uniqueness
  public List<Star> generateRandomStarList(int size, int dim, double minBound, double maxBound) {
    List<Star> randomInputs = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      Double[] randomCoords = generateRandomCoordinates(dim, minBound, maxBound);
      Star randomStar = new Star(i, Integer.toString(i), randomCoords[0], randomCoords[1], randomCoords[2]);
      randomInputs.add(randomStar);
    }
    return randomInputs;
  }

  // kNearestNeighbor Distance Based Model testing
  public Boolean KNNModelBasedTester(int size, int dim, double minBound, double maxBound, int numTests) {
    for (int i = 0; i < numTests; i++) {

      // Generate Random Inputs
      List<Star> randomStarList = generateRandomStarList(size, dim, minBound, maxBound);
      Double[] randomTarget = generateRandomCoordinates(dim, minBound, maxBound);
      Map<String, Star> starHashMap = new HashMap<>(size);
      for (Star star : randomStarList) {
        starHashMap.put(star.getProperName(), star);
      }
      KDTree<Star> NeighborKDTree = new KDTree<>(dim, randomStarList);

      //Fill StarDataset with the random stars, and KDTree
      StarDataset starDataset = new StarDataset();
      starDataset.setStarListHashAndTree(randomStarList, starHashMap, NeighborKDTree);

      // Run naiveNeighbors and KDTree Neighbors for comparison
      int numNeighbors = new Random().nextInt(2 * size + 1);
      List<Star> naiveNeighbors =
          starDataset.naiveNeighbors(numNeighbors, randomTarget[0], randomTarget[1], randomTarget[2]);
      List<Star> kdNeighbors =
          starDataset.neighbors(numNeighbors, randomTarget[0], randomTarget[1], randomTarget[2]);

      // Check all distances of the neighbors are the same
      boolean allDistancesSame = true;
      for (int j = 0; j < naiveNeighbors.size(); j++) {
        double naiveDistance = naiveNeighbors.get(j).squaredEuclideanDistance(randomTarget);
        double kdDistance = kdNeighbors.get(j).euclideanDistance(randomTarget);
        allDistancesSame = allDistancesSame || naiveDistance == kdDistance;
      }

      // If Any fail print out the names and distances, otherwise print that it passed
      if (naiveNeighbors.size() == kdNeighbors.size() && allDistancesSame) {
        System.out.print("Test #" + i + " Passed :)\r");
      } else {
        String naiveNeighborDesc = "";
        String kdNeighborDesc = "";
        for (Star naiveNeighbor : naiveNeighbors) {
          naiveNeighborDesc += "Name: "
              + naiveNeighbor.getProperName()
              + ", Distance: "
              + naiveNeighbor.euclideanDistance(randomTarget)
              + " ";
        }
        for (Star kdNeighbor : kdNeighbors) {
          kdNeighborDesc += "Name: "
              + kdNeighbor.getProperName()
              + ", Distance: "
              + kdNeighbor.euclideanDistance(randomTarget)
              + " ";
        }
        throw new Error("ERROR: Mismatched results. Naive Neighbors: " + naiveNeighborDesc + ", KDNeighbors: " + kdNeighborDesc);
      }
    }
    return true;
  }

  // Check to see all are included without checking for specific Orderings in case of tie
  public Boolean radiusMBTester(int size, int dim, double minBound, double maxBound, int numTests) {
    for (int i = 0; i < numTests; i++) {
      // Generate Random Inputs
      List<Star> randomStarList = generateRandomStarList(size, dim, minBound, maxBound);
      Double[] randomTarget = generateRandomCoordinates(dim, minBound, maxBound);
      Map<String, Star> starHashMap = new HashMap<>(size);
      for (Star star : randomStarList) {
        starHashMap.put(star.getProperName(), star);
      }
      KDTree<Star> radiusKDTree = new KDTree<>(dim, randomStarList);

      //Fill StarDataset with the random stars, and KDTree
      StarDataset starDataset = new StarDataset();
      starDataset.setStarListHashAndTree(randomStarList, starHashMap, radiusKDTree);

      // Run naiveRadius and KDTree Radius Search for comparison
      double radiusSize = new Random().nextDouble() * Math.abs(maxBound - minBound);
      List<Star> naiveRadius =
          starDataset.naiveRadius(radiusSize, randomTarget[0], randomTarget[1], randomTarget[2]);
      List<Star> kdRadius =
          starDataset.radius(radiusSize, randomTarget[0], randomTarget[1], randomTarget[2]);

      // If Any fail print out the names and distances, otherwise print that it passed
      if (naiveRadius.containsAll(kdRadius) && naiveRadius.size() == kdRadius.size()) {
        System.out.print("Test #" + i + " Passed :)\r");
      } else {
        String naiveRadiusDesc = "";
        String kdRadiusDesc = "";
        for (Star naiveRadi : naiveRadius) {
          naiveRadiusDesc += "Name: "
              + naiveRadi.getProperName()
              + ", Distance: "
              + naiveRadi.euclideanDistance(randomTarget)
              + " ";
        }
        for (Star kdNeighbor : kdRadius) {
          kdRadiusDesc += "Name: "
              + kdNeighbor.getProperName()
              + ", Distance: "
              + kdNeighbor.euclideanDistance(randomTarget)
              + " ";
        }
        throw new Error("ERROR: Mismatched results. Naive Neighbors: " + naiveRadiusDesc + ", KDNeighbors: " + kdRadiusDesc);
      }
    }
    return true;
  }
}
