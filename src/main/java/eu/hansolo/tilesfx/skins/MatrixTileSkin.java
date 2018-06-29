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
import eu.hansolo.tilesfx.chart.PixelMatrix;
import eu.hansolo.tilesfx.chart.PixelMatrix.PixelShape;
import eu.hansolo.tilesfx.chart.PixelMatrixBuilder;
import eu.hansolo.tilesfx.events.ChartDataEventListener;
import eu.hansolo.tilesfx.events.PixelMatrixEventListener;
import eu.hansolo.tilesfx.events.TileEvent;
import eu.hansolo.tilesfx.events.TileEvent.EventType;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.Helper;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.List;


/**
 * Created by hansolo on 01.11.17.
 */
public class MatrixTileSkin extends TileSkin {
    private Text                          titleText;
    private Text                          text;
    private PixelMatrix                   matrix;
    private ListChangeListener<ChartData> chartDataListener;
    private ChartDataEventListener        chartEventListener;
    private Tooltip                       selectionTooltip;
    private PixelMatrixEventListener      matrixListener;
    private EventHandler<MouseEvent>      mouseHandler;


    // ******************** Constructors **************************************
    public MatrixTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        matrix = PixelMatrixBuilder.create()
                                   .pixelShape(PixelShape.SQUARE)
                                   .useSpacer(true)
                                   .squarePixels(false)
                                   .colsAndRows(tile.getMatrixSize())
                                   .pixelOnColor(tile.getBarColor())
                                   .pixelOffColor(Helper.isDark(tile.getBackgroundColor()) ? tile.getBackgroundColor().brighter() : tile.getBackgroundColor().darker())
                                   .build();

        if (!tile.getChartData().isEmpty() && tile.getChartData().size() > 2) {
            matrix.setColsAndRows(tile.getChartData().size(), matrix.getRows());
        }

        chartEventListener = e -> updateMatrixWithChartData();
        tile.getChartData().forEach(chartData -> chartData.addChartDataEventListener(chartEventListener));

        chartDataListener = c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach(addedItem -> addedItem.addChartDataEventListener(chartEventListener));
                    if (!tile.getChartData().isEmpty() && tile.getChartData().size() > 2) {
                        matrix.setColsAndRows(tile.getChartData().size(), matrix.getRows());
                    }
                } else if (c.wasRemoved()) {
                    c.getRemoved().forEach(removedItem -> removedItem.removeChartDataEventListener(chartEventListener));
                    if (!tile.getChartData().isEmpty() && tile.getChartData().size() > 2) {
                        matrix.setColsAndRows(tile.getChartData().size(), matrix.getRows());
                    }
                }
            }
            updateMatrixWithChartData();
        };
        matrixListener    = e -> {
            if (tile.getChartData().isEmpty()) { return; }
            int       column      = e.getX();
            ChartData data        = tile.getChartData().get(column);
            String    tooltipText = new StringBuilder(data.getName()).append("\n").append(String.format(locale, formatString, data.getValue())).toString();
            Point2D popupLocation = new Point2D(e.getMouseScreenX() - selectionTooltip.getWidth() * 0.5, e.getMouseScreenY() - size * 0.025 - selectionTooltip.getHeight());

            selectionTooltip.setText(tooltipText);
            selectionTooltip.setX(popupLocation.getX());
            selectionTooltip.setY(popupLocation.getY());
            selectionTooltip.show(tile.getScene().getWindow());

            tile.fireTileEvent(new TileEvent(EventType.SELECTED_CHART_DATA, data));
        };
        mouseHandler = e -> {
            final javafx.event.EventType<? extends MouseEvent> TYPE = e.getEventType();
            if (MouseEvent.MOUSE_CLICKED.equals(TYPE)) {
                matrix.checkForClick(e);
            } else if (MouseEvent.MOUSE_MOVED.equals(TYPE)) {
                selectionTooltip.hide();
            } else if (MouseEvent.MOUSE_EXITED.equals(TYPE)) {
                selectionTooltip.hide();
            }
        };

        titleText = new Text();
        titleText.setFill(tile.getTitleColor());
        Helper.enableNode(titleText, !tile.getTitle().isEmpty());

        text = new Text(tile.getText());
        text.setFill(tile.getTextColor());
        Helper.enableNode(text, tile.isTextVisible());

        selectionTooltip = new Tooltip("");
        selectionTooltip.setWidth(60);
        selectionTooltip.setHeight(48);
        Tooltip.install(matrix, selectionTooltip);

        getPane().getChildren().addAll(titleText, matrix, text);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        tile.getChartData().addListener(chartDataListener);
        matrix.addPixelMatrixEventListener(matrixListener);
        matrix.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseHandler);
        matrix.addEventHandler(MouseEvent.MOUSE_MOVED, mouseHandler);
        matrix.addEventHandler(MouseEvent.MOUSE_EXITED, mouseHandler);
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !tile.getTitle().isEmpty());
            Helper.enableNode(text, tile.isTextVisible());
        } else if ("RECALC".equals(EVENT_TYPE)) {
            matrix.setColsAndRows(tile.getMatrixSize());
            resize();
        }
    }

    @Override public void dispose() {
        matrix.removeAllPixelMatrixEventListeners();
        matrix.removeEventHandler(MouseEvent.MOUSE_CLICKED, mouseHandler);
        matrix.removeEventHandler(MouseEvent.MOUSE_MOVED, mouseHandler);
        matrix.removeEventHandler(MouseEvent.MOUSE_EXITED, mouseHandler);
        matrix.dispose();
        tile.getChartData().removeListener(chartDataListener);
        tile.getChartData().forEach(chartData -> chartData.removeChartDataEventListener(chartEventListener));
        super.dispose();
    }

    private void updateMatrixWithChartData() {
        List<ChartData> dataList = tile.getChartData();
        int             cols     = dataList.size();
        int             rows     = matrix.getRows();
        double          factor   = rows / tile.getRange();
        Color           offColor = matrix.getPixelOffColor();

        matrix.setAllPixelsOff();
        for (int y = rows ; y >= 0 ; y--) {
            for (int x = 0 ; x < cols; x++) {
                int noOfActivePixels = Helper.roundDoubleToInt((maxValue - dataList.get(x).getValue()) * factor);
                matrix.setPixel(x, y, noOfActivePixels <= y ? dataList.get(x).getFillColor() : offColor);
            }
        }
        matrix.drawMatrix();
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

        double chartWidth  = contentBounds.getWidth();
        double chartHeight = contentBounds.getHeight();

        if (tile.isShowing() && width > 0 && height > 0) {
            pane.setMaxSize(width, height);
            pane.setPrefSize(width, height);

            matrix.setPrefSize(chartWidth, chartHeight);
            matrix.relocate((width - chartWidth) * 0.5, contentBounds.getY() + (contentBounds.getHeight() - chartHeight) * 0.5);

            resizeStaticText();
        }
    }

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(tile.getTitle());
        text.setText(tile.getText());

        resizeStaticText();
        matrix.drawMatrix();

        titleText.setFill(tile.getTitleColor());
        text.setFill(tile.getTextColor());

        matrix.setPixelOnColor(tile.getBarColor());
        matrix.setPixelOffColor(Helper.isDark(tile.getBackgroundColor()) ? tile.getBackgroundColor().brighter() : tile.getBackgroundColor().darker());
    }
}
