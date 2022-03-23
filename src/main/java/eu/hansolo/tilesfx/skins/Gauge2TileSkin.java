/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2016-2021 Gerrit Grunwald.
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
import eu.hansolo.tilesfx.events.TileEvt;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.AngleConicalGradient;
import eu.hansolo.tilesfx.tools.Helper;
import eu.hansolo.toolboxfx.GradientLookup;
import javafx.geometry.VPos;
import javafx.scene.CacheHint;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.scene.transform.Rotate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Gauge2TileSkin extends TileSkin {
    private double               angleRange;
    private double               angleStep;
    private double               oldValue;
    private Circle               knob;
    private Arc                  barBackground;
    private Arc                  bar;
    private Path                 needle;
    private Rotate               needleRotate;
    private Rotate               needleRectRotate;
    private Text                 titleText;
    private Text                 text;
    private Text                 valueText;
    private Text                 unitText;
    private TextFlow             valueUnitFlow;
    private Text                 minValueText;
    private Text                 maxValueText;
    private Color                barBackgroundColor;
    private GradientLookup       gradientLookup;
    private AngleConicalGradient conicalGradient;
    private Rectangle            barBounds;


    // ******************** Constructors **************************************
    public Gauge2TileSkin(final Tile TILE) {
        super(TILE);
        handleCurrentValue(tile.getValue());
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        angleRange = tile.getAngleRange();
        angleStep  = tile.getAngleStep();

        if (tile.isAutoScale()) tile.calcAutoScale();
        oldValue = tile.getValue();

        barBackgroundColor = tile.getBarBackgroundColor();
        gradientLookup     = new GradientLookup(tile.getGradientStops());

        knob = new Circle();

        barBackground = new Arc(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.696, PREFERRED_WIDTH * 0.275, PREFERRED_WIDTH * 0.275, angleRange * 0.5 + 90, -angleRange);
        barBackground.setType(ArcType.OPEN);
        barBackground.setStroke(barBackgroundColor);
        barBackground.setStrokeWidth(PREFERRED_WIDTH * 0.02819549 * 2);
        barBackground.setStrokeLineCap(StrokeLineCap.ROUND);
        barBackground.setFill(null);

        bar = new Arc(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.696, PREFERRED_WIDTH * 0.275, PREFERRED_WIDTH * 0.275, angleRange * 0.5 + 90, 0);
        bar.setType(ArcType.OPEN);
        bar.setStrokeWidth(PREFERRED_WIDTH * 0.02819549 * 2);
        bar.setStrokeLineCap(StrokeLineCap.ROUND);
        bar.setFill(null);

        barBounds = new Rectangle();

        createConicalGradient();

        needleRotate     = new Rotate((tile.getValue() - oldValue - minValue) * angleStep);
        needleRectRotate = new Rotate((tile.getValue() - oldValue - minValue) * angleStep);

        needle = new Path();
        needle.setFillRule(FillRule.EVEN_ODD);
        needle.getTransforms().setAll(needleRotate);
        needle.setFill(tile.getNeedleColor());
        needle.setStrokeWidth(0);
        needle.setStroke(Color.TRANSPARENT);

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

        minValueText = new Text(String.format(locale, "%." + tile.getTickLabelDecimals() + "f", tile.getMinValue()));
        minValueText.setFill(tile.getTitleColor());
        minValueText.setTextOrigin(VPos.CENTER);

        maxValueText = new Text(String.format(locale, "%." + tile.getTickLabelDecimals() + "f", tile.getMaxValue()));
        maxValueText.setFill(tile.getTitleColor());
        maxValueText.setTextOrigin(VPos.CENTER);

        text = new Text(tile.getText());
        text.setTextOrigin(VPos.TOP);
        text.setFill(tile.getTextColor());

        getPane().getChildren().addAll(knob, barBackground, bar, needle, titleText, valueUnitFlow, minValueText, maxValueText, text);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);
        if (TileEvt.VISIBILITY.getName().equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !tile.getTitle().isEmpty());
            Helper.enableNode(valueText, tile.isValueVisible());
            Helper.enableNode(valueUnitFlow, !tile.getUnit().isEmpty());
            Helper.enableNode(minValueText, tile.getMinValueVisible());
            Helper.enableNode(maxValueText, tile.getMaxValueVisible());
            Helper.enableNode(text, tile.isTextVisible());
        }
    }

    @Override protected void handleCurrentValue(final double VALUE) {
        double needleStartAngle = angleRange * 0.5;
        double targetAngle = (VALUE - minValue) * angleStep - needleStartAngle;
        targetAngle = Helper.clamp(-needleStartAngle, -needleStartAngle + angleRange, targetAngle);
        needleRotate.setAngle(targetAngle);
        needleRectRotate.setAngle(targetAngle);
        bar.setLength(-angleStep * (VALUE - minValue));
        if (tile.isStrokeWithGradient()) {
            needle.setFill(gradientLookup.getColorAt((VALUE - minValue) / tile.getRange()));
            bar.setStroke(conicalGradient.getImagePattern(barBounds));
        } else {
            needle.setFill(tile.getNeedleColor());
            bar.setStroke(tile.getBarColor());
        }

        if (tile.getShortenNumbers()) {
            valueText.setText(Helper.shortenNumber((long) VALUE));
        } else if (tile.getCustomDecimalFormatEnabled()) {
            valueText.setText(decimalFormat.format(VALUE));
        } else {
            valueText.setText(String.format(locale, formatString, VALUE));
        }
        resizeDynamicText();
    }

    private void drawNeedle() {
        double needleWidth  = size * 0.04536638;
        double needleHeight = size * 0.23706897;
        needle.setCache(false);
        needle.getElements().clear();
        needle.getElements().add(new MoveTo(needleWidth * 0.813182897862233, needleHeight *0.227272727272727));
        needle.getElements().add(new CubicCurveTo(needleWidth * 0.754441805225653, needleHeight *0.0743545454545455, needleWidth *0.788052256532067, needleHeight * 0, needleWidth * 0.499643705463183, needleHeight * 0));
        needle.getElements().add(new CubicCurveTo(needleWidth * 0.211235154394299, needleHeight *0, needleWidth *0.248907363420428, needleHeight * 0.0741090909090909, needleWidth * 0.186104513064133, needleHeight * 0.227272727272727));
        needle.getElements().add(new LineTo(needleWidth * 0.000831353919239905, needleHeight * 0.886363636363636));
        needle.getElements().add(new CubicCurveTo(needleWidth * -0.0155581947743468, needleHeight *0.978604545454545, needleWidth *0.211235154394299, needleHeight * 1, needleWidth * 0.499643705463183, needleHeight * 1));
        needle.getElements().add(new CubicCurveTo(needleWidth * 0.788052256532067, needleHeight *1, needleWidth *1.0253919239905, needleHeight * 0.976459090909091, needleWidth * 0.998456057007126, needleHeight * 0.886363636363636));
        needle.getElements().add(new LineTo(needleWidth * 0.813182897862233, needleHeight *0.227272727272727));
        needle.getElements().add(new ClosePath());
        needle.getElements().add(new MoveTo(needleWidth * 0.552826603325416, needleHeight *0.854286363636364));
        needle.getElements().add(new CubicCurveTo(needleWidth * 0.536223277909739, needleHeight *0.852981818181818, needleWidth *0.518313539192399, needleHeight * 0.852272727272727, needleWidth * 0.499643705463183, needleHeight * 0.852272727272727));
        needle.getElements().add(new CubicCurveTo(needleWidth * 0.480237529691211, needleHeight *0.852272727272727, needleWidth *0.46166270783848, needleHeight * 0.853040909090909, needleWidth * 0.444513064133017, needleHeight * 0.854445454545455));
        needle.getElements().add(new CubicCurveTo(needleWidth * 0.37313539192399, needleHeight *0.858890909090909, needleWidth *0.321496437054632, needleHeight * 0.871736363636364, needleWidth * 0.321496437054632, needleHeight * 0.886868181818182));
        needle.getElements().add(new CubicCurveTo(needleWidth * 0.321496437054632, needleHeight *0.905681818181818, needleWidth *0.401330166270784, needleHeight * 0.920959090909091, needleWidth * 0.499643705463183, needleHeight * 0.920959090909091));
        needle.getElements().add(new LineTo(needleWidth * 0.500285035629454, needleHeight *0.920959090909091));
        needle.getElements().add(new CubicCurveTo(needleWidth * 0.598598574821853, needleHeight *0.920959090909091, needleWidth *0.678432304038005, needleHeight * 0.905681818181818, needleWidth * 0.678432304038005, needleHeight * 0.886868181818182));
        needle.getElements().add(new CubicCurveTo(needleWidth * 0.678432304038005, needleHeight *0.871554545454545, needleWidth *0.625534441805226, needleHeight * 0.858581818181818, needleWidth * 0.552826603325416, needleHeight * 0.854286363636364));
        needle.getElements().add(new ClosePath());
        needle.setCache(true);
        needle.setCacheHint(CacheHint.ROTATE);
    }

    private void createConicalGradient() {
        List<Stop> stops = tile.getGradientStops();
        Map<Double, Color> stopAngleMap = new HashMap<>(stops.size());
        for (Stop stop : stops) { stopAngleMap.put(stop.getOffset() * angleRange, stop.getColor()); }
        double offsetFactor = ((360 - angleRange) / 2 + 180);
        conicalGradient = new AngleConicalGradient(barBounds.getX() * barBounds.getWidth() * 0.5, barBounds.getY() * barBounds.getHeight() * 0.5, offsetFactor, stopAngleMap);
    }


    // ******************** Resizing ******************************************
    @Override protected void resizeDynamicText() {
        double maxWidth = unitText.isManaged() ? width - size * 0.275 : width - size * 0.1;
        double fontSize = size * 0.12;
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

        fontSize = size * 0.06;
        unitText.setFont(Fonts.latoRegular(fontSize));
        if (unitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(unitText, maxWidth, fontSize); }

        fontSize = size * 0.04;
        minValueText.setFont(Fonts.latoRegular(fontSize));
        minValueText.setText(String.format(locale, "%.0f", tile.getMinValue()));
        minValueText.setX(width * 0.5 - bar.getRadiusX() * 0.9);
        minValueText.setY(height * 0.5 + size * 0.225);

        maxValueText.setFont(Fonts.latoRegular(fontSize));
        maxValueText.setText(String.format(locale, "%.0f", tile.getMaxValue()));
        maxValueText.setX(width * 0.5 + bar.getRadiusX() * 0.9 - maxValueText.getLayoutBounds().getWidth());
        maxValueText.setY(height * 0.5 + size * 0.225);


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

        createConicalGradient();

        knob.setCenterX(centerX);
        knob.setCenterY(centerY);
        knob.setRadius(barRadius * 0.1637931);

        barBackground.setCenterX(centerX);
        barBackground.setCenterY(centerY);
        barBackground.setRadiusX(barRadius);
        barBackground.setRadiusY(barRadius);
        barBackground.setStrokeWidth(barWidth);
        barBackground.setStartAngle(angleRange * 0.5 + 90);
        barBackground.setLength(-angleRange);

        bar.setCenterX(centerX);
        bar.setCenterY(centerY);
        bar.setRadiusX(barRadius);
        bar.setRadiusY(barRadius);
        bar.setStrokeWidth(barWidth);
        bar.setStartAngle(angleRange * 0.5 + 90);
        bar.setLength(-angleStep * tile.getValue());

        barBounds.setX(bar.getCenterX() - bar.getRadiusX() - barWidth * 0.5);
        barBounds.setY(bar.getCenterY() - bar.getRadiusY() - barWidth * 0.5);
        barBounds.setWidth(bar.getRadiusX() * 2 + barWidth);
        barBounds.setHeight(bar.getRadiusX() * 2 + barWidth);

        drawNeedle();

        needle.relocate((width - needle.getLayoutBounds().getWidth()) * 0.5, centerY - size * 0.21112);
        needleRotate.setPivotX(needle.getLayoutBounds().getWidth() * 0.5);
        needleRotate.setPivotY(needle.getLayoutBounds().getHeight() * 0.88659091);

        resizeStaticText();
        resizeDynamicText();

        valueUnitFlow.setPrefWidth(width - doubleInset);
        valueUnitFlow.relocate(inset, height * 0.5 + size * 0.25);
    }

    @Override protected void redraw() {
        super.redraw();
        angleRange = tile.getAngleRange();
        angleStep  = tile.getAngleStep();
        titleText.setText(tile.getTitle());
        text.setText(tile.getText());
        unitText.setText(tile.getUnit());
        minValueText.setText(String.format(locale, tickLabelFormatString, tile.getMinValue()));
        maxValueText.setText(String.format(locale, tickLabelFormatString, tile.getMaxValue()));
        resizeStaticText();

        barBackgroundColor = tile.getBarBackgroundColor();
        Color needleColor;
        if (tile.isStrokeWithGradient()) {
            gradientLookup.setStops(tile.getGradientStops());
            needleColor = gradientLookup.getColorAt(tile.getValue() / tile.getRange());
            createConicalGradient();
            bar.setStroke(conicalGradient.getImagePattern(barBounds));
        } else {
            needleColor = tile.getNeedleColor();
            bar.setStroke(tile.getBarColor());
        }

        knob.setFill(tile.getBarBackgroundColor());
        barBackground.setStroke(tile.getBarBackgroundColor());
        needle.setFill(needleColor);
        titleText.setFill(tile.getTitleColor());
        text.setFill(tile.getTextColor());
        minValueText.setFill(tile.getTitleColor());
        maxValueText.setFill(tile.getTitleColor());
        valueText.setFill(tile.getValueColor());
        unitText.setFill(tile.getUnitColor());
    }
}
