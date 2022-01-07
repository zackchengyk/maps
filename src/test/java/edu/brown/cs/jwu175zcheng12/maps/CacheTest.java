package edu.brown.cs.jwu175zcheng12.maps;

import edu.brown.cs.jwu175zcheng12.cache.MyCache;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class CacheTest {
  private MyCache<String, String> testCache;
  private static final int MAX_SIZE = 50;
  private static final int KEY_SIZE = 10;
  private static final int VALUE_SIZE = 20;

  @Before
  public void setUp() {
    testCache = new MyCache<>(MAX_SIZE, KEY_SIZE, VALUE_SIZE);
  }

  @After
  public void takeDown() {
    testCache = null;
  }

  @Test
  public void testCache() {
    setUp();
    assertTrue(testCache.addToCache("1234567890", "abcdefghij", 10, 10));
    assertEquals(testCache.getSize(), 20);
    assertFalse(testCache.addToCache("test", "sizeOver20", 10, 21));
    assertEquals(testCache.getSize(), 20);
    assertTrue(testCache.addToCache("a", "1", 10, 20));
    assertEquals(testCache.getSize(), 50);
    assertTrue(testCache.containsKey("1234567890"));
    assertTrue(testCache.addToCache("b", "2", 1, 1));
    assertEquals(testCache.getSize(), 32);
    assertFalse(testCache.containsKey("1234567890"));
    assertEquals(testCache.get("b"), "2");
    assertEquals(testCache.get("a"), "1");
    takeDown();
  }
}
