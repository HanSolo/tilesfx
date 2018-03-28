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
import eu.hansolo.tilesfx.Tile.ChartType;
import eu.hansolo.tilesfx.chart.ChartData;
import eu.hansolo.tilesfx.events.ChartDataEventListener;
import eu.hansolo.tilesfx.events.TileEvent;
import eu.hansolo.tilesfx.events.TileEvent.EventType;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.Helper;
import eu.hansolo.tilesfx.tools.Point;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import java.util.ArrayList;
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
    private List<Point>                   points;
    private Path                          fillPath;
    private Path                          strokePath;
    private Canvas                        canvas;
    private GraphicsContext               ctx;
    private boolean                       dataPointsVisible;
    private boolean                       smoothing;
    private double                        hStepSize;
    private double                        vStepSize;
    private Circle                        selector;
    private Tooltip                       selectorTooltip;
    private SequentialTransition          fadeInFadeOut;
    private Rectangle                     fillClip;
    private Rectangle                     strokeClip;
    private ChartDataEventListener        chartEventListener;
    private ListChangeListener<ChartData> chartDataListener;
    private EventHandler<MouseEvent>      clickHandler;
    private EventHandler<ActionEvent>     endOfTransformationHandler;


    // ******************** Constructors **************************************
    public SmoothAreaChartTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        chartEventListener         = e -> handleData();
        chartDataListener          = c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach(addedItem -> addedItem.addChartDataEventListener(chartEventListener));
                } else if (c.wasRemoved()) {
                    c.getRemoved().forEach(removedItem -> removedItem.removeChartDataEventListener(chartEventListener));
                }
            }
            handleData();
        };
        clickHandler               = e -> select(e);
        endOfTransformationHandler = e -> selectorTooltip.hide();

        smoothing = tile.isSmoothing();

        titleText = new Text();
        titleText.setFill(tile.getTitleColor());
        Helper.enableNode(titleText, !tile.getTitle().isEmpty());

        fillClip   = new Rectangle(0, 0, PREFERRED_WIDTH, PREFERRED_HEIGHT);
        strokeClip = new Rectangle(0, 0, PREFERRED_WIDTH, PREFERRED_HEIGHT);

        points = new ArrayList<>();

        fillPath = new Path();
        fillPath.setStroke(null);
        fillPath.setClip(fillClip);

        strokePath = new Path();
        strokePath.setFill(null);
        strokePath.setStroke(tile.getBarColor());
        strokePath.setClip(strokeClip);
        strokePath.setMouseTransparent(true);

        Helper.enableNode(fillPath, ChartType.AREA == tile.getChartType());

        canvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        canvas.setMouseTransparent(true);
        ctx    = canvas.getGraphicsContext2D();

        dataPointsVisible = tile.getDataPointsVisible();

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

        selector        = new Circle();
        selectorTooltip = new Tooltip("");
        selectorTooltip.setWidth(60);
        selectorTooltip.setHeight(48);
        Tooltip.install(selector, selectorTooltip);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(100), selector);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(100), selector);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        fadeInFadeOut = new SequentialTransition(fadeIn, new PauseTransition(Duration.millis(3000)), fadeOut);

        handleData();

        getPane().getChildren().addAll(titleText, fillPath, strokePath, canvas, valueUnitFlow, selector);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        tile.getChartData().forEach(chartData -> chartData.addChartDataEventListener(chartEventListener));
        tile.getChartData().addListener(chartDataListener);
        fillPath.addEventHandler(MouseEvent.MOUSE_PRESSED, clickHandler);
        fadeInFadeOut.setOnFinished(endOfTransformationHandler);
    }

    @Override public void dispose() {
        tile.getChartData().removeListener(chartDataListener);
        tile.getChartData().forEach(chartData -> chartData.removeChartDataEventListener(chartEventListener));
        fillPath.removeEventHandler(MouseEvent.MOUSE_PRESSED, clickHandler);
        endOfTransformationHandler = null;
        fadeInFadeOut.setOnFinished(null);
        super.dispose();
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !tile.getTitle().isEmpty());
            Helper.enableNode(valueText, tile.isValueVisible());
            Helper.enableNode(unitText, !tile.getUnit().isEmpty());
            dataPointsVisible = tile.getDataPointsVisible();
            if (dataPointsVisible) { drawChart(points); } else { ctx.clearRect(0, 0, width, height); }
        } else if ("SERIES".equals(EVENT_TYPE)) {
            Helper.enableNode(fillPath, ChartType.AREA == tile.getChartType());
        }
    }

    private void handleData() {
        selectorTooltip.hide();
        selector.setVisible(false);
        List<ChartData> data = tile.getChartData();
        if (null == data || data.isEmpty()) { return; }
        Optional<ChartData> lastDataEntry = data.stream().reduce((first, second) -> second);
        if (lastDataEntry.isPresent()) {
            valueText.setText(String.format(locale, formatString, lastDataEntry.get().getValue()));
            tile.setValue(lastDataEntry.get().getValue());
            resizeDynamicText();
        }
        dataSize  = data.size();
        maxValue  = data.stream().max(Comparator.comparing(c -> c.getValue())).get().getValue();
        hStepSize = width / (dataSize - 1);
        vStepSize = (height * 0.5) / maxValue;

        points.clear();
        for (int i = 0 ; i < dataSize ; i++) {
            points.add(new Point((i) * hStepSize, height - data.get(i).getValue() * vStepSize));
        }
        drawChart(points);
    }

    private void drawChart(final List<Point> POINTS) {
        if (POINTS.isEmpty()) return;
        Point[] points = smoothing ? Helper.subdividePoints(POINTS.toArray(new Point[0]), 8) : POINTS.toArray(new Point[0]);

        fillPath.getElements().clear();
        fillPath.getElements().add(new MoveTo(0, height));

        strokePath.getElements().clear();
        strokePath.getElements().add(new MoveTo(points[0].getX(), points[0].getY()));

        for (Point p : points) {
            fillPath.getElements().add(new LineTo(p.getX(), p.getY()));
            strokePath.getElements().add(new LineTo(p.getX(), p.getY()));
        }

        fillPath.getElements().add(new LineTo(width, height));
        fillPath.getElements().add(new LineTo(0, height));
        fillPath.getElements().add(new ClosePath());

        if (dataPointsVisible) { drawDataPoints(POINTS, tile.isFillWithGradient() ? tile.getGradientStops().get(0).getColor() : tile.getBarColor()); }
    }

    private void drawDataPoints(final List<Point> DATA, final Color COLOR) {
        if (DATA.isEmpty()) { return; }
        final double LOWER_BOUND_X = 0;
        final double LOWER_BOUND_Y = tile.getMinValue();
        ctx.clearRect(0, 0, width, height);
        for (Point point : DATA) {
            double x = (point.getX() - LOWER_BOUND_X);
            double y = (point.getY() - LOWER_BOUND_Y);
            drawDataPoint(x, y, COLOR);
        }
    }

    private void drawDataPoint(final double X, final double Y, final Color COLOR) {
        double borderSize     = size * 0.06;
        double symbolSize     = size * 0.04;
        double halfBorderSize = borderSize * 0.5;
        double halfSymbolSize = symbolSize * 0.5;
        ctx.save();
        ctx.setFill(tile.getBackgroundColor());
        ctx.fillOval(X -halfBorderSize, Y - halfBorderSize, borderSize, borderSize);
        ctx.setFill(COLOR);
        ctx.fillOval(X - halfSymbolSize, Y - halfSymbolSize, symbolSize, symbolSize);
        ctx.restore();
    }

    private void select(final MouseEvent EVT) {
        final double EVENT_X     = EVT.getX();
        final double CHART_X     = 0;
        final double CHART_WIDTH = width;

        if (Double.compare(EVENT_X, CHART_X) < 0 || Double.compare(EVENT_X, CHART_WIDTH) > 0) { return; }

        double            upperBound   = tile.getChartData().stream().max(Comparator.comparing(ChartData::getValue)).get().getValue();
        double            range        = upperBound - tile.getMinValue();
        double            factor       = range / (height * 0.5);
        List<PathElement> elements     = strokePath.getElements();
        int               noOfElements = elements.size();
        PathElement       lastElement  = elements.get(0);

        if (tile.isSnapToTicks()) {
            double    reverseFactor    = (height * 0.5) / range;
            int       noOfDataElements = tile.getChartData().size();
            double    interval         = width / (double) (noOfDataElements - 1);
            int       selectedIndex    = Helper.roundDoubleToInt(EVENT_X / interval);
            ChartData selectedData     = tile.getChartData().get(selectedIndex);
            double    selectedValue    = selectedData.getValue();

            selector.setCenterX(interval * selectedIndex);
            selector.setCenterY(height - selectedValue * reverseFactor);
            selector.setVisible(true);
            fadeInFadeOut.playFrom(Duration.millis(0));

            String tooltipText = new StringBuilder(selectedData.getName()).append("\n").append(String.format(locale, formatString, selectedValue)).toString();

            Point2D popupLocation = tile.localToScreen(selector.getCenterX() - selectorTooltip.getWidth() * 0.5, selector.getCenterY() - size * 0.025 - selectorTooltip.getHeight());
            selectorTooltip.setText(tooltipText);
            selectorTooltip.setX(popupLocation.getX());
            selectorTooltip.setY(popupLocation.getY());
            selectorTooltip.show(tile.getScene().getWindow());

            tile.fireTileEvent(new TileEvent(EventType.SELECTED_CHART_DATA, selectedData));
        } else {
            for (int i = 1; i < noOfElements; i++) {
                PathElement element = elements.get(i);

                double[] xy  = getXYFromPathElement(lastElement);
                double[] xy1 = getXYFromPathElement(element);

                if (EVENT_X > xy[0] && EVENT_X < xy1[0]) {
                    double deltaX        = xy1[0] - xy[0];
                    double deltaY        = xy1[1] - xy[1];
                    double m             = deltaY / deltaX;
                    double y             = m * (EVT.getX() - xy[0]) + xy[1];
                    double selectedValue = upperBound - (y - (height * 0.5)) * factor;

                    selector.setCenterX(EVT.getX());
                    selector.setCenterY(y);
                    selector.setVisible(true);
                    fadeInFadeOut.playFrom(Duration.millis(0));

                    Point2D popupLocation = tile.localToScreen(EVT.getX() - selectorTooltip.getWidth() * 0.5, selector.getCenterY() - size * 0.025 - selectorTooltip.getHeight());
                    selectorTooltip.setText(String.format(locale, formatString, selectedValue));
                    selectorTooltip.setX(popupLocation.getX());
                    selectorTooltip.setY(popupLocation.getY());
                    selectorTooltip.show(tile.getScene().getWindow());

                    tile.fireTileEvent(new TileEvent(EventType.SELECTED_CHART_DATA, new ChartData(selectedValue)));
                    break;
                }
                lastElement = element;
            }
        }
    }

    private double[] getXYFromPathElement(final PathElement ELEMENT) {
        if (ELEMENT instanceof MoveTo) {
            return new double[]{ ((MoveTo) ELEMENT).getX(), ((MoveTo) ELEMENT).getY() };
        } else {
            return new double[] { ((LineTo) ELEMENT).getX(), ((LineTo) ELEMENT).getY() };
        }
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

        boolean customFontEnabled = tile.isCustomFontEnabled();
        Font    customFont        = tile.getCustomFont();
        Font    font              = (customFontEnabled && customFont != null) ? Font.font(customFont.getFamily(), fontSize) : Fonts.latoRegular(fontSize);

        titleText.setFont(font);
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

        valueUnitFlow.setPrefWidth(contentBounds.getWidth());
        valueUnitFlow.relocate(contentBounds.getX(), contentBounds.getY());

        hStepSize = width / dataSize;
        vStepSize = (height * 0.5) / maxValue;

        selector.setRadius(size * 0.02);
        selector.setStrokeWidth(size * 0.01);

        handleData();
        strokePath.setStrokeWidth(size * 0.02);

        canvas.setWidth(width);
        canvas.setHeight(height);

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

        smoothing = tile.isSmoothing();

        titleText.setText(tile.getTitle());

        valueText.setText(String.format(locale, formatString, tile.getCurrentValue()));
        unitText.setText(tile.getUnit());

        resizeDynamicText();
        resizeStaticText();

        titleText.setFill(tile.getTitleColor());
        valueText.setFill(tile.getValueColor());
        unitText.setFill(tile.getUnitColor());
        selector.setStroke(tile.getForegroundColor());
        selector.setFill(tile.getBackgroundColor());
        Color fillPathColor1 = Helper.getColorWithOpacity(tile.getBarColor(), 0.7);
        Color fillPathColor2 = Helper.getColorWithOpacity(tile.getBarColor(), 0.1);
        if (tile.isFillWithGradient() && !tile.getGradientStops().isEmpty()) {
            fillPath.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, tile.getGradientStops()));
            strokePath.setStroke(tile.getGradientStops().get(0).getColor());
            if (dataPointsVisible) { drawDataPoints(points, tile.getGradientStops().get(0).getColor()); }
        } else {
            fillPath.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, new Stop(0, fillPathColor1), new Stop(1, fillPathColor2)));
            strokePath.setStroke(tile.getBarColor());
            if (dataPointsVisible) { drawDataPoints(points, tile.getBarColor()); }
        }
        drawChart(points);
    }
}