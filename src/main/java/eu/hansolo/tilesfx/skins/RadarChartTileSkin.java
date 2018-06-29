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
import eu.hansolo.tilesfx.chart.RadarChart;
import eu.hansolo.tilesfx.events.ChartDataEventListener;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.Helper;
import javafx.collections.ListChangeListener;
import javafx.scene.text.Font;
import javafx.scene.text.Text;


/**
 * Created by hansolo on 10.06.17.
 */
public class RadarChartTileSkin extends TileSkin {
    private Text                          titleText;
    private Text                          text;
    private RadarChart                    radarChart;
    private ListChangeListener<ChartData> chartDataListener;
    private ChartDataEventListener        chartEventListener;


    // ******************** Constructors **************************************
    public RadarChartTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        radarChart = new RadarChart(tile.getChartData());
        radarChart.setMaxValue(tile.getMaxValue());
        radarChart.setUnit(tile.getUnit());
        radarChart.setLegendVisible(true);
        radarChart.setThresholdVisible(tile.isThresholdVisible());
        radarChart.setMode(tile.getRadarChartMode());
        radarChart.setGridColor(tile.getChartGridColor());
        radarChart.setChartTextColor(tile.getTextColor());
        radarChart.setThresholdColor(tile.getThresholdColor());
        radarChart.setGradientStops(tile.getGradientStops());

        chartEventListener = e -> radarChart.redraw();
        tile.getChartData().forEach(chartData -> chartData.addChartDataEventListener(chartEventListener));

        chartDataListener  = c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach(addedItem -> addedItem.addChartDataEventListener(chartEventListener));
                } else if (c.wasRemoved()) {
                    c.getRemoved().forEach(removedItem -> removedItem.removeChartDataEventListener(chartEventListener));
                }
            }
            radarChart.redraw();
        };

        titleText = new Text();
        titleText.setFill(tile.getTitleColor());
        Helper.enableNode(titleText, !tile.getTitle().isEmpty());

        text = new Text(tile.getText());
        text.setFill(tile.getTextColor());
        Helper.enableNode(text, tile.isTextVisible());

        getPane().getChildren().addAll(titleText, radarChart, text);
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
            radarChart.setThresholdVisible(tile.isThresholdVisible());
        } else if ("RECALC".equals(EVENT_TYPE)) {
            radarChart.setMaxValue(tile.getMaxValue());
            radarChart.setUnit(tile.getUnit());
            radarChart.setMode(tile.getRadarChartMode());
            radarChart.setThresholdColor(tile.getThresholdColor());
            radarChart.setGradientStops(tile.getGradientStops());
        }
    }

    @Override public void dispose() {
        radarChart.dispose();
        tile.getChartData().removeListener(chartDataListener);
        tile.getChartData().forEach(chartData -> chartData.removeChartDataEventListener(chartEventListener));
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
        width  = tile.getWidth() - tile.getInsets().getLeft() - tile.getInsets().getRight();
        height = tile.getHeight() - tile.getInsets().getTop() - tile.getInsets().getBottom();
        size   = width < height ? width : height;

        double chartWidth   = contentBounds.getWidth();
        double chartHeight  = contentBounds.getHeight();
        double chartSize    = chartWidth < chartHeight ? chartWidth : chartHeight;

        if (tile.isShowing() && width > 0 && height > 0) {
            pane.setMaxSize(width, height);
            pane.setPrefSize(width, height);

            radarChart.setPrefSize(chartSize, chartSize);
            radarChart.relocate((width - chartSize) * 0.5, contentBounds.getY() + (contentBounds.getHeight() - chartSize) * 0.5);

            resizeStaticText();
        }
    }

    @Override protected void redraw() {
        super.redraw();
        radarChart.setSmoothing(tile.isSmoothing());
        radarChart.setUnit(tile.getUnit());
        radarChart.setMode(tile.getRadarChartMode());

        titleText.setText(tile.getTitle());
        text.setText(tile.getText());

        resizeStaticText();
        radarChart.redraw();

        titleText.setFill(tile.getTitleColor());
        text.setFill(tile.getTextColor());
        radarChart.setGridColor(tile.getForegroundColor());
        radarChart.setChartTextColor(tile.getForegroundColor());
        radarChart.setThresholdColor(tile.getThresholdColor());
        radarChart.setGradientStops(tile.getGradientStops());
        radarChart.setGridColor(tile.getChartGridColor());
    }
}
