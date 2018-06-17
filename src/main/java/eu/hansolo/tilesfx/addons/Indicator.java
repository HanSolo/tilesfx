/*
 * Copyright (c) 2018 by Gerrit Grunwald
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

package eu.hansolo.tilesfx.addons;

import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.events.IndicatorEvent;
import javafx.beans.DefaultProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;


/**
 * User: hansolo
 * Date: 17.06.18
 * Time: 18:17
 */
@DefaultProperty("children")
public class Indicator extends Region {
    private static final IndicatorEvent        INDICATOR_ON     = new IndicatorEvent(IndicatorEvent.INDICATOR_ON);
    private static final IndicatorEvent        INDICATOR_OFF    = new IndicatorEvent(IndicatorEvent.INDICATOR_OFF);
    private static final double                PREFERRED_WIDTH  = 64;
    private static final double                PREFERRED_HEIGHT = 64;
    private static final double                MINIMUM_WIDTH    = 16;
    private static final double                MINIMUM_HEIGHT   = 16;
    private static final double                MAXIMUM_WIDTH    = 1024;
    private static final double                MAXIMUM_HEIGHT   = 1024;
    private              double                size;
    private              double                width;
    private              double                height;
    private              Circle                ring;
    private              Circle                dot;
    private              Color                 _ringColor;
    private              ObjectProperty<Color> ringColor;
    private              Color                 _dotColor;
    private              ObjectProperty<Color> dotColor;
    private              boolean               _on;
    private              BooleanProperty       on;
    private              Pane                  pane;


    // ******************** Constructors **************************************
    public Indicator() {
        this(Tile.FOREGROUND, Tile.RED);
    }
    public Indicator(final Color DOT_COLOR) {
        this(Tile.FOREGROUND, DOT_COLOR);
    }
    public Indicator(final Color RING_COLOR, final Color DOT_COLOR) {
        getStylesheets().add(Indicator.class.getResource("indicator.css").toExternalForm());
        _ringColor = RING_COLOR;
        _dotColor  = DOT_COLOR;
        _on        = false;
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

        getStyleClass().add("indicator");

        ring = new Circle(PREFERRED_WIDTH * 0.5);
        ring.setStrokeType(StrokeType.INSIDE);
        ring.setStrokeWidth(PREFERRED_WIDTH * 0.078125);
        ring.setStroke(getRingColor());
        ring.setFill(Color.TRANSPARENT);

        dot = new Circle(PREFERRED_WIDTH * 0.3125);
        dot.setFill(getDotColor());

        pane = new Pane(ring, dot);

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        widthProperty().addListener(o -> resize());
        heightProperty().addListener(o -> resize());
        // add listeners to your propertes like
        //value.addListener(o -> handleControlPropertyChanged("VALUE"));
    }

    public Color getRingColor() { return null == ringColor ? _ringColor : ringColor.get(); }
    public void setRingColor(final Color COLOR) {
        if (null == ringColor) {
            _ringColor = COLOR;
            redraw();
        } else {
            ringColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> ringColorProperty() {
        if (null == ringColor) {
            ringColor = new ObjectPropertyBase<Color>(_ringColor) {
                @Override public Object getBean() { return Indicator.this; }
                @Override public String getName() { return "ringColor"; }
            };
            _ringColor = null;
        }
        return ringColor;
    }
    
    public Color getDotColor() { return null == dotColor ? _dotColor : dotColor.get(); }
    public void setDotColor(final Color COLOR) {
        if (null == dotColor) {
            _dotColor = COLOR;
            redraw();
        } else {
            dotColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> dotColorProperty() {
        if (null == dotColor) {
            dotColor = new ObjectPropertyBase<Color>(_dotColor) {
                @Override public Object getBean() { return Indicator.this; }
                @Override public String getName() { return "dotColor"; }
            };
            _dotColor = null;
        }
        return dotColor;
    }

    public boolean isOn() { return null == on ? _on : on.get(); }
    public void setOn(final boolean ON) {
        if (null == on) {
            _on = ON;
            fireEvent(_on ? INDICATOR_ON : INDICATOR_OFF);
            redraw();
        } else {
            on.set(ON);
        }
    }
    public BooleanProperty onProperty() {
        if (null == on) {
            on = new BooleanPropertyBase(_on) {
                @Override protected void invalidated() { fireEvent(get() ? INDICATOR_ON : INDICATOR_OFF); }
                @Override public Object getBean() { return Indicator.this; }
                @Override public String getName() { return "on"; }
            };
        }
        return on;
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

    @Override public ObservableList<Node> getChildren() { return super.getChildren(); }


    // ******************** Resizing ******************************************
    private void resize() {
        width  = getWidth() - getInsets().getLeft() - getInsets().getRight();
        height = getHeight() - getInsets().getTop() - getInsets().getBottom();
        size   = width < height ? width : height;

        double center = size * 0.5;

        if (width > 0 && height > 0) {
            pane.setMaxSize(size, size);
            pane.setPrefSize(size, size);
            pane.relocate((getWidth() - size) * 0.5, (getHeight() - size) * 0.5);

            ring.setStrokeWidth(size * 0.078125);
            ring.setRadius(center);
            ring.setCenterX(center);
            ring.setCenterY(center);

            dot.setRadius(size * 0.3125);
            dot.setCenterX(center);
            dot.setCenterY(center);

            redraw();
        }
    }

    private void redraw() {
        ring.setStroke(getRingColor());
        dot.setFill(isOn() ? getDotColor() : Color.TRANSPARENT);
    }
}
