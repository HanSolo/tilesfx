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

import eu.hansolo.tilesfx.events.TreeNodeEvent;
import eu.hansolo.tilesfx.events.TreeNodeEvent.EventType;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.Helper;
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
import javafx.event.WeakEventHandler;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tooltip;
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
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.Collections;
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
public class SunburstChart extends Region {
    public enum VisibleData {
        NONE, NAME, VALUE, NAME_VALUE
    }
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
    private static final double                                  PREFERRED_WIDTH   = 250;
    private static final double                                  PREFERRED_HEIGHT  = 250;
    private static final double                                  MINIMUM_WIDTH     = 50;
    private static final double                                  MINIMUM_HEIGHT    = 50;
    private static final double                                  MAXIMUM_WIDTH     = 2048;
    private static final double                                  MAXIMUM_HEIGHT    = 2048;
    private static final Color                                   BRIGHT_TEXT_COLOR = Color.WHITE;
    private static final Color                                   DARK_TEXT_COLOR   = Color.BLACK;
    private              double                                  size;
    private              double                                  width;
    private              double                                  height;
    private              double                                  centerX;
    private              double                                  centerY;
    private              Pane                                    segmentPane;
    private              Canvas                                  chartCanvas;
    private              GraphicsContext                         chartCtx;
    private              Pane                                    pane;
    private              Paint                                   backgroundPaint;
    private              Paint                                   borderPaint;
    private              double                                  borderWidth;
    private              List<Path>                              segments;
    private              VisibleData                             _visibleData;
    private              ObjectProperty<VisibleData>             visibleData;
    private              TextOrientation                         _textOrientation;
    private              ObjectProperty<TextOrientation>         textOrientation;
    private              Color                                   _backgroundColor;
    private              ObjectProperty<Color>                   backgroundColor;
    private              Color                                   _textColor;
    private              ObjectProperty<Color>                   textColor;
    private              boolean                                 _useColorFromParent;
    private              BooleanProperty                         useColorFromParent;
    private              int                                     _decimals;
    private              IntegerProperty                         decimals;
    private              boolean                                 _interactive;
    private              BooleanProperty                         interactive;
    private              boolean                                 _autoTextColor;
    private              BooleanProperty                         autoTextColor;
    private              Color                                   _brightTextColor;
    private              ObjectProperty<Color>                   brightTextColor;
    private              Color                                   _darkTextColor;
    private              ObjectProperty<Color>                   darkTextColor;
    private              boolean                                 _useChartDataTextColor;
    private              BooleanProperty                         useChartDataTextColor;
    private              String                                  formatString;
    private              ObjectProperty<TreeNode<ChartData>>     tree;
    private              TreeNode<ChartData>                     root;
    private              int                                     maxLevel;
    private              Map<Integer, List<TreeNode<ChartData>>> levelMap;
    private              InvalidationListener                    sizeListener;



    // ******************** Constructors **************************************
    public SunburstChart() {
        this(new TreeNode(new ChartData()));
    }
    public SunburstChart(final TreeNode TREE) {
        backgroundPaint        = Color.TRANSPARENT;
        borderPaint            = Color.TRANSPARENT;
        borderWidth            = 0d;
        segments               = new ArrayList<>(64);
        _visibleData           = VisibleData.NAME;
        _textOrientation       = TextOrientation.TANGENT;
        _backgroundColor       = Color.WHITE;
        _textColor             = Color.BLACK;
        _useColorFromParent    = false;
        _decimals              = 0;
        _interactive           = false;
        _autoTextColor         = true;
        _brightTextColor       = BRIGHT_TEXT_COLOR;
        _darkTextColor         = DARK_TEXT_COLOR;
        _useChartDataTextColor = false;
        formatString           = "%.0f";
        tree                   = new ObjectPropertyBase<TreeNode<ChartData>>(TREE) {
            @Override protected void invalidated() {
                if (null != get()) { get().flattened().forEach(node -> node.removeAllTreeNodeEventListeners()); }
                get().flattened().forEach(node -> node.setOnTreeNodeEvent(e -> redraw()));
                prepareData();
                if (isAutoTextColor()) { adjustTextColors(); }
                drawChart();
            }
            @Override public Object getBean() { return SunburstChart.this; }
            @Override public String getName() { return "tree"; }
        };
        levelMap               = new HashMap<>(8);
        sizeListener           = o -> resize();
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

        segmentPane = new Pane();

        chartCanvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        chartCanvas.setMouseTransparent(true);

        chartCtx    = chartCanvas.getGraphicsContext2D();

        pane = new Pane(segmentPane, chartCanvas);
        pane.setBackground(new Background(new BackgroundFill(backgroundPaint, CornerRadii.EMPTY, Insets.EMPTY)));
        pane.setBorder(new Border(new BorderStroke(borderPaint, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(borderWidth))));

        getChildren().setAll(pane);

        prepareData();
    }

    private void registerListeners() {
        widthProperty().addListener(sizeListener);
        heightProperty().addListener(sizeListener);
        tree.get().setOnTreeNodeEvent(e -> redraw());
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
        tree.get().removeAllTreeNodeEventListeners();
    }

    /**
     * Returns the data that should be visualized in the chart segments
     * @return the data that should be visualized in the chart segments
     */
    public VisibleData getVisibleData() { return null == visibleData ? _visibleData : visibleData.get(); }
    /**
     * Defines the data that should be visualized in the chart segments
     * @param VISIBLE_DATA
     */
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

    /**
     * Returns the orientation the text will be drawn in the segments
     * @return the orientation the text will be drawn in the segments
     */
    public TextOrientation getTextOrientation() { return null == textOrientation ? _textOrientation : textOrientation.get(); }
    /**
     * Defines the orientation the text will be drawn in the segments
     * @param ORIENTATION
     */
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

    /**
     * Returns the color that will be used to fill the background of the chart
     * @return the color that will be used to fill the background of the chart
     */
    public Color getBackgroundColor() { return null == backgroundColor ? _backgroundColor : backgroundColor.get(); }
    /**
     * Defines the color that will be used to fill the background of the chart
     * @param COLOR
     */
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

    /**
     * Returns the color that will be used to draw text in segments if useChartDataTextColor == false
     * @return the color that will be used to draw text in segments if useChartDataTextColor == false
     */
    public Color getTextColor() { return null == textColor ? _textColor : textColor.get(); }
    /**
     * Defines the color that will be used to draw text in segments if useChartDataTextColor == false
     * @param COLOR
     */
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

    /**
     * Returns true if the color of all chart segments in one group should be filled with the color
     * of the groups root node or by the color defined in the chart data elements
     * @return
     */
    public boolean getUseColorFromParent() { return null == useColorFromParent ? _useColorFromParent : useColorFromParent.get(); }
    /**
     * Defines if tthe color of all chart segments in one group should be filled with the color
     * of the groups root node or by the color defined in the chart data elements
     * @param USE
     */
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

    /**
     * Returns the number of decimals that will be used to format the values in the tooltip
     * @return
     */
    public int getDecimals() { return null == decimals ? _decimals : decimals.get(); }
    /**
     * Defines the number of decimals that will be used to format the values in the tooltip
     * @param DECIMALS
     */
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

    /**
     * Returns true if the chart is drawn using Path elements, fire ChartDataEvents and show tooltips on segments.
     * @return
     */
    public boolean isInteractive() { return null == interactive ? _interactive : interactive.get(); }
    /**
     * Defines if the chart should be drawn using Path elements, fire ChartDataEvents and shows tooltips on segments or
     * if the the chart should be drawn using one Canvas node.
     * @param INTERACTIVE
     */
    public void setInteractive(final boolean INTERACTIVE) {
        if (null == interactive) {
            _interactive = INTERACTIVE;
            redraw();
        } else {
            interactive.set(INTERACTIVE);
        }
    }
    public BooleanProperty interactiveProperty() {
        if (null == interactive) {
            interactive = new BooleanPropertyBase(_interactive) {
                @Override protected void invalidated() { redraw(); }
                @Override public Object getBean() { return SunburstChart.this; }
                @Override public String getName() { return "interactive"; }
            };
        }
        return interactive;
    }

    /**
     * Returns true if the text color of the chart data should be adjusted according to the chart data fill color.
     * e.g. if the fill color is dark the text will be set to the defined brightTextColor and vice versa.
     * @return true if the text color of the chart data should be adjusted according to the chart data fill color
     */
    public boolean isAutoTextColor() { return null == autoTextColor ? _autoTextColor : autoTextColor.get(); }
    /**
     * Defines if the text color of the chart data should be adjusted according to the chart data fill color
     * @param AUTOMATIC
     */
    public void setAutoTextColor(final boolean AUTOMATIC) {
        if (null == autoTextColor) {
            _autoTextColor = AUTOMATIC;
            adjustTextColors();
            redraw();
        } else {
            autoTextColor.set(AUTOMATIC);
        }
    }
    public BooleanProperty autoTextColorProperty() {
        if (null == autoTextColor) {
            autoTextColor = new BooleanPropertyBase(_autoTextColor) {
                @Override protected void invalidated() {
                    adjustTextColors();
                    redraw();
                }
                @Override public Object getBean() { return SunburstChart.this; }
                @Override public String getName() { return "autoTextColor"; }
            };
        }
        return autoTextColor;
    }

    /**
     * Returns the color that will be used by the autoTextColor feature as the bright text on dark segment fill colors
     * @return the color that will be used by the autoTextColor feature as the bright text on dark segment fill colors
     */
    public Color getBrightTextColor() { return null == brightTextColor ? _brightTextColor : brightTextColor.get(); }

    /**
     * Defines the color that will be used by the autoTextColor feature as the bright text on dark segment fill colors
     * @param COLOR
     */
    public void setBrightTextColor(final Color COLOR) {
        if (null == brightTextColor) {
            _brightTextColor = COLOR;
            if (isAutoTextColor()) {
                adjustTextColors();
                redraw();
            }
        } else {
            brightTextColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> brightTextColorProperty() {
        if (null == brightTextColor) {
            brightTextColor = new ObjectPropertyBase<Color>(_brightTextColor) {
                @Override protected void invalidated() {
                    if (isAutoTextColor()) {
                        adjustTextColors();
                        redraw();
                    }
                }
                @Override public Object getBean() { return SunburstChart.this; }
                @Override public String getName() { return "brightTextColor"; }
            };
            _brightTextColor = null;
        }
        return brightTextColor;
    }

    /**
     * Returns the color that will be used by the autoTextColor feature as the dark text on bright segment fill colors
     * @return the color that will be used by the autoTextColor feature as the dark text on bright segment fill colors
     */
    public Color getDarkTextColor() { return null == darkTextColor ? _darkTextColor : darkTextColor.get(); }
    /**
     * Defines the color that will be used by the autoTextColor feature as the dark text on bright segment fill colors
     * @param COLOR
     */
    public void setDarkTextColor(final Color COLOR) {
        if (null == darkTextColor) {
            _darkTextColor = COLOR;
            if (isAutoTextColor()) {
                adjustTextColors();
                redraw();
            }
        } else {
            darkTextColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> darkTextColorProperty() {
        if (null == darkTextColor) {
            darkTextColor = new ObjectPropertyBase<Color>(_darkTextColor) {
                @Override protected void invalidated() {
                    if (isAutoTextColor()) {
                        adjustTextColors();
                        redraw();
                    }
                }
                @Override public Object getBean() { return SunburstChart.this; }
                @Override public String getName() { return "darkTextColor"; }
            };
            _darkTextColor = null;
        }
        return darkTextColor;
    }

    /**
     * Returns true if the text color of the ChartData elements should be used to
     * fill the text in the segments
     * @return true if the text color of the segments will be taken from the ChartData elements
     */
    public boolean getUseChartDataTextColor() { return null == useChartDataTextColor ? _useChartDataTextColor : useChartDataTextColor.get(); }
    /**
     * Defines if the text color of the segments should be taken from the ChartData elements
     * @param USE
     */
    public void setUseChartDataTextColor(final boolean USE) {
        if (null == useChartDataTextColor) {
            _useChartDataTextColor = USE;
            redraw();
        } else {
            useChartDataTextColor.set(USE);
        }
    }
    public BooleanProperty useChartDataTextColor() {
        if (null == useChartDataTextColor) {
            useChartDataTextColor = new BooleanPropertyBase(_useChartDataTextColor) {
                @Override protected void invalidated() { redraw(); }
                @Override public Object getBean() { return SunburstChart.this; }
                @Override public String getName() { return "useChartDataTextColor"; }
            };
        }
        return useChartDataTextColor;
    }

    public TreeNode<ChartData> getTreeNode() {
        return tree.get();
    }
    /**
     * Defines the root element of the tree
     * @param TREE
     */
    public void setTree(final TreeNode<ChartData> TREE) {
        if (null != tree) { getTreeNode().flattened().forEach(node -> node.removeAllTreeNodeEventListeners()); }
        tree.set(TREE);
        getTreeNode().flattened().forEach(node -> node.setOnTreeNodeEvent(e -> redraw()));
        prepareData();
        if (isAutoTextColor()) { adjustTextColors(); }
        drawChart();
    }
    public ObjectProperty<TreeNode<ChartData>> treeNodeProperty() { return tree; }

    private void adjustTextColors() {
        Color brightColor = getBrightTextColor();
        Color darkColor   = getDarkTextColor();
        root.stream().forEach(node -> {
            ChartData data = node.getItem();
            boolean darkFillColor = Helper.isDark(data.getFillColor());
            boolean darkTextColor = Helper.isDark(data.getTextColor());
            if (darkFillColor && darkTextColor) { data.setTextColor(brightColor); }
            if (!darkFillColor && !darkTextColor) { data.setTextColor(darkColor); }
        });
    }

    private void prepareData() {
        root     = getTreeNode().getTreeRoot();
        maxLevel = root.getMaxLevel();

        // Create map of all nodes per level
        levelMap.clear();
        for (int i = 0 ; i <= maxLevel ; i++) { levelMap.put(i, new ArrayList<>()); }
        root.stream().forEach(node -> levelMap.get(node.getDepth()).add(node));

        for (int level = 1 ; level < maxLevel ; level++) {
            List<TreeNode<ChartData>> treeNodeList = levelMap.get(level);
            treeNodeList.stream()
                        .filter(node -> node.getChildren().isEmpty())
                        .forEach(node ->node.addNode(new TreeNode<ChartData>(new ChartData("", 0, Color.TRANSPARENT), node)));
        }
    }

    private void drawChart() {
        levelMap.clear();
        for (int i = 0 ; i <= maxLevel ; i++) { levelMap.put(i, new ArrayList<>()); }
        root.stream().forEach(node -> levelMap.get(node.getDepth()).add(node));
        boolean         isInteractive      = isInteractive();
        double          ringStepSize       = size * 0.8 / maxLevel;
        double          ringRadiusStep     = ringStepSize * 0.5;
        double          barWidth           = isInteractive ? ringStepSize * 0.5 : ringStepSize * 0.49;
        double          textRadiusStep     = size * 0.4 / maxLevel;
        double          segmentStrokeWidth = ringStepSize * 0.01;
        Color           bkgColor           = getBackgroundColor();
        Color           textColor          = getTextColor();
        TextOrientation textOrientation    = getTextOrientation();
        double          maxTextWidth       = barWidth * 0.9;

        chartCtx.clearRect(0, 0, size, size);
        chartCtx.setFill(isInteractive ? Color.TRANSPARENT : bkgColor);
        chartCtx.fillRect(0, 0, size, size);

        chartCtx.setFont(Fonts.latoRegular(barWidth * 0.2));
        chartCtx.setTextBaseline(VPos.CENTER);
        chartCtx.setTextAlign(TextAlignment.CENTER);
        chartCtx.setLineCap(StrokeLineCap.BUTT);

        segments.clear();

        for (int level = 1 ; level <= maxLevel ; level++) {
            List<TreeNode<ChartData>> nodesAtLevel = levelMap.get(level);
            double         xy           = centerX - ringStepSize * level * 0.5;
            double         wh           = ringStepSize * level;
            double         outerRadius  = ringRadiusStep * level + barWidth * 0.5;
            double         innerRadius  = outerRadius - barWidth;

            double segmentStartAngle;
            double segmentEndAngle = 0;
            for (TreeNode<ChartData> node : nodesAtLevel) {
                ChartData segmentData  = node.getItem();
                double    segmentAngle = getParentAngle(node) * getPercentage(node);
                Color     segmentColor = getUseColorFromParent() ? node.getMyRoot().getItem().getFillColor() : segmentData.getFillColor();

                segmentStartAngle = 90 + segmentEndAngle;
                segmentEndAngle  -= segmentAngle;

                // Only draw if segment fill color is not TRANSPARENT
                if (!Color.TRANSPARENT.equals(segmentData.getFillColor())) {
                    double value = segmentData.getValue();

                    if (isInteractive) {
                        segments.add(createSegment(-segmentStartAngle, -segmentStartAngle + segmentAngle, innerRadius, outerRadius, segmentColor, bkgColor, node));
                    } else {
                        // Segment Fill
                        chartCtx.setLineWidth(barWidth);
                        chartCtx.setStroke(segmentColor);
                        chartCtx.strokeArc(xy, xy, wh, wh, segmentStartAngle, -segmentAngle, ArcType.OPEN);

                        // Segment Stroke
                        double radStart = Math.toRadians(segmentStartAngle);
                        double cosStart = Math.cos(radStart);
                        double sinStart = Math.sin(radStart);
                        double x1       = centerX + innerRadius * cosStart;
                        double y1       = centerY - innerRadius * sinStart;
                        double x2       = centerX + outerRadius * cosStart;
                        double y2       = centerY - outerRadius * sinStart;

                        chartCtx.setLineWidth(segmentStrokeWidth);
                        chartCtx.setStroke(bkgColor);
                        chartCtx.strokeLine(x1, y1, x2, y2);
                    }

                    // Visible Data
                    if (getVisibleData() != VisibleData.NONE && segmentAngle > textOrientation.getMaxAngle()) {
                        double radText    = Math.toRadians(segmentStartAngle - (segmentAngle * 0.5));
                        double cosText    = Math.cos(radText);
                        double sinText    = Math.sin(radText);
                        double textRadius = textRadiusStep * level;
                        double textX      = centerX + textRadius * cosText;
                        double textY      = centerY - textRadius * sinText;

                        chartCtx.setFill(getUseChartDataTextColor() ? segmentData.getTextColor() : textColor);

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

        segmentPane.getChildren().setAll(segments);
    }

    public double getParentAngle(final TreeNode<ChartData> NODE) {
        List<TreeNode<ChartData>> parentList = new ArrayList<>();
        TreeNode<ChartData> node = NODE;
        while (!node.getParent().isRoot()) {
            node = node.getParent();
            parentList.add(node);
        }
        Collections.reverse(parentList);
        double parentAngle = 360.0;
        for (TreeNode<ChartData> n : parentList) { parentAngle = parentAngle * getPercentage(n); }
        return parentAngle;
    }

    private double getPercentage(final TreeNode<ChartData> NODE) {
        List<TreeNode<ChartData>> siblings   = NODE.getSiblings();
        double         sum        = siblings.stream().map(node -> node.getItem()).mapToDouble(ChartData::getValue).sum();
        return Double.compare(sum, 0) == 0 ? 1.0 : NODE.getItem().getValue() / sum;
    }

    private Path createSegment(final double START_ANGLE, final double END_ANGLE, final double INNER_RADIUS, final double OUTER_RADIUS, final Color FILL, final Color STROKE, final TreeNode<ChartData> NODE) {
        double  startAngleRad = Math.toRadians(START_ANGLE + 90);
        double  endAngleRad   = Math.toRadians(END_ANGLE + 90);
        boolean largeAngle    = Math.abs(END_ANGLE - START_ANGLE) > 180.0;

        double x1 = centerX + INNER_RADIUS * Math.sin(startAngleRad);
        double y1 = centerY - INNER_RADIUS * Math.cos(startAngleRad);

        double x2 = centerX + OUTER_RADIUS * Math.sin(startAngleRad);
        double y2 = centerY - OUTER_RADIUS * Math.cos(startAngleRad);

        double x3 = centerX + OUTER_RADIUS * Math.sin(endAngleRad);
        double y3 = centerY - OUTER_RADIUS * Math.cos(endAngleRad);

        double x4 = centerX + INNER_RADIUS * Math.sin(endAngleRad);
        double y4 = centerY - INNER_RADIUS * Math.cos(endAngleRad);

        MoveTo moveTo1 = new MoveTo(x1, y1);
        LineTo lineTo2 = new LineTo(x2, y2);
        ArcTo  arcTo3  = new ArcTo(OUTER_RADIUS, OUTER_RADIUS, 0, x3, y3, largeAngle, true);
        LineTo lineTo4 = new LineTo(x4, y4);
        ArcTo  arcTo1  = new ArcTo(INNER_RADIUS, INNER_RADIUS, 0, x1, y1, largeAngle, false);

        Path path = new Path(moveTo1, lineTo2, arcTo3, lineTo4, arcTo1);

        path.setFill(FILL);
        path.setStroke(STROKE);

        String tooltipText = new StringBuilder(NODE.getItem().getName()).append("\n").append(String.format(Locale.US, formatString, NODE.getItem().getValue())).toString();
        Tooltip.install(path, new Tooltip(tooltipText));

        path.setOnMousePressed(new WeakEventHandler<>(e -> NODE.getTreeRoot().fireTreeNodeEvent(new TreeNodeEvent(NODE, EventType.NODE_SELECTED))));

        return path;
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


    // ******************** Resizing ******************************************
    private void resize() {
        width  = getWidth() - getInsets().getLeft() - getInsets().getRight();
        height = getHeight() - getInsets().getTop() - getInsets().getBottom();
        size   = width < height ? width : height;

        if (width > 0 && height > 0) {
            pane.setMaxSize(size, size);
            pane.setPrefSize(size, size);
            pane.relocate((getWidth() - size) * 0.5, (getHeight() - size) * 0.5);

            segmentPane.setPrefSize(size, size);

            chartCanvas.setWidth(size);
            chartCanvas.setHeight(size);

            centerX = size * 0.5;
            centerY = centerX;

            redraw();
        }
    }

    public void redraw() {
        pane.setBackground(new Background(new BackgroundFill(backgroundPaint, CornerRadii.EMPTY, Insets.EMPTY)));
        pane.setBorder(new Border(new BorderStroke(borderPaint, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(borderWidth / PREFERRED_WIDTH * size))));

        segmentPane.setBackground(new Background(new BackgroundFill(getBackgroundColor(), CornerRadii.EMPTY, Insets.EMPTY)));
        segmentPane.setManaged(isInteractive());
        segmentPane.setVisible(isInteractive());

        drawChart();
    }
}
