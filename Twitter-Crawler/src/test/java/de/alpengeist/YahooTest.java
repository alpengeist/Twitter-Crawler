package de.alpengeist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import twitter4j.internal.org.json.JSONObject;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class YahooTest {
    private Logger log = LoggerFactory.getLogger(YahooTest.class);
    @Test
    public void testPlaces() throws Exception {
        YahooPlaces y = new YahooPlaces();
        JSONObject result = y.getPlace("Miesbach");
        log.info(result.toString(2));
        assertTrue(y.isOk(result));
        assertEquals(y.getLatitude(result), "47.789140");
        assertEquals(y.getLongitude(result), "11.833420");
        assertEquals(y.getGeoQuality(result), 40);
        assertEquals(y.getCountry(result), "Germany");

        result = y.getPlace("xxxxx");
        assertTrue(y.isOk(result));
        assertTrue(y.getFound(result) == 0);
    }
}
