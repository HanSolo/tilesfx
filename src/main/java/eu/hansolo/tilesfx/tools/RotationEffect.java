/*
 * Copyright (c) 2020 by Gerrit Grunwald
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

import javafx.animation.AnimationTimer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ArcType;


public class RotationEffect extends Region {
    private static final double                PREFERRED_WIDTH  = 250;
    private static final double                PREFERRED_HEIGHT = 250;
    private static final double                MINIMUM_WIDTH    = 10;
    private static final double                MINIMUM_HEIGHT   = 10;
    private static final double                MAXIMUM_WIDTH    = 1024;
    private static final double                MAXIMUM_HEIGHT   = 1024;
    private              double                width;
    private              double                height;
    private              double                offsetX;
    private              double                offsetY;
    private              Canvas                canvas;
    private              GraphicsContext       ctx;
    private              double                angle;
    private              long                  lastTimerCall;
    private              AnimationTimer        timer;
    private              boolean               isRunning;
    private              Color                 _color;
    private              ObjectProperty<Color> color;
    private              double                _alpha;
    private              DoubleProperty        alpha;
    private              double                centerX;
    private              double                centerY;
    private              RadialGradient        gradient;


    // ******************** Constructors **************************************
    public RotationEffect() {
        this(Color.WHITE, 0.1, 0.5, 0.5);
    }
    public RotationEffect(final Color color, final double alpha, final double centerX, final double centerY) {
        angle         = 0;
        lastTimerCall = System.nanoTime();
        timer         = new AnimationTimer() {
            @Override
            public void handle(final long now) {
                if (now > lastTimerCall + 20_000_000l) {
                    redraw();
                    lastTimerCall = now;
                }
            }
        };
        isRunning     = false;
        _color        = color;
        _alpha        = alpha;
        gradient      = new RadialGradient(0, 0, 0.5, 0.5, PREFERRED_WIDTH, true, CycleMethod.NO_CYCLE,
                                           new Stop(0.0, getColorWithOpacity(getAlpha())),
                                           new Stop(1.0, Color.TRANSPARENT));
        this.centerX  = centerX;
        this.centerY  = centerY;
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
    @Override protected double computeMinWidth(final double HEIGHT)  { return MINIMUM_WIDTH; }
    @Override protected double computeMinHeight(final double WIDTH)  { return MINIMUM_HEIGHT; }
    @Override protected double computePrefWidth(final double HEIGHT) { return super.computePrefWidth(HEIGHT); }
    @Override protected double computePrefHeight(final double WIDTH) { return super.computePrefHeight(WIDTH); }
    @Override protected double computeMaxWidth(final double HEIGHT)  { return MAXIMUM_WIDTH; }
    @Override protected double computeMaxHeight(final double WIDTH)  { return MAXIMUM_HEIGHT; }

    public void start() {
        timer.start();
        isRunning = true;
    }
    public void stop() {
        timer.stop();
        isRunning = false;
    }

    public Color getColor() { return null == color ? _color : color.get(); }
    public void setColor(final Color color) {
        if (null == this.color) {
            _color = color;
            updateGradient();
            redraw();
        } else {
            this.color.set(color);
        }
    }
    public ObjectProperty<Color> colorProperty() {
        if (null == color) {
            color = new ObjectPropertyBase(_color) {
                @Override protected void invalidated() {
                    updateGradient();
                    redraw();
                }
                @Override public Object getBean() { return RotationEffect.this; }
                @Override public String getName() { return "color"; }
            };
            _color = null;
        }
        return color;
    }

    public double getAlpha() { return null == alpha ? _alpha : alpha.get(); }
    public void setAlpha(final double alpha) {
        if (null == this.alpha) {
            _alpha = clamp(0.0, 1.0, alpha);
            updateGradient();
            redraw();
        } else {
            this.alpha.set(clamp(0.0, 1.0, alpha));
        }
    }
    public DoubleProperty alphaProperty() {
        if (null == alpha) {
            alpha = new DoublePropertyBase(_alpha) {
                @Override protected void invalidated() {
                    set(clamp(0.0, 1.0, get()));
                    updateGradient();
                    redraw();
                }
                @Override public Object getBean() { return RotationEffect.this; }
                @Override public String getName() { return "alpha"; }
            };
        }
        return alpha;
    }

    public double getCenterX() { return centerX; }
    public void setCenterX(final double centerX) {
        this.centerX = clamp(0.0, 1.0, centerX);
        updateGradient();
        redraw();
    }

    public double getCenterY() { return centerY; }
    public void setCenterY(final double centerY) {
        this.centerY = clamp(0.0, 1.0, centerY);
        updateGradient();
        redraw();
    }

    private Color getColorWithOpacity(final double alpha) {
        return Color.color(getColor().getRed(), getColor().getBlue(), getColor().getBlue(), alpha);
    }

    private void updateGradient() {
        gradient = new RadialGradient(0, 0,
                                      offsetX + width * centerX, offsetY + height * centerY, 1024,
                                      false, CycleMethod.NO_CYCLE,
                                      new Stop(0.0, getColorWithOpacity(getAlpha())),
                                      new Stop(1.0, Color.TRANSPARENT));
    }

    private double clamp(final double min, final double max, final double value) {
        if (value < min) { return min; }
        if (value > max) { return max; }
        return value;
    }


    // ******************** Resizing ******************************************
    private void resize() {
        width  = getWidth() - getInsets().getLeft() - getInsets().getRight();
        height = getHeight() - getInsets().getTop() - getInsets().getBottom();

        if (width > 0 && height > 0) {
            boolean wasRunning = isRunning;
            stop();

            offsetX = (getWidth() - width) * 0.5;
            offsetY = (getHeight() - height) * 0.5;

            updateGradient();

            canvas.setWidth(width);
            canvas.setHeight(height);

            if (wasRunning) {
                start();
            } else {
                redraw();
            }
        }
    }

    private void redraw() {
        ctx.clearRect(0, 0, width, height);
        boolean toggle = true;
        double x = -width - width * 0.5 + width * getCenterX();
        double y = -height - height * 0.5 + height * getCenterY();
        double w = 3 * width;
        double h = 3 * height;
        for (int i = 0 ; i < 360 ; i += 15) {
            ctx.setFill(toggle ? gradient : Color.TRANSPARENT);
            ctx.fillArc(x, y, w, h, -(i + angle), 15, ArcType.ROUND);
            toggle ^= true;
        }
        angle += 2;
        if (angle > 360) {
            angle = 0;
        }
    }
}
