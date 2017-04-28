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

import eu.hansolo.tilesfx.events.AlarmEvent;
import eu.hansolo.tilesfx.events.AlarmEventListener;
import eu.hansolo.tilesfx.events.SwitchEvent;
import eu.hansolo.tilesfx.events.TileEvent;
import eu.hansolo.tilesfx.events.TileEvent.EventType;
import eu.hansolo.tilesfx.events.TileEventListener;
import eu.hansolo.tilesfx.events.TimeEvent;
import eu.hansolo.tilesfx.events.TimeEvent.TimeEventType;
import eu.hansolo.tilesfx.events.TimeEventListener;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.skins.*;
import eu.hansolo.tilesfx.tools.ChartData;
import eu.hansolo.tilesfx.tools.TimeData;
import eu.hansolo.tilesfx.tools.Helper;
import eu.hansolo.tilesfx.tools.Location;
import eu.hansolo.tilesfx.tools.MovingAverage;
import eu.hansolo.tilesfx.tools.SectionComparator;
import eu.hansolo.tilesfx.tools.TimeSectionComparator;
import eu.hansolo.tilesfx.weather.DarkSky;
import javafx.animation.Animation.Status;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static eu.hansolo.tilesfx.tools.Helper.clamp;
import static eu.hansolo.tilesfx.tools.MovingAverage.MAX_PERIOD;


/**
 * Created by hansolo on 19.12.16.
 */
public class Tile extends Control {
    public enum SkinType { AREA_CHART("AreaChartTileSkin"), BAR_CHART("BarChartTileSkin"),
                           LINE_CHART("LineChartTileSkin"), CLOCK("ClockTileSkin"), GAUGE("GaugeTileSkin"),
                           HIGH_LOW("HighLowTileSkin)"), PERCENTAGE("PercentageTileSkin"),
                           PLUS_MINUS("PlusMinusTileSkin"), SLIDER("SliderTileSkin"),
                           SPARK_LINE("SparkLineTileSkin"), SWITCH("SwitchTileSkin"),
                           WORLDMAP("WorldMapTileSkin"), TIMER_CONTROL("TimerControlTileSkin"),
                           NUMBER("NumberTileSkin"), TEXT("TextTileSkin"),
                           WEATHER("WeatherTileSkin"), TIME("TimeTileSkin"),
                           CUSTOM("CustomTileSkin"), LEADER_BOARD("LeaderBoardTileSkin"),
                           MAP("MapTileSkin"), RADIAL_CHART("RadialChart"), DONUT_CHART("DonutChart"),
                           CIRCULAR_PROGRESS("CircularProgress"), STOCK("Stock");

        public final String CLASS_NAME;
        SkinType(final String CLASS_NAME) {
            this.CLASS_NAME = CLASS_NAME;
        }
    }
    public enum TextSize {
        NORMAL(0.06),
        BIGGER(0.08);

        public final double factor;

        TextSize(final double FACTOR) {
            factor = FACTOR;
        }
    }
    public enum TileColor {
        GRAY(Color.rgb(139,144,146), "gray"),
        RED(Color.rgb(229, 80, 76), "red"),
        LIGHT_RED(Color.rgb(255, 84, 56), "light-red"),
        GREEN(Color.rgb(143, 198, 94), "green"),
        LIGHT_GREEN(Color.rgb(132, 228, 50), "light-green"),
        BLUE(Color.rgb(55, 179, 252), "blue"),
        DARK_BLUE(Color.rgb(55, 94, 252), "dark-blue"),
        ORANGE(Color.rgb(237, 162, 57), "orange"),
        YELLOW_ORANGE(Color.rgb(229, 198, 76), "yellow-orange"),
        YELLOW(Color.rgb(229, 229, 76), "yellow"),
        MAGENTA(Color.rgb(198, 75, 232), "magenta");

        public final Color  color;
        public final String styleName;

        TileColor(final Color COLOR, final String STYLE_NAME) {
            color     = COLOR;
            styleName = STYLE_NAME;
        }
    }
    public enum MapProvider {
        BW("blackwhite"),
        STREET("street"),
        BRIGHT("bright"),
        DARK("dark"),
        SAT("sat"),
        TOPO("topo");

        public final String name;

        MapProvider(final String NAME) {
            name = NAME;
        }
    }

    public  static final Color       BACKGROUND            = Color.rgb(42, 42, 42);
    public  static final Color       FOREGROUND            = Color.rgb(223, 223, 223);
    public  static final Color       GRAY                  = TileColor.GRAY.color;
    public  static final Color       RED                   = TileColor.RED.color;
    public  static final Color       LIGHT_RED             = TileColor.LIGHT_RED.color;
    public  static final Color       GREEN                 = TileColor.GREEN.color;
    public  static final Color       LIGHT_GREEN           = TileColor.LIGHT_GREEN.color;
    public  static final Color       BLUE                  = TileColor.BLUE.color;
    public  static final Color       DARK_BLUE             = TileColor.DARK_BLUE.color;
    public  static final Color       ORANGE                = TileColor.ORANGE.color;
    public  static final Color       YELLOW_ORANGE         = TileColor.YELLOW_ORANGE.color;
    public  static final Color       YELLOW                = TileColor.YELLOW.color;
    public  static final Color       MAGENTA               = TileColor.MAGENTA.color;
    public  static final int         SHORT_INTERVAL        = 20;
    public  static final int         LONG_INTERVAL         = 1000;
    private static final int         MAX_NO_OF_DECIMALS    = 3;
    private static final String      COUNTRY_PROPERTIES    = "eu/hansolo/tilesfx/lowres.properties";
                    
    private        final TileEvent   EXCEEDED_EVENT        = new TileEvent(EventType.THRESHOLD_EXCEEDED);
    private        final TileEvent   UNDERRUN_EVENT        = new TileEvent(EventType.THRESHOLD_UNDERRUN);
    private        final TileEvent   RECALC_EVENT          = new TileEvent(EventType.RECALC);
    private        final TileEvent   REDRAW_EVENT          = new TileEvent(EventType.REDRAW);
    private        final TileEvent   RESIZE_EVENT          = new TileEvent(EventType.RESIZE);
    private        final TileEvent   VISIBILITY_EVENT      = new TileEvent(EventType.VISIBILITY);
    private        final TileEvent   SECTION_EVENT         = new TileEvent(EventType.SECTION);
    private        final TileEvent   SERIES_EVENT          = new TileEvent(EventType.SERIES);
    private        final TileEvent   DATA_EVENT            = new TileEvent(EventType.DATA);
    private        final TileEvent   ALERT_EVENT           = new TileEvent(EventType.ALERT);
    private        final TileEvent   VALUE_EVENT           = new TileEvent(EventType.VALUE);
    private        final TileEvent   FINISHED_EVENT        = new TileEvent(EventType.FINISHED);
    private        final TileEvent   GRAPHIC_EVENT         = new TileEvent(EventType.GRAPHIC);
    private        final TileEvent   AVERAGING_EVENT       = new TileEvent(EventType.AVERAGING);
    private        final TileEvent   LOCATION_EVENT        = new TileEvent(EventType.LOCATION);
    private        final TileEvent   TRACK_EVENT           = new TileEvent(EventType.TRACK);
    private        final TileEvent   MAP_PROVIDER_EVENT    = new TileEvent(EventType.MAP_PROVIDER);
    private        final TileEvent   TOOLTIP_TEXT_EVENT    = new TileEvent(EventType.TOOLTIP_TEXT);
    
    // Tile events
    private List<TileEventListener>  listenerList          = new CopyOnWriteArrayList<>();
    private List<AlarmEventListener> alarmListenerList     = new CopyOnWriteArrayList<>();
    private List<TimeEventListener>  timeEventListenerList = new CopyOnWriteArrayList<>();


    // Data related
    private              DoubleProperty                         value;
    private              DoubleProperty                         oldValue;      // last value
    private              DoubleProperty                         currentValue;
    private              DoubleProperty                         formerValue;   // last current value
    private              double                                 _minValue;
    private              DoubleProperty                         minValue;
    private              double                                 _maxValue;
    private              DoubleProperty                         maxValue;
    private              double                                 _range;
    private              DoubleProperty                         range;
    private              double                                 _threshold;
    private              DoubleProperty                         threshold;
    private              double                                 _referenceValue;
    private              DoubleProperty                         referenceValue;
    private              boolean                                _autoReferenceValue;
    private              BooleanProperty                        autoReferenceValue;
    private              String                                 _title;
    private              StringProperty                         title;
    private              TextAlignment                          _titleAlignment;
    private              ObjectProperty<TextAlignment>          titleAlignment;
    private              String                                 _description;
    private              StringProperty                         description;
    private              Pos                                    _descriptionAlignment;
    private              ObjectProperty<Pos>                    descriptionAlignment;
    private              String                                 _unit;
    private              StringProperty                         unit;
    private              String                                 _text;
    private              StringProperty                         text;
    private              TextAlignment                          _textAlignment;
    private              ObjectProperty<TextAlignment>          textAlignment;
    private              boolean                                _selected;
    private              BooleanProperty                        selected;
    private              boolean                                _averagingEnabled;
    private              BooleanProperty                        averagingEnabled;
    private              int                                    _averagingPeriod;
    private              IntegerProperty                        averagingPeriod;
    private              MovingAverage                          movingAverage;
    private              ObservableList<Section>                sections;
    private              ObservableList<Series<String, Number>> series;
    private              Properties                             countryProperties;
    private              Map<String, List<CountryPath>>         countryPaths;
    private              List<Stop>                             gradientStops;
    private              ObjectProperty<ZonedDateTime>          time;
    private              LongProperty                           currentTime;
    private              ZoneId                                 zoneId;
    private              int                                    updateInterval;
    private              ObservableList<TimeSection>            timeSections;
    private              LocalTime                              _duration;
    private              ObjectProperty<LocalTime>              duration;
    private              ObservableList<BarChartItem>           barChartItems;
    private              List<LeaderBoardItem>                  leaderBoardItems;
    private              ObjectProperty<Node>                   graphic;
    private              Location                               _currentLocation;
    private              ObjectProperty<Location>               currentLocation;
    private              ObservableList<Location>               poiList;
    private              ObservableList<ChartData>              chartDataList;
    private              List<Location>                         track;
    private              TileColor                              _trackColor;
    private              ObjectProperty<TileColor>              trackColor;
    private              MapProvider                            _mapProvider;
    private              ObjectProperty<MapProvider>            mapProvider;

    // UI related
    private              SkinType                               skinType;
    private              TextSize                               _textSize;
    private              ObjectProperty<TextSize>               textSize;
    private              boolean                                _roundedCorners;
    private              BooleanProperty                        roundedCorners;
    private              boolean                                _startFromZero;
    private              BooleanProperty                        startFromZero;
    private              boolean                                _returnToZero;
    private              BooleanProperty                        returnToZero;
    private              double                                 _minMeasuredValue;
    private              DoubleProperty                         minMeasuredValue;
    private              double                                 _maxMeasuredValue;
    private              DoubleProperty                         maxMeasuredValue;
    private              boolean                                _minMeasuredValueVisible;
    private              BooleanProperty                        minMeasuredValueVisible;
    private              boolean                                _maxMeasuredValueVisible;
    private              BooleanProperty                        maxMeasuredValueVisible;
    private              boolean                                _oldValueVisible;
    private              BooleanProperty                        oldValueVisible;
    private              boolean                                _valueVisible;
    private              BooleanProperty                        valueVisible;
    private              Color                                  _foregroundColor;
    private              ObjectProperty<Color>                  foregroundColor;
    private              Color                                  _backgroundColor;
    private              ObjectProperty<Color>                  backgroundColor;
    private              Color                                  _borderColor;
    private              ObjectProperty<Color>                  borderColor;
    private              double                                 _borderWidth;
    private              DoubleProperty                         borderWidth;
    private              Color                                  _activeColor;
    private              ObjectProperty<Color>                  activeColor;
    private              Color                                  _knobColor;
    private              ObjectProperty<Color>                  knobColor;
    private              boolean                                _animated;
    private              BooleanProperty                        animated;
    private              long                                   animationDuration;
    private              double                                 _startAngle;
    private              DoubleProperty                         startAngle;
    private              double                                 _angleRange;
    private              DoubleProperty                         angleRange;
    private              double                                 _angleStep;
    private              DoubleProperty                         angleStep;
    private              boolean                                _autoScale;
    private              BooleanProperty                        autoScale;
    private              boolean                                _shadowsEnabled;
    private              BooleanProperty                        shadowsEnabled;
    private              Locale                                 _locale;
    private              ObjectProperty<Locale>                 locale;
    private              NumberFormat                           _numberFormat;
    private              ObjectProperty<NumberFormat>           numberFormat;
    private              int                                    _decimals;
    private              IntegerProperty                        decimals;
    private              int                                    _tickLabelDecimals;
    private              IntegerProperty                        tickLabelDecimals;
    private              Color                                  _needleColor;
    private              ObjectProperty<Color>                  needleColor;
    private              Color                                  _barColor;
    private              ObjectProperty<Color>                  barColor;
    private              Color                                  _barBackgroundColor;
    private              ObjectProperty<Color>                  barBackgroundColor;
    private              Color                                  _titleColor;
    private              ObjectProperty<Color>                  titleColor;
    private              Color                                  _descriptionColor;
    private              ObjectProperty<Color>                  descriptionColor;
    private              Color                                  _unitColor;
    private              ObjectProperty<Color>                  unitColor;
    private              Color                                  _valueColor;
    private              ObjectProperty<Color>                  valueColor;
    private              Color                                  _thresholdColor;
    private              ObjectProperty<Color>                  thresholdColor;
    private              boolean                                _checkSectionsForValue;
    private              BooleanProperty                        checkSectionsForValue;
    private              boolean                                _checkThreshold;
    private              BooleanProperty                        checkThreshold;
    private              boolean                                _innerShadowEnabled;
    private              BooleanProperty                        innerShadowEnabled;
    private              boolean                                _thresholdVisible;
    private              BooleanProperty                        thresholdVisible;
    private              boolean                                _averageVisible;
    private              BooleanProperty                        averageVisible;
    private              boolean                                _sectionsVisible;
    private              BooleanProperty                        sectionsVisible;
    private              boolean                                _sectionsAlwaysVisible;
    private              BooleanProperty                        sectionsAlwaysVisible;
    private              boolean                                _sectionTextVisible;
    private              BooleanProperty                        sectionTextVisible;
    private              boolean                                _sectionIconsVisible;
    private              BooleanProperty                        sectionIconsVisible;
    private              boolean                                _highlightSections;
    private              BooleanProperty                        highlightSections;
    private              Orientation                            _orientation;
    private              ObjectProperty<Orientation>            orientation;
    private              boolean                                _keepAspect;
    private              BooleanProperty                        keepAspect;
    private              boolean                                _customFontEnabled;
    private              BooleanProperty                        customFontEnabled;
    private              Font                                   _customFont;
    private              ObjectProperty<Font>                   customFont;
    private              boolean                                _alert;
    private              BooleanProperty                        alert;
    private              String                                 _alertMessage;
    private              StringProperty                         alertMessage;
    private              boolean                                _smoothing;
    private              BooleanProperty                        smoothing;
    // others
    private              double                                 increment;
    private              double                                 originalMinValue;
    private              double                                 originalMaxValue;
    private              double                                 originalThreshold;
    private              Timeline                               timeline;
    private              Instant                                lastCall;
    private              boolean                                withinSpeedLimit;
    private              boolean                                _discreteSeconds;
    private              BooleanProperty                        discreteSeconds;
    private              boolean                                _discreteMinutes;
    private              BooleanProperty                        discreteMinutes;
    private              boolean                                _discreteHours;
    private              BooleanProperty                        discreteHours;
    private              boolean                                _secondsVisible;
    private              BooleanProperty                        secondsVisible;
    private              boolean                                _textVisible;
    private              BooleanProperty                        textVisible;
    private              boolean                                _dateVisible;
    private              BooleanProperty                        dateVisible;
    private              boolean                                _running;
    private              BooleanProperty                        running;
    private              Color                                  _textColor;
    private              ObjectProperty<Color>                  textColor;
    private              Color                                  _dateColor;
    private              ObjectProperty<Color>                  dateColor;
    private              Color                                  _hourTickMarkColor;
    private              ObjectProperty<Color>                  hourTickMarkColor;
    private              Color                                  _minuteTickMarkColor;
    private              ObjectProperty<Color>                  minuteTickMarkColor;
    private              Color                                  _alarmColor;
    private              ObjectProperty<Color>                  alarmColor;
    private              boolean                                _hourTickMarksVisible;
    private              BooleanProperty                        hourTickMarksVisible;
    private              boolean                                _minuteTickMarksVisible;
    private              BooleanProperty                        minuteTickMarksVisible;
    private              Color                                  _hourColor;
    private              ObjectProperty<Color>                  hourColor;
    private              Color                                  _minuteColor;
    private              ObjectProperty<Color>                  minuteColor;
    private              Color                                  _secondColor;
    private              ObjectProperty<Color>                  secondColor;
    private              boolean                                _alarmsEnabled;
    private              BooleanProperty                        alarmsEnabled;
    private              boolean                                _alarmsVisible;
    private              BooleanProperty                        alarmsVisible;
    private              ObservableList<Alarm>                  alarms;
    private              List<Alarm>                            alarmsToRemove;
    private              boolean                                _strokeWithGradient;
    private              BooleanProperty                        strokeWithGradient;
    private              DarkSky                                darkSky;
    private              String                                 _tooltipText;
    private              StringProperty                         tooltipText;

    private volatile     ScheduledFuture<?>                     periodicTickTask;
    private static       ScheduledExecutorService               periodicTickExecutorService;


    // ******************** Constructors **************************************
    public Tile() {
        this(SkinType.GAUGE, ZonedDateTime.now());
    }
    public Tile(final SkinType SKIN) {
        this(SKIN, ZonedDateTime.now());
    }
    public Tile(final SkinType SKIN_TYPE, final ZonedDateTime TIME) {
        setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        skinType = SKIN_TYPE;
        getStyleClass().add("tile");

        init(TIME);
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void init(final ZonedDateTime TIME) {
        _minValue                           = 0;
        _maxValue                           = 100;
        value                               = new DoublePropertyBase(_minValue) {
            private void update() {
                final double VALUE = get();
                withinSpeedLimit = !(Instant.now().minusMillis(getAnimationDuration()).isBefore(lastCall));
                lastCall = Instant.now();
                if (isAnimated() && withinSpeedLimit) {
                    long animationDuration = isReturnToZero() ? (long) (0.2 * getAnimationDuration()) : getAnimationDuration();
                    timeline.stop();

                    final KeyValue KEY_VALUE = new KeyValue(currentValue, VALUE, Interpolator.SPLINE(0.5, 0.4, 0.4, 1.0));
                    final KeyFrame KEY_FRAME = new KeyFrame(Duration.millis(animationDuration), KEY_VALUE);
                    timeline.getKeyFrames().setAll(KEY_FRAME);
                    timeline.play();
                } else {
                    currentValue.set(VALUE);
                    fireTileEvent(FINISHED_EVENT);
                }
                if (isAveragingEnabled()) { movingAverage.addData(new TimeData(VALUE)); }
            }
            @Override protected void invalidated() { update(); }
            @Override public void set(final double VALUE) {
                // ATTENTION There is an optimization in the properties so that properties
                // only get invalid if the the new value is different from the old value
                if (Helper.equals(VALUE, getFormerValue())) { update(); }
                super.set(VALUE);
                fireTileEvent(VALUE_EVENT);
            }
            @Override public Object getBean() { return Tile.this; }
            @Override public String getName() { return "value"; }
        };
        oldValue                            = new SimpleDoubleProperty(Tile.this, "oldValue", value.get());
        currentValue                        = new DoublePropertyBase(value.get()) {
            @Override protected void invalidated() {
                final double VALUE = get();
                if (isCheckThreshold()) {
                    double thrshld = getThreshold();
                    if (formerValue.get() < thrshld && VALUE > thrshld) {
                        fireTileEvent(EXCEEDED_EVENT);
                    } else if (formerValue.get() > thrshld && VALUE < thrshld) {
                        fireTileEvent(UNDERRUN_EVENT);
                    }
                }
                if (VALUE < getMinMeasuredValue()) {
                    setMinMeasuredValue(VALUE);
                } else if (VALUE > getMaxMeasuredValue()) {
                    setMaxMeasuredValue(VALUE);
                }
                formerValue.set(VALUE);
            }
            @Override public void set(final double VALUE) { super.set(VALUE); }
            @Override public Object getBean() { return Tile.this; }
            @Override public String getName() { return "currentValue";}
        };
        formerValue                         = new SimpleDoubleProperty(Tile.this, "formerValue", value.get());
        _range                              = _maxValue - _minValue;
        _threshold                          = _maxValue;
        _referenceValue                     = _minValue;
        _autoReferenceValue                 = true;
        time                                = new ObjectPropertyBase<ZonedDateTime>(TIME) {
            @Override protected void invalidated() {
                if (!isRunning() && isAnimated()) {
                    long animationDuration = getAnimationDuration();
                    timeline.stop();
                    final KeyValue KEY_VALUE = new KeyValue(currentTime, TIME.toEpochSecond());
                    final KeyFrame KEY_FRAME = new KeyFrame(javafx.util.Duration.millis(animationDuration), KEY_VALUE);
                    timeline.getKeyFrames().setAll(KEY_FRAME);
                    timeline.setOnFinished(e -> fireTileEvent(FINISHED_EVENT));
                    timeline.play();
                } else {
                    currentTime.set(TIME.toEpochSecond());
                    fireTileEvent(FINISHED_EVENT);
                }
            }
            @Override public Object getBean() { return Tile.this; }
            @Override public String getName() { return "time"; }
        };
        currentTime                         = new LongPropertyBase(time.get().toEpochSecond()) {
            @Override protected void invalidated() {}
            @Override public Object getBean() { return Tile.this; }
            @Override public String getName() { return "currentTime"; }
        };
        zoneId                              = time.get().getZone();
        _title                              = "";
        _titleAlignment                     = TextAlignment.LEFT;
        _description                        = "";
        _descriptionAlignment               = Pos.TOP_RIGHT;
        _unit                               = "";
        _selected                           = false;
        _text                               = "";
        _textAlignment                      = TextAlignment.LEFT;
        _averagingEnabled                   = false;
        _averagingPeriod                    = 10;
        _duration                           = LocalTime.of(1, 0);
        _currentLocation                    = new Location(0, 0);
        poiList                             = FXCollections.observableArrayList();
        chartDataList                       = FXCollections.observableArrayList();
        movingAverage                       = new MovingAverage(_averagingPeriod);
        sections                            = FXCollections.observableArrayList();
        series                              = FXCollections.observableArrayList();
        timeSections                        = FXCollections.observableArrayList();
        alarms                              = FXCollections.observableArrayList();
        alarmsToRemove                      = new ArrayList<>();
        barChartItems                       = FXCollections.observableArrayList();
        track                               = new ArrayList<>();
        _trackColor                         = TileColor.BLUE;
        _mapProvider                        = MapProvider.BW;
        leaderBoardItems                    = new ArrayList<>();
        gradientStops                       = new ArrayList<>(4);

        _textSize                           = TextSize.NORMAL;
        _roundedCorners                     = true;
        _startFromZero                      = false;
        _returnToZero                       = false;
        _minMeasuredValue                   = _maxValue;
        _maxMeasuredValue                   = _minValue;
        _minMeasuredValueVisible            = false;
        _maxMeasuredValueVisible            = false;
        _oldValueVisible                    = false;
        _valueVisible                       = true;
        _foregroundColor                    = FOREGROUND;
        _backgroundColor                    = BACKGROUND;
        _borderColor                        = Color.TRANSPARENT;
        _borderWidth                        = 1;
        _knobColor                          = FOREGROUND;
        _activeColor                        = BLUE;
        _animated                           = false;
        animationDuration                   = 800;
        _startAngle                         = 0;
        _angleRange                         = 180;
        _angleStep                          = _angleRange / _range;
        _autoScale                          = true;
        _shadowsEnabled                     = false;
        _locale                             = Locale.US;
        _numberFormat                       = NumberFormat.getInstance(_locale);
        _decimals                           = 1;
        _tickLabelDecimals                  = 1;
        _needleColor                        = FOREGROUND;
        _hourColor                          = FOREGROUND;
        _minuteColor                        = FOREGROUND;
        _secondColor                        = FOREGROUND;
        _barColor                           = BLUE;
        _barBackgroundColor                 = BACKGROUND;
        _titleColor                         = FOREGROUND;
        _descriptionColor                   = FOREGROUND;
        _unitColor                          = FOREGROUND;
        _valueColor                         = FOREGROUND;
        _textColor                          = FOREGROUND;
        _dateColor                          = FOREGROUND;
        _hourTickMarkColor                  = FOREGROUND;
        _minuteTickMarkColor                = FOREGROUND;
        _alarmColor                         = FOREGROUND;
        _thresholdColor                     = RED;
        _checkSectionsForValue              = false;
        _checkThreshold                     = false;
        _innerShadowEnabled                 = false;
        _thresholdVisible                   = false;
        _averageVisible                     = false;
        _sectionsVisible                    = false;
        _sectionsAlwaysVisible              = false;
        _sectionTextVisible                 = false;
        _sectionIconsVisible                = false;
        _highlightSections                  = false;
        _orientation                        = Orientation.HORIZONTAL;
        _keepAspect                         = true;
        _customFontEnabled                  = false;
        _customFont                         = Fonts.latoRegular(12);
        _alert                              = false;
        _alertMessage                       = "";
        _smoothing                          = false;
        _secondsVisible                     = false;
        _discreteSeconds                    = true;
        _discreteMinutes                    = true;
        _discreteHours                      = false;
        _textVisible                        = true;
        _dateVisible                        = false;
        _running                            = false;
        _hourTickMarksVisible               = true;
        _minuteTickMarksVisible             = true;
        _alarmsEnabled                      = false;
        _alarmsVisible                      = false;
        updateInterval                      = LONG_INTERVAL;
        increment                           = 1;
        originalMinValue                    = -Double.MAX_VALUE;
        originalMaxValue                    = Double.MAX_VALUE;
        originalThreshold                   = Double.MAX_VALUE;
        lastCall                            = Instant.now();
        timeline                            = new Timeline();
        timeline.setOnFinished(e -> {
            if (isReturnToZero() && !Helper.equals(currentValue.get(), 0.0)) {
                final KeyValue KEY_VALUE2 = new KeyValue(value, 0, Interpolator.SPLINE(0.5, 0.4, 0.4, 1.0));
                final KeyFrame KEY_FRAME2 = new KeyFrame(Duration.millis((long) (0.8 * getAnimationDuration())), KEY_VALUE2);
                timeline.getKeyFrames().setAll(KEY_FRAME2);
                timeline.play();
            }
            fireTileEvent(FINISHED_EVENT);
        });
    }

    private void registerListeners() {
        disabledProperty().addListener(o -> setOpacity(isDisabled() ? 0.4 : 1));
        valueProperty().addListener((o, ov, nv) -> oldValue.set(ov.doubleValue()));
    }


    // ******************** Data related methods ******************************

    /**
     * Returns the value of the Tile. If animated == true this value represents
     * the value at the end of the animation. Where currentValue represents the
     * current value during the animation.
     *
     * @return the value of the gauge
     */
    public double getValue() { return value.get(); }
    /**
     * Sets the value of the Tile to the given double. If animated == true this
     * value will be the end value after the animation is finished.
     *
     * @param VALUE
     */
    public void setValue(final double VALUE) { value.set(VALUE); }
    public DoubleProperty valueProperty() { return value; }

    /**
     * Returns the current value of the Tile. If animated == true this value
     * represents the current value during the animation. Otherwise it's returns
     * the same value as the getValue() method.
     *
     * @return the current value of the gauge
     */
    public double getCurrentValue() { return currentValue.get(); }
    public ReadOnlyDoubleProperty currentValueProperty() { return currentValue; }

    /**
     * Returns the last value of the Tile. This will not be the last value during
     * an animation but the final last value after the animation was finished.
     * If you need to get the last value during an animation you should use
     * formerValue instead.
     *
     * @return the last value of the gauge
     */
    public double getOldValue() { return oldValue.get(); }
    public ReadOnlyDoubleProperty oldValueProperty() { return oldValue; }

    /**
     * Returns the last value of the Tile. This will be the last value during
     * an animation.
     * If you need to get the last value after the animation is finished or if
     * you don't use animation at all (when using real values) you should use
     * oldValue instead.
     *
     * @return the last value of the gauge during an animation
     */
    public double getFormerValue() { return formerValue.get(); }
    public ReadOnlyDoubleProperty formerValueProperty() { return formerValue; }

    /**
     * Returns the minimum value of the scale. This value represents the lower
     * limit of the visible gauge values.
     *
     * @return the minimum value of the gauge scale
     */
    public double getMinValue() { return null == minValue ? _minValue : minValue.get(); }
    /**
     * Sets the minimum value of the gauge scale to the given value
     *
     * @param VALUE
     */
    public void setMinValue(final double VALUE) {
        if (Status.RUNNING == timeline.getStatus()) { timeline.jumpTo(Duration.ONE); }
        if (null == minValue) {
            if (VALUE > getMaxValue()) { setMaxValue(VALUE); }
            _minValue = clamp(-Double.MAX_VALUE, getMaxValue(), VALUE);
            setRange(getMaxValue() - _minValue);
            if (Helper.equals(originalMinValue, -Double.MAX_VALUE)) originalMinValue = _minValue;
            if (isStartFromZero() && _minValue < 0) setValue(0);
            if (Helper.equals(originalThreshold, getThreshold())) { setThreshold(clamp(_minValue, getMaxValue(), originalThreshold)); }
            fireTileEvent(RECALC_EVENT);
            if (!valueProperty().isBound()) Tile.this.setValue(clamp(getMinValue(), getMaxValue(), Tile.this.getValue()));
        } else {
            minValue.set(VALUE);
        }
    }
    public DoubleProperty minValueProperty() {
        if (null == minValue) {
            minValue = new DoublePropertyBase(_minValue) {
                @Override protected void invalidated() {
                    final double VALUE = get();
                    if (VALUE > getMaxValue()) setMaxValue(VALUE);
                    setRange(getMaxValue() - VALUE);
                    if (Helper.equals(originalMinValue, -Double.MAX_VALUE)) originalMinValue = VALUE;
                    if (isStartFromZero() && _minValue < 0) Tile.this.setValue(0);
                    if (Helper.lessThan(originalThreshold, getThreshold())) { setThreshold(clamp(VALUE, getMaxValue(), originalThreshold)); }
                    fireTileEvent(RECALC_EVENT);
                    if (!valueProperty().isBound()) Tile.this.setValue(clamp(getMinValue(), getMaxValue(), Tile.this.getValue()));
                }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "minValue";}
            };
        }
        return minValue;
    }

    /**
     * Returns the maximum value of the scale. This value represents the upper limit
     * of the visible gauge values.
     *
     * @return the maximum value of the gauge scale
     */
    public double getMaxValue() { return null == maxValue ? _maxValue : maxValue.get(); }
    /**
     * Sets the maximum value of the gauge scale to the given value
     *
     * @param VALUE
     */
    public void setMaxValue(final double VALUE) {
        if (Status.RUNNING == timeline.getStatus()) { timeline.jumpTo(Duration.ONE); }
        if (null == maxValue) {
            if (VALUE < getMinValue()) { setMinValue(VALUE); }
            _maxValue = clamp(getMinValue(), Double.MAX_VALUE, VALUE);
            setRange(_maxValue - getMinValue());
            if (Helper.equals(originalMaxValue, Double.MAX_VALUE)) originalMaxValue = _maxValue;
            if (Helper.biggerThan(originalThreshold, getThreshold())) { setThreshold(clamp(getMinValue(), _maxValue, originalThreshold)); }
            fireTileEvent(RECALC_EVENT);
            if (!valueProperty().isBound()) Tile.this.setValue(clamp(getMinValue(), getMaxValue(), Tile.this.getValue()));
        } else {
            maxValue.set(VALUE);
        }
    }
    public DoubleProperty maxValueProperty() {
        if (null == maxValue) {
            maxValue = new DoublePropertyBase(_maxValue) {
                @Override protected void invalidated() {
                    final double VALUE = get();
                    if (VALUE < getMinValue()) setMinValue(VALUE);
                    setRange(VALUE - getMinValue());
                    if (Helper.equals(originalMaxValue, Double.MAX_VALUE)) originalMaxValue = VALUE;
                    if (Helper.biggerThan(originalThreshold, getThreshold())) { setThreshold(clamp(getMinValue(), VALUE, originalThreshold)); }
                    fireTileEvent(RECALC_EVENT);
                    if (!valueProperty().isBound()) Tile.this.setValue(clamp(getMinValue(), getMaxValue(), Tile.this.getValue()));
                }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "maxValue"; }
            };
        }
        return maxValue;
    }

    /**
     * Always returns the range of the gauge scale (maxValue - minValue).
     * This value will be automatically calculated each time
     * the min- or maxValue will change.
     *
     * @return the range of the gauge scale
     */
    public double getRange() { return null == range ? _range : range.get(); }
    /**
     * This is a private method that sets the range to the given value
     * which is always (maxValue - minValue).
     *
     * @param RANGE
     */
    private void setRange(final double RANGE) {
        if (null == range) {
            _range = RANGE;
            setAngleStep(getAngleRange() / RANGE);
        } else {
            range.set(RANGE);
        }
    }
    public ReadOnlyDoubleProperty rangeProperty() {
        if (null == range) {
            range = new DoublePropertyBase((getMaxValue() - getMinValue())) {
                @Override protected void invalidated() { setAngleStep(getAngleRange() / get()); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "range"; }
            };
        }
        return range;
    }

    /**
     * Returns the threshold value that can be used to visualize a
     * threshold value on the scale. There are also events that will
     * be fired if the threshold was exceeded or underrun.
     * The value will be clamped to range of the gauge.
     *
     * @return the threshold value of the gauge
     */
    public double getThreshold() { return null == threshold ? _threshold : threshold.get(); }
    /**
     * Sets the threshold of the gauge to the given value. The value
     * will be clamped to the range of the gauge.
     *
     * @param THRESHOLD
     */
    public void setThreshold(final double THRESHOLD) {
        originalThreshold = THRESHOLD;
        if (null == threshold) {
            _threshold = clamp(getMinValue(), getMaxValue(), THRESHOLD);
            fireTileEvent(RESIZE_EVENT);
        } else {
            threshold.set(THRESHOLD);
        }
    }
    public DoubleProperty tresholdProperty() {
        if (null == threshold) {
            threshold = new DoublePropertyBase(_threshold) {
                @Override protected void invalidated() {
                    final double THRESHOLD = get();
                    if (THRESHOLD < getMinValue() || THRESHOLD > getMaxValue()) set(clamp(getMinValue(), getMaxValue(), THRESHOLD));
                    fireTileEvent(RESIZE_EVENT);
                }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "threshold"; }
            };
        }
        return threshold;
    }

    /**
     * Returns the reference value that will be used in the HighLowTileSkin
     * to visualize the increase or decrease of the current value compared
     * to the reference value;
     * @return the reference value that will be used in the HighLowTileSkin
     */
    public double getReferenceValue() { return null == referenceValue ? _referenceValue : referenceValue.get(); }
    /**
     * Defines the reference value that will be used in the HighLowTileSkin
     * @param VALUE
     */
    public void setReferenceValue(final double VALUE) {
        if (null == referenceValue) {
            _referenceValue = VALUE;
            fireTileEvent(REDRAW_EVENT);
        } else {
            referenceValue.set(VALUE);
        }
    }
    public DoubleProperty referenceValueProperty() {
        if (null == referenceValue) {
            referenceValue = new DoublePropertyBase(_referenceValue) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "referenceValue"; }
            };
        }
        return referenceValue;
    }

    /**
     * Returns true if the reference value for the StockTileSkin will be calculated automatically
     * @return true if the reference value for the StockTileSkin will be calculated automatically
     */
    public boolean isAutoReferenceValue() { return null == autoReferenceValue ? _autoReferenceValue : autoReferenceValue.get(); }
    /**
     * Defines if the reference value for the StockTileSkin should be calculated automatically
     * @param AUTO_REFERENCE_VALUE
     */
    public void setAutoReferenceValue(final boolean AUTO_REFERENCE_VALUE) {
        if (null == autoReferenceValue) {
            _autoReferenceValue = AUTO_REFERENCE_VALUE;
        } else {
            autoReferenceValue.set(AUTO_REFERENCE_VALUE);
        }
    }
    public BooleanProperty autoReferenceValueProperty() {
        if (null == autoReferenceValue) {
            autoReferenceValue = new BooleanPropertyBase(_autoReferenceValue) {
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "autoReferenceValue"; }
            };
        }
        return autoReferenceValue;
    }

    /**
     * Returns the title of the gauge. This title will usually
     * only be visible if it is not empty.
     *
     * @return the title of the gauge
     */
    public String getTitle() { return null == title ? _title : title.get(); }
    /**
     * Sets the title of the gauge. This title will only be visible
     * if it is not empty.
     *
     * @param TITLE
     */
    public void setTitle(final String TITLE) {
        if (null == title) {
            _title = TITLE;
            fireTileEvent(VISIBILITY_EVENT);
        } else {
            title.set(TITLE);
        }
    }
    public StringProperty titleProperty() {
        if (null == title) {
            title  = new StringPropertyBase(_title) {
                @Override protected void invalidated() { fireTileEvent(VISIBILITY_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "title"; }
            };
            _title = null;
        }
        return title;
    }

    /**
     * Returns the alignment that will be used to align the title
     * in the Tile. Keep in mind that this property will not be used
     * by every skin
     * @return the alignment of the title
     */
    public TextAlignment getTitleAlignment() { return null == titleAlignment ? _titleAlignment : titleAlignment.get(); }
    /**
     * Defines the alignment that will be used to align the title
     * in the Tile. Keep in mind that this property will not be used
     * by every skin.
     * @param ALIGNMENT
     */
    public void setTitleAlignment(final TextAlignment ALIGNMENT) {
        if (null == titleAlignment) {
            _titleAlignment = ALIGNMENT;
            fireTileEvent(RESIZE_EVENT);
        } else {
            titleAlignment.set(ALIGNMENT);
        }
    }
    public ObjectProperty<TextAlignment> titleAlignmentProperty() {
        if (null == titleAlignment) {
            titleAlignment = new ObjectPropertyBase<TextAlignment>(_titleAlignment) {
                @Override protected void invalidated() { fireTileEvent(RESIZE_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "titleAlignment"; }
            };
            _titleAlignment = null;
        }
        return titleAlignment;
    }


    /**
     * Returns the description text of the gauge. This description text will usually
     * only be visible if it is not empty.
     *
     * @return the description text of the gauge
     */
    public String getDescription() { return null == description ? _description : description.get(); }
    /**
     * Sets the description text of the gauge. This description text will usually
     * only be visible if it is not empty.
     *
     * @param DESCRIPTION
     */
    public void setDescription(final String DESCRIPTION) {
        if (null == description) {
            _description = DESCRIPTION;
            fireTileEvent(VISIBILITY_EVENT);
        } else {
            description.set(DESCRIPTION);
        }
    }
    public StringProperty descriptionProperty() {
        if (null == description) {
            description = new StringPropertyBase(_description) {
                @Override protected void invalidated() { fireTileEvent(VISIBILITY_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "description"; }
            };
            _description = null;
        }
        return description;
    }

    /**
     * Returns the current alignment of the description text (esp. in TextTileSkin)
     * @return the current alignment of the description text (esp. in TextTileSkin)
     */
    public Pos getDescriptionAlignment() { return null == descriptionAlignment ? _descriptionAlignment : descriptionAlignment.get(); }
    /**
     * Defines the alignment of the description text (esp. for the TextTileSkin).
     * Valid values are TOP_LEFT and TOP_RIGHT
     * @param ALIGNMENT
     */
    public void setDescriptionAlignment(final Pos ALIGNMENT) {
        if (null == descriptionAlignment) {
            _descriptionAlignment = ALIGNMENT;
            fireTileEvent(REDRAW_EVENT);
        } else {
            descriptionAlignment.set(ALIGNMENT);
        }
    }
    public ObjectProperty<Pos> descriptionAlignmentProperty() {
        if (null == descriptionAlignment) {
            descriptionAlignment = new ObjectPropertyBase<Pos>(_descriptionAlignment) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "descriptionAlignment"; }
            };
            _descriptionAlignment = null;
        }
        return descriptionAlignment;
    }

    /**
     * Returns the unit of the gauge. This unit will usually only
     * be visible if it is not empty.
     *
     * @return the unit of the gauge
     */
    public String getUnit() { return null == unit ? _unit : unit.get(); }
    /**
     * Sets the unit of the gauge. This unit will usually only be
     * visible if it is not empty.
     *
     * @param UNIT
     */
    public void setUnit(final String UNIT) {
        if (null == unit) {
            _unit = UNIT;
            fireTileEvent(VISIBILITY_EVENT);
        } else {
            unit.set(UNIT);
        }
    }
    public StringProperty unitProperty() {
        if (null == unit) {
            unit  = new StringPropertyBase(_unit) {
                @Override protected void invalidated() { fireTileEvent(VISIBILITY_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "unit"; }
            };
            _unit = null;
        }
        return unit;
    }

    /**
     * Returns true if the switch in the SwitchTileSkin is selected
     * @return true if the switch in the SwitchTileSkin is selected
     */
    public boolean isSelected() { return null == selected ? _selected : selected.get(); }
    /**
     * Defines if the switch in the SwitchTileSkin is selected
     * @param SELECTED
     */
    public void setSelected(final boolean SELECTED) {
        if (null == selected) {
            _selected = SELECTED;
            fireTileEvent(REDRAW_EVENT);
        } else {
            selected.set(SELECTED);
        }
    }
    public BooleanProperty selectedProperty() {
        if (null == selected) {
            selected = new BooleanPropertyBase(_selected) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "selected"; }
            };
        }
        return selected;
    }

    /**
     * Returns the moving average object
     * @return the moving average object
     */
    public MovingAverage getMovingAverage() { return movingAverage; }

    /**
     * Returns true if the averaging functionality is enabled.
     * @return true if the averaging functionality is enabled
     */
    public boolean isAveragingEnabled() { return null == averagingEnabled ? _averagingEnabled : averagingEnabled.get(); }
    /**
     * Defines if the averaging functionality will be enabled.
     */
    public void setAveragingEnabled(final boolean ENABLED) {
        if (null == averagingEnabled) {
            _averagingEnabled = ENABLED;
            fireTileEvent(REDRAW_EVENT);
        } else {
            averagingEnabled.set(ENABLED);
        }
    }
    public BooleanProperty averagingEnabledProperty() {
        if (null == averagingEnabled) {
            averagingEnabled = new BooleanPropertyBase(_averagingEnabled) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "averagingEnabled"; }
            };
        }
        return averagingEnabled;
    }

    /**
     * Returns the number of values that should be used for
     * the averaging of values. The value must be in the
     * range of 1 - 1000.
     * @return the number of values used for averaging
     */
    public int getAveragingPeriod() { return null == averagingPeriod ? _averagingPeriod : averagingPeriod.get(); }
    /**
     * Defines the number values that should be used for
     * the averaging of values. The value must be in the
     * range of 1 - 1000.
     * @param PERIOD
     */
    public void setAveragingPeriod(final int PERIOD) {
        if (null == averagingPeriod) {
            _averagingPeriod = Helper.clamp(0, MAX_PERIOD, PERIOD);
            movingAverage.setPeriod(_averagingPeriod); // MAX 1000 values
            fireTileEvent(AVERAGING_EVENT);
        } else {
            averagingPeriod.set(Helper.clamp(0, MAX_PERIOD, PERIOD));
        }
    }
    public IntegerProperty averagingPeriodProperty() {
        if (null == averagingPeriod) {
            averagingPeriod = new IntegerPropertyBase(_averagingPeriod) {
                @Override protected void invalidated() {
                    movingAverage.setPeriod(get());
                    fireTileEvent(AVERAGING_EVENT);
                }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "averagingPeriod"; }
            };
        }
        return averagingPeriod;
    }

    /**
     * Returns a deep copy of the current list of Data objects that will
     * be used to calculate the moving average.
     * @return the current list of Data objects used for the moving average
     */
    public Queue<TimeData> getAveragingWindow() { return movingAverage.getWindow(); }

    /**
     * Returns the moving average over the number of values
     * defined by averagingPeriod.
     * @return the moving the average over the number of values defined by averagingPeriod
     */
    public double getAverage() { return movingAverage.getAverage(); }
    /**
     * Returns the moving average over the given duration.
     * @param DURATION
     * @return the moving average over the given duration
     */
    public double getTimeBasedAverageOf(final java.time.Duration DURATION) { return movingAverage.getTimeBasedAverageOf(DURATION); }

    /**
     * Returns a duration that will be used in the TimeTileSkin
     * @return a duration that will be used in the TimeTileSkin
     */
    public LocalTime getDuration() { return null == duration ? _duration : duration.get(); }
    /**
     * Defines a duration that is used in the TimeTileSkin
     * @param DURATION
     */
    public void setDuration(final LocalTime DURATION) {
        if (null == duration) {
            _duration = DURATION;
            fireTileEvent(REDRAW_EVENT);
        } else {
            duration.set(DURATION);
        }
    }
    public ObjectProperty<LocalTime> durationProperty() {
        if (null == duration) {
            duration = new ObjectPropertyBase<LocalTime>(_duration) {
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "duration"; }
            };
            _duration = null;
        }
        return duration;
    }

    /**
     * Returns an observable list of Section objects. The sections
     * will be used to colorize areas with a special meaning such
     * as the red area in a rpm gauge. Sections in the Medusa library
     * usually are less eye-catching than Areas.
     *
     * @return an observable list of Section objects
     */
    public ObservableList<Section> getSections() { return sections; }
    /**
     * Sets the sections to the given list of Section objects. The
     * sections will be used to colorize areas with a special
     * meaning such as the red area in a rpm gauge.
     * Sections in the Medusa library
     * usually are less eye-catching than Areas.
     *
     * @param SECTIONS
     */
    public void setSections(final List<Section> SECTIONS) {
        sections.setAll(SECTIONS);
        sections.sort(new SectionComparator());
        fireTileEvent(SECTION_EVENT);
    }
    /**
     * Sets the sections to the given array of Section objects. The
     * sections will be used to colorize areas with a special
     * meaning such as the red area in a rpm gauge.
     *
     * @param SECTIONS
     */
    public void setSections(final Section... SECTIONS) { setSections(Arrays.asList(SECTIONS)); }
    /**
     * Adds the given Section to the list of sections.
     * Sections in the Medusa library
     * usually are less eye-catching than Areas.
     *
     * @param SECTION
     */
    public void addSection(final Section SECTION) {
        if (null == SECTION) return;
        sections.add(SECTION);
        sections.sort(new SectionComparator());
        fireTileEvent(SECTION_EVENT);
    }
    /**
     * Removes the given Section from the list of sections.
     * Sections in the Medusa library
     * usually are less eye-catching than Areas.
     *
     * @param SECTION
     */
    public void removeSection(final Section SECTION) {
        if (null == SECTION) return;
        sections.remove(SECTION);
        sections.sort(new SectionComparator());
        fireTileEvent(SECTION_EVENT);
    }
    /**
     * Clears the list of sections.
     */
    public void clearSections() {
        sections.clear();
        fireTileEvent(SECTION_EVENT);
    }

    public Collection<Series<String, Number>> getSeries() { return series; }
    public void setSeries(final List<Series<String, Number>> SERIES) {
        series.setAll(SERIES);
        fireTileEvent(SERIES_EVENT);
    }
    public void setSeries(final Series<String, Number>... SERIES) { setSeries(Arrays.asList(SERIES)); }
    public void addSeries(final Series<String, Number> SERIES) {
        if (null == SERIES) return;
        series.add(SERIES);
        fireTileEvent(SERIES_EVENT);
    }
    public void removeSeries(final Series<String, Number> SERIES) {
        if (null == SERIES) return;
        series.remove(SERIES);
        fireTileEvent(SERIES_EVENT);
    }
    public void clearSeries() {
        series.clear();
        fireTileEvent(SERIES_EVENT);
    }

    public ObservableList<BarChartItem> getBarChartItems() { return barChartItems; }
    public void setBarChartItems(final List<BarChartItem> ITEMS) {
        barChartItems.setAll(ITEMS);
        fireTileEvent(DATA_EVENT);
    }
    public void setBarChartItems(final BarChartItem... ITEMS) { setBarChartItems(Arrays.asList(ITEMS)); }
    public void addBarChartItem(final BarChartItem ITEM) {
        if (null == ITEM) return;
        barChartItems.add(ITEM);
        fireTileEvent(DATA_EVENT);
    }
    public void removeBarChartItem(final BarChartItem ITEM) {
        if (null == ITEM) return;
        barChartItems.remove(ITEM);
        fireTileEvent(DATA_EVENT);
    }
    public void clearBarChartItems() {
        barChartItems.clear();
        fireTileEvent(DATA_EVENT);
    }

    public List<LeaderBoardItem> getLeaderBoardItems() { return leaderBoardItems; }
    public void setLeaderBoardItems(final List<LeaderBoardItem> ITEMS) {
        leaderBoardItems.clear();
        leaderBoardItems.addAll(ITEMS);
        fireTileEvent(DATA_EVENT);
    }
    public void setLeaderBoardItems(final LeaderBoardItem... ITEMS) { setLeaderBoardItems(Arrays.asList(ITEMS)); }
    public void addLeaderBoardItem(final LeaderBoardItem ITEM) {
        if (null == ITEM) return;
        leaderBoardItems.add(ITEM);
        fireTileEvent(DATA_EVENT);
    }
    public void removeLeaderBoardItem(final LeaderBoardItem ITEM) {
        if (null == ITEM) return;
        leaderBoardItems.remove(ITEM);
        fireTileEvent(DATA_EVENT);
    }
    public void clearLeaderBoardItems() {
        leaderBoardItems.clear();
        fireTileEvent(DATA_EVENT);
    }

    public List<Stop> getGradientStops() { return gradientStops; }
    public void setGradientStops(final Stop... STOPS) {
        setGradientStops(Arrays.asList(STOPS));
    }
    public void setGradientStops(final List<Stop> STOPS) {
        gradientStops.clear();
        gradientStops.addAll(STOPS);
        fireTileEvent(REDRAW_EVENT);
    }

    /**
     * Returns an optional node that can be used in combination with the
     * CustomTileSkin
     * @return an optional node that can be used in combination with the CustomTileSkin
     */
    public Node getGraphic() { return null == graphic ? null : graphic.get(); }
    /**
     * Defines an optional node that can be used in combination with the
     * CustomTileSkin.
     * @param GRAPHIC
     */
    public void setGraphic(final Node GRAPHIC) { graphicProperty().set(GRAPHIC); }
    public ObjectProperty<Node> graphicProperty() {
        if (null == graphic) {
            graphic = new ObjectPropertyBase<Node>() {
                @Override protected void invalidated() { fireTileEvent(GRAPHIC_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "graphic"; }
            };
        }
        return graphic;
    }

    public Location getCurrentLocation() { return null == currentLocation ? _currentLocation : currentLocation.get(); }
    public void setCurrentLocation(final Location LOCATION) {
        if (null == currentLocation) {
            _currentLocation = LOCATION;
            fireTileEvent(LOCATION_EVENT);
        } else {
            currentLocation.set(LOCATION);
        }
    }
    public ObjectProperty<Location> currentLocationProperty() {
        if (null == currentLocation) {
            currentLocation = new ObjectPropertyBase<Location>(_currentLocation) {
                @Override protected void invalidated() { fireTileEvent(LOCATION_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "currentLocation"; }
            };
            _currentLocation = null;
        }
        return currentLocation;
    }
    public void updateLocation(final double LATITUDE, final double LONGITUDE) {
        getCurrentLocation().set(LATITUDE, LONGITUDE);
        fireTileEvent(LOCATION_EVENT);
    }

    public ObservableList<Location> getPoiList() { return poiList; }
    public void setPoiList(final List<Location> POI_LIST) {
        poiList.clear();
        poiList.addAll(POI_LIST);
        fireTileEvent(LOCATION_EVENT);
    }
    public void setPoiLocations(final Location... LOCATIONS) { setPoiList(Arrays.asList(LOCATIONS)); }
    public void addPoiLocation(final Location LOCATION) {
        if (null == LOCATION) return;
        poiList.add(LOCATION);
        fireTileEvent(LOCATION_EVENT);
    }
    public void removePoiLocation(final Location LOCATION) {
        if (null == LOCATION) return;
        poiList.remove(LOCATION);
        fireTileEvent(LOCATION_EVENT);
    }
    public void clearPoiLocations() {
        poiList.clear();
        fireTileEvent(DATA_EVENT);
    }

    public List<Location> getTrack() { return track; }
    public void setTrack(final Location... LOCATIONS) {
        setTrack(Arrays.asList(LOCATIONS));
    }
    public void setTrack(final List<Location> LOCATIONS) {
        track.clear();
        track.addAll(LOCATIONS);
        fireTileEvent(TRACK_EVENT);
    }
    public void clearTrack() {
        track.clear();
        fireTileEvent(TRACK_EVENT);
    }

    public TileColor getTrackColor() { return null == trackColor ? _trackColor : trackColor.get(); }
    public void setTrackColor(final TileColor COLOR) {
        if (null == trackColor) {
            _trackColor = COLOR;
            fireTileEvent(REDRAW_EVENT);
        } else {
            trackColor.set(COLOR);
        }
    }
    public ObjectProperty<TileColor> trackColorProperty() {
        if (null == trackColor) {
            trackColor = new ObjectPropertyBase<TileColor>(_trackColor) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "trackColor"; }
            };
            _trackColor = null;
        }
        return trackColor;
    }

    public MapProvider getMapProvider() { return null == mapProvider ? _mapProvider : mapProvider.get(); }
    public void setMapProvider(final MapProvider PROVIDER) {
        if (null == mapProvider) {
            _mapProvider = PROVIDER;
            fireTileEvent(MAP_PROVIDER_EVENT);
        } else {
            mapProvider.set(PROVIDER);
        }
    }
    public ObjectProperty<MapProvider> mapProviderProperty() {
        if (null == mapProvider) {
            mapProvider = new ObjectPropertyBase<MapProvider>(_mapProvider) {
                @Override protected void invalidated() { fireTileEvent(MAP_PROVIDER_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "mapProvider"; }
            };
            _mapProvider = null;
        }
        return mapProvider;
    }

    public ObservableList<ChartData> getRadialChartData() { return chartDataList; }
    public void addRadialChartData(final ChartData... DATA) { chartDataList.addAll(DATA); }
    public void addRadialChartData(final List<ChartData> DATA) { chartDataList.addAll(DATA); }
    public void setRadialChartData(final ChartData... DATA) { chartDataList.setAll(DATA); }
    public void setRadialChartData(final List<ChartData> DATA) { chartDataList.setAll(DATA); }
    public void removeRadialChartData(final ChartData DATA) { chartDataList.remove(DATA); }
    public void clearRadialChartData() { chartDataList.clear(); }

    /**
     * A convenient method to set the color of foreground elements like
     * title, description, unit, value, tickLabel and tickMark to the given
     * Color.
     *
     * @param COLOR
     */
    public void setForegroundBaseColor(final Color COLOR) {
        if (null == titleColor) { _titleColor = COLOR; } else { titleColor.set(COLOR); }
        if (null == descriptionColor) { _descriptionColor = COLOR; } else { descriptionColor.set(COLOR); }
        if (null == unitColor) { _unitColor = COLOR; } else { unitColor.set(COLOR); }
        if (null == valueColor) { _valueColor = COLOR; } else { valueColor.set(COLOR); }
        if (null == textColor) { _textColor = COLOR; } else { textColor.set(COLOR); }
        if (null == foregroundColor) { _foregroundColor = COLOR; } else { foregroundColor.set(COLOR); }
        fireTileEvent(REDRAW_EVENT);
    }

    /**
     * Returns the text size that will be used for the title,
     * subtitle and text in the different skins.
     * The factor in the text size will be used to calculate the
     * height of the font.
     * @return the text size that will be used for the title, subtitle and text
     */
    public TextSize getTextSize() { return null == textSize ? _textSize : textSize.get(); }
    /**
     * Defines the text size that will be used for the title,
     * subtitle and text in the different skins.
     * @param SIZE
     */
    public void setTextSize(final TextSize SIZE) {
        if (null == textSize) {
            _textSize = SIZE;
            fireTileEvent(REDRAW_EVENT);
        } else {
            textSize.set(SIZE);
        }
    }
    public ObjectProperty<TextSize> textSizeProperty() {
        if (null == textSize) {
            textSize = new ObjectPropertyBase<TextSize>(_textSize) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT);}
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "textSize"; }
            };
        }
        return textSize;
    }

    /**
     * Returns true if the corners of the Tiles are rounded
     * @return true if the corners of the Tiles are rounded
     */
    public boolean getRoundedCorners() { return null == roundedCorners ? _roundedCorners : roundedCorners.get(); }
    /**
     * Switches the corners of the Tiles between rounded and rectangular
     * @param ROUNDED
     */
    public void setRoundedCorners(final boolean ROUNDED) {
        if (null == roundedCorners) {
            _roundedCorners = ROUNDED;
            fireTileEvent(REDRAW_EVENT);
        } else {
            roundedCorners.set(ROUNDED);
        }
    }
    public BooleanProperty roundedCornersProperty() {
        if (null == roundedCorners) {
            roundedCorners = new BooleanPropertyBase(_roundedCorners) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "roundedCorners"; }
            };
        }
        return roundedCorners;
    }

    /**
     * Returns true if the visualization of the value should start from 0. This
     * is especially useful when you work for example with a gauge that has a
     * range with a negative minValue
     *
     * @return true if the visualization of the value should start from 0
     */
    public boolean isStartFromZero() { return null == startFromZero ? _startFromZero : startFromZero.get(); }
    /**
     * Defines the behavior of the visualization where the needle/bar should
     * start from 0 instead of the minValue. This is especially useful when
     * working with a gauge that has a range with a negative minValue
     *
     * @param IS_TRUE
     */
    public void setStartFromZero(final boolean IS_TRUE) {
        if (null == startFromZero) {
            _startFromZero = IS_TRUE;
            setValue(IS_TRUE && getMinValue() < 0 ? 0 : getMinValue());
            fireTileEvent(REDRAW_EVENT);
        } else {
            startFromZero.set(IS_TRUE);
        }
    }
    public BooleanProperty startFromZeroProperty() {
        if (null == startFromZero) {
            startFromZero = new BooleanPropertyBase(_startFromZero) {
                @Override protected void invalidated() {
                    Tile.this.setValue((get() && getMinValue() < 0) ? 0 : getMinValue());
                    fireTileEvent(REDRAW_EVENT);
                }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "startFromZero"; }
            };
        }
        return startFromZero;
    }

    /**
     * Returns true if the needle/bar should always return to zero. This setting
     * only makes sense if animated == true and the data rate is not too high.
     * Set to false when using real measured live data.
     *
     * @return true if the needle/bar should always return to zero.
     */
    public boolean isReturnToZero() { return null == returnToZero ? _returnToZero : returnToZero.get(); }
    /**
     * Defines the behavior of the visualization where the needle/bar should
     * always return to 0 after it reached the final value. This setting only makes
     * sense if animated == true and the data rate is not too high.
     * Set to false when using real measured live data.
     *
     * @param IS_TRUE
     */
    public void setReturnToZero(final boolean IS_TRUE) {
        if (null == returnToZero) {
            _returnToZero = Double.compare(getMinValue(), 0.0) <= 0 && IS_TRUE;
            fireTileEvent(REDRAW_EVENT);
        } else {
            returnToZero.set(IS_TRUE);
        }
    }
    public BooleanProperty returnToZeroProperty() {
        if (null == returnToZero) {
            returnToZero = new BooleanPropertyBase(_returnToZero) {
                @Override protected void invalidated() {
                    if (Helper.biggerThan(getMaxValue(), 0.0)) set(false);
                    fireTileEvent(REDRAW_EVENT);
                }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "returnToZero"; }
            };
        }
        return returnToZero;
    }

    /**
     * Returns the smallest value that was measured after the last reset.
     * The default value is the maxValue of the gauge.
     *
     * @return the smallest value that was measured after the last reset
     */
    public double getMinMeasuredValue() { return null == minMeasuredValue ? _minMeasuredValue : minMeasuredValue.get(); }
    /**
     * Sets the minMeasuredValue to the given value.
     *
     * @param MIN_MEASURED_VALUE
     */
    public void setMinMeasuredValue(final double MIN_MEASURED_VALUE) {
        if (null == minMeasuredValue) {
            _minMeasuredValue = MIN_MEASURED_VALUE;
        } else {
            minMeasuredValue.set(MIN_MEASURED_VALUE);
        }
    }
    public ReadOnlyDoubleProperty minMeasuredValueProperty() {
        if (null == minMeasuredValue) { minMeasuredValue = new SimpleDoubleProperty(this, "minMeasuredValue", _minMeasuredValue); }
        return minMeasuredValue;
    }

    /**
     * Returns the biggest value that was measured after the last reset.
     * The default value is the minValue of the gauge.
     *
     * @return the biggest value that was measured after the last reset
     */
    public double getMaxMeasuredValue() {
        return null == maxMeasuredValue ? _maxMeasuredValue : maxMeasuredValue.get();
    }
    /**
     * Sets the maxMeasuredVAlue to the given value.
     *
     * @param MAX_MEASURED_VALUE
     */
    public void setMaxMeasuredValue(final double MAX_MEASURED_VALUE) {
        if (null == maxMeasuredValue) {
            _maxMeasuredValue = MAX_MEASURED_VALUE;
        } else {
            maxMeasuredValue.set(MAX_MEASURED_VALUE);
        }
    }
    public ReadOnlyDoubleProperty maxMeasuredValueProperty() {
        if (null == maxMeasuredValue) { maxMeasuredValue = new SimpleDoubleProperty(this, "maxMeasuredValue", _maxMeasuredValue); }
        return maxMeasuredValue;
    }

    /**
     * Resets the min- and maxMeasuredValue to the value of the gauge.
     */
    public void resetMeasuredValues() {
        setMinMeasuredValue(getValue());
        setMaxMeasuredValue(getValue());
    }

    /**
     * Returns true if the indicator of the minMeasuredValue is visible.
     *
     * @return true if the indicator of the minMeasuredValue is visible
     */
    public boolean isMinMeasuredValueVisible() { return null == minMeasuredValueVisible ? _minMeasuredValueVisible : minMeasuredValueVisible.get(); }
    /**
     * Defines if the indicator of the minMeasuredValue should be visible.
     *
     * @param VISIBLE
     */
    public void setMinMeasuredValueVisible(final boolean VISIBLE) {
        if (null == minMeasuredValueVisible) {
            _minMeasuredValueVisible = VISIBLE;
            fireTileEvent(VISIBILITY_EVENT);
        } else {
            minMeasuredValueVisible.set(VISIBLE);
        }
    }
    public BooleanProperty minMeasuredValueVisibleProperty() {
        if (null == minMeasuredValueVisible) {
            minMeasuredValueVisible = new BooleanPropertyBase(_minMeasuredValueVisible) {
                @Override protected void invalidated() { fireTileEvent(VISIBILITY_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "minMeasuredValueVisible"; }
            };
        }
        return minMeasuredValueVisible;
    }

    /**
     * Returns true if the indicator of the maxMeasuredValue is visible.
     *
     * @return true if the indicator of the maxMeasuredValue is visible
     */
    public boolean isMaxMeasuredValueVisible() { return null == maxMeasuredValueVisible ? _maxMeasuredValueVisible : maxMeasuredValueVisible.get(); }
    /**
     * Defines if the indicator of the maxMeasuredValue should be visible.
     *
     * @param VISIBLE
     */
    public void setMaxMeasuredValueVisible(final boolean VISIBLE) {
        if (null == maxMeasuredValueVisible) {
            _maxMeasuredValueVisible = VISIBLE;
            fireTileEvent(VISIBILITY_EVENT);
        } else {
            maxMeasuredValueVisible.set(VISIBLE);
        }
    }
    public BooleanProperty maxMeasuredValueVisibleProperty() {
        if (null == maxMeasuredValueVisible) {
            maxMeasuredValueVisible = new BooleanPropertyBase(_maxMeasuredValueVisible) {
                @Override protected void invalidated() { fireTileEvent(VISIBILITY_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "maxMeasuredValueVisible"; }
            };
        }
        return maxMeasuredValueVisible;
    }

    /**
     * Returns true if the old value of the gauge is visible (not implemented)
     *
     * @return true if the old value of the gauge is visible (not implemented)
     */
    public boolean isOldValueVisible() { return null == oldValueVisible ? _oldValueVisible : oldValueVisible.get(); }
    /**
     * Defines if the old value of the gauge should be visible (not implemented)
     *
     * @param VISIBLE
     */
    public void setOldValueVisible(final boolean VISIBLE) {
        if (null == oldValueVisible) {
            _oldValueVisible = VISIBLE;
            fireTileEvent(VISIBILITY_EVENT);
        } else {
            oldValueVisible.set(VISIBLE);
        }
    }
    public BooleanProperty oldValueVisibleProperty() {
        if (null == oldValueVisible) {
            oldValueVisible = new BooleanPropertyBase(_oldValueVisible) {
                @Override protected void invalidated() { fireTileEvent(VISIBILITY_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "oldValueVisible"; }
            };
        }
        return oldValueVisible;
    }

    /**
     * Returns true if the visualization of the gauge value is visible.
     * Usually this is a Label or Text node.
     *
     * @return true if the visualization of the gauge value is visible
     */
    public boolean isValueVisible() { return null == valueVisible ? _valueVisible : valueVisible.get(); }
    /**
     * Defines if the visualization of the gauge value should be visible.
     *
     * @param VISIBLE
     */
    public void setValueVisible(final boolean VISIBLE) {
        if (null == valueVisible) {
            _valueVisible = VISIBLE;
            fireTileEvent(VISIBILITY_EVENT);
        } else {
            valueVisible.set(VISIBLE);
        }
    }
    public BooleanProperty valueVisibleProperty() {
        if (null == valueVisible) {
            valueVisible = new BooleanPropertyBase(_valueVisible) {
                @Override protected void invalidated() { fireTileEvent(VISIBILITY_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "valueVisible"; }
            };
        }
        return valueVisible;
    }

    /**
     * Returns the Paint object that will be used to fill the gauge foreground.
     * This is usally a Color object.
     *
     * @return the Paint object that will be used to fill the gauge foreground
     */
    public Color getForegroundColor() { return null == foregroundColor ? _foregroundColor : foregroundColor.get(); }
    /**
     * Defines the Paint object that will be used to fill the gauge foreground.
     *
     * @param COLOR
     */
    public void setForegroundColor(final Color COLOR) {
        if (null == foregroundColor) {
            _foregroundColor = COLOR;
            fireTileEvent(REDRAW_EVENT);
        } else {
            foregroundColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> foregroundColorProperty() {
        if (null == foregroundColor) {
            foregroundColor = new ObjectPropertyBase<Color>(_foregroundColor) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "foregroundColor"; }
            };
            _foregroundColor = null;
        }
        return foregroundColor;
    }
    
    /**
     * Returns the Paint object that will be used to fill the gauge background.
     * This is usally a Color object.
     *
     * @return the Paint object that will be used to fill the gauge background
     */
    public Color getBackgroundColor() { return null == backgroundColor ? _backgroundColor : backgroundColor.get(); }
    /**
     * Defines the Paint object that will be used to fill the gauge background.
     *
     * @param COLOR
     */
    public void setBackgroundColor(final Color COLOR) {
        if (null == backgroundColor) {
            _backgroundColor = COLOR;
            fireTileEvent(REDRAW_EVENT);
        } else {
            backgroundColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> backgroundColorProperty() {
        if (null == backgroundColor) {
            backgroundColor = new ObjectPropertyBase<Color>(_backgroundColor) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "backgroundColor"; }
            };
            _backgroundColor = null;
        }
        return backgroundColor;
    }

    /**
     * Returns the Paint object that will be used to draw the border of the gauge.
     * Usually this is a Color object.
     *
     * @return the Paint object that will be used to draw the border of the gauge
     */
    public Color getBorderColor() { return null == borderColor ? _borderColor : borderColor.get(); }
    /**
     * Defines the Paint object that will be used to draw the border of the gauge.
     *
     * @param PAINT
     */
    public void setBorderColor(final Color PAINT) {
        if (null == borderColor) {
            _borderColor = PAINT;
            fireTileEvent(REDRAW_EVENT);
        } else {
            borderColor.set(PAINT);
        }
    }
    public ObjectProperty<Color> borderColorProperty() {
        if (null == borderColor) {
            borderColor = new ObjectPropertyBase<Color>(_borderColor) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "borderColor"; }
            };
            _borderColor = null;
        }
        return borderColor;
    }

    /**
     * Returns the width in pixels that will be used to draw the border of the gauge.
     * The value will be clamped between 0 and 50 pixels.
     *
     * @return the width in pixels that will be used to draw the border of the gauge
     */
    public double getBorderWidth() { return null == borderWidth ? _borderWidth : borderWidth.get(); }
    /**
     * Defines the width in pixels that will be used to draw the border of the gauge.
     * The value will be clamped between 0 and 50 pixels.
     *
     * @param WIDTH
     */
    public void setBorderWidth(final double WIDTH) {
        if (null == borderWidth) {
            _borderWidth = clamp(0.0, 50.0, WIDTH);
            fireTileEvent(REDRAW_EVENT);
        } else {
            borderWidth.set(WIDTH);
        }
    }
    public DoubleProperty borderWidthProperty() {
        if (null == borderWidth) {
            borderWidth = new DoublePropertyBase(_borderWidth) {
                @Override protected void invalidated() {
                    final double WIDTH = get();
                    if (WIDTH < 0 || WIDTH > 50) set(clamp(0.0, 50.0, WIDTH));
                    fireTileEvent(REDRAW_EVENT);
                }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "borderWidth"; }
            };
        }
        return borderWidth;
    }

    /**
     * Returns the color that will be used to colorize the knob of
     * the radial gauges.
     *
     * @return the color that will be used to colorize the knob of the radial gauges
     */
    public Color getKnobColor() { return null == knobColor ? _knobColor : knobColor.get(); }
    /**
     * Defines the color that will be used to colorize the knob of
     * the radial gauges.
     *
     * @param COLOR
     */
    public void setKnobColor(final Color COLOR) {
        if (null == knobColor) {
            _knobColor = COLOR;
            fireTileEvent(RESIZE_EVENT);
        } else {
            knobColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> knobColorProperty() {
        if (null == knobColor) {
            knobColor  = new ObjectPropertyBase<Color>(_knobColor) {
                @Override protected void invalidated() { fireTileEvent(RESIZE_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "knobColor"; }
            };
            _knobColor = null;
        }
        return knobColor;
    }

    public Color getActiveColor() { return null == activeColor ? _activeColor : activeColor.get(); }

    public void setActiveColor(final Color COLOR) {
        if (null == activeColor) {
            _activeColor = COLOR;
            fireTileEvent(REDRAW_EVENT);
        } else {
            activeColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> activeColorProperty() {
        if (null == activeColor) {
            activeColor = new ObjectPropertyBase<Color>(_activeColor) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "activeColor"; }
            };
            _activeColor = null;
        }
        return activeColor;
    }

    /**
     * Returns true if setting the value of the gauge will be animated
     * using the duration defined in animationDuration [ms].
     * Keep in mind that it only makes sense to animate the setting if
     * the data rate is low (more than 1 value per second). If you use real
     * live measured data you should set animated to false.
     *
     * @return true if setting the value of the gauge will be animated
     */
    public boolean isAnimated() { return null == animated ? _animated : animated.get(); }
    /**
     * Defines if setting the value of the gauge should be animated using
     * the duration defined in animationDuration [ms].
     * Keep in mind that it only makes sense to animate the setting if
     * the data rate is low (more than 1 value per second). If you use real
     * live measured data you should set animated to false.
     *
     * @param ANIMATED
     */
    public void setAnimated(final boolean ANIMATED) {
        if (null == animated) {
            _animated = ANIMATED;
        } else {
            animated.set(ANIMATED);
        }
    }
    public BooleanProperty animatedProperty() {
        if (null == animated) { animated = new SimpleBooleanProperty(Tile.this, "animated", _animated); }
        return animated;
    }

    /**
     * Returns the duration in milliseconds that will be used to animate
     * the needle/bar of the gauge from the last value to the next value.
     * This will only be used if animated == true. This value will be
     * clamped in the range of 10ms - 10s.
     *
     * @return the duration in ms that will be used to animate the needle/bar
     */
    public long getAnimationDuration() { return animationDuration; }
    /**
     * Defines the duration ín milliseconds that will be used to animate
     * the needle/bar of the gauge from the last value to the next value.
     * This will only be used if animated == true. This value will be
     * clamped in the range of 10ms - 10s.
     *
     * @param ANIMATION_DURATION
     */
    public void setAnimationDuration(final long ANIMATION_DURATION) { animationDuration = clamp(10, 10000, ANIMATION_DURATION); }

    /**
     * Returns the angle in degree that defines the start of the scale with
     * it's minValue in a radial gauge. If set to 0 the scale will start at
     * the bottom center and the direction of counting is mathematical correct
     * counter-clockwise.
     * Means if you would like to start the scale on the left side in the
     * middle of the gauge height the startAngle should be set to 270 degrees.
     *
     * @return the angle in degree that defines the start of the scale
     */
    public double getStartAngle() { return null == startAngle ? _startAngle : startAngle.get(); }
    /**
     * Defines the angle in degree that defines the start of the scale with
     * it's minValue in a radial gauge. If set to 0 the scale will start at
     * the bottom center and the direction of counting is mathematical correct
     * counter-clockwise.
     * Means if you would like to start the scale on the left side in the
     * middle of the gauge height the startAngle should be set to 270 degrees.
     *
     * @param ANGLE
     */
    public void setStartAngle(final double ANGLE) {
        if (null == startAngle) {
            _startAngle = clamp(0.0, 360.0, ANGLE);
            fireTileEvent(RECALC_EVENT);
        } else {
            startAngle.set(ANGLE);
        }
    }
    public DoubleProperty startAngleProperty() {
        if (null == startAngle) {
            startAngle = new DoublePropertyBase(_startAngle) {
                @Override protected void invalidated() {
                    final double ANGLE = get();
                    if (ANGLE < 0 || ANGLE > 360 ) set(clamp(0.0, 360.0, ANGLE));
                    fireTileEvent(RECALC_EVENT);
                }
                @Override public Object getBean() { return this; }
                @Override public String getName() { return "startAngle"; }
            };
        }
        return startAngle;
    }

    /**
     * Returns the angle range in degree that will be used to draw the scale
     * of the radial gauge. The given range will be clamped in the range of
     * 0 - 360 degrees and will be drawn in the direction dependent on the
     * scaleDirection.
     *
     * @return the angle range in degree that will be used to draw the scale
     */
    public double getAngleRange() { return null == angleRange ? _angleRange : angleRange.get(); }
    /**
     * Defines the angle range in degree that will be used to draw the scale
     * of the radial gauge. The given range will be clamped in the range of
     * 0 - 360 degrees. The range will start at the startAngle and will be
     * drawn in the direction dependent on the scaleDirection.
     *
     * @param RANGE
     */
    public void setAngleRange(final double RANGE) {
        double tmpAngleRange = clamp(0.0, 360.0, RANGE);
        if (null == angleRange) {
            _angleRange = tmpAngleRange;
            setAngleStep(tmpAngleRange / getRange());
            if (isAutoScale()) { calcAutoScale(); }
            fireTileEvent(RECALC_EVENT);
        } else {
            angleRange.set(tmpAngleRange);
        }
    }
    public DoubleProperty angleRangeProperty() {
        if (null == angleRange) {
            angleRange = new DoublePropertyBase(_angleRange) {
                @Override protected void invalidated() {
                    final double ANGLE_RANGE = get();
                    if (ANGLE_RANGE < 0 || ANGLE_RANGE > 360) set(clamp(0.0, 360.0, ANGLE_RANGE));
                    setAngleStep(get() / getRange());
                    if (isAutoScale()) { calcAutoScale(); }
                    fireTileEvent(RECALC_EVENT);
                }
                @Override public Object getBean() { return this; }
                @Override public String getName() { return "angleRange"; }
            };
        }
        return angleRange;
    }

    /**
     * Returns the value that is calculated by dividing the angleRange
     * by the range. The angleStep will always be recalculated when changing
     * the min-, maxValue or angleRange.
     * E.g. angleRange = 180 degrees, range = 0 - 100 will lead to angleStep = 180/100 = 1.8
     *
     * @return the value that is calculated by dividing the angleRange by the range
     */
    public double getAngleStep() { return null == angleStep ? _angleStep : angleStep.get(); }
    /**
     * Private method that will be used to set the angleStep
     *
     * @param STEP
     */
    private void setAngleStep(final double STEP) {
        if (null == angleStep) {
            _angleStep = STEP;
        } else {
            angleStep.set(STEP);
        }
    }
    public ReadOnlyDoubleProperty angleStepProperty() {
        if (null == angleStep) { angleStep = new SimpleDoubleProperty(Tile.this, "angleStep", _angleStep); }
        return angleStep;
    }

    /**
     * Returns true if the scale will be calculated automatically based
     * on the defined values for min- and maxValue.
     * The autoscaling is on per default because otherwise you will
     * run into problems when having very large or very small scales like
     * 0 - 10000 or 0 - 1.
     *
     * @return true if the scale will be calculated automatically
     */
    public boolean isAutoScale() { return null == autoScale ? _autoScale : autoScale.get(); }
    /**
     * Defines if the scale should be calculated automatically based on
     * the defined values for min- and maxValue.
     * The autoscaling is on per default because otherwise you will
     * run into problems when having very large or very small scales like
     * 0 - 10000 or 0 - 1.
     *
     * @param AUTO_SCALE
     */
    public void setAutoScale(final boolean AUTO_SCALE) {
        if (null == autoScale) {
            _autoScale = AUTO_SCALE;
            if (_autoScale) {
                originalMinValue = getMinValue();
                originalMaxValue = getMaxValue();
                calcAutoScale();
            } else {
                setMinValue(Helper.equals(-Double.MAX_VALUE, originalMinValue) ? getMinValue() : originalMinValue);
                setMaxValue(Helper.equals(Double.MAX_VALUE, originalMaxValue) ? getMaxValue() : originalMaxValue);
            }
            fireTileEvent(RECALC_EVENT);
        } else {
            autoScale.set(AUTO_SCALE);
        }
    }
    public BooleanProperty autoScaleProperty() {
        if (null == autoScale) {
            autoScale = new BooleanPropertyBase(_autoScale) {
                @Override protected void invalidated() {
                    if (get()) {
                        calcAutoScale();
                    } else {
                        setMinValue(originalMinValue);
                        setMaxValue(originalMaxValue);
                    }
                    fireTileEvent(RECALC_EVENT);
                }
                @Override public Object getBean() { return this; }
                @Override public String getName() { return "autoScale"; }
            };
        }
        return autoScale;
    }

    /**
     * Returns true if effects like shadows will be drawn.
     * In some gauges inner- and dropshadows will be used which will be
     * switched on/off by setting the shadowsEnabled property.
     *
     * @return true if effects like shadows will be drawn
     */
    public boolean isShadowsEnabled() { return null == shadowsEnabled ? _shadowsEnabled : shadowsEnabled.get(); }
    /**
     * Defines if effects like shadows should be drawn.
     * In some gauges inner- and dropshadows will be used which will be
     * switched on/off by setting the shadowsEnabled property.
     *
     * @param ENABLED
     */
    public void setShadowsEnabled(final boolean ENABLED) {
        if (null == shadowsEnabled) {
            _shadowsEnabled = ENABLED;
            fireTileEvent(REDRAW_EVENT);
        } else {
            shadowsEnabled.set(ENABLED);
        }
    }
    public BooleanProperty shadowsEnabledProperty() {
        if (null == shadowsEnabled) {
            shadowsEnabled = new BooleanPropertyBase(_shadowsEnabled) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "shadowsEnabled"; }
            };
        }
        return shadowsEnabled;
    }
    
    public Locale getLocale() { return null == locale ? _locale : locale.get(); }
    public void setLocale(final Locale LOCALE) {
        if (null == locale) {
            _locale = null == LOCALE ? Locale.US : LOCALE;
            fireTileEvent(REDRAW_EVENT);
        } else {
            locale.set(LOCALE);
        }
    }
    public ObjectProperty<Locale> localeProperty() {
        if (null == locale) {
            locale  = new ObjectPropertyBase<Locale>(_locale) {
                @Override protected void invalidated() {
                    if (null == get()) set(Locale.US);
                    fireTileEvent(REDRAW_EVENT);
                }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "locale"; }
            };
            _locale = null;
        }
        return locale;
    }

    /**
     * Returns the number format that will be used to format the value
     * in the gauge (NOT USED AT THE MOMENT)
     *
     * @return the number format that will bused to format the value
     */
    public NumberFormat getNumberFormat() { return null == numberFormat ? _numberFormat : numberFormat.get(); }
    /**
     * Defines the number format that will be used to format the value
     * in the gauge (NOT USED AT THE MOMENT)
     *
     * @param FORMAT
     */
    public void setNumberFormat(final NumberFormat FORMAT) {
        if (null == numberFormat) {
            _numberFormat = null == FORMAT ? NumberFormat.getInstance(getLocale()) : FORMAT;
            fireTileEvent(RESIZE_EVENT);
        } else {
            numberFormat.set(FORMAT);
        }
    }
    public ObjectProperty<NumberFormat> numberFormatProperty() {
        if (null == numberFormat) {
            numberFormat  = new ObjectPropertyBase<NumberFormat>(_numberFormat) {
                @Override protected void invalidated() {
                    if (null == get()) set(NumberFormat.getInstance(getLocale()));
                    fireTileEvent(RESIZE_EVENT);
                }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "numberFormat"; }
            };
            _numberFormat = null;
        }
        return numberFormat;
    }

    /**
     * Returns the number of decimals that will be used to format the
     * value of the gauge. The number of decimals will be clamped to
     * a value between 0-3.
     *
     * @return the number of decimals that will be used to format the value
     */
    public int getDecimals() { return null == decimals ? _decimals : decimals.get(); }
    /**
     * Defines the number of decimals that will be used to format the
     * value of the gauge. The number of decimals will be clamped to
     * a value between 0-3.
     *
     * @param DECIMALS
     */
    public void setDecimals(final int DECIMALS) {
        if (null == decimals) {
            _decimals = clamp(0, MAX_NO_OF_DECIMALS, DECIMALS);
            fireTileEvent(REDRAW_EVENT);
        } else {
            decimals.set(DECIMALS);
        }
    }
    public IntegerProperty decimalsProperty() {
        if (null == decimals) {
            decimals = new IntegerPropertyBase(_decimals) {
                @Override protected void invalidated() {
                    final int VALUE = get();
                    if (VALUE < 0 || VALUE > MAX_NO_OF_DECIMALS) set(clamp(0, MAX_NO_OF_DECIMALS, VALUE));
                    fireTileEvent(REDRAW_EVENT);
                }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "decimals"; }
            };
        }
        return decimals;
    }

    /**
     * Returns the number of tickLabelDecimals that will be used to format the
     * ticklabels of the gauge. The number of tickLabelDecimals will be clamped to
     * a value between 0-3.
     *
     * @return the number of tickLabelDecimals that will be used to format the ticklabels
     */
    public int getTickLabelDecimals() { return null == tickLabelDecimals ? _tickLabelDecimals : tickLabelDecimals.get(); }
    /**
     * Defines the number of tickLabelDecimals that will be used to format the
     * ticklabels of the gauge. The number of tickLabelDecimals will be clamped to
     * a value between 0-3.
     *
     * @param DECIMALS
     */
    public void setTickLabelDecimals(final int DECIMALS) {
        if (null == tickLabelDecimals) {
            _tickLabelDecimals = clamp(0, MAX_NO_OF_DECIMALS, DECIMALS);
            fireTileEvent(REDRAW_EVENT);
        } else {
            tickLabelDecimals.set(DECIMALS);
        }
    }
    public IntegerProperty tickLabelDecimalsProperty() {
        if (null == tickLabelDecimals) {
            tickLabelDecimals = new IntegerPropertyBase(_tickLabelDecimals) {
                @Override protected void invalidated() {
                    final int VALUE = get();
                    if (VALUE < 0 || VALUE > MAX_NO_OF_DECIMALS) set(clamp(0, MAX_NO_OF_DECIMALS, VALUE));
                    fireTileEvent(REDRAW_EVENT);
                }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "tickLabelDecimals"; }
            };
        }
        return tickLabelDecimals;
    }
    
    /**
     * Returns the color that will be used to colorize the needle of
     * the radial gauges.
     *
     * @return the color that wil be used to colorize the needle
     */
    public Color getNeedleColor() { return null == needleColor ? _needleColor : needleColor.get(); }
    /**
     * Defines the color that will be used to colorize the needle of
     * the radial gauges.
     *
     * @param COLOR
     */
    public void setNeedleColor(final Color COLOR) {
        if (null == needleColor) {
            _needleColor = COLOR;
            fireTileEvent(REDRAW_EVENT);
        } else {
            needleColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> needleColorProperty() {
        if (null == needleColor) {
            needleColor  = new ObjectPropertyBase<Color>(_needleColor) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "needleColor"; }
            };
            _needleColor = null;
        }
        return needleColor;
    }

    /**
     * Returns the color that will be used to colorize the bar of
     * the gauge (if it has a bar).
     *
     * @return the color that will be used to colorized the bar (if available)
     */
    public Color getBarColor() { return null == barColor ? _barColor : barColor.get(); }
    /**
     * Defines the color that will be used to colorize the bar of
     * the gauge (if it has a bar).
     *
     * @param COLOR
     */
    public void setBarColor(final Color COLOR) {
        if (null == barColor) {
            _barColor = COLOR;
            fireTileEvent(REDRAW_EVENT);
        } else {
            barColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> barColorProperty() {
        if (null == barColor) {
            barColor  = new ObjectPropertyBase<Color>(_barColor) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "barColor"; }
            };
            _barColor = null;
        }
        return barColor;
    }

    /**
     * Returns the color that will be used to colorize the bar background of
     * the gauge (if it has a bar).
     *
     * @return the color that will be used to colorize the bar background
     */
    public Color getBarBackgroundColor() { return null == barBackgroundColor ? _barBackgroundColor : barBackgroundColor.get(); }
    /**
     * Returns the color that will be used to colorize the bar background of
     * the gauge (if it has a bar).
     *
     * @param COLOR
     */
    public void setBarBackgroundColor(final Color COLOR) {
        if (null == barBackgroundColor) {
            _barBackgroundColor = COLOR;
            fireTileEvent(REDRAW_EVENT);
        } else {
            barBackgroundColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> barBackgroundColorProperty() {
        if (null == barBackgroundColor) {
            barBackgroundColor  = new ObjectPropertyBase<Color>(_barBackgroundColor) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "barBackgroundColor"; }
            };
            _barBackgroundColor = null;
        }
        return barBackgroundColor;
    }

    /**
     * Returns the color that will be used to colorize the title
     * of the gauge.
     *
     * @return the color that will be used to colorize the title
     */
    public Color getTitleColor() { return null == titleColor ? _titleColor : titleColor.get(); }
    /**
     * Defines the color that will be used to colorize the title
     * of the gauge.
     *
     * @param COLOR
     */
    public void setTitleColor(final Color COLOR) {
        if (null == titleColor) {
            _titleColor = COLOR;
            fireTileEvent(REDRAW_EVENT);
        } else {
            titleColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> titleColorProperty() {
        if (null == titleColor) {
            titleColor  = new ObjectPropertyBase<Color>(_titleColor) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "titleColor"; }
            };
            _titleColor = null;
        }
        return titleColor;
    }

    /**
     * Returns the color that will be used to colorize the description text
     * of the gauge.
     *
     * @return the color that will be used to colorize the description
     */
    public Color getDescriptionColor() { return null == descriptionColor ? _descriptionColor : descriptionColor.get(); }
    /**
     * Defines the color that will be used to colorize the description text
     * of the gauge.
     *
     * @param COLOR
     */
    public void setDescriptionColor(final Color COLOR) {
        if (null == descriptionColor) {
            _descriptionColor = COLOR;
            fireTileEvent(REDRAW_EVENT);
        } else {
            descriptionColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> descriptionColorProperty() {
        if (null == descriptionColor) {
            descriptionColor = new ObjectPropertyBase<Color>(_descriptionColor) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "descriptionColor"; }
            };
            _descriptionColor = null;
        }
        return descriptionColor;
    }

    /**
     * Returns the color that will be used to colorize the unit
     * of the gauge.
     *
     * @return the color that will be used to colorize the unit
     */
    public Color getUnitColor() { return null == unitColor ? _unitColor : unitColor.get(); }
    /**
     * Defines the color that will be used to colorize the unit
     * of the gauge.
     *
     * @param COLOR
     */
    public void setUnitColor(final Color COLOR) {
        if (null == unitColor) {
            _unitColor = COLOR;
            fireTileEvent(REDRAW_EVENT);
        } else {
            unitColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> unitColorProperty() {
        if (null == unitColor) {
            unitColor  = new ObjectPropertyBase<Color>(_unitColor) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "unitColor"; }
            };
            _unitColor = null;
        }
        return unitColor;
    }

    /**
     * Returns the color that will be used to colorize the value
     * of the gauge.
     *
     * @return the color that will be used to colorize the value
     */
    public Color getValueColor() { return null == valueColor ? _valueColor : valueColor.get(); }
    /**
     * Defines the color that will be used to colorize the value
     * of the gauge.
     *
     * @param COLOR
     */
    public void setValueColor(final Color COLOR) {
        if (null == valueColor) {
            _valueColor = COLOR;
            fireTileEvent(REDRAW_EVENT);
        } else {
            valueColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> valueColorProperty() {
        if (null == valueColor) {
            valueColor  = new ObjectPropertyBase<Color>(_valueColor) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "valueColor"; }
            };
            _valueColor = null;
        }
        return valueColor;
    }

    /**
     * Returns the color that will be used to colorize the threshold
     * indicator of the gauge.
     *
     * @return the color that will be used to colorize the threshold indicator
     */
    public Color getThresholdColor() { return null == thresholdColor ? _thresholdColor : thresholdColor.get(); }
    /**
     * Defines the color that will be used to colorize the threshold
     * indicator of the gauge.
     *
     * @param COLOR
     */
    public void setThresholdColor(final Color COLOR) {
        if (null == thresholdColor) {
            _thresholdColor = COLOR;
            fireTileEvent(REDRAW_EVENT);
        } else {
            thresholdColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> thresholdColorProperty() {
        if (null == thresholdColor) {
            thresholdColor  = new ObjectPropertyBase<Color>(_thresholdColor) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "thresholdColor"; }
            };
            _thresholdColor = null;
        }
        return thresholdColor;
    }

    /**
     * Returns true if the value of the gauge should be checked against
     * all sections (if sections not empty). If a value enters a section
     * or leaves a section it will fire an event. The check will be performed
     * after the animation is finished (if animated == true).
     *
     * @return true if the value of the gauge should be checked against all sections
     */
    public boolean getCheckSectionsForValue() { return null == checkSectionsForValue ? _checkSectionsForValue : checkSectionsForValue.get(); }
    /**
     * Defines if the value of the gauge should be checked against
     * all sections (if sections not empty). If a value enters a section
     * or leaves a section it will fire an event. The check will be performed
     * after the animation is finished (if animated == true).
     *
     * @param CHECK
     */
    public void setCheckSectionsForValue(final boolean CHECK) {
        if (null == checkSectionsForValue) { _checkSectionsForValue = CHECK; } else { checkSectionsForValue.set(CHECK); }
    }
    public BooleanProperty checkSectionsForValueProperty() {
        if (null == checkSectionsForValue) { checkSectionsForValue = new SimpleBooleanProperty(Tile.this, "checkSectionsForValue", _checkSectionsForValue); }
        return checkSectionsForValue;
    }
    
    /**
     * Returns true if the value of the gauge should be checked against
     * the threshold. If a value crosses the threshold it will fire an
     * event (EXCEEDED and UNDERRUN. The check will be performed
     * after the animation is finished (if animated == true).
     *
     * @return true if the value of the gauge should be checked against the threshold
     */
    public boolean isCheckThreshold() { return null == checkThreshold ? _checkThreshold : checkThreshold.get(); }
    /**
     * Defines if the value of the gauge should be checked against
     * the threshold. If a value crosses the threshold it will fire an
     * event (EXCEEDED and UNDERRUN. The check will be performed
     * after the animation is finished (if animated == true).
     *
     * @param CHECK
     */
    public void setCheckThreshold(final boolean CHECK) {
        if (null == checkThreshold) {
            _checkThreshold = CHECK;
        } else {
            checkThreshold.set(CHECK);
        }
    }
    public BooleanProperty checkThresholdProperty() {
        if (null == checkThreshold) { checkThreshold = new SimpleBooleanProperty(Tile.this, "checkThreshold", _checkThreshold); }
        return checkThreshold;
    }

    /**
     * Returns true if an inner shadow should be drawn on the gauge
     * background.
     *
     * @return true if an inner shadow should be drawn on the gauge background
     */
    public boolean isInnerShadowEnabled() { return null == innerShadowEnabled ? _innerShadowEnabled : innerShadowEnabled.get(); }
    /**
     * Defines if an inner shadow should be drawn on the gauge
     * background.
     *
     * @param ENABLED
     */
    public void setInnerShadowEnabled(final boolean ENABLED) {
        if (null == innerShadowEnabled) {
            _innerShadowEnabled = ENABLED;
            fireTileEvent(REDRAW_EVENT);
        } else {
            innerShadowEnabled.set(ENABLED);
        }
    }
    public BooleanProperty innerShadowEnabledProperty() {
        if (null == innerShadowEnabled) {
            innerShadowEnabled = new BooleanPropertyBase(_innerShadowEnabled) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "innerShadowEnabled"; }
            };
        }
        return innerShadowEnabled;
    }

    /**
     * Returns true if the threshold indicator should be drawn.
     *
     * @return true if the threshold indicator should be drawn
     */
    public boolean isThresholdVisible() { return null == thresholdVisible ? _thresholdVisible : thresholdVisible.get(); }
    /**
     * Defines if the threshold indicator should be drawn
     *
     * @param VISIBLE
     */
    public void setThresholdVisible(final boolean VISIBLE) {
        if (null == thresholdVisible) {
            _thresholdVisible = VISIBLE;
            fireTileEvent(VISIBILITY_EVENT);
        } else {
            thresholdVisible.set(VISIBLE);
        }
    }
    public BooleanProperty thresholdVisibleProperty() {
        if (null == thresholdVisible) {
            thresholdVisible = new BooleanPropertyBase(_thresholdVisible) {
                @Override protected void invalidated() { fireTileEvent(VISIBILITY_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "thresholdVisible"; }
            };
        }
        return thresholdVisible;
    }

    /**
     * Returns true if the average indicator should be drawn.
     *
     * @return true if the average indicator should be drawn
     */
    public boolean isAverageVisible() { return null == averageVisible ? _averageVisible : averageVisible.get(); }
    /**
     * Defines if the average indicator should be drawn
     *
     * @param VISIBLE
     */
    public void setAverageVisible(final boolean VISIBLE) {
        if (null == averageVisible) {
            _averageVisible = VISIBLE;
            fireTileEvent(VISIBILITY_EVENT);
        } else {
            averageVisible.set(VISIBLE);
        }
    }
    public BooleanProperty averageVisibleProperty() {
        if (null == averageVisible) {
            averageVisible = new BooleanPropertyBase() {
                @Override protected void invalidated() { fireTileEvent(VISIBILITY_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "averageVisible"; }
            };
        }
        return averageVisible;
    }

    /**
     * Returns true if the sections will be drawn
     *
     * @return true if the sections will be drawn
     */
    public boolean getSectionsVisible() { return null == sectionsVisible ? _sectionsVisible : sectionsVisible.get(); }
    /**
     * Defines if the sections will be drawn
     *
     * @param VISIBLE
     */
    public void setSectionsVisible(final boolean VISIBLE) {
        if (null == sectionsVisible) {
            _sectionsVisible = VISIBLE;
            fireTileEvent(REDRAW_EVENT);
        } else {
            sectionsVisible.set(VISIBLE);
        }
    }
    public BooleanProperty sectionsVisibleProperty() {
        if (null == sectionsVisible) {
            sectionsVisible = new BooleanPropertyBase(_sectionsVisible) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "sectionsVisible"; }
            };
        }
        return sectionsVisible;
    }

    /**
     * Returns true if the sections in the IndicatorSkin
     * will always be visible
     * @return
     */
    public boolean getSectionsAlwaysVisible() { return null == sectionsAlwaysVisible ? _sectionsAlwaysVisible : sectionsAlwaysVisible.get(); }
    /**
     * Defines if the sections will always be visible.
     * This is currently only used in the IndicatorSkin
     * @param VISIBLE
     */
    public void setSectionsAlwaysVisible(final boolean VISIBLE) {
        if (null == sectionsAlwaysVisible) {
            _sectionsAlwaysVisible = VISIBLE;
            fireTileEvent(REDRAW_EVENT);
        } else {
            sectionsAlwaysVisible.set(VISIBLE);
        }
    }
    public BooleanProperty sectionsAlwaysVisibleProperty() {
        if (null == sectionsAlwaysVisible) {
            sectionsAlwaysVisible = new BooleanPropertyBase(_sectionsAlwaysVisible) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "sectionsAlwaysVisible"; }
            };
        }
        return sectionsAlwaysVisible;
    }

    /**
     * Returns true if the text of the sections should be drawn inside
     * the sections. This is currently only used in the SimpleSkin.
     *
     * @return true if the text of the sections should be drawn
     */
    public boolean isSectionTextVisible() { return null == sectionTextVisible ? _sectionTextVisible : sectionTextVisible.get(); }
    /**
     * Defines if the text of the sections should be drawn inside
     * the sections. This is currently only used in the SimpleSkin.
     *
     * @param VISIBLE
     */
    public void setSectionTextVisible(final boolean VISIBLE) {
        if (null == sectionTextVisible) {
            _sectionTextVisible = VISIBLE;
            fireTileEvent(REDRAW_EVENT);
        } else {
            sectionTextVisible.set(VISIBLE);
        }
    }
    public BooleanProperty sectionTextVisibleProperty() {
        if (null == sectionTextVisible) {
            sectionTextVisible = new BooleanPropertyBase(_sectionTextVisible) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "sectionTextVisible"; }
            };
        }
        return sectionTextVisible;
    }

    /**
     * Returns true if the icon of the sections should be drawn inside
     * the sections. This is currently only used in the SimpleSkin.
     *
     * @return true if the icon of the sections should be drawn
     */
    public boolean getSectionIconsVisible() { return null == sectionIconsVisible ? _sectionIconsVisible : sectionIconsVisible.get(); }
    /**
     * Defines if the icon of the sections should be drawn inside
     * the sections. This is currently only used in the SimpleSkin.
     *
     * @param VISIBLE
     */
    public void setSectionIconsVisible(final boolean VISIBLE) {
        if (null == sectionIconsVisible) {
            _sectionIconsVisible = VISIBLE;
            fireTileEvent(REDRAW_EVENT);
        } else {
            sectionIconsVisible.set(VISIBLE);
        }
    }
    public BooleanProperty sectionIconsVisibleProperty() {
        if (null == sectionIconsVisible) {
            sectionIconsVisible = new BooleanPropertyBase(_sectionIconsVisible) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "sectionIconsVisible"; }
            };
        }
        return sectionIconsVisible;
    }

    /**
     * Returns true if sections should be highlighted in case they
     * contain the current value.
     *
     * @return true if sections should be highlighted
     */
    public boolean isHighlightSections() { return null == highlightSections ? _highlightSections : highlightSections.get(); }
    /**
     * Defines if sections should be highlighted in case they
     * contain the current value
     *
     * @param HIGHLIGHT
     */
    public void setHighlightSections(final boolean HIGHLIGHT) {
        if (null == highlightSections) {
            _highlightSections = HIGHLIGHT;
            fireTileEvent(REDRAW_EVENT);
        } else {
            highlightSections.set(HIGHLIGHT);
        }
    }
    public BooleanProperty highlightSectionsProperty() {
        if (null == highlightSections) {
            highlightSections = new BooleanPropertyBase(_highlightSections) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "highlightSections"; }
            };
        }
        return highlightSections;
    }
    
    /**
     * Returns the orientation of the control. This feature
     * will only be used in the BulletChartSkin and LinearSkin.
     * Values are HORIZONTAL and VERTICAL
     *
     * @return the orientation of the control
     */
    public Orientation getOrientation() { return null == orientation ? _orientation : orientation.get(); }
    /**
     * Defines the orientation of the control. This feature
     * will only be used in the BulletChartSkin and LinearSkin.
     * Values are HORIZONTAL and VERTICAL
     *
     * @param ORIENTATION
     */
    public void setOrientation(final Orientation ORIENTATION) {
        if (null == orientation) {
            _orientation = ORIENTATION;
            fireTileEvent(RESIZE_EVENT);
        } else {
            orientation.set(ORIENTATION);
        }
    }
    public ObjectProperty<Orientation> orientationProperty() {
        if (null == orientation) {
            orientation  = new ObjectPropertyBase<Orientation>(_orientation) {
                @Override protected void invalidated() { fireTileEvent(RESIZE_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "orientation"; }
            };
            _orientation = null;
        }
        return orientation;
    }
    
    /**
     * Returns true if the control should keep it's aspect. This is
     * in principle only needed if the control has different width and
     * height.
     *
     * @return true if the control should keep it's aspect
     */
    public boolean isKeepAspect() { return null == keepAspect ? _keepAspect : keepAspect.get(); }
    /**
     * Defines if the control should keep it's aspect. This is
     * in principle only needed if the control has different width and
     * height.
     *
     * @param KEEP
     */
    public void setKeepAspect(final boolean KEEP) {
        if (null == keepAspect) {
            _keepAspect = KEEP;
        } else {
            keepAspect.set(KEEP);
        }
    }
    public BooleanProperty keepAspectProperty() {
        if (null == keepAspect) { keepAspect = new SimpleBooleanProperty(Tile.this, "keepAspect", _keepAspect); }
        return keepAspect;
    }

    /**
     * Returns true if the alert property was set.
     * This property can be used to visualize an alert
     * situation in a skin.
     * @return true if the alert property was set
     */
    public boolean isAlert() { return null == alert ? _alert : alert.get(); }
    /**
     * Defines if the alert property should be set. This
     * property can be used to visualize an alert situation
     * in the skin.
     * @param ALERT
     */
    public void setAlert(final boolean ALERT) {
        if (null == alert) {
            _alert = ALERT;
            fireTileEvent(ALERT_EVENT);
        } else {
            alert.set(ALERT);
        }
    }
    public BooleanProperty alertProperty() {
        if (null == alert) {
            alert = new BooleanPropertyBase(_alert) {
                @Override protected void invalidated() { fireTileEvent(ALERT_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "alert"; }
            };
        }
        return alert;
    }

    /**
     * Returns the alert message text that could be used in a tooltip
     * in case of an alert.
     * @return the alert message text
     */
    public String getAlertMessage() { return null == alertMessage ? _alertMessage : alertMessage.get(); }
    /**
     * Defines the text that could be used in a tooltip as an
     * alert message.
     * @param MESSAGE
     */
    public void setAlertMessage(final String MESSAGE) {
        if (null == alertMessage) {
            _alertMessage = MESSAGE;
            fireTileEvent(ALERT_EVENT);
        } else {
            alertMessage.set(MESSAGE);
        }
    }
    public StringProperty alertMessageProperty() {
        if (null == alertMessage) {
            alertMessage = new StringPropertyBase(_alertMessage) {
                @Override protected void invalidated() { fireTileEvent(ALERT_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "alertMessage"; }
            };
            _alertMessage = null;
        }
        return alertMessage;
    }

    /**
     * Returns true when smoothing is enabled. This property is only used
     * in the TileSparklineSkin to smooth the path. In a custom skin it
     * could be also used for other things.
     * @return true when smoothing is enabled
     */
    public boolean isSmoothing() { return null == smoothing ? _smoothing : smoothing.get(); }
    /**
     * Defines if the smoothing property should be enabled/disabled.
     * At the moment this is only used in the TileSparklineSkin.
     * @param SMOOTHING
     */
    public void setSmoothing(final boolean SMOOTHING) {
        if (null == smoothing) {
            _smoothing = SMOOTHING;
            fireTileEvent(REDRAW_EVENT);
        } else {
            smoothing.set(SMOOTHING);
        }
    }
    public BooleanProperty smoothingProperty() {
        if (null == smoothing) {
            smoothing = new BooleanPropertyBase(_smoothing) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "smoothing"; }
            };
        }
        return smoothing;
    }

    /**
     * Calling this method will lead to a recalculation of the scale
     */
    public void calcAutoScale() {
        double maxNoOfMajorTicks = 10;
        //double maxNoOfMinorTicks = 10;
        double niceRange         = (Helper.calcNiceNumber(getRange(), false));
        double majorTickSpace    = Helper.calcNiceNumber(niceRange / (maxNoOfMajorTicks - 1), true);
        double niceMinValue      = (Math.floor(getMinValue() / majorTickSpace) * majorTickSpace);
        double niceMaxValue      = (Math.ceil(getMaxValue() / majorTickSpace) * majorTickSpace);
        //double minorTickSpace    = Helper.calcNiceNumber(majorTickSpace / (maxNoOfMinorTicks - 1), true);
        setMinValue(niceMinValue);
        setMaxValue(niceMaxValue);
    }


    /**
     * Returns the current time of the clock.
     * @return the current time of the clock
     */
    public ZonedDateTime getTime() { return time.get(); }
    /**
     * Defines the current time of the clock.
     * @param TIME
     */
    public void setTime(final ZonedDateTime TIME) { time.set(TIME); }
    public void setTime(final long EPOCH_SECONDS) {
        time.set(ZonedDateTime.ofInstant(Instant.ofEpochSecond(EPOCH_SECONDS), getZoneId()));
    }
    public ObjectProperty<ZonedDateTime> timeProperty() { return time; }

    /**
     * Returns the current time in epoch seconds
     * @return the current time in epoch seconds
     */
    public long getCurrentTime() { return currentTime.get(); }
    public ReadOnlyLongProperty currentTimeProperty() { return currentTime; }

    public ZoneId getZoneId() { return zoneId; }

    
    /**
     * Returns the text that was defined for the clock.
     * This text could be used for additional information.
     * @return the text that was defined for the clock
     */
    public String getText() { return null == text ? _text : text.get(); }
    /**
     * Define the text for the clock.
     * This text could be used for additional information.
     * @param TEXT
     */
    public void setText(final String TEXT) {
        if (null == text) {
            _text = TEXT;
            fireTileEvent(REDRAW_EVENT);
        } else {
            text.set(TEXT);
        }
    }
    public StringProperty textProperty() {
        if (null == text) {
            text  = new StringPropertyBase(_text) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "text"; }
            };
            _text = null;
        }
        return text;
    }

    /**
     * Returns the alignment that will be used to align the text
     * in the Tile. Keep in mind that this property will not be used
     * by every skin
     * @return the alignment of the text
     */
    public TextAlignment getTextAlignment() { return null == textAlignment ? _textAlignment : textAlignment.get(); }
    /**
     * Defines the alignment that will be used to align the text
     * in the Tile. Keep in mind that this property will not be used
     * by every skin.
     * @param ALIGNMENT
     */
    public void setTextAlignment(final TextAlignment ALIGNMENT) {
        if (null == textAlignment) {
            _textAlignment = ALIGNMENT;
            fireTileEvent(RESIZE_EVENT);
        } else {
            textAlignment.set(ALIGNMENT);
        }
    }
    public ObjectProperty<TextAlignment> textAlignmentProperty() {
        if (null == textAlignment) {
            textAlignment = new ObjectPropertyBase<TextAlignment>(_textAlignment) {
                @Override protected void invalidated() { fireTileEvent(RESIZE_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "textAlignment"; }
            };
            _textAlignment = null;
        }
        return textAlignment;
    }

    /**
     * Returns an observable list of TimeSection objects. The sections
     * will be used to colorize areas with a special meaning.
     * TimeSections in the Medusa library usually are less eye-catching than
     * Areas.
     * @return an observable list of TimeSection objects
     */
    public ObservableList<TimeSection> getTimeSections() { return timeSections; }
    /**
     * Sets the sections to the given list of TimeSection objects. The
     * sections will be used to colorize areas with a special
     * meaning. Sections in the Medusa library usually are less eye-catching
     * than Areas.
     * @param SECTIONS
     */
    public void setTimeSections(final List<TimeSection> SECTIONS) {
        timeSections.setAll(SECTIONS);
        timeSections.sort(new TimeSectionComparator());
        fireTileEvent(SECTION_EVENT);
    }
    /**
     * Sets the sections to the given array of TimeSection objects. The
     * sections will be used to colorize areas with a special
     * meaning. Sections in the Medusa library usually are less eye-catching
     * than Areas.
     * @param SECTIONS
     */
    public void setTimeSections(final TimeSection... SECTIONS) { setTimeSections(Arrays.asList(SECTIONS)); }
    /**
     * Adds the given TimeSection to the list of sections.
     * Sections in the Medusa library usually are less eye-catching
     * than Areas.
     * @param SECTION
     */
    public void addTimeSection(final TimeSection SECTION) {
        if (null == SECTION) return;
        timeSections.add(SECTION);
        timeSections.sort(new TimeSectionComparator());
        fireTileEvent(SECTION_EVENT);
    }
    /**
     * Removes the given TimeSection from the list of sections.
     * Sections in the Medusa library usually are less eye-catching
     * than Areas.
     * @param SECTION
     */
    public void removeTimeSection(final TimeSection SECTION) {
        if (null == SECTION) return;
        timeSections.remove(SECTION);
        timeSections.sort(new TimeSectionComparator());
        fireTileEvent(SECTION_EVENT);
    }
    /**
     * Clears the list of sections.
     */
    public void clearTimeSections() {
        sections.clear();
        fireTileEvent(SECTION_EVENT);
    }
    
    /**
     * Returns true if the second hand of the clock should move
     * in discrete steps of 1 second. Otherwise it will move continuously like
     * in an automatic clock.
     * @return true if the second hand of the clock should move in discrete steps of 1 second
     */
    public boolean isDiscreteSeconds() { return null == discreteSeconds ? _discreteSeconds : discreteSeconds.get(); }
    /**
     * Defines if the second hand of the clock should move in
     * discrete steps of 1 second. Otherwise it will move continuously like
     * in an automatic clock.
     * @param DISCRETE
     */
    public void setDiscreteSeconds(boolean DISCRETE) {
        if (null == discreteSeconds) {
            _discreteSeconds = DISCRETE;
            stopTask(periodicTickTask);
            if (isAnimated()) return;
            scheduleTickTask();
        } else {
            discreteSeconds.set(DISCRETE);
        }
    }
    public BooleanProperty discreteSecondsProperty() {
        if (null == discreteSeconds) {
            discreteSeconds = new BooleanPropertyBase() {
                @Override protected void invalidated() {
                    stopTask(periodicTickTask);
                    if (isAnimated()) return;
                    scheduleTickTask();
                }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "discreteSeconds"; }
            };
        }
        return discreteSeconds;
    }

    /**
     * Returns true if the minute hand of the clock should move in
     * discrete steps of 1 minute. Otherwise it will move continuously like
     * in an automatic clock.
     * @return true if the minute hand of the clock should move in discrete steps of 1 minute
     */
    public boolean isDiscreteMinutes() { return null == discreteMinutes ? _discreteMinutes : discreteMinutes.get(); }
    /**
     * Defines if the minute hand of the clock should move in
     * discrete steps of 1 minute. Otherwise it will move continuously like
     * in an automatic clock.
     * @param DISCRETE
     */
    public void setDiscreteMinutes(boolean DISCRETE) {
        if (null == discreteMinutes) {
            _discreteMinutes = DISCRETE;
            stopTask(periodicTickTask);
            if (isAnimated()) return;
            scheduleTickTask();
        } else {
            discreteMinutes.set(DISCRETE);
        }
    }
    public BooleanProperty discreteMinutesProperty() {
        if (null == discreteMinutes) {
            discreteMinutes = new BooleanPropertyBase() {
                @Override protected void invalidated() {
                    stopTask(periodicTickTask);
                    if (isAnimated()) return;
                    scheduleTickTask();
                }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "discreteMinutes"; }
            };
        }
        return discreteMinutes;
    }

    /**
     * Returns true if the hour hand of the clock should move in
     * discrete steps of 1 hour. This behavior was more or less
     * implemented to realize the clock of clocks and should usually
     * be false.
     * @return true if the hour hand of the clock should move in discrete steps of 1 hour
     */
    public boolean isDiscreteHours() { return null == discreteHours ? _discreteHours : discreteHours.get(); }
    /**
     * Defines if the hour hand of the clock should move in
     * discrete steps of 1 hour. This behavior was more or less
     * implemented to realize the clock of clocks and should usually
     * be false.
     * @param DISCRETE
     */
    public void setDiscreteHours(final boolean DISCRETE) {
        if (null == discreteHours) {
            _discreteHours = DISCRETE;
        } else {
            discreteHours.set(DISCRETE);
        }
    }
    public BooleanProperty discreteHoursProperty() {
        if (null == discreteHours) { discreteHours = new SimpleBooleanProperty(Tile.this, "discreteHours", _discreteHours); }
        return discreteHours;
    }

    /**
     * Returns true if the second hand of the clock will be drawn.
     * @return true if the second hand of the clock will be drawn.
     */
    public boolean isSecondsVisible() { return null == secondsVisible ? _secondsVisible : secondsVisible.get(); }
    /**
     * Defines if the second hand of the clock will be drawn.
     * @param VISIBLE
     */
    public void setSecondsVisible(boolean VISIBLE) {
        if (null == secondsVisible) {
            _secondsVisible = VISIBLE;
            fireTileEvent(REDRAW_EVENT);
        } else {
            secondsVisible.set(VISIBLE);
        }
    }
    public BooleanProperty secondsVisibleProperty() {
        if (null == secondsVisible) {
            secondsVisible = new BooleanPropertyBase(_secondsVisible) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "secondsVisible"; }
            };
        }
        return secondsVisible;
    }

    /**
     * Returns true if the text of the clock will be drawn.
     * @return true if the text of the clock will be drawn
     */
    public boolean isTextVisible() { return null == textVisible ? _textVisible : textVisible.get(); }
    /**
     * Defines if the text of the clock will be drawn.
     * @param VISIBLE
     */
    public void setTextVisible(final boolean VISIBLE) {
        if (null == textVisible) {
            _textVisible = VISIBLE;
            fireTileEvent(VISIBILITY_EVENT);
        } else {
            textVisible.set(VISIBLE);
        }
    }
    public BooleanProperty textVisibleProperty() {
        if (null == textVisible) {
            textVisible = new BooleanPropertyBase(_textVisible) {
                @Override protected void invalidated() { fireTileEvent(VISIBILITY_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "textVisible"; }
            };
        }
        return textVisible;
    }

    /**
     * Returns true if the date of the clock will be drawn.
     * @return true if the date of the clock will be drawn
     */
    public boolean isDateVisible() { return null == dateVisible ? _dateVisible : dateVisible.get(); }
    /**
     * Defines if the date of the clock will be drawn.
     * @param VISIBLE
     */
    public void setDateVisible(final boolean VISIBLE) {
        if (null == dateVisible) {
            _dateVisible = VISIBLE;
            fireTileEvent(VISIBILITY_EVENT);
        } else {
            dateVisible.set(VISIBLE);
        }

    }
    public BooleanProperty dateVisibleProperty() {
        if (null == dateVisible) {
            dateVisible = new BooleanPropertyBase(_dateVisible) {
                @Override protected void invalidated() { fireTileEvent(VISIBILITY_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "dateVisible"; }
            };
        }
        return dateVisible;
    }

    /**
     * Returns true if the clock is running and shows the current time.
     * The clock will only start running if animated == false.
     * @return true if the clock is running
     */
    public boolean isRunning() { return null == running ? _running : running.get(); }
    /**
     * Defines if the clock is running.
     * The clock will only start running if animated == false;
     * @param RUNNING
     */
    public void setRunning(boolean RUNNING) {
        if (null == running) {
            _running = RUNNING;
            if (RUNNING && !isAnimated()) { scheduleTickTask(); } else { stopTask(periodicTickTask); }
        } else {
            running.set(RUNNING);
        }
    }
    public BooleanProperty runningProperty() {
        if (null == running) {
            running = new BooleanPropertyBase(_running) {
                @Override protected void invalidated() {
                    if (get() && !isAnimated()) { scheduleTickTask(); } else { stopTask(periodicTickTask); }
                }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "running"; }
            }; }
        return running;
    }

    /**
     * Returns the color that will be used to colorize the text of the clock.
     * @return the color that will be used to colorize the text of the clock
     */
    public Color getTextColor() { return null == textColor ? _textColor : textColor.get(); }
    /**
     * Defines the color that will be used to colorize the text of the clock.
     * @param COLOR
     */
    public void setTextColor(final Color COLOR) {
        if (null == textColor) {
            _textColor = COLOR;
            fireTileEvent(REDRAW_EVENT);
        } else {
            textColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> textColorProperty() {
        if (null == textColor) {
            textColor  = new ObjectPropertyBase<Color>(_textColor) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "textColor"; }
            };
            _textColor = null;
        }
        return textColor;
    }

    /**
     * Returns the color that will be used to colorize the date of the clock.
     * @return the color that will be used to colorize the date of the clock
     */
    public Color getDateColor() { return null == dateColor ? _dateColor : dateColor.get(); }
    /**
     * Defines the color that will be used to colorize the date of the clock
     * @param COLOR
     */
    public void setDateColor(final Color COLOR) {
        if (null == dateColor) {
            _dateColor = COLOR;
            fireTileEvent(REDRAW_EVENT);
        } else {
            dateColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> dateColorProperty() {
        if (null == dateColor) {
            dateColor  = new ObjectPropertyBase<Color>(_dateColor) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "dateColor"; }
            };
            _dateColor = null;
        }
        return dateColor;
    }

    /**
     * Returns the color that will be used to colorize the hour tickmarks of the clock.
     * @return the color that will be used to colorize the hour tickmarks of the clock
     */
    public Color getHourTickMarkColor() { return null == hourTickMarkColor ? _hourTickMarkColor : hourTickMarkColor.get(); }
    /**
     * Defines the color that will be used to colorize the hour tickmarks of the clock.
     * @param COLOR
     */
    public void setHourTickMarkColor(final Color COLOR) {
        if (null == hourTickMarkColor) {
            _hourTickMarkColor = COLOR;
            fireTileEvent(REDRAW_EVENT);
        } else {
            hourTickMarkColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> hourTickMarkColorProperty() {
        if (null == hourTickMarkColor) {
            hourTickMarkColor  = new ObjectPropertyBase<Color>(_hourTickMarkColor) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "hourTickMarkColor"; }
            };
            _hourTickMarkColor = null;
        }
        return hourTickMarkColor;
    }

    /**
     * Returns the color that will be used to colorize the minute tickmarks of the clock.
     * @return the color that will be used to colorize the minute tickmarks of the clock
     */
    public Color getMinuteTickMarkColor() { return null == minuteTickMarkColor ? _minuteTickMarkColor : minuteTickMarkColor.get(); }
    /**
     * Defines the color that will be used to colorize the minute tickmarks of the clock.
     * @param COLOR
     */
    public void setMinuteTickMarkColor(final Color COLOR) {
        if (null == minuteTickMarkColor) {
            _minuteTickMarkColor = COLOR;
            fireTileEvent(REDRAW_EVENT);
        } else {
            minuteTickMarkColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> minuteTickMarkColorProperty() {
        if (null == minuteTickMarkColor) {
            minuteTickMarkColor  = new ObjectPropertyBase<Color>(_minuteTickMarkColor) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "minuteTickMarkColor"; }
            };
            _minuteTickMarkColor = null;
        }
        return minuteTickMarkColor;
    }

    /**
     * Returns the color that will be used to colorize the alarm icon.
     * @return the color that will be used to colorize the alarm icon
     */
    public Color getAlarmColor() { return null == alarmColor ? _alarmColor : alarmColor.get(); }
    /**
     * Defines the color that will be used to colorize the alarm icon
     * @param COLOR
     */
    public void setAlarmColor(final Color COLOR) {
        if (null == alarmColor) {
            _alarmColor = COLOR;
            fireTileEvent(REDRAW_EVENT);
        } else {
            alarmColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> alarmColorProperty() {
        if (null == alarmColor) {
            alarmColor  = new ObjectPropertyBase<Color>(_alarmColor) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "alarmColor"; }
            };
            _alarmColor = null;
        }
        return alarmColor;
    }

    /**
     * Returns true if the hour tickmarks will be drawn.
     * @return true if the hour tickmarks will be drawn
     */
    public boolean isHourTickMarksVisible() { return null == hourTickMarksVisible ? _hourTickMarksVisible : hourTickMarksVisible.get(); }
    /**
     * Defines if the hour tickmarks will be drawn.
     * @param VISIBLE
     */
    public void setHourTickMarksVisible(final boolean VISIBLE) {
        if (null == hourTickMarksVisible) {
            _hourTickMarksVisible = VISIBLE;
            fireTileEvent(REDRAW_EVENT);
        } else {
            hourTickMarksVisible.set(VISIBLE);
        }
    }
    public BooleanProperty hourTickMarksVisibleProperty() {
        if (null == hourTickMarksVisible) {
            hourTickMarksVisible = new BooleanPropertyBase(_hourTickMarksVisible) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "hourTickMarksVisible"; }
            };
        }
        return hourTickMarksVisible;
    }

    /**
     * Returns true if the minute tickmarks will be drawn.
     * @return true if the minute tickmarks will be drawn
     */
    public boolean isMinuteTickMarksVisible() { return null == minuteTickMarksVisible ? _minuteTickMarksVisible : minuteTickMarksVisible.get(); }
    /**
     * Defines if the minute tickmarks will be drawn.
     * @param VISIBLE
     */
    public void setMinuteTickMarksVisible(final boolean VISIBLE) {
        if (null == minuteTickMarksVisible) {
            _minuteTickMarksVisible = VISIBLE;
            fireTileEvent(REDRAW_EVENT);
        } else {
            minuteTickMarksVisible.set(VISIBLE);
        }
    }
    public BooleanProperty minuteTickMarksVisibleProperty() {
        if (null == minuteTickMarksVisible) {
            minuteTickMarksVisible = new BooleanPropertyBase(_minuteTickMarksVisible) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "minuteTickMarksVisible"; }
            };
        }
        return minuteTickMarksVisible;
    }

    /**
     * Returns the color that will be used to colorize the hour hand of the clock.
     * @return the color that will be used to colorize the hour hand of the clock
     */
    public Color getHourColor() { return null == hourColor ? _hourColor : hourColor.get(); }
    /**
     * Defines the color that will be used to colorize the hour hand of the clock
     * @param COLOR
     */
    public void setHourColor(final Color COLOR) {
        if (null == hourColor) {
            _hourColor = COLOR;
            fireTileEvent(REDRAW_EVENT);
        } else {
            hourColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> hourColorProperty() {
        if (null == hourColor) {
            hourColor  = new ObjectPropertyBase<Color>(_hourColor) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "hourColor"; }
            };
            _hourColor = null;
        }
        return hourColor;
    }

    /**
     * Returns the color that will be used to colorize the minute hand of the clock.
     * @return the color that will be used to colorize the minute hand of the clock
     */
    public Color getMinuteColor() { return null == minuteColor ? _minuteColor : minuteColor.get(); }
    /**
     * Defines the color that will be used to colorize the minute hand of the clock.
     * @param COLOR
     */
    public void setMinuteColor(final Color COLOR) {
        if (null == minuteColor) {
            _minuteColor = COLOR;
            fireTileEvent(REDRAW_EVENT);
        } else {
            minuteColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> minuteColorProperty() {
        if (null == minuteColor) {
            minuteColor  = new ObjectPropertyBase<Color>(_minuteColor) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "minuteColor"; }
            };
            _minuteColor = null;
        }
        return minuteColor;
    }

    /**
     * Returns the color that will be used to colorize the second hand of the clock.
     * @return the color that will be used to colorize the second hand of the clock
     */
    public Color getSecondColor() { return null == secondColor ? _secondColor : secondColor.get(); }
    /**
     * Defines the color that will be used to colorize the second hand of the clock
     * @param COLOR
     */
    public void setSecondColor(final Color COLOR) {
        if (null == secondColor) {
            _secondColor = COLOR;
            fireTileEvent(REDRAW_EVENT);
        } else {
            secondColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> secondColorProperty() {
        if (null == secondColor) {
            secondColor  = new ObjectPropertyBase<Color>(_secondColor) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "secondColor"; }
            };
            _secondColor = null;
        }
        return secondColor;
    }

    /**
     * Returns true if alarms are enabled.
     * If false then no alarms will be triggered
     * @return true if alarms are enabled
     */
    public boolean isAlarmsEnabled() { return null == alarmsEnabled ? _alarmsEnabled : alarmsEnabled.get(); }
    /**
     * Defines if alarms are enabled.
     * If false then no alarms will be triggered.
     * @param CHECK
     */
    public void setAlarmsEnabled(final boolean CHECK) {
        if (null == alarmsEnabled) {
            _alarmsEnabled = CHECK;
            fireTileEvent(VISIBILITY_EVENT);
        } else {
            alarmsEnabled.set(CHECK);
        }
    }
    public BooleanProperty alarmsEnabledProperty() {
        if (null == alarmsEnabled) {
            alarmsEnabled = new BooleanPropertyBase(_alarmsEnabled) {
                @Override protected void invalidated() { fireTileEvent(VISIBILITY_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "alarmsEnabled"; }
            };
        }
        return alarmsEnabled;
    }

    /**
     * Returns true if alarm markers should be drawn.
     * @return true if alarm markers should be drawn
     */
    public boolean isAlarmsVisible() { return null == alarmsVisible ? _alarmsVisible : alarmsVisible.get(); }
    /**
     * Defines if alarm markers should be drawn.
     * @param VISIBLE
     */
    public void setAlarmsVisible(final boolean VISIBLE) {
        if (null == alarmsVisible) {
            _alarmsVisible = VISIBLE;
            fireTileEvent(REDRAW_EVENT);
        } else {
            alarmsVisible.set(VISIBLE);
        }
    }
    public BooleanProperty alarmsVisibleProperty() {
        if (null == alarmsVisible) {
            alarmsVisible = new BooleanPropertyBase(_alarmsVisible) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "alarmsVisible"; }
            };
        }
        return alarmsVisible;
    }

    /**
     * Returns an observable list of Alarm objects.
     * @return an observable list of Alarm objects
     */
    public ObservableList<Alarm> getAlarms() { return alarms; }
    /**
     * Sets the alarms to the given list of Alarm objects.
     * @param ALARMS
     */
    public void setAlarms(final List<Alarm> ALARMS) { alarms.setAll(ALARMS); }
    /**
     * Sets the alarms to the given array of Alarm objects.
     * @param ALARMS
     */
    public void setAlarms(final Alarm... ALARMS) { setAlarms(Arrays.asList(ALARMS)); }
    /**
     * Adds the given Alarm object from the list of alarms.
     * @param ALARM
     */
    public void addAlarm(final Alarm ALARM) { if (!alarms.contains(ALARM)) alarms.add(ALARM); }
    /**
     * Removes the given Alarm object from the list of alarms.
     * @param ALARM
     */
    public void removeAlarm(final Alarm ALARM) { if (alarms.contains(ALARM)) alarms.remove(ALARM); }
    /**
     * Clears the list of alarms.
     */
    public void clearAlarms() { alarms.clear(); }

    /**
     * Returns the text that will be shown in the Tile tooltip
     * @return the text that will be shown in the Tile tooltip
     */
    public String getTooltipText() { return null == tooltipText ? _tooltipText : tooltipText.get(); }
    /**
     * Defines the text that will shown in the Tile tooltip
     * @param TEXT
     */
    public void setTooltipText(final String TEXT) {
        if (null == tooltipText) {
            _tooltipText = TEXT;
            fireTileEvent(TOOLTIP_TEXT_EVENT);
        } else {
            tooltipText.set(TEXT);
        }
    }
    public StringProperty tooltipTextProperty() {
        if (null == tooltipText) {
            tooltipText = new StringPropertyBase(_tooltipText) {
                @Override protected void invalidated() { fireTileEvent(TOOLTIP_TEXT_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "tooltipText"; }
            };
            _tooltipText = null;
        }
        return tooltipText;
    }

    public double getIncrement() { return increment; }
    public void setIncrement(final double INCREMENT) { increment = clamp(0, 10, INCREMENT); }

    /**
     * Returns true if the control uses the given customFont to
     * render all text elements.
     * @return true if the control uses the given customFont
     */
    public boolean isCustomFontEnabled() { return null == customFontEnabled ? _customFontEnabled : customFontEnabled.get(); }
    /**
     * Defines if the control should use the given customFont
     * to render all text elements
     * @param ENABLED
     */
    public void setCustomFontEnabled(final boolean ENABLED) {
        if (null == customFontEnabled) {
            _customFontEnabled = ENABLED;
            fireTileEvent(RESIZE_EVENT);
        } else {
            customFontEnabled.set(ENABLED);
        }
    }
    public BooleanProperty customFontEnabledProperty() {
        if (null == customFontEnabled) {
            customFontEnabled = new BooleanPropertyBase(_customFontEnabled) {
                @Override protected void invalidated() { fireTileEvent(RESIZE_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "customFontEnabled"; }
            };
        }
        return customFontEnabled;
    }

    /**
     * Returns the given custom Font that can be used to render
     * all text elements. To enable the custom font one has to set
     * customFontEnabled = true
     * @return the given custom Font
     */
    public Font getCustomFont() { return null == customFont ? _customFont : customFont.get(); }
    /**
     * Defines the custom font that can be used to render all
     * text elements. To enable the custom font one has to set
     * customFontEnabled = true
     * @param FONT
     */
    public void setCustomFont(final Font FONT) {
        if (null == customFont) {
            _customFont = FONT;
            fireTileEvent(RESIZE_EVENT);
        } else {
            customFont.set(FONT);
        }
    }
    public ObjectProperty<Font> customFontProperty() {
        if (null == customFont) {
            customFont = new ObjectPropertyBase<Font>() {
                @Override protected void invalidated() { fireTileEvent(RESIZE_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "customFont"; }
            };
            _customFont = null;
        }
        return customFont;
    }

    /**
     * Returns a list of path elements that define the countries
     * @return a list of path elements that define the countries
     */
    public Map<String, List<CountryPath>> getCountryPaths() {
        if (null == countryProperties) { countryProperties = readProperties(COUNTRY_PROPERTIES); }
        if (null == countryPaths) {
            countryPaths = new HashMap<>();
            countryProperties.forEach((key, value) -> {
                String            name     = key.toString();
                List<CountryPath> pathList = new ArrayList<>();
                for (String path : value.toString().split(";")) { pathList.add(new CountryPath(name, path)); }
                countryPaths.put(name, pathList);
            });
        }
        return countryPaths;
    }

    /**
     * Returns true if a gradient defined by gradientStops will be
     * used to stroke the line in the SparklineTileSkin.
     * @return true if a gradient defined by gradientStops will be used to stroke the line in the SparklineTileSkin
     */
    public boolean isStrokeWithGradient() { return null == strokeWithGradient ? _strokeWithGradient : strokeWithGradient.get(); }
    /**
     * Defines the usage of a gradient defined by gradientStops to stroke the line
     * in the SparklineTileSkin
     * @param STROKE_WITH_GRADIENT
     */
    public void setStrokeWithGradient(final boolean STROKE_WITH_GRADIENT) {
        if (null == strokeWithGradient) {
            _strokeWithGradient = STROKE_WITH_GRADIENT;
            fireTileEvent(REDRAW_EVENT);
        } else {
            strokeWithGradient.set(STROKE_WITH_GRADIENT);
        }
    }
    public BooleanProperty strokeWithGradientProperty() {
        if (null == strokeWithGradient) {
            strokeWithGradient = new BooleanPropertyBase(_strokeWithGradient) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "strokeWithGradient"; }
            };
        }
        return  strokeWithGradient;
    }

    public DarkSky getDarkSky() { return darkSky; }
    public void setDarkSky(final DarkSky DARK_SKY) {
        darkSky = DARK_SKY;
        fireTileEvent(REDRAW_EVENT);
    }

    public void updateWeather() {
        if (null == darkSky) return;
        if (darkSky.update()) {
            fireTileEvent(REDRAW_EVENT);
        } else {
            //System.out.println("Wrong or missing DarkSky API key");
            throw new IllegalArgumentException("Do you use a valid DarkSKY API key?");
        }
    }

    private Properties readProperties(final String FILE_NAME) {
        final ClassLoader LOADER     = Thread.currentThread().getContextClassLoader();
        final Properties  PROPERTIES = new Properties();
        try(InputStream resourceStream = LOADER.getResourceAsStream(FILE_NAME)) {
            PROPERTIES.load(resourceStream);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return PROPERTIES;
    }

    /**
     * Calling this method will check the current time against all Alarm
     * objects in alarms. The Alarm object will fire events in case the
     * time is after the alarm time.
     * @param TIME
     */
    private void checkAlarms(final ZonedDateTime TIME) {
        alarmsToRemove.clear();
        for (Alarm alarm : alarms) {
            final ZonedDateTime ALARM_TIME = alarm.getTime();
            switch (alarm.getRepetition()) {
                case ONCE:
                    if (TIME.isAfter(ALARM_TIME)) {
                        if (alarm.isArmed()) {
                            fireAlarmEvent(new AlarmEvent(alarm));
                            alarm.executeCommand();
                        }
                        alarmsToRemove.add(alarm);
                    }
                    break;
                case HALF_HOURLY:
                    if ((ALARM_TIME.getMinute() == TIME.getMinute() ||
                         ALARM_TIME.plusMinutes(30).getMinute() == TIME.getMinute()) &&
                        ALARM_TIME.getSecond() == TIME.getSecond()) {
                        if (alarm.isArmed()) {
                            fireAlarmEvent(new AlarmEvent(alarm));
                            alarm.executeCommand();
                        }
                    }
                    break;
                case HOURLY:
                    if (ALARM_TIME.getMinute() == TIME.getMinute() &&
                        ALARM_TIME.getSecond() == TIME.getSecond()) {
                        if (alarm.isArmed()) {
                            fireAlarmEvent(new AlarmEvent(alarm));
                            alarm.executeCommand();
                        }
                    }
                    break;
                case DAILY:
                    if (ALARM_TIME.getHour()   == TIME.getHour() &&
                        ALARM_TIME.getMinute() == TIME.getMinute() &&
                        ALARM_TIME.getSecond() == TIME.getSecond()) {
                        if (alarm.isArmed()) {
                            fireAlarmEvent(new AlarmEvent(alarm));
                            alarm.executeCommand();
                        }
                    }
                    break;
                case WEEKLY:
                    if (ALARM_TIME.getDayOfWeek() == TIME.getDayOfWeek() &&
                        ALARM_TIME.getHour()      == TIME.getHour() &&
                        ALARM_TIME.getMinute()    == TIME.getMinute() &&
                        ALARM_TIME.getSecond()    == TIME.getSecond()) {
                        if (alarm.isArmed()) {
                            fireAlarmEvent(new AlarmEvent(alarm));
                            alarm.executeCommand();
                        }
                    }
                    break;
            }
        }
        for (Alarm alarm : alarmsToRemove) {
            removeAlarm(alarm);
        }
    }

    private void tick() { Platform.runLater(() -> {
        if (isAnimated()) return;
        ZonedDateTime oldTime = getTime();
        setTime(getTime().plus(java.time.Duration.ofMillis(updateInterval)));
        ZonedDateTime now = time.get();
        if (isAlarmsEnabled()) checkAlarms(now);
        if (getCheckSectionsForValue()) {
            int listSize = timeSections.size();
            for (TimeSection timeSection : timeSections) { timeSection.checkForTimeAndDate(now); }
        }

        if (timeEventListenerList.isEmpty()) return;
        // Fire TimeEvents
        if (oldTime.getSecond() != now.getSecond()) fireTimeEvent(new TimeEvent(Tile.this, now, TimeEventType.SECOND));
        if (oldTime.getMinute() != now.getMinute()) fireTimeEvent(new TimeEvent(Tile.this, now, TimeEventType.MINUTE));
        if (oldTime.getHour() != now.getHour()) fireTimeEvent(new TimeEvent(Tile.this, now, TimeEventType.HOUR));
    }); }

    
    // ******************** Scheduled tasks ***********************************
    private synchronized void enableTickExecutorService() {
        if (null == periodicTickExecutorService) {
            periodicTickExecutorService = new ScheduledThreadPoolExecutor(1, getThreadFactory("TileTick", true));
        }
    }
    private synchronized void scheduleTickTask() {
        enableTickExecutorService();
        stopTask(periodicTickTask);

        updateInterval = (isDiscreteMinutes() && isDiscreteSeconds()) ? LONG_INTERVAL : SHORT_INTERVAL;
        periodicTickTask = periodicTickExecutorService.scheduleAtFixedRate(() -> tick(), 0, updateInterval, TimeUnit.MILLISECONDS);
    }

    private static ThreadFactory getThreadFactory(final String THREAD_NAME, final boolean IS_DAEMON) {
        return runnable -> {
            Thread thread = new Thread(runnable, THREAD_NAME);
            thread.setDaemon(IS_DAEMON);
            return thread;
        };
    }

    private void stopTask(ScheduledFuture<?> task) {
        if (null == task) return;
        task.cancel(true);
        task = null;
    }

    /**
     * Calling this method will stop all threads. This is needed when using
     * JavaFX on mobile devices when the device goes to sleep mode.
     */
    public void stop() {
        if (null != periodicTickTask) { stopTask(periodicTickTask); }
        if (null != periodicTickExecutorService) { periodicTickExecutorService.shutdownNow(); }
    }

    private void createShutdownHook() { Runtime.getRuntime().addShutdownHook(new Thread(() -> stop())); }
    
    
    // ******************** Event handling ************************************
    public void setOnTileEvent(final TileEventListener LISTENER) { addTileEventListener(LISTENER); }
    public void addTileEventListener(final TileEventListener LISTENER) { if (!listenerList.contains(LISTENER)) listenerList.add(LISTENER); }
    public void removeTileEventListener(final TileEventListener LISTENER) { if (listenerList.contains(LISTENER)) listenerList.remove(LISTENER); }

    public void fireTileEvent(final TileEvent EVENT) {
        for (TileEventListener listener : listenerList) { listener.onTileEvent(EVENT); }
    }

    
    public void setOnAlarm(final AlarmEventListener LISTENER) { addAlarmEventListener(LISTENER); }
    public void addAlarmEventListener(final AlarmEventListener LISTENER) { if (!alarmListenerList.contains(LISTENER)) alarmListenerList.add(LISTENER); }
    public void removeAlarmEventListener(final AlarmEventListener LISTENER) { if (alarmListenerList.contains(LISTENER)) alarmListenerList.remove(LISTENER); }

    public void fireAlarmEvent(final AlarmEvent EVENT) {
        for (AlarmEventListener listener : alarmListenerList) { listener.onAlarmEvent(EVENT); }
    }


    public void setOnTimeEvent(final TimeEventListener LISTENER) { addTimeEventListener(LISTENER); }
    public void addTimeEventListener(final TimeEventListener LISTENER) { if (!timeEventListenerList.contains(LISTENER)) timeEventListenerList.add(LISTENER); }
    public void removeTimeEventListener(final TimeEventListener LISTENER) { if (timeEventListenerList.contains(LISTENER)) timeEventListenerList.remove(LISTENER); }

    public void fireTimeEvent(final TimeEvent EVENT) {
        for (TimeEventListener listener : timeEventListenerList) { listener.onTimeEvent(EVENT); }
    }


    public void setOnSwitchPressed(final EventHandler<SwitchEvent> HANDLER) { addEventHandler(SwitchEvent.SWITCH_PRESSED, HANDLER); }
    public void removeOnSwitchPressed(final EventHandler<SwitchEvent> HANDLER) { removeEventHandler(SwitchEvent.SWITCH_PRESSED, HANDLER); }

    public void setOnSwitchReleased(final EventHandler<SwitchEvent> HANDLER) { addEventHandler(SwitchEvent.SWITCH_RELEASED, HANDLER); }
    public void removeOnSwitchReleased(final EventHandler<SwitchEvent> HANDLER) { removeEventHandler(SwitchEvent.SWITCH_RELEASED, HANDLER); }


    // ******************** Style related *************************************
    @Override protected Skin createDefaultSkin() {
        switch (skinType) {
            case AREA_CHART       : return new AreaChartTileSkin(Tile.this);
            case BAR_CHART        : return new BarChartTileSkin(Tile.this);
            case LINE_CHART       : return new LineChartTileSkin(Tile.this);
            case CLOCK            : return new ClockTileSkin(Tile.this);
            case GAUGE            : return new GaugeTileSkin(Tile.this);
            case HIGH_LOW         : return new HighLowTileSkin(Tile.this);
            case PERCENTAGE       : return new PercentageTileSkin(Tile.this);
            case PLUS_MINUS       : return new PlusMinusTileSkin(Tile.this);
            case SLIDER           : return new SliderTileSkin(Tile.this);
            case SPARK_LINE       : return new SparkLineTileSkin(Tile.this);
            case SWITCH           : return new SwitchTileSkin(Tile.this);
            case WORLDMAP         : return new WorldMapTileSkin(Tile.this);
            case TIMER_CONTROL    : return new TimerControlTileSkin(Tile.this);
            case NUMBER           : return new NumberTileSkin(Tile.this);
            case TEXT             : return new TextTileSkin(Tile.this);
            case WEATHER          : return new WeatherTileSkin(Tile.this);
            case TIME             : return new TimeTileSkin(Tile.this);
            case CUSTOM           : return new CustomTileSkin(Tile.this);
            case LEADER_BOARD     : return new LeaderBoardTileSkin(Tile.this);
            case MAP              : return new MapTileSkin(Tile.this);
            case RADIAL_CHART     : return new RadialChartTileSkin(Tile.this);
            case DONUT_CHART      : return new DonutChartTileSkin(Tile.this);
            case CIRCULAR_PROGRESS: return new CircularProgressTileSkin(Tile.this);
            case STOCK            : return new StockTileSkin(Tile.this);
            default               : return new TileSkin(Tile.this);
        }
    }

    @Override public String getUserAgentStylesheet() { return getClass().getResource("tilesfx.css").toExternalForm(); }

    private void presetTileParameters(final SkinType SKIN_TYPE) {
        switch (SKIN_TYPE) {
            case AREA_CHART:
                break;
            case BAR_CHART:
                break;
            case LINE_CHART:
                break;
            case CLOCK:
                break;
            case GAUGE:
                setAnimated(true);
                setTickLabelDecimals(0);
                setBarColor(FOREGROUND);
                setThresholdColor(Tile.BLUE);
                setThresholdVisible(true);
                break;
            case HIGH_LOW:
                setMaxValue(Double.MAX_VALUE);
                setDecimals(2);
                setTickLabelDecimals(1);
                break;
            case PERCENTAGE:
                setAnimated(true);
                setThresholdColor(GRAY);
                setTickLabelDecimals(0);
                break;
            case PLUS_MINUS:
                break;
            case SLIDER:
                setBarBackgroundColor(Tile.FOREGROUND);
                break;
            case SPARK_LINE:
                setTextVisible(false);
                setAnimated(false);
                setAveragingEnabled(true);
                setAveragingPeriod(10);
                setDecimals(0);
                setTickLabelDecimals(0);
                break;
            case SWITCH:
                break;
            case WORLDMAP:
                setPrefSize(380, 250);
                break;
            case TIMER_CONTROL:
                setSectionsVisible(true);
                setHighlightSections(true);
                setCheckSectionsForValue(true);
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
                setAnimated(true);
                break;
            case DONUT_CHART:
                setAnimated(true);
                break;
            case CIRCULAR_PROGRESS:
                setBarBackgroundColor(getBackgroundColor().brighter());
                setAnimated(true);
                break;
            case STOCK:
                setAnimated(false);
                setAveragingPeriod(720);
                setAveragingEnabled(true);
                setDecimals(2);
                setTickLabelDecimals(2);
                setThresholdColor(GRAY);
                setTextVisible(false);
                break;
            default:
                break;
        }
    }

    public SkinType getSkinType() { return skinType; }
    public void setSkinType(final SkinType SKIN_TYPE) {
        skinType = SKIN_TYPE;
        switch (SKIN_TYPE) {
            case AREA_CHART       : setSkin(new AreaChartTileSkin(Tile.this)); break;
            case BAR_CHART        : setSkin(new BarChartTileSkin(Tile.this)); break;
            case LINE_CHART       : setSkin(new LineChartTileSkin(Tile.this)); break;
            case CLOCK            : setSkin(new ClockTileSkin(Tile.this)); break;
            case GAUGE            : setSkin(new GaugeTileSkin(Tile.this)); break;
            case HIGH_LOW         : setSkin(new HighLowTileSkin(Tile.this)); break;
            case PERCENTAGE       : setSkin(new PercentageTileSkin(Tile.this)); break;
            case PLUS_MINUS       : setSkin(new PlusMinusTileSkin(Tile.this)); break;
            case SLIDER           : setSkin(new SliderTileSkin(Tile.this)); break;
            case SPARK_LINE       : setSkin(new SparkLineTileSkin(Tile.this)); break;
            case SWITCH           : setSkin(new SwitchTileSkin(Tile.this)); break;
            case WORLDMAP         : setSkin(new WorldMapTileSkin(Tile.this)); break;
            case TIMER_CONTROL    : setSkin(new TimerControlTileSkin(Tile.this)); break;
            case NUMBER           : setSkin(new NumberTileSkin(Tile.this)); break;
            case TEXT             : setSkin(new TextTileSkin(Tile.this)); break;
            case WEATHER          : setSkin(new WeatherTileSkin(Tile.this)); break;
            case TIME             : setSkin(new TimeTileSkin(Tile.this)); break;
            case CUSTOM           : setSkin(new CustomTileSkin(Tile.this)); break;
            case LEADER_BOARD     : setSkin(new LeaderBoardTileSkin(Tile.this)); break;
            case RADIAL_CHART     : setSkin(new RadialChartTileSkin(Tile.this)); break;
            case DONUT_CHART      : setSkin(new DonutChartTileSkin(Tile.this)); break;
            case CIRCULAR_PROGRESS: setSkin(new CircularProgressTileSkin(Tile.this)); break;
            case STOCK            : setSkin(new StockTileSkin(Tile.this)); break;
            default               : setSkin(new TileSkin(Tile.this)); break;
        }
        fireTileEvent(RESIZE_EVENT);
        presetTileParameters(SKIN_TYPE);
    }
}
