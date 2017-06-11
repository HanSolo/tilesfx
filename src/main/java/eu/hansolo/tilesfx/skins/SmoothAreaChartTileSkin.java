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
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.Helper;
import eu.hansolo.tilesfx.chart.SmoothAreaChart;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.chart.Axis;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;


/**
 * Created by hansolo on 09.06.17.
 */
public class SmoothAreaChartTileSkin extends TileSkin {
    private Text                            titleText;
    private SmoothAreaChart<String, Number> chart;
    private Axis                            xAxis;
    private Axis                            yAxis;
    private Group                           plotContent;
    private Rectangle                       clip;


    // ******************** Constructors **************************************
    public SmoothAreaChartTileSkin(final Tile TILE) {
        super(TILE);
    }

    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        titleText = new Text();
        titleText.setFill(tile.getTitleColor());
        Helper.enableNode(titleText, !tile.getTitle().isEmpty());

        xAxis = tile.getXAxis();
        xAxis.setVisible(false);
        xAxis.setManaged(false);
        xAxis.setTickLabelsVisible(false);
        xAxis.setTickMarkVisible(false);
        xAxis.setBorder(null);
        xAxis.setPrefSize(0, 0);
        xAxis.setMinSize(0, 0);
        xAxis.setMaxSize(0, 0);

        yAxis = tile.getYAxis();
        yAxis.setVisible(false);
        yAxis.setManaged(false);
        yAxis.setTickLabelsVisible(false);
        yAxis.setTickMarkVisible(false);
        yAxis.setBorder(null);
        yAxis.setPrefSize(0, 0);
        yAxis.setMinSize(0, 0);
        yAxis.setMaxSize(0, 0);

        clip = new Rectangle(0, 0, PREFERRED_HEIGHT, PREFERRED_HEIGHT);

        chart = new SmoothAreaChart<>(xAxis, yAxis);
        chart.getData().addAll(tile.getSeries());
        chart.setLegendSide(Side.TOP);
        chart.setVerticalGridLinesVisible(false);
        chart.setVerticalZeroLineVisible(false);
        chart.setHorizontalGridLinesVisible(false);
        chart.setHorizontalZeroLineVisible(false);
        chart.setLegendVisible(false);
        chart.enableLegend(false);
        chart.setCreateSymbols(false);
        chart.setClip(clip);

        plotContent = chart.getPlotContent();
        //plotContent.setClip(clip);

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
        chart.setMinSize(width * 1.175, height * 0.6);
        chart.setPrefSize(width * 1.175, height * 0.6);
        chart.setMaxSize(width * 1.175, height * 0.6);
        chart.setPadding(new Insets(0));
        chart.relocate((width - chart.getLayoutBounds().getWidth()) * 0.5, height - chart.getLayoutBounds().getHeight());

        clip.setX(plotContent.getLayoutBounds().getMinX());
        clip.setY(plotContent.getLayoutBounds().getMinY());
        clip.setWidth(plotContent.getLayoutBounds().getWidth());
        clip.setHeight(plotContent.getLayoutBounds().getHeight());
        clip.setArcWidth(tile.getRoundedCorners() ? size * 0.05 : 0.0);
        clip.setArcHeight(tile.getRoundedCorners() ? size * 0.05 : 0.0);
    };

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(tile.getTitle());

        resizeStaticText();

        titleText.setFill(tile.getTitleColor());
    };
}