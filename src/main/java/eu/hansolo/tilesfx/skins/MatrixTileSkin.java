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
import eu.hansolo.tilesfx.chart.DotMatrix;
import eu.hansolo.tilesfx.chart.DotMatrix.DotShape;
import eu.hansolo.tilesfx.chart.DotMatrixBuilder;
import eu.hansolo.tilesfx.events.ChartDataEventListener;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.Helper;
import javafx.collections.ListChangeListener;
import javafx.scene.text.Text;


/**
 * Created by hansolo on 01.11.17.
 */
public class MatrixTileSkin extends TileSkin {
    private Text                          titleText;
    private Text                          text;
    private DotMatrix                     matrix;
    private ListChangeListener<ChartData> chartDataListener;
    private ChartDataEventListener        chartEventListener;


    // ******************** Constructors **************************************
    public MatrixTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        matrix = DotMatrixBuilder.create()
                                 .dotShape(DotShape.SQUARE)
                                 .useSpacer(true)
                                 .colsAndRows(12, 30)
                                 .dotOnColor(tile.getBarColor())
                                 .dotOffColor(Helper.isDark(tile.getBackgroundColor()) ? tile.getBackgroundColor().brighter() : tile.getBackgroundColor().darker())
                                 .build();

        chartEventListener = e -> matrix.drawMatrix();
        tile.getChartData().forEach(chartData -> chartData.addChartDataEventListener(chartEventListener));

        chartDataListener  = c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach(addedItem -> addedItem.addChartDataEventListener(chartEventListener));
                } else if (c.wasRemoved()) {
                    c.getRemoved().forEach(removedItem -> removedItem.removeChartDataEventListener(chartEventListener));
                }
            }
            matrix.drawMatrix();
        };

        titleText = new Text();
        titleText.setFill(tile.getTitleColor());
        Helper.enableNode(titleText, !tile.getTitle().isEmpty());

        text = new Text(tile.getText());
        text.setFill(tile.getTextColor());
        Helper.enableNode(text, tile.isTextVisible());

        getPane().getChildren().addAll(titleText, matrix, text);
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
        }
    }

    @Override public void dispose() {
        matrix.dispose();
        tile.getChartData().removeListener(chartDataListener);
        tile.getChartData().forEach(chartData -> chartData.removeChartDataEventListener(chartEventListener));
        super.dispose();
    }


    // ******************** Resizing ******************************************
    @Override protected void resizeStaticText() {
        double maxWidth = width - size * 0.1;
        double fontSize = size * textSize.factor;

        titleText.setFont(Fonts.latoRegular(fontSize));
        if (titleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(titleText, maxWidth, fontSize); }
        switch(tile.getTitleAlignment()) {
            default    :
            case LEFT  : titleText.relocate(size * 0.05, size * 0.05); break;
            case CENTER: titleText.relocate((width - titleText.getLayoutBounds().getWidth()) * 0.5, size * 0.05); break;
            case RIGHT : titleText.relocate(width - (size * 0.05) - titleText.getLayoutBounds().getWidth(), size * 0.05); break;
        }

        text.setFont(Fonts.latoRegular(fontSize));
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
        width  = tile.getWidth() - tile.getInsets().getLeft() - tile.getInsets().getRight();
        height = tile.getHeight() - tile.getInsets().getTop() - tile.getInsets().getBottom();
        size   = width < height ? width : height;

        double chartWidth   = width - size * 0.1;
        double chartHeight  = tile.isTextVisible() ? height - size * 0.28 : height - size * 0.205;
        double chartSize    = chartWidth < chartHeight ? chartWidth : chartHeight;

        if (width > 0 && height > 0) {
            pane.setMaxSize(width, height);
            pane.setPrefSize(width, height);

            matrix.setPrefSize(chartWidth, chartHeight);
            matrix.relocate((width - chartWidth) * 0.5, height * 0.15 + (height * (tile.isTextVisible() ? 0.75 : 0.85) - chartHeight) * 0.5);

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

        matrix.setDotOnColor(tile.getBarColor());
        matrix.setDotOffColor(Helper.isDark(tile.getBackgroundColor()) ? tile.getBackgroundColor().brighter() : tile.getBackgroundColor().darker());
    }
}
