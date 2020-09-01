/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2016-2020 Gerrit Grunwald.
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
import eu.hansolo.tilesfx.events.TileEvent.EventType;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.GradientLookup;
import eu.hansolo.tilesfx.tools.Helper;
import javafx.collections.ListChangeListener;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

import java.util.Locale;
import java.util.stream.Collectors;


public class RadialDistributionTileSkin extends TileSkin {
    private double          angleRange;
    private Arc             barBackground;
    private Canvas          canvas;
    private GraphicsContext ctx;
    private Text            titleText;
    private Text            text;
    private Text            valueText;
    private Text            unitText;
    private Text            descriptionText;
    private TextFlow        valueUnitFlow;
    private Text            minValueText;
    private Text            maxValueText;
    private double          average;
    private double          percentageTooLow;
    private double          percentageTooHigh;
    private Color           barBackgroundColor;
    private GradientLookup  gradientLookup;


    // ******************** Constructors **************************************
    public RadialDistributionTileSkin(final Tile TILE) {
        super(TILE);
        handleCurrentValue(tile.getValue());
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        angleRange = tile.getAngleRange();
        angleStep  = tile.getAngleStep();

        if (tile.isAutoScale()) tile.calcAutoScale();

        barBackgroundColor = tile.getBarBackgroundColor();
        gradientLookup     = new GradientLookup(tile.getGradientStops());

        barBackground = new Arc(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.696, PREFERRED_WIDTH * 0.275, PREFERRED_WIDTH * 0.275, angleRange * 0.5 + 90, -angleRange);
        barBackground.setType(ArcType.OPEN);
        barBackground.setStroke(barBackgroundColor);
        barBackground.setStrokeWidth(PREFERRED_WIDTH * 0.02819549 * 2);
        barBackground.setStrokeLineCap(StrokeLineCap.ROUND);
        barBackground.setFill(null);

        canvas = new Canvas();
        ctx    = canvas.getGraphicsContext2D();

        titleText = new Text(tile.getTitle());
        titleText.setFill(tile.getTitleColor());
        Helper.enableNode(titleText, !tile.getTitle().isEmpty());

        valueText = new Text(String.format(locale, formatString, tile.getCurrentValue()));
        valueText.setFill(tile.getValueColor());
        Helper.enableNode(valueText, tile.isValueVisible() && !tile.isAlert());

        unitText = new Text(tile.getUnit());
        unitText.setFill(tile.getUnitColor());
        Helper.enableNode(unitText, !tile.getUnit().isEmpty());

        valueUnitFlow = new TextFlow(valueText, unitText);
        valueUnitFlow.setTextAlignment(TextAlignment.CENTER);

        descriptionText = new Text(tile.getDescription());
        descriptionText.setFill(tile.getDescriptionColor());

        minValueText = new Text(String.format(locale, "%." + tile.getTickLabelDecimals() + "f", tile.getMinValue()));
        minValueText.setFill(tile.getTitleColor());
        minValueText.setTextOrigin(VPos.CENTER);

        maxValueText = new Text(String.format(locale, "%." + tile.getTickLabelDecimals() + "f", tile.getMaxValue()));
        maxValueText.setFill(tile.getTitleColor());
        maxValueText.setTextOrigin(VPos.CENTER);

        text = new Text(tile.getText());
        text.setTextOrigin(VPos.TOP);
        text.setFill(tile.getTextColor());

        getPane().getChildren().addAll(barBackground, canvas, titleText, descriptionText, valueUnitFlow, minValueText, maxValueText, text);

        updateValues();
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        tile.getChartData().addListener((ListChangeListener<ChartData>) c -> updateValues());
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);
        if (EventType.VISIBILITY.name().equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !tile.getTitle().isEmpty());
            Helper.enableNode(valueText, tile.isValueVisible());
            Helper.enableNode(valueUnitFlow, !tile.getUnit().isEmpty());
            Helper.enableNode(minValueText, tile.getMinValueVisible());
            Helper.enableNode(maxValueText, tile.getMaxValueVisible());
            Helper.enableNode(text, tile.isTextVisible());
        }
    }

    @Override protected void handleCurrentValue(final double VALUE) {
        resizeDynamicText();
    }

    private void updateValues() {
        if (!tile.getChartData().isEmpty()) {
            double noOfEntriesInRange = tile.getChartData().stream().filter(entry -> entry.getValue() >= tile.getLowerThreshold() && entry.getValue() <= tile.getThreshold()).collect(Collectors.toList()).size();
            double noOfEntries       = tile.getChartData().size();
            double percentageInRange = (noOfEntriesInRange / noOfEntries) * 100.0;
            valueText.setText(String.format(Locale.US, "%.0f", percentageInRange));

            average = tile.getChartData().stream().mapToDouble(ChartData::getValue).sum() / noOfEntries;
            double noOfEntriesTooLow  = tile.getChartData().stream().filter(entry -> entry.getValue() < tile.getLowerThreshold()).collect(Collectors.toList()).size();
            double noOfEntriesTooHigh = tile.getChartData().stream().filter(entry -> entry.getValue() > tile.getThreshold()).collect(Collectors.toList()).size();

            percentageTooLow  = (noOfEntriesTooLow / noOfEntries) * 100.0;
            percentageTooHigh = (noOfEntriesTooHigh / noOfEntries) * 100.0;
        }
        drawDots();
    }

    // ******************** Resizing ******************************************
    @Override public void layoutChildren(final double x, final double y, final double w, final double h) {
        super.layoutChildren(x, y, w, h);
        resizeDynamicText();
        resizeStaticText();
    }

    @Override protected void resizeDynamicText() {
        double maxWidth = unitText.isManaged() ? width - size * 0.275 : width - size * 0.1;
        double fontSize = size * 0.24;
        valueText.setFont(Fonts.latoRegular(fontSize));
        if (valueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(valueText, maxWidth, fontSize); }
    }

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

        maxWidth = width - (width - size * 0.275);

        fontSize = size * 0.10;
        descriptionText.setFont(Fonts.latoLight(fontSize));
        if (descriptionText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(descriptionText, maxWidth, fontSize); }
        descriptionText.setX(width * 0.5 - descriptionText.getLayoutBounds().getWidth() * 0.5);
        descriptionText.setY(height * 0.44);

        fontSize = size * 0.12;
        unitText.setFont(Fonts.latoRegular(fontSize));
        if (unitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(unitText, maxWidth, fontSize); }

        fontSize = size * 0.04;
        minValueText.setFont(Fonts.latoRegular(fontSize));
        minValueText.setText(String.format(locale, "%.0f", tile.getMinValue()));
        minValueText.setX(width * 0.5 - barBackground.getRadiusX() * 0.9);

        minValueText.setY(text.isManaged() ? (contentBounds.getCenterY() + size * 0.25) : (contentBounds.getCenterY() + size * 0.325));

        maxValueText.setFont(Fonts.latoRegular(fontSize));
        maxValueText.setText(String.format(locale, "%.0f", tile.getMaxValue()));
        maxValueText.setX(width * 0.5 + barBackground.getRadiusX() * 0.9 - maxValueText.getLayoutBounds().getWidth());
        maxValueText.setY(text.isManaged() ? (contentBounds.getCenterY() + size * 0.25) : (contentBounds.getCenterY() + size * 0.325));

        fontSize = size * 0.06;

        text.setText(tile.getText());
        text.setFont(font);
        if (text.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(text, maxWidth, fontSize); }
        switch(tile.getTextAlignment()) {
            default    :
            case LEFT  : text.setX(size * 0.05); break;
            case CENTER: text.setX((width - text.getLayoutBounds().getWidth()) * 0.5); break;
            case RIGHT : text.setX(width - (size * 0.05) - text.getLayoutBounds().getWidth()); break;
        }
        text.setY(height - size * 0.1);
    }

    @Override protected void resize() {
        super.resize();
        double centerX   = width * 0.5;
        double centerY   = height * 0.5;
        double barRadius = size * 0.3;
        double barWidth  = size * 0.07;

        barBackground.setCenterX(centerX);
        barBackground.setCenterY(centerY);
        barBackground.setRadiusX(barRadius);
        barBackground.setRadiusY(barRadius);
        barBackground.setStrokeWidth(barWidth);
        barBackground.setStartAngle(angleRange * 0.5 + 90);
        barBackground.setLength(-angleRange);

        canvas.setWidth(contentBounds.getWidth());
        canvas.setHeight(contentBounds.getHeight());
        canvas.relocate(contentBounds.getMinX(), contentBounds.getMinY());

        resizeStaticText();
        resizeDynamicText();

        valueUnitFlow.setPrefWidth(width - doubleInset);
        valueUnitFlow.relocate(inset, height * 0.5 - size * 0.07);
    }

    @Override protected void redraw() {
        super.redraw();
        angleRange = tile.getAngleRange();
        angleStep  = tile.getAngleStep();
        titleText.setText(tile.getTitle());
        descriptionText.setText(tile.getDescription());
        text.setText(tile.getText());
        unitText.setText("%");
        minValueText.setText(String.format(locale, tickLabelFormatString, tile.getMinValue()));
        maxValueText.setText(String.format(locale, tickLabelFormatString, tile.getMaxValue()));
        resizeStaticText();

        barBackgroundColor = tile.getBarBackgroundColor();

        if (tile.isStrokeWithGradient()) {
            gradientLookup.setStops(tile.getGradientStops());
        }

        drawDots();

        barBackground.setStroke(tile.getBarBackgroundColor());
        barBackground.setVisible(false);
        titleText.setFill(tile.getTitleColor());
        descriptionText.setFill(tile.getDescriptionColor());
        text.setFill(tile.getTextColor());
        minValueText.setFill(tile.getTitleColor());
        maxValueText.setFill(tile.getTitleColor());
        valueText.setFill(tile.getValueColor());
        unitText.setFill(tile.getUnitColor());
    }

    private void drawDots() {
        double width  = canvas.getWidth();
        double height = canvas.getHeight();
        double size   = width < height ? width : height;

        ctx.clearRect(0, 0, width, height);

        double centerX     = width * 0.5;
        double centerY     = height * 0.5;
        double radius      = size * 0.375;
        double diameter    = size * 0.08;
        double range       = maxValue - minValue;
        double startAngle  = tile.getStartAngle();
        double angleRange  = tile.getAngleRange();
        double angleStep   = angleRange / (range);
        double dotDiameter = diameter * 0.95;

        double barBackgroundX = centerX - radius - diameter * 0.5;
        double barBackgroundY = centerY - radius - diameter * 0.5;
        double barBackgroundW = radius * 2.0 + diameter;
        double barBackgroundH = radius * 2.0 + diameter;

        double rangeLowerBound = tile.getLowerThreshold();
        double rangeUpperBound = tile.getThreshold();

        double targetStartAngle = startAngle + angleRange - (rangeLowerBound * angleStep);
        double targetAngleRange = -(rangeUpperBound - rangeLowerBound) * angleStep;

        double offsetX = barBackgroundX + diameter * 1.05;
        double offsetY = barBackgroundY + diameter * 1.05;

        double angleOffset = startAngle - 45.0;

        boolean strokeWithGradient = tile.isStrokeWithGradient();

        // Draw dots
        ctx.setStroke(tile.getBarBackgroundColor());
        ctx.setLineWidth(diameter);
        ctx.setLineCap(StrokeLineCap.ROUND);
        ctx.strokeArc(barBackgroundX, barBackgroundY, barBackgroundW, barBackgroundH, startAngle, angleRange, ArcType.OPEN);
        ctx.setLineWidth(1);
        ctx.setStroke(Color.TRANSPARENT);

        if (!strokeWithGradient) {
            ctx.setFill(Helper.getColorWithOpacity(tile.getBarColor(), 0.1));
        }
        for (ChartData data : tile.getChartData()) {
            double angle = angleOffset + (data.getValue() * angleStep);
            ctx.save();
            ctx.translate(centerX, centerY);
            ctx.rotate(angle);
            ctx.translate(-centerX, -centerY);
            if (strokeWithGradient) {
                ctx.setFill(gradientLookup.getColorAt(data.getValue() / range));
            }
            ctx.fillOval(offsetX, offsetY, dotDiameter, dotDiameter);
            ctx.restore();
        }

        // Draw range
        ctx.save();
        double[] xy1  = Helper.rotatePointAroundRotationCenter(centerX - radius - diameter, centerY, centerX, centerY, -30 + rangeLowerBound * angleStep);
        double[] xyc1 = Helper.rotatePointAroundRotationCenter(centerX - radius - diameter, centerY, centerX, centerY, -30 + rangeUpperBound * angleStep + 8);
        double[] xyc2 = Helper.rotatePointAroundRotationCenter(centerX - radius, centerY, centerX, centerY, -30 + rangeUpperBound * angleStep + 8);
        double[] xy3  = Helper.rotatePointAroundRotationCenter(centerX - radius, centerY, centerX, centerY, -30 + rangeUpperBound * angleStep);
        double[] xyc3 = Helper.rotatePointAroundRotationCenter(centerX - radius, centerY, centerX, centerY, -30 + rangeLowerBound * angleStep - 8);
        double[] xyc4 = Helper.rotatePointAroundRotationCenter(centerX - radius - diameter, centerY, centerX, centerY, -30 + rangeLowerBound * angleStep - 8);

        ctx.beginPath();
        ctx.moveTo(xy1[0], xy1[1]);
        ctx.arc(centerX, centerY, radius + diameter, radius + diameter, targetStartAngle, targetAngleRange);
        ctx.bezierCurveTo(xyc1[0], xyc1[1], xyc2[0], xyc2[1], xy3[0], xy3[1]);
        ctx.arc(centerX, centerY, radius, radius, targetStartAngle + targetAngleRange, -targetAngleRange);
        ctx.bezierCurveTo(xyc3[0], xyc3[1], xyc4[0], xyc4[1], xy1[0], xy1[1]);
        ctx.closePath();

        ctx.setFill(Color.TRANSPARENT);
        ctx.setStroke(tile.getForegroundColor());
        ctx.stroke();
        ctx.restore();

        // Draw average text rotated around center
        ctx.save();
        ctx.setTextAlign(TextAlignment.CENTER);
        ctx.setTextBaseline(VPos.CENTER);
        ctx.setFill(tile.getForegroundColor());
        ctx.setFont(Fonts.latoBold(size * 0.04));
        ctx.translate(centerX, centerY);
        ctx.rotate(-30 + average * angleStep);

        // Rotate text 90 deg
        ctx.save();
        ctx.translate(-radius - diameter * 0.5, 0);
        ctx.rotate(-90);
        ctx.fillText(String.format(Locale.US, "%.0f", average), 0, 0);
        ctx.translate(radius + diameter * 0.5, 0);
        ctx.restore();

        ctx.translate(-centerX, -centerY);
        ctx.restore();

        // Draw average, percentage low and percentage high
        ctx.setFill(tile.getForegroundColor());
        ctx.setTextAlign(TextAlignment.CENTER);
        ctx.setTextBaseline(VPos.CENTER);
        ctx.setFont(Fonts.latoRegular(size * 0.05));
        ctx.fillText(String.format(Locale.US, "%.0f", average), width * 0.25, height * 0.9);
        ctx.setFont(Fonts.latoLight(size * 0.05));
        ctx.fillText("AVG", width * 0.25, height * 0.95);
        ctx.setFont(Fonts.latoRegular(size * 0.05));
        ctx.fillText(String.format(Locale.US, "%.0f%%", percentageTooHigh), width * 0.5, height * 0.9);
        ctx.setFont(Fonts.latoLight(size * 0.05));
        ctx.fillText("HIGH", width * 0.5, height * 0.95);
        ctx.setFont(Fonts.latoRegular(size * 0.05));
        ctx.fillText(String.format(Locale.US, "%.0f%%", percentageTooLow), width * 0.75, height * 0.9);
        ctx.setFont(Fonts.latoLight(size * 0.05));
        ctx.fillText("LOW", width * 0.75, height * 0.95);
    }
}
