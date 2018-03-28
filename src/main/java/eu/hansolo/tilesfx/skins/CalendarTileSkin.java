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
import eu.hansolo.tilesfx.chart.ChartData;
import eu.hansolo.tilesfx.events.TileEvent;
import eu.hansolo.tilesfx.events.TileEvent.EventType;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.Helper;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;


public class CalendarTileSkin extends TileSkin {
    private static final DateTimeFormatter        DAY_FORMATTER        = DateTimeFormatter.ofPattern("EEEE");
    private static final DateTimeFormatter        MONTH_YEAR_FORMATTER = DateTimeFormatter.ofPattern("MMMM YYYY");
    private              Border                   weekBorder;
    private              Text                     titleText;
    private              Text                     text;
    private              double                   cellOffsetX;
    private              double                   cellOffsetY;
    private              double                   cellWidth;
    private              double                   cellHeight;
    private              List<Label>              labels;
    private              EventHandler<MouseEvent> clickHandler;


    // ******************** Constructors **************************************
    public CalendarTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        final ZonedDateTime TIME = tile.getTime();

        titleText = new Text(MONTH_YEAR_FORMATTER.format(TIME));
        titleText.setFill(tile.getTitleColor());

        clickHandler = e -> checkClick(e);

        labels = new ArrayList<>(56);
        for (int i = 0 ; i < 56 ; i++) {
            Label label = new Label();
            label.setManaged(false);
            label.setVisible(false);
            label.setAlignment(Pos.CENTER);
            label.addEventHandler(MouseEvent.MOUSE_PRESSED, clickHandler);
            labels.add(label);
        }

        weekBorder = new Border(new BorderStroke(Color.TRANSPARENT,
                                                 Tile.GRAY,
                                                 Color.TRANSPARENT,
                                                 Color.TRANSPARENT,
                                                 BorderStrokeStyle.NONE,
                                                 BorderStrokeStyle.SOLID,
                                                 BorderStrokeStyle.NONE,
                                                 BorderStrokeStyle.NONE,
                                                 CornerRadii.EMPTY, BorderWidths.DEFAULT,
                                                 Insets.EMPTY));

        text = new Text(DAY_FORMATTER.format(TIME));
        text.setFill(tile.getTextColor());

        getPane().getChildren().addAll(titleText, text);
        getPane().getChildren().addAll(labels);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !titleText.getText().isEmpty());
            Helper.enableNode(text, tile.isTextVisible());
        }
    }


    @Override public void dispose() {
        labels.forEach(label -> label.removeEventHandler(MouseEvent.MOUSE_PRESSED, clickHandler));
        super.dispose();
    }

    private void drawCells() {
        List<ChartData> dataList   = tile.getChartData();
        ZonedDateTime   time       = tile.getTime();
        Locale          locale     = tile.getLocale();
        int             day        = time.getDayOfMonth();
        int             startDay   = time.withDayOfMonth(1).getDayOfWeek().getValue();
        long            lastDay    = time.range(DAY_OF_MONTH).getMaximum();
        Color           textColor  = tile.getTextColor();
        Color           bkgColor   = tile.getBackgroundColor();
        Font            regFont    = Fonts.latoRegular(size * 0.045);
        Font            bldFont    = Fonts.latoBold(size * 0.045);
        Background      bkgToday   = new Background(new BackgroundFill(tile.getBarColor(), new CornerRadii(size * 0.0125), new Insets(2)));
        Border          appmntBorder = new Border(new BorderStroke(tile.getAlarmColor(),
                                                                   tile.getAlarmColor(),
                                                                   tile.getAlarmColor(),
                                                                   tile.getAlarmColor(),
                                                                   BorderStrokeStyle.SOLID,
                                                                   BorderStrokeStyle.SOLID,
                                                                   BorderStrokeStyle.SOLID,
                                                                   BorderStrokeStyle.SOLID,
                                                                   new CornerRadii(size * 0.0125), BorderWidths.DEFAULT,
                                                                   new Insets(1)));
        boolean counting = false;
        int dayCounter = 1;
        for (int y = 0 ; y < 7 ; y++) {
            for (int x = 0 ; x < 8 ; x++) {
                int index = y * 8 + x;
                Label label = labels.get(index);

                String text;
                if (x == 0 && y == 0) {
                    text = "";
                    label.setManaged(false);
                    label.setVisible(false);
                } else if (y == 0) {
                    text = DayOfWeek.of(x).getDisplayName(TextStyle.SHORT, locale);
                    //label.setTextFill(x == 7 ? Tile.RED : textColor);
                    label.setTextFill(textColor);
                    label.setFont(bldFont);
                } else if (x == 0) {
                    text = Integer.toString(time.withDayOfMonth(1).plusDays((y - 1) * 7).get(IsoFields.WEEK_OF_WEEK_BASED_YEAR));
                    label.setTextFill(Tile.GRAY);
                    label.setFont(regFont);
                    label.setBorder(weekBorder);
                } else {
                    if (index - 7 > startDay) {
                        counting = true;
                        text = Integer.toString(dayCounter);

                        LocalDate currentDay = time.toLocalDate().plusDays(dayCounter - 1);
                        long appointments    = dataList.stream().filter(data -> data.getTimestampAsLocalDate().isEqual(currentDay)).count();

                        if (x == 7) {
                            if (appointments > 0) { label.setBorder(appmntBorder); } else { label.setBorder(null); }
                            label.setTextFill(Tile.RED);
                            label.setFont(regFont);
                        } else if (dayCounter == day) {
                            if (appointments > 0) { label.setBorder(appmntBorder); } else { label.setBorder(null); }
                            label.setBackground(bkgToday);
                            label.setTextFill(bkgColor);
                            label.setFont(bldFont);
                        } else {
                            if (appointments > 0) { label.setBorder(appmntBorder); } else { label.setBorder(null); }
                            label.setTextFill(textColor);
                            label.setFont(regFont);
                        }
                    } else {
                        text = "";
                        label.setManaged(false);
                        label.setVisible(false);
                    }
                    if (dayCounter > lastDay) {
                        text = "";
                        label.setManaged(false);
                        label.setVisible(false);
                    }
                    if (counting) { dayCounter++; }
                }

                label.setText(text);
                label.setVisible(true);
                label.setManaged(true);
                label.setPrefSize(cellWidth, cellHeight);
                label.relocate(x * cellWidth + cellOffsetX, y * cellHeight + cellOffsetY);
            }
        }
    }

    private void checkClick(final MouseEvent EVENT) {
        Label  selectedLabel = ((Label) EVENT.getSource());
        String selectedText  = selectedLabel.getText();
        if (null == selectedText ||
            selectedText.isEmpty() ||
            !Character.isDigit(selectedText.charAt(0))) { return; }
        if (selectedLabel.getBorder() != null && selectedLabel.getBorder().equals(weekBorder)) { return; }
        int selectedNo = Integer.parseInt(selectedText);
        if (selectedNo > 31) { return; }

        List<ChartData>     dataList          = tile.getChartData();
        ZonedDateTime       time              = tile.getTime();
        LocalDate           selectedDate      = LocalDate.of(time.getYear(), time.getMonth(), selectedNo);
        Optional<ChartData> selectedChartData = dataList.stream().filter(data -> data.getTimestampAsLocalDate().isEqual(selectedDate)).findAny();

        if (selectedChartData.isPresent()) { tile.fireTileEvent(new TileEvent(EventType.SELECTED_CHART_DATA, selectedChartData.get())); }
    }


    // ******************** Resizing ******************************************
    @Override protected void resizeDynamicText() {}
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

        double cellAreaWidth  = contentBounds.getWidth();
        double cellAreaHeight = contentBounds.getHeight();

        cellOffsetX = contentBounds.getX();
        cellOffsetY = (height - cellAreaHeight) * 0.5;

        cellWidth  = cellAreaWidth / 8.0;
        cellHeight = cellAreaHeight / 7.0;

        drawCells();

        redraw();
    }

    @Override protected void redraw() {
        super.redraw();
        final ZonedDateTime TIME = tile.getTime();
        titleText.setText(MONTH_YEAR_FORMATTER.format(TIME));
        text.setText(DAY_FORMATTER.format(TIME));
        resizeDynamicText();
        resizeStaticText();

        titleText.setFill(tile.getTitleColor());
        text.setFill(tile.getTextColor());
    }
}
