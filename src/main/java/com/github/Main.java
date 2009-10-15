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

import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.io.IOException;
import java.io.FileWriter;
import java.io.BufferedWriter;

public class Main
{
  final static Logger log = Logger.getLogger(Main.class);

  public static void main(String[] args) throws IOException, ExecutionException, InterruptedException
  {
    log.info("Loading data.");
    final Map<String, Repository> repositories = DataLoader.loadRepositories();
    final DataSet data_set = DataLoader.loadWatchings();

    if (args.length > 0)
    {
      // Perform cross-validation.
      final List<DataSet> folds = data_set.stratify(100);
      for (int i = 0; i < folds.size(); i++)
      {
        log.info(String.format("Starting fold %d.", i + 1));
        final DataSet test_set = folds.remove(0);
        final DataSet training_set = DataSet.combine(folds);

        log.info("Training");
        final NearestNeighbors knn = new NearestNeighbors(training_set);

        log.info("Classifying");
        final Map<String, Map<String, Collection<Float>>> evaluations = knn.evaluate(test_set.getWatchers().values());

        final Set<Watcher> prediction = NearestNeighbors.predict(knn, evaluations, 10, test_set.getWatchers());
        final Set<Watcher> all_predictions = NearestNeighbors.predict(knn, evaluations, 1000, test_set.getWatchers());

        analyze(test_set, training_set, prediction, all_predictions, knn, evaluations);

        log.info(String.format(">>> Results for fold %d: %f%% / %f%%", i + 1, NearestNeighbors.score(test_set, prediction) * 100, NearestNeighbors.score(test_set, all_predictions) * 100));

        folds.add(test_set);

        write_predictions(prediction);
      }
    }

    else
    {

      log.info("Training.");
      final NearestNeighbors knn = new NearestNeighbors(data_set);

      log.info("Evaluating.");
      final Set<Watcher> predictings = DataLoader.loadPredictings();
      final Map<String, Map<String, Collection<Float>>> evaluations = knn.evaluate(predictings);
      final Set<Watcher> predictions = NearestNeighbors.predict(knn, evaluations, 10, null);

      final List<Map.Entry<String, NeighborRegion>> sorted_regions = MyUtils.sortRegionsByPopularity(knn.training_regions, new Comparator<Map.Entry<String, NeighborRegion>>(){

        public int compare(final Map.Entry<String, NeighborRegion> first, final Map.Entry<String, NeighborRegion> second)
        {
          int firstValue = first.getValue().watchers.size();
          int secondValue = second.getValue().watchers.size();

          if (secondValue > firstValue)
          {
            return 1;
          }
          else if (firstValue < secondValue)
          {
            return -1;
          }

          return 0;
        }
      });

      // Fill in repositories for any watchers with fewer than 10 repos.
      log.info("Filling in repositories");
      for (final Watcher w : predictions)
      {
        if (w.repositories.size() < 10)
        {
          for (final Map.Entry<String, NeighborRegion> pair : sorted_regions)
          {
            final NeighborRegion region = pair.getValue();
            if (!region.most_forked.watchers.contains(w))
            {
              w.associate(region.most_forked);
            }

            if (w.repositories.size() == 10)
            {
              break;
            }
          }
        }
      }

      log.info ("Printing results file.");
      write_predictions(predictions);
      /*

 #repos_by_popularity = []
 #sorted_regions = knn.training_regions.values.sort { |x,y| y.most_popular.watchers.size <=> x.most_popular.watchers.size }
 #repos_by_popularity = sorted_regions.collect {|x| x.most_popular.id}
 #
 #$LOG.info "Printing results file."
 #File.open('results.txt', 'w') do |file|
 #
 #  predictions.each do |watcher|
 #    # Add the ten most popular repositories that the user is not already a watcher of to his repo list if
 #    # we don't have any predictions.
 #    if watcher.repositories.empty?
 #      if knn.training_watchers[watcher.id].nil?
 #        puts "No data for watcher: #{watcher.id}"
 #        repos_by_popularity[0..10].each do |repo_id|
 #          watcher.repositories << repo_id
 #        end
 #      else
 #        added_repo_count = 0
 #        repos_by_popularity.each do |suggested_repo_id|
 #          unless knn.training_watchers[watcher.id].repositories.include?(suggested_repo_id)
 #            watcher.repositories << suggested_repo_id
 #            added_repo_count += 1
 #          end
 #
 #          break if added_repo_count == 10
 #        end
 #      end
 #    end
 #
 ##    $LOG.debug "Score (#{watcher.id}): #{NearestNeighbors.accuracy(knn.training_watchers[watcher.id], watcher)} -- #{watcher.to_s}"
 #    file.puts watcher.to_s
 #  end
 #end
      */
    }

    System.exit(0);
  }

  private static void write_predictions(final Set<Watcher> prediction) throws IOException
  {
    final FileWriter fstream = new FileWriter("results.txt");
    final BufferedWriter out = new BufferedWriter(fstream);

    for (final Iterator<Watcher> it = prediction.iterator(); it.hasNext();)
    {
      out.write(it.next().toString());

      if (it.hasNext())
      {
        out.write("\n");
      }
    }

    out.close();
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
          log.info(String.format("Bad prediction %s:%s with distance %f -- (%d watchers; %d children; has parent: %b)", w.id, repo.id, MyUtils.mean(evaluations.get(w.id).get(repo.id)), repo.watchers.size(), repo.children.size(), repo.parent == null));
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
          log.info(String.format("Missing prediction %s:%s with distance %f -- (%d watchers; %d children; has parent: %b)", w.id, repo.id, MyUtils.mean(evaluations.get(w.id).get(repo.id)), repo.watchers.size(), repo.children.size(), repo.parent == null));
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
      if (test_watcher == null || training_watcher == null)
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
