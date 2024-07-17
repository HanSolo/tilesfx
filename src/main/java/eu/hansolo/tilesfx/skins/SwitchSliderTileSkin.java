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
import eu.hansolo.tilesfx.events.SwitchEvent;
import eu.hansolo.tilesfx.events.TileEvt;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.Helper;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import static eu.hansolo.tilesfx.tools.Helper.clamp;


public class SwitchSliderTileSkin extends TileSkin {
    private static final SwitchEvent              SWITCH_PRESSED  = new SwitchEvent(SwitchEvent.SWITCH_PRESSED);
    private static final SwitchEvent              SWITCH_RELEASED = new SwitchEvent(SwitchEvent.SWITCH_RELEASED);
    private final        TileEvt                  VALUE_CHANGING  = new TileEvt(tile, TileEvt.VALUE_CHANGING);
    private final        TileEvt                  VALUE_CHANGED   = new TileEvt(tile, TileEvt.VALUE_CHANGED);
    private              Text                     titleText;
    private              Text                     text;
    private              Text                     valueText;
    private              Text                     unitText;
    private              TextFlow                 valueUnitFlow;
    private              Label                    description;
    private              Rectangle                switchBorder;
    private              Rectangle                switchBackground;
    private              Circle                   switchThumb;
    private              Circle                   thumb;
    private              Rectangle                barBackground;
    private              Rectangle                bar;
    private              Point2D                  dragStart;
    private              double                   centerX;
    private              double                   centerY;
    private              double                   formerThumbPos;
    private              double                   trackStart;
    private              double                   trackLength;
    private              Timeline                 timeline;
    private              EventHandler<MouseEvent> mouseEventHandler;
    private              InvalidationListener     selectedListener;
    private              InvalidationListener     valueListener;


    // ******************** Constructors **************************************
    public SwitchSliderTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        mouseEventHandler = e -> {
            final EventType TYPE = e.getEventType();
            final Object    SRC  = e.getSource();
            if (MouseEvent.MOUSE_PRESSED == TYPE) {
                if (SRC.equals(thumb)) {
                    dragStart = thumb.localToParent(e.getX(), e.getY());
                    formerThumbPos = (tile.getCurrentValue() - minValue) / range;
                    tile.fireTileEvt(VALUE_CHANGING);
                } else if (SRC.equals(switchBorder)) {
                    tile.setActive(!tile.isActive());
                    tile.fireEvent(SWITCH_PRESSED);
                }
            } else if (MouseEvent.MOUSE_DRAGGED == TYPE) {
                Point2D currentPos = thumb.localToParent(e.getX(), e.getY());
                double  dragPos    = currentPos.getX() - dragStart.getX();
                thumbDragged((formerThumbPos + dragPos / trackLength));
            } else if (MouseEvent.MOUSE_RELEASED == TYPE) {
                if (SRC.equals(thumb)) {
                    tile.fireTileEvt(VALUE_CHANGED);
                } else if (SRC.equals(switchBorder)) {
                    tile.fireEvent(SWITCH_RELEASED);
                }
            }
        };
        selectedListener  = o -> moveThumb();
        valueListener     = o -> {
            if (tile.isActive() && Double.compare(tile.getValue(), tile.getMinValue()) != 0) {
                thumb.setFill(tile.getBarColor());
            } else {
                thumb.setFill(tile.getForegroundColor());
            }
        };

        timeline = new Timeline();
        timeline.setOnFinished(event -> thumb.setFill(tile.isActive() ? tile.getBarColor() : tile.getForegroundColor()));

        titleText = new Text();
        titleText.setFill(tile.getTitleColor());
        Helper.enableNode(titleText, !tile.getTitle().isEmpty());

        text = new Text(tile.getText());
        text.setFill(tile.getUnitColor());
        Helper.enableNode(text, tile.isTextVisible());

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

        barBackground = new Rectangle(PREFERRED_WIDTH * 0.795, PREFERRED_HEIGHT * 0.0275);

        bar = new Rectangle(0, PREFERRED_HEIGHT * 0.0275);

        thumb = new Circle(PREFERRED_WIDTH * 0.09);
        thumb.setEffect(shadow);

        switchBorder = new Rectangle();

        switchBackground = new Rectangle();
        switchBackground.setMouseTransparent(true);
        switchBackground.setFill(tile.isActive() ? tile.getActiveColor() : tile.getBackgroundColor());

        switchThumb = new Circle();
        switchThumb.setMouseTransparent(true);
        switchThumb.setEffect(shadow);

        getPane().getChildren().addAll(titleText, text, valueUnitFlow, description,
                                       barBackground, bar, thumb,
                                       switchBorder, switchBackground, switchThumb);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        thumb.addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEventHandler);
        thumb.addEventHandler(MouseEvent.MOUSE_DRAGGED, mouseEventHandler);
        thumb.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseEventHandler);

        switchBorder.addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEventHandler);
        switchBorder.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseEventHandler);

        tile.activeProperty().addListener(selectedListener);

        tile.valueProperty().addListener(valueListener);
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !tile.getTitle().isEmpty());
            Helper.enableNode(text, tile.isTextVisible());
            Helper.enableNode(valueText, tile.isValueVisible());
            Helper.enableNode(unitText, !tile.getUnit().isEmpty());
            Helper.enableNode(description, !tile.getDescription().isEmpty());
        }
    }

    private void moveThumb() {
        KeyValue switchThumbLeftX                 = new KeyValue(switchThumb.centerXProperty(), switchBackground.getLayoutX() + size * 0.05);
        KeyValue switchThumbRightX                = new KeyValue(switchThumb.centerXProperty(), switchBackground.getLayoutX() + switchBackground.getWidth() - size * 0.05);
        KeyValue switchBackgroundLeftColor  = new KeyValue(switchBackground.fillProperty(), tile.getBackgroundColor());
        KeyValue switchBackgroundRightColor = new KeyValue(switchBackground.fillProperty(), tile.getActiveColor());
        if (tile.isActive()) {
            // move thumb from left to the right
            KeyFrame kf0 = new KeyFrame(Duration.ZERO, switchThumbLeftX, switchBackgroundLeftColor);
            KeyFrame kf1 = new KeyFrame(Duration.millis(200), switchThumbRightX, switchBackgroundRightColor);
            timeline.getKeyFrames().setAll(kf0, kf1);
        } else {
            // move thumb from right to the left
            KeyFrame kf0 = new KeyFrame(Duration.ZERO, switchThumbRightX, switchBackgroundRightColor);
            KeyFrame kf1 = new KeyFrame(Duration.millis(200), switchThumbLeftX, switchBackgroundLeftColor);
            timeline.getKeyFrames().setAll(kf0, kf1);
        }
        timeline.play();
    }

    @Override protected void handleCurrentValue(final double VALUE) {
        if (tile.getCustomDecimalFormatEnabled()) {
            valueText.setText(decimalFormat.format(VALUE));
        } else {
            valueText.setText(String.format(locale, formatString, VALUE));
        }
        resizeDynamicText();
        centerX = trackStart + (trackLength * ((VALUE - minValue) / range));
        thumb.setCenterX(clamp(trackStart, (trackStart + trackLength), centerX));
        thumb.setFill(Double.compare(VALUE, tile.getMinValue()) != 0 ? tile.getBarColor() : tile.getForegroundColor());
        bar.setWidth(thumb.getCenterX() - trackStart);
    }

    private void thumbDragged(final double POSITION) {
        tile.setValue(clamp(minValue, maxValue, (POSITION * range) + minValue));
    }

    @Override public void dispose() {
        thumb.removeEventHandler(MouseEvent.MOUSE_PRESSED, mouseEventHandler);
        thumb.removeEventHandler(MouseEvent.MOUSE_DRAGGED, mouseEventHandler);
        thumb.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseEventHandler);

        switchBorder.removeEventHandler(MouseEvent.MOUSE_PRESSED, mouseEventHandler);
        switchBorder.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseEventHandler);

        tile.activeProperty().removeListener(selectedListener);

        tile.valueProperty().removeListener(valueListener);

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

        //maxWidth = size * 0.9;
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

        maxWidth = width - size * 0.275;
        fontSize = size * 0.12;
        unitText.setText(tile.getUnit());
        unitText.setFont(Fonts.latoRegular(fontSize));
        if (unitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(unitText, maxWidth, fontSize); }

        fontSize = size * 0.1;
        description.setFont(Fonts.latoRegular(fontSize));
    }

    @Override protected void resize() {
        super.resize();

        description.setPrefSize(contentBounds.getWidth(), size * 0.43);
        description.relocate(contentBounds.getX(), titleText.isVisible() ? height * 0.42 : height * 0.32);

        trackStart  = size * 0.14;
        trackLength = width - size * 0.28;
        centerX     = trackStart + (trackLength * ((tile.getCurrentValue() - minValue) / range));
        centerY     = height * 0.71;

        thumb.setRadius(size * 0.09);
        thumb.setCenterX(centerX);
        thumb.setCenterY(centerY);

        barBackground.setWidth(trackLength);
        barBackground.setHeight(size * 0.0275);
        barBackground.setX(trackStart);
        barBackground.setY(centerY - size * 0.01375);
        barBackground.setArcWidth(size * 0.0275);
        barBackground.setArcHeight(size * 0.0275);

        bar.setWidth(thumb.getCenterX() - trackStart);
        bar.setHeight(size * 0.0275);
        bar.setX(trackStart);
        bar.setY(centerY - size * 0.01375);
        bar.setArcWidth(size * 0.0275);
        bar.setArcHeight(size * 0.0275);

        switchBorder.setWidth(size * 0.2225);
        switchBorder.setHeight(size * 0.11);
        switchBorder.setArcWidth(size * 0.11);
        switchBorder.setArcHeight(size * 0.11);
        switchBorder.relocate(size * 0.05, centerY - size * 0.275);

        switchBackground.setWidth(size * 0.2125);
        switchBackground.setHeight(size * 0.1);
        switchBackground.setArcWidth(size * 0.1);
        switchBackground.setArcHeight(size * 0.1);
        switchBackground.relocate(switchBorder.getLayoutX() + size * 0.005, switchBorder.getLayoutY() + size * 0.005);

        switchThumb.setRadius(size * 0.045);
        switchThumb.setCenterX(tile.isActive() ? width * 0.30625 : width * 0.19375);
        switchThumb.setCenterX(tile.isActive() ? switchBackground.getLayoutX() + switchBackground.getWidth() - size * 0.05 : switchBackground.getLayoutX() + size * 0.05);
        switchThumb.setCenterY(switchBackground.getLayoutY() + switchBackground.getLayoutBounds().getHeight() * 0.5);

        valueUnitFlow.setPrefWidth(width - size * 0.1);
        valueUnitFlow.relocate(contentBounds.getX(), contentBounds.getY());
    }

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(tile.getTitle());
        text.setText(tile.getText());
        description.setText(tile.getDescription());
        description.setAlignment(tile.getDescriptionAlignment());

        resizeStaticText();

        titleText.setFill(tile.getTitleColor());
        text.setFill(tile.getTextColor());
        valueText.setFill(tile.getValueColor());
        unitText.setFill(tile.getUnitColor());
        barBackground.setFill(tile.getBarBackgroundColor());
        bar.setFill(tile.getBarColor());

        if (tile.isActive() && Double.compare(tile.getValue(), tile.getMinValue()) != 0) {
            thumb.setFill(tile.getBarColor());
        } else {
            thumb.setFill(tile.getForegroundColor());
        }
        switchBorder.setFill(tile.getForegroundColor());
        switchThumb.setFill(tile.getForegroundColor());
    }
}
