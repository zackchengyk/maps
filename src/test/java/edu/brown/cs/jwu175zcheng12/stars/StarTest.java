package edu.brown.cs.jwu175zcheng12.stars;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * A class which tests the Star class.
 */
public class StarTest {

  private Star star0;
  private Star star1;
  private Star star2;
  private Star star3;
  private Star funkyStar;
  private Star sameName;
  private Star binaryStar2;
  private static final double EPS = 0.001;

  /**
   * Sets up four stars for testing
   */
  @Before
  public void setUp() {
    star0 = new Star(0, "Star Zero", 0.125, 1.5, 3.75);
    star1 = new Star(1, "Star One", -9.875, 1.5, 3.75);
    star2 = new Star(2, "Star Two", 0, 0, 0);
    star3 = new Star(3, "Star Three", 10, 10, 10);
    funkyStar = new Star(4, "Efe18EB1u9gj'qweLFmf pGQt's", 3.4, Math.PI, 7.8);
    sameName = new Star(5, "Efe18EB1u9gj'qweLFmf pGQt's", 3.4, Math.PI, 7.8);
    binaryStar2 = new Star(6, "Star Two A", 0, 0, 0);
  }

  /**
   * Tests the getStarId(), getProperName(), and toString() functions from Star.
   */
  @Test
  public void testStarGetters() {
    assertEquals(0, star0.getStarId());
    assertEquals(1, star1.getStarId());
    assertEquals(4, funkyStar.getStarId());
    assertEquals("Star Zero", star0.getProperName());
    assertEquals("Star One", star1.getProperName());
    assertEquals("Efe18EB1u9gj'qweLFmf pGQt's", funkyStar.getProperName());

    Double[] temp = new Double[]{3.4, Math.PI, 7.8};
    assertEquals("Star{"
            + "starId=4"
            + ", properName=\"Efe18EB1u9gj'qweLFmf pGQt's\""
            + ", coordinates=" + Arrays.toString(temp)
            + "}",
        funkyStar.toString());
  }

  /**
   * Tests the equals() and hashCode() function from Star.
   */
  @Test
  public void testEqualsAndHashCode() {
    assertEquals(star0, star0);
    assertEquals(funkyStar, funkyStar);
    Star badIdStar = new Star(0, "Star Zero!?", -9.2, 160.1135, 2.1);
    Star anotherBadIdStar = new Star(0, "Also Star Zero!?", 1.0, 2.0, 4.0);

    // Equals self
    assertTrue(star0 == star0);
    assertTrue(star0.equals(star0));

    // Equals is reversible and only cares about starId
    assertTrue(star0.equals(badIdStar));
    assertTrue(badIdStar.equals(star0));

    // Equals does not care about name or location
    assertFalse(funkyStar.equals(sameName));
    assertFalse(star2.equals(binaryStar2));

    // Equals is transitive
    assertTrue(badIdStar.equals(anotherBadIdStar));
    assertTrue(star0.equals(anotherBadIdStar));

    // Misc equals edge cases
    assertFalse(star0.equals(null));
    assertFalse(star0.equals(1));
    assertFalse(star0.equals(new Double[]{0., 1., 2.}));

    // Tests hashCode()
    assertEquals(star0.hashCode(), star0.hashCode());
    assertEquals(star0.hashCode(), badIdStar.hashCode());
    assertNotEquals(star1.hashCode(), star0.hashCode());
  }

  /**
   * Tests the getX(), getY(), and getZ() functions.
   */
  @Test
  public void testCoordinateGetters() {
    assertEquals(0.125, star0.getX(), EPS);
    assertEquals(1.5, star1.getY(), EPS);
    assertEquals(0.0, star2.getZ(), EPS);
    assertEquals(10.0, star3.getX(), EPS);
    assertEquals(Math.PI, funkyStar.getY(), EPS);
    assertEquals(3.75, star0.getZ(), EPS);

    Assert.assertEquals(3, star0.getNumDimensions());
  }

  /**
   * Tests the getDistanceFrom() function.
   */
  @Test
  public void testGetDistanceFrom() {
    assertEquals(0.0, star0.getDistanceFrom(star0), EPS);
    assertEquals(10.0, star0.getDistanceFrom(star1), EPS);
    assertEquals(10.669, star1.getDistanceFrom(star2), EPS);
    assertEquals(17.3205, star2.getDistanceFrom(star3), EPS);
    assertEquals(14.451, star3.getDistanceFrom(star0), EPS);
    assertEquals(star0.getDistanceFrom(star3),
        star3.getDistanceFrom(star0), EPS);

    assertEquals(176.327, star0.getDistanceFrom(-100, -100, -100), EPS);
    assertEquals(120.452, funkyStar.getDistanceFrom(-6.1234, 123, 0.5887), EPS);
    assertEquals(4.0408, star0.getDistanceFrom(0, 0, 0), EPS);
    assertEquals(0.0, star3.getDistanceFrom(
        star3.getX(), star3.getY(), star3.getZ()), EPS);
    assertEquals(17.3205, star3.getDistanceFrom(
        star2.getX(), star2.getY(), star2.getZ()), EPS);

    assertThrows(IllegalArgumentException.class, () ->
        star3.euclideanDistance(new Double[]{0., 2., 4., 6.}));

    assertThrows(UnsupportedOperationException.class, () ->
        star3.haversineDistance(new Double[]{0., 2., 4., 6.}));

    assertThrows(UnsupportedOperationException.class, () ->
        star3.haversineDistance(new Double[]{0., 0.}));
  }
}
