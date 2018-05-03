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

package eu.hansolo.tilesfx;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.StringPropertyBase;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;


/**
 * Created by hansolo on 19.12.16.
 */
public class Section implements Comparable<Section> {
    public  final SectionEvent          ENTERED_EVENT = new SectionEvent(this, null, SectionEvent.TILES_FX_SECTION_ENTERED);
    public  final SectionEvent          LEFT_EVENT    = new SectionEvent(this, null, SectionEvent.TILES_FX_SECTION_LEFT);
    public  final SectionEvent          UPDATE_EVENT  = new SectionEvent(this, null, SectionEvent.TILES_FX_SECTION_UPDATE);
    private       double                _start;
    private       DoubleProperty        start;
    private       double                _stop;
    private       DoubleProperty        stop;
    private       String                _text;
    private       StringProperty        text;
    private       Image                 _icon;
    private       ObjectProperty<Image> icon;
    private       Color                 _color;
    private       ObjectProperty<Color> color;
    private       Color                 _highlightColor;
    private       ObjectProperty<Color> highlightColor;
    private       Color                 _textColor;
    private       ObjectProperty<Color> textColor;
    private       boolean               _active;
    private       BooleanProperty       active;
    private       double                checkedValue;
    private       String                styleClass;


    // ******************** Constructors **************************************
    /**
     * Represents an area of a given range, defined by a start and stop value.
     * This class is used for regions and areas in many gauges. It is possible
     * to check a value against the defined range and fire events in case the
     * value enters or leaves the defined region.
     */
    public Section(final String JSON_STRING) {
        Object     obj     = JSONValue.parse(JSON_STRING);
        JSONObject jsonObj = (JSONObject) obj;
        _start             = Double.parseDouble(jsonObj.getOrDefault("start", "-1").toString());
        _stop              = Double.parseDouble(jsonObj.getOrDefault("stop", "-1").toString());
        _text              = jsonObj.getOrDefault("text", "").toString();
        _color             = Color.web(jsonObj.getOrDefault("color", "#00000000").toString());
        _highlightColor    = Color.web(jsonObj.getOrDefault("highlightColor", "#00000000").toString());
        _textColor         = Color.web(jsonObj.getOrDefault("textColor", "#00000000").toString());
        _active            = Boolean.parseBoolean(jsonObj.getOrDefault("active", "false").toString());
        _icon              = null;
        styleClass         = "";
        checkedValue       = -Double.MAX_VALUE;
    }
    public Section() {
        this(-1, -1, "", null, Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT, "");
    }
    public Section(final double START, final double STOP) {
        this(START, STOP, "", null, Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT, "");
    }
    public Section(final double START, final double STOP, final Color COLOR) {
        this(START, STOP, "", null, COLOR, COLOR, Color.TRANSPARENT, "");
    }
    public Section(final double START, final double STOP, final Color COLOR, final Color HIGHLIGHT_COLOR) {
        this(START, STOP, "", null, COLOR, HIGHLIGHT_COLOR, Color.TRANSPARENT, "");
    }
    public Section(final double START, final double STOP, final Image ICON, final Color COLOR) {
        this(START, STOP, "", ICON, COLOR, COLOR, Color.WHITE, "");
    }
    public Section(final double START, final double STOP, final String TEXT, final Color COLOR) {
        this(START, STOP, TEXT, null, COLOR, COLOR, Color.WHITE, "");
    }
    public Section(final double START, final double STOP, final String TEXT, final Color COLOR, final Color TEXT_COLOR) {
        this(START, STOP, TEXT, null, COLOR, COLOR, TEXT_COLOR, "");
    }
    public Section(final double START, final double STOP, final String TEXT, final Image ICON, final Color COLOR, final Color TEXT_COLOR) {
        this(START, STOP, TEXT, ICON, COLOR, COLOR, TEXT_COLOR, "");
    }
    public Section(final double START, final double STOP, final String TEXT, final Image ICON, final Color COLOR, final Color HIGHLIGHT_COLOR, final Color TEXT_COLOR) {
        this(START, STOP, TEXT, ICON, COLOR, HIGHLIGHT_COLOR, TEXT_COLOR, "");
    }
    public Section(final double START, final double STOP, final String TEXT, final Image ICON, final Color COLOR, final Color HIGHLIGHT_COLOR, final Color TEXT_COLOR, final String STYLE_CLASS) {
        _start          = START;
        _stop           = STOP;
        _text           = TEXT;
        _icon           = ICON;
        _color          = COLOR;
        _highlightColor = HIGHLIGHT_COLOR;
        _textColor      = TEXT_COLOR;
        _active         = true;
        checkedValue    = -Double.MAX_VALUE;
        styleClass      = STYLE_CLASS;
    }


    // ******************** Methods *******************************************
    /**
     * Returns the value where the section begins.
     * @return the value where the section begins
     */
    public double getStart() { return null == start ? _start : start.get(); }
    /**
     * Defines the value where the section begins.
     * @param START
     */
    public void setStart(final double START) {
        if (null == start) {
            _start = START;
            fireSectionEvent(UPDATE_EVENT);
        } else {
            start.set(START);
        }
    }
    public DoubleProperty startProperty() {
        if (null == start) {
            start = new DoublePropertyBase(_start) {
                @Override protected void invalidated() { fireSectionEvent(UPDATE_EVENT); }
                @Override public Object getBean() { return Section.this; }
                @Override public String getName() { return "start"; }
            };
        }
        return start;
    }

    /**
     * Returns the value where the section ends.
     * @return the value where the section ends
     */
    public double getStop() { return null == stop ? _stop : stop.get(); }
    /**
     * Defines the value where the section ends.
     * @param STOP
     */
    public void setStop(final double STOP) {
        if (null == stop) {
            _stop = STOP;
            fireSectionEvent(UPDATE_EVENT);
        } else {
            stop.set(STOP);
        }
    }
    public DoubleProperty stopProperty() {
        if (null == stop) {
            stop = new DoublePropertyBase(_stop) {
                @Override protected void invalidated() { fireSectionEvent(UPDATE_EVENT); }
                @Override public Object getBean() { return Section.this; }
                @Override public String getName() { return "stop"; }
            };
        }
        return stop;
    }

    /**
     * Returns the text that was set for the section.
     * @return the text that was set for the section
     */
    public String getText() { return null == text ? _text : text.get(); }
    /**
     * Defines a text for the section.
     * @param TEXT
     */
    public void setText(final String TEXT) {
        if (null == text) {
            _text = TEXT;
            fireSectionEvent(UPDATE_EVENT);
        } else {
            text.set(TEXT);
        }
    }
    public StringProperty textProperty() {
        if (null == text) {
            text = new StringPropertyBase(_text) {
                @Override protected void invalidated() { fireSectionEvent(UPDATE_EVENT); }
                @Override public Object getBean() { return Section.this; }
                @Override public String getName() { return "text"; }
            };
        }
        return text;
    }

    /**
     * Returns the image that was defined for the section.
     * In some skins the image will be drawn (e.g. SimpleSkin).
     * @return the image that was defined for the section
     */
    public Image getImage() { return null == icon ? _icon : icon.get(); }
    /**
     * Defines an image for the section.
     * In some skins the image will be drawn (e.g. SimpleSkin)
     * @param IMAGE
     */
    public void setIcon(final Image IMAGE) {
        if (null == icon) {
            _icon = IMAGE;
            fireSectionEvent(UPDATE_EVENT);
        } else {
            icon.set(IMAGE);
        }
    }
    public ObjectProperty<Image> iconProperty() {
        if (null == icon) {
            icon = new ObjectPropertyBase<Image>(_icon) {
                @Override protected void invalidated() { fireSectionEvent(UPDATE_EVENT); }
                @Override public Object getBean() { return Section.this; }
                @Override public String getName() { return "icon"; }
            };
        }
        return icon;
    }

    /**
     * Returns the color that will be used to colorize the section in
     * a gauge.
     * @return the color that will be used to colorize the section
     */
    public Color getColor() { return null == color ? _color : color.get(); }
    /**
     * Defines the color that will be used to colorize the section in
     * a gauge.
     * @param COLOR
     */
    public void setColor(final Color COLOR) {
        if (null == color) {
            _color = COLOR;
            fireSectionEvent(UPDATE_EVENT);
        } else {
            color.set(COLOR);
        }
    }
    public ObjectProperty<Color> colorProperty() {
        if (null == color) {
            color = new ObjectPropertyBase<Color>(_color) {
                @Override protected void invalidated() { fireSectionEvent(UPDATE_EVENT); }
                @Override public Object getBean() { return Section.this; }
                @Override public String getName() { return "color"; }
            };
        }
        return color;
    }

    /**
     * Returns the color that will be used to colorize the section in
     * a gauge when it is highlighted.
     * @return the color that will be used to colorize a highlighted section
     */
    public Color getHighlightColor() { return null == highlightColor ? _highlightColor : highlightColor.get(); }
    /**
     * Defines the color that will be used to colorize a highlighted section
     * @param COLOR
     */
    public void setHighlightColor(final Color COLOR) {
        if (null == highlightColor) {
            _highlightColor = COLOR;
            fireSectionEvent(UPDATE_EVENT);
        } else {
            highlightColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> highlightColorProperty() {
        if (null == highlightColor) {
            highlightColor = new ObjectPropertyBase<Color>(_highlightColor) {
                @Override protected void invalidated() { fireSectionEvent(UPDATE_EVENT); }
                @Override public Object getBean() { return Section.this; }
                @Override public String getName() { return "highlightColor"; }
            };
        }
        return highlightColor;
    }

    /**
     * Returns the color that will be used to colorize the section text.
     * @return the color that will be used to colorize the section text
     */
    public Color getTextColor() { return null == textColor ? _textColor : textColor.get(); }
    /**
     * Defines the color that will be used to colorize the section text.
     * @param COLOR
     */
    public void setTextColor(final Color COLOR) {
        if (null == textColor) {
            _textColor = COLOR;
            fireSectionEvent(UPDATE_EVENT);
        } else {
            textColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> textColorProperty() {
        if (null == textColor) {
            textColor = new ObjectPropertyBase<Color>(_textColor) {
                @Override protected void invalidated() { fireSectionEvent(UPDATE_EVENT); }
                @Override public Object getBean() { return Section.this; }
                @Override public String getName() { return "textColor"; }
            };
        }
        return textColor;
    }

    public boolean isActive() { return null == active ? _active : active.get(); }
    public void setActive(final boolean ACTIVE) {
        if (null == active) {
            _active = ACTIVE;
        } else {
            active.set(ACTIVE);
        }
    }
    public ReadOnlyBooleanProperty activeProperty() {
        if (null == active) {
            active = new BooleanPropertyBase(_active) {
                @Override public Object getBean() { return Section.this; }
                @Override public String getName() { return "active"; }
            };
        }
        return active;
    }

    /**
     * Returns the style class that can be used to colorize the section.
     * This is not implemented in the current available skins.
     * @return the style class that can be used to colorize the section
     */
    public String getStyleClass() { return styleClass; }
    /**
     * Defines the style class that can be used to colorize the section.
     * This is not implemented in the current available skins.
     * @param STYLE_CLASS
     */
    public void setStyleClass(final String STYLE_CLASS) { styleClass = STYLE_CLASS; }

    /**
     * Returns true if the given value is within the range between
     * section.getStart() and section.getStop()
     * @param VALUE
     * @return true if the given value is within the range of the section
     */
    public boolean contains(final double VALUE) {
        return (Double.compare(VALUE, getStart()) >= 0 && Double.compare(VALUE, getStop()) <= 0);
    }

    /**
     * Checks if the section contains the given value and fires an event
     * in case the value "entered" or "left" the section. With this one
     * can react if a value enters/leaves a specific region in a gauge.
     * @param VALUE
     */
    public void checkForValue(final double VALUE) {
        boolean wasInSection = contains(checkedValue);
        boolean isInSection  = contains(VALUE);
        if (!wasInSection && isInSection) {
            fireSectionEvent(ENTERED_EVENT);
        } else if (wasInSection && !isInSection) {
            fireSectionEvent(LEFT_EVENT);
        }
        checkedValue = VALUE;
    }

    public boolean equals(final Section SECTION) {
        return (Double.compare(SECTION.getStart(), getStart()) == 0 &&
                Double.compare(SECTION.getStop(), getStop()) == 0 &&
                SECTION.getText().equals(getText()));
    }

    @Override public int compareTo(final Section SECTION) {
        if (Double.compare(getStart(), SECTION.getStart()) < 0) return -1;
        if (Double.compare(getStart(), SECTION.getStart()) > 0) return 1;
        return 0;
    }

    @Override public String toString() {
        return new StringBuilder()
            .append("{\n")
            .append("\"text\":\"").append(getText()).append("\",\n")
            .append("\"startValue\":").append(getStart()).append(",\n")
            .append("\"stopValue\":").append(getStop()).append(",\n")
            .append("\"color\":\"").append(getColor().toString().substring(0,8).replace("0x", "#")).append("\",\n")
            .append("\"highlightColor\":\"").append(getHighlightColor().toString().substring(0,8).replace("0x", "#")).append("\",\n")
            .append("\"textColor\":\"").append(getTextColor().toString().substring(0,8).replace("0x", "#")).append("\"\n")
            .append("}")
            .toString();
    }

    public JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("start", getStart());
        jsonObject.put("stop", getStop());
        jsonObject.put("text", getText());
        jsonObject.put("color", getColor().toString().replace("0x", "#"));
        jsonObject.put("highlightColor", getHighlightColor().toString().replace("0x", "#"));
        jsonObject.put("textColor", getTextColor().toString().replace("0x", "#"));
        jsonObject.put("active", isActive());
        return jsonObject;
    }

    public String toJSONString() {
        return toJSON().toJSONString();
    }


    // ******************** Event handling ************************************
    public final ObjectProperty<EventHandler<SectionEvent>> onSectionEnteredProperty() { return onSectionEntered; }
    public final void setOnSectionEntered(EventHandler<SectionEvent> value) { onSectionEnteredProperty().set(value); }
    public final EventHandler<SectionEvent> getOnSectionEntered() { return onSectionEnteredProperty().get(); }
    private ObjectProperty<EventHandler<SectionEvent>> onSectionEntered = new SimpleObjectProperty<>(this, "onSectionEntered");

    public final ObjectProperty<EventHandler<SectionEvent>> onSectionLeftProperty() { return onSectionLeft; }
    public final void setOnSectionLeft(EventHandler<SectionEvent> value) { onSectionLeftProperty().set(value); }
    public final EventHandler<SectionEvent> getOnSectionLeft() { return onSectionLeftProperty().get(); }
    private ObjectProperty<EventHandler<SectionEvent>> onSectionLeft = new SimpleObjectProperty<>(this, "onSectionLeft");

    public final ObjectProperty<EventHandler<SectionEvent>> onSectionUpdateProperty() { return onSectionUpdate; }
    public final void setOnSectionUpdate(EventHandler<SectionEvent> value) { onSectionUpdateProperty().set(value); }
    public final EventHandler<SectionEvent> getOnSectionUpdate() { return onSectionUpdateProperty().get(); }
    private ObjectProperty<EventHandler<SectionEvent>> onSectionUpdate = new SimpleObjectProperty<>(this, "onSectionUpdate");

    public void fireSectionEvent(final SectionEvent EVENT) {
        final EventHandler<SectionEvent> HANDLER;
        final EventType                  TYPE = EVENT.getEventType();
        if (SectionEvent.TILES_FX_SECTION_ENTERED == TYPE) {
            HANDLER = getOnSectionEntered();
        } else if (SectionEvent.TILES_FX_SECTION_LEFT == TYPE) {
            HANDLER = getOnSectionLeft();
        } else if (SectionEvent.TILES_FX_SECTION_UPDATE == TYPE) {
            HANDLER = getOnSectionUpdate();
        } else {
            HANDLER = null;
        }

        if (null == HANDLER) return;

        HANDLER.handle(EVENT);
    }


    // ******************** Inner Classes *************************************
    public static class SectionEvent extends Event {
        public static final EventType<SectionEvent> TILES_FX_SECTION_ENTERED = new EventType<>(ANY, "TILES_FX_SECTION_ENTERED");
        public static final EventType<SectionEvent> TILES_FX_SECTION_LEFT    = new EventType<>(ANY, "TILES_FX_SECTION_LEFT");
        public static final EventType<SectionEvent> TILES_FX_SECTION_UPDATE  = new EventType<>(ANY, "TILES_FX_SECTION_UPDATE");


        // ******************** Constructors **************************************
        public SectionEvent(final Object SOURCE, final EventTarget TARGET, EventType<SectionEvent> TYPE) {
            super(SOURCE, TARGET, TYPE);
        }
    }
}