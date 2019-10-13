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

import eu.hansolo.tilesfx.chart.ChartData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Created by hansolo on 06.12.16.
 */
public class Statistics {


    // ******************** Methods *******************************************
    public static final double getChartDataMean(final List<ChartData> CHART_DATA) {
        List<Double> data = new ArrayList<>();
        for (ChartData chartData : CHART_DATA) { data.add(chartData.getValue()); }
        return getMean(data);
    }
    public static final double getMean(final List<Double> DATA) {
        double sum = 0.0;
        for (Double v : DATA) {
            double v1 = v;
            sum += v1;
        }
        return sum / DATA.size(); }

    public static final double getChartDataVariance(final List<ChartData> CHART_DATA) {
        List<Double> data = new ArrayList<>();
        for (ChartData chartData : CHART_DATA) { data.add(chartData.getValue()); }
        return getVariance(data);
    }
    public static final double getVariance(final List<Double> DATA) {
        double mean = getMean(DATA);
        double temp = 0;
        for (double a : DATA) { temp += ((a - mean) * (a - mean)); }
        return temp / DATA.size();
    }

    public static final double getChartDataStdDev(final List<ChartData> CHART_DATA) {
        List<Double> data = new ArrayList<>();
        for (ChartData chartData : CHART_DATA) { data.add(chartData.getValue()); }
        return getStdDev(data);
    }
    public static final double getStdDev(final List<Double> DATA) { return Math.sqrt(getVariance(DATA)); }

    public static final double getChartDataMedian(final List<ChartData> CHART_DATA) {
        List<Double> data = new ArrayList<>();
        for (ChartData chartData : CHART_DATA) { data.add(chartData.getValue()); }
        return getMedian(data);
    }
    public static final double getMedian(final List<Double> DATA) {
        int size = DATA.size();
        Collections.sort(DATA);
        return size % 2 == 0 ? (DATA.get((size / 2) - 1) + DATA.get(size / 2)) / 2.0 : DATA.get(size / 2);
    }

    public static final double getChartDataMin(final List<ChartData> CHART_DATA) {
        List<Double> data = new ArrayList<>();
        for (ChartData chartData : CHART_DATA) { data.add(chartData.getValue()); }
        return getMin(data);
    }
    public static final double getMin(final List<Double> DATA) {
        boolean seen = false;
        double  best = 0;
        for (Double v : DATA) {
            double v1 = v;
            if (!seen || Double.compare(v1, best) < 0) {
                seen = true;
                best = v1;
            }
        }
        return seen ? best : 0; }

    public static final double getChartDataMax(final List<ChartData> CHART_DATA) {
        List<Double> data = new ArrayList<>();
        for (ChartData chartData : CHART_DATA) { data.add(chartData.getValue()); }
        return getMax(data);
    }
    public static final double getMax(final List<Double> DATA) {
        boolean seen = false;
        double  best = 0;
        for (Double v : DATA) {
            double v1 = v;
            if (!seen || Double.compare(v1, best) > 0) {
                seen = true;
                best = v1;
            }
        }
        return seen ? best : 0; }
}
