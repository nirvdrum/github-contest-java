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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.io.File;
import java.io.IOException;

public class DataLoader
{
  public static Map<String, Repository> loadRepositories() throws IOException
  {
    final Map<String, Repository> repositories = new HashMap<String, Repository>();
    final Map<String, String> relationships = new HashMap<String, String>();

    // First, discover all the repositories.
    for (final Object o : IOUtils.readLines(DataLoader.class.getClassLoader().getResourceAsStream("data/repos.txt")))
    {
      final String line = (String) o;

      final String[] mainParts = line.trim().split(":");
      final String repo_id = mainParts[0];

      final String[] repo_data = mainParts[1].split(",");
      final String created_at = repo_data[1];
      final String parent_id = repo_data.length == 3 ? repo_data[2] : null;

      final String[] fullName = repo_data[0].split("/");
      final String owner = fullName[0];
      final String name = fullName[1];

      repositories.put(repo_id, new Repository(repo_id, owner, name, created_at));

      if (parent_id != null)
      {
        relationships.put(repo_id, parent_id);
      }
    }

    // Now that all the repositories have been loaded, establish any parent-child relationships.
    for (final Map.Entry<String, String> pair : relationships.entrySet())
    {
      repositories.get(pair.getKey()).setParent(repositories.get(pair.getValue()));
    }

    final Map<String, Watcher> watchers = new HashMap<String, Watcher>();
    for (final Object o : IOUtils.readLines(DataLoader.class.getClassLoader().getResourceAsStream("data/data.txt")))
    {
      final String line = (String) o;

      final String[] mainParts = line.trim().split(":");
      final String user_id = mainParts[0];
      final String repo_id = mainParts[1];

      final Watcher watcher = watchers.get(user_id) == null ? new Watcher(user_id) : watchers.get(user_id);
      repositories.get(repo_id).associate(watcher);
    }

    return repositories;
  }

  public static DataSet loadWatchings() throws IOException
  {
    final DataSet ret = new DataSet();

    for (final Object o : IOUtils.readLines(DataLoader.class.getClassLoader().getResourceAsStream("data/data.txt")))
    {
      final String line = (String) o;

      final String[] mainParts = line.trim().split(":");
      final String user_id = mainParts[0];
      final String repo_id = mainParts[1];

      ret.add(new Watching(user_id, repo_id));
    }

    return ret;
  }

  public static Set<Watcher> loadPredictings() throws IOException
  {
    final Set<Watcher> ret = new HashSet<Watcher>();

    for (final Object o : IOUtils.readLines(DataLoader.class.getClassLoader().getResourceAsStream("data/test.txt")))
    {
      final String line = (String) o;

      ret.add(new Watcher(line.trim()));
    }

    return ret;
  }
}
