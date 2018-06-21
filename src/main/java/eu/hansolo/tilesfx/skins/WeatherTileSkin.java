/*
 * Copyright (c) 2017 by Gerrit Grunwald
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
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static eu.hansolo.tilesfx.tools.Helper.normalize;


/**
 * Created by hansolo on 21.12.16.
 */
public class WeatherTileSkin extends TileSkin {
    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("HH:mm");
    private              Text          titleText;
    private              Text          valueText;
    private              Text          unitText;
    private              WeatherSymbol weatherSymbol;
    private              Text          text;
    private              WeatherSymbol sunriseSymbol;
    private              WeatherSymbol sunsetSymbol;
    private              Text          sunriseText;
    private              Text          sunsetText;
    private              HBox          sunriseBox;
    private              HBox          sunsetBox;
    private              DarkSky       darkSky;


    // ******************** Constructors **************************************
    public WeatherTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        darkSky = tile.getDarkSky();

        titleText = new Text(tile.getTitle());
        titleText.setFill(tile.getTitleColor());
        Helper.enableNode(titleText, !tile.getTitle().isEmpty());

        valueText = new Text();

        unitText = new Text(darkSky.getUnit().temperatureUnitString);

        text = new Text("");
        text.setFill(tile.getTextColor());

        sunriseSymbol = new WeatherSymbol(ConditionAndIcon.SUNRISE, 22, tile.getForegroundColor());
        sunsetSymbol = new WeatherSymbol(ConditionAndIcon.SUNSET, 22, tile.getForegroundColor());

        sunriseText = new Text("");
        sunriseText.setTextOrigin(VPos.CENTER);
        sunriseText.setFill(tile.getTextColor());

        sunsetText = new Text("");
        sunsetText.setTextOrigin(VPos.CENTER);
        sunsetText.setFill(tile.getTextColor());

        sunriseBox = new HBox(sunriseSymbol, sunriseText);
        sunriseBox.setAlignment(Pos.CENTER_RIGHT);

        sunsetBox = new HBox(sunsetSymbol, sunsetText);
        sunsetBox.setAlignment(Pos.CENTER_RIGHT);

        weatherSymbol = new WeatherSymbol(ConditionAndIcon.NONE, 250, tile.getForegroundColor());

        getPane().getChildren().addAll(titleText, valueText, unitText, weatherSymbol, text, sunriseBox, sunsetBox);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !tile.getTitle().isEmpty());
            Helper.enableNode(valueText, tile.isValueVisible());
            Helper.enableNode(unitText, !tile.getUnit().isEmpty());
        }
    }


    // ******************** Resizing ******************************************
    @Override protected void resizeDynamicText() {
        double maxWidth = width - size * 0.1;
        double fontSize = size * textSize.factor;

        titleText.setFont(Fonts.latoRegular(fontSize));
        if (titleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(titleText, maxWidth, fontSize); }
        titleText.relocate(size * 0.05, size * 0.05);

        maxWidth = unitText.isVisible() ? width - size * 0.275 : width - size * 0.1;
        fontSize = size * 0.24;
        valueText.setFont(Fonts.latoRegular(fontSize));
        if (valueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(valueText, maxWidth, fontSize); }
        if (unitText.isVisible()) {
            valueText.relocate(width - size * 0.075 - valueText.getLayoutBounds().getWidth() - unitText.getLayoutBounds().getWidth(), size * 0.15);
        } else {
            valueText.relocate(width - size * 0.05 - valueText.getLayoutBounds().getWidth(), size * 0.15);
        }

        maxWidth = width - size * 0.1;
        fontSize = size * textSize.factor;
        text.setFont(Fonts.latoRegular(fontSize));
        if (text.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(text, maxWidth, fontSize); }
        text.setX(size * 0.05);
        text.setY(height - size * 0.05);

        Helper.fitNodeWidth(text, maxWidth);

        maxWidth = width - size * 0.705;
        fontSize = size * 0.06;

        sunriseText.setFont(Fonts.latoRegular(fontSize));
        if (sunriseText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(sunriseText, maxWidth, fontSize); }

        sunsetText.setFont(Fonts.latoRegular(fontSize));
        if (sunsetText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(sunsetText, maxWidth, fontSize); }
    }
    @Override protected void resizeStaticText() {
        double maxWidth = width - size * 0.1;
        double fontSize = size * textSize.factor;

        boolean customFontEnabled = tile.isCustomFontEnabled();
        Font    customFont        = tile.getCustomFont();
        Font    font              = (customFontEnabled && customFont != null) ? Font.font(customFont.getFamily(), fontSize) : Fonts.latoRegular(fontSize);

        titleText.setFont(font);
        if (titleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(titleText, maxWidth, fontSize); }
        switch(tile.getTitleAlignment()) {
            default    :
            case LEFT  : titleText.relocate(size * 0.05, size * 0.05); break;
            case CENTER: titleText.relocate((width - titleText.getLayoutBounds().getWidth()) * 0.5, size * 0.05); break;
            case RIGHT : titleText.relocate(width - (size * 0.05) - titleText.getLayoutBounds().getWidth(), size * 0.05); break;
        }

        maxWidth = width - size * 0.275;
        fontSize = size * 0.12;
        unitText.setFont(Fonts.latoRegular(fontSize));
        if (unitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(unitText, maxWidth, fontSize); }
        unitText.relocate(width - size * 0.05 - unitText.getLayoutBounds().getWidth(), size * 0.27);
    }

    @Override protected void resize() {
        super.resize();

        weatherSymbol.setPrefSize(width * 0.5, height - size * 0.4);
        weatherSymbol.relocate( contentBounds.getX(), (height - weatherSymbol.getPrefHeight()) * 0.5);

        sunriseBox.setPrefSize(width - size * 0.1, size * 0.09);
        sunriseBox.setSpacing(size * 0.025);
        sunriseBox.relocate(contentBounds.getX(), height * 0.6075 + size * 0.0);

        sunsetBox.setPrefSize(width - size * 0.1, size * 0.09);
        sunsetBox.setSpacing(size * 0.025);
        sunsetBox.relocate(contentBounds.getX(), height * 0.725 + size * 0.0);

        sunriseSymbol.setPrefSize(size * 0.1, size * 0.1);
        sunsetSymbol.setPrefSize(size * 0.1, size * 0.1);

        redraw();
    }

    @Override protected void redraw() {
        super.redraw();

        titleText.setText(tile.getTitle());
        resizeStaticText();

        DataPoint today = darkSky.getToday();
        Unit      unit  = darkSky.getUnit();

        valueText.setText(String.format(Locale.US, "%.0f", today.getTemperature()));
        unitText.setText(unit.temperatureUnitString);
        weatherSymbol.setCondition(today.getCondition());
        text.setText(normalize(today.getSummary()));
        sunriseText.setText(TF.format(today.getSunriseTime()));
        sunsetText.setText(TF.format(today.getSunsetTime()));
        resizeDynamicText();

        titleText.setFill(tile.getTitleColor());
        valueText.setFill(tile.getValueColor());
        unitText.setFill(tile.getUnitColor());
        text.setFill(tile.getTextColor());
        weatherSymbol.setSymbolColor(tile.getForegroundColor());
        sunriseSymbol.setSymbolColor(tile.getForegroundColor());
        sunsetSymbol.setSymbolColor(tile.getForegroundColor());
    }
}
