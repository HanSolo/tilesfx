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
import eu.hansolo.toolbox.evt.EvtPriority;
import eu.hansolo.toolbox.evt.EvtType;
import eu.hansolo.toolbox.evt.type.ChangeEvt;


public class TileEvt extends ChangeEvt {
    public static final EvtType<TileEvt> ANY                      = new EvtType<>(ChangeEvt.ANY, "ANY");
    public static final EvtType<TileEvt> SHOW_NOTIFY_REGION       = new EvtType<>(TileEvt.ANY, "SHOW_NOTIFY_REGION");
    public static final EvtType<TileEvt> HIDE_NOTIFY_REGION       = new EvtType<>(TileEvt.ANY, "HIDE_NOTIFY_REGION");
    public static final EvtType<TileEvt> SHOW_INFO_REGION         = new EvtType<>(TileEvt.ANY, "SHOW_INFO_REGION");
    public static final EvtType<TileEvt> HIDE_INFO_REGION         = new EvtType<>(TileEvt.ANY, "HIDE_INFO_REGION");
    public static final EvtType<TileEvt> SHOW_LOWER_RIGHT_REGION  = new EvtType<>(TileEvt.ANY, "SHOW_LOWER_RIGHT_REGION");
    public static final EvtType<TileEvt> HIDE_LOWER_RIGHT_REGION  = new EvtType<>(TileEvt.ANY, "HIDE_LOWER_RIGHT_REGION");
    public static final EvtType<TileEvt> RECALC                   = new EvtType<>(TileEvt.ANY, "RECALC");
    public static final EvtType<TileEvt> REDRAW                   = new EvtType<>(TileEvt.ANY, "REDRAW");
    public static final EvtType<TileEvt> REFRESH                  = new EvtType<>(TileEvt.ANY, "REFRESH");
    public static final EvtType<TileEvt> RESIZE                   = new EvtType<>(TileEvt.ANY, "RESIZE");
    public static final EvtType<TileEvt> VISIBILITY               = new EvtType<>(TileEvt.ANY, "VISIBILITY");
    public static final EvtType<TileEvt> SECTION                  = new EvtType<>(TileEvt.ANY, "SECTION");
    public static final EvtType<TileEvt> ALERT                    = new EvtType<>(TileEvt.ANY, "ALERT");
    public static final EvtType<TileEvt> VALUE                    = new EvtType<>(TileEvt.ANY, "VALUE");
    public static final EvtType<TileEvt> THRESHOLD_EXCEEDED       = new EvtType<>(TileEvt.ANY, "THRESHOLD_EXCEEDED");
    public static final EvtType<TileEvt> THRESHOLD_UNDERRUN       = new EvtType<>(TileEvt.ANY, "THRESHOLD_UNDERRUN");
    public static final EvtType<TileEvt> LOWER_THRESHOLD_EXCEEDED = new EvtType<>(TileEvt.ANY, "LOWER_THRESHOLD_EXCEEDED");
    public static final EvtType<TileEvt> LOWER_THRESHOLD_UNDERRUN = new EvtType<>(TileEvt.ANY, "LOWER_THRESHOLD_UNDERRUN");
    public static final EvtType<TileEvt> MAX_VALUE_EXCEEDED       = new EvtType<>(TileEvt.ANY, "MAX_VALUE_EXCEEDED");
    public static final EvtType<TileEvt> MIN_VALUE_UNDERRUN       = new EvtType<>(TileEvt.ANY, "MIN_VALUE_UNDERRUN");
    public static final EvtType<TileEvt> VALUE_IN_RANGE           = new EvtType<>(TileEvt.ANY, "VALUE_IN_RANGE");
    public static final EvtType<TileEvt> FINISHED                 = new EvtType<>(TileEvt.ANY, "FINISHED");
    public static final EvtType<TileEvt> SERIES                   = new EvtType<>(TileEvt.ANY, "SERIES");
    public static final EvtType<TileEvt> SERIES_SET               = new EvtType<>(TileEvt.ANY, "SERIES_SET");
    public static final EvtType<TileEvt> SERIES_ADD               = new EvtType<>(TileEvt.ANY, "SERIES_ADD");
    public static final EvtType<TileEvt> SERIES_REMOVE            = new EvtType<>(TileEvt.ANY, "SERIES_REMOVE");
    public static final EvtType<TileEvt> DATA                     = new EvtType<>(TileEvt.ANY, "DATA");
    public static final EvtType<TileEvt> GRAPHIC                  = new EvtType<>(TileEvt.ANY, "GRAPHIC");
    public static final EvtType<TileEvt> UPDATE                   = new EvtType<>(TileEvt.ANY, "UPDATE");
    public static final EvtType<TileEvt> AVERAGING                = new EvtType<>(TileEvt.ANY, "AVERAGING");
    public static final EvtType<TileEvt> TIME_PERIOD              = new EvtType<>(TileEvt.ANY, "TIME_PERIOD");
    public static final EvtType<TileEvt> LOCATION                 = new EvtType<>(TileEvt.ANY, "LOCATION");
    public static final EvtType<TileEvt> TRACK                    = new EvtType<>(TileEvt.ANY, "TRACK");
    public static final EvtType<TileEvt> MAP_PROVIDER             = new EvtType<>(TileEvt.ANY, "MAP_PROVIDER");
    public static final EvtType<TileEvt> TOOLTIP_TEXT             = new EvtType<>(TileEvt.ANY, "TOOLTIP_TEXT");
    public static final EvtType<TileEvt> VALUE_CHANGING           = new EvtType<>(TileEvt.ANY, "VALUE_CHANGING");
    public static final EvtType<TileEvt> VALUE_CHANGED            = new EvtType<>(TileEvt.ANY, "VALUE_CHANGED");
    public static final EvtType<TileEvt> FLIP_START               = new EvtType<>(TileEvt.ANY, "FLIP_START");
    public static final EvtType<TileEvt> FLIP_FINISHED            = new EvtType<>(TileEvt.ANY, "FLIP_FINISHED");
    public static final EvtType<TileEvt> SELECTED_CHART_DATA      = new EvtType<>(TileEvt.ANY, "SELECTED_CHART_DATA");
    public static final EvtType<TileEvt> BACKGROUND_IMAGE         = new EvtType<>(TileEvt.ANY, "BACKGROUND_IMAGE");
    public static final EvtType<TileEvt> REGIONS_ON_TOP           = new EvtType<>(TileEvt.ANY, "REGIONS_ON_TOP");
    public static final EvtType<TileEvt> INFO_REGION_HANDLER      = new EvtType<>(TileEvt.ANY, "INFO_REGION_HANDLER");
    public static final EvtType<TileEvt> SVG_PATH_PRESSED         = new EvtType<>(TileEvt.ANY, "SVG_PATH_PRESSED");
    public static final EvtType<TileEvt> CLEAR_DATA               = new EvtType<>(TileEvt.ANY, "CLEAR_DATA");
    public static final EvtType<TileEvt> HIGHLIGHT_SECTIONS       = new EvtType<>(TileEvt.ANY, "HIGHLIGHT_SECTIONS");
    public static final EvtType<TileEvt> ANIMATED_ON              = new EvtType<>(TileEvt.ANY, "ANIMATED_ON");
    public static final EvtType<TileEvt> ANIMATED_OFF             = new EvtType<>(TileEvt.ANY, "ANIMATED_OFF");


    private final ChartData data;


    // ******************** Constructors **************************************
    public TileEvt(final EvtType<? extends TileEvt> evtType, final ChartData data) {
        super(evtType);
        this.data = data;
    }
    public TileEvt(final Object src, final EvtType<? extends TileEvt> evtType) {
        super(src, evtType);
        this.data = null;
    }
    public TileEvt(final Object src, final EvtType<? extends TileEvt> evtType, final ChartData data) {
        super(src, evtType);
        this.data = data;
    }
    public TileEvt(final Object src, final EvtType<? extends TileEvt> evtType, final EvtPriority priority) {
        super(src, evtType, priority);
        this.data = null;
    }
    public TileEvt(final Object src, final EvtType<? extends TileEvt> evtType, final EvtPriority priority, final ChartData data) {
        super(src, evtType, priority);
        this.data = data;
    }


    // ******************** Methods *******************************************
    public EvtType<? extends TileEvt> getEvtType() { return (EvtType<? extends TileEvt>) super.getEvtType(); }

    public ChartData getData() { return data; }
}
