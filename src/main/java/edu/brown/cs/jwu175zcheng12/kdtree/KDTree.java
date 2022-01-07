package edu.brown.cs.jwu175zcheng12.kdtree;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.Deque;
import java.util.ArrayDeque;


/**
 * Implements the KDTree DataStructure for an ObjectInNDSpace.
 *
 * @param <C> the type of the data which will be stored at each node
 */
public class KDTree<C extends ObjectInNDSpace> {

  private KDTreeNode<C> tree;
  private final int maxDim;
  private static final int DEFAULT_PQ_SIZE = 11;
  private static final double TOLERANCE = 0.000000000000000000000000000000001;

  /**
   * Creates an empty KDTree with the given dimensions if possible.
   * @param dim
   *      The requested dimensions of the empty KDTree
   */
  public KDTree(int dim) {
    if (dim <= 0) {
      throw new Error("ERROR: KDTrees can only have positive dimensions, Input Dim: " + dim);
    }
    tree = new KDTreeNode<>();
    maxDim = dim;
  }

  /**
   * Creates an filled in KDTree [From the List] with the given dimensions if possible.
   * @param dim
   *      The requested dimensions of the empty KDTree
   * @param coordBasedList
   *      The list that we use to fill in the KDTree
   */
  public KDTree(int dim, List<C> coordBasedList) {
    if (dim <= 0) {
      throw new Error(
          "ERROR: KDTrees can only have positive dimensions, Input Dim: " + dim);
    }
    maxDim = dim;
    tree = buildKDTree(coordBasedList);
  }

  /**.
   * Getter function for the Tree itself
   * @return the KDTree itself as a KDTreeNode
   */
  public KDTreeNode<C> getTree() {
    return tree;
  }

  /**.
   * Getter function for the Dimensions of the KDTree
   * @return the dimensions of the KDTree
   */
  public int getMaxDim() {
    return maxDim;
  }

  /**.
   * String Representation of the KDTrees
   * @return the string representation of the KDTree
   */
  @Override
  public String toString() {
    return "KDTree of "
      + maxDim
      + " dimensions, {Tree: "
      + tree
      + "}";
  }

  /**.
   * Forces the Dimensions of the Coordinate Based Data to match the Tree
   * @param coordBasedList
   *      The list containing all the Coordinate Based Data
   */
  public void enforceDimensions(List<C> coordBasedList) {
    // Make sure that the dimensions of the coordBasedData matches the dimensions of the data
    for (C coord : coordBasedList) {
      if (coord.getCoordinates().length != maxDim) {
        throw new Error("ERROR: Dimension Mismatch, Expected coordinates to have "
          + maxDim
          + " dimensions. Offending Coordinate Based Data: "
          + coord);
      }
    }
  }

  /**.
   * Builds the KDTree by running the Recursive Builder with initial conditions set up
   * @param coordBasedList
   *      The list containing all the Coordinate Based Data
   * @return a KDTreeNode containing the entire KDTree from the List
   */
  public KDTreeNode<C> buildKDTree(List<C> coordBasedList) {
    enforceDimensions(coordBasedList);
    tree = recurBuildKDTree(coordBasedList, 0);
    return tree;
  }

  /**.
   * Finds the K Nearest Neighbors but as a List
   * @param k
   *      The number of Neighbors you want to find
   * @param target
   *      The localized position whose neighbors you want to find
   * @return a List containing the Nearest Neighbors of the target
   */
  public List<C> findKNearestNeighbors(int k, Double[] target) {
    if (k < 0) {
      throw new Error("ERROR: Cannot find a negative number of Neighbors");
    }
    if (k == 0) {
      return new ArrayList<>(0);
    }
    PriorityQueue<C> nearestNeighbors = priorityNearestNeighbors(k, target);
    List<C> nearestNeighborsList = new ArrayList<>();
    List<C> runningNeighborTiesList = new ArrayList<>();
    double runningNeighborTiesDistance = 0;

    while (!nearestNeighbors.isEmpty()) {
      C nextNeighbor = nearestNeighbors.poll();
      if (Math.abs(nextNeighbor.squaredEuclideanDistance(target) - runningNeighborTiesDistance)
          > TOLERANCE) {
        if (nearestNeighborsList.size() + runningNeighborTiesList.size() < k) {
          nearestNeighborsList.addAll(runningNeighborTiesList);
          runningNeighborTiesDistance = nextNeighbor.squaredEuclideanDistance(target);
          runningNeighborTiesList = new ArrayList<>();
        } else {
          break;
        }
      }
      runningNeighborTiesList.add(nextNeighbor);
    }

    Collections.shuffle(runningNeighborTiesList);
    nearestNeighborsList.addAll(runningNeighborTiesList);
    return nearestNeighborsList.subList(0, Math.min(k, nearestNeighborsList.size()));
  }

  /**.
   * Finds the Neighbors within the Radius but as a List
   * @param r
   *      The radius where you want to look for Neighbors
   * @param target
   *      The localized position whose neighbors you want to find
   * @return a List containing the Neighbors within the radius of the target
   */
  public List<C> findRadiusSearch(Double r, Double[] target) {
    if (r < 0) {
      throw new Error("ERROR: Radius must be non negative");
    }
    PriorityQueue<C> withinRadius = priorityRadiusSearch(r, target);
    List<C> withinRadiusList = new ArrayList<>();
    while (!withinRadius.isEmpty()) {
      withinRadiusList.add(withinRadius.poll());
    }
    return withinRadiusList;
  }

  /**.
   * Recursively builds the KDTree from a list of Coordinate Based Data
   * @param coordBasedList
   *      The list containing all the Coordinate Based Data
   * @param currDim
   *      Temporary Variable telling the function what axis it's currently on
   * @return a KDTreeNode containing the entire KDTree
   */
  private KDTreeNode<C> recurBuildKDTree(List<C> coordBasedList, int currDim) {
    if (coordBasedList.isEmpty()) {
      return new KDTreeNode<>();
    }

    //currDim is locally final and should not change before the sort
    final int currDimLocal = currDim;

    // Sorts the coordBasedList with the currDim as priority
    coordBasedList.sort((C o1, C o2) ->
        Double.compare(o1.getCoordinates()[currDimLocal], o2.getCoordinates()[currDimLocal]));

    // Breaks the coordBasedList into the middle value on the axis and 2 coordBasedLists.
    int cblSize = coordBasedList.size();
    C currCBData = coordBasedList.get(cblSize / 2);

    // Partition the rest of the CBDataList into left (< currCBData) or right (>= currCBData)
    List<C> leftCBDatas = coordBasedList.subList(0, cblSize / 2);
    List<C> rightCBDatas = coordBasedList.subList(cblSize / 2 + 1, cblSize);

    // Update Axis you're looking at and recursively build the KDTree
    currDim = (currDim + 1) % maxDim; // 0 = X, 1 = Y, ... maxDim - 1 = Max Axis
    KDTreeNode<C> leftKDTreeNode = recurBuildKDTree(leftCBDatas, currDim);
    KDTreeNode<C> rightKDTreeNode = recurBuildKDTree(rightCBDatas, currDim);
    return new KDTreeNode<>(currCBData, leftKDTreeNode, rightKDTreeNode);
  }

  /**.
   * Iteratively Finds the K Nearest Neighbors of the target in the KDTree
   * @param k
   *      The number of Neighbors you want to find
   * @param target
   *      The localized position whose neighbors you want to find
   * @return a Priority Queue containing the Nearest Neighbors of the target
   */
  private PriorityQueue<C> priorityNearestNeighbors(int k, Double[] target) {

    // Enforces that target has the right number of dimensions
    if (target.length != maxDim) {
      throw new Error("ERROR: Mismatched Dimension, input coordinates have "
      + target.length
      + " dimensions, expected "
      + maxDim
      + " Dimensions!");
    }

    Comparator<C> sqDistanceComparator = new Comparator<>() {
      @Override
      public int compare(C o1, C o2) {
        return Double.compare(o1.squaredEuclideanDistance(target),
            o2.squaredEuclideanDistance(target));
      }
    };

    PriorityQueue<C> nearestNeighbors = new PriorityQueue<>(DEFAULT_PQ_SIZE, sqDistanceComparator);
    Deque<KDTreeNode<C>> searchSpace = new ArrayDeque<>();
    searchSpace.add(tree); // Adds to the end of the deque

    int currDim = 0;
    while (!searchSpace.isEmpty()) {
      KDTreeNode<C> currKDTreeNode = searchSpace.pop(); // Pops from the front of the deque
      if (currKDTreeNode.isEmpty()) {
        continue;
      }
      nearestNeighbors.add(currKDTreeNode.getNodeVal());

      KDTreeNode<C> leftKDTreeNode = currKDTreeNode.getLeftNode();
      KDTreeNode<C> rightKDTreeNode = currKDTreeNode.getRightNode();
      KDTreeNode<C> minKDTreeNode;

      if ((leftKDTreeNode == null || leftKDTreeNode.isEmpty())
          && (rightKDTreeNode == null || rightKDTreeNode.isEmpty())) {
        continue;
      } else if (leftKDTreeNode == null || leftKDTreeNode.isEmpty()) {
        minKDTreeNode = rightKDTreeNode;
      } else if (rightKDTreeNode == null || rightKDTreeNode.isEmpty()) {
        minKDTreeNode = leftKDTreeNode;
      } else {
        double leftCutDist =
            Math.abs(leftKDTreeNode.getNodeVal().getCoordinates()[currDim]
                - target[currDim]);
        double rightCutDist =
            Math.abs(rightKDTreeNode.getNodeVal().getCoordinates()[currDim]
                - target[currDim]);
        if (leftCutDist < rightCutDist) {
          minKDTreeNode = leftKDTreeNode;
        } else {
          minKDTreeNode = rightKDTreeNode;
        }
      }

      // Distance to the cut line
      double cutDist = Math.abs(minKDTreeNode.getNodeVal()
          .getCoordinates()[currDim] - target[currDim]);
      double minDist = minKDTreeNode.getNodeVal().squaredEuclideanDistance(target);

      if (nearestNeighbors.size() < k - 1 || Math.pow(cutDist, 2) <= minDist) {
        searchSpace.add(leftKDTreeNode);
        searchSpace.add(rightKDTreeNode);
      } else {
        searchSpace.add(minKDTreeNode);
      }
    }

    return nearestNeighbors;
  }

  /**
   * Helper function which finds neighbors within a given radius r.
   * @param r
   *      The radius where you want to look for Neighbors
   * @param target
   *      The localized position whose neighbors you want to find
   * @return a Priority Queue containing the Neighbors within the radius of
   * the target
   */
  private PriorityQueue<C> priorityRadiusSearch(Double r, Double[] target) {

    // Enforces that target has the right number of dimensions
    if (target.length != maxDim) {
      throw new Error("ERROR: Mismatched Dimension, input coordinates have "
        + target.length
        + " dimensions, expected "
        + maxDim
        + " Dimensions!");
    }

    Comparator<C> sqDistanceComparator = new Comparator<>() {
      @Override
      public int compare(C o1, C o2) {
        return Double.compare(o1.squaredEuclideanDistance(target),
            o2.squaredEuclideanDistance(target));
      }
    };

    PriorityQueue<C> withinRadius = new PriorityQueue<>(DEFAULT_PQ_SIZE, sqDistanceComparator);
    Deque<KDTreeNode<C>> searchSpace = new ArrayDeque<>();
    searchSpace.add(tree); // Adds to the end of the deque

    int currDim = 0;
    while (!searchSpace.isEmpty()) {
      KDTreeNode<C> currKDTreeNode = searchSpace.pop(); // Pops from the front of the deque
      if (currKDTreeNode.isEmpty()) {
        continue;
      }

      if (currKDTreeNode.getNodeVal().squaredEuclideanDistance(target) <= Math.pow(r, 2)) {
        withinRadius.add(currKDTreeNode.getNodeVal());
        searchSpace.add(currKDTreeNode.getLeftNode());
        searchSpace.add(currKDTreeNode.getRightNode());
      } else {
        double cutDist =
            Math.abs(currKDTreeNode.getNodeVal().getCoordinates()[currDim] - target[currDim]);
        if (Math.pow(cutDist, 2) <= currKDTreeNode.getNodeVal().squaredEuclideanDistance(target)) {
          searchSpace.add(currKDTreeNode.getLeftNode());
          searchSpace.add(currKDTreeNode.getRightNode());
        } else if (target[currDim] < currKDTreeNode.getNodeVal().getCoordinates()[currDim]) {
          searchSpace.add(currKDTreeNode.getLeftNode());
        } else {
          searchSpace.add(currKDTreeNode.getRightNode());
        }
      }
    }

    return withinRadius;
  }
}
