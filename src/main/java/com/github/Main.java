package com.github;

import org.apache.log4j.Logger;

import java.util.Map;
import java.util.List;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: nirvdrum
 * Date: Aug 24, 2009
 * Time: 7:34:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class Main
{
  public static void main(String[] args) throws IOException
  {
    final Logger log = Logger.getLogger(Main.class);

    log.info("Loading data.");
    final Map<String, Repository> repositories = DataLoader.loadRepositories();
    final DataSet data_set = DataLoader.loadWatchings();

    // Perform cross-validation.
    final List<DataSet> folds = data_set.stratify(10);
    for (int i = 0; i < folds.size(); i++)
    {
      final DataSet test_set = folds.remove(0);
      final DataSet training_set = DataSet.combine(folds);

      log.info(String.format("Starting fold %d.", i + 1));
      log.info("Training");

      final NearestNeighbors knn = new NearestNeighbors(training_set);

      /*
      for (final Map.Entry<String, Watcher> mapping : test_set.getWatchers().entrySet())
      {
        System.out.println(mapping.getKey() + ":" + mapping.getValue());
      }

      for (final Map.Entry<String, Repository> mapping : test_set.getRepositories().entrySet())
      {
        System.out.println(mapping.getKey() + ":" + mapping.getValue());
      }

      for (final Map.Entry<String, Watcher> mapping : training_set.getWatchers().entrySet())
      {
        System.out.println(mapping.getKey() + ":" + mapping.getValue());
      }

      for (final Map.Entry<String, Repository> mapping : training_set.getRepositories().entrySet())
      {
        System.out.println(mapping.getKey() + ":" + mapping.getValue());
      }
      */

      folds.add(test_set);
    }
  }
}
