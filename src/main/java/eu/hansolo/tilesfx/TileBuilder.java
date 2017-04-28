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

package eu.hansolo.tilesfx;

import eu.hansolo.tilesfx.Tile.MapProvider;
import eu.hansolo.tilesfx.Tile.SkinType;
import eu.hansolo.tilesfx.Tile.TextSize;
import eu.hansolo.tilesfx.Tile.TileColor;
import eu.hansolo.tilesfx.events.AlarmEventListener;
import eu.hansolo.tilesfx.events.TileEventListener;
import eu.hansolo.tilesfx.events.TimeEventListener;
import eu.hansolo.tilesfx.skins.BarChartItem;
import eu.hansolo.tilesfx.skins.LeaderBoardItem;
import eu.hansolo.tilesfx.tools.ChartData;
import eu.hansolo.tilesfx.tools.Location;
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
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.XYChart.Series;
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

    public final B description(final String SUBTITLE) {
        properties.put("description", new SimpleStringProperty(SUBTITLE));
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

    public final B needleBorderColor(final Color COLOR) {
        properties.put("needleBorderColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B barColor(final Color COLOR) {
        properties.put("barColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B barBorderColor(final Color COLOR) {
        properties.put("barBorderColor", new SimpleObjectProperty<>(COLOR));
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

    public final B radialChartData(final ChartData... DATA) {
        properties.put("radialChartDataArray", new SimpleObjectProperty(DATA));
        return (B)this;
    }

    public final B radialChartData(final List<ChartData> DATA) {
        properties.put("radialChartDataList", new SimpleObjectProperty(DATA));
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

    public final B roundedCorners(final boolean ROUNDED) {
        properties.put("roundedCorners", new SimpleBooleanProperty(ROUNDED));
        return (B)this;
    }

    public final B textSize(final TextSize SIZE) {
        properties.put("textSize", new SimpleObjectProperty(SIZE));
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
        final Tile CONTROL;
        if (properties.containsKey("skinType")) {
            SkinType skinType = ((ObjectProperty<SkinType>) properties.get("skinType")).get();
            CONTROL = new Tile(skinType);
            switch (skinType) {
                case AREA_CHART:
                    break;
                case BAR_CHART:
                    break;
                case LINE_CHART:
                    break;
                case CLOCK:
                    break;
                case GAUGE:
                    CONTROL.setAnimated(true);
                    CONTROL.setTickLabelDecimals(0);
                    CONTROL.setBarColor(Tile.FOREGROUND);
                    CONTROL.setThresholdColor(Tile.BLUE);
                    CONTROL.setThresholdVisible(true);
                    break;
                case HIGH_LOW:
                    CONTROL.setMaxValue(Double.MAX_VALUE);
                    CONTROL.setDecimals(2);
                    CONTROL.setTickLabelDecimals(1);
                    break;
                case PERCENTAGE:
                    CONTROL.setAnimated(true);
                    CONTROL.setThresholdColor(Tile.GRAY);
                    CONTROL.setTickLabelDecimals(0);
                    break;
                case PLUS_MINUS:
                    break;
                case SLIDER:
                    CONTROL.setBarBackgroundColor(Tile.FOREGROUND);
                    break;
                case SPARK_LINE:
                    CONTROL.setTextVisible(false);
                    CONTROL.setAnimated(false);
                    CONTROL.setAveragingEnabled(true);
                    CONTROL.setAveragingPeriod(10);
                    CONTROL.setDecimals(0);
                    CONTROL.setTickLabelDecimals(0);
                    break;
                case SWITCH:
                    break;
                case WORLDMAP:
                    CONTROL.setPrefSize(380, 250);
                    break;
                case TIMER_CONTROL:
                    CONTROL.setSectionsVisible(true);
                    CONTROL.setHighlightSections(true);
                    CONTROL.setCheckSectionsForValue(true);
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
                    CONTROL.setAnimated(true);
                    break;
                case DONUT_CHART:
                    CONTROL.setAnimated(true);
                    break;
                case CIRCULAR_PROGRESS:
                    CONTROL.setBarBackgroundColor(CONTROL.getBackgroundColor().brighter());
                    CONTROL.setAnimated(true);
                    break;
                case STOCK:
                    CONTROL.setAnimated(false);
                    CONTROL.setAveragingPeriod(720);
                    CONTROL.setAveragingEnabled(true);
                    CONTROL.setDecimals(2);
                    CONTROL.setTickLabelDecimals(2);
                    CONTROL.setThresholdColor(Tile.GRAY);
                    CONTROL.setTextVisible(false);
                    break;
                default:
                    break;
            }
        } else {
            CONTROL = new Tile();
        }

        // Make sure that sections, areas and markers will be added first
        if (properties.keySet().contains("sectionsArray")) {
            CONTROL.setSections(((ObjectProperty<Section[]>) properties.get("sectionsArray")).get());
        }
        if(properties.keySet().contains("sectionsList")) {
            CONTROL.setSections(((ObjectProperty<List<Section>>) properties.get("sectionsList")).get());
        }

        if(properties.keySet().contains("foregroundBaseColor")) {
            CONTROL.setForegroundBaseColor(((ObjectProperty<Color>) properties.get("foregroundBaseColor")).get());
        }

        if (properties.keySet().contains("minValue")) {
            CONTROL.setMinValue(((DoubleProperty) properties.get("minValue")).get());
        }
        if (properties.keySet().contains("maxValue")) {
            CONTROL.setMaxValue(((DoubleProperty) properties.get("maxValue")).get());
        }

        if (properties.keySet().contains("alarmsArray")) {
            CONTROL.setAlarms(((ObjectProperty<Alarm[]>) properties.get("alarmsArray")).get());
        }
        if(properties.keySet().contains("alarmsList")) {
            CONTROL.setAlarms(((ObjectProperty<List<Alarm>>) properties.get("alarmsList")).get());
        }

        if (properties.keySet().contains("timeSectionsArray")) {
            CONTROL.setTimeSections(((ObjectProperty<TimeSection[]>) properties.get("timeSectionsArray")).get());
        }
        if(properties.keySet().contains("timeSectionsList")) {
            CONTROL.setTimeSections(((ObjectProperty<List<TimeSection>>) properties.get("timeSectionsList")).get());
        }

        if (properties.keySet().contains("seriesArray")) {
            CONTROL.setSeries(((ObjectProperty<Series<String, Number>[]>) properties.get("seriesArray")).get());
        }

        if (properties.keySet().contains("seriesList")) {
            CONTROL.setSeries(((ObjectProperty<List<Series<String, Number>>>) properties.get("seriesList")).get());
        }

        if (properties.keySet().contains("barChartItemsArray")) {
            CONTROL.setBarChartItems(((ObjectProperty<BarChartItem[]>) properties.get("barChartItemsArray")).get());
        }
        if(properties.keySet().contains("barChartItemsList")) {
            CONTROL.setBarChartItems(((ObjectProperty<List<BarChartItem>>) properties.get("barChartItemsList")).get());
        }

        if (properties.keySet().contains("leaderBoardItemsArray")) {
            CONTROL.setLeaderBoardItems(((ObjectProperty<LeaderBoardItem[]>) properties.get("leaderBoardItemsArray")).get());
        }
        if(properties.keySet().contains("leaderBoardItemsList")) {
            CONTROL.setLeaderBoardItems(((ObjectProperty<List<LeaderBoardItem>>) properties.get("leaderBoardItemsList")).get());
        }

        if (properties.keySet().contains("gradientStopsArray")) {
            CONTROL.setGradientStops(((ObjectProperty<Stop[]>) properties.get("gradientStopsArray")).get());
        }
        if (properties.keySet().contains("gradientStopsList")) {
            CONTROL.setGradientStops(((ObjectProperty<List<Stop>>) properties.get("gradientStopsList")).get());
        }

        if (properties.keySet().contains("radialChartDataArray")) {
            CONTROL.setRadialChartData(((ObjectProperty<ChartData[]>) properties.get("radialChartDataArray")).get());
        }
        if (properties.keySet().contains("radialChartDataList")) {
            CONTROL.setRadialChartData(((ObjectProperty<List<ChartData>>) properties.get("radialChartDataList")).get());
        }

        if (properties.keySet().contains("poiArray")) {
            CONTROL.setPoiLocations(((ObjectProperty<Location[]>) properties.get("poiArray")).get());
        }
        if (properties.keySet().contains("poiList")) {
            CONTROL.setPoiList(((ObjectProperty<List<Location>>) properties.get("poiList")).get());
        }

        if (properties.keySet().contains("trackArray")) {
            CONTROL.setTrack(((ObjectProperty<Location[]>) properties.get("trackArray")).get());
        }
        if (properties.keySet().contains("trackList")) {
            CONTROL.setTrack(((ObjectProperty<List<Location>>) properties.get("trackList")).get());
        }

        for (String key : properties.keySet()) {
            if ("prefSize".equals(key)) {
                Dimension2D dim = ((ObjectProperty<Dimension2D>) properties.get(key)).get();
                CONTROL.setPrefSize(dim.getWidth(), dim.getHeight());
            } else if("minSize".equals(key)) {
                Dimension2D dim = ((ObjectProperty<Dimension2D>) properties.get(key)).get();
                CONTROL.setMinSize(dim.getWidth(), dim.getHeight());
            } else if("maxSize".equals(key)) {
                Dimension2D dim = ((ObjectProperty<Dimension2D>) properties.get(key)).get();
                CONTROL.setMaxSize(dim.getWidth(), dim.getHeight());
            } else if("prefWidth".equals(key)) {
                CONTROL.setPrefWidth(((DoubleProperty) properties.get(key)).get());
            } else if("prefHeight".equals(key)) {
                CONTROL.setPrefHeight(((DoubleProperty) properties.get(key)).get());
            } else if("minWidth".equals(key)) {
                CONTROL.setMinWidth(((DoubleProperty) properties.get(key)).get());
            } else if("minHeight".equals(key)) {
                CONTROL.setMinHeight(((DoubleProperty) properties.get(key)).get());
            } else if("maxWidth".equals(key)) {
                CONTROL.setMaxWidth(((DoubleProperty) properties.get(key)).get());
            } else if("maxHeight".equals(key)) {
                CONTROL.setMaxHeight(((DoubleProperty) properties.get(key)).get());
            } else if("scaleX".equals(key)) {
                CONTROL.setScaleX(((DoubleProperty) properties.get(key)).get());
            } else if("scaleY".equals(key)) {
                CONTROL.setScaleY(((DoubleProperty) properties.get(key)).get());
            } else if ("layoutX".equals(key)) {
                CONTROL.setLayoutX(((DoubleProperty) properties.get(key)).get());
            } else if ("layoutY".equals(key)) {
                CONTROL.setLayoutY(((DoubleProperty) properties.get(key)).get());
            } else if ("translateX".equals(key)) {
                CONTROL.setTranslateX(((DoubleProperty) properties.get(key)).get());
            } else if ("translateY".equals(key)) {
                CONTROL.setTranslateY(((DoubleProperty) properties.get(key)).get());
            } else if ("padding".equals(key)) {
                CONTROL.setPadding(((ObjectProperty<Insets>) properties.get(key)).get());
            } else if("styleClass".equals(key)) {
                CONTROL.getStyleClass().setAll("gauge");
                CONTROL.getStyleClass().addAll(((ObjectProperty<String[]>) properties.get(key)).get());
            } else if ("autoScale".equals(key)) {
                CONTROL.setAutoScale(((BooleanProperty) properties.get(key)).get());
            } else if("value".equals(key)) {
                CONTROL.setValue(((DoubleProperty) properties.get(key)).get());
            } else if("decimals".equals(key)) {
                CONTROL.setDecimals(((IntegerProperty) properties.get(key)).get());
            } else if("tickLabelDecimals".equals(key)) {
                CONTROL.setTickLabelDecimals(((IntegerProperty) properties.get(key)).get());
            } else if("title".equals(key)) {
                CONTROL.setTitle(((StringProperty) properties.get(key)).get());
            } else if("titleAlignment".equals(key)) {
                CONTROL.setTitleAlignment(((ObjectProperty<TextAlignment>) properties.get(key)).get());
            } else if("description".equals(key)) {
                CONTROL.setDescription(((StringProperty) properties.get(key)).get());
            } else if ("descriptionAlignment".equals(key)) {
                CONTROL.setDescriptionAlignment(((ObjectProperty<Pos>) properties.get(key)).get());
            } else if("unit".equals(key)) {
                CONTROL.setUnit(((StringProperty) properties.get(key)).get());
            } else if ("selected".equals(key)) {
                CONTROL.setSelected(((BooleanProperty) properties.get(key)).get());
            } else if("averagingEnabled".equals(key)) {
                CONTROL.setAveragingEnabled(((BooleanProperty) properties.get(key)).get());
            } else if("averagingPeriod".equals(key)) {
                CONTROL.setAveragingPeriod(((IntegerProperty) properties.get(key)).get());
            } else if("startFromZero".equals(key)) {
                CONTROL.setStartFromZero(((BooleanProperty) properties.get(key)).get());
            } else if("returnToZero".equals(key)) {
                CONTROL.setReturnToZero(((BooleanProperty) properties.get(key)).get());
            } else if ("minMeasuredValueVisible".equals(key)) {
                CONTROL.setMinMeasuredValueVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("maxMeasuredValueVisible".equals(key)) {
                CONTROL.setMaxMeasuredValueVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("oldValueVisible".equals(key)) {
                CONTROL.setOldValueVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("valueVisible".equals(key)) {
                CONTROL.setValueVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("foregroundColor".equals(key)) {
                CONTROL.setForegroundColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("backgroundColor".equals(key)) {
                CONTROL.setBackgroundColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("borderColor".equals(key)) {
                CONTROL.setBorderColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("borderWidth".equals(key)) {
                CONTROL.setBorderWidth(((DoubleProperty) properties.get(key)).get());
            } else if ("knobColor".equals(key)) {
                CONTROL.setKnobColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if("animated".equals(key)) {
                CONTROL.setAnimated(((BooleanProperty) properties.get(key)).get());
            } else if("animationDuration".equals(key)) {
                CONTROL.setAnimationDuration(((LongProperty) properties.get(key)).get());
            } else if("startAngle".equals(key)) {
                CONTROL.setStartAngle(((DoubleProperty) properties.get(key)).get());
            } else if("angleRange".equals(key)) {
                CONTROL.setAngleRange(((DoubleProperty) properties.get(key)).get());
            } else if("needleColor".equals(key)) {
                CONTROL.setNeedleColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if("barColor".equals(key)) {
                CONTROL.setBarColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("barBackgroundColor".equals(key)) {
                CONTROL.setBarBackgroundColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if("locale".equals(key)) {
                CONTROL.setLocale(((ObjectProperty<Locale>) properties.get(key)).get());
            } else if("numberFormat".equals(key)) {
                CONTROL.setNumberFormat(((ObjectProperty<NumberFormat>) properties.get(key)).get());
            } else if("shadowsEnabled".equals(key)) {
                CONTROL.setShadowsEnabled(((BooleanProperty) properties.get(key)).get());
            } else if ("style".equals(key)) {
                CONTROL.setStyle(((StringProperty) properties.get(key)).get());
            } else if ("innerShadowEnabled".equals(key)) {
                CONTROL.setInnerShadowEnabled(((BooleanProperty) properties.get(key)).get());
            } else if ("thresholdVisible".equals(key)) {
                CONTROL.setThresholdVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("averageVisible".equals(key)) {
                CONTROL.setAverageVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("sectionsVisible".equals(key)) {
                CONTROL.setSectionsVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("sectionsAlwaysVisible".equals(key)) {
                CONTROL.setSectionsAlwaysVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("sectionTextVisible".equals(key)) {
                CONTROL.setSectionTextVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("sectionIconsVisible".equals(key)) {
                CONTROL.setSectionIconsVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("highlightSections".equals(key)) {
                CONTROL.setHighlightSections(((BooleanProperty) properties.get(key)).get());
            } else if ("titleColor".equals(key)) {
                CONTROL.setTitleColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("descriptionColor".equals(key)) {
                CONTROL.setDescriptionColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("unitColor".equals(key)) {
                CONTROL.setUnitColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("valueColor".equals(key)) {
                CONTROL.setValueColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("thresholdColor".equals(key)) {
                CONTROL.setThresholdColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("orientation".equals(key)) {
                CONTROL.setOrientation(((ObjectProperty<Orientation>) properties.get(key)).get());
            } else if ("checkSectionsForValue".equals(key)) {
                CONTROL.setCheckSectionsForValue(((BooleanProperty) properties.get(key)).get());
            } else if ("checkThreshold".equals(key)) {
                CONTROL.setCheckThreshold(((BooleanProperty) properties.get(key)).get());
            } else if ("onValueChanged".equals(key)) {
                CONTROL.currentValueProperty().addListener(((ObjectProperty<InvalidationListener>) properties.get(key)).get());
            } else if ("keepAspect".equals(key)) {
                CONTROL.setKeepAspect(((BooleanProperty) properties.get(key)).get());
            } else if ("threshold".equals(key)) {
                CONTROL.setThreshold(((DoubleProperty) properties.get(key)).get());
            } else if ("referenceValue".equals(key)) {
                CONTROL.setReferenceValue(((DoubleProperty) properties.get(key)).get());
            } else if ("autoReferenceValue".equals(key)) {
                CONTROL.setAutoReferenceValue(((BooleanProperty) properties.get(key)).get());
            } else if ("customFontEnabled".equals(key)) {
                CONTROL.setCustomFontEnabled(((BooleanProperty) properties.get(key)).get());
            } else if ("customFont".equals(key)) {
                CONTROL.setCustomFont(((ObjectProperty<Font>) properties.get(key)).get());
            } else if ("alertMessage".equals(key)) {
                CONTROL.setAlertMessage(((StringProperty) properties.get(key)).get());
            } else if ("smoothing".equals(key)) {
                CONTROL.setSmoothing(((BooleanProperty) properties.get(key)).get());
            } else if ("time".equals(key)) {
                CONTROL.setTime(((ObjectProperty<ZonedDateTime>) properties.get(key)).get());
            } else if ("text".equals(key)) {
                CONTROL.setText(((StringProperty) properties.get(key)).get());
            } else if ("textAlignment".equals(key)) {
                CONTROL.setTextAlignment(((ObjectProperty<TextAlignment>) properties.get(key)).get());
            } else if ("discreteSeconds".equals(key)) {
                CONTROL.setDiscreteSeconds(((BooleanProperty) properties.get(key)).get());
            } else if ("discreteMinutes".equals(key)) {
                CONTROL.setDiscreteMinutes(((BooleanProperty) properties.get(key)).get());
            } else if ("discreteHours".equals(key)) {
                CONTROL.setDiscreteHours(((BooleanProperty) properties.get(key)).get());
            } else if ("secondsVisible".equals(key)) {
                CONTROL.setSecondsVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("textVisible".equals(key)) {
                CONTROL.setTextVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("dateVisible".equals(key)) {
                CONTROL.setDateVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("textColor".equals(key)) {
                CONTROL.setTextColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("dateColor".equals(key)) {
                CONTROL.setDateColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("hourTickMarkColor".equals(key)) {
                CONTROL.setHourTickMarkColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("minuteTickMarkColor".equals(key)) {
                CONTROL.setMinuteTickMarkColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("alarmColor".equals(key)) {
                CONTROL.setAlarmColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("hourTickMarksVisible".equals(key)) {
                CONTROL.setHourTickMarksVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("minuteTickMarksVisible".equals(key)) {
                CONTROL.setMinuteTickMarksVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("hourColor".equals(key)) {
                CONTROL.setHourColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("minuteColor".equals(key)) {
                CONTROL.setMinuteColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("secondColor".equals(key)) {
                CONTROL.setSecondColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("onAlarm".equals(key)) {
                CONTROL.setOnAlarm(((ObjectProperty<AlarmEventListener>) properties.get(key)).get());
            } else if ("onTimeEvent".equals(key)) {
                CONTROL.setOnTimeEvent(((ObjectProperty<TimeEventListener>) properties.get(key)).get());
            } else if ("alarmsEnabled".equals(key)) {
                CONTROL.setAlarmsEnabled(((BooleanProperty) properties.get(key)).get());
            } else if ("alarmsVisible".equals(key)) {
                CONTROL.setAlarmsVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("running".equals(key)) {
                CONTROL.setRunning(((BooleanProperty) properties.get(key)).get());
            } else if ("increment".equals(key)) {
                CONTROL.setIncrement(((DoubleProperty) properties.get(key)).get());
            } else if ("activeColor".equals(key)) {
                CONTROL.setActiveColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("darkSky".equals(key)) {
                CONTROL.setDarkSky(((ObjectProperty<DarkSky>) properties.get(key)).get());
            } else if ("duration".equals(key)) {
                CONTROL.setDuration(((ObjectProperty<LocalTime>) properties.get(key)).get());
            } else if ("strokeWithGradient".equals(key)) {
                CONTROL.setStrokeWithGradient(((BooleanProperty) properties.get(key)).get());
            } else if ("graphic".equals(key)) {
                CONTROL.setGraphic(((ObjectProperty<Node>) properties.get(key)).get());
            } else if ("roundedCorners".equals(key)) {
                CONTROL.setRoundedCorners(((BooleanProperty) properties.get(key)).get());
            } else if ("textSize".equals(key)) {
                CONTROL.setTextSize(((ObjectProperty<TextSize>) properties.get(key)).get());
            } else if ("currentLocation".equals(key)) {
                CONTROL.setCurrentLocation(((ObjectProperty<Location>) properties.get(key)).get());
            } else if ("trackColor".equals(key)) {
                CONTROL.setTrackColor(((ObjectProperty<TileColor>) properties.get(key)).get());
            } else if ("mapProvider".equals(key)) {
                CONTROL.setMapProvider(((ObjectProperty<MapProvider>) properties.get(key)).get());
            } else if ("tooltipText".equals(key)) {
                CONTROL.setTooltipText(((StringProperty) properties.get(key)).get());
            }
        }
        return CONTROL;
    }
}
