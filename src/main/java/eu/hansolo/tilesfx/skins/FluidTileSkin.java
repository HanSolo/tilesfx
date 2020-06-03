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

package eu.hansolo.tilesfx.skins;

import eu.hansolo.tilesfx.Section;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.colors.ColorSkin;
import eu.hansolo.tilesfx.events.TileEvent.EventType;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.GradientLookup;
import eu.hansolo.tilesfx.tools.Helper;
import javafx.animation.AnimationTimer;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

import static eu.hansolo.tilesfx.tools.Helper.clamp;


public class FluidTileSkin extends TileSkin {
    private Canvas          canvas;
    private GraphicsContext ctx;
    private Text            titleText;
    private Text            valueText;
    private Text            upperUnitText;
    private Line            fractionLine;
    private Text            unitText;
    private VBox            unitFlow;
    private HBox            valueUnitFlow;
    private Text            text;
    private GradientLookup  gradientLookup;
    private List<Point>     particles;
    private double          density;
    private double          friction;
    private double          detail;
    private long            impulseInterval;
    private long            updateInterval;
    private long            lastUpdateCall;
    private long            lastImpulseCall;
    private AnimationTimer  timer;


    // ******************** Constructors **************************************
    public FluidTileSkin(final Tile TILE) {
        super(TILE);
        timer.start();
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        density         = 0.9; //0.75;
        friction        = 1.1; //1.14;
        detail          = Math.round(PREFERRED_WIDTH / 20); // no of particles used to build up the wave
        particles       = new ArrayList<>();
        impulseInterval = 1_000_000_000l;  // Interval between random impulses being inserted into the wave to keep it moving
        updateInterval  = 50_000_000l;     // Wave update interval
        lastUpdateCall  = System.nanoTime();
        lastImpulseCall = System.nanoTime();
        timer           = new AnimationTimer() {
            @Override public void handle(long now) {
                if (now > lastUpdateCall + updateInterval) {
                    update();
                    lastUpdateCall = now;
                }
                if (now > lastImpulseCall + impulseInterval) {
                    impulse();
                    lastImpulseCall = now;
                }
            }
        };

        // Create wave particles
        for( int i = 0 ; i < detail + 1 ; i++ ) {
            particles.add(new Point(PREFERRED_WIDTH / (detail - 4) * (i - 2), PREFERRED_HEIGHT * (1d - tile.getCurrentValue()),
                                    0, PREFERRED_HEIGHT * (1d - tile.getCurrentValue()),
                                    0, Math.random() * 3,
                                    0, 0,
                                    20));
        }

        canvas = new Canvas();
        ctx    = canvas.getGraphicsContext2D();

        gradientLookup = new GradientLookup();

        if (tile.getSections().isEmpty()) {
            tile.setSections(new Section(0.00, 0.25, ColorSkin.GREEN),
                             new Section(0.25, 0.50, ColorSkin.YELLOW),
                             new Section(0.50, 0.75, ColorSkin.ORANGE),
                             new Section(0.75, 1.00, ColorSkin.RED));
        }

        titleText = new Text();
        titleText.setFill(tile.getTitleColor());
        Helper.enableNode(titleText, !tile.getTitle().isEmpty());

        valueText = new Text(String.format(locale, formatString, ((tile.getValue() - minValue) / range * 100)));
        valueText.setFill(tile.getValueColor());
        valueText.setTextOrigin(VPos.BASELINE);
        valueText.setStroke(tile.getBackgroundColor());
        Helper.enableNode(valueText, tile.isValueVisible());

        upperUnitText = new Text("");
        upperUnitText.setFill(tile.getUnitColor());
        Helper.enableNode(upperUnitText, !tile.getUnit().isEmpty());

        fractionLine = new Line();

        unitText = new Text(tile.getUnit());
        unitText.setFill(tile.getUnitColor());
        Helper.enableNode(unitText, !tile.getUnit().isEmpty());

        unitFlow = new VBox(upperUnitText, unitText);
        unitFlow.setAlignment(Pos.CENTER_RIGHT);

        valueUnitFlow = new HBox(valueText, unitFlow);
        valueUnitFlow.setAlignment(Pos.CENTER);
        valueUnitFlow.setMouseTransparent(true);

        text = new Text(tile.getText());
        text.setFill(tile.getUnitColor());
        Helper.enableNode(text, tile.isTextVisible());

        getPane().getChildren().addAll(canvas, titleText, valueUnitFlow, fractionLine, text);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if (EventType.VALUE.name().equals(EVENT_TYPE)) {
            handleCurrentValue(tile.getCurrentValue());
        } else if (EventType.VISIBILITY.name().equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !tile.getTitle().isEmpty());
            Helper.enableNode(text, tile.isTextVisible());
        } else if (EventType.SECTION.name().equals(EVENT_TYPE)) {
            redraw();
        }
    }

    @Override protected void handleCurrentValue(final double VALUE) {
        double percentage = VALUE / (tile.getRange());
        if (tile.getCustomDecimalFormatEnabled()) {
            valueText.setText(decimalFormat.format(percentage));
        } else {
            valueText.setText(String.format(locale, formatString, percentage));
        }

        if (tile.isFillWithGradient()) {
            ctx.setFill(gradientLookup.getColorAt(percentage));
        } else {
            for (Section section : tile.getSections()) {
                if (section.contains(percentage)) {
                    ctx.setFill(section.getColor());
                    break;
                }
            }
        }

        resizeDynamicText();

        Point p;
        for( int i = 0 ; i < detail + 1 ; i++ ) {
            p = particles.get(i);
            p.y = size * (1d - percentage);
            p.originalY = p.y;
        }
    }


    // ******************** Resizing ******************************************
    @Override protected void resizeDynamicText() {
        double maxWidth = unitText.isVisible() ? width - size * 0.275 : width - size * 0.1;
        double fontSize = size * 0.48;
        valueText.setFont(Fonts.latoBold(fontSize));
        if (valueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(valueText, maxWidth, fontSize); }
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

        maxWidth = width - (width - size * 0.275);
        fontSize = upperUnitText.getText().isEmpty() ? size * 0.24 : size * 0.20;
        upperUnitText.setFont(Fonts.latoRegular(fontSize));
        if (upperUnitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(upperUnitText, maxWidth, fontSize); }

        fontSize = upperUnitText.getText().isEmpty() ? size * 0.24 : size * 0.20;
        unitText.setFont(Fonts.latoRegular(fontSize));
        if (unitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(unitText, maxWidth, fontSize); }

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
    }

    @Override protected void resize() {
        super.resize();

        canvas.setWidth(width);
        canvas.setHeight(height);

        Rectangle clip = new Rectangle(width, height);
        if (tile.getRoundedCorners()) {
            clip.setArcWidth(clamp(0, Double.MAX_VALUE, size * 0.025));
            clip.setArcHeight(clamp(0, Double.MAX_VALUE, size * 0.025));
        }
        canvas.setClip(clip);

        resizeDynamicText();
        resizeStaticText();

        valueUnitFlow.setPrefWidth(width - size * 0.1);
        valueUnitFlow.relocate(size * 0.05, (height - valueUnitFlow.getLayoutBounds().getHeight()) * 0.5);
        valueUnitFlow.setMaxHeight(valueText.getFont().getSize());

        fractionLine.setStartX(width - 0.17 * size);
        fractionLine.setStartY(tile.getTitle().isEmpty() ? size * 0.2 : size * 0.3);
        fractionLine.setEndX(width - 0.05 * size);
        fractionLine.setEndY(tile.getTitle().isEmpty() ? size * 0.2 : size * 0.3);
        fractionLine.setStroke(tile.getUnitColor());
        fractionLine.setStrokeWidth(size * 0.005);

        unitFlow.setTranslateY(-size * 0.005);

        for( int i = 0 ; i < detail + 1 ; i++ ) {
            Point p = particles.get(i);
            p.x = width / (detail - 4) * (i - 2);
            p.y = height * (1d - tile.getCurrentValue());

            p.originalX = p.x;
            p.originalY = p.y;
        }
    }

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(tile.getTitle());
        text.setText(tile.getText());

        if (tile.getCustomDecimalFormatEnabled()) {
            valueText.setText(decimalFormat.format(tile.getCurrentValue()));
        } else {
            valueText.setText(String.format(locale, formatString, tile.getCurrentValue()));
        }
        if (tile.getUnit().contains("/")) {
            String[] units = tile.getUnit().split("/");
            upperUnitText.setText(units[0]);
            unitText.setText(units[1]);
            Helper.enableNode(fractionLine, true);
        } else {
            upperUnitText.setText(" ");
            unitText.setText(tile.getUnit());
            Helper.enableNode(fractionLine, false);
        }

        resizeDynamicText();
        resizeStaticText();

        titleText.setFill(tile.getTitleColor());
        valueText.setFill(tile.getValueColor());
        upperUnitText.setFill(tile.getUnitColor());
        fractionLine.setStroke(tile.getUnitColor());
        unitText.setFill(tile.getUnitColor());
        text.setFill(tile.getTextColor());

        if (tile.isFillWithGradient() && !tile.getGradientStops().isEmpty()) {
            gradientLookup.setStops(tile.getGradientStops());
        }
    }

    private void impulse() {
        int forceRange = 2; // -value to +value
        insertImpulse(Math.random() * width, (Math.random() * (forceRange * 2) - forceRange ));
    }

    private void insertImpulse(final double POSITION_X, final double FORCE_Y) {
        int pos = (int) Math.round(POSITION_X / width * particles.size());
        if (pos > particles.size() - 1) return;
        Point particle = particles.get(pos);
        particle.forceY += FORCE_Y;
    }

    private void update() {
        ctx.clearRect(0, 0, width, height);
        ctx.beginPath();
        ctx.moveTo(particles.get(0).x, particles.get(0).y);
        int listSize = particles.size();
        Point currentParticle, previousParticle, nextParticle;
        for(int i = 0; i < listSize; i++) {
            currentParticle  = particles.get(i);
            previousParticle = i - 1 < 0 ? null : particles.get(i - 1);
            nextParticle     = i + 1 > listSize  - 1 ? null : particles.get(i + 1);

            if (null != previousParticle && null != nextParticle) {
                double forceY = 0;
                forceY += -density * (previousParticle.y - currentParticle.y);
                forceY += density * (currentParticle.y - nextParticle.y);
                forceY += density / 15 * (currentParticle.y - currentParticle.originalY);

                currentParticle.velocityY += -(forceY / currentParticle.mass) + currentParticle.forceY;
                currentParticle.velocityY /= friction;
                currentParticle.forceY    /= friction;
                currentParticle.y         += currentParticle.velocityY;

                ctx.quadraticCurveTo(previousParticle.x,
                                     previousParticle.y,
                                     previousParticle.x + (currentParticle.x - previousParticle.x) / 2,
                                     previousParticle.y + (currentParticle.y - previousParticle.y) / 2);
            }
        }

        ctx.lineTo(particles.get(particles.size() - 1).x, particles.get(particles.size() - 1).y);
        ctx.lineTo(width, height);
        ctx.lineTo(0, height);
        ctx.lineTo(particles.get(0).x, particles.get(0).y);
        ctx.closePath();

        ctx.fill();
    }


    // ******************** Inner Classes *************************************
    class Point {
        double x;
        double y;
        double originalX;
        double originalY;
        double velocityX;
        double velocityY;
        double forceX;
        double forceY;
        double mass;


        public Point(final double X, final double Y,
                     final double ORIGINAL_X, final double ORIGINAL_Y,
                     final double VELOCITY_X, final double VELOCITY_Y,
                     final double FORCE_X, final double FORCE_Y,
                     final double MASS) {
            x         = X;
            y         = Y;
            originalX = ORIGINAL_X;
            originalY = ORIGINAL_Y;
            velocityX = VELOCITY_X;
            velocityY = VELOCITY_Y;
            forceX    = FORCE_X;
            forceY    = FORCE_Y;
            mass      = MASS;
        }
    }
}
