package edu.brown.cs.jwu175zcheng12.maps;

import edu.brown.cs.jwu175zcheng12.graph.GeneralEdge;
import java.util.Objects;

/**
 * A class which represents a Way between two traversable Nodes on a map.
 */
public class Way extends GeneralEdge<Node, Way> {

  private final String wayId;
  private final String name;
  private final String type;

  // ----------------------------- Constructors ----------------------------

  /**
   * The constructor for this class. Every field is a final immutable object
   * or primitive, so no copying is necessary.
   *
   * @param wayId an String, the Id of this Way
   * @param start the Node at the beginning of this Way
   * @param end   the Node at the end of this Way
   * @param name  a String, the name of this Way (may be the empty string)
   * @param type  a String, the type of this Way (may NOT be the empty string
   *              or "unclassified")
   */
  public Way(String wayId, Node start, Node end, String name, String type) {
    super(start, end, null);
    if (wayId == null || name == null || type == null) {
      throw new IllegalArgumentException(
          "Arguments to the Way constructor cannot be null.");
    }
    if (type.length() == 0 || type.equals("unclassified")) {
      throw new IllegalArgumentException(
          "The Way's type cannot be the empty string or \"unclassified\".");
    }
    this.wayId = wayId;
    this.name = name;
    this.type = type;
  }

  // ------------------------- Getters and Setters -------------------------

  /**
   * A getter function for this class' wayId field.
   *
   * @return the wayId of this Way
   */
  public String getWayId() {
    return wayId;
  }

  /**
   * A getter function for this class' name field.
   *
   * @return the name of this Way (may be the empty string)
   */
  public String getName() {
    return name;
  }

  /**
   * A getter function for this class' type field.
   *
   * @return the type of this Way (may NOT be the empty string or
   * "unclassified")
   */
  public String getType() {
    return type;
  }

  // ---------------------------- Miscellaneous ----------------------------

  @Override
  public String toString() {
    return "Way{"
        + "wayId=\"" + wayId + "\""
        + ", start=" + super.getStart()
        + ", end=" + super.getEnd()
        + ", name=\"" + name + "\""
        + ", type=\"" + type + "\"}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Way way = (Way) o;
    return wayId.equals(way.wayId) && super.getStart().equals(way.getStart())
        && super.getEnd().equals(way.getEnd()) && Objects.equals(name, way.name)
        && Objects.equals(type, way.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(wayId, super.getStart(), super.getEnd(), name, type);
  }

  /**
   * A function purely for testing. Danger! Never use this outside of testing.
   *
   * @return the Euclidean distance between the start and end Nodes, treating
   * their coordinates as if they were on a 2D Cartesian plane.
   */
  public double euclideanWayLength() {
    return super.getStart().euclideanDistance(super.getEnd().getCoordinates());
  }
}
