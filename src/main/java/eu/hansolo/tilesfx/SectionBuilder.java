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

import eu.hansolo.tilesfx.Section.SectionEvent;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.HashMap;


/**
 * Created by hansolo on 23.12.16.
 */
public class SectionBuilder<B extends SectionBuilder<B>> {
    private HashMap<String, Property> properties = new HashMap<>();


    // ******************** Constructors **************************************
    protected SectionBuilder() {}


    // ******************** Methods *******************************************
    public static final SectionBuilder create() {
        return new SectionBuilder();
    }

    public final B start(final double VALUE) {
        properties.put("start", new SimpleDoubleProperty(VALUE));
        return (B)this;
    }

    public final B stop(final double VALUE) {
        properties.put("stop", new SimpleDoubleProperty(VALUE));
        return (B)this;
    }

    public final B text(final String TEXT) {
        properties.put("text", new SimpleStringProperty(TEXT));
        return (B)this;
    }

    public final B icon(final Image IMAGE) {
        properties.put("icon", new SimpleObjectProperty<>(IMAGE));
        return (B)this;
    }

    public final B color(final Color COLOR) {
        properties.put("color", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B highlightColor(final Color COLOR) {
        properties.put("highlightColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B textColor(final Color COLOR) {
        properties.put("textColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B active(final boolean ACTIVE) {
        properties.put("active", new SimpleBooleanProperty(ACTIVE));
        return (B)this;
    }

    public final B styleClass(final String STYLE_CLASS) {
        properties.put("styleClass", new SimpleStringProperty(STYLE_CLASS));
        return (B)this;
    }

    public final B onSectionEntered(final EventHandler<SectionEvent> HANDLER) {
        properties.put("onSectionEntered", new SimpleObjectProperty<>(HANDLER));
        return (B)this;
    }

    public final B onSectionLeft(final EventHandler<SectionEvent> HANDLER) {
        properties.put("onSectionLeft", new SimpleObjectProperty<>(HANDLER));
        return (B)this;
    }

    public final Section build() {
        final Section SECTION = new Section();
        for (String key : properties.keySet()) {
            if ("start".equals(key)) {
                SECTION.setStart(((DoubleProperty) properties.get(key)).get());
            } else if("stop".equals(key)) {
                SECTION.setStop(((DoubleProperty) properties.get(key)).get());
            } else if("text".equals(key)) {
                SECTION.setText(((StringProperty) properties.get(key)).get());
            } else if("icon".equals(key)) {
                SECTION.setIcon(((ObjectProperty<Image>) properties.get(key)).get());
            } else if ("color".equals(key)) {
                SECTION.setColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("highlightColor".equals(key)) {
                SECTION.setHighlightColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("textColor".equals(key)) {
                SECTION.setTextColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if("active".equals(key)) {
                SECTION.setActive(((BooleanProperty) properties.get(key)).get());
            } else if ("onSectionEntered".equals(key)) {
                SECTION.setOnSectionEntered(((ObjectProperty<EventHandler>) properties.get(key)).get());
            } else if ("onSectionLeft".equals(key)) {
                SECTION.setOnSectionLeft(((ObjectProperty<EventHandler>) properties.get(key)).get());
            } else if ("styleClass".equals(key)) {
                SECTION.setStyleClass(((StringProperty) properties.get(key)).get());
            }
        }
        return SECTION;
    }
}