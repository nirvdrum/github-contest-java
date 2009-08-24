package com.github;

import java.util.Map;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: nirvdrum
 * Date: Aug 24, 2009
 * Time: 7:34:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class Main
{
  public static void main(String[] args) throws IOException
  {
    final Map<String, Repository> repositories = DataLoader.loadRepositories();

    for (final Repository repo : repositories.values())
    {
      System.out.println(repo);
    }
  }
}
