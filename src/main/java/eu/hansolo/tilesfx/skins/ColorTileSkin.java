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

import eu.hansolo.tilesfx.Section;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.colors.ColorSkin;
import eu.hansolo.tilesfx.events.TileEvent.EventType;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.GradientLookup;
import eu.hansolo.tilesfx.tools.Helper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;


public class ColorTileSkin extends TileSkin {
    private Text           titleText;
    private Text           valueText;
    private Text           upperUnitText;
    private Line           fractionLine;
    private Text           unitText;
    private VBox           unitFlow;
    private HBox           valueUnitFlow;
    private Rectangle      barBackground;
    private Rectangle      bar;
    private GradientLookup gradientLookup;


    // ******************** Constructors **************************************
    public ColorTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        gradientLookup = new GradientLookup();

        if (tile.getSections().isEmpty()) {
            tile.setSections(new Section(0.00, 0.25, ColorSkin.GREEN),
                             new Section(0.25, 0.50, ColorSkin.YELLOW),
                             new Section(0.50, 0.75, ColorSkin.ORANGE),
                             new Section(0.75, 1.00, ColorSkin.RED));
        }
        tile.setBackgroundColor(tile.getSections().get(0).getColor());

        titleText = new Text();
        titleText.setFill(tile.getTitleColor());
        Helper.enableNode(titleText, !tile.getTitle().isEmpty());

        valueText = new Text(String.format(locale, formatString, ((tile.getValue() - minValue) / range * 100)));
        valueText.setFill(tile.getValueColor());
        valueText.setTextOrigin(VPos.BASELINE);
        Helper.enableNode(valueText, tile.isValueVisible());

        upperUnitText = new Text("");
        upperUnitText.setFill(tile.getUnitColor());
        Helper.enableNode(upperUnitText, !tile.getUnit().isEmpty());

        fractionLine = new Line();

        unitText = new Text(tile.getUnit());
        unitText.setFill(tile.getUnitColor());
        Helper.enableNode(unitText, !tile.getUnit().isEmpty());

        unitFlow = new VBox(upperUnitText, fractionLine, unitText);
        unitFlow.setAlignment(Pos.CENTER_RIGHT);

        valueUnitFlow = new HBox(valueText, unitFlow);
        valueUnitFlow.setAlignment(Pos.CENTER);
        valueUnitFlow.setMouseTransparent(true);

        barBackground = new Rectangle();
        barBackground.setFill(tile.getBarBackgroundColor());

        bar = new Rectangle();
        bar.setFill(tile.getForegroundColor());

        getPane().getChildren().addAll(titleText, valueUnitFlow, /*fractionLine,*/ barBackground, bar);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if (EventType.VALUE.name().equals(EVENT_TYPE)) {
            handleCurrentValue(tile.getCurrentValue());
        } else if (EventType.VISIBILITY.name().equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !tile.getTitle().isEmpty());
        } else if (EventType.SECTION.name().equals(EVENT_TYPE)) {
            redraw();
        }
    }

    @Override protected void handleCurrentValue(final double VALUE) {
        double percentage = VALUE / (tile.getRange());
        if (tile.getCustomDecimalFormatEnabled()) {
            valueText.setText(decimalFormat.format(percentage));
        } else {
            valueText.setText(String.format(locale, formatString, percentage));
        }

        if (tile.isFillWithGradient()) {
            tile.setBackgroundColor(gradientLookup.getColorAt(percentage));
        } else {
            for (Section section : tile.getSections()) {
                if (section.contains(percentage)) {
                    tile.setBackgroundColor(section.getColor());
                    break;
                }
            }
        }

        resizeDynamicText();

        bar.setWidth(size * 0.9 * percentage);
    }


    // ******************** Resizing ******************************************
    @Override public void layoutChildren(final double X, final double Y, final double W, final double H) {
        super.layoutChildren(X, Y, W, H);
        resize();
    }

    @Override protected void resizeDynamicText() {
        double maxWidth = unitText.isVisible() ? width - size * 0.3 : width - size * 0.1;
        double fontSize = size * 0.48;
        valueText.setFont(Fonts.latoBold(fontSize));
        double correctedFontSize = fontSize;
        if (valueText.getLayoutBounds().getWidth() > maxWidth) {
            correctedFontSize = Helper.adjustTextSize(valueText, maxWidth, fontSize);
        }
        double fontFactor = correctedFontSize / fontSize;

        maxWidth = size * 0.275;
        fontSize = upperUnitText.getText().isEmpty() ? size * 0.24 : size * 0.20;
        upperUnitText.setFont(Fonts.latoRegular(fontSize * fontFactor));
        if (upperUnitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(upperUnitText, maxWidth, fontSize); }

        fontSize = upperUnitText.getText().isEmpty() ? size * 0.24 : size * 0.20;
        unitText.setFont(Fonts.latoRegular(fontSize * fontFactor));
        if (unitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(unitText, maxWidth, fontSize); }

        valueUnitFlow.relocate(size * 0.05, (height - valueUnitFlow.getLayoutBounds().getHeight()) * 0.5);
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
    }

    @Override protected void resize() {
        super.resize();

        resizeDynamicText();
        resizeStaticText();

        valueUnitFlow.setPrefWidth(width - size * 0.1);
        valueUnitFlow.relocate(size * 0.05, (height - valueUnitFlow.getLayoutBounds().getHeight()) * 0.5);
        valueUnitFlow.setMaxHeight(valueText.getFont().getSize());

        fractionLine.setStartX(unitFlow.getWidth() * 0.1);
        fractionLine.setStartY(unitFlow.getLayoutBounds().getHeight() * 0.5);
        fractionLine.setEndX(unitFlow.getWidth() * 0.8);
        fractionLine.setEndY(unitFlow.getLayoutBounds().getHeight() * 0.5);
        fractionLine.setStroke(tile.getUnitColor());
        fractionLine.setStrokeWidth(size * 0.005);

        unitFlow.setTranslateY(-size * 0.005);

        barBackground.relocate(size * 0.05, height * 0.85);
        barBackground.setWidth(size * 0.9);
        barBackground.setHeight(size * 0.02);
        barBackground.setArcWidth(size * 0.02);
        barBackground.setArcHeight(size * 0.02);

        bar.relocate(size * 0.05, height * 0.85);
        bar.setHeight(size * 0.02);
        bar.setArcWidth(size * 0.02);
        bar.setArcHeight(size * 0.02);
    }

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(tile.getTitle());

        if (tile.getCustomDecimalFormatEnabled()) {
            valueText.setText(decimalFormat.format(Helper.clamp(minValue, maxValue, tile.getCurrentValue())));
        } else {
            valueText.setText(String.format(locale, formatString, Helper.clamp(minValue, maxValue, tile.getCurrentValue())));
        }
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

        resizeDynamicText();
        resizeStaticText();

        titleText.setFill(tile.getTitleColor());
        valueText.setFill(tile.getValueColor());
        upperUnitText.setFill(tile.getUnitColor());
        fractionLine.setStroke(tile.getUnitColor());
        unitText.setFill(tile.getUnitColor());

        if (tile.isFillWithGradient() && !tile.getGradientStops().isEmpty()) {
            gradientLookup.setStops(tile.getGradientStops());
        }

        barBackground.setFill(tile.getBarBackgroundColor());
        bar.setFill(tile.getForegroundColor());
    }
}
