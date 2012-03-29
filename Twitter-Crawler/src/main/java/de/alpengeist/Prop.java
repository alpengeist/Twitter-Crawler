package de.alpengeist;

import org.neo4j.graphdb.Node;

import static de.alpengeist.PropDatatype.*;

public enum Prop {
    TWITTER_ID("twitter_id", LONG),
    NAME("name", STRING),
    FULL_NAME("full_name", STRING),
    LABEL("Label", STRING),
    DEPTH("depth", LONG),
    WIDTH("width", LONG),
    FRIEND_COUNT("friend_count", LONG),
    FOLLOWER_COUNT("follower_count", LONG),
    TIMEZONE("timezone", STRING),
    LOCATION("location", STRING),

    GEOQUALITY("geo_quality", LONG),
    LONGITUDE("longitude", DOUBLE),
    LATITUDE("latitude", DOUBLE),
    COUNTRY("country", STRING)
    ;
    private String name;
    private PropDatatype type;

    Prop(String name, PropDatatype type) {
        this.name = name;
        this.type = type;
    }


    public String toString() {
        return name;
    }

    public PropDatatype getType() {
        return type;
    }

    public void set(Node node, Object value) {
        if (value != null)
            node.setProperty(this.toString(), value);
    }

    public boolean exists(Node node) {
        return node.hasProperty(this.toString());
    }

    public String getS(Node node) {
        return node.getProperty(this.toString(), "").toString();
    }

    public long getL(Node node) {
        return ((Number)node.getProperty(this.toString())).longValue();
    }

    public double getD(Node node) {
        return ((Number)node.getProperty(this.toString())).doubleValue();
    }
}
