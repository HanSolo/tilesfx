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
import eu.hansolo.tilesfx.weather.DarkSky.Unit;
import eu.hansolo.tilesfx.weather.DataPoint;
import eu.hansolo.tilesfx.weather.WeatherSymbol;
import javafx.scene.text.Text;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static eu.hansolo.tilesfx.tools.Helper.normalize;


/**
 * Created by hansolo on 21.12.16.
 */
public class WeatherTileSkin extends TileSkin {
    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("HH:mm");
    private              Text              titleText;
    private              Text              valueText;
    private              Text              unitText;
    private              WeatherSymbol     weatherSymbol;
    private              Text              summaryText;
    private              WeatherSymbol     sunriseSymbol;
    private              WeatherSymbol     sunsetSymbol;
    private              Text              sunriseText;
    private              Text              sunsetText;
    private              DarkSky           darkSky;


    // ******************** Constructors **************************************
    public WeatherTileSkin(final Tile TILE) {
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

        sunriseSymbol = new WeatherSymbol(ConditionAndIcon.SUNRISE, 22, getSkinnable().getForegroundColor());
        sunsetSymbol = new WeatherSymbol(ConditionAndIcon.SUNSET, 22, getSkinnable().getForegroundColor());

        sunriseText = new Text("");
        sunriseText.setFill(getSkinnable().getTextColor());

        sunsetText = new Text("");
        sunsetText.setFill(getSkinnable().getTextColor());

        weatherSymbol = new WeatherSymbol(ConditionAndIcon.NONE, 250, getSkinnable().getForegroundColor());

        getPane().getChildren().addAll(titleText, valueText, unitText, weatherSymbol, summaryText,
                                       sunriseSymbol, sunsetSymbol, sunriseText, sunsetText);
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
        double fontSize = size * textSize.factor;

        titleText.setFont(Fonts.latoRegular(fontSize));
        if (titleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(titleText, maxWidth, fontSize); }
        titleText.relocate(size * 0.05, size * 0.05);

        maxWidth = unitText.isVisible() ? size * 0.725 : size * 0.9;
        fontSize = size * 0.24;
        valueText.setFont(Fonts.latoRegular(fontSize));
        if (valueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(valueText, maxWidth, fontSize); }
        if (unitText.isVisible()) {
            valueText.relocate(size * 0.925 - valueText.getLayoutBounds().getWidth() - unitText.getLayoutBounds().getWidth(), size * 0.15);
        } else {
            valueText.relocate(size * 0.95 - valueText.getLayoutBounds().getWidth(), size * 0.15);
        }

        maxWidth = size * 0.9;
        fontSize = size * textSize.factor;
        summaryText.setFont(Fonts.latoRegular(fontSize));
        if (summaryText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(summaryText, maxWidth, fontSize); }
        summaryText.setX((size - summaryText.getLayoutBounds().getWidth()) * 0.5);
        summaryText.setY(size * 0.95);

        maxWidth = size * 0.295;
        fontSize = size * 0.06;

        sunriseText.setFont(Fonts.latoRegular(fontSize));
        if (sunriseText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(sunriseText, maxWidth, fontSize); }
        sunriseText.relocate((size * 0.95 - sunriseText.getLayoutBounds().getWidth()), size * 0.6075);

        sunsetText.setFont(Fonts.latoRegular(fontSize));
        if (sunsetText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(sunsetText, maxWidth, fontSize); }
        sunsetText.relocate((size * 0.95 - sunsetText.getLayoutBounds().getWidth()), size * 0.725);
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

        weatherSymbol.setPrefSize(size * 0.6, size * 0.6);
        weatherSymbol.relocate( size * 0.05, size * 0.8 - weatherSymbol.getPrefHeight());

        sunriseSymbol.setPrefSize(size * 0.09, size * 0.09);
        sunriseSymbol.relocate(size * 0.695, size * 0.5925);

        sunsetSymbol.setPrefSize(size * 0.09, size * 0.09);
        sunsetSymbol.relocate(size * 0.695, size * 0.7125);

        redraw();
    };

    @Override protected void redraw() {
        super.redraw();

        titleText.setText(getSkinnable().getTitle());
        resizeStaticText();

        DataPoint today = darkSky.getToday();
        Unit      unit  = darkSky.getUnit();

        valueText.setText(String.format(Locale.US, "%.0f", today.getTemperature()));
        unitText.setText(unit.temperatureUnitString);
        weatherSymbol.setCondition(today.getCondition());
        summaryText.setText(normalize(today.getSummary()));
        sunriseText.setText(TF.format(today.getSunriseTime()));
        sunsetText.setText(TF.format(today.getSunsetTime()));
        resizeDynamicText();

        titleText.setFill(getSkinnable().getTitleColor());
        valueText.setFill(getSkinnable().getValueColor());
        unitText.setFill(getSkinnable().getUnitColor());
        summaryText.setFill(getSkinnable().getTextColor());
        weatherSymbol.setSymbolColor(getSkinnable().getForegroundColor());
        sunriseSymbol.setSymbolColor(getSkinnable().getForegroundColor());
        sunsetSymbol.setSymbolColor(getSkinnable().getForegroundColor());
    };
}
