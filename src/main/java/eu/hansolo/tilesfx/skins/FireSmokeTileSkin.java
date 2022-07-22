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
import eu.hansolo.tilesfx.tools.Fire;
import eu.hansolo.tilesfx.tools.Helper;
import eu.hansolo.tilesfx.tools.Smoke;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import static eu.hansolo.tilesfx.tools.Helper.clamp;


public class FireSmokeTileSkin extends TileSkin {
    private Text  titleText;
    private Text  valueText;
    private Text  upperUnitText;
    private Line  fractionLine;
    private Text  unitText;
    private VBox  unitFlow;
    private HBox  valueUnitFlow;
    private Text  text;
    private Smoke smoke;
    private Fire  fire;


    // ******************** Constructors **************************************
    public FireSmokeTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        titleText = new Text();
        titleText.setFill(tile.getTitleColor());
        Helper.enableNode(titleText, !tile.getTitle().isEmpty());

        valueText = new Text();
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

        unitFlow = new VBox(upperUnitText, unitText);
        unitFlow.setAlignment(Pos.CENTER_RIGHT);

        valueUnitFlow = new HBox(valueText, unitFlow);
        //valueUnitFlow.setAlignment(Pos.CENTER);
        valueUnitFlow.setAlignment(Pos.BOTTOM_CENTER);
        valueUnitFlow.setMouseTransparent(true);

        text = new Text(tile.getText());
        text.setFill(tile.getUnitColor());
        Helper.enableNode(text, tile.isTextVisible());

        smoke = new Smoke();
        fire  = new Fire();

        getPane().getChildren().addAll(titleText, text, smoke, fire, valueUnitFlow, fractionLine);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if (TileEvt.VALUE.getName().equals(EVENT_TYPE)) {
            handleCurrentValue(tile.getCurrentValue());
        } else if (TileEvt.VISIBILITY.getName().equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !tile.getTitle().isEmpty());
            Helper.enableNode(text, tile.isTextVisible());
        } else if (TileEvt.SECTION.getName().equals(EVENT_TYPE)) {
            redraw();
        }
    }

    @Override protected void handleCurrentValue(final double VALUE) {
        if (tile.getCustomDecimalFormatEnabled()) {
            valueText.setText(decimalFormat.format(Helper.clamp(minValue, maxValue, VALUE)));
        } else {
            valueText.setText(String.format(locale, formatString, Helper.clamp(minValue, maxValue, VALUE)));
        }
        resizeDynamicText();
        if (VALUE > tile.getThreshold()) {
            smoke.start();
            fire.start();
        } else {
            smoke.stop();
            fire.stop();
        }
    }


    // ******************** Resizing ******************************************
    @Override protected void resizeDynamicText() {
        double maxWidth = unitText.isVisible() ? width - size * 0.275 : width - size * 0.1;
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

        text.setText(tile.getText());
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

    @Override protected void resize() {
        super.resize();

        smoke.setWidth(width);
        smoke.setHeight(height);
        fire.setWidth(width);
        fire.setHeight(height);

        Rectangle smokeClip = new Rectangle(width, height);
        Rectangle fireClip = new Rectangle(width, height);
        if (tile.getRoundedCorners()) {
            smokeClip.setArcWidth(clamp(0, Double.MAX_VALUE, size * 0.025));
            smokeClip.setArcHeight(clamp(0, Double.MAX_VALUE, size * 0.025));
            fireClip.setArcWidth(clamp(0, Double.MAX_VALUE, size * 0.025));
            fireClip.setArcHeight(clamp(0, Double.MAX_VALUE, size * 0.025));
        }
        smoke.setClip(smokeClip);
        fire.setClip(fireClip);

        resizeDynamicText();
        resizeStaticText();

        valueUnitFlow.setPrefWidth(width - size * 0.1);
        valueUnitFlow.relocate(size * 0.05, (height - valueUnitFlow.getLayoutBounds().getHeight()) * 0.5);
        valueUnitFlow.setMaxHeight(valueText.getFont().getSize());

        fractionLine.setStartX(width - 0.17 * size);
        fractionLine.setStartY(tile.getTitle().isEmpty() ? size * 0.2 : size * 0.3);
        fractionLine.setEndX(width - 0.05 * size);
        fractionLine.setEndY(tile.getTitle().isEmpty() ? size * 0.2 : size * 0.3);
        fractionLine.setStroke(tile.getUnitColor());
        fractionLine.setStrokeWidth(size * 0.005);

        unitFlow.setTranslateY(-size * 0.005);
    }

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(tile.getTitle());
        text.setText(tile.getText());

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
        text.setFill(tile.getTextColor());
    }
}
