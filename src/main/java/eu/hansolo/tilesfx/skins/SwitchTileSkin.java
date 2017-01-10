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
import eu.hansolo.tilesfx.events.SwitchEvent;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.Helper;
import javafx.animation.FillTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;


/**
 * Created by hansolo on 19.12.16.
 */
public class SwitchTileSkin extends TileSkin {
    private static final SwitchEvent SWITCH_PRESSED  = new SwitchEvent(SwitchEvent.SWITCH_PRESSED);
    private static final SwitchEvent SWITCH_RELEASED = new SwitchEvent(SwitchEvent.SWITCH_RELEASED);
    private Text      titleText;
    private Text      text;
    private Label     description;
    private Rectangle switchBorder;
    private Rectangle switchBackground;
    private Circle    thumb;
    private Timeline  timeline;


    // ******************** Constructors **************************************
    public SwitchTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        timeline = new Timeline();

        titleText = new Text();
        titleText.setFill(getSkinnable().getTitleColor());
        Helper.enableNode(titleText, !getSkinnable().getTitle().isEmpty());

        text = new Text(getSkinnable().getText());
        text.setFill(getSkinnable().getUnitColor());
        Helper.enableNode(text, getSkinnable().isTextVisible());

        description = new Label(getSkinnable().getDescription());
        description.setAlignment(Pos.TOP_RIGHT);
        description.setWrapText(true);
        description.setTextFill(getSkinnable().getTextColor());
        Helper.enableNode(description, !getSkinnable().getDescription().isEmpty());

        switchBorder = new Rectangle();

        switchBackground = new Rectangle();
        switchBackground.setMouseTransparent(true);
        switchBackground.setFill(getSkinnable().isSelected() ? getSkinnable().getActiveColor() : getSkinnable().getBackgroundColor());

        thumb = new Circle();
        thumb.setMouseTransparent(true);
        thumb.setEffect(shadow);

        getPane().getChildren().addAll(titleText, text, description, switchBorder, switchBackground, thumb);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        switchBorder.setOnMousePressed(e -> {
            getSkinnable().setSelected(!getSkinnable().isSelected());
            getSkinnable().fireEvent(SWITCH_PRESSED);
        });
        switchBorder.setOnMouseReleased(e -> getSkinnable().fireEvent(SWITCH_RELEASED));
        getSkinnable().selectedProperty().addListener(e -> moveThumb());
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !getSkinnable().getTitle().isEmpty());
            Helper.enableNode(text, getSkinnable().isTextVisible());
            Helper.enableNode(description, !getSkinnable().getDescription().isEmpty());
        }
    };

    private void moveThumb() {
        KeyValue thumbLeftX                 = new KeyValue(thumb.centerXProperty(), size * 0.3875);
        KeyValue thumbRightX                = new KeyValue(thumb.centerXProperty(), size * 0.6125);
        KeyValue switchBackgroundLeftColor  = new KeyValue(switchBackground.fillProperty(), getSkinnable().getBackgroundColor());
        KeyValue switchBackgroundRightColor = new KeyValue(switchBackground.fillProperty(), getSkinnable().getActiveColor());
        if (getSkinnable().isSelected()) {
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


    // ******************** Resizing ******************************************
    @Override protected void resizeStaticText() {
        double maxWidth = size * 0.9;
        double fontSize = size * textSize.factor;

        titleText.setFont(Fonts.latoRegular(fontSize));
        if (titleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(titleText, maxWidth, fontSize); }
        titleText.relocate(size * 0.05, size * 0.05);

        maxWidth = size * 0.9;
        fontSize = size * textSize.factor;
        text.setText(getSkinnable().getText());
        text.setFont(Fonts.latoRegular(fontSize));
        if (text.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(text, maxWidth, fontSize); }
        text.setX(size * 0.05);
        text.setY(size * 0.95);

        fontSize = size * 0.1;
        description.setFont(Fonts.latoRegular(fontSize));
    };

    @Override protected void resize() {
        super.resize();

        description.setPrefSize(size * 0.9, size * 43);
        description.relocate(size * 0.05, size * 0.42);

        switchBorder.setWidth(size * 0.445);
        switchBorder.setHeight(size * 0.22);
        switchBorder.setArcWidth(size * 0.22);
        switchBorder.setArcHeight(size * 0.22);
        switchBorder.relocate((size - switchBorder.getWidth()) * 0.5, getSkinnable().getDescription().isEmpty() ? (size - switchBorder.getHeight()) * 0.5 : size * 0.65);

        switchBackground.setWidth(size * 0.425);
        switchBackground.setHeight(size * 0.2);
        switchBackground.setArcWidth(size * 0.2);
        switchBackground.setArcHeight(size * 0.2);
        switchBackground.relocate((size - switchBackground.getWidth()) * 0.5, getSkinnable().getDescription().isEmpty() ? (size - switchBackground.getHeight()) * 0.5 : size * 0.66);

        thumb.setRadius(size * 0.09);
        thumb.setCenterX(getSkinnable().isSelected() ? size * 0.6125 : size * 0.3875);
        thumb.setCenterY(getSkinnable().getDescription().isEmpty() ? size * 0.5 : size * 0.76);
    };

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(getSkinnable().getTitle());
        text.setText(getSkinnable().getText());
        description.setText(getSkinnable().getDescription());

        resizeStaticText();

        titleText.setFill(getSkinnable().getTitleColor());
        text.setFill(getSkinnable().getTextColor());
        description.setTextFill(getSkinnable().getDescriptionColor());
        switchBorder.setFill(getSkinnable().getForegroundColor());
        thumb.setFill(getSkinnable().getForegroundColor());
    };
}
