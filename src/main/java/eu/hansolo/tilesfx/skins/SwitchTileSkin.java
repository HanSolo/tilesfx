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
import eu.hansolo.tilesfx.events.SwitchEvent;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.Helper;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;


/**
 * Created by hansolo on 19.12.16.
 */
public class SwitchTileSkin extends TileSkin {
    private static final SwitchEvent SWITCH_PRESSED  = new SwitchEvent(SwitchEvent.SWITCH_PRESSED);
    private static final SwitchEvent SWITCH_RELEASED = new SwitchEvent(SwitchEvent.SWITCH_RELEASED);
    private Text                     titleText;
    private Text                     text;
    private Label                    description;
    private Rectangle                switchBorder;
    private Rectangle                switchBackground;
    private Circle                   thumb;
    private Timeline                 timeline;
    private EventHandler<MouseEvent> mouseEventHandler;
    private InvalidationListener     selectedListener;


    // ******************** Constructors **************************************
    public SwitchTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        mouseEventHandler = e -> {
            final EventType TYPE = e.getEventType();
            if (MouseEvent.MOUSE_PRESSED == TYPE) {
                tile.setActive(!tile.isActive());
                tile.fireEvent(SWITCH_PRESSED);
            } else if(MouseEvent.MOUSE_RELEASED == TYPE) {
                tile.fireEvent(SWITCH_RELEASED);
            }
        };
        selectedListener = o -> moveThumb();

        timeline = new Timeline();

        titleText = new Text();
        titleText.setFill(tile.getTitleColor());
        Helper.enableNode(titleText, !tile.getTitle().isEmpty());

        text = new Text(tile.getText());
        text.setFill(tile.getUnitColor());
        Helper.enableNode(text, tile.isTextVisible());

        description = new Label(tile.getDescription());
        description.setAlignment(tile.getDescriptionAlignment());
        description.setWrapText(true);
        description.setTextFill(tile.getTextColor());
        Helper.enableNode(description, !tile.getDescription().isEmpty());

        switchBorder = new Rectangle();

        switchBackground = new Rectangle();
        switchBackground.setMouseTransparent(true);
        switchBackground.setFill(tile.isActive() ? tile.getActiveColor() : tile.getBackgroundColor());

        thumb = new Circle();
        thumb.setMouseTransparent(true);
        thumb.setEffect(shadow);

        getPane().getChildren().addAll(titleText, text, description, switchBorder, switchBackground, thumb);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        switchBorder.addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEventHandler);
        switchBorder.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseEventHandler);
        tile.activeProperty().addListener(selectedListener);
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !tile.getTitle().isEmpty());
            Helper.enableNode(text, tile.isTextVisible());
            Helper.enableNode(description, !tile.getDescription().isEmpty());
        }
    }

    private void moveThumb() {
        KeyValue thumbLeftX                 = new KeyValue(thumb.centerXProperty(), switchBackground.getLayoutX() + size * 0.1);
        KeyValue thumbRightX                = new KeyValue(thumb.centerXProperty(), switchBackground.getLayoutX() + switchBackground.getWidth() - size * 0.1);
        KeyValue switchBackgroundLeftColor  = new KeyValue(switchBackground.fillProperty(), tile.getBackgroundColor());
        KeyValue switchBackgroundRightColor = new KeyValue(switchBackground.fillProperty(), tile.getActiveColor());
        if (tile.isActive()) {
            // move thumb from left to the right
            KeyFrame kf0 = new KeyFrame(Duration.ZERO, thumbLeftX, switchBackgroundLeftColor);
            KeyFrame kf1 = new KeyFrame(Duration.millis(200), thumbRightX, switchBackgroundRightColor);
            timeline.getKeyFrames().setAll(kf0, kf1);
        } else {
            // move thumb from right to the left
            KeyFrame kf0 = new KeyFrame(Duration.ZERO, thumbRightX, switchBackgroundRightColor);
            KeyFrame kf1 = new KeyFrame(Duration.millis(200), thumbLeftX, switchBackgroundLeftColor);
            timeline.getKeyFrames().setAll(kf0, kf1);
        }
        timeline.play();
    }

    @Override public void dispose() {
        switchBorder.removeEventHandler(MouseEvent.MOUSE_PRESSED, mouseEventHandler);
        switchBorder.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseEventHandler);
        tile.activeProperty().removeListener(selectedListener);
        super.dispose();
    }


    // ******************** Resizing ******************************************
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

        fontSize = size * 0.1;
        description.setFont(Fonts.latoRegular(fontSize));
    }

    @Override protected void resize() {
        super.resize();

        description.setPrefSize(contentBounds.getWidth(), size * 0.43);
        description.relocate(contentBounds.getX(), height * 0.42);

        switchBorder.setWidth(size * 0.445);
        switchBorder.setHeight(size * 0.22);
        switchBorder.setArcWidth(size * 0.22);
        switchBorder.setArcHeight(size * 0.22);
        switchBorder.relocate((width - switchBorder.getWidth()) * 0.5, tile.getDescription().isEmpty() ? (height - switchBorder.getHeight()) * 0.5 : height - size * 0.40);

        switchBackground.setWidth(size * 0.425);
        switchBackground.setHeight(size * 0.2);
        switchBackground.setArcWidth(size * 0.2);
        switchBackground.setArcHeight(size * 0.2);
        switchBackground.relocate((width - switchBackground.getWidth()) * 0.5, tile.getDescription().isEmpty() ? (height - switchBackground.getHeight()) * 0.5 : height - size * 0.39);

        thumb.setRadius(size * 0.09);
        //thumb.setCenterX(tile.isActive() ? width * 0.6125 : width * 0.3875);
        thumb.setCenterX(tile.isActive() ? switchBackground.getLayoutX() + switchBackground.getWidth() - size * 0.1 : switchBackground.getLayoutX() + size * 0.1);
        thumb.setCenterY(tile.getDescription().isEmpty() ? height * 0.5 : height - size * 0.29);
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
        description.setTextFill(tile.getDescriptionColor());
        switchBorder.setFill(tile.getForegroundColor());
        thumb.setFill(tile.getForegroundColor());
    }
}
