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
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.Helper;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;


public class CenterTextTileSkin extends TileSkin {
    private Text            titleText;
    private Text            text;
    private Canvas          canvas;
    private GraphicsContext ctx;


    // ******************** Constructors **************************************
    public CenterTextTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        titleText = new Text();
        titleText.setFill(tile.getTitleColor());
        Helper.enableNode(titleText, !tile.getTitle().isEmpty());

        text = new Text(tile.getText());
        text.setFill(tile.getTextColor());
        Helper.enableNode(text, tile.isTextVisible());

        canvas = new Canvas(getContentBounds().getWidth(), getContentBounds().getHeight());
        ctx    = canvas.getGraphicsContext2D();

        getPane().getChildren().addAll(titleText, canvas, text);
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
        }
    }

    @Override public void dispose() {
        super.dispose();
    }


    // ******************** Resizing ******************************************
    @Override protected void resizeStaticText() {
        double  maxWidth = width - size * 0.1;
        double  fontSize = size * textSize.factor;

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
        width  = tile.getWidth() - tile.getInsets().getLeft() - tile.getInsets().getRight();
        height = tile.getHeight() - tile.getInsets().getTop() - tile.getInsets().getBottom();
        size   = width < height ? width : height;

        if (tile.isShowing() && width > 0 && height > 0) {
            pane.setMaxSize(width, height);
            pane.setPrefSize(width, height);
            canvas.setWidth(contentBounds.getWidth());
            canvas.setHeight(contentBounds.getHeight());
            canvas.setLayoutX(contentBounds.getMinX());
            canvas.setLayoutY(contentBounds.getMinY());
            redraw();
        }
    }

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(tile.getTitle());
        text.setText(tile.getText());

        resizeDynamicText();
        resizeStaticText();

        titleText.setFill(tile.getTitleColor());
        text.setFill(tile.getTextColor());
        drawText();
    }

    private void drawText() {
        ctx.clearRect(0, 0, contentBounds.getWidth(), contentBounds.getHeight());
        ctx.setTextAlign(TextAlignment.CENTER);
        ctx.setTextBaseline(VPos.CENTER);
        ctx.setFont(Fonts.latoRegular(size * 0.2));
        ctx.setFill(tile.getDescriptionColor());
        ctx.fillText(tile.getDescription(), contentBounds.getWidth() * 0.5, contentBounds.getHeight() * 0.5, contentBounds.getWidth() * 0.9);
    }
}
