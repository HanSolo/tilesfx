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

public class CtxCornerRadii {
    private double topLeft;
    private double topRight;
    private double bottomRight;
    private double bottomLeft;


    // ******************** Constructors **************************************
    public CtxCornerRadii() {
        this(0, 0, 0, 0);
    }
    public CtxCornerRadii(final double RADIUS) {
        this(RADIUS, RADIUS, RADIUS, RADIUS);
    }
    public CtxCornerRadii(final double TOP_LEFT, final double TOP_RIGHT,
                          final double BOTTOM_RIGHT, final double BOTTOM_LEFT) {
        topLeft = TOP_LEFT;
        topRight = TOP_RIGHT;
        bottomRight = BOTTOM_RIGHT;
        bottomLeft = BOTTOM_LEFT;
    }


    // ******************** Methods *******************************************
    public double getTopLeft() { return topLeft; }
    public void setTopLeft(final double VALUE) { topLeft = Helper.clamp(0, Double.MAX_VALUE, VALUE); }

    public double getTopRight() { return topRight; }
    public void setTopRight(final double VALUE) { topRight = Helper.clamp(0, Double.MAX_VALUE, VALUE); }

    public double getBottomRight() { return bottomRight; }
    public void setBottomRight(final double VALUE) { bottomRight = Helper.clamp(0, Double.MAX_VALUE, VALUE); }

    public double getBottomLeft() { return bottomLeft; }
    public void setBottomLeft(final double VALUE) { bottomLeft = Helper.clamp(0, Double.MAX_VALUE, VALUE); }
}
