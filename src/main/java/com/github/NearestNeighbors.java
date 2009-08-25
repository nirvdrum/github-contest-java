package com.github;

import org.apache.log4j.Logger;

import java.util.*;
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
  public final int TOP_REPOS_COUNT = 10;

  private final Logger log = Logger.getLogger(NearestNeighbors.class);

  public final Map<String, Watcher> training_watchers;
  public final Map<String, Repository> training_repositories;
  public final Map<String, NeighborRegion> training_regions = new HashMap<String, NeighborRegion>();
  public final Map<String, Set<NeighborRegion>> watchers_to_regions = new HashMap<String, Set<NeighborRegion>>();

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

      for (final Watcher w : repo.watchers)
      {
        if (watchers_to_regions.get(w.id) == null)
        {
          watchers_to_regions.put(w.id, new HashSet<NeighborRegion>());
        }

        watchers_to_regions.get(w.id).add(training_regions.get(root.id));
      }
    }
  }

  public Map<String, Map<String, Collection<Float>>> evaluate(final DataSet test_set) throws IOException
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

      final List<NeighborRegion> related_regions = find_regions_with_most_cutpoints(watcher, test_regions);
      for (final NeighborRegion related_region : related_regions)
      {
        storeDistance(results, watcher, related_region.most_popular, 0.0f);
        storeDistance(results, watcher, related_region.most_forked, 0.0f);
      }
    }


    /*

      ###################################
      ### Handling repository regions ###
      ###################################

      # Calculate the distance between the repository regions we know the test watcher is in, to every other
      # region in the training data.
      test_regions = @watchers_to_regions[watcher.id]

      repositories_to_check = Set.new
      old_size = 0

      related_regions = find_regions_with_most_cutpoints(watcher, test_regions)
      related_regions.each do |region_id|
        region = @training_regions[region_id]
        #repositories_to_check << region.most_popular.id
        #repositories_to_check << region.most_forked.id
        results[watcher.id][region.most_popular.id] ||= []
        results[watcher.id][region.most_popular.id] << 0
      end

=begin
      # Find a set of repositories from fellow watchers that happen to watch a lot of same repositories as the test watcher.
      repositories_to_check.merge find_repositories_containing_fellow_watchers(test_regions)

      $LOG.info "Added repos from fellow watchers for watcher #{watcher.id} -- new size #{repositories_to_check.size} (+ #{repositories_to_check.size - old_size})"
      old_size = repositories_to_check.size

      # Add in the most popular and most forked repositories from each region we know the test watcher is in.
      test_regions.values.each do |region|
        repositories_to_check << region.most_popular.id
        repositories_to_check << region.most_forked.id
      end

      $LOG.info "Added most_popular & most_forked from test_regions for watcher #{watcher.id} -- new size #{repositories_to_check.size} (+ #{repositories_to_check.size - old_size})"
      old_size = repositories_to_check.size

      # Add in the most popular and most forked regions we know the test watcher is in.
      related_regions = find_regions_containing_fellow_watchers(test_regions)
      related_regions.each do |region|
        repositories_to_check << region.most_popular.id
        repositories_to_check << region.most_forked.id
      end

      $LOG.info "Added regions from fellow watchers for watcher #{watcher.id} -- new size #{repositories_to_check.size} (+ #{repositories_to_check.size - old_size})"
      old_size = repositories_to_check.size


      ####################################################################
      ### Handling repositories owned by owners we're already watching ###
      ####################################################################
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

      $LOG.info "Added similarly owned for watcher #{watcher.id} -- new size #{repositories_to_check.size} (+ #{repositories_to_check.size - old_size})"
      old_size = repositories_to_check.size
=end




=begin
      test_region_count = 0
      test_regions.each do |test_region|
        thread_pool = []
        training_region_count = 0
        repositories_to_check.each do |training_repository_id|
          training_repository = @training_repositories[training_repository_id]
          $LOG.debug { "Processing watcher (#{test_watcher_count}/#{test_instances.size}) - (#{test_region_count}/#{test_regions.size}):(#{training_region_count}/#{related_repositories.size})"}
          training_region_count += 1

          unless training_repository.watchers.include?(watcher.id)
#            t = Thread.new(test_region, training_repository) do |test_region, training_repository|
#              distance = euclidian_distance(training_watcher, test_region.most_popular, training_repository)
#              [distance, training_repository.id]
#            end
#            thread_pool << t

            t2 = Thread.new do
              distance = euclidian_distance(training_watcher, test_region.most_forked, training_repository)
              [distance, training_repository.id]
            end
            thread_pool << t2
          end

          while thread_pool.size > THREAD_POOL_SIZE
            thread_pool.each do |t|
              if t.stop?
                distance, repo_id = t.value
                unless distance.nil?
                  results[watcher.id][repo_id] ||= []
                  results[watcher.id][repo_id] << distance
                end
              end

              thread_pool.delete(t)
            end
          end
        end

        thread_pool.each do |t|
          distance, repo_id = t.value
          unless distance.nil?
            results[watcher.id][repo_id] ||= []
            results[watcher.id][repo_id] << distance
          end
        end

        test_region_count += 1
      end
=end

    end

    results
     */


    return results;
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
          final NeighborRegion related_region = find_region(related_repo);

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

    final List<Map.Entry<String, Collection<Integer>>> sorted = new ArrayList<Map.Entry<String, Collection<Integer>>>(related_region_counts.entrySet());
    Collections.sort(sorted, new Comparator<Map.Entry<String, Collection<Integer>>>()
    {
      public int compare(final Map.Entry<String, Collection<Integer>> first, final Map.Entry<String, Collection<Integer>> second)
      {
        float firstAverage = mean(first.getValue());
        float secondAverage = mean(second.getValue());

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
    });

    final List<NeighborRegion> ret = new ArrayList<NeighborRegion>();

    int upperBound = sorted.size() < TOP_REPOS_COUNT ? sorted.size() : TOP_REPOS_COUNT;
    for (int i = 0; i < upperBound; i++)
    {
      final String region_id = sorted.get(i).getKey();
      ret.add(training_regions.get(region_id));
    }

    return ret;
  }

  private NeighborRegion find_region(final Repository repo)
  {
    return training_regions.get(Repository.findRoot(repo).id);
  }

  private float mean(Collection<Integer> terms)
  {
    if (terms.isEmpty())
    {
      return 0;
    }

    int total = 0;
    for (Integer term : terms)
    {
      total += term.intValue();
    }

    return total / terms.size();
  }
}
