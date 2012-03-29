package de.alpengeist;

import org.apache.commons.lang.StringUtils;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.ResponseList;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static de.alpengeist.Prop.*;

public class DataCrawler extends TwitterCrawler {
    private Logger log = LoggerFactory.getLogger(DataCrawler.class);

    private YahooPlaces places = new YahooPlaces();
    private int placesCountdown;

    public DataCrawler() throws IOException {
        super();

    }

    public void setPlacesCountdown(int max) {
        this.placesCountdown = max;
    }

    public void updateData(final long twitterId) {
        neo.transaction(new NeoFeeder.Trx() {
            @Override
            public void execute(GraphDatabaseService db) throws Exception {
                Node root = findUser(twitterId);
                // Extrawurst for start node
                updateRoot(root);
                try {
                    updateFriends(root, 1);
                } catch (TwitterException e) {
                    // save the work, just report and finish
                    log.warn("Twitter error", e);
                } finally {
                    dataCache.writeCache();
                }
            }
        });
    }

    private void updateRoot(Node node) throws TwitterException {
        if (dataCache.hasNode(node)) {
            dataCache.fillNode(node);
            fillInGeodata(node);
            dataCache.putNode(node);
        } else {
            fillInDataSingleNode(node);
        }
    }

    private void fillInDataSingleNode(Node node) throws TwitterException {
        Map<Long, Node> single = new HashMap<Long, Node>();
        single.put(TWITTER_ID.getL(node), node);
        fillInData(single);
    }

    // processes all friends of node and fills in their data if they have no name yet
    // we could update all friends, but that would eat our Twitter request every time and successive
    // node data enrichment would not be possible
    private void updateFriends(Node node, int depth) throws TwitterException {
        if (depth < maxdepth) {
            Map<Long, Node> friendsWithoutData = new HashMap<Long, Node>();
            for (Relationship rel : node.getRelationships(TwitterRelationship.FRIEND, Direction.OUTGOING)) {
                Node friend = rel.getEndNode();
                if (StringUtils.isEmpty(NAME.getS(friend))) {
                    if (dataCache.hasNode(friend)) {
                        dataCache.fillNode(friend);
                        fillInGeodata(friend);
                        dataCache.putNode(friend);
                        neo.addToNameIndex(friend);
                    } else {
                        friendsWithoutData.put(TWITTER_ID.getL(friend), friend);
                    }
                }
            }
            try {
                if (!(friendsWithoutData.isEmpty() || isRateLimitExceeded))
                    fillInData(friendsWithoutData);
            } catch (TwitterException e) {
                handleTwitterException(e);
            }
            // recurse into friends
            for (Relationship rel : node.getRelationships(TwitterRelationship.FRIEND, Direction.OUTGOING)) {
                updateFriends(rel.getEndNode(), depth + 1);
            }
        }
    }

    private void handleTwitterException(TwitterException e) {
        switch (e.getStatusCode()) {
            case 400:
                // most likely request rate limit; continue since cache can still feed following nodes
                isRateLimitExceeded = true;
                log.warn("twitter rate limit exceeded - skipping further requests");
                break;
            case 404:
                log.warn("some user does not exist anymore");
                break;
            default:
                // swallow it, later inquiries may be satisfied by the cache
        }
    }

    private void fillInData(Map<Long, Node> nodes) throws TwitterException {
        ResponseList response = twitter().lookupUsers(twitterIds(nodes.keySet()));
        for (Object aResponse : response) {
            User twUser = (User) aResponse;
            log.info("filling data {}:{}", twUser.getId(), twUser.getScreenName());
            Node node = nodes.get(twUser.getId());
            NAME.set(node, twUser.getScreenName());
            LABEL.set(node, twUser.getScreenName());
            FULL_NAME.set(node, twUser.getName());
            TIMEZONE.set(node, twUser.getTimeZone());
            FRIEND_COUNT.set(node, (long) twUser.getFriendsCount());
            FOLLOWER_COUNT.set(node, (long) twUser.getFollowersCount());
            LOCATION.set(node, twUser.getLocation());

            fillInGeodata(node);
            neo.addToNameIndex(node);
            dataCache.putNode(node);

        }
    }

    private long[] twitterIds(Set<Long> set) {
        long[] result = new long[set.size()];
        int i = 0;
        for (Long n : set) {
            result[i] = n;
            i++;
        }
        return result;
    }

    private void fillInGeodata(Node node) throws TwitterException {
        if (placesCountdown > 0
                && StringUtils.isNotEmpty(LOCATION.getS(node))
                && !GEOQUALITY.exists(node)
                ) {
            try {
                JSONObject result = places.getPlace(LOCATION.getS(node));
                if (places.isOk(result)) {
                    if (places.getFound(result) > 0) {
                        LATITUDE.set(node, Double.parseDouble(places.getLatitude(result)));
                        LONGITUDE.set(node, Double.parseDouble(places.getLongitude(result)));
                        GEOQUALITY.set(node, places.getGeoQuality(result));
                        COUNTRY.set(node, places.getCountry(result));
                    }
                }
            } catch (JSONException e) {
                throw new TwitterException("JSON exception: " + e.getMessage());
            } finally {
                --placesCountdown;
            }
        }
    }

    public void logStatistics() {
        super.logStatistics();
        places.logStatistics();
    }
}