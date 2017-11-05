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

import com.sun.javafx.charts.Legend;
import com.sun.javafx.charts.Legend.LegendItem;
import eu.hansolo.tilesfx.events.SmoothedChartEvent;
import eu.hansolo.tilesfx.tools.Helper;
import eu.hansolo.tilesfx.tools.Point;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.IntegerPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.Axis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class SmoothedChart<T, S> extends AreaChart<T, S> {
    private static final int                       MAX_SUBDIVISIONS = 16;
    private static final int                       MAX_DECIMALS     = 10;
    private              boolean                   _smoothed;
    private              BooleanProperty           smoothed;
    private              boolean                   _filled;
    private              BooleanProperty           filled;
    private              int                       _subDivisions;
    private              IntegerProperty           subDivisions;
    private              boolean                   _snapToTicks;
    private              BooleanProperty           snapToTicks;
    private              boolean                   _dataPointsVisible;
    private              BooleanProperty           dataPointsVisible;
    private              Color                     _selectorFillColor;
    private              ObjectProperty<Color>     selectorFillColor;
    private              Color                     _selectorStrokeColor;
    private              ObjectProperty<Color>     selectorStrokeColor;
    private              int                       _decimals;
    private              IntegerProperty           decimals;
    private              String                    formatString;
    private              Circle                    selector;
    private              Tooltip                   selectorTooltip;
    private              Region                    chartPlotBackground;
    private              SequentialTransition      fadeInFadeOut;
    private              List<Path>                strokePaths;
    private              boolean                   _interactive;
    private              BooleanProperty           interactive;
    private              EventHandler<MouseEvent>  clickHandler;
    private              EventHandler<ActionEvent> endOfTransformationHandler;


    // ******************** Constructors **************************************
    public SmoothedChart(final Axis<T> xAxis, final Axis<S> yAxis) {
        super(xAxis, yAxis);
        init();
        registerListeners();
    }
    public SmoothedChart(final Axis<T> xAxis, final Axis<S> yAxis, final ObservableList<Series<T, S>> data) {
        super(xAxis, yAxis, data);
        init();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void init() {
        _smoothed                  = true;
        _filled                    = false;
        _subDivisions              = 8;
        _snapToTicks               = true;
        _selectorFillColor         = Color.WHITE;
        _selectorStrokeColor       = Color.RED;
        _decimals                  = 2;
        _interactive               = true;
        formatString               = "%.2f";
        strokePaths                = new ArrayList<>();
        clickHandler               = e -> select(e);
        endOfTransformationHandler = e -> { selectorTooltip.hide(); };

        // Add selector to chart
        selector = new Circle();
        selector.setFill(_selectorFillColor);
        selector.setStroke(_selectorStrokeColor);
        selector.setOpacity(0);

        selectorTooltip = new Tooltip("");
        Tooltip.install(selector, selectorTooltip);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(100), selector);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(100), selector);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        fadeInFadeOut = new SequentialTransition(fadeIn, new PauseTransition(Duration.millis(3000)), fadeOut);
        fadeInFadeOut.setOnFinished(endOfTransformationHandler);

        chartPlotBackground = getChartBackground();
        chartPlotBackground.widthProperty().addListener(o -> resizeSelector(chartPlotBackground));
        chartPlotBackground.heightProperty().addListener(o -> resizeSelector(chartPlotBackground));
        chartPlotBackground.layoutYProperty().addListener(o -> resizeSelector(chartPlotBackground));

        Path horizontalGridLines = getHorizontalGridLines();
        if (null != horizontalGridLines) { horizontalGridLines.setMouseTransparent(true); }

        Path verticalGridLines = getVerticalGridLines();
        if (null != verticalGridLines) { verticalGridLines.setMouseTransparent(true); }

        getChartChildren().addAll(selector);
    }

    private void registerListeners() {
        getData().addListener((ListChangeListener<Series<T, S>>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    change.getAddedSubList().forEach(addedItem -> {
                        final Series<T, S> series     = addedItem;
                        final Path         strokePath = (Path) ((Group) series.getNode()).getChildren().get(1);
                        final Path         fillPath   = (Path) ((Group) series.getNode()).getChildren().get(0);
                        fillPath.addEventHandler(MouseEvent.MOUSE_PRESSED, clickHandler);
                        strokePath.addEventHandler(MouseEvent.MOUSE_PRESSED, clickHandler);
                        strokePaths.add(strokePath);
                    });
                } else if (change.wasRemoved()) {
                    change.getRemoved().forEach(removedItem -> {
                        final Series<T, S> series     = removedItem;
                        final Path         strokePath = (Path) ((Group) series.getNode()).getChildren().get(1);
                        final Path         fillPath   = (Path) ((Group) series.getNode()).getChildren().get(0);
                        fillPath.removeEventHandler(MouseEvent.MOUSE_PRESSED, clickHandler);
                        strokePath.removeEventHandler(MouseEvent.MOUSE_PRESSED, clickHandler);
                        strokePaths.remove(strokePath);
                    });

                }
            }
        });
    }


    // ******************** Public Methods ************************************
    public boolean isSmoothed() { return null == smoothed ? _smoothed : smoothed.get(); }
    public void setSmoothed(final boolean SMOOTHED) {
        if (null == smoothed) {
            _smoothed = SMOOTHED;
            layoutPlotChildren();
        } else {
            smoothed.set(SMOOTHED);
        }
    }
    public BooleanProperty smoothedProperty() {
        if (null == smoothed) {
            smoothed = new BooleanPropertyBase(_smoothed) {
                @Override protected void invalidated() { layoutPlotChildren(); }
                @Override public Object getBean() { return SmoothedChart.this; }
                @Override public String getName() { return "smoothed"; }
            };
        }
        return smoothed;
    }

    public boolean isFilled() { return null == filled ? _filled : filled.get(); }
    public void setFilled(final boolean FILLED) {
        if (null == filled) {
            _filled = FILLED;
            layoutPlotChildren();
        } else {
            filled.set(FILLED);
        }
    }
    public BooleanProperty filledProperty() {
        if (null == filled) {
            filled = new BooleanPropertyBase(_filled) {
                @Override protected void invalidated() { layoutPlotChildren(); }
                @Override public Object getBean() { return SmoothedChart.this; }
                @Override public String getName() { return "filled"; }
            };
        }
        return filled;
    }

    public int getSubDivisions() { return null == subDivisions ? _subDivisions : subDivisions.get(); }
    public void setSubDivisions(final int SUB_DIVISIONS) {
        if (null == subDivisions) {
            _subDivisions = Helper.clamp(1, MAX_SUBDIVISIONS, SUB_DIVISIONS);
            layoutPlotChildren();
        } else {
            subDivisions.set(SUB_DIVISIONS);
        }
    }
    public IntegerProperty subDivisionsProperty() {
        if (null == subDivisions) {
            subDivisions = new IntegerPropertyBase(_subDivisions) {
                @Override protected void invalidated() {
                    set(Helper.clamp(1, MAX_SUBDIVISIONS, get()));
                    layoutPlotChildren();
                }
                @Override public Object getBean() { return SmoothedChart.this; }
                @Override public String getName() { return "subDivisions"; }
            };
        }
        return subDivisions;
    }

    public boolean isSnapToTicks() { return null == snapToTicks ? _snapToTicks : snapToTicks.get(); }
    public void setSnapToTicks(final boolean SNAP) {
        if (null == snapToTicks) {
            _snapToTicks = SNAP;
        } else {
            snapToTicks.set(SNAP);
        }
    }
    public BooleanProperty snapToTicksProperty() {
        if (null == snapToTicks) {
            snapToTicks = new BooleanPropertyBase(_snapToTicks) {
                @Override protected void invalidated() {}
                @Override public Object getBean() { return SmoothedChart.this; }
                @Override public String getName() { return "snapToTicks"; }
            };
        }
        return snapToTicks;
    }

    public boolean getDataPointsVisible() { return null == dataPointsVisible ? _dataPointsVisible : dataPointsVisible.get(); }
    public void setDataPointsVisible(final boolean VISIBLE) {
        if (null == dataPointsVisible) {
            _dataPointsVisible = VISIBLE;
            getData().forEach(series -> setSymbolsVisible(series, _dataPointsVisible));
        } else {
            dataPointsVisible.set(VISIBLE);
        }
    }
    public BooleanProperty dataPointsVisibleProperty() {
        if (null == dataPointsVisible) {
            dataPointsVisible = new BooleanPropertyBase(_dataPointsVisible) {
                @Override protected void invalidated() { getData().forEach(series -> setSymbolsVisible(series, _dataPointsVisible)); }
                @Override public Object getBean() { return SmoothedChart.this; }
                @Override public String getName() { return "dataPointsVisible"; }
            };
        }
        return dataPointsVisible;
    }

    public Color getSelectorFillColor() { return null == selectorFillColor ? _selectorFillColor : selectorFillColor.get(); }
    public void setSelectorFillColor(final Color COLOR) {
        if (null == selectorFillColor) {
            _selectorFillColor = COLOR;
            selector.setFill(_selectorFillColor);
            layoutPlotChildren();
        } else {
            selectorFillColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> selectorFillColorProperty() {
        if (null == selectorFillColor) {
            selectorFillColor = new ObjectPropertyBase<Color>(_selectorFillColor) {
                @Override protected void invalidated() {
                    selector.setFill(get());
                    layoutPlotChildren();
                }
                @Override public Object getBean() { return SmoothedChart.this; }
                @Override public String getName() { return "selectorFillColor"; }
            };
            _selectorFillColor = null;
        }
        return selectorFillColor;
    }

    public Color getSelectorStrokeColor() { return null == selectorStrokeColor ? _selectorStrokeColor : selectorStrokeColor.get(); }
    public void setSelectorStrokeColor(final Color COLOR) {
        if (null == selectorStrokeColor) {
            _selectorStrokeColor = COLOR;
            selector.setStroke(_selectorStrokeColor);
            layoutPlotChildren();
        } else {
            selectorStrokeColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> selectorStrokeColorProperty() {
        if (null == selectorStrokeColor) {
            selectorStrokeColor = new ObjectPropertyBase<Color>(_selectorStrokeColor) {
                @Override protected void invalidated() {
                    selector.setStroke(get());
                    layoutPlotChildren();
                }
                @Override public Object getBean() { return SmoothedChart.this; }
                @Override public String getName() { return "selectorStrokeColor"; }
            };
            _selectorStrokeColor = null;
        }
        return selectorStrokeColor;
    }

    public int getDecimals() { return null == decimals ? _decimals : decimals.get(); }
    public void setDecimals(final int DECIMALS) {
        if (null == decimals) {
            _decimals    = Helper.clamp(0, MAX_DECIMALS, DECIMALS);
            formatString = new StringBuilder("%.").append(_decimals).append("f").toString();
        } else {
            decimals.set(DECIMALS);
        }
    }
    public IntegerProperty decimalsProperty() {
        if (null == decimals) {
            decimals = new IntegerPropertyBase(_decimals) {
                @Override protected void invalidated() {
                    set(Helper.clamp(0, MAX_DECIMALS, get()));
                    formatString = new StringBuilder("%.").append(_decimals).append("f").toString();
                }
                @Override public Object getBean() { return SmoothedChart.this; }
                @Override public String getName() { return "decimals"; }
            };
        }
        return decimals;
    }

    public boolean isInteractive() { return null == interactive ? _interactive : interactive.get(); }
    public void setInteractive(final boolean INTERACTIVE) {
        if (null == interactive) {
            _interactive = INTERACTIVE;
        } else {
            interactive.set(INTERACTIVE);
        }
    }
    public BooleanProperty interactiveProperty() {
        if (null == interactive) {
            interactive = new BooleanPropertyBase(_interactive) {
                @Override public Object getBean() { return SmoothedChart.this; }
                @Override public String getName() { return "interactive"; }
            };
        }
        return interactive;
    }

    public void setSymbolsVisible(final XYChart.Series<T, S> SERIES, final boolean VISIBLE) {
        if (!getData().contains(SERIES)) { return; }
        for (XYChart.Data<T, S> data : SERIES.getData()) {
            StackPane stackPane = (StackPane) data.getNode();
            stackPane.setVisible(VISIBLE);
        }
    }

    public void setSeriesColor(final XYChart.Series<T, S> SERIES, final Paint COLOR) {
        Background symbolBackground = new Background(new BackgroundFill(COLOR, new CornerRadii(5), Insets.EMPTY), new BackgroundFill(Color.WHITE, new CornerRadii(5), new Insets(2)));
        setSeriesColor(SERIES, COLOR, COLOR, symbolBackground, COLOR);
    }
    public void setSeriesColor(final XYChart.Series<T, S> SERIES, final Paint STROKE, final Paint FILL) {
        Background symbolBackground = new Background(new BackgroundFill(STROKE, new CornerRadii(5), Insets.EMPTY), new BackgroundFill(Color.WHITE, new CornerRadii(5), new Insets(2)));
        setSeriesColor(SERIES, STROKE, FILL, symbolBackground, STROKE);
    }
    public void setSeriesColor(final XYChart.Series<T, S> SERIES, final Paint STROKE, final Paint FILL, final Paint LEGEND_SYMBOL_FILL) {
        setSeriesColor(SERIES, STROKE, FILL, null, LEGEND_SYMBOL_FILL);
    }
    public void setSeriesColor(final XYChart.Series<T, S> SERIES, final Paint STROKE, final Paint FILL, final Background SYMBOL_BACKGROUND) {
        setSeriesColor(SERIES, STROKE, FILL, SYMBOL_BACKGROUND, STROKE);
    }
    public void setSeriesColor(final XYChart.Series<T, S> SERIES, final Paint STROKE, final Paint FILL, final BackgroundFill SYMBOL_STROKE, final BackgroundFill SYMBOL_Fill) {
        setSeriesColor(SERIES, STROKE, FILL, new Background(SYMBOL_STROKE, SYMBOL_Fill), STROKE);
    }
    public void setSeriesColor(final XYChart.Series<T, S> SERIES, final Paint STROKE, final Paint FILL, final Background SYMBOL_BACKGROUND, final Paint LEGEND_SYMBOL_FILL) {
        if (!getData().contains(SERIES)) { return; }
        if (null != FILL) { ((Path) ((Group) SERIES.getNode()).getChildren().get(0)).setFill(FILL); }
        if (null != STROKE) { ((Path) ((Group) SERIES.getNode()).getChildren().get(1)).setStroke(STROKE); }
        if (null != SYMBOL_BACKGROUND) { setSymbolColor(SERIES, SYMBOL_BACKGROUND); }
        if (null != LEGEND_SYMBOL_FILL) { setLegendSymbolColor(SERIES, LEGEND_SYMBOL_FILL); }
    }

    public void setSymbolColor(final Series<T, S> SERIES, final Background SYMBOL_BACKGROUND) {
        if (!getData().contains(SERIES)) { return; }
        for (XYChart.Data<T, S> data : SERIES.getData()) {
            StackPane stackPane = (StackPane) data.getNode();
            if (null == stackPane) { continue; }
            stackPane.setBackground(SYMBOL_BACKGROUND);
        }
    }

    public void setLegendSymbolColor(final Series<T, S> SERIES, final Paint LEGEND_SYMBOL_FILL) {
        if (getData().isEmpty()) { return; }
        if (!getData().contains(SERIES)) { return; }

        int seriesIndex = getData().indexOf(SERIES);
        if (seriesIndex == -1) { return; }

        Legend legend = (Legend) getLegend();
        if (null == legend) { return; }

        LegendItem item = legend.getItems().get(seriesIndex);
        if (null == item) { return; }

        Region symbol = (Region) item.getSymbol();
        if (null == symbol) { return; }

        symbol.setBackground(new Background(new BackgroundFill(LEGEND_SYMBOL_FILL, new CornerRadii(6), Insets.EMPTY)));
    }


    // ******************** Internal Methods **********************************
    @Override protected void layoutPlotChildren() {
        super.layoutPlotChildren();
        double height = getLayoutBounds().getHeight();
        for (int seriesIndex = 0; seriesIndex < getDataSize(); seriesIndex++) {
            final XYChart.Series<T, S> series     = getData().get(seriesIndex);
            final Path                 strokePath = (Path) ((Group) series.getNode()).getChildren().get(1);
            final Path                 fillPath   = (Path) ((Group) series.getNode()).getChildren().get(0);
            if (isSmoothed()) { smooth(strokePath.getElements(), fillPath.getElements(), height); }
            fillPath.setVisible(isFilled());
            fillPath.setManaged(isFilled());
        }
    }

    private int getDataSize() {
        final ObservableList<Series<T, S>> data = getData();
        return (data != null) ? data.size() : 0;
    }

    private Region getChartBackground() {
        for (Node node : lookupAll(".chart-plot-background")) {
            if (node instanceof Region) { return (Region) node; }
        }
        return null;
    }

    private Path getHorizontalGridLines() {
        for (Node node : lookupAll(".chart-horizontal-grid-lines")) {
            if (node instanceof Path) { return (Path) node; }
        }
        return null;
    }

    private Path getVerticalGridLines() {
        for (Node node : lookupAll(".chart-vertical-grid-lines")) {
            if (node instanceof Path) { return (Path) node; }
        }
        return null;
    }

    private void resizeSelector(final Region CHART_BACKGROUND) {
        selector.setVisible(false);
        final double CHART_WIDTH  = CHART_BACKGROUND.getLayoutBounds().getWidth();
        final double CHART_HEIGHT = CHART_BACKGROUND.getLayoutBounds().getHeight();
        final double SIZE         = CHART_WIDTH < CHART_HEIGHT ? CHART_WIDTH : CHART_HEIGHT;
        selector.setRadius(SIZE * 0.02);
        selector.setStrokeWidth(SIZE * 0.01);
    }

    private void select(final MouseEvent EVT) {
        if (!isInteractive()) { return; }

        final Path   PATH         = (Path) EVT.getSource();
        final double EVENT_X      = EVT.getX();
        final double CHART_X      = chartPlotBackground.getBoundsInParent().getMinX();
        final double CHART_MIN_Y  = chartPlotBackground.getBoundsInParent().getMinY();
        final double CHART_HEIGHT = chartPlotBackground.getBoundsInParent().getHeight();

        if (!PATH.contains(new Point2D(EVT.getX(), EVT.getY()))) { return; }
        if (!(getYAxis() instanceof NumberAxis)) { return; }

        double            upperBound   = ((NumberAxis) getYAxis()).getUpperBound();
        double            lowerBound   = ((NumberAxis) getYAxis()).getLowerBound();
        double            range        = upperBound - lowerBound;
        List<PathElement> elements     = PATH.getElements();
        int               noOfElements = PATH.getElements().size();
        Bounds            pathBounds   = PATH.getLayoutBounds();
        double            pathMinX     = pathBounds.getMinX();
        double            pathWidth    = pathBounds.getWidth();
        double            factor       = range / getYAxis().getLayoutBounds().getHeight();
        PathElement       lastElement  = elements.get(0);

        Series<T, S> series = getData().get(0);
        if (series.getData().isEmpty()) { return; }

        if (isSnapToTicks()) {
            double     reverseFactor    = CHART_HEIGHT / range;
            int        noOfDataElements = series.getData().size();
            double     interval         = pathWidth / (double) (noOfDataElements - 1);
            int        selectedIndex    = Helper.roundDoubleToInt((EVENT_X - pathMinX) / interval);
            Data<T, S> selectedData     = series.getData().get(selectedIndex);
            S          selectedYValue   = selectedData.getYValue();

            if (!(selectedYValue instanceof Number)) { return; }
            double selectedValue = ((Number) selectedYValue).doubleValue();

            selector.setCenterX(pathMinX + CHART_X + interval * selectedIndex);
            selector.setCenterY((CHART_MIN_Y + CHART_HEIGHT) - (selectedValue * reverseFactor));
            selector.setVisible(true);
            fadeInFadeOut.playFrom(Duration.millis(0));

            Point2D tooltipLocation = selector.localToScreen(selector.getCenterX(), selector.getCenterY());
            String  tooltipText     = new StringBuilder(selectedData.getXValue().toString()).append("\n").append(selectedData.getYValue()).toString();
            selectorTooltip.setText(tooltipText);
            selectorTooltip.setX(tooltipLocation.getX());
            selectorTooltip.setY(tooltipLocation.getY());
            selectorTooltip.show(getScene().getWindow());

            fireEvent(new SmoothedChartEvent(SmoothedChart.this, null, SmoothedChartEvent.DATA_SELECTED, selectedValue));
        } else {
            for (int i = 1; i < noOfElements; i++) {
                PathElement element = elements.get(i);

                double[] xy  = getXYFromPathElement(lastElement);
                double[] xy1 = getXYFromPathElement(element);
                if (xy[0] < 0 || xy[1] < 0 || xy1[0] < 0 || xy1[1] < 0) { continue; }

                if (EVENT_X > xy[0] && EVENT_X < xy1[0]) {
                    double deltaX        = xy1[0] - xy[0];
                    double deltaY        = xy1[1] - xy[1];
                    double m             = deltaY / deltaX;
                    double y             = m * (EVT.getX() - xy[0]) + xy[1];
                    double selectedValue = ((getYAxis().getLayoutBounds().getHeight() - y) * factor + lowerBound);

                    selector.setCenterX(CHART_X + EVT.getX());
                    selector.setCenterY(CHART_MIN_Y + y);
                    selector.setVisible(true);
                    fadeInFadeOut.playFrom(Duration.millis(0));

                    Point2D tooltipLocation = selector.localToScreen(selector.getCenterX(), selector.getCenterY());
                    String  tooltipText     = new StringBuilder(String.format(Locale.US, formatString, selectedValue)).toString();
                    selectorTooltip.setText(tooltipText);
                    selectorTooltip.setX(tooltipLocation.getX());
                    selectorTooltip.setY(tooltipLocation.getY());
                    selectorTooltip.show(getScene().getWindow());

                    fireEvent(new SmoothedChartEvent(SmoothedChart.this, null, SmoothedChartEvent.DATA_SELECTED, selectedValue));
                    break;
                }
                lastElement = element;
            }
        }
    }

    private void smooth(ObservableList<PathElement> strokeElements, ObservableList<PathElement> fillElements, final double HEIGHT) {
        if (fillElements.isEmpty()) return;
        // as we do not have direct access to the data, first recreate the list of all the data points we have
        final Point[] dataPoints = new Point[strokeElements.size()];
        for (int i = 0; i < strokeElements.size(); i++) {
            final PathElement element = strokeElements.get(i);
            if (element instanceof MoveTo) {
                final MoveTo move = (MoveTo) element;
                dataPoints[i] = new Point(move.getX(), move.getY());
            } else if (element instanceof LineTo) {
                final LineTo line = (LineTo) element;
                final double x = line.getX(), y = line.getY();
                dataPoints[i] = new Point(x, y);
            }
        }
        double firstX = dataPoints[0].getX();
        double lastX  = dataPoints[dataPoints.length - 1].getX();

        Point[] points = Helper.subdividePoints(dataPoints, getSubDivisions());

        fillElements.clear();
        fillElements.add(new MoveTo(firstX, HEIGHT));

        strokeElements.clear();
        strokeElements.add(new MoveTo(points[0].getX(), points[0].getY()));

        for (Point p : points) {
            if (Double.compare(p.getX(), firstX) >= 0) {
                fillElements.add(new LineTo(p.getX(), p.getY()));
                strokeElements.add(new LineTo(p.getX(), p.getY()));
            }
        }

        fillElements.add(new LineTo(lastX, HEIGHT));
        fillElements.add(new LineTo(0, HEIGHT));
        fillElements.add(new ClosePath());
    }

    private double[] getXYFromPathElement(final PathElement ELEMENT) {
        if (ELEMENT instanceof MoveTo) {
            return new double[]{ ((MoveTo) ELEMENT).getX(), ((MoveTo) ELEMENT).getY() };
        } else if (ELEMENT instanceof LineTo) {
            return new double[] { ((LineTo) ELEMENT).getX(), ((LineTo) ELEMENT).getY() };
        } else {
            return new double[] { -1, -1 };
        }
    }
}
