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

import eu.hansolo.toolbox.evt.EvtPriority;
import eu.hansolo.toolbox.evt.EvtType;
import eu.hansolo.toolbox.evt.type.ChangeEvt;

import java.time.ZonedDateTime;


public class TimeEvt extends TileEvt {
    public static final EvtType<TimeEvt> ANY    = new EvtType<>(ChangeEvt.ANY, "ANY");
    public static final EvtType<TimeEvt> HOUR   = new EvtType<>(TimeEvt.ANY, "HOUR");
    public static final EvtType<TimeEvt> MINUTE = new EvtType<>(TimeEvt.ANY, "MINUTE");
    public static final EvtType<TimeEvt> SECOND = new EvtType<>(TimeEvt.ANY, "SECOND");

    public final ZonedDateTime time;


    // ******************** Constructors **************************************
    public TimeEvt(final EvtType<? extends TimeEvt> evtType, final ZonedDateTime time) {
        super(evtType);
        this.time = time;
    }
    public TimeEvt(final Object src, final EvtType<? extends TimeEvt> evtType, final ZonedDateTime time) {
        super(src, evtType);
        this.time = time;
    }
    public TimeEvt(final Object src, final EvtType<? extends TimeEvt> evtType, final EvtPriority priority, final ZonedDateTime time) {
        super(src, evtType, priority);
        this.time = time;
    }


    // ******************** Methods *******************************************
    public ZonedDateTime getTime() { return time; }
}
