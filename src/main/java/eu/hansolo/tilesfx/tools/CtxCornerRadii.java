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
    private double upperLeft;
    private double upperRight;
    private double lowerRight;
    private double lowerLeft;


    // ******************** Constructors **************************************
    public CtxCornerRadii() {
        this(0, 0, 0, 0);
    }
    public CtxCornerRadii(final double RADIUS) {
        this(RADIUS, RADIUS, RADIUS, RADIUS);
    }
    public CtxCornerRadii(final double UPPER_LEFT, final double UPPER_RIGHT,
                          final double LOWER_RIGHT, final double LOWER_LEFT) {
        upperLeft  = UPPER_LEFT;
        upperRight = UPPER_RIGHT;
        lowerRight = LOWER_RIGHT;
        lowerLeft  = LOWER_LEFT;
    }


    // ******************** Methods *******************************************
    public double getUpperLeft() { return upperLeft; }
    public void setUpperLeft(final double VALUE) { upperLeft = Helper.clamp(0, Double.MAX_VALUE, VALUE); }

    public double getUpperRight() { return upperRight; }
    public void setUpperRight(final double VALUE) { upperRight = Helper.clamp(0, Double.MAX_VALUE, VALUE); }

    public double getLowerRight() { return lowerRight; }
    public void setLowerRight(final double VALUE) { lowerRight = Helper.clamp(0, Double.MAX_VALUE, VALUE); }

    public double getLowerLeft() { return lowerLeft; }
    public void setLowerLeft(final double VALUE) { lowerLeft = Helper.clamp(0, Double.MAX_VALUE, VALUE); }
}
