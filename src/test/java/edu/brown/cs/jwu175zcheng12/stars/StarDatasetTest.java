package edu.brown.cs.jwu175zcheng12.stars;

import edu.brown.cs.jwu175zcheng12.csvdataset.StarDataset;

import java.util.List;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * A class which tests the StarDataset class.
 */
public class StarDatasetTest {

  private StarDataset starDataset;
  private static final double EPS = 0.001;

  /**
   * Tests that loadData doesn't behave differently whether the user
   * specifies for headers to be checked or not
   */
  @Test
  public void testHeaderChecking() {
    StarDataset starDataset = new StarDataset();
    starDataset.loadData("data/stars/one-star.csv", false);
    starDataset.loadData("data/stars/one-star.csv", true);
    starDataset.loadData("data/stars/ten-star.csv", false);
    starDataset.loadData("data/stars/ten-star.csv", true);
    starDataset.loadData("data/stars/stardata.csv", false);
    starDataset.loadData("data/stars/stardata.csv", true);
  }

  @Test
  public void testBadCSV() {
    StarDataset starDataset = new StarDataset();
    // Tests handleOneCSVRow() exceptions (not actually thrown, because they
    // are caught by loadData())
    starDataset.loadData("data/stars/stars-bad-csv-format.csv", true);
    starDataset.loadData("data/stars/stars-header-only.csv", true);
    starDataset.loadData("data/stars/stars-bad-header.csv", true);
    starDataset.loadData("data/stars/empty-csv.csv", true);
    starDataset.loadData("data/stars/stars-bad-number.csv", true);
    starDataset.loadData("data/stars/bad-dup-id-star.csv", true);

    // Assert that when the dataset is trash, nothing can be returned (an error
    // message is printed, though)
    Star fakeStar = new Star(1, "allData is null lol", 123414.1, -34.12, 0.0);
    assertTrue(starDataset.naiveRadius(1.0, fakeStar).isEmpty());
    assertTrue(starDataset.radius(1.0, fakeStar).isEmpty());
    assertTrue(starDataset.naiveNeighbors(2, fakeStar).isEmpty());
    assertTrue(starDataset.neighbors(2, fakeStar).isEmpty());
    assertTrue(starDataset.naiveRadius(1.0, 0.0, 1.0, 2.0).isEmpty());
    assertTrue(starDataset.radius(1.0, 0.0, 1.0, 2.0).isEmpty());
    assertTrue(starDataset.naiveNeighbors(2, 0.0, 1.0, 2.0).isEmpty());
    assertTrue(starDataset.neighbors(2, 0.0, 1.0, 2.0).isEmpty());
  }

  /**
   * Tests the StarDataset functions with nothing loaded.
   */
  @Test
  public void testNoStar() {
    StarDataset starDataset = new StarDataset();
    Star someMadeUpStar = new Star(0, "0", 0, 0, 0);

    // Tests getStarByName()
    Star star1 = starDataset.getStarByName("Any Star");
    assertEquals(null, star1);

    // Tests naiveRadius() and radius()
    List<Star> result1 = starDataset.naiveRadius(100, 0, 0, 0);
    assertEquals(0, result1.size());
    List<Star> result1_KDTree = starDataset.radius(100, 0, 0, 0);
    assertEquals(0, result1_KDTree.size());
    List<Star> result2 = starDataset.naiveRadius(100, someMadeUpStar);
    assertEquals(0, result2.size());
    List<Star> result2_KDTree = starDataset.radius(100, someMadeUpStar);
    assertEquals(0, result2_KDTree.size());

    // Tests naiveNeighbors() and neighbors()
    List<Star> result3 = starDataset.naiveNeighbors(100, 0, 0, 0);
    assertEquals(0, result3.size());
    List<Star> result3_KDTree = starDataset.neighbors(100, 0, 0, 0);
    assertEquals(0, result3_KDTree.size());
    List<Star> result4 = starDataset.naiveNeighbors(100, someMadeUpStar);
    assertEquals(0, result4.size());
    List<Star> result4_KDTree = starDataset.neighbors(100, someMadeUpStar);
    assertEquals(0, result4_KDTree.size());

    // Tests exception handling
    starDataset.loadData("lol/no/such/file.csv", true);

    // Tests naiveRadius(), radius(), naiveNeighbors(), and neighbors() when
    // refStar is not in dataset
    starDataset.loadData("data/stars/one-star.csv", true);
    List<Star> result5 = starDataset.naiveRadius(100, someMadeUpStar);
    assertEquals(0, result5.size());
    List<Star> result5_KDTree = starDataset.radius(100, someMadeUpStar);
    assertEquals(0, result5_KDTree.size());
    List<Star> result6 = starDataset.naiveNeighbors(100, someMadeUpStar);
    assertEquals(0, result6.size());
    List<Star> result6_KDTree = starDataset.neighbors(100, someMadeUpStar);
    assertEquals(0, result6_KDTree.size());
  }

  /**
   * Tests the StarDataset functions with the one-star.csv file.
   */
  @Test
  public void testOneStar() {
    StarDataset starDataset = new StarDataset();
    starDataset.loadData("data/stars/one-star.csv", true);

    // Tests getStarByName()
    Star lonelyStar = starDataset.getStarByName("Lonely Star");
    assertEquals(5.0, lonelyStar.getX(), EPS);
    assertEquals(-2.24, lonelyStar.getY(), EPS);
    assertEquals(10.04, lonelyStar.getZ(), EPS);
    assertEquals(1, lonelyStar.getStarId());
    assertEquals("Lonely Star", lonelyStar.getProperName());

    // Tests that naiveRadius() and naiveNeighbors() return just lonelyStar
    List<Star> result1 = starDataset.naiveRadius(100, 0, 10.123, 5.6183);
    List<Star> result2 = starDataset.naiveNeighbors(100, 51.99, 1234, 348.2);
    assertEquals(result1, result2);
    assertEquals(1, result1.size());
    assertEquals(lonelyStar, result2.get(0));

    // Tests that radius() and neighbors() return just lonelyStar
    List<Star> result1_KDTree = starDataset.radius(100, 0, 10.123, 5.6183);
    List<Star> result2_KDTree = starDataset.neighbors(100, 51.99, 1234, 348.2);
    assertEquals(result1_KDTree, result2_KDTree);
    assertEquals(1, result1_KDTree.size());
    assertEquals(lonelyStar, result2_KDTree.get(0));

    // Tests that naiveRadius() and naiveNeighbors() do not return their refStar
    List<Star> result3 = starDataset.naiveRadius(0, lonelyStar);
    List<Star> result4 = starDataset.naiveNeighbors(2, lonelyStar);
    assertEquals(result3, result4);
    assertEquals(0, result3.size());

    // Tests that radius() and neighbors() do not return their refStar
    List<Star> result3_KDTree = starDataset.radius(0, lonelyStar);
    List<Star> result4_KDTree = starDataset.neighbors(2, lonelyStar);
    assertEquals(result3_KDTree, result4_KDTree);
    assertEquals(0, result3_KDTree.size());


    // Tests that the starDataset is cleared between loads
    starDataset.loadData("data/stars/ten-star.csv", true);
    Star isThereALonelyStar = starDataset.getStarByName("Lonely Star");
    assertEquals(null, isThereALonelyStar);
  }

  /**
   * Tests the StarDataset functions with the three-star.csv file.
   */
  @Test
  public void testThreeStar() {
    StarDataset starDataset = new StarDataset();

    // Tests getStarByName() returning null if nothing in dataset
    Star datasetNotLoaded = starDataset.getStarByName("Sol");
    assertEquals(null, datasetNotLoaded);

    starDataset.loadData("data/stars/three-star.csv", true);

    // Tests getStarByName() returning null if not found
    Star notInThisDataset = starDataset.getStarByName("Sirius");
    assertEquals(null, notInThisDataset);

    // Tests that naiveRadius() returns all boundary stars, but not outside
    Star star3 = starDataset.getStarByName("Star Three");
    List<Star> result1 = starDataset.naiveRadius(0.5, 1.5, 0, 0);
    assertEquals(2, result1.size());
    assertNotEquals(result1.get(0), result1.get(1));
    assertFalse(result1.contains(star3));

    // Tests that radius() returns all boundary stars, but not outside
    List<Star> result1_KDTree = starDataset.radius(0.5, 1.5, 0, 0);
    assertEquals(2, result1_KDTree.size());
    assertNotEquals(result1_KDTree.get(0), result1_KDTree.get(1));
    assertFalse(result1_KDTree.contains(star3));

    // Tests that naiveNeighbors returns nothing if k = 0
    List<Star> result2 = starDataset.naiveNeighbors(0, star3);
    List<Star> result3 = starDataset.naiveNeighbors(0, 0, 0, 0);
    assertEquals(0, result2.size());
    assertEquals(0, result3.size());

    // Tests that neighbors returns nothing if k = 0
    List<Star> result2_KDTree = starDataset.neighbors(0, star3);
    List<Star> result3_KDTree = starDataset.neighbors(0, 0, 0, 0);
    assertEquals(0, result2_KDTree.size());
    assertEquals(0, result3_KDTree.size());
  }

  /**
   * Tests the StarDataset functions with the tie-star.csv file.
   */
  @Test
  public void testTieStar() {
    StarDataset starDataset = new StarDataset();
    starDataset.loadData("data/stars/tie-star.csv", true);

    Star colton = starDataset.getStarByName("Colton");
    Star daphne = starDataset.getStarByName("Daphne");
    Star daniel = starDataset.getStarByName("Daniel");
    Star lulu = starDataset.getStarByName("Lulu");
    Star sweetiePie = starDataset.getStarByName("Sweetie Pie");
    Star harvey = starDataset.getStarByName("Harvey");
    Star rocket = starDataset.getStarByName("Rocket");
    Star tommy = starDataset.getStarByName("Tommy");
    Star mystery = starDataset.getStarByName("Mystery");
    Star charli = starDataset.getStarByName("Charli");
    Star charliAgain = starDataset.getStarByName("Charli");
    Star charliOneMoreTime = starDataset.getStarByName("Charli");

    // Tests getStarByName()
    assertNotEquals(colton, daphne);
    assertNotEquals(daphne, daniel);
    assertNotEquals(daniel, colton);
    assertEquals(charli, charliAgain);
    assertEquals(charliAgain, charliOneMoreTime);

    // Tests that naiveRadius() returns same-position stars if r = 0
    List<Star> result1 = starDataset.naiveRadius(0, daniel);
    assertTrue(result1.contains(colton));
    assertTrue(result1.contains(daphne));

    // Tests that radius() returns same-position stars if r = 0
    List<Star> result1_KDTree = starDataset.radius(0, daniel);
    assertTrue(result1_KDTree.contains(colton));
    assertTrue(result1_KDTree.contains(daphne));

    // Tests that naiveRadius() returns all boundary stars
    List<Star> result2 = starDataset.naiveRadius(1, daniel);
    assertEquals(9, result2.size());
    List<Star> result3 = starDataset.naiveRadius(1, 0, 0, 0);
    assertEquals(10, result3.size());

    // Tests that naiveRadius() returns all boundary stars
    List<Star> result2_KDTree = starDataset.radius(1, daniel);
    assertEquals(9, result2_KDTree.size());
    List<Star> result3_KDTree = starDataset.radius(1, 0, 0, 0);
    assertEquals(10, result3_KDTree.size());

    // Tests that naiveNeighbors() returns same-position stars, and
    // randomly selects if more than k
    List<Star> result4 = starDataset.naiveNeighbors(1, daniel);
    assertTrue(result4.contains(colton) || result4.contains(daphne));
    assertFalse(result4.contains(colton) && result4.contains(daphne));

    // Tests that neighbors() returns same-position stars, and
    // randomly selects if more than k
    List<Star> result4_KD_Tree = starDataset.neighbors(1, daniel);
    assertTrue(result4_KD_Tree.contains(colton)
        || result4_KD_Tree.contains(daphne));
    assertFalse(result4_KD_Tree.contains(colton)
        && result4_KD_Tree.contains(daphne));

    // Tests that naiveNeighbors() returns stars in order of distance
    List<Star> result5 = starDataset.naiveNeighbors(20, lulu);
    Double lastStarDist = null;
    for (Star resultStar : result5) {
      if (lastStarDist != null) {
        assertTrue(resultStar.getDistanceFrom(lulu) >= lastStarDist);
      }
      lastStarDist = resultStar.getDistanceFrom(lulu);
    }

    // Tests that neighbors() returns stars in order of distance
    List<Star> result5_KDTree = starDataset.neighbors(20, lulu);
    lastStarDist = null;
    for (Star resultStar : result5_KDTree) {
      if (lastStarDist != null) {
        assertTrue(resultStar.getDistanceFrom(lulu) >= lastStarDist);
      }
      lastStarDist = resultStar.getDistanceFrom(lulu);
    }

    // Tests that naiveRadius() returns stars in order of distance and
    // correctly cuts out stars past radius limit
    List<Star> result6 = starDataset.naiveRadius(1.7, lulu);
    assertTrue(result6.contains(charli));
    assertFalse(result6.contains(rocket));
    lastStarDist = null;
    for (Star resultStar : result6) {
      if (lastStarDist != null) {
        assertTrue(resultStar.getDistanceFrom(lulu) >= lastStarDist);
      }
      lastStarDist = resultStar.getDistanceFrom(lulu);
    }

    // Tests that radius() returns stars in order of distance and
    // correctly cuts out stars past radius limit
    List<Star> result6_KDTree = starDataset.radius(1.7, lulu);
    assertTrue(result6_KDTree.contains(charli));
    assertFalse(result6_KDTree.contains(rocket));
    lastStarDist = null;
    for (Star resultStar : result6_KDTree) {
      if (lastStarDist != null) {
        assertTrue(resultStar.getDistanceFrom(lulu) >= lastStarDist);
      }
      lastStarDist = resultStar.getDistanceFrom(lulu);
    }

    // Tests naiveNeighbors() again for JaCoCo
    // Tests that naiveNeighbors() returns stars in order of distance
    List<Star> result7 = starDataset.naiveNeighbors(200000, daphne);
    lastStarDist = null;
    for (Star resultStar : result7) {
      if (lastStarDist != null) {
        assertTrue(resultStar.getDistanceFrom(daphne) >= lastStarDist);
      }
      lastStarDist = resultStar.getDistanceFrom(daphne);
    }

    // Tests neighbors() again for JaCoCo
    // Tests that neighbors() returns stars in order of distance
    List<Star> result7_KDTree = starDataset.neighbors(200000, daphne);
    lastStarDist = null;
    for (Star resultStar : result7_KDTree) {
      if (lastStarDist != null) {
        assertTrue(resultStar.getDistanceFrom(daphne) >= lastStarDist);
      }
      lastStarDist = resultStar.getDistanceFrom(daphne);
    }
  }

  /**
   * Tests the StarDataset functions with the stardata.csv file,
   * and a little bit of command stuff thrown in.
   */
  @Test
  public void testNormalStardata() {
    StarDataset starDataset = new StarDataset();
    starDataset.loadData("data/stars/stardata.csv", true);

    // Tests naiveNeighbors()
    Star sol = starDataset.getStarByName("Sol");
    List<Star> result1 = starDataset.naiveNeighbors(5, sol);
    assertEquals(70667, result1.get(0).getStarId());
    assertEquals(71454, result1.get(1).getStarId());
    assertEquals(71457, result1.get(2).getStarId());
    assertEquals(87666, result1.get(3).getStarId());
    assertEquals(118721, result1.get(4).getStarId());
    List<Star> result2 = starDataset.naiveNeighbors(5, 0, 0, 0);
    assertEquals(0, result2.get(0).getStarId());
    assertEquals(70667, result2.get(1).getStarId());
    assertEquals(71454, result2.get(2).getStarId());
    assertEquals(71457, result2.get(3).getStarId());
    assertEquals(87666, result2.get(4).getStarId());

    // Tests naiveRadius()
    Star proximaCentauri = starDataset.getStarByName("Proxima Centauri");
    Star andreas = starDataset.getStarByName("Andreas");
    List<Star> result3 = starDataset.naiveRadius(10, sol);
    assertTrue(result3.contains(proximaCentauri));
    assertFalse(result3.contains(andreas));
    List<Star> result4 = starDataset.naiveRadius(10, 0, 0, 0);
    assertTrue(result4.contains(proximaCentauri));
    assertFalse(result4.contains(andreas));

    // Stress test
    List<Star> result5 = starDataset.naiveNeighbors(1000, 0, 0, 0);
    assertEquals(1000, result5.size());
    assertTrue(result5.contains(proximaCentauri));
    Double lastStarDist = null;
    for (Star resultStar : result5) {
      if (lastStarDist != null) {
        assertTrue(resultStar.getDistanceFrom(0, 0, 0) >= lastStarDist);
      }
      lastStarDist = resultStar.getDistanceFrom(0, 0, 0);
    }

    // Compare naiveRadius() and radius()
    List<Star> result6 = starDataset.naiveRadius(100, andreas);
    List<Star> result7 = starDataset.radius(100, andreas);
    for (Star resultStar : result6) {
      assertTrue(result7.remove(resultStar));
    }
    assertEquals(0, result7.size());

    // Compare naiveNeighbors() and neighbors()
    List<Star> result8 = starDataset.naiveNeighbors(500, proximaCentauri);
    List<Star> result9 = starDataset.neighbors(500, proximaCentauri);
    for (Star resultStar : result8) {
      assertTrue(result9.remove(resultStar));
    }
    assertEquals(0, result9.size());
  }
}
