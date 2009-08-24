package com.github;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: nirvdrum
 * Date: Aug 24, 2009
 * Time: 7:29:01 AM
 * To change this template use File | Settings | File Templates.
 */
@Test
public class WatcherTest
{
    @BeforeTest
    public void setUp()
    {
    }

    public void test_new()
    {
        String id = "1";

        Watcher w = new Watcher("1");

        assertEquals(w.id, id);
    }
}
