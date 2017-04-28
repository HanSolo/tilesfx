/*
 * Copyright (c) 2016 by Gerrit Grunwald
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
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.Helper;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

import static eu.hansolo.tilesfx.tools.Helper.clamp;


/**
 * Created by hansolo on 19.12.16.
 */
public class SliderTileSkin extends TileSkin {
    private Text                     titleText;
    private Text                     text;
    private Text                     valueText;
    private Text                     unitText;
    private TextFlow                 valueUnitFlow;
    private Label                    description;
    private Circle                   thumb;
    private Rectangle                barBackground;
    private Rectangle                bar;
    private Point2D                  dragStart;
    private double                   centerX;
    private double                   centerY;
    private double                   formerThumbPos;
    private double                   trackStart;
    private double                   trackLength;
    private EventHandler<MouseEvent> mouseEventHandler;


    // ******************** Constructors **************************************
    public SliderTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        mouseEventHandler = e -> {
            final EventType TYPE = e.getEventType();
            if (MouseEvent.MOUSE_PRESSED == TYPE) {
                dragStart      = thumb.localToParent(e.getX(), e.getY());
                formerThumbPos = (tile.getCurrentValue() - minValue) / range;
            } else if (MouseEvent.MOUSE_DRAGGED == TYPE) {
                Point2D currentPos = thumb.localToParent(e.getX(), e.getY());
                double  dragPos    = currentPos.getX() - dragStart.getX();
                thumbDragged((formerThumbPos + dragPos / trackLength));
            }
        };

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

        getPane().getChildren().addAll(titleText, text, valueUnitFlow, description, barBackground, bar, thumb);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        thumb.addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEventHandler);
        thumb.addEventHandler(MouseEvent.MOUSE_DRAGGED, mouseEventHandler);
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
    };

    @Override protected void handleCurrentValue(final double VALUE) {
        valueText.setText(String.format(locale, formatString, VALUE));
        resizeDynamicText();
        centerX = trackStart + (trackLength * ((VALUE - minValue) / range));
        thumb.setCenterX(clamp(trackStart, (trackStart + trackLength), centerX));
        thumb.setFill(Double.compare(VALUE, tile.getMinValue()) != 0 ? tile.getBarColor() : tile.getForegroundColor());
        bar.setWidth(thumb.getCenterX() - trackStart);
    };

    private void thumbDragged(final double POSITION) {
        tile.setValue(clamp(minValue, maxValue, (POSITION * range) + minValue));
    }

    @Override public void dispose() {
        thumb.removeEventHandler(MouseEvent.MOUSE_PRESSED, mouseEventHandler);
        thumb.removeEventHandler(MouseEvent.MOUSE_DRAGGED, mouseEventHandler);
        super.dispose();
    }


    // ******************** Resizing ******************************************
    @Override protected void resizeDynamicText() {
        double maxWidth = unitText.isVisible() ? width - size * 0.275 : width - size * 0.1;
        double fontSize = size * 0.24;
        valueText.setFont(Fonts.latoRegular(fontSize));
        if (valueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(valueText, maxWidth, fontSize); }
    };
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

        //maxWidth = size * 0.9;
        fontSize = size * textSize.factor;
        text.setText(tile.getText());
        text.setFont(Fonts.latoRegular(fontSize));
        if (text.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(text, maxWidth, fontSize); }
        switch(tile.getTextAlignment()) {
            default    :
            case LEFT  : text.setX(size * 0.05); break;
            case CENTER: text.setX((width - text.getLayoutBounds().getWidth()) * 0.5); break;
            case RIGHT : text.setX(width - (size * 0.05) - text.getLayoutBounds().getWidth()); break;
        }
        text.setY(height - size * 0.05);

        maxWidth = width - size * 0.85;
        fontSize = size * 0.12;
        unitText.setFont(Fonts.latoRegular(fontSize));
        if (unitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(unitText, maxWidth, fontSize); }

        fontSize = size * 0.1;
        description.setFont(Fonts.latoRegular(fontSize));
    };

    @Override protected void resize() {
        super.resize();

        description.setPrefSize(width - size * 0.1, size * 0.43);
        description.relocate(size * 0.05, height * 0.42);

        trackStart  = size * 0.14;
        trackLength = width - size * 0.28;
        centerX     = trackStart + (trackLength * ((tile.getCurrentValue() - minValue) / range));
        centerY     = height * 0.71;

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

        thumb.setRadius(size * 0.09);
        thumb.setCenterX(centerX);
        thumb.setCenterY(centerY);

        valueUnitFlow.setPrefWidth(width - size * 0.1);
        valueUnitFlow.relocate(size * 0.05, size * 0.15);
    };

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(tile.getTitle());
        text.setText(tile.getText());
        unitText.setText(tile.getUnit());
        description.setText(tile.getDescription());
        description.setAlignment(tile.getDescriptionAlignment());

        resizeStaticText();

        titleText.setFill(tile.getTitleColor());
        text.setFill(tile.getTextColor());
        valueText.setFill(tile.getValueColor());
        unitText.setFill(tile.getUnitColor());
        barBackground.setFill(tile.getBarBackgroundColor());
        bar.setFill(tile.getBarColor());
        thumb.setFill(Double.compare(tile.getValue(), tile.getMinValue()) != 0 ? tile.getBarColor() : tile.getForegroundColor());
    };
}
