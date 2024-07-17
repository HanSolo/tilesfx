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
        properties.forEach((key, property) -> {
            switch (key) {
                case "name"               -> item.setName(((StringProperty) property).get());
                case "value"              -> item.setValue(((DoubleProperty) property).get());
                case "timestamp"          -> item.setTimestamp(((ObjectProperty<Instant>) property).get());
                case "duration"           -> item.setDuration(((ObjectProperty<java.time.Duration>) property).get());
                case "barColor"           -> item.setBarColor(((ObjectProperty<Color>) property).get());
                case "barBackgroundColor" -> item.setBarBackgroundColor(((ObjectProperty<Color>) property).get());
                case "nameColor"          -> item.setNameColor(((ObjectProperty<Color>) property).get());
                case "valueColor"         -> item.setValueColor(((ObjectProperty<Color>) property).get());
                case "formatString"       -> item.setFormatString(((StringProperty) property).get());
                case "maxValue"           -> item.setMaxValue(((DoubleProperty) property).get());
                case "shortenNumbers"     -> item.setShortenNumbers(((BooleanProperty) property).get());
                case "percentageVisible"  -> item.setPercentageVisible(((BooleanProperty) property).get());
                case "onChartDataEvent"   -> item.setOnChartDataEvent(((ObjectProperty<ChartDataEventListener>) property).get());
            }
        });
        return item;
    }
}
