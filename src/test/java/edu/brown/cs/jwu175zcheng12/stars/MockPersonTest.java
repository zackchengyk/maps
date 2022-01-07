package edu.brown.cs.jwu175zcheng12.stars;

import edu.brown.cs.jwu175zcheng12.csvdataset.MockPersonDataset;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * A class which tests the MockPerson class.
 */
public class MockPersonTest {

  private static final double EPS = 0.001;

  /**
   * Tests MockPerson's and MockPersonDataset's methods.
   */
  @Test
  public void testToString() {
    MockPerson m1 = new MockPerson("First", "Last", "05/05/0505",
        "m1@m1.com", "lol", "69 Brown St");
    assertEquals("MockPerson{firstName='First"
        + "', lastName='Last"
        + "', datetime='05/05/0505"
        + "', emailAddress='m1@m1.com"
        + "', gender='lol"
        + "', streetAddress='69 Brown St"
        + "'}", m1.toString());

    MockPersonDataset mSet = new MockPersonDataset();
    mSet.loadData("data/stars/mock-person-data-short.csv", true);

    // Tests handleOneCSVRow() exceptions (not actually thrown, because they
    // are caught by loadData())
    mSet.loadData("data/stars/mock-person-bad-csv-format.csv", true);
    mSet.loadData("data/stars/mock-person-bad-date.csv", true);
    mSet.loadData("data/stars/mock-person-bad-email.csv", true);
    mSet.loadData("data/stars/mock-person-bad-header.csv", true);
    mSet.loadData("data/stars/mock-person-header-only.csv", true);
    mSet.loadData("data/stars/empty-csv.csv", true);
  }
}
