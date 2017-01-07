/*
 * Copyright (c) 2016 by Gerrit Grunwald
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

import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.GradientLookup;
import eu.hansolo.tilesfx.tools.Helper;
import eu.hansolo.tilesfx.tools.MovingAverage;
import eu.hansolo.tilesfx.tools.Statistics;
import javafx.geometry.VPos;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Text;
import javafx.util.Pair;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static eu.hansolo.tilesfx.tools.Helper.clamp;


/**
 * Created by hansolo on 19.12.16.
 */
public class SparkLineTileSkin extends TileSkin {
    private static final int  MONTH           = 2_592_000;
    private static final int  DAY             = 86_400;
    private static final int  HOUR            = 3_600;
    private static final int  MINUTE          = 60;
    private DateTimeFormatter timeFormatter   = DateTimeFormatter.ofPattern("HH:mm");
    private Text              titleText;
    private Text              valueText;
    private Text              unitText;
    private Text              averageText;
    private Text              highText;
    private Text              lowText;
    private Text              subTitleText;
    private Text              timeSpanText;
    private Rectangle         graphBounds;
    private List<PathElement> pathElements;
    private Path              sparkLine;
    private Circle            dot;
    private Rectangle         stdDeviationArea;
    private Line              averageLine;
    private LinearGradient    gradient;
    private GradientLookup    gradientLookup;
    private double            low;
    private double            high;
    private double            stdDeviation;
    private int               noOfDatapoints;
    private List<Double>      dataList;
    private MovingAverage     movingAverage;


    // ******************** Constructors **************************************
    public SparkLineTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        timeFormatter   = DateTimeFormatter.ofPattern("HH:mm", getSkinnable().getLocale());

        if (getSkinnable().isAutoScale()) getSkinnable().calcAutoScale();

        gradientLookup = new GradientLookup(getSkinnable().getGradientStops());
        low            = getSkinnable().getMaxValue();
        high           = getSkinnable().getMinValue();
        stdDeviation   = 0;
        movingAverage  = getSkinnable().getMovingAverage();
        noOfDatapoints = getSkinnable().getAveragingPeriod();
        dataList       = new LinkedList<>();
        for (int i = 0; i < noOfDatapoints; i++) { dataList.add(minValue); }

        // To get smooth lines in the chart we need at least 4 values
        if (noOfDatapoints < 4) throw new IllegalArgumentException("Please increase the averaging period to a value larger than 3.");

        graphBounds = new Rectangle(PREFERRED_WIDTH * 0.05, PREFERRED_HEIGHT * 0.5, PREFERRED_WIDTH * 0.9, PREFERRED_HEIGHT * 0.45);

        titleText = new Text(getSkinnable().getTitle());
        titleText.setFill(getSkinnable().getTitleColor());
        Helper.enableNode(titleText, !getSkinnable().getTitle().isEmpty());

        valueText = new Text(String.format(locale, formatString, getSkinnable().getValue()));
        valueText.setFill(getSkinnable().getValueColor());
        Helper.enableNode(valueText, getSkinnable().isValueVisible());

        unitText = new Text(getSkinnable().getUnit());
        unitText.setFill(getSkinnable().getUnitColor());
        Helper.enableNode(unitText, !getSkinnable().getUnit().isEmpty());

        averageText = new Text(String.format(locale, formatString, getSkinnable().getAverage()));
        averageText.setFill(Tile.FOREGROUND);
        Helper.enableNode(averageText, getSkinnable().isAverageVisible());

        highText = new Text();
        highText.setTextOrigin(VPos.BOTTOM);
        highText.setFill(getSkinnable().getValueColor());

        lowText = new Text();
        lowText.setTextOrigin(VPos.TOP);
        lowText.setFill(getSkinnable().getValueColor());

        subTitleText = new Text(getSkinnable().getSubTitle());
        subTitleText.setTextOrigin(VPos.TOP);
        subTitleText.setFill(getSkinnable().getSubTitleColor());

        timeSpanText = new Text("");
        timeSpanText.setTextOrigin(VPos.TOP);
        timeSpanText.setFill(getSkinnable().getSubTitleColor());
        Helper.enableNode(timeSpanText, getSkinnable().getSubTitle().isEmpty());

        stdDeviationArea = new Rectangle();
        Helper.enableNode(stdDeviationArea, getSkinnable().isAverageVisible());

        averageLine = new Line();
        averageLine.setStroke(Tile.FOREGROUND);
        averageLine.getStrokeDashArray().addAll(PREFERRED_WIDTH * 0.005, PREFERRED_WIDTH * 0.005);
        Helper.enableNode(averageLine, getSkinnable().isAverageVisible());

        pathElements = new ArrayList<>(noOfDatapoints);
        pathElements.add(0, new MoveTo());
        for (int i = 1 ; i < noOfDatapoints ; i++) { pathElements.add(i, new LineTo()); }

        sparkLine = new Path();
        sparkLine.getElements().addAll(pathElements);
        sparkLine.setFill(null);
        sparkLine.setStroke(getSkinnable().getBarColor());
        sparkLine.setStrokeWidth(PREFERRED_WIDTH * 0.0075);
        sparkLine.setStrokeLineCap(StrokeLineCap.ROUND);
        sparkLine.setStrokeLineJoin(StrokeLineJoin.ROUND);

        dot = new Circle();
        dot.setFill(getSkinnable().getBarColor());

        getPane().getChildren().addAll(titleText, valueText, unitText, stdDeviationArea, averageLine, sparkLine, dot, averageText, highText, lowText, timeSpanText, subTitleText);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        getSkinnable().averagingPeriodProperty().addListener(o -> handleEvents("AVERAGING_PERIOD"));
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !getSkinnable().getTitle().isEmpty());
            Helper.enableNode(valueText, getSkinnable().isValueVisible());
            Helper.enableNode(unitText, !getSkinnable().getUnit().isEmpty());
            Helper.enableNode(timeSpanText, getSkinnable().getSubTitle().isEmpty());
            Helper.enableNode(averageLine, getSkinnable().isAverageVisible());
            Helper.enableNode(averageText, getSkinnable().isAverageVisible());
            Helper.enableNode(stdDeviationArea, getSkinnable().isAverageVisible());
            redraw();
        } else if ("SECTION".equals(EVENT_TYPE)) {

        } else if ("ALERT".equals(EVENT_TYPE)) {

        } else if ("VALUE".equals(EVENT_TYPE)) {
            if(getSkinnable().isAnimated()) { getSkinnable().setAnimated(false); }
            if (!getSkinnable().isAveragingEnabled()) { getSkinnable().setAveragingEnabled(true); }
            double value = clamp(minValue, maxValue, getSkinnable().getValue());
            addData(value);
            handleCurrentValue(value);
        } else if ("AVERAGING_PERIOD".equals(EVENT_TYPE)) {
            noOfDatapoints = getSkinnable().getAveragingPeriod();
            // To get smooth lines in the chart we need at least 4 values
            if (noOfDatapoints < 4) throw new IllegalArgumentException("Please increase the averaging period to a value larger than 3.");
            for (int i = 0; i < noOfDatapoints; i++) { dataList.add(minValue); }
            pathElements.clear();
            pathElements.add(0, new MoveTo());
            for (int i = 1 ; i < noOfDatapoints ; i++) { pathElements.add(i, new LineTo()); }
            sparkLine.getElements().setAll(pathElements);
            redraw();
        }
    };

    @Override protected void handleCurrentValue(final double VALUE) {
        low  = Statistics.getMin(dataList);
        high = Statistics.getMax(dataList);
        if (Double.compare(low, high) == 0) {
            low  = minValue;
            high = maxValue;
        }
        range = high - low;

        double minX  = graphBounds.getX();
        double maxX  = minX + graphBounds.getWidth();
        double minY  = graphBounds.getY();
        double maxY  = minY + graphBounds.getHeight();
        double stepX = graphBounds.getWidth() / (noOfDatapoints - 1);
        double stepY = graphBounds.getHeight() / range;

        if (getSkinnable().isSmoothing()) {
            smooth(dataList);
        } else {
            MoveTo begin = (MoveTo) pathElements.get(0);
            begin.setX(minX);
            begin.setY(maxY - Math.abs(low - dataList.get(0)) * stepY);
            for (int i = 1; i < (noOfDatapoints - 1); i++) {
                LineTo lineTo = (LineTo) pathElements.get(i);
                lineTo.setX(minX + i * stepX);
                lineTo.setY(maxY - Math.abs(low - dataList.get(i)) * stepY);
            }
            LineTo end = (LineTo) pathElements.get(noOfDatapoints - 1);
            end.setX(maxX);
            end.setY(maxY - Math.abs(low - dataList.get(noOfDatapoints - 1)) * stepY);

            dot.setCenterX(maxX);
            dot.setCenterY(end.getY());

            if (getSkinnable().isStrokeWithGradient()) {
                setupGradient();
                dot.setFill(gradient);
                sparkLine.setStroke(gradient);
            }
        }

        double average = getSkinnable().getAverage();
        double averageY = clamp(minY, maxY, maxY - Math.abs(low - average) * stepY);

        averageLine.setStartX(minX);
        averageLine.setEndX(maxX);
        averageLine.setStartY(averageY);
        averageLine.setEndY(averageY);

        stdDeviationArea.setY(averageLine.getStartY() - (stdDeviation * 0.5 * stepY));
        stdDeviationArea.setHeight(stdDeviation * stepY);

        valueText.setText(String.format(locale, formatString, VALUE));
        averageText.setText(String.format(locale, formatString, average));

        highText.setText(String.format(locale, formatString, high));

        if (getSkinnable().getSubTitle().isEmpty()) {
            if (null == movingAverage.getTimeSpan()) {
                lowText.setText(String.format(locale, formatString, low));
            } else {
                timeSpanText.setText(createTimeSpanText());
                subTitleText.setText(timeFormatter.format(movingAverage.getLastEntry().getTimestampAsDateTime(getSkinnable().getZoneId())));
            }
        } else {
            lowText.setText(String.format(locale, formatString, low));
        }
        resizeDynamicText();
    }

    private void addData(final double VALUE) {
        if (dataList.size() <= noOfDatapoints) {
            Collections.rotate(dataList, -1);
            dataList.set((noOfDatapoints - 1), VALUE);
        } else {
            dataList.add(VALUE);
        }
        stdDeviation = Statistics.getStdDev(dataList);
    }

    private void setupGradient() {
        double loFactor = (low - minValue) / getSkinnable().getRange();
        double hiFactor = (high - minValue) / getSkinnable().getRange();
        Stop   loStop   = new Stop(loFactor, gradientLookup.getColorAt(loFactor));
        Stop   hiStop   = new Stop(hiFactor, gradientLookup.getColorAt(hiFactor));
        gradient = new LinearGradient(0, graphBounds.getY() + graphBounds.getHeight(), 0, graphBounds.getY(), false, CycleMethod.NO_CYCLE, loStop, hiStop);
    }

    private String createTimeSpanText() {
        long   timeSpan = movingAverage.getTimeSpan().getEpochSecond();
        ZoneId zoneId   = getSkinnable().getZoneId();
        StringBuilder timeSpanBuilder = new StringBuilder("\u2190 ");
        if (timeSpan > MONTH) { // 1 Month (30 days)
            int    months = (int)(timeSpan / MONTH);
            double days   = timeSpan % MONTH;
            timeSpanBuilder.append(months).append("M").append(String.format(Locale.US, "%.0f", days)).append("d").append(" \u2192");
        } else if (timeSpan > DAY) { // 1 Day
            int    days  = (int) (timeSpan / DAY);
            double hours = (timeSpan - (days * DAY)) / HOUR;
            timeSpanBuilder.append(days).append("d").append(String.format(Locale.US, "%.0f", hours)).append("h").append(" \u2192");
        } else if (timeSpan > HOUR) { // 1 Hour
            int    hours   = (int)(timeSpan / HOUR);
            double minutes = (timeSpan - (hours * HOUR)) / MINUTE;
            timeSpanBuilder.append(hours).append("h").append(String.format(Locale.US, "%.0f", minutes)).append("m").append(" \u2192");
        } else if (timeSpan > MINUTE) { // 1 Minute
            int    minutes = (int)(timeSpan / MINUTE);
            double seconds = (timeSpan - (minutes * MINUTE));
            timeSpanBuilder.append(minutes).append("m").append(String.format(Locale.US, "%.0f", seconds)).append("s").append(" \u2192");
        } else {
            int seconds = (int)timeSpan;
            timeSpanBuilder.append(seconds).append("s").append(" \u2192");
        }
        return timeSpanBuilder.toString();
    }


    // ******************** Smoothing *****************************************
    public void smooth(final List<Double> DATA_LIST) {
        int      size = DATA_LIST.size();
        double[] x    = new double[size];
        double[] y    = new double[size];

        low  = Statistics.getMin(DATA_LIST);
        high = Statistics.getMax(DATA_LIST);
        if (Double.compare(low, high) == 0) {
            low  = minValue;
            high = maxValue;
        }
        range = high - low;

        double minX  = graphBounds.getX();
        double maxX  = minX + graphBounds.getWidth();
        double minY  = graphBounds.getY();
        double maxY  = minY + graphBounds.getHeight();
        double stepX = graphBounds.getWidth() / (noOfDatapoints - 1);
        double stepY = graphBounds.getHeight() / range;

        for (int i = 0 ; i < size ; i++) {
            x[i] = minX + i * stepX;
            y[i] = maxY - Math.abs(low - DATA_LIST.get(i)) * stepY;
        }

        Pair<Double[], Double[]> px = computeControlPoints(x);
        Pair<Double[], Double[]> py = computeControlPoints(y);

        sparkLine.getElements().clear();
        for (int i = 0 ; i < size - 1 ; i++) {
            sparkLine.getElements().add(new MoveTo(x[i], y[i]));
            sparkLine.getElements().add(new CubicCurveTo(px.getKey()[i], py.getKey()[i], px.getValue()[i], py.getValue()[i], x[i + 1], y[i + 1]));
        }
        dot.setCenterX(maxX);
        dot.setCenterY(y[size - 1]);
    }
    private Pair<Double[], Double[]> computeControlPoints(final double[] K) {
        int      n  = K.length - 1;
        Double[] p1 = new Double[n];
        Double[] p2 = new Double[n];

	    /*rhs vector*/
        double[] a = new double[n];
        double[] b = new double[n];
        double[] c = new double[n];
        double[] r = new double[n];

	    /*left most segment*/
        a[0] = 0;
        b[0] = 2;
        c[0] = 1;
        r[0] = K[0]+2*K[1];

	    /*internal segments*/
        for (int i = 1; i < n - 1; i++) {
            a[i] = 1;
            b[i] = 4;
            c[i] = 1;
            r[i] = 4 * K[i] + 2 * K[i + 1];
        }

	    /*right segment*/
        a[n-1] = 2;
        b[n-1] = 7;
        c[n-1] = 0;
        r[n-1] = 8 * K[n - 1] + K[n];

	    /*solves Ax = b with the Thomas algorithm*/
        for (int i = 1; i < n; i++) {
            double m = a[i] / b[i - 1];
            b[i] = b[i] - m * c[i - 1];
            r[i] = r[i] - m * r[i - 1];
        }

        p1[n-1] = r[n-1] / b[n-1];
        for (int i = n - 2; i >= 0; --i) { p1[i] = (r[i] - c[i] * p1[i + 1]) / b[i]; }

        for (int i = 0 ; i < n - 1 ; i++) { p2[i] = 2 * K[i + 1] - p1[i + 1]; }
        p2[n - 1] = 0.5 * (K[n] + p1[n - 1]);

        return new Pair<>(p1, p2);
    }


    // ******************** Resizing ******************************************
    @Override protected void resizeDynamicText() {
        double maxWidth = unitText.isManaged() ? size * 0.725 : size * 0.9;
        double fontSize = size * 0.24;
        valueText.setFont(Fonts.latoRegular(fontSize));
        if (valueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(valueText, maxWidth, fontSize); }
        if (unitText.isVisible()) {
            valueText.relocate(size * 0.925 - valueText.getLayoutBounds().getWidth() - unitText.getLayoutBounds().getWidth(), size * 0.15);
        } else {
            valueText.relocate(size * 0.95 - valueText.getLayoutBounds().getWidth(), size * 0.15);
        }

        maxWidth = size * 0.3;
        fontSize = size * 0.05;
        averageText.setFont(Fonts.latoRegular(fontSize));
        if (averageText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(averageText, maxWidth, fontSize); }
        if (averageLine.getStartY() < graphBounds.getY() + graphBounds.getHeight() * 0.5) {
            averageText.setY(averageLine.getStartY() + (size * 0.0425));
        } else {
            averageText.setY(averageLine.getStartY() - (size * 0.0075));
        }

        highText.setFont(Fonts.latoRegular(fontSize));
        if (highText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(highText, maxWidth, fontSize); }
        highText.setY(graphBounds.getY() - size * 0.0125);

        lowText.setFont(Fonts.latoRegular(fontSize));
        if (lowText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(lowText, maxWidth, fontSize); }
        lowText.setY(size * 0.9);

        maxWidth = size * 0.75;
        fontSize = size * 0.05;
        subTitleText.setFont(Fonts.latoRegular(fontSize));
        if (subTitleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(subTitleText, maxWidth, fontSize); }
        subTitleText.relocate(size * 0.95 - subTitleText.getLayoutBounds().getWidth(), size * 0.9);

        maxWidth = size * 0.75;
        fontSize = size * 0.05;
        timeSpanText.setFont(Fonts.latoRegular(fontSize));
        if (timeSpanText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(timeSpanText, maxWidth, fontSize); }
        timeSpanText.relocate((size - timeSpanText.getLayoutBounds().getWidth()) * 0.5, size * 0.9);
    }
    @Override protected void resizeStaticText() {
        double maxWidth = size * 0.9;
        double fontSize = size * 0.06;

        titleText.setFont(Fonts.latoRegular(fontSize));
        if (titleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(titleText, maxWidth, fontSize); }
        titleText.relocate(size * 0.05, size * 0.05);

        maxWidth = size * 0.15;
        unitText.setFont(Fonts.latoRegular(fontSize));
        if (unitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(unitText, maxWidth, fontSize); }
        unitText.relocate(size * 0.95 - unitText.getLayoutBounds().getWidth(), size * 0.3275);

        averageText.setX(size * 0.05);
        highText.setX(size * 0.05);
        lowText.setX(size * 0.05);
    }

    @Override protected void resize() {
        super.resize();
        graphBounds = new Rectangle(size * 0.05, size * 0.5, size * 0.9, size * 0.39);

        stdDeviationArea.setX(graphBounds.getX());
        stdDeviationArea.setWidth(graphBounds.getWidth());

        averageLine.getStrokeDashArray().setAll(graphBounds.getWidth() * 0.01, graphBounds.getWidth() * 0.01);

        handleCurrentValue(getSkinnable().getValue());
        sparkLine.setStrokeWidth(size * 0.01);
        dot.setRadius(size * 0.014);

        if (getSkinnable().isStrokeWithGradient()) { setupGradient(); }

        resizeStaticText();
        resizeDynamicText();
    };

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(getSkinnable().getTitle());
        if (!getSkinnable().getSubTitle().isEmpty()) { subTitleText.setText(getSkinnable().getSubTitle()); }
        resizeStaticText();

        titleText.setFill(getSkinnable().getTitleColor());
        valueText.setFill(getSkinnable().getValueColor());
        highText.setFill(getSkinnable().getValueColor());
        lowText.setFill(getSkinnable().getValueColor());
        subTitleText.setFill(getSkinnable().getSubTitleColor());
        timeSpanText.setFill(getSkinnable().getSubTitleColor());
        sparkLine.setStroke(getSkinnable().isStrokeWithGradient() ? gradient : getSkinnable().getBarColor());
        stdDeviationArea.setFill(Helper.getTranslucentColorFrom(Tile.FOREGROUND, 0.1));
        dot.setFill(getSkinnable().isStrokeWithGradient() ? gradient : getSkinnable().getBarColor());
    };
}
