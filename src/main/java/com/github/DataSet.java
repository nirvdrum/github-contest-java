package com.github;

import java.util.*;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: nirvdrum
 * Date: Aug 24, 2009
 * Time: 8:57:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class DataSet
{
  public final Set<Watching> watchings = new HashSet<Watching>();

  private Map<String, Watcher> watchers = null;
  private Map<String, Repository> repositories = null;

  public void add(final Watching watching)
  {
    watchings.add(watching);
  }

  public List<DataSet> cross_validation(final int foldCount)
  {
    return null;
  }

  public Map<String, Watcher> getWatchers() throws IOException
  {
    if (watchers == null)
    {
      buildModels();
    }

    return watchers;
  }

  public Map<String, Repository> getRepositories() throws IOException
  {
    if (repositories == null)
    {
      buildModels();
    }

    return repositories;
  }

  private void buildModels() throws IOException
  {
    watchers = new HashMap<String, Watcher>();
    repositories = new HashMap<String, Repository>();

    final Map<String, Repository> raw_repositories = DataLoader.loadRepositories();

    for (final Watching watching : watchings)
    {
      final Watcher watcher = watchers.get(watching.watcher_id) == null ? new Watcher(watching.watcher_id) : watchers.get(watching.watcher_id);
      watchers.put(watching.watcher_id, watcher);

      final Repository repo = repositories.get(watching.repository_id) == null
          ? new Repository(watching.repository_id, raw_repositories.get(watching.repository_id).owner, raw_repositories.get(watching.repository_id).name, raw_repositories.get(watching.repository_id).created_at)
          : repositories.get(watching.repository_id);
      repositories.put(watching.repository_id, repo);

      watcher.associate(repo);
    }

    for (final Map.Entry<String, Repository> mapping : raw_repositories.entrySet())
    {
      final String repo_id = mapping.getKey();
      final Repository repo = mapping.getValue();

      if ((repositories.get(repo_id) != null) && (repo.parent != null) && (repositories.get(repo.parent.id) != null))
      {
        repositories.get(repo_id).setParent(repositories.get(repo.parent.id));
      }
    }
  }

  public List<DataSet> stratify(final int foldCount)
  {
    final List<Watching> sorted = new ArrayList<Watching>(watchings);
    Collections.sort(sorted, new Comparator<Watching>(){
      public int compare(final Watching first, final Watching second)
      {
        return first.repository_id.compareTo(second.repository_id);
      }
    });

    final List<DataSet> folds = new ArrayList<DataSet>(foldCount);
    for (int i = 0; i < foldCount; i++)
    {
      final DataSet fold = new DataSet();

      int index = i;

      while (index < sorted.size())
      {
        fold.add(sorted.get(index));
        index += foldCount;
      }

      folds.add(fold);
    }

    return folds;
  }

  @Override
  public boolean equals(final Object o)
  {
    if (this == o)
    {
      return true;
    }
    if (o == null || getClass() != o.getClass())
    {
      return false;
    }

    final DataSet dataSet = (DataSet) o;

    if (watchings.size() != dataSet.watchings.size())
    {
      return false;
    }

    if (watchings != null ? !watchings.equals(dataSet.watchings) : dataSet.watchings != null)
    {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode()
  {
    return watchings != null ? watchings.hashCode() : 0;
  }

  public static DataSet combine(final Collection<DataSet> subsets)
  {
    return DataSet.combine(subsets.toArray(new DataSet[subsets.size()]));
  }

  public static DataSet combine(final DataSet... subsets)
  {
    final DataSet ret = new DataSet();

    for (final DataSet subset : subsets)
    {
      ret.watchings.addAll(subset.watchings);
    }

    return ret;
  }
}
