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
import javafx.scene.text.Text;


/**
 * Created by hansolo on 20.12.16.
 */
public class TextTileSkin extends TileSkin {
    private Text titleText;
    private Text valueText;
    private Text unitText;


    // ******************** Constructors **************************************
    public TextTileSkin(final Tile TILE) {
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

        getPane().getChildren().addAll(titleText, valueText, unitText);
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
        valueText.setText(String.format(locale, formatString, VALUE));
        resizeDynamicText();
    };


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
    };

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(getSkinnable().getTitle());
        unitText.setText(getSkinnable().getUnit());

        resizeStaticText();

        titleText.setFill(getSkinnable().getTitleColor());
        valueText.setFill(getSkinnable().getValueColor());
        unitText.setFill(getSkinnable().getUnitColor());
    };
}
