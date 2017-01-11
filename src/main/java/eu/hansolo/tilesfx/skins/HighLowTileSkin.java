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
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
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
    private Text     text;
    private Text     valueText;
    private Text     unitText;
    private Label    description;
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

        text = new Text(getSkinnable().getUnit());
        text.setFill(getSkinnable().getUnitColor());
        Helper.enableNode(text, getSkinnable().isTextVisible());

        valueText = new Text(String.format(locale, formatString, getSkinnable().getValue()));
        valueText.setFill(getSkinnable().getValueColor());
        Helper.enableNode(valueText, getSkinnable().isValueVisible());

        unitText = new Text(getSkinnable().getUnit());
        unitText.setFill(getSkinnable().getUnitColor());
        Helper.enableNode(unitText, !getSkinnable().getUnit().isEmpty());

        description = new Label(getSkinnable().getDescription());
        description.setAlignment(Pos.TOP_RIGHT);
        description.setWrapText(true);
        description.setTextFill(getSkinnable().getTextColor());
        Helper.enableNode(description, !getSkinnable().getDescription().isEmpty());

        indicator = new SVGPath();
        indicator.setContent("M 14 8 C 15 7 16 7 17 8 C 17 8 30 20 30 20 C 32 22 31 24 28 24 C 28 24 3 24 3 24 C 0 24 -1 22 1 20 C 1 20 14 8 14 8 Z");
        indicator.resize(PREFERRED_WIDTH * 0.11034483, PREFERRED_HEIGHT * 0.06896552);
        indicator.setFill(state.color);

        referenceText = new Text(String.format(locale, formatString, getSkinnable().getReferenceValue()));
        referenceText.setFill(state.color);

        referenceUnitText = new Text(getSkinnable().getUnit());
        referenceUnitText.setFill(Tile.FOREGROUND);

        referenceUnitFlow = new TextFlow(referenceText, referenceUnitText);

        getPane().getChildren().addAll(titleText, text, valueText, unitText, description, indicator, referenceUnitFlow);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !getSkinnable().getTitle().isEmpty());
            Helper.enableNode(text, getSkinnable().isTextVisible());
            Helper.enableNode(valueText, getSkinnable().isValueVisible());
            Helper.enableNode(unitText, !getSkinnable().getUnit().isEmpty());
            Helper.enableNode(description, !getSkinnable().getDescription().isEmpty());
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

        maxWidth = size * 0.9;
        fontSize = size * textSize.factor;
        text.setText(getSkinnable().getText());
        text.setFont(Fonts.latoRegular(fontSize));
        if (text.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(text, maxWidth, fontSize); }
        text.setX(size * 0.05);
        text.setY(size * 0.95);

        maxWidth = size * 0.15;
        fontSize = size * 0.12;
        unitText.setFont(Fonts.latoRegular(fontSize));
        if (unitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(unitText, maxWidth, fontSize); }
        unitText.relocate(size * 0.95 - unitText.getLayoutBounds().getWidth(), size * 0.27);

        maxWidth = size * 0.45;
        fontSize = size * 0.18;
        referenceText.setFont(Fonts.latoRegular(fontSize));
        if (referenceText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(referenceText, maxWidth, fontSize); }

        maxWidth = size * 0.1;
        fontSize = size * 0.12;
        referenceUnitText.setFont(Fonts.latoRegular(fontSize));
        if (referenceUnitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(referenceUnitText, maxWidth, fontSize); }

        fontSize = size * 0.1;
        description.setFont(Fonts.latoRegular(fontSize));
    };

    @Override protected void resize() {
        super.resize();

        description.setPrefSize(size * 0.9, size * 43);
        description.relocate(size * 0.05, size * 0.42);

        setIndicatorSize(indicator, size * 0.11034483, size * 0.06896552);
        indicator.setLayoutX(size * 0.05);
        indicator.setLayoutY(size * 0.65);
        resizeStaticText();
        resizeDynamicText();
        referenceUnitFlow.relocate(size * 0.225, size * 0.595);
    };

    private void setIndicatorSize(final Node NODE, final double TARGET_WIDTH, final double TARGET_HEIGHT) {
        NODE.setScaleX(TARGET_WIDTH / NODE.getLayoutBounds().getWidth());
        NODE.setScaleY(TARGET_HEIGHT / NODE.getLayoutBounds().getHeight());
    }

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(getSkinnable().getTitle());
        text.setText(getSkinnable().getText());
        referenceText.setText(String.format(locale, "%." + getSkinnable().getTickLabelDecimals() + "f", getSkinnable().getReferenceValue()));
        unitText.setText(getSkinnable().getUnit());
        description.setText(getSkinnable().getDescription());

        resizeStaticText();

        titleText.setFill(getSkinnable().getTitleColor());
        text.setFill(getSkinnable().getTextColor());
        valueText.setFill(getSkinnable().getValueColor());
        unitText.setFill(getSkinnable().getUnitColor());
        description.setTextFill(getSkinnable().getDescriptionColor());
        referenceText.setFill(state.color);
        referenceUnitText.setFill(state.color);
        indicator.setFill(state.color);
    };
}
