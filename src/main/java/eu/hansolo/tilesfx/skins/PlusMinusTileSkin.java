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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

import static eu.hansolo.tilesfx.tools.Helper.clamp;


/**
 * Created by hansolo on 19.12.16.
 */
public class PlusMinusTileSkin extends TileSkin {
    private Text     titleText;
    private Text     text;
    private Text     valueText;
    private Text     unitText;
    private TextFlow valueUnitFlow;
    private Label    description;
    private Label    plusLabel;
    private Label    minusLabel;


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

        text = new Text(getSkinnable().getText());
        text.setFill(getSkinnable().getUnitColor());
        Helper.enableNode(text, getSkinnable().isTextVisible());

        valueText = new Text(String.format(locale, formatString, ((getSkinnable().getValue() - minValue) / range * 100)));
        valueText.setFill(getSkinnable().getValueColor());
        Helper.enableNode(valueText, getSkinnable().isValueVisible());

        unitText = new Text(getSkinnable().getUnit());
        unitText.setFill(getSkinnable().getUnitColor());
        Helper.enableNode(unitText, !getSkinnable().getUnit().isEmpty());

        valueUnitFlow = new TextFlow(valueText, unitText);
        valueUnitFlow.setTextAlignment(TextAlignment.RIGHT);

        description = new Label(getSkinnable().getDescription());
        description.setAlignment(Pos.TOP_RIGHT);
        description.setWrapText(true);
        description.setTextFill(getSkinnable().getTextColor());
        Helper.enableNode(description, !getSkinnable().getDescription().isEmpty());

        plusLabel = new Label("+");
        plusLabel.setAlignment(Pos.CENTER);
        plusLabel.setEffect(shadow);
        plusLabel.setPickOnBounds(false);
        
        minusLabel = new Label("-");
        minusLabel.setAlignment(Pos.CENTER);
        minusLabel.setEffect(shadow);
        minusLabel.setPickOnBounds(false);

        getPane().getChildren().addAll(titleText, text, valueUnitFlow, description, minusLabel, plusLabel);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        plusLabel.setOnMousePressed(e -> increment());
        plusLabel.setOnMouseReleased(e -> {
            plusLabel.setTextFill(getSkinnable().getForegroundColor());
            plusLabel.setBorder(new Border(new BorderStroke(getSkinnable().getForegroundColor(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(size * 0.01))));
        });
        minusLabel.setOnMousePressed(e -> decrement());
        minusLabel.setOnMouseReleased(e -> {
            minusLabel.setTextFill(getSkinnable().getForegroundColor());
            minusLabel.setBorder(new Border(new BorderStroke(getSkinnable().getForegroundColor(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(size * 0.01))));
        });
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
        valueText.setText(String.format(locale, formatString, VALUE));
        resizeDynamicText();
    };

    private void increment() {
        plusLabel.setTextFill(getSkinnable().getActiveColor());
        plusLabel.setBorder(new Border(new BorderStroke(getSkinnable().getActiveColor(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(size * 0.01))));
        double newValue = clamp(minValue, maxValue, getSkinnable().getValue() + getSkinnable().getIncrement());
        getSkinnable().setValue(newValue);
    }
    private void decrement() {
        minusLabel.setTextFill(getSkinnable().getActiveColor());
        minusLabel.setBorder(new Border(new BorderStroke(getSkinnable().getActiveColor(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(size * 0.01))));
        double newValue = clamp(minValue, maxValue, getSkinnable().getValue() - getSkinnable().getIncrement());
        getSkinnable().setValue(newValue);
    }


    // ******************** Resizing ******************************************
    @Override protected void resizeDynamicText() {
        double maxWidth = unitText.isVisible() ? size * 0.725 : size * 0.9;
        double fontSize = size * 0.24;
        valueText.setFont(Fonts.latoRegular(fontSize));
        if (valueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(valueText, maxWidth, fontSize); }
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

        fontSize = size * 0.1;
        description.setFont(Fonts.latoRegular(fontSize));
    };

    @Override protected void resize() {
        super.resize();

        description.setPrefSize(size * 0.9, size * 43);
        description.relocate(size * 0.05, size * 0.42);

        double buttonSize = size * 0.18;

        minusLabel.setFont(Fonts.latoBold(size * 0.2));
        minusLabel.setPrefSize(buttonSize, buttonSize);
        minusLabel.setMinSize(buttonSize, buttonSize);
        minusLabel.setMaxSize(buttonSize, buttonSize);
        minusLabel.setPadding(new Insets(-0.055 * size, 0, 0, 0));
        minusLabel.setBorder(new Border(new BorderStroke(getSkinnable().getForegroundColor(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(size * 0.01))));
        minusLabel.relocate(size * 0.05, size * 0.80 - buttonSize);
        
        plusLabel.setFont(Fonts.latoBold(size * 0.2));
        plusLabel.setPrefSize(buttonSize, buttonSize);
        plusLabel.setMinSize(buttonSize, buttonSize);
        plusLabel.setMaxSize(buttonSize, buttonSize);
        plusLabel.setPadding(new Insets(-0.05 * size, 0, 0, 0));
        plusLabel.setBorder(new Border(new BorderStroke(getSkinnable().getForegroundColor(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(size * 0.01))));
        plusLabel.relocate(size * 0.95 - buttonSize, size * 0.80 - buttonSize);

        valueUnitFlow.setPrefWidth(size * 0.9);
        valueUnitFlow.relocate(size * 0.05, size * 0.15);
    };

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(getSkinnable().getTitle());
        text.setText(getSkinnable().getText());
        unitText.setText(getSkinnable().getUnit());

        resizeStaticText();

        titleText.setFill(getSkinnable().getTitleColor());
        text.setFill(getSkinnable().getTextColor());
        valueText.setFill(getSkinnable().getValueColor());
        unitText.setFill(getSkinnable().getUnitColor());
        plusLabel.setTextFill(getSkinnable().getForegroundColor());
        minusLabel.setTextFill(getSkinnable().getForegroundColor());
    };
}
