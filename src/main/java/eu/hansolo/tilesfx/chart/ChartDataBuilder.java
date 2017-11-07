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

package eu.hansolo.tilesfx.chart;

import eu.hansolo.tilesfx.events.ChartDataEventListener;
import eu.hansolo.tilesfx.tools.Location;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.HashMap;


public class ChartDataBuilder<B extends ChartDataBuilder<B>> {
    private HashMap<String, Property> properties = new HashMap<>();


    // ******************** Constructors **************************************
    protected ChartDataBuilder() {}


    // ******************** Methods *******************************************
    public static final ChartDataBuilder create() {
        return new ChartDataBuilder();
    }

    public final B name(final String NAME) {
        properties.put("name", new SimpleStringProperty(NAME));
        return (B)this;
    }

    public final B value(final double VALUE) {
        properties.put("value", new SimpleDoubleProperty(VALUE));
        return (B)this;
    }

    public final B timestamp(final Instant TIMESTAMP) {
        properties.put("timestamp", new SimpleObjectProperty(TIMESTAMP));
        return (B)this;
    }
    public final B timestamp(final ZonedDateTime TIMESTAMP) {
        properties.put("timestamp", new SimpleObjectProperty(TIMESTAMP.toInstant()));
        return (B)this;
    }

    public final B location(final Location LOCATION) {
        properties.put("location", new SimpleObjectProperty(LOCATION));
        return (B)this;
    }

    public final B fillColor(final Color COLOR) {
        properties.put("fillColor", new SimpleObjectProperty(COLOR));
        return (B)this;
    }

    public final B strokeColor(final Color COLOR) {
        properties.put("strokeColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B textColor(final Color COLOR) {
        properties.put("textColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B animated(final boolean ANIMATED) {
        properties.put("animated", new SimpleBooleanProperty(ANIMATED));
        return (B)this;
    }

    public final B onChartDataEvent(final ChartDataEventListener HANDLER) {
        properties.put("onChartDataEvent", new SimpleObjectProperty<>(HANDLER));
        return (B)this;
    }
    

    public final ChartData build() {
        final ChartData DATA = new ChartData();
        for (String key : properties.keySet()) {
            if ("name".equals(key)) {
                DATA.setName(((StringProperty) properties.get(key)).get());
            } else if("value".equals(key)) {
                DATA.setValue(((DoubleProperty) properties.get(key)).get());
            } else if ("timestamp".equals(key)) {
                DATA.setTimestamp(((ObjectProperty<Instant>) properties.get(key)).get());
            } else if ("location".equals(key)) {
                DATA.setLocation(((ObjectProperty<Location>) properties.get(key)).get());
            } else if ("fillColor".equals(key)) {
                DATA.setFillColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("strokeColor".equals(key)) {
                DATA.setStrokeColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("textColor".equals(key)) {
                DATA.setTextColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("animated".equals(key)) {
                DATA.setAnimated(((BooleanProperty) properties.get(key)).get());
            } else if ("onChartDataEvent".equals(key)) {
                DATA.setOnChartDataEvent(((ObjectProperty<ChartDataEventListener>) properties.get(key)).get());
            }
        }
        return DATA;
    }
}
