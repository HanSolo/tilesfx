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

package eu.hansolo.tilesfx.skins;

import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.chart.ChartData;
import eu.hansolo.tilesfx.events.ChartDataEventListener;
import eu.hansolo.tilesfx.events.TileEvent;
import eu.hansolo.tilesfx.events.TileEvent.EventType;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.Helper;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.util.Pair;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;


/**
 * Created by hansolo on 09.06.17.
 */
public class SmoothAreaChartTileSkin extends TileSkin {
    private Text                          titleText;
    private Text                          valueText;
    private Text                          unitText;
    private TextFlow                      valueUnitFlow;
    private int                           dataSize;
    private double                        maxValue;
    private Path                          fillPath;
    private Path                          strokePath;
    private double                        hStepSize;
    private double                        vStepSize;
    private Rectangle                     fillClip;
    private Rectangle                     strokeClip;
    private ChartDataEventListener        chartEventListener;
    private ListChangeListener<ChartData> chartDataListener;
    private EventHandler<MouseEvent>      clickHandler;


    // ******************** Constructors **************************************
    public SmoothAreaChartTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        chartEventListener = e -> handleData();
        chartDataListener  = c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach(addedItem -> addedItem.addChartDataEventListener(chartEventListener));
                } else if (c.wasRemoved()) {
                    c.getRemoved().forEach(removedItem -> removedItem.removeChartDataEventListener(chartEventListener));
                }
            }
            handleData();
        };
        clickHandler       = e -> selectData(e);

        titleText = new Text();
        titleText.setFill(tile.getTitleColor());
        Helper.enableNode(titleText, !tile.getTitle().isEmpty());

        fillClip   = new Rectangle(0, 0, PREFERRED_HEIGHT, PREFERRED_HEIGHT);
        strokeClip = new Rectangle(0, 0, PREFERRED_HEIGHT, PREFERRED_HEIGHT);

        fillPath = new Path();
        fillPath.setStroke(null);
        fillPath.setClip(fillClip);

        strokePath = new Path();
        strokePath.setFill(null);
        strokePath.setStroke(tile.getBarColor());
        strokePath.setClip(strokeClip);
        strokePath.setMouseTransparent(true);

        valueText = new Text(String.format(locale, formatString, ((tile.getValue() - minValue) / range * 100)));
        valueText.setFill(tile.getValueColor());
        valueText.setTextOrigin(VPos.BASELINE);
        Helper.enableNode(valueText, tile.isValueVisible());

        unitText = new Text(" " + tile.getUnit());
        unitText.setFill(tile.getUnitColor());
        unitText.setTextOrigin(VPos.BASELINE);
        Helper.enableNode(unitText, !tile.getUnit().isEmpty());

        valueUnitFlow = new TextFlow(valueText, unitText);
        valueUnitFlow.setTextAlignment(TextAlignment.RIGHT);

        handleData();

        getPane().getChildren().addAll(titleText, fillPath, strokePath, valueUnitFlow);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        tile.getChartData().forEach(chartData -> chartData.addChartDataEventListener(chartEventListener));
        tile.getChartData().addListener(chartDataListener);
        fillPath.addEventHandler(MouseEvent.MOUSE_PRESSED, clickHandler);
    }

    @Override public void dispose() {
        tile.getChartData().removeListener(chartDataListener);
        tile.getChartData().forEach(chartData -> chartData.removeChartDataEventListener(chartEventListener));
        fillPath.removeEventHandler(MouseEvent.MOUSE_PRESSED, clickHandler);
        super.dispose();
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !tile.getTitle().isEmpty());
            Helper.enableNode(valueText, tile.isValueVisible());
            Helper.enableNode(unitText, !tile.getUnit().isEmpty());
        }
    }

    @Override protected void handleCurrentValue(final double VALUE) {
        valueText.setText(String.format(locale, formatString, VALUE));
        resizeDynamicText();
    }

    private void handleData() {
        List<ChartData> data = tile.getChartData();
        if (null == data || data.isEmpty()) { return; }
        Optional<ChartData> lastDataEntry = data.stream().reduce((first, second) -> second);
        if (lastDataEntry.isPresent()) { tile.setValue(lastDataEntry.get().getValue()); }
        dataSize  = data.size();
        maxValue  = data.stream().max(Comparator.comparing(c -> c.getValue())).get().getValue();
        hStepSize = width / dataSize;
        vStepSize = (height * 0.5) / maxValue;
        fillPath.getElements().clear();
        fillPath.getElements().add(new MoveTo(0, height));
        strokePath.getElements().clear();
        strokePath.getElements().add(new MoveTo(0, height - data.get(0).getValue() * vStepSize));
        for (int i = 0 ; i < dataSize ; i++) {
            fillPath.getElements().add(new LineTo((i + 1) * hStepSize, height - data.get(i).getValue() * vStepSize));
            strokePath.getElements().add(new LineTo((i + 1) * hStepSize, height - data.get(i).getValue() * vStepSize));
        }
        fillPath.getElements().add(new LineTo(width, height));
        fillPath.getElements().add(new ClosePath());
        smooth(strokePath.getElements(), fillPath.getElements());
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
        for (int i = 2; i < dataPoints.length; i++) {
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

    private void selectData(final MouseEvent EVT) {
        final double EVENT_X     = EVT.getX();
        final double CHART_X     = 0;
        final double CHART_WIDTH = width;

        if (Double.compare(EVENT_X, CHART_X) < 0 || Double.compare(EVENT_X, CHART_WIDTH) > 0) { return; }

        double lowerBound = tile.getChartData().stream().min(Comparator.comparing(ChartData::getValue)).get().getValue();
        double upperBound = tile.getChartData().stream().max(Comparator.comparing(ChartData::getValue)).get().getValue();
        double range      = upperBound - lowerBound;
        double factor     = range / (height * 0.5);

        double x0 = 0;
        double y0 = 0;
        double x1 = 0;
        double y1 = 0;
        double x2 = 0;
        double y2 = 0;
        double x3 = 0;
        double y3 = 0;
        double x = EVT.getX(); // x coordinate of selected point
        for (PathElement element : strokePath.getElements()) {
            if (element instanceof MoveTo) {
                final MoveTo moveTo = (MoveTo) element;
                x0 = moveTo.getX();
                y0 = moveTo.getY();
            } else if (element instanceof CubicCurveTo) {
                final CubicCurveTo cubicCurveTo = (CubicCurveTo) element;
                x1 = cubicCurveTo.getControlX1();
                y1 = cubicCurveTo.getControlY1();
                x2 = cubicCurveTo.getControlX2();
                y2 = cubicCurveTo.getControlY2();
                x3 = cubicCurveTo.getX();
                y3 = cubicCurveTo.getY();

                if (x > x0 && x < x3) break;

                x0 = cubicCurveTo.getX();
                y0 = cubicCurveTo.getY();
            }
        }

        //double cx = 3.0 * (x1 - x0);
        //double bx = 3.0 * (x2 - x1) - cx;
        //double ax = x3 - x0 - cx - bx;
        double cy = 3.0 * (y1 - y0);
        double by = 3.0 * (y2 - y1) - cy;
        double ay = y3 - y0 - cy - by;

        double a0 = x0;
        double a1 = 3.0 * (x1 - x0);
        double a2 = 3.0 * (x2 - 2.0 * x1 + x0);
        double a3 = x3 - 3.0 * x2 + 3.0 * x1 - x0;
        double t  = invB3P(a0, a1, a2, a3, x);
        double yt = ay * t * t * t + by * t * t + cy * t + y0;

        double selectedValue = upperBound - (yt - (height * 0.5)) * factor;
        tile.fireTileEvent(new TileEvent(EventType.SELECTED_CHART_DATA, new ChartData(selectedValue)));
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

        c  = (1.0 + p);        /* Check for p being too near to zero     */
        if (Double.compare(c, 1.0) == 0) {
            c = 1.0 + q;      /* Check for q being too near to zero     */
            if (Double.compare(c, 1.0)  == 0) { return( (float)(-h) ); }

            t = -Math.exp(Math.log(Math.abs(q)) / 3.0);
            if (q < 0.0) {
                t = -t;
            }
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
                        t = Helper.clamp(-0.00005, 1.00005, t);
                    }
                }
            } else {
                t = R * Math.cosh(Math.log(S + Math.sqrt((S + 1.0) * (S - 1.0))) / 3.0) - h;  /* arcosh */
            }
        } else {
            t = R * Math.sinh(Math.log(S + Math.sqrt(S * S + 1.0)) / 3.0) - h;                /* arsinh */
        }

        return t;
    }


    // ******************** Resizing ******************************************
    @Override protected void resizeDynamicText() {
        double maxWidth = unitText.isVisible() ? width - size * 0.275 : width - size * 0.1;
        double fontSize = size * 0.24;
        valueText.setFont(Fonts.latoRegular(fontSize));
        if (valueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(valueText, maxWidth, fontSize); }
    }
    @Override protected void resizeStaticText() {
        double maxWidth = width - size * 0.1;
        double fontSize = size * textSize.factor;

        titleText.setFont(Fonts.latoRegular(fontSize));
        if (titleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(titleText, maxWidth, fontSize); }

        switch(tile.getTitleAlignment()) {
            default    :
            case LEFT  : titleText.relocate(size * 0.05, size * 0.05); break;
            case CENTER: titleText.relocate((width - titleText.getLayoutBounds().getWidth()) * 0.5, size * 0.05); break;
            case RIGHT : titleText.relocate(width - (size * 0.05) - titleText.getLayoutBounds().getWidth(), size * 0.05); break;
        }
    }

    @Override protected void resize() {
        super.resize();

        valueUnitFlow.setPrefWidth(width - size * 0.1);
        valueUnitFlow.relocate(size * 0.05, size * 0.15);

        hStepSize = width / dataSize;
        vStepSize = (height * 0.5) / maxValue;

        handleData();
        strokePath.setStrokeWidth(size * 0.02);

        double cornerRadius = tile.getRoundedCorners() ? size * 0.05 : 0;

        fillClip.setX(0);
        fillClip.setY(0);
        fillClip.setWidth(tile.getWidth());
        fillClip.setHeight(tile.getHeight());
        fillClip.setArcWidth(cornerRadius);
        fillClip.setArcHeight(cornerRadius);

        strokeClip.setX(0);
        strokeClip.setY(0);
        strokeClip.setWidth(tile.getWidth());
        strokeClip.setHeight(tile.getHeight());
        strokeClip.setArcWidth(cornerRadius);
        strokeClip.setArcHeight(cornerRadius);
    }

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(tile.getTitle());

        valueText.setText(String.format(locale, formatString, tile.getCurrentValue()));
        unitText.setText(tile.getUnit());

        resizeDynamicText();
        resizeStaticText();

        titleText.setFill(tile.getTitleColor());
        valueText.setFill(tile.getValueColor());
        unitText.setFill(tile.getUnitColor());
        Color fillPathColor1 = Helper.getColorWithOpacity(tile.getBarColor(), 0.7);
        Color fillPathColor2 = Helper.getColorWithOpacity(tile.getBarColor(), 0.1);
        fillPath.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                                            new Stop(0, fillPathColor1),
                                            new Stop(1, fillPathColor2)));
        strokePath.setStroke(tile.getBarColor());
    }
}