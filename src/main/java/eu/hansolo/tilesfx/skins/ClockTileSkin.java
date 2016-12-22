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
import javafx.geometry.VPos;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;


/**
 * Created by hansolo on 19.12.16.
 */
public class ClockTileSkin extends TileSkin {
    private DateTimeFormatter timeFormatter;
    private DateTimeFormatter dateFormatter;
    private DateTimeFormatter dayOfWeekFormatter;
    private Text              title;
    private Rectangle         timeRect;
    private Text              timeText;
    private Text              dayOfWeekText;
    private Text              dateText;


    // ******************** Constructors **************************************
    public ClockTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        timeFormatter      = DateTimeFormatter.ofPattern("HH:mm", getSkinnable().getLocale());
        dateFormatter      = DateTimeFormatter.ofPattern("dd MMM YYYY", getSkinnable().getLocale());
        dayOfWeekFormatter = DateTimeFormatter.ofPattern("EEEE", getSkinnable().getLocale());

        System.out.println(dayOfWeekFormatter.format(LocalDateTime.now()));

        title = new Text("");
        title.setTextOrigin(VPos.TOP);
        Helper.enableNode(title, !getSkinnable().getTitle().isEmpty());

        timeRect = new Rectangle();

        timeText = new Text(timeFormatter.format(getSkinnable().getTime()));
        timeText.setTextOrigin(VPos.CENTER);

        dateText = new Text(dateFormatter.format(getSkinnable().getTime()));

        dayOfWeekText = new Text(dayOfWeekFormatter.format(getSkinnable().getTime()));

        getPane().getChildren().addAll(title, timeRect, timeText, dateText, dayOfWeekText);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        if (getSkinnable().isAnimated()) {
            getSkinnable().currentTimeProperty().addListener(o -> updateTime(
                ZonedDateTime.ofInstant(Instant.ofEpochSecond(getSkinnable().getCurrentTime()), ZoneId.of(ZoneId.systemDefault().getId()))));
        } else {
            getSkinnable().timeProperty().addListener(o -> updateTime(getSkinnable().getTime()));
        }
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);
    };

    public void updateTime(final ZonedDateTime TIME) {
        timeText.setText(timeFormatter.format(getSkinnable().getTime()));
        timeText.setX((size - timeText.getLayoutBounds().getWidth()) * 0.5);
        timeText.setY(size * 0.4);

        dayOfWeekText.setText(dayOfWeekFormatter.format(TIME));
        dayOfWeekText.setX(size * 0.05);

        dateText.setText(dateFormatter.format(TIME));
        dateText.setX(size * 0.05);
    }


    // ******************** Resizing ******************************************
    @Override protected void resizeDynamicText() {
        double maxWidth = size * 0.9;
        double fontSize = size * 0.06;

        title.setFont(Fonts.latoRegular(fontSize));
        title.setText(getSkinnable().getTitle());
        if (title.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(title, maxWidth, fontSize); }
        title.setX(size * 0.05);
        title.setY(size * 0.05);

        maxWidth = size * 0.9;
        fontSize = size * 0.3;
        timeText.setFont(Fonts.latoRegular(fontSize));
        timeText.setText(timeFormatter.format(getSkinnable().getTime()));
        Helper.adjustTextSize(timeText, maxWidth, fontSize);
        timeText.setX((size - timeText.getLayoutBounds().getWidth()) * 0.5);
        timeText.setY(size * 0.4);

        maxWidth = size * 0.9;
        fontSize = size * 0.1;
        dayOfWeekText.setFont(Fonts.latoRegular(fontSize));
        if (dayOfWeekText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(dayOfWeekText, maxWidth, fontSize); }
        dayOfWeekText.setX(size * 0.05);
        dayOfWeekText.setY(size * 0.75);

        maxWidth = size * 0.9;
        dateText.setFont(Fonts.latoRegular(fontSize));
        if (dateText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(dateText, maxWidth, fontSize); }
        dateText.setX(size * 0.05);
        dateText.setY(size * 0.875);
    };

    @Override protected void resize() {
        super.resize();

        timeRect.setWidth(size);
        timeRect.setHeight(size * 0.4);
        timeRect.setX(0);
        timeRect.setY(size * 0.2);
    };

    @Override protected void redraw() {
        super.redraw();

        timeFormatter      = DateTimeFormatter.ofPattern("HH:mm", getSkinnable().getLocale());
        dateFormatter      = DateTimeFormatter.ofPattern("dd MMM YYYY", getSkinnable().getLocale());
        dayOfWeekFormatter = DateTimeFormatter.ofPattern("EEEE", getSkinnable().getLocale());

        ZonedDateTime time = getSkinnable().getTime();

        updateTime(time);

        resizeDynamicText();

        title.setFill(getSkinnable().getTitleColor());
        timeRect.setFill(getSkinnable().getBackgroundColor().darker());
        timeText.setFill(getSkinnable().getTitleColor());
        dateText.setFill(getSkinnable().getDateColor());
        dayOfWeekText.setFill(getSkinnable().getDateColor());
    };
}
