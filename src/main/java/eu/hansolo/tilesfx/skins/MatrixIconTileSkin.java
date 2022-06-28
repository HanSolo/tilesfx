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
import eu.hansolo.tilesfx.chart.PixelMatrix;
import eu.hansolo.tilesfx.chart.PixelMatrix.PixelShape;
import eu.hansolo.tilesfx.chart.PixelMatrixBuilder;
import eu.hansolo.tilesfx.events.TileEvt;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.Helper;
import eu.hansolo.tilesfx.tools.MatrixIcon;
import javafx.animation.AnimationTimer;
import javafx.scene.text.Font;
import javafx.scene.text.Text;


public class MatrixIconTileSkin extends TileSkin {
    private Text           titleText;
    private Text           text;
    private PixelMatrix    matrix;
    private int            iconCounter;
    private long           updateInterval;
    private long           pauseInterval;
    private AnimationTimer timer;
    private AnimationTimer pauseTimer;
    private long           lastTimerCall;


    // ******************** Constructors **************************************
    public MatrixIconTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        iconCounter = 0;

        matrix = PixelMatrixBuilder.create()
                                   .pixelShape(PixelShape.SQUARE)
                                   .useSpacer(true)
                                   .squarePixels(true)
                                   .colsAndRows(8, 8)
                                   .pixelOnColor(tile.getBarColor())
                                   //.pixelOffColor(Helper.isDark(tile.getBackgroundColor()) ? tile.getBackgroundColor().brighter() : tile.getBackgroundColor().darker())
                                   .pixelOffColor(MatrixIcon.BACKGROUND)
                                   .innerShadowEnabled(true)
                                   .build();

        titleText = new Text();
        titleText.setFill(tile.getTitleColor());
        Helper.enableNode(titleText, !tile.getTitle().isEmpty());

        text = new Text(tile.getText());
        text.setFill(tile.getTextColor());
        Helper.enableNode(text, tile.isTextVisible());

        updateInterval = tile.getAnimationDuration() * 1_000_000l;
        lastTimerCall       = System.nanoTime();
        timer = new AnimationTimer() {
            @Override public void handle(final long now) {
                if (now > lastTimerCall + updateInterval) {
                    updateMatrix();
                    lastTimerCall = now;
                }
            }
        };
        pauseInterval = tile.getPauseDuration() * 1_000_000l;
        pauseTimer = new AnimationTimer() {
            @Override public void handle(final long now) {
                if (now > lastTimerCall + pauseInterval) {
                    pauseTimer.stop();
                    if (tile.isAnimated()) { timer.start(); }
                }
            }
        };

        getPane().getChildren().addAll(titleText, matrix, text);

        if (tile.isAnimated() && tile.getMatrixIcons().size() > 1) { timer.start(); }
    }

    @Override protected void registerListeners() {
        super.registerListeners();
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if (TileEvt.VISIBILITY.getName().equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !tile.getTitle().isEmpty());
            Helper.enableNode(text, tile.isTextVisible());
        } else if (TileEvt.RECALC.getName().equals(EVENT_TYPE)) {
            matrix.setColsAndRows(tile.getMatrixSize());
            resize();
        } else if (TileEvt.ANIMATED_ON.getName().equals(EVENT_TYPE)) {
            updateInterval = tile.getAnimationDuration() * 1_000_000l;
            pauseInterval  = tile.getPauseDuration() * 1_000_000l;
            if (tile.getMatrixIcons().size() > 1) {
                timer.start();
            }
        } else if (TileEvt.ANIMATED_OFF.getName().equals(EVENT_TYPE)) {
            timer.stop();
            updateMatrix();
        }
    }

    @Override public void dispose() {
        matrix.dispose();
        super.dispose();
    }

    private void updateMatrix() {
        matrix.setAllPixelsOff();
        if (!tile.getMatrixIcons().isEmpty()) {
            MatrixIcon matrixIcon = tile.getMatrixIcons().get(iconCounter);
            for (int y = 0 ; y < 8 ; y++) {
                for (int x = 0 ; x < 8 ; x++) {
                    matrix.setPixel(x, y, matrixIcon.getMatrix()[x][y].getColor());
                }
            }
            iconCounter++;
            if (iconCounter > tile.getMatrixIcons().size() - 1) {
                iconCounter = 0;
                timer.stop();
                pauseTimer.start();
            }
        }
        matrix.drawMatrix();
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

        double chartWidth  = contentBounds.getWidth();
        double chartHeight = contentBounds.getHeight();

        if (tile.isShowing() && width > 0 && height > 0) {
            pane.setMaxSize(width, height);
            pane.setPrefSize(width, height);

            matrix.setPrefSize(chartWidth, chartHeight);
            matrix.relocate((width - chartWidth) * 0.5, contentBounds.getY() + (contentBounds.getHeight() - chartHeight) * 0.5);

            resizeStaticText();
        }
    }

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(tile.getTitle());
        text.setText(tile.getText());

        resizeStaticText();

        titleText.setFill(tile.getTitleColor());
        text.setFill(tile.getTextColor());

        matrix.setPixelOnColor(tile.getBarColor());
        matrix.setPixelOffColor(Helper.isDark(tile.getBackgroundColor()) ? tile.getBackgroundColor().brighter() : tile.getBackgroundColor().darker());
        updateMatrix();
    }
}
