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

import eu.hansolo.tilesfx.Tile;
import eu.hansolo.toolbox.evt.EvtPriority;
import eu.hansolo.toolbox.evt.EvtType;
import eu.hansolo.toolboxfx.evt.type.LocationChangeEvt;
import eu.hansolo.toolboxfx.geom.Location;


public class LocationEvt extends LocationChangeEvt {
    private Tile tile;


    // ******************** Constructors **************************************
    public LocationEvt(final EvtType<? extends LocationChangeEvt> evtType, final Location oldLocation, final Location location, final Tile tile) {
        super(evtType, oldLocation, location);
        this.tile = tile;
    }
    public LocationEvt(final Object src, final EvtType<? extends LocationChangeEvt> evtType, final Location oldLocation, final Location location, final Tile tile) {
        super(src, evtType, oldLocation, location);
        this.tile = tile;
    }
    public LocationEvt(final Object src, final EvtType<? extends LocationChangeEvt> evtType, final EvtPriority priority, final Location oldLocation, final Location location, final Tile tile) {
        super(src, evtType, priority, oldLocation, location);
        this.tile = tile;
    }


    // ******************** Methods *******************************************
    public Tile getTile() { return tile; }
}
