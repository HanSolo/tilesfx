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
import eu.hansolo.tilesfx.chart.ChartData;
import eu.hansolo.tilesfx.events.ChartDataEvent;
import eu.hansolo.tilesfx.events.ChartDataEvent.EventType;
import eu.hansolo.tilesfx.events.ChartDataEventListener;
import eu.hansolo.tilesfx.fonts.Fonts;
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
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.util.Locale;

import static eu.hansolo.tilesfx.tools.Helper.clamp;


/**
 * User: hansolo
 * Date: 23.12.16
 * Time: 13:10
 */
@DefaultProperty("children")
public class BarChartItem extends Region implements Comparable<BarChartItem>{
    private static final double                PREFERRED_WIDTH  = 250;
    private static final double                PREFERRED_HEIGHT = 30;
    private static final double                MINIMUM_WIDTH    = 25;
    private static final double                MINIMUM_HEIGHT   = 3.6;
    private static final double                MAXIMUM_WIDTH    = 1024;
    private static final double                MAXIMUM_HEIGHT   = 1024;
    private static final double                ASPECT_RATIO     = PREFERRED_HEIGHT / PREFERRED_WIDTH;
    private              double                width;
    private              double                height;
    private              double                size;
    private              double                parentWidth;
    private              double                parentHeight;
    private              Text                  nameText;
    private              Text                  valueText;
    private              Rectangle             barBackground;
    private              Rectangle             bar;
    private              Pane                  pane;
    private              ObjectProperty<Color> nameColor;
    private              ObjectProperty<Color> valueColor;
    private              ObjectProperty<Color> barBackgroundColor;
    private              String                formatString;
    private              Locale                locale;
    private              double                maxValue;
    private              double                stepSize;
    private              ChartData             chartData;


    // ******************** Constructors **************************************
    public BarChartItem() {
        this("", 0, Tile.BLUE);
    }
    public BarChartItem(final String NAME) {
        this(NAME, 0, Tile.BLUE);
    }
    public BarChartItem(final String NAME, final double VALUE) {
        this(NAME, VALUE, Tile.BLUE);
    }
    public BarChartItem(final String NAME, final double VALUE, final Color COLOR) {
        nameColor          = new ObjectPropertyBase<Color>(Tile.FOREGROUND) {
            @Override protected void invalidated() { nameText.setFill(get()); }
            @Override public Object getBean() { return BarChartItem.this; }
            @Override public String getName() { return "nameColor"; }
        };
        valueColor         = new ObjectPropertyBase<Color>(Tile.FOREGROUND) {
            @Override protected void invalidated() {  valueText.setFill(get()); }
            @Override public Object getBean() { return BarChartItem.this; }
            @Override public String getName() { return "valueColor"; }
        };
        barBackgroundColor = new ObjectPropertyBase<Color>(Color.rgb(72, 72, 72)) {
            @Override protected void invalidated() { barBackground.setFill(get()); }
            @Override public Object getBean() { return BarChartItem.this; }
            @Override public String getName() { return "barBackgroundColor"; }
        };
        formatString       = "%.0f";
        locale             = Locale.US;
        maxValue           = 100;
        chartData          = new ChartData(NAME, VALUE, COLOR);
        stepSize           = PREFERRED_WIDTH * 0.85 / maxValue;
        parentWidth        = 250;
        parentHeight       = 250;
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
                setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            }
        }

        nameText = new Text(getName());
        nameText.setTextOrigin(VPos.TOP);

        valueText = new Text(String.format(locale, formatString, getValue()));
        valueText.setTextOrigin(VPos.TOP);

        barBackground = new Rectangle();

        bar = new Rectangle();

        pane = new Pane(nameText, valueText, barBackground, bar);
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

    public Color getBarBackgroundColor() { return barBackgroundColor.get(); }
    public void setBarBackgroundColor(final Color COLOR) { barBackgroundColor.set(COLOR); }
    public ObjectProperty<Color> barBackgroundColorProperty() { return barBackgroundColor; }

    public Color getBarColor() { return chartData.getFillColor(); }
    public void setBarColor(final Color COLOR) { chartData.setFillColor(COLOR); }

    @Override public int compareTo(final BarChartItem ITEM) { return Double.compare(getValue(), ITEM.getValue()); }

    public void setStepSize(final double STEP_SIZE) {
        stepSize = STEP_SIZE;
        updateBar(getValue());
    }

    public void setMaxValue(final double MAX_VALUE) {
        maxValue = MAX_VALUE;
        stepSize = (parentWidth - size * 0.15) / maxValue;
        updateBar(getValue());
    }

    public void setLocale(final Locale LOCALE) {
        locale = LOCALE;
        valueText.setText(String.format(locale, formatString, getValue()));
    }

    public String getFormatString() { return formatString; }
    public void setFormatString(final String FORMAT_STRING) {
        formatString = FORMAT_STRING;
        valueText.setText(String.format(locale, formatString, getValue()));
    }

    protected void setParentSize(final double WIDTH, final double HEIGHT) {
        parentWidth  = WIDTH;
        parentHeight = HEIGHT;
        resize();
    }

    private void updateBar(final double VALUE) {
        valueText.setText(String.format(locale, formatString, VALUE));
        valueText.setX((parentWidth - size * 0.05) - valueText.getLayoutBounds().getWidth());
        bar.setWidth(clamp(0, (parentWidth - size * 0.15), VALUE * stepSize));
        bar.setFill(getBarColor());
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
            stepSize = (parentWidth - size * 0.15) / maxValue;

            pane.setMaxSize(parentWidth, height * 0.12);
            pane.setPrefSize(parentWidth, height * 0.12);

            nameText.setFont(Fonts.latoRegular(size * 0.06));
            nameText.setX(size * 0.05);
            nameText.setY(0);

            valueText.setFont(Fonts.latoRegular(size * 0.06));
            valueText.setX((parentWidth - size * 0.05) - valueText.getLayoutBounds().getWidth());
            valueText.setY(0);

            barBackground.setX(size * 0.075);
            barBackground.setY(size * 0.10333333);
            barBackground.setWidth(parentWidth - size * 0.15);
            barBackground.setHeight(size * 0.01);

            bar.setX(size * 0.075);
            bar.setY(size * 0.09666667);
            bar.setWidth(clamp(0, (parentWidth - size * 0.15), getValue() * stepSize));
            bar.setHeight(size * 0.02333333);

            redraw();
        }
    }

    private void redraw() {
        nameText.setFill(getNameColor());
        valueText.setFill(getValueColor());
        barBackground.setFill(getBarBackgroundColor());
        bar.setFill(getBarColor());
    }

    @Override public String toString() {
        return new StringBuilder(getName()).append(",").append(getValue()).append(",").append(getBarColor()).toString();
    }
}
