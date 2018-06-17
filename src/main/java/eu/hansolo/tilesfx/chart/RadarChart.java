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

package eu.hansolo.tilesfx.chart;

import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.Helper;
import eu.hansolo.tilesfx.tools.Point;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.IntegerPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.StringPropertyBase;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.CacheHint;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Paint;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static eu.hansolo.tilesfx.tools.Helper.adjustTextSize;
import static eu.hansolo.tilesfx.tools.Helper.clamp;
import static eu.hansolo.tilesfx.tools.Helper.getContrastColor;


/**
 * Created by hansolo on 10.06.17.
 */
public class RadarChart extends Region {
    public  enum Mode { SECTOR, POLYGON };
    private static final int                       MIN_NO_OF_SECTORS = 4;
    private static final int                       MAX_NO_OF_SECTORS = 128;
    private static final double                    PREFERRED_WIDTH   = 120;
    private static final double                    PREFERRED_HEIGHT  = 120;
    private static final double                    MINIMUM_WIDTH     = 10;
    private static final double                    MINIMUM_HEIGHT    = 10;
    private static final double                    MAXIMUM_WIDTH     = 1024;
    private static final double                    MAXIMUM_HEIGHT    = 1024;
    private              double                    size;
    private              Pane                      pane;
    private              Canvas                    chartCanvas;
    private              GraphicsContext           chartCtx;
    private              Canvas                    overlayCanvas;
    private              GraphicsContext           overlayCtx;
    private              Text                      unitText;
    private              Text                      minValueText;
    private              Text                      legend1Text;
    private              Text                      legend2Text;
    private              Text                      legend3Text;
    private              Text                      legend4Text;
    private              Text                      maxValueText;
    private              DropShadow                dropShadow;
    private              double                    legendStep;
    private              int                       decimals;
    private              String                    formatString;
    private              int                       _noOfSectors;
    private              IntegerProperty           noOfSectors;
    private              double                    angleStep;
    private              ObservableList<ChartData> data;
    private              double                    _minValue;
    private              DoubleProperty            minValue;
    private              double                    _maxValue;
    private              DoubleProperty            maxValue;
    private              double                    _threshold;
    private              DoubleProperty            threshold;
    private              double                    _range;
    private              DoubleProperty            range;
    private              String                    _unit;
    private              StringProperty            unit;
    private              boolean                   _legendVisible;
    private              BooleanProperty           legendVisible;
    private              boolean                   _thresholdVisible;
    private              BooleanProperty           thresholdVisible;
    private              ObservableList<Stop>      gradientStops;
    private              List<Stop>                stops;
    private              boolean                   _smoothing;
    private              Color                     _chartBackgroundColor;
    private              ObjectProperty<Color>     chartBackgroundColor;
    private              Color                     _chartForegroundColor;
    private              ObjectProperty<Color>     chartForegroundColor;
    private              Color                     _chartTextColor;
    private              ObjectProperty<Color>     chartTextColor;
    private              Color                     _gridColor;
    private              ObjectProperty<Color>     gridColor;
    private              Paint                     _chartFill;
    private              ObjectProperty<Paint>     chartFill;
    private              Color                     _thresholdColor;
    private              ObjectProperty<Color>     thresholdColor;
    private              Mode                      _mode;
    private              ObjectProperty<Mode>      mode;
    private              double                    legendScaleFactor;
    private              InvalidationListener      resizeListener;
    private              ListChangeListener<Stop>  gradientListener;


    // ******************** Constructors **************************************
    public RadarChart() { this(null); }
    public RadarChart(final List<ChartData> DATA) {
        _minValue             = 0;
        _maxValue             = 100;
        _range                = 100;
        _threshold            = 100;
        _noOfSectors          = MIN_NO_OF_SECTORS;
        _unit                 = "";
        _legendVisible        = false;
        _thresholdVisible     = false;
        _mode                 = Mode.POLYGON;
        gradientStops         = FXCollections.observableArrayList();
        decimals              = 0;
        formatString          = new StringBuilder("%.").append(decimals).append("f").toString();
        data                  = FXCollections.observableArrayList();
        legendScaleFactor     = 1.0;
        _smoothing            = false;
        _chartBackgroundColor = Color.TRANSPARENT;
        _chartForegroundColor = Tile.FOREGROUND;
        _chartTextColor       = Tile.FOREGROUND;
        _gridColor            = Tile.GRAY;
        _chartFill            = Tile.BLUE;
        _thresholdColor       = Tile.LIGHT_RED;
        resizeListener        = o -> resize();
        gradientListener      = change -> {
            stops.clear();
            for (Stop stop : getGradientStops()) {
                if (Double.compare(stop.getOffset(), 0.0) == 0) { stops.add(new Stop(0, stop.getColor())); }
                stops.add(new Stop(stop.getOffset() * 0.69924 + 0.285, stop.getColor()));
            }
            redraw();
        };

        init();
        initGraphics();
        initData(DATA);
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void initData(final List<ChartData> DATA) {
        if (null == DATA || DATA.isEmpty()) {
            for (int i = 0; i < (getNoOfSectors() + 1); i++) { addData(new ChartData(0d)); }
        } else {
            setData(DATA);
        }
    }

    private void init() {
        if (Double.compare(getPrefWidth(), 0.0) <= 0 || Double.compare(getPrefHeight(), 0.0) <= 0 ||
            Double.compare(getWidth(), 0.0) <= 0 || Double.compare(getHeight(), 0.0) <= 0) {
            if (getPrefWidth() > 0 && getPrefHeight() > 0) {
                setPrefSize(getPrefWidth(), getPrefHeight());
            } else {
                setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            }
        }

        if (Double.compare(getMinWidth(), 0.0) <= 0 || Double.compare(getMinHeight(), 0.0) <= 0) {
            setMinSize(MINIMUM_WIDTH, MINIMUM_HEIGHT);
        }

        if (Double.compare(getMaxWidth(), 0.0) <= 0 || Double.compare(getMaxHeight(), 0.0) <= 0) {
            setMaxSize(MAXIMUM_WIDTH, MAXIMUM_HEIGHT);
        }
    }

    private void initGraphics() {
        stops = new ArrayList<>(16);
        for (Stop stop : getGradientStops()) {
            if (Double.compare(stop.getOffset(), 0.0) == 0) stops.add(new Stop(0, stop.getColor()));
            stops.add(new Stop(stop.getOffset() * 0.69924 + 0.285, stop.getColor()));
        }

        chartCanvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        chartCtx    = chartCanvas.getGraphicsContext2D();

        overlayCanvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        overlayCtx    = overlayCanvas.getGraphicsContext2D();

        unitText = new Text(getUnit());
        unitText.setTextAlignment(TextAlignment.CENTER);
        unitText.setTextOrigin(VPos.CENTER);
        unitText.setFont(Fonts.latoLight(0.045 * PREFERRED_WIDTH));

        legendStep = (getMaxValue() - getMinValue()) / 5d;
        dropShadow = new DropShadow(BlurType.TWO_PASS_BOX, Color.BLACK, 5, 0, 0, 0);

        minValueText = new Text(String.format(Locale.US, formatString, getMinValue()));
        minValueText.setTextAlignment(TextAlignment.CENTER);
        minValueText.setTextOrigin(VPos.CENTER);
        minValueText.setVisible(isLegendVisible());
        minValueText.setEffect(dropShadow);

        legend1Text = new Text(String.format(Locale.US, formatString, getMinValue() + legendStep));
        legend1Text.setTextAlignment(TextAlignment.CENTER);
        legend1Text.setTextOrigin(VPos.CENTER);
        legend1Text.setVisible(isLegendVisible());
        legend1Text.setEffect(dropShadow);

        legend2Text = new Text(String.format(Locale.US, formatString, getMinValue() + legendStep * 2));
        legend2Text.setTextAlignment(TextAlignment.CENTER);
        legend2Text.setTextOrigin(VPos.CENTER);
        legend2Text.setVisible(isLegendVisible());
        legend2Text.setEffect(dropShadow);

        legend3Text = new Text(String.format(Locale.US, formatString, getMinValue() + legendStep * 3));
        legend3Text.setTextAlignment(TextAlignment.CENTER);
        legend3Text.setTextOrigin(VPos.CENTER);
        legend3Text.setVisible(isLegendVisible());
        legend3Text.setEffect(dropShadow);

        legend4Text = new Text(String.format(Locale.US, formatString, getMinValue() + legendStep * 3));
        legend4Text.setTextAlignment(TextAlignment.CENTER);
        legend4Text.setTextOrigin(VPos.CENTER);
        legend4Text.setVisible(isLegendVisible());
        legend4Text.setEffect(dropShadow);

        maxValueText = new Text(String.format(Locale.US, formatString, getMaxValue()));
        maxValueText.setTextAlignment(TextAlignment.CENTER);
        maxValueText.setTextOrigin(VPos.CENTER);
        maxValueText.setVisible(isLegendVisible());
        maxValueText.setEffect(dropShadow);

        // Add all nodes
        pane = new Pane(chartCanvas, overlayCanvas, unitText, minValueText, legend1Text, legend2Text, legend3Text, legend4Text, maxValueText);
        pane.setBackground(new Background(new BackgroundFill(getChartBackgroundColor(), new CornerRadii(1024), Insets.EMPTY)));

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        widthProperty().addListener(resizeListener);
        heightProperty().addListener(resizeListener);
        //data.addListener((MapChangeListener<Integer, ChartData>) change -> redraw());
        gradientStops.addListener(gradientListener);
    }

    public void dispose() {
        widthProperty().removeListener(resizeListener);
        heightProperty().removeListener(resizeListener);
        gradientStops.removeListener(gradientListener);
    }


    // ******************** Methods *******************************************
    public double getMinValue() { return null == minValue ? _minValue : minValue.get(); }
    public void setMinValue(final double VALUE) {
        if (null == minValue) {
            _minValue = clamp(-Double.MAX_VALUE, getMaxValue(), VALUE);
            setRange(getMaxValue() - _minValue);
            redraw();
        } else {
            minValue.set(VALUE);
        }
    }
    public ReadOnlyDoubleProperty minValueProperty() {
        if (null == minValue) {
            minValue = new DoublePropertyBase(_minValue) {
                @Override public void set(final double MIN_VALUE) {
                    super.set(clamp(-Double.MAX_VALUE, getMaxValue(), MIN_VALUE));
                    setRange(getMaxValue() - get());
                }
                @Override public Object getBean() { return RadarChart.this; }
                @Override public String getName() { return "minValue"; }
            };
        }
        return minValue;
    }

    public double getMaxValue() { return null == maxValue ? _maxValue : maxValue.get(); }
    public void setMaxValue(final double VALUE) {
        if (null == maxValue) {
            _maxValue = clamp(getMinValue(), Double.MAX_VALUE, VALUE);
            setRange(_maxValue - getMinValue());
            redraw();
        } else {
            maxValue.set(VALUE);
        }
    }
    public ReadOnlyDoubleProperty maxValueProperty() {
        if (null == maxValue) {
            maxValue = new DoublePropertyBase(_maxValue) {
                @Override protected void invalidated() {
                    set(clamp(getMinValue(), Double.MAX_VALUE, get()));
                    setRange(_maxValue - getMinValue());
                    redraw();
                }
                @Override public Object getBean() { return RadarChart.this; }
                @Override public String getName() { return "maxValue"; }
            };
        }
        return maxValue;
    }

    public double getRange() { return null == range ? _range : range.get(); }
    private void setRange(final double VALUE) {
        if (null == range) {
            _range = VALUE;
        } else {
            range.set(VALUE);
        }
    }
    public ReadOnlyDoubleProperty rangeProperty() {
        if (null == range) {
            range = new DoublePropertyBase(_range) {
                @Override public Object getBean() { return RadarChart.this; }
                @Override public String getName() { return "range"; }
            };
        }
        return range;
    }

    public double getThreshold() { return null == threshold ? _threshold : threshold.get(); }
    public void setThreshold(final double VALUE) {
        if (null == threshold) {
            _threshold = clamp(getMinValue(), getMaxValue(), VALUE);
            setRange(getMaxValue() - VALUE);
            drawOverlay();
        } else {
            threshold.set(VALUE);
        }
    }
    public DoubleProperty thresholdProperty() {
        if (null == threshold) {
            threshold = new DoublePropertyBase(_threshold) {
                @Override protected void invalidated() {
                    set(clamp(getMinValue(), getMaxValue(), get()));
                    setRange(getMaxValue() - get());
                    drawOverlay();
                }
                @Override public Object getBean() { return RadarChart.this; }
                @Override public String getName() { return "threshold"; }
            };
        }
        return threshold;
    }

    public int getNoOfSectors() { return null == noOfSectors ? _noOfSectors : noOfSectors.get(); }
    public void setNoOfSectors(final int SECTORS) {
        if (null == noOfSectors) {
            _noOfSectors = clamp(1, MAX_NO_OF_SECTORS, SECTORS);
            angleStep    = 360.0 / _noOfSectors;
            redraw();
        } else {
            noOfSectors.set(SECTORS);
        }
    }
    public IntegerProperty noOfSectorsProperty() {
        if (null == noOfSectors) {
            noOfSectors = new IntegerPropertyBase(_noOfSectors) {
                @Override public void set(final int SECTORS) {
                    super.set(clamp(1, MAX_NO_OF_SECTORS, SECTORS));
                    angleStep = 360.0 / get();
                    redraw();
                }
                @Override public Object getBean() { return RadarChart.this; }
                @Override public String getName() { return "noOfSectors"; }
            };
        }
        return noOfSectors;
    }

    public boolean isThresholdVisible() { return null == thresholdVisible ? _thresholdVisible : thresholdVisible.get(); }
    public void setThresholdVisible(final boolean VISIBLE) {
        if (null == thresholdVisible) {
            _thresholdVisible = VISIBLE;
            redraw();
        } else {
            thresholdVisible.set(VISIBLE);
        }
    }
    public BooleanProperty thresholdVisibleProperty() {
        if (null == thresholdVisible) {
            thresholdVisible = new BooleanPropertyBase(_thresholdVisible) {
                @Override protected void invalidated() { redraw(); }
                @Override public Object getBean() { return RadarChart.this; }
                @Override public String getName() { return "thresholdVisible"; }
            };
        }
        return thresholdVisible;
    }

    public ObservableList<Stop> getGradientStops() { return gradientStops; }
    public void setGradientStops(final List<Stop> STOPS) { gradientStops.setAll(STOPS); }
    public void setGradientStops(final Stop... STOPS) { gradientStops.setAll(STOPS); }
    public void addGradientStop(final Stop STOP) { gradientStops.add(STOP); }

    public String getUnit() { return null == unit ? _unit : unit.get(); }
    public void setUnit(final String TEXT) {
        if (null == unit) {
            _unit = TEXT;
            redraw();
        } else {
            unit.set(TEXT);
        }
    }
    public StringProperty unitProperty() {
        if (null == unit) {
            unit = new StringPropertyBase(_unit) {
                @Override protected void invalidated() { redraw(); }
                @Override public Object getBean() { return RadarChart.this; }
                @Override public String getName() { return "unit"; }
            };
            _unit = null;
        }
        return unit;
    }

    public boolean isLegendVisible() { return null == legendVisible ? _legendVisible : legendVisible.get(); }
    public void setLegendVisible(final boolean VISIBLE) {
        if (null == legendVisible) {
            _legendVisible = VISIBLE;
            redraw();
        } else {
            legendVisible.set(VISIBLE);
        }
    }
    public BooleanProperty legendVisibleProperty() {
        if (null == legendVisible) {
            legendVisible = new BooleanPropertyBase(_legendVisible) {
                @Override protected void invalidated() { redraw(); }
                @Override public Object getBean() { return RadarChart.this; }
                @Override public String getName() { return "legendVisible"; }
            };
        }
        return legendVisible;
    }

    public ObservableList<ChartData> getData() { return data; }
    public void setData(final List<ChartData> DATA) {
        if (DATA.size() < MIN_NO_OF_SECTORS) throw new IllegalArgumentException("Not enough sectors (min. " + MIN_NO_OF_SECTORS + "needed)");
        if (DATA.size() > MAX_NO_OF_SECTORS) throw new IllegalArgumentException("Too many sectors (max. " + MAX_NO_OF_SECTORS + " sectors allowed)");
        DATA.forEach(d -> addData(d));
    }
    public void addData(final ChartData DATA) {
        if (data.size() > (getNoOfSectors() + 1)) throw new IllegalArgumentException("Too many sectors (max. " + getNoOfSectors() + " sectors allowed)");
        data.add(DATA);
        setNoOfSectors(data.size());
    }

    public void reset() {
        data.clear();
        initData(data);
    }

    public Color getChartBackgroundColor() { return null == chartBackgroundColor ? _chartBackgroundColor : chartBackgroundColor.getValue(); }
    public void setChartBackgroundColor(Color COLOR) {
        if (null == chartBackgroundColor) {
            _chartBackgroundColor = COLOR;
            redraw();
        } else {
            chartBackgroundColor.setValue(COLOR);
        }
    }
    public ObjectProperty<Color> chartBackgroundColorProperty() {
        if (null == chartBackgroundColor) {
            chartBackgroundColor = new ObjectPropertyBase<Color>(_chartBackgroundColor) {
                @Override protected void invalidated() { redraw(); }
                @Override public Object getBean() { return RadarChart.this; }
                @Override public String getName() { return "chartBackgroundColor"; }
            };
            _chartBackgroundColor = null;
        }
        return chartBackgroundColor;
    }

    public Color getChartForegroundColor() { return null == chartForegroundColor ? _chartForegroundColor : chartForegroundColor.getValue(); }
    public void setChartForegroundColor(final Color COLOR) {
        if (null == chartForegroundColor) {
            _chartForegroundColor = COLOR;
            redraw();
        } else {
            chartForegroundColor.setValue(COLOR);
        }
    }
    public ObjectProperty<Color> chartForegroundColorProperty() {
        if (null == chartForegroundColor) {
            chartForegroundColor = new ObjectPropertyBase<Color>(_chartForegroundColor) {
                @Override protected void invalidated() { redraw(); }
                @Override public Object getBean() { return RadarChart.this; }
                @Override public String getName() { return "chartForegroundColor"; }
            };
            _chartForegroundColor = null;
        }
        return chartForegroundColor;
    }

    public Color getChartTextColor() { return null == chartTextColor ? _chartTextColor : chartTextColor.getValue(); }
    public void setChartTextColor(Color COLOR) {
        if (null == chartTextColor) {
            _chartTextColor = COLOR;
            redraw();
        } else {
            chartTextColor.setValue(COLOR);
        }
    }
    public ObjectProperty<Color> chartTextColorProperty() {
        if (null == chartTextColor) {
            chartTextColor  = new ObjectPropertyBase<Color>(_chartTextColor) {
                @Override protected void invalidated() { redraw(); }
                @Override public Object getBean() { return RadarChart.this; }
                @Override public String getName() { return "chartTextColor"; }
            };
            _chartTextColor = null;
        }
        return chartTextColor;
    }

    public Color getGridColor() { return null == gridColor ? _gridColor : gridColor.getValue(); }
    public void setGridColor(Color COLOR) {
        if (null == gridColor) {
            _gridColor = COLOR;
            redraw();
        } else {
            gridColor.setValue(COLOR);
        }
    }
    public ObjectProperty<Color> gridColorProperty() {
        if (null == gridColor) {
            gridColor  = new ObjectPropertyBase<Color>(_gridColor) {
                @Override protected void invalidated() { redraw(); }
                @Override public Object getBean() { return RadarChart.this; }
                @Override public String getName() { return "gridColor"; }
            };
            _gridColor = null;
        }
        return gridColor;
    }

    public Paint getChartFill() { return null == chartFill ? _chartFill : chartFill.getValue(); }
    public void setChartFill(final Paint PAINT) {
        if (null == chartFill) {
            _chartFill = PAINT;
            redraw();
        } else {
            chartFill.setValue(PAINT);
        }
    }
    public ObjectProperty<Paint> chartFillProperty() {
        if (null == chartFill) {
            chartFill  = new ObjectPropertyBase<Paint>(_chartFill) {
                @Override protected void invalidated() { redraw(); }
                @Override public Object getBean() { return RadarChart.this; }
                @Override public String getName() { return "chartFill"; }
            };
            _chartFill = null;
        }
        return chartFill;
    }

    public Color getThresholdColor() { return null == thresholdColor ? _thresholdColor : thresholdColor.getValue(); }
    public void setThresholdColor(final Color COLOR) {
        if (null == thresholdColor) {
            _thresholdColor = COLOR;
            redraw();
        } else {
            thresholdColor.setValue(COLOR);
        }
    }
    public ObjectProperty<Color> thresholdColorProperty() {
        if (null == thresholdColor) {
            thresholdColor  = new ObjectPropertyBase<Color>(_thresholdColor) {
                @Override protected void invalidated() { redraw(); }
                @Override public Object getBean() { return RadarChart.this; }
                @Override public String getName() { return "thresholdColor"; }
            };
            _thresholdColor = null;
        }
        return thresholdColor;
    }

    public void scaleLegendToValue(final double VALUE) {
        legendScaleFactor = VALUE;
        redrawText();
    }

    public Mode getMode() { return null == mode ? _mode : mode.get(); }
    public void setMode(final Mode MODE) {
        if (null == mode) {
            _mode = MODE;
            redraw();
        } else {
            mode.set(MODE);
        }
    }
    public ObjectProperty<Mode> modeProperty() {
        if (null == mode) {
            mode = new ObjectPropertyBase<Mode>(_mode) {
                @Override protected void invalidated() { redraw(); }
                @Override public Object getBean() { return RadarChart.this; }
                @Override public String getName() { return "mode"; }
            };
            _mode = null;
        }
        return mode;
    }

    public boolean isSmoothing() { return _smoothing; }
    public void setSmoothing(final boolean SMOOTHING) {
        _smoothing = SMOOTHING;
        redraw();
    }


    // ******************** Style related *************************************
    @Override public String getUserAgentStylesheet() {
        return RadarChart.class.getResource("radarchart.css").toExternalForm();
    }


    // ******************** Private Methods ***********************************
    private void resize() {
        double width  = getWidth() - getInsets().getLeft() - getInsets().getRight();
        double height = getHeight() - getInsets().getTop() - getInsets().getBottom();
        size          = width < height ? width : height;

        if (size > 0) {
            pane.setMaxSize(size, size);
            pane.relocate((getWidth() - size) * 0.5, (getHeight() - size) * 0.5);
            pane.setBackground(new Background(new BackgroundFill(getChartBackgroundColor(), new CornerRadii(1024), Insets.EMPTY)));

            chartCanvas.setWidth(size);
            chartCanvas.setHeight(size);

            overlayCanvas.setWidth(size);
            overlayCanvas.setHeight(size);

            redraw();
        }
    }

    private void redrawText() {
        Color textColor = getChartTextColor();
        dropShadow.setColor(null == textColor ? Color.BLACK : getContrastColor(textColor));
        dropShadow.setRadius(size * 0.025);

        unitText.setFill(textColor);
        unitText.setText(getUnit());
        unitText.setFont(Fonts.latoRegular(size * 0.1));
        adjustTextSize(unitText, size * 0.22, size * 0.1);
        unitText.relocate((size - unitText.getLayoutBounds().getWidth()) * 0.5, (size - unitText.getLayoutBounds().getHeight()) * 0.5);

        minValueText.setVisible(isLegendVisible());
        legend1Text.setVisible(isLegendVisible());
        legend2Text.setVisible(isLegendVisible());
        legend3Text.setVisible(isLegendVisible());
        legend4Text.setVisible(isLegendVisible());
        maxValueText.setVisible(isLegendVisible());

        if (isLegendVisible()) {
            Font font = Fonts.latoRegular(size * 0.025);

            minValueText.setFill(textColor);
            minValueText.setText(String.format(Locale.US, formatString, getMinValue()));
            minValueText.setFont(font);
            minValueText.relocate((size - minValueText.getLayoutBounds().getWidth()) * 0.5, 0.3435 * size);

            legendStep = getRange() / 5.0;

            legend1Text.setFill(textColor);
            legend1Text.setText(String.format(Locale.US, formatString, (getMinValue() + legendStep) * legendScaleFactor));
            legend1Text.setFont(font);
            legend1Text.relocate((size - legend1Text.getLayoutBounds().getWidth()) * 0.5, 0.29 * size);

            legend2Text.setFill(textColor);
            legend2Text.setText(String.format(Locale.US, formatString, (getMinValue() + legendStep * 2) * legendScaleFactor));
            legend2Text.setFont(font);
            legend2Text.relocate((size - legend2Text.getLayoutBounds().getWidth()) * 0.5, 0.225 * size);

            legend3Text.setFill(textColor);
            legend3Text.setText(String.format(Locale.US, formatString, (getMinValue() + legendStep * 3) * legendScaleFactor));
            legend3Text.setFont(font);
            legend3Text.relocate((size - legend3Text.getLayoutBounds().getWidth()) * 0.5, 0.1599 * size);

            legend4Text.setFill(textColor);
            legend4Text.setText(String.format(Locale.US, formatString, (getMinValue() + legendStep * 4) * legendScaleFactor));
            legend4Text.setFont(font);
            legend4Text.relocate((size - legend4Text.getLayoutBounds().getWidth()) * 0.5, 0.097 * size);

            maxValueText.setFill(textColor);
            maxValueText.setText(String.format(Locale.US, formatString, getMaxValue()));
            maxValueText.setFont(font);
            maxValueText.relocate((size - maxValueText.getLayoutBounds().getWidth()) * 0.5, 0.048 * size);
        }
    }

    public void redraw() {
        chartCanvas.setCache(false);
        drawChart();
        chartCanvas.setCache(true);
        chartCanvas.setCacheHint(CacheHint.QUALITY);

        overlayCanvas.setCache(false);
        drawOverlay();
        overlayCanvas.setCache(true);
        overlayCanvas.setCacheHint(CacheHint.QUALITY);

        redrawText();
    }

    private void drawChart() {
        final double CENTER_X      = 0.5 * size;
        final double CENTER_Y      = CENTER_X;
        final double CIRCLE_SIZE   = 0.9 * size;
        final double DATA_RANGE    = getRange();
        final double RANGE         = 0.35714 * CIRCLE_SIZE;
        final double OFFSET        = 0.14286 * CIRCLE_SIZE;
        final int    NO_OF_SECTORS = getNoOfSectors();
        final double MIN_VALUE     = getMinValue();
        final double MAX_VALUE     = getMaxValue();

        // clear the chartCanvas
        chartCtx.clearRect(0, 0, size, size);


        // draw the chart data
        chartCtx.save();
        if (gradientStops.isEmpty()) {
            chartCtx.setFill(getChartFill());
        } else {
            chartCtx.setFill(new RadialGradient(0, 0,
                                                CENTER_X, CENTER_Y,
                                                CIRCLE_SIZE * 0.5,
                                                false, CycleMethod.NO_CYCLE,
                                                stops));
        }

        double radiusFactor;
        switch(getMode()) {
            case POLYGON:
                if (isSmoothing()) {
                    double      radAngle     = Math.toRadians(180);
                    double      radAngleStep = Math.toRadians(angleStep);
                    List<Point> points       = new ArrayList<>();

                    double x = CENTER_X + (-Math.sin(radAngle) * (CENTER_Y - (0.36239 * size)));
                    double y = CENTER_Y + (+Math.cos(radAngle) * (CENTER_Y - (0.36239 * size)));
                    points.add(new Point(x, y));

                    for (int i = 0 ; i < NO_OF_SECTORS ; i++) {
                        double r1 = (CENTER_Y - (CENTER_Y - OFFSET - ((data.get(i).getValue() - MIN_VALUE) / DATA_RANGE) * RANGE));
                        x = CENTER_X + (-Math.sin(radAngle) * r1);
                        y = CENTER_Y + (+Math.cos(radAngle) * r1);
                        points.add(new Point(x, y));
                        radAngle += radAngleStep;
                    }
                    double r3 = (CENTER_Y - (CENTER_Y - OFFSET - ((data.get(NO_OF_SECTORS - 1).getValue() - MIN_VALUE) / DATA_RANGE) * RANGE));
                    x = CENTER_X + (-Math.sin(radAngle) * r3);
                    y = CENTER_Y + (+Math.cos(radAngle) * r3);
                    points.add(new Point(x, y));

                    Point[] interpolatedPoints = Helper.subdividePoints(points.toArray(new Point[0]), 8);

                    chartCtx.beginPath();
                    chartCtx.moveTo(interpolatedPoints[0].getX(), interpolatedPoints[0].getY());
                    for (int i = 0 ; i < interpolatedPoints.length - 1 ; i++) {
                        Point point = interpolatedPoints[i];
                        chartCtx.lineTo(point.getX(), point.getY());
                    }
                    chartCtx.lineTo(interpolatedPoints[interpolatedPoints.length - 1].getX(), interpolatedPoints[interpolatedPoints.length - 1].getY());
                    chartCtx.closePath();

                    chartCtx.fill();
                } else {
                    chartCtx.beginPath();
                    chartCtx.moveTo(CENTER_X, 0.36239 * size);
                    for (int i = 0; i < NO_OF_SECTORS; i++) {
                        radiusFactor = (clamp(MIN_VALUE, MAX_VALUE, (data.get(i).getValue()) - MIN_VALUE) / DATA_RANGE);
                        //chartCtx.lineTo(CENTER_X, CENTER_Y - OFFSET - radiusFactor * RANGE);
                        chartCtx.lineTo(CENTER_X, CENTER_Y - OFFSET - radiusFactor * RANGE);

                        chartCtx.translate(CENTER_X, CENTER_Y);
                        chartCtx.rotate(angleStep);
                        chartCtx.translate(-CENTER_X, -CENTER_Y);
                    }
                    radiusFactor = ((clamp(MIN_VALUE, MAX_VALUE, data.get(NO_OF_SECTORS - 1).getValue()) - MIN_VALUE) / DATA_RANGE);
                    chartCtx.lineTo(CENTER_X, CENTER_Y - OFFSET - radiusFactor * RANGE);
                    chartCtx.closePath();
                    chartCtx.fill();
                }
                break;
            case SECTOR:
                chartCtx.translate(CENTER_X, CENTER_Y);
                chartCtx.rotate(-90);
                chartCtx.translate(-CENTER_X, -CENTER_Y);
                // sector mode
                for (int i = 0 ; i < NO_OF_SECTORS ; i++) {
                    radiusFactor = (clamp(MIN_VALUE, MAX_VALUE, (data.get(i).getValue() - MIN_VALUE)) / DATA_RANGE);
                    chartCtx.beginPath();
                    chartCtx.moveTo(CENTER_X, CENTER_Y);
                    chartCtx.arc(CENTER_X, CENTER_Y, radiusFactor * RANGE + OFFSET, radiusFactor * RANGE + OFFSET, 0, -angleStep);
                    chartCtx.closePath();
                    chartCtx.fill();

                    chartCtx.translate(CENTER_X, CENTER_Y);
                    chartCtx.rotate(angleStep);
                    chartCtx.translate(-CENTER_X, -CENTER_Y);
                }
                break;
        }
        chartCtx.restore();
    }

    private void drawOverlay() {
        final Paint  CHART_BKG     = getChartBackgroundColor();
        final double CENTER_X      = 0.5 * size;
        final double CENTER_Y      = CENTER_X;
        final double CIRCLE_SIZE   = 0.90 * size;
        final double DATA_RANGE    = getRange();
        final double RANGE         = 0.35714 * CIRCLE_SIZE;
        final double OFFSET        = 0.14286 * CIRCLE_SIZE;
        final int    NO_OF_SECTORS = getNoOfSectors();
        final double MIN_VALUE     = getMinValue();
        double radius;

        // clear the chartCanvas
        overlayCtx.clearRect(0, 0, size, size);

        // draw center point
        overlayCtx.save();
        overlayCtx.setFill(CHART_BKG);
        overlayCtx.translate(CENTER_X - OFFSET, CENTER_Y - OFFSET);
        overlayCtx.fillOval(0, 0, 2 * OFFSET, 2 * OFFSET);
        overlayCtx.restore();

        // draw concentric rings
        overlayCtx.setLineWidth(1);
        overlayCtx.setStroke(getGridColor());
        double ringStepSize = (CIRCLE_SIZE - CIRCLE_SIZE * 0.28571) / 20.0;
        double pos          = 0.5 * (size - CIRCLE_SIZE);
        double ringSize     = CIRCLE_SIZE;
        for (int i = 0 ; i < 11 ; i++) {
            overlayCtx.strokeOval(pos, pos, ringSize, ringSize);
            pos      += ringStepSize;
            ringSize -= 2 * ringStepSize;
        }

        // draw star lines
        overlayCtx.save();
        for (int i = 0 ; i < NO_OF_SECTORS ; i++) {
            overlayCtx.strokeLine(CENTER_X, 0.37 * size, CENTER_X, 0.5 * (size - CIRCLE_SIZE));
            overlayCtx.translate(CENTER_X, CENTER_Y);
            overlayCtx.rotate(angleStep);
            overlayCtx.translate(-CENTER_X, -CENTER_Y);
        }
        overlayCtx.restore();

        // draw threshold line
        if (isThresholdVisible()) {
            radius = ((getThreshold() - MIN_VALUE) / DATA_RANGE);
            overlayCtx.setLineWidth(clamp(1d, 3d, size * 0.005));
            overlayCtx.setStroke(getThresholdColor());
            overlayCtx.strokeOval(0.5 * size - OFFSET - radius * RANGE, 0.5 * size - OFFSET - radius * RANGE,
                                  2 * (radius * RANGE + OFFSET), 2 * (radius * RANGE + OFFSET));
        }

        // prerotate if sectormode
        overlayCtx.save();

        if (Mode.SECTOR == getMode()) {
            overlayCtx.translate(CENTER_X, CENTER_Y);
            overlayCtx.rotate(angleStep * 0.5);
            overlayCtx.translate(-CENTER_X, -CENTER_Y);
        }

        // draw text
        overlayCtx.save();
        overlayCtx.setFont(Fonts.latoRegular(0.04 * size));
        overlayCtx.setTextAlign(TextAlignment.CENTER);
        overlayCtx.setTextBaseline(VPos.CENTER);
        if (NO_OF_SECTORS == 0) { overlayCtx.setFill(getChartForegroundColor()); }
        for (int i = 0 ; i < NO_OF_SECTORS ; i++) {
            overlayCtx.setFill(data.get(i).getTextColor());
            overlayCtx.fillText(data.get(i).getName(), CENTER_X, size * 0.02);
            overlayCtx.translate(CENTER_X, CENTER_Y);
            overlayCtx.rotate(angleStep);
            overlayCtx.translate(-CENTER_X, -CENTER_Y);
        }
        overlayCtx.restore();

        overlayCtx.restore();
    }
}
