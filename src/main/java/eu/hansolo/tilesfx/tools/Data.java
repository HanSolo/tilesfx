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

package eu.hansolo.tilesfx.tools;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;


/**
 * Created by hansolo on 01.11.16.
 */
public class Data {
    private final Instant timestamp;
    private final double  value;


    // ******************** Constructors **************************************
    public Data(final double VALUE) {
        value     = VALUE;
        timestamp = Instant.now();
    }


    // ******************** Methods *******************************************
    public double getValue() { return value; }

    public Instant getTimestamp() { return timestamp; }

    public ZonedDateTime getTimestampAsDateTime(final ZoneId ZONE_ID) { return ZonedDateTime.ofInstant(timestamp, ZONE_ID); }

    @Override  public String toString() {
        return new StringBuilder().append("{\n")
                                  .append("  \"timestamp\":").append(timestamp.getEpochSecond()).append(",\n")
                                  .append("  \"value\":").append(value).append(",\n")
                                  .append("}")
                                  .toString();
    }
}
