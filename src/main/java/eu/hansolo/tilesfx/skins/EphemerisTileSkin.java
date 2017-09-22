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
import eu.hansolo.tilesfx.tools.SunMoonCalculator;
import javafx.scene.text.Text;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;


public class EphemerisTileSkin extends TileSkin {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private SunMoonCalculator smc;
    private Text              titleText;
    private Text              text;
    private Text              blueHourSunriseText;
    private Text              sunriseText;
    private Text              goldenHourSunriseText;
    private Text              goldenHourSunsetText;
    private Text              sunsetText;
    private Text              blueHourSunsetText;


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

        blueHourSunriseText   = new Text();
        sunriseText           = new Text();
        goldenHourSunriseText = new Text();
        goldenHourSunsetText  = new Text();
        sunsetText            = new Text();
        blueHourSunsetText    = new Text();

        text = new Text(tile.getText());
        Helper.enableNode(text, tile.isTextVisible());

        getPane().getChildren().addAll(titleText, text, blueHourSunriseText, sunriseText, goldenHourSunriseText, goldenHourSunsetText, sunsetText, blueHourSunsetText);
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
    };

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
        double maxWidth = width - size * 0.1;
        double fontSize = size * textSize.factor;

        blueHourSunriseText.setFont(Fonts.latoRegular(fontSize));
        if (blueHourSunriseText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(blueHourSunriseText, maxWidth, fontSize); }
        blueHourSunriseText.relocate(size * 0.05, size * 0.175);

        sunriseText.setFont(Fonts.latoRegular(fontSize));
        if (sunriseText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(sunriseText, maxWidth, fontSize); }
        sunriseText.relocate(size * 0.05, size * 0.275);

        goldenHourSunriseText.setFont(Fonts.latoRegular(fontSize));
        if (goldenHourSunriseText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(goldenHourSunriseText, maxWidth, fontSize); }
        goldenHourSunriseText.relocate(size * 0.05, size * 0.375);

        goldenHourSunsetText.setFont(Fonts.latoRegular(fontSize));
        if (goldenHourSunsetText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(goldenHourSunsetText, maxWidth, fontSize); }
        goldenHourSunsetText.relocate(size * 0.05, size * 0.575);

        sunsetText.setFont(Fonts.latoRegular(fontSize));
        if (sunsetText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(sunsetText, maxWidth, fontSize); }
        sunsetText.relocate(size * 0.05, size * 0.675);

        blueHourSunsetText.setFont(Fonts.latoRegular(fontSize));
        if (blueHourSunsetText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(blueHourSunsetText, maxWidth, fontSize); }
        blueHourSunsetText.relocate(size * 0.05, size * 0.775);
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
    };

    @Override protected void resize() {
        super.resize();
    };

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
        blueHourSunriseText.setFill(tile.getTextColor());
        sunriseText.setFill(tile.getTextColor());
        goldenHourSunriseText.setFill(tile.getTextColor());
        goldenHourSunsetText.setFill(tile.getTextColor());
        sunsetText.setFill(tile.getTextColor());
        blueHourSunsetText.setFill(tile.getTextColor());
    };
}
