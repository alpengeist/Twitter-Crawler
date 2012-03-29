package de.alpengeist;

import org.testng.annotations.Test;

import java.io.File;

public class CacheTest {
    @Test
    public void testCache() throws Exception {
        NodeCache c = new NodeCache("D:\\rubydev\\workspace\\TwitterCrawler\\neo\\src\\test\\org\\alpengeist\\cache.csv");
        c.writeCache(new File("D:\\rubydev\\workspace\\TwitterCrawler\\neo\\src\\test\\org\\alpengeist\\cache_out.csv"));
    }
    @Test
    public void testLiveCache() throws Exception {
        NodeCache c = new NodeCache("D:\\rubydev\\workspace\\TwitterCrawler\\twitter_cache.csv");
    }
}
