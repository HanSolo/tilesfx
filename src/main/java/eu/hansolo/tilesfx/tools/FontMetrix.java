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

import javafx.geometry.Bounds;
import javafx.scene.text.Font;
import javafx.scene.text.Text;


public class FontMetrix {
    private final Text   internalText;
    private       double ascent;
    private       double descent;
    private       double lineHeight;


    public FontMetrix(final Font font) {
        internalText = new Text();
        internalText.setFont(font);
        final Bounds bounds = internalText.getLayoutBounds();
        lineHeight = bounds.getHeight();
        ascent     = -bounds.getMinY();
        descent    = bounds.getMaxY();
    }


    public double getAscent() { return ascent; }

    public double getDescent() { return descent; }

    public double getLineHeight() { return lineHeight; }

    public double computeStringWidth(final String text) {
        internalText.setText(text);
        return internalText.getLayoutBounds().getWidth();
    }
}
