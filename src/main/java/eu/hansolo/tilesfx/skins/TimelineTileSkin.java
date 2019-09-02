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

package eu.hansolo.tilesfx.skins;

import eu.hansolo.tilesfx.Section;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.chart.ChartData;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.DoubleExponentialSmoothingForLinearSeries;
import eu.hansolo.tilesfx.tools.DoubleExponentialSmoothingForLinearSeries.Model;
import eu.hansolo.tilesfx.tools.GradientLookup;
import eu.hansolo.tilesfx.tools.Helper;
import eu.hansolo.tilesfx.tools.MovingAverage;
import eu.hansolo.tilesfx.tools.NiceScale;
import eu.hansolo.tilesfx.tools.Point;
import eu.hansolo.tilesfx.tools.Statistics;
import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static eu.hansolo.tilesfx.tools.Helper.clamp;
import static eu.hansolo.tilesfx.tools.Helper.enableNode;


public class TimelineTileSkin extends TileSkin {
    private static final int                     SEC_MONTH     = 2_592_000;
    private static final int                     SEC_DAY       = 86_400;
    private static final int                     SEC_HOUR      = 3_600;
    private static final int                     SEC_MINUTE    = 60;
    private              DateTimeFormatter       timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private              Text                    titleText;
    private              Text                    valueText;
    private              Text                    upperUnitText;
    private              Line                    fractionLine;
    private              Text                    unitText;
    private              TextFlow                unitFlow;
    private              TextFlow                valueUnitFlow;
    private              Text                    averageText;
    private              Text                    minText;
    private              Text                    maxText;
    private              Text                    highText;
    private              Text                    lowText;
    private              Text                    text;
    private              Text                    timeSpanText;
    private              Rectangle               graphBounds;
    private              List<PathElement>       pathElements;
    private              Path                    sparkLine;
    private              Circle                  dot;
    private              Rectangle               stdDeviationArea;
    private              Line                    thresholdLine;
    private              Line                    lowerThresholdLine;
    private              Line                    averageLine;
    private              Group                   sectionGroup;
    private              Map<Section, Rectangle> sections;
    private              LinearGradient          gradient;
    private              GradientLookup          gradientLookup;
    private              double                  low;
    private              double                  high;
    private              double                  stdDeviation;
    private              int                     noOfDatapoints;
    private              List<ChartData>         dataList;
    private              Duration                timePeriod;
    private              MovingAverage           movingAverage;
    private              InvalidationListener    periodListener;
    private              NiceScale               niceScaleY;
    private              List<Line>              horizontalTickLines;
    private              double                  horizontalLineOffset;
    private              double                  tickLabelFontSize;
    private              List<Text>              tickLabelsY;
    private              Color                   tickLineColor;
    private              Color                   tickLabelColor;
    private              Text                    trendText;


    // ******************** Constructors **************************************
    public TimelineTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        periodListener = o -> handleEvents("PERIOD");

        timeFormatter = DateTimeFormatter.ofPattern("HH:mm", tile.getLocale());

        if (tile.isAutoScale()) { tile.calcAutoScale(); }

        niceScaleY = new NiceScale(tile.getMinValue(), tile.getMaxValue());
        niceScaleY.setMaxTicks(5);
        tickLineColor       = Color.color(tile.getChartGridColor().getRed(), tile.getChartGridColor().getGreen(), tile.getChartGridColor().getBlue(), 0.5);
        tickLabelColor      = tile.getTickLabelColor();
        horizontalTickLines = new ArrayList<>(5);
        tickLabelsY         = new ArrayList<>(5);
        for (int i = 0 ; i < 5 ; i++) {
            Line hLine = new Line(0, 0, 0, 0);
            hLine.getStrokeDashArray().addAll(1.0, 2.0);
            hLine.setStroke(Color.TRANSPARENT);
            horizontalTickLines.add(hLine);
            Text tickLabelY = new Text("");
            tickLabelY.setFill(Color.TRANSPARENT);
            tickLabelsY.add(tickLabelY);
        }

        gradientLookup = new GradientLookup(tile.getGradientStops());
        low            = tile.getMaxValue();
        high           = tile.getMinValue();
        stdDeviation   = 0;
        movingAverage  = tile.getMovingAverage();
        dataList       = new LinkedList<>();
        timePeriod     = tile.getTimePeriod();
        noOfDatapoints = (int) timePeriod.getSeconds();

        graphBounds = new Rectangle(PREFERRED_WIDTH * 0.05, PREFERRED_HEIGHT * 0.5, PREFERRED_WIDTH * 0.9, PREFERRED_HEIGHT * 0.45);

        titleText = new Text(tile.getTitle());
        titleText.setFill(tile.getTitleColor());
        Helper.enableNode(titleText, !tile.getTitle().isEmpty());

        valueText = new Text(String.format(locale, formatString, tile.getValue()));
        valueText.setFill(tile.getValueColor());
        Helper.enableNode(valueText, tile.isValueVisible());

        upperUnitText = new Text("");
        upperUnitText.setFill(tile.getUnitColor());
        Helper.enableNode(upperUnitText, !tile.getUnit().isEmpty());

        fractionLine = new Line();

        unitText = new Text(tile.getUnit());
        unitText.setFill(tile.getUnitColor());
        Helper.enableNode(unitText, !tile.getUnit().isEmpty());

        unitFlow = new TextFlow(upperUnitText, unitText);
        unitFlow.setTextAlignment(TextAlignment.RIGHT);

        valueUnitFlow = new TextFlow(valueText, unitFlow);
        valueUnitFlow.setTextAlignment(TextAlignment.RIGHT);

        averageText = new Text(String.format(locale, formatString, tile.getAverage()));
        averageText.setFill(Tile.FOREGROUND);
        Helper.enableNode(averageText, tile.isAverageVisible());

        minText = new Text();
        minText.setTextOrigin(VPos.TOP);
        minText.setFill(tile.getValueColor());

        maxText = new Text();
        maxText.setTextOrigin(VPos.BOTTOM);
        maxText.setFill(tile.getValueColor());

        highText = new Text();
        highText.setTextOrigin(VPos.BOTTOM);
        highText.setFill(tile.getValueColor());

        lowText = new Text();
        lowText.setTextOrigin(VPos.TOP);
        lowText.setFill(tile.getValueColor());

        text = new Text(tile.getText());
        text.setTextOrigin(VPos.TOP);
        text.setFill(tile.getTextColor());

        timeSpanText = new Text("");
        timeSpanText.setTextOrigin(VPos.TOP);
        timeSpanText.setFill(tile.getTextColor());
        Helper.enableNode(timeSpanText, !tile.isTextVisible());

        stdDeviationArea = new Rectangle();
        Helper.enableNode(stdDeviationArea, tile.isAverageVisible());

        thresholdLine = new Line();
        thresholdLine.setStroke(tile.getThresholdColor());
        thresholdLine.getStrokeDashArray().addAll(PREFERRED_WIDTH * 0.005, PREFERRED_WIDTH * 0.005);
        Helper.enableNode(thresholdLine, tile.isThresholdVisible());

        lowerThresholdLine = new Line();
        lowerThresholdLine.setStroke(tile.getLowerThresholdColor());
        lowerThresholdLine.getStrokeDashArray().addAll(PREFERRED_WIDTH * 0.005, PREFERRED_WIDTH * 0.005);
        Helper.enableNode(lowerThresholdLine, tile.isThresholdVisible());

        averageLine = new Line();
        averageLine.setStroke(Tile.FOREGROUND);
        averageLine.getStrokeDashArray().addAll(PREFERRED_WIDTH * 0.005, PREFERRED_WIDTH * 0.005);
        Helper.enableNode(averageLine, tile.isAverageVisible());

        sections = new HashMap<>();
        tile.getSections().forEach(section -> sections.put(section, new Rectangle()));
        sectionGroup = new Group();
        sectionGroup.getChildren().addAll(sections.values());
        Helper.enableNode(sectionGroup, tile.getSectionsVisible());

        pathElements = new ArrayList<>(noOfDatapoints);
        pathElements.add(0, new MoveTo());
        for (int i = 1 ; i < noOfDatapoints ; i++) { pathElements.add(i, new LineTo()); }

        trendText = new Text("");
        trendText.setTextOrigin(VPos.TOP);
        trendText.setFill(tile.getTextColor());

        sparkLine = new Path();
        sparkLine.getElements().addAll(pathElements);
        sparkLine.setFill(null);
        sparkLine.setStroke(tile.getBarColor());
        sparkLine.setStrokeWidth(PREFERRED_WIDTH * 0.0075);
        sparkLine.setStrokeLineCap(StrokeLineCap.ROUND);
        sparkLine.setStrokeLineJoin(StrokeLineJoin.ROUND);

        dot = new Circle();
        dot.setFill(tile.getBarColor());

        getPane().getChildren().addAll(titleText, valueUnitFlow, fractionLine, sectionGroup, stdDeviationArea, thresholdLine, lowerThresholdLine, averageLine, sparkLine, dot, averageText, minText, maxText, highText, lowText, trendText, timeSpanText, text);
        getPane().getChildren().addAll(horizontalTickLines);
        getPane().getChildren().addAll(tickLabelsY);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        tile.timePeriodProperty().addListener(periodListener);
        tile.getChartData().addListener((ListChangeListener<ChartData>) c -> {
            while(c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach(chartData -> addData(chartData));
                } else if (c.wasRemoved()) {
                    c.getRemoved().forEach(chartData -> dataList.remove(chartData));
                }
            }
        });
        tile.getSections().addListener((ListChangeListener<Section>) c -> {
            while(c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach(section -> sections.put(section, new Rectangle()));
                } else if (c.wasRemoved()) {
                    c.getRemoved().forEach(section -> sections.remove(section));
                }
            }
            sectionGroup.getChildren().setAll(sections.values());
            resize();
        });
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !tile.getTitle().isEmpty());
            Helper.enableNode(text, tile.isTextVisible());
            Helper.enableNode(valueText, tile.isValueVisible());
            Helper.enableNode(unitText, !tile.getUnit().isEmpty());
            Helper.enableNode(timeSpanText, !tile.isTextVisible());
            Helper.enableNode(averageLine, tile.isAverageVisible());
            Helper.enableNode(averageText, tile.isAverageVisible());
            Helper.enableNode(stdDeviationArea, tile.isAverageVisible());
            Helper.enableNode(thresholdLine, tile.isThresholdVisible());
            Helper.enableNode(lowerThresholdLine, tile.isThresholdVisible());
            Helper.enableNode(sectionGroup, tile.getSectionsVisible());
            Helper.enableNode(trendText, tile.isTrendVisible());
            redraw();
        } else if ("VALUE".equals(EVENT_TYPE)) {
            if(tile.isAnimated()) { tile.setAnimated(false); }
            if (!tile.isAveragingEnabled()) { tile.setAveragingEnabled(true); }
            double value = clamp(minValue, maxValue, tile.getValue());
            addData(new ChartData(value));
            handleCurrentValue(value);
        } else if ("PERIOD".equals(EVENT_TYPE)) {
            timePeriod = tile.getTimePeriod();
            timeSpanText.setText(createTimeSpanText());

            noOfDatapoints = (int) timePeriod.getSeconds();

            // To get smooth lines in the chart we need at least 4 values
            //if (noOfDatapoints < 4) throw new IllegalArgumentException("Please increase the averaging period to a value larger than 3.");
            for (int i = 0; i < noOfDatapoints; i++) { dataList.add(new ChartData(minValue, Instant.now().minusSeconds(i * 10))); }
            pathElements.clear();
            pathElements.add(0, new MoveTo());
            for (int i = 1 ; i < noOfDatapoints ; i++) { pathElements.add(i, new LineTo()); }
            sparkLine.getElements().setAll(pathElements);
            redraw();
        }
    }

    @Override protected void handleCurrentValue(final double VALUE) {
        if (dataList.size() < 4) { return; }

        List<Double> clampedDataList = dataList.stream().map(chartData -> Helper.clamp(minValue, maxValue, chartData.getValue())).collect(Collectors.toList());

        low  = clampedDataList.stream().min(Comparator.comparingDouble(Double::doubleValue)).orElse(tile.getLowerThreshold());
        high = clampedDataList.stream().max(Comparator.comparingDouble(Double::doubleValue)).orElse(tile.getThreshold());

        range = (maxValue - minValue);

        double minX  = graphBounds.getX();
        double maxX  = minX + graphBounds.getWidth();
        double minY  = graphBounds.getY();
        double maxY  = minY + graphBounds.getHeight();
        double stepX = graphBounds.getWidth() / (noOfDatapoints - 1);
        double stepY = graphBounds.getHeight() / range;

        niceScaleY.setMinMax(minValue, maxValue);
        int    lineCountY       = 1;
        int    tickLabelOffsetY = 1;
        double tickSpacingY     = niceScaleY.getTickSpacing();
        double tickStepY        = tickSpacingY * stepY;
        double tickStartY       = maxY - tickStepY;
        if (tickSpacingY < minValue) {
            tickLabelOffsetY = (int) (minValue / tickSpacingY) + 1;
            tickStartY = maxY - (tickLabelOffsetY * tickSpacingY - minValue) * stepY;
        }

        horizontalTickLines.forEach(line -> line.setStroke(Color.TRANSPARENT));
        tickLabelsY.forEach(label -> label.setFill(Color.TRANSPARENT));
        horizontalLineOffset = 0;
        for (double y = tickStartY; Math.round(y) > minY; y -= tickStepY) {
            Line line  = horizontalTickLines.get(lineCountY);
            Text label = tickLabelsY.get(lineCountY);
            //label.setText(String.format(locale, "%.0f", low + (tickSpacingY * (lineCountY + tickLabelOffsetY))));
            label.setText(String.format(locale, "%.0f", minValue + lineCountY * tickSpacingY));
            label.setY(y + graphBounds.getHeight() * 0.03);
            label.setFill(tickLabelColor);
            horizontalLineOffset = Math.max(label.getLayoutBounds().getWidth(), horizontalLineOffset);

            line.setStartX(minX);
            line.setStartY(y);
            line.setEndY(y);
            line.setStroke(tickLineColor);
            lineCountY++;
            lineCountY = clamp(0, 4, lineCountY);
        }
        if (tickLabelFontSize < 6) { horizontalLineOffset = 0; }
        horizontalTickLines.forEach(line -> line.setEndX(maxX - horizontalLineOffset));
        tickLabelsY.forEach(label -> label.setX(maxX - label.getLayoutBounds().getWidth()));

        minText.setText(String.format(locale, formatString, minValue));
        maxText.setText(String.format(locale, formatString, maxValue));

        lowText.setText(String.format(locale, formatString, low));
        highText.setText(String.format(locale, formatString, high));

        minText.setX((maxX - minText.getLayoutBounds().getWidth()));
        maxText.setX((maxX - maxText.getLayoutBounds().getWidth()));

        if (!clampedDataList.isEmpty()) {
            if (tile.isSmoothing()) {
                smooth(clampedDataList);
            } else {
                MoveTo begin = (MoveTo) pathElements.get(0);
                begin.setX(minX);
                begin.setY(maxY - Math.abs(minValue - Helper.clamp(minValue, maxValue, clampedDataList.get(0).doubleValue())) * stepY);
                for (int i = 1 ; i < (noOfDatapoints - 1) ; i++) {
                    LineTo lineTo = (LineTo) pathElements.get(i);
                    lineTo.setX(minX + i * stepX);
                    lineTo.setY(maxY - Math.abs(minValue - Helper.clamp(minValue, maxValue, clampedDataList.get(i).doubleValue())) * stepY);
                }
                LineTo end = (LineTo) pathElements.get(noOfDatapoints - 1);
                end.setX(maxX);
                end.setY(maxY - Math.abs(minValue - Helper.clamp(minValue, maxValue, clampedDataList.get(noOfDatapoints - 1).doubleValue())) * stepY);

                dot.setCenterX(maxX);
                dot.setCenterY(end.getY());
            }

            if (tile.isStrokeWithGradient()) {
                setupGradient();
                dot.setFill(gradient);
                sparkLine.setStroke(gradient);
            }

            sections.entrySet().forEach(entry -> {
                Section   section   = entry.getKey();
                Rectangle rectangle = entry.getValue();
                rectangle.setX(minX);
                rectangle.setY(clamp(minY, maxY, maxY - Math.abs(minValue - section.getStop()) * stepY));
                rectangle.setWidth(graphBounds.getWidth());
                rectangle.setHeight(Math.abs(section.getStop() - section.getStart()) * stepY);
                rectangle.setFill(section.getColor());
            });

            double average  = tile.getAverage();
            double averageY = clamp(minY, maxY, maxY - Math.abs(minValue - average) * stepY);

            averageLine.setStartX(minX);
            averageLine.setStartY(averageY);
            averageLine.setEndX(maxX);
            averageLine.setEndY(averageY);

            double threshold  = tile.getThreshold();
            double thresholdY = clamp(minY, maxY, maxY - Math.abs(minValue - threshold) * stepY);

            thresholdLine.setStartX(minX);
            thresholdLine.setStartY(thresholdY);
            thresholdLine.setEndX(maxX);
            thresholdLine.setEndY(thresholdY);

            double lowerThreshold  = tile.getLowerThreshold();
            double lowerThresholdY = clamp(minY, maxY, maxY - Math.abs(minValue - lowerThreshold) * stepY);

            lowerThresholdLine.setStartX(minX);
            lowerThresholdLine.setStartY(lowerThresholdY);
            lowerThresholdLine.setEndX(maxX);
            lowerThresholdLine.setEndY(lowerThresholdY);

            stdDeviationArea.setY(averageLine.getStartY() - (stdDeviation * 0.5 * stepY));
            stdDeviationArea.setHeight(stdDeviation * stepY);

            averageText.setText(String.format(locale, formatString, average));
        }
        valueText.setText(String.format(locale, formatString, VALUE));

        if (!tile.isTextVisible()) {
            text.setText(timeFormatter.format(movingAverage.getLastEntry().getTimestampAsDateTime(tile.getZoneId())));
        }
        resizeDynamicText();
    }

    private void addData(final ChartData DATA) {
        if (dataList.size() >= tile.getMaxTimePeriod().getSeconds()) {
            Collections.rotate(dataList, -1);
            if (!dataList.isEmpty()) { dataList.set((noOfDatapoints - 1), DATA); }
        } else {
            dataList.add(DATA);
        }

        Predicate<ChartData> isNotInTimePeriod = chartData -> !chartData.isWithinTimePeriod(Instant.now(), timePeriod);
        List<ChartData> clampedDataList = new ArrayList<>(dataList);
        clampedDataList.removeIf(isNotInTimePeriod);
        Collections.sort(clampedDataList, Comparator.comparing(ChartData::getTimestamp));

        if (clampedDataList.size() == Integer.MAX_VALUE - 1 || clampedDataList.size() >= noOfDatapoints) {
            Collections.rotate(clampedDataList, -1);
            if (!clampedDataList.isEmpty()) { clampedDataList.set((noOfDatapoints - 1), DATA); }
        }

        int n = Helper.clamp(2, clampedDataList.size(), tile.getNumberOfValuesForTrendCalculation());
        if (clampedDataList.size() > n) {
            List<Double> lastNValues = clampedDataList.stream().map(ChartData::getValue).collect(Helper.lastN(n));
            Model        model       = DoubleExponentialSmoothingForLinearSeries.fit(lastNValues.stream().mapToDouble(Double::doubleValue).toArray(), 0.8, 0.2);
            trendText.setText(String.format(tile.getLocale(), "%.0f", model.forecast(1)[0]));
            //double stepX = graphBounds.getWidth() / (noOfDatapoints - 1);
            //System.out.println("TrendAngle: " + (360 - Helper.getAngleFromXY(0, DATA.getValue(), stepX, model.forecast(1)[0])));
            //System.out.println("Last 4 values: " + last3Values + " -> Trend: " + model.forecast(1)[0]);
        }

        stdDeviation = Statistics.getChartDataStdDev(clampedDataList);
        pathElements.clear();
        pathElements.add(new MoveTo());
        for (int i = 0 ; i < noOfDatapoints - 1 ; i++) { pathElements.add(new LineTo()); }
        sparkLine.getElements().setAll(pathElements);
        if (DATA.getValue() <= tile.getLowerThreshold()) {
            tile.showNotifyRegion(true);
            tile.setTooltipText("Blood sugar too low");
        } else if (DATA.getValue() >= tile.getThreshold()) {
            tile.showNotifyRegion(true);
            tile.setTooltipText("Blood sugar too high");
        } else {
            tile.showNotifyRegion(false);
            tile.setTooltipText("");
        }
        handleCurrentValue(DATA.getValue());
    }

    private void setupGradient() {
        double loFactor = (low - minValue) / tile.getRange();
        double hiFactor = (high - minValue) / tile.getRange();
        Stop   loStop   = new Stop(loFactor, gradientLookup.getColorAt(loFactor));
        Stop   hiStop   = new Stop(hiFactor, gradientLookup.getColorAt(hiFactor));

        List<Stop> stopsInBetween = gradientLookup.getStopsBetween(loFactor, hiFactor);

        double     range  = hiFactor - loFactor;
        double     factor = 1.0 / range;
        List<Stop> stops  = new ArrayList<>();
        stops.add(new Stop(0, loStop.getColor()));
        for (Stop stop : stopsInBetween) {
            stops.add(new Stop((stop.getOffset() - loFactor) * factor, stop.getColor()));
        }
        stops.add(new Stop(1, hiStop.getColor()));

        gradient = new LinearGradient(0, graphBounds.getY() + graphBounds.getHeight(), 0, graphBounds.getY(), false, CycleMethod.NO_CYCLE, stops);
    }

    private String createTimeSpanText() {
        long          timeSpan        = timePeriod.getSeconds();
        StringBuilder timeSpanBuilder = new StringBuilder(movingAverage.isFilling() ? "\u22a2 " : "\u2190 ");
        if (timeSpan > SEC_MONTH) { // 1 Month (30 days)
            int    months = (int)(timeSpan / SEC_MONTH);
            double days   = timeSpan % SEC_MONTH;
            timeSpanBuilder.append(months).append("M").append(String.format(Locale.US, "%.0f", days)).append("d").append(" \u2192");
        } else if (timeSpan > SEC_DAY) { // 1 Day
            int    days  = (int) (timeSpan / SEC_DAY);
            double hours = (timeSpan - (days * SEC_DAY)) / SEC_HOUR;
            timeSpanBuilder.append(days).append("d").append(String.format(Locale.US, "%.0f", hours)).append("h").append(" \u2192");
        } else if (timeSpan > SEC_HOUR) { // 1 Hour
            int    hours   = (int)(timeSpan / SEC_HOUR);
            double minutes = (timeSpan - (hours * SEC_HOUR)) / SEC_MINUTE;
            timeSpanBuilder.append(hours).append("h").append(String.format(Locale.US, "%.0f", minutes)).append("m").append(" \u2192");
        } else if (timeSpan > SEC_MINUTE) { // 1 Minute
            int    minutes = (int)(timeSpan / SEC_MINUTE);
            double seconds = (timeSpan - (minutes * SEC_MINUTE));
            timeSpanBuilder.append(minutes).append("m").append(String.format(Locale.US, "%.0f", seconds)).append("s").append(" \u2192");
        } else {
            int seconds = (int)timeSpan;
            timeSpanBuilder.append(seconds).append("s").append(" \u2192");
        }
        return timeSpanBuilder.toString();
    }

    @Override public void dispose() {
        tile.timePeriodProperty().removeListener(periodListener);
        super.dispose();
    }

    private void smooth(final List<Double> DATA_LIST) {
        Task<Point[]> smoothTask = new Task<Point[]>() {
            @Override protected Point[] call() {
                return Helper.smoothSparkLine(DATA_LIST, minValue, maxValue, graphBounds, noOfDatapoints);
            }
        };
        smoothTask.setOnSucceeded(t -> {
            Point[] smoothedPoints = smoothTask.getValue();
            int lengthMinusOne = smoothedPoints.length - 1;
            sparkLine.getElements().clear();
            sparkLine.getElements().add(new MoveTo(smoothedPoints[0].getX(), smoothedPoints[0].getY()));
            for (int i = 1 ; i < lengthMinusOne ; i++) {
                sparkLine.getElements().add(new LineTo(smoothedPoints[i].getX(), smoothedPoints[i].getY()));
            }
            dot.setCenterX(smoothedPoints[lengthMinusOne].getX());
            dot.setCenterY(smoothedPoints[lengthMinusOne].getY());
        });
        Thread smoothThread = new Thread(smoothTask);
        smoothThread.setDaemon(true);
        smoothThread.start();
    }


    // ******************** Resizing ******************************************
    @Override protected void resizeDynamicText() {
        double maxWidth = unitText.isVisible() ? width - size * 0.275 : width - size * 0.1;
        double fontSize = size * 0.24;
        valueText.setFont(Fonts.latoRegular(fontSize));
        if (valueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(valueText, maxWidth, fontSize); }

        maxWidth = width - size * 0.7;
        fontSize = size * 0.06;
        averageText.setFont(Fonts.latoRegular(fontSize));
        if (averageText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(averageText, maxWidth, fontSize); }
        if (averageLine.getStartY() < graphBounds.getY() + graphBounds.getHeight() * 0.5) {
            averageText.setY(averageLine.getStartY() + (size * 0.0425));
        } else {
            averageText.setY(averageLine.getStartY() - (size * 0.0075));
        }

        minText.setFont(Fonts.latoRegular(fontSize));
        if (minText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(minText, maxWidth, fontSize); }
        minText.setY(height - size * 0.1);

        maxText.setFont(Fonts.latoRegular(fontSize));
        if (maxText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(maxText, maxWidth, fontSize); }
        maxText.setY(graphBounds.getY() - size * 0.0125);

        lowText.setFont(Fonts.latoRegular(fontSize));
        if (lowText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(lowText, maxWidth, fontSize); }
        lowText.setY(height - size * 0.1);

        highText.setFont(Fonts.latoRegular(fontSize));
        if (highText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(highText, maxWidth, fontSize); }
        highText.setY(graphBounds.getY() - size * 0.0125);

        trendText.setFont(Fonts.latoRegular(fontSize));
        if (trendText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(trendText, maxWidth, fontSize); }
        trendText.setY(height - size * 0.1);

        maxWidth = width - size * 0.25;
        fontSize = size * 0.06;
        text.setFont(Fonts.latoRegular(fontSize));
        if (text.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(text, maxWidth, fontSize); }
        text.relocate(width - size * 0.05 - text.getLayoutBounds().getWidth(), height - size * 0.1);

        maxWidth = width - size * 0.25;
        fontSize = size * 0.06;
        timeSpanText.setFont(Fonts.latoRegular(fontSize));
        if (timeSpanText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(timeSpanText, maxWidth, fontSize); }
        timeSpanText.relocate((width - timeSpanText.getLayoutBounds().getWidth()) * 0.5, height - size * 0.1);
    }
    @Override protected void resizeStaticText() {
        double maxWidth = width - size * 0.1;
        double fontSize = size * textSize.factor;

        boolean customFontEnabled = tile.isCustomFontEnabled();
        Font    customFont        = tile.getCustomFont();
        Font    font              = (customFontEnabled && customFont != null) ? Font.font(customFont.getFamily(), fontSize) : Fonts.latoRegular(fontSize);

        titleText.setFont(font);
        if (titleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(titleText, maxWidth, fontSize); }
        switch(tile.getTitleAlignment()) {
            default    :
            case LEFT  : titleText.relocate(size * 0.05, size * 0.05); break;
            case CENTER: titleText.relocate((width - titleText.getLayoutBounds().getWidth()) * 0.5, size * 0.05); break;
            case RIGHT : titleText.relocate(width - (size * 0.05) - titleText.getLayoutBounds().getWidth(), size * 0.05); break;
        }

        maxWidth = width - size * 0.275;
        fontSize = upperUnitText.getText().isEmpty() ? size * 0.12 : size * 0.10;
        upperUnitText.setFont(Fonts.latoRegular(fontSize));
        if (upperUnitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(upperUnitText, maxWidth, fontSize); }

        maxWidth = width - size * 0.275;
        fontSize = upperUnitText.getText().isEmpty() ? size * 0.12 : size * 0.10;
        unitText.setFont(Fonts.latoRegular(fontSize));
        if (unitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(unitText, maxWidth, fontSize); }

        lowText.setX(size * 0.05);
        highText.setX(size * 0.05);
        averageText.setX(size * 0.05);
        trendText.setX(size * 0.25);
    }

    @Override protected void resize() {
        super.resize();
        graphBounds = new Rectangle(contentBounds.getX(), titleText.isVisible() ? size * 0.5 : size * 0.4, contentBounds.getWidth(), titleText.isVisible() ? height - size * 0.61 : height - size * 0.51);

        tickLabelFontSize  = graphBounds.getHeight() * 0.1;
        Font tickLabelFont = Fonts.latoRegular(tickLabelFontSize);
        tickLabelsY.forEach(label -> {
            enableNode(label, tickLabelFontSize >= 6);
            label.setFont(tickLabelFont);
        });
        horizontalTickLines.forEach(line -> line.setStrokeWidth(0.5));

        stdDeviationArea.setX(graphBounds.getX());
        stdDeviationArea.setWidth(graphBounds.getWidth());

        thresholdLine.getStrokeDashArray().setAll(graphBounds.getWidth() * 0.01, graphBounds.getWidth() * 0.01);
        lowerThresholdLine.getStrokeDashArray().setAll(graphBounds.getWidth() * 0.01, graphBounds.getWidth() * 0.01);
        averageLine.getStrokeDashArray().setAll(graphBounds.getWidth() * 0.01, graphBounds.getWidth() * 0.01);

        handleCurrentValue(tile.getValue());
        if (noOfDatapoints < 60) {
            sparkLine.setStrokeWidth(size * 0.01);
            dot.setRadius(size * 0.014);
        } else if (noOfDatapoints < 3600) {
            sparkLine.setStrokeWidth(size * 0.0075);
            dot.setRadius(size * 0.0105);
        } else {
            sparkLine.setStrokeWidth(size * 0.005);
            dot.setRadius(size * 0.007);
        }

        if (tile.isStrokeWithGradient()) { setupGradient(); }

        resizeStaticText();
        resizeDynamicText();

        valueUnitFlow.setPrefWidth(width - size * 0.1);
        valueUnitFlow.relocate(size * 0.05, contentBounds.getY());

        fractionLine.setStartX(width - 0.17 * size);
        fractionLine.setStartY(height * 0.3);
        fractionLine.setEndX(width - 0.05 * size);
        fractionLine.setEndY(height * 0.3);
        fractionLine.setStroke(tile.getUnitColor());
        fractionLine.setStrokeWidth(size * 0.005);

        unitFlow.setTranslateY(valueText.getLayoutBounds().getMinY() - upperUnitText.getLayoutBounds().getHeight());
    }

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(tile.getTitle());
        text.setText(tile.getText());

        if (tile.getUnit().contains("/")) {
            String[] units = tile.getUnit().split("/");
            upperUnitText.setText(units[0] + "\n");
            unitText.setText(units[1]);
            Helper.enableNode(fractionLine, true);
        } else {
            upperUnitText.setText("");
            unitText.setText(tile.getUnit());
            Helper.enableNode(fractionLine, false);
        }

        if (!tile.getDescription().isEmpty()) { text.setText(tile.getDescription()); }

        if (tile.isTextVisible()) {
            text.setText(tile.getText());
        } else if (!tile.isTextVisible() && null != movingAverage.getTimeSpan()) {
            timeSpanText.setText(createTimeSpanText());
            text.setText(timeFormatter.format(movingAverage.getLastEntry().getTimestampAsDateTime(tile.getZoneId())));
        }

        resizeStaticText();

        titleText.setFill(tile.getTitleColor());
        valueText.setFill(tile.getValueColor());
        upperUnitText.setFill(tile.getUnitColor());
        unitText.setFill(tile.getUnitColor());
        minText.setFill(tile.getValueColor());
        maxText.setFill(tile.getValueColor());
        lowText.setFill(tile.getValueColor());
        highText.setFill(tile.getValueColor());
        trendText.setFill(tile.getTextColor());
        text.setFill(tile.getTextColor());
        timeSpanText.setFill(tile.getTextColor());
        if (tile.isStrokeWithGradient()) {
            setupGradient();
            sparkLine.setStroke(gradient);
        } else {
            sparkLine.setStroke(tile.getBarColor());
        }
        stdDeviationArea.setFill(Helper.getColorWithOpacity(Tile.FOREGROUND, 0.1));
        dot.setFill(tile.isStrokeWithGradient() ? gradient : tile.getBarColor());
    }
}
