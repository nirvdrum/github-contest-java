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

import static org.testng.Assert.*;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;

@Test
public class NeighborRegionTest
{
  public void testRegionId()
  {
    final Repository parent = new Repository("1234", "user_a", "yo", "2009-02-26");
    final Repository child = new Repository("2345", "user_b", "yo", "2009-02-27");

    child.setParent(parent);

    final NeighborRegion parentRegion = new NeighborRegion(parent);
    assertEquals(parentRegion.id, parent.id);

    // A region is always identified by the repository root.
    final NeighborRegion childRegion = new NeighborRegion(child);
    assertEquals(childRegion.id, parent.id);
  }

  public void testRepositoriesWhenCreatedWithDescendant()
  {
    final Repository parent = new Repository("1234", "user_a", "yo", "2009-02-26");
    final Repository child = new Repository("2345", "user_b", "yo", "2009-02-27");
    final Repository grandchild = new Repository("6790", "user_c", "yo", "2009-03-10");

    child.setParent(parent);
    grandchild.setParent(child);

    final NeighborRegion region = new NeighborRegion(grandchild);
    assertEquals(region.repositories, new HashSet<Repository>(Arrays.asList(grandchild, parent)));
  }

  public void testWatchers()
  {
    final Repository r1 = new Repository("1234", "user_a", "yo", "2009-02-26");
    final Repository r2 = new Repository("2345", "user_b", "yo", "2009-02-27");

    r2.setParent(r1);

    final Watcher w1 = new Watcher("1");
    final Watcher w2 = new Watcher("2");
    final Watcher w3 = new Watcher("5");

    r1.associate(w1);
    r2.associate(w2);
    r2.associate(w3);

    final NeighborRegion region = new NeighborRegion(r2);
    assertEquals(region.watchers, new HashSet<Watcher>(Arrays.asList(w1, w2, w3)));

    // Adding a new repository to the region should update the watchers.
    final Repository r3 = new Repository("6790", "user_c", "yo", "2009-03-12");
    final Watcher w4 = new Watcher("7");
    r3.associate(w4);

    region.add(r3);
    assertEquals(region.watchers, new HashSet<Watcher>(Arrays.asList(w1, w2, w3, w4)));
  }

  public void testMostPopular()
  {
    final Repository r1 = new Repository("1234", "user_a", "yo", "2009-02-26");
    final Repository r2 = new Repository("2345", "user_b", "yo", "2009-02-27");

    r2.setParent(r1);

    final Watcher w1 = new Watcher("1");
    final Watcher w2 = new Watcher("2");
    final Watcher w3 = new Watcher("5");

    r1.associate(w1);
    r2.associate(w2);
    r2.associate(w3);

    final NeighborRegion region = new NeighborRegion(r2);
    assertEquals(region.most_popular, r2);
  }

  public void testMostForked()
  {
    final Repository r1 = new Repository("1234", "user_a", "yo", "2009-02-26");
    final Repository r2 = new Repository("2345", "user_b", "yo", "2009-02-27");
    final Repository r3 = new Repository("6790", "user_c", "yo", "2009-03-12");
    final Repository r4 = new Repository("8324", "user_d", "yo", "2009-04-07");

    r2.setParent(r1);
    r2.setParent(r2);
    r4.setParent(r2);

    final NeighborRegion region = new NeighborRegion(r1);
    region.add(r2);
    region.add(r3);
    region.add(r4);

    assertEquals(region.most_forked, r2);
  }

  public void testCutPointCount()
  {
    final Repository r1 = new Repository("1234", "user_a", "yo", "2009-02-26");
    final Repository r2 = new Repository("2345", "user_b", "yo", "2009-02-27");

    final Watcher w1 = new Watcher("1");
    final Watcher w2 = new Watcher("2");
    final Watcher w3 = new Watcher("5");
    final Watcher w4 = new Watcher("7");

    r1.associate(w1);
    r1.associate(w2);
    r1.associate(w3);

    r2.associate(w2);
    r2.associate(w3);
    r2.associate(w4);

    // The two cut points are w2 and w3.  The relationship should be symmetric.

    final NeighborRegion first = new NeighborRegion(r1);
    final NeighborRegion second = new NeighborRegion(r2);

    assertEquals(first.cut_point_count(second), 2);
    assertEquals(second.cut_point_count(first), 2);
  }

  public void testAddAssociatesRegionWithRepository()
  {
    final Repository r1 = new Repository("1234", "user_a", "yo", "2009-02-26");
    final NeighborRegion region = new NeighborRegion(r1);

    assertEquals(region, r1.region);

    final Repository r2 = new Repository("2345", "user_b", "yo", "2009-02-27");
    region.add(r2);

    assertEquals(region, r2.region);
  }
}
