package de.alpengeist;


import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NodeCache {
    private static final String SEPARATOR = "##";
    public class Entry {
        public long twitterId;
        public String data;
        public long[] friendIds;
        public String toString() {
            StringBuilder b = new StringBuilder();
            return b.append(0).append(SEPARATOR)
                    .append(twitterId).append(SEPARATOR)
                    .append(StringUtils.trimToEmpty(data)).append(SEPARATOR)
                    .append(StringUtils.join(sillyConversion(friendIds), SEPARATOR))
            .toString();
        }
    }

    private Logger log = LoggerFactory.getLogger(NodeCache.class);
    private Map<Long, Entry> cache = new HashMap<Long, Entry>();
    private File file;

    public NodeCache(String filePath) throws IOException {
        file = new File(filePath);
        readCache();
    }

    public Entry get(long twitterId) {
        return cache.get(twitterId);
    }

    public void put(long twitterId, long[] friendIds) {
        Entry e = new Entry();
        e.twitterId = twitterId;
        e.friendIds = friendIds;
        cache.put(e.twitterId, e);
    }

    private Long[] sillyConversion(long[] array) {
        Long[] result = new Long[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        return result;
    }

    private void readCache() throws IOException {
        if (file.exists()) {
            List<String> lines = FileUtils.readLines(file);
            for (String line : lines) {
                String[] values = line.split(SEPARATOR);
                Entry e = new Entry();
                // values[0] = record type
                e.twitterId = Long.parseLong(values[1]);
                //log.info(e.twitterId+"");
                if (values.length >= 3) {
                    //e.name = values[2];
                }
                if (values.length >= 4) {
                    e.friendIds = new long[values.length - 3];
                    for (int f = 0, v = 3; v < values.length; v++, f++) {
                        e.friendIds[f] = Long.parseLong(values[v]);
                    }

                } else {
                    e.friendIds = new long[0];
                }
                cache.put(e.twitterId, e);
            }
        } else {
            log.info("Twitter cache does not exist");
        }
    }

    public void writeCache() throws IOException {
        writeCache(file);
    }

    public void writeCache(File outfile) throws IOException {
        log.info("writing cache to {}", outfile.getAbsolutePath());
        List<String> lines = new ArrayList<String>(cache.size());
        for (Entry e : cache.values()) {
            lines.add(e.toString());
        }
        FileUtils.writeLines(outfile, lines);
    }
}
