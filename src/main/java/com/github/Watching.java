package com.github;

/**
 * Created by IntelliJ IDEA.
 * User: nirvdrum
 * Date: Aug 24, 2009
 * Time: 7:39:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class Watching
{
  public final String watcher_id;
  public final String repository_id;

  public Watching(final String watcher_id, final String repository_id)
  {
    this.watcher_id = watcher_id;
    this.repository_id = repository_id;
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

    final Watching watching = (Watching) o;

    if (repository_id != null ? !repository_id.equals(watching.repository_id) : watching.repository_id != null)
    {
      return false;
    }
    if (watcher_id != null ? !watcher_id.equals(watching.watcher_id) : watching.watcher_id != null)
    {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode()
  {
    int result = watcher_id != null ? watcher_id.hashCode() : 0;
    result = 31 * result + (repository_id != null ? repository_id.hashCode() : 0);
    return result;
  }

  @Override
  public String toString()
  {
    return String.format("%s:%s", watcher_id, repository_id);
  }
}
