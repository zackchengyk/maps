package edu.brown.cs.jwu175zcheng12.cache;

/**
 * Object to keep track of the size and value of an item in a cache.
 * @param <V> the type of value in the cache
 */
public class CacheObject<V> {

  private final V value;
  private final int size;

  /**
   * Constructor.
   * @param value value to hold
   * @param size size
   */
  public CacheObject(V value, int size) {
    this.value = value;
    this.size = size;
  }

  /**
   * Getter method to get the value of this cache object.
   * @return the value contained in this cache object
   */
  public V getValue() {
    return this.value;
  }

  /**
   * Getter method to get the size of this cache object.
   * @return the size of this cache object
   */
  public int getSize() {
    return this.size;
  }

}
