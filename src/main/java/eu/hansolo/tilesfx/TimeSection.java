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
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Created by hansolo on 31.01.16.
 */
public class TimeSection implements Comparable<TimeSection> {
    public  final TimeSectionEvent          ENTERED_EVENT = new TimeSectionEvent(this, null, TimeSectionEvent.TIME_SECTION_ENTERED);
    public  final TimeSectionEvent          LEFT_EVENT    = new TimeSectionEvent(this, null, TimeSectionEvent.TIME_SECTION_LEFT);
    private       LocalTime                 _start;
    private       ObjectProperty<LocalTime> start;
    private       LocalTime                 _stop;
    private       ObjectProperty<LocalTime> stop;
    private       String                    _text;
    private       StringProperty            text;
    private       Image                     _icon;
    private       ObjectProperty<Image>     icon;
    private       Color                     _color;
    private       ObjectProperty<Color>     color;
    private       Color                     _highlightColor;
    private       ObjectProperty<Color>     highlightColor;
    private       Color                     _textColor;
    private       ObjectProperty<Color>     textColor;
    private       LocalTime                 checkedValue;
    private       boolean                   _active;
    private       BooleanProperty           active;
    private       Set<DayOfWeek>            days;


    // ******************** Constructors **************************************
    /**
     * Represents an area of a given range, defined by a start and stop time.
     * This class is used for regions and areas in many clocks. It is possible
     * to check a time against the defined range and fire events in case the
     * value enters or leaves the defined region.
     */
    public TimeSection(final String JSON_STRING) {
        Object     obj     = JSONValue.parse(JSON_STRING);
        JSONObject jsonObj = (JSONObject) obj;
        _start             = LocalTime.parse(jsonObj.getOrDefault("start", LocalTime.now()).toString());
        _stop              = LocalTime.parse(jsonObj.getOrDefault("stop", LocalTime.now()).toString());
        _text              = jsonObj.getOrDefault("text", "").toString();
        _color             = Color.web(jsonObj.getOrDefault("color", "#00000000").toString());
        _highlightColor    = Color.web(jsonObj.getOrDefault("highlightColor", "#00000000").toString());
        _textColor         = Color.web(jsonObj.getOrDefault("textColor", "#00000000").toString());
        _active            = Boolean.parseBoolean(jsonObj.getOrDefault("active", "false").toString());
        _icon              = null;
        checkedValue       = LocalTime.MIN;
        days               = new HashSet<>(8);
        days.addAll(Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));
    }
    public TimeSection() {
        this(LocalTime.now(), LocalTime.now(), "", null, Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT, false, DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
    }
    public TimeSection(final LocalTime START, final LocalTime STOP) {
        this(START, STOP, "", null, Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT, false, DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
    }
    public TimeSection(final LocalTime START, final LocalTime STOP, final Color COLOR) {
        this(START, STOP, "", null, COLOR, COLOR, Color.TRANSPARENT, false, DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
    }
    public TimeSection(final LocalTime START, final LocalTime STOP, final Color COLOR, final Color HIGHLIGHT_COLOR) {
        this(START, STOP, "", null, COLOR, HIGHLIGHT_COLOR, Color.TRANSPARENT, false, DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
    }
    public TimeSection(final LocalTime START, final LocalTime STOP, final Color COLOR, final Color HIGHLIGHT_COLOR, final DayOfWeek... DAYS) {
        this(START, STOP, "", null, COLOR, HIGHLIGHT_COLOR, Color.TRANSPARENT, false, DAYS);
    }
    public TimeSection(final LocalTime START, final LocalTime STOP, final Image ICON, final Color COLOR) {
        this(START, STOP, "", ICON, COLOR, COLOR, Color.WHITE, false, DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
    }
    public TimeSection(final LocalTime START, final LocalTime STOP, final String TEXT, final Color COLOR) {
        this(START, STOP, TEXT, null, COLOR, COLOR, Color.WHITE, false, DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
    }
    public TimeSection(final LocalTime START, final LocalTime STOP, final String TEXT, final Color COLOR, final Color TEXT_COLOR) {
        this(START, STOP, TEXT, null, COLOR, COLOR, TEXT_COLOR, false, DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
    }
    public TimeSection(final LocalTime START, final LocalTime STOP, final String TEXT, final Image ICON, final Color COLOR, final Color TEXT_COLOR) {
        this(START, STOP, TEXT, ICON, COLOR, COLOR, TEXT_COLOR, false, DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
    }
    public TimeSection(final LocalTime START, final LocalTime STOP, final String TEXT, final Image ICON, final Color COLOR, final Color HIGHLIGHT_COLOR, final Color TEXT_COLOR, final boolean ACTIVE, final DayOfWeek... DAYS) {
        _start          = START;
        _stop           = STOP;
        _text           = TEXT;
        _icon           = ICON;
        _color          = COLOR;
        _highlightColor = HIGHLIGHT_COLOR;
        _textColor      = TEXT_COLOR;
        checkedValue    = LocalTime.MIN;
        _active         = ACTIVE;
        days            = new HashSet<>(8);
        days.addAll(Arrays.asList(DAYS));
    }


    // ******************** Methods *******************************************
    /**
     * Returns the time when the section begins.
     * @return the time when the section begins
     */
    public LocalTime getStart() { return null == start ? _start : start.get(); }
    /**
     * Defines the time when the section starts.
     * @param START
     */
    public void setStart(final LocalTime START) {
        if (null == start) {
            _start = START;
        } else {
            start.set(START);
        }
    }
    public ObjectProperty<LocalTime> startProperty() {
        if (null == start) { start = new SimpleObjectProperty<>(TimeSection.this, "start", _start); }
        return start;
    }

    /**
     * Returns the time when the section ends.
     * @return the time when the section ends
     */
    public LocalTime getStop() { return null == stop ? _stop : stop.get(); }
    /**
     * Defines the time when the section ends
     * @param STOP
     */
    public void setStop(final LocalTime STOP) {
        if (null == stop) {
            _stop = STOP;
        } else {
            stop.set(STOP);
        }
    }
    public ObjectProperty<LocalTime> stopProperty() {
        if (null == stop) { stop = new SimpleObjectProperty<>(TimeSection.this, "stop", _stop); }
        return stop;
    }

    /**
     * Returns the text that was set for the section.
     * @return the text that was set for the section
     */
    public String getText() { return null == text ? _text : text.get(); }
    /**
     * Defines the text for the section
     * @param TEXT
     */
    public void setText(final String TEXT) {
        if (null == text) {
            _text = TEXT;
        } else {
            text.set(TEXT);
        }
    }
    public StringProperty textProperty() {
        if (null == text) { text = new SimpleStringProperty(TimeSection.this, "text", _text); }
        return text;
    }

    /**
     * Returns the image that was defined for the section.
     * @return the image that was defined for the section
     */
    public Image getImage() { return null == icon ? _icon : icon.get(); }
    /**
     * Defines an image for the section.
     * @param IMAGE
     */
    public void setIcon(final Image IMAGE) {
        if (null == icon) {
            _icon = IMAGE;
        } else {
            icon.set(IMAGE);
        }
    }
    public ObjectProperty<Image> iconProperty() {
        if (null == icon) { icon = new SimpleObjectProperty<>(this, "icon", _icon); }
        return icon;
    }

    /**
     * Returns the color that will be used to colorize the section
     * in a clock.
     * @return the color that will be used to colorize the section
     */
    public Color getColor() { return null == color ? _color : color.get(); }
    /**
     * Defines the color that will be used to colorize the section
     * in a clock.
     * @param COLOR
     */
    public void setColor(final Color COLOR) {
        if (null == color) {
            _color = COLOR;
        } else {
            color.set(COLOR);
        }
    }
    public ObjectProperty<Color> colorProperty() {
        if (null == color) { color = new SimpleObjectProperty<>(TimeSection.this, "color", _color); }
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
        } else {
            highlightColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> highlightColorProperty() {
        if (null == highlightColor) { highlightColor = new SimpleObjectProperty<>(TimeSection.this, "highlightColor", _highlightColor); }
        return highlightColor;
    }

    /**
     * Returns the color that will be used to colorize the section text.
     * @return the color that will bused to colorize the section text
     */
    public Color getTextColor() { return null == textColor ? _textColor : textColor.get(); }
    /**
     * Defines the color that will be used to colorize the section text
     * @param COLOR
     */
    public void setTextColor(final Color COLOR) {
        if (null == textColor) {
            _textColor = COLOR;
        } else {
            textColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> textColorProperty() {
        if (null == textColor) { textColor = new SimpleObjectProperty<>(TimeSection.this, "textColor", _textColor); }
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
                @Override public Object getBean() { return TimeSection.this; }
                @Override public String getName() { return "active"; }
            };
        }
        return active;
    }

    public Set<DayOfWeek> getDays() { return days; }
    public List<DayOfWeek> getDaysAsList() { return new ArrayList<>(days); }
    public void setDays(final DayOfWeek... DAYS) {
        days.clear();
        Collections.addAll(days, DAYS);
    }
    public void setDays(final Set<DayOfWeek> DAYS) {
        days.clear();
        days.addAll(DAYS);
    }
    public void addDay(final DayOfWeek DAY) { days.add(DAY); }
    public void removeDay(final DayOfWeek DAY) { days.remove(DAY); }
    public void clearDays() { days.clear(); }


    /**
     * Returns true if the given time is within the range between
     * section.getStart() and section.getStop()
     * @param VALUE
     * @return true if the given time is within the range of the section
     */
    public boolean contains(final LocalTime VALUE) {
        return VALUE.isAfter(getStart()) && VALUE.isBefore(getStop());
    }

    /**
     * Checks if the section contains the given time and fires an event
     * in case the value "entered" or "left" the section. With this one
     * can react if a time "enters"/"leaves" a section.
     * This can be useful to control things like switching lights on if
     * the time enters the region and switching it of again when the time
     * left the region.
     * When calling this method the days won't be taken into account but
     * only the time!!!
     * If the TimeSection is not active no event will be fired.
     * @param VALUE
     */
    public void checkForTime(final LocalTime VALUE) {
        if (!isActive()) return;
        boolean wasInSection = contains(checkedValue);
        boolean isInSection  = contains(VALUE);
        if (!wasInSection && isInSection) {
            fireTimeSectionEvent(ENTERED_EVENT);
        } else if (wasInSection && !isInSection) {
            fireTimeSectionEvent(LEFT_EVENT);
        }
        checkedValue = VALUE;
    }
    public void checkForTimeAndDay(final LocalTime VALUE, final DayOfWeek DAY) {
        if (days.contains(DAY)) { checkForTime(VALUE); }
    }
    /**
     * Checks if the section days contains the day of week of the given ZonedDateTime
     * object and if the section contains the time itself. With this one can react if
     * a time "enters"/"leaves" a section.
     * @param DATE_TIME
     */
    public void checkForTimeAndDate(final ZonedDateTime DATE_TIME) {
        if (days.contains(DATE_TIME.getDayOfWeek())) { checkForTime(DATE_TIME.toLocalTime()); }
    }

    public boolean equals(final TimeSection SECTION) {
        return (SECTION.getStart().equals(getStart()) &&
                SECTION.getStop().equals(getStop()) &&
                SECTION.getText().equals(getText()));
    }

    @Override public int compareTo(final TimeSection SECTION) {
        if (getStart().isBefore(SECTION.getStart())) return -1;
        if (getStart().isAfter(SECTION.getStart())) return 1;
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
        jsonObject.put("start", getStart().toString());
        jsonObject.put("stop", getStop().toString());
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
    public final ObjectProperty<EventHandler<TimeSectionEvent>> onTimeSectionEnteredProperty() { return onTimeSectionEntered; }
    public final void setOnTimeSectionEntered(EventHandler<TimeSectionEvent> value) { onTimeSectionEnteredProperty().set(value); }
    public final EventHandler<TimeSectionEvent> getOnTimeSectionEntered() { return onTimeSectionEnteredProperty().get(); }
    private ObjectProperty<EventHandler<TimeSectionEvent>> onTimeSectionEntered = new SimpleObjectProperty<>(this, "onTimeSectionEntered");

    public final ObjectProperty<EventHandler<TimeSectionEvent>> onTimeSectionLeftProperty() { return onTimeSectionLeft; }
    public final void setOnTimeSectionLeft(EventHandler<TimeSectionEvent> value) { onTimeSectionLeftProperty().set(value); }
    public final EventHandler<TimeSectionEvent> getOnTimeSectionLeft() { return onTimeSectionLeftProperty().get(); }
    private ObjectProperty<EventHandler<TimeSectionEvent>> onTimeSectionLeft = new SimpleObjectProperty<>(this, "onTimeSectionLeft");

    public void fireTimeSectionEvent(final TimeSectionEvent EVENT) {
        final EventHandler<TimeSectionEvent> HANDLER;
        final EventType                      TYPE = EVENT.getEventType();
        if (TimeSectionEvent.TIME_SECTION_ENTERED == TYPE) {
            HANDLER = getOnTimeSectionEntered();
        } else if (TimeSectionEvent.TIME_SECTION_LEFT == TYPE) {
            HANDLER = getOnTimeSectionLeft();
        } else {
            HANDLER = null;
        }

        if (null == HANDLER) return;

        HANDLER.handle(EVENT);
    }


    // ******************** Inner Classes *************************************
    public static class TimeSectionEvent extends Event {
        public static final EventType<TimeSectionEvent> TIME_SECTION_ENTERED = new EventType<>(ANY, "TIME_SECTION_ENTERED");
        public static final EventType<TimeSectionEvent> TIME_SECTION_LEFT    = new EventType<>(ANY, "TIME_SECTION_LEFT");

        // ******************** Constructors **************************************
        public TimeSectionEvent(final Object SOURCE, final EventTarget TARGET, EventType<TimeSectionEvent> TYPE) {
            super(SOURCE, TARGET, TYPE);
        }
    }
}