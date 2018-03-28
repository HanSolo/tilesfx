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
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.Helper;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

import static eu.hansolo.tilesfx.tools.Helper.clamp;


/**
 * Created by hansolo on 19.12.16.
 */
public class PlusMinusTileSkin extends TileSkin {
    private Text                     titleText;
    private Text                     text;
    private Text                     valueText;
    private Text                     unitText;
    private TextFlow                 valueUnitFlow;
    private Label                    description;
    private Label                    plusLabel;
    private Label                    minusLabel;
    private EventHandler<MouseEvent> mouseEventHandler;


    // ******************** Constructors **************************************
    public PlusMinusTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        mouseEventHandler = e -> {
            final EventType TYPE = e.getEventType();
            final Label     SRC  = (Label) e.getSource();
            if (MouseEvent.MOUSE_PRESSED == TYPE) {
                if (SRC.equals(minusLabel)) {
                    decrement();
                } else if (SRC.equals(plusLabel)) {
                    increment();
                }
            } else if (MouseEvent.MOUSE_RELEASED == TYPE) {
                if (SRC.equals(minusLabel)) {
                    minusLabel.setTextFill(tile.getForegroundColor());
                    minusLabel.setBorder(new Border(new BorderStroke(tile.getForegroundColor(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(size * 0.01))));
                } else if (SRC.equals(plusLabel)) {
                    plusLabel.setTextFill(tile.getForegroundColor());
                    plusLabel.setBorder(new Border(new BorderStroke(tile.getForegroundColor(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(size * 0.01))));
                }
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

        plusLabel = new Label("+");
        plusLabel.setAlignment(Pos.CENTER);
        plusLabel.setEffect(shadow);
        plusLabel.setPickOnBounds(false);
        
        minusLabel = new Label("-");
        minusLabel.setAlignment(Pos.CENTER);
        minusLabel.setEffect(shadow);
        minusLabel.setPickOnBounds(false);

        getPane().getChildren().addAll(titleText, text, valueUnitFlow, description, minusLabel, plusLabel);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        plusLabel.addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEventHandler);
        plusLabel.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseEventHandler);
        minusLabel.addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEventHandler);
        minusLabel.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseEventHandler);
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

    @Override protected void handleCurrentValue(final double VALUE) {
        valueText.setText(String.format(locale, formatString, VALUE));
        resizeDynamicText();
    }

    private void increment() {
        plusLabel.setTextFill(tile.getActiveColor());
        plusLabel.setBorder(new Border(new BorderStroke(tile.getActiveColor(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(size * 0.01))));
        double newValue = clamp(minValue, maxValue, tile.getValue() + tile.getIncrement());
        tile.setValue(newValue);
    }
    private void decrement() {
        minusLabel.setTextFill(tile.getActiveColor());
        minusLabel.setBorder(new Border(new BorderStroke(tile.getActiveColor(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(size * 0.01))));
        double newValue = clamp(minValue, maxValue, tile.getValue() - tile.getIncrement());
        tile.setValue(newValue);
    }

    @Override public void dispose() {
        plusLabel.removeEventHandler(MouseEvent.MOUSE_PRESSED, mouseEventHandler);
        plusLabel.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseEventHandler);
        minusLabel.removeEventHandler(MouseEvent.MOUSE_PRESSED, mouseEventHandler);
        minusLabel.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseEventHandler);
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
        unitText.setFont(Fonts.latoRegular(fontSize));
        if (unitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(unitText, maxWidth, fontSize); }

        fontSize = size * 0.1;
        description.setFont(Fonts.latoRegular(fontSize));
    }

    @Override protected void resize() {
        super.resize();

        description.setPrefSize(width - size * 0.1, size * 0.43);
        description.relocate(size * 0.05, titleText.isVisible() ? height * 0.42 : height * 0.32);

        double buttonSize = size * 0.18;

        minusLabel.setFont(Fonts.latoBold(size * 0.2));
        minusLabel.setPrefSize(buttonSize, buttonSize);
        minusLabel.setMinSize(buttonSize, buttonSize);
        minusLabel.setMaxSize(buttonSize, buttonSize);
        //minusLabel.setPadding(new Insets(-0.055 * size, 0, 0, 0));
        minusLabel.setPadding(new Insets(-0.0625 * size, 0, 0, 0));
        minusLabel.setBorder(new Border(new BorderStroke(tile.getForegroundColor(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(size * 0.01))));
        minusLabel.relocate(size * 0.05, height - size * 0.20 - buttonSize);
        
        plusLabel.setFont(Fonts.latoBold(size * 0.2));
        plusLabel.setPrefSize(buttonSize, buttonSize);
        plusLabel.setMinSize(buttonSize, buttonSize);
        plusLabel.setMaxSize(buttonSize, buttonSize);
        plusLabel.setPadding(new Insets(-0.05 * size, 0, 0, 0));
        plusLabel.setBorder(new Border(new BorderStroke(tile.getForegroundColor(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(size * 0.01))));
        plusLabel.relocate(width - size * 0.05 - buttonSize, height - size * 0.20 - buttonSize);

        valueUnitFlow.setPrefWidth(contentBounds.getWidth());
        valueUnitFlow.relocate(contentBounds.getX(), contentBounds.getY());
    }

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
        plusLabel.setTextFill(tile.getForegroundColor());
        minusLabel.setTextFill(tile.getForegroundColor());
    }
}
