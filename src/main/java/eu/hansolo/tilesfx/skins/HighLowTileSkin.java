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
import javafx.animation.FillTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import static eu.hansolo.tilesfx.tools.Helper.clamp;


/**
 * Created by hansolo on 19.12.16.
 */
public class HighLowTileSkin extends TileSkin {
    private enum State {
        INCREASE(Tile.GREEN, 0),
        DECREASE(Tile.RED, 180),
        CONSTANT(Tile.ORANGE, 90);

        public final Color  color;
        public final double angle;

        State(final Color COLOR, final double ANGLE) {
            color = COLOR;
            angle = ANGLE;
        }
    }
    private SVGPath  indicator;
    private Text     titleText;
    private Text     valueText;
    private Text     unitText;
    private TextFlow valueUnitFlow;
    private Text     referenceText;
    private Text     referenceUnitText;
    private TextFlow referenceUnitFlow;
    private State    state;


    // ******************** Constructors **************************************
    public HighLowTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        updateState(getSkinnable().getValue(), getSkinnable().getReferenceValue());

        titleText = new Text();
        titleText.setFill(getSkinnable().getTitleColor());
        Helper.enableNode(titleText, !getSkinnable().getTitle().isEmpty());

        valueText = new Text(String.format(locale, formatString, getSkinnable().getValue()));
        valueText.setFill(getSkinnable().getValueColor());
        Helper.enableNode(valueText, getSkinnable().isValueVisible());

        unitText = new Text(getSkinnable().getUnit());
        unitText.setFill(getSkinnable().getUnitColor());
        Helper.enableNode(unitText, !getSkinnable().getUnit().isEmpty());

        valueUnitFlow = new TextFlow(valueText, unitText);

        indicator = new SVGPath();
        indicator.setContent("M 14 8 C 15 7 16 7 17 8 C 17 8 30 20 30 20 C 32 22 31 24 28 24 C 28 24 3 24 3 24 C 0 24 -1 22 1 20 C 1 20 14 8 14 8 Z");
        indicator.resize(PREFERRED_WIDTH * 0.11034483, PREFERRED_HEIGHT * 0.11034483);
        indicator.setFill(state.color);

        referenceText = new Text(String.format(locale, formatString, getSkinnable().getReferenceValue()));
        referenceText.setFill(state.color);

        referenceUnitText = new Text(getSkinnable().getUnit());
        referenceUnitText.setFill(Tile.FOREGROUND);

        referenceUnitFlow = new TextFlow(referenceText, referenceUnitText);

        getPane().getChildren().addAll(titleText, valueUnitFlow, indicator, referenceUnitFlow);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
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
        updateState(VALUE, getSkinnable().getReferenceValue());
        valueText.setText(String.format(locale, formatString, VALUE));
        referenceText.setText(String.format(locale, formatString, getSkinnable().getReferenceValue()));

        resizeDynamicText();

        RotateTransition rotateTransition = new RotateTransition(Duration.millis(200), indicator);
        rotateTransition.setFromAngle(indicator.getRotate());
        rotateTransition.setToAngle(state.angle);

        FillTransition fillIndicatorTransition = new FillTransition(Duration.millis(200), indicator);
        fillIndicatorTransition.setFromValue((Color) indicator.getFill());
        fillIndicatorTransition.setToValue(state.color);

        FillTransition fillReferenceTransition = new FillTransition(Duration.millis(200), referenceText);
        fillReferenceTransition.setFromValue((Color) indicator.getFill());
        fillReferenceTransition.setToValue(state.color);

        FillTransition fillReferenceUnitTransition = new FillTransition(Duration.millis(200), referenceUnitText);
        fillReferenceUnitTransition.setFromValue((Color) indicator.getFill());
        fillReferenceUnitTransition.setToValue(state.color);

        ParallelTransition parallelTransition = new ParallelTransition(rotateTransition, fillIndicatorTransition, fillReferenceTransition, fillReferenceUnitTransition);
        parallelTransition.play();
    };

    private void updateState(final double VALUE, final double REFERENCE_VALUE) {
        if (Double.compare(VALUE, REFERENCE_VALUE) > 0) {
            state = State.INCREASE;
        } else if (Double.compare(VALUE, REFERENCE_VALUE) < 0) {
            state = State.DECREASE;
        } else {
            state = State.CONSTANT;
        }
    }


    // ******************** Resizing ******************************************
    @Override protected void resizeDynamicText() {
        double maxWidth = unitText.isVisible() ? size * 0.725 : size * 0.9;
        double fontSize = 0.24 * size;
        valueText.setFont(Fonts.latoRegular(fontSize));
        if (valueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(valueText, maxWidth, fontSize); }
    };
    @Override protected void resizeStaticText() {
        double maxWidth = size * 0.9;
        double fontSize = size * 0.06;

        titleText.setFont(Fonts.latoRegular(fontSize));
        if (titleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(titleText, maxWidth, fontSize); }
        titleText.relocate(size * 0.05, size * 0.05);

        maxWidth = size * 0.15;
        unitText.setFont(Fonts.latoRegular(fontSize));
        if (unitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(unitText, maxWidth, fontSize); }

        maxWidth = size * 0.45;
        fontSize = size * 0.18;
        referenceText.setFont(Fonts.latoRegular(fontSize));
        if (referenceText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(referenceText, maxWidth, fontSize); }

        maxWidth = size * 0.1;
        fontSize = size * 0.12;
        referenceUnitText.setFont(Fonts.latoRegular(fontSize));
        if (referenceUnitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(referenceUnitText, maxWidth, fontSize); }
    };

    @Override protected void resize() {
        super.resize();
        indicator.resize(size * 0.11034483, size * 0.11034483);
        indicator.setLayoutX(size * 0.05);
        indicator.setLayoutY(size * 0.75);
        resizeStaticText();
        resizeDynamicText();
        valueUnitFlow.relocate(size * 0.05, size * 0.15);
        referenceUnitFlow.relocate(size * 0.225, size * 0.695);
    };

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(getSkinnable().getTitle());
        referenceText.setText(String.format(locale, "%." + getSkinnable().getTickLabelDecimals() + "f", getSkinnable().getReferenceValue()));

        resizeStaticText();

        titleText.setFill(getSkinnable().getTitleColor());
        valueText.setFill(getSkinnable().getValueColor());
        unitText.setFill(getSkinnable().getUnitColor());
        referenceText.setFill(state.color);
        referenceUnitText.setFill(state.color);
        indicator.setFill(state.color);
    };
}

