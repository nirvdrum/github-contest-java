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
import org.apache.commons.collections.CollectionUtils;

import java.util.*;
import java.io.IOException;

@Test
public class DataLoaderTest
{
  public void testLoadRepos() throws IOException
  {
    final Repository a = new Repository("1234", "user_a/blah", "2009-02-26");
    final Repository b = new Repository("2345", "user_b/yo", "2009-05-17");
    final Repository c = new Repository("6790", "user_c/yo", "2009-03-19");
    final Repository d = new Repository("8324", "user_d/hmm", "2009-04-16");

    b.setParent(c);

    final Watcher w1 = new Watcher("1");
    a.associate(w1);

    final Watcher w2 = new Watcher("2");
    b.associate(w1);
    b.associate(w2);

    final Watcher w3 = new Watcher("5");
    c.associate(w3);

    final Map<String, Repository> expected = new HashMap<String, Repository>();
    expected.put("1234", a);
    expected.put("2345", b);
    expected.put("6790", c);
    expected.put("8324", d);

    assertEquals(DataLoader.loadRepositories(), expected);
  }

  public void testLoadWatchings() throws IOException
  {
    final Watcher w1 = new Watcher("1");
    final Watcher w2 = new Watcher("2");
    final Watcher w3 = new Watcher("5");

    final Repository r1 = new Repository("1234", "user_a", "yo", "2009-02-26");
    final Repository r2 = new Repository("2345", "user_b", "hey", "2009-05-22");
    final Repository r3 = new Repository("6790", "user_c", "blah", "2009-11-13");

    final Set<Watching> expected = new HashSet<Watching>(Arrays.asList(new Watching(w1.id, r1.id), new Watching(w2.id, r2.id), new Watching(w3.id, r3.id), new Watching(w1.id, r2.id)));

    assertEquals(DataLoader.loadWatchings().watchings, expected);
  }

  public void testLoadPredictings() throws IOException
  {
    final Watcher w1 = new Watcher("1");
    final Watcher w2 = new Watcher("5");

    final Set<Watcher> expected = new HashSet<Watcher>(Arrays.asList(w1, w2));

    assertTrue(CollectionUtils.isEqualCollection(expected, DataLoader.loadPredictings()));
  }
}
