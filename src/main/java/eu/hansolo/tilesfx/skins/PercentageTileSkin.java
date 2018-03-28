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
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

import static eu.hansolo.tilesfx.tools.Helper.clamp;


/**
 * Created by hansolo on 19.12.16.
 */
public class PercentageTileSkin extends TileSkin {
    private Region    barBackground;
    private Rectangle barClip;
    private Rectangle bar;
    private Text      titleText;
    private Text      valueText;
    private Text      unitText;
    private TextFlow  valueUnitFlow;
    private Label     description;
    private Text      percentageText;
    private Text      percentageUnitText;
    private Rectangle maxValueRect;
    private Text      maxValueText;
    private Text      maxValueUnitText;
    private Color     barColor;


    // ******************** Constructors **************************************
    public PercentageTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        barColor = tile.getBarColor();

        barBackground = new Region();
        barBackground.setBackground(new Background(new BackgroundFill(tile.getBarBackgroundColor(), new CornerRadii(0.0, 0.0, 0.025, 0.025, true), Insets.EMPTY)));

        barClip = new Rectangle();

        bar = new Rectangle();
        bar.setFill(tile.getBarColor());
        bar.setStroke(null);
        bar.setClip(barClip);

        titleText = new Text();
        titleText.setFill(tile.getTitleColor());
        Helper.enableNode(titleText, !tile.getTitle().isEmpty());

        valueText = new Text(String.format(locale, formatString, ((tile.getValue() - minValue) / range * 100)));
        valueText.setFill(tile.getValueColor());
        Helper.enableNode(valueText, tile.isValueVisible());

        unitText = new Text(tile.getUnit());
        unitText.setFill(tile.getUnitColor());
        Helper.enableNode(unitText, !tile.getUnit().isEmpty());

        valueUnitFlow = new TextFlow(valueText, unitText);
        valueUnitFlow.setTextAlignment(TextAlignment.RIGHT);

        description = new Label(tile.getDescription());
        description.setAlignment(tile.getDescriptionAlignment());
        description.setWrapText(true);
        description.setTextFill(tile.getTextColor());
        Helper.enableNode(description, !tile.getDescription().isEmpty());

        percentageText = new Text();
        percentageText.setFill(tile.getBarColor());

        percentageUnitText = new Text("%");
        percentageUnitText.setFill(tile.getBarColor());

        maxValueRect = new Rectangle();
        maxValueRect.setFill(tile.getThresholdColor());

        maxValueText = new Text();
        maxValueText.setFill(tile.getBackgroundColor());

        maxValueUnitText = new Text(tile.getUnit());
        maxValueUnitText.setFill(tile.getBackgroundColor());

        getPane().getChildren().addAll(barBackground, bar, titleText, valueUnitFlow, description, percentageText, percentageUnitText, maxValueRect, maxValueText, maxValueUnitText);
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
            Helper.enableNode(unitText, !tile.getUnit().isEmpty());
            Helper.enableNode(description, !tile.getDescription().isEmpty());
        }
    }

    @Override protected void handleCurrentValue(final double VALUE) {
        double targetValue = (clamp(minValue, maxValue, VALUE) - minValue) * stepSize;
        bar.setWidth(targetValue);
        valueText.setText(String.format(locale, formatString, VALUE));
        percentageText.setText(String.format(locale, formatString, ((VALUE - minValue) / range * 100)));
        maxValueRect.setFill(Double.compare(VALUE, maxValue) >= 0 ? barColor : tile.getThresholdColor());
        resizeDynamicText();
        if (sectionsVisible && !sections.isEmpty()) { setBarColor(VALUE); }
    }

    private void setBarColor(final double VALUE) {
        Color color = barColor;
        for(Section section : sections) {
            if (section.contains(VALUE)) {
                color = section.getColor();
                break;
            }
        }
        bar.setFill(color);
        percentageText.setFill(color);
        percentageUnitText.setFill(color);
    }


    // ******************** Resizing ******************************************
    @Override protected void resizeDynamicText() {
        double maxWidth = unitText.isVisible() ? size * 0.725 : size * 0.9;
        double fontSize = size * 0.24;
        valueText.setFont(Fonts.latoRegular(fontSize));
        if (valueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(valueText, maxWidth, fontSize); }

        percentageUnitText.relocate(percentageText.getLayoutBounds().getMaxX() + size * 0.075, height - size * 0.25);
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

        maxWidth = width - size * 0.275;
        fontSize = size * 0.12;
        unitText.setFont(Fonts.latoRegular(fontSize));
        if (unitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(unitText, maxWidth, fontSize); }

        maxWidth = width - size * 0.55;
        fontSize = size * 0.18;
        percentageText.setFont(Fonts.latoRegular(fontSize));
        if (percentageText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(percentageText, maxWidth, fontSize); }
        percentageText.relocate(size * 0.05, height - size * 0.305);

        maxWidth = width - size * 0.9;
        fontSize = size * 0.12;
        percentageUnitText.setFont(Fonts.latoRegular(fontSize));
        if (percentageUnitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(percentageUnitText, maxWidth, fontSize); }
        percentageUnitText.relocate(percentageText.getLayoutBounds().getMaxX() + size * 0.075, height - size * 0.25);

        maxWidth = width - size * 0.8;
        fontSize = size * 0.05;
        maxValueUnitText.setFont(Fonts.latoRegular(fontSize));
        if (maxValueUnitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(maxValueUnitText, maxWidth, fontSize); }
        maxValueUnitText.setX((width - size * 0.075) - maxValueUnitText.getLayoutBounds().getWidth());
        maxValueUnitText.setY(height - size * 0.145);

        maxWidth = width - size * 0.55;
        fontSize = size * 0.08;
        maxValueText.setFont(Fonts.latoRegular(fontSize));
        if (maxValueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(maxValueText, maxWidth, fontSize); }
        if (unitText.isVisible()) {
            maxValueText.setX((width - size * 0.075) - (size * 0.01 + maxValueText.getLayoutBounds().getWidth() + maxValueUnitText.getLayoutBounds().getWidth()));
        } else {
            maxValueText.setX((width - size * 0.075) - maxValueText.getLayoutBounds().getWidth());
        }
        maxValueText.setY(height - size * 0.145);

        fontSize = size * 0.1;
        description.setFont(Fonts.latoRegular(fontSize));
    }

    @Override protected void resize() {
        super.resize();

        description.setPrefSize(width - size * 0.1, size * 0.43);
        description.relocate(size * 0.05, height * 0.42);

        barBackground.setPrefSize(width, size * 0.035);
        barBackground.relocate(0, height - size * 0.035);

        barClip.setX(0);
        barClip.setY(height - size * 0.05);
        barClip.setWidth(width);
        barClip.setHeight(size * 0.05);
        barClip.setArcWidth(tile.getRoundedCorners() ? size * 0.05 : 0.0);
        barClip.setArcHeight(tile.getRoundedCorners() ? size * 0.05 : 0.0);

        bar.setX(0);
        bar.setY(height - size * 0.035);
        bar.setWidth(clamp(minValue, maxValue, tile.getCurrentValue()) * stepSize);
        bar.setHeight(size * 0.035);

        maxValueRect.setWidth((maxValueText.getLayoutBounds().getWidth() + maxValueUnitText.getLayoutBounds().getWidth()) + size * 0.06);
        maxValueRect.setHeight(maxValueText.getLayoutBounds().getHeight() * 1.01);
        maxValueRect.setX((width - size * 0.05) - maxValueRect.getWidth());
        maxValueRect.setY(height - size * 0.2225);
        maxValueRect.setArcWidth(size * 0.025);
        maxValueRect.setArcHeight(size * 0.025);

        valueUnitFlow.setPrefWidth(width - size * 0.1);
        valueUnitFlow.relocate(size * 0.05, contentBounds.getY());
    }

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(tile.getTitle());
        unitText.setText(tile.getUnit());
        description.setText(tile.getDescription());
        description.setAlignment(tile.getDescriptionAlignment());
        percentageText.setText(String.format(locale, "%." + tile.getDecimals() + "f", tile.getValue() / range * 100));
        maxValueText.setText(String.format(locale, "%." + tile.getTickLabelDecimals() + "f", tile.getMaxValue()));
        maxValueUnitText.setText(tile.getUnit());

        resizeStaticText();

        barBackground.setBackground(new Background(new BackgroundFill(tile.getBarBackgroundColor().brighter().brighter(), new CornerRadii(0.0, 0.0, tile.getRoundedCorners() ? size * 0.025 : 0.0, tile.getRoundedCorners() ? size * 0.025 : 0.0, false), Insets.EMPTY)));
        barColor = tile.getBarColor();

        if (sectionsVisible && !sections.isEmpty()) {
            setBarColor(tile.getValue());
        } else {
            bar.setFill(barColor);
        }

        titleText.setFill(tile.getTitleColor());
        unitText.setFill(tile.getUnitColor());
        description.setTextFill(tile.getDescriptionColor());
        maxValueText.setFill(tile.getBackgroundColor());
        maxValueUnitText.setFill(tile.getBackgroundColor());
        maxValueRect.setFill(Double.compare(tile.getCurrentValue(), maxValue) >= 0 ? barColor : tile.getThresholdColor());
        valueText.setFill(tile.getValueColor());
        unitText.setFill(tile.getUnitColor());
    }
}
