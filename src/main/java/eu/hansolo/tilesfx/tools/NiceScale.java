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

/**
 * Created by hansolo on 23.04.17.
 */
public class NiceScale {

    private double min;
    private double max;
    private double maxTicks = 10;
    private double tickSpacing;
    private double range;
    private double niceMin;
    private double niceMax;


    public NiceScale(final double MIN, final double MAX) {
        min = MIN;
        max = MAX;
        calculate();
    }

    private void calculate() {
        range       = niceNum(max - min, false);
        tickSpacing = niceNum(range / (maxTicks - 1), true);
        niceMin     = Math.floor(min / tickSpacing) * tickSpacing;
        niceMax     = Math.ceil(max / tickSpacing) * tickSpacing;
    }

    private double niceNum(final double RANGE, final boolean ROUND) {
        double exponent;     // exponent of RANGE
        double fraction;     // fractional part of RANGE
        double niceFraction; // nice, rounded fraction

        exponent = Math.floor(Math.log10(RANGE));
        fraction = RANGE / Math.pow(10, exponent);

        if (ROUND) {
            if (fraction < 1.5)
                niceFraction = 1;
            else if (fraction < 3)
                niceFraction = 2;
            else if (fraction < 7)
                niceFraction = 5;
            else
                niceFraction = 10;
        } else {
            if (fraction <= 1)
                niceFraction = 1;
            else if (fraction <= 2)
                niceFraction = 2;
            else if (fraction <= 5)
                niceFraction = 5;
            else
                niceFraction = 10;
        }
        return niceFraction * Math.pow(10, exponent);
    }

    public void setMinMax(final double MIN, final double MAX) {
        min = MIN;
        max = MAX;
        calculate();
    }

    public void setMaxTicks(final double MAX_TICKS) {
        maxTicks = MAX_TICKS;
        calculate();
    }

    public double getTickSpacing() { return tickSpacing; }

    public double getNiceMin() { return niceMin; }

    public double getNiceMax() { return niceMax; }
}
