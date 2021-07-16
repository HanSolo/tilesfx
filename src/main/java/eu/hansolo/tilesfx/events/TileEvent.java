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
package eu.hansolo.tilesfx.events;


import eu.hansolo.tilesfx.chart.ChartData;


/**
 * Created by hansolo on 19.12.16.
 */
public class TileEvent {
    public enum EventType { SHOW_NOTIFY_REGION, HIDE_NOTIFY_REGION, SHOW_INFO_REGION, HIDE_INFO_REGION, SHOW_LOWER_RIGHT_REGION, HIDE_LOWER_RIGHT_REGION,
                            RECALC, REDRAW, REFRESH, RESIZE, VISIBILITY, SECTION, ALERT, VALUE,
                            THRESHOLD_EXCEEDED, THRESHOLD_UNDERRUN,
                            LOWER_THRESHOLD_EXCEEDED, LOWER_THRESHOLD_UNDERRUN,
                            MAX_VALUE_EXCEEDED, MIN_VALUE_UNDERRUN, VALUE_IN_RANGE,
                            FINISHED, SERIES, SERIES_SET, SERIES_ADD, SERIES_REMOVE, DATA, GRAPHIC, UPDATE, AVERAGING, TIME_PERIOD, LOCATION, TRACK, MAP_PROVIDER,
                            TOOLTIP_TEXT, VALUE_CHANGING, VALUE_CHANGED, FLIP_START, FLIP_FINISHED,
                            SELECTED_CHART_DATA, BACKGROUND_IMAGE, REGIONS_ON_TOP, INFO_REGION_HANDLER, SVG_PATH_PRESSED,
                            CLEAR_DATA, HIGHLIGHT_SECTIONS, ANIMATED_ON, ANIMATED_OFF }

    private final EventType EVENT_TYPE;
    private final ChartData DATA;


    // ******************** Constructors **************************************
    public TileEvent(final EventType EVENT_TYPE) {
        this(EVENT_TYPE, null);
    }
    public TileEvent(final EventType EVENT_TYPE, final ChartData DATA) {
        this.EVENT_TYPE = EVENT_TYPE;
        this.DATA       = DATA;
    }


    // ******************** Methods *******************************************
    public EventType getEventType() { return EVENT_TYPE; }

    public ChartData getData() { return DATA; }
}
