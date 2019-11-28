/*
 * Copyright (c) 2018 by Gerrit Grunwald
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
import eu.hansolo.tilesfx.tools.GradientLookup;
import eu.hansolo.tilesfx.tools.Helper;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;


public class BarGaugeTileSkin extends TileSkin {
    private static final double         START_ANGLE  = 90;
    private static final double         ANGLE_RANGE  = 180;
    private              Text           titleText;
    private              Text           valueText;
    private              Text           upperUnitText;
    private              Line           fractionLine;
    private              Text           unitText;
    private              VBox           unitFlow;
    private              HBox           valueUnitFlow;
    private              Text           text;
    private              GradientLookup gradientLookup;
    private              boolean        colorGradientEnabled;
    private              int            noOfGradientStops;
    private              Text           minValueText;
    private              Text           maxValueText;
    private              Arc            barBackground;
    private              Arc            bar;
    private              Line           threshold;
    private              Text           thresholdText;
    private              Line           lowerThreshold;
    private              Text           lowerThresholdText;


    // ******************** Constructors **************************************
    public BarGaugeTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        if (tile.isAutoScale()) tile.calcAutoScale();

        gradientLookup       = new GradientLookup(tile.getGradientStops());
        noOfGradientStops    = tile.getGradientStops().size();
        sectionsVisible      = tile.getSectionsVisible();
        colorGradientEnabled = tile.isStrokeWithGradient();

        titleText = new Text(tile.getTitle());
        titleText.setFill(tile.getTitleColor());
        Helper.enableNode(titleText, !tile.getTitle().isEmpty());

        valueText = new Text(String.format(locale, formatString, tile.getValue()));
        valueText.setFill(tile.getValueColor());
        Helper.enableNode(valueText, tile.isValueVisible());

        upperUnitText = new Text("");
        upperUnitText.setFill(tile.getUnitColor());
        Helper.enableNode(upperUnitText, !tile.getUnit().isEmpty());

        fractionLine = new Line();

        unitText = new Text(tile.getUnit());
        unitText.setFill(tile.getUnitColor());
        Helper.enableNode(unitText, !tile.getUnit().isEmpty());

        unitFlow = new VBox(upperUnitText, unitText);
        unitFlow.setAlignment(Pos.CENTER_RIGHT);

        valueUnitFlow = new HBox(valueText, unitFlow);
        valueUnitFlow.setAlignment(Pos.CENTER);
        valueUnitFlow.setMouseTransparent(true);

        text = new Text(tile.getText());
        text.setTextOrigin(VPos.TOP);
        text.setFill(tile.getTextColor());

        barBackground = new Arc(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.5, PREFERRED_WIDTH * 0.4, PREFERRED_HEIGHT * 0.4, 0, ANGLE_RANGE);
        barBackground.setType(ArcType.OPEN);
        barBackground.setStroke(tile.getBarBackgroundColor());
        barBackground.setStrokeWidth(PREFERRED_WIDTH * 0.125);
        barBackground.setStrokeLineCap(StrokeLineCap.BUTT);
        barBackground.setFill(null);

        bar = new Arc(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.5, PREFERRED_WIDTH * 0.4, PREFERRED_HEIGHT * 0.4, 270, 0);
        bar.setType(ArcType.OPEN);
        bar.setStroke(tile.getBarColor());
        bar.setStrokeWidth(PREFERRED_WIDTH * 0.125);
        bar.setStrokeLineCap(StrokeLineCap.BUTT);
        bar.setFill(null);

        lowerThreshold = new Line();
        lowerThreshold.setStroke(tile.getLowerThresholdColor());
        lowerThreshold.setStrokeLineCap(StrokeLineCap.BUTT);
        Helper.enableNode(lowerThreshold, tile.isLowerThresholdVisible());

        lowerThresholdText = new Text(String.format(locale, formatString, tile.getLowerThreshold()));
        Helper.enableNode(lowerThresholdText, tile.isLowerThresholdVisible());
        
        threshold = new Line();
        threshold.setStroke(tile.getThresholdColor());
        threshold.setStrokeLineCap(StrokeLineCap.BUTT);
        Helper.enableNode(threshold, tile.isThresholdVisible());
        
        thresholdText = new Text(String.format(locale, formatString, tile.getThreshold()));
        Helper.enableNode(thresholdText, tile.isThresholdVisible());
        
        minValueText = new Text();
        maxValueText = new Text();

        getPane().getChildren().addAll(barBackground, bar, lowerThreshold, lowerThresholdText, threshold, thresholdText, minValueText, maxValueText, titleText, valueUnitFlow, fractionLine, text);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !tile.getTitle().isEmpty());
            Helper.enableNode(text, tile.isTextVisible());
            Helper.enableNode(valueText, tile.isValueVisible());
            Helper.enableNode(unitFlow, !tile.getUnit().isEmpty());
            Helper.enableNode(lowerThreshold, tile.isLowerThresholdVisible());
            Helper.enableNode(lowerThresholdText, tile.isLowerThresholdVisible());
            Helper.enableNode(threshold, tile.isThresholdVisible());
            Helper.enableNode(thresholdText, tile.isThresholdVisible());
            sectionsVisible = tile.getSectionsVisible();
            redraw();
        }
    }

    @Override protected void handleCurrentValue(final double VALUE) {
        if (tile.getCustomDecimalFormatEnabled()) {
            valueText.setText(decimalFormat.format(VALUE));
        } else {
            valueText.setText(String.format(locale, formatString, VALUE));
        }
        resizeDynamicText();
        setBar(VALUE);
    }

    private void setBar( final double VALUE ) {
        double barLength    = 0;
        double barStart     = 0;
        double step         = tile.getAngleStep();
        double clampedValue = Helper.clamp(minValue, maxValue, VALUE);

        if ( tile.isStartFromZero() ) {
            if ( ( VALUE > minValue || minValue < 0 ) && ( VALUE < maxValue || maxValue > 0 ) ) {
                if ( maxValue < 0 ) {
                    barStart = START_ANGLE - 270 - ANGLE_RANGE;
                    barLength = ( maxValue - clampedValue ) * step;
                } else if ( minValue > 0 ) {
                    barStart = START_ANGLE - 270;
                    barLength = ( minValue - clampedValue ) * step;
                } else {
                    barStart = START_ANGLE - 270 + minValue * step;
                    barLength = -clampedValue * step;
                }
            }
        } else {
            barStart = START_ANGLE - 270;
            barLength = ( minValue - clampedValue ) * step;
        }

        bar.setStartAngle(barStart);
        bar.setLength(barLength);

        setBarColor(VALUE);
    }

    private void setBarColor(final double VALUE) {
        if (!sectionsVisible && !colorGradientEnabled) {
            bar.setStroke(tile.getBarColor());
        } else if (colorGradientEnabled && noOfGradientStops > 1) {
            bar.setStroke(gradientLookup.getColorAt((VALUE - minValue) / range));
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
        super.dispose();
    }


    // ******************** Resizing ******************************************
    @Override protected void resizeDynamicText() {
        double maxWidth = unitText.isVisible() ? width - size * 0.275 : width - size * 0.1;
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
        fontSize = upperUnitText.getText().isEmpty() ? size * 0.12 : size * 0.10;
        upperUnitText.setFont(Fonts.latoRegular(fontSize));
        if (upperUnitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(upperUnitText, maxWidth, fontSize); }

        fontSize = upperUnitText.getText().isEmpty() ? size * 0.12 : size * 0.10;
        unitText.setFont(Fonts.latoRegular(fontSize));
        if (unitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(unitText, maxWidth, fontSize); }

        maxWidth = size * 0.15;
        fontSize = size * 0.07;
        minValueText.setFont(Fonts.latoRegular(fontSize));
        minValueText.setText(String.format(locale, tickLabelFormatString, minValue));
        if (minValueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(minValueText, maxWidth, fontSize); }
        minValueText.setX(width * 0.5 - size * 0.2);
        minValueText.setY(height * 0.85);

        maxValueText.setFont(Fonts.latoRegular(fontSize));
        maxValueText.setText(String.format(locale, tickLabelFormatString, maxValue));
        if (maxValueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(maxValueText, maxWidth, fontSize); }
        maxValueText.setX(width * 0.5 + size * 0.2 - maxValueText.getLayoutBounds().getWidth());
        maxValueText.setY(height * 0.85);

        maxWidth = width - size * 0.25;
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

        handleCurrentValue(tile.getValue());

        resizeStaticText();
        resizeDynamicText();

        double centerX = width * 0.5;
        double centerY = height * 0.85;

        valueUnitFlow.setPrefWidth(width - size * 0.1);
        valueUnitFlow.relocate(size * 0.05, contentBounds.getY());
        valueUnitFlow.setMaxHeight(valueText.getFont().getSize());

        fractionLine.setStartX(width - 0.17 * size);
        fractionLine.setStartY(tile.getTitle().isEmpty() ? size * 0.2 : size * 0.3);
        fractionLine.setEndX(width - 0.05 * size);
        fractionLine.setEndY(tile.getTitle().isEmpty() ? size * 0.2 : size * 0.3);
        fractionLine.setStroke(tile.getUnitColor());
        fractionLine.setStrokeWidth(size * 0.005);

        unitFlow.setTranslateY(-size * 0.005);

        barBackground.setCenterX(centerX);
        barBackground.setCenterY(centerY);
        barBackground.setRadiusX(size * 0.325);
        barBackground.setRadiusY(size * 0.325);
        barBackground.setStrokeWidth(size * 0.15);

        bar.setCenterX(centerX);
        bar.setCenterY(centerY);
        bar.setRadiusX(size * 0.325);
        bar.setRadiusY(size * 0.325);
        bar.setStrokeWidth(size * 0.15);

        lowerThreshold.setStrokeWidth(Helper.clamp(1.0, 2.0, 0.00675676 * size));
        double lowerThresholdInnerRadius = 0.25 * size;
        double lowerThresholdOuterRadius = 0.40 * size;
        double lowerThresholdAngle       = Helper.clamp(90.0, 270.0, (tile.getThreshold() - minValue) * angleStep + 90.0);
        lowerThreshold.setStartX(centerX + lowerThresholdInnerRadius * Math.sin(-Math.toRadians(lowerThresholdAngle)));
        lowerThreshold.setStartY(centerY + lowerThresholdInnerRadius * Math.cos(-Math.toRadians(lowerThresholdAngle)));
        lowerThreshold.setEndX(centerX + lowerThresholdOuterRadius * Math.sin(-Math.toRadians(lowerThresholdAngle)));
        lowerThreshold.setEndY(centerY + lowerThresholdOuterRadius * Math.cos(-Math.toRadians(lowerThresholdAngle)));

        double lowerThresholdTextRadius = 0.43 * size;
        lowerThresholdText.setText(String.format(locale, tickLabelFormatString, tile.getThreshold()));
        lowerThresholdText.setFont(Fonts.latoRegular(size * 0.047));
        lowerThresholdText.setRotate(lowerThresholdAngle + 180);
        lowerThresholdText.relocate(centerX - (lowerThresholdText.getLayoutBounds().getWidth() * 0.5) + lowerThresholdTextRadius * Math.sin(-Math.toRadians(lowerThresholdAngle)),
                               centerY - (lowerThresholdText.getLayoutBounds().getWidth() * 0.5) + lowerThresholdTextRadius * Math.cos(-Math.toRadians(lowerThresholdAngle)));
        
        threshold.setStrokeWidth(Helper.clamp(1.0, 2.0, 0.00675676 * size));
        double thresholdInnerRadius = 0.25 * size;
        double thresholdOuterRadius = 0.40 * size;
        double thresholdAngle       = Helper.clamp(90.0, 270.0, (tile.getThreshold() - minValue) * angleStep + 90.0);
        threshold.setStartX(centerX + thresholdInnerRadius * Math.sin(-Math.toRadians(thresholdAngle)));
        threshold.setStartY(centerY + thresholdInnerRadius * Math.cos(-Math.toRadians(thresholdAngle)));
        threshold.setEndX(centerX + thresholdOuterRadius * Math.sin(-Math.toRadians(thresholdAngle)));
        threshold.setEndY(centerY + thresholdOuterRadius * Math.cos(-Math.toRadians(thresholdAngle)));

        double thresholdTextRadius = 0.43 * size;
        thresholdText.setText(String.format(locale, tickLabelFormatString, tile.getThreshold()));
        thresholdText.setFont(Fonts.latoRegular(size * 0.047));
        thresholdText.setRotate(thresholdAngle + 180);
        thresholdText.relocate(centerX - (thresholdText.getLayoutBounds().getWidth() * 0.5) + thresholdTextRadius * Math.sin(-Math.toRadians(thresholdAngle)),
                               centerY - (thresholdText.getLayoutBounds().getWidth() * 0.5) + thresholdTextRadius * Math.cos(-Math.toRadians(thresholdAngle)));
    }

    @Override protected void redraw() {
        super.redraw();

        gradientLookup.setStops(tile.getGradientStops());
        colorGradientEnabled = tile.isStrokeWithGradient();

        barBackground.setStroke(tile.getBarBackgroundColor());
        setBar(tile.getCurrentValue());
        lowerThreshold.setStroke(tile.getLowerThresholdColor());
        threshold.setStroke(tile.getThresholdColor());

        titleText.setText(tile.getTitle());
        text.setText(tile.getText());
        if (tile.getUnit().contains("/")) {
            String[] units = tile.getUnit().split("/");
            upperUnitText.setText(units[0]);
            unitText.setText(units[1]);
            Helper.enableNode(fractionLine, true);
        } else {
            upperUnitText.setText(" ");
            unitText.setText(tile.getUnit());
            Helper.enableNode(fractionLine, false);
        }
        if (!tile.getDescription().isEmpty()) { text.setText(tile.getDescription()); }

        text.setText(tile.getText());

        resizeStaticText();

        titleText.setFill(tile.getTitleColor());
        valueText.setFill(tile.getValueColor());
        upperUnitText.setFill(tile.getUnitColor());
        fractionLine.setStroke(tile.getUnitColor());
        unitText.setFill(tile.getUnitColor());
        text.setFill(tile.getTextColor());

        minValueText.setFill(tile.getTextColor());
        maxValueText.setFill(tile.getTextColor());

        lowerThresholdText.setFill(tile.getValueColor());
        thresholdText.setFill(tile.getValueColor());
    }
}
