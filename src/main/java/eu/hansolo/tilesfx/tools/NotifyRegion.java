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

package eu.hansolo.tilesfx.tools;

import eu.hansolo.tilesfx.Tile;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;


/**
 * User: hansolo
 * Date: 08.11.17
 * Time: 04:37
 */
public class NotifyRegion extends Region {
    private static final double          PREFERRED_WIDTH  = 52;
    private static final double          PREFERRED_HEIGHT = 52;
    private static final double          MINIMUM_WIDTH    = 1;
    private static final double          MINIMUM_HEIGHT   = 1;
    private static final double          MAXIMUM_WIDTH    = 1024;
    private static final double          MAXIMUM_HEIGHT   = 1024;
    private              double          size;
    private              double          width;
    private              double          height;
    private              Canvas          canvas;
    private              GraphicsContext ctx;
    private              Color           backgroundColor;
    private              Color           foregroundColor;
    private              boolean         roundedCorner;


    // ******************** Constructors **************************************
    public NotifyRegion() {
        backgroundColor = Tile.YELLOW;
        foregroundColor = Tile.BACKGROUND;
        roundedCorner   = true;
        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void initGraphics() {
        if (Double.compare(getPrefWidth(), 0.0) <= 0 || Double.compare(getPrefHeight(), 0.0) <= 0 || Double.compare(getWidth(), 0.0) <= 0 ||
            Double.compare(getHeight(), 0.0) <= 0) {
            if (getPrefWidth() > 0 && getPrefHeight() > 0) {
                setPrefSize(getPrefWidth(), getPrefHeight());
            } else {
                setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            }
        }

        canvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        ctx    = canvas.getGraphicsContext2D();

        getChildren().setAll(canvas);
    }

    private void registerListeners() {
        widthProperty().addListener(o -> resize());
        heightProperty().addListener(o -> resize());
    }


    // ******************** Methods *******************************************
    @Override public void layoutChildren() {
        super.layoutChildren();
    }

    @Override protected double computeMinWidth(final double HEIGHT) { return MINIMUM_WIDTH; }
    @Override protected double computeMinHeight(final double WIDTH) { return MINIMUM_HEIGHT; }
    @Override protected double computePrefWidth(final double HEIGHT) { return super.computePrefWidth(HEIGHT); }
    @Override protected double computePrefHeight(final double WIDTH) { return super.computePrefHeight(WIDTH); }
    @Override protected double computeMaxWidth(final double HEIGHT) { return MAXIMUM_WIDTH; }
    @Override protected double computeMaxHeight(final double WIDTH) { return MAXIMUM_HEIGHT; }

    public Color getBackgroundColor() { return backgroundColor; }
    public void setBackgroundColor(final Color COLOR) {
        backgroundColor = COLOR;
        redraw();
    }

    public Color getForegroundColor() { return foregroundColor; }
    public void setForegroundColor(final Color COLOR) {
        foregroundColor = COLOR;
        redraw();
    }

    public boolean isRoundedCorner() { return roundedCorner; }
    public void setRoundedCorner(final boolean ROUNDED) {
        roundedCorner = ROUNDED;
        redraw();
    }

    @Override public ObservableList<Node> getChildren() { return super.getChildren(); }


    // ******************** Resizing ******************************************
    private void resize() {
        width  = getWidth() - getInsets().getLeft() - getInsets().getRight();
        height = getHeight() - getInsets().getTop() - getInsets().getBottom();
        size   = width < height ? width : height;

        if (width > 0 && height > 0) {
            canvas.setWidth(size);
            canvas.setHeight(size);
            canvas.relocate((getWidth() - size) * 0.5, (getHeight() - size) * 0.5);
            redraw();
        }
    }

    private void redraw() {
        ctx.clearRect(0, 0, size, size);
        ctx.beginPath();
        if (isRoundedCorner()) {
            ctx.moveTo(0, 0);
            ctx.lineTo(size - size * 0.19230769, 0);
            ctx.quadraticCurveTo(size, 0, size, size * 0.19230769);
            ctx.lineTo(size, size * 0.19230769);
            ctx.lineTo(size, size);
            ctx.closePath();
        } else {
            ctx.moveTo(0, 0);
            ctx.lineTo(size, 0);
            ctx.lineTo(size, size);
            ctx.closePath();
        }
        ctx.setFill(getBackgroundColor());
        ctx.fill();

        ctx.save();
        ctx.setFill(getForegroundColor());
        ctx.translate(size * 0.80769231,size * 0.13461538);
        ctx.rotate( 45 );
        ctx.fillRect(0, 0, size * 0.07692308, size * 0.25);
        ctx.fillRect(0, size * 0.28846154, size * 0.07692308, size * 0.07692308);
        ctx.restore();
    }
}
