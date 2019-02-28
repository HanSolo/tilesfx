/*
 * Copyright (c) 2018 by Gerrit Grunwald
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
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.Locale;


/**
 * User: hansolo
 * Date: 20.06.18
 * Time: 12:44
 */
public class StatusTileSkin extends TileSkin {
    private Text      titleText;
    private Label     description;
    private Line      verticalDivider;
    private Line      horizontal1Divider;
    private Line      horizontal2Divider;
    private Label     leftValueLabel;
    private Label     middleValueLabel;
    private Label     rightValueLabel;
    private Label     leftLabel;
    private Label     middleLabel;
    private Label     rightLabel;
    private Text      text;
    private StackPane leftGraphicsPane;
    private StackPane middleGraphicsPane;
    private StackPane rightGraphicsPane;


    // ******************** Constructors **************************************
    public StatusTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        titleText = new Text();
        titleText.setFill(tile.getTitleColor());
        Helper.enableNode(titleText, !tile.getTitle().isEmpty());

        description = new Label(tile.getDescription());
        description.setAlignment(tile.getDescriptionAlignment());
        description.setTextAlignment(TextAlignment.RIGHT);
        description.setWrapText(true);
        description.setTextOverrun(OverrunStyle.WORD_ELLIPSIS);
        description.setTextFill(tile.getTextColor());
        description.setPrefSize(PREFERRED_WIDTH * 0.9, PREFERRED_HEIGHT * 0.795);
        Helper.enableNode(description, tile.isTextVisible());

        Color foregroundColor = getSkinnable().getForegroundColor();
        Color halfTranslucent = Helper.getColorWithOpacity(foregroundColor, 0.5);

        verticalDivider = new Line(0, PREFERRED_HEIGHT * 0.35, PREFERRED_WIDTH, PREFERRED_HEIGHT * 0.35);
        verticalDivider.getStrokeDashArray().addAll(2.0, 2.0);
        verticalDivider.setStrokeDashOffset(1);
        verticalDivider.setStroke(halfTranslucent);
        verticalDivider.setStrokeWidth(1);

        horizontal1Divider = new Line(PREFERRED_WIDTH / 3, PREFERRED_HEIGHT * 0.45, PREFERRED_WIDTH / 3, PREFERRED_HEIGHT * 0.85);
        horizontal1Divider.getStrokeDashArray().addAll(2.0, 2.0);
        horizontal1Divider.setStrokeDashOffset(1);
        horizontal1Divider.setStroke(halfTranslucent);
        horizontal1Divider.setStrokeWidth(1);

        horizontal2Divider = new Line(2 * PREFERRED_WIDTH / 3, PREFERRED_HEIGHT * 0.45, 2 * PREFERRED_WIDTH / 3, PREFERRED_HEIGHT * 0.85);
        horizontal2Divider.getStrokeDashArray().addAll(2.0, 2.0);
        horizontal2Divider.setStrokeDashOffset(1);
        horizontal2Divider.setStroke(halfTranslucent);
        horizontal2Divider.setStrokeWidth(1);

        leftGraphicsPane = new StackPane();
        middleGraphicsPane = new StackPane();
        rightGraphicsPane = new StackPane();

        if (null != tile.getLeftGraphics()) { leftGraphicsPane.getChildren().setAll(tile.getLeftGraphics()); }
        if (null != tile.getMiddleGraphics()) { middleGraphicsPane.getChildren().setAll(tile.getMiddleGraphics()); }
        if (null != tile.getRightGraphics()) { rightGraphicsPane.getChildren().setAll(tile.getRightGraphics()); }

        leftValueLabel = new Label();
        leftValueLabel.setAlignment(Pos.CENTER);

        middleValueLabel = new Label();
        middleValueLabel.setAlignment(Pos.CENTER);

        rightValueLabel = new Label();
        rightValueLabel.setAlignment(Pos.CENTER);

        leftLabel = new Label();
        leftLabel.setAlignment(Pos.CENTER);

        middleLabel = new Label();
        middleLabel.setAlignment(Pos.CENTER);

        rightLabel = new Label();
        rightLabel.setAlignment(Pos.CENTER);

        text = new Text(tile.getText());
        text.setFill(tile.getUnitColor());
        Helper.enableNode(text, tile.isTextVisible());

        getPane().getChildren().addAll(titleText, description,
                                       verticalDivider, horizontal1Divider, horizontal2Divider, leftGraphicsPane, middleGraphicsPane, rightGraphicsPane,
                                       leftValueLabel, middleValueLabel, rightValueLabel,
                                       leftLabel, middleLabel, rightLabel,
                                       text);
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
            Helper.enableNode(description, !tile.getDescription().isEmpty());
        } else if ("RECALC".equals(EVENT_TYPE)) {
            if (null != tile.getLeftGraphics()) { leftGraphicsPane.getChildren().setAll(tile.getLeftGraphics()); }
            if (null != tile.getMiddleGraphics()) { middleGraphicsPane.getChildren().setAll(tile.getMiddleGraphics()); }
            if (null != tile.getRightGraphics()) { rightGraphicsPane.getChildren().setAll(tile.getRightGraphics()); }
            resize();
        }
    }


    // ******************** Resizing ******************************************
    @Override protected void resizeDynamicText() {
        double oneThirdWidth = width / 3;
        double fontSize      = size * 0.1;

        description.setFont(Fonts.latoRegular(fontSize));
        description.setLayoutY(height * 0.175);

        fontSize = size * 0.075;

        leftValueLabel.setFont(Fonts.latoRegular(fontSize));
        middleValueLabel.setFont(Fonts.latoRegular(fontSize));
        rightValueLabel.setFont(Fonts.latoRegular(fontSize));

        leftValueLabel.relocate(0, height * 0.59);
        middleValueLabel.relocate(oneThirdWidth, height * 0.59);
        rightValueLabel.relocate(width - oneThirdWidth, height * 0.59);
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

        description.setPrefSize(contentBounds.getWidth(), contentBounds.getHeight());
        description.relocate(contentBounds.getX(), contentBounds.getY());

        double oneThirdWidth = width / 3;
        double iconSize      = Helper.clamp(1, height * 0.1, oneThirdWidth * 0.5);
        double offsetX       = (oneThirdWidth - iconSize) * 0.5;

        verticalDivider.setStartX(0);
        verticalDivider.setEndX(width);
        verticalDivider.setStartY(height * 0.35);
        verticalDivider.setEndY(height * 0.35);

        horizontal1Divider.setStartX(oneThirdWidth);
        horizontal1Divider.setStartY(height * 0.4);
        horizontal1Divider.setEndX(oneThirdWidth);
        horizontal1Divider.setEndY(height * 0.8);

        horizontal2Divider.setStartX(2 * oneThirdWidth);
        horizontal2Divider.setStartY(height * 0.4);
        horizontal2Divider.setEndX(2 * oneThirdWidth);
        horizontal2Divider.setEndY(height * 0.8);

        leftGraphicsPane.setMinSize(iconSize, iconSize);
        leftGraphicsPane.setPrefSize(iconSize, iconSize);
        leftGraphicsPane.setMaxSize(iconSize, iconSize);
        leftGraphicsPane.relocate(offsetX, height * 0.425);

        middleGraphicsPane.setMinSize(iconSize, iconSize);
        middleGraphicsPane.setPrefSize(iconSize, iconSize);
        middleGraphicsPane.setMaxSize(iconSize, iconSize);
        middleGraphicsPane.relocate(oneThirdWidth + offsetX, height * 0.425);

        rightGraphicsPane.setMinSize(iconSize, iconSize);
        rightGraphicsPane.setPrefSize(iconSize, iconSize);
        rightGraphicsPane.setMaxSize(iconSize, iconSize);
        rightGraphicsPane.relocate(2 * oneThirdWidth + offsetX, height * 0.425);

        leftValueLabel.setPrefWidth(oneThirdWidth);
        middleValueLabel.setPrefWidth(oneThirdWidth);
        rightValueLabel.setPrefWidth(oneThirdWidth);

        leftLabel.setPrefWidth(oneThirdWidth);
        middleLabel.setPrefWidth(oneThirdWidth);
        rightLabel.setPrefWidth(oneThirdWidth);

        leftLabel.setFont(Fonts.latoRegular(size * 0.035));
        middleLabel.setFont(Fonts.latoRegular(size * 0.035));
        rightLabel.setFont(Fonts.latoRegular(size * 0.035));

        leftLabel.relocate(0, height * 0.75);
        middleLabel.relocate(oneThirdWidth, height * 0.75);
        rightLabel.relocate(width - oneThirdWidth, height * 0.75);
    }

    @Override protected void redraw() {
        super.redraw();

        titleText.setText(tile.getTitle());
        text.setText(tile.getText());
        description.setText(tile.getDescription());
        description.setAlignment(tile.getDescriptionAlignment());

        leftValueLabel.setText(String.format(Locale.US, "%.0f", tile.getLeftValue()));
        middleValueLabel.setText(String.format(Locale.US, "%.0f", tile.getMiddleValue()));
        rightValueLabel.setText(String.format(Locale.US, "%.0f", tile.getRightValue()));

        leftLabel.setText(tile.getLeftText());
        middleLabel.setText(tile.getMiddleText());
        rightLabel.setText(tile.getRightText());

        resizeDynamicText();
        resizeStaticText();

        titleText.setFill(tile.getTitleColor());
        text.setFill(tile.getTextColor());
        description.setTextFill(tile.getTextColor());
        Color foregroundColor = getSkinnable().getForegroundColor();
        Color halfTranslucent = Helper.getColorWithOpacity(foregroundColor, 0.5);

        leftValueLabel.setTextFill(foregroundColor);
        middleValueLabel.setTextFill(foregroundColor);
        rightValueLabel.setTextFill(foregroundColor);

        leftLabel.setTextFill(halfTranslucent);
        middleLabel.setTextFill(halfTranslucent);
        rightLabel.setTextFill(halfTranslucent);

        verticalDivider.setStroke(halfTranslucent);
        horizontal1Divider.setStroke(halfTranslucent);
        horizontal2Divider.setStroke(halfTranslucent);
    }
}
