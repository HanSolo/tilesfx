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

public class CatmullRomSpline {
    private double p0;
    private double p1;
    private double p2;
    private double p3;


    // ******************** Constructors **************************************
    public CatmullRomSpline(final double P0, final double P1, final double P2, final double P3) {
        p0 = P0;
        p1 = P1;
        p2 = P2;
        p3 = P3;
    }


    // ******************** Methods *******************************************
    public double q(final double T) {
        return 0.5 * ((2 * p1) + (p2 - p0) * T + (2 * p0 - 5 * p1 + 4 * p2 - p3) * T * T + (3 * p1 -p0 - 3 * p2 + p3) * T * T * T);
    }

    public double getP0() { return p0; }
    public void setP0(final double P0) { p0 = P0; }

    public double getP1() { return p1; }
    public void setP1(final double P1) { p1 = P1; }

    public double getP2() { return p2; }
    public void setP2(final double P2) { p2 = P2; }

    public double getP3() { return p3; }
    public void setP3(final double P3) { p3 = P3; }
}
