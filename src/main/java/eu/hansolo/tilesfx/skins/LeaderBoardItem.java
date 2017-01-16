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
import eu.hansolo.tilesfx.events.UpdateEvent;
import eu.hansolo.tilesfx.fonts.Fonts;
import javafx.beans.DefaultProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.StringPropertyBase;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;

import java.util.Locale;


/**
 * User: hansolo
 * Date: 11.01.17
 * Time: 08:15
 */
@DefaultProperty("children")
public class LeaderBoardItem extends Region implements Comparable<LeaderBoardItem> {
    public enum State {
        RISE(Tile.GREEN, 0),
        FALL(Tile.RED, 180),
        CONSTANT(Color.TRANSPARENT, 90);

        public final Color  color;
        public final double angle;

        State(final Color COLOR, final double ANGLE) {
            color = COLOR;
            angle = ANGLE;
        }
    }
    private static final double                PREFERRED_WIDTH  = 250;
    private static final double                PREFERRED_HEIGHT = 30;
    private static final double                MINIMUM_WIDTH    = 25;
    private static final double                MINIMUM_HEIGHT   = 3.6;
    private static final double                MAXIMUM_WIDTH    = 1024;
    private static final double                MAXIMUM_HEIGHT   = 1024;
    private static final double                ASPECT_RATIO     = PREFERRED_HEIGHT / PREFERRED_WIDTH;
    private static final UpdateEvent           UPDATE_EVENT     = new UpdateEvent(UpdateEvent.UPDATE_LEADER_BOARD);
    private              double                width;
    private              double                height;
    private              Text                  nameText;
    private              Text                  valueText;
    private              SVGPath               indicator;
    private              Line                  separator;
    private              Pane                  pane;
    private              StringProperty        name;
    private              DoubleProperty        value;
    private              ObjectProperty<Color> nameColor;
    private              ObjectProperty<Color> valueColor;
    private              ObjectProperty<Color> separatorColor;
    private              State                 state;
    private              String                formatString;
    private              Locale                locale;
    private              int                   index;
    private              int                   lastIndex;



    // ******************** Constructors **************************************
    public LeaderBoardItem() {
        this("", 0);
    }
    public LeaderBoardItem(final String NAME) {
        this(NAME, 0);
    }
    public LeaderBoardItem(final String NAME, final double VALUE) {
        name           = new StringPropertyBase(NAME) {
            @Override protected void invalidated() { nameText.setText(get()); }
            @Override public Object getBean() { return LeaderBoardItem.this; }
            @Override public String getName() { return "name"; }
        };
        value          = new DoublePropertyBase(VALUE) {
            @Override protected void invalidated() {
                valueText.setText(String.format(locale, formatString, get()));
                valueText.setX((width * 0.95) - valueText.getLayoutBounds().getWidth());
            }
            @Override public Object getBean() { return LeaderBoardItem.this; }
            @Override public String getName() { return "value"; }
        };
        nameColor      = new ObjectPropertyBase<Color>(Tile.FOREGROUND) {
            @Override protected void invalidated() { nameText.setFill(get()); }
            @Override public Object getBean() { return LeaderBoardItem.this; }
            @Override public String getName() { return "nameColor"; }
        };
        valueColor     = new ObjectPropertyBase<Color>(Tile.FOREGROUND) {
            @Override protected void invalidated() {  valueText.setFill(get()); }
            @Override public Object getBean() { return LeaderBoardItem.this; }
            @Override public String getName() { return "valueColor"; }
        };
        separatorColor = new ObjectPropertyBase<Color>(Color.rgb(72, 72, 72)) {
            @Override protected void invalidated() { separator.setStroke(get()); }
            @Override public Object getBean() { return LeaderBoardItem.this; }
            @Override public String getName() { return "separatorColor"; }
        };
        formatString   = "%.0f";
        locale         = Locale.US;
        index          = 1024;
        lastIndex      = 1024;

        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void initGraphics() {
        if (Double.compare(getPrefWidth(), 0.0) <= 0 || Double.compare(getPrefHeight(), 0.0) <= 0 ||
            Double.compare(getWidth(), 0.0) <= 0 || Double.compare(getHeight(), 0.0) <= 0) {
            if (getPrefWidth() > 0 && getPrefHeight() > 0) {
                setPrefSize(getPrefWidth(), getPrefHeight());
            } else {
                setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT); // 11x7
            }
        }

        state = State.CONSTANT;

        indicator = new SVGPath();
        indicator.setContent("M 0 7 L 5.5 0 L 11 7 L 0 7 Z");
        setIndicatorSize(indicator, PREFERRED_WIDTH * 0.04782608, PREFERRED_WIDTH * 0.03043478);
        indicator.setFill(state.color);
        indicator.setRotate(state.angle);

        nameText = new Text(getName());
        nameText.setTextOrigin(VPos.TOP);

        valueText = new Text(String.format(locale, formatString, getValue()));
        valueText.setTextOrigin(VPos.TOP);

        separator = new Line();

        pane = new Pane(indicator, nameText, valueText, separator);
        pane.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        widthProperty().addListener(o -> resize());
        heightProperty().addListener(o -> resize());
    }


    // ******************** Methods *******************************************
    @Override protected double computeMinWidth(final double HEIGHT) { return MINIMUM_WIDTH; }
    @Override protected double computeMinHeight(final double WIDTH) { return MINIMUM_HEIGHT; }
    @Override protected double computePrefWidth(final double HEIGHT) { return super.computePrefWidth(HEIGHT); }
    @Override protected double computePrefHeight(final double WIDTH) { return super.computePrefHeight(WIDTH); }
    @Override protected double computeMaxWidth(final double HEIGHT) { return MAXIMUM_WIDTH; }
    @Override protected double computeMaxHeight(final double WIDTH) { return MAXIMUM_HEIGHT; }

    @Override public ObservableList<Node> getChildren() { return super.getChildren(); }

    public String getName() { return name.get(); }
    public void setName(final String NAME) { name.set(NAME); }
    public StringProperty nameProperty() { return name; }

    public double getValue() { return value.get(); }
    public void setValue(final double VALUE) { value.set(VALUE); }
    public DoubleProperty valueProperty() { return value; }

    public Color getNameColor() { return nameColor.get(); }
    public void setNameColor(final Color COLOR) { nameColor.set(COLOR); }
    public ObjectProperty<Color> nameColorProperty() { return nameColor; }

    public Color getValueColor() { return valueColor.get(); }
    public void setValueColor(final Color COLOR) { valueColor.set(COLOR); }
    public ObjectProperty<Color> valueColorProperty() { return valueColor; }

    public Color getSeparatorColor() { return separatorColor.get(); }
    public void setSeparatorColor(final Color COLOR) { separatorColor.set(COLOR); }
    public ObjectProperty<Color> separatorColorProperty() { return separatorColor; }

    public int getIndex() { return index; }
    public void setIndex(final int INDEX) {
        lastIndex = index;
        index     = INDEX;
        if (index > lastIndex) {
            state = State.FALL;
        } else if (index < lastIndex) {
            state = State.RISE;
        } else {
            state = State.CONSTANT;
        }
        indicator.setFill(state.color);
        indicator.setRotate(state.angle);
        fireEvent(UPDATE_EVENT);
    }

    public int getLastIndex() { return lastIndex; }

    public State getState() { return state; }

    @Override public int compareTo(final LeaderBoardItem SEGMENT) { return Double.compare(getValue(), SEGMENT.getValue()); }

    public void setLocale(final Locale LOCALE) {
        locale = LOCALE;
        valueText.setText(String.format(locale, formatString, getValue()));
    }

    public void setFormatString(final String FORMAT_STRING) {
        formatString = FORMAT_STRING;
        valueText.setText(String.format(locale, formatString, getValue()));
    }


    // ******************** Resizing ******************************************
    private void resize() {
        width  = getWidth() - getInsets().getLeft() - getInsets().getRight();
        height = getHeight() - getInsets().getTop() - getInsets().getBottom();

        if (ASPECT_RATIO * width > height) {
            width = 1 / (ASPECT_RATIO / height);
        } else if (1 / (ASPECT_RATIO / height) > width) {
            height = ASPECT_RATIO * width;
        }

        if (width > 0 && height > 0) {
            pane.setMaxSize(width, height);
            pane.setPrefSize(width, height);

            setIndicatorSize(indicator, width * 0.04782608, width * 0.03043478);
            indicator.setLayoutX(width * 0.05);
            indicator.setLayoutY((height - indicator.getBoundsInLocal().getHeight()) * 0.25);

            nameText.setFont(Fonts.latoRegular(width * 0.06));
            nameText.setX(width * 0.12);
            nameText.setY(0);

            valueText.setFont(Fonts.latoRegular(width * 0.06));
            valueText.setX((width * 0.95) - valueText.getLayoutBounds().getWidth());
            valueText.setY(0);

            separator.setStartX(width * 0.05);
            separator.setStartY(height);
            separator.setEndX(width * 0.95);
            separator.setEndY(height);

            redraw();
        }
    }

    private void setIndicatorSize(final Node NODE, final double TARGET_WIDTH, final double TARGET_HEIGHT) {
        NODE.setScaleX(TARGET_WIDTH / NODE.getLayoutBounds().getWidth());
        NODE.setScaleY(TARGET_HEIGHT / NODE.getLayoutBounds().getHeight());
    }

    private void redraw() {
        nameText.setFill(getNameColor());
        valueText.setFill(getValueColor());
        indicator.setFill(state.color);
        separator.setStroke(getSeparatorColor());
    }

    @Override public String toString() {
        return new StringBuilder(getName()).append(",").append(getValue()).append(",").toString();
    }
}
