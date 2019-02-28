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

import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;


/**
 * Created by hansolo on 25.12.16.
 */
public class GradientLookup {
    private Map<Double, Stop> stops;


    // ******************** Constructors **************************************
    public GradientLookup () {
        this(new Stop[]{});
    }
    public GradientLookup(final Stop... STOPS) {
        this(Arrays.asList(STOPS));
    }
    public GradientLookup(final List<Stop> STOPS) {
        stops = new TreeMap<>();
        for (Stop stop : STOPS) { stops.put(stop.getOffset(), stop); }
        init();
    }


    // ******************** Initialization ************************************
    private void init() {
        if (stops.isEmpty()) return;

        double minFraction = Collections.min(stops.keySet());
        double maxFraction = Collections.max(stops.keySet());

        if (Double.compare(minFraction, 0) > 0) { stops.put(0.0, new Stop(0.0, stops.get(minFraction).getColor())); }
        if (Double.compare(maxFraction, 1) < 0) { stops.put(1.0, new Stop(1.0, stops.get(maxFraction).getColor())); }
    }


    // ******************** Methods *******************************************
    public Color getColorAt(final double POSITION_OF_COLOR) {
        if (stops.isEmpty()) return Color.BLACK;

        final double POSITION = Helper.clamp(0.0, 1.0, POSITION_OF_COLOR);
        final Color COLOR;
        if (stops.size() == 1) {
            final Map<Double, Color> ONE_ENTRY = (Map<Double, Color>) stops.entrySet().iterator().next();
            COLOR = stops.get(ONE_ENTRY.keySet().iterator().next()).getColor();
        } else {
            Stop lowerBound = stops.get(0.0);
            Stop upperBound = stops.get(1.0);
            for (Double fraction : stops.keySet()) {
                if (Double.compare(fraction,POSITION) < 0) {
                    lowerBound = stops.get(fraction);
                }
                if (Double.compare(fraction, POSITION) > 0) {
                    upperBound = stops.get(fraction);
                    break;
                }
            }
            COLOR = interpolateColor(lowerBound, upperBound, POSITION);
        }
        return COLOR;
    }

    public List<Stop> getStops() { return new ArrayList<>(stops.values()); }
    public void setStops(final Stop... STOPS) { setStops(Arrays.asList(STOPS)); }
    public void setStops(final List<Stop> STOPS) {
        stops.clear();
        for (Stop stop : STOPS) { stops.put(stop.getOffset(), stop); }
        init();
    }

    public Stop getStopAt(final double POSITION_OF_STOP) {
        if (stops.isEmpty()) { throw new IllegalArgumentException("GradientStop stops should not be empty"); };

        final double POSITION = Helper.clamp(0.0, 1.0, POSITION_OF_STOP);

        Stop stop = null;
        double distance = Math.abs(stops.get(Double.valueOf(0)).getOffset() - POSITION);
        for(Entry<Double, Stop> entry : stops.entrySet()) {
            double cdistance = Math.abs(entry.getKey() - POSITION);
            if (cdistance < distance) {
                stop = stops.get(entry.getKey());
                distance = cdistance;
            }
        }
        return stop;
    }

    public List<Stop> getStopsBetween(final double MIN_OFFSET, final double MAX_OFFSET) {
        List<Stop> selectedStops = new ArrayList<>();
        for (Entry<Double, Stop> entry : stops.entrySet()) {
            if (entry.getValue().getOffset() >= MIN_OFFSET && entry.getValue().getOffset() <= MAX_OFFSET) { selectedStops.add(entry.getValue()); }
        }
        return selectedStops;
    }

    private Color interpolateColor(final Stop LOWER_BOUND, final Stop UPPER_BOUND, final double POSITION) {
        final double POS  = (POSITION - LOWER_BOUND.getOffset()) / (UPPER_BOUND.getOffset() - LOWER_BOUND.getOffset());

        final double DELTA_RED     = (UPPER_BOUND.getColor().getRed()     - LOWER_BOUND.getColor().getRed())     * POS;
        final double DELTA_GREEN   = (UPPER_BOUND.getColor().getGreen()   - LOWER_BOUND.getColor().getGreen())   * POS;
        final double DELTA_BLUE    = (UPPER_BOUND.getColor().getBlue()    - LOWER_BOUND.getColor().getBlue())    * POS;
        final double DELTA_OPACITY = (UPPER_BOUND.getColor().getOpacity() - LOWER_BOUND.getColor().getOpacity()) * POS;

        double red     = Helper.clamp(0.0, 1.0, (LOWER_BOUND.getColor().getRed()     + DELTA_RED));
        double green   = Helper.clamp(0.0, 1.0, (LOWER_BOUND.getColor().getGreen()   + DELTA_GREEN));
        double blue    = Helper.clamp(0.0, 1.0, (LOWER_BOUND.getColor().getBlue()    + DELTA_BLUE));
        double opacity = Helper.clamp(0.0, 1.0, (LOWER_BOUND.getColor().getOpacity() + DELTA_OPACITY));

        return Color.color(red, green, blue, opacity);
    }
}