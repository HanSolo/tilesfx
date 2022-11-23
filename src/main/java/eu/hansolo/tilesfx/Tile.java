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

import eu.hansolo.tilesfx.chart.ChartData;
import eu.hansolo.tilesfx.chart.RadarChartMode;
import eu.hansolo.tilesfx.chart.SunburstChart;
import eu.hansolo.tilesfx.chart.TilesFXSeries;
import eu.hansolo.tilesfx.colors.Bright;
import eu.hansolo.tilesfx.events.AlarmEvent;
import eu.hansolo.tilesfx.events.AlarmEventListener;
import eu.hansolo.tilesfx.events.BoundsEventListener;
import eu.hansolo.tilesfx.events.SwitchEvent;
import eu.hansolo.tilesfx.events.TileEvent;
import eu.hansolo.tilesfx.events.TileEvent.EventType;
import eu.hansolo.tilesfx.events.TileEventListener;
import eu.hansolo.tilesfx.events.TimeEvent;
import eu.hansolo.tilesfx.events.TimeEvent.TimeEventType;
import eu.hansolo.tilesfx.events.TimeEventListener;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.skins.*;
import eu.hansolo.tilesfx.tools.Country;
import eu.hansolo.tilesfx.tools.CountryGroup;
import eu.hansolo.tilesfx.tools.CountryPath;
import eu.hansolo.tilesfx.tools.Helper;
import eu.hansolo.tilesfx.tools.InfoRegion;
import eu.hansolo.tilesfx.tools.Location;
import eu.hansolo.tilesfx.tools.LowerRightRegion;
import eu.hansolo.tilesfx.tools.MatrixIcon;
import eu.hansolo.tilesfx.tools.MovingAverage;
import eu.hansolo.tilesfx.tools.NotifyRegion;
import eu.hansolo.tilesfx.tools.Rank;
import eu.hansolo.tilesfx.tools.SectionComparator;
import eu.hansolo.tilesfx.tools.TimeData;
import eu.hansolo.tilesfx.tools.TimeSectionComparator;
import javafx.animation.Animation.Status;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.NamedArg;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.IntegerPropertyBase;
import javafx.beans.property.LongProperty;
import javafx.beans.property.LongPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.StringPropertyBase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.SimpleStyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.StyleablePropertyFactory;
import javafx.event.EventHandler;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static eu.hansolo.tilesfx.tools.Helper.clamp;
import static eu.hansolo.tilesfx.tools.MovingAverage.MAX_PERIOD;


/**
 * Created by hansolo on 19.12.16.
 */
public class Tile extends Control {
    public enum SkinType { SMOOTHED_CHART("ChartTileSkin"), BAR_CHART("BarChartTileSkin"),
                           CLOCK("ClockTileSkin"), GAUGE("GaugeTileSkin"), GAUGE2("Gauge2TileSkin"),
                           HIGH_LOW("HighLowTileSkin)"), PERCENTAGE("PercentageTileSkin"),
                           PLUS_MINUS("PlusMinusTileSkin"), SLIDER("SliderTileSkin"),
                           SPARK_LINE("SparkLineTileSkin"), SWITCH("SwitchTileSkin"),
                           WORLDMAP("WorldMapTileSkin"), TIMER_CONTROL("TimerControlTileSkin"),
                           NUMBER("NumberTileSkin"), TEXT("TextTileSkin"),
                           TIME("TimeTileSkin"),
                           CUSTOM("CustomTileSkin"), CUSTOM_SCROLLABLE("CustomScrollableTileSkin"),
                           LEADER_BOARD("LeaderBoardTileSkin"),
                           MAP("MapTileSkin"), RADIAL_CHART("RadialChartTileSkin"), DONUT_CHART("DonutChartTileSkin"),
                           CIRCULAR_PROGRESS("CircularProgressTileSkin"), STOCK("StockTileSkin"),
                           CIRCLE_PROGRESS_NUM("CircleProgressTileNumberSkin"),
                           GAUGE_SPARK_LINE("GaugeSparkLineTileSkin"), SMOOTH_AREA_CHART("SmoothAreaChartTileSkin"),
                           RADAR_CHART("RadarChartTileSkin"), RADAR_NODE_CHART("RadarNodeChartTileSkin"), COUNTRY("CountryTileSkin"),
                           CHARACTER("CharacterTileSkin"), FLIP("FlipTileSkin"), SWITCH_SLIDER("SwitchSliderTileSkin"),
                           DATE("DateTileSkin"), CALENDAR("CalendarTileSkin"), SUNBURST("SunburstTileSkin"),
                           MATRIX("MatrixTileSkin"), MATRIX_ICON("MatrixIconTileSkin"),
                           RADIAL_PERCENTAGE("RadialPercentageTileSkin"),
                           STATUS("StatusTileSkin"), BAR_GAUGE("BarGaugeTileSkin"),
                           IMAGE("ImageTileSkin"), IMAGE_COUNTER("ImageCounterTileSkin"),
                           TIMELINE("TimelineTileSkin"), CLUSTER_MONITOR("ClusterMonitorTileSkin"),
                           LED("LedTileSkin"), COUNTDOWN_TIMER("CountdownTimerTileSkin"),
                           CYCLE_STEP("CycleStepTileSkin"), COLOR("ColorTileSkin"),
                           FLUID("FluidTileSkin"), FIRE_SMOKE("FireSmokeTileSkin"),
                           TURNOVER("TurnoverTileSkin"), RADIAL_DISTRIBUTION("RadialDistributionTileSkin");

        public final String CLASS_NAME;
        SkinType(final String CLASS_NAME) {
            this.CLASS_NAME = CLASS_NAME;
        }
    }
    public enum TextSize {
        SMALL(0.04),
        SMALLER(0.05),
        NORMAL(0.06),
        BIGGER(0.08);

        public final double factor;

        TextSize(final double FACTOR) {
            factor = FACTOR;
        }
    }
    public enum TileColor {
        GRAY(Color.rgb(139,144,146), "GRAY"),
        RED(Color.rgb(229, 80, 76), "RED"),
        LIGHT_RED(Color.rgb(255, 84, 56), "LIGHT_RED"),
        GREEN(Color.rgb(143, 198, 94), "GREEN"),
        LIGHT_GREEN(Color.rgb(132, 228, 50), "LIGHT_GREEN"),
        BLUE(Color.rgb(55, 179, 252), "BLUE"),
        DARK_BLUE(Color.rgb(55, 94, 252), "DARK_BLUE"),
        ORANGE(Color.rgb(237, 162, 57), "ORANGE"),
        YELLOW_ORANGE(Color.rgb(229, 198, 76), "YELLOW_ORANGE"),
        YELLOW(Color.rgb(229, 229, 76), "YELLOW"),
        MAGENTA(Color.rgb(198, 75, 232), "MAGENTA"),
        PINK(Color.rgb(233, 14, 139), "PINK");

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
    public enum ChartType { LINE, AREA }
    public enum ImageMask {
        NONE, ROUND, RECTANGULAR
    }
    public enum ItemSorting {
        NONE, ASCENDING, DESCENDING
    }
    public enum ItemSortingTopic {
        VALUE, TIMESTAMP, DURATION
    }


    public static final  Color                          BACKGROUND                     = Color.rgb(42, 42, 42); // #2a2a2a
    public static final  Color                          FOREGROUND                     = Color.rgb(223, 223, 223); // #dfdfdf
    public static final  Color                          GRAY                           = TileColor.GRAY.color;
    public static final  Color                          RED                            = TileColor.RED.color;
    public static final  Color                          LIGHT_RED                      = TileColor.LIGHT_RED.color;
    public static final  Color                          GREEN                          = TileColor.GREEN.color;
    public static final  Color                          LIGHT_GREEN                    = TileColor.LIGHT_GREEN.color;
    public static final  Color                          BLUE                           = TileColor.BLUE.color;
    public static final  Color                          DARK_BLUE                      = TileColor.DARK_BLUE.color;
    public static final  Color                          ORANGE                         = TileColor.ORANGE.color;
    public static final  Color                          YELLOW_ORANGE                  = TileColor.YELLOW_ORANGE.color;
    public static final  Color                          YELLOW                         = TileColor.YELLOW.color;
    public static final  Color                          MAGENTA                        = TileColor.MAGENTA.color;
    public static final  Color                          PINK                           = TileColor.PINK.color;
    public static final  int                            SHORT_INTERVAL                 = 20;
    public static final  int                            LONG_INTERVAL                  = 1000;
    public static final  java.time.Duration             DEFAULT_TIME_PERIOD            = java.time.Duration.ofMinutes(1);
    public static final  TimeUnit                       DEFAULT_TIME_PERIOD_RESOLUTION = TimeUnit.SECONDS;
    private static final int                            MAX_NO_OF_DECIMALS             = 3;

    private final        TileEvent                      SHOW_NOTIFY_REGION_EVENT       = new TileEvent(EventType.SHOW_NOTIFY_REGION);
    private final        TileEvent                      HIDE_NOTIFY_REGION_EVENT       = new TileEvent(EventType.HIDE_NOTIFY_REGION);
    private final        TileEvent                      SHOW_INFO_REGION_EVENT         = new TileEvent(EventType.SHOW_INFO_REGION);
    private final        TileEvent                      HIDE_INFO_REGION_EVENT         = new TileEvent(EventType.HIDE_INFO_REGION);
    private final        TileEvent                      SHOW_LOWER_RIGHT_REGION_EVENT  = new TileEvent(EventType.SHOW_LOWER_RIGHT_REGION);
    private final        TileEvent                      HIDE_LOWER_RIGHT_REGION_EVENT  = new TileEvent(EventType.HIDE_LOWER_RIGHT_REGION);
    private final        TileEvent                      EXCEEDED_THRESHOLD_EVENT       = new TileEvent(EventType.THRESHOLD_EXCEEDED);
    private final        TileEvent                      UNDERRUN_THRESHOLD_EVENT       = new TileEvent(EventType.THRESHOLD_UNDERRUN);
    private final        TileEvent                      EXCEEDED_LOWER_THRESHOLD_EVENT = new TileEvent(EventType.LOWER_THRESHOLD_EXCEEDED);
    private final        TileEvent                      UNDERRUN_LOWER_THRESHOLD_EVENT = new TileEvent(EventType.LOWER_THRESHOLD_UNDERRUN);
    private final        TileEvent                      MAX_VALUE_EXCEEDED             = new TileEvent(EventType.MAX_VALUE_EXCEEDED);
    private final        TileEvent                      MIN_VALUE_UNDERRUN             = new TileEvent(EventType.MIN_VALUE_UNDERRUN);
    private final        TileEvent                      VALUE_IN_RANGE                 = new TileEvent(EventType.VALUE_IN_RANGE);
    private final        TileEvent                      RECALC_EVENT                   = new TileEvent(EventType.RECALC);
    private final        TileEvent                      REDRAW_EVENT                   = new TileEvent(EventType.REDRAW);
    private final        TileEvent                      RESIZE_EVENT                   = new TileEvent(EventType.RESIZE);
    private final        TileEvent                      VISIBILITY_EVENT               = new TileEvent(EventType.VISIBILITY);
    private final        TileEvent                      SECTION_EVENT                  = new TileEvent(EventType.SECTION);
    private final        TileEvent                      SERIES_EVENT                   = new TileEvent(EventType.SERIES);
    private final        TileEvent                      DATA_EVENT                     = new TileEvent(EventType.DATA);
    private final        TileEvent                      ALERT_EVENT                    = new TileEvent(EventType.ALERT);
    private final        TileEvent                      VALUE_EVENT                    = new TileEvent(EventType.VALUE);
    private final        TileEvent                      FINISHED_EVENT                 = new TileEvent(EventType.FINISHED);
    private final        TileEvent                      GRAPHIC_EVENT                  = new TileEvent(EventType.GRAPHIC);
    private final        TileEvent                      AVERAGING_EVENT                = new TileEvent(EventType.AVERAGING);
    private final        TileEvent                      TIME_PERIOD_EVENT              = new TileEvent(EventType.TIME_PERIOD);
    private final        TileEvent                      LOCATION_EVENT                 = new TileEvent(EventType.LOCATION);
    private final        TileEvent                      TRACK_EVENT                    = new TileEvent(EventType.TRACK);
    private final        TileEvent                      MAP_PROVIDER_EVENT             = new TileEvent(EventType.MAP_PROVIDER);
    private final        TileEvent                      FLIP_START_EVENT               = new TileEvent(EventType.FLIP_START);
    private final        TileEvent                      BKG_IMAGE_EVENT                = new TileEvent(EventType.BACKGROUND_IMAGE);
    private final        TileEvent                      REGIONS_ON_TOP_EVENT           = new TileEvent(EventType.REGIONS_ON_TOP);
    private final        TileEvent                      INFO_REGION_HANDLER_EVENT      = new TileEvent(EventType.INFO_REGION_HANDLER);
    private final        TileEvent                      CLEAR_DATA_EVENT               = new TileEvent(EventType.CLEAR_DATA);
    private final        TileEvent                      HIGHLIGHT_SECTIONS             = new TileEvent(EventType.HIGHLIGHT_SECTIONS);
    private final        TileEvent                      ANIMATED_ON_EVENT              = new TileEvent(EventType.ANIMATED_ON);
    private final        TileEvent                      ANIMATED_OFF_EVENT             = new TileEvent(EventType.ANIMATED_OFF);

    private static final StyleablePropertyFactory<Tile> FACTORY                        = new StyleablePropertyFactory<>(Region.getClassCssMetaData());
    private static final CssMetaData<Tile, Color>       THUMB_COLOR                    = FACTORY.createColorCssMetaData("-thumb-color", s -> s.thumbColor, Color.rgb(223, 223, 223, 0.5), false);

    private static       String                         userAgentStyleSheet;

    // Tile events
    private              Queue<TileEvent>               tileEventQueue                 = new LinkedBlockingQueue<>();
    private              List<TileEventListener>        tileEventListeners             = new CopyOnWriteArrayList<>();
    private              List<AlarmEventListener>       alarmEventListeners            = new CopyOnWriteArrayList<>();
    private              List<TimeEventListener>        timeEventListeners             = new CopyOnWriteArrayList<>();
    private              List<BoundsEventListener>      boundsListeners                = new CopyOnWriteArrayList<>();

    private              BooleanBinding                 showing;

    // Data related
    private DoubleProperty                                value;
    private DoubleProperty                                oldValue;      // last value
    private DoubleProperty                                currentValue;
    private DoubleProperty                                formerValue;   // last current value
    private double                                        _minValue;
    private DoubleProperty                                minValue;
    private double                                        _maxValue;
    private DoubleProperty                                maxValue;
    private double                                        _range;
    private DoubleProperty                                range;
    private double                                        _threshold;
    private DoubleProperty                                threshold;
    private double                                        _lowerThreshold;
    private DoubleProperty                                lowerThreshold;
    private double                                        _referenceValue;
    private DoubleProperty                                referenceValue;
    private boolean                                       _autoReferenceValue;
    private BooleanProperty                               autoReferenceValue;
    private String                                        _title;
    private StringProperty                                title;
    private TextAlignment                                 _titleAlignment;
    private ObjectProperty<TextAlignment>                 titleAlignment;
    private String                                        _description;
    private StringProperty                                description;
    private Pos                                           _descriptionAlignment;
    private ObjectProperty<Pos>                           descriptionAlignment;
    private String                                        _unit;
    private StringProperty                                unit;
    private String                                        oldFlipText;
    private String                                        _flipText;
    private StringProperty                                flipText;
    private String                                        _text;
    private StringProperty                                text;
    private TextAlignment                                 _textAlignment;
    private ObjectProperty<TextAlignment>                 textAlignment;
    private boolean                                       _active;
    private BooleanProperty                               active;
    private boolean                                       _averagingEnabled;
    private BooleanProperty                               averagingEnabled;
    private int                                           _averagingPeriod;
    private IntegerProperty                               averagingPeriod;
    private java.time.Duration                            _timePeriod;
    private ObjectProperty<java.time.Duration>            timePeriod;
    private java.time.Duration                            _maxTimePeriod;
    private ObjectProperty<java.time.Duration>            maxTimePeriod;
    private TimeUnit                                      _timePeriodResolution;
    private ObjectProperty<TimeUnit>                      timePeriodResolution;
    private MovingAverage                                 movingAverage;
    private boolean                                       _fixedYScale;
    private BooleanProperty                               fixedYScale;
    private ObservableList<Section>                       sections;
    private ObservableList<TilesFXSeries<String, Number>> series;
    private List<Stop>                                    gradientStops;
    private ObjectProperty<ZonedDateTime>                 time;
    private LongProperty                                  currentTime;
    private ZoneId                                        zoneId;
    private int                                           updateInterval;
    private ObservableList<TimeSection>                   timeSections;
    private LocalTime                                     _duration;
    private ObjectProperty<LocalTime>                     duration;
    private ObservableList<BarChartItem>                  barChartItems;
    private ObservableList<LeaderBoardItem>               leaderBoardItems;
    private ObjectProperty<Image>                         image;
    private ImageMask                                     _imageMask;
    private ObjectProperty<ImageMask>                     imageMask;
    private ObjectProperty<Node>                          graphic;
    private ObjectProperty<SVGPath>                       svgPath;
    private Location                                      _currentLocation;
    private ObjectProperty<Location>                      currentLocation;
    private ObservableList<Location>                      poiList;
    private ObservableList<ChartData>                     chartDataList;
    private List<Location>                                track;
    private TileColor                                     _trackColor;
    private ObjectProperty<TileColor>                     trackColor;
    private MapProvider                                   _mapProvider;
    private ObjectProperty<MapProvider>                   mapProvider;
    private List<String>                                  characterList;
    private long                                          flipTimeInMS;
    private SunburstChart                                 sunburstChart;
    private ItemSorting                                   _itemSorting;
    private ObjectProperty<ItemSorting>                   itemSorting;
    private ItemSortingTopic                              _itemSortingTopic;
    private ObjectProperty<ItemSortingTopic>              itemSortingTopic;

    // UI related
    private StyleableProperty<Color>                      thumbColor;
    private boolean                                       _flatUI;
    private BooleanProperty                               flatUI;
    private SkinType                                      skinType;
    private TextSize                                      _textSize;
    private ObjectProperty<TextSize>                      textSize;
    private boolean                                       _roundedCorners;
    private BooleanProperty                               roundedCorners;
    private boolean                                       _startFromZero;
    private BooleanProperty                               startFromZero;
    private boolean                                       _returnToZero;
    private BooleanProperty                               returnToZero;
    private double                                        _minMeasuredValue;
    private DoubleProperty                                minMeasuredValue;
    private double                                        _maxMeasuredValue;
    private DoubleProperty                                maxMeasuredValue;
    private boolean                                       _minMeasuredValueVisible;
    private BooleanProperty                               minMeasuredValueVisible;
    private boolean                                       _maxMeasuredValueVisible;
    private BooleanProperty                               maxMeasuredValueVisible;
    private boolean                                       _oldValueVisible;
    private BooleanProperty                               oldValueVisible;
    private boolean                                       _valueVisible;
    private BooleanProperty                               valueVisible;
    private Color                                         _foregroundColor;
    private ObjectProperty<Color>                         foregroundColor;
    private Color                                         _backgroundColor;
    private ObjectProperty<Color>                         backgroundColor;
    private Color                                         _borderColor;
    private ObjectProperty<Color>                         borderColor;
    private double                                        _borderWidth;
    private DoubleProperty                                borderWidth;
    private Color                                         _activeColor;
    private ObjectProperty<Color>                         activeColor;
    private Color                                         _knobColor;
    private ObjectProperty<Color>                         knobColor;
    private boolean                                       _animated;
    private BooleanProperty                               animated;
    private long                                          animationDuration;
    private long                                          pauseDuration;
    private double                                        _startAngle;
    private DoubleProperty                                startAngle;
    private double                                        _angleRange;
    private DoubleProperty                                angleRange;
    private double                                        _angleStep;
    private DoubleProperty                                angleStep;
    private boolean                                       _autoScale;
    private BooleanProperty                               autoScale;
    private boolean                                       _shadowsEnabled;
    private BooleanProperty                               shadowsEnabled;
    private Locale                                        _locale;
    private ObjectProperty<Locale>                        locale;
    private NumberFormat                                  _numberFormat;
    private ObjectProperty<NumberFormat>                  numberFormat;
    private int                                           _decimals;
    private IntegerProperty                               decimals;
    private int                                           _tickLabelDecimals;
    private IntegerProperty                               tickLabelDecimals;
    private boolean                                       _tickLabelsXVisible;
    private BooleanProperty                               tickLabelsXVisible;
    private boolean                                       _tickLabelsYVisible;
    private BooleanProperty                               tickLabelsYVisible;
    private boolean                                       _minValueVisible;
    private BooleanProperty                               minValueVisible;
    private boolean                                       _maxValueVisible;
    private BooleanProperty                               maxValueVisible;
    private Color                                         _needleColor;
    private ObjectProperty<Color>                         needleColor;
    private Color                                         _barColor;
    private ObjectProperty<Color>                         barColor;
    private Color                                         _barBackgroundColor;
    private ObjectProperty<Color>                         barBackgroundColor;
    private Color                                         _titleColor;
    private ObjectProperty<Color>                         titleColor;
    private Color                                         _descriptionColor;
    private ObjectProperty<Color>                         descriptionColor;
    private Color                                         _unitColor;
    private ObjectProperty<Color>                         unitColor;
    private Color                                         _valueColor;
    private ObjectProperty<Color>                         valueColor;
    private Color                                         _thresholdColor;
    private ObjectProperty<Color>                         thresholdColor;
    private Color                                         _lowerThresholdColor;
    private ObjectProperty<Color>                         lowerThresholdColor;
    private boolean                                       _checkSectionsForValue;
    private BooleanProperty                               checkSectionsForValue;
    private boolean                                       _checkThreshold;
    private BooleanProperty                               checkThreshold;
    private boolean                                       _checkLowerThreshold;
    private BooleanProperty                               checkLowerThreshold;
    private boolean                                       _innerShadowEnabled;
    private BooleanProperty                               innerShadowEnabled;
    private boolean                                       _thresholdVisible;
    private BooleanProperty                               thresholdVisible;
    private boolean                                       _lowerThresholdVisible;
    private BooleanProperty                               lowerThresholdVisible;
    private boolean                                       _averageVisible;
    private BooleanProperty                               averageVisible;
    private boolean                                       _sectionsVisible;
    private BooleanProperty                               sectionsVisible;
    private boolean                                       _sectionsAlwaysVisible;
    private BooleanProperty                               sectionsAlwaysVisible;
    private boolean                                       _sectionTextVisible;
    private BooleanProperty                               sectionTextVisible;
    private boolean                                       _sectionIconsVisible;
    private BooleanProperty                               sectionIconsVisible;
    private boolean                                       _highlightSections;
    private BooleanProperty                               highlightSections;
    private Orientation                                   _orientation;
    private ObjectProperty<Orientation>                   orientation;
    private boolean                                       _keepAspect;
    private BooleanProperty                               keepAspect;
    private boolean                                       _customFontEnabled;
    private BooleanProperty                               customFontEnabled;
    private Font                                          _customFont;
    private ObjectProperty<Font>                          customFont;
    private boolean                                       _customDecimalFormatEnabled;
    private BooleanProperty                               customDecimalFormatEnabled;
    private DecimalFormat                                 _customDecimalFormat;
    private ObjectProperty<DecimalFormat>                 customDecimalFormat;
    private boolean                                       _alert;
    private BooleanProperty                               alert;
    private String                                        _alertMessage;
    private StringProperty                                alertMessage;
    private boolean                                       _smoothing;
    private BooleanProperty                               smoothing;
    private double                                        increment;
    private double                                        originalMinValue;
    private double                                        originalMaxValue;
    private double                                        originalThreshold;
    private Timeline                                      timeline;
    private Instant                                       lastCall;
    private boolean                                       withinSpeedLimit;
    private boolean                                       _discreteSeconds;
    private BooleanProperty                               discreteSeconds;
    private boolean                                       _discreteMinutes;
    private BooleanProperty                               discreteMinutes;
    private boolean                                       _discreteHours;
    private BooleanProperty                               discreteHours;
    private boolean                                       _secondsVisible;
    private BooleanProperty                               secondsVisible;
    private boolean                                       _textVisible;
    private BooleanProperty                               textVisible;
    private boolean                                       _dateVisible;
    private BooleanProperty                               dateVisible;
    private boolean                                       _running;
    private BooleanProperty                               running;
    private Color                                         _textColor;
    private ObjectProperty<Color>                         textColor;
    private Color                                         _dateColor;
    private ObjectProperty<Color>                         dateColor;
    private Color                                         _hourTickMarkColor;
    private ObjectProperty<Color>                         hourTickMarkColor;
    private Color                                         _minuteTickMarkColor;
    private ObjectProperty<Color>                         minuteTickMarkColor;
    private Color                                         _alarmColor;
    private ObjectProperty<Color>                         alarmColor;
    private Color                                         _tickLabelColor;
    private ObjectProperty<Color>                         tickLabelColor;
    private Color                                         _tickMarkColor;
    private ObjectProperty<Color>                         tickMarkColor;
    private boolean                                       _hourTickMarksVisible;
    private BooleanProperty                               hourTickMarksVisible;
    private boolean                                       _minuteTickMarksVisible;
    private BooleanProperty                               minuteTickMarksVisible;
    private Color                                         _hourColor;
    private ObjectProperty<Color>                         hourColor;
    private Color                                         _minuteColor;
    private ObjectProperty<Color>                         minuteColor;
    private Color                                         _secondColor;
    private ObjectProperty<Color>                         secondColor;
    private boolean                                       _alarmsEnabled;
    private BooleanProperty                               alarmsEnabled;
    private boolean                                       _alarmsVisible;
    private BooleanProperty                               alarmsVisible;
    private ObservableList<Alarm>                         alarms;
    private List<Alarm>                                   alarmsToRemove;
    private boolean                                       _strokeWithGradient;
    private BooleanProperty                               strokeWithGradient;
    private boolean                                       _fillWithGradient;
    private BooleanProperty                               fillWithGradient;
    private String                                        _tooltipText;
    private StringProperty                                tooltipText;
    private Tooltip                                       tooltip;
    private Axis                                          _xAxis;
    private ObjectProperty<Axis>                          xAxis;
    private Axis                                          _yAxis;
    private ObjectProperty<Axis>                          yAxis;
    private RadarChartMode                                _radarChartMode;
    private ObjectProperty<RadarChartMode>                radarChartMode;
    private Color                                         _chartGridColor;
    private ObjectProperty<Color>                         chartGridColor;
    private Country                                       _country;
    private ObjectProperty<Country>                       country;
    private CountryGroup                                  _countryGroup;
    private ObjectProperty<CountryGroup>                  countryGroup;
    private boolean                                       _dataPointsVisible;
    private BooleanProperty                               dataPointsVisible;
    private boolean                                       _snapToTicks;
    private BooleanProperty                               snapToTicks;
    private int                                           _minorTickCount;
    private double                                        _majorTickUnit;
    private int[]                                         _matrixSize;
    private ObservableList<MatrixIcon>                    matrixIcons;
    private ChartType                                     _chartType;
    private double                                        _tooltipTimeout;
    private DoubleProperty                                tooltipTimeout;
    private Color                                         _notifyRegionBackgroundColor;
    private Color                                         _notifyRegionForegroundColor;
    private String                                        _notifyRegionTooltipText;
    private Color                                         _infoRegionBackgroundColor;
    private Color                                         _infoRegionForegroundColor;
    private String                                        _infoRegionTooltipText;
    private Color                                         _lowerRightRegionBackgroundColor;
    private Color                                         _lowerRightRegionForegroundColor;
    private String                                        _lowerRightRegionTooltipText;
    private Image                                         _backgroundImage;
    private double                                        _backgroundImageOpacity;
    private boolean                                       _backgroundImageKeepAspect;
    private String                                        _leftText;
    private StringProperty                                leftText;
    private String                                        _middleText;
    private StringProperty                                middleText;
    private String                                        _rightText;
    private StringProperty                                rightText;
    private double                                        _leftValue;
    private DoubleProperty                                leftValue;
    private double                                        _middleValue;
    private DoubleProperty                                middleValue;
    private double                                        _rightValue;
    private DoubleProperty                                rightValue;
    private Node                                          _leftGraphics;
    private ObjectProperty<Node>                          leftGraphics;
    private Node                                          _middleGraphics;
    private ObjectProperty<Node>                          middleGraphics;
    private Node                                          _rightGraphics;
    private ObjectProperty<Node>                          rightGraphics;
    private boolean                                       _trendVisible;
    private BooleanProperty                               trendVisible;
    private long                                          _timeoutMs;
    private LongProperty                                  timeoutMs;
    private Rank                                          _rank;
    private ObjectProperty<Rank>                          rank;
    private boolean                                       _interactive;
    private BooleanProperty                               interactive;
    private int                                           _numberOfValuesForTrendCalculation;
    private IntegerProperty                               numberOfValuesForTrendCalculation;
    private EventHandler<MouseEvent>                      infoRegionHandler;

    private volatile ScheduledFuture<?>                   periodicTickTask;
    private          ScheduledExecutorService             periodicTickExecutorService;


    // ******************** Constructors **************************************
    public Tile() {
        this(SkinType.GAUGE);
    }
    public Tile(@NamedArg(value="skinType", defaultValue="SkinType.GAUGE") SkinType skinType) {
        setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        this.skinType = skinType;
        getStyleClass().add("tile");

        init();
        registerListeners();
    }
    public Tile(@NamedArg(value="skinType", defaultValue="SkinType.GAUGE") SkinType skinType,
                @NamedArg(value="minValue", defaultValue="0") double minValue,
                @NamedArg(value="maxValue", defaultValue="100") double maxValue,
                @NamedArg(value="value", defaultValue="0") double value,
                @NamedArg(value="threshold", defaultValue="100") double threshold,
                @NamedArg(value="lowerThreshold", defaultValue="0") double lowerThreshold,
                @NamedArg(value="referenceValue", defaultValue="0") double referenceValue,
                @NamedArg(value="autoReferenceValue", defaultValue="true") boolean autoReferenceValue,
                @NamedArg(value="title", defaultValue="") String title,
                @NamedArg(value="titleAlignment", defaultValue="TextAlignment.LEFT") TextAlignment titleAlignment,
                @NamedArg(value="description", defaultValue="") String description,
                @NamedArg(value="descriptionAlignment", defaultValue="Pos.TOP_RIGHT") Pos descriptionAlignment,
                @NamedArg(value="unit", defaultValue="") String unit,
                @NamedArg(value="flipText", defaultValue="") String flipText,
                @NamedArg(value="active", defaultValue="false") boolean active,
                @NamedArg(value="text", defaultValue="") String text,
                @NamedArg(value="textAlignment", defaultValue="TextAlignment.LEFT") TextAlignment textAlignment,
                @NamedArg(value="averagingEnabled", defaultValue="false") boolean averagingEnabled,
                @NamedArg(value="averagingPeriod", defaultValue="10") int averagingPeriod,
                @NamedArg(value="timePeriod", defaultValue="java.time.Duration.ofMinutes(1)") java.time.Duration timePeriod,
                @NamedArg(value="maxTimePeriod", defaultValue="java.time.Duration.ofMinutes(1)") java.time.Duration maxTimePeriod,
                @NamedArg(value="timePeriodResolution", defaultValue="TimeUnit.SECONDS") java.util.concurrent.TimeUnit timePeriodResolution,
                @NamedArg(value="fixedYScale", defaultValue="false") boolean _fixedYScale,
                @NamedArg(value="imageMask", defaultValue="ImageMask.NONE") ImageMask imageMask,
                @NamedArg(value="trackColor", defaultValue="TileColor.BLUE") Color trackColor,
                @NamedArg(value="mapProvider", defaultValue="MapProvider.BW") MapProvider mapProvider,
                @NamedArg(value="flipTimeInMS", defaultValue="500") long flipTimeInMS,
                @NamedArg(value="itemSorting", defaultValue="ItemSorting.DESCENDING") ItemSorting itemSorting,
                @NamedArg(value="itemSortingTopic", defaultValue="ItemSortingTopic.VALUE") ItemSortingTopic itemSortingTopic,
                @NamedArg(value="textSize", defaultValue="TextSize.NORMAL") TextSize textSize,
                @NamedArg(value="roundedCorners", defaultValue="true") boolean roundedCorners,
                @NamedArg(value="startFromZero", defaultValue="false") boolean startFromZero,
                @NamedArg(value="returnToZero", defaultValue="false") boolean returnToZero,
                @NamedArg(value="minMeasuredValue", defaultValue="100") double minMeasuredValue,
                @NamedArg(value="maxMeasuredValue", defaultValue="0") double maxMeasuredValue,
                @NamedArg(value="minMeasuredValueVisible", defaultValue="false") boolean minMeasuredValueVisible,
                @NamedArg(value="maxMeasuredValueVisible", defaultValue="false") boolean maxMeasuredValueVisible,
                @NamedArg(value="oldValueVisible", defaultValue="false") boolean oldValueVisible,
                @NamedArg(value="valueVisible", defaultValue="true") boolean valueVisible,
                @NamedArg(value="foregroundColor", defaultValue="#dfdfdf") Color foregroundColor,
                @NamedArg(value="backgroundColor", defaultValue="#2a2a2a") Color backgroundColor,
                @NamedArg(value="borderColor", defaultValue="#00000000") Color borderColor,
                @NamedArg(value="borderWidth", defaultValue="1") double borderWidth,
                @NamedArg(value="knobColor", defaultValue="#dfdfdf") Color knobColor,
                @NamedArg(value="activeColor", defaultValue="#4274c6") Color activeColor,
                @NamedArg(value="animated", defaultValue="false") boolean animated,
                @NamedArg(value="animationDuration", defaultValue="800") long animationDuration,
                @NamedArg(value="startAngle", defaultValue="0") double startAngle,
                @NamedArg(value="angleRange", defaultValue="180") double angleRange,
                @NamedArg(value="autoScale", defaultValue="true") boolean autoScale,
                @NamedArg(value="shadowsEnabled", defaultValue="false") boolean shadowsEnabled,
                @NamedArg(value="locale", defaultValue="Locale.US") Locale locale,
                @NamedArg(value="numberFormat", defaultValue="NumberFormat.getInstance(Locale.US)") NumberFormat numberFormat,
                @NamedArg(value="decimals", defaultValue="1") int decimals,
                @NamedArg(value="tickLabelDecimals", defaultValue="1") int tickLabelDecimals,
                @NamedArg(value="tickLabelsXVisible", defaultValue="true") boolean tickLabelsXVisible,
                @NamedArg(value="tickLabelsYVisible", defaultValue="true") boolean tickLabelsYVisible,
                @NamedArg(value="minValueVisible", defaultValue="true") boolean minValueVisible,
                @NamedArg(value="maxValueVisible", defaultValue="true") boolean maxValueVisible,
                @NamedArg(value="needleColor", defaultValue="#dfdfdf") Color needleColor,
                @NamedArg(value="hourColor", defaultValue="#dfdfdf") Color hourColor,
                @NamedArg(value="minuteColor", defaultValue="#dfdfdf") Color minuteColor,
                @NamedArg(value="secondColor", defaultValue="#dfdfdf") Color secondColor,
                @NamedArg(value="barColor", defaultValue="#4274c6") Color barColor,
                @NamedArg(value="barBackgroundColor", defaultValue="#2a2a2a") Color barBackgroundColor,
                @NamedArg(value="titleColor", defaultValue="#dfdfdf") Color titleColor,
                @NamedArg(value="descriptionColor", defaultValue="#dfdfdf") Color descriptionColor,
                @NamedArg(value="unitColor", defaultValue="#dfdfdf") Color unitColor,
                @NamedArg(value="valueColor", defaultValue="#dfdfdf") Color valueColor,
                @NamedArg(value="textColor", defaultValue="#dfdfdf") Color textColor,
                @NamedArg(value="dateColor", defaultValue="#dfdfdf") Color dateColor,
                @NamedArg(value="hourTickMarkColor", defaultValue="#dfdfdf") Color hourTickMarkColor,
                @NamedArg(value="minuteTickMarkColor", defaultValue="#dfdfdf") Color minuteTickMarkColor,
                @NamedArg(value="alarmColor", defaultValue="#dfdfdf") Color alarmColor,
                @NamedArg(value="tickLabelColor", defaultValue="#dfdfdf") Color tickLabelColor,
                @NamedArg(value="tickMarkColor", defaultValue="#dfdfdf") Color tickMarkColor,
                @NamedArg(value="thresholdColor", defaultValue="#e5504c") Color thresholdColor,
                @NamedArg(value="lowerThresholdColor", defaultValue="#e5504c") Color _lowerThresholdColor,
                @NamedArg(value="checkSectionsForValue", defaultValue="false") boolean checkSectionsForValue,
                @NamedArg(value="checkThreshold", defaultValue="false") boolean checkThreshold,
                @NamedArg(value="checkLowerThreshold", defaultValue="false") boolean checkLowerThreshold,
                @NamedArg(value="innerShadowEnabled", defaultValue="false") boolean innerShadowEnabled,
                @NamedArg(value="thresholdVisible", defaultValue="false") boolean thresholdVisible,
                @NamedArg(value="averageVisible", defaultValue="false") boolean averageVisible,
                @NamedArg(value="sectionsVisible", defaultValue="false") boolean sectionsVisible,
                @NamedArg(value="sectionsAlwaysVisible", defaultValue="false") boolean sectionsAlwaysVisible,
                @NamedArg(value="secions", defaultValue="null") List<Section> sections,
                @NamedArg(value="sectionTextVisible", defaultValue="false") boolean sectionTextVisible,
                @NamedArg(value="sectionIconsVisible", defaultValue="false") boolean sectionIconsVisible,
                @NamedArg(value="highlightSections", defaultValue="false") boolean highlightSections,
                @NamedArg(value="orientation", defaultValue="Orientation.HORIZONTAL") Orientation orientation,
                @NamedArg(value="keepAspect", defaultValue="true") boolean keepAspect,
                @NamedArg(value="customFontEnabled", defaultValue="false") boolean customFontEnabled,
                @NamedArg(value="customFont", defaultValue="Fonts.latoRegular(12)") Font customFont,
                @NamedArg(value="customDecimalFormatEnabled", defaultValue="false") boolean customDecimalFormatEnabled,
                @NamedArg(value="customDecimalFormat", defaultValue="new DecimalFormat(\"#\")") DecimalFormat customDecimalFormat,
                @NamedArg(value="alert", defaultValue="false") boolean alert,
                @NamedArg(value="alertMessage", defaultValue="") String alertMessage,
                @NamedArg(value="smoothing", defaultValue="false") boolean smoothing,
                @NamedArg(value="secondsVisible", defaultValue="false") boolean secondsVisible,
                @NamedArg(value="discreteSeconds", defaultValue="true") boolean discreteSeconds,
                @NamedArg(value="discreteMinutes", defaultValue="true") boolean discreteMinutes,
                @NamedArg(value="discreteHours", defaultValue="false") boolean discreteHours,
                @NamedArg(value="textVisible", defaultValue="true") boolean textVisible,
                @NamedArg(value="dateVisible", defaultValue="false") boolean dateVisible,
                @NamedArg(value="running", defaultValue="false") boolean running,
                @NamedArg(value="hourTickMarksVisible", defaultValue="true") boolean hourTickMarksVisible,
                @NamedArg(value="minuteTickMarksVisible", defaultValue="true") boolean minuteTickMarksVisible,
                @NamedArg(value="alarmsEnabled", defaultValue="false") boolean alarmsEnabled,
                @NamedArg(value="alarmsVisible", defaultValue="false") boolean alarmsVisible,
                @NamedArg(value="strokeWithGradient", defaultValue="false") boolean strokeWithGradient,
                @NamedArg(value="fillWithGradient", defaultValue="false") boolean fillWithGradient,
                @NamedArg(value="radarChartMode", defaultValue="RadarChartMode.POLYGON") RadarChartMode radarChartMode,
                @NamedArg(value="chartGridColor", defaultValue="#8B9092") Color chartGridColor,
                @NamedArg(value="dataPointsVisible", defaultValue="false") boolean dataPointsVisible,
                @NamedArg(value="snapToTicks", defaultValue="false") boolean snapToTicks,
                @NamedArg(value="minorTickCount", defaultValue="0") int minorTickCount,
                @NamedArg(value="majorTickUnit", defaultValue="1") int majorTickUnit,
                @NamedArg(value="chartType", defaultValue="ChartType.LINE") ChartType chartType,
                @NamedArg(value="tooltipTimeout", defaultValue="2000") long tooltipTimeout,
                @NamedArg(value="notifyRegionBackgroundColor", defaultValue="#E5E54C") Color notifyRegionBackgroundColor,
                @NamedArg(value="notifyRegionForegroundColor", defaultValue="Tile.#2a2a2a") Color notifyRegionForegroundColor,
                @NamedArg(value="notifyRegionTooltipText", defaultValue="") String notifyRegionTooltipText,
                @NamedArg(value="showNotifyRegion", defaultValue="false") String showNotifyRegion,
                @NamedArg(value="infoRegionBackgroundColor", defaultValue="#375EFC") Color infoRegionBackgroundColor,
                @NamedArg(value="infoRegionForegroundColor", defaultValue="#dfdfdf") Color infoRegionForegroundColor,
                @NamedArg(value="infoRegionTooltipText", defaultValue="") String infoRegionTooltipText,
                @NamedArg(value="showInfoRegion", defaultValue="false") String showInfoRegion,
                @NamedArg(value="lowerRightRegionBackgroundColor", defaultValue="#375EFC") Color lowerRightRegionBackgroundColor,
                @NamedArg(value="lowerRightRegionForegroundColor", defaultValue="#8B9092") Color lowerRightRegionForegroundColor,
                @NamedArg(value="lowerRightRegionTooltipText", defaultValue="") String lowerRightRegionTooltipText,
                @NamedArg(value="showLowerRightRegion", defaultValue="false") String showLowerRightRegion,
                @NamedArg(value="backgroundImage", defaultValue="null") Image backgroundImage,
                @NamedArg(value="backgroundImageOpacity", defaultValue="0.2") double backgroundImageOpacity,
                @NamedArg(value="backgroundImageKeepAspect", defaultValue="true") boolean backgroundImageKeepAspect,
                @NamedArg(value="leftText", defaultValue="") String leftText,
                @NamedArg(value="middleText", defaultValue="") String middleText,
                @NamedArg(value="rightText", defaultValue="") String rightText,
                @NamedArg(value="leftValue", defaultValue="0") double leftValue,
                @NamedArg(value="middleValue", defaultValue="0") double middleValue,
                @NamedArg(value="rightValue", defaultValue="0") double rightValue,
                @NamedArg(value="leftGraphics", defaultValue="null") Node leftGraphics,
                @NamedArg(value="middleGraphics", defaultValue="null") Node middleGraphics,
                @NamedArg(value="rightGraphics", defaultValue="null") Node rightGraphics,
                @NamedArg(value="trendVisible", defaultValue="true") boolean trendVisible,
                @NamedArg(value="timeoutMs", defaultValue="1000") long timeoutMs,
                @NamedArg(value="ranking", defaultValue="Rank.DEFAULT") Rank rank,
                @NamedArg(value="interactive", defaultValue="true") boolean interactive,
                @NamedArg(value="numberOfValuesForTrendCalculation", defaultValue="3") int numberOfValuesForTrendCalculation,
                @NamedArg(value="updateInterval", defaultValue="1000") int updateInterval,
                @NamedArg(value="increment", defaultValue="1") int increment,
                @NamedArg(value="flatUI", defaultValue="true") boolean flatUI,
                @NamedArg(value="thumbColor", defaultValue="#DFDFDF80") boolean thumbColor
                ) {
        setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        this.skinType = skinType;
        getStyleClass().add("tile");

        init();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void init() {
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
                    double threshold = getThreshold();
                    if (formerValue.get() < threshold && VALUE > threshold) {
                        fireTileEvent(EXCEEDED_THRESHOLD_EVENT);
                    } else if (formerValue.get() > threshold && VALUE < threshold) {
                        fireTileEvent(UNDERRUN_THRESHOLD_EVENT);
                    }
                }

                if (isCheckLowerThreshold()) {
                    double lowerThreshold = getLowerThreshold();
                    if (formerValue.get() < lowerThreshold && VALUE > lowerThreshold) {
                        fireTileEvent(EXCEEDED_LOWER_THRESHOLD_EVENT);
                    } else if (formerValue.get() > lowerThreshold && VALUE < lowerThreshold) {
                        fireTileEvent(UNDERRUN_LOWER_THRESHOLD_EVENT);
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
        _lowerThreshold                     = _minValue;
        _referenceValue                     = _minValue;
        _autoReferenceValue                 = true;

        currentTime                         = new LongPropertyBase(getTime().toEpochSecond()) {
            @Override public Object getBean() { return Tile.this; }
            @Override public String getName() { return "currentTime"; }
        };
        _title                              = "";
        _titleAlignment                     = TextAlignment.LEFT;
        _description                        = "";
        _descriptionAlignment               = Pos.TOP_RIGHT;
        _unit                               = "";
        oldFlipText                         = "";
        _flipText                           = "";
        _active                             = false;
        _text                               = "";
        _textAlignment                      = TextAlignment.LEFT;
        _averagingEnabled                   = false;
        _averagingPeriod                    = MovingAverage.DEFAULT_PERIOD;
        _timePeriod                         = DEFAULT_TIME_PERIOD;
        _maxTimePeriod                      = DEFAULT_TIME_PERIOD;
        _timePeriodResolution               = DEFAULT_TIME_PERIOD_RESOLUTION;
        _fixedYScale                        = false;
        _duration                           = LocalTime.of(1, 0);
        _imageMask                          = ImageMask.NONE;
        _currentLocation                    = new Location(0, 0);
        _trackColor                         = TileColor.BLUE;
        _mapProvider                        = MapProvider.BW;
        flipTimeInMS                        = 500;
        _itemSorting                        = ItemSorting.NONE;
        _itemSortingTopic                   = ItemSortingTopic.VALUE;
        thumbColor                          = new SimpleStyleableObjectProperty<>(THUMB_COLOR, this, "thumbColor");
        _flatUI                             = true;
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
        pauseDuration                       = 2000;
        _startAngle                         = 0;
        _angleRange                         = 180;
        _angleStep                          = _angleRange / _range;
        _autoScale                          = true;
        _shadowsEnabled                     = false;
        _locale                             = Locale.US;
        _numberFormat                       = NumberFormat.getInstance(_locale);
        _decimals                           = 1;
        _tickLabelDecimals                  = 1;
        _tickLabelsXVisible                 = true;
        _tickLabelsYVisible                 = true;
        _minValueVisible                    = true;
        _maxValueVisible                    = true;
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
        _tickLabelColor                     = FOREGROUND;
        _tickMarkColor                      = FOREGROUND;
        _thresholdColor                     = RED;
        _lowerThresholdColor                = RED;
        _checkSectionsForValue              = false;
        _checkThreshold                     = false;
        _checkLowerThreshold                = false;
        _innerShadowEnabled                 = false;
        _thresholdVisible                   = false;
        _lowerThresholdVisible              = false;
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
        _customDecimalFormatEnabled         = false;
        _customDecimalFormat                = new DecimalFormat("#");
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
        _strokeWithGradient                 = false;
        _fillWithGradient                   = false;
        tooltip                             = new Tooltip(null);
        _xAxis                              = new CategoryAxis();
        _yAxis                              = new NumberAxis();
        _radarChartMode                     = RadarChartMode.POLYGON;
        _chartGridColor                     = Tile.GRAY;
        _dataPointsVisible                  = false;
        _snapToTicks                        = false;
        _minorTickCount                     = 0;
        _majorTickUnit                      = 1;
        _matrixSize                         = new int[]{ 30, 25 };
        matrixIcons                         = FXCollections.observableArrayList();
        _chartType                          = ChartType.LINE;
        _tooltipTimeout                     = 2000;
        _notifyRegionBackgroundColor        = Tile.YELLOW;
        _notifyRegionForegroundColor        = Tile.BACKGROUND;
        _notifyRegionTooltipText            = "";
        _infoRegionBackgroundColor          = Tile.DARK_BLUE;
        _infoRegionForegroundColor          = Tile.FOREGROUND;
        _infoRegionTooltipText              = "";
        _lowerRightRegionBackgroundColor    = Tile.GRAY;
        _lowerRightRegionForegroundColor    = Tile.BACKGROUND;
        _lowerRightRegionTooltipText        = "";
        _backgroundImage                    = null;
        _backgroundImageOpacity             = 0.2;
        _backgroundImageKeepAspect          = true;
        _leftText                           = "";
        _middleText                         = "";
        _rightText                          = "";
        _leftValue                          = 0;
        _middleValue                        = 0;
        _rightValue                         = 0;
        _leftGraphics                       = null;
        _middleGraphics                     = null;
        _rightGraphics                      = null;
        _trendVisible                       = false;
        _timeoutMs                          = 1000;
        _rank                               = Rank.DEFAULT;
        _interactive                        = true;
        _numberOfValuesForTrendCalculation  = 3;
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
        presetTileParameters(skinType);
    }

    public void reInit() {
        setTrackColor(TileColor.BLUE);
        setTextSize(TextSize.NORMAL);
        setRoundedCorners(true);
        setMinMeasuredValueVisible(false);
        setMaxMeasuredValueVisible(false);
        setOldValueVisible(false);
        setValueVisible(true);
        setForegroundColor(FOREGROUND);
        setBackgroundColor(BACKGROUND);
        setBorderColor(Color.TRANSPARENT);
        setBorderWidth(1);
        setKnobColor(FOREGROUND);
        setActiveColor(BLUE);
        setAnimated(false);
        setShadowsEnabled(false);
        setNeedleColor(FOREGROUND);
        setHourColor(FOREGROUND);
        setMinuteColor(FOREGROUND);
        setSecondColor(FOREGROUND);
        setBarColor(BLUE);
        setBarBackgroundColor(BACKGROUND);
        setTitleColor(FOREGROUND);
        setDescriptionColor(FOREGROUND);
        setUnitColor(FOREGROUND);
        setValueColor(FOREGROUND);
        setTextColor(FOREGROUND);
        setDateColor(FOREGROUND);
        setHourTickMarkColor(FOREGROUND);
        setMinuteTickMarkColor(FOREGROUND);
        setAlarmColor(FOREGROUND);
        setTickLabelColor(FOREGROUND);
        setTickMarkColor(FOREGROUND);
        setThresholdColor(RED);
        setLowerThresholdColor(RED);
        setInnerShadowEnabled(false);
        setThresholdVisible(false);
        setAverageVisible(false);
        setSectionsVisible(false);
        setSectionsAlwaysVisible(false);
        setSectionTextVisible(false);
        setSectionIconsVisible(false);
        setHighlightSections(false);
        setOrientation(Orientation.HORIZONTAL);
        setKeepAspect(true);
        setSmoothing(false);
        setSecondsVisible(false);
        setDateVisible(false);
        setHourTickMarksVisible(false);
        setMinuteTickMarksVisible(false);
        setAlarmsVisible(false);
        setStrokeWithGradient(false);
        setFillWithGradient(false);
        setChartGridColor(Tile.GRAY);
        setDataPointsVisible(false);
        setSnapToTicks(false);
        setNotifyRegionBackgroundColor(Tile.YELLOW);
        setNotifyRegionForegroundColor(Tile.BACKGROUND);
        setInfoRegionBackgroundColor(Tile.DARK_BLUE);
        setInfoRegionForegroundColor(Tile.FOREGROUND);
        setBackgroundImageOpacity(0.2);
        setBackgroundImageKeepAspect(true);
        setTrendVisible(false);
    }

    private void registerListeners() {
        disabledProperty().addListener(o -> setOpacity(isDisabled() ? 0.4 : 1));
        valueProperty().addListener((o, ov, nv) -> oldValue.set(ov.doubleValue()));
        currentValueProperty().addListener(o -> {
            double currentValue = getCurrentValue();
            if (currentValue > getMaxValue()) {
                fireTileEvent(MAX_VALUE_EXCEEDED);
            } else if (currentValue < getMinValue()) {
                fireTileEvent(MIN_VALUE_UNDERRUN);
            } else {
                fireTileEvent(VALUE_IN_RANGE);
            }
        });
        if (null != getScene()) {
            setupBinding();
        } else {
            sceneProperty().addListener((o1, ov1, nv1) -> {
                if (null == nv1) { return; }
                if (null != getScene().getWindow()) {
                    setupBinding();
                } else {
                    sceneProperty().get().windowProperty().addListener((o2, ov2, nv2) -> {
                        if (null == nv2) { return; }
                        setupBinding();
                    });
                }
            });
        }
    }


    // ******************** Methods *******************************************

    /**
     * Returns the value of the Tile. If animated == true this value represents
     * the value at the end of the animation. Where currentValue represents the
     * current value during the animation.
     *
     * @return the value of the tile
     */
    public double getValue() { return value.get(); }
    /**
     * Sets the value of the Tile to the given double. If animated == true this
     * value will be the end value after the animation is finished.
     *
     * @param VALUE
     */
    public void setValue(final double VALUE) {
        if (!value.isBound()) {
            value.set(VALUE);
        }
    }
    public DoubleProperty valueProperty() { return value; }

    /**
     * Returns the current value of the Tile. If animated == true this value
     * represents the current value during the animation. Otherwise it's returns
     * the same value as the getValue() method.
     *
     * @return the current value of the tile
     */
    public double getCurrentValue() { return currentValue.get(); }
    public ReadOnlyDoubleProperty currentValueProperty() { return currentValue; }

    /**
     * Returns the last value of the Tile. This will not be the last value during
     * an animation but the final last value after the animation was finished.
     * If you need to get the last value during an animation you should use
     * formerValue instead.
     *
     * @return the last value of the tile
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
     * @return the last value of the tile during an animation
     */
    public double getFormerValue() { return formerValue.get(); }
    public ReadOnlyDoubleProperty formerValueProperty() { return formerValue; }

    public void increaseValue(final double value) {
        setValue(getValue() + value);
    }
    public void decreaseValue(final double value) {
        setValue(getValue() - value);
    }

    /**
     * Returns the minimum value of the scale. This value represents the lower
     * limit of the visible tile values.
     *
     * @return the minimum value of the tile scale
     */
    public double getMinValue() { return null == minValue ? _minValue : minValue.get(); }
    /**
     * Sets the minimum value of the tile scale to the given value
     *
     * @param VALUE
     */
    public void setMinValue(final double VALUE) {
        if (Status.RUNNING == timeline.getStatus()) { timeline.jumpTo(Duration.ONE); }
        if (null == minValue) {
            if (VALUE > getMaxValue()) { setMaxValue(VALUE); }
            _minValue = clamp(-Double.MAX_VALUE, getMaxValue(), VALUE);
            setRange(getMaxValue() - _minValue);
            if (Helper.equals(originalMinValue, -Double.MAX_VALUE)) { originalMinValue = _minValue; }
            if (isStartFromZero() && _minValue < 0) { setValue(0); }
            if (Helper.equals(originalThreshold, getThreshold())) { setThreshold(clamp(_minValue, getMaxValue(), originalThreshold)); }
            fireTileEvent(RECALC_EVENT);
            if (!valueProperty().isBound() && isShowing()) { Tile.this.setValue(clamp(getMinValue(), getMaxValue(), Tile.this.getValue())); }
        } else {
            if (!minValue.isBound()) {
                minValue.set(VALUE);
            }
        }
    }
    public DoubleProperty minValueProperty() {
        if (null == minValue) {
            minValue = new DoublePropertyBase(_minValue) {
                @Override protected void invalidated() {
                    double value = get();
                    if (value > getMaxValue()) { setMaxValue(value); }
                    setRange(getMaxValue() - value);
                    if (Helper.equals(originalMinValue, -Double.MAX_VALUE)) originalMinValue = value;
                    if (isStartFromZero() && _minValue < 0) Tile.this.setValue(0);
                    if (Helper.lessThan(originalThreshold, getThreshold())) { setThreshold(clamp(value, getMaxValue(), originalThreshold)); }
                    fireTileEvent(RECALC_EVENT);
                    if (!valueProperty().isBound() && isShowing()) Tile.this.setValue(clamp(getMinValue(), getMaxValue(), Tile.this.getValue()));
                }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "minValue";}
            };
        }
        return minValue;
    }
    
    /**
     * Returns the maximum value of the scale. This value represents the upper limit
     * of the visible tile values.
     *
     * @return the maximum value of the tile scale
     */
    public double getMaxValue() { return null == maxValue ? _maxValue : maxValue.get(); }
    /**
     * Sets the maximum value of the tile scale to the given value
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
            if (!valueProperty().isBound() && isShowing()) Tile.this.setValue(clamp(getMinValue(), getMaxValue(), Tile.this.getValue()));
        } else {
            if (!maxValue.isBound()) {
                maxValue.set(VALUE);
            }
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
                    if (!valueProperty().isBound() && isShowing()) Tile.this.setValue(clamp(getMinValue(), getMaxValue(), Tile.this.getValue()));
                }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "maxValue"; }
            };
        }
        return maxValue;
    }

    /**
     * Always returns the range of the tile scale (maxValue - minValue).
     * This value will be automatically calculated each time
     * the min- or maxValue will change.
     *
     * @return the range of the tile scale
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
     * The value will be clamped to range of the tile.
     *
     * @return the threshold value of the tile
     */
    public double getThreshold() { return null == threshold ? _threshold : threshold.get(); }
    /**
     * Sets the threshold of the tile to the given value. The value
     * will be clamped to the range of the tile.
     *
     * @param THRESHOLD
     */
    public void setThreshold(final double THRESHOLD) {
        originalThreshold = THRESHOLD;
        if (null == threshold) {
            _threshold = clamp(getMinValue(), getMaxValue(), THRESHOLD);
            fireTileEvent(RESIZE_EVENT);
        } else {
            if (!threshold.isBound()) {
                threshold.set(THRESHOLD);
            }
        }
    }
    public DoubleProperty thresholdProperty() {
        if (null == threshold) {
            threshold = new DoublePropertyBase(_threshold) {
                @Override protected void invalidated() {
                    final double THRESHOLD = get();
                    if (!isBound() && (THRESHOLD < getMinValue() || THRESHOLD > getMaxValue())) {
                        set(clamp(getMinValue(), getMaxValue(), THRESHOLD));
                    }
                    fireTileEvent(RESIZE_EVENT);
                }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "threshold"; }
            };
        }
        return threshold;
    }

    public double getLowerThreshold() { return null == lowerThreshold ? _lowerThreshold : lowerThreshold.get(); }
    public void setLowerThreshold(final double THRESHOLD) {
        if (null == lowerThreshold) {
            _lowerThreshold = clamp(getMinValue(), getMaxValue(), THRESHOLD);
            fireTileEvent(RESIZE_EVENT);
        } else {
            if (!lowerThreshold.isBound()) {
                lowerThreshold.set(THRESHOLD);
            }
        }
    }
    public DoubleProperty lowerThresholdProperty() {
        if (null == lowerThreshold) {
            lowerThreshold = new DoublePropertyBase(_lowerThreshold) {
                @Override protected void invalidated() {
                    set(clamp(getMinValue(), getMaxValue(), get()));
                    fireTileEvent(RESIZE_EVENT);
                }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "lowerThreshold"; }
            };
        }
        return lowerThreshold;
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
            if (!referenceValue.isBound()) {
                referenceValue.set(VALUE);
            }
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

    public SunburstChart getSunburstChart() {
        if (null == sunburstChart) { sunburstChart = new SunburstChart(); }
        return sunburstChart;
    }

    /**
     * Returns the title of the tile. This title will usually
     * only be visible if it is not empty.
     *
     * @return the title of the tile
     */
    public String getTitle() { return null == title ? _title : title.get(); }
    /**
     * Sets the title of the tile. This title will only be visible
     * if it is not empty.
     *
     * @param TITLE
     */
    public void setTitle(final String TITLE) {
        if (null == title) {
            _title = null == TITLE ? "" : TITLE;
            fireTileEvent(VISIBILITY_EVENT);
            fireTileEvent(REDRAW_EVENT);
        } else {
            if (!title.isBound()) {
                title.set(TITLE);
            }
        }
    }
    public StringProperty titleProperty() {
        if (null == title) {
            title  = new StringPropertyBase(_title) {
                @Override protected void invalidated() {
                    if (null == get()) { set(""); }
                    fireTileEvent(VISIBILITY_EVENT);
                    fireTileEvent(REDRAW_EVENT);
                }
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
            if (!titleAlignment.isBound()) {
                titleAlignment.set(ALIGNMENT);
            }
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
     * Returns the description text of the tile. This description text will usually
     * only be visible if it is not empty.
     *
     * @return the description text of the tile
     */
    public String getDescription() { return null == description ? _description : description.get(); }
    /**
     * Sets the description text of the tile. This description text will usually
     * only be visible if it is not empty.
     *
     * @param DESCRIPTION
     */
    public void setDescription(final String DESCRIPTION) {
        if (null == description) {
            _description = DESCRIPTION;
            fireTileEvent(VISIBILITY_EVENT);
            fireTileEvent(REDRAW_EVENT);
        } else {
            if (!description.isBound()) {
                description.set(DESCRIPTION);
            }
        }
    }
    public StringProperty descriptionProperty() {
        if (null == description) {
            description = new StringPropertyBase(_description) {
                @Override protected void invalidated() {
                    fireTileEvent(VISIBILITY_EVENT);
                    fireTileEvent(REDRAW_EVENT);
                }
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
            if (!descriptionAlignment.isBound()) {
                descriptionAlignment.set(ALIGNMENT);
            }
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
     * Returns the unit of the tile. This unit will usually only
     * be visible if it is not empty.
     *
     * @return the unit of the tile
     */
    public String getUnit() { return null == unit ? _unit : unit.get(); }
    /**
     * Sets the unit of the tile. This unit will usually only be
     * visible if it is not empty.
     *
     * @param UNIT
     */
    public void setUnit(final String UNIT) {
        if (null == unit) {
            _unit = UNIT;
            fireTileEvent(VISIBILITY_EVENT);
            fireTileEvent(REDRAW_EVENT);
        } else {
            if (!unit.isBound()) {
                unit.set(UNIT);
            }
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
     * Returns the text that will be used to visualized the FlipTileSkin
     * @return the text that will be used to visualize the FlipTileSkin
     */
    public String getFlipText() { return null == flipText ? _flipText : flipText.get(); }
    /**
     * Defines the text that will be used to visualize the FlipTileSkin
     * @param TEXT
     */
    public void setFlipText(final String TEXT) {
        if (null == flipText) {
            _flipText = TEXT;
            if (!oldFlipText.equals(_flipText)) { fireTileEvent(FLIP_START_EVENT); }
            oldFlipText = _flipText;
        } else {
            if (!flipText.isBound()) {
                flipText.set(TEXT);
            }
        }
    }
    public StringProperty flipTextProperty() {
        if (null == flipText) {
            flipText = new StringPropertyBase(_flipText) {
                @Override protected void invalidated() {
                    if (!oldFlipText.equals(get())) { fireTileEvent(FLIP_START_EVENT); }
                    oldFlipText = get();
                }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "flipText"; }
            };
            _flipText = null;
        }
        return flipText;
    }

    /**
     * Returns true if the switch in the SwitchTileSkin is active
     * @return true if the switch in the SwitchTileSkin is active
     */
    public boolean isActive() { return null == active ? _active : active.get(); }
    /**
     * Defines if the switch in the SwitchTileSkin is active
     * @param SELECTED
     */
    public void setActive(final boolean SELECTED) {
        if (null == active) {
            _active = SELECTED;
            fireTileEvent(REDRAW_EVENT);
        } else {
            if (!active.isBound()) {
                active.set(SELECTED);
            }
        }
    }
    public BooleanProperty activeProperty() {
        if (null == active) {
            active = new BooleanPropertyBase(_active) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "active"; }
            };
        }
        return active;
    }

    /**
     * Returns the moving average object
     * @return the moving average object
     */
    public MovingAverage getMovingAverage() {
        if (null == movingAverage) { movingAverage = new MovingAverage(getAveragingPeriod()); }
        return movingAverage;
    }

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
            if (!averagingEnabled.isBound()) {
                averagingEnabled.set(ENABLED);
            }
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
            getMovingAverage().setPeriod(_averagingPeriod); // MAX 1000 values
            if (null == showing) return;
            fireTileEvent(AVERAGING_EVENT);
        } else {
            if (!averagingPeriod.isBound()) {
                averagingPeriod.set(Helper.clamp(0, MAX_PERIOD, PERIOD));
            }
        }
    }
    public IntegerProperty averagingPeriodProperty() {
        if (null == averagingPeriod) {
            averagingPeriod = new IntegerPropertyBase(_averagingPeriod) {
                @Override protected void invalidated() {
                    getMovingAverage().setPeriod(get());
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
    public double getAverage() {
        if (null == movingAverage) { movingAverage = new MovingAverage(getAveragingPeriod()); }
        return movingAverage.getAverage();
    }
    /**
     * Returns the moving average over the given duration.
     * @param DURATION
     * @return the moving average over the given duration
     */
    public double getTimeBasedAverageOf(final java.time.Duration DURATION) { return movingAverage.getTimeBasedAverageOf(DURATION); }

    /**
     * Returns the duration that should be used for the data shown in the TimelineTileSkin
     * @return the duration that should be used for the data shown in the TimelineTileSkin
     */
    public java.time.Duration getTimePeriod() { return null == timePeriod ? _timePeriod : timePeriod.get(); }
    /**
     * Defines the duration that should be used for the data shown in the TimelineTileSkin
     * @param PERIOD
     */
    public void setTimePeriod(final java.time.Duration PERIOD) {
        if (null == timePeriod) {
            _timePeriod = PERIOD;
            if (PERIOD.getSeconds() > getMaxTimePeriod().getSeconds()) {
                setMaxTimePeriod(PERIOD);
            }
            fireTileEvent(TIME_PERIOD_EVENT);
        } else {
            if (!timePeriod.isBound()) {
                timePeriod.set(PERIOD);
            }
        }
    }
    public ObjectProperty<java.time.Duration> timePeriodProperty() {
        if (null == timePeriod) {
            timePeriod = new ObjectPropertyBase<>(_timePeriod) {
                @Override protected void invalidated() {
                    if (get().getSeconds() > getMaxTimePeriod().getSeconds()) {
                        setMaxTimePeriod(get());
                    }
                    fireTileEvent(TIME_PERIOD_EVENT);
                }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "timePeriod"; }
            };
            _timePeriod = null;
        }
        return timePeriod;
    }

    /**
     * Returns the duration that will be used to store datapoints in the skin
     * Values that are not within that duration won't be stored.
     * @return the duration that will be used to store datapoints in the skin
     */
    public java.time.Duration getMaxTimePeriod() { return null == maxTimePeriod ? _maxTimePeriod : maxTimePeriod.get(); }
    /**
     * Defines the duration that will be used to store datapoints in the skin
     * @param MAX_PERIOD
     */
    public void setMaxTimePeriod(final java.time.Duration MAX_PERIOD) {
        if (null == maxTimePeriod) {
            _maxTimePeriod = MAX_PERIOD;
            if (getTimePeriod().getSeconds() > _maxTimePeriod.getSeconds()) {
                setTimePeriod(MAX_PERIOD);
                fireTileEvent(TIME_PERIOD_EVENT);
            }
        } else {
            if (!maxTimePeriod.isBound()) {
                maxTimePeriod.set(MAX_PERIOD);
            }
        }
    }
    public ObjectProperty<java.time.Duration> maxTimePeriodProperty() {
        if (null == maxTimePeriod) {
            maxTimePeriod = new ObjectPropertyBase<>(_maxTimePeriod) {
                @Override protected void invalidated() {
                    if (getTimePeriod().getSeconds() < get().getSeconds()) {
                        setTimePeriod(get());
                    }
                    fireTileEvent(TIME_PERIOD_EVENT);
                }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "maxTimePeriod"; }
            };
            _maxTimePeriod = null;
        }

        return maxTimePeriod;
    }

    public TimeUnit getTimePeriodResolution() { return null == timePeriodResolution ? _timePeriodResolution : timePeriodResolution.get(); }
    public void setTimePeriodResolution(final TimeUnit RESOLUTION) {
        if (null == timePeriodResolution) {
            _timePeriodResolution = RESOLUTION;
            fireTileEvent(TIME_PERIOD_EVENT);
        } else {
            if (!timePeriodResolution.isBound()) {
                timePeriodResolution.set(RESOLUTION);
            }
        }
    }
    public ObjectProperty<TimeUnit> timePeriodResolutionProperty() {
        if (null == timePeriodResolution) {
            timePeriodResolution = new ObjectPropertyBase<TimeUnit>(_timePeriodResolution) {
                @Override protected void invalidated() { fireTileEvent(TIME_PERIOD_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "timePeriodResolution"; }
            };
            _timePeriodResolution = null;
        }
        return timePeriodResolution;
    }

    /**
     * Returns true if the y-scale in the SparkLineTileSkin will always be defined by the minValue and maxValue.
     * If false the y-scale will be defined by the minMeasured and maxMeasured value.
     * @return true if the y-scale in the SparkLineTileSkin will always be defined by the minValue and maxValue
     */
    public boolean isFixedYScale() { return null == fixedYScale ? _fixedYScale : fixedYScale.get(); }
    /**
     * Defines how the y-scale will be drawn. If true, the y-scale will always be defined by the minValue and maxValue.
     * If false the y-scale will be defined by the minMeasured and maxMeasured value.
     * @param FIXED_Y_SCALE
     */
    public void setFixedYScale(final boolean FIXED_Y_SCALE) {
        if (null == fixedYScale) {
            _fixedYScale = FIXED_Y_SCALE;
            fireTileEvent(RESIZE_EVENT);
        } else {
            if (!fixedYScale.isBound()) {
                fixedYScale.set(FIXED_Y_SCALE);
            }
        }
    }
    public BooleanProperty fixedYScaleProperty() {
        if (null == fixedYScale) {
            fixedYScale = new BooleanPropertyBase(_fixedYScale) {
                @Override protected void invalidated() { fireTileEvent(RESIZE_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "fixedYScale"; }
            };
        }
        return fixedYScale;
    }

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
            if (!duration.isBound()) {
                duration.set(DURATION);
            }
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
     * as the red area in a rpm tile. Sections in the Medusa library
     * usually are less eye-catching than Areas.
     *
     * @return an observable list of Section objects
     */
    public ObservableList<Section> getSections() {
        if (null == sections) { sections = FXCollections.observableArrayList(); }
        return sections;
    }
    /**
     * Sets the sections to the given list of Section objects. The
     * sections will be used to colorize areas with a special
     * meaning such as the red area in a rpm tile.
     * Sections in the Medusa library
     * usually are less eye-catching than Areas.
     *
     * @param SECTIONS
     */
    public void setSections(final List<Section> SECTIONS) {
        getSections().setAll(SECTIONS);
        getSections().sort(new SectionComparator());
        fireTileEvent(SECTION_EVENT);
    }
    /**
     * Sets the sections to the given array of Section objects. The
     * sections will be used to colorize areas with a special
     * meaning such as the red area in a rpm tile.
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
        getSections().add(SECTION);
        getSections().sort(new SectionComparator());
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
        getSections().remove(SECTION);
        getSections().sort(new SectionComparator());
        fireTileEvent(SECTION_EVENT);
    }
    /**
     * Clears the list of sections.
     */
    public void clearSections() {
        getSections().clear();
        fireTileEvent(SECTION_EVENT);
    }

    public ObservableList<Series<String, Number>> getSeries() {
        return getTilesFXSeries().stream().map(tilesFxSeries -> tilesFxSeries.getSeries()).collect(Collectors.toCollection(FXCollections::observableArrayList));
    }
    public void setSeries(final List<Series<String, Number>> SERIES) {
        SERIES.forEach(series -> addTilesFXSeries(new TilesFXSeries<String, Number>(series)));
    }
    public void setSeries(final Series<String, Number>... SERIES) {
        setSeries(Arrays.asList(SERIES));
    }
    public void addSeries(final Series<String, Number> SERIES) {
        addTilesFXSeries(new TilesFXSeries<String, Number>(SERIES));
    }
    public void removeSeries(final Series<String, Number> SERIES) {
        TilesFXSeries<String, Number> seriesToRemove = series.stream().filter(tilesFxSeries -> tilesFxSeries.getSeries().equals(SERIES)).findFirst().orElse(null);
        if (null == seriesToRemove) return;
        series.removeAll(seriesToRemove);
    }
    public void clearSeries() { clearTilesFXSeries(); }

    public ObservableList<TilesFXSeries<String, Number>> getTilesFXSeries() {
        if (null == series) { series = FXCollections.observableArrayList(); }
        return series;
    }
    public void setTilesFXSeries(final List<TilesFXSeries<String, Number>> SERIES) {
        getTilesFXSeries().setAll(SERIES);
        fireTileEvent(SERIES_EVENT);
    }
    public void setTilesFXSeries(final TilesFXSeries<String, Number>... SERIES) { setTilesFXSeries(Arrays.asList(SERIES)); }
    public void addTilesFXSeries(final TilesFXSeries<String, Number> SERIES) {
        if (null == SERIES) return;
        getTilesFXSeries().add(SERIES);
        fireTileEvent(SERIES_EVENT);
    }
    public void removeTilesFXSeries(final TilesFXSeries<String, Number> SERIES) {
        if (null == SERIES) return;
        if (getTilesFXSeries().contains(SERIES)) {
            getTilesFXSeries().remove(SERIES);
            fireTileEvent(SERIES_EVENT);
        }
    }
    public void clearTilesFXSeries() {
        getTilesFXSeries().clear();
        fireTileEvent(SERIES_EVENT);
    }

    public ObservableList<BarChartItem> getBarChartItems() {
        if (null == barChartItems) { barChartItems = FXCollections.observableArrayList(); }
        return barChartItems;
    }
    public void setBarChartItems(final List<BarChartItem> ITEMS) {
        getBarChartItems().setAll(ITEMS);
        fireTileEvent(DATA_EVENT);
    }
    public void setBarChartItems(final BarChartItem... ITEMS) { setBarChartItems(Arrays.asList(ITEMS)); }
    public void addBarChartItem(final BarChartItem ITEM) {
        if (null == ITEM) return;
        getBarChartItems().add(ITEM);
        fireTileEvent(DATA_EVENT);
    }
    public void removeBarChartItem(final BarChartItem ITEM) {
        if (null == ITEM) return;
        getBarChartItems().remove(ITEM);
        fireTileEvent(DATA_EVENT);
    }
    public void clearBarChartItems() {
        getBarChartItems().clear();
        fireTileEvent(DATA_EVENT);
    }

    public ObservableList<LeaderBoardItem> getLeaderBoardItems() {
        if (null == leaderBoardItems) { leaderBoardItems = FXCollections.observableArrayList(); }
        return leaderBoardItems;
    }
    public void setLeaderBoardItems(final List<LeaderBoardItem> ITEMS) {
        getLeaderBoardItems().setAll(ITEMS);
        fireTileEvent(DATA_EVENT);
    }
    public void setLeaderBoardItems(final LeaderBoardItem... ITEMS) { setLeaderBoardItems(Arrays.asList(ITEMS)); }
    public void addLeaderBoardItem(final LeaderBoardItem ITEM) {
        if (null == ITEM) return;
        getLeaderBoardItems().add(ITEM);
        fireTileEvent(DATA_EVENT);
    }
    public void removeLeaderBoardItem(final LeaderBoardItem ITEM) {
        if (null == ITEM) return;
        getLeaderBoardItems().remove(ITEM);
        fireTileEvent(DATA_EVENT);
    }
    public void clearLeaderBoardItems() {
        getLeaderBoardItems().clear();
        fireTileEvent(DATA_EVENT);
    }

    public List<Stop> getGradientStops() {
        if (null == gradientStops) { gradientStops = new ArrayList<>(4); }
        return gradientStops;
    }
    public void setGradientStops(final Stop... STOPS) {
        setGradientStops(Arrays.asList(STOPS));
    }
    public void setGradientStops(final List<Stop> STOPS) {
        getGradientStops().clear();
        getGradientStops().addAll(STOPS);
        fireTileEvent(REDRAW_EVENT);
    }

    public Image getImage() { return null == image ? null : image.get(); }
    public void setImage(final Image IMAGE) {
        if (!imageProperty().isBound()) {
            imageProperty().set(IMAGE);
        }
    }
    public ObjectProperty<Image> imageProperty() {
        if (null == image) {
            image = new ObjectPropertyBase<Image>() {
                @Override protected void invalidated() { fireTileEvent(RESIZE_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "image"; }
            };
        }
        return image;
    }

    public ImageMask getImageMask() { return null == imageMask ? _imageMask : imageMask.get(); }
    public void setImageMask(final ImageMask MASK) {
        if (null == imageMask) {
            _imageMask = MASK;
            fireTileEvent(RESIZE_EVENT);
        } else {
            if (!imageMask.isBound()) {
                imageMask.set(MASK);
            }
        }
    }
    public ObjectProperty<ImageMask> imageMaskProperty() {
        if (null == imageMask) {
            imageMask = new ObjectPropertyBase<ImageMask>(_imageMask) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "imageMask"; }
            };
            _imageMask = null;
        }
        return imageMask;
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
    public void setGraphic(final Node GRAPHIC) {
        if (!graphicProperty().isBound()) {
            graphicProperty().set(GRAPHIC);
        }
    }
    public ObjectProperty<Node> graphicProperty() {
        if (null == graphic) {
            graphic = new ObjectPropertyBase<>() {
                @Override protected void invalidated() {
                	fireTileEvent(GRAPHIC_EVENT);
                	fireTileEvent(RESIZE_EVENT);
                }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "graphic"; }
            };
        }
        return graphic;
    }

    public SVGPath getSVGPath() { return null == svgPath ? null : svgPath.get(); }
    public void setSVGPath(final SVGPath SVG_PATH) {
        if (!svgPathProperty().isBound()) {
            svgPathProperty().set(SVG_PATH);
        }
    }
    public ObjectProperty<SVGPath> svgPathProperty() {
        if (null == svgPath) {
            svgPath = new ObjectPropertyBase<>() {
                @Override protected void invalidated() {
                    fireTileEvent(RESIZE_EVENT);
                }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "svgPath"; }
            };
        }
        return svgPath;
    }

    public Location getCurrentLocation() { return null == currentLocation ? _currentLocation : currentLocation.get(); }
    public void setCurrentLocation(final Location LOCATION) {
        if (null == currentLocation) {
            _currentLocation = LOCATION;
            fireTileEvent(LOCATION_EVENT);
        } else {
            if (!currentLocation.isBound()) {
                currentLocation.set(LOCATION);
            }
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

    public ObservableList<Location> getPoiList() {
        if (null == poiList) { poiList = FXCollections.observableArrayList(); }
        return poiList;
    }
    public void setPoiList(final List<Location> POI_LIST) { getPoiList().setAll(POI_LIST); }
    public void setPoiLocations(final Location... LOCATIONS) { setPoiList(Arrays.asList(LOCATIONS)); }
    public void addPoiLocation(final Location LOCATION) {
        if (null == LOCATION) return;
        if (!getPoiList().contains(LOCATION)) { getPoiList().add(LOCATION); };
    }
    public void removePoiLocation(final Location LOCATION) {
        if (null == LOCATION) return;
        if (getPoiList().contains(LOCATION)) { getPoiList().remove(LOCATION); }
    }
    public void clearPoiLocations() {
        getPoiList().clear();
        fireTileEvent(DATA_EVENT);
    }

    public List<Location> getTrack() {
        if (null == track) { track = new ArrayList<>(); }
        return track;
    }
    public void setTrack(final Location... LOCATIONS) {
        setTrack(Arrays.asList(LOCATIONS));
    }
    public void setTrack(final List<Location> LOCATIONS) {
        getTrack().clear();
        getTrack().addAll(LOCATIONS);
        fireTileEvent(TRACK_EVENT);
    }
    public void clearTrack() {
        getTrack().clear();
        fireTileEvent(TRACK_EVENT);
    }

    public TileColor getTrackColor() { return null == trackColor ? _trackColor : trackColor.get(); }
    public void setTrackColor(final TileColor COLOR) {
        if (null == trackColor) {
            _trackColor = COLOR;
            fireTileEvent(REDRAW_EVENT);
        } else {
            if (!trackColor.isBound()) {
                trackColor.set(COLOR);
            }
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
            if (!mapProvider.isBound()) {
                mapProvider.set(PROVIDER);
            }
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

    public List<String> getCharacterList() {
        if (null == characterList) { characterList = new ArrayList<>(); }
        return characterList;
    }
    public void setCharacters(final String... CHARACTERS) {
        getCharacterList().clear();
        Arrays.stream(CHARACTERS)
              .filter(Objects::nonNull)
              .filter(character -> !character.isEmpty())
              .forEach(character -> getCharacterList().add(character) /*characterList.add(character.substring(0, 1)) */);
    }

    public long getFlipTimeInMS() { return flipTimeInMS; }
    public void setFlipTimeInMS(final long FLIP_TIME) { flipTimeInMS = Helper.clamp(0, 2000, FLIP_TIME); }

    public ItemSorting getItemSorting() { return null == itemSorting ? _itemSorting : itemSorting.get(); }
    public void setItemSorting(final ItemSorting ITEM_SORTING) {
        if (null == itemSorting) {
            _itemSorting = ITEM_SORTING;
            fireTileEvent(DATA_EVENT);
        } else {
            if (!itemSorting.isBound()) {
                itemSorting.set(ITEM_SORTING);
            }
        }
    }
    public ObjectProperty<ItemSorting> itemSortingProperty() {
        if (null == itemSorting) {
            itemSorting = new ObjectPropertyBase<ItemSorting>(_itemSorting) {
                @Override protected void invalidated() { fireTileEvent(DATA_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "itemSorting"; }
            };
            _itemSorting = null;
        }
        return itemSorting;
    }

    public ItemSortingTopic getItemSortingTopic() { return null == itemSortingTopic ? _itemSortingTopic : itemSortingTopic.get(); }
    public void setItemSortingTopic(final ItemSortingTopic ITEM_SORTING_TOPIC) {
        if (null == itemSortingTopic) {
            _itemSortingTopic = ITEM_SORTING_TOPIC;
            fireTileEvent(DATA_EVENT);
        } else {
            if (!itemSortingTopic.isBound()) {
                itemSortingTopic.set(ITEM_SORTING_TOPIC);
            }
        }
    }
    public ObjectProperty<ItemSortingTopic> itemSortingTopicProperty() {
        if (null == itemSortingTopic) {
            itemSortingTopic = new ObjectPropertyBase<ItemSortingTopic>(_itemSortingTopic) {
                @Override protected void invalidated() { fireTileEvent(DATA_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "itemSortingTopic"; }
            };
            _itemSortingTopic = null;
        }
        return itemSortingTopic;
    }

    public ObservableList<ChartData> getChartData() {
        if (null == chartDataList) { chartDataList = FXCollections.observableArrayList(); }
        return chartDataList;
    }
    public void addChartData(final ChartData... DATA) { addChartData(Arrays.asList(DATA)); }
    public void addChartData(final List<ChartData> DATA) {
        getChartData().addAll(DATA);
        updateChartData();
    }
    public void setChartData(final ChartData... DATA) { setChartData(Arrays.asList(DATA)); }
    public void setChartData(final List<ChartData> DATA) {
        getChartData().setAll(DATA);
        updateChartData();
    }
    public void removeChartData(final ChartData... DATA) { removeChartData(Arrays.asList(DATA)); }
    public void removeChartData(final List<ChartData> DATA) { getChartData().removeAll(DATA); }
    public void clearChartData() { getChartData().clear(); }
    private void updateChartData() {
        getChartData().forEach(chartData -> {
            chartData.setAnimated(isAnimated());
            chartData.setAnimationDuration(getAnimationDuration());
        });
    }

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

    public Color getThumbColor() { return thumbColor.getValue(); }
    public void setThumbColor(final Color THUMB_COLOR) { thumbColor.setValue(THUMB_COLOR); }
    public ObjectProperty<Color> thumbColorProperty() { return (ObjectProperty<Color>) thumbColor; }

    /**
     * Returns true if the the UI should be more flat than skeuomorphic
     * This is mainly used for the LedTileSkin
     * @return true if the UI should be more flat than skeuomorphic
     */
    public boolean isFlatUI() { return null == flatUI ? _flatUI : flatUI.get(); }
    /**
     * Defines if the UI should be more flat than skeuomorphic. At the moment only
     * used in the LedTileSkin
     * @param FLATUI
     */
    public void setFlatUI(final boolean FLATUI) {
        if (null == flatUI) {
            _flatUI = FLATUI;
            fireTileEvent(REDRAW_EVENT);
        } else {
            if (!flatUI.isBound()) {
                flatUI.set(FLATUI);
            }
        }
    }
    public BooleanProperty flatUIProperty() {
        if (null == flatUI) {
            flatUI = new BooleanPropertyBase(_flatUI) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "flatUI"; }
            };
        }
        return flatUI;
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
            if (!textSize.isBound()) {
                textSize.set(SIZE);
            }
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
            if (!roundedCorners.isBound()) {
                roundedCorners.set(ROUNDED);
            }
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
     * is especially useful when you work for example with a tile that has a
     * range with a negative minValue
     *
     * @return true if the visualization of the value should start from 0
     */
    public boolean isStartFromZero() { return null == startFromZero ? _startFromZero : startFromZero.get(); }
    /**
     * Defines the behavior of the visualization where the needle/bar should
     * start from 0 instead of the minValue. This is especially useful when
     * working with a tile that has a range with a negative minValue
     *
     * @param IS_TRUE
     */
    public void setStartFromZero(final boolean IS_TRUE) {
        if (null == startFromZero) {
            _startFromZero = IS_TRUE;
            setValue(IS_TRUE && getMinValue() < 0 ? 0 : getMinValue());
            fireTileEvent(REDRAW_EVENT);
        } else {
            if (!startFromZero.isBound()) {
                startFromZero.set(IS_TRUE);
            }
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
            if (!returnToZero.isBound()) {
                returnToZero.set(IS_TRUE);
            }
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
     * The default value is the maxValue of the tile.
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
            if (!minMeasuredValue.isBound()) {
                minMeasuredValue.set(MIN_MEASURED_VALUE);
            }
        }
    }
    public ReadOnlyDoubleProperty minMeasuredValueProperty() {
        if (null == minMeasuredValue) { minMeasuredValue = new SimpleDoubleProperty(this, "minMeasuredValue", _minMeasuredValue); }
        return minMeasuredValue;
    }

    /**
     * Returns the biggest value that was measured after the last reset.
     * The default value is the minValue of the tile.
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
            if (!maxMeasuredValue.isBound()) {
                maxMeasuredValue.set(MAX_MEASURED_VALUE);
            }
        }
    }
    public ReadOnlyDoubleProperty maxMeasuredValueProperty() {
        if (null == maxMeasuredValue) { maxMeasuredValue = new SimpleDoubleProperty(this, "maxMeasuredValue", _maxMeasuredValue); }
        return maxMeasuredValue;
    }

    /**
     * Resets the min- and maxMeasuredValue to the value of the tile.
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
            if (!minMeasuredValueVisible.isBound()) {
                minMeasuredValueVisible.set(VISIBLE);
            }
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
            if (!maxMeasuredValueVisible.isBound()) {
                maxMeasuredValueVisible.set(VISIBLE);
            }
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
     * Returns true if the old value of the tile is visible (not implemented)
     *
     * @return true if the old value of the tile is visible (not implemented)
     */
    public boolean isOldValueVisible() { return null == oldValueVisible ? _oldValueVisible : oldValueVisible.get(); }
    /**
     * Defines if the old value of the tile should be visible (not implemented)
     *
     * @param VISIBLE
     */
    public void setOldValueVisible(final boolean VISIBLE) {
        if (null == oldValueVisible) {
            _oldValueVisible = VISIBLE;
            fireTileEvent(VISIBILITY_EVENT);
        } else {
            if (!oldValueVisible.isBound()) {
                oldValueVisible.set(VISIBLE);
            }
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
     * Returns true if the visualization of the tile value is visible.
     * Usually this is a Label or Text node.
     *
     * @return true if the visualization of the tile value is visible
     */
    public boolean isValueVisible() { return null == valueVisible ? _valueVisible : valueVisible.get(); }
    /**
     * Defines if the visualization of the tile value should be visible.
     *
     * @param VISIBLE
     */
    public void setValueVisible(final boolean VISIBLE) {
        if (null == valueVisible) {
            _valueVisible = VISIBLE;
            fireTileEvent(VISIBILITY_EVENT);
        } else {
            if (!valueVisible.isBound()) {
                valueVisible.set(VISIBLE);
            }
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
     * Returns the Paint object that will be used to fill the tile foreground.
     * This is usally a Color object.
     *
     * @return the Paint object that will be used to fill the tile foreground
     */
    public Color getForegroundColor() { return null == foregroundColor ? _foregroundColor : foregroundColor.get(); }
    /**
     * Defines the Paint object that will be used to fill the tile foreground.
     *
     * @param COLOR
     */
    public void setForegroundColor(final Color COLOR) {
        if (null == foregroundColor) {
            _foregroundColor = COLOR;
            fireTileEvent(REDRAW_EVENT);
        } else {
            if (!foregroundColor.isBound()) {
                foregroundColor.set(COLOR);
            }
        }
    }
    public ObjectProperty<Color> foregroundColorProperty() {
        if (null == foregroundColor) {
            foregroundColor = new ObjectPropertyBase<>(_foregroundColor) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "foregroundColor"; }
            };
            _foregroundColor = null;
        }
        return foregroundColor;
    }
    
    /**
     * Returns the Paint object that will be used to fill the tile background.
     * This is usally a Color object.
     *
     * @return the Paint object that will be used to fill the tile background
     */
    public Color getBackgroundColor() { return null == backgroundColor ? _backgroundColor : backgroundColor.get(); }
    /**
     * Defines the Paint object that will be used to fill the tile background.
     *
     * @param COLOR
     */
    public void setBackgroundColor(final Color COLOR) {
        if (null == backgroundColor) {
            _backgroundColor = COLOR;
            fireTileEvent(REDRAW_EVENT);
        } else {
            if (!backgroundColor.isBound()) {
                backgroundColor.set(COLOR);
            }
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
     * Returns the Paint object that will be used to draw the border of the tile.
     * Usually this is a Color object.
     *
     * @return the Paint object that will be used to draw the border of the tile
     */
    public Color getBorderColor() { return null == borderColor ? _borderColor : borderColor.get(); }
    /**
     * Defines the Paint object that will be used to draw the border of the tile.
     *
     * @param PAINT
     */
    public void setBorderColor(final Color PAINT) {
        if (null == borderColor) {
            _borderColor = PAINT;
            fireTileEvent(REDRAW_EVENT);
        } else {
            if (!borderColor.isBound()) {
                borderColor.set(PAINT);
            }
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
     * Returns the width in pixels that will be used to draw the border of the tile.
     * The value will be clamped between 0 and 50 pixels.
     *
     * @return the width in pixels that will be used to draw the border of the tile
     */
    public double getBorderWidth() { return null == borderWidth ? _borderWidth : borderWidth.get(); }
    /**
     * Defines the width in pixels that will be used to draw the border of the tile.
     * The value will be clamped between 0 and 50 pixels.
     *
     * @param WIDTH
     */
    public void setBorderWidth(final double WIDTH) {
        if (null == borderWidth) {
            _borderWidth = clamp(0.0, 50.0, WIDTH);
            fireTileEvent(REDRAW_EVENT);
        } else {
            if (!borderWidth.isBound()) {
                borderWidth.set(WIDTH);
            }
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
     * the radial tiles.
     *
     * @return the color that will be used to colorize the knob of the radial tiles
     */
    public Color getKnobColor() { return null == knobColor ? _knobColor : knobColor.get(); }
    /**
     * Defines the color that will be used to colorize the knob of
     * the radial tiles.
     *
     * @param COLOR
     */
    public void setKnobColor(final Color COLOR) {
        if (null == knobColor) {
            _knobColor = COLOR;
            fireTileEvent(RESIZE_EVENT);
        } else {
            if (!knobColor.isBound()) {
                knobColor.set(COLOR);
            }
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
            if (!activeColor.isBound()) {
                activeColor.set(COLOR);
            }
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
     * Returns true if setting the value of the tile will be animated
     * using the duration defined in animationDuration [ms].
     * Keep in mind that it only makes sense to animate the setting if
     * the data rate is low (more than 1 value per second). If you use real
     * live measured data you should set animated to false.
     *
     * @return true if setting the value of the tile will be animated
     */
    public boolean isAnimated() { return null == animated ? _animated : animated.get(); }
    /**
     * Defines if setting the value of the tile should be animated using
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
            updateChartData();
            fireTileEvent(ANIMATED ? ANIMATED_ON_EVENT : ANIMATED_OFF_EVENT);
        } else {
            if (!animated.isBound()) {
                animated.set(ANIMATED);
            }
        }
    }
    public BooleanProperty animatedProperty() {
        if (null == animated) {
            animated = new BooleanPropertyBase(_animated) {
                @Override protected void invalidated() {
                    updateChartData();
                    fireTileEvent(get() ? ANIMATED_ON_EVENT : ANIMATED_OFF_EVENT);
                }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "animated"; }
            };
        }
        return animated;
    }

    /**
     * Returns the duration in milliseconds that will be used to animate
     * the needle/bar of the tile from the last value to the next value.
     * This will only be used if animated == true. This value will be
     * clamped in the range of 10ms - 10s.
     *
     * @return the duration in ms that will be used to animate the needle/bar
     */
    public long getAnimationDuration() { return animationDuration; }
    /**
     * Defines the duration in milliseconds that will be used to animate
     * the needle/bar of the tile from the last value to the next value.
     * This will only be used if animated == true. This value will be
     * clamped in the range of 10ms - 10s.
     *
     * @param ANIMATION_DURATION
     */
    public void setAnimationDuration(final long ANIMATION_DURATION) { animationDuration = clamp(10, 10000, ANIMATION_DURATION); }

    /**
     * Returns the duration in milliseconds that will be used as pause
     * between animations in the MatrixIconSkin
     * clamped in the range of 10ms - 10s.
     *
     * @return the duration in milliseconds that will be used as pause
     */
    public long getPauseDuration() { return pauseDuration; }
    /**
     * Defines the duration in milliseconds that will be used as pause
     * between animations in the MatrixIconSkin
     * clamped in the range of 10ms - 10s.
     *
     * @param PAUSE_DURATION
     */
    public void setPauseDuration(final long PAUSE_DURATION) { pauseDuration = clamp(10, 10000, PAUSE_DURATION); }


    /**
     * Returns the angle in degree that defines the start of the scale with
     * it's minValue in a radial tile. If set to 0 the scale will start at
     * the bottom center and the direction of counting is mathematical correct
     * counter-clockwise.
     * Means if you would like to start the scale on the left side in the
     * middle of the tile height the startAngle should be set to 270 degrees.
     *
     * @return the angle in degree that defines the start of the scale
     */
    public double getStartAngle() { return null == startAngle ? _startAngle : startAngle.get(); }
    /**
     * Defines the angle in degree that defines the start of the scale with
     * it's minValue in a radial tile. If set to 0 the scale will start at
     * the bottom center and the direction of counting is mathematical correct
     * counter-clockwise.
     * Means if you would like to start the scale on the left side in the
     * middle of the tile height the startAngle should be set to 270 degrees.
     *
     * @param ANGLE
     */
    public void setStartAngle(final double ANGLE) {
        if (null == startAngle) {
            _startAngle = clamp(0.0, 360.0, ANGLE);
            fireTileEvent(RECALC_EVENT);
        } else {
            if (!startAngle.isBound()) {
                startAngle.set(ANGLE);
            }
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
     * of the radial tile. The given range will be clamped in the range of
     * 0 - 360 degrees and will be drawn in the direction dependent on the
     * scaleDirection.
     *
     * @return the angle range in degree that will be used to draw the scale
     */
    public double getAngleRange() { return null == angleRange ? _angleRange : angleRange.get(); }
    /**
     * Defines the angle range in degree that will be used to draw the scale
     * of the radial tile. The given range will be clamped in the range of
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
            if (!angleRange.isBound()) {
                angleRange.set(tmpAngleRange);
            }
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
            if (!angleStep.isBound()) {
                angleStep.set(STEP);
            }
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
            if (!autoScale.isBound()) {
                autoScale.set(AUTO_SCALE);
            }
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
     * In some tiles inner- and dropshadows will be used which will be
     * switched on/off by setting the shadowsEnabled property.
     *
     * @return true if effects like shadows will be drawn
     */
    public boolean isShadowsEnabled() { return null == shadowsEnabled ? _shadowsEnabled : shadowsEnabled.get(); }
    /**
     * Defines if effects like shadows should be drawn.
     * In some tiles inner- and dropshadows will be used which will be
     * switched on/off by setting the shadowsEnabled property.
     *
     * @param ENABLED
     */
    public void setShadowsEnabled(final boolean ENABLED) {
        if (null == shadowsEnabled) {
            _shadowsEnabled = ENABLED;
            fireTileEvent(REDRAW_EVENT);
        } else {
            if (!shadowsEnabled.isBound()) {
                shadowsEnabled.set(ENABLED);
            }
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
            if (!locale.isBound()) {
                locale.set(LOCALE);
            }
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
     * in the tile (NOT USED AT THE MOMENT)
     *
     * @return the number format that will bused to format the value
     */
    public NumberFormat getNumberFormat() { return null == numberFormat ? _numberFormat : numberFormat.get(); }
    /**
     * Defines the number format that will be used to format the value
     * in the tile (NOT USED AT THE MOMENT)
     *
     * @param FORMAT
     */
    public void setNumberFormat(final NumberFormat FORMAT) {
        if (null == numberFormat) {
            _numberFormat = null == FORMAT ? NumberFormat.getInstance(getLocale()) : FORMAT;
            fireTileEvent(RESIZE_EVENT);
        } else {
            if (!numberFormat.isBound()) {
                numberFormat.set(FORMAT);
            }
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
     * value of the tile. The number of decimals will be clamped to
     * a value between 0-3.
     *
     * @return the number of decimals that will be used to format the value
     */
    public int getDecimals() { return null == decimals ? _decimals : decimals.get(); }
    /**
     * Defines the number of decimals that will be used to format the
     * value of the tile. The number of decimals will be clamped to
     * a value between 0-3.
     *
     * @param DECIMALS
     */
    public void setDecimals(final int DECIMALS) {
        if (null == decimals) {
            _decimals = clamp(0, MAX_NO_OF_DECIMALS, DECIMALS);
            fireTileEvent(REDRAW_EVENT);
        } else {
            if (!decimals.isBound()) {
                decimals.set(DECIMALS);
            }
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
     * ticklabels of the tile. The number of tickLabelDecimals will be clamped to
     * a value between 0-3.
     *
     * @return the number of tickLabelDecimals that will be used to format the ticklabels
     */
    public int getTickLabelDecimals() { return null == tickLabelDecimals ? _tickLabelDecimals : tickLabelDecimals.get(); }
    /**
     * Defines the number of tickLabelDecimals that will be used to format the
     * ticklabels of the tile. The number of tickLabelDecimals will be clamped to
     * a value between 0-3.
     *
     * @param DECIMALS
     */
    public void setTickLabelDecimals(final int DECIMALS) {
        if (null == tickLabelDecimals) {
            _tickLabelDecimals = clamp(0, MAX_NO_OF_DECIMALS, DECIMALS);
            fireTileEvent(REDRAW_EVENT);
        } else {
            if (!tickLabelDecimals.isBound()) {
                tickLabelDecimals.set(DECIMALS);
            }
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
    
    public boolean getTickLabelsXVisible() { return null == tickLabelsXVisible ? _tickLabelsXVisible : tickLabelsXVisible.get(); }
    public void setTickLabelsXVisible(final boolean VISIBLE) {
        if (null == this.tickLabelsXVisible) {
            _tickLabelsXVisible = VISIBLE;
            fireTileEvent(VISIBILITY_EVENT);
        } else {
            if (!tickLabelsXVisible.isBound()) {
                tickLabelsXVisible.set(VISIBLE);
            }
        }
    }
    public BooleanProperty tickLabelsXVisibleProperty() {
        if (null == tickLabelsXVisible) {
            tickLabelsXVisible = new BooleanPropertyBase(_tickLabelsXVisible) {
                @Override protected void invalidated() { fireTileEvent(VISIBILITY_EVENT); }  
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "tickLabelsXVisible"; }
            };
        }
        return tickLabelsXVisible;
    }

    public boolean getTickLabelsYVisible() { return null == tickLabelsYVisible ? _tickLabelsYVisible : tickLabelsYVisible.get(); }
    public void setTickLabelsYVisible(final boolean VISIBLE) {
        if (null == this.tickLabelsYVisible) {
            _tickLabelsYVisible = VISIBLE;
            fireTileEvent(VISIBILITY_EVENT);
        } else {
            if (!tickLabelsYVisible.isBound()) {
                tickLabelsYVisible.set(VISIBLE);
            }
        }
    }
    public BooleanProperty tickLabelsYVisibleProperty() {
        if (null == tickLabelsYVisible) {
            tickLabelsYVisible = new BooleanPropertyBase(_tickLabelsYVisible) {
                @Override protected void invalidated() { fireTileEvent(VISIBILITY_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "tickLabelsYVisible"; }
            };
        }
        return tickLabelsYVisible;
    }
    
    public boolean getMinValueVisible() { return null == minValueVisible ? _minValueVisible : minValueVisible.get(); }
    public void setMinValueVisible(final boolean VISIBLE) {
        if (null == this.minValueVisible) {
            _minValueVisible = VISIBLE;
            fireTileEvent(VISIBILITY_EVENT);
        } else {
            if (!minValueVisible.isBound()) {
                minValueVisible.set(VISIBLE);
            }
        }
    }
    public BooleanProperty minValueVisibleProperty() {
        if (null == minValueVisible) {
            minValueVisible = new BooleanPropertyBase(_minValueVisible) {
                @Override protected void invalidated() { fireTileEvent(VISIBILITY_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "minValueVisible"; }
            };
        }
        return minValueVisible;
    }

    public boolean getMaxValueVisible() { return null == maxValueVisible ? _maxValueVisible : maxValueVisible.get(); }
    public void setMaxValueVisible(final boolean VISIBLE) {
        if (null == this.maxValueVisible) {
            _maxValueVisible = VISIBLE;
            fireTileEvent(VISIBILITY_EVENT);
        } else {
            if (!maxValueVisible.isBound()) {
                maxValueVisible.set(VISIBLE);
            }
        }
    }
    public BooleanProperty maxValueVisibleProperty() {
        if (null == maxValueVisible) {
            maxValueVisible = new BooleanPropertyBase(_maxValueVisible) {
                @Override protected void invalidated() { fireTileEvent(VISIBILITY_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "maxValueVisible"; }
            };
        }
        return maxValueVisible;
    }
    
    /**
     * Returns the color that will be used to colorize the needle of
     * the radial tiles.
     *
     * @return the color that wil be used to colorize the needle
     */
    public Color getNeedleColor() { return null == needleColor ? _needleColor : needleColor.get(); }
    /**
     * Defines the color that will be used to colorize the needle of
     * the radial tiles.
     *
     * @param COLOR
     */
    public void setNeedleColor(final Color COLOR) {
        if (null == needleColor) {
            _needleColor = COLOR;
            fireTileEvent(REDRAW_EVENT);
        } else {
            if (!needleColor.isBound()) {
                needleColor.set(COLOR);
            }
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
     * the tile (if it has a bar).
     *
     * @return the color that will be used to colorized the bar (if available)
     */
    public Color getBarColor() { return null == barColor ? _barColor : barColor.get(); }
    /**
     * Defines the color that will be used to colorize the bar of
     * the tile (if it has a bar).
     *
     * @param COLOR
     */
    public void setBarColor(final Color COLOR) {
        if (null == barColor) {
            _barColor = COLOR;
            fireTileEvent(REDRAW_EVENT);
        } else {
            if (!barColor.isBound()) {
                barColor.set(COLOR);
            }
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
     * the tile (if it has a bar).
     *
     * @return the color that will be used to colorize the bar background
     */
    public Color getBarBackgroundColor() { return null == barBackgroundColor ? _barBackgroundColor : barBackgroundColor.get(); }
    /**
     * Returns the color that will be used to colorize the bar background of
     * the tile (if it has a bar).
     *
     * @param COLOR
     */
    public void setBarBackgroundColor(final Color COLOR) {
        if (null == barBackgroundColor) {
            _barBackgroundColor = COLOR;
            fireTileEvent(REDRAW_EVENT);
        } else {
            if (!barBackgroundColor.isBound()) {
                barBackgroundColor.set(COLOR);
            }
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
     * of the tile.
     *
     * @return the color that will be used to colorize the title
     */
    public Color getTitleColor() { return null == titleColor ? _titleColor : titleColor.get(); }
    /**
     * Defines the color that will be used to colorize the title
     * of the tile.
     *
     * @param COLOR
     */
    public void setTitleColor(final Color COLOR) {
        if (null == titleColor) {
            _titleColor = COLOR;
            fireTileEvent(REDRAW_EVENT);
        } else {
            if (!titleColor.isBound()) {
                titleColor.set(COLOR);
            }
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
     * of the tile.
     *
     * @return the color that will be used to colorize the description
     */
    public Color getDescriptionColor() { return null == descriptionColor ? _descriptionColor : descriptionColor.get(); }
    /**
     * Defines the color that will be used to colorize the description text
     * of the tile.
     *
     * @param COLOR
     */
    public void setDescriptionColor(final Color COLOR) {
        if (null == descriptionColor) {
            _descriptionColor = COLOR;
            fireTileEvent(REDRAW_EVENT);
        } else {
            if (!descriptionColor.isBound()) {
                descriptionColor.set(COLOR);
            }
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
     * of the tile.
     *
     * @return the color that will be used to colorize the unit
     */
    public Color getUnitColor() { return null == unitColor ? _unitColor : unitColor.get(); }
    /**
     * Defines the color that will be used to colorize the unit
     * of the tile.
     *
     * @param COLOR
     */
    public void setUnitColor(final Color COLOR) {
        if (null == unitColor) {
            _unitColor = COLOR;
            fireTileEvent(REDRAW_EVENT);
        } else {
            if (!unitColor.isBound()) {
                unitColor.set(COLOR);
            }
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
     * of the tile.
     *
     * @return the color that will be used to colorize the value
     */
    public Color getValueColor() { return null == valueColor ? _valueColor : valueColor.get(); }
    /**
     * Defines the color that will be used to colorize the value
     * of the tile.
     *
     * @param COLOR
     */
    public void setValueColor(final Color COLOR) {
        if (null == valueColor) {
            _valueColor = COLOR;
            fireTileEvent(REDRAW_EVENT);
        } else {
            if (!valueColor.isBound()) {
                valueColor.set(COLOR);
            }
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
     * indicator of the tile.
     *
     * @return the color that will be used to colorize the threshold indicator
     */
    public Color getThresholdColor() { return null == thresholdColor ? _thresholdColor : thresholdColor.get(); }
    /**
     * Defines the color that will be used to colorize the threshold
     * indicator of the tile.
     *
     * @param COLOR
     */
    public void setThresholdColor(final Color COLOR) {
        if (null == thresholdColor) {
            _thresholdColor = COLOR;
            fireTileEvent(REDRAW_EVENT);
        } else {
            if (!thresholdColor.isBound()) {
                thresholdColor.set(COLOR);
            }
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

    public Color getLowerThresholdColor() { return null == lowerThresholdColor ? _lowerThresholdColor : lowerThresholdColor.get(); }
    public void setLowerThresholdColor(final Color COLOR) {
        if (null == lowerThresholdColor) {
            _lowerThresholdColor = COLOR;
            fireTileEvent(REDRAW_EVENT);
        } else {
            if (!lowerThresholdColor.isBound()) {
                lowerThresholdColor.set(COLOR);
            }
        }
    }
    public ObjectProperty<Color> lowerThresholdColorProperty() {
        if (null == lowerThresholdColor) {
            lowerThresholdColor = new ObjectPropertyBase<Color>(_lowerThresholdColor) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "lowerThresholdColor"; }
            };
            _lowerThresholdColor = null;
        }
        return lowerThresholdColor;
    }

    /**
     * Returns true if the value of the tile should be checked against
     * all sections (if sections not empty). If a value enters a section
     * or leaves a section it will fire an event. The check will be performed
     * after the animation is finished (if animated == true).
     *
     * @return true if the value of the tile should be checked against all sections
     */
    public boolean getCheckSectionsForValue() { return null == checkSectionsForValue ? _checkSectionsForValue : checkSectionsForValue.get(); }
    /**
     * Defines if the value of the tile should be checked against
     * all sections (if sections not empty). If a value enters a section
     * or leaves a section it will fire an event. The check will be performed
     * after the animation is finished (if animated == true).
     *
     * @param CHECK
     */
    public void setCheckSectionsForValue(final boolean CHECK) {
        if (null == checkSectionsForValue) {
            _checkSectionsForValue = CHECK;
        } else {
            if (!checkSectionsForValue.isBound()) {
                checkSectionsForValue.set(CHECK);
            }
        }
    }
    public BooleanProperty checkSectionsForValueProperty() {
        if (null == checkSectionsForValue) { checkSectionsForValue = new SimpleBooleanProperty(Tile.this, "checkSectionsForValue", _checkSectionsForValue); }
        return checkSectionsForValue;
    }

    /**
     * Returns true if the value of the tile should be checked against
     * the threshold. In case the value exceeds or underruns the threshold an
     * event (EXCEEDED / UNDERRUN) will be fired. The check will be performed
     * after the animation is finished (if animated == true).
     *
     * @return true if the value of the tile should be checked against the threshold
     */
    public boolean isCheckThreshold() { return null == checkThreshold ? _checkThreshold : checkThreshold.get(); }
    /**
     * Defines if the current value should be checked against the threshold
     * @param CHECK
     */
    public void setCheckThreshold(final boolean CHECK) {
        if (null == checkThreshold) {
            _checkThreshold = CHECK;
        } else {
            if (!checkThreshold.isBound()) {
                checkThreshold.set(CHECK);
            }
        }
    }
    public BooleanProperty checkThresholdProperty() {
        if (null == checkThreshold) { checkThreshold = new SimpleBooleanProperty(Tile.this, "checkThreshold", _checkThreshold); }
        return checkThreshold;
    }

    /**
     * Returns true if the value of the tile should be checked against
     * the lower threshold. In case the value exceeds or underruns the lower threshold an
     * event (EXCEEDED / UNDERRUN) will be fired. The check will be performed
     * after the animation is finished (if animated == true).
     *
     * @return true if the value of the tile should be checked against the lower threshold
     */
    public boolean isCheckLowerThreshold() { return null == checkLowerThreshold ? _checkLowerThreshold : checkLowerThreshold.get(); }
    /**
     * Defines if the current value should be checked against the lower threshold
     * @param CHECK
     */
    public void setCheckLowerThreshold(final boolean CHECK) {
        if (null == checkLowerThreshold) {
            _checkLowerThreshold = CHECK;
        } else {
            if (!checkLowerThreshold.isBound()) {
                checkLowerThreshold.set(CHECK);
            }
        }
    }
    public BooleanProperty checkLowerThresholdProperty() {
        if (null == checkLowerThreshold) { checkLowerThreshold = new SimpleBooleanProperty(Tile.this, "checkLowerThreshold", _checkLowerThreshold); }
        return checkLowerThreshold;
    }

    /**
     * Returns true if an inner shadow should be drawn on the tile
     * background.
     *
     * @return true if an inner shadow should be drawn on the tile background
     */
    public boolean isInnerShadowEnabled() { return null == innerShadowEnabled ? _innerShadowEnabled : innerShadowEnabled.get(); }
    /**
     * Defines if an inner shadow should be drawn on the tile
     * background.
     *
     * @param ENABLED
     */
    public void setInnerShadowEnabled(final boolean ENABLED) {
        if (null == innerShadowEnabled) {
            _innerShadowEnabled = ENABLED;
            fireTileEvent(REDRAW_EVENT);
        } else {
            if (!innerShadowEnabled.isBound()) {
                innerShadowEnabled.set(ENABLED);
            }
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
            if (!thresholdVisible.isBound()) {
                thresholdVisible.set(VISIBLE);
            }
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
     * Returns true if the lower threshold indicator should be drawn.
     *
     * @return true if the lower threshold indicator should be drawn
     */
    public boolean isLowerThresholdVisible() { return null == lowerThresholdVisible ? _lowerThresholdVisible : lowerThresholdVisible.get(); }
    /**
     * Defines if the lower threshold indicator should be drawn
     *
     * @param VISIBLE
     */
    public void setLowerThresholdVisible(final boolean VISIBLE) {
        if (null == lowerThresholdVisible) {
            _lowerThresholdVisible = VISIBLE;
            fireTileEvent(VISIBILITY_EVENT);
        } else {
            if (!lowerThresholdVisible.isBound()) {
                lowerThresholdVisible.set(VISIBLE);
            }
        }
    }
    public BooleanProperty lowerThresholdVisibleProperty() {
        if (null == lowerThresholdVisible) {
            lowerThresholdVisible = new BooleanPropertyBase(_lowerThresholdVisible) {
                @Override protected void invalidated() { fireTileEvent(VISIBILITY_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "lowerThresholdVisible"; }
            };
        }
        return lowerThresholdVisible;
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
            if (!averageVisible.isBound()) {
                averageVisible.set(VISIBLE);
            }
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
            if (!sectionsVisible.isBound()) {
                sectionsVisible.set(VISIBLE);
            }
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
            if (!sectionsAlwaysVisible.isBound()) {
                sectionsAlwaysVisible.set(VISIBLE);
            }
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
            if (!sectionTextVisible.isBound()) {
                sectionTextVisible.set(VISIBLE);
            }
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
            if (!sectionIconsVisible.isBound()) {
                sectionIconsVisible.set(VISIBLE);
            }
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
            //fireTileEvent(REDRAW_EVENT);
            fireTileEvent(HIGHLIGHT_SECTIONS);
        } else {
            if (!highlightSections.isBound()) {
                highlightSections.set(HIGHLIGHT);
            }
        }
    }
    public BooleanProperty highlightSectionsProperty() {
        if (null == highlightSections) {
            highlightSections = new BooleanPropertyBase(_highlightSections) {
                @Override protected void invalidated() { fireTileEvent(HIGHLIGHT_SECTIONS); }
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
            if (!orientation.isBound()) {
                orientation.set(ORIENTATION);
            }
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
            if (!keepAspect.isBound()) {
                keepAspect.set(KEEP);
            }
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
            if (!alert.isBound()) {
                alert.set(ALERT);
            }
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
            if (!alertMessage.isBound()) {
                alertMessage.set(MESSAGE);
            }
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
     * in the SparkLineTileSkin and RadarChartTileSkin (Polygon mode) to smooth the path.
     * In a custom skin it could be also used for other things.
     * @return true when smoothing is enabled
     */
    public boolean isSmoothing() { return null == smoothing ? _smoothing : smoothing.get(); }
    /**
     * Defines if the smoothing property should be enabled/disabled.
     * At the moment this is only used in the SparkLineTileSkin and
     * RadarChartTileSkin.
     * @param SMOOTHING
     */
    public void setSmoothing(final boolean SMOOTHING) {
        if (null == smoothing) {
            _smoothing = SMOOTHING;
            fireTileEvent(REDRAW_EVENT);
        } else {
            if (!smoothing.isBound()) {
                smoothing.set(SMOOTHING);
            }
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
    public ZonedDateTime getTime() {
        if (null == time) {
            ZonedDateTime now = ZonedDateTime.now();
            time = new ObjectPropertyBase<ZonedDateTime>(now) {
                @Override protected void invalidated() {
                    zoneId = get().getZone();
                    fireTileEvent(RECALC_EVENT);
                    if (!isRunning() && isAnimated()) {
                        long animationDuration = getAnimationDuration();
                        timeline.stop();
                        final KeyValue KEY_VALUE = new KeyValue(currentTime, now.toEpochSecond());
                        final KeyFrame KEY_FRAME = new KeyFrame(javafx.util.Duration.millis(animationDuration), KEY_VALUE);
                        timeline.getKeyFrames().setAll(KEY_FRAME);
                        timeline.setOnFinished(e -> fireTileEvent(FINISHED_EVENT));
                        timeline.play();
                    } else {
                        currentTime.set(now.toEpochSecond());
                        fireTileEvent(FINISHED_EVENT);
                    }
                }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "time"; }
            };
        }
        return time.get();
    }
    /**
     * Defines the current time of the clock.
     * @param TIME
     */
    public void setTime(final ZonedDateTime TIME) {
        time.set(TIME);
    }
    public void setTime(final long EPOCH_SECONDS) {
        if (!time.isBound()) {
            time.set(ZonedDateTime.ofInstant(Instant.ofEpochSecond(EPOCH_SECONDS), getZoneId()));
        }
    }
    public ObjectProperty<ZonedDateTime> timeProperty() { return time; }

    /**
     * Returns the current time in epoch seconds
     * @return the current time in epoch seconds
     */
    public long getCurrentTime() { return currentTime.get(); }
    public ReadOnlyLongProperty currentTimeProperty() { return currentTime; }

    public ZoneId getZoneId() {
        if (null == zoneId) { zoneId = getTime().getZone(); }
        return zoneId;
    }

    
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
            if (!text.isBound()) {
                text.set(TEXT);
            }
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
            if (!textAlignment.isBound()) {
                textAlignment.set(ALIGNMENT);
            }
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
    public ObservableList<TimeSection> getTimeSections() {
        if (null == timeSections) { timeSections = FXCollections.observableArrayList(); }
        return timeSections;
    }
    /**
     * Sets the sections to the given list of TimeSection objects. The
     * sections will be used to colorize areas with a special
     * meaning. Sections in the Medusa library usually are less eye-catching
     * than Areas.
     * @param SECTIONS
     */
    public void setTimeSections(final List<TimeSection> SECTIONS) {
        getTimeSections().setAll(SECTIONS);
        getTimeSections().sort(new TimeSectionComparator());
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
        getTimeSections().add(SECTION);
        getTimeSections().sort(new TimeSectionComparator());
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
        getTimeSections().remove(SECTION);
        getTimeSections().sort(new TimeSectionComparator());
        fireTileEvent(SECTION_EVENT);
    }
    /**
     * Clears the list of sections.
     */
    public void clearTimeSections() {
        getTimeSections().clear();
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
            if (!discreteSeconds.isBound()) {
                discreteSeconds.set(DISCRETE);
            }
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
            if (!discreteMinutes.isBound()) {
                discreteMinutes.set(DISCRETE);
            }
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
            if (!discreteHours.isBound()) {
                discreteHours.set(DISCRETE);
            }
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
            if (!secondsVisible.isBound()) {
                secondsVisible.set(VISIBLE);
            }
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
            if (!textVisible.isBound()) {
                textVisible.set(VISIBLE);
            }
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
            if (!dateVisible.isBound()){
                dateVisible.set(VISIBLE);
            }
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
            if (!running.isBound()) {
                running.set(RUNNING);
            }
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
            if (!textColor.isBound()) {
                textColor.set(COLOR);
            }
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
            if (!dateColor.isBound()) {
                dateColor.set(COLOR);
            }
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
            if (!hourTickMarkColor.isBound()) {
                hourTickMarkColor.set(COLOR);
            }
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
            if (!minuteTickMarkColor.isBound()) {
                minuteTickMarkColor.set(COLOR);
            }
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
            if (!alarmColor.isBound()) {
                alarmColor.set(COLOR);
            }
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
     * Returns the color that will be used to colorize tick labels (e.g. in the SparkLineTileSkin)
     * @return the color that will be used to colorize tick labels (e.g. in the SparkLineTileSkin)
     */
    public Color getTickLabelColor() { return null == tickLabelColor ? _tickLabelColor : tickLabelColor.get(); }
    public void setTickLabelColor(final Color COLOR) {
        if (null == tickLabelColor) {
            _tickLabelColor = COLOR;
            fireTileEvent(REDRAW_EVENT);
        } else {
            if (!tickLabelColor.isBound()) {
                tickLabelColor.set(COLOR);
            }
        }
    }
    public ObjectProperty<Color> tickLabelColorProperty() {
        if (null == tickLabelColor) {
            tickLabelColor = new ObjectPropertyBase<Color>(_tickLabelColor) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "tickLabelColor"; }
            };
            _tickLabelColor = null;
        }
        return tickLabelColor;
    }

    /**
     * Returns the color that will be used to colorize tick marks in some skins
     * @return the color that will be used to colorize tick marks in some skins
     */
    public Color getTickMarkColor() { return null == tickMarkColor ? _tickMarkColor : tickMarkColor.get(); }
    public void setTickMarkColor(final Color COLOR) {
        if (null == tickMarkColor) {
            _tickMarkColor = COLOR;
            fireTileEvent(REDRAW_EVENT);
        } else {
            if (!tickMarkColor.isBound()) {
                tickMarkColor.set(COLOR);
            }
        }
    }
    public ObjectProperty<Color> tickMarkColorProperty() {
        if (null == tickMarkColor) {
            tickMarkColor = new ObjectPropertyBase<Color>(_tickMarkColor) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "tickMarkColor"; }
            };
            _tickMarkColor = null;
        }
        return tickMarkColor;
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
            if (!hourTickMarksVisible.isBound()) {
                hourTickMarksVisible.set(VISIBLE);
            }
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
            if (!minuteTickMarksVisible.isBound()) {
                minuteTickMarksVisible.set(VISIBLE);
            }
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
            if (!hourColor.isBound()) {
                hourColor.set(COLOR);
            }
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
            if (!minuteColor.isBound()) {
                minuteColor.set(COLOR);
            }
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
            if (!secondColor.isBound()) {
                secondColor.set(COLOR);
            }
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
            if (!alarmsEnabled.isBound()) {
                alarmsEnabled.set(CHECK);
            }
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
            if (!alarmsVisible.isBound()) {
                alarmsVisible.set(VISIBLE);
            }
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
    public ObservableList<Alarm> getAlarms() {
        if (null == alarms) { alarms = FXCollections.observableArrayList(); }
        return alarms;
    }
    /**
     * Sets the alarms to the given list of Alarm objects.
     * @param ALARMS
     */
    public void setAlarms(final List<Alarm> ALARMS) { getAlarms().setAll(ALARMS); }
    /**
     * Sets the alarms to the given array of Alarm objects.
     * @param ALARMS
     */
    public void setAlarms(final Alarm... ALARMS) { setAlarms(Arrays.asList(ALARMS)); }
    /**
     * Adds the given Alarm object from the list of alarms.
     * @param ALARM
     */
    public void addAlarm(final Alarm ALARM) { if (!getAlarms().contains(ALARM)) { getAlarms().add(ALARM); }}
    /**
     * Removes the given Alarm object from the list of alarms.
     * @param ALARM
     */
    public void removeAlarm(final Alarm ALARM) { if (getAlarms().contains(ALARM)) { getAlarms().remove(ALARM); }}
    /**
     * Clears the list of alarms.
     */
    public void clearAlarms() { getAlarms().clear(); }

    /**
     * Returns the text that will be shown in the Tile tooltip
     * @return the text that will be shown in the Tile tooltip
     */
    public String getTooltipText() { return null == tooltipText ? _tooltipText : tooltipText.get(); }
    /**
     * Defines the text that will be shown in the Tile tooltip
     * @param TEXT
     */
    public void setTooltipText(final String TEXT) {
        if (null == tooltipText) {
        tooltip.setText(TEXT);
            if (null == TEXT || TEXT.isEmpty()) {
                setTooltip(null);
            } else {
                setTooltip(tooltip);
            }
        } else {
            if (!tooltipText.isBound()) {
                tooltipText.set(TEXT);
            }
        }

    }
    public StringProperty tooltipTextProperty() {
        if (null == tooltipText) {
            tooltipText = new StringPropertyBase() {
                @Override protected void invalidated() {
                    tooltip.setText(get());
                    if (null == get() || get().isEmpty()) {
                        setTooltip(null);
                    } else {
                        setTooltip(tooltip);
                    }
                }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "tooltipText"; }
            };
            _tooltipText = null;
        }
        return tooltipText;
    }

    public Axis getXAxis() { return null == xAxis ? _xAxis : xAxis.get(); }
    public void setXAxis(final Axis AXIS) {
        if (null == xAxis) {
            _xAxis = AXIS;
            fireTileEvent(RESIZE_EVENT);
        } else {
            if (!xAxis.isBound()) {
                xAxis.set(AXIS);
            }
        }
    }
    public ObjectProperty<Axis> xAxisProperty() {
        if (null == xAxis) {
            xAxis = new ObjectPropertyBase<Axis>(_xAxis) {
                @Override protected void invalidated() { fireTileEvent(RESIZE_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() {
                    return "xAxis";
                }
            };
            _xAxis = null;
        }
        return xAxis;
    }

    public Axis getYAxis() { return null == yAxis ? _yAxis : yAxis.get(); }
    public void setYAxis(final Axis AXIS) {
        if (null == yAxis) {
            _yAxis = AXIS;
            fireTileEvent(RESIZE_EVENT);
        } else {
            if (!yAxis.isBound()) {
                yAxis.set(AXIS);
            }
        }
    }
    public ObjectProperty<Axis> yAxisProperty() {
        if (null == yAxis) {
            yAxis = new ObjectPropertyBase<Axis>(_yAxis) {
                @Override protected void invalidated() { fireTileEvent(RESIZE_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "yAxis"; }
            };
            _yAxis = null;
        }
        return yAxis;
    }

    /**
     * Returns the mode of the RadarChartTileSkin.
     * There are Mode.POLYGON and Mode.SECTOR.
     * @return the mode of the RadarChartTileSkin
     */
    public RadarChartMode getRadarChartMode() { return null == radarChartMode ? _radarChartMode : radarChartMode.get(); }
    /**
     * Defines the mode that is used in the RadarChartTileSkin
     * to visualize the data in the RadarChart.
     * There are Mode.POLYGON and Mode.SECTOR.
     * @param MODE
     */
    public void setRadarChartMode(final RadarChartMode MODE) {
        if (null == radarChartMode) {
            _radarChartMode = MODE;
            fireTileEvent(RECALC_EVENT);
        } else {
            if (!radarChartMode.isBound()) {
                radarChartMode.set(MODE);
            }
        }
    }
    public ObjectProperty<RadarChartMode> radarChartModeProperty() {
        if (null == radarChartMode) {
            radarChartMode = new ObjectPropertyBase<RadarChartMode>(_radarChartMode) {
                @Override protected void invalidated() { fireTileEvent(RECALC_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "radarChartMode"; }
            };
            _radarChartMode = null;
        }
        return radarChartMode;
    }

    /**
     * Returns the color that will be used to colorize lines in
     * charts e.g. the grid in the RadarChartTileSkin
     * @return the color that will be used to colorize lines in charts
     */
    public Color getChartGridColor() { return null == chartGridColor ? _chartGridColor : chartGridColor.get(); }
    /**
     * Defines the color that will be used to colorize lines in
     * charts e.g. the grid in the RadarChartTileSkin
     * @param COLOR
     */
    public void setChartGridColor(final Color COLOR) {
        if (null == chartGridColor) {
            _chartGridColor = COLOR;
            fireTileEvent(REDRAW_EVENT);
        } else {
            if (!chartGridColor.isBound()) {
                chartGridColor.set(COLOR);
            }
        }
    }
    public ObjectProperty<Color> chartGridColorProperty() {
        if (null == chartGridColor) {
            chartGridColor = new ObjectPropertyBase<Color>(_chartGridColor) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "chartGridColor"; }
            };
            _chartGridColor = null;
        }
        return chartGridColor;
    }

    /**
     * Returns the Locale that will be used to visualize the country
     * in the CountryTileSkin
     * @return the Locale that will be used to visualize the country in the CountryTileSkin
     */
    public Country getCountry() {
        if (null == _country && null == country) { _country = Country.DE; }
        return null == country ? _country : country.get();
    }
    /**
     * Defines the Locale that will be used to visualize the country
     * in the CountryTileSkin
     * @param COUNTRY
     */
    public void setCountry(final Country COUNTRY) {
        if (null == country) {
            _country = COUNTRY;
            fireTileEvent(RECALC_EVENT);
        } else {
            if (!country.isBound()) {
                country.set(COUNTRY);
            }
        }
    }
    public ObjectProperty<Country> countryProperty() {
        if (null == country) {
            country = new ObjectPropertyBase<Country>(_country) {
                @Override protected void invalidated() { fireTileEvent(RECALC_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "country"; }
            };
            _country = null;
        }
        return country;
    }

    public CountryGroup getCountryGroup() {
        if (null == _countryGroup && null == countryGroup) { _countryGroup = Helper.EU; }
        return null == countryGroup ? _countryGroup : countryGroup.get();
    }
    public void setCountryGroup(final CountryGroup GROUP) {
        if (null == countryGroup) {
            _countryGroup = GROUP;
            fireTileEvent(RECALC_EVENT);
        } else {
            if (!countryGroup.isBound()) {
                countryGroup.set(GROUP);
            }
        }
    }
    public ObjectProperty<CountryGroup> countryGroupProperty() {
        if (null == countryGroup) {
            countryGroup = new ObjectPropertyBase<CountryGroup>(_countryGroup) {
                @Override protected void invalidated() { fireTileEvent(RECALC_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "countryGroup"; }
            };
            _countryGroup = null;
        }
        return countryGroup;
    }

    public boolean getDataPointsVisible() { return null == dataPointsVisible ? _dataPointsVisible : dataPointsVisible.get(); }
    public void setDataPointsVisible(final boolean VISIBLE) {
        if (null == dataPointsVisible) {
            _dataPointsVisible = VISIBLE;
            fireTileEvent(VISIBILITY_EVENT);
        } else {
            if (!dataPointsVisible.isBound()) {
                dataPointsVisible.set(VISIBLE);
            }
        }
    }
    public BooleanProperty dataPointsVisibleProperty() {
        if (null == dataPointsVisible) {
            dataPointsVisible = new BooleanPropertyBase(_dataPointsVisible) {
                @Override protected void invalidated() { fireTileEvent(VISIBILITY_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "dataPointsVisible"; }
            };
        }
        return dataPointsVisible;
    }

    public boolean isSnapToTicks() { return null == snapToTicks ? _snapToTicks : snapToTicks.get(); }
    public void setSnapToTicks(final boolean SNAP) {
        if (null == snapToTicks) {
            _snapToTicks = SNAP;
            fireTileEvent(REDRAW_EVENT);
        } else {
            if (!snapToTicks.isBound()) {
                snapToTicks.set(SNAP);
            }
        }
    }
    public BooleanProperty snapToTicksProperty() {
        if (null == snapToTicks) {
            snapToTicks = new BooleanPropertyBase(_snapToTicks) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "snapToTicks"; }
            };
        }
        return snapToTicks;
    }

    /**
     * The number of ticks between 2 major tick marks (used in SliderTileSkin)
     * @return the number of ticks between 2 major tick marks
     */
    public int getMinorTickCount() { return _minorTickCount; }
    public void setMinorTickCount(final int MINOR_TICK_COUNT) { _minorTickCount = Helper.clamp(0, 10, MINOR_TICK_COUNT); }

    /**
     * The distance between 2 major tick marks (used in SliderTileSkin)
     * @return the distance between 2 major tick marks
     */
    public double getMajorTickUnit() { return _majorTickUnit; }
    public void setMajorTickUnit(final double MAJOR_TICK_UNIT) { _majorTickUnit = Double.compare(MAJOR_TICK_UNIT, 0.0) <= 0 ? 0.25 : MAJOR_TICK_UNIT; }

    public int[] getMatrixSize() { return _matrixSize; }
    public void setMatrixSize(final int[] COLUMNS_AND_ROWS) {
        setMatrixSize(COLUMNS_AND_ROWS[0], COLUMNS_AND_ROWS[1]);
    }
    public void setMatrixSize(final int COLUMNS, final int ROWS) {
        _matrixSize = new int[] { Helper.clamp(2, 1000, COLUMNS), Helper.clamp(2, 1000, ROWS) };
        fireTileEvent(RECALC_EVENT);
    }

    public List<MatrixIcon> getMatrixIcons() { return matrixIcons; }
    public void setMatrixIcons(final MatrixIcon... MATRIX_ICONS) { setMatrixIcons(Arrays.asList(MATRIX_ICONS)); }
    public void setMatrixIcons(final List<MatrixIcon> MATRIX_ICONS) {
        matrixIcons.setAll(MATRIX_ICONS);
        fireTileEvent(REDRAW_EVENT);
    }
    public void addMatrixIcon(final MatrixIcon MATRIX_ICON) {
        if (matrixIcons.contains(MATRIX_ICON)) { return; }
        matrixIcons.add(MATRIX_ICON);
        fireTileEvent(REDRAW_EVENT);
    }
    public void removeMatrixIcon(final MatrixIcon MATRIX_ICON) {
        if (matrixIcons.contains(MATRIX_ICON)) {
            matrixIcons.remove(MATRIX_ICON);
        }
        fireTileEvent(REDRAW_EVENT);
    }

    public ChartType getChartType() { return _chartType; }
    public void setChartType(final ChartType TYPE) {
        _chartType = TYPE;
        fireTileEvent(SERIES_EVENT);
    }

    public double getTooltipTimeout() { return null == tooltipTimeout ? _tooltipTimeout : tooltipTimeout.get(); }
    public void setTooltipTimeout(final double TIMEOUT) {
        if (null == tooltipTimeout) {
            _tooltipTimeout = Helper.clamp(0, 10000, TIMEOUT);
            fireTileEvent(REDRAW_EVENT);
        } else {
            if (!tooltipTimeout.isBound()) {
                tooltipTimeout.set(TIMEOUT);
            }
        }
    }
    public DoubleProperty tooltipTimeoutProperty() {
        if (null == tooltipTimeout) {
            tooltipTimeout = new DoublePropertyBase(_tooltipTimeout) {
                @Override protected void invalidated() {
                    set(Helper.clamp(0, 10000, get()));
                    fireTileEvent(REDRAW_EVENT);
                }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() {
                    return "tootipTimeout";
                }
            };
        }
        return tooltipTimeout;
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
            if (!customFontEnabled.isBound()) {
                customFontEnabled.set(ENABLED);
            }
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
            if (!customFont.isBound()) {
                customFont.set(FONT);
            }
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

    public boolean getCustomDecimalFormatEnabled() { return null == customDecimalFormatEnabled ? _customDecimalFormatEnabled : customDecimalFormatEnabled.get(); }
    public void setCustomDecimalFormatEnabled(final boolean ENABLED) {
        if (null == customDecimalFormatEnabled) {
            _customDecimalFormatEnabled = ENABLED;
            fireTileEvent(REDRAW_EVENT);
        } else {
            if (!customDecimalFormatEnabled.isBound()) {
                customDecimalFormatEnabled.set(ENABLED);
            }
        }
    }
    public BooleanProperty customDecimalFormatEnabledProperty() {
        if (null == customDecimalFormatEnabled) {
            customDecimalFormatEnabled = new BooleanPropertyBase(_customDecimalFormatEnabled) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "customDecimalFormatEnabled"; }
            };
        }
        return customDecimalFormatEnabled;
    }

    public DecimalFormat getCustomDecimalFormat() { return null == customDecimalFormat ? _customDecimalFormat : customDecimalFormat.get(); }
    public void setCustomDecimalFormat(final DecimalFormat DECIMAL_FORMAT) {
        if (null == customDecimalFormat) {
            _customDecimalFormat = DECIMAL_FORMAT;
            fireTileEvent(REDRAW_EVENT);
        } else {
            if (!customDecimalFormat.isBound()) {
                customDecimalFormat.set(DECIMAL_FORMAT);
            }
        }
    }
    public ObjectProperty<DecimalFormat> customDecimalFormatProperty() {
        if (null == customDecimalFormat) {
            customDecimalFormat = new ObjectPropertyBase(_customDecimalFormat) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "customDecimalFormat"; }
            };
            _customDecimalFormat = null;
        }
        return customDecimalFormat;
    }

    /**
     * Returns a list of path elements that define the countries
     * @return a list of path elements that define the countries
     */
    public Map<String, List<CountryPath>> getCountryPaths() {
        return Helper.getLoresCountryPaths();
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
            if (!strokeWithGradient.isBound()) {
                strokeWithGradient.set(STROKE_WITH_GRADIENT);
            }
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

    /**
     * Returns true if a gradient defined by gradientStops will be
     * used to fill the area in the SmoothAreaTileSkin.
     * @return true if a gradient defined by gradientStops will be used to fill the area in the SmoothAreaTileSkin
     */
    public boolean isFillWithGradient() { return null == fillWithGradient ? _fillWithGradient : fillWithGradient.get(); }
    /**
     * Defines the usage of a gradient defined by gradientStops to fill the area
     * in the SmoothAreaTileSkin
     * @param FILL_WITH_GRADIENT
     */
    public void setFillWithGradient(final boolean FILL_WITH_GRADIENT) {
        if (null == fillWithGradient) {
            _fillWithGradient = FILL_WITH_GRADIENT;
            fireTileEvent(REDRAW_EVENT);
        } else {
            if (!fillWithGradient.isBound()) {
                fillWithGradient.set(FILL_WITH_GRADIENT);
            }
        }
    }
    public BooleanProperty fillWithGradientProperty() {
        if (null == fillWithGradient) {
            fillWithGradient = new BooleanPropertyBase(_fillWithGradient) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "fillWithGradient"; }
            };
        }
        return fillWithGradient;
    }

    /**
     * Returns the notify region.
     * Only if the getSkin() != null
     * @return the notify region
     */
    public NotifyRegion getNotifyRegion() {
        if (null == getSkin()) { return null; }
        return ((TileSkin) getSkin()).getNotifyRegion();
    }

    public Color getNotifyRegionBackgroundColor() { return _notifyRegionBackgroundColor; }
    public void setNotifyRegionBackgroundColor(final Color COLOR) {
        _notifyRegionBackgroundColor = COLOR;
        fireTileEvent(REDRAW_EVENT);
    }

    public Color getNotifyRegionForegroundColor() { return _notifyRegionForegroundColor; }
    public void setNotifyRegionForegroundColor(final Color COLOR) {
        _notifyRegionForegroundColor = COLOR;
        fireTileEvent(REDRAW_EVENT);
    }
    
    public String getNotifyRegionTooltipText() { return _notifyRegionTooltipText; }
    public void setNotifyRegionTooltipText(final String TEXT) {
        _notifyRegionTooltipText = TEXT;
        fireTileEvent(REDRAW_EVENT);
    }

    /**
     * Returns the info region.
     * Only if the getSkin() != null
     * @return the info region
     */
    public InfoRegion getInfoRegion() {
        if (null == getSkin()) { return null; }
        return ((TileSkin) getSkin()).getInfoRegion();
    }

    public Color getInfoRegionBackgroundColor() { return _infoRegionBackgroundColor; }
    public void setInfoRegionBackgroundColor(final Color COLOR) {
        _infoRegionBackgroundColor = COLOR;
        fireTileEvent(REDRAW_EVENT);
    }

    public Color getInfoRegionForegroundColor() { return _infoRegionForegroundColor; }
    public void setInfoRegionForegroundColor(final Color COLOR) {
        _infoRegionForegroundColor = COLOR;
        fireTileEvent(REDRAW_EVENT);
    }

    public String getInfoRegionTooltipText() { return _infoRegionTooltipText; }
    public void setInfoRegionTooltipText(final String TEXT) {
        _infoRegionTooltipText = TEXT;
        fireTileEvent(REDRAW_EVENT);
    }

    /**
     * Returns the lower right region.
     * Only if the getSkin() != null
     * @return the lower right region
     */
    public LowerRightRegion getLowerRightRegion() {
        if (null == getSkin()) { return null; }
        return ((TileSkin) getSkin()).getLowerRightRegion();
    }

    public Color getLowerRightRegionBackgroundColor() { return _lowerRightRegionBackgroundColor; }
    public void setLowerRightRegionBackgroundColor(final Color COLOR) {
        _lowerRightRegionBackgroundColor = COLOR;
        fireTileEvent(REDRAW_EVENT);
    }

    public Color getLowerRightRegionForegroundColor() { return _lowerRightRegionForegroundColor; }
    public void setLowerRightRegionForegroundColor(final Color COLOR) {
        _lowerRightRegionForegroundColor = COLOR;
        fireTileEvent(REDRAW_EVENT);
    }

    public String getLowerRightRegionTooltipText() { return _lowerRightRegionTooltipText; }
    public void setLowerRightRegionTooltipText(final String TEXT) {
        _lowerRightRegionTooltipText = TEXT;
        fireTileEvent(REDRAW_EVENT);
    }
    
    public Image getBackgroundImage() { return _backgroundImage; }
    public void setBackgroundImage(final Image IMAGE) {
        _backgroundImage = IMAGE;
        fireTileEvent(BKG_IMAGE_EVENT);
    }

    public double getBackgroundImageOpacity() { return _backgroundImageOpacity; }
    public void setBackgroundImageOpacity(final double OPACITY) {
        _backgroundImageOpacity = Helper.clamp(0, 1, OPACITY);
        if (null == _backgroundImage) return;
        fireTileEvent(REDRAW_EVENT);
    }

    public boolean getBackgroundImageKeepAspect() { return _backgroundImageKeepAspect; }
    public void setBackgroundImageKeepAspect(final boolean KEEP_ASPECT) {
        _backgroundImageKeepAspect = KEEP_ASPECT;
        fireTileEvent(REDRAW_EVENT);
    }

    public String getLeftText() { return null == leftText ? _leftText : leftText.get(); }
    public void setLeftText(final String TEXT) {
        if (null == leftText) {
            _leftText = TEXT;
            fireTileEvent(REDRAW_EVENT);
        } else {
            if (!leftText.isBound()) {
                leftText.set(TEXT);
            }
        }
    }
    public StringProperty leftTextProperty() {
        if (null == leftText) {
            leftText = new StringPropertyBase(_leftText) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "leftText"; }
            };
            _leftText = null;
        }
        return leftText;
    }

    public String getMiddleText() { return null == middleText ? _middleText : middleText.get(); }
    public void setMiddleText(final String TEXT) {
        if (null == middleText) {
            _middleText = TEXT;
            fireTileEvent(REDRAW_EVENT);
        } else {
            if (!middleText.isBound()) {
                middleText.set(TEXT);
            }
        }
    }
    public StringProperty middleTextProperty() {
        if (null == middleText) {
            middleText = new StringPropertyBase(_middleText) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "middleText"; }
            };
            _middleText = null;
        }
        return middleText;
    }

    public String getRightText() { return null == rightText ? _rightText : rightText.get(); }
    public void setRightText(final String TEXT) {
        if (null == rightText) {
            _rightText = TEXT;
            fireTileEvent(REDRAW_EVENT);
        } else {
            if (!rightText.isBound()) {
                rightText.set(TEXT);
            }
        }
    }
    public StringProperty rightTextProperty() {
        if (null == rightText) {
            rightText = new StringPropertyBase(_rightText) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "rightText"; }
            };
            _rightText = null;
        }
        return rightText;
    }
    
    public double getLeftValue() { return null == leftValue ? _leftValue : leftValue.get(); }
    public void setLeftValue(final double VALUE) {
        if (null == leftValue) {
            _leftValue = VALUE;
            fireTileEvent(REDRAW_EVENT);
        } else {
            if (!leftValue.isBound()) {
                leftValue.set(VALUE);
            }
        }
    }
    public DoubleProperty leftValueProperty() {
        if (null == leftValue) {
            leftValue = new DoublePropertyBase(_leftValue) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "leftValue"; }
            };
        }
        return leftValue;
    }

    public double getMiddleValue() { return null == middleValue ? _middleValue : middleValue.get(); }
    public void setMiddleValue(final double VALUE) {
        if (null == middleValue) {
            _middleValue = VALUE;
            fireTileEvent(REDRAW_EVENT);
        } else {
            if (!middleValue.isBound()) {
                middleValue.set(VALUE);
            }
        }
    }
    public DoubleProperty middleValueProperty() {
        if (null == middleValue) {
            middleValue = new DoublePropertyBase(_middleValue) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "middleValue"; }
            };
        }
        return middleValue;
    }

    public double getRightValue() { return null == rightValue ? _rightValue : rightValue.get(); }
    public void setRightValue(final double VALUE) {
        if (null == rightValue) {
            _rightValue = VALUE;
            fireTileEvent(REDRAW_EVENT);
        } else {
            if (!rightValue.isBound()) {
                rightValue.set(VALUE);
            }
        }
    }
    public DoubleProperty rightValueProperty() {
        if (null == rightValue) {
            rightValue = new DoublePropertyBase(_rightValue) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "rightValue"; }
            };
        }
        return rightValue;
    }
    
    public Node getLeftGraphics() { return null == leftGraphics ? _leftGraphics : leftGraphics.get(); }
    public void setLeftGraphics(final Node NODE) {
        if (null == leftGraphics) {
            _leftGraphics = NODE;
            fireTileEvent(RECALC_EVENT);
        } else {
            if (!leftGraphics.isBound()) {
                leftGraphics.set(NODE);
            }
        }
    }
    public ObjectProperty<Node> leftGraphicsProperty() {
        if (null == leftGraphics) {
            leftGraphics = new ObjectPropertyBase<Node>(_leftGraphics) {
                @Override protected void invalidated() { fireTileEvent(RECALC_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "leftGraphics"; }
            };
            _leftGraphics = null;
        }
        return leftGraphics;
    }

    public Node getMiddleGraphics() { return null == middleGraphics ? _middleGraphics : middleGraphics.get(); }
    public void setMiddleGraphics(final Node NODE) {
        if (null == middleGraphics) {
            _middleGraphics = NODE;
            fireTileEvent(RECALC_EVENT);
        } else {
            if (!middleGraphics.isBound()) {
                middleGraphics.set(NODE);
            }
        }
    }
    public ObjectProperty<Node> middleGraphicsProperty() {
        if (null == middleGraphics) {
            middleGraphics = new ObjectPropertyBase<Node>(_middleGraphics) {
                @Override protected void invalidated() { fireTileEvent(RECALC_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "middleGraphics"; }
            };
            _middleGraphics = null;
        }
        return middleGraphics;
    }

    public Node getRightGraphics() { return null == rightGraphics ? _rightGraphics : rightGraphics.get(); }
    public void setRightGraphics(final Node NODE) {
        if (null == rightGraphics) {
            _rightGraphics = NODE;
            fireTileEvent(RECALC_EVENT);
        } else {
            if (!rightGraphics.isBound()) {
                rightGraphics.set(NODE);
            }
        }
    }
    public ObjectProperty<Node> rightGraphicsProperty() {
        if (null == rightGraphics) {
            rightGraphics = new ObjectPropertyBase<Node>(_rightGraphics) {
                @Override protected void invalidated() { fireTileEvent(RECALC_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "rightGraphics"; }
            };
            _rightGraphics = null;
        }
        return rightGraphics;
    }

    /**
     * Returns true if the trend indicator in the TimelineTileSkin is visible
     * @return true if the trend indicator in the TimelineTileSkin is visible
     */
    public boolean isTrendVisible() { return null == trendVisible ? _trendVisible : trendVisible.get(); }
    /**
     * Defines the visibility of the trend indicator in the TimelineTileSkin
     * @param VISIBLE
     */
    public void setTrendVisible(final boolean VISIBLE) {
        if (null == trendVisible) {
            _trendVisible = VISIBLE;
            fireTileEvent(VISIBILITY_EVENT);
        } else {
            if (!trendVisible.isBound()) {
                trendVisible.set(VISIBLE);
            }
        }
    }
    public BooleanProperty trendVisibleProperty() {
        if (null == trendVisible) {
            trendVisible = new BooleanPropertyBase(_trendVisible) {
                @Override protected void invalidated() { fireTileEvent(VISIBILITY_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "trendVisible"; }
            };
        }
        return trendVisible;
    }

    /**
     * Returns a timeout period in ms which is used e.g. in the TimelineTileSkin
     * @return a timeout period in ms which is used e.g. in the TimelineTileSkin
     */
    public long getTimeoutMs() { return null == timeoutMs ? _timeoutMs : timeoutMs.get(); }
    public void setTimeoutMs(final long TIMEOUT_MS) {
        if (null == timeoutMs) {
            _timeoutMs = TIMEOUT_MS;
            fireTileEvent(REDRAW_EVENT);
        } else {
            if (!timeoutMs.isBound()) {
                timeoutMs.set(TIMEOUT_MS);
            }
        }
    }
    public LongProperty timeoutMsProperty() {
        if (null == timeoutMs) {
            timeoutMs = new LongPropertyBase(_timeoutMs) {
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "timeoutMs"; }
            };
        }
        return timeoutMs;
    }

    public Rank getRank() { return null == rank ? _rank : rank.get(); }
    public void setRank(final Rank RANK) {
        if (null == rank) {
            _rank = RANK;
            fireTileEvent(REDRAW_EVENT);
        } else {
            if (!rank.isBound()) {
                rank.set(RANK);
            }
        }
    }
    public ObjectProperty<Rank> rankProperty() {
        if (null == rank) {
            rank = new ObjectPropertyBase<Rank>(_rank) {
                @Override protected void invalidated() { fireTileEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "ranking"; }
            };
            _rank = null;
        }
        return rank;
    }

    public boolean isInteractive() { return null == interactive ? _interactive : interactive.get(); }
    public void setInteractive(final boolean INTERACTIVE) {
        if (null == interactive) {
            _interactive = INTERACTIVE;
        } else {
            if (!interactive.isBound()) {
                interactive.set(INTERACTIVE);
            }
        }
    }
    public BooleanProperty interactiveProperty() {
        if (null == interactive) {
            interactive = new BooleanPropertyBase() {
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "interactive"; }
            };
        }
        return interactive;
    }

    public int getNumberOfValuesForTrendCalculation() { return null == numberOfValuesForTrendCalculation ? _numberOfValuesForTrendCalculation : numberOfValuesForTrendCalculation.get(); }
    public void setNumberOfValuesForTrendCalculation(final int NUMBER) {
        if (null == numberOfValuesForTrendCalculation) {
            _numberOfValuesForTrendCalculation = NUMBER;
        } else {
            if (!numberOfValuesForTrendCalculation.isBound()) {
                numberOfValuesForTrendCalculation.set(NUMBER);
            }
        }
    }
    public IntegerProperty numberOfValuesForTrendCalculationProperty() {
        if (null == numberOfValuesForTrendCalculation) {
            numberOfValuesForTrendCalculation = new IntegerPropertyBase(_numberOfValuesForTrendCalculation) {
                @Override public Object getBean() { return Tile.this; }
                @Override public String getName() { return "numberOfValuesForTrendCalculation"; }
            };
        }
        return numberOfValuesForTrendCalculation;
    }

    public void showNotifyRegion(final boolean SHOW) { fireTileEvent(SHOW ? SHOW_NOTIFY_REGION_EVENT : HIDE_NOTIFY_REGION_EVENT); }

    public void showInfoRegion(final boolean SHOW) { fireTileEvent(SHOW ? SHOW_INFO_REGION_EVENT : HIDE_INFO_REGION_EVENT); }

    public void showLowerRightRegion(final boolean SHOW) { fireTileEvent(SHOW ? SHOW_LOWER_RIGHT_REGION_EVENT : HIDE_LOWER_RIGHT_REGION_EVENT); }

    public EventHandler<MouseEvent> getInfoRegionHandler() { return infoRegionHandler; }
    public void setInfoRegionEventHandler(final EventHandler<MouseEvent> HANDLER) {
        infoRegionHandler = HANDLER;
        fireTileEvent(INFO_REGION_HANDLER_EVENT);
    }

    public boolean isShowing() { return null == showing ? false : showing.get(); }
    public BooleanBinding showingProperty() { return showing; }

    public void clearData() {
        fireTileEvent(CLEAR_DATA_EVENT);
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
        if (null == alarmsToRemove) { alarmsToRemove = new ArrayList<>(); }
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
        for (Alarm alarm : alarmsToRemove) { removeAlarm(alarm); }
    }

    private void tick() { Platform.runLater(() -> {
        ZonedDateTime oldTime = getTime();
        setTime(getTime().plus(java.time.Duration.ofMillis(updateInterval)));
        ZonedDateTime now = time.get();
        if (isAlarmsEnabled()) checkAlarms(now);
        if (getCheckSectionsForValue() && timeSections != null) {
            for (TimeSection timeSection : timeSections) { timeSection.checkForTimeAndDate(now); }
        }

        if (timeEventListeners.isEmpty()) return;
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
    public void addTileEventListener(final TileEventListener LISTENER) { if (!tileEventListeners.contains(LISTENER)) tileEventListeners.add(LISTENER); }
    public void removeTileEventListener(final TileEventListener LISTENER) { if (tileEventListeners.contains(LISTENER)) tileEventListeners.remove(LISTENER); }
    public void removeAllTileEventListeners() { tileEventListeners.clear(); }

    public void fireTileEvent(final TileEvent EVENT) {
        if (null != showing && showing.get()) {
            for (TileEventListener listener : tileEventListeners) { listener.onTileEvent(EVENT); }
        } else {
            tileEventQueue.add(EVENT);
        }
    }

    
    public void setOnAlarm(final AlarmEventListener LISTENER) { addAlarmEventListener(LISTENER); }
    public void addAlarmEventListener(final AlarmEventListener LISTENER) { if (!alarmEventListeners.contains(LISTENER)) alarmEventListeners.add(LISTENER); }
    public void removeAlarmEventListener(final AlarmEventListener LISTENER) { if (alarmEventListeners.contains(LISTENER)) alarmEventListeners.remove(LISTENER); }
    public void removeAllAlarmEventListeners() { alarmEventListeners.clear(); }

    public void fireAlarmEvent(final AlarmEvent EVENT) {
        for (AlarmEventListener listener : alarmEventListeners) { listener.onAlarmEvent(EVENT); }
    }


    public void setOnTimeEvent(final TimeEventListener LISTENER) { addTimeEventListener(LISTENER); }
    public void addTimeEventListener(final TimeEventListener LISTENER) { if (!timeEventListeners.contains(LISTENER)) timeEventListeners.add(LISTENER); }
    public void removeTimeEventListener(final TimeEventListener LISTENER) { if (timeEventListeners.contains(LISTENER)) timeEventListeners.remove(LISTENER); }
    public void removeAllTimeEventListeners() { timeEventListeners.clear(); }

    public void fireTimeEvent(final TimeEvent EVENT) {
        for (TimeEventListener listener : timeEventListeners) { listener.onTimeEvent(EVENT); }
    }


    public void setOnSwitchPressed(final EventHandler<SwitchEvent> HANDLER) { addEventHandler(SwitchEvent.SWITCH_PRESSED, HANDLER); }
    public void removeOnSwitchPressed(final EventHandler<SwitchEvent> HANDLER) { removeEventHandler(SwitchEvent.SWITCH_PRESSED, HANDLER); }

    public void setOnSwitchReleased(final EventHandler<SwitchEvent> HANDLER) { addEventHandler(SwitchEvent.SWITCH_RELEASED, HANDLER); }
    public void removeOnSwitchReleased(final EventHandler<SwitchEvent> HANDLER) { removeEventHandler(SwitchEvent.SWITCH_RELEASED, HANDLER); }

    public void setOnContentSizeChanged(final BoundsEventListener LISTENER) {
        if (null == getSkin()) {
            if (!boundsListeners.contains(LISTENER)) { boundsListeners.add(LISTENER); }
        } else {
            ((TileSkin) (getSkin())).setOnContentBoundsChanged(LISTENER);
        }
    }
    public void removeOnContentSizeChanged(final BoundsEventListener LISTENER) {
        if (boundsListeners.contains(LISTENER)) { boundsListeners.remove(LISTENER); }
    }

    private void setupBinding() {
        showing = Bindings.createBooleanBinding(() -> {
            if (getScene() != null && getScene().getWindow() != null) {
                return getScene().getWindow().isShowing();
            } else {
                return false;
            }            
        }, sceneProperty(), getScene().windowProperty(), getScene().getWindow().showingProperty());
        
        showing.addListener(o -> {
        if (showing.get()) {
            while(tileEventQueue.peek() != null) {
                TileEvent event = tileEventQueue.poll();
                    for (TileEventListener listener : tileEventListeners) { listener.onTileEvent(event); }
                }
            }

            boundsListeners.forEach(listener -> ((TileSkin) (getSkin())).setOnContentBoundsChanged(listener));
            ((TileSkin) (getSkin())).getContentBounds().fireBoundsEvent();

            fireTileEvent(REGIONS_ON_TOP_EVENT);
            fireTileEvent(RESIZE_EVENT);
        });
    }


    // ******************** Style related *************************************
    @Override protected Skin createDefaultSkin() {
        switch (skinType) {
            case SMOOTHED_CHART    : return new SmoothedChartTileSkin(Tile.this);
            case BAR_CHART           : return new BarChartTileSkin(Tile.this);
            case CLOCK               : return new ClockTileSkin(Tile.this);
            case GAUGE               : return new GaugeTileSkin(Tile.this);
            case GAUGE2              : return new Gauge2TileSkin(Tile.this);
            case HIGH_LOW            : return new HighLowTileSkin(Tile.this);
            case PERCENTAGE          : return new PercentageTileSkin(Tile.this);
            case PLUS_MINUS          : return new PlusMinusTileSkin(Tile.this);
            case SLIDER              : return new SliderTileSkin(Tile.this);
            case SPARK_LINE          : return new SparkLineTileSkin(Tile.this);
            case SWITCH              : return new SwitchTileSkin(Tile.this);
            case WORLDMAP            : return new WorldMapTileSkin(Tile.this);
            case TIMER_CONTROL       : return new TimerControlTileSkin(Tile.this);
            case NUMBER              : return new NumberTileSkin(Tile.this);
            case TEXT                : return new TextTileSkin(Tile.this);
            case TIME                : return new TimeTileSkin(Tile.this);
            case CUSTOM              : return new CustomTileSkin(Tile.this);
            case CUSTOM_SCROLLABLE   : return new CustomScrollableTileSkin(Tile.this);
            case LEADER_BOARD        : return new LeaderBoardTileSkin(Tile.this);
            case MAP                 : return new MapTileSkin(Tile.this);
            case RADIAL_CHART        : return new RadialChartTileSkin(Tile.this);
            case DONUT_CHART         : return new DonutChartTileSkin(Tile.this);
            case CIRCULAR_PROGRESS   : return new CircularProgressTileSkin(Tile.this);
            case CIRCLE_PROGRESS_NUM : return new CircleProgressTileNumberSkin(Tile.this);
            case STOCK               : return new StockTileSkin(Tile.this);
            case GAUGE_SPARK_LINE    : return new GaugeSparkLineTileSkin(Tile.this);
            case SMOOTH_AREA_CHART   : return new SmoothAreaChartTileSkin(Tile.this);
            case RADAR_CHART         : return new RadarChartTileSkin(Tile.this);
            case RADAR_NODE_CHART    : return new RadarNodeChartTileSkin(Tile.this);
            case COUNTRY             : return new CountryTileSkin(Tile.this);
            case CHARACTER           : return new CharacterTileSkin(Tile.this);
            case FLIP                : return new FlipTileSkin(Tile.this);
            case SWITCH_SLIDER       : return new SwitchSliderTileSkin(Tile.this);
            case DATE                : return new DateTileSkin(Tile.this);
            case CALENDAR            : return new CalendarTileSkin(Tile.this);
            case SUNBURST            : return new SunburstChartTileSkin(Tile.this);
            case MATRIX              : return new MatrixTileSkin(Tile.this);
            case MATRIX_ICON         : return new MatrixIconTileSkin(Tile.this);
            case RADIAL_PERCENTAGE   : return new RadialPercentageTileSkin(Tile.this);
            case STATUS              : return new StatusTileSkin(Tile.this);
            case BAR_GAUGE           : return new BarGaugeTileSkin(Tile.this);
            case IMAGE               : return new ImageTileSkin(Tile.this);
            case IMAGE_COUNTER       : return new ImageCounterTileSkin(Tile.this);
            case TIMELINE            : return new TimelineTileSkin(Tile.this);
            case CLUSTER_MONITOR     : return new ClusterMonitorTileSkin(Tile.this);
            case LED                 : return new LedTileSkin(Tile.this);
            case COUNTDOWN_TIMER     : return new CountdownTimerTileSkin(Tile.this);
            case CYCLE_STEP          : return new CycleStepTileSkin(Tile.this);
            case COLOR               : return new ColorTileSkin(Tile.this);
            case FLUID               : return new FluidTileSkin(Tile.this);
            case FIRE_SMOKE          : return new FireSmokeTileSkin(Tile.this);
            case TURNOVER            : return new TurnoverTileSkin(Tile.this);
            case RADIAL_DISTRIBUTION : return new RadialDistributionTileSkin(Tile.this);
            default                  : return new TileSkin(Tile.this);
        }
    }

    @Override public String getUserAgentStylesheet() {
        if (null == userAgentStyleSheet) { userAgentStyleSheet = getClass().getResource("tilesfx.css").toExternalForm(); }
        return userAgentStyleSheet;
    }

    public void presetTileParameters(final SkinType SKIN_TYPE) {
        reInit();
        switch (SKIN_TYPE) {
            case SMOOTHED_CHART:
                break;
            case BAR_CHART:
                setItemSorting(ItemSorting.DESCENDING);
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
            case GAUGE2:
                setStartAngle(330);
                setAngleRange(240);
                setAnimated(true);
                setTickLabelDecimals(0);
                setBarColor(Tile.BLUE);
                setBarBackgroundColor(BACKGROUND.brighter());
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
                setHourTickMarksVisible(true);
                setMinuteTickMarksVisible(true);
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
                setItemSorting(ItemSorting.DESCENDING);
                break;
            case MAP:
                break;
            case RADIAL_CHART:
                setAnimated(true);
                break;
            case DONUT_CHART:
                setItemSorting(ItemSorting.DESCENDING);
                setAnimated(true);
                break;
            case CIRCULAR_PROGRESS:
                setBarBackgroundColor(getBackgroundColor().brighter());
                setAnimated(true);
                break;
            case CIRCLE_PROGRESS_NUM:
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
            case GAUGE_SPARK_LINE:
                setBarColor(Tile.BLUE);
                setBarBackgroundColor(Tile.BACKGROUND.brighter());
                setAngleRange(270);
                break;
            case SMOOTH_AREA_CHART:
                setSmoothing(true);
                setChartType(ChartType.AREA);
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
                setBarBackgroundColor(Tile.FOREGROUND);
                break;
            case DATE:
                setTitleAlignment(TextAlignment.CENTER);
                setTextAlignment(TextAlignment.CENTER);
                break;
            case CALENDAR:
                setTitleAlignment(TextAlignment.CENTER);
                setTextAlignment(TextAlignment.CENTER);
                break;
            case SUNBURST:
                break;
            case MATRIX:
                break;
            case MATRIX_ICON:
                break;
            case RADIAL_PERCENTAGE:
                setBarBackgroundColor(getBackgroundColor().brighter());
                setAnimated(true);
                break;
            case STATUS:
                setDescriptionAlignment(Pos.TOP_CENTER);
                break;
            case BAR_GAUGE:
                setBarBackgroundColor(Tile.BACKGROUND.brighter());
                setBarColor(Tile.BLUE);
                setAngleRange(180);
                setTickLabelDecimals(0);
                break;
            case IMAGE:
                setTextAlignment(TextAlignment.CENTER);
                break;
            case IMAGE_COUNTER:
                setTextAlignment(TextAlignment.LEFT);
                setDecimals(0);
                break;
            case TIMELINE:
                setDataPointsVisible(true);
                setTextVisible(false);
                setAnimated(false);
                setAveragingEnabled(true);
                setAveragingPeriod(Helper.calcNumberOfDatapointsForPeriod(getTimePeriod(), getTimePeriodResolution()));
                setDecimals(0);
                setTickLabelDecimals(0);
                break;
            case CLUSTER_MONITOR:
                setTitle("");
                setTextVisible(false);
                setUnit(Helper.PERCENTAGE);
                setAnimated(false);
                setDecimals(0);
                setBarColor(BLUE);
                break;
            case LED:
                setActiveColor(Bright.GREEN);
                break;
            case COUNTDOWN_TIMER:
                setBarBackgroundColor(getBackgroundColor().brighter());
                setAnimated(false);
                setTimePeriod(java.time.Duration.ofSeconds(60));
                break;
            case CYCLE_STEP:
                break;
            case COLOR:
                setForegroundColor(Color.WHITE);
                setUnit("\u0025");
                setDecimals(0);
                setBarBackgroundColor(Tile.BACKGROUND);
                break;
            case FLUID:
                break;
            case FIRE_SMOKE:
                break;
            case TURNOVER:
                setTextAlignment(TextAlignment.CENTER);
                setImageMask(ImageMask.ROUND);
                break;
            case RADIAL_DISTRIBUTION:
                setStartAngle(330);
                setAngleRange(240);
                setAnimated(false);
                setTickLabelDecimals(0);
                setBarBackgroundColor(BACKGROUND.brighter());
                break;
            default:
                break;
        }
    }

    public SkinType getSkinType() { return skinType; }
    public void setSkinType(final SkinType SKIN_TYPE) {
        skinType = SKIN_TYPE;
        switch (SKIN_TYPE) {
            case SMOOTHED_CHART     : setSkin(new SmoothedChartTileSkin(Tile.this)); break;
            case BAR_CHART          : setSkin(new BarChartTileSkin(Tile.this)); break;
            case CLOCK              : setSkin(new ClockTileSkin(Tile.this)); break;
            case GAUGE              : setSkin(new GaugeTileSkin(Tile.this)); break;
            case GAUGE2             : setSkin(new Gauge2TileSkin(Tile.this)); break;
            case HIGH_LOW           : setSkin(new HighLowTileSkin(Tile.this)); break;
            case PERCENTAGE         : setSkin(new PercentageTileSkin(Tile.this)); break;
            case PLUS_MINUS         : setSkin(new PlusMinusTileSkin(Tile.this)); break;
            case SLIDER             : setSkin(new SliderTileSkin(Tile.this)); break;
            case SPARK_LINE         : setSkin(new SparkLineTileSkin(Tile.this)); break;
            case SWITCH             : setSkin(new SwitchTileSkin(Tile.this)); break;
            case WORLDMAP           : setSkin(new WorldMapTileSkin(Tile.this)); break;
            case TIMER_CONTROL      : setSkin(new TimerControlTileSkin(Tile.this)); break;
            case NUMBER             : setSkin(new NumberTileSkin(Tile.this)); break;
            case TEXT               : setSkin(new TextTileSkin(Tile.this)); break;
            case TIME               : setSkin(new TimeTileSkin(Tile.this)); break;
            case CUSTOM             : setSkin(new CustomTileSkin(Tile.this)); break;
            case CUSTOM_SCROLLABLE  : setSkin(new CustomScrollableTileSkin(Tile.this)); break;
            case LEADER_BOARD       : setSkin(new LeaderBoardTileSkin(Tile.this)); break;
            case RADIAL_CHART       : setSkin(new RadialChartTileSkin(Tile.this)); break;
            case DONUT_CHART        : setSkin(new DonutChartTileSkin(Tile.this)); break;
            case CIRCULAR_PROGRESS  : setSkin(new CircularProgressTileSkin(Tile.this)); break;
            case CIRCLE_PROGRESS_NUM: setSkin(new CircleProgressTileNumberSkin(Tile.this)); break;
            case STOCK              : setSkin(new StockTileSkin(Tile.this)); break;
            case GAUGE_SPARK_LINE   : setSkin(new GaugeSparkLineTileSkin(Tile.this)); break;
            case SMOOTH_AREA_CHART  : setSkin(new SmoothAreaChartTileSkin(Tile.this)); break;
            case RADAR_CHART        : setSkin(new RadarChartTileSkin(Tile.this)); break;
            case RADAR_NODE_CHART   : setSkin(new RadarNodeChartTileSkin(Tile.this)); break;
            case COUNTRY            : setSkin(new CountryTileSkin(Tile.this)); break;
            case CHARACTER          : setSkin(new CharacterTileSkin(Tile.this)); break;
            case FLIP               : setSkin(new FlipTileSkin(Tile.this)); break;
            case SWITCH_SLIDER      : setSkin(new SwitchSliderTileSkin(Tile.this)); break;
            case DATE               : setSkin(new DateTileSkin(Tile.this)); break;
            case CALENDAR           : setSkin(new CalendarTileSkin(Tile.this)); break;
            case SUNBURST           : setSkin(new SunburstChartTileSkin(Tile.this)); break;
            case MATRIX             : setSkin(new MatrixTileSkin(Tile.this)); break;
            case MATRIX_ICON        : setSkin(new MatrixIconTileSkin(Tile.this)); break;
            case RADIAL_PERCENTAGE  : setSkin(new RadialPercentageTileSkin(Tile.this)); break;
            case STATUS             : setSkin(new StatusTileSkin(Tile.this)); break;
            case BAR_GAUGE          : setSkin(new BarGaugeTileSkin(Tile.this)); break;
            case IMAGE              : setSkin(new ImageTileSkin(Tile.this)); break;
            case IMAGE_COUNTER      : setSkin(new ImageCounterTileSkin(Tile.this)); break;
            case TIMELINE           : setSkin(new TimelineTileSkin(Tile.this)); break;
            case CLUSTER_MONITOR    : setSkin(new ClusterMonitorTileSkin(Tile.this)); break;
            case LED                : setSkin(new LedTileSkin(Tile.this)); break;
            case COUNTDOWN_TIMER    : setSkin(new CountdownTimerTileSkin(Tile.this)); break;
            case CYCLE_STEP         : setSkin(new CycleStepTileSkin(Tile.this)); break;
            case COLOR              : setSkin(new ColorTileSkin(Tile.this)); break;
            case FLUID              : setSkin(new FluidTileSkin(Tile.this)); break;
            case FIRE_SMOKE         : setSkin(new FireSmokeTileSkin(Tile.this)); break;
            case TURNOVER           : setSkin(new TurnoverTileSkin(Tile.this)); break;
            case RADIAL_DISTRIBUTION: setSkin(new RadialDistributionTileSkin(Tile.this)); break;
            default                 : setSkin(new TileSkin(Tile.this)); break;
        }
        fireTileEvent(RESIZE_EVENT);
        presetTileParameters(SKIN_TYPE);
    }
}
