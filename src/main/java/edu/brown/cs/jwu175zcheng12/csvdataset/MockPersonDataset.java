package edu.brown.cs.jwu175zcheng12.csvdataset;

import edu.brown.cs.jwu175zcheng12.stars.MockPerson;
import edu.brown.cs.jwu175zcheng12.repl.GeneralREPL;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * A class which represents a data set of mock people.
 */
public final class MockPersonDataset extends GeneralCSVDataset<MockPerson> {

  /**
   * The constructor for this class. It has no fields of its own.
   */
  public MockPersonDataset() {
    super("mock people", new String[]{"firstName", "lastName",
        "dateTime", "emailAddress", "gender", "streetAddress"});
  }

  @Override
  protected void handleOneCSVRow(String rowString, String[] rowStringArray,
                                 int counter) throws IOException {
    if (rowStringArray.length > 6) {
      GeneralREPL.printError("Malformed CSV input at row " + counter + ".");
      GeneralREPL.printError("Row looks like this: '" + rowString + "'.");
      throw new IOException("Malformed CSV.");
    }
    if (rowStringArray.length < 6) {
      // Pad back of array with null; accounts for missing fields "name,etc,,,,"
      rowStringArray = Arrays.copyOf(rowStringArray, 6);
    }
    // Make empty strings null
    int aDifferentCounter = 0;
    for (String ele : rowStringArray) {
      if (ele != null && ele.isEmpty()) {
        rowStringArray[aDifferentCounter] = null;
      }
      aDifferentCounter++;
    }
    // Assign variables
    String firstName = rowStringArray[0];
    String lastName = rowStringArray[1];
    String datetime = rowStringArray[2];
    String emailAddress = rowStringArray[3];
    String gender = rowStringArray[4];
    String streetAddress = rowStringArray[5];
    // Input checking
    if (datetime != null && !Pattern.matches("\\d{1,2}/\\d{1,2}/\\d{1,4}", datetime)) {
      GeneralREPL.printError("Bad non-null date pattern: '" + datetime
          + "', found at row " + counter + ".");
      throw new IOException("Bad non-null date pattern '" + datetime
          + "', found at row " + counter + ".");
    }
    if (emailAddress != null && !Pattern.matches("[^\\s]+@[\\w\\d_]+(?:.\\w+)+", emailAddress)) {
      GeneralREPL.printError("Bad non-null email pattern: '" + emailAddress
          + "', found at row " + counter + ".");
      throw new IOException("Bad non-null email pattern '" + emailAddress
          + "', found at row " + counter + ".");
    }
    // Add person to allData
    MockPerson newMockPerson = new MockPerson(firstName, lastName, datetime,
        emailAddress, gender, streetAddress);
    super.addToAllData(newMockPerson);
    // Print person
    GeneralREPL.print(newMockPerson.toString());
  }
}
