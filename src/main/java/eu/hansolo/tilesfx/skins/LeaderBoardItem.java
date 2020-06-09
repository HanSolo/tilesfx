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

package eu.hansolo.tilesfx.skins;

import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.Tile.ItemSortingTopic;
import eu.hansolo.tilesfx.chart.ChartData;
import eu.hansolo.tilesfx.events.ChartDataEvent;
import eu.hansolo.tilesfx.events.ChartDataEvent.EventType;
import eu.hansolo.tilesfx.events.ChartDataEventListener;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.Helper;
import javafx.beans.DefaultProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.text.Text;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;


/**
 * User: hansolo
 * Date: 11.01.17
 * Time: 08:15
 */
@DefaultProperty("children")
public class LeaderBoardItem extends Region implements Comparable<LeaderBoardItem> {
    public enum State {
        RISE(Tile.GREEN, 0),
        FALL(Tile.RED, 180),
        CONSTANT(Color.TRANSPARENT, 90);

        public final Color  color;
        public final double angle;

        State(final Color COLOR, final double ANGLE) {
            color = COLOR;
            angle = ANGLE;
        }
    }
    private static final double                PREFERRED_WIDTH  = 250;
    private static final double                PREFERRED_HEIGHT = 30;
    private static final double                MINIMUM_WIDTH    = 25;
    private static final double                MINIMUM_HEIGHT   = 25;
    private static final double                MAXIMUM_WIDTH    = 1024;
    private static final double                MAXIMUM_HEIGHT   = 72;
    private static final double                ASPECT_RATIO     = PREFERRED_HEIGHT / PREFERRED_WIDTH;
    private              double                width;
    private              double                height;
    private              double                size;
    private              double                parentWidth;
    private              double                parentHeight;
    private              Text                  nameText;
    private              Text                  valueText;
    private              Path                  triangle;
    private              Line                  separator;
    private              Pane                  pane;
    private              ChartData             chartData;
    private              ObjectProperty<Color> nameColor;
    private              ObjectProperty<Color> valueColor;
    private              ObjectProperty<Color> separatorColor;
    private              State                 state;
    private              String                formatString;
    private              String                durationFormatString;
    private              DateTimeFormatter     timestampFormatter;
    private              Locale                locale;
    private              int                   index;
    private              int                   lastIndex;
    private              ItemSortingTopic      itemSortingTopic;



    // ******************** Constructors **************************************
    public LeaderBoardItem() {
        this("", 0, Instant.now(), Duration.ZERO);
    }
    public LeaderBoardItem(final String NAME) {
        this(NAME, 0, Instant.now(), Duration.ZERO);
    }
    public LeaderBoardItem(final String NAME, final double VALUE) {
        this(NAME, VALUE, Instant.now(), Duration.ZERO);
    }
    public LeaderBoardItem(final String NAME, final Instant TIMESTAMP) {
        this(NAME, 0, TIMESTAMP, Duration.ZERO);
    }
    public LeaderBoardItem(final String NAME, final Duration DURATION) {
        this(NAME, 0, Instant.now(), DURATION);
    }
    public LeaderBoardItem(final String NAME, final double VALUE, final Instant TIMESTAMP) {
        this(NAME, VALUE, TIMESTAMP, Duration.ZERO);
    }
    public LeaderBoardItem(final String NAME, final double VALUE, final Duration DURATION) {
        this(NAME, VALUE, Instant.now(), DURATION);
    }
    public LeaderBoardItem(final String NAME, final double VALUE, final Instant TIMESTAMP, final Duration DURATION) {
        chartData            = new ChartData(NAME, VALUE, TIMESTAMP, DURATION);
        nameColor            = new ObjectPropertyBase<>(Tile.FOREGROUND) {
            @Override protected void invalidated() { nameText.setFill(get()); }
            @Override public Object getBean() { return LeaderBoardItem.this; }
            @Override public String getName() { return "nameColor"; }
        };
        valueColor           = new ObjectPropertyBase<>(Tile.FOREGROUND) {
            @Override protected void invalidated() {  valueText.setFill(get()); }
            @Override public Object getBean() { return LeaderBoardItem.this; }
            @Override public String getName() { return "valueColor"; }
        };
        separatorColor       = new ObjectPropertyBase<>(Color.rgb(72, 72, 72)) {
            @Override protected void invalidated() { separator.setStroke(get()); }
            @Override public Object getBean() { return LeaderBoardItem.this; }
            @Override public String getName() { return "separatorColor"; }
        };
        itemSortingTopic     = ItemSortingTopic.VALUE;
        formatString         = "%.0f";
        durationFormatString = "%d:%02d:%02d";
        timestampFormatter   = DateTimeFormatter.ofPattern("dd.MM.yyyy hh:mm:ss");
        locale               = Locale.US;
        index                = 1024;
        lastIndex            = 1024;
        parentWidth          = 250;
        parentHeight         = 250;

        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void initGraphics() {
        if (Double.compare(getPrefWidth(), 0.0) <= 0 || Double.compare(getPrefHeight(), 0.0) <= 0 ||
            Double.compare(getWidth(), 0.0) <= 0 || Double.compare(getHeight(), 0.0) <= 0) {
            if (getPrefWidth() > 0 && getPrefHeight() > 0) {
                setPrefSize(getPrefWidth(), getPrefHeight());
            } else {
                setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT); // 11x7
            }
        }

        state = State.CONSTANT;

        triangle = new Path();
        triangle.setStroke(null);
        triangle.setFill(state.color);
        triangle.setRotate(state.angle);

        nameText = new Text(getName());
        nameText.setTextOrigin(VPos.TOP);

        valueText = new Text();
        valueText.setTextOrigin(VPos.TOP);
        updateValueText();

        separator = new Line();

        pane = new Pane(triangle, nameText, valueText, separator);
        pane.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        widthProperty().addListener(o -> resize());
        heightProperty().addListener(o -> resize());
    }


    // ******************** Methods *******************************************
    @Override protected double computeMinWidth(final double HEIGHT) { return MINIMUM_WIDTH; }
    @Override protected double computeMinHeight(final double WIDTH) { return MINIMUM_HEIGHT; }
    @Override protected double computePrefWidth(final double HEIGHT) { return super.computePrefWidth(HEIGHT); }
    @Override protected double computePrefHeight(final double WIDTH) { return super.computePrefHeight(WIDTH); }
    @Override protected double computeMaxWidth(final double HEIGHT) { return MAXIMUM_WIDTH; }
    @Override protected double computeMaxHeight(final double WIDTH) { return MAXIMUM_HEIGHT; }

    @Override public ObservableList<Node> getChildren() { return super.getChildren(); }

    public String getName() { return chartData.getName(); }
    public void setName(final String NAME) { chartData.setName(NAME); }

    public double getValue() { return chartData.getValue(); }
    public void setValue(final double VALUE) { chartData.setValue(VALUE); }

    public Instant getTimestamp() { return chartData.getTimestamp(); }
    public void setTimestamp(final Instant TIMESTAMP) { chartData.setTimestamp(TIMESTAMP); }

    public Duration getDuration() { return chartData.getDuration(); }
    public void setDuration(final Duration DURATION) { chartData.setDuration(DURATION); }

    public Color getNameColor() { return nameColor.get(); }
    public void setNameColor(final Color COLOR) { nameColor.set(COLOR); }
    public ObjectProperty<Color> nameColorProperty() { return nameColor; }

    public Color getValueColor() { return valueColor.get(); }
    public void setValueColor(final Color COLOR) { valueColor.set(COLOR); }
    public ObjectProperty<Color> valueColorProperty() { return valueColor; }

    public ChartData getChartData() { return chartData; }
    public void setChartData(final ChartData DATA) {
        chartData = DATA;
        chartData.fireChartDataEvent(new ChartDataEvent(EventType.UPDATE, chartData));
    }

    public Color getSeparatorColor() { return separatorColor.get(); }
    public void setSeparatorColor(final Color COLOR) { separatorColor.set(COLOR); }
    public ObjectProperty<Color> separatorColorProperty() { return separatorColor; }

    public int getIndex() { return index; }
    public void setIndex(final int INDEX) {
        lastIndex = index;
        index     = INDEX;
        if (index > lastIndex) {
            state = State.FALL;
        } else if (index < lastIndex) {
            state = State.RISE;
        } else {
            state = State.CONSTANT;
        }
        triangle.setFill(state.color);
        triangle.setRotate(state.angle);

        updateValueText();
        valueText.relocate((parentWidth - size * 0.05) - valueText.getLayoutBounds().getWidth(), 0);
    }

    public int getLastIndex() { return lastIndex; }

    public State getState() { return state; }

    @Override public int compareTo(final LeaderBoardItem ITEM) {
        switch(itemSortingTopic) {
            case DURATION : return Long.compare(getDuration().toMillis(), ITEM.getDuration().toMillis());
            case TIMESTAMP: return Long.compare(getTimestamp().toEpochMilli(), ITEM.getTimestamp().toEpochMilli());
            case VALUE    :
            default       : return Double.compare(getValue(), ITEM.getValue());
        }
    }

    public void setLocale(final Locale LOCALE) {
        locale = LOCALE;
        updateValueText();
    }

    public void setItemSortingTopic(final ItemSortingTopic ITEM_SORTING_TOPIC) {
        itemSortingTopic = ITEM_SORTING_TOPIC;
        updateValueText();
    }

    public void setFormatString(final String FORMAT_STRING) {
        formatString = FORMAT_STRING;
        updateValueText();
    }

    public void setDurationFormatString(final String DURATION_FORMAT_STRING) {
        durationFormatString = DURATION_FORMAT_STRING;
        updateValueText();
    }

    public void setTimestampFormatter(final DateTimeFormatter TIMESTAMP_FORMATTER) {
        timestampFormatter = TIMESTAMP_FORMATTER;
        updateValueText();
    }

    protected void setParentSize(final double WIDTH, final double HEIGHT) {
        parentWidth  = WIDTH;
        parentHeight = HEIGHT;
        resize();
    }

    private void drawTriangle() {
        MoveTo    moveTo    = new MoveTo(0, 0.028 * size);
        LineTo    lineTo1   = new LineTo(0.022 * size, 0);
        LineTo    lineTo2   = new LineTo(0.044 * size, 0.028 * size);
        LineTo    lineTo3   = new LineTo(0, 0.028 * size);
        ClosePath closePath = new ClosePath();
        triangle.getElements().setAll(moveTo, lineTo1, lineTo2, lineTo3, closePath);
    }

    private void updateValueText() {
        switch (itemSortingTopic) {
            case DURATION:
                long seconds = chartData.getDuration().getSeconds();
                long absSeconds = Math.abs(seconds);
                valueText.setText(String.format(locale, durationFormatString, absSeconds / 3600, (absSeconds % 3600) / 60, absSeconds % 60));
                break;
            case TIMESTAMP: valueText.setText(timestampFormatter.format(ZonedDateTime.ofInstant(getTimestamp(), ZoneId.systemDefault()))); break;
            case VALUE    :
            default       : valueText.setText(String.format(locale, formatString, getValue())); break;
        }
    }


    // ******************** Event Handling ************************************
    public void setOnChartDataEvent(final ChartDataEventListener LISTENER) { chartData.addChartDataEventListener(LISTENER); }
    public void addChartDataEventListener(final ChartDataEventListener LISTENER) { chartData.addChartDataEventListener(LISTENER); }
    public void removeChartDataEventListener(final ChartDataEventListener LISTENER) { chartData.removeChartDataEventListener(LISTENER); }


    // ******************** Resizing ******************************************
    private void resize() {
        width  = getWidth() - getInsets().getLeft() - getInsets().getRight();
        height = getHeight() - getInsets().getTop() - getInsets().getBottom();
        size   = parentWidth < parentHeight ? parentWidth : parentHeight;

        if (ASPECT_RATIO * width > height) {
            width = 1 / (ASPECT_RATIO / height);
        } else if (1 / (ASPECT_RATIO / height) > width) {
            height = ASPECT_RATIO * width;
        }

        if (width > 0 && height > 0) {
            double itemHeight = Helper.clamp(MINIMUM_HEIGHT, MAXIMUM_HEIGHT, height * 0.14);
            pane.setMinSize(parentWidth, itemHeight);
            pane.setMaxSize(parentWidth, itemHeight);
            pane.setPrefSize(parentWidth, itemHeight);

            drawTriangle();
            
            triangle.setLayoutX(size * 0.05);
            triangle.setLayoutY((height - triangle.getBoundsInLocal().getHeight()) * 0.25);

            double fontSize = Helper.clamp(12, MAXIMUM_HEIGHT * 0.5, size * 0.06);

            nameText.setFont(Fonts.latoRegular(fontSize));
            nameText.setX(size * 0.12);
            nameText.setY(0);

            valueText.setFont(Fonts.latoRegular(fontSize));
            valueText.relocate((parentWidth - size * 0.05) - valueText.getLayoutBounds().getWidth(), 0);

            separator.setStartX(size * 0.05);
            separator.setStartY(fontSize * 1.5);
            separator.setEndX(parentWidth - size * 0.05);
            separator.setEndY(fontSize * 1.5);

            redraw();
        }
    }

    private void redraw() {
        nameText.setFill(getNameColor());
        valueText.setFill(getValueColor());
        triangle.setFill(state.color);
        separator.setStroke(getSeparatorColor());
    }

    @Override public String toString() {
        return new StringBuilder(getName()).append(",").append(getValue()).append(",").toString();
    }
}
