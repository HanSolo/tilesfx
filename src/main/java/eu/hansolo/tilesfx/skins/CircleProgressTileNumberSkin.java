

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
package eu.hansolo.tilesfx.skins;

import eu.hansolo.tilesfx.Section;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.Helper;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

import java.util.List;
import java.util.Locale;

import static eu.hansolo.tilesfx.tools.Helper.enableNode;


public class CircleProgressTileNumberSkin extends TileSkin {

    /**
     * Adopted from CircularProgressTileSkin originally Created by hansolo on 03.03.17.
     * Modified by Wolf Runnermann on 22.11.2022
     */

    private static final double  ANGLE_RANGE = 360;
    private double               size;
    private double               chartSize;
    private Arc                  barBackground;
    private Arc                  bar;
    private Line                 separator;
    private Text                 titleText;
    private Text                 text;

    private Text smallValueText;
    private Text smallUnitText;
    private TextFlow smallTextFlow;
    private Text centerValueText;
    private Text centerUnitText;
    private TextFlow centerTextFlow;

    private double               minValue;
    private double               range;
    private double               angleStep;
    private boolean              sectionsVisible;
    private List<Section>        sections;
    private String               formatString;
    private Locale               locale;
    private StackPane            graphicContainer;
    private ChangeListener       graphicListener;
    private InvalidationListener currentValueListener;


    // ******************** Constructors **************************************
    public CircleProgressTileNumberSkin(Tile TILE) {
        super(TILE);

        setBar(TILE.getCurrentValue());
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        if (tile.isAutoScale()) tile.calcAutoScale();
        minValue             = tile.getMinValue();
        range                = tile.getRange();
        angleStep            = ANGLE_RANGE / range;
        sectionsVisible      = tile.getSectionsVisible();
        sections             = tile.getSections();
        formatString         = new StringBuilder("%.").append(Integer.toString(tile.getDecimals())).append("f").toString();
        locale               = tile.getLocale();
        currentValueListener = o -> setBar(tile.getCurrentValue());

        graphicListener      = (o, ov, nv) -> { if (nv != null) { graphicContainer.getChildren().setAll(tile.getGraphic()); }};

        titleText = new Text();
        titleText.setFill(tile.getTitleColor());
        enableNode(titleText, !tile.getTitle().isEmpty());

        text = new Text(tile.getText());
        text.setFill(tile.getTextColor());
        enableNode(text, tile.isTextVisible());

        barBackground = new Arc(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.5, PREFERRED_WIDTH * 0.468, PREFERRED_HEIGHT * 0.468, 90, 360);
        barBackground.setType(ArcType.OPEN);
        barBackground.setStroke(tile.getBarBackgroundColor());
        barBackground.setStrokeWidth(PREFERRED_WIDTH * 0.1);
        barBackground.setStrokeLineCap(StrokeLineCap.BUTT);
        barBackground.setFill(null);

        bar = new Arc(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.5, PREFERRED_WIDTH * 0.468, PREFERRED_HEIGHT * 0.468, 90, 0);
        bar.setType(ArcType.OPEN);
        bar.setStroke(tile.getBarColor());
        bar.setStrokeWidth(PREFERRED_WIDTH * 0.1);
        bar.setStrokeLineCap(StrokeLineCap.BUTT);
        bar.setFill(null);

        separator = new Line(PREFERRED_WIDTH * 0.5, 1, PREFERRED_WIDTH * 0.5, 0.16667 * PREFERRED_HEIGHT);
        separator.setStroke(tile.getBackgroundColor());
        separator.setFill(Color.TRANSPARENT);

        centerValueText = new Text(String.format(locale, formatString, tile.getCurrentValue()));
        centerValueText.setFont(Fonts.latoRegular(PREFERRED_WIDTH * 0.27333));
        centerValueText.setFill(tile.getValueColor());
        centerValueText.setTextOrigin(VPos.CENTER);

        centerUnitText = new Text(tile.getUnit());
        centerUnitText.setFont(Fonts.latoLight(PREFERRED_WIDTH * 0.08));
        centerUnitText.setFill(tile.getUnitColor());

        centerTextFlow = new TextFlow(centerValueText, centerUnitText);
        centerTextFlow.setTextAlignment(TextAlignment.CENTER);

        smallValueText = new Text(String.format(locale, formatString, tile.getCurrentValue()));
        smallValueText.setFont(Fonts.latoRegular(PREFERRED_WIDTH * 0.27333));
        smallValueText.setFill(tile.getValueColor());
        smallValueText.setTextOrigin(VPos.CENTER);
        enableNode(smallValueText, tile.isValueVisible());

        smallUnitText = new Text(tile.getUnit());
        smallUnitText = new Text(Helper.PERCENTAGE);
        smallUnitText.setFont(Fonts.latoLight(PREFERRED_WIDTH * 0.08));
        smallUnitText.setFill(tile.getUnitColor());
        enableNode(smallUnitText, !tile.getUnit().isEmpty());

        smallTextFlow = new TextFlow(smallValueText, smallUnitText);
        smallTextFlow.setTextAlignment(TextAlignment.CENTER);

        graphicContainer = new StackPane();
        graphicContainer.setMinSize(size * 0.9, tile.isTextVisible() ? size * 0.72 : size * 0.795);
        graphicContainer.setMaxSize(size * 0.9, tile.isTextVisible() ? size * 0.72 : size * 0.795);
        graphicContainer.setPrefSize(size * 0.9, tile.isTextVisible() ? size * 0.72 : size * 0.795);
        if (null == tile.getGraphic()) {
            enableNode(graphicContainer, false);
        } else {
            graphicContainer.getChildren().setAll(tile.getGraphic());
        }

        getPane().getChildren().addAll(barBackground, bar, separator, titleText, text, graphicContainer, smallTextFlow, centerTextFlow);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        tile.currentValueProperty().addListener(currentValueListener);
        tile.graphicProperty().addListener(graphicListener);
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if ("RECALC".equals(EVENT_TYPE)) {
            minValue  = tile.getMinValue();
            range     = tile.getRange();
            angleStep = ANGLE_RANGE / range;
            sections  = tile.getSections();
            redraw();
            setBar(tile.getCurrentValue());
        } else if ("VISIBILITY".equals(EVENT_TYPE)) {
            enableNode(titleText, !tile.getTitle().isEmpty());
            enableNode(text, tile.isTextVisible());
            enableNode(centerUnitText, !tile.getUnit().isEmpty());
            enableNode(centerValueText, tile.isValueVisible());
        }
    }

    private void setBar(final double VALUE) {
        if (minValue > 0) {
            bar.setLength((minValue - VALUE) * angleStep);
        } else {
            bar.setLength(-VALUE * angleStep);
        }
        setBarColor(VALUE);

        smallValueText.setText(String.format(locale, formatString, VALUE / range * 100.0));
        if (tile.getCustomDecimalFormatEnabled()) {
            centerValueText.setText(decimalFormat.format(VALUE));
        } else {
            centerValueText.setText(String.format(locale, formatString, VALUE));
        }
    }

    private void setBarColor(final double VALUE) {
        if (!sectionsVisible) {
            bar.setStroke(tile.getBarColor());
        } else {
            bar.setStroke(tile.getBarColor());
            for (Section section : sections) {
                if (section.contains(VALUE)) {
                    bar.setStroke(section.getColor());
                    break;
                }
            }
        }
    }

    @Override public void dispose() {
        tile.currentValueProperty().removeListener(currentValueListener);
        tile.graphicProperty().removeListener(graphicListener);
        super.dispose();
    }


    // ******************** Resizing ******************************************
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

        text.setFont(font);
        if (text.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(text, maxWidth, fontSize); }
        switch(tile.getTextAlignment()) {
            default    :
            case LEFT  : text.setX(size * 0.05); break;
            case CENTER: text.setX((width - text.getLayoutBounds().getWidth()) * 0.5); break;
            case RIGHT : text.setX(width - (size * 0.05) - text.getLayoutBounds().getWidth()); break;
        }
        text.setY(height - size * 0.05);
    }
    @Override protected void resizeDynamicText() {
        double maxWidth = centerUnitText.isVisible() ? chartSize * 0.7 : chartSize * 0.8;
        double fontSize = graphicContainer.isVisible() ? chartSize * 0.15 : chartSize * 0.2;
        centerValueText.setFont(Fonts.latoRegular(fontSize));
        if (centerValueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(centerValueText, maxWidth, fontSize); }

        fontSize = graphicContainer.isVisible() ? chartSize * 0.07 : chartSize * 0.08;
        centerUnitText.setFont(Fonts.latoLight(fontSize));
        if (centerUnitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(centerUnitText, maxWidth, fontSize); }

        maxWidth = smallUnitText.isVisible() ? chartSize * 0.3 : chartSize * 0.4;
        fontSize = graphicContainer.isVisible() ? chartSize * 0.075 : chartSize * 0.1;
        smallValueText.setFont(Fonts.latoRegular(fontSize));
        if (smallValueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(smallValueText, maxWidth, fontSize); }

        fontSize = graphicContainer.isVisible() ? chartSize * 0.035 : chartSize * 0.04;
        smallUnitText.setFont(Fonts.latoLight(fontSize));
        if (smallUnitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(smallUnitText, maxWidth, fontSize); }
    }

    @Override protected void resize() {
        super.resize();
        width  = tile.getWidth() - tile.getInsets().getLeft() - tile.getInsets().getRight();
        height = tile.getHeight() - tile.getInsets().getTop() - tile.getInsets().getBottom();
        size   = width < height ? width : height;

        if (tile.isShowing() && width > 0 && height > 0) {
            pane.setMaxSize(width, height);
            pane.setPrefSize(width, height);

            double chartWidth  = contentBounds.getWidth();
            double chartHeight = contentBounds.getHeight();
            chartSize          = chartWidth < chartHeight ? chartWidth : chartHeight;

            double maxContainerSize = chartSize * 0.5;
            double containerWidth   = maxContainerSize - size * 0.1;
            double containerHeight  = tile.isTextVisible() ? height - maxContainerSize * 0.28 : height - maxContainerSize * 0.205;

            double radius = chartSize * 0.495 - contentBounds.getX();

            barBackground.setCenterX(contentCenterX);
            barBackground.setCenterY(contentCenterY);
            barBackground.setRadiusX(radius);
            barBackground.setRadiusY(radius);
            barBackground.setStrokeWidth(chartSize * 0.1);

            bar.setCenterX(contentCenterX);
            bar.setCenterY(contentCenterY);
            bar.setRadiusX(radius);
            bar.setRadiusY(radius);
            bar.setStrokeWidth(chartSize * 0.1);

            separator.setStartX(contentCenterX);
            separator.setStartY(contentCenterX - radius - chartSize * 0.05);
            separator.setEndX(contentCenterX);
            separator.setEndY(contentCenterX - radius + chartSize * 0.05);

            if (graphicContainer.isVisible() && containerWidth > 0 && containerHeight > 0) {
                graphicContainer.setMinSize(containerWidth, containerHeight);
                graphicContainer.setMaxSize(containerWidth, containerHeight);
                graphicContainer.setPrefSize(containerWidth, containerHeight);
                graphicContainer.relocate((width - containerWidth) * 0.5, (height - containerHeight) * 0.35);

                if (null != tile) {
                    Node graphic = tile.getGraphic();
                    if (tile.getGraphic() instanceof Shape) {
                        double graphicWidth  = graphic.getBoundsInLocal().getWidth();
                        double graphicHeight = graphic.getBoundsInLocal().getHeight();

                        if (graphicWidth > containerWidth || graphicHeight > containerHeight) {
                            double scale;
                            if (graphicWidth - containerWidth > graphicHeight - containerHeight) {
                                scale = containerWidth / graphicWidth;
                            } else {
                                scale = containerHeight / graphicHeight;
                            }

                            graphic.setScaleX(scale);
                            graphic.setScaleY(scale);
                        }
                    } else if (tile.getGraphic() instanceof ImageView) {
                        ((ImageView) graphic).setFitWidth(containerWidth);
                        ((ImageView) graphic).setFitHeight(containerHeight);
                    }
                }
            }
            resizeStaticText();
            centerTextFlow.setPrefWidth(width * 0.9);
            centerTextFlow.relocate(width * 0.05, graphicContainer.isVisible() ? bar.getCenterY() + chartSize * 0.12 : bar.getCenterY() - chartSize * 0.12);

            smallTextFlow.setPrefWidth(width * 0.9);
            smallTextFlow.relocate(width * 0.05, graphicContainer.isVisible() ? bar.getCenterY() - chartSize * 0.32 : bar.getCenterY() + chartSize * 0.15);
        }
    }

    @Override protected void redraw() {
        super.redraw();
        locale           = tile.getLocale();
        formatString     = new StringBuilder("%.").append(Integer.toString(tile.getDecimals())).append("f").toString();
        sectionsVisible  = tile.getSectionsVisible();

        barBackground.setStroke(tile.getBarBackgroundColor());
        setBarColor(tile.getCurrentValue());
        smallValueText.setFill(tile.getValueColor());
        smallUnitText.setFill(tile.getUnitColor());
        centerValueText.setFill(tile.getValueColor());
        centerUnitText.setFill(tile.getUnitColor());
        titleText.setFill(tile.getTitleColor());
        text.setFill(tile.getTextColor());
        separator.setStroke(tile.getBackgroundColor());

        titleText.setText(tile.getTitle());
        text.setText(tile.getText());
        centerUnitText.setText(tile.getUnit());

        resizeStaticText();
        resizeDynamicText();
    }
}
