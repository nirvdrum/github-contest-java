package com.github;

import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: nirvdrum
 * Date: Aug 24, 2009
 * Time: 7:28:34 AM
 * To change this template use File | Settings | File Templates.
 */
public class Watcher
{
  public final String id;

  public Set<Repository> repositories = new HashSet<Repository>();

  public final Map<String, Integer> owner_counts = new HashMap<String, Integer>();

  public Watcher(final String id)
  {
    this.id = id;
  }

  public String toString()
  {
    final StringBuilder ret = new StringBuilder(id);
    if (!repositories.isEmpty())
    {
      ret.append(":");
      
      for (final Repository repo : repositories)
      {
        ret.append(repo.id).append(",");
      }

      // Remove the last "," since we'll add one too many.
      ret.deleteCharAt(ret.length() - 1);
    }

    return ret.toString();
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

    final Watcher watcher = (Watcher) o;

    if (id != null ? !id.equals(watcher.id) : watcher.id != null)
    {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode()
  {
    return id != null ? id.hashCode() : 0;
  }

  public void associate(final Repository repo)
  {
    repositories.add(repo);

    repo.watchers.add(this);

    // Take care of the owner counts.
    if (owner_counts.get(repo.owner) == null)
    {
      owner_counts.put(repo.owner, new Integer(0));
    }
    owner_counts.put(repo.owner, new Integer(owner_counts.get(repo.owner).intValue() + 1));
  }

  public float owner_distribution(final String owner)
  {
    if (owner_counts.get(owner) == null)
    {
      return 0.0f;
    }

    return owner_counts.get(owner).floatValue() / repositories.size();
  }
}
