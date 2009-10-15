/**
 * Copyright 2009 Kevin J. Menard Jr.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.github;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import static org.testng.Assert.*;

import java.util.Arrays;

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
    assertTrue(watcher.toString().equals("1:2345,1234") || watcher.toString().equals("1:1234,2345"));
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

  public void test_owner_distributions()
  {
    // Only one repo, so distribution should be 1.0.
    final Repository r1 = new Repository("1234", "user_a", "yo", "2009-02-26");
    watcher.associate(r1);
    assertEquals(1.0f, watcher.owner_distribution(r1.owner));

    // Add in another repo by another owner.  Both should now be evenly distributed.
    final Repository r2 = new Repository("2345", "user_b", "hey", "2009-03-31");
    watcher.associate(r2);
    assertEquals(0.5f, watcher.owner_distribution(r1.owner));
    assertEquals(0.5f, watcher.owner_distribution(r2.owner));

    // An owner that isn't in the set should always be 0.0
    assertEquals(0.0f, watcher.owner_distribution("oarceihlaoei"));
  }
}
