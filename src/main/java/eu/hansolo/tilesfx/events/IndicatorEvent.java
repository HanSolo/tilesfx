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

package eu.hansolo.tilesfx.events;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;


public class IndicatorEvent extends Event {
    public static final EventType<IndicatorEvent> INDICATOR_ON  = new EventType<>(ANY, "INDICATOR_ON");
    public static final EventType<IndicatorEvent> INDICATOR_OFF = new EventType<>(ANY, "INDICATOR_OFF");


    // ******************** Constructors **************************************
    public IndicatorEvent(final EventType<IndicatorEvent> TYPE) { super(TYPE); }
    public IndicatorEvent(final Object SOURCE, final EventTarget TARGET, EventType<IndicatorEvent> TYPE) { super(SOURCE, TARGET, TYPE); }
}
