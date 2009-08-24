package com.github;

import static org.testng.Assert.*;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
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

  /*
   def test_load_watchings
    expected_data_labels = ["user_id", "repo_id"]

    expected_data_items = [
            ["1", "1234"],
            ["2", "2345"],
            ["5", "6790"],
            ["1", "2345"]
    ]

    expected_data_set = Ai4r::Data::DataSet.new(:data_labels => expected_data_labels, :data_items => expected_data_items)

    data_set = DataLoader.load_watchings

    assert_equal expected_data_labels, data_set.data_labels
    assert_equal expected_data_items, data_set.data_items
  end

  def test_load_repos

    a = Repository.new "1234", "user_a/blah", "2009-02-26"
    b = Repository.new "2345", "user_b/yo", "2009-05-17"
    c = Repository.new "6790", "user_c/yo", "2009-03-19"
    d = Repository.new "8324", "user_d/hmm", "2009-04-16"

    b.parent = c

    w1 = Watcher.new("1")
    a.watchers << w1
    b.watchers << Watcher.new("2")
    b.watchers << w1
    c.watchers << Watcher.new("5")

    expected = {
            "1234" => a,
            "2345" => b,
            "6790" => c,
            "8324" => d
    }

    assert_equal expected, DataLoader.load_repositories
  end

  def test_load_predicting

    expected_data_labels = ["user_id"]
    expected_data_items = [["1"], ["5"]]

    data_set = DataLoader.load_predictings

    assert_equal expected_data_labels, data_set.data_labels
    assert_equal expected_data_items, data_set.data_items
  end
   */
}
