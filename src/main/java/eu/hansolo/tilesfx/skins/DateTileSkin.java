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
import javafx.scene.control.OverrunStyle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;


public class DateTileSkin extends TileSkin {
    private static final DateTimeFormatter DAY_FORMATTER        = DateTimeFormatter.ofPattern("EEEE");
    private static final DateTimeFormatter MONTH_YEAR_FORMATTER = DateTimeFormatter.ofPattern("MMMM YYYY");
    private Text  titleText;
    private Text  text;
    private Label description;


    // ******************** Constructors **************************************
    public DateTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        final ZonedDateTime TIME = tile.getTime();

        titleText = new Text(DAY_FORMATTER.format(TIME));
        titleText.setFill(tile.getTitleColor());

        description = new Label(Integer.toString(TIME.getDayOfMonth()));
        description.setAlignment(Pos.CENTER);
        description.setTextAlignment(TextAlignment.CENTER);
        description.setWrapText(true);
        description.setTextOverrun(OverrunStyle.WORD_ELLIPSIS);
        description.setTextFill(tile.getTextColor());
        description.setPrefSize(PREFERRED_WIDTH * 0.9, PREFERRED_HEIGHT * 0.72);
        description.setFont(Fonts.latoLight(PREFERRED_HEIGHT * 0.65));

        text = new Text(MONTH_YEAR_FORMATTER.format(TIME));
        text.setFill(tile.getTextColor());

        getPane().getChildren().addAll(titleText, text, description);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !titleText.getText().isEmpty());
            Helper.enableNode(text, tile.isTextVisible());
            Helper.enableNode(description, !description.getText().isEmpty());
        }
    }


    // ******************** Resizing ******************************************
    @Override protected void resizeDynamicText() {
        double fontSize = size * 0.65;
        description.setFont(Fonts.latoLight(fontSize));
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

        description.setPrefSize(width - size * 0.1, height * 0.7);
        description.relocate(contentBounds.getX(), height * 0.1125);

        redraw();
    }

    @Override protected void redraw() {
        super.redraw();
        final ZonedDateTime TIME = tile.getTime();
        titleText.setText(DAY_FORMATTER.format(TIME));
        text.setText(MONTH_YEAR_FORMATTER.format(TIME));
        description.setText(Integer.toString(TIME.getDayOfMonth()));
        resizeDynamicText();
        resizeStaticText();

        titleText.setFill(tile.getTitleColor());
        text.setFill(tile.getTextColor());
        description.setTextFill(TIME.getDayOfWeek().getValue() == 7 ? Tile.RED : tile.getTextColor());
    }
}
