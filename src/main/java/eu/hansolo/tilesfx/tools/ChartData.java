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

import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.events.ChartDataEvent;
import eu.hansolo.tilesfx.events.ChartDataEventListener;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Created by hansolo on 17.02.17.
 */
public class ChartData {
    public  static boolean               animated     = true;
    private final ChartDataEvent         UPDATE_EVENT = new ChartDataEvent(ChartData.this);
    private String                       name;
    private double                       value;
    private Color                        color;
    private Instant                      timestamp;
    private List<ChartDataEventListener> listenerList = new CopyOnWriteArrayList<>();
    private DoubleProperty               currentValue;
    private Timeline                     timeline;


    // ******************** Constructors **************************************
    public ChartData() {
        this("", 0, Tile.BLUE, Instant.now());
    }
    public ChartData(double VALUE) {
        this("", VALUE, Tile.BLUE, Instant.now());
    }
    public ChartData(final double VALUE, final Instant TIMESTAMP) {
        this("", VALUE, Tile.BLUE, TIMESTAMP);
    }
    public ChartData(final String NAME, final double VALUE) {
        this(NAME, VALUE, Tile.BLUE, Instant.now());
    }
    public ChartData(final String NAME, final double VALUE, final Instant TIMESTAMP) {
        this(NAME, VALUE, Tile.BLUE, TIMESTAMP);
    }
    public ChartData(final String NAME, final double VALUE, final Color COLOR) {
        this(NAME, VALUE, COLOR, Instant.now());
    }
    public ChartData(final String NAME, final double VALUE, final Color COLOR, final Instant TIMESTAMP) {
        name         = NAME;
        value        = VALUE;
        color        = COLOR;
        timestamp    = TIMESTAMP;
        currentValue = new DoublePropertyBase(value) {
            @Override protected void invalidated() {
                value = get();
                fireChartDataEvent(UPDATE_EVENT);
            }
            @Override public Object getBean() { return ChartData.this; }
            @Override public String getName() { return "currentValue"; }
        };
        timeline     = new Timeline();
        animated     = true;
    }


    // ******************** Methods *******************************************
    public String getName() { return name; }
    public void setName(final String NAME) {
        name = NAME;
        fireChartDataEvent(UPDATE_EVENT);
    }

    public double getValue() { return value; }
    public void setValue(final double VALUE) {
        if (animated) {
            timeline.stop();
            KeyValue kv1 = new KeyValue(currentValue, value, Interpolator.EASE_BOTH);
            KeyValue kv2 = new KeyValue(currentValue, VALUE, Interpolator.EASE_BOTH);
            KeyFrame kf1 = new KeyFrame(Duration.ZERO, kv1);
            KeyFrame kf2 = new KeyFrame(Duration.millis(800), kv2);
            timeline.getKeyFrames().setAll(kf1, kf2);
            timeline.play();
        } else {
            value = VALUE;
            fireChartDataEvent(UPDATE_EVENT);
        }
    }

    public Color getColor() { return color; }
    public void setColor(final Color COLOR) {
        color = COLOR;
        fireChartDataEvent(UPDATE_EVENT);
    }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(final Instant TIMESTAMP) {
        timestamp = TIMESTAMP;
        fireChartDataEvent(UPDATE_EVENT);
    }

    public ZonedDateTime getTimestampAsDateTime(final ZoneId ZONE_ID) { return ZonedDateTime.ofInstant(timestamp, ZONE_ID); }

    @Override public String toString() {
        return new StringBuilder().append("{\n")
                                  .append("  \"name\":").append(name).append(",\n")
                                  .append("  \"value\":").append(value).append(",\n")
                                  .append("  \"color\":").append(color.toString().replace("0x", "#")).append(",\n")
                                  .append("  \"timestamp\":").append(timestamp.toEpochMilli()).append(",\n")
                                  .append("}")
                                  .toString();
    }


    // ******************** Event Handling ************************************
    public void setOnChartDataEvent(final ChartDataEventListener LISTENER) { addChartDataEventListener(LISTENER); }
    public void addChartDataEventListener(final ChartDataEventListener LISTENER) { if (!listenerList.contains(LISTENER)) listenerList.add(LISTENER); }
    public void removeChartDataEventListener(final ChartDataEventListener LISTENER) { if (listenerList.contains(LISTENER)) listenerList.remove(LISTENER); }

    public void fireChartDataEvent(final ChartDataEvent EVENT) {
        for (ChartDataEventListener listener : listenerList) { listener.onChartDataEvent(EVENT); }
    }
}
