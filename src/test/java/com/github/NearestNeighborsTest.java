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
import static org.testng.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

@Test
public class NearestNeighborsTest
{
  public void testNew() throws IOException
  {
    final DataSet training_set = DataLoader.loadWatchings();

    final Repository r1 = new Repository("1234", "user_a", "yo", "2009-02-26");
    final Repository r2 = new Repository("2345", "user_b", "blah", "2008-11-16");
    final Repository r3 = new Repository("6790", "user_c", "blah", "2008-10-12");

    r2.setParent(r3);

    final Watcher w1 = new Watcher("1");
    final Watcher w2 = new Watcher("2");
    final Watcher w3 = new Watcher("5");

    r1.associate(w1);
    r2.associate(w1);
    r2.associate(w2);
    r3.associate(w3);

    final NearestNeighbors knn = new NearestNeighbors(training_set);

    assertEquals(knn.training_watchers, training_set.getWatchers());
    assertEquals(knn.training_repositories, training_set.getRepositories());
    assertEquals(knn.training_regions.keySet(), new HashSet<String>(Arrays.asList("1234", "6790")));
  }
}
