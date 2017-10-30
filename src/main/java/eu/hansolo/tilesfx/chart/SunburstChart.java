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

import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.TreeNode;
import javafx.beans.DefaultProperty;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.IntegerPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
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


/**
 * User: hansolo
 * Date: 29.10.17
 * Time: 09:50
 */
@DefaultProperty("children")
public class SunburstChart extends Region {
    public enum VisibleData     { NONE, NAME, VALUE, NAME_VALUE }
    public enum TextOrientation {
        HORIZONTAL(12),
        TANGENT(8),
        ORTHOGONAL(12);

        private double maxAngle;
        TextOrientation(final double MAX_ANGLE) {
            maxAngle = MAX_ANGLE;
        }

        public double getMaxAngle() { return maxAngle; }
    }

    private static final double                          PREFERRED_WIDTH  = 250;
    private static final double                          PREFERRED_HEIGHT = 250;
    private static final double                          MINIMUM_WIDTH    = 50;
    private static final double                          MINIMUM_HEIGHT   = 50;
    private static final double                          MAXIMUM_WIDTH    = 1024;
    private static final double                          MAXIMUM_HEIGHT   = 1024;
    private              double                          size;
    private              double                          width;
    private              double                          height;
    private              Canvas                          chartCanvas;
    private              GraphicsContext                 chartCtx;
    private              Pane                            pane;
    private              Paint                           backgroundPaint;
    private              Paint                           borderPaint;
    private              double                          borderWidth;
    private              VisibleData                     _visibleData;
    private              ObjectProperty<VisibleData>     visibleData;
    private              TextOrientation                 _textOrientation;
    private              ObjectProperty<TextOrientation> textOrientation;
    private              Color                           _backgroundColor;
    private              ObjectProperty<Color>           backgroundColor;
    private              Color                           _textColor;
    private              ObjectProperty<Color>           textColor;
    private              boolean                         _useColorFromParent;
    private              BooleanProperty                 useColorFromParent;
    private              int                             _decimals;
    private              IntegerProperty                 decimals;
    private              String                          formatString;
    private              TreeNode                        tree;
    private              TreeNode                        root;
    private              int                             maxLevel;
    private              Map<Integer, List<TreeNode>>    levelMap;
    private              InvalidationListener            sizeListener;



    // ******************** Constructors **************************************
    public SunburstChart() {
        this(new TreeNode(new ChartData()));
    }
    public SunburstChart(final TreeNode TREE) {
        backgroundPaint     = Color.TRANSPARENT;
        borderPaint         = Color.TRANSPARENT;
        borderWidth         = 0d;
        _visibleData        = VisibleData.NAME;
        _textOrientation    = TextOrientation.TANGENT;
        _backgroundColor    = Color.WHITE;
        _textColor          = Color.BLACK;
        _useColorFromParent = false;
        _decimals           = 0;
        formatString        = "%.0f";
        tree                = TREE;
        levelMap            = new HashMap<>(8);
        sizeListener        = o -> resize();
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
        widthProperty().addListener(sizeListener);
        heightProperty().addListener(sizeListener);
        tree.flattened().forEach(node -> node.setOnTreeNodeEvent(e -> redraw()));
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

    @Override public ObservableList<Node> getChildren() { return super.getChildren(); }

    public void dispose() {
        widthProperty().removeListener(sizeListener);
        heightProperty().removeListener(sizeListener);
        tree.removeAllTreeNodeEventListeners();
    }

    public VisibleData getVisibleData() { return null == visibleData ? _visibleData : visibleData.get(); }
    public void setVisibleData(final VisibleData VISIBLE_DATA) {
        if (null == visibleData) {
            _visibleData = VISIBLE_DATA;
            redraw();
        } else {
            visibleData.set(VISIBLE_DATA);
        }
    }
    public ObjectProperty<VisibleData> visibleDataProperty() {
        if (null == visibleData) {
            visibleData = new ObjectPropertyBase<VisibleData>(_visibleData) {
                @Override protected void invalidated() { redraw(); }
                @Override public Object getBean() { return SunburstChart.this; }
                @Override public String getName() { return "visibleData"; }
            };
            _visibleData = null;
        }
        return visibleData;
    }

    public TextOrientation getTextOrientation() { return null == textOrientation ? _textOrientation : textOrientation.get(); }
    public void setTextOrientation(final TextOrientation ORIENTATION) {
        if (null == textOrientation) {
            _textOrientation = ORIENTATION;
            redraw();
        } else {
            textOrientation.set(ORIENTATION);
        }
    }
    public ObjectProperty<TextOrientation> textOrientationProperty() {
        if (null == textOrientation) {
            textOrientation = new ObjectPropertyBase<TextOrientation>(_textOrientation) {
                @Override protected void invalidated() { redraw(); }
                @Override public Object getBean() { return SunburstChart.this; }
                @Override public String getName() { return "textOrientation"; }
            };
            _textOrientation = null;
        }
        return textOrientation;
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
                @Override public Object getBean() { return SunburstChart.this; }
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
                @Override public Object getBean() { return SunburstChart.this; }
                @Override public String getName() { return "textColor"; }
            };
            _textColor = null;
        }
        return textColor;
    }

    public boolean getUseColorFromParent() { return null == useColorFromParent ? _useColorFromParent : useColorFromParent.get(); }
    public void setUseColorFromParent(final boolean USE) {
        if (null == useColorFromParent) {
            _useColorFromParent = USE;
            redraw();
        } else {
            useColorFromParent.set(USE);
        }
    }
    public BooleanProperty useColorFromParentProperty() {
        if (null == useColorFromParent) {
            useColorFromParent = new BooleanPropertyBase(_useColorFromParent) {
                @Override protected void invalidated() { redraw(); }
                @Override public Object getBean() { return SunburstChart.this; }
                @Override public String getName() { return "useColorFromParent"; }
            };
        }
        return useColorFromParent;
    }

    public int getDecimals() { return null == decimals ? _decimals : decimals.get(); }
    public void setDecimals(final int DECIMALS) {
        if (null == decimals) {
            _decimals    = clamp(0, 5, DECIMALS);
            formatString = new StringBuilder("%.").append(_decimals).append("f").toString();
            redraw();
        } else {
            decimals.set(DECIMALS);
        }
    }
    public IntegerProperty decimalsProperty() {
        if (null == decimals) {
            decimals = new IntegerPropertyBase(_decimals) {
                @Override protected void invalidated() {
                    set(clamp(0, 5, get()));
                    formatString = new StringBuilder("%.").append(get()).append("f").toString();
                    redraw();
                }
                @Override public Object getBean() { return SunburstChart.this; }
                @Override public String getName() { return "decimals"; }
            };
        }
        return decimals;
    }

    public void setTree(final TreeNode TREE) {
        if (null != tree) { tree.flattened().forEach(node -> node.removeAllTreeNodeEventListeners()); }
        tree = TREE;
        tree.flattened().forEach(node -> node.setOnTreeNodeEvent(e -> redraw()));
        prepareData();
        drawChart();
    }

    private void prepareData() {
        root     = tree.getTreeRoot();
        maxLevel = root.getMaxLevel();

        // Create map of all nodes per level
        levelMap.clear();
        for (int i = 0 ; i <= maxLevel ; i++) { levelMap.put(i, new ArrayList<>()); }
        root.stream().forEach(node -> levelMap.get(node.getDepth()).add(node));

        for (int level = 1 ; level < maxLevel ; level++) {
            List<TreeNode> treeNodeList = levelMap.get(level);
            treeNodeList.stream()
                        .filter(node -> node.getChildren().isEmpty())
                        .forEach(node ->node.addNode(new TreeNode(new ChartData("", 0, Color.TRANSPARENT), node)));
        }
    }

    private void drawChart() {
        levelMap.clear();
        for (int i = 0 ; i <= maxLevel ; i++) { levelMap.put(i, new ArrayList<>()); }
        root.stream().forEach(node -> levelMap.get(node.getDepth()).add(node));

        double          canvasSize         = chartCanvas.getWidth();
        double          widthStep          = canvasSize * 0.8 / maxLevel;
        double          barWidth           = widthStep * 0.49;
        double          center             = canvasSize * 0.5;
        double          outerRadiusStep    = widthStep * 0.5;
        double          textRadiusStep     = canvasSize * 0.4 / maxLevel;
        double          segmentStrokeWidth = widthStep * 0.01;
        Color           bkgColor           = getBackgroundColor();
        Color           textColor          = getTextColor();
        TextOrientation textOrientation    = getTextOrientation();
        double          maxTextWidth       = barWidth * 0.9;

        chartCtx.clearRect(0, 0, canvasSize, canvasSize);
        chartCtx.setFill(bkgColor);
        chartCtx.fillRect(0, 0, canvasSize, canvasSize);

        chartCtx.setFont(Fonts.latoRegular(barWidth * 0.2));
        chartCtx.setTextBaseline(VPos.CENTER);
        chartCtx.setTextAlign(TextAlignment.CENTER);
        chartCtx.setLineCap(StrokeLineCap.BUTT);

        for (int level = 1 ; level <= maxLevel ; level++) {
            List<TreeNode> nodesAtLevel = levelMap.get(level);
            double xy                   = center - widthStep * level * 0.5;
            double wh                   = widthStep * level;

            double segmentStartAngle;
            double segmentEndAngle = 0;
            for (TreeNode node : nodesAtLevel) {
                ChartData segmentData  = node.getData();
                double    segmentAngle = node.getParentAngle() * node.getPercentage();
                Color     segmentColor = getUseColorFromParent() ? node.getMyRoot().getData().getFillColor() : segmentData.getFillColor();
                segmentStartAngle = 90 + segmentEndAngle;
                segmentEndAngle  -= segmentAngle;

                // Only draw if segment fill color is not TRANSPARENT
                if (!Color.TRANSPARENT.equals(segmentData.getFillColor())) {
                    double value = segmentData.getValue();

                    // Segment Fill
                    chartCtx.setLineWidth(barWidth);
                    chartCtx.setStroke(segmentColor);
                    chartCtx.strokeArc(xy, xy, wh, wh, segmentStartAngle, -segmentAngle, ArcType.OPEN);

                    // Segment Stroke
                    double innerRadius = wh * 0.5 - barWidth * 0.5;
                    double outerRadius = outerRadiusStep * level + barWidth * 0.5;
                    double radStart    = Math.toRadians(segmentStartAngle);
                    double cosStart    = Math.cos(radStart);
                    double sinStart    = Math.sin(radStart);
                    double x1          = center + innerRadius * cosStart;
                    double y1          = center - innerRadius * sinStart;
                    double x2          = center + outerRadius * cosStart;
                    double y2          = center - outerRadius * sinStart;

                    chartCtx.setLineWidth(segmentStrokeWidth);
                    chartCtx.setStroke(bkgColor);
                    chartCtx.strokeLine(x1, y1, x2, y2);

                    // Visible Data
                    if (getVisibleData() != VisibleData.NONE && segmentAngle > textOrientation.getMaxAngle()) {
                        double radText    = Math.toRadians(segmentStartAngle - (segmentAngle * 0.5));
                        double cosText    = Math.cos(radText);
                        double sinText    = Math.sin(radText);
                        double textRadius = textRadiusStep * level;
                        double textX      = center + textRadius * cosText;
                        double textY      = center - textRadius * sinText;

                        chartCtx.setFill(textColor);

                        chartCtx.save();
                        chartCtx.translate(textX, textY);

                        rotateContextForText(chartCtx, segmentStartAngle, -(segmentAngle * 0.5), textOrientation);

                        switch (getVisibleData()) {
                            case VALUE:
                                chartCtx.fillText(String.format(Locale.US, formatString, value), 0, 0, maxTextWidth);
                                break;
                            case NAME:
                                chartCtx.fillText(segmentData.getName(), 0, 0, maxTextWidth);
                                break;
                            case NAME_VALUE:
                                chartCtx.fillText(String.join("", segmentData.getName(), " (", String.format(Locale.US, formatString, value),")"), 0, 0, maxTextWidth);
                                break;
                        }
                        chartCtx.restore();
                    }
                }
            }
        }
    }

    private static void rotateContextForText(final GraphicsContext CTX, final double START_ANGLE, final double ANGLE, final TextOrientation ORIENTATION) {
        switch (ORIENTATION) {
            case TANGENT:
                if ((360 - START_ANGLE - ANGLE) % 360 > 90 && (360 - START_ANGLE - ANGLE) % 360 < 270) {
                    CTX.rotate((180 - START_ANGLE - ANGLE) % 360);
                } else {
                    CTX.rotate((360 - START_ANGLE - ANGLE) % 360);
                }
                break;
            case ORTHOGONAL:
                if ((360 - START_ANGLE - ANGLE - 90) % 360 > 90 && (360 - START_ANGLE - ANGLE - 90) % 360 < 270) {
                    CTX.rotate((90 - START_ANGLE - ANGLE) % 360);
                } else {
                    CTX.rotate((270 - START_ANGLE - ANGLE) % 360);
                }
                break;
            case HORIZONTAL:
            default:
                break;
        }
    }

    private int clamp(final int MIN, final int MAX, final int VALUE) {
        if (VALUE < MIN) return MIN;
        if (VALUE > MAX) return MAX;
        return VALUE;
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
