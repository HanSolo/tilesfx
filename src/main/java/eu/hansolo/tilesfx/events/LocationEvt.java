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

import eu.hansolo.tilesfx.tools.Location;
import eu.hansolo.toolbox.evt.EvtPriority;
import eu.hansolo.toolbox.evt.EvtType;
import eu.hansolo.toolbox.evt.type.ChangeEvt;


/**
 * Created by hansolo on 12.02.17.
 */
public class LocationEvt extends TileEvt {
    public static final EvtType<LocationEvt> ANY      = new EvtType<>(ChangeEvt.ANY, "ANY");
    public static final EvtType<LocationEvt> LOCATION = new EvtType<>(LocationEvt.ANY, "LOCATION");

    private final Location location;


    // ******************** Constructors **************************************
    public LocationEvt(final EvtType<? extends LocationEvt> evtType, final Location location) {
        super(evtType);
        this.location = location;
    }
    public LocationEvt(final Object src, final EvtType<? extends LocationEvt> evtType, final Location location) {
        super(src, evtType);
        this.location = location;
    }
    public LocationEvt(final Object src, final EvtType<? extends LocationEvt> evtType, final EvtPriority priority, final Location location) {
        super(src, evtType, priority);
        this.location = location;
    }


    // ******************** Methods *******************************************
    public Location getLocation() { return location; }
}
