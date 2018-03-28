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
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

import java.time.LocalTime;


/**
 * Created by hansolo on 23.12.16.
 */
public class TimeTileSkin extends TileSkin {
    private Text     titleText;
    private Text     text;
    private Text     leftText;
    private Text     leftUnit;
    private Text     rightText;
    private Text     rightUnit;
    private TextFlow timeText;
    private Label    description;


    // ******************** Constructors **************************************
    public TimeTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        titleText = new Text();
        titleText.setFill(tile.getTitleColor());
        Helper.enableNode(titleText, !tile.getTitle().isEmpty());

        text = new Text(tile.getText());
        text.setFill(tile.getUnitColor());
        Helper.enableNode(text, tile.isTextVisible());

        LocalTime duration = tile.getDuration();

        leftText = new Text(Integer.toString(duration.getHour() > 0 ? duration.getHour() : duration.getMinute()));
        leftText.setFill(tile.getValueColor());
        leftUnit = new Text(duration.getHour() > 0 ? "h" : "m");
        leftUnit.setFill(tile.getValueColor());

        rightText = new Text(Integer.toString(duration.getHour() > 0 ? duration.getMinute() : duration.getSecond()));
        rightText.setFill(tile.getValueColor());
        rightUnit = new Text(duration.getHour() > 0 ? "m" : "s");
        rightUnit.setFill(tile.getValueColor());

        timeText = new TextFlow(leftText, leftUnit, rightText, rightUnit);
        timeText.setTextAlignment(TextAlignment.RIGHT);
        timeText.setPrefWidth(PREFERRED_WIDTH * 0.9);

        description = new Label(tile.getDescription());
        description.setAlignment(Pos.TOP_RIGHT);
        description.setWrapText(true);
        description.setTextFill(tile.getTextColor());
        Helper.enableNode(description, !tile.getDescription().isEmpty());

        getPane().getChildren().addAll(titleText, text, timeText, description);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !tile.getTitle().isEmpty());
            Helper.enableNode(text, tile.isTextVisible());
            Helper.enableNode(timeText, tile.isValueVisible());
            Helper.enableNode(description, !tile.getDescription().isEmpty());
        }
    }


    // ******************** Resizing ******************************************
    @Override protected void resizeDynamicText() {
        double fontSize = size * 0.24;
        leftText.setFont(Fonts.latoRegular(fontSize));
        rightText.setFont(Fonts.latoRegular(fontSize));
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

        fontSize = size * 0.12;
        leftUnit.setFont(Fonts.latoRegular(fontSize));
        rightUnit.setFont(Fonts.latoRegular(fontSize));

        fontSize = size * 0.1;
        description.setFont(Fonts.latoRegular(fontSize));
    }

    @Override protected void resize() {
        super.resize();

        timeText.setPrefWidth(contentBounds.getWidth());
        timeText.relocate(contentBounds.getX(), contentBounds.getY());

        description.setPrefSize(contentBounds.getWidth(), titleText.isVisible() ? size * 0.43 : size * 0.53);
        description.relocate(contentBounds.getX(), titleText.isVisible() ? height * 0.42 : height * 0.32);
    }

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(tile.getTitle());
        text.setText(tile.getText());
        LocalTime duration = tile.getDuration();
        leftText.setText(Integer.toString(duration.getHour() > 0 ? duration.getHour() : duration.getMinute()));
        leftUnit.setText(duration.getHour() > 0 ? " h  " : " m  ");
        rightText.setText(Integer.toString(duration.getHour() > 0 ? duration.getMinute() : duration.getSecond()));
        rightUnit.setText(duration.getHour() > 0 ? " m" : " s");

        description.setText(tile.getDescription());

        resizeDynamicText();
        resizeStaticText();

        titleText.setFill(tile.getTitleColor());
        text.setFill(tile.getTextColor());
        leftText.setFill(tile.getValueColor());
        leftUnit.setFill(tile.getValueColor());
        rightText.setFill(tile.getValueColor());
        rightUnit.setFill(tile.getValueColor());
        description.setTextFill(tile.getDescriptionColor());
    }
}
