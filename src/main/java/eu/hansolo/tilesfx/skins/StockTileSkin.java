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

package eu.hansolo.tilesfx.skins;

import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.Helper;
import eu.hansolo.tilesfx.tools.MovingAverage;
import eu.hansolo.tilesfx.tools.Statistics;
import javafx.animation.FillTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.beans.InvalidationListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
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
import javafx.scene.text.TextBoundsType;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static eu.hansolo.tilesfx.tools.Helper.clamp;


/**
 * Created by hansolo on 05.03.17.
 */
public class StockTileSkin extends TileSkin {
    private static final long    MONTH           = 2_592_000;
    private static final long    DAY             = 86_400;
    private static final long    HOUR            = 3_600;
    private static final long    MINUTE          = 60;
    private DateTimeFormatter    timeFormatter   = DateTimeFormatter.ofPattern("HH:mm");
    private enum                 State {
        INCREASE(Tile.GREEN, 0),
        DECREASE(Tile.RED, 180),
        CONSTANT(Tile.ORANGE, 90);

        public final Color  color;
        public final double angle;

        State(final Color COLOR, final double ANGLE) {
            color = COLOR;
            angle = ANGLE;
        }
    }
    private Path                 triangle;
    private StackPane            indicatorPane;
    private Text                 titleText;
    private Text                 valueText;
    private TextFlow             valueUnitFlow;
    private Text                 highText;
    private Text                 lowText;
    private Text                 changePercentageText;
    private TextFlow             changePercentageFlow;
    private Label                changeText;
    private Text                 text;
    private Text                 timeSpanText;
    private Rectangle            graphBounds;
    private Line                 referenceLine;
    private List<PathElement>    pathElements;
    private Path                 sparkLine;
    private Circle               dot;
    private double               low;
    private double               high;
    private int                  noOfDatapoints;
    private List<Double>         dataList;
    private MovingAverage        movingAverage;
    private InvalidationListener averagingListener;
    private State                state;


    // ******************** Constructors **************************************
    public StockTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        averagingListener = o -> handleEvents("AVERAGING_PERIOD");

        timeFormatter = DateTimeFormatter.ofPattern("HH:mm", tile.getLocale());

        state = State.CONSTANT;

        if (tile.isAutoScale()) tile.calcAutoScale();

        low            = tile.getMaxValue();
        high           = tile.getMinValue();
        movingAverage  = tile.getMovingAverage();
        noOfDatapoints = tile.getAveragingPeriod();
        dataList       = new LinkedList<>();

        // To get smooth lines in the chart we need at least 4 values
        if (noOfDatapoints < 4) throw new IllegalArgumentException("Please increase the averaging period to a value larger than 3.");

        graphBounds = new Rectangle(PREFERRED_WIDTH * 0.05, PREFERRED_HEIGHT * 0.5, PREFERRED_WIDTH * 0.9, PREFERRED_HEIGHT * 0.45);

        titleText = new Text(tile.getTitle());
        titleText.setFill(tile.getTitleColor());
        Helper.enableNode(titleText, !tile.getTitle().isEmpty());

        valueText = new Text(String.format(locale, formatString, tile.getValue()));
        valueText.setBoundsType(TextBoundsType.VISUAL);
        valueText.setFill(tile.getValueColor());
        Helper.enableNode(valueText, tile.isValueVisible());

        valueUnitFlow = new TextFlow(valueText);
        valueUnitFlow.setTextAlignment(TextAlignment.RIGHT);

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

        referenceLine = new Line();
        referenceLine.getStrokeDashArray().addAll(3d, 3d);
        referenceLine.setVisible(false);

        pathElements = new ArrayList<>(noOfDatapoints);
        pathElements.add(0, new MoveTo());
        for (int i = 1 ; i < noOfDatapoints ; i++) { pathElements.add(i, new LineTo()); }

        sparkLine = new Path();
        sparkLine.getElements().addAll(pathElements);
        sparkLine.setFill(null);
        sparkLine.setStroke(tile.getBarColor());
        sparkLine.setStrokeWidth(PREFERRED_WIDTH * 0.0075);
        sparkLine.setStrokeLineCap(StrokeLineCap.ROUND);
        sparkLine.setStrokeLineJoin(StrokeLineJoin.ROUND);

        dot = new Circle();
        dot.setFill(tile.getBarColor());
        dot.setVisible(false);

        triangle = new Path();
        triangle.setStroke(null);
        triangle.setFill(state.color);
        indicatorPane = new StackPane(triangle);

        changeText = new Label(String.format(locale, "%." + tile.getTickLabelDecimals() + "f", (tile.getCurrentValue() - tile.getReferenceValue())));
        changeText.setTextFill(state.color);
        changeText.setAlignment(Pos.CENTER_RIGHT);

        changePercentageText = new Text(new StringBuilder().append(String.format(locale, "%." + tile.getTickLabelDecimals() + "f", (tile.getCurrentValue() / tile.getReferenceValue() * 100.0) - 100.0)).append("\u0025").toString());
        changePercentageText.setFill(state.color);

        changePercentageFlow = new TextFlow(indicatorPane, changePercentageText);
        changePercentageFlow.setTextAlignment(TextAlignment.RIGHT);

        getPane().getChildren().addAll(titleText, valueUnitFlow, sparkLine, dot, referenceLine, highText, lowText, timeSpanText, text, changeText, changePercentageFlow);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        tile.averagingPeriodProperty().addListener(averagingListener);
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !tile.getTitle().isEmpty());
            Helper.enableNode(valueText, tile.isValueVisible());
            Helper.enableNode(timeSpanText, !tile.isTextVisible());
            redraw();
        } else if ("VALUE".equals(EVENT_TYPE)) {
            if(tile.isAnimated()) { tile.setAnimated(false); }
            if (!tile.isAveragingEnabled()) { tile.setAveragingEnabled(true); }
            double value = clamp(minValue, maxValue, tile.getValue());
            addData(value);
            handleCurrentValue(value);
        } else if ("AVERAGING".equals(EVENT_TYPE)) {
            noOfDatapoints = tile.getAveragingPeriod();
            // To get smooth lines in the chart we need at least 4 values
            if (noOfDatapoints < 4) throw new IllegalArgumentException("Please increase the averaging period to a value larger than 3.");
            for (int i = 0; i < noOfDatapoints; i++) { dataList.add(minValue); }
            pathElements.clear();
            pathElements.add(0, new MoveTo());
            for (int i = 1 ; i < noOfDatapoints ; i++) { pathElements.add(i, new LineTo()); }
            sparkLine.getElements().setAll(pathElements);
            redraw();
        }
    }

    @Override protected void handleCurrentValue(final double VALUE) {
        low  = Statistics.getMin(dataList);
        high = Statistics.getMax(dataList);
        if (Helper.equals(low, high)) {
            low  = minValue;
            high = maxValue;
        }
        range = high - low;

        double minX           = graphBounds.getX();
        double maxX           = minX + graphBounds.getWidth();
        double minY           = graphBounds.getY();
        double maxY           = minY + graphBounds.getHeight();
        double stepX          = graphBounds.getWidth() / (noOfDatapoints - 1);
        double stepY          = graphBounds.getHeight() / range;
        double referenceValue = tile.getReferenceValue();

        if(!dataList.isEmpty()) {
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

            updateState(VALUE, referenceValue);

            referenceLine.setStartY(maxY - Math.abs(low - referenceValue) * stepY);
            referenceLine.setEndY(maxY - Math.abs(low - referenceValue) * stepY);

            changeText.setText(String.format(locale, "%." + tile.getTickLabelDecimals() + "f", (VALUE - referenceValue)));

            StringBuilder changePercentageTextBuilder = new StringBuilder();
            if (Double.compare(tile.getReferenceValue(), 0.0) == 0) {
                changePercentageTextBuilder.append(String.format(locale, "%." + tile.getTickLabelDecimals() + "f", 0.0));
            } else {
                changePercentageTextBuilder.append(String.format(locale, "%." + tile.getTickLabelDecimals() + "f", (VALUE / tile.getReferenceValue() * 100.0) - 100.0));
            }
            changePercentageTextBuilder.append("\u0025");
            changePercentageText.setText(changePercentageTextBuilder.toString());

            RotateTransition rotateTransition = new RotateTransition(Duration.millis(200), triangle);
            rotateTransition.setFromAngle(triangle.getRotate());
            rotateTransition.setToAngle(state.angle);

            FillTransition fillIndicatorTransition = new FillTransition(Duration.millis(200), triangle);
            fillIndicatorTransition.setFromValue((Color) triangle.getFill());
            fillIndicatorTransition.setToValue(state.color);

            FillTransition fillReferenceTransition = new FillTransition(Duration.millis(200), changePercentageText);
            fillReferenceTransition.setFromValue((Color) triangle.getFill());
            fillReferenceTransition.setToValue(state.color);

            ParallelTransition parallelTransition = new ParallelTransition(rotateTransition, fillIndicatorTransition, fillReferenceTransition);
            parallelTransition.play();
        }
        valueText.setText(String.format(locale, formatString, VALUE));

        highText.setText(String.format(locale, formatString, high));
        lowText.setText(String.format(locale, formatString, low));

        if (!tile.isTextVisible() && null != movingAverage.getTimeSpan()) {
            timeSpanText.setText(createTimeSpanText());
            text.setText(timeFormatter.format(movingAverage.getLastEntry().getTimestampAsDateTime(tile.getZoneId())));

        }
        resizeDynamicText();
    }

    private void addData(final double VALUE) {
        if (!dot.isVisible()) {
            dot.setVisible(true);
            referenceLine.setVisible(true);
        }
        if (dataList.isEmpty()) {
            double referenceValue = tile.getReferenceValue() != 0 ? tile.getReferenceValue() : VALUE;
            for (int i = 0 ; i < noOfDatapoints ; i ++) {
                dataList.add(referenceValue);
            }
            if (tile.isAutoReferenceValue()) { tile.setReferenceValue(referenceValue); }
        }
        if (dataList.size() <= noOfDatapoints) {
            Collections.rotate(dataList, -1);
            dataList.set((noOfDatapoints - 1), VALUE);
            if (tile.isAutoReferenceValue()) { tile.setReferenceValue(dataList.get(0)); }
        } else {
            dataList.add(VALUE);
        }
    }

    private void updateState(final double VALUE, final double REFERENCE_VALUE) {
        if (Double.compare(VALUE, REFERENCE_VALUE) > 0) {
            state = State.INCREASE;
        } else if (Double.compare(VALUE, REFERENCE_VALUE) < 0) {
            state = State.DECREASE;
        } else {
            state = State.CONSTANT;
        }
        changeText.setTextFill(state.color);
        changePercentageText.setFill(state.color);
        triangle.setFill(state.color);
    }

    private void drawTriangle() {
        MoveTo       moveTo        = new MoveTo(0.056 * size * 0.5, 0.032 * size * 0.5);
        CubicCurveTo cubicCurveTo1 = new CubicCurveTo(0.060 * size * 0.5, 0.028 * size * 0.5, 0.064 * size * 0.5, 0.028 * size * 0.5, 0.068 * size * 0.5, 0.032 * size * 0.5);
        CubicCurveTo cubicCurveTo2 = new CubicCurveTo(0.068 * size * 0.5, 0.032 * size * 0.5, 0.120 * size * 0.5, 0.080 * size * 0.5, 0.12 * size * 0.5,  0.080 * size * 0.5);
        CubicCurveTo cubicCurveTo3 = new CubicCurveTo(0.128 * size * 0.5, 0.088 * size * 0.5, 0.124 * size * 0.5, 0.096 * size * 0.5, 0.112 * size * 0.5, 0.096 * size * 0.5);
        CubicCurveTo cubicCurveTo4 = new CubicCurveTo(0.112 * size * 0.5, 0.096 * size * 0.5, 0.012 * size * 0.5, 0.096 * size * 0.5, 0.012 * size * 0.5, 0.096 * size * 0.5);
        CubicCurveTo cubicCurveTo5 = new CubicCurveTo(0.0, 0.096 * size * 0.5, -0.004 * size * 0.5, 0.088 * size * 0.5, 0.004 * size * 0.5, 0.080 * size * 0.5);
        CubicCurveTo cubicCurveTo6 = new CubicCurveTo(0.004 * size * 0.5, 0.080 * size * 0.5, 0.056 * size * 0.5, 0.032 * size * 0.5, 0.056 * size * 0.5, 0.032 * size * 0.5);
        ClosePath    closePath     = new ClosePath();
        triangle.getElements().setAll(moveTo, cubicCurveTo1, cubicCurveTo2, cubicCurveTo3, cubicCurveTo4, cubicCurveTo5, cubicCurveTo6, closePath);
    }

    private String createTimeSpanText() {
        long          timeSpan        = movingAverage.getTimeSpan().getEpochSecond();
        StringBuilder timeSpanBuilder = new StringBuilder(movingAverage.isFilling() ? "\u22a2 " : "\u2190 ");
        if (timeSpan > MONTH) { // 1 Month (30 days)
            long   months = (timeSpan / MONTH);
            double days   = timeSpan % MONTH;
            timeSpanBuilder.append(months).append("M").append(String.format(locale, "%.0f", days)).append("d").append(" \u2192");
        } else if (timeSpan > DAY) { // 1 Day
            long   days  = (timeSpan / DAY);
            double hours = (timeSpan - (days * DAY)) / HOUR;
            timeSpanBuilder.append(days).append("d").append(String.format(locale, "%.0f", hours)).append("h").append(" \u2192");
        } else if (timeSpan > HOUR) { // 1 Hour
            long   hours   = (timeSpan / HOUR);
            double minutes = (timeSpan - (hours * HOUR)) / MINUTE;
            timeSpanBuilder.append(hours).append("h").append(String.format(locale, "%.0f", minutes)).append("m").append(" \u2192");
        } else if (timeSpan > MINUTE) { // 1 Minute
            long   minutes = (timeSpan / MINUTE);
            double seconds = (timeSpan - (minutes * MINUTE));
            timeSpanBuilder.append(minutes).append("m").append(String.format(locale, "%.0f", seconds)).append("s").append(" \u2192");
        } else {
            long seconds = timeSpan;
            timeSpanBuilder.append(seconds).append("s").append(" \u2192");
        }
        return timeSpanBuilder.toString();
    }

    @Override public void dispose() {
        tile.averagingPeriodProperty().removeListener(averagingListener);
        super.dispose();
    }


    // ******************** Resizing ******************************************
    @Override protected void resizeDynamicText() {
        double maxWidth = width - size * 0.1;
        double fontSize = size * 0.24;
        valueText.setFont(Fonts.latoRegular(fontSize));
        if (valueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(valueText, maxWidth, fontSize); }

        maxWidth = width - size * 0.55;
        fontSize = size * 0.06;

        changeText.setFont(Fonts.latoRegular(fontSize));

        changePercentageText.setFont(Fonts.latoRegular(fontSize));
        if (changePercentageText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(changePercentageText, maxWidth, fontSize); }

        maxWidth = width - size * 0.7;
        fontSize = size * 0.06;

        highText.setFont(Fonts.latoRegular(fontSize));
        if (highText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(highText, maxWidth, fontSize); }
        highText.setY(graphBounds.getY() - size * 0.0125);

        lowText.setFont(Fonts.latoRegular(fontSize));
        if (lowText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(lowText, maxWidth, fontSize); }
        lowText.setY(height - size * 0.1);

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

        highText.setX(size * 0.05);
        lowText.setX(size * 0.05);
    }

    @Override protected void resize() {
        super.resize();

        graphBounds = new Rectangle(inset, size * 0.6, width - doubleInset, height - size * 0.71);

        referenceLine.setStartX(graphBounds.getX());
        referenceLine.setEndX(graphBounds.getX() + graphBounds.getWidth());

        handleCurrentValue(tile.getValue());
        if (tile.getAveragingPeriod() < 250) {
            sparkLine.setStrokeWidth(size * 0.01);
            dot.setRadius(size * 0.014);
        } else if (tile.getAveragingPeriod() < 500) {
            sparkLine.setStrokeWidth(size * 0.0075);
            dot.setRadius(size * 0.0105);
        } else {
            sparkLine.setStrokeWidth(size * 0.005);
            dot.setRadius(size * 0.007);
        }

        drawTriangle();
        indicatorPane.setPadding(new Insets(0, size * 0.0175, 0, 0));

        resizeStaticText();
        resizeDynamicText();

        changeText.setPrefWidth(0.6 * width - size * 0.1);
        changeText.relocate(width - changeText.getPrefWidth() - size * 0.05, graphBounds.getY() - size * 0.175);

        changePercentageFlow.setPrefWidth(0.6 * width - size * 0.1);
        changePercentageFlow.relocate(width - changePercentageFlow.getPrefWidth() - inset, graphBounds.getY() - size * 0.085);

        valueUnitFlow.setMaxWidth(contentBounds.getWidth());
        valueUnitFlow.setMinWidth(contentBounds.getWidth());
        valueUnitFlow.setPrefWidth(contentBounds.getWidth());
        valueUnitFlow.relocate(contentBounds.getX(), contentBounds.getY());
    }

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(tile.getTitle());
        if (!tile.getDescription().isEmpty()) { text.setText(tile.getDescription()); }

        if (tile.isTextVisible()) {
            text.setText(tile.getText());
        } else if (!tile.isTextVisible() && null != movingAverage.getTimeSpan()) {
            timeSpanText.setText(createTimeSpanText());
            text.setText(timeFormatter.format(movingAverage.getLastEntry().getTimestampAsDateTime(tile.getZoneId())));
        }

        changeText.setText(String.format(locale, "%." + tile.getTickLabelDecimals() + "f", (tile.getCurrentValue() - tile.getReferenceValue())));
        StringBuilder changePercentageTextBuilder = new StringBuilder();
        if (Double.compare(tile.getReferenceValue(), 0.0) == 0) {
            changePercentageTextBuilder.append(String.format(locale, "%." + tile.getTickLabelDecimals() + "f", 0.0));
        } else {
            changePercentageTextBuilder.append(String.format(locale, "%." + tile.getTickLabelDecimals() + "f", (tile.getCurrentValue() / tile.getReferenceValue() * 100.0) - 100.0));
        }
        changePercentageTextBuilder.append("\u0025");
        changePercentageText.setText(changePercentageTextBuilder.toString());

        resizeStaticText();

        titleText.setFill(tile.getTitleColor());
        valueText.setFill(tile.getValueColor());
        highText.setFill(tile.getValueColor());
        lowText.setFill(tile.getValueColor());
        text.setFill(tile.getTextColor());
        timeSpanText.setFill(tile.getTextColor());
        referenceLine.setStroke(tile.getThresholdColor());
        sparkLine.setStroke(tile.getBarColor());
        dot.setFill(tile.getBarColor());
        changeText.setTextFill(state.color);
        changePercentageText.setFill(state.color);
        triangle.setFill(state.color);
    }
}
