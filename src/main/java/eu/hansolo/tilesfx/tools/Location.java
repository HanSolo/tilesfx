/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2016-2021 Gerrit Grunwald.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.hansolo.tilesfx.tools;

import eu.hansolo.tilesfx.Tile.TileColor;
import eu.hansolo.tilesfx.events.LocationEvt;
import eu.hansolo.toolbox.evt.EvtObserver;
import eu.hansolo.toolbox.geom.CardinalDirection;
import javafx.scene.paint.Color;

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
    private String                         name;
    private Instant                        timestamp;
    private double                         latitude;
    private double                         longitude;
    private double                         altitude;
    private String                         info;
    private Color                          color;
    private int                            zoomLevel;
    private List<EvtObserver<LocationEvt>> observers;


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
        zoomLevel = 15;
        observers = new CopyOnWriteArrayList<>();
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
        fireLocationEvent(new LocationEvt(Location.this, LocationEvt.LOCATION, Location.this));
    }

    public double getLongitude() { return longitude; }
    public void setLongitude(final double LONGITUDE) {
        longitude = LONGITUDE;
        fireLocationEvent(new LocationEvt(Location.this, LocationEvt.LOCATION, Location.this));
    }

    public double getAltitude() { return altitude; }
    public void setAltitude(final double ALTITUDE) {
        altitude = ALTITUDE;
        fireLocationEvent(new LocationEvt(Location.this, LocationEvt.LOCATION, Location.this));
    }

    public String getInfo() { return info; }
    public void setInfo(final String INFO) { info = INFO; }

    public Color getColor() { return color; }
    public void setColor(final Color COLOR) {
        color = COLOR;
        fireLocationEvent(new LocationEvt(Location.this, LocationEvt.LOCATION, Location.this));
    }

    public ZonedDateTime getZonedDateTime() { return getZonedDateTime(ZoneId.systemDefault()); }
    public ZonedDateTime getZonedDateTime(final ZoneId ZONE_ID) { return ZonedDateTime.ofInstant(timestamp, ZONE_ID); }

    public int getZoomLevel() { return zoomLevel; }
    public void setZoomLevel(final int LEVEL) {
        zoomLevel = clamp(0, 17, LEVEL);
        fireLocationEvent(new LocationEvt(Location.this, LocationEvt.LOCATION, Location.this));
    }

    public void update(final double LATITUDE, final double LONGITUDE) { set(LATITUDE, LONGITUDE); }

    public void set(final double LATITUDE, final double LONGITUDE) {
        latitude  = LATITUDE;
        longitude = LONGITUDE;
        timestamp = Instant.now();
        fireLocationEvent(new LocationEvt(Location.this, LocationEvt.LOCATION, Location.this));
    }
    public void set(final double LATITUDE, final double LONGITUDE, final double ALTITUDE, final Instant TIMESTAMP) {
        latitude  = LATITUDE;
        longitude = LONGITUDE;
        altitude  = ALTITUDE;
        timestamp = TIMESTAMP;
        fireLocationEvent(new LocationEvt(Location.this, LocationEvt.LOCATION, Location.this));
    }
    public void set(final double LATITUDE, final double LONGITUDE, final double ALTITUDE, final Instant TIMESTAMP, final String INFO) {
        latitude  = LATITUDE;
        longitude = LONGITUDE;
        altitude  = ALTITUDE;
        timestamp = TIMESTAMP;
        info      = INFO;
        fireLocationEvent(new LocationEvt(Location.this, LocationEvt.LOCATION, Location.this));
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
        fireLocationEvent(new LocationEvt(Location.this, LocationEvt.LOCATION, Location.this));
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
    public void setOnLocationEvt(final EvtObserver<LocationEvt> OBSERVER) { addLocationEvtObserver(OBSERVER); }
    public void addLocationEvtObserver(final EvtObserver<LocationEvt> OBSERVER)    { if (!observers.contains(OBSERVER)) observers.add(OBSERVER); }
    public void removeLocationEvtObserver(final EvtObserver<LocationEvt> OBSERVER) { if (observers.contains(OBSERVER)) observers.remove(OBSERVER); }
    public void removeAllLocationEvtObservers() { observers.clear(); }

    public void fireLocationEvent(final LocationEvt EVENT) {
        observers.forEach(observer -> observer.handle(EVENT));
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

    public String toJSONString() {
        return new StringBuilder().append("{")
                           .append("\"nam\":\"").append(name).append("\",")
                           .append("\"tst\":").append(timestamp.getEpochSecond()).append(",")
                           .append("\"lat\":").append(latitude).append(",")
                           .append("\"lon\":").append(longitude).append(",")
                           .append("\"alt\":").append(altitude).append(",")
                           .append("\"inf\":\"").append(info).append("\",")
                           .append("\"col\":\"").append(color.toString().replace("0x", "#")).append("\",")
                           .append("\"zml\":").append(zoomLevel).append(",")
                           .append("}")
                           .toString();
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
