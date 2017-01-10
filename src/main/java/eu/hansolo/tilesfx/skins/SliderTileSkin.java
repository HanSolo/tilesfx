/*
 * Copyright (c) 2016 by Gerrit Grunwald
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
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.Helper;
import javafx.geometry.Point2D;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import static eu.hansolo.tilesfx.tools.Helper.clamp;


/**
 * Created by hansolo on 19.12.16.
 */
public class SliderTileSkin extends TileSkin {
    private Text      titleText;
    private Text      valueText;
    private Text      unitText;
    private Circle    thumb;
    private Rectangle barBackground;
    private Rectangle bar;
    private Point2D   dragStart;
    private double    centerX;
    private double    centerY;
    private double    formerThumbPos;
    private double    trackStart;
    private double    trackLength;


    // ******************** Constructors **************************************
    public SliderTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        titleText = new Text();
        titleText.setFill(getSkinnable().getTitleColor());
        Helper.enableNode(titleText, !getSkinnable().getTitle().isEmpty());

        valueText = new Text(String.format(locale, formatString, ((getSkinnable().getValue() - minValue) / range * 100)));
        valueText.setFill(getSkinnable().getValueColor());
        Helper.enableNode(valueText, getSkinnable().isValueVisible());

        unitText = new Text(getSkinnable().getUnit());
        unitText.setFill(getSkinnable().getUnitColor());
        Helper.enableNode(unitText, !getSkinnable().getUnit().isEmpty());

        barBackground = new Rectangle(PREFERRED_WIDTH * 0.795, PREFERRED_HEIGHT * 0.0275);

        bar = new Rectangle(0, PREFERRED_HEIGHT * 0.0275);

        thumb = new Circle(PREFERRED_WIDTH * 0.09);
        thumb.setEffect(shadow);

        getPane().getChildren().addAll(titleText, valueText, unitText, barBackground, bar, thumb);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        thumb.setOnMousePressed(e -> {
            dragStart      = thumb.localToParent(e.getX(), e.getY());
            formerThumbPos = (getSkinnable().getCurrentValue() - minValue) / range;
        });
        thumb.setOnMouseDragged(e -> {
            Point2D currentPos = thumb.localToParent(e.getX(), e.getY());
            double  dragPos    = currentPos.getX() - dragStart.getX();
            thumbDragged((formerThumbPos + dragPos / trackLength));
        });
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !getSkinnable().getTitle().isEmpty());
            Helper.enableNode(valueText, getSkinnable().isValueVisible());
            Helper.enableNode(unitText, !getSkinnable().getUnit().isEmpty());
        }
    };

    @Override protected void handleCurrentValue(final double VALUE) {
        valueText.setText(String.format(locale, formatString, VALUE));
        resizeDynamicText();
        centerX = trackStart + (trackLength * ((VALUE - minValue) / range));
        thumb.setCenterX(clamp(trackStart, (trackStart + trackLength), centerX));
        thumb.setFill(Double.compare(VALUE, getSkinnable().getMinValue()) != 0 ? getSkinnable().getBarColor() : getSkinnable().getForegroundColor());
        bar.setWidth(thumb.getCenterX() - trackStart);
    };

    private void thumbDragged(final double POSITION) {
        getSkinnable().setValue(clamp(minValue, maxValue, (POSITION * range) + minValue));
    }


    // ******************** Resizing ******************************************
    @Override protected void resizeDynamicText() {
        double maxWidth = size * 0.9;
        double fontSize = size * 0.24;
        valueText.setFont(Fonts.latoRegular(fontSize));
        if (valueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(valueText, maxWidth, fontSize); }
        if (unitText.isVisible()) {
            valueText.relocate(size * 0.925 - valueText.getLayoutBounds().getWidth() - unitText.getLayoutBounds().getWidth(), size * 0.15);
        } else {
            valueText.relocate(size * 0.95 - valueText.getLayoutBounds().getWidth(), size * 0.15);
        }
    };
    @Override protected void resizeStaticText() {
        double maxWidth = size * 0.9;
        double fontSize = size * textSize.factor;

        titleText.setFont(Fonts.latoRegular(fontSize));
        if (titleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(titleText, maxWidth, fontSize); }
        titleText.relocate(size * 0.05, size * 0.05);

        maxWidth = size * 0.15;
        fontSize = size * 0.12;
        unitText.setFont(Fonts.latoRegular(fontSize));
        if (unitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(unitText, maxWidth, fontSize); }
        unitText.relocate(size * 0.95 - unitText.getLayoutBounds().getWidth(), size * 0.27);
    };

    @Override protected void resize() {
        super.resize();

        trackStart  = size * 0.14;
        trackLength = size * 0.72;
        centerX     = trackStart + (trackLength * ((getSkinnable().getCurrentValue() - minValue) / range));
        centerY     = size * 0.86;

        barBackground.setWidth(trackLength);
        barBackground.setHeight(size * 0.0275);
        barBackground.setX(trackStart);
        barBackground.setY(size * 0.84625);
        barBackground.setArcWidth(size * 0.0275);
        barBackground.setArcHeight(size * 0.0275);

        bar.setWidth(thumb.getCenterX() - trackStart);
        bar.setHeight(size * 0.0275);
        bar.setX(trackStart);
        bar.setY(size * 0.84625);
        bar.setArcWidth(size * 0.0275);
        bar.setArcHeight(size * 0.0275);

        thumb.setRadius(size * 0.09);
        thumb.setCenterX(centerX);
        thumb.setCenterY(size * 0.675);
        thumb.setCenterY(centerY);
    };

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(getSkinnable().getTitle());
        unitText.setText(getSkinnable().getUnit());

        resizeStaticText();

        titleText.setFill(getSkinnable().getTitleColor());
        valueText.setFill(getSkinnable().getValueColor());
        unitText.setFill(getSkinnable().getUnitColor());
        barBackground.setFill(getSkinnable().getBarBackgroundColor());
        bar.setFill(getSkinnable().getBarColor());
        thumb.setFill(getSkinnable().getForegroundColor());
    };
}
