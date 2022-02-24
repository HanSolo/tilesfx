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
import eu.hansolo.tilesfx.events.ChartDataEventListener;
import eu.hansolo.tilesfx.events.TileEvent;
import eu.hansolo.tilesfx.events.TileEvent.EventType;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.CtxBounds;
import eu.hansolo.tilesfx.tools.Helper;
import javafx.beans.InvalidationListener;
import javafx.collections.WeakListChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


/**
 * User: hansolo
 * Date: 18.09.19
 * Time: 03:12
 */
public class ClusterMonitorTileSkin extends TileSkin {
    private static final double                    MIN_HEIGHT        = 100;
    private        final TileEvent                 SVG_PRESSED_EVENT = new TileEvent(EventType.SVG_PATH_PRESSED);
    private              Text                      titleText;
    private              Text                      text;
    private              VBox                      chartPane;
    private              ChartDataEventListener    updateHandler;
    private              InvalidationListener      paneSizeListener;
    private              Map<ChartData, ChartItem> dataItemMap;
    private              Region                    graphicRegion;
    private              EventHandler<MouseEvent>  svgPathPressedHandler;


    // ******************** Constructors **************************************
    public ClusterMonitorTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        updateHandler    = e -> {
            switch(e.getType()) {
                case UPDATE  : updateChart(); break;
                case FINISHED: updateChart(); break;
            }
        };
        paneSizeListener = e -> updateChart();
        dataItemMap      = new HashMap<>();

        chartPane = new VBox();

        Collections.sort(tile.getChartData(), Comparator.comparing(ChartData::getName));
        tile.getChartData().forEach(data -> {
            data.addChartDataEventListener(updateHandler);
            dataItemMap.put(data, new ChartItem(data, contentBounds, data.getFormatString()));
            chartPane.getChildren().add(dataItemMap.get(data));
        });

        titleText = new Text(tile.getTitle());
        titleText.setFill(tile.getTitleColor());
        Helper.enableNode(titleText, !tile.getTitle().isEmpty());

        text = new Text(tile.getText());
        text.setFill(tile.getUnitColor());
        Helper.enableNode(text, tile.isTextVisible());

        SVGPath svgPath = tile.getSVGPath();
        if (null != svgPath) {
            svgPathPressedHandler = e -> tile.fireTileEvent(SVG_PRESSED_EVENT);
            graphicRegion = new Region();
            graphicRegion.setShape(svgPath);
            getPane().getChildren().addAll(titleText, text, chartPane, graphicRegion);
        } else {
            getPane().getChildren().addAll(titleText, text, chartPane);
        }
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        tile.getChartData().addListener(new WeakListChangeListener<>(change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    change.getAddedSubList().forEach(addedData -> {
                        addedData.addChartDataEventListener(updateHandler);
                        dataItemMap.put(addedData, new ChartItem(addedData, contentBounds));
                    });
                } else if (change.wasRemoved()) {
                    change.getRemoved().forEach(removedData -> {
                        removedData.removeChartDataEventListener(updateHandler);
                        dataItemMap.remove(removedData);
                    });
                }
            }
            chartPane.getChildren().clear();
            dataItemMap.entrySet().forEach(entry -> chartPane.getChildren().add(entry.getValue()));
            updateChart();
        }));
        if (null != tile.getSVGPath()) { graphicRegion.addEventHandler(MouseEvent.MOUSE_PRESSED, svgPathPressedHandler); }

        pane.widthProperty().addListener(paneSizeListener);
        pane.heightProperty().addListener(paneSizeListener);
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !tile.getTitle().isEmpty());
            Helper.enableNode(text, tile.isTextVisible());
            if (null != graphicRegion) { Helper.enableNode(graphicRegion, tile.isTextVisible()); }
        } else if ("DATA".equals(EVENT_TYPE)) {
            updateChart();
        }
    }

    @Override public void dispose() {
        pane.widthProperty().removeListener(paneSizeListener);
        pane.heightProperty().removeListener(paneSizeListener);
        tile.getBarChartItems().forEach(item -> item.removeChartDataEventListener(updateHandler));
        if (null != tile.getSVGPath()) { graphicRegion.removeEventHandler(MouseEvent.MOUSE_PRESSED, svgPathPressedHandler); }
        dataItemMap.clear();
        super.dispose();
    }


    // ******************** Resizing ******************************************
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

        chartPane.setPrefSize(width * 0.8, contentBounds.getHeight());
        chartPane.relocate(contentBounds.getX(), contentBounds.getY());
        chartPane.setSpacing(contentBounds.getHeight() * 0.25);

        double itemHeight = contentBounds.getHeight() / (dataItemMap.size());

        dataItemMap.values().forEach(item -> {
            item.setCompressed(height < MIN_HEIGHT);
            item.setPrefSize(contentBounds.getWidth(), itemHeight);
            item.setLayoutX(contentBounds.getX());
        });

        int noOfChartData = tile.getChartData().size();
        if (titleText.getText().isEmpty()) {
            chartPane.setSpacing((contentBounds.getHeight() - (noOfChartData * itemHeight)));
        } else {
            chartPane.setSpacing((contentBounds.getHeight() - (noOfChartData * itemHeight)) / 1.5);
        }

        if (null != graphicRegion) {
            double prefGraphicSize = 0.05 * size;
            graphicRegion.setMinSize(prefGraphicSize, prefGraphicSize);
            graphicRegion.setMaxSize(prefGraphicSize, prefGraphicSize);
            graphicRegion.setPrefSize(prefGraphicSize, prefGraphicSize);
            switch(tile.getTextAlignment()) {
                default    :
                case LEFT  :
                case CENTER: graphicRegion.relocate(width - (size * 0.05) - prefGraphicSize, height - size * 0.05 - prefGraphicSize); break;
                case RIGHT : graphicRegion.relocate(size * 0.05, height - size * 0.05 - prefGraphicSize); break;
            }
        }

        updateChart();
    }

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(tile.getTitle());
        text.setText(tile.getText());

        resizeDynamicText();
        resizeStaticText();

        titleText.setFill(tile.getTitleColor());
        text.setFill(tile.getTextColor());

        if (null != graphicRegion) {
            graphicRegion.setBackground(new Background(new BackgroundFill(tile.getSVGPath().getFill(), CornerRadii.EMPTY, Insets.EMPTY)));
        }
    }

    private void updateChart() {
        int noOfItems = dataItemMap.size();
        if (noOfItems == 0) return;
        for (int i = 0 ; i < noOfItems ; i++) {
            ChartData item = dataItemMap.keySet().iterator().next();
            dataItemMap.get(item).update();

            if (i > 1) { break; }
        }
    }


    // ******************** Internal Classes **********************************
    private class ChartItem extends Region {
        private static final double                 PREF_WIDTH  = 100;
        private static final double                 PREF_HEIGHT = 95;
        private              ChartData              chartData;
        private              CtxBounds              contentBounds;
        private              Label                  title;
        private              Label                  value;
        private              Rectangle              scale;
        private              Rectangle              bar;
        private              String                 formatString;
        private              double                 step;
        private              boolean                compressed;
        private              ChartDataEventListener chartDataListener;


        public ChartItem(final ChartData CHART_DATA, final CtxBounds CONTENT_BOUNDS) {
            this(CHART_DATA, CONTENT_BOUNDS, "%.0f%%");
        }
        public ChartItem(final ChartData CHART_DATA, final CtxBounds CONTENT_BOUNDS, final String FORMAT_STRING) {
            chartData         = CHART_DATA;
            contentBounds     = CONTENT_BOUNDS;
            title             = new Label(chartData.getName());
            value             = new Label(String.format(Locale.US, FORMAT_STRING, chartData.getValue()));
            scale             = new Rectangle(0, 0);
            bar               = new Rectangle(0, 0);
            formatString      = FORMAT_STRING;
            step              = PREF_WIDTH / (CHART_DATA.getMaxValue() - CHART_DATA.getMinValue());
            compressed        = false;
            chartDataListener = e -> {
                switch(e.getType()) {
                    case UPDATE  : update(); break;
                    case FINISHED: update(); break;
                }
            };
            initGraphics();
            registerListeners();
        }


        private void initGraphics() {
            setPrefSize(contentBounds.getWidth(), contentBounds.getHeight() * 0.2375);
            Font font = Fonts.latoRegular(Helper.clamp(1, 48, getPrefHeight() * 0.50526316));
            title.setFont(font);
            title.setTextFill(chartData.getTextColor());
            title.setAlignment(Pos.CENTER_LEFT);

            value.setFont(font);
            value.setTextFill(chartData.getTextColor());
            value.setAlignment(Pos.CENTER_RIGHT);
            scale.setFill(Color.rgb(90, 90, 90));
            bar.setFill(chartData.getFillColor());

            getChildren().addAll(scale, bar, title, value);
        }

        private void registerListeners() {
            chartData.addChartDataEventListener(chartDataListener);
            widthProperty().addListener(o -> resize());
            heightProperty().addListener(o -> resize());
        }

        public String getFormatString() { return formatString; }
        public void setFormatString(final String FORMAT_STRING) {
            formatString = FORMAT_STRING;
            update();
        }

        public boolean isCompressed() { return compressed; }
        public void setCompressed(final boolean COMPRESSED) {
            compressed = COMPRESSED;
            Helper.enableNode(scale, !compressed);
            resize();
        }

        public void update() {
            value.setText(String.format(Locale.US, formatString, chartData.getValue()));
            bar.setWidth(chartData.getValue() * step);
            if (tile.isFillWithGradient() && null != chartData.getGradientLookup()) {
                bar.setFill(chartData.getGradientLookup().getColorAt(chartData.getValue() / (chartData.getMaxValue() - chartData.getMinValue())));
            } else {
                bar.setFill(chartData.getFillColor());
            }
            if (compressed) {
                title.setTextFill(bar.getWidth() > width * 0.2 ? tile.getBackgroundColor() : chartData.getTextColor());
                value.setTextFill(bar.getWidth() > width * 0.8 ? tile.getBackgroundColor() : chartData.getTextColor());
            } else {
                title.setTextFill(chartData.getTextColor());
                value.setTextFill(chartData.getTextColor());
            }
        }

        public void dispose() {
            chartData.removeChartDataEventListener(chartDataListener);
        }

        private void resize() {
            double width  = getPrefWidth();
            double height = getPrefHeight();

            step = width / (chartData.getMaxValue() - chartData.getMinValue());

            double textWidth  = width * 0.5;
            double textHeight = height * 0.13;

            title.setPrefSize(textWidth, textHeight);
            value.setPrefSize(textWidth, textHeight);

            Font font = Fonts.latoRegular(Helper.clamp(1, 48, getPrefHeight() * (compressed ? 0.5 : 0.35)));
            title.setFont(font);
            value.setFont(font);

            title.setLayoutX(0);

            value.setLayoutX(width * 0.5);

            bar.setX(0);
            if (compressed) {
                bar.setY(height * 0.05);
                bar.setHeight(height * 0.9);
                value.setLayoutY((height - font.getSize()) * 0.5);
                title.setLayoutY((height - font.getSize()) * 0.5);
            } else {
                bar.setY(title.getLayoutY() + font.getSize() + height * 0.12);
                bar.setHeight(height * 0.35);
                value.setLayoutY(0);
                title.setLayoutY(0);
            }
            bar.setWidth(chartData.getValue() * step);

            scale.setX(0);
            scale.setWidth(width);
            scale.setHeight(height * 0.05263158);
            scale.setY(bar.getY() + (bar.getHeight() - scale.getHeight()) * 0.5);

            update();
        }
    }
}
