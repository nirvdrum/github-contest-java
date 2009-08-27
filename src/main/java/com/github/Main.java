package com.github;

import org.apache.log4j.Logger;

import java.util.*;
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
  final static Logger log = Logger.getLogger(Main.class);

  public static void main(String[] args) throws IOException
  {
    log.info("Loading data.");
    final Map<String, Repository> repositories = DataLoader.loadRepositories();
    final DataSet data_set = DataLoader.loadWatchings();

    // Perform cross-validation.
    final List<DataSet> folds = data_set.stratify(100000);
    for (int i = 0; i < folds.size(); i++)
    {
      log.info(String.format("Starting fold %d.", i + 1));
      final DataSet test_set = folds.remove(0);
      final DataSet training_set = DataSet.combine(folds);

      log.info("Training");
      final NearestNeighbors knn = new NearestNeighbors(training_set);

      log.info("Classifying");
      final Map<String, Map<String, Collection<Float>>> evaluations = knn.evaluate(test_set);

      final Set<Watcher> prediction = NearestNeighbors.predict(knn, evaluations, 10);
      final Set<Watcher> all_predictions = NearestNeighbors.predict(knn, evaluations, 1000);

      analyze(test_set, training_set, prediction, all_predictions, knn, evaluations);

      folds.add(test_set);
    }
  }

  private static void analyze(final DataSet test_set, final DataSet training_set, final Set<Watcher> prediction, final Set<Watcher> all_predictions, final NearestNeighbors knn, final Map<String, Map<String, Collection<Float>>> evaluations) throws IOException
  {
    int no_region_count = 0;
    int most_popular_count = 0;
    int most_forked_count = 0;
    int able_to_predict = 0;
    int total_able_to_be_predicted = 0;

    for (final Watcher test_watcher : test_set.getWatchers().values())
    {
      // Check if the test watcher is even in the traning set.
      if (training_set.getWatchers().get(test_watcher.id) == null)
      {
        log.info(String.format("No training data for watcher %s -- impossible to predict", test_watcher.id));

        continue;
      }

      total_able_to_be_predicted += test_watcher.repositories.size();

      for (final Repository test_repo : test_watcher.repositories)
      {
        // We can't predict something we don't have in the training set.
        if (knn.training_repositories.get(test_repo.id) != null)
        {
          able_to_predict++;

          // Regions only make sense with training data, not test data.
          final Repository training_repo = knn.training_repositories.get(test_repo.id);
          final NeighborRegion region = knn.find_region(training_repo);

          // Track how many "most popular" repositories the users watch.
          if (region.most_popular == training_repo)
          {
            most_popular_count++;
          }

          // Track how many "most forked" repositories the users watch.
          if (region.most_forked == training_repo)
          {
            most_forked_count++;
          }

          // Find any test repositories for the user that we did not find distances for in the evaluation phase.
          if (test_set.getWatchers().get(test_watcher.id).repositories.contains(test_repo) && (evaluations.get(test_watcher.id).get(test_repo.id) == null))
          {
            log.info(String.format("Failed to find %s:%s", test_watcher.id, test_repo.id));
          }
        }
      }
    }


    // Analyze the k predictions (i.e., the ones we'd pass on down to GitHub).
    final Map<String, Watcher> watchers_to_predictions = new HashMap<String, Watcher>();
    for (final Watcher w : prediction)
    {
      watchers_to_predictions.put(w.id, w);

      // Figure out which of the test watcher's repositories are in the training set.  We shouldn't penalize accuracy if something legitimately couldn't be found.
      final Set<Repository> available_repos = new HashSet<Repository>();

      // Analyze the predicted repository.
      for (final Repository repo : w.repositories)
      {
        // Let us know if we predicted a repository that isn't in the test watcher's data set.
        if (!test_set.getWatchers().get(w.id).repositories.contains(repo))
        {
          log.info(String.format("Bad prediction %s:%s with distance %f", w.id, repo.id, MyUtils.mean(evaluations.get(w.id).get(repo.id))));
        }

        if (training_set.getRepositories().get(repo.id) != null)
        {
          available_repos.add(repo);
        }
      }

      final Watcher normalized_watcher = new Watcher(w.id);
      normalized_watcher.repositories.addAll(available_repos);

      log.info(String.format("Accuracy for watcher %s: %f%%", w.id, NearestNeighbors.accuracy(test_set.getWatchers().get(w.id), w) * 100));
    }

    // Analyze the large n predictions (i.e., all distances we created and then picked predictions from).
    for (final Watcher w : all_predictions)
    {
      for (final Repository repo : w.repositories)
      {
        if (test_set.getWatchers().get(w.id).repositories.contains(repo) && !watchers_to_predictions.get(w.id).repositories.contains(repo))
        {
          log.info(String.format("Missing prediction %s:%s with distance %f", w.id, repo.id, MyUtils.mean(evaluations.get(w.id).get(repo.id))));
        }
      }
    }

    int has_parent_count = 0;
    int has_children_count = 0;
    int same_owner_count = 0;
    int total_repo_count = 0;
    int training_most_popular_count = 0;
    int training_most_forked_count = 0;

    // Get some relationship stats.
    for (final Watcher test_watcher : test_set.getWatchers().values())
    {
      final Watcher training_watcher = training_set.getWatchers().get(test_watcher.id);
      if (test_watcher == null)
      {
        continue;
      }

      for (final Repository training_repo : training_watcher.repositories)
      {
        if (training_repo.parent != null)
        {
          has_parent_count++;
        }

        if (!training_repo.children.isEmpty())
        {
          has_children_count++;
        }

        final NeighborRegion region = knn.find_region(training_repo);
        if (region.most_forked == training_repo)
        {
          training_most_forked_count++;
        }

        if (region.most_popular == training_repo)
        {
          training_most_popular_count++;
        }
      }

      // Figure out how many of the test watcher's repositories are owned by the same user (although, not necessarily the same user for all repos).
      total_repo_count += training_watcher.repositories.size();
      final Set<String> owners = new HashSet<String>();

      for (final Repository r : training_watcher.repositories)
      {
        owners.add(r.name);
      }

      same_owner_count += owners.size();
    }

    log.info(String.format("Training stat: Has parent ratio: %f%%", (((float) has_parent_count) / total_repo_count) * 100));
    log.info(String.format("Training stat: Has children ratio: %f%%", (((float) has_children_count) / total_repo_count) * 100));
    log.info(String.format("Training stat: Same owner ratio: %f%%", (((float) (total_repo_count - same_owner_count)) / total_repo_count) * 100));
    log.info(String.format("Training stat: Most popular: %f%%", (((float) training_most_popular_count) / total_repo_count) * 100));
    log.info(String.format("Training stat: Most forked: %f%%", (((float) training_most_forked_count) / total_repo_count) * 100));

    log.info(String.format(">>> Best possible prediction accuracy: %f%%", (((float) able_to_predict) / total_able_to_be_predicted) * 100));
    log.info(String.format(">>> Actual repo was most popular: %f%%", (((float) most_popular_count) / total_able_to_be_predicted) * 100));
    log.info(String.format(">>> Actual repo was most forked: %f%%", (((float) most_forked_count) / total_able_to_be_predicted) * 100));
  }
}
