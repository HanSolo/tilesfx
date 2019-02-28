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

import java.time.ZonedDateTime;


/**
 * Created by hansolo on 19.12.16.
 */
public class TimeEvent {
    public enum TimeEventType { HOUR, MINUTE, SECOND };
    public final ZonedDateTime TIME;
    public final TimeEventType TYPE;


    // ******************** Constructors **************************************
    public TimeEvent(final Object SRC, final ZonedDateTime TIME, final TimeEventType TYPE) {
        this.TIME = TIME;
        this.TYPE = TYPE;
    }
}