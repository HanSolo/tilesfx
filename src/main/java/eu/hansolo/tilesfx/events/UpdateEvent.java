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

package eu.hansolo.tilesfx.events;

import javafx.beans.NamedArg;
import javafx.event.Event;
import javafx.event.EventType;


/**
 * Created by hansolo on 11.01.17.
 */
public class UpdateEvent extends Event {
    public static EventType<UpdateEvent> UPDATE_BAR_CHART    = new EventType<>(ANY, "UPDATE_BAR_CHART");
    public static EventType<UpdateEvent> UPDATE_LEADER_BOARD = new EventType<>(ANY, "UPDATE_LEADER_BOARD");

    public UpdateEvent(@NamedArg("eventType") final EventType<? extends Event> TYPE) {
        super(TYPE);
    }
}
