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
import eu.hansolo.tilesfx.Tile.TextSize;
import eu.hansolo.tilesfx.Tile.TileColor;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.Helper;
import eu.hansolo.tilesfx.tools.SunMoonCalculator;
import eu.hansolo.tilesfx.weather.DarkSky.ConditionAndIcon;
import eu.hansolo.tilesfx.weather.WeatherSymbol;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;


public class EphemerisTileSkin extends TileSkin {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private SunMoonCalculator smc;
    private Text              titleText;
    private Text              text;
    
    private Circle            blueHourDotMorning;
    private Text              blueHourTitleMorning;
    private Text              blueHourSunriseText;
    private VBox              blueHourSunriseTextBox;
    private HBox              blueHourSunriseBox;

    private WeatherSymbol     sunriseSymbol;
    private Text              sunriseTitle;
    private Text              sunriseText;
    private VBox              sunriseTextBox;
    private HBox              sunriseBox;

    private Circle            goldenHourDotMorning;
    private Text              goldenHourTitleMorning;
    private Text              goldenHourSunriseText;
    private VBox              goldenHourSunriseTextBox;
    private HBox              goldenHourSunriseBox;

    private Circle            goldenHourDotEvening;
    private Text              goldenHourTitleEvening;
    private Text              goldenHourSunsetText;
    private VBox              goldenHourSunsetTextBox;
    private HBox              goldenHourSunsetBox;

    private WeatherSymbol     sunsetSymbol;
    private Text              sunsetTitle;
    private Text              sunsetText;
    private VBox              sunsetTextBox;
    private HBox              sunsetBox;

    private Circle            blueHourDotEvening;
    private Text              blueHourTitleEvening;
    private Text              blueHourSunsetText;
    private VBox              blueHourSunsetTextBox;
    private HBox              blueHourSunsetBox;
    
    private VBox              infoBoxMorning;
    private VBox              infoBoxEvening;
    private HBox              infoBox;


    // ******************** Constructors **************************************
    public EphemerisTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        LocalDateTime now = LocalDateTime.now();
        ZonedDateTime zdt = now.atZone(tile.getZoneId());

        try {
            smc = new SunMoonCalculator(zdt.getYear(), zdt.getMonthValue(), zdt.getDayOfMonth(), tile.getCurrentLocation().getLatitude(), tile.getCurrentLocation().getLongitude());
            smc.calcEphemeris(tile.getZoneId());
        } catch (Exception e) {
            smc = null;
        }
        titleText = new Text();
        titleText.setFill(tile.getTitleColor());
        Helper.enableNode(titleText, !tile.getTitle().isEmpty());

        blueHourDotMorning       = new Circle(14, TileColor.BLUE.color);
        blueHourTitleMorning     = new Text("Blue Hour");
        blueHourSunriseText      = new Text("--:--");
        blueHourSunriseTextBox   = new VBox(blueHourTitleMorning, blueHourSunriseText);
        blueHourSunriseBox       = new HBox(blueHourDotMorning, blueHourSunriseTextBox);
        blueHourSunriseBox.setAlignment(Pos.CENTER_LEFT);

        sunriseSymbol            = new WeatherSymbol(ConditionAndIcon.SUNRISE, 22, tile.getForegroundColor());
        sunriseTitle             = new Text("Sunrise");
        sunriseText              = new Text("--:--");
        sunriseTextBox           = new VBox(sunriseTitle, sunriseText);
        sunriseBox               = new HBox(sunriseSymbol, sunriseTextBox);
        sunriseBox.setAlignment(Pos.CENTER_LEFT);

        goldenHourDotMorning     = new Circle(14, TileColor.ORANGE.color);
        goldenHourTitleMorning   = new Text("Golden Hour");
        goldenHourSunriseText    = new Text("--:--");
        goldenHourSunriseTextBox = new VBox(goldenHourTitleMorning, goldenHourSunriseText);
        goldenHourSunriseBox     = new HBox(goldenHourDotMorning, goldenHourSunriseTextBox);
        goldenHourSunriseBox.setAlignment(Pos.CENTER_LEFT);

        goldenHourDotEvening     = new Circle(14, TileColor.ORANGE.color);
        goldenHourTitleEvening   = new Text("Golden Hour");
        goldenHourSunsetText     = new Text("--:--");
        goldenHourSunsetTextBox  = new VBox(goldenHourTitleEvening, goldenHourSunsetText);
        goldenHourSunsetBox      = new HBox(goldenHourDotEvening, goldenHourSunsetTextBox);
        goldenHourSunsetBox.setAlignment(Pos.CENTER_LEFT);

        sunsetSymbol             = new WeatherSymbol(ConditionAndIcon.SUNSET, 22, tile.getForegroundColor());
        sunsetTitle              = new Text("Sunset");
        sunsetText               = new Text("--:--");
        sunsetTextBox            = new VBox(sunsetTitle, sunsetText);
        sunsetBox                = new HBox(sunsetSymbol, sunsetTextBox);
        sunsetBox.setAlignment(Pos.CENTER_LEFT);

        blueHourDotEvening       = new Circle(14, TileColor.BLUE.color);
        blueHourTitleEvening     = new Text("Blue Hour");
        blueHourSunsetText       = new Text("--:--");
        blueHourSunsetTextBox    = new VBox(blueHourTitleEvening, blueHourSunsetText);
        blueHourSunsetBox        = new HBox(blueHourDotEvening, blueHourSunsetTextBox);
        blueHourSunsetBox.setAlignment(Pos.CENTER_LEFT);

        infoBoxMorning = new VBox(blueHourSunriseBox, sunriseBox, goldenHourSunriseBox);
        infoBoxEvening = new VBox(goldenHourSunsetBox, sunsetBox, blueHourSunsetBox);
        infoBox        = new HBox(infoBoxMorning, infoBoxEvening);
        infoBox.setAlignment(Pos.CENTER);

        text = new Text(tile.getText());
        Helper.enableNode(text, tile.isTextVisible());

        getPane().getChildren().addAll(titleText, text, infoBox);
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
        } else if ("RECALC".equals(EVENT_TYPE)) {
            calcEphemeris();
        }
    }

    private void calcEphemeris() {
        try { smc.setDate(LocalDate.now()); } catch (Exception e) {}
        smc.calcEphemeris(tile.getZoneId());

        blueHourSunriseText.setText(String.join(" - ", TIME_FORMATTER.format(smc.getSunriseCivil()), TIME_FORMATTER.format(smc.getSunriseBlueHour())));
        sunriseText.setText(TIME_FORMATTER.format(smc.getSunrise()));
        goldenHourSunriseText.setText(String.join(" - ", TIME_FORMATTER.format(smc.getSunrise()), TIME_FORMATTER.format(smc.getSunriseGoldenHour())));
        goldenHourSunsetText.setText(String.join(" - ", TIME_FORMATTER.format(smc.getSunsetGoldenHour()), TIME_FORMATTER.format(smc.getSunset())));
        sunsetText.setText(TIME_FORMATTER.format(smc.getSunset()));
        blueHourSunsetText.setText(String.join(" - ", TIME_FORMATTER.format(smc.getSunsetBlueHour()), TIME_FORMATTER.format(smc.getSunsetCivil())));
    }


    // ******************** Resizing ******************************************
    @Override protected void resizeDynamicText() {
        double fontSize = size * TextSize.SMALL.factor;
        Font   font     = Fonts.latoRegular(fontSize);

        blueHourSunriseText.setFont(font);
        sunriseText.setFont(font);
        goldenHourSunriseText.setFont(font);
        goldenHourSunsetText.setFont(font);
        sunsetText.setFont(font);
        blueHourSunsetText.setFont(font);

        fontSize = size * TextSize.SMALLER.factor;
        font = Fonts.latoRegular(fontSize);

        blueHourTitleMorning.setFont(font);
        sunriseTitle.setFont(font);
        goldenHourTitleMorning.setFont(font);
        goldenHourTitleEvening.setFont(font);
        sunsetTitle.setFont(font);
        blueHourTitleEvening.setFont(font);
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

        text.setText(tile.getText());
        text.setFont(font);
        if (text.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(text, maxWidth, fontSize); }
        switch(tile.getTextAlignment()) {
            default    :
            case LEFT  : text.setX(size * 0.05); break;
            case CENTER: text.setX((width - text.getLayoutBounds().getWidth()) * 0.5); break;
            case RIGHT : text.setX(width - (size * 0.05) - text.getLayoutBounds().getWidth()); break;
        }
        text.setY(height - size * 0.05);
    }

    @Override protected void resize() {
        super.resize();

        blueHourDotMorning.setRadius(size * 0.03);
        goldenHourDotMorning.setRadius(size * 0.03);
        blueHourDotEvening.setRadius(size * 0.03);
        goldenHourDotEvening.setRadius(size * 0.03);

        sunriseSymbol.setPrefSize(size * 0.07, size * 0.07);
        sunsetSymbol.setPrefSize(size * 0.07, size * 0.07);

        blueHourSunriseBox.setSpacing(size * 0.025);
        sunriseBox.setSpacing(size * 0.025);
        goldenHourSunriseBox.setSpacing(size * 0.025);
        goldenHourSunsetBox.setSpacing(size * 0.025);
        sunsetBox.setSpacing(size * 0.025);
        blueHourSunsetBox.setSpacing(size * 0.025);

        infoBoxMorning.setSpacing(contentBounds.getHeight() * 0.25);
        infoBoxEvening.setSpacing(contentBounds.getHeight() * 0.25);

        infoBox.setSpacing(width * 0.15);

        infoBox.setPrefSize(contentBounds.getWidth(), contentBounds.getHeight());
        infoBox.relocate(contentBounds.getX(), contentBounds.getY());
    }

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(tile.getTitle());
        blueHourSunriseText.setText(String.join(" - ", TIME_FORMATTER.format(smc.getSunriseCivil()), TIME_FORMATTER.format(smc.getSunriseBlueHour())));
        sunriseText.setText(TIME_FORMATTER.format(smc.getSunrise()));
        goldenHourSunriseText.setText(String.join(" - ", TIME_FORMATTER.format(smc.getSunrise()), TIME_FORMATTER.format(smc.getSunriseGoldenHour())));
        goldenHourSunsetText.setText(String.join(" - ", TIME_FORMATTER.format(smc.getSunsetGoldenHour()), TIME_FORMATTER.format(smc.getSunset())));
        sunsetText.setText(TIME_FORMATTER.format(smc.getSunset()));
        blueHourSunsetText.setText(String.join(" - ", TIME_FORMATTER.format(smc.getSunsetBlueHour()), TIME_FORMATTER.format(smc.getSunsetCivil())));

        resizeDynamicText();
        resizeStaticText();

        titleText.setFill(tile.getTitleColor());
        text.setFill(tile.getTextColor());

        blueHourDotMorning.setFill(TileColor.BLUE.color);
        blueHourTitleMorning.setFill(tile.getTextColor());
        blueHourSunriseText.setFill(tile.getTextColor());

        sunriseSymbol.setSymbolColor(tile.getTextColor());
        sunriseTitle.setFill(tile.getTextColor());
        sunriseText.setFill(tile.getTextColor());

        goldenHourDotMorning.setFill(TileColor.ORANGE.color);
        goldenHourTitleMorning.setFill(tile.getTextColor());
        goldenHourSunriseText.setFill(tile.getTextColor());

        goldenHourDotEvening.setFill(TileColor.ORANGE.color);
        goldenHourTitleEvening.setFill(tile.getTextColor());
        goldenHourSunsetText.setFill(tile.getTextColor());

        sunsetSymbol.setSymbolColor(tile.getTextColor());
        sunsetTitle.setFill(tile.getTextColor());
        sunsetText.setFill(tile.getTextColor());

        blueHourDotEvening.setFill(TileColor.BLUE.color);
        blueHourTitleEvening.setFill(tile.getTextColor());
        blueHourSunsetText.setFill(tile.getTextColor());
    }
}
