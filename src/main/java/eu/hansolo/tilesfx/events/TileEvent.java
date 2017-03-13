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


/**
 * Created by hansolo on 19.12.16.
 */
public class TileEvent {
    public enum EventType { RECALC, REDRAW, RESIZE, VISIBILITY, SECTION, ALERT, VALUE,
                            THRESHOLD_EXCEEDED, THRESHOLD_UNDERRUN, FINISHED, SERIES,
                            DATA, GRAPHIC, UPDATE, AVERAGING, LOCATION, TRACK, MAP_PROVIDER, TOOLTIP_TEXT };
    private final EventType EVENT_TYPE;


    // ******************** Constructors **************************************
    public TileEvent(final EventType EVENT_TYPE) {
        this.EVENT_TYPE = EVENT_TYPE;
    }


    // ******************** Methods *******************************************
    public EventType getEventType() { return EVENT_TYPE; }
}
