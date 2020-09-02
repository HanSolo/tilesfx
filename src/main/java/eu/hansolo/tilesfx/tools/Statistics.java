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

import eu.hansolo.tilesfx.chart.ChartData;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Created by hansolo on 06.12.16.
 */
public class Statistics {


    // ******************** Methods *******************************************
    public static final double getChartDataMean(final List<ChartData> DATA) {
        return getMean(DATA.stream().map(ChartData::getValue).collect(Collectors.toList()));
    }
    public static final double getMean(final List<Double> DATA) { return DATA.stream().mapToDouble(v -> v).sum() / DATA.size(); }

    public static final double getChartDataVariance(final List<ChartData> DATA) {
        return getVariance(DATA.stream().map(ChartData::getValue).collect(Collectors.toList()));
    }
    public static final double getVariance(final List<Double> DATA) {
        double mean = getMean(DATA);
        double temp = 0;
        for (double a : DATA) { temp += ((a - mean) * (a - mean)); }
        return temp / DATA.size();
    }

    public static final double getChartDataStdDev(final List<ChartData> DATA) {
        return getStdDev(DATA.stream().map(ChartData::getValue).collect(Collectors.toList()));
    }
    public static final double getStdDev(final List<Double> DATA) { return Math.sqrt(getVariance(DATA)); }

    public static final double getChartDataMedian(final List<ChartData> DATA) {
        return getMedian(DATA.stream().map(ChartData::getValue).collect(Collectors.toList()));
    }
    public static final double getMedian(final List<Double> DATA) {
        int size = DATA.size();
        Collections.sort(DATA);
        return size % 2 == 0 ? (DATA.get((size / 2) - 1) + DATA.get(size / 2)) / 2.0 : DATA.get(size / 2);
    }

    public static final double getChartDataMin(final List<ChartData> DATA) {
        return getMin(DATA.stream().map(ChartData::getValue).collect(Collectors.toList()));
    }
    public static final double getMin(final List<Double> DATA) { return DATA.stream().mapToDouble(v -> v).min().orElse(0); }

    public static final double getChartDataMax(final List<ChartData> DATA) {
        return getMax(DATA.stream().map(ChartData::getValue).collect(Collectors.toList()));
    }
    public static final double getMax(final List<Double> DATA) { return DATA.stream().mapToDouble(v -> v).max().orElse(0); }

    public static final double getChartDataAverage(final List<ChartData> DATA) {
        return getAverage(DATA.stream().map(ChartData::getValue).collect(Collectors.toList()));
    }
    public static final double getAverage(final List<Double> DATA) {
        return DATA.stream().mapToDouble(data -> data.doubleValue()).average().orElse(-1);
    }

    public static final double percentile(List<Double> entries, double percentile) {
        Collections.sort(entries);
        int index = (int) Math.ceil(percentile / 100.0 * entries.size());
        return entries.get(index-1);
    }

    public static final Map<LocalTime, DataPoint> analyze(final List<ChartData> entries) {
        if (entries.isEmpty()) { return new HashMap(); }
        final Map<LocalTime, DataPoint> dataMap = new HashMap<>();
        final Instant                 now     = Instant.now();

        for (int hour = 0 ; hour < 24 ; hour++) {
            for (int bucket = 0 ; bucket < 60 ; bucket += 10) {
                final int h = hour;
                final int m = bucket;
                List<ChartData> bucketEntries = entries.stream().filter(entry -> entry.getTimestamp().atZone(ZoneOffset.systemDefault()).getHour() == h &&
                    entry.getTimestamp().atZone(ZoneOffset.systemDefault()).getMinute() <= m).collect(Collectors.toList());
                final double minBucketValue = bucketEntries.stream().min(Comparator.comparingDouble(ChartData::getValue)).get().getValue();
                final double maxBucketValue = bucketEntries.stream().max(Comparator.comparingDouble(ChartData::getValue)).get().getValue();
                final double avgBucketValue = minBucketValue + (maxBucketValue - minBucketValue) / 2.0;

                final LocalTime key   = LocalTime.of(h, m, 0);
                final DataPoint value = new DataPoint(minBucketValue, maxBucketValue, avgBucketValue);

                dataMap.put(key, value);
            }
        }

        return dataMap;
    }
}
