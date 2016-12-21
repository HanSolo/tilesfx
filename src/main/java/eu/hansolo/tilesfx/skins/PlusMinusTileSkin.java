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
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.text.Text;

import static eu.hansolo.tilesfx.tools.Helper.clamp;


/**
 * Created by hansolo on 19.12.16.
 */
public class PlusMinusTileSkin extends TileSkin {
    private Text titleText;
    private Text valueText;
    private Text unitText;
    private Path plusButton;
    private Path minusButton;


    // ******************** Constructors **************************************
    public PlusMinusTileSkin(final Tile TILE) {
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

        plusButton = new Path();
        plusButton.setPickOnBounds(true);
        drawPlusButton();

        minusButton = new Path();
        minusButton.setPickOnBounds(true);
        drawMinusButton();

        getPane().getChildren().addAll(titleText, valueText, unitText, minusButton, plusButton);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        plusButton.setOnMousePressed(e -> increment());
        plusButton.setOnMouseReleased(e -> plusButton.setFill(getSkinnable().getForegroundColor()));
        minusButton.setOnMousePressed(e -> decrement());
        minusButton.setOnMouseReleased(e -> minusButton.setFill(getSkinnable().getForegroundColor()));
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
    };

    private void increment() {
        plusButton.setFill(getSkinnable().getActiveColor());
        double newValue = clamp(minValue, maxValue, getSkinnable().getValue() + getSkinnable().getIncrement());
        getSkinnable().setValue(newValue);
    }
    private void decrement() {
        minusButton.setFill(getSkinnable().getActiveColor());
        double newValue = clamp(minValue, maxValue, getSkinnable().getValue() - getSkinnable().getIncrement());
        getSkinnable().setValue(newValue);
    }

    private void drawMinusButton() {
        double iconSize = PREFERRED_WIDTH * 0.18;
        minusButton.getElements().clear();
        minusButton.getElements().add(new MoveTo(0.05555555555555555 * iconSize, 0.5 * iconSize));
        minusButton.getElements().add(new CubicCurveTo(0.05555555555555555 * iconSize, 0.25 * iconSize,
                                                       0.25 * iconSize, 0.05555555555555555 * iconSize,
                                                       0.5 * iconSize, 0.05555555555555555 * iconSize));
        minusButton.getElements().add(new CubicCurveTo(0.75 * iconSize, 0.05555555555555555 * iconSize,
                                                       0.9444444444444444 * iconSize, 0.25 * iconSize,
                                                       0.9444444444444444 * iconSize, 0.5 * iconSize));
        minusButton.getElements().add(new CubicCurveTo(0.9444444444444444 * iconSize, 0.75 * iconSize,
                                                       0.75 * iconSize, 0.9444444444444444 * iconSize,
                                                       0.5 * iconSize, 0.9444444444444444 * iconSize));
        minusButton.getElements().add(new CubicCurveTo(0.25 * iconSize, 0.9444444444444444 * iconSize,
                                                       0.05555555555555555 * iconSize, 0.75 * iconSize,
                                                       0.05555555555555555 * iconSize, 0.5 * iconSize));
        minusButton.getElements().add(new ClosePath());
        minusButton.getElements().add(new MoveTo(0.0, 0.5 * iconSize));
        minusButton.getElements().add(new CubicCurveTo(0.0, 0.7777777777777778 * iconSize,
                                                       0.2222222222222222 * iconSize, iconSize,
                                                       0.5 * iconSize, iconSize));
        minusButton.getElements().add(new CubicCurveTo(0.7777777777777778 * iconSize, iconSize,
                                                       iconSize, 0.7777777777777778 * iconSize,
                                                       iconSize, 0.5 * iconSize));
        minusButton.getElements().add(new CubicCurveTo(iconSize, 0.2222222222222222 * iconSize,
                                                       0.7777777777777778 * iconSize, 0.0,
                                                       0.5 * iconSize, 0.0));
        minusButton.getElements().add(new CubicCurveTo(0.2222222222222222 * iconSize, 0.0,
                                                       0.0, 0.2222222222222222 * iconSize,
                                                       0.0, 0.5 * iconSize));
        minusButton.getElements().add(new ClosePath());
        minusButton.getElements().add(new MoveTo(0.19444444444444445 * iconSize, 0.4583333333333333 * iconSize));
        minusButton.getElements().add(new LineTo(0.19444444444444445 * iconSize, 0.5694444444444444 * iconSize));
        minusButton.getElements().add(new LineTo(0.8055555555555556 * iconSize, 0.5694444444444444 * iconSize));
        minusButton.getElements().add(new LineTo(0.8055555555555556 * iconSize, 0.4583333333333333 * iconSize));
        minusButton.getElements().add(new LineTo(0.19444444444444445 * iconSize, 0.4583333333333333 * iconSize));
        minusButton.getElements().add(new ClosePath());
    }
    private void drawPlusButton() {
        double iconSize = PREFERRED_WIDTH * 0.18;
        plusButton.getElements().clear();
        plusButton.getElements().add(new MoveTo(0.05555555555555555 * iconSize, 0.5 * iconSize));
        plusButton.getElements().add(new CubicCurveTo(0.05555555555555555 * iconSize, 0.25 * iconSize,
                                                      0.25 * iconSize, 0.05555555555555555 * iconSize,
                                                      0.5 * iconSize, 0.05555555555555555 * iconSize));
        plusButton.getElements().add(new CubicCurveTo(0.75 * iconSize, 0.05555555555555555 * iconSize,
                                                      0.9444444444444444 * iconSize, 0.25 * iconSize,
                                                      0.9444444444444444 * iconSize, 0.5 * iconSize));
        plusButton.getElements().add(new CubicCurveTo(0.9444444444444444 * iconSize, 0.75 * iconSize,
                                                      0.75 * iconSize, 0.9444444444444444 * iconSize,
                                                      0.5 * iconSize, 0.9444444444444444 * iconSize));
        plusButton.getElements().add(new CubicCurveTo(0.25 * iconSize, 0.9444444444444444 * iconSize,
                                                      0.05555555555555555 * iconSize, 0.75 * iconSize,
                                                      0.05555555555555555 * iconSize, 0.5 * iconSize));
        plusButton.getElements().add(new ClosePath());
        plusButton.getElements().add(new MoveTo(0.0, 0.5 * iconSize));
        plusButton.getElements().add(new CubicCurveTo(0.0, 0.7777777777777778 * iconSize,
                                                      0.2222222222222222 * iconSize, iconSize,
                                                      0.5 * iconSize, iconSize));
        plusButton.getElements().add(new CubicCurveTo(0.7777777777777778 * iconSize, iconSize,
                                                      iconSize, 0.7777777777777778 * iconSize,
                                                      iconSize, 0.5 * iconSize));
        plusButton.getElements().add(new CubicCurveTo(iconSize, 0.2222222222222222 * iconSize,
                                                      0.7777777777777778 * iconSize, 0.0,
                                                      0.5 * iconSize, 0.0));
        plusButton.getElements().add(new CubicCurveTo(0.2222222222222222 * iconSize, 0.0,
                                                      0.0, 0.2222222222222222 * iconSize,
                                                      0.0, 0.5 * iconSize));
        plusButton.getElements().add(new ClosePath());
        plusButton.getElements().add(new MoveTo(0.19444444444444445 * iconSize, 0.4583333333333333 * iconSize));
        plusButton.getElements().add(new LineTo(0.19444444444444445 * iconSize, 0.5694444444444444 * iconSize));
        plusButton.getElements().add(new LineTo(0.4444444444444444 * iconSize, 0.5694444444444444 * iconSize));
        plusButton.getElements().add(new LineTo(0.4444444444444444 * iconSize, 0.8194444444444444 * iconSize));
        plusButton.getElements().add(new LineTo(0.5555555555555556 * iconSize, 0.8194444444444444 * iconSize));
        plusButton.getElements().add(new LineTo(0.5555555555555556 * iconSize, 0.5694444444444444 * iconSize));
        plusButton.getElements().add(new LineTo(0.8055555555555556 * iconSize, 0.5694444444444444 * iconSize));
        plusButton.getElements().add(new LineTo(0.8055555555555556 * iconSize, 0.4583333333333333 * iconSize));
        plusButton.getElements().add(new LineTo(0.5555555555555556 * iconSize, 0.4583333333333333 * iconSize));
        plusButton.getElements().add(new LineTo(0.5555555555555556 * iconSize, 0.20833333333333334 * iconSize));
        plusButton.getElements().add(new LineTo(0.4444444444444444 * iconSize, 0.20833333333333334 * iconSize));
        plusButton.getElements().add(new LineTo(0.4444444444444444 * iconSize, 0.4583333333333333 * iconSize));
        plusButton.getElements().add(new LineTo(0.19444444444444445 * iconSize, 0.4583333333333333 * iconSize));
        plusButton.getElements().add(new ClosePath());
    }


    // ******************** Resizing ******************************************
    @Override protected void resizeDynamicText() {
        double maxWidth = size * 0.9;
        double fontSize = size * 0.24;
        valueText.setFont(Fonts.latoRegular(fontSize));
        if (valueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(valueText, maxWidth, fontSize); }
        valueText.relocate(size * 0.95 - valueText.getLayoutBounds().getWidth(), size * 0.15);
    };
    @Override protected void resizeStaticText() {
        double maxWidth = size * 0.9;
        double fontSize = size * 0.06;

        titleText.setFont(Fonts.latoRegular(fontSize));
        if (titleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(titleText, maxWidth, fontSize); }
        titleText.relocate(size * 0.05, size * 0.05);

        maxWidth = size * 0.9;
        fontSize = size * 0.1;
        unitText.setFont(Fonts.latoRegular(fontSize));
        if (unitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(unitText, maxWidth, fontSize); }
        unitText.relocate(size * 0.95 - unitText.getLayoutBounds().getWidth(), size * 0.42);
    };

    @Override protected void resize() {
        super.resize();

        minusButton.resize(size * 0.18, size * 0.18);
        minusButton.relocate(size * 0.05, size * 0.95 - minusButton.getLayoutBounds().getHeight());

        plusButton.resize(size * 0.18, size * 0.18);
        plusButton.relocate(size * 0.95 - plusButton.getLayoutBounds().getWidth(), size * 0.95 - plusButton.getLayoutBounds().getHeight());
    };

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(getSkinnable().getTitle());
        unitText.setText(getSkinnable().getUnit());

        resizeStaticText();

        titleText.setFill(getSkinnable().getTitleColor());
        valueText.setFill(getSkinnable().getValueColor());
        unitText.setFill(getSkinnable().getUnitColor());
        plusButton.setFill(getSkinnable().getForegroundColor());
        minusButton.setFill(getSkinnable().getForegroundColor());
    };
}
