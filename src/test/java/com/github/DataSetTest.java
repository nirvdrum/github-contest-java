package com.github;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.util.*;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: nirvdrum
 * Date: Aug 24, 2009
 * Time: 9:16:50 PM
 * To change this template use File | Settings | File Templates.
 */
@Test
public class DataSetTest
{
  public void testEquality()
  {
    final DataSet first = new DataSet();
    first.add(new Watching("1", "1234"));
    first.add(new Watching("2", "2345"));

    final DataSet second = new DataSet();
    second.add(new Watching("1", "1234"));
    second.add(new Watching("2", "2345"));

    final DataSet third = new DataSet();
    third.add(new Watching("1", "1234"));

    assertEquals(first, second);
    assertEquals(second, first);
    assertEquals(first.hashCode(), second.hashCode());

    assertFalse(first.equals(third));
    assertFalse(third.equals(first));
  }

  public void testStratify()
  {
    final DataSet data_set = new DataSet();
    data_set.add(new Watching("a", "1"));
    data_set.add(new Watching("a", "2"));
    data_set.add(new Watching("d", "3"));
    data_set.add(new Watching("e", "4"));
    data_set.add(new Watching("f", "5"));
    data_set.add(new Watching("b", "1"));
    data_set.add(new Watching("q", "6"));
    data_set.add(new Watching("r", "7"));
    data_set.add(new Watching("c", "1"));

    final DataSet expected_first_fold = new DataSet();
    expected_first_fold.add(new Watching("c", "1"));
    expected_first_fold.add(new Watching("a", "2"));
    expected_first_fold.add(new Watching("f", "5"));

    final DataSet expected_second_fold = new DataSet();
    expected_second_fold.add(new Watching("b", "1"));
    expected_second_fold.add(new Watching("d", "3"));
    expected_second_fold.add(new Watching("q", "6"));

    final DataSet expected_third_fold = new DataSet();
    expected_third_fold.add(new Watching("a", "1"));
    expected_third_fold.add(new Watching("e", "4"));
    expected_third_fold.add(new Watching("r", "7"));

    final List<DataSet> folds = data_set.stratify(3);

    assertEquals(folds, Arrays.asList(expected_first_fold, expected_second_fold, expected_third_fold));
  }

  public void testCombine()
  {
    final DataSet data_set = new DataSet();
    data_set.add(new Watching("a", "1"));
    data_set.add(new Watching("a", "2"));
    data_set.add(new Watching("d", "3"));
    data_set.add(new Watching("e", "4"));
    data_set.add(new Watching("f", "5"));
    data_set.add(new Watching("b", "1"));
    data_set.add(new Watching("q", "6"));
    data_set.add(new Watching("r", "7"));
    data_set.add(new Watching("c", "1"));

    final List<DataSet> folds = data_set.stratify(3);

    assertEquals(data_set, DataSet.combine(folds));
  }

  public void test_get_watchers() throws IOException
  {
    final DataSet data_set = DataLoader.loadWatchings();

    final Watcher w1 = new Watcher("1");
    final Watcher w2 = new Watcher("2");
    final Watcher w3 = new Watcher("5");

    final Repository r1 = new Repository("1234", "user_a", "blah", "2009-02-26");
    final Repository r2 = new Repository("2345", "user_b", "yo", "2009-05-17");
    final Repository r3 = new Repository("6790", "user_c", "yo", "2009-03-19");

    r2.setParent(r3);

    w1.associate(r1);
    w1.associate(r2);
    w2.associate(r2);
    w3.associate(r3);

    final Map<String, Watcher> expected = new HashMap<String, Watcher>();
    expected.put("1", w1);
    expected.put("2", w2);
    expected.put("5", w3);

    assertEquals(data_set.getWatchers(), expected);
  }

  public void test_get_repositories() throws IOException
  {
    final DataSet data_set = DataLoader.loadWatchings();

    final Watcher w1 = new Watcher("1");
    final Watcher w2 = new Watcher("2");
    final Watcher w3 = new Watcher("5");

    final Repository r1 = new Repository("1234", "user_a", "blah", "2009-02-26");
    final Repository r2 = new Repository("2345", "user_b", "yo", "2009-05-17");
    final Repository r3 = new Repository("6790", "user_c", "yo", "2009-03-19");

    r2.setParent(r3);

    w1.associate(r1);
    w1.associate(r2);
    w2.associate(r2);
    w3.associate(r3);

    final Map<String, Repository> expected = new HashMap<String, Repository>();
    expected.put("1234", r1);
    expected.put("2345", r2);
    expected.put("6790", r3);

    assertEquals(data_set.getRepositories(), expected);
  }
}
