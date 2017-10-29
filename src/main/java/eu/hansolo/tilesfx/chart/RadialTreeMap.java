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

import apple.laf.JRSUIUtils.Tree;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.TreeNode;
import javafx.beans.DefaultProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.collections.ObservableList;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static eu.hansolo.tilesfx.tools.Helper.clamp;


/**
 * User: hansolo
 * Date: 29.10.17
 * Time: 09:50
 */
@DefaultProperty("children")
public class RadialTreeMap extends Region {
    private static final double                PREFERRED_WIDTH  = 250;
    private static final double                PREFERRED_HEIGHT = 250;
    private static final double                MINIMUM_WIDTH    = 50;
    private static final double                MINIMUM_HEIGHT   = 50;
    private static final double                MAXIMUM_WIDTH    = 1024;
    private static final double                MAXIMUM_HEIGHT   = 1024;
    private              double                size;
    private              double                width;
    private              double                height;
    private              Canvas                chartCanvas;
    private              GraphicsContext       chartCtx;
    private              Pane                  pane;
    private              Paint                 backgroundPaint;
    private              Paint                 borderPaint;
    private              double                borderWidth;
    private              TreeNode<ChartData>   tree;
    private              boolean               _valueVisible;
    private              BooleanProperty       valueVisible;
    private              Color                 _backgroundColor;
    private              ObjectProperty<Color> backgroundColor;
    private              Color                 _textColor;
    private              ObjectProperty<Color> textColor;



    // ******************** Constructors **************************************
    public RadialTreeMap() {
        this(new TreeNode<>(new ChartData()));
    }
    public RadialTreeMap(final TreeNode<ChartData> TREE) {
        backgroundPaint = Color.TRANSPARENT;
        borderPaint     = Color.TRANSPARENT;
        borderWidth     = 0d;
        tree            = TREE;
        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void initGraphics() {
        if (Double.compare(getPrefWidth(), 0.0) <= 0 || Double.compare(getPrefHeight(), 0.0) <= 0 || Double.compare(getWidth(), 0.0) <= 0 ||
            Double.compare(getHeight(), 0.0) <= 0) {
            if (getPrefWidth() > 0 && getPrefHeight() > 0) {
                setPrefSize(getPrefWidth(), getPrefHeight());
            } else {
                setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            }
        }

        chartCanvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        chartCtx    = chartCanvas.getGraphicsContext2D();

        pane = new Pane(chartCanvas);
        pane.setBackground(new Background(new BackgroundFill(backgroundPaint, CornerRadii.EMPTY, Insets.EMPTY)));
        pane.setBorder(new Border(new BorderStroke(borderPaint, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(borderWidth))));

        getChildren().setAll(pane);

        prepareData();
    }

    private void registerListeners() {
        widthProperty().addListener(o -> resize());
        heightProperty().addListener(o -> resize());
        // add listeners to your propertes like
        //value.addListener(o -> handleControlPropertyChanged("VALUE"));
    }


    // ******************** Methods *******************************************
    @Override public void layoutChildren() {
        super.layoutChildren();
    }

    @Override protected double computeMinWidth(final double HEIGHT) { return MINIMUM_WIDTH; }
    @Override protected double computeMinHeight(final double WIDTH) { return MINIMUM_HEIGHT; }
    @Override protected double computePrefWidth(final double HEIGHT) { return super.computePrefWidth(HEIGHT); }
    @Override protected double computePrefHeight(final double WIDTH) { return super.computePrefHeight(WIDTH); }
    @Override protected double computeMaxWidth(final double HEIGHT) { return MAXIMUM_WIDTH; }
    @Override protected double computeMaxHeight(final double WIDTH) { return MAXIMUM_HEIGHT; }

    private void handleControlPropertyChanged(final String PROPERTY) {
        if ("".equals(PROPERTY)) {

        }
    }

    @Override public ObservableList<Node> getChildren() { return super.getChildren(); }

    public boolean isValueVisible() { return null == valueVisible ? _valueVisible : valueVisible.get(); }
    public void setValueVisible(final boolean VISIBLE) {
        if (null == valueVisible) {
            _valueVisible = VISIBLE;
            redraw();
        } else {
            valueVisible.set(VISIBLE);
        }
    }
    public BooleanProperty valueVisibleProperty() {
        if (null == valueVisible) {
            valueVisible = new BooleanPropertyBase(_valueVisible) {
                @Override protected void invalidated() { redraw(); }
                @Override public Object getBean() { return RadialTreeMap.this; }
                @Override public String getName() { return "valueVisible"; }
            };
        }
        return valueVisible;
    }

    public Color getBackgroundColor() { return null == backgroundColor ? _backgroundColor : backgroundColor.get(); }
    public void setBackgroundColor(final Color COLOR) {
        if (null == backgroundColor) {
            _backgroundColor = COLOR;
            redraw();
        } else {
            backgroundColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> backgroundColorProperty() {
        if (null == backgroundColor) {
            backgroundColor = new ObjectPropertyBase<Color>(_backgroundColor) {
                @Override protected void invalidated() { redraw(); }
                @Override public Object getBean() { return RadialTreeMap.this; }
                @Override public String getName() { return "backgroundColor"; }
            };
            _backgroundColor = null;
        }
        return backgroundColor;
    }

    public Color getTextColor() { return null == textColor ? _textColor : textColor.get(); }
    public void setTextColor(final Color COLOR) {
        if (null == textColor) {
            _textColor = COLOR;
            redraw();
        } else {
            textColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> textColorProperty() {
        if (null == textColor) {
            textColor = new ObjectPropertyBase<Color>(_textColor) {
                @Override protected void invalidated() { redraw(); }
                @Override public Object getBean() { return RadialTreeMap.this; }
                @Override public String getName() { return "textColor"; }
            };
            _textColor = null;
        }
        return textColor;
    }

    public void setTree(final TreeNode<ChartData> TREE) {
        tree = TREE;
        prepareData();
    }

    private void prepareData() {
        TreeNode<ChartData> root     = tree.getRoot();
        int                 maxLevel = root.getMaxLevel();

        Map<Integer, List<TreeNode<ChartData>>> levelMap = new HashMap<>(maxLevel);
        for (int i = 0 ; i <= maxLevel ; i++) { levelMap.put(i, new ArrayList<>()); }
        root.stream().forEach(node -> levelMap.get(node.getLevel()).add(node));

        for (int level = 1 ; level <= maxLevel ; level++) {
            List<TreeNode<ChartData>> treeNodeList = levelMap.get(level);

            List<TreeNode<ChartData>> nodesToFill = new ArrayList<>();
            for (TreeNode<ChartData> node : treeNodeList) {
                if (node.getChildren().isEmpty() && level < maxLevel) { nodesToFill.add(node); }
            }
            nodesToFill.forEach(node -> {
                node.addNode(new TreeNode<>(new ChartData("", 0, Color.TRANSPARENT), node));
            });
        }
        drawChart();
    }

    private void drawChart() {
        TreeNode<ChartData>                     root     = tree.getRoot();
        int                                     maxLevel = root.getMaxLevel();

        // Create map of all nodes per level
        Map<Integer, List<TreeNode<ChartData>>> levelMap = new HashMap<>(maxLevel);
        for (int i = 0 ; i <= maxLevel ; i++) { levelMap.put(i, new ArrayList<>()); }
        root.stream().forEach(node -> levelMap.get(node.getLevel()).add(node));

        double canvasSize     = chartCanvas.getWidth();
        double chartWidthStep = canvasSize * 0.8 / maxLevel;
        double barWidth       = chartWidthStep * 0.49;
        double center         = canvasSize * 0.5;
        Color  bkgColor       = getBackgroundColor();
        Color  textColor      = getTextColor();

        chartCtx.clearRect(0, 0, canvasSize, canvasSize);

        chartCtx.setFont(Fonts.latoRegular(barWidth * 0.45));
        chartCtx.setTextBaseline(VPos.CENTER);
        chartCtx.setTextAlign(TextAlignment.CENTER);

        for (int level = 1 ; level <= maxLevel ; level++) {
            List<TreeNode<ChartData>> treeNodeList = levelMap.get(level);

            int    oldNoOfItems    = levelMap.get(level - 1).size();
            double angleRange      = 360.0 / oldNoOfItems;
            double xy              = center - chartWidthStep * level * 0.5;
            double wh              = chartWidthStep * level;
            double outerRadiusStep = canvasSize * 0.4 / maxLevel;
            double outerRadius     = outerRadiusStep * level;

            chartCtx.setLineCap(StrokeLineCap.BUTT);
            chartCtx.setFill(textColor);

            double startAngle;
            double endAngle = 0;
            for (TreeNode<ChartData> node : treeNodeList) {
                ChartData data = node.getData();
                int    parentNoOfItems = node.getParent().getChildren().size();
                double angle           = angleRange / parentNoOfItems;
                startAngle = 90 + endAngle;
                endAngle  -= angle;

                // Segment Fill
                chartCtx.setLineWidth(barWidth);
                chartCtx.setStroke(data.getFillColor());
                chartCtx.strokeArc(xy, xy, wh, wh, startAngle, -angle, ArcType.OPEN);

                // Segment Stroke
                double innerR = wh * 0.5 - barWidth * 0.5;
                double outerR = canvasSize * 0.5;

                double radStart = Math.toRadians(startAngle);
                double cosStart = Math.cos(radStart);
                double sinStart = Math.sin(radStart);

                double x1       = center + innerR * cosStart;
                double y1       = center - innerR * sinStart;

                double x2       = center + outerR * cosStart;
                double y2       = center - outerR * sinStart;

                chartCtx.setLineWidth(chartWidthStep * 0.01);
                chartCtx.setStroke(Color.WHITE);
                chartCtx.strokeLine(x1, y1, x2, y2);

                // Value
                if (!Color.TRANSPARENT.equals(data.getFillColor())) {
                    double radValueText = Math.toRadians(startAngle - (angle * 0.5));
                    double cosValue     = Math.cos(radValueText);
                    double sinValue     = Math.sin(radValueText);

                    double x = outerRadius * cosValue;
                    double y = -outerRadius * sinValue;
                    chartCtx.setFill(textColor);
                    chartCtx.fillText(String.format(Locale.US, "%.0f", data.getValue()), center + x, center + y, barWidth);
                }
            }
        }
    }


    // ******************** Resizing ******************************************
    private void resize() {
        width  = getWidth() - getInsets().getLeft() - getInsets().getRight();
        height = getHeight() - getInsets().getTop() - getInsets().getBottom();
        size   = width < height ? width : height;

        if (width > 0 && height > 0) {
            pane.setMaxSize(size, size);
            pane.setPrefSize(size, size);
            pane.relocate((getWidth() - size) * 0.5, (getHeight() - size) * 0.5);

            chartCanvas.setWidth(size);
            chartCanvas.setHeight(size);

            redraw();
        }
    }

    private void redraw() {
        pane.setBackground(new Background(new BackgroundFill(backgroundPaint, CornerRadii.EMPTY, Insets.EMPTY)));
        pane.setBorder(new Border(new BorderStroke(borderPaint, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(borderWidth / PREFERRED_WIDTH * size))));
        drawChart();
    }
}
