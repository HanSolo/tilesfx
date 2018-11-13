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
import eu.hansolo.tilesfx.chart.ChartData;
import eu.hansolo.tilesfx.tools.Helper;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tooltip;
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
import java.util.stream.Collectors;


/**
 * Created by hansolo on 27.02.17.
 */
public class DonutChartTileSkin extends TileSkin {
    private Text                          titleText;
    private Text                          text;
    private Canvas                        chartCanvas;
    private GraphicsContext               chartCtx;
    private Canvas                        legendCanvas;
    private GraphicsContext               legendCtx;
    private ListChangeListener<ChartData> chartDataListener;
    private ChartDataEventListener        chartEventListener;
    private double                        centerX;
    private double                        centerY;
    private double                        innerRadius;
    private double                        outerRadius;
    private Tooltip                       selectionTooltip;
    private EventHandler<MouseEvent>      clickHandler;
    private EventHandler<MouseEvent>      moveHandler;


    // ******************** Constructors **************************************
    public DonutChartTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        chartEventListener = e -> redraw();
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
            drawLegend();
        };

        clickHandler = e -> {
            double          x          = e.getX();
            double          y          = e.getY();
            double          startAngle = 90;
            double          angle      = 0;
            List<ChartData> dataList   = tile.isSortedData() ? tile.getChartData().stream().sorted(Comparator.comparingDouble(ChartData::getValue)).collect(Collectors.toList()) : tile.getChartData();
            int             noOfItems  = dataList.size();
            double          sum        = dataList.stream().mapToDouble(ChartData::getValue).sum();
            double          stepSize   = 360.0 / sum;
            double          barWidth   = chartCanvas.getWidth() * 0.1;
            double          ri         = outerRadius - barWidth * 0.5;
            double          ro         = outerRadius + barWidth * 0.5;

            for (int i = 0 ; i < noOfItems ; i++) {
                ChartData data  = dataList.get(i);
                double    value = data.getValue();
                startAngle -= angle;
                angle = value * stepSize;
                boolean hit = Helper.isInRingSegment(x, y, centerX, centerY, ro, ri, startAngle, angle);
                if (hit) {
                    String tooltipText = new StringBuilder(data.getName()).append("\n").append(String.format(locale, formatString, value)).toString();

                    Point2D popupLocation = new Point2D(e.getScreenX() - selectionTooltip.getWidth() * 0.5, e.getScreenY() - size * 0.025 - selectionTooltip.getHeight());
                    selectionTooltip.setText(tooltipText);
                    selectionTooltip.setX(popupLocation.getX());
                    selectionTooltip.setY(popupLocation.getY());
                    selectionTooltip.show(tile.getScene().getWindow());

                    tile.fireTileEvent(new TileEvent(EventType.SELECTED_CHART_DATA, data)); break;
                }
            }
        };
        moveHandler  = e -> selectionTooltip.hide();

        titleText = new Text();
        titleText.setFill(tile.getTitleColor());
        Helper.enableNode(titleText, !tile.getTitle().isEmpty());

        text = new Text(tile.getText());
        text.setFill(tile.getTextColor());
        Helper.enableNode(text, tile.isTextVisible());

        chartCanvas = new Canvas(size * 0.9, tile.isTextVisible() ? height - size * 0.28 : height - size * 0.205);
        chartCtx    = chartCanvas.getGraphicsContext2D();

        legendCanvas = new Canvas(size * 0.225, tile.isTextVisible() ? height - size * 0.28 : height - size * 0.205);
        legendCtx    = legendCanvas.getGraphicsContext2D();

        selectionTooltip = new Tooltip("");
        selectionTooltip.setWidth(60);
        selectionTooltip.setHeight(48);
        Tooltip.install(chartCanvas, selectionTooltip);

        getPane().getChildren().addAll(titleText, legendCanvas,text, chartCanvas);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        tile.getChartData().addListener(chartDataListener);
        chartCanvas.addEventHandler(MouseEvent.MOUSE_PRESSED, clickHandler);
        chartCanvas.addEventHandler(MouseEvent.MOUSE_MOVED, moveHandler);
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !tile.getTitle().isEmpty());
            Helper.enableNode(text, tile.isTextVisible());
            double chartCanvasWidth   = width - size * 0.1;
            double chartCanvasHeight  = tile.isTextVisible() ? height - size * 0.28 : height - size * 0.205;
            double chartCanvasSize    = chartCanvasWidth < chartCanvasHeight ? chartCanvasWidth : chartCanvasHeight;
            double legendCanvasWidth  = width * 0.225;
            double legendCanvasHeight = chartCanvasSize;
            chartCanvas.setWidth(chartCanvasSize);
            chartCanvas.setHeight(chartCanvasSize);
            legendCanvas.setWidth(legendCanvasWidth);
            legendCanvas.setHeight(legendCanvasHeight);
        }
    }

    @Override public void dispose() {
        tile.getChartData().removeListener(chartDataListener);
        tile.getChartData().forEach(chartData -> chartData.removeChartDataEventListener(chartEventListener));
        chartCanvas.removeEventHandler(MouseEvent.MOUSE_PRESSED, clickHandler);
        chartCanvas.removeEventHandler(MouseEvent.MOUSE_MOVED, moveHandler);
        super.dispose();
    }

    private void drawChart() {
        List<ChartData> dataList       = tile.isSortedData() ? tile.getChartData().stream().sorted(Comparator.comparingDouble(ChartData::getValue)).collect(Collectors.toList()) : tile.getChartData();
        double          canvasSize     = chartCanvas.getWidth();
        int             noOfItems      = dataList.size();
        double          center         = canvasSize * 0.5;
        double          barWidth       = canvasSize * 0.09;
        double          sum            = dataList.stream().mapToDouble(ChartData::getValue).sum();
        double          stepSize       = 360.0 / sum;
        double          angle          = 0;
        double          startAngle     = 90;
        double          xy             = canvasSize * 0.15;
        double          wh             = canvasSize * 0.7;
        //Color           bkgColor       = tile.getBackgroundColor();
        Color           textColor      = tile.getTextColor();

        centerX        = xy + wh * 0.5;
        centerY        = xy + wh * 0.5;
        innerRadius    = canvasSize * 0.225;
        outerRadius    = canvasSize * 0.45;

        chartCtx.clearRect(0, 0, canvasSize, canvasSize);
        chartCtx.setLineCap(StrokeLineCap.BUTT);
        chartCtx.setFill(textColor);
        chartCtx.setTextBaseline(VPos.CENTER);
        chartCtx.setTextAlign(TextAlignment.CENTER);

        // Sum
        if (tile.isValueVisible()) {
            chartCtx.setFont(Fonts.latoRegular(canvasSize * 0.15));
            chartCtx.fillText(String.format(Locale.US, "%.0f", sum), center, center, canvasSize * 0.4);
        }

        chartCtx.setFont(Fonts.latoRegular(barWidth * 0.5));
        for (int i = 0 ; i < noOfItems ; i++) {
            ChartData data  = dataList.get(i);
            double    value = data.getValue();
            startAngle -= angle;
            angle = value * stepSize;

            // Segment
            chartCtx.setLineWidth(barWidth);
            chartCtx.setStroke(data.getFillColor());
            chartCtx.strokeArc(xy, xy, wh, wh, startAngle, -angle, ArcType.OPEN);

            double radValue = Math.toRadians(startAngle - (angle * 0.5));
            double cosValue = Math.cos(radValue);
            double sinValue = Math.sin(radValue);

            // Percentage
            double x = innerRadius * cosValue;
            double y = -innerRadius * sinValue;
            chartCtx.setFill(textColor);
            chartCtx.fillText(String.format(Locale.US, "%.0f%%", (value / sum * 100.0)), center + x, center + y, barWidth);

            // Value
            if (angle > 10) {
                x = outerRadius * cosValue;
                y = -outerRadius * sinValue;
                chartCtx.setFill(data.getTextColor());
                chartCtx.fillText(String.format(Locale.US, "%.0f", value), center + x, center + y, barWidth);
            }
        }
    }

    private void drawLegend() {
        List<ChartData> dataList     = tile.getChartData();
        double          canvasWidth  = legendCanvas.getWidth();
        double          canvasHeight = legendCanvas.getHeight();
        int             noOfItems    = dataList.size();
        //List<ChartData> sortedDataList = dataList.stream().sorted(Comparator.comparingDouble(ChartData::getValue).reversed()).collect(Collectors.toList());
        Color           textColor    = tile.getTextColor();
        double          stepSize     = canvasHeight * 0.9 / (noOfItems + 1);

        legendCtx.clearRect(0, 0, canvasWidth, canvasHeight);
        legendCtx.setTextAlign(TextAlignment.LEFT);
        legendCtx.setTextBaseline(VPos.CENTER);
        legendCtx.setFont(Fonts.latoRegular(canvasHeight * 0.05));

        for (int i = 0 ; i < noOfItems ; i++) {
            ChartData data = dataList.get(i);

            legendCtx.setFill(data.getFillColor());
            legendCtx.fillOval(0, (i + 1) * stepSize, size * 0.0375, size * 0.0375);
            legendCtx.setFill(textColor);
            legendCtx.fillText(data.getName(), size * 0.05, (i + 1) * stepSize + canvasHeight * 0.025);
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

        double chartCanvasWidth   = contentBounds.getWidth();
        double chartCanvasHeight  = contentBounds.getHeight();
        double chartCanvasSize    = chartCanvasWidth < chartCanvasHeight ? chartCanvasWidth : chartCanvasHeight;
        double legendCanvasWidth  = width * 0.225;
        double legendCanvasHeight = chartCanvasSize;


        if (tile.isShowing() && width > 0 && height > 0) {
            pane.setMaxSize(width, height);
            pane.setPrefSize(width, height);

            legendCanvas.setWidth(legendCanvasWidth);
            legendCanvas.setHeight(legendCanvasHeight);

            legendCanvas.relocate(contentBounds.getX(), contentBounds.getY());
            legendCanvas.setVisible(width > (height * 1.2));

            chartCanvas.setWidth(chartCanvasSize);
            chartCanvas.setHeight(chartCanvasSize);

            if (width > (height * 1.5)) {
                chartCanvas.relocate((width - chartCanvasSize) * 0.5, contentBounds.getY() + (contentBounds.getHeight() - chartCanvasSize) * 0.5);
            } else if (width > (height * 1.2)) {
                chartCanvas.relocate((width - size * 0.05 - chartCanvasSize), contentBounds.getY() + (contentBounds.getHeight() - chartCanvasSize) * 0.5);
            } else {
                chartCanvas.relocate((width - chartCanvasSize) * 0.5, contentBounds.getY() + (contentBounds.getHeight() - chartCanvasSize) * 0.5);
            }

            resizeStaticText();
        }
    }

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(tile.getTitle());
        text.setText(tile.getText());

        resizeStaticText();
        drawLegend();
        drawChart();

        titleText.setFill(tile.getTitleColor());
        text.setFill(tile.getTextColor());
    }
}
