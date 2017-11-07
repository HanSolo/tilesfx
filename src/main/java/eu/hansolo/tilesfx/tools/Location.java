/*
 * Copyright (c) 2017 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.tilesfx.tools;

import eu.hansolo.tilesfx.Tile.TileColor;
import eu.hansolo.tilesfx.events.LocationEvent;
import eu.hansolo.tilesfx.events.LocationEventListener;
import javafx.scene.paint.Color;
import org.json.simple.JSONObject;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import static eu.hansolo.tilesfx.tools.Helper.clamp;


/**
 * Created by hansolo on 12.02.17.
 */
public class Location {
    public enum CardinalDirection {
        N("North", 348.75, 11.25),
        NNE("North North-East", 11.25, 33.75),
        NE("North-East", 33.75, 56.25),
        ENE("East North-East", 56.25, 78.75),
        E("East", 78.75, 101.25),
        ESE("East South-East", 101.25, 123.75),
        SE("South-East", 123.75, 146.25),
        SSE("South South-East", 146.25, 168.75),
        S("South", 168.75, 191.25),
        SSW("South South-West", 191.25, 213.75),
        SW("South-West", 213.75, 236.25),
        WSW("West South-West", 236.25, 258.75),
        W("West", 258.75, 281.25),
        WNW("West North-West", 281.25, 303.75),
        NW("North-West", 303.75, 326.25),
        NNW("North North-West", 326.25, 348.75);

        public String direction;
        public double from;
        public double to;

        CardinalDirection(final String DIRECTION, final double FROM, final double TO) {
            direction = DIRECTION;
            from      = FROM;
            to        = TO;
        }
    }
    private String                      name;
    private Instant                     timestamp;
    private double                      latitude;
    private double                      longitude;
    private double                      altitude;
    private String                      info;
    private Color                       color;
    private int                         zoomLevel;
    private List<LocationEventListener> listenerList;


    // ******************** Constructors **************************************
    public Location() {
        this(0, 0, 0, Instant.now(), "", "", TileColor.BLUE.color);
    }
    public Location(final double LATITUDE, final double LONGITUDE) {
        this(LATITUDE, LONGITUDE, 0, Instant.now(), "", "", TileColor.BLUE.color);
    }
    public Location(final double LATITUDE, final double LONGITUDE, final String NAME) {
        this(LATITUDE, LONGITUDE, 0, Instant.now() ,NAME, "", TileColor.BLUE.color);
    }
    public Location(final double LATITUDE, final double LONGITUDE, final String NAME, final Color COLOR) {
        this(LATITUDE, LONGITUDE, 0, Instant.now() ,NAME, "", COLOR);
    }
    public Location(final double LATITUDE, final double LONGITUDE, final String NAME, final String INFO) {
        this(LATITUDE, LONGITUDE, 0, Instant.now() ,NAME, INFO, TileColor.BLUE.color);
    }
    public Location(final double LATITUDE, final double LONGITUDE, final String NAME, final String INFO, final Color COLOR) {
        this(LATITUDE, LONGITUDE, 0, Instant.now() ,NAME, INFO, COLOR);
    }
    public Location(final double LATITUDE, final double LONGITUDE, final double ALTITUDE, final String NAME) {
        this(LATITUDE, LONGITUDE, ALTITUDE, Instant.now(), NAME, "", TileColor.BLUE.color);
    }
    public Location(final double LATITUDE, final double LONGITUDE, final double ALTITUDE, final Instant TIMESTAMP, final String NAME) {
        this(LATITUDE, LONGITUDE, ALTITUDE, TIMESTAMP, NAME, "", TileColor.BLUE.color);
    }
    public Location(final double LATITUDE, final double LONGITUDE, final double ALTITUDE, final Instant TIMESTAMP, final String NAME, final String INFO, final Color COLOR) {
        name         = NAME;
        latitude     = LATITUDE;
        longitude    = LONGITUDE;
        altitude     = ALTITUDE;
        timestamp    = TIMESTAMP;
        info         = INFO;
        color        = COLOR;
        zoomLevel    = 15;
        listenerList = new CopyOnWriteArrayList<>();
    }


    // ******************** Methods *******************************************
    public String getName() { return name; }
    public void setName(final String NAME) { name = NAME; }

    public Instant getTimestamp() { return timestamp; }
    public long getTimestampInSeconds() { return timestamp.getEpochSecond(); }
    public void setTimestamp(final Instant TIMESTAMP) { timestamp = TIMESTAMP; }

    public double getLatitude() { return latitude; }
    public void setLatitude(final double LATITUDE) {
        latitude = LATITUDE;
        fireLocationEvent(new LocationEvent(Location.this));
    }

    public double getLongitude() { return longitude; }
    public void setLongitude(final double LONGITUDE) {
        longitude = LONGITUDE;
        fireLocationEvent(new LocationEvent(Location.this));
    }

    public double getAltitude() { return altitude; }
    public void setAltitude(final double ALTITUDE) {
        altitude = ALTITUDE;
        fireLocationEvent(new LocationEvent(Location.this));
    }

    public String getInfo() { return info; }
    public void setInfo(final String INFO) { info = INFO; }

    public Color getColor() { return color; }
    public void setColor(final Color COLOR) {
        color = COLOR;
        fireLocationEvent(new LocationEvent(Location.this));
    }

    public ZonedDateTime getZonedDateTime() { return getZonedDateTime(ZoneId.systemDefault()); }
    public ZonedDateTime getZonedDateTime(final ZoneId ZONE_ID) { return ZonedDateTime.ofInstant(timestamp, ZONE_ID); }

    public int getZoomLevel() { return zoomLevel; }
    public void setZoomLevel(final int LEVEL) {
        zoomLevel = clamp(0, 17, LEVEL);
        fireLocationEvent(new LocationEvent(Location.this));
    }

    public void update(final double LATITUDE, final double LONGITUDE) { set(LATITUDE, LONGITUDE); }

    public void set(final double LATITUDE, final double LONGITUDE) {
        latitude  = LATITUDE;
        longitude = LONGITUDE;
        timestamp = Instant.now();
        fireLocationEvent(new LocationEvent(Location.this));
    }
    public void set(final double LATITUDE, final double LONGITUDE, final double ALTITUDE, final Instant TIMESTAMP) {
        latitude  = LATITUDE;
        longitude = LONGITUDE;
        altitude  = ALTITUDE;
        timestamp = TIMESTAMP;
        fireLocationEvent(new LocationEvent(Location.this));
    }
    public void set(final double LATITUDE, final double LONGITUDE, final double ALTITUDE, final Instant TIMESTAMP, final String INFO) {
        latitude    = LATITUDE;
        longitude   = LONGITUDE;
        altitude    = ALTITUDE;
        timestamp   = TIMESTAMP;
        info        = INFO;
        fireLocationEvent(new LocationEvent(Location.this));
    }
    public void set(final Location LOCATION) {
        name      = LOCATION.getName();
        latitude  = LOCATION.getLatitude();
        longitude = LOCATION.getLongitude();
        altitude  = LOCATION.getAltitude();
        timestamp = LOCATION.getTimestamp();
        info      = LOCATION.info;
        color     = LOCATION.getColor();
        zoomLevel = LOCATION.getZoomLevel();
        fireLocationEvent(new LocationEvent(Location.this));
    }

    public double getDistanceTo(final Location LOCATION) { return calcDistanceInMeter(this, LOCATION); }

    public boolean isWithinRangeOf(final Location LOCATION, final double METERS) { return getDistanceTo(LOCATION) < METERS; }

    public double calcDistanceInMeter(final Location P1, final Location P2) {
        return calcDistanceInMeter(P1.getLatitude(), P1.getLongitude(), P2.getLatitude(), P2.getLongitude());
    }
    public double calcDistanceInKilometer(final Location P1, final Location P2) {
        return calcDistanceInMeter(P1, P2) / 1000.0;
    }
    public double calcDistanceInMeter(final double LAT_1, final double LON_1, final double LAT_2, final double LON_2) {
        final double EARTH_RADIUS      = 6_371_000; // m
        final double LAT_1_RADIANS     = Math.toRadians(LAT_1);
        final double LAT_2_RADIANS     = Math.toRadians(LAT_2);
        final double DELTA_LAT_RADIANS = Math.toRadians(LAT_2-LAT_1);
        final double DELTA_LON_RADIANS = Math.toRadians(LON_2-LON_1);

        final double A = Math.sin(DELTA_LAT_RADIANS * 0.5) * Math.sin(DELTA_LAT_RADIANS * 0.5) + Math.cos(LAT_1_RADIANS) * Math.cos(LAT_2_RADIANS) * Math.sin(DELTA_LON_RADIANS * 0.5) * Math.sin(DELTA_LON_RADIANS * 0.5);
        final double C = 2 * Math.atan2(Math.sqrt(A), Math.sqrt(1-A));

        final double DISTANCE = EARTH_RADIUS * C;

        return DISTANCE;
    }

    public double getAltitudeDifferenceInMeter(final Location LOCATION) { return (altitude - LOCATION.getAltitude()); }

    public double getBearingTo(final Location LOCATION) {
        return calcBearingInDegree(getLatitude(), getLongitude(), LOCATION.getLatitude(), LOCATION.getLongitude());
    }
    public double getBearingTo(final double LATITUDE, final double LONGITUDE) {
        return calcBearingInDegree(getLatitude(), getLongitude(), LATITUDE, LONGITUDE);
    }

    public boolean isZero() { return Double.compare(latitude, 0d) == 0 && Double.compare(longitude, 0d) == 0; }

    public double calcBearingInDegree(final double LAT_1, final double LON_1, final double LAT_2, final double LON_2) {
        double lat1     = Math.toRadians(LAT_1);
        double lon1     = Math.toRadians(LON_1);
        double lat2     = Math.toRadians(LAT_2);
        double lon2     = Math.toRadians(LON_2);
        double deltaLon = lon2 - lon1;
        double deltaPhi = Math.log(Math.tan(lat2 * 0.5 + Math.PI * 0.25) / Math.tan(lat1 * 0.5 + Math.PI * 0.25));
        if (Math.abs(deltaLon) > Math.PI) {
            if (deltaLon > 0) {
                deltaLon = -(2.0 * Math.PI - deltaLon);
            } else {
                deltaLon = (2.0 * Math.PI + deltaLon);
            }
        }
        double bearing = (Math.toDegrees(Math.atan2(deltaLon, deltaPhi)) + 360.0) % 360.0;
        return bearing;
    }

    public String getCardinalDirectionFromBearing(final double BEARING) {
        double bearing = BEARING % 360.0;
        for (CardinalDirection cardinalDirection : CardinalDirection.values()) {
            if (Double.compare(bearing, cardinalDirection.from) >= 0 && Double.compare(bearing, cardinalDirection.to) < 0) {
                return cardinalDirection.direction;
            }
        }
        return "";
    }


    // ******************** Event Handling ************************************
    public void setOnLocationEvent(final LocationEventListener LISTENER) { addLocationEventListener(LISTENER); }
    public void addLocationEventListener(final LocationEventListener LISTENER) { if (!listenerList.contains(LISTENER)) listenerList.add(LISTENER); }
    public void removeLocationEventListener(final LocationEventListener LISTENER) { if (listenerList.contains(LISTENER)) listenerList.remove(LISTENER); }

    public void fireLocationEvent(final LocationEvent EVENT) {
        for (LocationEventListener listener : listenerList) { listener.onLocationEvent(EVENT); }
    }


    // ******************** Misc **********************************************
    @Override public boolean equals(final Object OBJECT) {
        if (OBJECT instanceof Location) {
            final Location LOCATION = (Location) OBJECT;
            return (Double.compare(latitude, LOCATION.latitude) == 0 &&
                    Double.compare(longitude, LOCATION.longitude) == 0 &&
                    Double.compare(altitude, LOCATION.altitude) == 0);
        } else {
            return false;
        }
    }

    public JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("nam", name);
        jsonObject.put("tst", new Long(timestamp.getEpochSecond()));
        jsonObject.put("lat", new Double(latitude));
        jsonObject.put("lon", new Double(longitude));
        jsonObject.put("alt", new Double(altitude));
        jsonObject.put("inf", new String(info));
        jsonObject.put("col", new String(color.toString().replace("0x", "#")));
        jsonObject.put("zml", new Integer(zoomLevel));
        return jsonObject;
    }

    public String toJSONString() {
        return toJSON().toJSONString();
    }

    @Override public String toString() {
        return new StringBuilder().append("Name     : ").append(name).append("\n")
                                  .append("Timestamp: ").append(timestamp).append("\n")
                                  .append("Latitude : ").append(latitude).append("\n")
                                  .append("Longitude: ").append(longitude).append("\n")
                                  .append("Altitude : ").append(String.format(Locale.US, "%.1f", altitude)).append(" m\n")
                                  .append("Info     : ").append(info).append("\n")
                                  .append("Color    : ").append(color.toString().replace("0x", "#")).append("\n")
                                  .append("ZoomLevel: ").append(zoomLevel).append("\n")
                                  .toString();
    }

    @Override public int hashCode() {
        int result;
        long temp;
        result = name != null ? name.hashCode() : 0;
        temp = Double.doubleToLongBits(latitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(altitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
