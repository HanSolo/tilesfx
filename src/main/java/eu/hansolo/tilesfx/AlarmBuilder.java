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
package eu.hansolo.tilesfx;

import eu.hansolo.tilesfx.Alarm.Repetition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;

import java.time.ZonedDateTime;
import java.util.HashMap;


/**
 * Created by hansolo on 18.02.17.
 */
public class AlarmBuilder<B extends AlarmBuilder<B>> {
    private HashMap<String, Property> properties = new HashMap<>();


    // ******************** Constructors **************************************
    protected AlarmBuilder() {}


    // ******************** Methods *******************************************
    public static final AlarmBuilder create() {
        return new AlarmBuilder();
    }

    public final B repetition(final Repetition REPETITION) {
        properties.put("repetition", new SimpleObjectProperty<>(REPETITION));
        return (B)this;
    }

    public final B time(final ZonedDateTime TIME) {
        properties.put("time", new SimpleObjectProperty<>(TIME));
        return (B)this;
    }

    public final B armed(final boolean ARMED) {
        properties.put("armed", new SimpleBooleanProperty(ARMED));
        return (B)this;
    }

    public final B text(final String TEXT) {
        properties.put("text", new SimpleStringProperty(TEXT));
        return (B)this;
    }

    public final B command(final Command COMMAND) {
        properties.put("command", new SimpleObjectProperty<>(COMMAND));
        return (B)this;
    }

    public final B color(final Color COLOR) {
        properties.put("color", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final Alarm build() {
        final Alarm SECTION = new Alarm();
        properties.forEach((key, property) -> {
            switch (key) {
                case "repetition" -> SECTION.setRepetition(((ObjectProperty<Repetition>) property).get());
                case "time"       -> SECTION.setTime(((ObjectProperty<ZonedDateTime>) property).get());
                case "armed"      -> SECTION.setArmed(((BooleanProperty) property).get());
                case "text"       -> SECTION.setText(((StringProperty) property).get());
                case "command"    -> SECTION.setCommand(((ObjectProperty<Command>) property).get());
                case "color"      -> SECTION.setColor(((ObjectProperty<Color>) property).get());
            }
        });
        return SECTION;
    }
}
