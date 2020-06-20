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
package eu.hansolo.tilesfx.events;

import javafx.geometry.Point2D;
import javafx.scene.input.MouseButton;


public class PixelMatrixEvent {
    private final int         X;
    private final int         Y;
    private final double      MOUSE_SCREEN_X;
    private final double      MOUSE_SCREEN_Y;
    private final MouseButton BUTTON;


    // ******************** Constructors **************************************
    public PixelMatrixEvent(final int X, final int Y, final double MOUSE_X, final double MOUSE_Y, final MouseButton BUTTON) {
        this.X              = X;
        this.Y              = Y;
        this.MOUSE_SCREEN_X = MOUSE_X;
        this.MOUSE_SCREEN_Y = MOUSE_Y;
        this.BUTTON         = BUTTON;
    }


    // ******************** Methods *******************************************
    public int getX() { return X; }
    public int getY() { return Y; }

    public double getMouseScreenX() { return MOUSE_SCREEN_X; }
    public double getMouseScreenY() { return MOUSE_SCREEN_Y; }
    public Point2D getMouseScreenPos() { return new Point2D(MOUSE_SCREEN_X, MOUSE_SCREEN_Y); }

    public MouseButton getMouseButton() { return BUTTON; }
}
