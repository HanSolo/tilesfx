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
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;


/**
 * Created by hansolo on 23.12.16.
 */
public class TextTileSkin extends TileSkin {
    private Text  titleText;
    private Text  text;
    private Label description;


    // ******************** Constructors **************************************
    public TextTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        titleText = new Text();
        titleText.setFill(tile.getTitleColor());
        Helper.enableNode(titleText, !tile.getTitle().isEmpty());

        description = new Label(tile.getDescription());
        description.setAlignment(Pos.TOP_RIGHT);
        description.setTextAlignment(tile.getDescriptionAlignment());
        description.setWrapText(true);
        description.setTextOverrun(OverrunStyle.WORD_ELLIPSIS);
        description.setTextFill(tile.getTextColor());
        description.setPrefSize(PREFERRED_WIDTH * 0.9, PREFERRED_HEIGHT * 0.795);
        Helper.enableNode(description, tile.isTextVisible());

        text = new Text(tile.getText());
        text.setFill(tile.getUnitColor());
        Helper.enableNode(text, tile.isTextVisible());

        getPane().getChildren().addAll(titleText, text, description);
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
            Helper.enableNode(description, !tile.getDescription().isEmpty());
        }
    };


    // ******************** Resizing ******************************************
    @Override protected void resizeDynamicText() {
        double fontSize = size * 0.1;
        description.setFont(Fonts.latoRegular(fontSize));
    };
    @Override protected void resizeStaticText() {
        double maxWidth = width - size * 0.1;
        double fontSize = size * textSize.factor;

        titleText.setFont(Fonts.latoRegular(fontSize));
        if (titleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(titleText, maxWidth, fontSize); }
        titleText.relocate(size * 0.05, size * 0.05);

        fontSize = size * textSize.factor;
        text.setText(tile.getText());
        text.setFont(Fonts.latoRegular(fontSize));
        if (text.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(text, maxWidth, fontSize); }
        text.setX(size * 0.05);
        text.setY(height - size * 0.05);
    };

    @Override protected void resize() {
        super.resize();

        description.setPrefSize(width - size * 0.1, height - size * 0.215);
        description.relocate(size * 0.05, size * 0.15);
    };

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(tile.getTitle());
        text.setText(tile.getText());
        description.setText(tile.getDescription());

        resizeDynamicText();
        resizeStaticText();

        titleText.setFill(tile.getTitleColor());
        text.setFill(tile.getTextColor());
        description.setTextFill(tile.getTextColor());
    };
}
