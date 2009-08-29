package com.github;

import org.apache.log4j.Logger;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;
import java.util.concurrent.*;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: nirvdrum
 * Date: Aug 25, 2009
 * Time: 8:14:02 AM
 * To change this template use File | Settings | File Templates.
 */
public class NearestNeighbors
{
  public final int TOP_COMMON_WATCHERS_COUNT = 10;
  public final int TOP_REPOS_COUNT = 100;
  public final int THREAD_POOL_SIZE = 5;

  private final Logger log = Logger.getLogger(NearestNeighbors.class);

  public final Map<String, Watcher> training_watchers;
  public final Map<String, Repository> training_repositories;
  public final Map<String, NeighborRegion> training_regions = new HashMap<String, NeighborRegion>();
  public final Map<String, Set<NeighborRegion>> watchers_to_regions = new HashMap<String, Set<NeighborRegion>>();
  public final Map<String, Set<Repository>> owners_to_repositories = new HashMap<String, Set<Repository>>();

  public NearestNeighbors(final DataSet training_set) throws IOException
  {
    log.info("knn-init: Loading watchers and repositories.");
    training_watchers = training_set.getWatchers();
    training_repositories = training_set.getRepositories();

    log.info("knn-init: Building repository regions.");
    for (final Map.Entry<String, Repository> pair : training_repositories.entrySet())
    {
      final Repository repo = pair.getValue();
      final Repository root = Repository.findRoot(repo);
      
      final NeighborRegion existing_region = training_regions.get(root.id);

      if (existing_region == null)
      {
        training_regions.put(root.id, new NeighborRegion(repo));
      }
      else
      {
        existing_region.add(repo);
      }

      // Store in inverted list structure from watcher ID to regions.
      for (final Watcher w : repo.watchers)
      {
        if (watchers_to_regions.get(w.id) == null)
        {
          watchers_to_regions.put(w.id, new HashSet<NeighborRegion>());
        }

        watchers_to_regions.get(w.id).add(training_regions.get(root.id));
      }


      // Store in inverted list structure from owenr to regions.
      if (owners_to_repositories.get(repo.owner) == null)
      {
        owners_to_repositories.put(repo.owner, new HashSet<Repository>());
      }
      owners_to_repositories.get(repo.owner).add(repo);
    }
  }

  public Map<String, Map<String, Collection<Float>>> evaluate(final DataSet test_set) throws IOException, InterruptedException, ExecutionException
  {
    log.info("knn-evaluate: Loading watchers.");
    final Map<String, Watcher> test_instances = test_set.getWatchers();

    log.debug(String.format("knn-evaluate: Total unique test watchers: %d", test_instances.size()));

    final Map<String, Map<String, Collection<Float>>> results = new HashMap<String, Map<String, Collection<Float>>>();

    // For each watcher in the test set . . .
    log.info("knn-evaluate: Starting evaluations"); 
    int test_watcher_count = 0;
    for (final Watcher watcher : test_instances.values())
    {
      test_watcher_count++;
      log.info(String.format("Processing watcher (%d/%d)", test_watcher_count, test_instances.size()));

      results.put(watcher.id, new HashMap<String, Collection<Float>>());

      // See if we have any training instances for the watcher.  If not, we really can't guess anything.
      final Watcher training_watcher = training_watchers.get(watcher.id);
      if (training_watcher == null)
      {
        continue;
      }

      /***********************************
       *** Handling repository regions ***
       ***********************************/

      // Calculate the distance between the repository regions we know the test watcher is in, to every other
      // region in the training data.
      final Set<NeighborRegion> test_regions = watchers_to_regions.get(watcher.id);

      /*
      final List<NeighborRegion> related_regions = find_regions_with_most_cutpoints(watcher, test_regions);
      for (final NeighborRegion related_region : related_regions)
      {
        storeDistance(results, watcher, related_region.most_popular, 0.0f);
        storeDistance(results, watcher, related_region.most_forked, 0.0f);
      }
      */

    /*
      also_owned_counts = {}
      training_watcher.repositories.each do |repo_id|
        repo = @training_repositories[repo_id]

        also_owned_counts[repo.owner] ||= 0
        also_owned_counts[repo.owner] += 1
      end

      also_owned_counts.each do |owner, count|
        # If 5% or more of the test watcher's repositories are owned by the same person, look at the owner's other repositories.
        if (also_owned_repos.size.to_f / training_watcher.repositories.size) > 0.05 || (also_owned_repos.size.to_f / @owners_to_repositories[owner].size) > 0.3
          repositories_to_check.merge(@owners_to_repositories[owner].collect {|r| r.id})
        end
      end
      */

      // Add in the most forked regions from similar watchers.
      /*
      final Set<NeighborRegion> related_regions = find_regions_containing_fellow_watchers(test_regions);
      for (final NeighborRegion region : related_regions)
      {
        repositories_to_check.add(region.most_forked);
      }
      */

      /*************************************
       **** Begin distance calculations ****
       *************************************/

      final ExecutorService pool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

      int test_region_count = 0;

      for (final NeighborRegion test_region : test_regions)
      {
        test_region_count++;

        final CompletionService<Map<Repository, Float>> cs = new ExecutorCompletionService<Map<Repository, Float>>(pool);
        int training_region_count = 0;


        final Set<Repository> repositories_to_check = new HashSet<Repository>();

        // Add in the most forked repositories from each region we know the test watcher is in.
        for (final NeighborRegion region : test_regions)
        {
          repositories_to_check.add(region.most_forked);
        }

        for (final Repository repo : training_watcher.repositories)
        {
          if (repo.parent != null)
          {
            repositories_to_check.add(repo.parent);
          }
        }


        /********************************************************************
         *** Handling repositories owned by owners we're already watching ***
         ********************************************************************/

        if (owners_to_repositories.get(test_region.most_forked.owner).size() > 100)
        {
          log.info("Also owned size: " + owners_to_repositories.get(test_region.most_forked.owner).size());
        }

        for (final Repository also_owned : owners_to_repositories.get(test_region.most_forked.owner))
        {
          if (also_owned.region.most_forked.equals(also_owned))
          {
            repositories_to_check.add(also_owned);
          }
        }


        for (final Repository training_repository : repositories_to_check)
        {
          training_region_count++;

          if (log.isDebugEnabled())
          {
            log.debug(String.format("Processing watcher (%d/%d) - (%d/%d):(%d/%d)", test_watcher_count, test_instances.size(), test_region_count, test_regions.size(), training_region_count, repositories_to_check.size()));
          }

          // Submit distance calculation task if the test watcher isn't already watching the repository.
          cs.submit(new Callable<Map<Repository, Float>>()
          {

            public Map<Repository, Float> call() throws Exception
            {
              final Map<Repository, Float> ret = new HashMap<Repository, Float>();

              if (!training_repository.watchers.contains(training_watcher))
              {
                float distance = euclidian_distance(training_watcher, test_region.most_forked, training_repository);

                ret.put(training_repository, Float.valueOf(distance));
              }

              return ret;
            }

          });
        }

        // Process the distance calculation results.
        for (int i = 0; i < repositories_to_check.size(); i++)
        {
          final Map<Repository, Float> distance = cs.take().get();

          for (final Map.Entry<Repository, Float> pair : distance.entrySet())
          {
            storeDistance(results, watcher, pair.getKey(), pair.getValue().floatValue());
          }
        }
      }
    }


    /*


=begin
      # Find a set of repositories from fellow watchers that happen to watch a lot of same repositories as the test watcher.
      repositories_to_check.merge find_repositories_containing_fellow_watchers(test_regions)

      # Add in the most popular and most forked regions we know the test watcher is in.
      related_regions = find_regions_containing_fellow_watchers(test_regions)
      related_regions.each do |region|
        repositories_to_check << region.most_popular.id
        repositories_to_check << region.most_forked.id
      end

      $LOG.info "Added regions from fellow watchers for watcher #{watcher.id} -- new size #{repositories_to_check.size} (+ #{repositories_to_check.size - old_size})"
      old_size = repositories_to_check.size

      $LOG.info "Added similarly owned for watcher #{watcher.id} -- new size #{repositories_to_check.size} (+ #{repositories_to_check.size - old_size})"
      old_size = repositories_to_check.size
=end




=begin

    end

    results
     */


    return results;
  }

  private Set<NeighborRegion> find_regions_containing_fellow_watchers(final Set<NeighborRegion> test_regions)
  {
    // Take a look at each region the test instance is in.
    // For each region, find the most common watchers.
    final Map<Watcher, Number> similar_watcher_counts = new HashMap<Watcher, Number>();
    for (final NeighborRegion watched_region : test_regions)
    {
      for (final Watcher related_watcher : watched_region.watchers)
      {
        if (similar_watcher_counts.get(related_watcher) == null)
        {
          similar_watcher_counts.put(related_watcher, 0);
        }

        similar_watcher_counts.put(related_watcher, similar_watcher_counts.get(related_watcher).intValue() + 1);
      }
    }

    // Convert raw counts to ratios.
    for (final Map.Entry<Watcher, Number> pair : similar_watcher_counts.entrySet())
    {
      similar_watcher_counts.put(pair.getKey(), pair.getValue().floatValue() / test_regions.size());
    }

    // Collect the user IDs for the 10 most common watchers.
    final List<Map.Entry<Watcher, Number>> sorted = MyUtils.sortWatcherCounts(similar_watcher_counts, new NumberMeanComparator());

    final List<Watcher> most_common_watchers = new ArrayList<Watcher>();
    final int upperBound = sorted.size() < TOP_COMMON_WATCHERS_COUNT ? sorted.size() : TOP_COMMON_WATCHERS_COUNT;
    for (int i = 0; i < upperBound; i++)
    {
      most_common_watchers.add(sorted.get(i).getKey());
    }
    // # Collect the user IDs for any user that appears in 50% or more of the watcher's repository regions.
    // #most_common_watchers = @similar_watcher_counts.find_all {|key, value| value >= TOP_COMMON_WATCHERS_PERCENT}.collect {|key, value| key}

    // Now go through each of those watchers and add in all the repository regions that they're watching, but
    // that the current watcher is not watching.
    final Set<NeighborRegion> ret = new HashSet<NeighborRegion>();
    for (final Watcher common_watcher : most_common_watchers)
    {
      ret.addAll(watchers_to_regions.get(common_watcher.id));

      if (ret.size() > TOP_REPOS_COUNT)
      {
        break;
      }
    }

    /*
    # Now sort the related regions by number of watchers and grab the 100 top ones.
    sorted_related_regions = related_regions.to_a.sort { |x, y| y.watchers.size <=> x.watchers.size }
    sorted_related_regions[0...TOP_COMMON_REPOS]
     */

    return ret;
  }

  /**
   * Calculates Euclidian distance between two repositories.
   * 
   * @param training_watcher
   * @param first
   * @param second
   * @return
   */
  private float euclidian_distance(final Watcher training_watcher, final Repository first, final Repository second)
  {
    if (first.equals(second))
    {
      return 1000.0f;
    }

    final Collection<Watcher> common_watchers = CollectionUtils.intersection(first.watchers, second.watchers);
    float distance = 1000.0f;

    // Set up weights.

    final float parent_child_weight = 0.4f;
    final float related_weight = 0.75f;
    final float same_owner_weight = 0.65f;
    final float common_watcher_weight = 1.0f;

    // Penalize repositories that have no parent nor children.
    final float lone_repo_weight = (first.parent == null && first.children.isEmpty()) || (second.parent == null && second.children.isEmpty()) ? 2.0f : 1.0f;

    /*
    // Figure out how many repositories the common watchers have in common with the test watcher.
    final Map<String, Number> similar_watcher_counts = new HashMap<String, Number>();
    for (final NeighborRegion watched_region : watchers_to_regions.get(training_watcher.id))
    {
      for (final Watcher w : watched_region.watchers)
      {
        if (similar_watcher_counts.get(w.id) == null)
        {
          similar_watcher_counts.put(w.id, new Integer(0));
        }

        similar_watcher_counts.put(w.id, new Integer(similar_watcher_counts.get(w.id).intValue() + 1));
      }
    }

    // Convert raw counts to ratios.
    for (final Map.Entry<String, Number> pair : similar_watcher_counts.entrySet())
    {
      similar_watcher_counts.put(pair.getKey(), pair.getValue().floatValue() / watchers_to_regions.get(training_watcher.id).size());
    }

    // Figure out how many repositories the test watcher watches that are owned by the first repository owner.
    float similarly_owned_count = 0.0f;
    for (final NeighborRegion watched_region : watchers_to_regions.get(training_watcher.id))
    {
      if (watched_region.most_forked.owner == first.owner)
      {
        similarly_owned_count += 1.0f;
      }
    }

    // TODO: (KJM 08/29/09) Consider looking up the region, since that's what we use everywhere else.
    int total_watchers = 1;
    for (final Repository repo : owners_to_repositories.get(first.owner))
    {
      total_watchers += repo.watchers.size();
    }

    // If common_watchers is empty, just make the value 1.0 because we use the value as a divisor.
    float common_watchers_repo_diversity = common_watchers.isEmpty() ? 1.0f : 0.0f;
    for (final Watcher w : common_watchers)
    {
      common_watchers_repo_diversity += w.repositories.size();
    }
    common_watchers_repo_diversity /= common_watchers.size();
    */

    if ((first.parent != null) && first.parent.equals(second))
    {
      return 0.9f;
      //distance = (float) (parent_child_weight * (1.0 - (((float)common_watchers.size()) / MyUtils.mean(Arrays.asList(first.watchers.size(), second.watchers.size()))))
      //    * (1.0 - ((similarly_owned_count + total_watchers) / Math.max(owners_to_repositories.get(first.owner).size(), 1))) + MyUtils.mean(similar_watcher_counts.values()));
    }
    else if (first.isRelated(second))
    {
      return 0.5f;
      //distance = (float) (related_weight * (1.0 - (common_watchers.size() / MyUtils.mean(Arrays.asList(first.watchers.size(), second.watchers.size()))))
      //    - ((common_watchers.size() / common_watchers_repo_diversity) / MyUtils.mean(Arrays.asList(first.watchers.size(), second.watchers.size()))));
    }
    else if (first.owner.equals(second.owner))
    {
      return 0.93f;
      //distance = (float) (same_owner_weight * (1.0 - (MyUtils.mean(Arrays.asList(((float)common_watchers.size()) / first.watchers.size(), ((float)common_watchers.size()) / second.watchers.size()))))
      //    * (1.0 - ((similarly_owned_count + total_watchers) / owners_to_repositories.get(first.owner).size())) + MyUtils.mean(similar_watcher_counts.values()));
    }
    else
    {
      if (!common_watchers.isEmpty())
      {
        return 0.7f;
        //final float first_common_watchers_ratio = ((float) common_watchers.size()) / first.watchers.size();
        //final float second_common_watchers_ratio = ((float) common_watchers.size()) / second.watchers.size();

        //distance = (float) (common_watcher_weight * (1.0 - (MyUtils.mean(Arrays.asList(first_common_watchers_ratio, second_common_watchers_ratio))))
        //    - (((float)common_watchers.size()) / common_watchers_repo_diversity) / MyUtils.mean(Arrays.asList(first.watchers.size(), second.watchers.size())));
      }
    }

    return lone_repo_weight * distance;

    /*
    common_watchers_repo_diversity = common_watchers.empty? ? 1 : common_watchers.collect {|w| training_watchers[w].repositories.size}.mean


 #   @@comparisons[second.id] ||= {}
 #   @@comparisons[second.id][first.id] = distance

    distance


    # Other factors for calculating distance:
    # - Ages of repositories
    # - Ancestry of two repositories (give higher weight if one of the repositories is the most popular by watchings and/or forks)
    # - # of forks
    # - watcher chains (e.g., repo a has watchers <2, 5>, repo b has watchers <5, 7>, repo c has watchers <7> . . . a & c may be slightly related.
    # - Language overlaps
    # - Size of repositories?

    # Also, look at weighting different attributes.  Maybe use GA to optimize.
     */
  }

  private void storeDistance(final Map<String, Map<String, Collection<Float>>> results, final Watcher watcher, final Repository repo, final Float distance)
  {
    if (results.get(watcher.id).get(repo.id) == null)
    {
      results.get(watcher.id).put(repo.id, new ArrayList<Float>());
    }

    results.get(watcher.id).get(repo.id).add(distance);
  }

  private List<NeighborRegion> find_regions_with_most_cutpoints(final Watcher test_watcher, final Set<NeighborRegion> test_regions)
  {
    final Map<String, Collection<Integer>> related_region_counts = new HashMap<String, Collection<Integer>>();

    // Look at each watcher in each of the test watcher's regions and find the other regions each of those watchers is in.
    for (final NeighborRegion watched_region : test_regions)
    {
      for (final Watcher related_watcher : watched_region.watchers)
      {
        for (final Repository related_repo : related_watcher.repositories)
        {
          final NeighborRegion related_region = related_repo.region;

          // Don't both adding in the region if we already know that it contains the test watcher.
          if (!related_region.watchers.contains(test_watcher))
          {
            // Initialize counts list if necessary.
            if (related_region_counts.get(related_region.id) == null)
            {
              related_region_counts.put(related_region.id, new ArrayList<Integer>());
            }

            // Add the cut point count.
            related_region_counts.get(related_region.id).add(watched_region.cut_point_count(related_region));
          }
        }
      }
    }

    final List<Map.Entry<String, Collection<Integer>>> sorted = MyUtils.sortMapByValues(related_region_counts, new IntegerMeanComparator());

    final List<NeighborRegion> ret = new ArrayList<NeighborRegion>();

    int upperBound = sorted.size() < TOP_REPOS_COUNT ? sorted.size() : TOP_REPOS_COUNT;
    for (int i = 0; i < upperBound; i++)
    {
      final String region_id = sorted.get(i).getKey();
      ret.add(training_regions.get(region_id));
    }

    return ret;
  }

  

  public NeighborRegion find_region(final Repository repo)
  {
    return training_regions.get(Repository.findRoot(repo).id);
  }

  /**
   * Chooses the k best predictions to make from all evaluated distances.
   * Evaluations is a hash of the form {watcher_id => {repo1_id => distance1, repo2_id => distance2}}
   *
   * @param knn
   * @param evaluations
   * @param k
   * @return
   */
  public static Set<Watcher> predict(final NearestNeighbors knn, final Map<String, Map<String, Collection<Float>>> evaluations, final int k)
  {
    final Set<Watcher> ret = new HashSet<Watcher>();

    for (final Map.Entry<String, Map<String, Collection<Float>>> evaluation : evaluations.entrySet())
    {
      final String user_id = evaluation.getKey();
      final Watcher w = new Watcher(user_id);

      final Map<String, Collection<Float>> distances = evaluation.getValue();

      if (!distances.isEmpty())
      {
        final List<Map.Entry<String, Collection<Float>>> sorted = MyUtils.sortMapByValues(distances, new FloatMeanComparator());

        int upperBound = distances.size() < k ? distances.size() : k;
        for (int i = 0; i < upperBound; i++)
        {
          // TODO (KJM 8/10/09) Only add repo if distance is below some threshold.
          final String repo_id = sorted.get(i).getKey();
          w.associate(knn.training_repositories.get(repo_id));
        }
      }

      ret.add(w);
    }

    return ret;
  }

  /**
   * Calculates accuracy between actual and predicted watchers.
   * 
   * @param actual
   * @param predicted
   * @return
   */
  public static float accuracy(final Watcher actual, final Watcher predicted)
  {
    if ((actual == null) || (predicted == null))
    {
      return 0.0f;
    }

    if ((actual.repositories.isEmpty()) && (predicted.repositories.isEmpty()))
    {
      return 1.0f;
    }

    if ((actual.repositories.isEmpty()) && (!predicted.repositories.isEmpty()))
    {
      return 0.0f;
    }

    if ((actual.repositories.isEmpty()) || (predicted.repositories.isEmpty()))
    {
      return 0.0f;
    }

    int number_correct = CollectionUtils.intersection(actual.repositories, predicted.repositories).size();
    int number_incorrect = CollectionUtils.subtract(predicted.repositories, actual.repositories).size();

    // Rate the accuracy of the predictions, with a bias towards positive results.
    return ((float) number_correct) / actual.repositories.size(); // - ((float) (number_incorrect) / predicted.repositories.size();
  }

  /**
   * Aggregates accuracies of evaluations of each item in the test set, yielding an overall accuracy score.
   * 
   * @param test_set
   * @param predictions
   * @return
   */
  public static float score(final DataSet test_set, final Set<Watcher> predictions) throws IOException
  {
    float number_correct = 0.0f;
    int total_repositories_to_predict = 0;

    // Look at each predicted answer for each watcher.  If the prediction appears in the watcher's list, then it
    // was an accurate prediction.  Otherwise, no score awarded.
    for (final Watcher prediction : predictions)
    {
      final Watcher actual = test_set.getWatchers().get(prediction.id);
      total_repositories_to_predict += actual.repositories.size();

      for (final Repository r : prediction.repositories)
      {
        if (actual.repositories.contains(r))
        {
          number_correct++;
        }
      }
    }

    return number_correct / total_repositories_to_predict;
  }

  private class IntegerMeanComparator implements Comparator<Map.Entry<String, Collection<Integer>>>
  {
    public int compare(final Map.Entry<String, Collection<Integer>> first, final Map.Entry<String, Collection<Integer>> second)
    {

      float firstAverage = MyUtils.mean(first.getValue());
      float secondAverage = MyUtils.mean(second.getValue());

      if (secondAverage > firstAverage)
      {
        return 1;
      }
      else if (firstAverage < secondAverage)
      {
        return -1;
      }

      return 0;
    }
  }

  private static class FloatMeanComparator implements Comparator<Map.Entry<String, Collection<Float>>>
  {
    public int compare(final Map.Entry<String, Collection<Float>> first, final Map.Entry<String, Collection<Float>> second)
    {

      float firstAverage = MyUtils.mean(first.getValue());
      float secondAverage = MyUtils.mean(second.getValue());

      if (secondAverage > firstAverage)
      {
        return 1;
      }
      else if (firstAverage < secondAverage)
      {
        return -1;
      }

      return 0;
    }
  }

  private static class NumberMeanComparator implements Comparator<Map.Entry<Watcher, Number>>
  {
    public int compare(final Map.Entry<Watcher, Number> first, final Map.Entry<Watcher, Number> second)
    {
      final float firstValue = first.getValue().floatValue();
      final float secondValue = second.getValue().floatValue();

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
  }
}
