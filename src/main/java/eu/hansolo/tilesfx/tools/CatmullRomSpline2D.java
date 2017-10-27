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

public class CatmullRomSpline2D {
    private CatmullRomSpline splineXValues;
    private CatmullRomSpline splineYValues;


    // ******************** Constructors **************************************
    public CatmullRomSpline2D(final Point P0, final Point P1, final Point P2, final Point P3) {
        assert P0 != null : "p0 cannot be null";
        assert P1 != null : "p1 cannot be null";
        assert P2 != null : "p2 cannot be null";
        assert P3 != null : "p3 cannot be null";

        splineXValues = new CatmullRomSpline(P0.getX(), P1.getX(), P2.getX(), P3.getX());
        splineYValues = new CatmullRomSpline(P0.getY(), P1.getY(), P2.getY(), P3.getY());
    }


    // ******************** Methods *******************************************
    public Point q(final double T) { return new Point(splineXValues.q(T), splineYValues.q(T)); }
}
