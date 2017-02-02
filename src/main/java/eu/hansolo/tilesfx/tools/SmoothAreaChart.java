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

package eu.hansolo.tilesfx.tools;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.NamedArg;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.IntegerPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.StyleablePropertyFactory;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.Axis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import javafx.util.Pair;

import java.util.Locale;


/**
 * Created by hansolo on 04.01.17.
 */
public class SmoothAreaChart<X, Y> extends AreaChart<X, Y> {
    private static final StyleablePropertyFactory<SmoothAreaChart> FACTORY              = new StyleablePropertyFactory<>(Region.getClassCssMetaData());
    private static final CssMetaData<SmoothAreaChart, Color>       SELECTOR_COLOR       = FACTORY.createColorCssMetaData("-selector-color", s -> s.selectorColor, Color.web("#2468ea"), false);
    private static final CssMetaData<SmoothAreaChart, Color>       SELECTOR_CIRCLE_FILL = FACTORY.createColorCssMetaData("-selector-circle-fill", s -> s.selectorCircleFill, Color.TRANSPARENT, false);
    private        final StyleableProperty<Color> selectorColor;
    private        final StyleableProperty<Color> selectorCircleFill;
    private              BooleanProperty          selectorEnabled;
    private              DoubleProperty           selectedValue;
    private              IntegerProperty          selectedValueDecimals;
    private              BooleanProperty          areaVisible;
    private              String                   valueFormatString;
    private              double                   lowerBound;
    private              double                   upperBound;
    private              double                   range;
    private              EventHandler<MouseEvent> mousePressHandler;
    private              EventHandler<MouseEvent> mouseReleaseHandler;
    private              Timeline                 timeline;

    private Region chartPlotBackground;
    private Line   selector;
    private Circle selectorCircle;
    private Text   selectedValueText;


    // ******************** Constructors **************************************
    public SmoothAreaChart(final @NamedArg("xAxis") Axis<X> X_AXIS, @NamedArg("yAxis") Axis<Y> Y_AXIS, final @NamedArg("data") ObservableList<Series<X,Y>> DATA) {
        super(X_AXIS, Y_AXIS, DATA);

        selectorColor             = new StyleableObjectProperty<Color>(SELECTOR_COLOR.getInitialValue(SmoothAreaChart.this)) {
            @Override protected void invalidated() {
                selector.setStroke(get());
                selectorCircle.setStroke(get());
                selectedValueText.setFill(get());
            }
            @Override public Object getBean() { return SmoothAreaChart.this; }
            @Override public String getName() { return "selectorColor"; }
            @Override public CssMetaData<? extends Styleable, Color> getCssMetaData() { return SELECTOR_COLOR; }
        };
        selectorCircleFill        = new StyleableObjectProperty<Color>(SELECTOR_CIRCLE_FILL.getInitialValue(SmoothAreaChart.this)) {
            @Override protected void invalidated() { selectorCircle.setFill(get()); }
            @Override public Object getBean() { return SmoothAreaChart.this; }
            @Override public String getName() { return "selectorCircleFill"; }
            @Override public CssMetaData<? extends Styleable, Color> getCssMetaData() { return SELECTOR_COLOR; }
        };
        selectorEnabled           = new BooleanPropertyBase(false) {
            @Override protected void invalidated() {
                if(get()) {
                    setOnMousePressed(mousePressHandler);
                    setOnMouseReleased(mouseReleaseHandler);
                } else {
                    removeEventHandler(MouseEvent.MOUSE_PRESSED, mousePressHandler);
                    removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleaseHandler);
                }
            }
            @Override public Object getBean() { return SmoothAreaChart.this; }
            @Override public String getName() { return "selectorEnabled"; }
        };
        selectedValue             = new DoublePropertyBase(0) {
            @Override public Object getBean() { return SmoothAreaChart.this; }
            @Override public String getName() { return "selectedValue"; }
        };
        selectedValueDecimals     = new IntegerPropertyBase(0) {
            @Override protected void invalidated() {
                set(clamp(0, 3, get()));
                valueFormatString = new StringBuilder("%.").append(get()).append("f").toString();
            }
            @Override public Object getBean() { return SmoothAreaChart.this; }
            @Override public String getName() { return "selectedValueDecimals"; }
        };
        areaVisible               = new BooleanPropertyBase(true) {
            @Override protected void invalidated() { layoutPlotChildren(); }
            @Override public Object getBean() { return SmoothAreaChart.this; }
            @Override public String getName() { return "areaVisible"; }
        };
        valueFormatString         = "%.0f";
        lowerBound                = ((NumberAxis) getYAxis()).getLowerBound();
        upperBound                = ((NumberAxis) getYAxis()).getUpperBound();
        range                     = upperBound - lowerBound;

        mousePressHandler         = evt -> selectData(evt);
        mouseReleaseHandler       = evt -> timeline.play();

        // Add additional nodes
        selector = new Line();
        selector.setStroke(getSelectorColor());
        selector.setOpacity(0);
        selectorCircle = new Circle(5);
        selectorCircle.setFill(null);
        selectorCircle.setStroke(getSelectorColor());
        selectorCircle.setOpacity(0);

        chartPlotBackground = getChartBackground();
        chartPlotBackground.widthProperty().addListener(o -> resizeSelector(chartPlotBackground));
        chartPlotBackground.heightProperty().addListener(o -> resizeSelector(chartPlotBackground));
        chartPlotBackground.layoutYProperty().addListener(o -> resizeSelector(chartPlotBackground));

        selectedValueText = new Text("");
        selectedValueText.setTextAlignment(TextAlignment.CENTER);
        selectedValueText.setFill(getSelectorColor());
        selectedValueText.setOpacity(0);
        selectedValueText.setFont(Font.font(12));

        getChartChildren().addAll(selector, selectorCircle, selectedValueText);

        initTimeline();

        setLegend(getLegend());
        setData(DATA);
    }
    public SmoothAreaChart(final @NamedArg("xAxis") Axis<X> X_AXIS, final @NamedArg("yAxis") Axis<Y> Y_AXIS) {
        this(X_AXIS, Y_AXIS, FXCollections.observableArrayList());
    }

    private void initTimeline() {
        KeyValue kvSelectorStart = new KeyValue(selector.opacityProperty(), 1, Interpolator.EASE_BOTH);
        KeyValue kvSelectorWait  = new KeyValue(selector.opacityProperty(), 1, Interpolator.EASE_BOTH);
        KeyValue kvSelectorStop  = new KeyValue(selector.opacityProperty(), 0, Interpolator.EASE_BOTH);

        KeyValue kvSelectorCircleStart  = new KeyValue(selectorCircle.opacityProperty(), 1, Interpolator.EASE_BOTH);
        KeyValue kvSelectorCircleWait   = new KeyValue(selectorCircle.opacityProperty(), 1, Interpolator.EASE_BOTH);
        KeyValue kvSelectorCircleStop   = new KeyValue(selectorCircle.opacityProperty(), 0, Interpolator.EASE_BOTH);

        KeyValue kvValueTextStart       = new KeyValue(selectedValueText.opacityProperty(), 1, Interpolator.EASE_BOTH);
        KeyValue kvValueTextWait        = new KeyValue(selectedValueText.opacityProperty(), 1, Interpolator.EASE_BOTH);
        KeyValue kvValueTextStop        = new KeyValue(selectedValueText.opacityProperty(), 0, Interpolator.EASE_BOTH);

        KeyFrame kfStart = new KeyFrame(Duration.ZERO, kvSelectorStart, kvSelectorCircleStart, kvValueTextStart);
        KeyFrame kfWait  = new KeyFrame(Duration.millis(2000), kvSelectorWait, kvSelectorCircleWait, kvValueTextWait);
        KeyFrame kfStop  = new KeyFrame(Duration.millis(2500), kvSelectorStop, kvSelectorCircleStop, kvValueTextStop);
        timeline                        = new Timeline(kfStart, kfWait, kfStop);
    }


    // ******************** Methods *******************************************
    public Region getChartBackground() {
        for (Node node : lookupAll(".chart-plot-background")) {
            if (node instanceof Region) { return (Region) node; }
        }
        //for (Node node : lookupAll(".chart-series-area-fill series0 default-color0")) {
        //    if (node instanceof Path) return (Path) node;
        //}
        return null;
    }

    public double getSelectedValue() { return selectedValue.get(); }
    public ReadOnlyDoubleProperty selectedValueProperty() { return selectedValue; }

    public Color getSelectorColor() { return selectorColor.getValue(); }
    public void setSelectorColor(final Color COLOR) { selectorColor.setValue(COLOR); }
    public ObjectProperty<Color> selectorColorProperty() { return (ObjectProperty<Color>) selectorColor; }

    public Color getSelectorCircleFill() { return selectorCircleFill.getValue(); }
    public void setSelectorCircleFill(final Color COLOR) { selectorCircleFill.setValue(COLOR); }
    public ObjectProperty<Color> selectorCircleFillProperty() { return (ObjectProperty<Color>) selectorCircleFill; }

    public boolean isSelectorEnabled() { return selectorEnabled.get(); }
    public void setSelectorEnabled(final boolean ENABLED) { selectorEnabled.set(ENABLED); }
    public BooleanProperty selectorEnabledProperty() { return selectorEnabled; }

    public int getSelectedValueDecimals() { return selectedValueDecimals.get(); }
    public void setSelectedValueDecimals(final int DECIMALS) { selectedValueDecimals.set(DECIMALS); }
    public IntegerProperty selectedValueDecimalsProperty() { return selectedValueDecimals; }

    public boolean isAreaVisible() { return areaVisible.get(); }
    public void setAreaVisible(final boolean VISIBLE) { areaVisible.set(VISIBLE); }
    public BooleanProperty areaVisibleProperty() { return areaVisible; }

    @Override protected void layoutPlotChildren() {
        super.layoutPlotChildren();
        for (int seriesIndex = 0; seriesIndex < getDataSize(); seriesIndex++) {
            final XYChart.Series<X, Y> series     = getData().get(seriesIndex);
            final Path                 seriesLine = (Path) ((Group) series.getNode()).getChildren().get(1);
            final Path                 fillPath   = (Path) ((Group) series.getNode()).getChildren().get(0);
            fillPath.setVisible(isAreaVisible());
            fillPath.setManaged(isAreaVisible());
            smooth(seriesLine.getElements(), fillPath.getElements());
        }
    }

    private int getDataSize() {
        final ObservableList<Series<X, Y>> data = getData();
        return (data != null) ? data.size() : 0;
    }

    private static void smooth(ObservableList<PathElement> strokeElements, ObservableList<PathElement> fillElements) {
        if (fillElements.isEmpty()) return;
        // as we do not have direct access to the data, first recreate the list of all the data points we have
        final Point2D[] dataPoints = new Point2D[strokeElements.size()];
        for (int i = 0; i < strokeElements.size(); i++) {
            final PathElement element = strokeElements.get(i);
            if (element instanceof MoveTo) {
                MoveTo move   = (MoveTo) element;
                dataPoints[i] = new Point2D(move.getX(), move.getY());
            } else if (element instanceof LineTo) {
                LineTo line   = (LineTo) element;
                dataPoints[i] = new Point2D(line.getX(), line.getY());
            }
        }
        // next we need to know the zero Y value
        final double zeroY = ((MoveTo) fillElements.get(0)).getY();
        // now clear and rebuild elements
        strokeElements.clear();
        fillElements.clear();
        Pair<Point2D[], Point2D[]> result              = calcCurveControlPoints(dataPoints);
        Point2D[]                  firstControlPoints  = result.getKey();
        Point2D[]                  secondControlPoints = result.getValue();
        // start both paths
        strokeElements.add(new MoveTo(dataPoints[0].getX(), dataPoints[0].getY()));
        fillElements.add(new MoveTo(dataPoints[0].getX(), zeroY));
        fillElements.add(new LineTo(dataPoints[0].getX(), dataPoints[0].getY()));
        // add curves
        for (int i = 1; i < dataPoints.length; i++) {
            final int ci = i - 1;
            strokeElements.add(new CubicCurveTo(
                firstControlPoints[ci].getX(), firstControlPoints[ci].getY(),
                secondControlPoints[ci].getX(), secondControlPoints[ci].getY(),
                dataPoints[i].getX(), dataPoints[i].getY()));
            fillElements.add(new CubicCurveTo(
                firstControlPoints[ci].getX(), firstControlPoints[ci].getY(),
                secondControlPoints[ci].getX(), secondControlPoints[ci].getY(),
                dataPoints[i].getX(), dataPoints[i].getY()));
        }
        // end the paths
        fillElements.add(new LineTo(dataPoints[dataPoints.length - 1].getX(), zeroY));
        fillElements.add(new ClosePath());
    }

    private double invB3P(double a0, double a1, double a2, double a3, double x) {
        double c;
        double h, p, q, D, R, S, F, t;
        double w1 = 2.0 * Math.PI / 3.0;
        double w2 = 4.0 * Math.PI / 3.0;

        c = 1.0 + a3;

        if (Double.compare(c, 1.0) == 0) { a3 = 1e-6; }
        h  = a2 / 3.0 / a3;
        p  = (3.0 * a1 * a3 - a2 * a2) / 3.0 / a3 / a3;
        q  = (2.0 * a2 * a2 * a2 - 9.0 * a1 * a2 * a3 - 27.0 * a3 * a3 * (x - a0)) / 27.0 / a3 / a3 / a3;
        c  = (1.0 + p);        /* Check for p being too near to zero */
        if (Double.compare(c, 1.0) == 0) {
            c = 1.0 + q;      /* Check for q being too near to zero */
            if (Double.compare(c, 1.0)  == 0) { return( (float)(-h) ); }

            t = -Math.exp(Math.log(Math.abs(q)) / 3.0);
            if (q < 0.0) { t = -t; }
            t -= h;
            return t;
        }

        R  = Math.sqrt(Math.abs(p) / 3.0);
        S  = Math.abs(q) / 2.0 / R / R / R;

        R  = -2.0 * R;
        if (q < 0.0) { R = -R; }

        if (p < 0.0) {
            D = p * p * p / 27.0 + q * q / 4.0;
            if (D <= 0.0) {
                F = Math.acos(S)/3.0;
                t = R * Math.cos(F + w2) - h;
                if ((t < -0.00005) || (t > 1.00005)) {
                    t = R * Math.cos(F + w1) - h;
                    if ((t < -0.00005) || (t > 1.00005)) {
                        t = R * Math.cos(F) - h;
                        t = clamp(-0.00005, 1.00005, t);
                    }
                }
            } else {
                t = R * Math.cosh(Math.log(S + Math.sqrt((S + 1.0) * (S - 1.0))) / 3.0) - h;  /* arcosh */
            }
        } else {
            t = R * Math.sinh(Math.log(S + Math.sqrt(S * S + 1.0)) / 3.0) - h;               /* arsinh */
        }
        return t;
    }

    /**
     * Calculate open-ended Bezier Spline Control Points.
     *
     * @param dataPoints Input data Bezier spline points.
     * @return The spline points
     */
    private static Pair<Point2D[], Point2D[]> calcCurveControlPoints(Point2D[] dataPoints) {
        Point2D[] firstControlPoints;
        Point2D[] secondControlPoints;
        int n = dataPoints.length - 1;
        if (n == 1) { // Special case: Bezier curve should be a straight line.
            firstControlPoints     = new Point2D[1];
            // 3P1 = 2P0 + P3
            firstControlPoints[0]  = new Point2D((2 * dataPoints[0].getX() + dataPoints[1].getX()) / 3, (2 * dataPoints[0].getY() + dataPoints[1].getY()) / 3);
            secondControlPoints    = new Point2D[1];
            // P2 = 2P1 – P0
            secondControlPoints[0] = new Point2D(2 * firstControlPoints[0].getX() - dataPoints[0].getX(), 2 * firstControlPoints[0].getY() - dataPoints[0].getY());
            return new Pair<>(firstControlPoints, secondControlPoints);
        }

        // Calculate first Bezier control points
        // Right hand side vector
        double[] rhs = new double[n];

        // Set right hand side X values
        for (int i = 1; i < n - 1; ++i) {
            rhs[i] = 4 * dataPoints[i].getX() + 2 * dataPoints[i + 1].getX();
        }
        rhs[0]     = dataPoints[0].getX() + 2 * dataPoints[1].getX();
        rhs[n - 1] = (8 * dataPoints[n - 1].getX() + dataPoints[n].getX()) / 2.0;
        // Get first control points X-values
        double[] x = getFirstControlPoints(rhs);

        // Set right hand side Y values
        for (int i = 1; i < n - 1; ++i) {
            rhs[i] = 4 * dataPoints[i].getY() + 2 * dataPoints[i + 1].getY();
        }
        rhs[0]     = dataPoints[0].getY() + 2 * dataPoints[1].getY();
        rhs[n - 1] = (8 * dataPoints[n - 1].getY() + dataPoints[n].getY()) / 2.0;
        // Get first control points Y-values
        double[] y = getFirstControlPoints(rhs);

        // Fill output arrays.
        firstControlPoints  = new Point2D[n];
        secondControlPoints = new Point2D[n];
        for (int i = 0; i < n; ++i) {
            // First control point
            firstControlPoints[i] = new Point2D(x[i], y[i]);
            // Second control point
            if (i < n - 1) {
                secondControlPoints[i] = new Point2D(2 * dataPoints[i + 1].getX() - x[i + 1], 2 * dataPoints[i + 1].getY() - y[i + 1]);
            } else {
                secondControlPoints[i] = new Point2D((dataPoints[n].getX() + x[n - 1]) / 2, (dataPoints[n].getY() + y[n - 1]) / 2);
            }
        }
        return new Pair<>(firstControlPoints, secondControlPoints);
    }

    /**
     * Solves a tridiagonal system for one of coordinates (x or y) of first
     * Bezier control points.
     *
     * @param rhs Right hand side vector.
     * @return Solution vector.
     */
    private static double[] getFirstControlPoints(double[] rhs) {
        int      n   = rhs.length;
        double[] x   = new double[n]; // Solution vector.
        double[] tmp = new double[n]; // Temp workspace.
        double   b   = 2.0;

        x[0] = rhs[0] / b;

        for (int i = 1; i < n; i++) {// Decomposition and forward substitution.
            tmp[i] = 1 / b;
            b      = (i < n - 1 ? 4.0 : 3.5) - tmp[i];
            x[i]   = (rhs[i] - x[i - 1]) / b;
        }
        for (int i = 1; i < n; i++) {
            x[n - i - 1] -= tmp[n - i] * x[n - i]; // Backsubstitution.
        }
        return x;
    }

    private void resizeSelector(final Region CHART_BACKGROUND) {
        selector.setLayoutX(CHART_BACKGROUND.getLayoutX());
        selector.setLayoutY(CHART_BACKGROUND.getLayoutY());
        selector.setStartY(CHART_BACKGROUND.getLayoutBounds().getMinY() + 5);
        selector.setEndY(CHART_BACKGROUND.getLayoutBounds().getMaxY());
        selectorCircle.setCenterX(CHART_BACKGROUND.getLayoutX());
    }

    private void selectData(final MouseEvent EVENT) {
        if (getData().isEmpty() ||
            getData().get(0).getData().isEmpty() ||
            getPlotChildren().isEmpty()) {
            return;
        }

        // Set Selector
        timeline.stop();
        final double chartX     = localToScene(chartPlotBackground.getBoundsInParent()).getMinX();
        final double chartY     = localToScene(chartPlotBackground.getBoundsInParent()).getMinY();
        final double chartWidth = getPlotChildren().get(0).getParent().getLayoutBounds().getWidth();
        final double insetLeft  = getInsets().getLeft();
        final double eventX     = EVENT.getSceneX() - chartX - insetLeft;
        final double eventY     = EVENT.getSceneY() - chartY;
        selector.setStartX(clamp(insetLeft, chartWidth, eventX));
        selector.setEndX(clamp(insetLeft, chartWidth, eventX));
        selectorCircle.setCenterX(selector.getBoundsInParent().getMinX() + selector.getStrokeWidth() / 2);

        // Select data
        lowerBound = ((NumberAxis) getYAxis()).getLowerBound();
        upperBound = ((NumberAxis) getYAxis()).getUpperBound();
        range      = upperBound - lowerBound;

        double factor       = range / getYAxis().getLayoutBounds().getHeight();
        double nearestValue = -1;
        double nearestYt    = -1;
        double distance     = Double.MAX_VALUE;
        for (Series<X, Y> series : getData()) {
            final Path seriesLine = (Path) ((Group) series.getNode()).getChildren().get(1);
            double     x0         = 0;
            double     y0         = 0;
            double     x1         = 0;
            double     y1         = 0;
            double     x2         = 0;
            double     y2         = 0;
            double     x3         = 0;
            double     y3         = 0;
            double     x          = selector.getStartX();
            for (PathElement element : seriesLine.getElements()) {
                if (element instanceof MoveTo) {
                    MoveTo moveTo = (MoveTo) element;
                    x0 = moveTo.getX();
                    y0 = moveTo.getY();
                } else if (element instanceof CubicCurveTo) {
                    CubicCurveTo cubicCurveTo = (CubicCurveTo) element;
                    x1 = cubicCurveTo.getControlX1();
                    y1 = cubicCurveTo.getControlY1();
                    x2 = cubicCurveTo.getControlX2();
                    y2 = cubicCurveTo.getControlY2();
                    x3 = cubicCurveTo.getX();
                    y3 = cubicCurveTo.getY();

                    if (x > x0 && x < x3) { break; }

                    x0 = cubicCurveTo.getX();
                    y0 = cubicCurveTo.getY();
                }
            }
            double cy = 3.0 * (y1 - y0);
            double by = 3.0 * (y2 - y1) - cy;
            double ay = y3 - y0 - cy - by;
            double a0 = x0;
            double a1 = 3.0 * (x1 - x0);
            double a2 = 3.0 * (x2 - 2.0 * x1 + x0);
            double a3 = x3 - 3.0 * x2 + 3.0 * x1 - x0;
            double t  = invB3P(a0, a1, a2, a3, x);
            double yt = ay * t * t * t + by * t * t + cy * t + y0;

            if (Math.abs(eventY - (yt + 10)) < distance) {
                distance     = Math.abs(eventY - (yt + 10));
                nearestValue = ((getYAxis().getLayoutBounds().getHeight() - yt) * factor + lowerBound);
                nearestYt    = yt + 10;
            }
        }

        selectedValue.set(nearestValue);
        selectorCircle.setCenterY(nearestYt);

        // Set Selector Text
        selectedValueText.setText(String.format(Locale.US, valueFormatString, getSelectedValue()));
        selectedValueText.setX(clamp(insetLeft, chartX + chartWidth, (selector.getBoundsInParent().getMinX()) - selectedValueText.getLayoutBounds().getWidth() * 0.5));
        selectedValueText.setY(selector.getStartY() + getInsets().getLeft());
        selectedValueText.setOpacity(1);
        selector.setOpacity(1);
        selectorCircle.setOpacity(1);
    }

    private double clamp(final double MIN, final double MAX, final double VALUE) {
        if (VALUE < MIN) return MIN;
        if (VALUE > MAX) return MAX;
        return VALUE;
    }
    private int clamp(final int MIN, final int MAX, final int VALUE) {
        if (VALUE < MIN) return MIN;
        if (VALUE > MAX) return MAX;
        return VALUE;
    }
}
