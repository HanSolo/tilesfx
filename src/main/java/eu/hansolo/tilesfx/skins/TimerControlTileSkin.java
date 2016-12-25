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
import eu.hansolo.tilesfx.TimeSection;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.Helper;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by hansolo on 20.12.16.
 */
public class TimerControlTileSkin extends TileSkin {
    private static final double                CLOCK_SCALE_FACTOR = 0.75;
    private              DateTimeFormatter     dateFormatter;
    private              double                clockSize;
    private              Pane                  sectionsPane;
    private              Path                  minuteTickMarks;
    private              Path                  hourTickMarks;
    private              Rectangle             hour;
    private              Rectangle             minute;
    private              Rectangle             second;
    private              Circle                knob;
    private              Text                  title;
    private              Text                  amPmText;
    private              Text                  dateText;
    private              Text                  text;
    private              Rotate                hourRotate;
    private              Rotate                minuteRotate;
    private              Rotate                secondRotate;
    private              Group                 shadowGroupHour;
    private              Group                 shadowGroupMinute;
    private              Group                 shadowGroupSecond;
    private              DropShadow            dropShadow;
    private              Map<TimeSection, Arc> sectionMap;


    // ******************** Constructors **************************************
    public TimerControlTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        dateFormatter = DateTimeFormatter.ofPattern("EE d", getSkinnable().getLocale());

        sectionMap   = new HashMap<>(getSkinnable().getTimeSections().size());
        for (TimeSection section : getSkinnable().getTimeSections()) { sectionMap.put(section, new Arc()); }

        minuteRotate = new Rotate();
        hourRotate   = new Rotate();
        secondRotate = new Rotate();

        sectionsPane = new Pane();
        sectionsPane.getChildren().addAll(sectionMap.values());
        Helper.enableNode(sectionsPane, getSkinnable().isSecondsVisible());

        minuteTickMarks = new Path();
        minuteTickMarks.setFillRule(FillRule.EVEN_ODD);
        minuteTickMarks.setFill(null);
        minuteTickMarks.setStroke(getSkinnable().getMinuteColor());
        minuteTickMarks.setStrokeLineCap(StrokeLineCap.ROUND);

        hourTickMarks = new Path();
        hourTickMarks.setFillRule(FillRule.EVEN_ODD);
        hourTickMarks.setFill(null);
        hourTickMarks.setStroke(getSkinnable().getHourColor());
        hourTickMarks.setStrokeLineCap(StrokeLineCap.ROUND);

        hour = new Rectangle(3, 60);
        hour.setArcHeight(3);
        hour.setArcWidth(3);
        hour.setStroke(getSkinnable().getHourColor());
        hour.getTransforms().setAll(hourRotate);

        minute = new Rectangle(3, 96);
        minute.setArcHeight(3);
        minute.setArcWidth(3);
        minute.setStroke(getSkinnable().getMinuteColor());
        minute.getTransforms().setAll(minuteRotate);

        second = new Rectangle(1, 96);
        second.setArcHeight(1);
        second.setArcWidth(1);
        second.setStroke(getSkinnable().getSecondColor());
        second.getTransforms().setAll(secondRotate);
        second.setVisible(getSkinnable().isSecondsVisible());
        second.setManaged(getSkinnable().isSecondsVisible());

        knob = new Circle(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.5, 4.5);
        knob.setStroke(Color.web("#282a3280"));

        dropShadow = new DropShadow();
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.25));
        dropShadow.setBlurType(BlurType.TWO_PASS_BOX);
        dropShadow.setRadius(0.015 * PREFERRED_WIDTH);
        dropShadow.setOffsetY(0.015 * PREFERRED_WIDTH);

        shadowGroupHour   = new Group(hour);
        shadowGroupMinute = new Group(minute);
        shadowGroupSecond = new Group(second, knob);

        shadowGroupHour.setEffect(getSkinnable().isShadowsEnabled() ? dropShadow : null);
        shadowGroupMinute.setEffect(getSkinnable().isShadowsEnabled() ? dropShadow : null);
        shadowGroupSecond.setEffect(getSkinnable().isShadowsEnabled() ? dropShadow : null);

        title = new Text("");
        title.setTextOrigin(VPos.TOP);
        Helper.enableNode(title, !getSkinnable().getTitle().isEmpty());

        amPmText = new Text(getSkinnable().getTime().get(ChronoField.AMPM_OF_DAY) == 0 ? "AM" : "PM");

        dateText = new Text("");
        Helper.enableNode(dateText, getSkinnable().isDateVisible());

        text = new Text("");
        Helper.enableNode(text, getSkinnable().isTextVisible());

        getPane().getChildren().addAll(sectionsPane, hourTickMarks, minuteTickMarks, title, amPmText, dateText, text, shadowGroupHour, shadowGroupMinute, shadowGroupSecond);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        if (getSkinnable().isAnimated()) {
            getSkinnable().currentTimeProperty().addListener(o -> updateTime(
                ZonedDateTime.ofInstant(Instant.ofEpochSecond(getSkinnable().getCurrentTime()), ZoneId.of(ZoneId.systemDefault().getId()))));
        } else {
            getSkinnable().timeProperty().addListener(o -> updateTime(getSkinnable().getTime()));
        }
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(title, !getSkinnable().getTitle().isEmpty());
            Helper.enableNode(text, getSkinnable().isTextVisible());
            Helper.enableNode(dateText, getSkinnable().isDateVisible());
            Helper.enableNode(second, getSkinnable().isSecondsVisible());
            Helper.enableNode(sectionsPane, getSkinnable().isSecondsVisible());
        } else if ("SECTION".equals(EVENT_TYPE)) {
            sectionMap.clear();
            for (TimeSection section : getSkinnable().getTimeSections()) { sectionMap.put(section, new Arc()); }
            sectionsPane.getChildren().setAll(sectionMap.values());
            resize();
            redraw();
        }
    };

    private void drawTicks() {
        minuteTickMarks.setCache(false);
        hourTickMarks.setCache(false);
        minuteTickMarks.getElements().clear();
        hourTickMarks.getElements().clear();
        double  sinValue;
        double  cosValue;
        double  startAngle             = 180;
        double  angleStep              = 360 / 60;
        Point2D center                 = new Point2D(clockSize * 0.5, clockSize * 0.5);
        boolean hourTickMarksVisible   = getSkinnable().isHourTickMarksVisible();
        boolean minuteTickMarksVisible = getSkinnable().isMinuteTickMarksVisible();
        for (double angle = 0, counter = 0 ; Double.compare(counter, 59) <= 0 ; angle -= angleStep, counter++) {
            sinValue = Math.sin(Math.toRadians(angle + startAngle));
            cosValue = Math.cos(Math.toRadians(angle + startAngle));

            Point2D innerPoint       = new Point2D(center.getX() + clockSize * 0.405 * sinValue, center.getY() + clockSize * 0.405 * cosValue);
            Point2D innerMinutePoint = new Point2D(center.getX() + clockSize * 0.435 * sinValue, center.getY() + clockSize * 0.435 * cosValue);
            Point2D outerPoint       = new Point2D(center.getX() + clockSize * 0.465 * sinValue, center.getY() + clockSize * 0.465 * cosValue);

            if (counter % 5 == 0) {
                // Draw hour tickmark
                if (hourTickMarksVisible) {
                    hourTickMarks.setStrokeWidth(clockSize * 0.01);
                    hourTickMarks.getElements().add(new MoveTo(innerPoint.getX(), innerPoint.getY()));
                    hourTickMarks.getElements().add(new LineTo(outerPoint.getX(), outerPoint.getY()));
                } else if (minuteTickMarksVisible) {
                    minuteTickMarks.setStrokeWidth(clockSize * 0.005);
                    minuteTickMarks.getElements().add(new MoveTo(innerMinutePoint.getX(), innerMinutePoint.getY()));
                    minuteTickMarks.getElements().add(new LineTo(outerPoint.getX(), outerPoint.getY()));
                }
            } else if (counter % 1 == 0 && minuteTickMarksVisible) {
                // Draw minute tickmark
                minuteTickMarks.setStrokeWidth(clockSize * 0.005);
                minuteTickMarks.getElements().add(new MoveTo(innerMinutePoint.getX(), innerMinutePoint.getY()));
                minuteTickMarks.getElements().add(new LineTo(outerPoint.getX(), outerPoint.getY()));
            }
        }
        minuteTickMarks.setCache(true);
        minuteTickMarks.setCacheHint(CacheHint.QUALITY);
        hourTickMarks.setCache(true);
        hourTickMarks.setCacheHint(CacheHint.QUALITY);
    }

    private void drawTimeSections() {
        if (sectionMap.isEmpty()) return;
        ZonedDateTime     time              = getSkinnable().getTime();
        boolean           isAM              = time.get(ChronoField.AMPM_OF_DAY) == 0;
        double            offset            = 90;
        double            angleStep         = 360.0 / 60.0;
        boolean           highlightSections = getSkinnable().isHighlightSections();
        for (TimeSection section : sectionMap.keySet()) {
            LocalTime   start     = section.getStart();
            LocalTime   stop      = section.getStop();
            boolean     isStartAM = start.get(ChronoField.AMPM_OF_DAY) == 0;
            boolean     isStopAM  = stop.get(ChronoField.AMPM_OF_DAY) == 0;
            boolean     draw      = isAM ? (isStartAM || isStopAM) : (!isStartAM || !isStopAM);
            if (draw) {
                double sectionStartAngle = (start.getHour() % 12 * 5.0 + start.getMinute() / 12.0 + start.getSecond() / 300.0) * angleStep + 180;
                double sectionAngleExtend = ((stop.getHour() - start.getHour()) % 12 * 5.0 + (stop.getMinute() - start.getMinute()) / 12.0 + (stop.getSecond() - start.getSecond()) / 300.0) * angleStep;
                if (start.getHour() > stop.getHour()) { sectionAngleExtend = (360.0 - Math.abs(sectionAngleExtend)); }

                Arc arc = sectionMap.get(section);
                arc.setCenterX(clockSize * 0.5);
                arc.setCenterY(clockSize * 0.5);
                arc.setRadiusX(clockSize * 0.45);
                arc.setRadiusY(clockSize * 0.45);
                arc.setStartAngle(-(offset + sectionStartAngle));
                arc.setLength(-sectionAngleExtend);
                arc.setType(ArcType.OPEN);
                arc.setStrokeWidth(clockSize * 0.04);
                arc.setStrokeLineCap(StrokeLineCap.BUTT);

                if (highlightSections) {
                    arc.setStroke(section.contains(time.toLocalTime()) ? section.getHighlightColor() : section.getColor());
                } else {
                    arc.setStroke(section.getColor());
                }
            }
        }
    }

    public void updateTime(final ZonedDateTime TIME) {
        if (getSkinnable().isDiscreteHours()) {
            hourRotate.setAngle(TIME.getHour() * 30);
        } else {
            hourRotate.setAngle(0.5 * (60 * TIME.getHour() + TIME.getMinute()));
        }

        if (getSkinnable().isDiscreteMinutes()) {
            minuteRotate.setAngle(TIME.getMinute() * 6);
        } else {
            minuteRotate.setAngle(TIME.getMinute() * 6 + TIME.getSecond() * 0.1);
        }

        if (second.isVisible()) {
            if (getSkinnable().isDiscreteSeconds()) {
                secondRotate.setAngle(TIME.getSecond() * 6);
            } else {
                secondRotate.setAngle(TIME.getSecond() * 6 + TIME.get(ChronoField.MILLI_OF_SECOND) * 0.006);
            }
        }

        if (sectionsVisible) {
            for (TimeSection section : sectionMap.keySet()) {
                if (highlightSections) {
                    sectionMap.get(section).setStroke(section.contains(TIME.toLocalTime()) ? section.getHighlightColor() : section.getColor());
                } else {
                    sectionMap.get(section).setStroke(section.getColor());
                }
            }
        }

        amPmText.setText(getSkinnable().getTime().get(ChronoField.AMPM_OF_DAY) == 0 ? "AM" : "PM");
        Helper.adjustTextSize(amPmText, 0.2 * size, size * 0.05);
        amPmText.setX((size - amPmText.getLayoutBounds().getWidth()) * 0.5);
        amPmText.setY(size * 0.4);

        dateText.setText(dateFormatter.format(TIME).toUpperCase());
        Helper.adjustTextSize(dateText, 0.3 * size, size * 0.05);
        dateText.setX((size - dateText.getLayoutBounds().getWidth()) * 0.5);
        dateText.setY(size * 0.65);
    }


    // ******************** Resizing ******************************************
    @Override protected void resizeDynamicText() {
        double maxWidth = size * 0.9;
        double fontSize = size * 0.06;

        title.setFont(Fonts.latoRegular(fontSize));
        title.setText(getSkinnable().getTitle());
        if (title.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(title, maxWidth, fontSize); }
        title.setX(size * 0.05);
        title.setY(size * 0.05);

        maxWidth = size * 0.2;
        fontSize = size * 0.05;
        amPmText.setText(getSkinnable().getTime().get(ChronoField.AMPM_OF_DAY) == 0 ? "AM" : "PM");
        Helper.adjustTextSize(amPmText, maxWidth, fontSize);
        amPmText.setX((size - amPmText.getLayoutBounds().getWidth()) * 0.5);
        amPmText.setY(size * 0.4);

        maxWidth = size * 0.6;
        dateText.setFont(Fonts.latoRegular(fontSize));
        if (dateText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(dateText, maxWidth, fontSize); }
        dateText.setX((size - dateText.getLayoutBounds().getWidth()) * 0.5);
        dateText.setY(size * 0.65);

        maxWidth = size * 0.9;
        fontSize = size * 0.05;
        text.setText(getSkinnable().getText());
        if (text.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(text, maxWidth, fontSize); }
        text.setX(size * 0.05);
        text.setY(size * 0.95);
    };

    @Override protected void resize() {
        super.resize();
        clockSize     = size * CLOCK_SCALE_FACTOR;
        double center = size * 0.5;

        sectionsPane.setMinSize(clockSize, clockSize);
        sectionsPane.relocate((size - clockSize) * 0.5, (size - clockSize) * 0.5);

        dropShadow.setRadius(0.008 * size);
        dropShadow.setOffsetY(0.008 * size);

        drawTimeSections();
        drawTicks();

        hourTickMarks.relocate((size - hourTickMarks.getLayoutBounds().getWidth()) * 0.5,
                               (size - hourTickMarks.getLayoutBounds().getHeight()) * 0.5);
        minuteTickMarks.relocate((size - minuteTickMarks.getLayoutBounds().getWidth()) * 0.5,
                                 (size - minuteTickMarks.getLayoutBounds().getHeight()) * 0.5);

        hour.setFill(getSkinnable().getHourColor());
        hour.setCache(false);
        hour.setWidth(clockSize * 0.015);
        hour.setHeight(clockSize * 0.29);
        hour.setArcWidth(clockSize * 0.015);
        hour.setArcHeight(clockSize * 0.015);
        hour.setCache(true);
        hour.setCacheHint(CacheHint.ROTATE);
        hour.relocate((size - hour.getWidth()) * 0.5, size * 0.21 / CLOCK_SCALE_FACTOR);

        minute.setFill(getSkinnable().getMinuteColor());
        minute.setCache(false);
        minute.setWidth(clockSize * 0.015);
        minute.setHeight(clockSize * 0.47);
        minute.setArcWidth(clockSize * 0.015);
        minute.setArcHeight(clockSize * 0.015);
        minute.setCache(true);
        minute.setCacheHint(CacheHint.ROTATE);
        minute.relocate((size - minute.getWidth()) * 0.5, size * 0.11 / CLOCK_SCALE_FACTOR);

        second.setFill(getSkinnable().getSecondColor());
        second.setCache(false);
        second.setWidth(clockSize * 0.005);
        second.setHeight(clockSize * 0.47);
        second.setArcWidth(clockSize * 0.015);
        second.setArcHeight(clockSize * 0.015);
        second.setCache(true);
        second.setCacheHint(CacheHint.ROTATE);
        second.relocate((size - second.getWidth()) * 0.5, size * 0.11 / CLOCK_SCALE_FACTOR);

        knob.setFill(getSkinnable().getKnobColor());
        knob.setRadius(clockSize * 0.0225);
        knob.setCenterX(center);
        knob.setCenterY(center);

        minuteRotate.setPivotX(minute.getWidth() * 0.5);
        minuteRotate.setPivotY(minute.getHeight());
        hourRotate.setPivotX(hour.getWidth() * 0.5);
        hourRotate.setPivotY(hour.getHeight());
        secondRotate.setPivotX(second.getWidth() * 0.5);
        secondRotate.setPivotY(second.getHeight());
    };

    @Override protected void redraw() {
        super.redraw();

        dateFormatter = DateTimeFormatter.ofPattern("EE d", getSkinnable().getLocale());

        shadowGroupHour.setEffect(getSkinnable().isShadowsEnabled() ? dropShadow : null);
        shadowGroupMinute.setEffect(getSkinnable().isShadowsEnabled() ? dropShadow : null);
        shadowGroupSecond.setEffect(getSkinnable().isShadowsEnabled() ? dropShadow : null);

        // Tick Marks
        minuteTickMarks.setStroke(getSkinnable().getMinuteColor());
        hourTickMarks.setStroke(getSkinnable().getHourColor());

        ZonedDateTime time = getSkinnable().getTime();

        updateTime(time);

        resizeDynamicText();

        title.setFill(getSkinnable().getTitleColor());
        amPmText.setFill(getSkinnable().getTitleColor());
        dateText.setFill(getSkinnable().getDateColor());
        text.setFill(getSkinnable().getTextColor());
    };
}