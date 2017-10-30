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

package eu.hansolo.tilesfx.tools;

import eu.hansolo.tilesfx.TimeSection;

import java.io.Serializable;
import java.util.Comparator;


/**
 * Created by hansolo on 31.01.16.
 */
public class TimeSectionComparator implements Comparator<TimeSection>, Serializable {
    @Override public int compare(final TimeSection SECTION_1, final TimeSection SECTION_2) {
        return SECTION_1.compareTo(SECTION_2);
    }
}

