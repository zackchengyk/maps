package edu.brown.cs.jwu175zcheng12.kdtree;

import java.util.NoSuchElementException;

/**.
 * Implements a Generic Binary Tree.
 *
 * @param <C> the type of the data which will be stored at each node
 */
public class KDTreeNode<C> {
  private final C nodeVal;
  private final KDTreeNode<C> leftKDTreeNode, rightKDTreeNode;

  /**.
   * Creates a KDTreeNode with a Value and reference to left and Right Nodes
   * @param currNode
   *      The Value at the current node
   * @param  leftKDTreeNode
   *      The left child of the current node
   * @param  rightKDTreeNode
   *      The right child of the current node
   */
  KDTreeNode(C currNode, KDTreeNode<C> leftKDTreeNode, KDTreeNode<C> rightKDTreeNode) {
    this.nodeVal = currNode;
    this.leftKDTreeNode = leftKDTreeNode;
    this.rightKDTreeNode = rightKDTreeNode;
  }

  /**.
   * Creates a Empty KDTreeNode with no Value and no reference to left and Right Nodes
   */
  public KDTreeNode() {
    this.nodeVal = null;
    this.leftKDTreeNode = null;
    this.rightKDTreeNode = null;
  }

  /**.
   * Tells you if the current node is empty or not
   * @return if the current node is empty or not
   */
  public boolean isEmpty() {
    return this.nodeVal == null;
  }

  /**.
   * Getter function for the left node
   * @return an reference of the left node
   */
  public KDTreeNode<C> getLeftNode() {
    return this.leftKDTreeNode;
  }

  /**.
   * Getter function for the right node
   * @return an reference of the right node
   */
  public KDTreeNode<C> getRightNode() {
    return this.rightKDTreeNode;
  }

  /**.
   * Getter function for the current value at the current node
   * @return the value at the current node
   */
  public C getNodeVal() {
    if (this.isEmpty()) {
      throw new Error("ERROR: KDTreeNode is Empty");
    }
    return this.nodeVal;
  }

  /**
   * Getter function for the nodeVal of the KDTreeNode at a specific point in the
   * KDTree, given by the path in input String str.
   * <p>
   * E.g. str = "RLR" gives the root node's right child's left child's right
   * child's nodeVal.
   *
   * @param str the path string, consisting of a series of R and L characters
   * @return the value at the KDTreeNode specified by the path string
   */
  public C getLR(String str) {
    KDTreeNode<C> current = this;
    int i = 0;
    while (i < str.length() && current != null) {
      if (str.charAt(i) == 'R') {
        current = current.getRightNode();
        i++;
      } else if (str.charAt(i) == 'L') {
        current = current.getLeftNode();
        i++;
      } else {
        throw new IllegalArgumentException(
            "Input string must consist of R and L only.");
      }
    }
    if (i < str.length() || current == null) {
      throw new NoSuchElementException(
          "Leaf node passed: the requested path could not be followed.");
    }
    return current.nodeVal;
  }

  /**.
   * String Representation of the node (Entire Tree)
   * @return a string representation of the node and all of it's connections
   */
  @Override
  public String toString() {
    if (this.isEmpty()) {
      return "Empty KDTreeNode";
    }
    return "KDTreeNode{"
      + "Value at KDTreeNode = " + nodeVal
      + ", Left KDTreeNode = " + leftKDTreeNode
      + ", Right KDTreeNode = " + rightKDTreeNode
      + '}';
  }
}
