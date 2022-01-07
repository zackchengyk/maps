package edu.brown.cs.jwu175zcheng12.stars;

import edu.brown.cs.jwu175zcheng12.repl.GeneralPair;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * A class which tests the GeneralPair class.
 */
public class GeneralPairTest {

  private static final double EPS = 0.001;

  /**
   * Tests GeneralPair's methods.
   */
  @Test
  public void testEqualsConstructorsAndGetters() {
    Star star0 = new Star(0, "Star Zero", 0.125, 1.5, 3.75);
    Star star1 = new Star(1, "Star One", -9.875, 1.5, 3.75);

    GeneralPair<Star, Double> pair0 = new GeneralPair<>(star0, 1122.123491235);
    GeneralPair<Star, Double> pair1 = new GeneralPair<>(star0, 1122.123491235);
    GeneralPair<Star, Double> pair2 = new GeneralPair<>(star0, 1122.123491235);
    GeneralPair<Star, Double> pair3 = new GeneralPair<>(star1, 1122.123491235);
    GeneralPair<Star, Double> pair4 = new GeneralPair<>(star0, -69.5112760);
    GeneralPair<Star, Double> pair3null = new GeneralPair<>(null, 1122.123491235);
    GeneralPair<Star, Double> pair4null = new GeneralPair<>(star0, null);

    Integer testInteger = 2;
    GeneralPair<Integer, GeneralPair<Star, Double>> intAndPairPair =
        new GeneralPair<>(testInteger, pair2);
    GeneralPair<Object, String> nullAndLolPair = new GeneralPair<>(null, "lol");

    // Getters
    assertEquals(testInteger, intAndPairPair.getFirst());
    assertEquals(pair2, intAndPairPair.getSecond());
    assertEquals(null, nullAndLolPair.getFirst());
    assertEquals("lol", nullAndLolPair.getSecond());

    // Equals self
    assertTrue(pair0 == pair0);
    assertTrue(pair0.equals(pair0));

    // Equals is reversible and only cares about its two elements
    assertTrue(pair0.equals(pair1));
    assertTrue(pair1.equals(pair0));

    // Equals does not care about only one of two elements being correct
    assertFalse(pair0.equals(pair3));
    assertFalse(pair0.equals(pair4));
    assertFalse(pair0.equals(pair3null));
    assertFalse(pair0.equals(pair4null));

    // Equals is transitive
    assertTrue(pair0.equals(pair2));
    assertTrue(pair1.equals(pair2));

    // Misc equals edge cases
    assertFalse(pair1.equals(null));
    assertFalse(pair1.equals(1));
    assertFalse(pair1.equals(star0));
    assertFalse(pair1.equals(new Double[]{0., 1., 2.}));

    // Tests hashCode()
    assertEquals(pair1.hashCode(), pair1.hashCode());
    assertEquals(pair1.hashCode(), pair2.hashCode());
    assertNotEquals(pair3.hashCode(), pair1.hashCode());
  }
}
