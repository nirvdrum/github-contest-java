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

import org.testng.annotations.Test;
import static org.testng.Assert.*;

@Test
public class WatchingTest
{
  public void testEquality()
  {
    final Watching first = new Watching("1", "1234");
    final Watching second = new Watching("1", "1234");
    final Watching third = new Watching("1", "2345");

    assertEquals(first, second);
    assertEquals(second, first);
    assertEquals(first.hashCode(), second.hashCode());

    assertFalse(first.equals(third));
    assertFalse(third.equals(first));
  }

  public void testToString()
  {
    final Watching w = new Watching("1", "1234");

    assertEquals(w.toString(), "1:1234");
  }
}
