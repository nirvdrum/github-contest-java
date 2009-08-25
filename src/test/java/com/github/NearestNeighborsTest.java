package com.github;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 * User: nirvdrum
 * Date: Aug 25, 2009
 * Time: 8:14:20 AM
 * To change this template use File | Settings | File Templates.
 */
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
