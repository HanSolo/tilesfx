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
package eu.hansolo.tilesfx.skins;

import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.chart.ChartData;
import eu.hansolo.tilesfx.events.ChartDataEvent;
import eu.hansolo.tilesfx.events.ChartDataEventListener;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.Helper;
import eu.hansolo.toolboxfx.FontMetrix;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.List;


public class CycleStepTileSkin extends TileSkin {
    private Text                          titleText;
    private Text                          text;
    private List<ChartItem>               chartItems;
    private VBox                          chartBox;
    private ListChangeListener<ChartData> chartDataListener;


    // ******************** Constructors **************************************
    public CycleStepTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        chartDataListener = change -> {
            chartBox.getChildren().clear();
            double sum = tile.getChartData().stream().mapToDouble(chartData -> chartData.getValue()).sum();
            tile.getChartData().forEach(chartData -> chartBox.getChildren().add(new ChartItem(chartData, sum)));
            updateChart();
        };

        chartItems = new ArrayList<>();
        double sum = tile.getChartData().stream().mapToDouble(chartData -> chartData.getValue()).sum();
        tile.getChartData().forEach(chartData -> chartItems.add(new ChartItem(chartData, sum)));

        chartBox = new VBox(0);
        chartBox.setFillWidth(true);
        chartBox.getChildren().addAll(chartItems);

        titleText = new Text();
        titleText.setFill(tile.getTitleColor());
        Helper.enableNode(titleText, !tile.getTitle().isEmpty());

        text = new Text(tile.getText());
        text.setFill(tile.getUnitColor());
        Helper.enableNode(text, tile.isTextVisible());

        getPane().getChildren().addAll(titleText, text, chartBox);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        tile.getChartData().addListener(chartDataListener);
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !tile.getTitle().isEmpty());
            Helper.enableNode(text, tile.isTextVisible());
        } else if ("DATA".equals(EVENT_TYPE)) {
            updateChart();
        }
    }

    @Override public void dispose() {
        tile.getChartData().removeListener(chartDataListener);
        super.dispose();
    }


    // ******************** Resizing ******************************************
    private void updateChart() {
        Platform.runLater(() -> {
            double sum = tile.getChartData().stream().mapToDouble(chartData -> chartData.getValue()).sum();
            double lastFactor = 0;
            for (int i = 0 ; i < chartBox.getChildren().size() ; i++) {
                ChartItem item = (ChartItem) chartBox.getChildren().get(i);
                item.update(sum, lastFactor);
                lastFactor += item.getChartData().getValue() / sum;
            }
        });
    }

    @Override protected void resizeStaticText() {
        double maxWidth = width - size * 0.1;
        double fontSize = size * textSize.factor;

        boolean customFontEnabled = tile.isCustomFontEnabled();
        Font    customFont        = tile.getCustomFont();
        Font    font              = (customFontEnabled && customFont != null) ? Font.font(customFont.getFamily(), fontSize) : Fonts.latoRegular(fontSize);

        titleText.setFont(font);
        if (titleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(titleText, maxWidth, fontSize); }
        switch(tile.getTitleAlignment()) {
            default    :
            case LEFT  : titleText.relocate(size * 0.05, size * 0.05); break;
            case CENTER: titleText.relocate((width - titleText.getLayoutBounds().getWidth()) * 0.5, size * 0.05); break;
            case RIGHT : titleText.relocate(width - (size * 0.05) - titleText.getLayoutBounds().getWidth(), size * 0.05); break;
        }

        text.setText(tile.getText());
        text.setFont(font);
        if (text.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(text, maxWidth, fontSize); }
        switch(tile.getTextAlignment()) {
            default    :
            case LEFT  : text.setX(size * 0.05); break;
            case CENTER: text.setX((width - text.getLayoutBounds().getWidth()) * 0.5); break;
            case RIGHT : text.setX(width - (size * 0.05) - text.getLayoutBounds().getWidth()); break;
        }
        text.setY(height - size * 0.05);
    }

    @Override protected void resize() {
        super.resize();
        chartBox.relocate(contentBounds.getMinX(), contentBounds.getMinY());
        chartBox.setPrefSize(contentBounds.getWidth(), contentBounds.getHeight());
        updateChart();
    }

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(tile.getTitle());
        text.setText(tile.getText());

        tile.getBarChartItems().forEach(item -> {
            item.setNameColor(tile.getTextColor());
            item.setValueColor(tile.getValueColor());
        });

        resizeDynamicText();
        resizeStaticText();

        titleText.setFill(tile.getTitleColor());
        text.setFill(tile.getTextColor());
    }


    public class ChartItem extends Region implements ChartDataEventListener {
        private static final double          PREFERRED_WIDTH  = 250;
        private static final double          PREFERRED_HEIGHT = 250;
        private static final double          MINIMUM_WIDTH    = 10;
        private static final double          MINIMUM_HEIGHT   = 10;
        private static final double          MAXIMUM_WIDTH    = 1024;
        private static final double          MAXIMUM_HEIGHT   = 1024;
        private              double          size;
        private              double          width;
        private              double          height;
        private              Canvas          canvas;
        private              GraphicsContext ctx;
        private              ChartData       chartData;
        private              double          sum;
        private              double          factorX;



        // ******************** Constructors **************************************
        public ChartItem(final ChartData chartData, final double sum) {
            this.chartData = chartData;
            this.sum       = sum;
            this.factorX   = 0;
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

            canvas = new Canvas();
            ctx    = canvas.getGraphicsContext2D();

            getChildren().setAll(canvas);
        }

        private void registerListeners() {
            widthProperty().addListener(o -> resize());
            heightProperty().addListener(o -> resize());
            chartData.addChartDataEventListener(this);
        }


        // ******************** Methods *******************************************
        @Override protected double computeMinWidth(final double HEIGHT) { return MINIMUM_WIDTH; }
        @Override protected double computeMinHeight(final double WIDTH) { return MINIMUM_HEIGHT; }
        @Override protected double computePrefWidth(final double HEIGHT) { return super.computePrefWidth(HEIGHT); }
        @Override protected double computePrefHeight(final double WIDTH) { return super.computePrefHeight(WIDTH); }
        @Override protected double computeMaxWidth(final double HEIGHT) { return MAXIMUM_WIDTH; }
        @Override protected double computeMaxHeight(final double WIDTH) { return MAXIMUM_HEIGHT; }



        public ChartData getChartData() { return chartData; }

        public void update(final double sum, final double factorX) {
            this.sum     = sum;
            this.factorX = factorX;
            redraw();
        }

        // ******************** Resizing ******************************************
        private void resize() {
            width  = getWidth() - getInsets().getLeft() - getInsets().getRight();
            height = getHeight() - getInsets().getTop() - getInsets().getBottom();
            size   = width < height ? width : height;

            if (width > 0 && height > 0) {
                canvas.setWidth(width);
                canvas.setHeight(height);
                canvas.relocate((getWidth() - width) * 0.5, (getHeight() - height) * 0.5);

                redraw();
            }
        }

        private void redraw() {
            double     value              = chartData.getValue();
            String     formatString       = "%." + tile.getDecimals() + "f";
            double     maxTextWidth       = width * 0.4;
            double     barStartX          = maxTextWidth + 5;
            double     barStartY          = height * 0.2;
            double     maxBarWidth        = width - barStartX;
            double     barWidth           = value / sum * maxBarWidth;
            double     barHeight          = height * 0.6;
            Color      barBackgroundColor = Helper.getColorWithOpacity(tile.getForegroundColor(), 0.1);
            Color      barColor           = chartData.getFillColor();
            boolean    autoItemTextColor  = tile.getAutoItemTextColor();
            Color      textColor          = tile.getForegroundColor();
            Font       valueFont          = Fonts.latoRegular(height * 0.3);
            FontMetrix fontMetrix         = new FontMetrix(valueFont);
            String     valueText          = String.format(tile.getLocale(), formatString, value);
            if (autoItemTextColor) {
                if (fontMetrix.computeStringWidth(valueText) > barWidth) {
                    textColor = Helper.isDark(tile.getBackgroundColor()) ? tile.getAutoItemBrightTextColor() : tile.getAutoItemDarkTextColor();
                } else {
                    textColor = Helper.isDark(barColor) ? tile.getAutoItemBrightTextColor() : tile.getAutoItemDarkTextColor();
                }
            }

            ctx.setTextBaseline(VPos.CENTER);
            ctx.setFont(Fonts.latoRegular(height * 0.4));
            ctx.setTextAlign(TextAlignment.LEFT);
            ctx.clearRect(0, 0, width, height);
            ctx.setFill(tile.getForegroundColor());
            ctx.fillText(chartData.getName(), 0, height / 2, maxTextWidth);
            ctx.setFill(barBackgroundColor);
            ctx.fillRect(barStartX, barStartY, maxBarWidth, barHeight);
            ctx.setFill(barColor);
            ctx.fillRect(barStartX + factorX * maxBarWidth, barStartY, barWidth, barHeight);
            ctx.setFill(textColor);
            ctx.setFont(valueFont);
            ctx.setTextAlign(TextAlignment.CENTER);
            ctx.fillText(valueText, barStartX + factorX * maxBarWidth + barWidth * 0.5, height * 0.5, maxTextWidth);
        }

        @Override public void onChartDataEvent(final ChartDataEvent EVENT) {
            double sum = tile.getChartData().stream().mapToDouble(chartData -> chartData.getValue()).sum();
            double lastFactor = 0;
            for (int i = 0 ; i < chartBox.getChildren().size() ; i++) {
                ChartItem item = (ChartItem) chartBox.getChildren().get(i);
                item.update(sum, lastFactor);
                lastFactor += item.getChartData().getValue() / sum;
            }
            redraw();
        }
    }
}
