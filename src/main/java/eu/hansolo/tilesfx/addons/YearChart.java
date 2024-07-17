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
package eu.hansolo.tilesfx.addons;

import eu.hansolo.tilesfx.Tile;
import eu.hansolo.toolboxfx.GradientLookup;
import javafx.beans.DefaultProperty;
import javafx.collections.ObservableList;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;


@DefaultProperty("children")
public class YearChart extends Region {
    public  static final Map<Integer, String> monthNames       = Map.ofEntries(Map.entry(1, "J"), Map.entry(2, "F"), Map.entry(3, "M"),
                                                                               Map.entry(4, "A"), Map.entry(5, "M"), Map.entry(6, "J"),
                                                                               Map.entry(7, "J"), Map.entry(8, "A"), Map.entry(9, "S"),
                                                                               Map.entry(10, "O"), Map.entry(11, "N"), Map.entry(12, "D"));
    private static final double               PREFERRED_WIDTH  = 288;
    private static final double               PREFERRED_HEIGHT = 24;
    private static final double               MINIMUM_WIDTH    = 10;
    private static final double               MINIMUM_HEIGHT   = 10;
    private static final double               MAXIMUM_WIDTH    = 2048;
    private static final double               MAXIMUM_HEIGHT   = 2048;
    private              double               width;
    private              double               height;
    private              Canvas               canvas;
    private              GraphicsContext      ctx;
    private              Map<Integer, Double> months;
    private              GradientLookup       gradientLookup;
    private              boolean              showMonth;
    private              boolean              separateMonths;
    private              String               text;
    private              Color                textColor;
    private              double               minValue;
    private              double               maxValue;


    // ******************** Constructors **************************************
    public YearChart() {
        this("", Color.WHITE, false, false);
    }
    public YearChart(final String text, final Color textColor, final boolean showMonth, final boolean separateMonths) {
        this.text           = text;
        this.textColor      = textColor;
        this.showMonth      = showMonth;
        this.separateMonths = separateMonths;
        this.months         = new HashMap<>(12);
        this.gradientLookup = new GradientLookup(new Stop(0.00, Tile.BLUE),
                                                 new Stop(0.25, Tile.GREEN),
                                                 new Stop(0.50, Tile.YELLOW),
                                                 new Stop(0.75, Tile.ORANGE),
                                                 new Stop(1.00, Tile.RED));

        for (int i = 1 ; i < 13 ; i++) { months.put(i, 0.0); }
        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void initGraphics() {
        if (Double.compare(getPrefWidth(), 0.0) <= 0 || Double.compare(getPrefHeight(), 0.0) <= 0 || Double.compare(getWidth(), 0.0) <= 0 ||
            Double.compare(getHeight(), 0.0) <= 0) {
            if (getPrefWidth() > 0 && getPrefHeight() > 0) {
                setPrefSize(getPrefWidth(), getPrefHeight());
            } else {
                setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            }
        }

        canvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        ctx    = canvas.getGraphicsContext2D();
        ctx.setTextAlign(TextAlignment.CENTER);
        ctx.setTextBaseline(VPos.CENTER);

        getChildren().setAll(canvas);
    }

    private void registerListeners() {
        widthProperty().addListener(o -> resize());
        heightProperty().addListener(o -> resize());
        // add listeners to your propertes like
        //value.addListener(o -> handleControlPropertyChanged("VALUE"));
    }


    // ******************** Methods *******************************************
    @Override protected double computeMinWidth(final double HEIGHT) { return MINIMUM_WIDTH; }
    @Override protected double computeMinHeight(final double WIDTH) { return MINIMUM_HEIGHT; }
    @Override protected double computePrefWidth(final double HEIGHT) { return super.computePrefWidth(HEIGHT); }
    @Override protected double computePrefHeight(final double WIDTH) { return super.computePrefHeight(WIDTH); }
    @Override protected double computeMaxWidth(final double HEIGHT) { return MAXIMUM_WIDTH; }
    @Override protected double computeMaxHeight(final double WIDTH) { return MAXIMUM_HEIGHT; }

    @Override public ObservableList<Node> getChildren() { return super.getChildren(); }

    public void set(final int month, final double value) {
        if (month < 1 || month > 12) { throw new IllegalArgumentException("Month must be in the range of 1..12"); }
        months.put(month, value);

        minValue = months.values().stream().min(Comparator.comparingDouble(v -> v)).get();
        maxValue = months.values().stream().max(Comparator.comparingDouble(v -> v)).get();

        redraw();
    }
    public double get(final int month) {
        if (month < 1 || month > 12) { throw new IllegalArgumentException("Month must be in the range of 1..12"); }
        return months.get(month);
    }

    public void setGradientLookup(final GradientLookup gradientLookup) {
        this.gradientLookup = gradientLookup;
        redraw();
    }

    public String getText() { return text; }
    public void setText(final String text) { this.text = text; }

    public Color getTextColor() { return textColor; }
    public void setTextColor(final Color color) {
        textColor = color;
        redraw();
    }

    public boolean getShowMonth() { return showMonth; }
    public void setShowMonth(final boolean showMonths) {
        this.showMonth = showMonths;
        redraw();
    }

    public boolean getSeparateMonths() { return separateMonths; }
    public void setSeparateMonths(final boolean separateMonths) {
        this.separateMonths = separateMonths;
        redraw();
    }

    public double getMinValue() { return minValue; }
    public double getMaxValue() { return maxValue; }


    // ******************** Resizing ******************************************
    private void resize() {
        width = getWidth() - getInsets().getLeft() - getInsets().getRight();
        height = getHeight() - getInsets().getTop() - getInsets().getBottom();

        if (width > 0 && height > 0) {
            canvas.setWidth(width);
            canvas.setHeight(height);
            canvas.relocate((getWidth() - width) * 0.5, (getHeight() - height) * 0.5);

            redraw();
        }
    }

    private void redraw() {
        ctx.clearRect(0, 0, width, height);
        double range = maxValue - minValue;
        double yearWidth;
        double yearHeight;
        double yearSize;
        double gap = 0;
        if (width >= height) {
            // Horizontal
            yearWidth  = width / 12.0;
            yearHeight = height;
            yearSize   = yearWidth < yearHeight ? yearWidth : yearHeight;
            if (getSeparateMonths()) {
                gap = yearSize * 0.05;
            }
            yearWidth  = yearWidth  - 2 * gap;
            yearHeight = yearHeight - 2 * gap;
            ctx.setFont(Font.font(yearSize * 0.5));
            for (int i = 0 ; i < 12 ; i++) {
                ctx.setFill(gradientLookup.getColorAt((months.get(i + 1) - minValue) / range));
                ctx.fillRect(gap + i * (yearWidth + gap), gap, yearWidth, yearHeight);
                if (getShowMonth()) {
                    ctx.setFill(getTextColor());
                    ctx.fillText(monthNames.get(i + 1), gap + i * (yearWidth + gap) + yearWidth / 2.0, gap + yearHeight / 2.0);
                }
            }
        } else {
            // Vertical
            yearWidth  = width;
            yearHeight = height / 12.0;
            yearSize   = yearWidth < yearHeight ? yearWidth : yearHeight;
            ctx.setFont(Font.font(yearSize * 0.5));
            if (getSeparateMonths()) {
                gap = yearSize * 0.05;
            }
            yearWidth  = yearWidth  - 2 * gap;
            yearHeight = yearHeight - 2 * gap;
            for (int i = 0 ; i < 12 ; i++) {
                ctx.setFill(gradientLookup.getColorAt((months.get(i + 1) - minValue) / range));
                ctx.fillRect(gap, gap + i * (yearHeight + gap), yearWidth, yearHeight);
                if (getShowMonth()) {
                    ctx.setFill(getTextColor());
                    ctx.fillText(monthNames.get(i + 1), gap + yearWidth / 2.0, gap + i * (yearHeight + gap) + yearHeight / 2.0);
                }
            }
        }
    }
}
