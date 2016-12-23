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

import java.time.LocalTime;


/**
 * Created by hansolo on 23.12.16.
 */
public class TimeTileSkin extends TileSkin {
    private Text     titleText;
    private Text     leftText;
    private Text     leftUnit;
    private Text     rightText;
    private Text     rightUnit;
    private TextFlow timeText;
    private Label    text;


    // ******************** Constructors **************************************
    public TimeTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        titleText = new Text();
        titleText.setFill(getSkinnable().getTitleColor());
        Helper.enableNode(titleText, !getSkinnable().getTitle().isEmpty());

        LocalTime duration = getSkinnable().getDuration();

        leftText = new Text(Integer.toString(duration.getHour() > 0 ? duration.getHour() : duration.getMinute()));
        leftText.setFill(getSkinnable().getValueColor());
        leftUnit = new Text(duration.getHour() > 0 ? "h" : "m");
        leftUnit.setFill(getSkinnable().getValueColor());

        rightText = new Text(Integer.toString(duration.getHour() > 0 ? duration.getMinute() : duration.getSecond()));
        rightText.setFill(getSkinnable().getValueColor());
        rightUnit = new Text(duration.getHour() > 0 ? "m" : "s");
        rightUnit.setFill(getSkinnable().getValueColor());

        timeText = new TextFlow(leftText, leftUnit, rightText, rightUnit);
        timeText.setPrefWidth(PREFERRED_WIDTH * 0.9);

        text = new Label(getSkinnable().getText());
        text.setAlignment(Pos.TOP_LEFT);
        text.setWrapText(true);
        text.setTextFill(getSkinnable().getTextColor());
        Helper.enableNode(text, getSkinnable().isTextVisible());

        getPane().getChildren().addAll(titleText, timeText, text);
    }

    @Override protected void registerListeners() {
        super.registerListeners();

    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !getSkinnable().getTitle().isEmpty());
            Helper.enableNode(timeText, getSkinnable().isValueVisible());
            Helper.enableNode(text, getSkinnable().isTextVisible());
        }
    };


    // ******************** Resizing ******************************************
    @Override protected void resizeDynamicText() {
        double fontSize = size * 0.24;
        leftText.setFont(Fonts.latoRegular(fontSize));
        rightText.setFont(Fonts.latoRegular(fontSize));
    };
    @Override protected void resizeStaticText() {
        double maxWidth = size * 0.9;
        double fontSize = size * 0.06;

        titleText.setFont(Fonts.latoRegular(fontSize));
        if (titleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(titleText, maxWidth, fontSize); }
        titleText.relocate(size * 0.05, size * 0.05);

        fontSize = size * 0.12;
        leftUnit.setFont(Fonts.latoRegular(fontSize));
        rightUnit.setFont(Fonts.latoRegular(fontSize));

        fontSize = size * 0.1;
        text.setFont(Fonts.latoRegular(fontSize));
    };

    @Override protected void resize() {
        super.resize();

        timeText.setPrefWidth(size * 0.9);
        timeText.relocate(size * 0.05, size * 0.15);

        text.setPrefSize(size * 0.9, size * 43);
        text.relocate(size * 0.05, size * 0.42);
    };

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(getSkinnable().getTitle());

        LocalTime duration = getSkinnable().getDuration();
        leftText.setText(Integer.toString(duration.getHour() > 0 ? duration.getHour() : duration.getMinute()));
        leftUnit.setText(duration.getHour() > 0 ? " h  " : " m  ");
        rightText.setText(Integer.toString(duration.getHour() > 0 ? duration.getMinute() : duration.getSecond()));
        rightUnit.setText(duration.getHour() > 0 ? " m" : " s");

        text.setText(getSkinnable().getText());

        resizeDynamicText();
        resizeStaticText();

        titleText.setFill(getSkinnable().getTitleColor());
        leftText.setFill(getSkinnable().getValueColor());
        leftUnit.setFill(getSkinnable().getValueColor());
        rightText.setFill(getSkinnable().getValueColor());
        rightUnit.setFill(getSkinnable().getValueColor());
        text.setTextFill(getSkinnable().getTextColor());
    };
}
