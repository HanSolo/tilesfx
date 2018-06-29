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
import eu.hansolo.tilesfx.events.ChartDataEventListener;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.Helper;
import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.VPos;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

import java.util.List;
import java.util.Locale;

import static eu.hansolo.tilesfx.tools.Helper.enableNode;


public class RadialPercentageTileSkin extends TileSkin {
    private static final double                        ANGLE_RANGE = 360;
    private              double                        size;
    private              double                        chartSize;
    private              Arc                           barBackground;
    private              Arc                           bar;
    private              Arc                           proportionBar;
    private              Line                          separator;
    private              Text                          titleText;
    private              Text                          text;
    private              Text                          percentageValueText;
    private              Text                          percentageUnitText;
    private              TextFlow                      percentageFlow;
    private              Text                          descriptionText;
    private              Text                          unitText;
    private              TextFlow                      valueUnitFlow;
    private              double                        minValue;
    private              double                        range;
    private              double                        angleStep;
    private              double                        referenceValue;
    private              String                        formatString;
    private              Locale                        locale;
    private              double                        sum;
    private              List<ChartData>               dataList;
    private              ChartDataEventListener        chartEventListener;
    private              ListChangeListener<ChartData> chartDataListener;
    private              InvalidationListener          currentValueListener;


    // ******************** Constructors **************************************
    public RadialPercentageTileSkin(Tile TILE) {
        super(TILE);

        setBar(TILE.getCurrentValue());
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        if (tile.isAutoScale()) tile.calcAutoScale();
        minValue             = tile.getMinValue();
        range                = tile.getRange();
        sectionsVisible      = tile.getSectionsVisible();
        sections             = tile.getSections();
        formatString         = new StringBuilder("%.").append(Integer.toString(tile.getDecimals())).append("f").toString();
        locale               = tile.getLocale();
        dataList             = tile.getChartData();
        sum                  = dataList.stream().mapToDouble(ChartData::getValue).sum();
        angleStep            = ANGLE_RANGE / sum;
        referenceValue       = tile.getReferenceValue() < maxValue ? maxValue : tile.getReferenceValue();

        chartEventListener = e -> setProportionBar();
        tile.getChartData().forEach(chartData -> chartData.addChartDataEventListener(chartEventListener));

        chartDataListener = c -> {
            dataList = tile.getChartData();
            sum      = dataList.stream().mapToDouble(ChartData::getValue).sum();
            setProportionBar();
        };
        currentValueListener = o -> setBar(tile.getCurrentValue());

        titleText = new Text();
        titleText.setFill(tile.getTitleColor());
        enableNode(titleText, !tile.getTitle().isEmpty());

        text = new Text(tile.getText());
        text.setFill(tile.getTextColor());
        enableNode(text, tile.isTextVisible());

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

        proportionBar = new Arc(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.5, PREFERRED_WIDTH * 0.468, PREFERRED_HEIGHT * 0.468, 90, 0);
        proportionBar.setType(ArcType.OPEN);
        proportionBar.setStroke(tile.getBarColor());
        proportionBar.setStrokeWidth(PREFERRED_WIDTH * 0.015);
        proportionBar.setStrokeLineCap(StrokeLineCap.BUTT);
        proportionBar.setFill(null);

        separator = new Line(PREFERRED_WIDTH * 0.5, 1, PREFERRED_WIDTH * 0.5, 0.16667 * PREFERRED_HEIGHT);
        separator.setStroke(tile.getBackgroundColor());
        separator.setFill(Color.TRANSPARENT);

        percentageValueText = new Text(String.format(locale, formatString, tile.getCurrentValue()));
        percentageValueText.setFont(Fonts.latoRegular(PREFERRED_WIDTH * 0.27333));
        percentageValueText.setFill(tile.getValueColor());
        percentageValueText.setTextOrigin(VPos.CENTER);

        percentageUnitText = new Text(tile.getUnit());
        percentageUnitText = new Text("\u0025");
        percentageUnitText.setFont(Fonts.latoLight(PREFERRED_WIDTH * 0.08));
        percentageUnitText.setFill(tile.getUnitColor());

        percentageFlow = new TextFlow(percentageValueText, percentageUnitText);
        percentageFlow.setTextAlignment(TextAlignment.CENTER);

        descriptionText = new Text(String.format(locale, formatString, tile.getCurrentValue()));
        descriptionText.setFont(Fonts.latoRegular(PREFERRED_WIDTH * 0.27333));
        descriptionText.setFill(tile.getValueColor());
        descriptionText.setTextOrigin(VPos.CENTER);
        enableNode(descriptionText, tile.isValueVisible());

        unitText = new Text(tile.getUnit());
        unitText = new Text("\u0025");
        unitText.setFont(Fonts.latoLight(PREFERRED_WIDTH * 0.08));
        unitText.setFill(tile.getUnitColor());
        enableNode(unitText, !tile.getUnit().isEmpty());

        valueUnitFlow = new TextFlow(descriptionText, unitText);
        valueUnitFlow.setTextAlignment(TextAlignment.CENTER);

        getPane().getChildren().addAll(barBackground, proportionBar, bar, separator, titleText, text, percentageFlow, valueUnitFlow);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        tile.getChartData().addListener(chartDataListener);
        tile.currentValueProperty().addListener(currentValueListener);
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);
        if ("RECALC".equals(EVENT_TYPE)) {
            referenceValue = tile.getReferenceValue() < maxValue ? maxValue : tile.getReferenceValue();
            angleStep      = ANGLE_RANGE / range;
            sum            = dataList.stream().mapToDouble(ChartData::getValue).sum();
            sections       = tile.getSections();
            redraw();
            setBar(tile.getCurrentValue());
        } else if ("VISIBILITY".equals(EVENT_TYPE)) {
            enableNode(titleText, !tile.getTitle().isEmpty());
            enableNode(text, tile.isTextVisible());
            enableNode(unitText, !tile.getUnit().isEmpty());
            enableNode(descriptionText, tile.isValueVisible());
        }
    }

    private void setBar(final double VALUE) {
        if (minValue > 0) {
            bar.setLength((minValue - VALUE) * angleStep);
        } else {
            bar.setLength(-VALUE * angleStep);
        }

        percentageValueText.setText(String.format(locale, formatString, VALUE / sum * 100.0));
        setProportionBar();
    }

    private void setProportionBar() {
        sum = dataList.stream().mapToDouble(ChartData::getValue).sum();
        proportionBar.setLength(-sum * ANGLE_RANGE / referenceValue);
    }

    @Override public void dispose() {
        tile.currentValueProperty().removeListener(currentValueListener);
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
    @Override protected void resizeDynamicText() {
        double maxWidth = percentageUnitText.isVisible() ? chartSize * 0.7 : chartSize * 0.8;
        double fontSize = chartSize * 0.2;
        percentageValueText.setFont(Fonts.latoRegular(fontSize));
        if (percentageValueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(percentageValueText, maxWidth, fontSize); }

        fontSize = chartSize * 0.08;
        percentageUnitText.setFont(Fonts.latoLight(fontSize));
        if (percentageUnitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(percentageUnitText, maxWidth, fontSize); }

        maxWidth = unitText.isVisible() ? chartSize * 0.3 : chartSize * 0.4;
        fontSize = chartSize * 0.075;
        descriptionText.setFont(Fonts.latoRegular(fontSize));
        if (descriptionText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(descriptionText, maxWidth, fontSize); }

        fontSize = chartSize * 0.04;
        unitText.setFont(Fonts.latoLight(fontSize));
        if (unitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(unitText, maxWidth, fontSize); }
    }

    @Override protected void resize() {
        super.resize();
        width  = tile.getWidth() - tile.getInsets().getLeft() - tile.getInsets().getRight();
        height = tile.getHeight() - tile.getInsets().getTop() - tile.getInsets().getBottom();
        size   = width < height ? width : height;

        if (tile.isShowing() && width > 0 && height > 0) {
            pane.setMaxSize(width, height);
            pane.setPrefSize(width, height);

            double chartWidth  = contentBounds.getWidth();
            double chartHeight = contentBounds.getHeight();
            chartSize = chartWidth < chartHeight ? chartWidth : chartHeight;

            double y = height * 0.15 + (height * (tile.isTextVisible() ? 0.75 : 0.85) - chartSize) * 0.5;
            //double radius = chartSize * 0.495 - contentBounds.getX();

            double radius = chartSize * 0.4135;

            barBackground.setCenterX(contentCenterX);
            barBackground.setCenterY(contentCenterY);
            barBackground.setRadiusX(radius);
            barBackground.setRadiusY(radius);
            barBackground.setStrokeWidth(chartSize * 0.015);

            proportionBar.setCenterX(contentCenterX);
            proportionBar.setCenterY(contentCenterY);
            proportionBar.setRadiusX(chartSize * 0.456);
            proportionBar.setRadiusY(chartSize * 0.456);
            proportionBar.setStrokeWidth(chartSize * 0.015);

            bar.setCenterX(contentCenterX);
            bar.setCenterY(contentCenterY);
            bar.setRadiusX(radius);
            bar.setRadiusY(radius);
            bar.setStrokeWidth(chartSize * 0.1);

            separator.setStartX(contentCenterX);
            separator.setStartY(y + chartSize * 0.0365);
            separator.setEndX(contentCenterX);
            separator.setEndY(y + chartSize * 0.1365);

            resizeStaticText();
            resizeDynamicText();

            percentageFlow.setPrefWidth(contentBounds.getWidth());
            percentageFlow.relocate(contentBounds.getX(), bar.getCenterY() - chartSize * 0.2);

            valueUnitFlow.setPrefWidth(width * 0.6);
            valueUnitFlow.relocate((width * 0.2), bar.getCenterY() + chartSize * 0.06);
        }
    }

    @Override protected void redraw() {
        super.redraw();
        locale           = tile.getLocale();
        formatString     = new StringBuilder("%.").append(Integer.toString(tile.getDecimals())).append("f").toString();
        sectionsVisible  = tile.getSectionsVisible();

        barBackground.setStroke(tile.getBarBackgroundColor());

        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, tile.getGradientStops());

        bar.setStroke(tile.isFillWithGradient() ? gradient : tile.getBarColor());
        proportionBar.setStroke(tile.isFillWithGradient() ? gradient : tile.getBarColor());
        percentageValueText.setFill(tile.getValueColor());
        percentageUnitText.setFill(tile.getUnitColor());
        descriptionText.setFill(tile.getDescriptionColor());
        unitText.setFill(tile.getUnitColor());
        titleText.setFill(tile.getTitleColor());
        text.setFill(tile.getTextColor());
        separator.setStroke(tile.getBackgroundColor());

        titleText.setText(tile.getTitle());
        descriptionText.setText(tile.getDescription());
        text.setText(tile.getText());
        unitText.setText(tile.getUnit());

        referenceValue = tile.getReferenceValue() < maxValue ? maxValue : tile.getReferenceValue();

        resizeStaticText();
        resizeDynamicText();
    }
}
