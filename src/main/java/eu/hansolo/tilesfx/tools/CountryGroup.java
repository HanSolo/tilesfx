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
package eu.hansolo.tilesfx.tools;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class CountryGroup {
    private String        name;
    private List<Country> countries;


    // ******************** Constructors **************************************
    public CountryGroup(final String NAME, final Country... COUNTRIES) {
        name      = NAME;
        countries = new ArrayList<>(COUNTRIES.length);
        countries.addAll(Arrays.stream(COUNTRIES).toList());
    }


    // ******************** Methods *******************************************
    public String getName() { return name; }

    public List<Country> getCountries() { return countries; }

    public void setColor(final Color COLOR) {
        for (Country country : getCountries()) { country.setFill(COLOR); }
    }
}