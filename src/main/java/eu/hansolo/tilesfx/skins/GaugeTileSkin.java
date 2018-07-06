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
import javafx.geometry.VPos;
import javafx.scene.CacheHint;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.Line;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.scene.transform.Rotate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by hansolo on 19.12.16.
 */
public class GaugeTileSkin extends TileSkin {
    private double            oldValue;
    private Arc               barBackground;
    private Arc               thresholdBar;
    private Rectangle         needleRect;
    private Path              needle;
    private Rotate            needleRotate;
    private Rotate            needleRectRotate;
    private Text              titleText;
    private Text              valueText;
    private Text              unitText;
    private TextFlow          valueUnitFlow;
    private Text              minValueText;
    private Text              maxValueText;
    private Rectangle         thresholdRect;
    private Text              thresholdText;
    private Pane              sectionPane;
    private Path              alertIcon;
    private Tooltip           alertTooltip;
    private Map<Section, Arc> sectionMap;
    private Color             barColor;
    private Color             thresholdColor;


    // ******************** Constructors **************************************
    public GaugeTileSkin(final Tile TILE) {
        super(TILE);
        handleCurrentValue(tile.getValue());
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        if (tile.isAutoScale()) tile.calcAutoScale();
        oldValue          = tile.getValue();
        sectionMap        = new HashMap<>(sections.size());
        for(Section section : sections) { sectionMap.put(section, new Arc()); }

        barColor       = tile.getBarColor();
        thresholdColor = tile.getThresholdColor();

        barBackground = new Arc(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.696, PREFERRED_WIDTH * 0.275, PREFERRED_WIDTH * 0.275, angleRange * 0.5 + 90, -angleRange);
        barBackground.setType(ArcType.OPEN);
        barBackground.setStroke(barColor);
        barBackground.setStrokeWidth(PREFERRED_WIDTH * 0.02819549 * 2);
        barBackground.setStrokeLineCap(StrokeLineCap.BUTT);
        barBackground.setFill(null);

        thresholdBar = new Arc(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.696, PREFERRED_WIDTH * 0.275, PREFERRED_WIDTH * 0.275, -angleRange * 0.5 + 90, 0);
        thresholdBar.setType(ArcType.OPEN);
        thresholdBar.setStroke(tile.getThresholdColor());
        thresholdBar.setStrokeWidth(PREFERRED_WIDTH * 0.02819549 * 2);
        thresholdBar.setStrokeLineCap(StrokeLineCap.BUTT);
        thresholdBar.setFill(null);
        Helper.enableNode(thresholdBar, !tile.getSectionsVisible());

        sectionPane = new Pane();
        Helper.enableNode(sectionPane, tile.getSectionsVisible());

        if (sectionsVisible) { drawSections(); }

        alertIcon = new Path();
        alertIcon.setFillRule(FillRule.EVEN_ODD);
        alertIcon.setFill(Color.YELLOW);
        alertIcon.setStroke(null);
        Helper.enableNode(alertIcon, tile.isAlert());
        alertTooltip = new Tooltip(tile.getAlertMessage());
        Tooltip.install(alertIcon, alertTooltip);

        needleRotate     = new Rotate((tile.getValue() - oldValue - minValue) * angleStep);
        needleRectRotate = new Rotate((tile.getValue() - oldValue - minValue) * angleStep);

        needleRect = new Rectangle();
        needleRect.setFill(tile.getBackgroundColor());
        needleRect.getTransforms().setAll(needleRectRotate);

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
        valueText.setTextOrigin(VPos.BASELINE);
        Helper.enableNode(valueText, tile.isValueVisible() && !tile.isAlert());

        unitText = new Text(tile.getUnit());
        unitText.setFill(tile.getUnitColor());
        unitText.setTextOrigin(VPos.BASELINE);
        Helper.enableNode(unitText, tile.isValueVisible() && !tile.isAlert());

        valueUnitFlow = new TextFlow(valueText, unitText);
        valueUnitFlow.setTextAlignment(TextAlignment.CENTER);

        minValueText = new Text(String.format(locale, "%." + tile.getTickLabelDecimals() + "f", tile.getMinValue()));
        minValueText.setFill(tile.getTitleColor());

        maxValueText = new Text(String.format(locale, "%." + tile.getTickLabelDecimals() + "f", tile.getMaxValue()));
        maxValueText.setFill(tile.getTitleColor());

        thresholdRect = new Rectangle();
        thresholdRect.setFill(sectionsVisible ? Color.TRANSPARENT : tile.getValue() > tile.getThreshold() ? tile.getThresholdColor() : Tile.GRAY);
        Helper.enableNode(thresholdRect, tile.isThresholdVisible());

        thresholdText = new Text(String.format(locale, "%." + tile.getTickLabelDecimals() + "f", tile.getThreshold()));
        thresholdText.setFill(sectionsVisible ? Color.TRANSPARENT : Tile.GRAY);
        Helper.enableNode(thresholdText, tile.isThresholdVisible());

        getPane().getChildren().addAll(barBackground, thresholdBar, sectionPane, alertIcon, needleRect, needle, titleText, valueUnitFlow, minValueText, maxValueText, thresholdRect, thresholdText);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !tile.getTitle().isEmpty());
            Helper.enableNode(valueText, tile.isValueVisible());
            Helper.enableNode(sectionPane, tile.getSectionsVisible());
            Helper.enableNode(thresholdRect, tile.isThresholdVisible());
            Helper.enableNode(thresholdText, tile.isThresholdVisible());
            Helper.enableNode(unitText, !tile.getUnit().isEmpty());
            sectionsVisible = tile.getSectionsVisible();
        } else if ("SECTION".equals(EVENT_TYPE)) {
            sections = tile.getSections();
            sectionMap.clear();
            for(Section section : sections) { sectionMap.put(section, new Arc()); }
        } else if ("ALERT".equals(EVENT_TYPE)) {
            Helper.enableNode(valueText, tile.isValueVisible() && !tile.isAlert());
            Helper.enableNode(unitText, tile.isValueVisible() && !tile.isAlert());
            Helper.enableNode(alertIcon, tile.isAlert());
            alertTooltip.setText(tile.getAlertMessage());
        }
    }

    @Override protected void handleCurrentValue(final double VALUE) {
        double needleStartAngle = angleRange * 0.5;
        double targetAngle = (VALUE - minValue) * angleStep - needleStartAngle;
        targetAngle = Helper.clamp(-needleStartAngle, -needleStartAngle + angleRange, targetAngle);
        needleRotate.setAngle(targetAngle);
        needleRectRotate.setAngle(targetAngle);
        valueText.setText(String.format(locale, formatString, VALUE));
        thresholdRect.setFill(sectionsVisible ? Color.TRANSPARENT : tile.getValue() > tile.getThreshold() ? tile.getThresholdColor() : Tile.GRAY);
        resizeDynamicText();
        highlightSections(VALUE);
    }

    private void highlightSections(final double VALUE) {
        if (!sectionsVisible || sections.isEmpty()) return;
        if (highlightSections) {
            sections.forEach(section -> sectionMap.get(section).setVisible(section.contains(VALUE)));
        } else {
            sections.forEach(section -> sectionMap.get(section).setOpacity(section.contains(VALUE) ? 1.0 : 0.25));
        }
    }

    private void drawSections() {
        sectionPane.getChildren().clear();
        if (!sectionsVisible || sections.isEmpty()) return;

        double     centerX      = width * 0.5;
        double     centerY      = height * 0.5;
        double     innerRadius  = size * 0.2775;
        double     outerRadius  = size * 0.3225;
        int        noOfSections = sections.size();
        List<Line> sectionLines = new ArrayList<>(noOfSections);
        for (int i = 0 ; i < noOfSections - 1 ; i++) {
            Section section = sections.get(i);
            double  angle   = Helper.clamp(90.0, 270.0, (section.getStop() - minValue) * angleStep + 90.0);
            Line    line    = new Line(centerX + innerRadius * Math.sin(-Math.toRadians(angle)), centerY + innerRadius * Math.cos(-Math.toRadians(angle)),
                                       centerX + outerRadius * Math.sin(-Math.toRadians(angle)), centerY + outerRadius * Math.cos(-Math.toRadians(angle)));
            line.setStroke(tile.getBackgroundColor());
            sectionLines.add(line);
        }
        sectionPane.getChildren().addAll(sectionLines);

        double barRadius = size * 0.3;
        double barWidth  = size * 0.045;
        double maxValue  = tile.getMaxValue();
        for (Section section : sections) {
            double startAngle = (section.getStart() - minValue) * angleStep - angleRange;
            double length;
            if (section.getStop() > maxValue) {
                length = (maxValue - section.getStart()) * angleStep;
            } else if (Double.compare(section.getStart(), minValue) < 0) {
                length = (section.getStop() - minValue) * angleStep;
            } else {
                length = (section.getStop() - section.getStart()) * angleStep;
            }
            Arc sectionArc = new Arc(centerX, centerY + size * 0.2825, barRadius, barRadius, -startAngle, -length);

            sectionArc.setType(ArcType.OPEN);
            sectionArc.setStroke(section.getColor());
            sectionArc.setStrokeWidth(barWidth);
            sectionArc.setStrokeLineCap(StrokeLineCap.BUTT);
            sectionArc.setFill(null);
            sectionArc.setVisible(!highlightSections);
            sectionArc.setOpacity(highlightSections ? 1.0 : 0.25);
            Tooltip sectionTooltip = new Tooltip(section.getText());
            sectionTooltip.setTextAlignment(TextAlignment.CENTER);
            Tooltip.install(sectionArc, sectionTooltip);
            sectionMap.put(section, sectionArc);
        }
        sectionPane.getChildren().addAll(sectionMap.values());
    }

    private void drawNeedle() {
        double needleWidth  = size * 0.05;
        double needleHeight = size * 0.3325;
        needle.setCache(false);

        needle.getElements().clear();
        needle.getElements().add(new MoveTo(0.25 * needleWidth, 0.924812030075188 * needleHeight));
        needle.getElements().add(new CubicCurveTo(0.25 * needleWidth, 0.9022556390977443 * needleHeight,
                                                  0.35 * needleWidth, 0.8872180451127819 * needleHeight,
                                                  0.5 * needleWidth, 0.8872180451127819 * needleHeight));
        needle.getElements().add(new CubicCurveTo(0.65 * needleWidth, 0.8872180451127819 * needleHeight,
                                                  0.75 * needleWidth, 0.9022556390977443 * needleHeight,
                                                  0.75 * needleWidth, 0.924812030075188 * needleHeight));
        needle.getElements().add(new CubicCurveTo(0.75 * needleWidth, 0.9473684210526315 * needleHeight,
                                                  0.65 * needleWidth, 0.9624060150375939 * needleHeight,
                                                  0.5 * needleWidth, 0.9624060150375939 * needleHeight));
        needle.getElements().add(new CubicCurveTo(0.35 * needleWidth, 0.9624060150375939 * needleHeight,
                                                  0.25 * needleWidth, 0.9473684210526315 * needleHeight,
                                                  0.25 * needleWidth, 0.924812030075188 * needleHeight));
        needle.getElements().add(new ClosePath());
        needle.getElements().add(new MoveTo(0.0, 0.924812030075188 * needleHeight));
        needle.getElements().add(new CubicCurveTo(0.0, 0.9699248120300752 * needleHeight,
                                                  0.2 * needleWidth, needleHeight,
                                                  0.5 * needleWidth, needleHeight));
        needle.getElements().add(new CubicCurveTo(0.8 * needleWidth, needleHeight,
                                                  needleWidth, 0.9699248120300752 * needleHeight,
                                                  needleWidth, 0.924812030075188 * needleHeight));
        needle.getElements().add(new CubicCurveTo(needleWidth, 0.8947368421052632 * needleHeight,
                                                  0.85 * needleWidth, 0.8646616541353384 * needleHeight,
                                                  0.65 * needleWidth, 0.849624060150376 * needleHeight));
        needle.getElements().add(new CubicCurveTo(0.65 * needleWidth, 0.849624060150376 * needleHeight,
                                                  0.65 * needleWidth, 0.022556390977443608 * needleHeight,
                                                  0.65 * needleWidth, 0.022556390977443608 * needleHeight));
        needle.getElements().add(new CubicCurveTo(0.65 * needleWidth, 0.007518796992481203 * needleHeight,
                                                  0.6 * needleWidth, 0.0,
                                                  0.5 * needleWidth, 0.0));
        needle.getElements().add(new CubicCurveTo(0.4 * needleWidth, 0.0,
                                                  0.35 * needleWidth, 0.007518796992481203 * needleHeight,
                                                  0.35 * needleWidth, 0.022556390977443608 * needleHeight));
        needle.getElements().add(new CubicCurveTo(0.35 * needleWidth, 0.022556390977443608 * needleHeight,
                                                  0.35 * needleWidth, 0.849624060150376 * needleHeight,
                                                  0.35 * needleWidth, 0.849624060150376 * needleHeight));
        needle.getElements().add(new CubicCurveTo(0.15 * needleWidth, 0.8646616541353384 * needleHeight,
                                                  0.0, 0.8947368421052632 * needleHeight,
                                                  0.0, 0.924812030075188 * needleHeight));
        needle.getElements().add(new ClosePath());
        needle.setCache(true);
        needle.setCacheHint(CacheHint.ROTATE);
    }

    private void drawAlertIcon() {
        alertIcon.setCache(false);
        double iconWidth  = size * 0.155;
        double iconHeight = size * 0.135;
        alertIcon.getElements().clear();
        alertIcon.getElements().add(new MoveTo(0.4411764705882353 * iconWidth, 0.3380952380952381 * iconHeight));
        alertIcon.getElements().add(new CubicCurveTo(0.4411764705882353 * iconWidth, 0.3 * iconHeight,
                                                     0.4684873949579832 * iconWidth, 0.2714285714285714 * iconHeight,
                                                     0.5 * iconWidth, 0.2714285714285714 * iconHeight));
        alertIcon.getElements().add(new CubicCurveTo(0.5315126050420168 * iconWidth, 0.2714285714285714 * iconHeight,
                                                     0.5588235294117647 * iconWidth, 0.3 * iconHeight,
                                                     0.5588235294117647 * iconWidth, 0.3380952380952381 * iconHeight));
        alertIcon.getElements().add(new CubicCurveTo(0.5588235294117647 * iconWidth, 0.3380952380952381 * iconHeight,
                                                     0.5588235294117647 * iconWidth, 0.6 * iconHeight,
                                                     0.5588235294117647 * iconWidth, 0.6 * iconHeight));
        alertIcon.getElements().add(new CubicCurveTo(0.5588235294117647 * iconWidth, 0.6357142857142857 * iconHeight,
                                                     0.5315126050420168 * iconWidth, 0.6666666666666666 * iconHeight,
                                                     0.5 * iconWidth, 0.6666666666666666 * iconHeight));
        alertIcon.getElements().add(new CubicCurveTo(0.4684873949579832 * iconWidth, 0.6666666666666666 * iconHeight,
                                                     0.4411764705882353 * iconWidth, 0.6357142857142857 * iconHeight,
                                                     0.4411764705882353 * iconWidth, 0.6 * iconHeight));
        alertIcon.getElements().add(new CubicCurveTo(0.4411764705882353 * iconWidth, 0.6 * iconHeight,
                                                     0.4411764705882353 * iconWidth, 0.3380952380952381 * iconHeight,
                                                     0.4411764705882353 * iconWidth, 0.3380952380952381 * iconHeight));
        alertIcon.getElements().add(new ClosePath());
        alertIcon.getElements().add(new MoveTo(0.4411764705882353 * iconWidth, 0.8 * iconHeight));
        alertIcon.getElements().add(new CubicCurveTo(0.4411764705882353 * iconWidth, 0.7642857142857142 * iconHeight,
                                                     0.4684873949579832 * iconWidth, 0.7333333333333333 * iconHeight,
                                                     0.5 * iconWidth, 0.7333333333333333 * iconHeight));
        alertIcon.getElements().add(new CubicCurveTo(0.5315126050420168 * iconWidth, 0.7333333333333333 * iconHeight,
                                                     0.5588235294117647 * iconWidth, 0.7642857142857142 * iconHeight,
                                                     0.5588235294117647 * iconWidth, 0.8 * iconHeight));
        alertIcon.getElements().add(new CubicCurveTo(0.5588235294117647 * iconWidth, 0.8380952380952381 * iconHeight,
                                                     0.5315126050420168 * iconWidth, 0.8666666666666667 * iconHeight,
                                                     0.5 * iconWidth, 0.8666666666666667 * iconHeight));
        alertIcon.getElements().add(new CubicCurveTo(0.4684873949579832 * iconWidth, 0.8666666666666667 * iconHeight,
                                                     0.4411764705882353 * iconWidth, 0.8380952380952381 * iconHeight,
                                                     0.4411764705882353 * iconWidth, 0.8 * iconHeight));
        alertIcon.getElements().add(new ClosePath());
        alertIcon.getElements().add(new MoveTo(0.5504201680672269 * iconWidth, 0.04285714285714286 * iconHeight));
        alertIcon.getElements().add(new CubicCurveTo(0.523109243697479 * iconWidth, -0.011904761904761904 * iconHeight,
                                                     0.47689075630252103 * iconWidth, -0.011904761904761904 * iconHeight,
                                                     0.4495798319327731 * iconWidth, 0.04285714285714286 * iconHeight));
        alertIcon.getElements().add(new CubicCurveTo(0.4495798319327731 * iconWidth, 0.04285714285714286 * iconHeight,
                                                     0.012605042016806723 * iconWidth, 0.9 * iconHeight,
                                                     0.012605042016806723 * iconWidth, 0.9 * iconHeight));
        alertIcon.getElements().add(new CubicCurveTo(-0.014705882352941176 * iconWidth, 0.9547619047619048 * iconHeight,
                                                     0.0063025210084033615 * iconWidth, iconHeight,
                                                     0.06302521008403361 * iconWidth, iconHeight));
        alertIcon.getElements().add(new CubicCurveTo(0.06302521008403361 * iconWidth, iconHeight,
                                                     0.9369747899159664 * iconWidth, iconHeight,
                                                     0.9369747899159664 * iconWidth, iconHeight));
        alertIcon.getElements().add(new CubicCurveTo(0.9936974789915967 * iconWidth, iconHeight,
                                                     1.0147058823529411 * iconWidth, 0.9547619047619048 * iconHeight,
                                                     0.9873949579831933 * iconWidth, 0.9 * iconHeight));
        alertIcon.getElements().add(new CubicCurveTo(0.9873949579831933 * iconWidth, 0.9 * iconHeight,
                                                     0.5504201680672269 * iconWidth, 0.04285714285714286 * iconHeight,
                                                     0.5504201680672269 * iconWidth, 0.04285714285714286 * iconHeight));
        alertIcon.getElements().add(new ClosePath());
        alertIcon.setCache(true);
        alertIcon.setCacheHint(CacheHint.SPEED);
    }


    // ******************** Resizing ******************************************
    @Override protected void resizeDynamicText() {
        double maxWidth = unitText.isManaged() ? width - size * 0.275 : width - size * 0.1;
        double fontSize = size * 0.24;
        valueText.setFont(Fonts.latoRegular(fontSize));
        if (valueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(valueText, maxWidth, fontSize); }

        fontSize = size * 0.1;
        unitText.setFont(Fonts.latoRegular(fontSize));
        if (unitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(unitText, maxWidth, fontSize); }

        thresholdText.setFill(sectionsVisible ? Color.TRANSPARENT : tile.getBackgroundColor());
        if (!sectionsVisible) {
            fontSize = size * 0.08;
            thresholdText.setFont(Fonts.latoRegular(fontSize));
            thresholdText.setTextOrigin(VPos.CENTER);
            if (thresholdText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(thresholdText, maxWidth, fontSize); }
            thresholdText.setX((width - thresholdText.getLayoutBounds().getWidth()) * 0.5);
            thresholdText.setY(thresholdRect.getLayoutBounds().getMinY() + thresholdRect.getHeight() * 0.5);
        }
    }
    @Override protected void resizeStaticText() {
        double maxWidth = width - size * 0.1;
        double fontSize = size * textSize.factor;
        double textRadius;
        double sinValue;
        double cosValue;
        double textX;
        double textY;

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

        maxWidth = size * 0.15;
        fontSize = size * 0.07;
        maxValueText.setFont(Fonts.latoRegular(fontSize));
        if (maxValueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(maxValueText, maxWidth, fontSize); }
        textRadius = size * 0.3;
        sinValue   = Math.sin(Math.toRadians(90 + (180 - angleRange) * 0.5));
        cosValue   = Math.cos(Math.toRadians(90 + (180 - angleRange) * 0.5));
        textX      = width * 0.5 + textRadius * sinValue;
        textY      = barBackground.getLayoutBounds().getMaxY() + size * 0.05 + textRadius * cosValue;
        maxValueText.setTranslateX(-maxValueText.getLayoutBounds().getWidth() * 0.5);
        maxValueText.setTranslateY(-maxValueText.getLayoutBounds().getHeight() * 0.5);
        maxValueText.relocate(textX, textY);

        minValueText.setFont(Fonts.latoRegular(maxValueText.getFont().getSize()));
        if (minValueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(minValueText, maxWidth, fontSize); }
        sinValue  = Math.sin(Math.toRadians(-90 - (180 - angleRange) * 0.5));
        cosValue  = Math.cos(Math.toRadians(-90 - (180 - angleRange) * 0.5));
        textX     = width * 0.5 + textRadius * sinValue;
        //textY     = barBackground.getLayoutBounds().getMaxY() + size * 0.05 + textRadius * cosValue;
        minValueText.setTranslateX(-minValueText.getLayoutBounds().getWidth() * 0.5);
        minValueText.setTranslateY(-minValueText.getLayoutBounds().getHeight() * 0.5);
        minValueText.relocate(textX, textY);

        if (!sectionsVisible) {
            maxWidth = size * 0.3;
            fontSize = size * 0.08;
            thresholdText.setFont(Fonts.latoRegular(fontSize));
            thresholdText.setTextOrigin(VPos.CENTER);
            if (thresholdText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(thresholdText, maxWidth, fontSize); }
            thresholdText.setX((width - thresholdText.getLayoutBounds().getWidth()) * 0.5);
            thresholdText.setY(thresholdRect.getLayoutBounds().getMinY() + thresholdRect.getHeight() * 0.5);
        }
    }

    @Override protected void resize() {
        super.resize();
        double centerX   = width * 0.5;
        double centerY   = height * 0.5;
        double barRadius = size * 0.3;
        double barWidth  = size * 0.045;

        sectionPane.setMaxSize(size, size);

        barBackground.setCenterX(centerX);
        barBackground.setCenterY(centerY + size * 0.2825);
        barBackground.setRadiusX(barRadius);
        barBackground.setRadiusY(barRadius);
        barBackground.setStrokeWidth(barWidth);
        barBackground.setStartAngle(angleRange * 0.5 + 90);
        barBackground.setLength(-angleRange);

        thresholdBar.setCenterX(centerX);
        thresholdBar.setCenterY(centerY + size * 0.2825);
        thresholdBar.setRadiusX(barRadius);
        thresholdBar.setRadiusY(barRadius);
        thresholdBar.setStrokeWidth(barWidth);
        thresholdBar.setStartAngle(90 - angleRange * 0.5);
        thresholdBar.setLength((tile.getMaxValue() - tile.getThreshold()) * angleStep);

        if (sectionsVisible) { drawSections(); }

        drawAlertIcon();
        alertIcon.relocate((size - alertIcon.getLayoutBounds().getWidth()) * 0.5, size * 0.244);

        needleRect.setWidth(size * 0.035);
        needleRect.setHeight(size * 0.05);
        needleRect.relocate((width - needleRect.getWidth()) * 0.5, centerY - size * 0.0425);
        needleRectRotate.setPivotX(needleRect.getLayoutBounds().getWidth() * 0.5);
        needleRectRotate.setPivotY(size * 0.325);

        drawNeedle();

        needle.relocate((width - needle.getLayoutBounds().getWidth()) * 0.5, centerY - size * 0.025);
        needleRotate.setPivotX(needle.getLayoutBounds().getWidth() * 0.5);
        needleRotate.setPivotY(needle.getLayoutBounds().getHeight() - needle.getLayoutBounds().getWidth() * 0.5);

        resizeStaticText();
        resizeDynamicText();

        valueUnitFlow.setPrefWidth(width * 0.9);
        valueUnitFlow.relocate(size * 0.05, contentBounds.getY());

        thresholdRect.setWidth(thresholdText.getLayoutBounds().getWidth() + size * 0.05);
        thresholdRect.setHeight(thresholdText.getLayoutBounds().getHeight());
        thresholdRect.setX((width - thresholdRect.getWidth()) * 0.5);
        thresholdRect.setY(centerY + size * 0.35);
        thresholdRect.setArcWidth(size * 0.025);
        thresholdRect.setArcHeight(size * 0.025);
    }

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(tile.getTitle());
        unitText.setText(tile.getUnit());
        minValueText.setText(String.format(locale, tickLabelFormatString, tile.getMinValue()));
        maxValueText.setText(String.format(locale, tickLabelFormatString, tile.getMaxValue()));
        thresholdText.setText(String.format(locale, tickLabelFormatString, tile.getThreshold()));
        resizeStaticText();

        barColor       = tile.getBarColor();
        thresholdColor = tile.getThresholdColor();

        barBackground.setStroke(barColor);
        thresholdBar.setStroke(tile.getThresholdColor());
        needleRect.setFill(tile.getBackgroundColor());
        needle.setFill(tile.getNeedleColor());
        titleText.setFill(tile.getTitleColor());
        minValueText.setFill(tile.getTitleColor());
        maxValueText.setFill(tile.getTitleColor());
        thresholdRect.setFill(sectionsVisible ? Color.TRANSPARENT : tile.getValue() > tile.getThreshold() ? tile.getThresholdColor() : Tile.GRAY);
        thresholdText.setFill(sectionsVisible ? Color.TRANSPARENT : tile.getBackgroundColor());
        valueText.setFill(tile.getValueColor());

        drawSections();
        highlightSections(tile.getValue());
    }
}
