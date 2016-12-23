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


/**
 * Created by hansolo on 23.12.16.
 */
public class TextTileSkin extends TileSkin {
    private Text  titleText;
    private Label text;


    // ******************** Constructors **************************************
    public TextTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        titleText = new Text();
        titleText.setFill(getSkinnable().getTitleColor());
        Helper.enableNode(titleText, !getSkinnable().getTitle().isEmpty());

        text = new Label(getSkinnable().getText());
        text.setAlignment(Pos.TOP_LEFT);
        text.setWrapText(true);
        text.setTextOverrun(OverrunStyle.WORD_ELLIPSIS);
        text.setTextFill(getSkinnable().getTextColor());
        text.setPrefSize(PREFERRED_WIDTH * 0.9, PREFERRED_HEIGHT * 0.795);
        Helper.enableNode(text, getSkinnable().isTextVisible());

        getPane().getChildren().addAll(titleText, text);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !getSkinnable().getTitle().isEmpty());
            Helper.enableNode(text, getSkinnable().isTextVisible());
        }
    };


    // ******************** Resizing ******************************************
    @Override protected void resizeDynamicText() {
        double fontSize = size * 0.10;
        text.setFont(Fonts.latoRegular(fontSize));
    };
    @Override protected void resizeStaticText() {
        double maxWidth = size * 0.9;
        double fontSize = size * 0.06;

        titleText.setFont(Fonts.latoRegular(fontSize));
        if (titleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(titleText, maxWidth, fontSize); }
        titleText.relocate(size * 0.05, size * 0.05);
    };

    @Override protected void resize() {
        super.resize();

        text.setPrefSize(size * 0.9, size * 0.795);
        text.relocate(size * 0.05, size * 0.15);
    };

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(getSkinnable().getTitle());
        text.setText(getSkinnable().getText());

        resizeDynamicText();
        resizeStaticText();

        titleText.setFill(getSkinnable().getTitleColor());
        text.setTextFill(getSkinnable().getTextColor());
    };
}
