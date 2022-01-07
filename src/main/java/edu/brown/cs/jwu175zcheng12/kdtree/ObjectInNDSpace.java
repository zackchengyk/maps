package edu.brown.cs.jwu175zcheng12.kdtree;

import java.util.Arrays;

/**
 * An abstract class which represents an object in ND space.
 */
public abstract class ObjectInNDSpace {

  private final Double[] coordinates;
  private final int numDimensions;
  private final Double sphereRadius;
  private static final Double EARTH_RADIUS = 6371.0;

  /**
   * The constructor for this class. Note that a copy must be made of the
   * coordinates argument, since if not the caller might be able to change its
   * cells after the ObjectInNDSpace has been created.
   *
   * @param coordinates an Array of Doubles representing the coordinates
   *                    of this object's location in ND space, where the 0th
   *                    element might be the x coordinate, the 1st might be the
   *                    y coordinate, etc.
   */
  protected ObjectInNDSpace(Double[] coordinates) {
    if (coordinates == null) {
      throw new IllegalArgumentException(
          "The coordinates of an ObjectInNDSpace cannot be null.");
    }
    if (Arrays.asList(coordinates).contains(null)) {
      throw new IllegalArgumentException(
          "The coordinates of an ObjectInNDSpace cannot contain null values.");
    }
    this.coordinates = coordinates.clone();
    this.numDimensions = coordinates.length;
    this.sphereRadius = null;
  }

  /**
   * Another constructor for this class. This constructor offers the ability to
   * set the sphereRadius, which is used for haversineDistances. It is set to
   * null by default if the other constructor is used---using this constructor
   * with a null argument in sphereRadius is will default sphereRadius to
   * EARTH_RADIUS. Coordinates are assumed to be in the form [latitude,
   * longitude] if this constructor is used.
   *
   * Note that a copy must be made of the coordinates argument, since if not
   * the caller might be able to change its cells after the ObjectInNDSpace has
   * been created.
   *
   * @param coordinates  an Array of Doubles representing the coordinates
   *                     of this object's location in ND space, where the 0th
   *                     element might be the x coordinate, the 1st might be the
   *                     y coordinate, etc.
   * @param sphereRadius the radius of the sphere on which the two coordinates,
   *                     latitude and longitude, are placed.
   */
  protected ObjectInNDSpace(Double[] coordinates, Double sphereRadius) {
    if (coordinates == null) {
      throw new IllegalArgumentException(
          "The coordinates of an ObjectInNDSpace cannot be null.");
    }
    if (coordinates.length != 2) {
      throw new IllegalArgumentException(
          "Coordinates must have [latitude, longitude] form in this constructor.");
    }
    if (coordinates[0] == null || coordinates[1] == null) {
      throw new IllegalArgumentException(
          "The coordinates of an ObjectInNDSpace cannot contain null values.");
    }
    this.coordinates = coordinates.clone();
    this.numDimensions = coordinates.length;
    if (sphereRadius == null) {
      this.sphereRadius = EARTH_RADIUS;
    } else {
      this.sphereRadius = sphereRadius;
    }
  }

  /**
   * A getter function for this class' n-th coordinate. Indexing begins from 0.
   *
   * @param n the index of the coordinate to be returned (0 is x, 1 is y, etc)
   * @return the n-th coordinate of this ObjectInNDSpace
   */
  public final double getNthCoordinate(int n) {
    return this.coordinates[n];
  }

  /**
   * A getter function for this class' coordinates. The extra steps make sure
   * that the output does not expose the mutable coordinates field.
   *
   * @return the coordinates array of this ObjectInNDSpace
   */
  public final Double[] getCoordinates() {
    Double[] output = new Double[this.numDimensions];
    System.arraycopy(this.coordinates, 0,
        output, 0, this.numDimensions);
    return output;
  }

  /**
   * A getter function for this class' numDimensions.
   *
   * @return the numDimensions of this ObjectInNDSpace
   */
  public final int getNumDimensions() {
    return this.numDimensions;
  }

  /**
   * A function which calculates the Euclidean distance between a
   * given reference position and this ObjectInNDSpace's position.
   *
   * @param refCoordinates an Array of Doubles with length numDimensions
   *                       giving the coordinates of the reference position
   * @return a double, the Euclidean distance between the given
   * reference position and this ObjectInNDSpace's position.
   */
  public final double euclideanDistance(Double[] refCoordinates) {
    return Math.sqrt(this.squaredEuclideanDistance(refCoordinates));
  }

  /**
   * A function which calculates the squared Euclidean distance between a given
   * reference position and this ObjectInNDSpace's position.
   *
   * @param refCoordinates an Array of Doubles with length numDimensions
   *                       giving the coordinates of the reference position
   * @return a double, the squared Euclidean distance between the given
   * reference position and this ObjectInNDSpace's position.
   */
  public final double squaredEuclideanDistance(Double[] refCoordinates) {
    if (refCoordinates.length != numDimensions) {
      throw new IllegalArgumentException(
          "Input must be a Double array with length " + numDimensions + ".");
    }
    double dist = 0;
    for (int i = 0; i < this.coordinates.length; i++) {
      dist += Math.pow(refCoordinates[i] - this.coordinates[i], 2);
    }
    return dist;
  }

  /**
   * A function which calculates the Haversine distance between a
   * given reference position and this ObjectInNDSpace's position. Both are
   * assumed to have position specified by [latitude, longitude]. Latitude and
   * longitude should both be provided in DEGREES, and this function ASSUMES
   * that both coordinates are provided for the same sphereRadius!
   *
   * @param refCoordinates an Array of Doubles with length 2 giving the
   *                       [latitude, longitude] of the reference position
   * @return a double, the Haversine distance between the given
   * reference position and this ObjectInNDSpace's position.
   */
  public final double haversineDistance(Double[] refCoordinates) {
    if (this.numDimensions != 2 || refCoordinates.length != 2) {
      throw new UnsupportedOperationException(
          "Cannot compute Haversine distance unless both coordinates are [latitude, longitude].");
    }
    double phi1 = this.getCoordinates()[0];
    double lam1 = this.getCoordinates()[1];
    double phi2 = refCoordinates[0];
    double lam2 = refCoordinates[1];
    double sinSqPhiTerm = Math.pow(Math.sin(Math.toRadians((phi1 + phi2) / 2)), 2);
    double sinSqLamTerm = Math.pow(Math.sin(Math.toRadians((lam1 + lam2) / 2)), 2);
    double insideSqrtTerm = sinSqPhiTerm + Math.cos(Math.toRadians(phi1))
        * Math.cos(Math.toRadians(phi2)) * sinSqLamTerm;
    return 2 * this.sphereRadius * Math.asin(Math.sqrt(insideSqrtTerm));
  }
}
