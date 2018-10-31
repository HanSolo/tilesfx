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
import eu.hansolo.tilesfx.events.ChartDataEventListener;
import eu.hansolo.tilesfx.events.TileEvent;
import eu.hansolo.tilesfx.events.TileEvent.EventType;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.Helper;
import eu.hansolo.tilesfx.chart.ChartData;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static eu.hansolo.tilesfx.tools.Helper.clamp;


/**
 * Created by hansolo on 17.02.17.
 */
public class RadialChartTileSkin extends TileSkin {
    private Text                          titleText;
    private Text                          text;
    private Canvas                        chartCanvas;
    private GraphicsContext               chartCtx;
    private ListChangeListener<ChartData> chartDataListener;
    private ChartDataEventListener        chartEventListener;
    private EventHandler<MouseEvent>      clickHandler;


    // ******************** Constructors **************************************
    public RadialChartTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        chartEventListener = e -> drawChart();
        tile.getChartData().forEach(chartData -> chartData.addChartDataEventListener(chartEventListener));

        chartDataListener  = c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach(addedItem -> addedItem.addChartDataEventListener(chartEventListener));
                } else if (c.wasRemoved()) {
                    c.getRemoved().forEach(removedItem -> removedItem.removeChartDataEventListener(chartEventListener));
                }
            }
            drawChart();
        };

        clickHandler = e -> {
            System.out.println("clicked");
            double          x           = e.getX();
            double          y           = e.getY();
            double          startAngle  = 90;
            List<ChartData> dataList    = tile.getChartData();
            int             noOfItems   = dataList.size();
            double          canvasSize  = chartCanvas.getWidth();
            double          radius      = canvasSize * 0.5;
            double          innerSpacer = radius * 0.18;
            double          barWidth    = (radius - innerSpacer) / tile.getChartData().size();
            double          max         = noOfItems == 0 ? 0 : dataList.stream().max(Comparator.comparingDouble(ChartData::getValue)).get().getValue();

            for (int i = 0 ; i < noOfItems ; i++) {
                ChartData data    = dataList.get(i);
                double    value   = clamp(0, Double.MAX_VALUE, data.getValue());
                double    barXY   = barWidth * 0.5 + i * barWidth;
                double    barWH   = canvasSize - barWidth - (2 * i * barWidth);
                double    angle   = value / max * 270.0;
                double    centerX = barXY + barWH * 0.5;
                double    centerY = centerX;

                boolean hit = Helper.isInRingSegment(x, y, centerX, centerY, (barWH + barWidth) * 0.5, (barWH - barWidth) * 0.5, startAngle, angle);
                if (hit) { tile.fireTileEvent(new TileEvent(EventType.SELECTED_CHART_DATA, data)); break; }
            }
        };

        titleText = new Text();
        titleText.setFill(tile.getTitleColor());
        Helper.enableNode(titleText, !tile.getTitle().isEmpty());

        text = new Text(tile.getText());
        text.setFill(tile.getTextColor());
        Helper.enableNode(text, tile.isTextVisible());

        chartCanvas = new Canvas(size * 0.9, tile.isTextVisible() ? size * 0.72 : size * 0.795);
        chartCtx = chartCanvas.getGraphicsContext2D();

        getPane().getChildren().addAll(titleText, chartCanvas, text);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        tile.getChartData().addListener(chartDataListener);
        chartCanvas.addEventHandler(MouseEvent.MOUSE_PRESSED, clickHandler);
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !tile.getTitle().isEmpty());
            Helper.enableNode(text, tile.isTextVisible());
            chartCanvas.setWidth(tile.isTextVisible() ? size * 0.68 : size * 0.795);
            chartCanvas.setHeight(tile.isTextVisible() ? size * 0.68 : size * 0.795);
        }
    }

    @Override public void dispose() {
        tile.getChartData().removeListener(chartDataListener);
        tile.getChartData().forEach(chartData -> chartData.removeChartDataEventListener(chartEventListener));
        chartCanvas.removeEventHandler(MouseEvent.MOUSE_PRESSED, clickHandler);
        super.dispose();
    }

    private void drawChart() {
        double          canvasSize     = chartCanvas.getWidth();
        double          radius         = canvasSize * 0.5;
        double          innerSpacer    = radius * 0.18;
        double          barWidth       = (radius - innerSpacer) / tile.getChartData().size();
        //List<RadialChartData> sortedDataList = tile.getChartData().stream().sorted(Comparator.comparingDouble(RadialChartData::getValue)).collect(Collectors.toList());
        List<ChartData> dataList       = tile.getChartData();
        int             noOfItems      = dataList.size();
        double          max            = noOfItems == 0 ? 0 : dataList.stream().max(Comparator.comparingDouble(ChartData::getValue)).get().getValue();

        double          nameX          = radius * 0.975;
        double          nameWidth      = radius * 0.95;
        double          valueY         = radius * 0.94;
        double          valueWidth     = barWidth * 0.9;
        Color           bkgColor       = Color.color(tile.getTextColor().getRed(), tile.getTextColor().getGreen(), tile.getTextColor().getBlue(), 0.15);

        chartCtx.clearRect(0, 0, canvasSize, canvasSize);
        chartCtx.setLineCap(StrokeLineCap.BUTT);
        chartCtx.setFill(tile.getTextColor());
        chartCtx.setTextAlign(TextAlignment.RIGHT);
        chartCtx.setTextBaseline(VPos.CENTER);
        chartCtx.setFont(Fonts.latoRegular(barWidth * 0.5));

        chartCtx.setStroke(bkgColor);
        chartCtx.setLineWidth(1);
        chartCtx.strokeLine(radius, 0, radius, radius - barWidth * 0.875);
        chartCtx.strokeLine(0, radius, radius - barWidth * 0.875, radius);
        chartCtx.strokeArc(noOfItems * barWidth, noOfItems * barWidth, canvasSize - (2 * noOfItems * barWidth), canvasSize - (2 * noOfItems * barWidth), 90, -270, ArcType.OPEN);

        for (int i = 0 ; i < noOfItems ; i++) {
            ChartData data  = dataList.get(i);
            double    value = clamp(0, Double.MAX_VALUE, data.getValue());
            double    bkgXY = i * barWidth;
            double    bkgWH = canvasSize - (2 * i * barWidth);
            double    barXY = barWidth * 0.5 + i * barWidth;
            double    barWH = canvasSize - barWidth - (2 * i * barWidth);
            double    angle = value / max * 270.0;

            // Background
            chartCtx.setLineWidth(1);
            chartCtx.setStroke(bkgColor);
            chartCtx.strokeArc(bkgXY, bkgXY, bkgWH, bkgWH, 90, -270, ArcType.OPEN);

            // DataBar
            chartCtx.setLineWidth(barWidth);
            chartCtx.setStroke(data.getFillColor());
            chartCtx.strokeArc(barXY, barXY, barWH, barWH, 90, -angle, ArcType.OPEN);

            // Name
            chartCtx.setTextAlign(TextAlignment.RIGHT);
            chartCtx.fillText(data.getName(), nameX, barXY, nameWidth);

            // Value
            chartCtx.setTextAlign(TextAlignment.CENTER);
            chartCtx.fillText(String.format(Locale.US, "%.0f", value), barXY, valueY, valueWidth);
        }
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

        double canvasWidth  = contentBounds.getWidth();
        double canvasHeight = contentBounds.getHeight();
        double canvasSize   = canvasWidth < canvasHeight ? canvasWidth : canvasHeight;

        if (tile.isShowing() && width > 0 && height > 0) {
            pane.setMaxSize(width, height);
            pane.setPrefSize(width, height);

            chartCanvas.setWidth(canvasSize);
            chartCanvas.setHeight(canvasSize);

            chartCanvas.relocate((contentBounds.getWidth() - canvasSize) * 0.5, contentBounds.getY() + (contentBounds.getHeight() - canvasSize) * 0.5);

            resizeStaticText();
        }
    }

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(tile.getTitle());
        text.setText(tile.getText());

        resizeStaticText();
        drawChart();

        titleText.setFill(tile.getTitleColor());
        text.setFill(tile.getTextColor());
    }
}
