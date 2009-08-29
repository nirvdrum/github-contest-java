package com.github;

import org.apache.commons.collections.CollectionUtils;

import java.util.Set;
import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 * User: nirvdrum
 * Date: Aug 25, 2009
 * Time: 8:24:40 AM
 * To change this template use File | Settings | File Templates.
 */
public class NeighborRegion
{
  public final String id;
  public final Set<Repository> repositories = new HashSet<Repository>();

  public final Set<Watcher> watchers = new HashSet<Watcher>();
  public Repository most_popular;
  public Repository most_forked;

  public NeighborRegion(final Repository repository)
  {
    final Repository root = Repository.findRoot(repository);

    this.id = root.id;
    this.most_popular = root;
    this.most_forked = root;

    add(root);
    add(repository);
  }

  public void add(final Repository repo)
  {
    repo.region = this;

    watchers.addAll(repo.watchers);

    repositories.add(repo);

    if (repo.watchers.size() > most_popular.watchers.size())
    {
      most_popular = repo;
    }

    if (repo.children.size() > most_forked.children.size())
    {
      most_forked = repo;
    }
  }

  public int cut_point_count(final NeighborRegion other)
  { 
    return CollectionUtils.intersection(watchers, other.watchers).size();
  }
}
