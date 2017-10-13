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
import eu.hansolo.tilesfx.events.TileEvent;
import eu.hansolo.tilesfx.events.TileEvent.EventType;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.CtxBounds;
import eu.hansolo.tilesfx.tools.CtxCornerRadii;
import eu.hansolo.tilesfx.tools.Helper;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.VPos;
import javafx.scene.CacheHint;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.List;


public class FlipTileSkin extends TileSkin {
    private final TileEvent       FLIP_FINISHED = new TileEvent(EventType.FLIP_FINISHED);
    private       List<String>    characters;
    private       int             currentSelectionIndex;
    private       int             nextSelectionIndex;
    private       double          flapHeight;
    private       Canvas          upperBackground;
    private       GraphicsContext upperBackgroundCtx;
    private       Canvas          upperBackgroundText;
    private       GraphicsContext upperBackgroundTextCtx;
    private       Canvas          lowerBackground;
    private       GraphicsContext lowerBackgroundCtx;
    private       Canvas          lowerBackgroundText;
    private       GraphicsContext lowerBackgroundTextCtx;
    private       Canvas          flap;
    private       GraphicsContext flapCtx;
    private       Canvas          flapTextFront;
    private       GraphicsContext flapTextFrontCtx;
    private       Canvas          flapTextBack;
    private       GraphicsContext flapTextBackCtx;
    private       Rotate          rotateFlap;
    private       Font            font;
    private       double          centerX;
    private       double          centerY;
    private       Timeline        timeline;


    // ******************** Constructors **************************************
    public FlipTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        timeline              = new Timeline();
        characters            = tile.getCharacterList();
        currentSelectionIndex = 0;
        nextSelectionIndex    = 1;

        centerX    = PREFERRED_WIDTH * 0.5;
        centerY    = PREFERRED_HEIGHT * 0.5;

        pane.setBackground(null);
        pane.setBorder(null);

        rotateFlap = new Rotate();
        rotateFlap.setAxis(Rotate.X_AXIS);
        rotateFlap.setAngle(0);

        flapHeight = PREFERRED_HEIGHT * 0.495;

        upperBackground    = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT * 0.495);
        upperBackgroundCtx = upperBackground.getGraphicsContext2D();

        upperBackgroundText    = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT * 0.495);
        upperBackgroundTextCtx = upperBackgroundText.getGraphicsContext2D();
        upperBackgroundTextCtx.setTextBaseline(VPos.CENTER);
        upperBackgroundTextCtx.setTextAlign(TextAlignment.CENTER);

        lowerBackground    = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT * 0.495);
        lowerBackgroundCtx = lowerBackground.getGraphicsContext2D();

        lowerBackgroundText    = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT * 0.495);
        lowerBackgroundTextCtx = lowerBackgroundText.getGraphicsContext2D();
        lowerBackgroundTextCtx.setTextBaseline(VPos.CENTER);
        lowerBackgroundTextCtx.setTextAlign(TextAlignment.CENTER);

        flap = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT * 0.495);
        flap.getTransforms().add(rotateFlap);
        flapCtx = flap.getGraphicsContext2D();

        flapTextFront = new Canvas();
        flapTextFront.getTransforms().add(rotateFlap);
        flapTextFrontCtx = flapTextFront.getGraphicsContext2D();
        flapTextFrontCtx.setTextBaseline(VPos.CENTER);
        flapTextFrontCtx.setTextAlign(TextAlignment.CENTER);

        flapTextBack  = new Canvas();
        flapTextBack.getTransforms().add(rotateFlap);
        flapTextBack.setOpacity(0);
        flapTextBackCtx = flapTextBack.getGraphicsContext2D();
        flapTextBackCtx.setTextBaseline(VPos.CENTER);
        flapTextBackCtx.setTextAlign(TextAlignment.CENTER);

        pane.getChildren().addAll(upperBackground,
                                  lowerBackground,
                                  upperBackgroundText,
                                  lowerBackgroundText,
                                  flap,
                                  flapTextFront,
                                  flapTextBack);
    }

    @Override protected void registerListeners() {
        super.registerListeners();

        rotateFlap.angleProperty().addListener((o, ov, nv) -> {
            if (nv.doubleValue() > 90) {
                flapTextFront.setOpacity(0);
                flapTextBack.setOpacity(1);
            }
        });

        timeline.setOnFinished(EVENT -> {
            tile.fireTileEvent(FLIP_FINISHED);
            if (Double.compare(rotateFlap.getAngle(), 180) == 0) {
                rotateFlap.setAngle(0);
                flapTextBack.setOpacity(0);
                flapTextFront.setOpacity(1);
                drawText();
                if (!tile.getFlipText().equals(characters.get(currentSelectionIndex))) { flipForward(); }
            } else if(Double.compare(rotateFlap.getAngle(), 0) == 0) {
                rotateFlap.setAngle(180);
            }
        });
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);
        if (EVENT_TYPE.equals("FLIP_START")) {
            flipForward();
        }
    }

    private void flipForward() {
        timeline.stop();

        flap.setCache(true);
        flap.setCacheHint(CacheHint.ROTATE);
        //flap.setCacheHint(CacheHint.SPEED);

        currentSelectionIndex++;
        if (currentSelectionIndex >= characters.size()) {
            currentSelectionIndex = 0;
        }
        nextSelectionIndex = currentSelectionIndex + 1;
        if (nextSelectionIndex >= characters.size()) {
            nextSelectionIndex = 0;
        }
        KeyValue keyValueFlap = new KeyValue(rotateFlap.angleProperty(), 180, Interpolator.SPLINE(0.5, 0.4, 0.4, 1.0));
        //KeyValue keyValueFlap = new KeyValue(rotateFlap.angleProperty(), 180, Interpolator.EASE_IN);
        KeyFrame keyFrame     = new KeyFrame(Duration.millis(tile.getFlipTimeInMS()), keyValueFlap);
        timeline.getKeyFrames().setAll(keyFrame);
        timeline.play();
    }

    private void drawFlaps() {
        double cornerRadius = tile.getRoundedCorners() ? size * 0.025 : 0;

        // Upper Background
        upperBackground.setCache(false);
        upperBackgroundCtx.clearRect(0, 0, width, flapHeight);
        Helper.drawRoundedRect(upperBackgroundCtx, new CtxBounds(0, 0, width, flapHeight), new CtxCornerRadii(cornerRadius, cornerRadius, 0, 0));
        upperBackgroundCtx.setFill(tile.getBackgroundColor());
        upperBackgroundCtx.fill();
        upperBackground.setCache(true);
        upperBackground.setCacheHint(CacheHint.SPEED);

        // Lower Background
        lowerBackground.setCache(false);
        lowerBackgroundCtx.clearRect(0, 0, width, flapHeight);
        Helper.drawRoundedRect(lowerBackgroundCtx, new CtxBounds(0, 0, width, flapHeight), new CtxCornerRadii(0, 0, cornerRadius, cornerRadius));
        lowerBackgroundCtx.setFill(tile.getBackgroundColor());
        lowerBackgroundCtx.fill();
        lowerBackground.setCache(true);
        lowerBackground.setCacheHint(CacheHint.SPEED);

        // Flap
        flap.setCache(false);
        flapCtx.clearRect(0, 0, width, flapHeight);
        Helper.drawRoundedRect(flapCtx, new CtxBounds(0, 0, width, flapHeight), new CtxCornerRadii(cornerRadius, cornerRadius, 0, 0));
        flapCtx.setFill(tile.getBackgroundColor());
        flapCtx.fill();
        flap.setCache(true);
        flap.setCacheHint(CacheHint.SPEED);
    }

    private void drawText() {
        if (characters.isEmpty()) { return; }
        final String CURRENT_TEXT = characters.get(currentSelectionIndex);
        final String NEXT_TEXT    = characters.get(nextSelectionIndex);
        final Color  TEXT_COLOR   = tile.getForegroundColor();

        // set the text on the upper background
        upperBackgroundTextCtx.clearRect(0, 0, width, flapHeight);
        upperBackgroundTextCtx.setFill(TEXT_COLOR);
        upperBackgroundTextCtx.fillText(NEXT_TEXT, centerX, centerY, width);

        // set the text on the lower background
        lowerBackgroundTextCtx.clearRect(0, 0, width, flapHeight);
        lowerBackgroundTextCtx.setFill(TEXT_COLOR);
        lowerBackgroundTextCtx.fillText(CURRENT_TEXT, centerX, 0, width);

        // set the text on the flap front
        flapTextFrontCtx.clearRect(0, 0, width, flapHeight);
        flapTextFrontCtx.setFill(TEXT_COLOR);
        flapTextFrontCtx.fillText(CURRENT_TEXT, centerX, centerY, width);

        // set the text on the flap back
        flapTextBackCtx.clearRect(0, 0, width, flapHeight);
        flapTextBackCtx.save();
        flapTextBackCtx.scale(1,-1);
        flapTextBackCtx.setFill(TEXT_COLOR);
        flapTextBackCtx.fillText(NEXT_TEXT, centerX, -centerY, width);
        flapTextBackCtx.restore();
    }


    // ******************** Resizing ******************************************
    @Override protected void resize() {
        super.resize();
        centerX    = width * 0.5;
        centerY    = height * 0.5;
        flapHeight = height * 0.495;

        upperBackground.setWidth(width);
        upperBackground.setHeight(flapHeight);

        lowerBackground.setWidth(width);
        lowerBackground.setHeight(flapHeight);
        lowerBackground.setTranslateY(height - flapHeight);

        upperBackgroundText.setWidth(width);
        upperBackgroundText.setHeight(flapHeight);

        lowerBackgroundText.setWidth(width);
        lowerBackgroundText.setHeight(flapHeight);
        lowerBackgroundText.setTranslateY(height - flapHeight);

        flap.setWidth(width);
        flap.setHeight(flapHeight);
        rotateFlap.setPivotY(centerY);

        flapTextFront.setWidth(width);
        flapTextFront.setHeight(flapHeight);

        flapTextBack.setWidth(width);
        flapTextBack.setHeight(flapHeight);

        font = Fonts.latoBold(height * 0.75);

        upperBackgroundTextCtx.setFont(font);
        lowerBackgroundTextCtx.setFont(font);
        flapTextFrontCtx.setFont(font);
        flapTextBackCtx.setFont(font);

        redraw();
    }

    @Override protected void redraw() {
        drawFlaps();
        drawText();
    }
}
