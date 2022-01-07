package edu.brown.cs.jwu175zcheng12.stars;

/**
 * A class which represents a row of the Mockaroo-generated dataset.
 */
public class MockPerson {
  private final String firstName;
  private final String lastName;
  private final String datetime;
  private final String emailAddress;
  private final String gender;
  private final String streetAddress;

  /**
   * The constructor for this class. Every field is a final immutable object, so
   * no copying is necessary.
   *
   * @param firstName     the first name of this mock person. May be null.
   * @param lastName      the last name of this mock person. May be null.
   * @param datetime      the associated datetime of this mock person,
   *                      perhaps a birthday, who knows. May be null.
   * @param emailAddress  the email address of this mock person. May be null.
   * @param gender        the gender of this mock person. May be null.
   * @param streetAddress the street address of this mock person. May be null.
   */
  public MockPerson(String firstName, String lastName, String datetime,
                    String emailAddress, String gender, String streetAddress) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.datetime = datetime;
    this.emailAddress = emailAddress;
    this.gender = gender;
    this.streetAddress = streetAddress;
  }

  @Override
  public String toString() {
    return "MockPerson{firstName='" + firstName
        + "', lastName='" + lastName
        + "', datetime='" + datetime
        + "', emailAddress='" + emailAddress
        + "', gender='" + gender
        + "', streetAddress='" + streetAddress
        + "'}";
  }
}
