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

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;

import java.time.Instant;
import java.util.HashMap;


/**
 * Created by hansolo on 14.02.17.
 */
public class LocationBuilder<B extends LocationBuilder<B>> {
    private HashMap<String, Property> properties = new HashMap<>();


    // ******************** Constructors **************************************
    protected LocationBuilder() {}


    // ******************** Methods *******************************************
    public static final LocationBuilder create() {
        return new LocationBuilder();
    }

    public final B name(final String NAME) {
        properties.put("name", new SimpleStringProperty(NAME));
        return (B) this;
    }

    public final B timestamp(final Instant TIMESTAMP) {
        properties.put("timestamp", new SimpleObjectProperty<>(TIMESTAMP));
        return (B) this;
    }

    public final B latitude(final double LATITUDE) {
        properties.put("latitude", new SimpleDoubleProperty(LATITUDE));
        return (B) this;
    }

    public final B longitude(final double LONGITUDE) {
        properties.put("longitude", new SimpleDoubleProperty(LONGITUDE));
        return (B) this;
    }

    public final B altitude(final double ALTITUDE) {
        properties.put("altitude", new SimpleDoubleProperty(ALTITUDE));
        return (B) this;
    }

    public final B info(final String INFO) {
        properties.put("info", new SimpleStringProperty(INFO));
        return (B) this;
    }

    public final B color(final Color COLOR) {
        properties.put("color", new SimpleObjectProperty(COLOR));
        return (B) this;
    }

    public final B zoomLevel(final int LEVEL) {
        properties.put("zoomLevel", new SimpleIntegerProperty(LEVEL));
        return (B) this;
    }

    public final Location build() {
        Location location = new Location();
        properties.forEach((key, property) -> {
            if ("name".equals(key)) {
                location.setName(((StringProperty) properties.get(key)).get());
            } else if ("timestamp".equals(key)) {
                location.setTimestamp(((ObjectProperty<Instant>) properties.get(key)).get());
            } else if ("latitude".equals(key)) {
                location.setLatitude(((DoubleProperty) properties.get(key)).get());
            } else if ("longitude".equals(key)) {
                location.setLongitude(((DoubleProperty) properties.get(key)).get());
            } else if ("altitude".equals(key)) {
                location.setAltitude(((DoubleProperty) properties.get(key)).get());
            } else if ("info".equals(key)) {
                location.setInfo(((StringProperty) properties.get(key)).get());
            } else if ("color".equals(key)) {
                location.setColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("zoomLevel".equals(key)) {
                location.setZoomLevel(((IntegerProperty) properties.get(key)).get());
            }

        });
        return location;
    }
}
