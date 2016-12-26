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
import javafx.animation.FillTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;


/**
 * Created by hansolo on 19.12.16.
 */
public class SwitchTileSkin extends TileSkin {
    private Text      titleText;
    private Text      text;
    private Rectangle switchBorder;
    private Rectangle switchBackground;
    private Circle    thumb;


    // ******************** Constructors **************************************
    public SwitchTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        titleText = new Text();
        titleText.setFill(getSkinnable().getTitleColor());
        Helper.enableNode(titleText, !getSkinnable().getTitle().isEmpty());

        text = new Text(getSkinnable().getUnit());
        text.setFill(getSkinnable().getUnitColor());
        Helper.enableNode(text, getSkinnable().isTextVisible());

        switchBorder = new Rectangle();

        switchBackground = new Rectangle();
        switchBackground.setMouseTransparent(true);
        switchBackground.setFill(getSkinnable().isSelected() ? getSkinnable().getActiveColor() : getSkinnable().getBackgroundColor());

        thumb = new Circle();
        thumb.setMouseTransparent(true);

        getPane().getChildren().addAll(titleText, text, switchBorder, switchBackground, thumb);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        switchBorder.setOnMousePressed(e -> getSkinnable().setSelected(!getSkinnable().isSelected()));
        getSkinnable().selectedProperty().addListener(e -> moveThumb());
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !getSkinnable().getTitle().isEmpty());
            Helper.enableNode(text, getSkinnable().isTextVisible());
        }
    };

    private void moveThumb() {
        if (getSkinnable().isSelected()) {
            // move thumb to the right
            TranslateTransition moveThumb = new TranslateTransition(Duration.millis(200), thumb);
            moveThumb.setFromX(0);
            moveThumb.setToX(size * 0.225);
            FillTransition fillSwitch = new FillTransition(Duration.millis(200), switchBackground);
            fillSwitch.setFromValue(getSkinnable().getBackgroundColor());
            fillSwitch.setToValue(getSkinnable().getActiveColor());
            ParallelTransition parallelTransition = new ParallelTransition(moveThumb, fillSwitch);
            parallelTransition.play();
        } else {
            // move thumb to the left
            TranslateTransition moveThumb = new TranslateTransition(Duration.millis(200), thumb);
            moveThumb.setFromX(size * 0.225);
            moveThumb.setToX(0);
            FillTransition fillSwitch = new FillTransition(Duration.millis(200), switchBackground);
            fillSwitch.setFromValue(getSkinnable().getActiveColor());
            fillSwitch.setToValue(getSkinnable().getBackgroundColor());
            ParallelTransition parallelTransition = new ParallelTransition(moveThumb, fillSwitch);
            parallelTransition.play();
        }
    }


    // ******************** Resizing ******************************************
    @Override protected void resizeStaticText() {
        double maxWidth = size * 0.9;
        double fontSize = size * 0.06;

        titleText.setFont(Fonts.latoRegular(fontSize));
        if (titleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(titleText, maxWidth, fontSize); }
        titleText.relocate(size * 0.05, size * 0.05);

        maxWidth = size * 0.9;
        fontSize = size * 0.05;
        text.setText(getSkinnable().getText());
        if (text.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(text, maxWidth, fontSize); }
        text.setX(size * 0.05);
        text.setY(size * 0.95);
    };

    @Override protected void resize() {
        super.resize();

        switchBorder.setWidth(size * 0.445);
        switchBorder.setHeight(size * 0.22);
        switchBorder.setArcWidth(size * 0.22);
        switchBorder.setArcHeight(size * 0.22);
        switchBorder.relocate((size - switchBorder.getWidth()) * 0.5, (size - switchBorder.getHeight()) * 0.5);

        switchBackground.setWidth(size * 0.425);
        switchBackground.setHeight(size * 0.2);
        switchBackground.setArcWidth(size * 0.2);
        switchBackground.setArcHeight(size * 0.2);
        switchBackground.relocate((size - switchBackground.getWidth()) * 0.5, (size - switchBackground.getHeight()) * 0.5);

        thumb.setRadius(size * 0.09);
        thumb.setCenterX(size * 0.3875);
        thumb.setCenterY(size * 0.5);
        moveThumb();
    };

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(getSkinnable().getTitle());
        text.setText(getSkinnable().getText());

        resizeStaticText();

        titleText.setFill(getSkinnable().getTitleColor());
        text.setFill(getSkinnable().getTextColor());
        switchBorder.setFill(getSkinnable().getForegroundColor());
        thumb.setFill(getSkinnable().getForegroundColor());
    };
}
