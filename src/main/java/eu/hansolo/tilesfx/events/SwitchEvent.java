/*
 * Copyright (c) 2016 by Gerrit Grunwald
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

package eu.hansolo.tilesfx.events;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;


/**
 * Created by hansolo on 26.12.16.
 */
public class SwitchEvent extends Event {
    public static final EventType<SwitchEvent> SWITCH_PRESSED  = new EventType<>(ANY, "SWITCH_PRESSED");
    public static final EventType<SwitchEvent> SWITCH_RELEASED = new EventType<>(ANY, "SWITCH_RELEASED");


    // ******************** Constructors **************************************
    public SwitchEvent(final EventType<SwitchEvent> TYPE) { super(TYPE); }
    public SwitchEvent(final Object SOURCE, final EventTarget TARGET, EventType<SwitchEvent> TYPE) { super(SOURCE, TARGET, TYPE); }

}
