package edu.brown.cs.jwu175zcheng12.graph;

import edu.brown.cs.jwu175zcheng12.kdtree.ObjectInNDSpace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * An abstract class which represents a GeneralNode in some unknown-dimensional
 * space.
 *
 * @param <N> the concrete extension of GeneralNode that this GeneralEdge uses
 * @param <E> the concrete extension of GeneralEdge that this GeneralEdge uses
 */
public abstract class GeneralNode<N extends GeneralNode<N, E>, E extends GeneralEdge<N, E>>
    extends ObjectInNDSpace {

  private final String nodeId;

  /**
   * The constructor for this class. Note that a copy is made of the
   * coordinates argument by the ObjectInNDSpace constructor.
   *
   * @param coordinates an Array of Doubles representing the coordinates
   *                    of this N's location in space
   * @param nodeId      a String, the Id of this GeneralNode
   */
  public GeneralNode(Double[] coordinates, String nodeId) {
    super(coordinates, null);
    if (nodeId == null) {
      throw new IllegalArgumentException(
          "The nodeId of a GeneralNode cannot be null.");
    }
    this.nodeId = nodeId;
  }

  /**
   * A getter function for this class' nodeId field.
   *
   * @return a String, the nodeId of this N
   */
  public String getNodeId() {
    return this.nodeId;
  }

  /**
   * An abstract function which gets the E's which lead out of this N.
   *
   * @return a Collection of E's which leads out of this N.
   */
  public abstract Collection<E> getEdgesOut();

  /**
   * An abstract function which computes the distance (Euclidean distance,
   * Haversine distance, etc) to another N. The exact distance calculation is
   * to be determined by the concrete extension of this class (i.e. N).
   *
   * @param other another N
   * @return the distance from this N to that N
   */
  public abstract double distanceTo(N other);

  /**
   * A function which uses an A* algorithm to find the optimal path,
   * represented as an ordered list of E's, from this N to the target N where
   * N extends GeneralNode.
   *
   * @param targetNode the target N (where N extends GeneralNode!)
   * @return a List of E's that make up the shortest path from start to end
   */
  public List<E> runDijkstraStar(N targetNode) {
    return aStarHelper(targetNode, 1.0);
  }

  /**
   * A helper function which uses an A* algorithm to find the optimal path,
   * represented as an ordered list of E, from this N to the target N. The
   * heuristic is the "distanceTo()" the target N, and the input heuristicWeight
   * determines how much weight to give to this heuristic.
   *
   * @param targetNode      the target N
   * @param heuristicWeight a double, by which to scale the distance heuristic
   * @return a List of E which represents the optimal path from this N to the
   * target N. The List will be empty if the targetNode is this N. Null will be
   * returned if the targetNode cannot be reached from this N.
   */
  private List<E> aStarHelper(N targetNode, double heuristicWeight) {

    // Check if the start node and the end node are the same
    if (this.equals(targetNode)) {
      return new ArrayList<E>();
    }

    Map<GeneralNode<N, E>, Double> gScore = new HashMap<>();
    Map<GeneralNode<N, E>, Double> fScore = new HashMap<>();

    /**
     * Comparator class in order to compare the node scores stored in the fScore
     * Map.
     */
    class NodeComparator implements Comparator<GeneralNode<N, E>> {
      public int compare(GeneralNode<N, E> a, GeneralNode<N, E> b) {
        return Double.compare(fScore.get(a), fScore.get(b));
      }
    }

    PriorityQueue<GeneralNode<N, E>> unvisited = new PriorityQueue<>(new NodeComparator());

    // Set the g score and the f score of the start node and re-add it to the priority queue
    gScore.put(this, (double) 0);
    fScore.put(this, this.distanceTo(targetNode));
    unvisited.add(this);

    HashSet<GeneralNode<N, E>> visited = new HashSet<>();

    Map<N, E> cameFrom = new HashMap<>();

    while (unvisited.peek() != null) {
      GeneralNode<N, E> current = unvisited.poll();

      if (!visited.contains(current)) {
        for (E edge : this.getEdgesOut()) {
          N next = edge.getEnd();
          double d = gScore.get(current) + edge.getWeight();
          if (!gScore.containsKey(next) || d <= gScore.get(next)) {
            cameFrom.put(next, edge);
            gScore.put(next, d);
            double heuristic = 0;
            if (heuristicWeight == 0) {
              heuristic = heuristicWeight * next.distanceTo(targetNode);
            }
            fScore.put(next, d + heuristic);
            unvisited.add(next);
          }
        }
      }
      visited.add(current);
    }

    List<E> path = new ArrayList<>();

    E lastEdge = cameFrom.get(targetNode);
    while (lastEdge != null) {
      path.add(lastEdge);
      lastEdge = cameFrom.get(lastEdge.getStart());
    }
    Collections.reverse(path);

    return path;
  }
}
