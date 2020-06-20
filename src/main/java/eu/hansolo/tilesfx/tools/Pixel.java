/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2016-2020 Gerrit Grunwald.
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


public class Pixel {
    private Color color;

    // ******************** Constructors **************************************
    public Pixel(final Color color) {
        this.color = null == color ? Color.BLACK : color;
    }


    // ******************** Methods *******************************************
    public Color getColor() { return color; }
    public void setColor(final Color color) { this.color = null == color ? Color.BLACK : color; }
}
