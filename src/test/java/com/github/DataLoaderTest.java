package com.github;

import static org.testng.Assert.*;
import org.testng.annotations.Test;

import java.util.*;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: nirvdrum
 * Date: Aug 24, 2009
 * Time: 6:17:49 PM
 * To change this template use File | Settings | File Templates.
 */
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

  /*
  def test_load_predicting

    expected_data_labels = ["user_id"]
    expected_data_items = [["1"], ["5"]]

    data_set = DataLoader.load_predictings

    assert_equal expected_data_labels, data_set.data_labels
    assert_equal expected_data_items, data_set.data_items
  end
   */
}
