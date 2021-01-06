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
package eu.hansolo.tilesfx.addons;

import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.Helper;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.Locale;


public class HappinessIndicator extends Region {
    public enum Happiness {
        UNHAPPY, NEUTRAL, HAPPY;
    }

    private static final double                    PREFERRED_WIDTH  = 64;
    private static final double                    PREFERRED_HEIGHT = 64;
    private static final double                    MINIMUM_WIDTH    = 16;
    private static final double                    MINIMUM_HEIGHT   = 16;
    private static final double                    MAXIMUM_WIDTH    = 1024;
    private static final double                    MAXIMUM_HEIGHT   = 1024;
    private              double                    size;
    private              double                    width;
    private              double                    height;
    private              Text                      text;
    private              Canvas                    canvas;
    private              GraphicsContext           ctx;
    private              ObjectProperty<Happiness> happiness;
    private              DoubleProperty            value;
    private              ObjectProperty<Color>     barBackgroundColor;
    private              ObjectProperty<Color>     textColor;
    private              BooleanProperty           textVisible;
    private              ObjectProperty<Color>     happyColor;
    private              ObjectProperty<Color>     neutralColor;
    private              ObjectProperty<Color>     unhappyColor;


    // ******************** Constructors **************************************
    public HappinessIndicator() {
        this(Happiness.HAPPY, 0);
    }
    public HappinessIndicator(final Happiness HAPPINESS) {
        this(HAPPINESS, 0);
    }
    public HappinessIndicator(final Happiness HAPPINESS, final double VALUE) {
        happiness          = new ObjectPropertyBase<>(HAPPINESS) {
            @Override protected void invalidated() { redraw(); }
            @Override public Object getBean() { return HappinessIndicator.this; }
            @Override public String getName() { return "happiness"; }
        };
        value              = new DoublePropertyBase(VALUE) {
            @Override protected void invalidated() {
                double value = Helper.clamp(0, 1, get());
                set(value);
                text.setText(String.format(Locale.US, "%.0f%%", value * 100.0));
                redraw();
            }
            @Override public Object getBean() { return HappinessIndicator.this; }
            @Override public String getName() { return "value"; }
        };
        barBackgroundColor = new ObjectPropertyBase<>(Tile.BACKGROUND.brighter()) {
            @Override protected void invalidated() { redraw(); }
            @Override public Object getBean() { return HappinessIndicator.this; }
            @Override public String getName() { return "barBackgroundColor"; }
        };
        textColor          = new ObjectPropertyBase<>(Tile.FOREGROUND) {
            @Override protected void invalidated() { text.setFill(get()); }
            @Override public Object getBean() { return HappinessIndicator.this; }
            @Override public String getName() { return "textColor"; }
        };
        textVisible        = new BooleanPropertyBase(true) {
            @Override protected void invalidated() {
                Helper.enableNode(text, get());
                redraw();
            }
            @Override public Object getBean() { return HappinessIndicator.this; }
            @Override public String getName() { return "textVisible"; }
        };
        happyColor         = new ObjectPropertyBase<>(Tile.GREEN) {
            @Override protected void invalidated() { redraw(); }
            @Override public Object getBean() { return HappinessIndicator.this; }
            @Override public String getName() { return "happyColor"; }
        };
        neutralColor       = new ObjectPropertyBase<>(Tile.YELLOW_ORANGE) {
            @Override protected void invalidated() { redraw(); }
            @Override public Object getBean() { return HappinessIndicator.this; }
            @Override public String getName() { return "neutralColor"; }
        };
        unhappyColor       = new ObjectPropertyBase<>(Tile.RED) {
            @Override protected void invalidated() { redraw(); }
            @Override public Object getBean() { return HappinessIndicator.this; }
            @Override public String getName() { return "unhappyColor"; }
        };
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

        text   = new Text(String.format(Locale.US, "%.0f%%", getValue() * 100.0));
        text.setTextAlignment(TextAlignment.CENTER);
        text.setTextOrigin(VPos.TOP);
        text.setFill(getTextColor());
        Helper.enableNode(text, getTextVisible());

        getChildren().setAll(canvas, text);
    }

    private void registerListeners() {
        widthProperty().addListener(o -> resize());
        heightProperty().addListener(o -> resize());
    }

    public double getValue() { return value.get(); }
    public void setValue(final double VALUE) { value.set(VALUE); }
    public DoubleProperty valueProperty() { return value; }

    public Color getBarBackgroundColor() { return barBackgroundColor.get(); }
    public void setBarBackgroundColor(final Color COLOR) { barBackgroundColor.set(COLOR); }
    public ObjectProperty<Color> barBackgroundColorProperty() { return barBackgroundColor; }

    public Color getTextColor() { return textColor.get(); }
    public void setTextColor(final Color COLOR) { textColor.set(COLOR); }
    public ObjectProperty<Color> textColorProperty() { return textColor; }

    public boolean getTextVisible() { return textVisible.get(); }
    public void setTextVisible(final boolean VISIBLE) { textVisible.set(VISIBLE); }
    public BooleanProperty textVisibleProperty() { return textVisible; }

    public Happiness getHappiness() { return happiness.get(); }
    public void setHappiness(final Happiness HAPPINESS) { happiness.set(HAPPINESS); }
    public ObjectProperty<Happiness> happinessProperty() { return happiness; }

    public Color getHappyColor() { return happyColor.get(); }
    public void setHappyColor(final Color COLOR) { happyColor.set(COLOR); }
    public ObjectProperty<Color> happyColorProperty() { return happyColor; }

    public Color getNeutralColor() { return neutralColor.get(); }
    public void setNeutralColor(final Color COLOR) { neutralColor.set(COLOR); }
    public ObjectProperty<Color> neutralColorProperty() { return neutralColor; }

    public Color getUnhappyColor() { return unhappyColor.get(); }
    public void setUnhappyColor(final Color COLOR) { unhappyColor.set(COLOR); }
    public ObjectProperty<Color> unhappyColorProperty() { return unhappyColor; }


    // ******************** Methods *******************************************
    @Override protected double computeMinWidth(final double HEIGHT) { return MINIMUM_WIDTH; }
    @Override protected double computeMinHeight(final double WIDTH) { return MINIMUM_HEIGHT; }
    @Override protected double computePrefWidth(final double HEIGHT) { return super.computePrefWidth(HEIGHT); }
    @Override protected double computePrefHeight(final double WIDTH) { return super.computePrefHeight(WIDTH); }
    @Override protected double computeMaxWidth(final double HEIGHT) { return MAXIMUM_WIDTH; }
    @Override protected double computeMaxHeight(final double WIDTH) { return MAXIMUM_HEIGHT; }


    // ******************** Resizing ******************************************
    private void resize() {
        width  = getWidth() - getInsets().getLeft() - getInsets().getRight();
        height = getHeight() - getInsets().getTop() - getInsets().getBottom();
        size   = width < height ? width : height;

        if (width > 0 && height > 0) {
            canvas.setWidth(size);
            canvas.setHeight(size);
            canvas.relocate((getWidth() - size) * 0.5, (getHeight() - size) * 0.5);

            text.setFont(Fonts.latoBold(size * 0.25));
            text.relocate((getWidth() - text.getLayoutBounds().getWidth()) * 0.5, height * 0.5 - size * 0.54);

            redraw();
        }
    }

    private void redraw() {
        double scaling = getTextVisible() ? 0.7 : 1.0;
        double offsetY = getTextVisible() ? size * 0.15 : 0;

        double circleSize   = size * 0.9 * scaling;
        double circleOffset = (size - circleSize) / 2;

        ctx.clearRect(0, 0, width, height);
        ctx.setLineCap(StrokeLineCap.ROUND);

        ctx.setStroke(getBarBackgroundColor());
        ctx.setLineWidth(size * 0.08823529 * scaling);
        ctx.strokeOval(circleOffset, circleOffset + offsetY, circleSize, circleSize);

        Color currentColor = Color.TRANSPARENT;

        ctx.save();
        ctx.translate(offsetY, offsetY + offsetY);

        ctx.setLineWidth(size * 0.05882353 * scaling);
        switch(getHappiness()) {
            case HAPPY:
                currentColor = getHappyColor();
                ctx.setStroke(currentColor);
                ctx.beginPath();
                ctx.moveTo(size * 0.26470588 * scaling, size * 0.59411765 * scaling);
                ctx.bezierCurveTo(size * 0.26470588 * scaling, size * 0.59411765 * scaling, size * 0.33047353 * scaling,size * 0.73529412 * scaling, size * 0.5 * scaling,size * 0.73529412 * scaling );
                ctx.bezierCurveTo(size * 0.67506176 * scaling,size * 0.73529412 * scaling, size * 0.73529412 * scaling,size * 0.59411765 * scaling, size * 0.73529412 * scaling,size * 0.59411765 * scaling);
                ctx.stroke();
                break;
            case NEUTRAL:
                currentColor = getNeutralColor();
                ctx.setStroke(currentColor);
                ctx.strokeLine(size * 0.26470588 * scaling,size * 0.65294118 * scaling, size * 0.73529412 * scaling,size * 0.65294118 * scaling);
                break;
            case UNHAPPY:
                currentColor = getUnhappyColor();
                ctx.setStroke(currentColor);
                ctx.beginPath();
                ctx.moveTo(size * 0.26470588 * scaling, size * 0.70588235 * scaling);
                ctx.bezierCurveTo(size * 0.26470588 * scaling, size * 0.70588235 * scaling, size * 0.33047353 * scaling,size * 0.56803529 * scaling, size * 0.5 * scaling,size * 0.56803529 * scaling);
                ctx.bezierCurveTo(size * 0.67506176 * scaling,size * 0.56803529 * scaling, size * 0.73529412 * scaling,size * 0.70588235 * scaling, size * 0.73529412 * scaling,size * 0.70588235 * scaling);
                ctx.stroke();
                break;
        }
        ctx.setFill(currentColor);
        ctx.fillOval(size * 0.26470588 * scaling, size * 0.3 * scaling, size * 0.14705882 * scaling, size * 0.14705882 * scaling);
        ctx.fillOval(size * 0.58823529 * scaling, size * 0.3 * scaling, size * 0.14705882 * scaling, size * 0.14705882 * scaling);

        ctx.restore();

        ctx.setStroke(currentColor);
        ctx.setLineWidth(size * 0.08823529 * scaling);
        ctx.strokeArc(circleOffset, circleOffset + offsetY, circleSize, circleSize, 90, -(getValue() * 360.0), ArcType.OPEN);
    }
}
