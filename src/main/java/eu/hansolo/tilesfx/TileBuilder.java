/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2016-2021 Gerrit Grunwald.
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
package eu.hansolo.tilesfx;

import eu.hansolo.fx.countries.Country;
import eu.hansolo.fx.countries.tools.BusinessRegion;
import eu.hansolo.tilesfx.Tile.ChartType;
import eu.hansolo.tilesfx.Tile.ImageMask;
import eu.hansolo.tilesfx.Tile.ItemSorting;
import eu.hansolo.tilesfx.Tile.ItemSortingTopic;
import eu.hansolo.tilesfx.Tile.MapProvider;
import eu.hansolo.tilesfx.Tile.SkinType;
import eu.hansolo.tilesfx.Tile.TextSize;
import eu.hansolo.tilesfx.Tile.TileColor;
import eu.hansolo.tilesfx.chart.RadarChartMode;
import eu.hansolo.tilesfx.chart.SunburstChart.TextOrientation;
import eu.hansolo.tilesfx.chart.SunburstChart.VisibleData;
import eu.hansolo.tilesfx.chart.TilesFXSeries;
import eu.hansolo.tilesfx.colors.Bright;
import eu.hansolo.tilesfx.events.AlarmEvt;
import eu.hansolo.tilesfx.events.TileEvt;
import eu.hansolo.tilesfx.events.TimeEvt;
import eu.hansolo.tilesfx.skins.BarChartItem;
import eu.hansolo.tilesfx.skins.LeaderBoardItem;
import eu.hansolo.tilesfx.chart.ChartData;
import eu.hansolo.tilesfx.tools.Helper;
import eu.hansolo.tilesfx.tools.MatrixIcon;
import eu.hansolo.tilesfx.tools.Rank;
import eu.hansolo.tilesfx.tools.TreeNode;
import eu.hansolo.toolbox.evt.EvtObserver;
import eu.hansolo.toolboxfx.geom.Location;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


/**
 * Created by hansolo on 13.12.15.
 */
public class TileBuilder<B extends TileBuilder<B>> {
    private HashMap<String, Property> properties = new HashMap<>();


    // ******************** Constructors **************************************
    protected TileBuilder() {}


    // ******************** Methods *******************************************
    public static final TileBuilder create() {
        return new TileBuilder();
    }

    public final B skinType(final SkinType TYPE) {
        properties.put("skinType", new SimpleObjectProperty<>(TYPE));
        return (B)this;
    }

    public final B value(final double VALUE) {
        properties.put("value", new SimpleDoubleProperty(VALUE));
        return (B) this;
    }

    public final B minValue(final double VALUE) {
        properties.put("minValue", new SimpleDoubleProperty(VALUE));
        return (B) this;
    }

    public final B maxValue(final double VALUE) {
        properties.put("maxValue", new SimpleDoubleProperty(VALUE));
        return (B) this;
    }

    public final B threshold(final double VALUE) {
        properties.put("threshold", new SimpleDoubleProperty(VALUE));
        return (B)this;
    }

    public final B lowerThreshold(final double VALUE) {
        properties.put("lowerThreshold", new SimpleDoubleProperty(VALUE));
        return (B)this;
    }

    public final B referenceValue(final double VALUE) {
        properties.put("referenceValue", new SimpleDoubleProperty(VALUE));
        return (B)this;
    }

    public final B autoReferenceValue(final boolean AUTO_REFERENCE_VALUE) {
        properties.put("autoReferenceValue", new SimpleBooleanProperty(AUTO_REFERENCE_VALUE));
        return (B)this;
    }

    public final B decimals(final int DECIMALS) {
        properties.put("decimals", new SimpleIntegerProperty(DECIMALS));
        return (B)this;
    }

    public final B shortenNumbers(final boolean SHORTEN) {
        properties.put("shortenNumbers", new SimpleBooleanProperty(SHORTEN));
        return (B)this;
    }

    public final B tickLabelDecimals(final int DECIMALS) {
        properties.put("tickLabelDecimals", new SimpleIntegerProperty(DECIMALS));
        return (B)this;
    }

    public final B tickLabelsXVisible(final boolean VISIBLE) {
        properties.put("tickLabelsXVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B tickLabelsYVisible(final boolean VISIBLE) {
        properties.put("tickLabelsYVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B minValueVisible(final boolean VISIBLE) {
        properties.put("minValueVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B maxValueVisible(final boolean VISIBLE) {
        properties.put("maxValueVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B title(final String TITLE) {
        properties.put("title", new SimpleStringProperty(TITLE));
        return (B)this;
    }

    public final B titleAlignment(final TextAlignment ALIGNMENT) {
        properties.put("titleAlignment", new SimpleObjectProperty(ALIGNMENT));
        return (B)this;
    }

    public final B description(final String DESCRIPTION) {
        properties.put("description", new SimpleStringProperty(DESCRIPTION));
        return (B)this;
    }

    public final B descriptionAlignment(final Pos ALIGNMENT) {
        properties.put("descriptionAlignment", new SimpleObjectProperty(ALIGNMENT));
        return (B)this;
    }

    public final B unit(final String UNIT) {
        properties.put("unit", new SimpleStringProperty(UNIT));
        return (B)this;
    }

    public final B thumbColor(final Color THUMB_COLOR) {
        properties.put("thumbColor", new SimpleObjectProperty<>(THUMB_COLOR));
        return (B)this;
    }

    public final B flatUI(final boolean FLAT_UI) {
        properties.put("flatUI", new SimpleBooleanProperty(FLAT_UI));
        return (B)this;
    }

    public final B duration(final LocalTime DURATION) {
        properties.put("duration", new SimpleObjectProperty(DURATION));
        return (B)this;
    }

    public final B selected(final boolean SELECTED) {
        properties.put("selected", new SimpleBooleanProperty(SELECTED));
        return (B)this;
    }

    public final B averagingEnabled(final boolean ENABLED) {
        properties.put("averagingEnabled", new SimpleBooleanProperty(ENABLED));
        return (B)this;
    }

    public final B averagingPeriod(final int PERIOD) {
        properties.put("averagingPeriod", new SimpleIntegerProperty(PERIOD));
        return (B)this;
    }

    public final B timePeriod(final java.time.Duration PERIOD) {
        properties.put("timePeriod", new SimpleObjectProperty(PERIOD));
        return (B)this;
    }

    public final B maxTimePeriod(final java.time.Duration MAX_PERIOD) {
        properties.put("maxTimePeriod", new SimpleObjectProperty<>(MAX_PERIOD));
        return (B)this;
    }

    public final B timePeriodResolution(final TimeUnit RESOLUTION) {
        properties.put("timePeriodResolution", new SimpleObjectProperty(RESOLUTION));
        return (B)this;
    }

    public final B fixedYScale(final boolean FIXED_Y_SCALE) {
        properties.put("fixedYScale", new SimpleBooleanProperty(FIXED_Y_SCALE));
        return (B)this;
    }

    public final B foregroundBaseColor(final Color COLOR) {
        properties.put("foregroundBaseColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B startFromZero(final boolean START) {
        properties.put("startFromZero", new SimpleBooleanProperty(START));
        return (B)this;
    }

    public final B returnToZero(final boolean RETURN) {
        properties.put("returnToZero", new SimpleBooleanProperty(RETURN));
        return (B)this;
    }

    public final B minMeasuredValueVisible(final boolean VISIBLE) {
        properties.put("minMeasuredValueVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B maxMeasuredValueVisible(final boolean VISIBLE) {
        properties.put("maxMeasuredValueVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B oldValueVisible(final boolean VISIBLE) {
        properties.put("oldValueVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B valueVisible(final boolean VISIBLE) {
        properties.put("valueVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B foregroundColor(final Color COLOR) {
        properties.put("foregroundColor", new SimpleObjectProperty(COLOR));
        return (B)this;
    }

    public final B backgroundColor(final Color COLOR) {
        properties.put("backgroundColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B borderColor(final Color COLOR) {
        properties.put("borderColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B borderWidth(final double WIDTH) {
        properties.put("borderWidth", new SimpleDoubleProperty(WIDTH));
        return (B)this;
    }

    public final B knobColor(final Color COLOR) {
        properties.put("knobColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B activeColor(final Color COLOR) {
        properties.put("activeColor", new SimpleObjectProperty(COLOR));
        return (B)this;
    }

    public final B animated(final boolean ANIMATED) {
        properties.put("animated", new SimpleBooleanProperty(ANIMATED));
        return (B)this;
    }

    public final B animationDuration(final long DURATION) {
        properties.put("animationDuration", new SimpleLongProperty(DURATION));
        return (B)this;
    }

    public final B pauseDuration(final long DURATION) {
        properties.put("pauseDuration", new SimpleLongProperty(DURATION));
        return (B)this;
    }

    public final B startAngle(final double ANGLE) {
        properties.put("startAngle", new SimpleDoubleProperty(ANGLE));
        return (B)this;
    }

    public final B angleRange(final double RANGE) {
        properties.put("angleRange", new SimpleDoubleProperty(RANGE));
        return (B)this;
    }

    public final B autoScale(final boolean AUTO_SCALE) {
        properties.put("autoScale", new SimpleBooleanProperty(AUTO_SCALE));
        return (B)this;
    }

    public final B needleColor(final Color COLOR) {
        properties.put("needleColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B barColor(final Color COLOR) {
        properties.put("barColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B barBackgroundColor(final Color COLOR) {
        properties.put("barBackgroundColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B locale(final Locale LOCALE) {
        properties.put("locale", new SimpleObjectProperty<>(LOCALE));
        return (B)this;
    }

    public final B numberFormat(final NumberFormat FORMAT) {
        properties.put("numberFormat", new SimpleObjectProperty<>(FORMAT));
        return (B)this;
    }

    public final B shadowsEnabled(final boolean ENABLED) {
        properties.put("shadowsEnabled", new SimpleBooleanProperty(ENABLED));
        return (B)this;
    }

    public final B styleClass(final String... STYLES) {
        properties.put("styleClass", new SimpleObjectProperty<>(STYLES));
        return (B)this;
    }

    public final B sections(final Section... SECTIONS) {
        properties.put("sectionsArray", new SimpleObjectProperty<>(SECTIONS));
        return (B)this;
    }

    public final B sections(final List<Section> SECTIONS) {
        properties.put("sectionsList", new SimpleObjectProperty<>(SECTIONS));
        return (B)this;
    }

    public final B series(final Series<String, Number>... SERIES) {
        properties.put("seriesArray", new SimpleObjectProperty(SERIES));
        return (B)this;
    }

    public final B series(final List<Series<String, Number>> SERIES) {
        properties.put("seriesList", new SimpleObjectProperty(SERIES));
        return (B)this;
    }

    public final B tilesFxSeries(final TilesFXSeries<String, Number>... SERIES) {
        properties.put("tilesFxSeriesArray", new SimpleObjectProperty(SERIES));
        return (B)this;
    }

    public final B tilesFxSeries(final List<TilesFXSeries<String, Number>> SERIES) {
        properties.put("tilesFxSeriesList", new SimpleObjectProperty(SERIES));
        return (B)this;
    }

    public final B chartType(final ChartType TYPE) {
        properties.put("chartType", new SimpleObjectProperty(TYPE));
        return (B)this;
    }

    public final B tooltipTimeout(final double TIMEOUT) {
        properties.put("tooltipTimeout", new SimpleDoubleProperty(TIMEOUT));
        return (B)this;
    }

    public final B barChartItems(final BarChartItem... ITEMS) {
        properties.put("barChartItemsArray", new SimpleObjectProperty<>(ITEMS));
        return (B)this;
    }

    public final B barChartItems(final List<BarChartItem> ITEMS) {
        properties.put("barChartItemsList", new SimpleObjectProperty<>(ITEMS));
        return (B)this;
    }

    public final B leaderBoardItems(final LeaderBoardItem... ITEMS) {
        properties.put("leaderBoardItemsArray", new SimpleObjectProperty<>(ITEMS));
        return (B)this;
    }

    public final B leaderBoardItems(final List<LeaderBoardItem> ITEMS) {
        properties.put("leaderBoardItemsList", new SimpleObjectProperty<>(ITEMS));
        return (B)this;
    }

    public final B chartData(final ChartData... DATA) {
        properties.put("chartDataArray", new SimpleObjectProperty(DATA));
        return (B)this;
    }

    public final B chartData(final List<ChartData> DATA) {
        properties.put("chartDataList", new SimpleObjectProperty(DATA));
        return (B)this;
    }

    public final B characters(final String... CHARACTERS) {
        properties.put("characterArray", new SimpleObjectProperty<>(CHARACTERS));
        return (B)this;
    }

    public final B flipTimeInMS(final long TIME) {
        properties.put("flipTimeInMS", new SimpleLongProperty(TIME));
        return (B)this;
    }

    public final B flipText(final String TEXT) {
        properties.put("flipText", new SimpleStringProperty(TEXT));
        return (B)this;
    }

    public final B itemSorting(final ItemSorting ITEM_SORTING) {
        properties.put("itemSorting", new SimpleObjectProperty<>(ITEM_SORTING));
        return (B)this;
    }

    public final B itemSortingTopic(final ItemSortingTopic ITEM_SORTING_TOPIC) {
        properties.put("itemSortingTopic", new SimpleObjectProperty<>(ITEM_SORTING_TOPIC));
        return (B)this;
    }

    public final B autoItemTextColor(final boolean AUTO) {
        properties.put("autoItemTextColor", new SimpleBooleanProperty(AUTO));
        return (B)this;
    }

    public final B autoItemDarkTextColor(final Color COLOR) {
        properties.put("autoItemDarkTextColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B autoItemBrightTextColor(final Color COLOR) {
        properties.put("autoItemBrightTextColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B titleColor(final Color COLOR) {
        properties.put("titleColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B descriptionColor(final Color COLOR) {
        properties.put("descriptionColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public B unitColor(final Color COLOR) {
        properties.put("unitColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public B valueColor(final Color COLOR) {
        properties.put("valueColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public B thresholdColor(final Color COLOR) {
        properties.put("thresholdColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public B lowerThresholdColor(final Color COLOR) {
        properties.put("lowerThresholdColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B checkSectionsForValue(final boolean CHECK) {
        properties.put("checkSectionsForValue", new SimpleBooleanProperty(CHECK));
        return (B)this;
    }

    public final B checkThreshold(final boolean CHECK) {
        properties.put("checkThreshold", new SimpleBooleanProperty(CHECK));
        return (B)this;
    }

    public final B checkLowerThreshold(final boolean CHECK) {
        properties.put("checkLowerThreshold", new SimpleBooleanProperty(CHECK));
        return (B)this;
    }

    public final B innerShadowEnabled(final boolean ENABLED) {
        properties.put("innerShadowEnabled", new SimpleBooleanProperty(ENABLED));
        return (B)this;
    }

    public final B thresholdVisible(final boolean VISIBLE) {
        properties.put("thresholdVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B lowerThresholdVisible(final boolean VISIBLE) {
        properties.put("lowerThresholdVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B averageVisible(final boolean VISIBLE) {
        properties.put("averageVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B sectionsVisible(final boolean VISIBLE) {
        properties.put("sectionsVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B sectionsAlwaysVisible(final boolean VISIBLE) {
        properties.put("sectionsAlwaysVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B sectionTextVisible(final boolean VISIBLE) {
        properties.put("sectionTextVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B sectionIconsVisible(final boolean VISIBLE) {
        properties.put("sectionIconsVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B highlightSections(final boolean HIGHLIGHT) {
        properties.put("highlightSections", new SimpleBooleanProperty(HIGHLIGHT));
        return (B)this;
    }

    public final B orientation(final Orientation ORIENTATION) {
        properties.put("orientation", new SimpleObjectProperty<>(ORIENTATION));
        return (B)this;
    }

    public final B customFontEnabled(final boolean ENABLED) {
        properties.put("customFontEnabled", new SimpleBooleanProperty(ENABLED));
        return (B)this;
    }

    public final B customFont(final Font FONT) {
        properties.put("customFont", new SimpleObjectProperty(FONT));
        return (B)this;
    }

    public final B customDecimalFormatEnabled(final boolean ENABLED) {
        properties.put("customDecimalFormatEnabled", new SimpleBooleanProperty(ENABLED));
        return (B)this;
    }

    public final B customDecimalFormat(final DecimalFormat DECIMAL_FORMAT) {
        properties.put("customDecimalFormat", new SimpleObjectProperty<>(DECIMAL_FORMAT));
        return (B)this;
    }

    public final B alertMessage(final String MESSAGE) {
        properties.put("alertMessage", new SimpleStringProperty(MESSAGE));
        return (B)this;
    }

    public final B smoothing(final boolean SMOOTHING) {
        properties.put("smoothing", new SimpleBooleanProperty(SMOOTHING));
        return (B)this;
    }

    public final B onValueChanged(final InvalidationListener LISTENER) {
        properties.put("onValueChanged", new SimpleObjectProperty<>(LISTENER));
        return (B)this;
    }

    public final B onThresholdExceeded(final EvtObserver<TileEvt> HANDLER) {
        properties.put("onThresholdExceeded", new SimpleObjectProperty<>(HANDLER));
        return (B)this;
    }

    public final B onThresholdUnderrun(final EvtObserver<TileEvt> HANDLER) {
        properties.put("onThresholdUnderrun", new SimpleObjectProperty<>(HANDLER));
        return (B)this;
    }

    public final B time(final ZonedDateTime TIME) {
        properties.put("time", new SimpleObjectProperty<>(TIME));
        return (B)this;
    }

    public final B text(final String TEXT) {
        properties.put("text", new SimpleStringProperty(TEXT));
        return (B)this;
    }

    public final B textAlignment(final TextAlignment ALIGNMENT) {
        properties.put("textAlignment", new SimpleObjectProperty(ALIGNMENT));
        return (B)this;
    }

    public final B timeSections(final TimeSection... SECTIONS) {
        properties.put("timeSectionsArray", new SimpleObjectProperty<>(SECTIONS));
        return (B)this;
    }

    public final B timeSections(final List<TimeSection> SECTIONS) {
        properties.put("timeSectionsList", new SimpleObjectProperty<>(SECTIONS));
        return (B)this;
    }

    public final B discreteSeconds(final boolean DISCRETE) {
        properties.put("discreteSeconds", new SimpleBooleanProperty(DISCRETE));
        return (B)this;
    }

    public final B discreteMinutes(final boolean DISCRETE) {
        properties.put("discreteMinutes", new SimpleBooleanProperty(DISCRETE));
        return (B)this;
    }

    public final B discreteHours(final boolean DISCRETE) {
        properties.put("discreteHours", new SimpleBooleanProperty(DISCRETE));
        return (B)this;
    }

    public final B secondsVisible(final boolean VISIBLE) {
        properties.put("secondsVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B textVisible(final boolean VISIBLE) {
        properties.put("textVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B dateVisible(final boolean VISIBLE) {
        properties.put("dateVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B percentageVisible(final boolean VISIBLE) {
        properties.put("percentageVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B running(final boolean RUNNING) {
        properties.put("running", new SimpleBooleanProperty(RUNNING));
        return (B)this;
    }

    public final B textColor(final Color COLOR) {
        properties.put("textColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B dateColor(final Color COLOR) {
        properties.put("dateColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B hourTickMarkColor(final Color COLOR) {
        properties.put("hourTickMarkColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B minuteTickMarkColor(final Color COLOR) {
        properties.put("minuteTickMarkColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B alarmColor(final Color COLOR) {
        properties.put("alarmColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B tickLabelColor(final Color COLOR) {
        properties.put("tickLabelColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B tickMarkColor(final Color COLOR) {
        properties.put("tickMarkColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B hourTickMarksVisible(final boolean VISIBLE) {
        properties.put("hourTickMarksVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B minuteTickMarksVisible(final boolean VISIBLE) {
        properties.put("minuteTickMarksVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B hourColor(final Color COLOR) {
        properties.put("hourColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B minuteColor(final Color COLOR) {
        properties.put("minuteColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B secondColor(final Color COLOR) {
        properties.put("secondColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B alarmsEnabled(final boolean ENABLED) {
        properties.put("alarmsEnabled", new SimpleBooleanProperty(ENABLED));
        return (B)this;
    }

    public final B alarmsVisible(final boolean VISIBLE) {
        properties.put("alarmsVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B tooltipText(final String TEXT) {
        properties.put("tooltipText", new SimpleStringProperty(TEXT));
        return (B)this;
    }

    public final B alarms(final Alarm... ALARMS) {
        properties.put("alarmsArray", new SimpleObjectProperty<>(ALARMS));
        return (B)this;
    }

    public final B alarms(final List<Alarm> ALARMS) {
        properties.put("alarmsList", new SimpleObjectProperty<>(ALARMS));
        return (B)this;
    }

    public final B onAlarm(final EvtObserver<AlarmEvt> LISTENER) {
        properties.put("onAlarm", new SimpleObjectProperty<>(LISTENER));
        return (B)this;
    }

    public final B onTimeEvent(final EvtObserver<TimeEvt> LISTENER) {
        properties.put("onTimeEvent", new SimpleObjectProperty<>(LISTENER));
        return (B)this;
    }

    public final B onTileEvent(final EvtObserver<TileEvt> LISTENER) {
        properties.put("onTileEvent", new SimpleObjectProperty(LISTENER));
        return (B)this;
    }

    public final B increment(final double INCREMENT) {
        properties.put("increment", new SimpleDoubleProperty(INCREMENT));
        return (B)this;
    }

    public final B image(final Image IMAGE) {
        properties.put("image", new SimpleObjectProperty(IMAGE));
        return (B)this;
    }

    public final B imageMask(final ImageMask MASK) {
        properties.put("imageMask", new SimpleObjectProperty(MASK));
        return (B)this;
    }

    public final B graphic(final Node GRAPHIC) {
        properties.put("graphic", new SimpleObjectProperty(GRAPHIC));
        return (B)this;
    }

    public final B svgPath(final SVGPath SVG_PATH) {
        properties.put("svgPath", new SimpleObjectProperty(SVG_PATH));
        return (B)this;
    }

    public final B currentLocation(final Location LOCATION) {
        properties.put("currentLocation", new SimpleObjectProperty(LOCATION));
        return (B)this;
    }

    public final B pointsOfInterest(final Location... LOCATIONS) {
        properties.put("poiArray", new SimpleObjectProperty(LOCATIONS));
        return (B)this;
    }
    public final B pointsOfInterest(final List<Location> LOCATIONS) {
        properties.put("poiList", new SimpleObjectProperty(LOCATIONS));
        return (B)this;
    }

    public final B track(final Location... LOCATIONS) {
        properties.put("trackArray", new SimpleObjectProperty(LOCATIONS));
        return (B)this;
    }
    public final B track(final List<Location> LOCATIONS) {
        properties.put("trackList", new SimpleObjectProperty(LOCATIONS));
        return (B)this;
    }

    public final B trackColor(final TileColor COLOR) {
        properties.put("trackColor", new SimpleObjectProperty(COLOR));
        return (B)this;
    }

    public final B mapProvider(final MapProvider PROVIDER) {
        properties.put("mapProvider", new SimpleObjectProperty(PROVIDER));
        return (B)this;
    }

    public final B gradientStops(final Stop... STOPS) {
        properties.put("gradientStopsArray", new SimpleObjectProperty(STOPS));
        return (B)this;
    }
    public final B gradientStops(final List<Stop> STOPS) {
        properties.put("gradientStopsList", new SimpleObjectProperty(STOPS));
        return (B)this;
    }

    public final B strokeWithGradient(final boolean STROKE_WITH_GRADIENT) {
        properties.put("strokeWithGradient", new SimpleBooleanProperty(STROKE_WITH_GRADIENT));
        return (B)this;
    }

    public final B fillWithGradient(final boolean FILL_WITH_GRADIENT) {
        properties.put("fillWithGradient", new SimpleBooleanProperty(FILL_WITH_GRADIENT));
        return (B)this;
    }

    public final B roundedCorners(final boolean ROUNDED) {
        properties.put("roundedCorners", new SimpleBooleanProperty(ROUNDED));
        return (B)this;
    }

    public final B textSize(final TextSize SIZE) {
        properties.put("textSize", new SimpleObjectProperty(SIZE));
        return (B)this;
    }

    public final B xAxis(final Axis AXIS) {
        properties.put("xAxis", new SimpleObjectProperty(AXIS));
        return (B)this;
    }

    public final B yAxis(final Axis AXIS) {
        properties.put("yAxis", new SimpleObjectProperty(AXIS));
        return (B)this;
    }

    public final B radarChartMode(final RadarChartMode RadarChartMODE) {
       properties.put("radarChartMode", new SimpleObjectProperty(RadarChartMODE));
       return (B)this;
    }

    public final B chartGridColor(final Color COLOR) {
        properties.put("chartGridColor", new SimpleObjectProperty(COLOR));
        return (B)this;
    }

    public final B country(final Country COUNTRY) {
        properties.put("country", new SimpleObjectProperty(COUNTRY));
        return (B)this;
    }

    public final B countryGroup(final BusinessRegion COUNTRY_GROUP) {
        properties.put("countryGroup", new SimpleObjectProperty(COUNTRY_GROUP));
        return (B)this;
    }

    public final B dataPointsVisible(final boolean VISIBLE) {
        properties.put("dataPointsVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B sunburstTree(final TreeNode TREE) {
        properties.put("sunburstTree", new SimpleObjectProperty(TREE));
        return (B)this;
    }

    public final B sunburstBackgroundColor(final Color COLOR) {
        properties.put("sunburstBackgroundColor", new SimpleObjectProperty(COLOR));
        return (B)this;
    }

    public final B sunburstTextColor(final Color COLOR) {
        properties.put("sunburstTextColor", new SimpleObjectProperty(COLOR));
        return (B)this;
    }

    public final B sunburstUseColorFromParent(final boolean USE) {
        properties.put("sunburstUseColorFromParent", new SimpleBooleanProperty(USE));
        return (B)this;
    }

    public final B sunburstTextOrientation(final TextOrientation ORIENTATION) {
        properties.put("sunburstTextOrientation", new SimpleObjectProperty(ORIENTATION));
        return (B)this;
    }

    public final B sunburstVisibleData(final VisibleData VISIBLE_DATA) {
        properties.put("sunburstVisibleData", new SimpleObjectProperty(VISIBLE_DATA));
        return (B)this;
    }

    public final B sunburstInteractive(final boolean INTERACTIVE) {
        properties.put("sunburstInteractive", new SimpleBooleanProperty(INTERACTIVE));
        return (B)this;
    }

    public final B sunburstAutoTextColor(final boolean AUTOMATIC) {
        properties.put("sunburstAutoTextColor", new SimpleBooleanProperty(AUTOMATIC));
        return (B)this;
    }

    public final B sunburstUseChartDataTextColor(final boolean USE) {
        properties.put("sunburstUseChartDataTextColor", new SimpleBooleanProperty(USE));
        return (B)this;
    }

    public final B snapToTicks(final boolean SNAP) {
        properties.put("snapToTicks", new SimpleBooleanProperty(SNAP));
        return (B)this;
    }

    public final B minorTickCount(final int COUNT) {
        properties.put("minorTickCount", new SimpleIntegerProperty(COUNT));
        return (B)this;
    }

    public final B majorTickUnit(final double UNIT) {
        properties.put("majorTickUnit", new SimpleDoubleProperty(UNIT));
        return (B)this;
    }

    public final B matrixSize(final int COLS, final int ROWS) {
        properties.put("matrixSize", null);
        properties.put("matrixColumns", new SimpleIntegerProperty(COLS));
        properties.put("matrixRows", new SimpleIntegerProperty(ROWS));
        return (B)this;
    }

    public final B matrixIcons(final MatrixIcon... MATRIX_ICONS) {
        properties.put("matrixIconsArray", new SimpleObjectProperty(MATRIX_ICONS));
        return (B)this;
    }
    public final B matrixIcons(final List<MatrixIcon> MATRIX_ICONS) {
        properties.put("matrixIconsList", new SimpleObjectProperty(MATRIX_ICONS));
        return (B)this;
    }

    public final B notifyRegionBackgroundColor(final Color COLOR) {
        properties.put("notifyRegionBackgroundColor", new SimpleObjectProperty(COLOR));
        return (B)this;
    }

    public final B notifyRegionForegroundColor(final Color COLOR) {
        properties.put("notifyRegionForegroundColor", new SimpleObjectProperty(COLOR));
        return (B)this;
    }

    public final B notifyRegionTooltipText(final String TEXT) {
        properties.put("notifyRegionTooltipText", new SimpleStringProperty(TEXT));
        return (B)this;
    }    
    
    public final B showNotifyRegion(final boolean SHOW) {
        properties.put("showNotifyRegion", new SimpleBooleanProperty(SHOW));
        return (B)this;
    }

    public final B infoRegionBackgroundColor(final Color COLOR) {
        properties.put("infoRegionBackgroundColor", new SimpleObjectProperty(COLOR));
        return (B)this;
    }

    public final B infoRegionForegroundColor(final Color COLOR) {
        properties.put("infoRegionForegroundColor", new SimpleObjectProperty(COLOR));
        return (B)this;
    }

    public final B infoRegionTooltipText(final String TEXT) {
        properties.put("infoRegionTooltipText", new SimpleStringProperty(TEXT));
        return (B)this;
    }

    public final B showInfoRegion(final boolean SHOW) {
        properties.put("showInfoRegion", new SimpleBooleanProperty(SHOW));
        return (B)this;
    }

    public final B lowerRightRegionBackgroundColor(final Color COLOR) {
        properties.put("lowerRightRegionBackgroundColor", new SimpleObjectProperty(COLOR));
        return (B)this;
    }

    public final B lowerRightRegionForegroundColor(final Color COLOR) {
        properties.put("lowerRightRegionForegroundColor", new SimpleObjectProperty(COLOR));
        return (B)this;
    }

    public final B lowerRightRegionTooltipText(final String TEXT) {
        properties.put("lowerRightRegionTooltipText", new SimpleStringProperty(TEXT));
        return (B)this;
    }

    public final B showLowerRightRegion(final boolean SHOW) {
        properties.put("showLowerRightRegion", new SimpleBooleanProperty(SHOW));
        return (B)this;
    }


    public final B leftText(final String TEXT) {
        properties.put("leftText", new SimpleStringProperty(TEXT));
        return (B)this;
    }

    public final B middleText(final String TEXT) {
        properties.put("middleText", new SimpleStringProperty(TEXT));
        return (B)this;
    }

    public final B rightText(final String TEXT) {
        properties.put("rightText", new SimpleStringProperty(TEXT));
        return (B)this;
    }

    public final B leftValue(final double VALUE) {
        properties.put("leftValue", new SimpleDoubleProperty(VALUE));
        return (B)this;
    }

    public final B middleValue(final double VALUE) {
        properties.put("middleValue", new SimpleDoubleProperty(VALUE));
        return (B)this;
    }

    public final B rightValue(final double VALUE) {
        properties.put("rightValue", new SimpleDoubleProperty(VALUE));
        return (B)this;
    }

    public final B leftGraphics(final Node NODE) {
        properties.put("leftGraphics", new SimpleObjectProperty(NODE));
        return (B)this;
    }

    public final B middleGraphics(final Node NODE) {
        properties.put("middleGraphics", new SimpleObjectProperty(NODE));
        return (B)this;
    }

    public final B rightGraphics(final Node NODE) {
        properties.put("rightGraphics", new SimpleObjectProperty(NODE));
        return (B)this;
    }

    public final B trendVisible(final boolean VISIBLE) {
        properties.put("trendVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B timeoutMs(final long TIMEOUT_MS) {
        properties.put("timeoutMs", new SimpleLongProperty(TIMEOUT_MS));
        return (B)this;
    }

    public final B rank(final Rank RANK) {
        properties.put("rank", new SimpleObjectProperty<>(RANK));
        return (B)this;
    }

    public final B interactive(final boolean INTERACTIVE) {
        properties.put("interactive", new SimpleBooleanProperty(INTERACTIVE));
        return (B)this;
    }

    public final B numberOfValuesForTrendCalculation(final int NUMBER) {
        properties.put("numberOfValuesForTrendCalculation", new SimpleIntegerProperty(NUMBER));
        return (B)this;
    }

    public final B backgroundImage(final Image IMAGE) {
        properties.put("backgroundImage", new SimpleObjectProperty<>(IMAGE));
        return (B)this;
    }

    public final B backgroundImageOpacity(final double OPACITY) {
        properties.put("backgroundImageOpacity", new SimpleDoubleProperty(OPACITY));
        return (B)this;
    }

    public final B backgroundImageKeepAspect(final boolean KEEP_ASPECT) {
        properties.put("backgroundImageKeepAspect", new SimpleBooleanProperty(KEEP_ASPECT));
        return (B)this;
    }

    public final B infoRegionEventHandler(final EventHandler<MouseEvent> HANDLER) {
        properties.put("infoRegionEventHandler", new SimpleObjectProperty(HANDLER));
        return (B)this;
    }

    public final B prefSize(final double WIDTH, final double HEIGHT) {
        properties.put("prefSize", new SimpleObjectProperty<>(new Dimension2D(WIDTH, HEIGHT)));
        return (B)this;
    }
    public final B minSize(final double WIDTH, final double HEIGHT) {
        properties.put("minSize", new SimpleObjectProperty<>(new Dimension2D(WIDTH, HEIGHT)));
        return (B)this;
    }
    public final B maxSize(final double WIDTH, final double HEIGHT) {
        properties.put("maxSize", new SimpleObjectProperty<>(new Dimension2D(WIDTH, HEIGHT)));
        return (B)this;
    }

    public final B prefWidth(final double PREF_WIDTH) {
        properties.put("prefWidth", new SimpleDoubleProperty(PREF_WIDTH));
        return (B)this;
    }
    public final B prefHeight(final double PREF_HEIGHT) {
        properties.put("prefHeight", new SimpleDoubleProperty(PREF_HEIGHT));
        return (B)this;
    }

    public final B minWidth(final double MIN_WIDTH) {
        properties.put("minWidth", new SimpleDoubleProperty(MIN_WIDTH));
        return (B)this;
    }
    public final B minHeight(final double MIN_HEIGHT) {
        properties.put("minHeight", new SimpleDoubleProperty(MIN_HEIGHT));
        return (B)this;
    }

    public final B maxWidth(final double MAX_WIDTH) {
        properties.put("maxWidth", new SimpleDoubleProperty(MAX_WIDTH));
        return (B)this;
    }
    public final B maxHeight(final double MAX_HEIGHT) {
        properties.put("maxHeight", new SimpleDoubleProperty(MAX_HEIGHT));
        return (B)this;
    }

    public final B scaleX(final double SCALE_X) {
        properties.put("scaleX", new SimpleDoubleProperty(SCALE_X));
        return (B)this;
    }
    public final B scaleY(final double SCALE_Y) {
        properties.put("scaleY", new SimpleDoubleProperty(SCALE_Y));
        return (B)this;
    }

    public final B layoutX(final double LAYOUT_X) {
        properties.put("layoutX", new SimpleDoubleProperty(LAYOUT_X));
        return (B)this;
    }
    public final B layoutY(final double LAYOUT_Y) {
        properties.put("layoutY", new SimpleDoubleProperty(LAYOUT_Y));
        return (B)this;
    }

    public final B translateX(final double TRANSLATE_X) {
        properties.put("translateX", new SimpleDoubleProperty(TRANSLATE_X));
        return (B)this;
    }
    public final B translateY(final double TRANSLATE_Y) {
        properties.put("translateY", new SimpleDoubleProperty(TRANSLATE_Y));
        return (B)this;
    }

    public final B padding(final Insets INSETS) {
        properties.put("padding", new SimpleObjectProperty<>(INSETS));
        return (B)this;
    }


    public final Tile build() {
        final Tile TILE;
        if (properties.containsKey("skinType")) {
            SkinType skinType = ((ObjectProperty<SkinType>) properties.get("skinType")).get();
            TILE = new Tile(skinType);
            switch (skinType) {
                case SMOOTHED_CHART:
                    break;
                case BAR_CHART:
                    TILE.setItemSorting(ItemSorting.DESCENDING);
                    TILE.setAnimated(false);
                    break;
                case CLOCK:
                    break;
                case GAUGE:
                    TILE.setAnimated(true);
                    TILE.setTickLabelDecimals(0);
                    TILE.setBarColor(Tile.FOREGROUND);
                    TILE.setThresholdColor(Tile.BLUE);
                    TILE.setThresholdVisible(true);
                    break;
                case GAUGE2:
                    TILE.setAngleRange(240);
                    TILE.setStartAngle(330);
                    TILE.setAnimated(true);
                    TILE.setTickLabelDecimals(0);
                    TILE.setBarColor(Tile.BLUE);
                    TILE.setBarBackgroundColor(Tile.BACKGROUND.brighter());
                    break;
                case HIGH_LOW:
                    TILE.setMaxValue(Double.MAX_VALUE);
                    TILE.setDecimals(2);
                    TILE.setTickLabelDecimals(1);
                    break;
                case PERCENTAGE:
                    TILE.setAnimated(true);
                    TILE.setThresholdColor(Tile.GRAY);
                    TILE.setTickLabelDecimals(0);
                    break;
                case PLUS_MINUS:
                    break;
                case SLIDER:
                    TILE.setBarBackgroundColor(Tile.FOREGROUND);
                    break;
                case SPARK_LINE:
                    TILE.setTextVisible(false);
                    TILE.setAnimated(false);
                    TILE.setAveragingEnabled(true);
                    TILE.setAveragingPeriod(10);
                    TILE.setDecimals(0);
                    TILE.setTickLabelDecimals(0);
                    break;
                case SWITCH:
                    break;
                case WORLDMAP:
                    TILE.setPrefSize(380, 250);
                    TILE.setDataPointsVisible(true);
                    break;
                case TIMER_CONTROL:
                    TILE.setSectionsVisible(true);
                    TILE.setHighlightSections(true);
                    TILE.setCheckSectionsForValue(true);
                    break;
                case NUMBER:
                    break;
                case TEXT:
                    break;
                case TIME:
                    break;
                case CUSTOM:
                    break;
                case CUSTOM_SCROLLABLE:
                    break;
                case LEADER_BOARD:
                    TILE.setItemSorting(ItemSorting.DESCENDING);
                    break;
                case MAP:
                    break;
                case RADIAL_CHART:
                    TILE.setAnimated(true);
                    break;
                case DONUT_CHART:
                    TILE.setItemSorting(ItemSorting.DESCENDING);
                    TILE.setAnimated(true);
                    break;
                case CIRCULAR_PROGRESS:
                    TILE.setBarBackgroundColor(TILE.getBackgroundColor().brighter());
                    TILE.setAnimated(true);
                    break;
                case STOCK:
                    TILE.setAnimated(false);
                    TILE.setAveragingPeriod(720);
                    TILE.setAveragingEnabled(true);
                    TILE.setDecimals(2);
                    TILE.setTickLabelDecimals(2);
                    TILE.setThresholdColor(Tile.GRAY);
                    TILE.setTextVisible(false);
                    break;
                case GAUGE_SPARK_LINE:
                    TILE.setBarBackgroundColor(Tile.BACKGROUND.brighter());
                    TILE.setBarColor(Tile.BLUE);
                    TILE.setAngleRange(270);
                    break;
                case SMOOTH_AREA_CHART:
                    TILE.setSmoothing(true);
                    TILE.setChartType(ChartType.AREA);
                    break;
                case RADAR_CHART:
                    break;
                case RADAR_NODE_CHART:
                    break;
                case COUNTRY:
                    break;
                case CHARACTER:
                    break;
                case FLIP:
                    break;
                case SWITCH_SLIDER:
                    TILE.setBarBackgroundColor(Tile.FOREGROUND);
                    break;
                case DATE:
                    TILE.setTitleAlignment(TextAlignment.CENTER);
                    TILE.setTextAlignment(TextAlignment.CENTER);
                    break;
                case CALENDAR:
                    TILE.setTitleAlignment(TextAlignment.CENTER);
                    TILE.setTextAlignment(TextAlignment.CENTER);
                    break;
                case SUNBURST:
                    break;
                case MATRIX:
                    break;
                case MATRIX_ICON:
                    break;
                case RADIAL_PERCENTAGE:
                    TILE.setBarBackgroundColor(TILE.getBackgroundColor().brighter());
                    TILE.setAnimated(true);
                    break;
                case STATUS:
                    TILE.setDescriptionAlignment(Pos.TOP_CENTER);
                    break;
                case BAR_GAUGE:
                    TILE.setBarBackgroundColor(Tile.BACKGROUND.brighter());
                    TILE.setBarColor(Tile.BLUE);
                    TILE.setAngleRange(180);
                    TILE.setTickLabelDecimals(0);
                    break;
                case IMAGE:
                    TILE.setTextAlignment(TextAlignment.CENTER);
                    break;
                case IMAGE_COUNTER:
                    TILE.setTextAlignment(TextAlignment.LEFT);
                    TILE.setDecimals(0);
                    break;
                case TIMELINE:
                    TILE.setDataPointsVisible(true);
                    TILE.setTextVisible(false);
                    TILE.setAnimated(false);
                    TILE.setAveragingEnabled(true);
                    TILE.setAveragingPeriod(Helper.calcNumberOfDatapointsForPeriod(TILE.getTimePeriod(), TILE.getTimePeriodResolution()));
                    TILE.setDecimals(0);
                    TILE.setTickLabelDecimals(0);
                    break;
                case CLUSTER_MONITOR:
                    TILE.setTitle("");
                    TILE.setTextVisible(false);
                    TILE.setUnit(Helper.PERCENTAGE);
                    TILE.setAnimated(false);
                    TILE.setDecimals(0);
                    TILE.setBarColor(Tile.BLUE);
                    break;
                case LED:
                    TILE.setActiveColor(Bright.GREEN);
                    break;
                case COUNTDOWN_TIMER:
                    TILE.setBarBackgroundColor(TILE.getBackgroundColor().brighter());
                    TILE.setAnimated(false);
                    TILE.setTimePeriod(java.time.Duration.ofSeconds(60));
                    break;
                case CYCLE_STEP:
                    break;
                case COLOR:
                    TILE.setForegroundColor(Color.WHITE);
                    TILE.setUnit("\u0025");
                    TILE.setDecimals(0);
                    TILE.setBarBackgroundColor(Tile.BACKGROUND);
                    break;
                case FLUID:
                    break;
                case FIRE_SMOKE:
                    break;
                case TURNOVER:
                    TILE.setTextAlignment(TextAlignment.CENTER);
                    TILE.setImageMask(ImageMask.ROUND);
                    break;
                case RADIAL_DISTRIBUTION:
                    TILE.setStartAngle(330);
                    TILE.setAngleRange(240);
                    TILE.setAnimated(false);
                    TILE.setTickLabelDecimals(0);
                    TILE.setBarBackgroundColor(Tile.BACKGROUND.brighter());
                    break;
                case SPINNER:
                    break;
                case CENTER_TEXT:
                    TILE.setDescriptionAlignment(Pos.CENTER);
                    break;
                default:
                    break;
            }
        } else {
            TILE = new Tile();
        }

        // Make sure that sections, areas and markers will be added first
        if (properties.keySet().contains("sectionsArray")) {
            TILE.setSections(((ObjectProperty<Section[]>) properties.get("sectionsArray")).get());
        }
        if(properties.keySet().contains("sectionsList")) {
            TILE.setSections(((ObjectProperty<List<Section>>) properties.get("sectionsList")).get());
        }

        if (properties.keySet().contains("characterArray")) {
            TILE.setCharacters(((ObjectProperty<String[]>) properties.get("characterArray")).get());
        }

        if(properties.keySet().contains("foregroundBaseColor")) {
            TILE.setForegroundBaseColor(((ObjectProperty<Color>) properties.get("foregroundBaseColor")).get());
        }

        if (properties.keySet().contains("maxValue")) {
            TILE.setMaxValue(((DoubleProperty) properties.get("maxValue")).get());
        }
        if (properties.keySet().contains("minValue")) {
            TILE.setMinValue(((DoubleProperty) properties.get("minValue")).get());
        }

        if (properties.keySet().contains("alarmsArray")) {
            TILE.setAlarms(((ObjectProperty<Alarm[]>) properties.get("alarmsArray")).get());
        }
        if(properties.keySet().contains("alarmsList")) {
            TILE.setAlarms(((ObjectProperty<List<Alarm>>) properties.get("alarmsList")).get());
        }

        if (properties.keySet().contains("timeSectionsArray")) {
            TILE.setTimeSections(((ObjectProperty<TimeSection[]>) properties.get("timeSectionsArray")).get());
        }
        if(properties.keySet().contains("timeSectionsList")) {
            TILE.setTimeSections(((ObjectProperty<List<TimeSection>>) properties.get("timeSectionsList")).get());
        }

        if (properties.keySet().contains("seriesArray")) {
            TILE.setSeries(((ObjectProperty<Series<String, Number>[]>) properties.get("seriesArray")).get());
        }

        if (properties.keySet().contains("seriesList")) {
            TILE.setSeries(((ObjectProperty<List<Series<String, Number>>>) properties.get("seriesList")).get());
        }

        if (properties.keySet().contains("tilesFxSeriesArray")) {
            TILE.setTilesFXSeries(((ObjectProperty<TilesFXSeries<String, Number>[]>) properties.get("tilesFxSeriesArray")).get());
        }

        if (properties.keySet().contains("tilesFxSeriesList")) {
            TILE.setTilesFXSeries(((ObjectProperty<List<TilesFXSeries<String, Number>>>) properties.get("tilesFxSeriesList")).get());
        }

        if (properties.keySet().contains("barChartItemsArray")) {
            BarChartItem[] items = ((ObjectProperty<BarChartItem[]>) properties.get("barChartItemsArray")).get();
            for (BarChartItem item : items) {
                item.getChartData().setAnimated(TILE.isAnimated());
            }
            TILE.setBarChartItems(items);
        }
        if(properties.keySet().contains("barChartItemsList")) {
            List<BarChartItem> items = ((ObjectProperty<List<BarChartItem>>) properties.get("barChartItemsList")).get();
            for (BarChartItem item : items) {
                item.getChartData().setAnimated(TILE.isAnimated());
            }
            TILE.setBarChartItems(items);
        }

        if (properties.keySet().contains("leaderBoardItemsArray")) {
            TILE.setLeaderBoardItems(((ObjectProperty<LeaderBoardItem[]>) properties.get("leaderBoardItemsArray")).get());
        }
        if(properties.keySet().contains("leaderBoardItemsList")) {
            TILE.setLeaderBoardItems(((ObjectProperty<List<LeaderBoardItem>>) properties.get("leaderBoardItemsList")).get());
        }

        if (properties.keySet().contains("gradientStopsArray")) {
            TILE.setGradientStops(((ObjectProperty<Stop[]>) properties.get("gradientStopsArray")).get());
        }
        if (properties.keySet().contains("gradientStopsList")) {
            TILE.setGradientStops(((ObjectProperty<List<Stop>>) properties.get("gradientStopsList")).get());
        }

        if (properties.keySet().contains("matrixIconsArray")) {
            TILE.setMatrixIcons(((ObjectProperty<MatrixIcon[]>) properties.get("matrixIconsArray")).get());
        }
        if (properties.keySet().contains("matrixIconsList")) {
            TILE.setMatrixIcons(((ObjectProperty<List<MatrixIcon>>) properties.get("matrixIconsList")).get());
        }

        if (properties.keySet().contains("chartDataArray")) {
            TILE.setChartData(((ObjectProperty<ChartData[]>) properties.get("chartDataArray")).get());
        }
        if (properties.keySet().contains("chartDataList")) {
            TILE.setChartData(((ObjectProperty<List<ChartData>>) properties.get("chartDataList")).get());
        }

        if (properties.keySet().contains("poiArray")) {
            TILE.setPoiLocations(((ObjectProperty<Location[]>) properties.get("poiArray")).get());
        }
        if (properties.keySet().contains("poiList")) {
            TILE.setPoiList(((ObjectProperty<List<Location>>) properties.get("poiList")).get());
        }

        if (properties.keySet().contains("trackArray")) {
            TILE.setTrack(((ObjectProperty<Location[]>) properties.get("trackArray")).get());
        }
        if (properties.keySet().contains("trackList")) {
            TILE.setTrack(((ObjectProperty<List<Location>>) properties.get("trackList")).get());
        }

        for (String key : properties.keySet()) {
            switch (key) {
                case "prefSize"                          -> {
                    Dimension2D dim = ((ObjectProperty<Dimension2D>) properties.get(key)).get();
                    TILE.setPrefSize(dim.getWidth(), dim.getHeight());
                }
                case "minSize"                           -> {
                    Dimension2D dim = ((ObjectProperty<Dimension2D>) properties.get(key)).get();
                    TILE.setMinSize(dim.getWidth(), dim.getHeight());
                }
                case "maxSize"                           -> {
                    Dimension2D dim = ((ObjectProperty<Dimension2D>) properties.get(key)).get();
                    TILE.setMaxSize(dim.getWidth(), dim.getHeight());
                }
                case "prefWidth"                         -> TILE.setPrefWidth(((DoubleProperty) properties.get(key)).get());
                case "prefHeight"                        -> TILE.setPrefHeight(((DoubleProperty) properties.get(key)).get());
                case "minWidth"                          -> TILE.setMinWidth(((DoubleProperty) properties.get(key)).get());
                case "minHeight"                         -> TILE.setMinHeight(((DoubleProperty) properties.get(key)).get());
                case "maxWidth"                          -> TILE.setMaxWidth(((DoubleProperty) properties.get(key)).get());
                case "maxHeight"                         -> TILE.setMaxHeight(((DoubleProperty) properties.get(key)).get());
                case "scaleX"                            -> TILE.setScaleX(((DoubleProperty) properties.get(key)).get());
                case "scaleY"                            -> TILE.setScaleY(((DoubleProperty) properties.get(key)).get());
                case "layoutX"                           -> TILE.setLayoutX(((DoubleProperty) properties.get(key)).get());
                case "layoutY"                           -> TILE.setLayoutY(((DoubleProperty) properties.get(key)).get());
                case "translateX"                        -> TILE.setTranslateX(((DoubleProperty) properties.get(key)).get());
                case "translateY"                        -> TILE.setTranslateY(((DoubleProperty) properties.get(key)).get());
                case "padding"                           -> TILE.setPadding(((ObjectProperty<Insets>) properties.get(key)).get());
                case "styleClass"                        -> {
                    TILE.getStyleClass().setAll("tile");
                    TILE.getStyleClass().addAll(((ObjectProperty<String[]>) properties.get(key)).get());
                }
                case "autoScale"                         -> TILE.setAutoScale(((BooleanProperty) properties.get(key)).get());
                case "value"                             -> TILE.setValue(((DoubleProperty) properties.get(key)).get());
                case "decimals"                          -> TILE.setDecimals(((IntegerProperty) properties.get(key)).get());
                case "shortenNumbers"                    -> TILE.setShortenNumbers(((BooleanProperty) properties.get(key)).get());
                case "tickLabelDecimals"                 -> TILE.setTickLabelDecimals(((IntegerProperty) properties.get(key)).get());
                case "tickLabelsXVisible"                -> TILE.setTickLabelsXVisible(((BooleanProperty) properties.get(key)).get());
                case "tickLabelsYVisible"                -> TILE.setTickLabelsYVisible(((BooleanProperty) properties.get(key)).get());
                case "minValueVisible"                   -> TILE.setMinValueVisible(((BooleanProperty) properties.get(key)).get());
                case "maxValueVisible"                   -> TILE.setMaxValueVisible(((BooleanProperty) properties.get(key)).get());
                case "title"                             -> TILE.setTitle(((StringProperty) properties.get(key)).get());
                case "titleAlignment"                    -> TILE.setTitleAlignment(((ObjectProperty<TextAlignment>) properties.get(key)).get());
                case "description"                       -> TILE.setDescription(((StringProperty) properties.get(key)).get());
                case "descriptionAlignment"              -> TILE.setDescriptionAlignment(((ObjectProperty<Pos>) properties.get(key)).get());
                case "unit"                              -> TILE.setUnit(((StringProperty) properties.get(key)).get());
                case "thumbColor"                        -> TILE.setThumbColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "flatUI"                            -> TILE.setFlatUI(((BooleanProperty) properties.get(key)).get());
                case "selected"                          -> TILE.setActive(((BooleanProperty) properties.get(key)).get());
                case "averagingEnabled"                  -> TILE.setAveragingEnabled(((BooleanProperty) properties.get(key)).get());
                case "averagingPeriod"                   -> TILE.setAveragingPeriod(((IntegerProperty) properties.get(key)).get());
                case "timePeriod"                        -> TILE.setTimePeriod(((ObjectProperty<java.time.Duration>) properties.get(key)).get());
                case "maxTimePeriod"                     -> TILE.setMaxTimePeriod(((ObjectProperty<java.time.Duration>) properties.get(key)).get());
                case "timePeriodResolution"              -> TILE.setTimePeriodResolution(((ObjectProperty<TimeUnit>) properties.get(key)).get());
                case "fixedYScale"                       -> TILE.setFixedYScale(((BooleanProperty) properties.get(key)).get());
                case "startFromZero"                     -> TILE.setStartFromZero(((BooleanProperty) properties.get(key)).get());
                case "returnToZero"                      -> TILE.setReturnToZero(((BooleanProperty) properties.get(key)).get());
                case "minMeasuredValueVisible"           -> TILE.setMinMeasuredValueVisible(((BooleanProperty) properties.get(key)).get());
                case "maxMeasuredValueVisible"           -> TILE.setMaxMeasuredValueVisible(((BooleanProperty) properties.get(key)).get());
                case "oldValueVisible"                   -> TILE.setOldValueVisible(((BooleanProperty) properties.get(key)).get());
                case "valueVisible"                      -> TILE.setValueVisible(((BooleanProperty) properties.get(key)).get());
                case "foregroundColor"                   -> TILE.setForegroundColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "backgroundColor"                   -> TILE.setBackgroundColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "borderColor"                       -> TILE.setBorderColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "borderWidth"                       -> TILE.setBorderWidth(((DoubleProperty) properties.get(key)).get());
                case "knobColor"                         -> TILE.setKnobColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "animated"                          -> TILE.setAnimated(((BooleanProperty) properties.get(key)).get());
                case "animationDuration"                 -> TILE.setAnimationDuration(((LongProperty) properties.get(key)).get());
                case "pauseDuration"                     -> TILE.setPauseDuration(((LongProperty) properties.get(key)).get());
                case "startAngle"                        -> TILE.setStartAngle(((DoubleProperty) properties.get(key)).get());
                case "angleRange"                        -> TILE.setAngleRange(((DoubleProperty) properties.get(key)).get());
                case "needleColor"                       -> TILE.setNeedleColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "barColor"                          -> TILE.setBarColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "barBackgroundColor"                -> TILE.setBarBackgroundColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "locale"                            -> TILE.setLocale(((ObjectProperty<Locale>) properties.get(key)).get());
                case "numberFormat"                      -> TILE.setNumberFormat(((ObjectProperty<NumberFormat>) properties.get(key)).get());
                case "shadowsEnabled"                    -> TILE.setShadowsEnabled(((BooleanProperty) properties.get(key)).get());
                case "style"                             -> TILE.setStyle(((StringProperty) properties.get(key)).get());
                case "innerShadowEnabled"                -> TILE.setInnerShadowEnabled(((BooleanProperty) properties.get(key)).get());
                case "thresholdVisible"                  -> TILE.setThresholdVisible(((BooleanProperty) properties.get(key)).get());
                case "lowerThresholdVisible"             -> TILE.setLowerThresholdVisible(((BooleanProperty) properties.get(key)).get());
                case "averageVisible"                    -> TILE.setAverageVisible(((BooleanProperty) properties.get(key)).get());
                case "sectionsVisible"                   -> TILE.setSectionsVisible(((BooleanProperty) properties.get(key)).get());
                case "sectionsAlwaysVisible"             -> TILE.setSectionsAlwaysVisible(((BooleanProperty) properties.get(key)).get());
                case "sectionTextVisible"                -> TILE.setSectionTextVisible(((BooleanProperty) properties.get(key)).get());
                case "sectionIconsVisible"               -> TILE.setSectionIconsVisible(((BooleanProperty) properties.get(key)).get());
                case "highlightSections"                 -> TILE.setHighlightSections(((BooleanProperty) properties.get(key)).get());
                case "titleColor"                        -> TILE.setTitleColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "descriptionColor"                  -> TILE.setDescriptionColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "unitColor"                         -> TILE.setUnitColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "valueColor"                        -> TILE.setValueColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "thresholdColor"                    -> TILE.setThresholdColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "lowerThresholdColor"               -> TILE.setLowerThresholdColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "orientation"                       -> TILE.setOrientation(((ObjectProperty<Orientation>) properties.get(key)).get());
                case "checkSectionsForValue"             -> TILE.setCheckSectionsForValue(((BooleanProperty) properties.get(key)).get());
                case "checkThreshold"                    -> TILE.setCheckThreshold(((BooleanProperty) properties.get(key)).get());
                case "checkLowerThreshold"               -> TILE.setCheckLowerThreshold(((BooleanProperty) properties.get(key)).get());
                case "onValueChanged"                    -> TILE.currentValueProperty().addListener(((ObjectProperty<InvalidationListener>) properties.get(key)).get());
                case "keepAspect"                        -> TILE.setKeepAspect(((BooleanProperty) properties.get(key)).get());
                case "threshold"                         -> TILE.setThreshold(((DoubleProperty) properties.get(key)).get());
                case "lowerThreshold"                    -> TILE.setLowerThreshold(((DoubleProperty) properties.get(key)).get());
                case "referenceValue"                    -> TILE.setReferenceValue(((DoubleProperty) properties.get(key)).get());
                case "autoReferenceValue"                -> TILE.setAutoReferenceValue(((BooleanProperty) properties.get(key)).get());
                case "customFontEnabled"                 -> TILE.setCustomFontEnabled(((BooleanProperty) properties.get(key)).get());
                case "customFont"                        -> TILE.setCustomFont(((ObjectProperty<Font>) properties.get(key)).get());
                case "customDecimalFormatEnabled"        -> TILE.setCustomDecimalFormatEnabled(((BooleanProperty) properties.get(key)).get());
                case "customDecimalFormat"               -> TILE.setCustomDecimalFormat(((ObjectProperty<DecimalFormat>) properties.get(key)).get());
                case "alertMessage"                      -> TILE.setAlertMessage(((StringProperty) properties.get(key)).get());
                case "smoothing"                         -> TILE.setSmoothing(((BooleanProperty) properties.get(key)).get());
                case "time"                              -> TILE.setTime(((ObjectProperty<ZonedDateTime>) properties.get(key)).get());
                case "text"                              -> TILE.setText(((StringProperty) properties.get(key)).get());
                case "textAlignment"                     -> TILE.setTextAlignment(((ObjectProperty<TextAlignment>) properties.get(key)).get());
                case "discreteSeconds"                   -> TILE.setDiscreteSeconds(((BooleanProperty) properties.get(key)).get());
                case "discreteMinutes"                   -> TILE.setDiscreteMinutes(((BooleanProperty) properties.get(key)).get());
                case "discreteHours"                     -> TILE.setDiscreteHours(((BooleanProperty) properties.get(key)).get());
                case "secondsVisible"                    -> TILE.setSecondsVisible(((BooleanProperty) properties.get(key)).get());
                case "textVisible"                       -> TILE.setTextVisible(((BooleanProperty) properties.get(key)).get());
                case "dateVisible"                       -> TILE.setDateVisible(((BooleanProperty) properties.get(key)).get());
                case "percentageVisible"                 -> TILE.setPercentageVisible(((BooleanProperty) properties.get(key)).get());
                case "textColor"                         -> TILE.setTextColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "dateColor"                         -> TILE.setDateColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "hourTickMarkColor"                 -> TILE.setHourTickMarkColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "minuteTickMarkColor"               -> TILE.setMinuteTickMarkColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "alarmColor"                        -> TILE.setAlarmColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "tickLabelColor"                    -> TILE.setTickLabelColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "tickMarkColor"                     -> TILE.setTickMarkColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "hourTickMarksVisible"              -> TILE.setHourTickMarksVisible(((BooleanProperty) properties.get(key)).get());
                case "minuteTickMarksVisible"            -> TILE.setMinuteTickMarksVisible(((BooleanProperty) properties.get(key)).get());
                case "hourColor"                         -> TILE.setHourColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "minuteColor"                       -> TILE.setMinuteColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "secondColor"                       -> TILE.setSecondColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "onAlarm"                           -> TILE.setOnAlarmEvt(((ObjectProperty<EvtObserver<AlarmEvt>>) properties.get(key)).get());
                case "onTimeEvent"                       -> TILE.setOnTimeEvt(((ObjectProperty<EvtObserver<TimeEvt>>) properties.get(key)).get());
                case "onTileEvent"                       -> TILE.addTileObserver(TileEvt.ANY, ((ObjectProperty<EvtObserver<TileEvt>>) properties.get(key)).get());
                case "alarmsEnabled"                     -> TILE.setAlarmsEnabled(((BooleanProperty) properties.get(key)).get());
                case "alarmsVisible"                     -> TILE.setAlarmsVisible(((BooleanProperty) properties.get(key)).get());
                case "running"                           -> TILE.setRunning(((BooleanProperty) properties.get(key)).get());
                case "increment"                         -> TILE.setIncrement(((DoubleProperty) properties.get(key)).get());
                case "activeColor"                       -> TILE.setActiveColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "duration"                          -> TILE.setDuration(((ObjectProperty<LocalTime>) properties.get(key)).get());
                case "strokeWithGradient"                -> TILE.setStrokeWithGradient(((BooleanProperty) properties.get(key)).get());
                case "fillWithGradient"                  -> TILE.setFillWithGradient(((BooleanProperty) properties.get(key)).get());
                case "image"                             -> TILE.setImage(((ObjectProperty<Image>) properties.get(key)).get());
                case "imageMask"                         -> TILE.setImageMask(((ObjectProperty<ImageMask>) properties.get(key)).get());
                case "graphic"                           -> TILE.setGraphic(((ObjectProperty<Node>) properties.get(key)).get());
                case "svgPath"                           -> TILE.setSVGPath(((ObjectProperty<SVGPath>) properties.get(key)).get());
                case "roundedCorners"                    -> TILE.setRoundedCorners(((BooleanProperty) properties.get(key)).get());
                case "textSize"                          -> TILE.setTextSize(((ObjectProperty<TextSize>) properties.get(key)).get());
                case "currentLocation"                   -> TILE.setCurrentLocation(((ObjectProperty<Location>) properties.get(key)).get());
                case "trackColor"                        -> TILE.setTrackColor(((ObjectProperty<TileColor>) properties.get(key)).get());
                case "mapProvider"                       -> TILE.setMapProvider(((ObjectProperty<MapProvider>) properties.get(key)).get());
                case "tooltipText"                       -> TILE.setTooltipText(((StringProperty) properties.get(key)).get());
                case "xAxis"                             -> TILE.setXAxis(((ObjectProperty<Axis>) properties.get(key)).get());
                case "yAxis"                             -> TILE.setYAxis(((ObjectProperty<Axis>) properties.get(key)).get());
                case "radarChartMode"                    -> TILE.setRadarChartMode(((ObjectProperty<RadarChartMode>) properties.get(key)).get());
                case "chartGridColor"                    -> TILE.setChartGridColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "country"                           -> TILE.setCountry(((ObjectProperty<Country>) properties.get(key)).get());
                case "countryGroup"                      -> TILE.setCountryGroup(((ObjectProperty<BusinessRegion>) properties.get(key)).get());
                case "flipTimeInMS"                      -> TILE.setFlipTimeInMS(((LongProperty) properties.get(key)).get());
                case "flipText"                          -> TILE.setFlipText(((StringProperty) properties.get(key)).get());
                case "itemSorting"                       -> TILE.setItemSorting(((ObjectProperty<ItemSorting>) properties.get(key)).get());
                case "itemSortingTopic"                  -> TILE.setItemSortingTopic(((ObjectProperty<ItemSortingTopic>) properties.get(key)).get());
                case "autoItemTextColor"                 -> TILE.setAutoItemTextColor(((BooleanProperty) properties.get(key)).get());
                case "autoItemDarkTextColor"             -> TILE.setAutoItemDarkTextColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "autoItemBrightTextColor"           -> TILE.setAutoItemBrightTextColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "dataPointsVisible"                 -> TILE.setDataPointsVisible(((BooleanProperty) properties.get(key)).get());
                case "sunburstTree"                      -> TILE.getSunburstChart().setTree(((ObjectProperty<TreeNode>) properties.get(key)).get());
                case "sunburstBackgroundColor"           -> TILE.getSunburstChart().setBackgroundColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "sunburstTextColor"                 -> TILE.getSunburstChart().setTextColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "sunburstUseColorFromParent"        -> TILE.getSunburstChart().setUseColorFromParent(((BooleanProperty) properties.get(key)).get());
                case "sunburstTextOrientation"           -> TILE.getSunburstChart().setTextOrientation(((ObjectProperty<TextOrientation>) properties.get(key)).get());
                case "sunburstVisibleData"               -> TILE.getSunburstChart().setVisibleData(((ObjectProperty<VisibleData>) properties.get(key)).get());
                case "sunburstInteractive"               -> TILE.getSunburstChart().setInteractive(((BooleanProperty) properties.get(key)).get());
                case "sunburstAutoTextColor"             -> TILE.getSunburstChart().setAutoTextColor(((BooleanProperty) properties.get(key)).get());
                case "sunburstUseChartDataTextColor"     -> TILE.getSunburstChart().setUseChartDataTextColor(((BooleanProperty) properties.get(key)).get());
                case "snapToTicks"                       -> TILE.setSnapToTicks(((BooleanProperty) properties.get(key)).get());
                case "minorTickCount"                    -> TILE.setMinorTickCount(((IntegerProperty) properties.get(key)).get());
                case "majorTickUnit"                     -> TILE.setMajorTickUnit(((DoubleProperty) properties.get(key)).get());
                case "matrixSize"                        -> {
                    final int COLS = ((IntegerProperty) properties.get("matrixColumns")).get();
                    final int ROWS = ((IntegerProperty) properties.get("matrixRows")).get();
                    TILE.setMatrixSize(COLS, ROWS);
                }
                case "chartType"                         -> TILE.setChartType(((ObjectProperty<ChartType>) properties.get(key)).get());
                case "tooltipTimeout"                    -> TILE.setTooltipTimeout(((DoubleProperty) properties.get(key)).get());
                case "notifyRegionBackgroundColor"       -> TILE.setNotifyRegionBackgroundColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "notifyRegionForegroundColor"       -> TILE.setNotifyRegionForegroundColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "showNotifyRegion"                  -> TILE.showNotifyRegion(((BooleanProperty) properties.get(key)).get());
                case "infoRegionBackgroundColor"         -> TILE.setInfoRegionBackgroundColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "infoRegionForegroundColor"         -> TILE.setInfoRegionForegroundColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "showInfoRegion"                    -> TILE.showInfoRegion(((BooleanProperty) properties.get(key)).get());
                case "lowerRightRegionBackgroundColor"   -> TILE.setLowerRightRegionBackgroundColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "lowerRightRegionForegroundColor"   -> TILE.setLowerRightRegionForegroundColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "lowerRightRegionTooltipText"       -> TILE.setLowerRightRegionTooltipText(((StringProperty) properties.get(key)).get());
                case "showLowerRightRegion"              -> TILE.showLowerRightRegion(((BooleanProperty) properties.get(key)).get());
                case "leftText"                          -> TILE.setLeftText(((StringProperty) properties.get(key)).get());
                case "middleText"                        -> TILE.setMiddleText(((StringProperty) properties.get(key)).get());
                case "rightText"                         -> TILE.setRightText(((StringProperty) properties.get(key)).get());
                case "leftValue"                         -> TILE.setLeftValue(((DoubleProperty) properties.get(key)).get());
                case "middleValue"                       -> TILE.setMiddleValue(((DoubleProperty) properties.get(key)).get());
                case "rightValue"                        -> TILE.setRightValue(((DoubleProperty) properties.get(key)).get());
                case "leftGraphics"                      -> TILE.setLeftGraphics(((ObjectProperty<Node>) properties.get(key)).get());
                case "middleGraphics"                    -> TILE.setMiddleGraphics(((ObjectProperty<Node>) properties.get(key)).get());
                case "rightGraphics"                     -> TILE.setRightGraphics(((ObjectProperty<Node>) properties.get(key)).get());
                case "trendVisible"                      -> TILE.setTrendVisible(((BooleanProperty) properties.get(key)).get());
                case "timeoutMs"                         -> TILE.setTimeoutMs(((LongProperty) properties.get(key)).get());
                case "rank"                              -> TILE.setRank(((ObjectProperty<Rank>) properties.get(key)).get());
                case "interactive"                       -> TILE.setInteractive(((BooleanProperty) properties.get(key)).get());
                case "numberOfValuesForTrendCalculation" -> TILE.setNumberOfValuesForTrendCalculation(((IntegerProperty) properties.get(key)).get());
                case "backgroundImage"                   -> TILE.setBackgroundImage(((ObjectProperty<Image>) properties.get(key)).get());
                case "backgroundImageOpacity"            -> TILE.setBackgroundImageOpacity(((DoubleProperty) properties.get(key)).get());
                case "backgroundImageKeepAspect"         -> TILE.setBackgroundImageKeepAspect(((BooleanProperty) properties.get(key)).get());
                case "infoRegionEventHandler"            -> TILE.setInfoRegionEventHandler(((ObjectProperty<EventHandler<MouseEvent>>) properties.get(key)).get());
                case "infoRegionTooltipText"             -> TILE.setInfoRegionTooltipText(((StringProperty) properties.get(key)).get());
                case "notifyRegionTooltipText"           -> TILE.setNotifyRegionTooltipText(((StringProperty) properties.get(key)).get());
            }
        }
        properties.clear();
        return TILE;
    }
}
