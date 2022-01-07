package edu.brown.cs.jwu175zcheng12.cache;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * A caching class. It stores key and value objects with different sizes, making sure that the
 * cache size does not exceed a specified limit.
 * @param <K> the key type of the cache
 * @param <V> the value type of the cache
 */
public class MyCache<K, V> {

  private final Map<K, CacheObject<V>> cache;
  private final int maxCacheSize;
  private final int maxKeySize;
  private final int maxValueSize;
  private int curSize;

  /**
   * Constructor.
   * @param maxCacheSize max cache size
   * @param maxKeySize max key size
   * @param maxValueSize max value size
   */
  public MyCache(int maxCacheSize, int maxKeySize, int maxValueSize) {
    this.cache = new LinkedHashMap<>();
    this.maxCacheSize = maxCacheSize;
    this.maxKeySize = maxKeySize;
    this.maxValueSize = maxValueSize;
    this.curSize = 0;
  }

  /**
   * Method to add a key-value pair to the cache, checking the size and adjusting the cache
   * accordingly.
   * @param key the key to add
   * @param value the value which corresponds to the given key
   * @param keySize the cache size taken up by the key
   * @param valueSize the cache size taken up by the value
   * @return true if the pair was added successfully, false otherwise
   */
  public boolean addToCache(K key, V value, int keySize, int valueSize) {
    if (keySize <= this.maxKeySize && valueSize <= this.maxValueSize) {
      this.cache.put(key, new CacheObject<>(value, valueSize + keySize));
      this.curSize += keySize + valueSize;
      while (this.curSize > this.maxCacheSize) {
        this.removeOldest();
      }
      return true;
    }
    return false;
  }

  /**
   * Method used by the cache to remove the oldest key-value pair in the cache
   * and adjust the size of the cache accordingly.
   */
  private void removeOldest() {
    Map.Entry<K, CacheObject<V>> entry = cache.entrySet().iterator().next();
    this.curSize -= entry.getValue().getSize();
    this.cache.remove(entry.getKey());
  }

  /**
   * Getter method for a value given a specific key.
   * @param key the key to search for
   * @return the corresponding value of the key in the hashmap
   */
  public V get(K key) {
    return this.cache.get(key).getValue();
  }

  /**
   * Method to check if a key exists in this cache.
   * @param key the key to search for
   * @return true if the key is contained, false otherwise
   */
  public boolean containsKey(K key) {
    return this.cache.containsKey(key);
  }

  /**
   * Method used to reset the cache.
   */
  public void clearCache() {
    this.cache.clear();
  }

  /**
   * Method to get the current size of the cache.
   * @return the current size of the cache
   */
  public int getSize() {
    return this.curSize;
  }
}
