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
import eu.hansolo.tilesfx.events.SwitchEvent;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.DefaultProperty;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;


/**
 * User: hansolo
 * Date: 13.06.18
 * Time: 08:35
 */
@DefaultProperty("children")
public class Switch extends Region {
    private static final SwitchEvent              SWITCH_PRESSED  = new SwitchEvent(SwitchEvent.SWITCH_PRESSED);
    private static final SwitchEvent              SWITCH_RELEASED = new SwitchEvent(SwitchEvent.SWITCH_RELEASED);
    private static final double                   PREFERRED_WIDTH  = 110;
    private static final double                   PREFERRED_HEIGHT = 55;
    private static final double                   MINIMUM_WIDTH    = 20;
    private static final double                   MINIMUM_HEIGHT   = 20;
    private static final double                   MAXIMUM_WIDTH    = 1024;
    private static final double                   MAXIMUM_HEIGHT   = 1024;
    private static       double                   aspectRatio;
    private              double                   size;
    private              double                   width;
    private              double                   height;
    private              DropShadow               shadow;
    private              Rectangle                switchBorder;
    private              Rectangle                switchBackground;
    private              Circle                   thumb;
    private              Pane                     pane;
    private              Timeline                 timeline;
    private              EventHandler<MouseEvent> mouseEventHandler;
    private              InvalidationListener     selectedListener;
    private              boolean                  _active;
    private              BooleanProperty          active;
    private              Color                    _activeColor;
    private              ObjectProperty<Color>    activeColor;
    private              Color                    _foregroundColor;
    private              ObjectProperty<Color>    foregroundColor;
    private              Color                    _backgroundColor;
    private              ObjectProperty<Color>    backgroundColor;


    // ******************** Constructors **************************************
    public Switch() {
        getStylesheets().add(Switch.class.getResource("switch.css").toExternalForm());
        aspectRatio      = PREFERRED_HEIGHT / PREFERRED_WIDTH;
        _active          = false;
        _activeColor     = Tile.BLUE;
        _foregroundColor = Tile.FOREGROUND;
        _backgroundColor = Tile.BACKGROUND;

        mouseEventHandler = e -> {
            final EventType TYPE = e.getEventType();
            if (MouseEvent.MOUSE_PRESSED == TYPE) {
                setActive(!isActive());
                fireEvent(SWITCH_PRESSED);
            } else if(MouseEvent.MOUSE_RELEASED == TYPE) {
                fireEvent(SWITCH_RELEASED);
            }
        };
        selectedListener = o -> moveThumb();

        timeline = new Timeline();

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

        getStyleClass().add("switch");

        shadow = new DropShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.65), 3, 0, 0, 0);

        switchBorder = new Rectangle();
        switchBorder.setFill(getForegroundColor());

        switchBackground = new Rectangle();
        switchBackground.setMouseTransparent(true);
        switchBackground.setFill(isActive() ? getActiveColor() : getBackgroundColor());

        thumb = new Circle();
        thumb.setMouseTransparent(true);
        thumb.setFill(getForegroundColor());
        thumb.setEffect(shadow);

        pane = new Pane(switchBorder, switchBackground, thumb);

        getChildren().setAll(pane);
    }

    public void dispose() {
        switchBorder.removeEventHandler(MouseEvent.MOUSE_PRESSED, mouseEventHandler);
        switchBorder.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseEventHandler);
        activeProperty().removeListener(selectedListener);
    }

    private void registerListeners() {
        widthProperty().addListener(o -> resize());
        heightProperty().addListener(o -> resize());
        switchBorder.addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEventHandler);
        switchBorder.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseEventHandler);
        activeProperty().addListener(selectedListener);
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

    public boolean isActive() { return null == active ? _active : active.get(); }
    public void setActive(final boolean ACTIVE) {
        if (null == active) {
            _active = ACTIVE;
        } else {
            active.set(ACTIVE);
        }
    }
    public BooleanProperty activeProperty() {
        if (null == active) {
            active = new BooleanPropertyBase(_active) {
                @Override protected void invalidated() {  }
                @Override public Object getBean() { return Switch.this; }
                @Override public String getName() { return "active"; }
            };
        }
        return active;
    }

    public Color getActiveColor() { return null == activeColor ? _activeColor : activeColor.get(); }
    public void setActiveColor(final Color COLOR) {
        if (null == activeColor) {
            _activeColor = COLOR;
            redraw();
        } else {
            activeColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> activeColorProperty() {
        if (null == activeColor) {
            activeColor = new ObjectPropertyBase<Color>(_activeColor) {
                @Override protected void invalidated() { redraw(); }
                @Override public Object getBean() { return Switch.this; }
                @Override public String getName() { return "activeColor"; }
            };
            _activeColor = null;
        }
        return activeColor;
    }

    public Color getForegroundColor() { return null == foregroundColor ? _foregroundColor : foregroundColor.get(); }
    public void setForegroundColor(final Color COLOR) {
        if (null == foregroundColor) {
            _foregroundColor = COLOR;
            redraw();
        } else {
            foregroundColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> foregroundColorProperty() {
        if (null == foregroundColor) {
            foregroundColor = new ObjectPropertyBase<Color>(_foregroundColor) {
                @Override protected void invalidated() { redraw(); }
                @Override public Object getBean() { return Switch.this; }
                @Override public String getName() { return "foregroundColor"; }
            };
            _foregroundColor = null;
        }
        return foregroundColor;
    }
    
    public Color getBackgroundColor() { return null == backgroundColor ? _backgroundColor : backgroundColor.get(); }
    public void setBackgroundColor(final Color COLOR) {
        if (null == backgroundColor) {
            _backgroundColor = COLOR;
            redraw();
        } else {
            backgroundColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> backgroundColorProperty() {
        if (null == backgroundColor) {
            backgroundColor = new ObjectPropertyBase<Color>(_backgroundColor) {
                @Override protected void invalidated() { redraw(); }
                @Override public Object getBean() { return Switch.this; }
                @Override public String getName() { return "backgroundColor"; }
            };
            _backgroundColor = null;
        }
        return backgroundColor;
    }

    private void moveThumb() {
        KeyValue thumbLeftX                 = new KeyValue(thumb.centerXProperty(), thumb.getRadius() + height * 0.1);
        KeyValue thumbRightX                = new KeyValue(thumb.centerXProperty(), width - thumb.getRadius() - height * 0.1);
        KeyValue switchBackgroundLeftColor  = new KeyValue(switchBackground.fillProperty(), getBackgroundColor());
        KeyValue switchBackgroundRightColor = new KeyValue(switchBackground.fillProperty(), getActiveColor());
        if (isActive()) {
            // move thumb from left to the right
            KeyFrame kf0 = new KeyFrame(Duration.ZERO, thumbLeftX, switchBackgroundLeftColor);
            KeyFrame kf1 = new KeyFrame(Duration.millis(200), thumbRightX, switchBackgroundRightColor);
            timeline.getKeyFrames().setAll(kf0, kf1);
        } else {
            // move thumb from right to the left
            KeyFrame kf0 = new KeyFrame(Duration.ZERO, thumbRightX, switchBackgroundRightColor);
            KeyFrame kf1 = new KeyFrame(Duration.millis(200), thumbLeftX, switchBackgroundLeftColor);
            timeline.getKeyFrames().setAll(kf0, kf1);
        }
        timeline.play();
    }


    // ******************** Resizing ******************************************
    private void resize() {
        width  = getWidth() - getInsets().getLeft() - getInsets().getRight();
        height = getHeight() - getInsets().getTop() - getInsets().getBottom();
        size   = width < height ? width : height;

        if (aspectRatio * width > height) {
            width = 1 / (aspectRatio / height);
        } else if (1 / (aspectRatio / height) > width) {
            height = aspectRatio * width;
        }

        if (width > 0 && height > 0) {
            pane.setMaxSize(width, height);
            pane.setPrefSize(width, height);
            pane.relocate((getWidth() - width) * 0.5, (getHeight() - height) * 0.5);

            shadow.setRadius(width * 0.012);

            switchBorder.setWidth(width);
            switchBorder.setHeight(height);
            switchBorder.setArcWidth(height);
            switchBorder.setArcHeight(height);
            switchBorder.relocate((width - switchBorder.getWidth()) * 0.5, (height - switchBorder.getHeight()) * 0.5);

            switchBackground.setWidth(width * 0.95505618);
            switchBackground.setHeight(height * 0.90909091);
            switchBackground.setArcWidth(height * 0.90909091);
            switchBackground.setArcHeight(height * 0.90909091);
            switchBackground.relocate((width - switchBackground.getWidth()) * 0.5, (height - switchBackground.getHeight()) * 0.5);

            thumb.setRadius(height * 0.40909091);
            thumb.setCenterX(isActive() ? width - thumb.getRadius() - height * 0.1 : thumb.getRadius() + height * 0.1);
            thumb.setCenterY(height * 0.5);

            redraw();
        }
    }

    private void redraw() {
        switchBorder.setFill(getForegroundColor());
        switchBackground.setFill(isActive() ? getActiveColor() : getBackgroundColor());
        thumb.setFill(getForegroundColor());
    }
}
