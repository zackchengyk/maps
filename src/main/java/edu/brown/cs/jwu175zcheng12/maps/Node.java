package edu.brown.cs.jwu175zcheng12.maps;

import edu.brown.cs.jwu175zcheng12.graph.GeneralNode;
import edu.brown.cs.jwu175zcheng12.repl.GeneralPair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;

/**
 * A class which represents a traversable Node on a map.
 */
public class Node extends GeneralNode<Node, Way> {

  private Collection<Way> waysOut = null;
  private MapDatabase workingMapDatabase = null;

  // ----------------------------- Constructors ----------------------------

  /**
   * The constructor for this class. Every field is a final immutable object
   * or primitive, so no copying is necessary. mapsDatabase is set to null by
   * default when using this constructor, which means that getWaysOut will
   * fail if setWaysOut is not used!
   *
   * @param nodeId    a String, the Id of this Node
   * @param latitude  a double, the latitude of the Node's position
   * @param longitude a double, the longitude of the Node's position
   */
  public Node(String nodeId, Double latitude, Double longitude) {
    super(new Double[]{latitude, longitude}, nodeId);
  }

  /**
   * Another constructor for this class. This constructor offers the ability to
   * set the mapsDatabase, which is used for getWaysOut (unless setWaysOut is
   * used instead).
   * <p>
   * Note that the mapsDatabase is totally mutable, as is intended---
   * we want mapsDatabase to refer to the same mapsDatabase that the other Nodes
   * are using.
   *
   * @param nodeId      a String, the Id of this Node
   * @param latitude    a double, the latitude of the Node's position
   * @param longitude   a double, the longitude of the Node's position
   * @param workingMapDatabase a MapDatabase, a pointer to the database which this Node
   *                    came from and which contains this Node's connected Ways
   */
  public Node(String nodeId, Double latitude, Double longitude,
              MapDatabase workingMapDatabase) {
    this(nodeId, latitude, longitude);
    if (workingMapDatabase == null) {
      throw new IllegalArgumentException(
          "When using this Node constructor, workingMapDatabase cannot be null.");
    }
    this.workingMapDatabase = workingMapDatabase;
  }

  // ------------------------- Setter (For Testing) ------------------------

  /**
   * A setter function for this class' waysOut field.
   *
   * @param waysOut a Collection of Ways to use as this Node's waysOut field.
   */
  public void setWaysOut(Collection<Way> waysOut) {
    for (Way wayOut : waysOut) {
      if (wayOut.getStart() != this) {
        throw new IllegalArgumentException("Cannot include Way (id = \""
            + wayOut.getWayId() + "\") in this Node's (id = \""
            + super.getNodeId() + "\") waysOut, as it starts from Node (id = \""
            + wayOut.getStart().getNodeId() + "\") instead.");
      }
    }
    this.waysOut = waysOut;
  }

  // ------------------ Implementation of Abstract Methods -----------------

  @Override
  public Collection<Way> getEdgesOut() {
    return this.getWaysOut();
  }

  @Override
  public double distanceTo(Node other) {
    return this.haversineDistance(other.getCoordinates());
  }

  /**
   * A helper function to get this class' waysOut field. If not already
   * loaded (i.e. waysOut == null), this function does a database query to fill
   * in that field before getting and returning it.
   *
   * @return a Collection of Ways which leads out of this Node.
   */
  public Collection<Way> getWaysOut() {
    // If not already loaded, load in from database
    if (this.waysOut == null) {
      if (workingMapDatabase == null) {
        throw new UnsupportedOperationException(
            "Node was constructed without specifying workingMapDatabase. "
                + "Cannot getWaysOut before calling setWaysOut/setWayDatabase.");
      }
      // Set waysOut to the result of a database query
      this.waysOut = this.workingMapDatabase
          .getWaysInOrOutOfNode(super.getNodeId(), true);
    }
    // Return
    return this.waysOut;
  }

  // ----------------------------- Pathfinding -----------------------------

  /**
   * A function which uses Dijkstra's algorithm to find the optimal path,
   * represented as an ordered list of Ways, from this Node to the target Node.
   *
   * @param targetNode the target Node
   * @return a List of Ways which represents the optimal path from this Node to
   * the target Node. The List will be empty if the targetNode is this Node.
   * Null will be returned if the targetNode cannot be reached from this Node.
   */
  public List<Way> dijkstraPath(Node targetNode) {
    return aStarHelper(targetNode, 0.);
  }

  /**
   * A function which uses an A* algorithm to find the optimal path,
   * represented as an ordered list of Ways, from this Node to the target Node.
   *
   * @param targetNode the target Node
   * @return a List of Ways which represents the optimal path from this Node to
   * the target Node. The List will be empty if the targetNode is this Node.
   * Null will be returned if the targetNode cannot be reached from this Node.
   */
  public List<Way> aStarPath(Node targetNode) {
    return aStarHelper(targetNode, 1.);
  }

  /**
   * A helper function which uses an A* algorithm to find the optimal path,
   * represented as an ordered list of Ways, from this Node to the target Node.
   * The heuristic is haversine distance to the target Node, and the input
   * heuristicWeight determines how much weight to give to this heuristic.
   *
   * @param targetNode      the target Node
   * @param heuristicWeight a double, by which to scale the distance heuristic
   * @return a List of Ways which represents the optimal path from this Node to
   * the target Node. The List will be empty if the targetNode is this Node.
   * Null will be returned if the targetNode cannot be reached from this Node.
   */
  private List<Way> aStarHelper(Node targetNode, double heuristicWeight) {

    // Handle base case
    if (this.equals(targetNode)) {
      return new ArrayList<>(0);
    }

    // Create hashmap of the best distance a Node had when it was processed
    Map<Node, Double> processed = new HashMap<>();
    // Create hashmap of the last Way which is on the shortest path to a Node
    Map<Node, GeneralPair<Way, Double>> lastWayAndDistToNode = new HashMap<>();
    // Create priority queue of (Node, distance)
    PriorityQueue<GeneralPair<Node, Double>> minHeap = new PriorityQueue<>(
        Comparator.comparingDouble(GeneralPair::getSecond));

    // Add starting Node to hashMap and minHeap
    lastWayAndDistToNode.put(this, new GeneralPair<>(null, 0.));
    minHeap.add(new GeneralPair<>(this, 0.));
    // Iterate over minHeap
    while (minHeap.peek() != null) {
      // Take off the minHeap
      GeneralPair<Node, Double> currPair = minHeap.remove();
      // Get Node, but distance must come from lastWayAndDistToNode due to A*
      Node currNode = currPair.getFirst();
      Double currDist = lastWayAndDistToNode.get(currNode).getSecond();
      // Process Node only if it hasn't been processed yet, or if the distance
      // to it now is lower than it was back then
      if (!processed.containsKey(currNode)) {
        // Iterate over the currentNode's ways
        Collection<Way> nextWays = currNode.getWaysOut();
        for (Way nextWay : nextWays) {
          // Get Node at other end, and distance
          Node nextNode = nextWay.getEnd();
          Double nextDist = currDist
              + currNode.haversineDistance(nextNode.getCoordinates());
          // Add to hashMap and minHeap if not already processed better-ly
          if (!lastWayAndDistToNode.containsKey(nextNode)
              || lastWayAndDistToNode.get(nextNode).getSecond() > nextDist) {
            // Add to hashMap (used later for retrieval and actual path dist)
            lastWayAndDistToNode.put(nextNode,
                new GeneralPair<>(nextWay, nextDist));
            // Add to minHeap (used for order of search, so heuristic incl.)
            double heuristic = 0;
            if (heuristicWeight == 0) {
              heuristic = heuristicWeight
                  * nextNode.haversineDistance(targetNode.getCoordinates());
            }
            minHeap.add(new GeneralPair<>(nextNode, nextDist + heuristic));
          }
        }
      }
      // Keep this conditional just in case (don't overwrite a better value!)
      if (processed.containsKey(currNode) && processed.get(currNode) < currDist) {
        continue;
      }
      // Consider this node processed
      processed.put(currNode, currDist);
    }

    // Return null if no path exists
    if (!lastWayAndDistToNode.containsKey(targetNode)) {
      return null;
    }

    // Get the optimal path
    List<Way> results = new ArrayList<>();
    GeneralPair<Way, Double> lastWayAndDist = lastWayAndDistToNode.get(targetNode);
    while (lastWayAndDist != null && lastWayAndDist.getFirst() != null) {
      results.add(lastWayAndDist.getFirst());
      lastWayAndDist = lastWayAndDistToNode.get(lastWayAndDist.getFirst().getStart());
    }
    Collections.reverse(results);
    return results;
  }

  // ---------------------------- Miscellaneous ----------------------------

  @Override
  public String toString() {
    return "Node{nodeId=\"" + super.getNodeId() + "\"}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Node node = (Node) o;
    return super.getNodeId().equals(node.getNodeId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.getNodeId());
  }
}
