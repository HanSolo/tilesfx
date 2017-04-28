/*
 * Copyright (c) 2016 by Gerrit Grunwald
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
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.Helper;
import eu.hansolo.tilesfx.tools.SmoothAreaChart;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.text.Text;


/**
 * Created by hansolo on 19.12.16.
 */
public class AreaChartTileSkin extends TileSkin {
    private Text                            titleText;
    private SmoothAreaChart<String, Number> chart;
    private CategoryAxis                    xAxis;
    private NumberAxis                      yAxis;


    // ******************** Constructors **************************************
    public AreaChartTileSkin(final Tile TILE) {
        super(TILE);
    }

    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        titleText = new Text();
        titleText.setFill(tile.getTitleColor());
        Helper.enableNode(titleText, !tile.getTitle().isEmpty());

        xAxis = new CategoryAxis();
        yAxis = new NumberAxis();

        chart = new SmoothAreaChart<>(xAxis, yAxis);
        chart.getData().addAll(tile.getSeries());
        chart.setLegendSide(Side.TOP);
        chart.setVerticalZeroLineVisible(false);
        chart.setCreateSymbols(false);

        getPane().getChildren().addAll(titleText, chart);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);
    };


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
    };

    @Override protected void resize() {
        super.resize();

        chart.setMinSize(width - size * 0.1, height - size * 0.1);
        chart.setPrefSize(width - size * 0.1, height - size * 0.1);
        chart.setMaxSize(width - size * 0.1, height - size * 0.1);
        chart.setPadding(new Insets(titleText.getLayoutBounds().getHeight() + size * 0.05, 0, 0, 0));
        chart.relocate(size * 0.05, size * 0.05);
    };

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(tile.getTitle());

        resizeStaticText();

        titleText.setFill(tile.getTitleColor());
    };
}