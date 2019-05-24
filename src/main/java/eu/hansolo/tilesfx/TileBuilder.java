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

package eu.hansolo.tilesfx;

import eu.hansolo.tilesfx.Tile.ChartType;
import eu.hansolo.tilesfx.Tile.ImageMask;
import eu.hansolo.tilesfx.Tile.MapProvider;
import eu.hansolo.tilesfx.Tile.SkinType;
import eu.hansolo.tilesfx.Tile.TextSize;
import eu.hansolo.tilesfx.Tile.TileColor;
import eu.hansolo.tilesfx.chart.RadarChart;
import eu.hansolo.tilesfx.chart.SunburstChart.TextOrientation;
import eu.hansolo.tilesfx.chart.SunburstChart.VisibleData;
import eu.hansolo.tilesfx.chart.TilesFXSeries;
import eu.hansolo.tilesfx.events.AlarmEventListener;
import eu.hansolo.tilesfx.events.TileEventListener;
import eu.hansolo.tilesfx.events.TimeEventListener;
import eu.hansolo.tilesfx.skins.BarChartItem;
import eu.hansolo.tilesfx.skins.LeaderBoardItem;
import eu.hansolo.tilesfx.chart.ChartData;
import eu.hansolo.tilesfx.tools.Country;
import eu.hansolo.tilesfx.tools.CountryGroup;
import eu.hansolo.tilesfx.tools.Location;
import eu.hansolo.tilesfx.tools.TreeNode;
import eu.hansolo.tilesfx.weather.DarkSky;
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
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.text.NumberFormat;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


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
        return (B) this;
    }

    public final B tickLabelDecimals(final int DECIMALS) {
        properties.put("tickLabelDecimals", new SimpleIntegerProperty(DECIMALS));
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

    public final B checkSectionsForValue(final boolean CHECK) {
        properties.put("checkSectionsForValue", new SimpleBooleanProperty(CHECK));
        return (B)this;
    }

    public final B checkThreshold(final boolean CHECK) {
        properties.put("checkThreshold", new SimpleBooleanProperty(CHECK));
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

    public final B onThresholdExceeded(final TileEventListener HANDLER) {
        properties.put("onThresholdExceeded", new SimpleObjectProperty<>(HANDLER));
        return (B)this;
    }

    public final B onThresholdUnderrun(final TileEventListener HANDLER) {
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

    public final B onAlarm(final AlarmEventListener LISTENER) {
        properties.put("onAlarm", new SimpleObjectProperty<>(LISTENER));
        return (B)this;
    }

    public final B onTimeEvent(final TimeEventListener LISTENER) {
        properties.put("onTimeEvent", new SimpleObjectProperty<>(LISTENER));
        return (B)this;
    }

    public final B increment(final double INCREMENT) {
        properties.put("increment", new SimpleDoubleProperty(INCREMENT));
        return (B)this;
    }

    public final B darkSky(final DarkSky DARK_SKY) {
        properties.put("darkSky", new SimpleObjectProperty(DARK_SKY));
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

    public final B radarChartMode(final RadarChart.Mode MODE) {
       properties.put("radarChartMode", new SimpleObjectProperty(MODE));
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

    public final B countryGroup(final CountryGroup COUNTRY_GROUP) {
        properties.put("countryGroup", new SimpleObjectProperty(COUNTRY_GROUP));
        return (B)this;
    }

    public final B sortedData(final boolean SORTED) {
        properties.put("sortedData", new SimpleBooleanProperty(SORTED));
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

    public final B notifyRegionBackgroundColor(final Color COLOR) {
        properties.put("notifyRegionBackgroundColor", new SimpleObjectProperty(COLOR));
        return (B)this;
    }

    public final B notifyRegionForegroundColor(final Color COLOR) {
        properties.put("notifyRegionForegroundColor", new SimpleObjectProperty(COLOR));
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

    public final B backgroundImage(final Image IMAGE) {
        properties.put("backgroundImage", new SimpleObjectProperty(IMAGE));
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
                case WEATHER:
                    break;
                case TIME:
                    break;
                case CUSTOM:
                    break;
                case LEADER_BOARD:
                    break;
                case MAP:
                    break;
                case RADIAL_CHART:
                    TILE.setAnimated(true);
                    break;
                case DONUT_CHART:
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
                    TILE.setBarColor(Tile.BLUE);
                    TILE.setAngleRange(270);
                    break;
                case SMOOTH_AREA_CHART:
                    TILE.setSmoothing(true);
                    TILE.setChartType(ChartType.AREA);
                    break;
                case RADAR_CHART:
                    break;
                case COUNTRY:
                    break;
                case EPHEMERIS:
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
            TILE.setBarChartItems(((ObjectProperty<BarChartItem[]>) properties.get("barChartItemsArray")).get());
        }
        if(properties.keySet().contains("barChartItemsList")) {
            TILE.setBarChartItems(((ObjectProperty<List<BarChartItem>>) properties.get("barChartItemsList")).get());
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
            if ("prefSize".equals(key)) {
                Dimension2D dim = ((ObjectProperty<Dimension2D>) properties.get(key)).get();
                TILE.setPrefSize(dim.getWidth(), dim.getHeight());
            } else if("minSize".equals(key)) {
                Dimension2D dim = ((ObjectProperty<Dimension2D>) properties.get(key)).get();
                TILE.setMinSize(dim.getWidth(), dim.getHeight());
            } else if("maxSize".equals(key)) {
                Dimension2D dim = ((ObjectProperty<Dimension2D>) properties.get(key)).get();
                TILE.setMaxSize(dim.getWidth(), dim.getHeight());
            } else if("prefWidth".equals(key)) {
                TILE.setPrefWidth(((DoubleProperty) properties.get(key)).get());
            } else if("prefHeight".equals(key)) {
                TILE.setPrefHeight(((DoubleProperty) properties.get(key)).get());
            } else if("minWidth".equals(key)) {
                TILE.setMinWidth(((DoubleProperty) properties.get(key)).get());
            } else if("minHeight".equals(key)) {
                TILE.setMinHeight(((DoubleProperty) properties.get(key)).get());
            } else if("maxWidth".equals(key)) {
                TILE.setMaxWidth(((DoubleProperty) properties.get(key)).get());
            } else if("maxHeight".equals(key)) {
                TILE.setMaxHeight(((DoubleProperty) properties.get(key)).get());
            } else if("scaleX".equals(key)) {
                TILE.setScaleX(((DoubleProperty) properties.get(key)).get());
            } else if("scaleY".equals(key)) {
                TILE.setScaleY(((DoubleProperty) properties.get(key)).get());
            } else if ("layoutX".equals(key)) {
                TILE.setLayoutX(((DoubleProperty) properties.get(key)).get());
            } else if ("layoutY".equals(key)) {
                TILE.setLayoutY(((DoubleProperty) properties.get(key)).get());
            } else if ("translateX".equals(key)) {
                TILE.setTranslateX(((DoubleProperty) properties.get(key)).get());
            } else if ("translateY".equals(key)) {
                TILE.setTranslateY(((DoubleProperty) properties.get(key)).get());
            } else if ("padding".equals(key)) {
                TILE.setPadding(((ObjectProperty<Insets>) properties.get(key)).get());
            } else if("styleClass".equals(key)) {
                TILE.getStyleClass().setAll("tile");
                TILE.getStyleClass().addAll(((ObjectProperty<String[]>) properties.get(key)).get());
            } else if ("autoScale".equals(key)) {
                TILE.setAutoScale(((BooleanProperty) properties.get(key)).get());
            } else if("value".equals(key)) {
                TILE.setValue(((DoubleProperty) properties.get(key)).get());
            } else if("decimals".equals(key)) {
                TILE.setDecimals(((IntegerProperty) properties.get(key)).get());
            } else if("tickLabelDecimals".equals(key)) {
                TILE.setTickLabelDecimals(((IntegerProperty) properties.get(key)).get());
            } else if("title".equals(key)) {
                TILE.setTitle(((StringProperty) properties.get(key)).get());
            } else if("titleAlignment".equals(key)) {
                TILE.setTitleAlignment(((ObjectProperty<TextAlignment>) properties.get(key)).get());
            } else if("description".equals(key)) {
                TILE.setDescription(((StringProperty) properties.get(key)).get());
            } else if ("descriptionAlignment".equals(key)) {
                TILE.setDescriptionAlignment(((ObjectProperty<Pos>) properties.get(key)).get());
            } else if("unit".equals(key)) {
                TILE.setUnit(((StringProperty) properties.get(key)).get());
            } else if ("selected".equals(key)) {
                TILE.setActive(((BooleanProperty) properties.get(key)).get());
            } else if("averagingEnabled".equals(key)) {
                TILE.setAveragingEnabled(((BooleanProperty) properties.get(key)).get());
            } else if("averagingPeriod".equals(key)) {
                TILE.setAveragingPeriod(((IntegerProperty) properties.get(key)).get());
            } else if("startFromZero".equals(key)) {
                TILE.setStartFromZero(((BooleanProperty) properties.get(key)).get());
            } else if("returnToZero".equals(key)) {
                TILE.setReturnToZero(((BooleanProperty) properties.get(key)).get());
            } else if ("minMeasuredValueVisible".equals(key)) {
                TILE.setMinMeasuredValueVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("maxMeasuredValueVisible".equals(key)) {
                TILE.setMaxMeasuredValueVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("oldValueVisible".equals(key)) {
                TILE.setOldValueVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("valueVisible".equals(key)) {
                TILE.setValueVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("foregroundColor".equals(key)) {
                TILE.setForegroundColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("backgroundColor".equals(key)) {
                TILE.setBackgroundColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("borderColor".equals(key)) {
                TILE.setBorderColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("borderWidth".equals(key)) {
                TILE.setBorderWidth(((DoubleProperty) properties.get(key)).get());
            } else if ("knobColor".equals(key)) {
                TILE.setKnobColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if("animated".equals(key)) {
                TILE.setAnimated(((BooleanProperty) properties.get(key)).get());
            } else if("animationDuration".equals(key)) {
                TILE.setAnimationDuration(((LongProperty) properties.get(key)).get());
            } else if("startAngle".equals(key)) {
                TILE.setStartAngle(((DoubleProperty) properties.get(key)).get());
            } else if("angleRange".equals(key)) {
                TILE.setAngleRange(((DoubleProperty) properties.get(key)).get());
            } else if("needleColor".equals(key)) {
                TILE.setNeedleColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if("barColor".equals(key)) {
                TILE.setBarColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("barBackgroundColor".equals(key)) {
                TILE.setBarBackgroundColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if("locale".equals(key)) {
                TILE.setLocale(((ObjectProperty<Locale>) properties.get(key)).get());
            } else if("numberFormat".equals(key)) {
                TILE.setNumberFormat(((ObjectProperty<NumberFormat>) properties.get(key)).get());
            } else if("shadowsEnabled".equals(key)) {
                TILE.setShadowsEnabled(((BooleanProperty) properties.get(key)).get());
            } else if ("style".equals(key)) {
                TILE.setStyle(((StringProperty) properties.get(key)).get());
            } else if ("innerShadowEnabled".equals(key)) {
                TILE.setInnerShadowEnabled(((BooleanProperty) properties.get(key)).get());
            } else if ("thresholdVisible".equals(key)) {
                TILE.setThresholdVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("averageVisible".equals(key)) {
                TILE.setAverageVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("sectionsVisible".equals(key)) {
                TILE.setSectionsVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("sectionsAlwaysVisible".equals(key)) {
                TILE.setSectionsAlwaysVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("sectionTextVisible".equals(key)) {
                TILE.setSectionTextVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("sectionIconsVisible".equals(key)) {
                TILE.setSectionIconsVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("highlightSections".equals(key)) {
                TILE.setHighlightSections(((BooleanProperty) properties.get(key)).get());
            } else if ("titleColor".equals(key)) {
                TILE.setTitleColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("descriptionColor".equals(key)) {
                TILE.setDescriptionColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("unitColor".equals(key)) {
                TILE.setUnitColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("valueColor".equals(key)) {
                TILE.setValueColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("thresholdColor".equals(key)) {
                TILE.setThresholdColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("orientation".equals(key)) {
                TILE.setOrientation(((ObjectProperty<Orientation>) properties.get(key)).get());
            } else if ("checkSectionsForValue".equals(key)) {
                TILE.setCheckSectionsForValue(((BooleanProperty) properties.get(key)).get());
            } else if ("checkThreshold".equals(key)) {
                TILE.setCheckThreshold(((BooleanProperty) properties.get(key)).get());
            } else if ("onValueChanged".equals(key)) {
                TILE.currentValueProperty().addListener(((ObjectProperty<InvalidationListener>) properties.get(key)).get());
            } else if ("keepAspect".equals(key)) {
                TILE.setKeepAspect(((BooleanProperty) properties.get(key)).get());
            } else if ("threshold".equals(key)) {
                TILE.setThreshold(((DoubleProperty) properties.get(key)).get());
            } else if ("referenceValue".equals(key)) {
                TILE.setReferenceValue(((DoubleProperty) properties.get(key)).get());
            } else if ("autoReferenceValue".equals(key)) {
                TILE.setAutoReferenceValue(((BooleanProperty) properties.get(key)).get());
            } else if ("customFontEnabled".equals(key)) {
                TILE.setCustomFontEnabled(((BooleanProperty) properties.get(key)).get());
            } else if ("customFont".equals(key)) {
                TILE.setCustomFont(((ObjectProperty<Font>) properties.get(key)).get());
            } else if ("alertMessage".equals(key)) {
                TILE.setAlertMessage(((StringProperty) properties.get(key)).get());
            } else if ("smoothing".equals(key)) {
                TILE.setSmoothing(((BooleanProperty) properties.get(key)).get());
            } else if ("time".equals(key)) {
                TILE.setTime(((ObjectProperty<ZonedDateTime>) properties.get(key)).get());
            } else if ("text".equals(key)) {
                TILE.setText(((StringProperty) properties.get(key)).get());
            } else if ("textAlignment".equals(key)) {
                TILE.setTextAlignment(((ObjectProperty<TextAlignment>) properties.get(key)).get());
            } else if ("discreteSeconds".equals(key)) {
                TILE.setDiscreteSeconds(((BooleanProperty) properties.get(key)).get());
            } else if ("discreteMinutes".equals(key)) {
                TILE.setDiscreteMinutes(((BooleanProperty) properties.get(key)).get());
            } else if ("discreteHours".equals(key)) {
                TILE.setDiscreteHours(((BooleanProperty) properties.get(key)).get());
            } else if ("secondsVisible".equals(key)) {
                TILE.setSecondsVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("textVisible".equals(key)) {
                TILE.setTextVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("dateVisible".equals(key)) {
                TILE.setDateVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("textColor".equals(key)) {
                TILE.setTextColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("dateColor".equals(key)) {
                TILE.setDateColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("hourTickMarkColor".equals(key)) {
                TILE.setHourTickMarkColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("minuteTickMarkColor".equals(key)) {
                TILE.setMinuteTickMarkColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("alarmColor".equals(key)) {
                TILE.setAlarmColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("hourTickMarksVisible".equals(key)) {
                TILE.setHourTickMarksVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("minuteTickMarksVisible".equals(key)) {
                TILE.setMinuteTickMarksVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("hourColor".equals(key)) {
                TILE.setHourColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("minuteColor".equals(key)) {
                TILE.setMinuteColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("secondColor".equals(key)) {
                TILE.setSecondColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("onAlarm".equals(key)) {
                TILE.setOnAlarm(((ObjectProperty<AlarmEventListener>) properties.get(key)).get());
            } else if ("onTimeEvent".equals(key)) {
                TILE.setOnTimeEvent(((ObjectProperty<TimeEventListener>) properties.get(key)).get());
            } else if ("alarmsEnabled".equals(key)) {
                TILE.setAlarmsEnabled(((BooleanProperty) properties.get(key)).get());
            } else if ("alarmsVisible".equals(key)) {
                TILE.setAlarmsVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("running".equals(key)) {
                TILE.setRunning(((BooleanProperty) properties.get(key)).get());
            } else if ("increment".equals(key)) {
                TILE.setIncrement(((DoubleProperty) properties.get(key)).get());
            } else if ("activeColor".equals(key)) {
                TILE.setActiveColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("darkSky".equals(key)) {
                TILE.setDarkSky(((ObjectProperty<DarkSky>) properties.get(key)).get());
            } else if ("duration".equals(key)) {
                TILE.setDuration(((ObjectProperty<LocalTime>) properties.get(key)).get());
            } else if ("strokeWithGradient".equals(key)) {
                TILE.setStrokeWithGradient(((BooleanProperty) properties.get(key)).get());
            } else if ("fillWithGradient".equals(key)) {
                TILE.setFillWithGradient(((BooleanProperty) properties.get(key)).get());
            } else if ("image".equals(key)) {
                TILE.setImage(((ObjectProperty<Image>) properties.get(key)).get());
            } else if ("imageMask".equals(key)) {
                TILE.setImageMask(((ObjectProperty<ImageMask>) properties.get(key)).get());
            } else if ("graphic".equals(key)) {
                TILE.setGraphic(((ObjectProperty<Node>) properties.get(key)).get());
            } else if ("roundedCorners".equals(key)) {
                TILE.setRoundedCorners(((BooleanProperty) properties.get(key)).get());
            } else if ("textSize".equals(key)) {
                TILE.setTextSize(((ObjectProperty<TextSize>) properties.get(key)).get());
            } else if ("currentLocation".equals(key)) {
                TILE.setCurrentLocation(((ObjectProperty<Location>) properties.get(key)).get());
            } else if ("trackColor".equals(key)) {
                TILE.setTrackColor(((ObjectProperty<TileColor>) properties.get(key)).get());
            } else if ("mapProvider".equals(key)) {
                TILE.setMapProvider(((ObjectProperty<MapProvider>) properties.get(key)).get());
            } else if ("tooltipText".equals(key)) {
                TILE.setTooltipText(((StringProperty) properties.get(key)).get());
            } else if ("xAxis".equals(key)) {
                TILE.setXAxis(((ObjectProperty<Axis>) properties.get(key)).get());
            } else if ("yAxis".equals(key)) {
                TILE.setYAxis(((ObjectProperty<Axis>) properties.get(key)).get());
            } else if ("radarChartMode".equals(key)) {
                TILE.setRadarChartMode(((ObjectProperty<RadarChart.Mode>) properties.get(key)).get());
            } else if ("chartGridColor".equals(key)) {
                TILE.setChartGridColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("country".equals(key)) {
                TILE.setCountry(((ObjectProperty<Country>) properties.get(key)).get());
            } else if ("countryGroup".equals(key)) {
                TILE.setCountryGroup(((ObjectProperty<CountryGroup>) properties.get(key)).get());
            } else if ("sortedData".equals(key)) {
                TILE.setSortedData(((BooleanProperty) properties.get(key)).get());
            } else if ("flipTimeInMS".equals(key)) {
                TILE.setFlipTimeInMS(((LongProperty) properties.get(key)).get());
            } else if ("flipText".equals(key)) {
                TILE.setFlipText(((StringProperty) properties.get(key)).get());
            } else if ("dataPointsVisible".equals(key)) {
                TILE.setDataPointsVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("sunburstTree".equals(key)) {
                TILE.getSunburstChart().setTree(((ObjectProperty<TreeNode>) properties.get(key)).get());
            } else if ("sunburstBackgroundColor".equals(key)) {
                TILE.getSunburstChart().setBackgroundColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("sunburstTextColor".equals(key)) {
                TILE.getSunburstChart().setTextColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("sunburstUseColorFromParent".equals(key)) {
                TILE.getSunburstChart().setUseColorFromParent(((BooleanProperty) properties.get(key)).get());
            } else if ("sunburstTextOrientation".equals(key)) {
                TILE.getSunburstChart().setTextOrientation(((ObjectProperty<TextOrientation>) properties.get(key)).get());
            } else if("sunburstVisibleData".equals(key)) {
                TILE.getSunburstChart().setVisibleData(((ObjectProperty<VisibleData>) properties.get(key)).get());
            } else if ("sunburstInteractive".equals(key)) {
                TILE.getSunburstChart().setInteractive(((BooleanProperty) properties.get(key)).get());
            } else if ("sunburstAutoTextColor".equals(key)) {
                TILE.getSunburstChart().setAutoTextColor(((BooleanProperty) properties.get(key)).get());
            } else if ("sunburstUseChartDataTextColor".equals(key)) {
                TILE.getSunburstChart().setUseChartDataTextColor(((BooleanProperty) properties.get(key)).get());
            } else if ("snapToTicks".equals(key)) {
                TILE.setSnapToTicks(((BooleanProperty) properties.get(key)).get());
            } else if ("minorTickCount".equals(key)) {
                TILE.setMinorTickCount(((IntegerProperty) properties.get(key)).get());
            } else if ("majorTickUnit".equals(key)) {
                TILE.setMajorTickUnit(((DoubleProperty) properties.get(key)).get());
            } else if ("matrixSize".equals(key)) {
                final int COLS = ((IntegerProperty) properties.get("matrixColumns")).get();
                final int ROWS = ((IntegerProperty) properties.get("matrixRows")).get();
                TILE.setMatrixSize(COLS, ROWS);
            } else if ("chartType".equals(key)) {
                TILE.setChartType(((ObjectProperty<ChartType>) properties.get(key)).get());
            } else if ("tooltipTimeout".equals(key)) {
                TILE.setTooltipTimeout(((DoubleProperty) properties.get(key)).get());
            } else if ("notifyRegionBackgroundColor".equals(key)) {
                TILE.setNotifyRegionBackgroundColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("notifyRegionForegroundColor".equals(key)) {
                TILE.setNotifyRegionForegroundColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("showNotifyRegion".equals(key)) {
                TILE.showNotifyRegion(((BooleanProperty) properties.get(key)).get());
            } else if ("infoRegionBackgroundColor".equals(key)) {
                TILE.setInfoRegionBackgroundColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("infoRegionForegroundColor".equals(key)) {
                TILE.setInfoRegionForegroundColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("showInfoRegion".equals(key)) {
                TILE.showInfoRegion(((BooleanProperty) properties.get(key)).get());
            } else if ("leftText".equals(key)) {
                TILE.setLeftText(((StringProperty) properties.get(key)).get());
            } else if ("middleText".equals(key)) {
                TILE.setMiddleText(((StringProperty) properties.get(key)).get());
            } else if ("rightText".equals(key)) {
                TILE.setRightText(((StringProperty) properties.get(key)).get());
            } else if ("leftValue".equals(key)) {
                TILE.setLeftValue(((DoubleProperty) properties.get(key)).get());
            } else if ("middleValue".equals(key)) {
                TILE.setMiddleValue(((DoubleProperty) properties.get(key)).get());
            } else if ("rightValue".equals(key)) {
                TILE.setRightValue(((DoubleProperty) properties.get(key)).get());
            } else if ("leftGraphics".equals(key)) {
                TILE.setLeftGraphics(((ObjectProperty<Node>) properties.get(key)).get());
            } else if ("middleGraphics".equals(key)) {
                TILE.setMiddleGraphics(((ObjectProperty<Node>) properties.get(key)).get());
            } else if ("rightGraphics".equals(key)) {
                TILE.setRightGraphics(((ObjectProperty<Node>) properties.get(key)).get());
            } else if ("backgroundImage".equals(key)) {
                TILE.setBackgroundImage(((ObjectProperty<Image>) properties.get(key)).get());
            } else if ("backgroundImageOpacity".equals(key)) {
                TILE.setBackgroundImageOpacity(((DoubleProperty) properties.get(key)).get());
            } else if ("backgroundImageKeepAspect".equals(key)) {
                TILE.setBackgroundImageKeepAspect(((BooleanProperty) properties.get(key)).get());
            } else if ("infoRegionEventHandler".equals(key)) {
                TILE.setInfoRegionEventHandler(((ObjectProperty<EventHandler<MouseEvent>>) properties.get(key)).get());
            } else if ("infoRegionTooltipText".equals(key)) {
                TILE.setInfoRegionTooltipText(((StringProperty) properties.get(key)).get());
            }
        }
        properties.clear();
        return TILE;
    }
}
