package edu.brown.cs.jwu175zcheng12.repl;

import java.util.Objects;

/**
 * An abstract class which represents a pair.
 *
 * @param <A> the type of the first member of this pair.
 * @param <B> the type of the second member of this pair.
 */
public class GeneralPair<A, B> {

  private final A first;
  private final B second;

  /**
   * The constructor for this class. Note that A and B, if mutable, remain
   * mutable. GeneralPair merely points to A and B.
   *
   * @param first   an A, the first element of this A-B pair
   * @param second  a B, the second element of this A-B pair
   */
  public GeneralPair(A first, B second) {
    this.first = first;
    this.second = second;
  }

  /**
   * A getter function for this class' first field.
   *
   * @return an A, the first element of this A-B pair
   */
  public final A getFirst() {
    return this.first;
  }

  /**
   * A getter function for this class' second field.
   *
   * @return a B, the second element of this A-B pair
   */
  public final B getSecond() {
    return this.second;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GeneralPair<?, ?> pair = (GeneralPair<?, ?>) o;
    return first.equals(pair.first) && second.equals(pair.second);
  }

  @Override
  public int hashCode() {
    return Objects.hash(first, second);
  }
}
