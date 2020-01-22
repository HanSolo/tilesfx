/*
 * Copyright (c) 2020 by Gerrit Grunwald
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
import eu.hansolo.tilesfx.events.TileEvent.EventType;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.Helper;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.InnerShadow;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;


public class LedTileSkin extends TileSkin {
    private              InnerShadow innerShadow;
    private              Paint       borderFill;
    private              Paint       onFill;
    private              Paint       offFill;
    private              Paint       highlightFill;
    private              Text        titleText;
    private              Text        text;
    private              Label       description;
    private              Circle      ledBorder;
    private              Circle      led;
    private              Circle      hightlight;


    // ******************** Constructors **************************************
    public LedTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        titleText = new Text();
        titleText.setFill(tile.getTitleColor());
        Helper.enableNode(titleText, !tile.getTitle().isEmpty());

        text = new Text(tile.getText());
        text.setFill(tile.getUnitColor());
        Helper.enableNode(text, tile.isTextVisible());

        description = new Label(tile.getDescription());
        description.setAlignment(tile.getDescriptionAlignment());
        description.setWrapText(true);
        description.setTextFill(tile.getTextColor());
        Helper.enableNode(description, !tile.getDescription().isEmpty());

        ledBorder  = new Circle();
        led        = new Circle();
        hightlight = new Circle();

        innerShadow  = new InnerShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.65), 0.07 * size, 0, 0, 0);
        led.setEffect(innerShadow);

        getPane().getChildren().addAll(titleText, text, description, ledBorder, led, hightlight);
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if (EventType.VISIBILITY.name().equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !tile.getTitle().isEmpty());
            Helper.enableNode(text, tile.isTextVisible());
            Helper.enableNode(description, !tile.getDescription().isEmpty());
        } else if (EventType.REDRAW.name().equals(EVENT_TYPE)) {
            updateFills();
            redraw();
        }
    }

    @Override protected void handleCurrentValue(final double VALUE) {
        led.setFill(tile.isActive() ? onFill : offFill);
    }

    private void updateFills() {
        borderFill = new LinearGradient(0, 0,
                           1, 1,
                           true, CycleMethod.NO_CYCLE,
                           new Stop(0.0, Color.rgb(20, 20, 20, 0.65)),
                           new Stop(0.15, Color.rgb(20, 20, 20, 0.65)),
                           new Stop(0.26, Color.rgb(41, 41, 41, 0.65)),
                           new Stop(0.26, Color.rgb(41, 41, 41, 0.64)),
                           new Stop(0.85, Color.rgb(200, 200, 200, 0.41)),
                           new Stop(1.0, Color.rgb(200, 200, 200, 0.35)));

        final Color ledColor = tile.getActiveColor();

        onFill = new LinearGradient(0, 0,
                                    1, 1,
                                    true, CycleMethod.NO_CYCLE,
                                    new Stop(0.0, ledColor.deriveColor(0d, 1d, 0.77, 1d)),
                                    new Stop(0.49, ledColor.deriveColor(0d, 1d, 0.5, 1d)),
                                    new Stop(1.0, ledColor));

        offFill = new LinearGradient(0, 0,
                                     1, 1,
                                     true, CycleMethod.NO_CYCLE,
                                     new Stop(0.0, ledColor.deriveColor(0d, 1d, 0.20, 1d)),
                                     new Stop(0.49, ledColor.deriveColor(0d, 1d, 0.13, 1d)),
                                     new Stop(1.0, ledColor.deriveColor(0d, 1d, 0.2, 1d)));

        highlightFill = new RadialGradient(0, 0,
                                           hightlight.getCenterX() - hightlight.getRadius(), hightlight.getCenterY() - hightlight.getRadius(),
                                           hightlight.getRadius(),
                                           false, CycleMethod.NO_CYCLE,
                                           new Stop(0.0, Color.WHITE),
                                           new Stop(1.0, Color.TRANSPARENT));
    }


    // ******************** Resizing ******************************************
    @Override protected void resizeStaticText() {
        double maxWidth = width - size * 0.1;
        double fontSize = size * textSize.factor;

        boolean customFontEnabled = tile.isCustomFontEnabled();
        Font customFont        = tile.getCustomFont();
        Font font              = (customFontEnabled && customFont != null) ? Font.font(customFont.getFamily(), fontSize) : Fonts.latoRegular(fontSize);

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

        fontSize = size * 0.1;
        description.setFont(Fonts.latoRegular(fontSize));
        description.setAlignment(Pos.TOP_CENTER);
        description.setWrapText(false);
    }

    @Override protected void resize() {
        super.resize();

        description.setPrefWidth(contentBounds.getWidth());
        description.relocate(contentBounds.getX(), contentBounds.getY());

        updateFills();

        ledBorder.setRadius(size * 0.19);
        ledBorder.setCenterX(width * 0.5);
        ledBorder.setCenterY(height * 0.5);

        led.setRadius(size * 0.15);
        led.setCenterX(width * 0.5);
        led.setCenterY(height * 0.5);

        hightlight.setRadius(size * 0.13);
        hightlight.setCenterX(width * 0.5);
        hightlight.setCenterY(height * 0.5);

        innerShadow.setRadius(0.075 * size);
    }


    @Override protected void redraw() {
        super.redraw();

        titleText.setText(tile.getTitle());
        text.setText(tile.getText());
        description.setText(tile.getDescription());
        description.setAlignment(tile.getDescriptionAlignment());

        resizeStaticText();

        titleText.setFill(tile.getTitleColor());
        text.setFill(tile.getTextColor());
        description.setTextFill(tile.getDescriptionColor());

        ledBorder.setFill(borderFill);
        led.setFill(tile.isActive() ? onFill : offFill);
        hightlight.setFill(highlightFill);
    }
}