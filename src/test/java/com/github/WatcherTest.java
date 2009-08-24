package com.github;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import static org.testng.Assert.*;

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: nirvdrum
 * Date: Aug 24, 2009
 * Time: 7:29:01 AM
 * To change this template use File | Settings | File Templates.
 */
@Test
public class WatcherTest
{
  private Watcher watcher;

  @BeforeMethod
  public void setUp()
  {
    watcher = new Watcher("1");
  }

  public void test_new()
  {
    String id = "1";

    Watcher w = new Watcher("1");

    assertEquals(w.id, id);
  }

  public void test_to_string()
  {
    assertEquals("1", watcher.toString());

    final Repository one = new Repository("1234", "user_a/yo", "2009-02-26");
    final Repository two = new Repository("2345", "user_b/blah", "2009-03-18");

    watcher.repositories.add(one);
    assertEquals("1:1234", watcher.toString());

    watcher.repositories.add(two);
    assertEquals("1:2345,1234", watcher.toString());
  }

  public void test_equality()
  {
    final Watcher first = new Watcher("1");
    final Watcher second = new Watcher("1");

    assertEquals(first, second);
    assertEquals(second, first);
    assertEquals(first.hashCode(), second.hashCode());

    final Watcher third = new Watcher("2");
    assertFalse(first.equals(third));
    assertFalse(third.equals(first));
  }

  public void test_associate()
  {
    final Repository repo = new Repository("1234", "user_a", "yo", "2009-02-26");

    watcher.associate(repo);

    // Check that bi-directional mappings are set up.
    assertEquals(Arrays.asList(repo), watcher.repositories);
    assertEquals(Arrays.asList(watcher), repo.watchers);
  }
}
