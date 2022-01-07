package edu.brown.cs.jwu175zcheng12.graph;

/**
 * An abstract class which represents a GeneralEdge between two GeneralNodes in
 * some unknown-dimensional space.
 *
 * @param <N> the concrete extension of GeneralNode that this GeneralEdge uses
 * @param <E> the concrete extension of GeneralEdge that this GeneralEdge uses
 */
public abstract class GeneralEdge<N extends GeneralNode<N, E>, E extends GeneralEdge<N, E>> {
  private final N start;
  private final N end;
  private final double weight;

  /**
   * The constructor for this class. Every field is a final immutable object
   * or primitive, so no copying is necessary.
   *
   * @param start   the N at the beginning of this E
   * @param end     the N at the end of this E
   * @param weight  the weight associated with this E, or null if the
   *                distanceTo() between start and end should be used as the
   *                weight instead
   */
  public GeneralEdge(N start, N end, Double weight) {
    // Save start and end N's
    if (start == null || end == null) {
      throw new IllegalArgumentException(
          "Arguments to the GeneralEdge constructor cannot be null.");
    }
    this.start = start;
    this.end = end;

    // Save weight
    if (weight == null) {
      this.weight = start.distanceTo(end);
    } else if (weight < 0) {
      throw new IllegalArgumentException(
          "When provided, the GeneralEdge's weight cannot be less than zero.");
    } else {
      this.weight = weight;
    }
  }

  /**
   * A getter function for this class' start field.
   *
   * @return the GeneralNode at the start of this GeneralEdge
   */
  public final N getStart() {
    return this.start;
  }

  /**
   * A getter function for this class' end field.
   *
   * @return the GeneralNode at the end of this GeneralEdge
   */
  public final N getEnd() {
    return this.end;
  }

  /**
   * A getter function for this class' weight field.
   *
   * @return the weight associated with this GeneralEdge
   */
  public final double getWeight() {
    return this.weight;
  }
}
