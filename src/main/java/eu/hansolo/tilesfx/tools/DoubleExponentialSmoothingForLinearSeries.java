/*
 * Copyright (c) 2019 by Gerrit Grunwald
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

import java.util.Arrays;


public class DoubleExponentialSmoothingForLinearSeries {

    /**
     * Performs double exponential smoothing for given time series.
     * <p/>
     * This method is suitable for fitting series with linear trend.
     *
     * @param data  An array containing the recorded data of the time series
     * @param alpha Smoothing factor for data (0 < alpha < 1)
     * @param beta  Smoothing factor for trend (0 < beta < 1)
     *
     * @return Instance of model that can be used to forecast future values
     */
    public static Model fit(final double[] data, final double alpha, final double beta) {
        validateParams(alpha, beta);                        //validating values of alpha and beta

        double[] smoothedData = new double[data.length];    //array to store smoothed values

        double[] trends = new double[data.length + 1];
        double[] levels = new double[data.length + 1];

        //initializing values of parameters
        smoothedData[0] = data[0];
        trends[0] = data[1] - data[0];
        levels[0] = data[0];

        for (int t = 0 ; t < data.length ; t++) {
            smoothedData[t] = trends[t] + levels[t];
            levels[t + 1] = alpha * data[t] + (1 - alpha) * (levels[t] + trends[t]);
            trends[t + 1] = beta * (levels[t + 1] - levels[t]) + (1 - beta) * trends[t];
        }
        return new Model(smoothedData, trends, levels, calculateSSE(data, smoothedData));
    }

    private static double calculateSSE(double[] data, double[] smoothedData) {
        double sse = 0;
        for (int i = 0 ; i < data.length ; i++) {
            sse += Math.pow(smoothedData[i] - data[i], 2);
        }
        return sse;
    }

    private static void validateParams(double alpha, double beta) {
        if (alpha < 0 || alpha > 1) {
            throw new RuntimeException("The value of alpha must be between 0 and 1");
        }

        if (beta < 0 || beta > 1) {
            throw new RuntimeException("The value of beta must be between 0 and 1");
        }
    }

    public static void main(String[] args) {
        //double[] testData = {17.55, 21.86, 23.89, 26.93, 26.89, 28.83, 30.08, 30.95, 30.19, 31.58, 32.58, 33.48, 39.02, 41.39, 41.60};

        double[] testData = { 128.0, 135.0, 131.0, 205.0, 173.0, 184.0 };

        Model model = DoubleExponentialSmoothingForLinearSeries.fit(testData, 0.8, 0.2);

        System.out.println("Input values: " + Arrays.toString(testData));
        System.out.println("Smoothed values: " + Arrays.toString(model.getSmoothedData()));
        System.out.println("Trend: " + Arrays.toString(model.getTrend()));
        System.out.println("Level: " + Arrays.toString(model.getLevel()));
        System.out.println("Sum of squared error: " + model.getSSE());
        System.out.println("Forecast: " + Arrays.toString(model.forecast(3)));
    }


    public static class Model {
        private final double[] smoothedData;
        private final double[] trends;
        private final double[] levels;
        private final double   sse;

        public Model(final double[] smoothedData, final double[] trends, final double[] levels, final double sse) {
            this.smoothedData = smoothedData;
            this.trends       = trends;
            this.levels       = levels;
            this.sse          = sse;
        }


        /**
         * Forecasts future values.
         *
         * @param size no of future values that you need to forecast
         *
         * @return forecast data
         */
        public double[] forecast(final int size) {
            double[] forecastData = new double[size];
            for (int i = 0 ; i < size ; i++) {
                forecastData[i] = levels[levels.length - 1] + (i + 1) * trends[trends.length - 1];
            }
            return forecastData;
        }

        public double[] getSmoothedData() { return smoothedData; }

        public double[] getTrend() { return trends; }

        public double[] getLevel() { return levels; }

        public double getSSE() { return sse; }
    }
}
