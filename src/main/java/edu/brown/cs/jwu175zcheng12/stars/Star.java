package edu.brown.cs.jwu175zcheng12.stars;

import edu.brown.cs.jwu175zcheng12.kdtree.ObjectInNDSpace;

import java.util.Arrays;
import java.util.Objects;

/**
 * A class which represents a star.
 */
public class Star extends ObjectInNDSpace {

  private final int starId;
  private final String properName;

  /**
   * The constructor for this class. Every field is a final immutable object
   * or primitive, so no copying is necessary.
   *
   * @param starId     an int, the identification number for a Star (unique)
   * @param properName a String, the name of a Star (not unique)
   * @param x          a double, the x-coordinate of the Star's position
   * @param y          a double, the y-coordinate of the Star's position
   * @param z          a double, the z-coordinate of the Star's position
   */
  public Star(int starId, String properName, double x, double y, double z) {
    super(new Double[]{x, y, z});
    this.starId = starId;
    this.properName = properName;
  }

  /**
   * A getter function for this class' starId field.
   *
   * @return the starId of this Star
   */
  public int getStarId() {
    return this.starId;
  }

  /**
   * A getter function for this class' properName field.
   *
   * @return the properName of this Star
   */
  public String getProperName() {
    return this.properName;
  }

  /**
   * A getter function for this Star's x-coordinate.
   *
   * @return this Star's x-coordinate
   */
  public final double getX() {
    return super.getNthCoordinate(0);
  }

  /**
   * A getter function for this Star's y-coordinate.
   *
   * @return this Star's y-coordinate
   */
  public final double getY() {
    return super.getNthCoordinate(1);
  }

  /**
   * A getter function for this Star's z-coordinate.
   *
   * @return this Star's z-coordinate
   */
  public final double getZ() {
    return super.getNthCoordinate(2);
  }

  /**
   * A function which calculates the Euclidean distance between a
   * given reference Star and this Star's position.
   *
   * @param refStar a Star whose position is to be used as the reference
   *                position
   * @return a double, the Euclidean distance between the given
   * reference Star and this Star's position.
   */
  public final double getDistanceFrom(Star refStar) {
    return super.euclideanDistance(refStar.getCoordinates());
  }

  /**
   * A function which calculates the Euclidean distance between a
   * given reference position and this Star's position.
   *
   * @param refX an int, the x-coordinate of the reference position
   * @param refY an int, the y-coordinate of the reference position
   * @param refZ an int, the z-coordinate of the reference position
   * @return a double, the Euclidean distance between the given
   * reference position and this Star's position.
   */
  public final double getDistanceFrom(double refX, double refY, double refZ) {
    return super.euclideanDistance(new Double[]{refX, refY, refZ});
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Star star = (Star) o;
    return starId == star.starId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(starId);
  }

  @Override
  public String toString() {
    return "Star{"
        + "starId=" + starId
        + ", properName=\"" + properName + "\""
        + ", coordinates=" + Arrays.toString(super.getCoordinates())
        + "}";
  }
}
