package com.github;

import java.util.HashSet;
import java.util.Set;

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

  public Watcher(final String id)
  {
    this.id = id;
  }
}
