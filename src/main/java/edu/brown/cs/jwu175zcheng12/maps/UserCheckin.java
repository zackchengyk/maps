package edu.brown.cs.jwu175zcheng12.maps;

/**
 * class for passing user checkin data.
 */
public class UserCheckin {

  private final int id;
  private final String name;
  private final double ts;
  private final double lat;
  private final double lon;

  /**
   * The constructor for this class.
   *
   * @param userId    the userId field for a userCheckin
   * @param username  the username field for a userCheckin
   * @param timestamp the timestamp field for a userCheckin
   * @param latitude  the latitude field for a userCheckin
   * @param longitude the longitude field for a userCheckin
   */
  public UserCheckin(
      int userId,
      String username,
      double timestamp,
      double latitude,
      double longitude) {
    id = userId;
    name = username;
    ts = timestamp;
    lat = latitude;
    lon = longitude;
  }

  /**
   * A getter function for this class's id field.
   * @return this class's id field
   */
  public int getId() {
    return id;
  }

  /**
   * A getter function for this class's name field.
   * @return this class's name field
   */
  public String getName() {
    return name;
  }

  /**
   * A getter function for this class's timestamp field.
   * @return this class's timestamp field
   */
  public double getTimestamp() {
    return ts;
  }

  /**
   * A getter function for this class's lat field.
   * @return this class's lat field
   */
  public double getLat() {
    return lat;
  }

  /**
   * A getter function for this class's lon field.
   * @return this class's lon field
   */
  public double getLon() {
    return lon;
  }
}
