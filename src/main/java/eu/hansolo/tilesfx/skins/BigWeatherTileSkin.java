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
import eu.hansolo.tilesfx.weather.DarkSky;
import eu.hansolo.tilesfx.weather.DarkSky.ConditionAndIcon;
import eu.hansolo.tilesfx.weather.WeatherSymbol;
import javafx.scene.text.Text;

import java.util.Locale;

import static eu.hansolo.tilesfx.tools.Helper.normalize;


/**
 * Created by hansolo on 27.12.16.
 */
public class BigWeatherTileSkin extends TileSkin {
    protected static final double        PREFERRED_WIDTH  = 510;
    protected static final double        PREFERRED_HEIGHT = 250;
    private                Text          titleText;
    private                Text          valueText;
    private                Text          unitText;
    private                WeatherSymbol weatherSymbol;
    private                Text          summaryText;
    private                DarkSky       darkSky;


    // ******************** Constructors **************************************
    public BigWeatherTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        darkSky = getSkinnable().getDarkSky();

        titleText = new Text(getSkinnable().getTitle());
        titleText.setFill(getSkinnable().getTitleColor());
        Helper.enableNode(titleText, !getSkinnable().getTitle().isEmpty());

        valueText = new Text();

        unitText = new Text(darkSky.getUnit().temperatureUnitString);

        summaryText = new Text("");
        summaryText.setFill(getSkinnable().getTextColor());

        weatherSymbol = new WeatherSymbol(ConditionAndIcon.NONE, 250, getSkinnable().getForegroundColor());

        getPane().getChildren().addAll(titleText, valueText, unitText, weatherSymbol, summaryText);
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


    // ******************** Resizing ******************************************
    @Override protected void resizeDynamicText() {
        double maxWidth = size * 0.9;
        double fontSize = size * 0.06;

        titleText.setFont(Fonts.latoRegular(fontSize));
        if (titleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(titleText, maxWidth, fontSize); }
        titleText.relocate(size * 0.05, size * 0.05);

        maxWidth = size * 0.15;
        unitText.setFont(Fonts.latoRegular(fontSize));
        if (unitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(unitText, maxWidth, fontSize); }
        unitText.relocate(size * 0.95 - unitText.getLayoutBounds().getWidth(), size * 0.36);

        maxWidth = unitText.isVisible() ? size * 0.725 : size * 0.9;
        fontSize = size * 0.24;
        valueText.setFont(Fonts.latoRegular(fontSize));
        if (valueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(valueText, maxWidth, fontSize); }
        if (unitText.isVisible()) {
            valueText.relocate(size * 0.925 - valueText.getLayoutBounds().getWidth() - unitText.getLayoutBounds().getWidth(), size * 0.1825);
        } else {
            valueText.relocate(size * 0.95 - valueText.getLayoutBounds().getWidth(), size * 0.1825);
        }

        maxWidth = size * 0.9;
        fontSize = size * 0.05;
        summaryText.setFont(Fonts.latoRegular(fontSize));
        if (summaryText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(summaryText, maxWidth, fontSize); }
        summaryText.relocate((size - summaryText.getLayoutBounds().getWidth()) * 0.5, size * 0.9);
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

        weatherSymbol.setPrefSize(size * 0.6, size * 0.6);
        weatherSymbol.relocate( size * 0.95 - weatherSymbol.getPrefWidth(), size * 0.8 - weatherSymbol.getPrefHeight());

        redraw();
    };

    @Override protected void redraw() {
        super.redraw();

        titleText.setText(getSkinnable().getTitle());
        resizeStaticText();

        valueText.setText(String.format(Locale.US, "%.0f", darkSky.getToday().getTemperature()));
        unitText.setText(darkSky.getUnit().temperatureUnitString);
        weatherSymbol.setCondition(darkSky.getToday().getCondition());
        summaryText.setText(normalize(darkSky.getToday().getSummary()));
        resizeDynamicText();

        titleText.setFill(getSkinnable().getTitleColor());
        valueText.setFill(getSkinnable().getValueColor());
        unitText.setFill(getSkinnable().getUnitColor());
        summaryText.setFill(getSkinnable().getTextColor());
        weatherSymbol.setSymbolColor(getSkinnable().getForegroundColor());
    };
}
