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

package eu.hansolo.tilesfx;

import javafx.scene.control.Tooltip;
import javafx.scene.shape.SVGPath;

import java.util.Locale;


/**
 * Created by hansolo on 21.12.16.
 */
public class CountryPath extends SVGPath {
    private final String  NAME;
    private final Locale  LOCALE;
    private final Tooltip TOOLTIP;


    // ******************** Constructors **************************************
    public CountryPath(final String NAME) {
        this(NAME, null);
    }
    public CountryPath(final String NAME, final String CONTENT) {
        super();
        this.NAME    = NAME;
        this.LOCALE  = new Locale("", NAME);
        this.TOOLTIP = new Tooltip(LOCALE.getDisplayCountry());
        Tooltip.install(this, TOOLTIP);
        if (null == CONTENT) return;
        setContent(CONTENT);
    }


    // ******************** Methods *******************************************
    public String getName() { return NAME; }

    public Locale getLocale() { return LOCALE; }

    public Tooltip getTooltip() { return TOOLTIP; }
}
