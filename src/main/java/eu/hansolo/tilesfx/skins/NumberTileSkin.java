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
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;


/**
 * Created by hansolo on 20.12.16.
 */
public class NumberTileSkin extends TileSkin {
    private Text     titleText;
    private Text     numberText;
    private Text     unitText;
    private TextFlow valueText;
    private Label    text;


    // ******************** Constructors **************************************
    public NumberTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        titleText = new Text();
        titleText.setFill(getSkinnable().getTitleColor());
        Helper.enableNode(titleText, !getSkinnable().getTitle().isEmpty());

        numberText = new Text(String.format(locale, formatString, ((getSkinnable().getValue() - minValue) / range * 100)));
        numberText.setFill(getSkinnable().getValueColor());
        Helper.enableNode(numberText, getSkinnable().isValueVisible());

        unitText = new Text(" " + getSkinnable().getUnit());
        unitText.setFill(getSkinnable().getUnitColor());
        Helper.enableNode(unitText, !getSkinnable().getUnit().isEmpty());

        valueText = new TextFlow(numberText, unitText);
        valueText.setPrefWidth(PREFERRED_WIDTH * 0.9);

        text = new Label(getSkinnable().getText());
        text.setAlignment(Pos.TOP_LEFT);
        text.setWrapText(true);
        text.setTextFill(getSkinnable().getTextColor());
        Helper.enableNode(text, getSkinnable().isTextVisible());

        getPane().getChildren().addAll(titleText, valueText, text);
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
        numberText.setText(String.format(locale, formatString, VALUE));
        resizeDynamicText();
    };


    // ******************** Resizing ******************************************
    @Override protected void resizeDynamicText() {
        double maxWidth = size * 0.9;
        double fontSize = size * 0.24;
        numberText.setFont(Fonts.latoRegular(fontSize));
    };
    @Override protected void resizeStaticText() {
        double maxWidth = size * 0.9;
        double fontSize = size * 0.06;

        titleText.setFont(Fonts.latoRegular(fontSize));
        if (titleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(titleText, maxWidth, fontSize); }
        titleText.relocate(size * 0.05, size * 0.05);

        fontSize = size * 0.12;
        unitText.setFont(Fonts.latoRegular(fontSize));

        fontSize = size * 0.1;
        text.setFont(Fonts.latoRegular(fontSize));
    };

    @Override protected void resize() {
        super.resize();

        valueText.setPrefWidth(size * 0.9);
        valueText.relocate(size * 0.05, size * 0.15);

        text.setPrefSize(size * 0.9, size * 43);
        text.relocate(size * 0.05, size * 0.42);
    };

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(getSkinnable().getTitle());
        unitText.setText(" " + getSkinnable().getUnit());
        text.setText(getSkinnable().getText());

        resizeDynamicText();
        resizeStaticText();

        titleText.setFill(getSkinnable().getTitleColor());
        numberText.setFill(getSkinnable().getValueColor());
        unitText.setFill(getSkinnable().getUnitColor());
        text.setTextFill(getSkinnable().getTextColor());
    };
}
