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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;


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
    private Path      triangle;
    private StackPane indicatorPane;
    private Text      titleText;
    private Text      text;
    private Text      valueText;
    private Text      unitText;
    private TextFlow  valueUnitFlow;
    private Label     description;
    private Text      referenceText;
    private Text      referenceUnitText;
    private TextFlow  referenceUnitFlow;
    private State     state;


    // ******************** Constructors **************************************
    public HighLowTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        updateState(tile.getValue(), tile.getReferenceValue());

        titleText = new Text();
        titleText.setFill(tile.getTitleColor());
        Helper.enableNode(titleText, !tile.getTitle().isEmpty());

        text = new Text(tile.getUnit());
        text.setFill(tile.getUnitColor());
        Helper.enableNode(text, tile.isTextVisible());

        valueText = new Text(String.format(locale, formatString, tile.getValue()));
        valueText.setFill(tile.getValueColor());
        Helper.enableNode(valueText, tile.isValueVisible());

        unitText = new Text(tile.getUnit());
        unitText.setFill(tile.getUnitColor());
        Helper.enableNode(unitText, !tile.getUnit().isEmpty());

        valueUnitFlow = new TextFlow(valueText, unitText);
        valueUnitFlow.setTextAlignment(TextAlignment.RIGHT);

        description = new Label(tile.getDescription());
        description.setAlignment(tile.getDescriptionAlignment());
        description.setWrapText(true);
        description.setTextFill(tile.getTextColor());
        Helper.enableNode(description, !tile.getDescription().isEmpty());
        
        triangle = new Path();
        triangle.setStroke(null);
        triangle.setFill(state.color);
        indicatorPane = new StackPane(triangle);
        
        referenceText = new Text(String.format(locale, "%." + tile.getTickLabelDecimals() + "f", tile.getReferenceValue()));
        referenceText.setFill(state.color);

        referenceUnitText = new Text(tile.getUnit());
        referenceUnitText.setFill(Tile.FOREGROUND);

        referenceUnitFlow = new TextFlow(indicatorPane, referenceText, referenceUnitText);
        referenceUnitFlow.setTextAlignment(TextAlignment.LEFT);

        getPane().getChildren().addAll(titleText, text, valueUnitFlow, description, referenceUnitFlow);

        //handleCurrentValue(tile.getValue());
    }

    @Override protected void registerListeners() {
        super.registerListeners();
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !tile.getTitle().isEmpty());
            Helper.enableNode(text, tile.isTextVisible());
            Helper.enableNode(valueText, tile.isValueVisible());
            Helper.enableNode(unitText, !tile.getUnit().isEmpty());
            Helper.enableNode(description, !tile.getDescription().isEmpty());
        }
    };

    @Override protected void handleCurrentValue(final double VALUE) {
        updateState(VALUE, tile.getReferenceValue());
        valueText.setText(String.format(locale, formatString, VALUE));
        referenceText.setText(String.format(locale, "%." + tile.getTickLabelDecimals() + "f", tile.getReferenceValue()));
        resizeDynamicText();

        RotateTransition rotateTransition = new RotateTransition(Duration.millis(200), triangle);
        rotateTransition.setFromAngle(triangle.getRotate());
        rotateTransition.setToAngle(state.angle);

        FillTransition fillIndicatorTransition = new FillTransition(Duration.millis(200), triangle);
        fillIndicatorTransition.setFromValue((Color) triangle.getFill());
        fillIndicatorTransition.setToValue(state.color);

        FillTransition fillReferenceTransition = new FillTransition(Duration.millis(200), referenceText);
        fillReferenceTransition.setFromValue((Color) triangle.getFill());
        fillReferenceTransition.setToValue(state.color);

        FillTransition fillReferenceUnitTransition = new FillTransition(Duration.millis(200), referenceUnitText);
        fillReferenceUnitTransition.setFromValue((Color) triangle.getFill());
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

    private void drawTriangle() {
        MoveTo       moveTo        = new MoveTo(0.056 * size, 0.032 * size);
        CubicCurveTo cubicCurveTo1 = new CubicCurveTo(0.060 * size, 0.028 * size, 0.064 * size, 0.028 * size, 0.068 * size, 0.032 * size);
        CubicCurveTo cubicCurveTo2 = new CubicCurveTo(0.068 * size, 0.032 * size, 0.120 * size, 0.080 * size, 0.12 * size,  0.080 * size);
        CubicCurveTo cubicCurveTo3 = new CubicCurveTo(0.128 * size, 0.088 * size, 0.124 * size, 0.096 * size, 0.112 * size, 0.096 * size);
        CubicCurveTo cubicCurveTo4 = new CubicCurveTo(0.112 * size, 0.096 * size, 0.012 * size, 0.096 * size, 0.012 * size, 0.096 * size);
        CubicCurveTo cubicCurveTo5 = new CubicCurveTo(0.0, 0.096 * size, -0.004 * size, 0.088 * size, 0.004 * size, 0.080 * size);
        CubicCurveTo cubicCurveTo6 = new CubicCurveTo(0.004 * size, 0.080 * size, 0.056 * size, 0.032 * size, 0.056 * size, 0.032 * size);
        ClosePath    closePath     = new ClosePath();
        triangle.getElements().setAll(moveTo, cubicCurveTo1, cubicCurveTo2, cubicCurveTo3, cubicCurveTo4, cubicCurveTo5, cubicCurveTo6, closePath);
    }


    // ******************** Resizing ******************************************
    @Override protected void resizeDynamicText() {
        double maxWidth = unitText.isVisible() ? width - size * 0.275 : width - size * 0.1;
        double fontSize = 0.24 * size;
        valueText.setFont(Fonts.latoRegular(fontSize));
        if (valueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(valueText, maxWidth, fontSize); }
    };
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

        fontSize = size * textSize.factor;
        text.setText(tile.getText());
        text.setFont(Fonts.latoRegular(fontSize));
        if (text.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(text, maxWidth, fontSize); }
        switch(tile.getTextAlignment()) {
            default    :
            case LEFT  : text.setX(size * 0.05); break;
            case CENTER: text.setX((width - text.getLayoutBounds().getWidth()) * 0.5); break;
            case RIGHT : text.setX(width - (size * 0.05) - text.getLayoutBounds().getWidth()); break;
        }
        text.setY(height - size * 0.05);

        maxWidth = width - size * 0.275;
        fontSize = size * 0.12;
        unitText.setFont(Fonts.latoRegular(fontSize));
        if (unitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(unitText, maxWidth, fontSize); }

        maxWidth = width - size * 0.55;
        fontSize = size * 0.18;
        referenceText.setFont(Fonts.latoRegular(fontSize));
        if (referenceText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(referenceText, maxWidth, fontSize); }

        maxWidth = width - size * 0.9;
        fontSize = size * 0.12;
        referenceUnitText.setFont(Fonts.latoRegular(fontSize));
        if (referenceUnitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(referenceUnitText, maxWidth, fontSize); }

        fontSize = size * 0.1;
        description.setFont(Fonts.latoRegular(fontSize));
    };

    @Override protected void resize() {
        super.resize();

        description.setPrefSize(width - size * 0.1, size * 0.43);
        description.relocate(size * 0.05, height * 0.42);

        drawTriangle();
        indicatorPane.setPadding(new Insets(0, size * 0.035, 0, 0));

        resizeStaticText();
        resizeDynamicText();
        referenceUnitFlow.setPrefWidth(width - size * 0.1);
        referenceUnitFlow.relocate(size * 0.05, height * 0.595);

        valueUnitFlow.setPrefWidth(width - size * 0.1);
        valueUnitFlow.relocate(size * 0.05, size * 0.15);
    };

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(tile.getTitle());
        text.setText(tile.getText());
        referenceText.setText(String.format(locale, "%." + tile.getTickLabelDecimals() + "f", tile.getReferenceValue()));
        unitText.setText(tile.getUnit());
        description.setText(tile.getDescription());
        description.setAlignment(tile.getDescriptionAlignment());

        resizeStaticText();

        titleText.setFill(tile.getTitleColor());
        text.setFill(tile.getTextColor());
        valueText.setFill(tile.getValueColor());
        unitText.setFill(tile.getUnitColor());
        description.setTextFill(tile.getDescriptionColor());
        referenceText.setFill(state.color);
        referenceUnitText.setFill(state.color);
        triangle.setFill(state.color);
    };
}
