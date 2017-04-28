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

import eu.hansolo.tilesfx.Section;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.Helper;
import javafx.beans.InvalidationListener;
import javafx.geometry.VPos;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

import java.util.List;
import java.util.Locale;


/**
 * Created by hansolo on 03.03.17.
 */
public class CircularProgressTileSkin extends TileSkin {
    private static final double  ANGLE_RANGE = 360;
    private double               size;
    private double               chartSize;
    private Arc                  barBackground;
    private Arc                  bar;
    private Line                 separator;
    private Text                 titleText;
    private Text                 text;
    private Text                 valueText;
    private Text                 unitText;
    private TextFlow             valueUnitFlow;
    private double               minValue;
    private double               range;
    private double               angleStep;
    private boolean              sectionsVisible;
    private List<Section>        sections;
    private String               formatString;
    private Locale               locale;
    private InvalidationListener currentValueListener;


    // ******************** Constructors **************************************
    public CircularProgressTileSkin(Tile TILE) {
        super(TILE);

        setBar(TILE.getCurrentValue());
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        if (tile.isAutoScale()) tile.calcAutoScale();
        minValue             = tile.getMinValue();
        range                = tile.getRange();
        angleStep            = ANGLE_RANGE / range;
        sectionsVisible      = tile.getSectionsVisible();
        sections             = tile.getSections();
        formatString         = new StringBuilder("%.").append(Integer.toString(tile.getDecimals())).append("f").toString();
        locale               = tile.getLocale();
        currentValueListener = o -> setBar(tile.getCurrentValue());

        titleText = new Text();
        titleText.setFill(tile.getTitleColor());
        Helper.enableNode(titleText, !tile.getTitle().isEmpty());

        text = new Text(tile.getText());
        text.setFill(tile.getTextColor());
        Helper.enableNode(text, tile.isTextVisible());

        barBackground = new Arc(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.5, PREFERRED_WIDTH * 0.468, PREFERRED_HEIGHT * 0.468, 90, 360);
        barBackground.setType(ArcType.OPEN);
        barBackground.setStroke(tile.getBarBackgroundColor());
        barBackground.setStrokeWidth(PREFERRED_WIDTH * 0.1);
        barBackground.setStrokeLineCap(StrokeLineCap.BUTT);
        barBackground.setFill(null);

        bar = new Arc(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.5, PREFERRED_WIDTH * 0.468, PREFERRED_HEIGHT * 0.468, 90, 0);
        bar.setType(ArcType.OPEN);
        bar.setStroke(tile.getBarColor());
        bar.setStrokeWidth(PREFERRED_WIDTH * 0.1);
        bar.setStrokeLineCap(StrokeLineCap.BUTT);
        bar.setFill(null);

        separator = new Line(PREFERRED_WIDTH * 0.5, 1, PREFERRED_WIDTH * 0.5, 0.16667 * PREFERRED_HEIGHT);
        separator.setStroke(tile.getBackgroundColor());
        separator.setFill(Color.TRANSPARENT);

        valueText = new Text(String.format(locale, formatString, tile.getCurrentValue()));
        valueText.setFont(Fonts.latoRegular(PREFERRED_WIDTH * 0.27333));
        valueText.setFill(tile.getValueColor());
        valueText.setTextOrigin(VPos.CENTER);
        Helper.enableNode(valueText, tile.isValueVisible());

        unitText = new Text(tile.getUnit());
        unitText.setFont(Fonts.latoLight(PREFERRED_WIDTH * 0.08));
        unitText.setFill(tile.getUnitColor());
        Helper.enableNode(unitText, !tile.getUnit().isEmpty());

        valueUnitFlow = new TextFlow(valueText, unitText);
        valueUnitFlow.setTextAlignment(TextAlignment.CENTER);

        getPane().getChildren().addAll(barBackground, bar, separator, titleText, text, valueUnitFlow);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        tile.currentValueProperty().addListener(currentValueListener);
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if ("RECALC".equals(EVENT_TYPE)) {
            minValue  = tile.getMinValue();
            range     = tile.getRange();
            angleStep = ANGLE_RANGE / range;
            sections  = tile.getSections();
            redraw();
            setBar(tile.getCurrentValue());
        } else if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !tile.getTitle().isEmpty());
            Helper.enableNode(text, tile.isTextVisible());
            Helper.enableNode(unitText, !tile.getUnit().isEmpty());
            Helper.enableNode(valueText, tile.isValueVisible());
        }
    };

    private void setBar(final double VALUE) {
        if (minValue > 0) {
            bar.setLength((minValue - VALUE) * angleStep);
        } else {
            bar.setLength(-VALUE * angleStep);
        }
        setBarColor(VALUE);
        valueText.setText(String.format(locale, formatString, VALUE / tile.getRange() * 100.0));
    }
    
    private void setBarColor(final double VALUE) {
        if (!sectionsVisible) {
            bar.setStroke(tile.getBarColor());
        } else {
            bar.setStroke(tile.getBarColor());
            for (Section section : sections) {
                if (section.contains(VALUE)) {
                    bar.setStroke(section.getColor());
                    break;
                }
            }
        }
    }

    @Override public void dispose() {
        tile.currentValueProperty().removeListener(currentValueListener);
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
    };
    @Override protected void resizeDynamicText() {
        double maxWidth = unitText.isVisible() ? chartSize * 0.7 : chartSize * 0.8;
        double fontSize = chartSize * 0.2;
        valueText.setFont(Fonts.latoRegular(fontSize));
        if (valueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(valueText, maxWidth, fontSize); }

        maxWidth = chartSize * 0.1;
        fontSize = chartSize * 0.08;
        unitText.setFont(Fonts.latoLight(fontSize));
        if (unitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(valueText, maxWidth, fontSize); }
    }

    @Override protected void resize() {
        super.resize();
        width  = tile.getWidth() - tile.getInsets().getLeft() - tile.getInsets().getRight();
        height = tile.getHeight() - tile.getInsets().getTop() - tile.getInsets().getBottom();
        size   = width < height ? width : height;

        double chartWidth  = width - size * 0.1;
        double chartHeight = tile.isTextVisible() ? height - size * 0.28 : height - size * 0.205;
        chartSize = chartWidth < chartHeight ? chartWidth : chartHeight;

        if (width > 0 && height > 0) {
            pane.setMaxSize(width, height);
            pane.setPrefSize(width, height);

            double x = (width - chartSize) * 0.5;
            double y = height * 0.15 + (height * (tile.isTextVisible() ? 0.75 : 0.85) - chartSize) * 0.5;

            barBackground.setCenterX(x + chartSize * 0.5);
            barBackground.setCenterY(y + chartSize * 0.5);
            barBackground.setRadiusX(chartSize * 0.4135);
            barBackground.setRadiusY(chartSize * 0.4135);
            barBackground.setStrokeWidth(chartSize * 0.1);

            bar.setCenterX(x + chartSize * 0.5);
            bar.setCenterY(y + chartSize * 0.5);
            bar.setRadiusX(chartSize * 0.4135);
            bar.setRadiusY(chartSize * 0.4135);
            bar.setStrokeWidth(chartSize * 0.1);

            separator.setStartX(x + chartSize * 0.5);
            separator.setStartY(y + chartSize * 0.0365);
            separator.setEndX(x + chartSize * 0.5);
            separator.setEndY(y + chartSize * 0.1365);

            resizeStaticText();

            valueUnitFlow.setPrefWidth(width * 0.9);
            valueUnitFlow.relocate(width * 0.05, bar.getCenterY() - chartSize * 0.12);
        }
    }

    @Override protected void redraw() {
        super.redraw();
        locale           = tile.getLocale();
        formatString     = new StringBuilder("%.").append(Integer.toString(tile.getDecimals())).append("f").toString();
        sectionsVisible  = tile.getSectionsVisible();

        barBackground.setStroke(tile.getBarBackgroundColor());
        setBarColor(tile.getCurrentValue());
        valueText.setFill(tile.getValueColor());
        unitText.setFill(tile.getUnitColor());
        titleText.setFill(tile.getTitleColor());
        text.setFill(tile.getTextColor());
        separator.setStroke(tile.getBackgroundColor());

        titleText.setText(tile.getTitle());
        text.setText(tile.getText());
        unitText.setText(tile.getUnit());

        resizeDynamicText();
        resizeStaticText();
    }
}
