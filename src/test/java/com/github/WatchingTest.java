package com.github;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: nirvdrum
 * Date: Aug 24, 2009
 * Time: 8:41:36 PM
 * To change this template use File | Settings | File Templates.
 */
@Test
public class WatchingTest
{
  public void testEquality()
  {
    final Watching first = new Watching("1", "1234");
    final Watching second = new Watching("1", "1234");
    final Watching third = new Watching("1", "2345");

    assertEquals(first, second);
    assertEquals(second, first);
    assertEquals(first.hashCode(), second.hashCode());

    assertFalse(first.equals(third));
    assertFalse(third.equals(first));
  }

  public void testToString()
  {
    final Watching w = new Watching("1", "1234");

    assertEquals(w.toString(), "1:1234");
  }
}
