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

import eu.hansolo.tilesfx.chart.ChartData;


/**
 * Created by hansolo on 17.02.17.
 */
public class ChartDataEvent {
    public enum EventType { UPDATE, FINISHED }

    private ChartData data;
    private EventType type;


    // ******************** Constructors **************************************
    public ChartDataEvent(final EventType TYPE, final ChartData DATA) {
        type = TYPE;
        data = DATA;
    }


    // ******************** Methods *******************************************
    public EventType getType() { return type; }

    public ChartData getData() { return data; }
}
