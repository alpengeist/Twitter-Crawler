package de.alpengeist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.TwitterException;
import twitter4j.internal.http.HttpClient;
import twitter4j.internal.http.HttpClientFactory;
import twitter4j.internal.http.HttpParameter;
import twitter4j.internal.http.HttpRequest;
import twitter4j.internal.http.HttpResponse;
import twitter4j.internal.http.RequestMethod;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class YahooPlaces {
    private Logger log = LoggerFactory.getLogger(YahooPlaces.class);
    private HttpClient client;
    private int requestCounter = 0;

    public YahooPlaces() {
        client = HttpClientFactory.getInstance(Config.get());
    }

    public JSONObject getPlace(String place) throws TwitterException, JSONException {
        HttpParameter[] para = new HttpParameter[] {
            new HttpParameter("q", place),
            new HttpParameter("flags", "J")
        };
        Map<String, String> header = new HashMap<>();
        header.put("Accept", "application/json");
        HttpRequest req = new HttpRequest(RequestMethod.GET, "http://where.yahooapis.com/geocode", para, null, header);
        HttpResponse resp = client.request(req);
        requestCounter++;
        return resp.asJSONObject().getJSONObject("ResultSet");
    }

    public boolean isOk(JSONObject obj) throws JSONException {
        return obj.getInt("Error") == 0;
    }

    public int getFound(JSONObject obj) throws JSONException {
        return obj.getInt("Found");
    }

    public String getLatitude(JSONObject obj) throws JSONException {
        return ((JSONObject)obj.getJSONArray("Results").get(0)).getString("latitude");
    }

    public String getLongitude(JSONObject obj) throws JSONException {
        return ((JSONObject)obj.getJSONArray("Results").get(0)).getString("longitude");
    }

    public String getCountry(JSONObject obj) throws JSONException {
        return ((JSONObject)obj.getJSONArray("Results").get(0)).getString("country");
    }

    public long getGeoQuality(JSONObject obj) throws JSONException {
        return obj.getInt("Quality");
    }

    public void logStatistics() {
        log.info("{} requests to YahooPlaces", requestCounter);
    }
}
