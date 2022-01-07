package edu.brown.cs.jwu175zcheng12.csvdataset;

import edu.brown.cs.jwu175zcheng12.kdtree.KDTree;
import edu.brown.cs.jwu175zcheng12.repl.GeneralPair;
import edu.brown.cs.jwu175zcheng12.stars.Star;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static edu.brown.cs.jwu175zcheng12.repl.GeneralREPL.printError;

/**
 * A class which represents a data set of stars.
 */
public final class StarDataset extends GeneralCSVDataset<Star> {

  // super.allData is a list and so fulfills the requirement: "you may use any
  // class that implements the List interface to store your star data".
  private Map<String, Star> starNameMap;
  private Set<Integer> starIdSet;
  private KDTree<Star> starKDTree;
  private static final double HALF = 0.5;

  // ---------------------- Constructor and Overrides ----------------------

  /**
   * The constructor for this class. It has no input arguments.
   */
  public StarDataset() {
    super("stars",
        new String[]{"StarID", "ProperName", "X", "Y", "Z"});
  }

  @Override
  public void loadData(String filename, boolean checkFirstLineHeaders) {
    // Create name-to-Star map and starId set
    this.starNameMap = new HashMap<>();
    this.starIdSet = new HashSet<>();
    // Load data into Collection<Star> allData and starNameMap, checking for
    // ID uniqueness
    super.loadData(filename, checkFirstLineHeaders);
    if (super.getAllData() == null) {
      this.starNameMap = null;
      this.starKDTree = null;
      return;
    }
    // Generate KDTree
    this.starKDTree = new KDTree<>(3, new ArrayList<>(super.getAllData()));
  }

  @Override
  protected void handleOneCSVRow(String rowString, String[] rowStringArray,
                                 int counter) throws IOException {
    if (rowStringArray.length == 5) {
      int starId;
      double x;
      double y;
      double z;
      try {
        starId = Integer.parseInt(rowStringArray[0]);
        String properName = rowStringArray[1];
        x = Double.parseDouble(rowStringArray[2]);
        y = Double.parseDouble(rowStringArray[3]);
        z = Double.parseDouble(rowStringArray[4]);
        // Check if ID was already used
        if (starIdSet.contains(starId)) {
          printError(
              "Repeated star ID detected at row " + counter + ".");
          throw new IOException();
        } else {
          starIdSet.add(starId);
        }
        Star star = new Star(starId, properName, x, y, z);
        super.addToAllData(star);
        this.starNameMap.put(properName, star);
      } catch (NumberFormatException error) {
        printError("Bad number format at row " + counter + ".");
        printError("Row looks like this: '" + rowString + "'.");
        throw error;
      }
    } else {
      printError("Malformed CSV input at row " + counter + ".");
      printError("Row looks like this: '" + rowString + "'.");
      throw new IOException("Malformed CSV.");
    }
  }

  /**
   * A setter function purely for testing to set the list and tree to a given
   * value. Danger! Never use this outside of testing.
   *
   * @param list a list of Stars
   * @param map  a HashMap of Star names to Stars
   * @param tree a KDTree of Stars
   */
  public void setStarListHashAndTree(List<Star> list, Map<String, Star> map,
                                     KDTree<Star> tree) {
    assert list.size() == map.size();
    super.setAllData(list);
    this.starNameMap = map;
    this.starKDTree = tree;
  }

  // ------------------- Functions Which Clients Will Use ------------------

  /**
   * A function which searches the allData collection for a Star
   * which has properName equal to searchName.
   *
   * @param searchName the properName of the Star to be returned
   * @return the first (and assumed only) Star object in allData which has
   * properName equal to searchName, or null if no such Star is found
   */
  public Star getStarByName(String searchName) {
    if (this.starNameMap == null) {
      printError("No star data loaded.");
      return null;
    }
    if (this.starNameMap.containsKey(searchName)) {
      return this.starNameMap.get(searchName);
    }
    return null;
  }

  /**
   * A function which finds up to k nearest Stars for
   * a given reference Star, sorted from closest to farthest away.
   * <p>
   * This function uses a KDTree-based approach.
   *
   * @param k       a non-negative integer upper limit (inclusive) of
   *                nearest stars to find
   * @param refStar the reference Star which our search bubble is centered upon
   * @return a List of up to k Stars in descending order of proximity
   * to the reference Star
   */
  public List<Star> neighbors(int k, Star refStar) {
    return neighborsHelper(k, refStar.getX(), refStar.getY(), refStar.getZ(),
        refStar);
  }

  /**
   * A function which finds up to k nearest Stars for
   * a given reference position, sorted from closest to farthest away.
   * <p>
   * This function uses a KDTree-based approach.
   *
   * @param k a non-negative integer upper limit (inclusive) of
   *          nearest stars to find
   * @param x a double, the x-coordinate of the reference position
   * @param y a double, the y-coordinate of the reference position
   * @param z a double, the z-coordinate of the reference position
   * @return a List of up to k Stars in descending order of proximity
   * to our reference position
   */
  public List<Star> neighbors(int k, double x, double y, double z) {
    return neighborsHelper(k, x, y, z, null);
  }

  /**
   * A function which finds all the stars within a certain radius
   * from a given reference Star, sorted from closest to farthest away.
   * <p>
   * This function uses a KDTree-based approach.
   *
   * @param r       a non-negative double, the radius of the bubble to search
   * @param refStar the reference Star which our search bubble is centered upon
   * @return a List of Stars with distance to the reference Star
   * less than or equal to r, in descending order of proximity
   */
  public List<Star> radius(double r, Star refStar) {
    return radiusHelper(r, refStar.getX(), refStar.getY(),
        refStar.getZ(), refStar);
  }

  /**
   * A function which finds all the stars within a certain radius
   * from a given reference position, sorted from closest to farthest away.
   * <p>
   * This function uses a KDTree-based approach.
   *
   * @param r a non-negative double, the radius of the bubble to search
   * @param x a double, the x-coordinate of the reference position
   * @param y a double, the y-coordinate of the reference position
   * @param z a double, the z-coordinate of the reference position
   * @return a List of Stars with distance to our reference position
   * less than or equal to r, in descending order of proximity
   */
  public List<Star> radius(double r, double x, double y, double z) {
    return radiusHelper(r, x, y, z, null);
  }

  /**
   * A function which finds up to k nearest Stars for
   * a given reference Star, sorted from closest to farthest away.
   * <p>
   * This function uses a naive, list-based approach.
   *
   * @param k       a non-negative integer upper limit (inclusive) of
   *                nearest stars to find
   * @param refStar the reference Star which our search bubble is centered upon
   * @return a List of up to k Stars in descending order of proximity
   * to the reference Star
   */
  public List<Star> naiveNeighbors(int k, Star refStar) {
    return naiveNeighborsHelper(k, refStar.getX(), refStar.getY(),
        refStar.getZ(), refStar);
  }

  /**
   * A function which finds up to k nearest Stars for
   * a given reference position, sorted from closest to farthest away.
   * <p>
   * This function uses a naive, list-based approach.
   *
   * @param k a non-negative integer upper limit (inclusive) of
   *          nearest stars to find
   * @param x a double, the x-coordinate of the reference position
   * @param y a double, the y-coordinate of the reference position
   * @param z a double, the z-coordinate of the reference position
   * @return a List of up to k Stars in descending order of proximity
   * to our reference position
   */
  public List<Star> naiveNeighbors(int k, double x, double y, double z) {
    return naiveNeighborsHelper(k, x, y, z, null);
  }

  /**
   * A function which finds all the stars within a certain radius
   * from a given reference Star, sorted from closest to farthest away.
   * <p>
   * This function uses a naive, list-based approach.
   *
   * @param r       a non-negative double, the radius of the bubble to search
   * @param refStar the reference Star which our search bubble is centered upon
   * @return a List of Stars with distance to the reference Star
   * less than or equal to r, in descending order of proximity
   */
  public List<Star> naiveRadius(double r, Star refStar) {
    return naiveRadiusHelper(r, refStar.getX(), refStar.getY(),
        refStar.getZ(), refStar);
  }

  /**
   * A function which finds all the stars within a certain radius
   * from a given reference position, sorted from closest to farthest away.
   * <p>
   * This function uses a naive, list-based approach.
   *
   * @param r a non-negative double, the radius of the bubble to search
   * @param x a double, the x-coordinate of the reference position
   * @param y a double, the y-coordinate of the reference position
   * @param z a double, the z-coordinate of the reference position
   * @return a List of Stars with distance to our reference position
   * less than or equal to r, in descending order of proximity
   */
  public List<Star> naiveRadius(double r, double x, double y, double z) {
    return naiveRadiusHelper(r, x, y, z, null);
  }

  // -------------------------- Helper Functions ---------------------------

  /**
   * A HELPER function which finds up to k nearest Stars for
   * a given reference position / Star, sorted from closest to farthest away.
   * <p>
   * This function uses a KDTree-based approach.
   *
   * @param k       a non-negative integer upper limit (inclusive) of
   *                nearest stars to find
   * @param x       a double, the x-coordinate of the reference position
   * @param y       a double, the y-coordinate of the reference position
   * @param z       a double, the z-coordinate of the reference position
   * @param refStar the reference Star which our search bubble is centered upon,
   *                which may be null if we are searching by coordinates only
   * @return a List of up to k Stars in descending order of proximity
   * to our reference position / Star
   */
  private List<Star> neighborsHelper(int k, double x, double y, double z,
                                     Star refStar) {
    // Check that starKDTree is not null and contains refStar, if provided
    if (this.starKDTree == null) {
      printError("No star data loaded into KDTree.");
      return new ArrayList<>(0);
    } else if (refStar != null
        && !starNameMap.containsKey(refStar.getProperName())) {
      printError("Reference star \"" + refStar.getProperName()
          + "\" not found in current dataset.");
      return new ArrayList<>(0);
    }
    // Call the starKDTree's neighbors method
    List<Star> stars;
    if (refStar == null) {
      stars = starKDTree.findKNearestNeighbors(k, new Double[]{x, y, z});
    } else {
      stars = starKDTree.findKNearestNeighbors(k + 1, refStar.getCoordinates());
      if (!stars.removeIf(star -> star.equals(refStar))) {
        // Closest star is itself so this could only happen if all stars were tied
        stars.remove(new Random().nextInt(stars.size()));
      }
    }
    return stars;
  }

  /**
   * A HELPER function which finds all the stars within a certain radius
   * from a given reference position / Star, sorted from closest to farthest
   * away.
   * <p>
   * This function uses a naive, list-based approach.
   *
   * @param r       a non-negative double, the radius of the bubble to search
   * @param x       a double, the x-coordinate of the reference position
   * @param y       a double, the y-coordinate of the reference position
   * @param z       a double, the z-coordinate of the reference position
   * @param refStar the reference Star which our search bubble is centered upon,
   *                which may be null if we are searching by coordinates only
   * @return a List of Stars with distance to our reference position / Star
   * less than or equal to r, in descending order of proximity
   */
  private List<Star> radiusHelper(double r, double x, double y, double z,
                                  Star refStar) {
    // Check that starKDTree is not null and contains refStar, if provided
    if (this.starKDTree == null) {
      printError("No star data loaded into KDTree.");
      return new ArrayList<>(0);
    } else if (refStar != null
        && !starNameMap.containsKey(refStar.getProperName())) {
      printError("Reference star \"" + refStar.getProperName()
          + "\" not found in current dataset.");
      return new ArrayList<>(0);
    }
    // Call the starKDTree's radius method
    List<Star> stars;
    if (refStar == null) {
      stars = starKDTree.findRadiusSearch(r, new Double[]{x, y, z});
    } else {
      stars = starKDTree.findRadiusSearch(r, refStar.getCoordinates());
      stars.removeIf(star -> star.equals(refStar));
    }
    return stars;
  }

  /**
   * A HELPER function which finds up to k nearest Stars for
   * a given reference position / Star, sorted from closest to farthest away.
   * <p>
   * This function uses a naive, list-based approach.
   *
   * @param k       a non-negative integer upper limit (inclusive) of
   *                nearest stars to find
   * @param x       a double, the x-coordinate of the reference position
   * @param y       a double, the y-coordinate of the reference position
   * @param z       a double, the z-coordinate of the reference position
   * @param refStar the reference Star which our search bubble is centered upon,
   *                which may be null if we are searching by coordinates only
   * @return a List of up to k Stars in descending order of proximity
   * to our reference position / Star
   */
  private List<Star> naiveNeighborsHelper(int k, double x, double y, double z,
                                          Star refStar) {
    // Check that allData is not null and contains refStar, if provided
    if (super.getAllData() == null) {
      printError("No star data loaded.");
      return new ArrayList<>(0);
    } else if (refStar != null && !super.getAllData().contains(refStar)) {
      printError("Reference star \"" + refStar.getProperName()
          + "\" not found in current dataset.");
      return new ArrayList<>(0);
    }
    // Check that k is nonzero
    if (k == 0) {
      return new ArrayList<>(0);
    }
    // Set up heap of Star-Double pairs, heapSize, and furthestDistance
    PriorityQueue<GeneralPair<Star, Double>> maxHeap = new PriorityQueue<>(
        (a, b) -> Double.compare(b.getSecond(), a.getSecond()));
    int heapSize = 0;
    Double furthestDistance = Double.POSITIVE_INFINITY;
    // Iterate slowly over allData
    for (Star star : super.getAllData()) {
      // Check that star is not refStar, if provided
      if (refStar != null && star == refStar) {
        continue;
      }
      // Get star's distance from refStar, and worst distance too
      Double currentDistance = star.getDistanceFrom(x, y, z);
      if (maxHeap.peek() != null) {
        furthestDistance = maxHeap.peek().getSecond();
      }
      // Decide whether to put into heap
      boolean addIntoHeap = false;
      if (heapSize < k || currentDistance.equals(furthestDistance)) {
        if (heapSize < k || Math.random() > HALF) {
          addIntoHeap = true;
        }
      } else if (currentDistance < furthestDistance) {
        addIntoHeap = true;
      }
      // Add into heap, if applicable
      if (addIntoHeap) {
        if (heapSize == k) {
          maxHeap.remove();
          heapSize--;
        }
        maxHeap.add(new GeneralPair<>(star, currentDistance));
        heapSize++;
      }
    }
    // Create ordered results collection
    List<Star> results = new ArrayList<>(heapSize);
    while (maxHeap.peek() != null) {
      results.add(maxHeap.remove().getFirst());
    }
    Collections.reverse(results);
    return results;
  }

  /**
   * A HELPER function which finds all the stars within a certain radius
   * from a given reference position / Star, sorted from closest to farthest
   * away.
   * <p>
   * This function uses a naive, list-based approach.
   *
   * @param r       a non-negative double, the radius of the bubble to search
   * @param x       a double, the x-coordinate of the reference position
   * @param y       a double, the y-coordinate of the reference position
   * @param z       a double, the z-coordinate of the reference position
   * @param refStar the reference Star which our search bubble is centered upon,
   *                which may be null if we are searching by coordinates only
   * @return a List of Stars with distance to our reference position / Star
   * less than or equal to r, in descending order of proximity
   */
  private List<Star> naiveRadiusHelper(double r, double x, double y, double z,
                                       Star refStar) {
    // Check that allData is not null and contains refStar, if provided
    if (super.getAllData() == null) {
      printError("No star data loaded.");
      return new ArrayList<>(0);
    } else if (refStar != null && !super.getAllData().contains(refStar)) {
      printError("Reference star \"" + refStar.getProperName()
          + "\" not found in current dataset.");
      return new ArrayList<>(0);
    }
    // Set up ArrayList of GeneralPairs<Star, Double>
    List<GeneralPair<Star, Double>> resultPairs = new ArrayList<>();
    // Iterate slowly over allData
    for (Star star : super.getAllData()) {
      // Check that star is not refStar, if provided
      if (refStar != null && star == refStar) {
        continue;
      }
      // Get star's distance from refStar
      double currentDistance = star.getDistanceFrom(x, y, z);
      // Add to resultPairs if currentDistance <= radius r
      if (currentDistance <= r) {
        resultPairs.add(new GeneralPair<>(star, currentDistance));
      }
    }
    // Sort by reference distance
    resultPairs.sort(Comparator.comparingDouble(GeneralPair::getSecond));
    return resultPairs.stream().map(GeneralPair::getFirst)
        .collect(Collectors.toList());
  }
}
