package com.github;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
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
  public final Map<String, Watcher> training_watchers;
  public final Map<String, Repository> training_repositories;
  public final Map<String, NeighborRegion> training_regions = new HashMap<String, NeighborRegion>();
  public final Map<String, Set<NeighborRegion>> watchers_to_regions = new HashMap<String, Set<NeighborRegion>>();

  public NearestNeighbors(final DataSet training_set) throws IOException
  {
    training_watchers = training_set.getWatchers();
    training_repositories = training_set.getRepositories();

    for (final Map.Entry<String, Repository> pair : training_repositories.entrySet())
    {
      final String repo_id = pair.getKey();
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
}
