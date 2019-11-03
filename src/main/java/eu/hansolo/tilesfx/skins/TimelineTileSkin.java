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
import eu.hansolo.tilesfx.events.TileEvent;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.DoubleExponentialSmoothingForLinearSeries;
import eu.hansolo.tilesfx.tools.DoubleExponentialSmoothingForLinearSeries.Model;
import eu.hansolo.tilesfx.tools.Helper;
import eu.hansolo.tilesfx.tools.MovingAverage;
import eu.hansolo.tilesfx.tools.NiceScale;
import eu.hansolo.tilesfx.tools.Statistics;
import eu.hansolo.tilesfx.tools.TimeData;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static eu.hansolo.tilesfx.tools.Helper.clamp;
import static eu.hansolo.tilesfx.tools.Helper.enableNode;


/**
 * User: hansolo
 * Date: 13.09.19
 * Time: 03:12
 */
public class TimelineTileSkin extends TileSkin {
    private static final int                      SEC_MONTH        = 2_592_000;
    private static final int                      SEC_DAY          = 86_400;
    private static final int                      SEC_HOUR         = 3_600;
    private static final int                      SEC_MINUTE       = 60;
    private static final DateTimeFormatter        MONTH_FORMATTER  = DateTimeFormatter.ofPattern("MM");
    private static final DateTimeFormatter        DAY_FORMATTER    = DateTimeFormatter.ofPattern("dd");
    private static final DateTimeFormatter        HOUR_FORMATTER   = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter        MINUTE_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter        SECOND_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private              DateTimeFormatter        DTF              = DateTimeFormatter.ofPattern("dd.YY HH:mm");
    private              DateTimeFormatter        timeFormatter    = DateTimeFormatter.ofPattern("HH:mm");
    private              Text                     titleText;
    private              Text                     valueText;
    private              Text                     upperUnitText;
    private              Line                     fractionLine;
    private              Text                     unitText;
    private              VBox                     unitFlow;
    private              HBox                     valueUnitFlow;
    private              Text                     averageText;
    private              Text                     averageText2;
    private              Text                     minText;
    private              Text                     maxText;
    private              Text                     highText;
    private              Text                     lowText;
    private              Text                     text;
    private              Text                     timeSpanText;
    private              Rectangle                graphBounds;
    private              Map<ChartData, Circle>   dots;
    private              Path                     path;
    private              Group                    dotGroup;
    private              Rectangle                stdDeviationArea;
    private              Line                     thresholdLine;
    private              Line                     lowerThresholdLine;
    private              Line                     averageLine;
    private              Group                    sectionGroup;
    private              Map<Section, Rectangle>  sections;
    private              Map<Section, Label>      percentageInSections;
    private              Group                    percentageInSectionGroup;
    private              LinearGradient           gradient;
    private              double                   low;
    private              double                   high;
    private              double                   stdDeviation;
    private              int                      noOfDatapoints;
    private              int                      maxNoOfDatapoints;
    private              List<ChartData>          dataList;
    private              List<ChartData>          reducedDataList;
    private              Duration                 timePeriod;
    private              MovingAverage            movingAverage;
    private              InvalidationListener     periodListener;
    private              NiceScale                niceScaleY;
    private              List<Line>               horizontalTickLines;
    private              double                   horizontalLineOffset;
    private              List<Line>               verticalTickLines;
    private              double                   tickLabelFontSize;
    private              List<Text>               tickLabelsX;
    private              List<Text>               tickLabelsY;
    private              Color                    tickLineColor;
    private              Color                    tickLabelColor;
    private              Text                     trendText;
    private              Instant                  lastUpdate;
    private              double                   dotRadius;
    private              EventHandler<MouseEvent> mouseListener;
    private              Tooltip                  dotTooltip;


    // ******************** Constructors **************************************
    public TimelineTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        dotTooltip = new Tooltip("");
        dotTooltip.setAutoHide(true);
        dotTooltip.setHideDelay(javafx.util.Duration.seconds(0));
        dotTooltip.setShowDuration(javafx.util.Duration.seconds(5));

        periodListener     = o -> handleEvents("PERIOD");

        mouseListener      = e -> handleMouseEvents(e);

        timeFormatter      = DateTimeFormatter.ofPattern("HH:mm", tile.getLocale());

        timePeriod         = tile.getTimePeriod();

        if (tile.isAutoScale()) { tile.calcAutoScale(); }

        niceScaleY = new NiceScale(minValue, tile.getMaxValue());
        niceScaleY.setMaxTicks(5);
        tickLineColor       = Color.color(tile.getChartGridColor().getRed(), tile.getChartGridColor().getGreen(), tile.getChartGridColor().getBlue(), 0.5);
        tickLabelColor      = tile.getTickLabelColor();
        horizontalTickLines = new ArrayList<>(5);
        verticalTickLines   = new ArrayList<>(16);
        tickLabelsX         = new ArrayList<>(16);
        tickLabelsY         = new ArrayList<>(5);
        int noOfVerticalLines = getNoOfVerticalLines(Instant.now(), timePeriod);
        for (long i = 0 ; i < noOfVerticalLines ; i++) {
            Line vLine = new Line(0, 0, 0, 0);
            vLine.getStrokeDashArray().addAll(1.0, 2.0);
            vLine.setStroke(Color.TRANSPARENT);
            vLine.setMouseTransparent(true);
            verticalTickLines.add(vLine);
            Text tickLabelX = new Text("");
            tickLabelX.setTextOrigin(VPos.BOTTOM);
            tickLabelX.setMouseTransparent(true);
            tickLabelsX.add(tickLabelX);
        }

        for (int i = 0 ; i < 5 ; i++) {
            Line hLine = new Line(0, 0, 0, 0);
            hLine.getStrokeDashArray().addAll(1.0, 2.0);
            hLine.setStroke(Color.TRANSPARENT);
            hLine.setMouseTransparent(true);
            horizontalTickLines.add(hLine);
            Text tickLabelY = new Text("");
            tickLabelY.setFill(Color.TRANSPARENT);
            tickLabelY.setMouseTransparent(true);
            tickLabelsY.add(tickLabelY);
        }

        low               = maxValue;
        high              = minValue;
        stdDeviation      = 0;
        movingAverage     = tile.getMovingAverage();
        dataList          = new ArrayList<>();
        reducedDataList   = new ArrayList<>();
        dotRadius         = 3;
        noOfDatapoints    = calcNumberOfDatapointsForPeriod(timePeriod);
        maxNoOfDatapoints = calcNumberOfDatapointsForPeriod(tile.getMaxTimePeriod());

        graphBounds = new Rectangle(PREFERRED_WIDTH * 0.05, PREFERRED_HEIGHT * 0.5, PREFERRED_WIDTH * 0.9, PREFERRED_HEIGHT * 0.45);

        tile.setAveragingPeriod(noOfDatapoints);

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

        unitFlow = new VBox(upperUnitText, unitText);
        unitFlow.setAlignment(Pos.CENTER_RIGHT);

        valueUnitFlow = new HBox(valueText, unitFlow);
        valueUnitFlow.setAlignment(Pos.BOTTOM_RIGHT);

        averageText = new Text(String.format(locale, "\u2300 " + formatString, tile.getAverage()));
        averageText.setFill(Tile.FOREGROUND);
        Helper.enableNode(averageText, tile.isAverageVisible());

        averageText2 = new Text(String.format(locale, "\u2300 " + formatString, tile.getAverage()));
        averageText2.setFill(Tile.FOREGROUND);
        Helper.enableNode(averageText2, tile.isAverageVisible());

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
        tile.getSections().forEach(section -> {
            Rectangle sectionRect = new Rectangle();
            sectionRect.setMouseTransparent(true);
            sections.put(section, sectionRect);
        });
        sectionGroup = new Group();
        sectionGroup.getChildren().addAll(sections.values());
        Helper.enableNode(sectionGroup, tile.getSectionsVisible());

        percentageInSections = new HashMap<>();
        tile.getSections().forEach(section -> {
            Label sectionLabel = new Label();
            sectionLabel.setAlignment(Pos.CENTER_RIGHT);
            sectionLabel.setTextFill(tile.getTextColor());
            percentageInSections.put(section, sectionLabel);
        });
        percentageInSectionGroup = new Group();
        percentageInSectionGroup.getChildren().setAll(percentageInSections.values());
        Helper.enableNode(percentageInSectionGroup, tile.getSectionsVisible());

        trendText = new Text("");
        trendText.setTextOrigin(VPos.TOP);
        trendText.setFill(tile.getTextColor());

        lastUpdate = Instant.now();

        path = new Path();
        path.setMouseTransparent(true);
        path.setStrokeLineJoin(StrokeLineJoin.ROUND);
        path.setStrokeLineCap(StrokeLineCap.ROUND);

        dots = new LinkedHashMap<>(noOfDatapoints);
        dotGroup = new Group();
        if (tile.getDataPointsVisible()) {
            dotGroup.getChildren().setAll(dots.values());
            dotGroup.getChildren().add(path);
        } else {
            dotGroup.getChildren().setAll(path);
        }

        getPane().getChildren().addAll(titleText, valueUnitFlow, fractionLine, sectionGroup, stdDeviationArea, thresholdLine, lowerThresholdLine, dotGroup, percentageInSectionGroup, averageLine, averageText, averageText2, minText, maxText, highText, lowText, trendText, timeSpanText, text);
        getPane().getChildren().addAll(verticalTickLines);
        getPane().getChildren().addAll(horizontalTickLines);
        getPane().getChildren().addAll(tickLabelsX);
        getPane().getChildren().addAll(tickLabelsY);

        TimerTask timerTask = new TimerTask() {
            @Override public void run() {
                Platform.runLater(() -> checkForOutdated());
            }
        };
        Timer timer = new Timer("Timer");
        timer.scheduleAtFixedRate(timerTask, 1000, 500);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        tile.timePeriodProperty().addListener(periodListener);
        tile.getChartData().addListener((ListChangeListener<ChartData>) c -> {
            while(c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach(chartData -> addData(chartData));
                }
            }
            Set<ChartData> dataSet = tile.getChartData().stream().filter(data -> !dataList.contains(data)).collect(Collectors.toSet());
            Platform.runLater(() -> tile.removeChartData(new ArrayList<>(dataSet)));
        });
        tile.getSections().addListener((ListChangeListener<Section>) c -> {
            while(c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach(section -> {
                        Rectangle sectionRect = new Rectangle();
                        sectionRect.setMouseTransparent(true);
                        sections.put(section, sectionRect);
                    });
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
        if(tile.isAnimated()) { tile.setAnimated(false); }
        if (TileEvent.EventType.VISIBILITY.name().equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !tile.getTitle().isEmpty());
            Helper.enableNode(text, tile.isTextVisible());
            Helper.enableNode(valueText, tile.isValueVisible());
            Helper.enableNode(valueUnitFlow, !tile.getUnit().isEmpty());
            Helper.enableNode(timeSpanText, !tile.isTextVisible());
            Helper.enableNode(averageLine, tile.isAverageVisible());
            Helper.enableNode(averageText, tile.isAverageVisible());
            Helper.enableNode(averageText2, tile.isAverageVisible());
            Helper.enableNode(stdDeviationArea, tile.isAverageVisible());
            Helper.enableNode(thresholdLine, tile.isThresholdVisible());
            Helper.enableNode(lowerThresholdLine, tile.isThresholdVisible());
            Helper.enableNode(sectionGroup, tile.getSectionsVisible());
            Helper.enableNode(percentageInSectionGroup, tile.getSectionsVisible());
            Helper.enableNode(trendText, tile.isTrendVisible());
            redraw();
        } else if (TileEvent.EventType.VALUE.name().equals(EVENT_TYPE)) {
            double value = clamp(minValue, maxValue, tile.getValue());
            tile.getChartData().add(new ChartData("", value, Instant.now()));
        } else if (TileEvent.EventType.SECTION.name().equals(EVENT_TYPE)) {
            percentageInSections.clear();
            tile.getSections().forEach(section -> {
                Label sectionLabel = new Label();
                sectionLabel.setAlignment(Pos.CENTER_RIGHT);
                sectionLabel.setTextFill(tile.getTextColor());
                percentageInSections.put(section, sectionLabel);
            });
            percentageInSectionGroup.getChildren().setAll(percentageInSections.values());
        } else if (TileEvent.EventType.TIME_PERIOD.name().equals(EVENT_TYPE)) {
            timePeriod        = tile.getTimePeriod();
            noOfDatapoints    = calcNumberOfDatapointsForPeriod(timePeriod);
            maxNoOfDatapoints = calcNumberOfDatapointsForPeriod(tile.getMaxTimePeriod());
            timeSpanText.setText(createTimeSpanText());
            tile.setAveragingPeriod(noOfDatapoints);

            // Add initial values
            dots.values().forEach(dot -> {
                dot.removeEventHandler(MouseEvent.MOUSE_ENTERED, mouseListener);
                dot.removeEventHandler(MouseEvent.MOUSE_EXITED, mouseListener);
            });
            dots.clear();
            reducedDataList.forEach(data -> {
                Circle dot = new Circle(dotRadius);
                dot.addEventHandler(MouseEvent.MOUSE_ENTERED, mouseListener);
                dot.addEventHandler(MouseEvent.MOUSE_EXITED, mouseListener);
                dots.put(data, dot);
            });
            if (tile.getDataPointsVisible()) {
                dotGroup.getChildren().setAll(dots.values());
                dotGroup.getChildren().add(path);
            } else {
                dotGroup.getChildren().setAll(path);
            }

            redraw();
        } else if (TileEvent.EventType.REGIONS_ON_TOP.name().equals(EVENT_TYPE)) {
            valueUnitFlow.setPrefWidth(width - size * 0.1);
            valueUnitFlow.relocate(size * 0.05, contentBounds.getY());

            fractionLine.setStartX(width - 0.17 * size);
            fractionLine.setStartY(tile.getTitle().isEmpty() ? size * 0.2 : size * 0.3);
            fractionLine.setEndX(width - 0.05 * size);
            fractionLine.setEndY(tile.getTitle().isEmpty() ? size * 0.2 : size * 0.3);
            fractionLine.setStroke(tile.getUnitColor());
            fractionLine.setStrokeWidth(size * 0.005);
        } else if (TileEvent.EventType.CLEAR_DATA.name().equals(EVENT_TYPE)) {
            tile.clearChartData();
            dataList.clear();
            reducedDataList.clear();
            handleCurrentValue(minValue);
            Platform.runLater(() -> {
                path.getElements().clear();
                dots.clear();
                dotGroup.getChildren().clear();
            });
        }
    }

    private void handleMouseEvents(final MouseEvent e) {
        EventType type = e.getEventType();
        Circle    dot  = (Circle) e.getSource();
        ChartData data = dots.entrySet().stream().filter(entry -> entry.getValue().equals(dot)).map(entry -> entry.getKey()).findAny().orElse(null);
        if (MouseEvent.MOUSE_ENTERED.equals(type)) {
            if (null != data) {
                dotTooltip.setX(e.getScreenX());
                dotTooltip.setY(e.getScreenY());
                LocalDateTime localDateTime = LocalDateTime.ofInstant(data.getTimestamp(), tile.getZoneId());
                dotTooltip.setText(String.join("\n", DTF.format(localDateTime), String.format(tile.getLocale(), String.join(" ", formatString, tile.getUnit()), data.getValue())));
                dotTooltip.show(tile.getScene().getWindow());
            }
        } else if (MouseEvent.MOUSE_EXITED.equals(type)) {
            dotTooltip.hide();
        }
    }

    @Override protected void handleCurrentValue(final double VALUE) {
        low  = reducedDataList.stream().min(Comparator.comparingDouble(ChartData::getValue)).map(data -> data.getValue()).orElse(tile.getLowerThreshold());
        high = reducedDataList.stream().max(Comparator.comparingDouble(ChartData::getValue)).map(data -> data.getValue()).orElse(tile.getThreshold());

        range = (maxValue - minValue);

        Instant now = Instant.now();
        lastUpdate = now;

        long maxTime = now.getEpochSecond();
        long minTime = now.minus(timePeriod.toSeconds(), ChronoUnit.SECONDS).getEpochSecond();

        TimeUnit resolution = tile.getTimePeriodResolution();
        long resolutionStep;
        switch(resolution) {
            case DAYS   : resolutionStep = Helper.SECONDS_PER_DAY; break;
            case HOURS  : resolutionStep = Helper.SECONDS_PER_HOUR; break;
            case MINUTES: resolutionStep = Helper.SECONDS_PER_MINUTE; break;
            case SECONDS:
            default     : resolutionStep = 1;      break;
        }

        double minX  = graphBounds.getX();
        double maxX  = minX + graphBounds.getWidth();
        double minY  = graphBounds.getY();
        double maxY  = minY + graphBounds.getHeight();
        double stepX = graphBounds.getWidth() / timePeriod.getSeconds();
        double stepY = graphBounds.getHeight() / range;

        niceScaleY.setMinMax(minValue, maxValue);
        int    lineCountY        = 1;
        int    tickLabelOffsetY  = 1;
        double tickSpacingY      = niceScaleY.getTickSpacing();
        double tickStepY         = tickSpacingY * stepY;
        double tickStartY        = maxY - tickStepY;
        if (tickSpacingY < minValue) {
            tickLabelOffsetY = (int) (minValue / tickSpacingY) + 1;
            tickStartY = maxY - (tickLabelOffsetY * tickSpacingY - minValue) * stepY;
        }

        verticalTickLines.forEach(line -> line.setStroke(Color.TRANSPARENT));
        horizontalTickLines.forEach(line -> line.setStroke(Color.TRANSPARENT));
        tickLabelsX.forEach(label -> label.setFill(Color.TRANSPARENT));
        tickLabelsY.forEach(label -> label.setFill(Color.TRANSPARENT));

        horizontalLineOffset = 0;
        for (double y = tickStartY; Math.round(y) > minY; y -= tickStepY) {
            Line line  = horizontalTickLines.get(lineCountY);
            Text label = tickLabelsY.get(lineCountY);
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

        int  lineCountX = 0;
        ZonedDateTime dateTime;
        for (long t = minTime ;  t < maxTime ; t++) {
            dateTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(t), tile.getZoneId());
            double x        = -1;
            String timeText = "";
            if (timePeriod.getSeconds() > Helper.SECONDS_PER_MONTH) {
                if (1 == dateTime.getDayOfMonth() && 0 == dateTime.getHour() && 0 == dateTime.getMinute() && 0 == dateTime.getSecond()) { // Full day
                    x = minX + ((t - minTime) * stepX);
                    timeText = MONTH_FORMATTER.format(dateTime);
                }
            } else if (timePeriod.getSeconds() > Helper.SECONDS_PER_DAY) {
                if (0 == dateTime.getHour() && 0 == dateTime.getMinute() && 0 == dateTime.getSecond()) { // Full day
                    x = minX + ((t - minTime) * stepX);
                    timeText = DAY_FORMATTER.format(dateTime);
                }
            } else if (timePeriod.getSeconds() > Helper.SECONDS_PER_DAY / 2) {
                if (dateTime.getHour() % 2 == 0 && 0 == dateTime.getMinute() && 0 == dateTime.getSecond()) { // Full hour
                    x = minX + ((t - minTime) * stepX);
                    timeText = HOUR_FORMATTER.format(dateTime);
                }
            } else if (timePeriod.getSeconds() > Helper.SECONDS_PER_DAY / 4) {
                if (0 == dateTime.getMinute() && 0 == dateTime.getSecond()) { // Full hour
                    x = minX + ((t - minTime) * stepX);
                    timeText = HOUR_FORMATTER.format(dateTime);
                }
            } else if (timePeriod.getSeconds() > Helper.SECONDS_PER_HOUR) {
                if ((0 == dateTime.getMinute() || 30 == dateTime.getMinute()) && 0 == dateTime.getSecond()) { // Full hour and half hour
                    x = minX + ((t - minTime) * stepX);
                    timeText = HOUR_FORMATTER.format(dateTime);
                }
            } else if (timePeriod.getSeconds() > Helper.SECONDS_PER_MINUTE) {
                if (0 == dateTime.getSecond() && dateTime.getMinute() % 5 == 0) { // 5 minutes
                    x = minX + ((t - minTime) * stepX);
                    timeText = MINUTE_FORMATTER.format(dateTime);
                }
            } else {
                if (dateTime.getSecond() % 10 == 0) { // 10 seconds
                    x = minX + ((t - minTime) * stepX);
                    timeText = SECOND_FORMATTER.format(dateTime);
                }
            }
            if (x > -1) {
                x     = minX + ((t - minTime) * stepX);
                Line   line  = verticalTickLines.get(lineCountX);
                Text   label = tickLabelsX.get(lineCountX);
                label.setText(timeText);
                label.setX(x - (label.getLayoutBounds().getWidth() * 0.5));
                label.setY(graphBounds.getY());
                label.setFill(tickLabelColor);
                line.setStartX(x);
                line.setEndX(x);
                line.setStartY(minY);
                line.setEndY(maxY);
                line.setStroke(tickLineColor);
                lineCountX++;
            }
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

        // Draw dots and line
        if (!reducedDataList.isEmpty()) {
            if (tile.isStrokeWithGradient()) { setupGradient(); }

            Iterator entries = dots.entrySet().iterator();
            Map.Entry<ChartData, Circle> entry = (Map.Entry) entries.next();
            ChartData data = entry.getKey();
            Circle    dot  = entry.getValue();
            path.getElements().clear();
            path.getElements().add(new MoveTo(maxX - (maxTime - data.getTimestamp().getEpochSecond()) * stepX, maxY - Math.abs(minValue - Helper.clamp(minValue, maxValue, data.getValue())) * stepY));
            for (long timeSlot = maxTime ; timeSlot >= minTime ; timeSlot -= resolutionStep) {
                if (data.getTimestamp().getEpochSecond() > timeSlot - resolutionStep) {
                    dot.setCenterX(maxX - (maxTime - data.getTimestamp().getEpochSecond()) * stepX);
                    dot.setCenterY(maxY - Math.abs(minValue - Helper.clamp(minValue, maxValue, data.getValue())) * stepY);
                    dot.setFill(tile.isStrokeWithGradient() ? gradient : tile.getBarColor());
                    path.getElements().add(new LineTo(dot.getCenterX(), dot.getCenterY()));
                    if (entries.hasNext()) {
                        entry = (Map.Entry) entries.next();
                        data = entry.getKey();
                        dot  = entry.getValue();
                    }
                }
            }
            path.setStroke(tile.isStrokeWithGradient() ? gradient : tile.getBarColor());
            if (tile.isSmoothing()) {
                Helper.smoothPath(path, false);
            }

            sections.entrySet().forEach(e -> {
                Section   section   = e.getKey();
                Rectangle rectangle = e.getValue();
                rectangle.setX(minX);
                rectangle.setY(clamp(minY, maxY, maxY - Math.abs(minValue - section.getStop()) * stepY));
                rectangle.setWidth(graphBounds.getWidth());
                rectangle.setHeight(Math.abs(section.getStop() - section.getStart()) * stepY);
                rectangle.setFill(section.getColor());
            });

            double average  = Statistics.getChartDataAverage(reducedDataList);
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

            averageText.setText(String.format(locale, "\u2300 " + formatString, average));
            averageText2.setText(String.format(locale, "\u2300 " + formatString, average));
        }
        valueText.setText(String.format(locale, formatString, VALUE));

        if (!tile.isTextVisible() && null != movingAverage.getTimeSpan()) {
            timeSpanText.setText(createTimeSpanText());
            text.setText(HOUR_FORMATTER.format(movingAverage.getLastEntry().getTimestampAsDateTime(tile.getZoneId())));
        }

        resizeDynamicText();
    }

    private void addData(final ChartData DATA) {
        if (dataList.size() >= maxNoOfDatapoints) {
            Collections.rotate(dataList, -1);
            if (!dataList.isEmpty()) { dataList.set((noOfDatapoints - 1), DATA); }
        } else {
            dataList.add(DATA);
            if (tile.isAveragingEnabled()) { movingAverage.addData(new TimeData(DATA.getValue(), DATA.getTimestamp())); }
        }

        Predicate<ChartData> isNotInTimePeriod = chartData -> !chartData.isWithinTimePeriod(Instant.now(), timePeriod);
        reducedDataList.clear();
        reducedDataList.addAll(dataList);
        reducedDataList.removeIf(isNotInTimePeriod);

        if (reducedDataList.size() == Integer.MAX_VALUE - 1 || reducedDataList.size() >= noOfDatapoints) {
            Collections.rotate(reducedDataList, -1);
            if (!reducedDataList.isEmpty()) { reducedDataList.set((noOfDatapoints - 1), DATA); }
        }
        Collections.sort(reducedDataList, Comparator.comparing(ChartData::getTimestamp).reversed());

        dots.values().forEach(dot -> {
            dot.removeEventHandler(MouseEvent.MOUSE_ENTERED, mouseListener);
            dot.removeEventHandler(MouseEvent.MOUSE_EXITED, mouseListener);
        });
        dots.clear();
        reducedDataList.forEach(data -> {
            Circle dot = new Circle(dotRadius);
            dot.addEventHandler(MouseEvent.MOUSE_ENTERED, mouseListener);
            dot.addEventHandler(MouseEvent.MOUSE_EXITED, mouseListener);
            dots.put(data, dot);
        });
        if (tile.getDataPointsVisible()) {
            dotGroup.getChildren().setAll(dots.values());
            dotGroup.getChildren().add(path);
        } else {
            dotGroup.getChildren().setAll(path);
        }

        int n = Helper.clamp(2, reducedDataList.size(), tile.getNumberOfValuesForTrendCalculation());
        if (reducedDataList.size() > n) {
            List<Double> firstNValues = reducedDataList.stream().map(ChartData::getValue).limit(n).collect(Collectors.toList());
            Model        model        = DoubleExponentialSmoothingForLinearSeries.fit(firstNValues.stream().mapToDouble(Double::doubleValue).toArray(), 0.8, 0.2);
            String       forecast     = String.format(tile.getLocale(), "%.0f", model.forecast(1)[0]);
            double       stepX        = graphBounds.getWidth() / (noOfDatapoints - 1);
            double       trendAngle   = (Helper.getAngleFromXY(0, DATA.getValue(), stepX, model.forecast(1)[0]) - 90);
            if (90 <= trendAngle && trendAngle < 112.5) {
                trendText.setText("\u2191");
            } else if (112.5 <= trendAngle && trendAngle < 147.5) {
                trendText.setText("\u2197");
            } else if (147.5 <= trendAngle && trendAngle < 202.5) {
                trendText.setText("\u2192");
            } else if (202.5 <= trendAngle && trendAngle < 247.5) {
                trendText.setText("\u2198");
            } else if (247.5 <= trendAngle && trendAngle < 270) {
                trendText.setText("\u2193");
            } else {
                trendText.setText("");
            }
        }

        stdDeviation = Statistics.getChartDataStdDev(reducedDataList);

        analyse(reducedDataList);

        handleCurrentValue(DATA.getValue());
    }

    private void setupGradient() {
        gradient = new LinearGradient(0, graphBounds.getY() + graphBounds.getHeight(), 0, graphBounds.getY(), false, CycleMethod.NO_CYCLE, tile.getGradientStops());
    }

    private int calcNumberOfDatapointsForPeriod(final Duration TIME_PERIOD) {
        return Helper.calcNumberOfDatapointsForPeriod(TIME_PERIOD, tile.getTimePeriodResolution());
    }

    private String createTimeSpanText() {
        long          timeSpan        = timePeriod.getSeconds();
        StringBuilder timeSpanBuilder = new StringBuilder();
        if (timeSpan > SEC_MONTH) { // 1 Month (30 days)
            int    months = (int)(timeSpan / SEC_MONTH);
            double days   = timeSpan % SEC_MONTH;
            timeSpanBuilder.append(months).append("M");
            if(days > 0) { timeSpanBuilder.append(String.format(Locale.US, "%.0f", days)).append("d"); }
        } else if (timeSpan > SEC_DAY) { // 1 Day
            int    days  = (int) (timeSpan / SEC_DAY);
            double hours = (timeSpan - (days * SEC_DAY)) / SEC_HOUR;
            timeSpanBuilder.append(days).append("d");
            if (hours > 0) { timeSpanBuilder.append(String.format(Locale.US, "%.0f", hours)).append("h"); }
        } else if (timeSpan > SEC_HOUR) { // 1 Hour
            int    hours   = (int)(timeSpan / SEC_HOUR);
            double minutes = (timeSpan - (hours * SEC_HOUR)) / SEC_MINUTE;
            timeSpanBuilder.append(hours).append("h");
            if (minutes > 0) { timeSpanBuilder.append(String.format(Locale.US, "%.0f", minutes)).append("m"); }
        } else if (timeSpan > SEC_MINUTE) { // 1 Minute
            int    minutes = (int)(timeSpan / SEC_MINUTE);
            double seconds = (timeSpan - (minutes * SEC_MINUTE));
            timeSpanBuilder.append(minutes).append("m");
            if (seconds > 0) { timeSpanBuilder.append(String.format(Locale.US, "%.0f", seconds)).append("s"); }
        } else {
            int seconds = (int)timeSpan;
            timeSpanBuilder.append(seconds).append("s");
        }
        return timeSpanBuilder.toString();
    }

    @Override public void dispose() {
        tile.timePeriodProperty().removeListener(periodListener);
        super.dispose();
    }

    private void checkForOutdated() {
        valueText.setOpacity(((Instant.now().toEpochMilli() - lastUpdate.toEpochMilli())) > tile.getTimeoutMs() ? 0.5 : 1.0);
    }

    private void analyse(final List<ChartData> clampedDataList) {
        double noOfPointsInTimePeriod = clampedDataList.size();
        percentageInSections.entrySet().forEach(entry -> {
            double noOfPointsInSection = clampedDataList.stream().filter(chartData -> entry.getKey().contains(chartData.getValue())).mapToDouble(ChartData::getValue).count();
            entry.getValue().setText(String.format(tile.getLocale(), "%.0f%%", ((noOfPointsInSection / noOfPointsInTimePeriod * 100))));
        });
    }

    private int getNoOfVerticalLines(final Instant START, final Duration TIME_PERIOD) {
        long maxTime    = START.getEpochSecond();
        long minTime    = START.minus(TIME_PERIOD.toSeconds(), ChronoUnit.SECONDS).getEpochSecond();
        int  lineCountX = 0;
        ZonedDateTime dateTime;
        for (long t = minTime ;  t < maxTime ; t++) {
            dateTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(t), tile.getZoneId());
            if (TIME_PERIOD.getSeconds() > Helper.SECONDS_PER_MONTH) {
                if (1 == dateTime.getDayOfMonth() && 0 == dateTime.getHour() && 0 == dateTime.getMinute() && 0 == dateTime.getSecond()) { // Full day
                    lineCountX++;
                }
            } else if (TIME_PERIOD.getSeconds() > Helper.SECONDS_PER_DAY) {
                if (0 == dateTime.getHour() && 0 == dateTime.getMinute() && 0 == dateTime.getSecond()) { // Full day
                    lineCountX++;
                }
            } else if (TIME_PERIOD.getSeconds() > Helper.SECONDS_PER_DAY / 2) {
                if (dateTime.getHour() % 2 == 0 && 0 == dateTime.getMinute() && 0 == dateTime.getSecond()) { // Full hour
                    lineCountX++;
                }
            } else if (TIME_PERIOD.getSeconds() > Helper.SECONDS_PER_DAY / 4) {
                if (0 == dateTime.getMinute() && 0 == dateTime.getSecond()) { // Full hour
                    lineCountX++;
                }
            } else if (TIME_PERIOD.getSeconds() > Helper.SECONDS_PER_HOUR) {
                if ((0 == dateTime.getMinute() || 30 == dateTime.getMinute()) && 0 == dateTime.getSecond()) { // Full hour and half hour
                    lineCountX++;
                }
            } else if (TIME_PERIOD.getSeconds() > Helper.SECONDS_PER_MINUTE) {
                if (0 == dateTime.getSecond() && dateTime.getMinute() % 5 == 0) { // 5 minutes
                    lineCountX++;
                }
            } else {
                if (dateTime.getSecond() % 10 == 0) { // 10 seconds
                    lineCountX++;
                }
            }
        }
        return lineCountX;
    }


    // ******************** Resizing ******************************************
    @Override protected void resizeDynamicText() {
        double maxWidth = valueUnitFlow.isVisible() ? (width - (size * 0.275)) : (width - (size * 0.1));
        double fontSize = size * 0.24;
        valueText.setFont(Fonts.latoRegular(fontSize));
        if (valueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(valueText, maxWidth, fontSize); }

        maxWidth = width - size * 0.7;
        fontSize = size * 0.03;
        averageText.setFont(Fonts.latoRegular(fontSize));
        if (averageText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(averageText, maxWidth, fontSize); }
        if (averageLine.getStartY() < graphBounds.getY() + graphBounds.getHeight() * 0.5) {
            averageText.setY(averageLine.getStartY() + (size * 0.0425));
        } else {
            averageText.setY(averageLine.getStartY() - (size * 0.0075));
        }
        averageText.setVisible(fontSize > 6);

        fontSize = size * 0.06;
        minText.setFont(Fonts.latoRegular(fontSize));
        if (minText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(minText, maxWidth, fontSize); }
        minText.setY(height - size * 0.1);

        maxText.setFont(Fonts.latoRegular(fontSize));
        if (maxText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(maxText, maxWidth, fontSize); }
        maxText.setY(graphBounds.getY() - size * 0.0175);

        lowText.setFont(Fonts.latoRegular(fontSize));
        if (lowText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(lowText, maxWidth, fontSize); }
        lowText.setY(height - size * 0.1);

        highText.setFont(Fonts.latoRegular(fontSize));
        if (highText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(highText, maxWidth, fontSize); }
        highText.setY(graphBounds.getY() - size * 0.0175);

        trendText.setFont(Fonts.latoRegular(fontSize));
        if (trendText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(trendText, maxWidth, fontSize); }
        trendText.relocate((width - trendText.getLayoutBounds().getWidth()) * 0.25, height - size * 0.1);

        averageText2.setFont(Fonts.latoRegular(fontSize));
        if (averageText2.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(averageText2, maxWidth, fontSize); }
        averageText2.relocate((width - averageText2.getLayoutBounds().getWidth()) * 0.75, height - size * 0.1);

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

        percentageInSections.entrySet().forEach(entry -> {
            entry.getValue().setFont(Fonts.latoRegular(size * 0.025));
            entry.getValue().setPrefWidth(size * 0.065);
            entry.getValue().relocate(size * 0.05, sections.get(entry.getKey()).getLayoutBounds().getCenterY() - entry.getValue().getLayoutBounds().getCenterY());
            entry.getValue().setVisible(size * 0.025 > 6);
        });
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

        maxWidth = width - (width - size * 0.275);
        fontSize = upperUnitText.getText().isEmpty() ? size * 0.12 : size * 0.10;
        upperUnitText.setFont(Fonts.latoRegular(fontSize));
        if (upperUnitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(upperUnitText, maxWidth, fontSize); }

        fontSize = upperUnitText.getText().isEmpty() ? size * 0.12 : size * 0.10;
        unitText.setFont(Fonts.latoRegular(fontSize));
        if (unitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(unitText, maxWidth, fontSize); }

        lowText.setX(size * 0.05);
        highText.setX(size * 0.05);
        averageText.setX(size * 0.15);
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

        double miniLabelFontSize = size * 0.022;
        Font miniTickLabelFont = Fonts.latoRegular(miniLabelFontSize);
        tickLabelsX.forEach(label -> {
            enableNode(label, miniLabelFontSize >= 6);
            label.setFont(miniTickLabelFont);
        });
        verticalTickLines.forEach(line -> line.setStrokeWidth(0.5));

        stdDeviationArea.setX(graphBounds.getX());
        stdDeviationArea.setWidth(graphBounds.getWidth());

        thresholdLine.getStrokeDashArray().setAll(graphBounds.getWidth() * 0.01, graphBounds.getWidth() * 0.01);
        lowerThresholdLine.getStrokeDashArray().setAll(graphBounds.getWidth() * 0.01, graphBounds.getWidth() * 0.01);
        averageLine.getStrokeDashArray().setAll(graphBounds.getWidth() * 0.01, graphBounds.getWidth() * 0.01);

        handleCurrentValue(Double.parseDouble(valueText.getText()));

        if (noOfDatapoints < 60) {
            dotRadius = size * 0.01;
        } else if (noOfDatapoints < 3600) {
            dotRadius = size * 0.0075;
        } else {
            dotRadius = size * 0.005;
        }
        dots.values().forEach(dot -> dot.setRadius(dotRadius));

        path.setStrokeWidth(size * 0.01);

        if (tile.isStrokeWithGradient()) { setupGradient(); }

        resizeStaticText();
        resizeDynamicText();

        valueUnitFlow.setPrefWidth(width - size * 0.1);
        valueUnitFlow.relocate(size * 0.05, contentBounds.getY());
        valueUnitFlow.setMaxHeight(valueText.getFont().getSize());

        fractionLine.setStartX(width - 0.17 * size);
        fractionLine.setStartY(tile.getTitle().isEmpty() ? size * 0.2 : size * 0.3);
        fractionLine.setEndX(width - 0.05 * size);
        fractionLine.setEndY(tile.getTitle().isEmpty() ? size * 0.2 : size * 0.3);
        fractionLine.setStroke(tile.getUnitColor());
        fractionLine.setStrokeWidth(size * 0.005);

        unitFlow.setTranslateY(-size * 0.005);
    }

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(tile.getTitle());
        text.setText(tile.getText());

        if (tile.getUnit().contains("/")) {
            String[] units = tile.getUnit().split("/");
            upperUnitText.setText(units[0]);
            unitText.setText(units[1]);
            Helper.enableNode(fractionLine, true);
        } else {
            upperUnitText.setText(" ");
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
        averageText.setFill(tile.getForegroundColor());
        averageText2.setFill(tile.getForegroundColor());
        timeSpanText.setFill(tile.getTextColor());
        stdDeviationArea.setFill(Helper.getColorWithOpacity(Tile.FOREGROUND, 0.1));
    }
}
