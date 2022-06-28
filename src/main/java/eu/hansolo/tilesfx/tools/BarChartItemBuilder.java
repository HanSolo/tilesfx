/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2016-2022 Gerrit Grunwald.
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

import eu.hansolo.tilesfx.chart.ChartData;
import eu.hansolo.tilesfx.events.ChartDataEventListener;
import eu.hansolo.tilesfx.skins.BarChartItem;
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


public class BarChartItemBuilder<B extends BarChartItemBuilder<B>> {
    private HashMap<String, Property> properties = new HashMap<>();


    // ******************** Constructors **************************************
    protected BarChartItemBuilder() {}


    // ******************** Methods *******************************************
    public static final BarChartItemBuilder create() {
        return new BarChartItemBuilder();
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
        properties.put("timestamp", new SimpleObjectProperty<>(TIMESTAMP));
        return (B)this;
    }
    public final B timestamp(final ZonedDateTime TIMESTAMP) {
        properties.put("timestamp", new SimpleObjectProperty<>(TIMESTAMP.toInstant()));
        return (B)this;
    }

    public final B duration(final java.time.Duration DURATION) {
        properties.put("duration", new SimpleObjectProperty<>(DURATION));
        return (B)this;
    }

    public final B shortenNumbers(final boolean SHORTEN) {
        properties.put("shortenNumbers", new SimpleBooleanProperty(SHORTEN));
        return (B)this;
    }

    public final B percentageVisible(final boolean VISIBLE) {
        properties.put("percentageVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B nameColor(final Color COLOR) {
        properties.put("nameColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B valueColor(final Color COLOR) {
        properties.put("valueColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B barColor(final Color COLOR) {
        properties.put("barColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B barBackgroundColor(final Color COLOR) {
        properties.put("barBackgroundColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B chartData(final ChartData CHART_DATA) {
        properties.put("chartData", new SimpleObjectProperty<>(CHART_DATA));
        return (B)this;
    }

    public final BarChartItem build() {
        final BarChartItem item = new BarChartItem();
        for (String key : properties.keySet()) {
            if ("name".equals(key)) {
                item.setName(((StringProperty) properties.get(key)).get());
            } else if("value".equals(key)) {
                item.setValue(((DoubleProperty) properties.get(key)).get());
            } else if ("timestamp".equals(key)) {
                item.setTimestamp(((ObjectProperty<Instant>) properties.get(key)).get());
            } else if ("duration".equals(key)) {
                item.setDuration(((ObjectProperty<java.time.Duration>) properties.get(key)).get());
            } else if ("barColor".equals(key)) {
                item.setBarColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("barBackgroundColor".equals(key)) {
                item.setBarBackgroundColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("nameColor".equals(key)) {
                item.setNameColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("valueColor".equals(key)) {
                item.setValueColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if("formatString".equals(key)) {
                item.setFormatString(((StringProperty) properties.get(key)).get());
            } else if("maxValue".equals(key)) {
                item.setMaxValue(((DoubleProperty) properties.get(key)).get());
            } else if ("shortenNumbers".equals(key)) {
                item.setShortenNumbers(((BooleanProperty) properties.get(key)).get());
            } else if ("percentageVisible".equals(key)) {
                item.setPercentageVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("onChartDataEvent".equals(key)) {
                item.setOnChartDataEvent(((ObjectProperty<ChartDataEventListener>) properties.get(key)).get());
            }
        }
        return item;
    }
}
