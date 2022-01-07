package edu.brown.cs.jwu175zcheng12.stars;

import edu.brown.cs.jwu175zcheng12.csvdataset.StarDataset;
import edu.brown.cs.jwu175zcheng12.kdtree.KDTree;
import edu.brown.cs.jwu175zcheng12.kdtree.KDTreeNode;
import edu.brown.cs.jwu175zcheng12.kdtree.ObjectInNDSpace;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

public class KDTreeTest {

  private static class oneDimCoords extends ObjectInNDSpace {
    double[] coordinates;
    public oneDimCoords(double x) {
      super(new Double[]{x});
    }
  }

  private KDTree<Star> emptyStarsKDTree;
  private KDTree<oneDimCoords> emptyOneDimKDTree;
  private KDTree<Star> tenStarKDTree;
  private KDTree<Star> threeStarKDTree;
  private KDTree<Star> tieStarKDTree;
  private KDTree<Star> radStarKDTree;
  private KDTree<Star> oneStarKDTree;
  private KDTree<oneDimCoords> oneDimCoordsTree;
  /**
   * Sets up the KDTrees
   */
  @Before
  public void setUp() {
    // StarDatasets
    StarDataset threeStarDataset = new StarDataset();
    threeStarDataset.loadData("data/stars/three-star.csv", true);
    StarDataset tenStarDataset = new StarDataset();
    tenStarDataset.loadData("data/stars/ten-star.csv", true);
    StarDataset tieStarDataset = new StarDataset();
    tieStarDataset.loadData("data/stars/tie-star.csv", true);
    StarDataset oneStarDataset = new StarDataset();
    oneStarDataset.loadData("data/stars/one-star.csv", true);
    StarDataset radStarDataset = new StarDataset();
    radStarDataset.loadData("data/stars/stars-radius-13-from-origin.csv", true);

    //KDTrees
    emptyStarsKDTree = new KDTree<>(3);
    emptyOneDimKDTree = new KDTree<>(1);
    threeStarKDTree = new KDTree<>(3, new ArrayList<>(threeStarDataset.getAllData()));
    tenStarKDTree = new KDTree<>(3, new ArrayList<>(tenStarDataset.getAllData()));
    tieStarKDTree = new KDTree<>(3, new ArrayList<>(tieStarDataset.getAllData()));
    oneStarKDTree = new KDTree<>(3, new ArrayList<>(oneStarDataset.getAllData()));
    radStarKDTree = new KDTree<>(3, new ArrayList<>(radStarDataset.getAllData()));
    List<oneDimCoords> oneDimList = new ArrayList<>(List.of(new oneDimCoords(2), new oneDimCoords(0), new oneDimCoords(-1)));
    oneDimCoordsTree = new KDTree<>(1, oneDimList);
  }

  /**
   * Resets the KDTRees
   */
  @After
  public void tearDown() {
    emptyStarsKDTree = null;
    emptyOneDimKDTree = null;
    threeStarKDTree = null;
    tenStarKDTree = null;
    tieStarKDTree = null;
    oneStarKDTree = null;
    radStarKDTree = null;
    oneDimCoordsTree = null;
  }

  @Test
  public void constructorTests() {
    setUp();
    Error negDim = assertThrows(
      Error.class,
      () -> new KDTree<Star>(-3));
    assertTrue(negDim.getMessage().contains("KDTrees can only have positive dimensions"));
    Error negListDim = assertThrows(
      Error.class,
      () -> new KDTree<Star>(-3, new ArrayList<>()));
    assertTrue(negListDim.getMessage().contains("KDTrees can only have positive dimensions"));
    Error zeroDim = assertThrows(
      Error.class,
      () -> new KDTree<Star>(0));
    assertTrue(zeroDim.getMessage().contains("KDTrees can only have positive dimensions"));
    Error zeroListDim = assertThrows(
      Error.class,
      () -> new KDTree<Star>(0, new ArrayList<>()));
    assertTrue(zeroListDim.getMessage().contains("KDTrees can only have positive dimensions"));

    // Both Constructors can be used to create same output
    assertEquals(
      emptyStarsKDTree.getTree().isEmpty(),
      new KDTree<Star>(3, new ArrayList<>()).getTree().isEmpty());
    tearDown();
  }

  @Test
  public void getterAndToStringTests() {
    setUp();
    // Get Tree [More Extensive testing to be done in the Build Tree Tests]
    assertEquals(emptyStarsKDTree.getTree().isEmpty(), new KDTreeNode<>().isEmpty());
    assertEquals(emptyOneDimKDTree.getTree().isEmpty(), new KDTreeNode<>().isEmpty());
    assertEquals(threeStarKDTree.getTree().getNodeVal(), new Star(2, "Star Two", 2, 0, 0));
    assertEquals(
      threeStarKDTree.getTree().getLeftNode().getNodeVal(),
      new Star(1, "Star One", 1, 0, 0));
    assertEquals(
      threeStarKDTree.getTree().getRightNode().getNodeVal(),
      new Star(3, "Star Three", 3, 0, 0));

    // Get Dimension
    assertEquals(emptyStarsKDTree.getMaxDim(), 3);
    assertEquals(emptyOneDimKDTree.getMaxDim(), 1);
    assertEquals(tenStarKDTree.getMaxDim(), 3);
    assertEquals(tieStarKDTree.getMaxDim(), 3);
    assertEquals(radStarKDTree.getMaxDim(), 3);
    assertEquals(oneStarKDTree.getMaxDim(), 3);
    assertEquals(oneDimCoordsTree.getMaxDim(), 1);

    // To String Tests
    assertEquals(emptyStarsKDTree.toString(), "KDTree of 3 dimensions, {Tree: Empty KDTreeNode}");
    assertEquals(emptyOneDimKDTree.toString(), "KDTree of 1 dimensions, {Tree: Empty KDTreeNode}");
    assertEquals(
      tenStarKDTree.toString(),
      "KDTree of 3 dimensions, "
        + "{Tree: KDTreeNode{"
        + "Value at KDTreeNode = Star{starId=0, properName=\"Sol\", coordinates=[0.0, 0.0, 0.0]}, "
        + "Left KDTreeNode = KDTreeNode{"
        + "Value at KDTreeNode = Star{starId=71454, properName=\"Rigel Kentaurus B\", coordinates=[-0.50359, -0.42128, -1.1767]}, "
        + "Left KDTreeNode = KDTreeNode{"
        + "Value at KDTreeNode = Star{starId=87666, properName=\"Barnard's Star\", coordinates=[-0.01729, -1.81533, 0.14824]}, "
        + "Left KDTreeNode = KDTreeNode{"
        + "Value at KDTreeNode = Star{starId=71457, properName=\"Rigel Kentaurus A\", coordinates=[-0.50362, -0.42139, -1.17665]}, "
        + "Left KDTreeNode = Empty KDTreeNode, "
        + "Right KDTreeNode = Empty KDTreeNode}, "
        + "Right KDTreeNode = Empty KDTreeNode}, "
        + "Right KDTreeNode = KDTreeNode{"
        + "Value at KDTreeNode = Star{starId=118721, properName=\"\", coordinates=[-2.28262, 0.64697, 0.29354]}, "
        + "Left KDTreeNode = KDTreeNode{"
        + "Value at KDTreeNode = Star{starId=70667, properName=\"Proxima Centauri\", coordinates=[-0.47175, -0.36132, -1.15037]}, "
        + "Left KDTreeNode = Empty KDTreeNode, "
        + "Right KDTreeNode = Empty KDTreeNode}, "
        + "Right KDTreeNode = Empty KDTreeNode}}, "
        + "Right KDTreeNode = KDTreeNode{"
        + "Value at KDTreeNode = Star{starId=3, properName=\"\", coordinates=[277.11358, 0.02422, 223.27753]}, "
        + "Left KDTreeNode = KDTreeNode{"
        + "Value at KDTreeNode = Star{starId=1, properName=\"\", coordinates=[282.43485, 0.00449, 5.36884]}, "
        + "Left KDTreeNode = KDTreeNode{"
        + "Value at KDTreeNode = Star{starId=2, properName=\"\", coordinates=[43.04329, 0.00285, -15.24144]}, "
        + "Left KDTreeNode = Empty KDTreeNode, "
        + "Right KDTreeNode = Empty KDTreeNode}, "
        + "Right KDTreeNode = Empty KDTreeNode}, "
        + "Right KDTreeNode = KDTreeNode{"
        + "Value at KDTreeNode = Star{starId=3759, properName=\"96 G. Psc\", coordinates=[7.26388, 1.55643, 0.68697]}, "
        + "Left KDTreeNode = Empty KDTreeNode, Right KDTreeNode = Empty KDTreeNode}}}}");
    tearDown();
  }

  @Test
  public void enforceDimensionsTest() {
    setUp();
    Star s1 = new Star(1, "Star One",  7, 10, -4);
    Star s2 = new Star(2, "Star Two",  -7, 0, 0);
    Star s3 = new Star(3, "Star Three",6, 0, 4);
    Star s4 = new Star(4, "Star Four", 9, 7, -10);
    Star s5 = new Star(5, "Star Five", 4, -4, -1);
    Star s6 = new Star(6, "Star Six",  -7, 7, 9);
    List<Star> randomStars = List.of(s1,s2,s3,s4,s5,s6);
    Error moreDim = assertThrows(
      Error.class,
      () -> new KDTree<Star>(4).enforceDimensions(randomStars));
    assertTrue(moreDim.getMessage().contains("Dimension Mismatch"));
    Error lessDim = assertThrows(
      Error.class,
      () -> new KDTree<Star>(2).enforceDimensions(randomStars));
    assertTrue(lessDim.getMessage().contains("Dimension Mismatch"));
    tearDown();
  }

  @Test
  public void KDBuilderTests() {
    setUp();
    // Empty Case
    assertTrue(tenStarKDTree.buildKDTree(new ArrayList<>()).isEmpty());

    // 6 Stars with coordinate integers from -10 to 10
    // Random Tree is still pretty balanced
    Star s1 = new Star(1, "Star One",  7, 10, -4);
    Star s2 = new Star(2, "Star Two",  -7, 0, 0);
    Star s3 = new Star(3, "Star Three",6, 0, 4);
    Star s4 = new Star(4, "Star Four", 9, 7, -10);
    Star s5 = new Star(5, "Star Five", 4, -4, -1);
    Star s6 = new Star(6, "Star Six",  -7, 7, 9);
    List<Star> randomStars = new ArrayList<>(List.of(s1,s2,s3,s4,s5,s6));
    emptyStarsKDTree.buildKDTree(randomStars);
    assertEquals(emptyStarsKDTree.getTree().getNodeVal(), s3);
    assertEquals(emptyStarsKDTree.getTree().getLeftNode().getNodeVal(), s2);
    assertEquals(emptyStarsKDTree.getTree().getLeftNode().getLeftNode().getNodeVal(), s5);
    assertEquals(emptyStarsKDTree.getTree().getLeftNode().getRightNode().getNodeVal(), s6);
    assertEquals(emptyStarsKDTree.getTree().getRightNode().getNodeVal(), s1);
    assertEquals(emptyStarsKDTree.getTree().getRightNode().getLeftNode().getNodeVal(), s4);


    // All nonempty KDTrees were constructed using this building method
    assertEquals(
      oneStarKDTree.getTree().getNodeVal(),
      new Star(1,"Lonely Star",5,-2.24,10.04));
    assertEquals(
      threeStarKDTree.getTree().getNodeVal(),
      new Star(2,"Star Two",2,0,0));
    assertEquals(
      threeStarKDTree.getTree().getLeftNode().getNodeVal(),
      new Star(1,"Star One",1,0,0));
    assertEquals(
      threeStarKDTree.getTree().getRightNode().getNodeVal(),
      new Star(3,"Star Three",3,0,0));

    // Enforce dimensions still fails correctly
    Error moreDim = assertThrows(
      Error.class,
      () -> new KDTree<Star>(4).buildKDTree(randomStars));
    assertTrue(moreDim.getMessage().contains("Dimension Mismatch"));
    Error lessDim = assertThrows(
      Error.class,
      () -> new KDTree<Star>(2).buildKDTree(randomStars));
    assertTrue(lessDim.getMessage().contains("Dimension Mismatch"));
    tearDown();
  }

  @Test
  public void KNearestNeighborsTests() {
    setUp();

    // Enforce dimensions still fails correctly
    Error moreDim = assertThrows(
      Error.class,
      () -> new KDTree<Star>(4).findKNearestNeighbors(5, new Double[]{1.0, 2.0, 3.0}));
    assertTrue(moreDim.getMessage().contains("Mismatched Dimension"));
    Error lessDim = assertThrows(
      Error.class,
      () -> new KDTree<Star>(2).findKNearestNeighbors(5, new Double[]{1.0, 2.0, 3.0}));
    assertTrue(lessDim.getMessage().contains("Mismatched Dimension"));

    // Throw error when K is negative
    Error negK = assertThrows(
      Error.class,
      () -> new KDTree<Star>(3).findKNearestNeighbors(-1, new Double[]{1.0, 2.0, 3.0}));
    assertTrue(negK.getMessage().contains("Cannot find a negative number of Neighbors"));

    // K is zero returns empty list
    assertEquals(tenStarKDTree.findKNearestNeighbors(0, new Double[]{1.0, 2.0, 3.0}), new ArrayList<>());
    assertEquals(tieStarKDTree.findKNearestNeighbors(0, new Double[]{1.0, 2.0, 3.0}), new ArrayList<>());

    // K greater than the space returns every neighbor
    List<Star> threeStarSolution = new ArrayList<>();
    threeStarSolution.add(new Star(1,"Star One",1,0,0));
    threeStarSolution.add(new Star(2,"Star Two",2,0,0));
    threeStarSolution.add(new Star(3,"Star Three",3,0,0));
    assertEquals(threeStarKDTree.findKNearestNeighbors(4, new Double[]{0.0, 0.0, 0.0}), threeStarSolution);

    // Partial K chooses the k closest neighbors
    List<Star> tenStarSolution = new ArrayList<>();
    tenStarSolution.add(new Star(0, "Sol", 0.0, 0.0, 0.0));
    tenStarSolution.add(new Star(70667, "Proxima Centauri", -0.47175, -0.36132, -1.15037));
    tenStarSolution.add(new Star(71454, "Rigel Kentaurus B", -0.50359, -0.42128, -1.1767));
    tenStarSolution.add(new Star(71457, "Rigel Kentaurus A", -0.50362, -0.42139, -1.17665));
    tenStarSolution.add(new Star(87666, "Barnard's Star", -0.01729, -1.81533, 0.14824));
    tenStarSolution.add(new Star(118721, "", -2.28262, 0.64697, 0.29354));
    tenStarSolution.add(new Star(3759, "96 G. Psc", 7.26388, 1.55643, 0.68697));
    tenStarSolution.add(new Star(2, "", 43.04329, 0.00285, -15.24144));

    assertEquals(tenStarKDTree.findKNearestNeighbors(8, new Double[]{0.0, 0.0, 0.0}), tenStarSolution);

    tearDown();
  }

  @Test
  public void radiusSearchTests() {
    setUp();

    // Enforce dimensions still fails correctly
    Error moreDimErr = assertThrows(
      Error.class,
      () -> new KDTree<Star>(4).findRadiusSearch(5.0, new Double[]{1.0, 2.0, 3.0}));
    assertTrue(moreDimErr.getMessage().contains("Mismatched Dimension"));
    Error lessDimErr = assertThrows(
      Error.class,
      () -> new KDTree<Star>(2).findRadiusSearch(2.4, new Double[]{1.0, 2.0, 3.0}));
    assertTrue(lessDimErr.getMessage().contains("Mismatched Dimension"));

    // Throw error when R is negative
    Error negKErr = assertThrows(
      Error.class,
      () -> new KDTree<Star>(3).findRadiusSearch(-1.0, new Double[]{1.0, 2.0, 3.0}));
    assertTrue(negKErr.getMessage().contains("Radius must be non negative"));

    // No Stars within radius
    assertEquals(tenStarKDTree.findRadiusSearch(0.0, new Double[]{1.0, 2.0, 3.0}), new ArrayList<>());
    assertEquals(threeStarKDTree.findRadiusSearch(0.0, new Double[]{1.0, 2.0, 3.0}), new ArrayList<>());

    // Radius of 0 still can contain stars
    List<Star> solList = new ArrayList<>();
    solList.add(new Star(0,"Sol",0,0,0));
    assertEquals(tenStarKDTree.findRadiusSearch(0.0, new Double[]{0.0, 0.0, 0.0}), solList);

    // Radius Big Enough to return every neighbor
    List<Star> threeStarRad = new ArrayList<>();
    threeStarRad.add(new Star(1,"Star One",1,0,0));
    threeStarRad.add(new Star(2,"Star Two",2,0,0));
    threeStarRad.add(new Star(3,"Star Three",3,0,0));
    assertEquals(threeStarKDTree.findRadiusSearch(100.0, new Double[]{0.0, 0.0, 0.0}), threeStarRad);

    // Radius Small Enough only includes some neighbors [Including on border] [Ordered based on distance]
    List<Star> tenStarRad = new ArrayList<>();
    tenStarRad.add(new Star(0, "Sol",0.0, 0.0, 0.0));
    tenStarRad.add(new Star(70667, "Proxima Centauri", -0.47175, -0.36132, -1.15037));
    tenStarRad.add(new Star(71454, "Rigel Kentaurus B", -0.50359, -0.42128, -1.1767));
    tenStarRad.add(new Star(71457, "Rigel Kentaurus A", -0.50362, -0.42139, -1.17665));
    tenStarRad.add(new Star(87666, "Barnard's Star", -0.01729, -1.81533, 0.14824));
    tenStarRad.add(new Star(118721, "", -2.28262, 0.64697, 0.29354));
    tenStarRad.add(new Star(3759, "96 G. Psc", 7.26388, 1.55643, 0.68697));
    assertEquals(tenStarKDTree.findRadiusSearch(30.0, new Double[]{0.0, 0.0, 0.0}), tenStarRad);

    // Radius Ties are all included
    List<Star> tieStarSolution = new ArrayList<>();
    tieStarSolution.add(new Star(6, "Sweetie Pie", 0.0, 0.0, 1.0));
    tieStarSolution.add(new Star(4, "Lulu", 0.0, 0.0, 1.0));
    tieStarSolution.add(new Star(5, "Harvey", 0.0, 0.0, 1.0));
    assertEquals(tieStarKDTree.findRadiusSearch(3.0, new Double[]{1.0, 2.0, 3.0}), tieStarSolution);
    tearDown();
  }
}
