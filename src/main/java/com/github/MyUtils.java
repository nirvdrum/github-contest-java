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

import java.util.*;

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
