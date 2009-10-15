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

import org.apache.commons.collections.CollectionUtils;

import java.util.Set;
import java.util.HashSet;

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
