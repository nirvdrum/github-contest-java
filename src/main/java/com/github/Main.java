package com.github;

import java.util.Map;
import java.util.Set;
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
    final Map<String, Repository> repositories = DataLoader.loadRepositories();


    
    final DataSet data_set = DataLoader.loadWatchings();

    // Perform cross-validation.
    final List<DataSet> folds = data_set.stratify(10);
    for (int i = 0; i < folds.size(); i++)
    {
      final DataSet test_set = folds.remove(0);
      final DataSet training_set = DataSet.combine(folds);


      //final Map<Map<String, Watcher>, Map<String, Repository>> test_data = test_set.to_models();
      //final Map<Map<String, Watcher>, Map<String, Repository>> training_data = training_set.to_models();


      folds.add(test_set);
    }

    final DataSet training_set = folds.get(0);
    final DataSet test_set = folds.get(1);
  }
}
