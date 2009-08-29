package com.github;

import org.apache.commons.io.FileUtils;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: nirvdrum
 * Date: Aug 24, 2009
 * Time: 6:15:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class DataLoader
{
  public static Map<String, Repository> loadRepositories() throws IOException
  {
    final Map<String, Repository> repositories = new HashMap<String, Repository>();
    final Map<String, String> relationships = new HashMap<String, String>();

    // First, discover all the repositories.
    final File repoFile = FileUtils.toFile(DataLoader.class.getClassLoader().getResource("data/repos.txt"));
    for (final Object o : FileUtils.readLines(repoFile))
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
    final File watchingsFile = FileUtils.toFile(DataLoader.class.getClassLoader().getResource("data/data.txt"));
    for (final Object o : FileUtils.readLines(watchingsFile))
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

    final File watchingsFile = FileUtils.toFile(DataLoader.class.getClassLoader().getResource("data/data.txt"));
    for (final Object o : FileUtils.readLines(watchingsFile))
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

    final File predictingsFile = FileUtils.toFile(DataLoader.class.getClassLoader().getResource("data/test.txt"));
    for (final Object o : FileUtils.readLines(predictingsFile))
    {
      final String line = (String) o;

      ret.add(new Watcher(line.trim()));
    }

    return ret;
  }
}
