package com.github;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: nirvdrum
 * Date: Aug 25, 2009
 * Time: 1:05:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class MyUtils
{
  public static <T extends Number> List<Map.Entry<String, Collection<T>>> sortMapByValues(final Map<String, Collection<T>> related_region_counts, Comparator<Map.Entry<String, Collection<T>>> comparator)
  {
    final List<Map.Entry<String, Collection<T>>> sorted = new ArrayList<Map.Entry<String, Collection<T>>>(related_region_counts.entrySet());
    Collections.sort(sorted, comparator);
    
    return sorted;
  }

  public static List<Map.Entry<Watcher, Number>> sortWatcherCounts(final Map<Watcher, Number> related_region_counts, Comparator<Map.Entry<Watcher, Number>> comparator)
  {
    final List<Map.Entry<Watcher, Number>> sorted = new ArrayList<Map.Entry<Watcher, Number>>(related_region_counts.entrySet());
    Collections.sort(sorted, comparator);

    return sorted;
  }

  public static List<Map.Entry<String, NeighborRegion>> sortRegionsByPopularity(final Map<String, NeighborRegion> regions, Comparator<Map.Entry<String, NeighborRegion>> comparator)
  {
    final List<Map.Entry<String, NeighborRegion>> sorted = new ArrayList<Map.Entry<String, NeighborRegion>>(regions.entrySet());
    Collections.sort(sorted, comparator);

    return sorted;
  }

  public static float mean(Collection<? extends Number> terms)
  {
    if (terms.isEmpty())
    {
      return 0;
    }

    float total = 0;
    for (Number term : terms)
    {
      total += term.doubleValue();
    }

    return ((float) total) / terms.size();
  }
}
