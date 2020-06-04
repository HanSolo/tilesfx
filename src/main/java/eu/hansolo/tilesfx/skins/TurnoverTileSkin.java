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

import eu.hansolo.tilesfx.tools.Ranking;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.Tile.ImageMask;
import eu.hansolo.tilesfx.events.TileEvent.EventType;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.Helper;
import eu.hansolo.tilesfx.tools.RotationEffect;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;


public class TurnoverTileSkin extends TileSkin {
    private Text           titleText;
    private Text           valueText;
    private Text           upperUnitText;
    private Line           fractionLine;
    private Text           unitText;
    private VBox           unitFlow;
    private HBox           valueUnitFlow;
    private Text           text;
    private RotationEffect rotationEffect;
    private ImageView      imgView;
    private StackPane      graphicContainer;
    private Circle         rankingCircle;
    private Text           rankingText;
    private StackPane      rankingContainer;
    private Circle         roundFrame;
    private Rectangle      rectangularFrame;
    private ChangeListener imageListener;


    // ******************** Constructors **************************************
    public TurnoverTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        imageListener = (o, ov, nv) -> { if (nv != null) { imgView.setImage(tile.getImage()); }};

        rotationEffect = new RotationEffect();
        rotationEffect.setCenterY(0.4);
        rotationEffect.setVisible(false);

        titleText = new Text();
        titleText.setFill(tile.getTitleColor());
        Helper.enableNode(titleText, !tile.getTitle().isEmpty());

        valueText = new Text(String.format(locale, formatString, ((tile.getValue() - minValue) / range * 100)));
        valueText.setFill(tile.getValueColor());
        valueText.setTextOrigin(VPos.BASELINE);
        Helper.enableNode(valueText, tile.isValueVisible());

        upperUnitText = new Text("");
        upperUnitText.setFill(tile.getUnitColor());
        Helper.enableNode(upperUnitText, !tile.getUnit().isEmpty());

        fractionLine = new Line();

        unitText = new Text(tile.getUnit());
        unitText.setFill(tile.getUnitColor());
        Helper.enableNode(unitText, !tile.getUnit().isEmpty());

        unitFlow = new VBox(upperUnitText, unitText);
        unitFlow.setAlignment(Pos.CENTER_RIGHT);

        valueUnitFlow = new HBox(valueText, unitFlow);
        valueUnitFlow.setAlignment(Pos.CENTER);
        valueUnitFlow.setMouseTransparent(true);

        text = new Text(tile.getText());
        text.setFill(tile.getTextColor());
        Helper.enableNode(text, tile.isTextVisible());

        imgView = new ImageView(tile.getImage());

        rankingCircle = new Circle();
        rankingCircle.setFill(tile.getRank().getColor());

        rankingText = new Text();
        rankingText.setFill(tile.getBackgroundColor());
        rankingText.setTextOrigin(VPos.CENTER);
        rankingText.setTextAlignment(TextAlignment.CENTER);

        rankingContainer = new StackPane(rankingCircle, rankingText);
        if (Ranking.NONE == tile.getRank().getRanking()) {
            rankingContainer.setVisible(false);
        }

        roundFrame = new Circle();
        roundFrame.setFill(Color.TRANSPARENT);

        rectangularFrame = new Rectangle();
        rectangularFrame.setFill(Color.TRANSPARENT);

        graphicContainer = new StackPane();
        graphicContainer.setMinSize(size * 0.9, tile.isTextVisible() ? size * 0.72 : size * 0.795);
        graphicContainer.setMaxSize(size * 0.9, tile.isTextVisible() ? size * 0.72 : size * 0.795);
        graphicContainer.setPrefSize(size * 0.9, tile.isTextVisible() ? size * 0.72 : size * 0.795);
        graphicContainer.getChildren().setAll(roundFrame, rectangularFrame, imgView);

        getPane().getChildren().addAll(rotationEffect, titleText, graphicContainer, rankingContainer, valueUnitFlow, fractionLine, text);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        tile.imageProperty().addListener(imageListener);
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if (EventType.VALUE.name().equals(EVENT_TYPE)) {
            handleCurrentValue(tile.getCurrentValue());
        } else if (EventType.VISIBILITY.name().equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !tile.getTitle().isEmpty());
            Helper.enableNode(text, tile.isTextVisible());
            graphicContainer.setMaxSize(size * 0.9, tile.isTextVisible() ? size * 0.68 : size * 0.795);
            graphicContainer.setPrefSize(size * 0.9, tile.isTextVisible() ? size * 0.68 : size * 0.795);
        }
    }

    @Override protected void handleCurrentValue(final double VALUE) {
        if (tile.getCustomDecimalFormatEnabled()) {
            valueText.setText(decimalFormat.format(VALUE));
        } else {
            valueText.setText(String.format(locale, formatString, VALUE));
        }
        if (VALUE > tile.getThreshold()) {
            rotationEffect.setVisible(true);
            rotationEffect.start();
        } else {
            rotationEffect.stop();
            rotationEffect.setVisible(false);
        }
        resizeDynamicText();
    }

    @Override public void dispose() {
        tile.graphicProperty().removeListener(imageListener);
        super.dispose();
    }


    // ******************** Resizing ******************************************
    @Override protected void resizeDynamicText() {
        double maxWidth = unitText.isVisible() ? width - size * 0.275 : width - size * 0.1;
        double fontSize = size * 0.24;
        valueText.setFont(Fonts.latoBold(fontSize));
        double correctedFontSize = fontSize;
        if (valueText.getLayoutBounds().getWidth() > maxWidth) {
            correctedFontSize = Helper.adjustTextSize(valueText, maxWidth, fontSize);
        }
        double fontFactor = correctedFontSize / fontSize;

        maxWidth = size * 0.275;
        fontSize = upperUnitText.getText().isEmpty() ? size * 0.12 : size * 0.10;
        upperUnitText.setFont(Fonts.latoRegular(fontSize * fontFactor));
        if (upperUnitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(upperUnitText, maxWidth, fontSize); }

        fontSize = upperUnitText.getText().isEmpty() ? size * 0.12 : size * 0.10;
        unitText.setFont(Fonts.latoRegular(fontSize * fontFactor));
        if (unitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(unitText, maxWidth, fontSize); }
    }
    @Override protected void resizeStaticText() {
        double  maxWidth = width - size * 0.1;
        double  fontSize = size * textSize.factor;

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
        width  = tile.getWidth() - tile.getInsets().getLeft() - tile.getInsets().getRight();
        height = tile.getHeight() - tile.getInsets().getTop() - tile.getInsets().getBottom();
        size   = width < height ? width : height;

        double containerWidth  = contentBounds.getWidth();
        double containerHeight = contentBounds.getHeight();
        double containerSize   = containerWidth < containerHeight ? containerWidth : containerHeight;

        if (tile.isShowing() && width > 0 && height > 0) {
            pane.setMaxSize(width, height);
            pane.setPrefSize(width, height);

            rotationEffect.setPrefSize(width, height);

            if (containerWidth > 0 && containerHeight > 0) {
                graphicContainer.setMinSize(containerWidth, containerHeight);
                graphicContainer.setMaxSize(containerWidth, containerHeight);
                graphicContainer.setPrefSize(containerWidth, containerHeight);
                graphicContainer.relocate(contentBounds.getX(), contentBounds.getY() * 0.3);

                if (null != tile) {
                    imgView.setPreserveRatio(true);
                    imgView.setFitWidth(containerWidth * 0.46);
                    imgView.setFitHeight(containerHeight * 0.46);
                    imgView.relocate((width - containerWidth) * 0.5, (height - containerHeight) * 0.5);

                    switch(tile.getImageMask()) {
                        case ROUND:
                            imgView.setClip(new Circle(imgView.getLayoutBounds().getWidth()  * 0.5, imgView.getLayoutBounds().getHeight() * 0.5, containerSize * 0.23));
                            rectangularFrame.setManaged(false);
                            rectangularFrame.setVisible(false);
                            roundFrame.setManaged(true);
                            roundFrame.setVisible(true);
                            roundFrame.setRadius(containerSize * 0.25);
                            roundFrame.setStrokeWidth(size * 0.01);
                            break;
                        case RECTANGULAR:
                            Rectangle clip = new Rectangle(containerSize * 0.46, containerSize * 0.46);
                            clip.setArcWidth(size * 0.03);
                            clip.setArcHeight(size * 0.03);
                            imgView.setClip(clip);
                            roundFrame.setManaged(false);
                            roundFrame.setVisible(false);
                            rectangularFrame.setManaged(true);
                            rectangularFrame.setVisible(true);
                            rectangularFrame.setWidth(containerSize * 0.5);
                            rectangularFrame.setHeight(containerSize * 0.5);
                            rectangularFrame.setStrokeWidth(size * 0.01);
                            rectangularFrame.setArcWidth(size * 0.05);
                            rectangularFrame.setArcHeight(size * 0.05);
                            break;
                        case NONE:
                        default  :
                            imgView.setClip(null);
                            roundFrame.setManaged(false);
                            roundFrame.setVisible(false);
                            rectangularFrame.setManaged(false);
                            rectangularFrame.setVisible(false);
                            break;
                    }
                    if (ImageMask.ROUND == tile.getImageMask()) {
                        imgView.setClip(new Circle(imgView.getLayoutBounds().getWidth()  * 0.5, imgView.getLayoutBounds().getHeight() * 0.5, containerSize * 0.23));
                        roundFrame.setManaged(true);
                        roundFrame.setVisible(true);
                        roundFrame.setRadius(containerSize * 0.25);
                        roundFrame.setStrokeWidth(size * 0.01);
                    } else {
                        roundFrame.setManaged(false);
                        roundFrame.setVisible(false);
                    }
                }
            }
            rankingCircle.setRadius(containerSize * 0.075);
            rankingText.setFont(Fonts.latoRegular(size * 0.075));
            rankingContainer.relocate(width * 0.5 - rankingCircle.getRadius(), height * 0.5 - containerSize * 0.4 - rankingCircle.getRadius());

            resizeDynamicText();
            resizeStaticText();

            valueUnitFlow.setPrefWidth(width - size * 0.1);
            valueUnitFlow.relocate(size * 0.05, (height - valueUnitFlow.getLayoutBounds().getHeight()) * 0.8);
            valueUnitFlow.setMaxHeight(valueText.getFont().getSize());

            fractionLine.setStartX(width - 0.17 * size);
            fractionLine.setStartY(tile.getTitle().isEmpty() ? size * 0.2 : size * 0.3);
            fractionLine.setEndX(width - 0.05 * size);
            fractionLine.setEndY(tile.getTitle().isEmpty() ? size * 0.2 : size * 0.3);
            fractionLine.setStroke(tile.getUnitColor());
            fractionLine.setStrokeWidth(size * 0.005);

            unitFlow.setTranslateY(-size * 0.005);
        }
    }

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(tile.getTitle());
        text.setText(tile.getText());
        if (tile.getCustomDecimalFormatEnabled()) {
            valueText.setText(decimalFormat.format(tile.getCurrentValue()));
        } else {
            valueText.setText(String.format(locale, formatString, tile.getCurrentValue()));
        }
        if (tile.getUnit().contains("/")) {
            String[] units = tile.getUnit().split("/");
            upperUnitText.setText(units[0]);
            unitText.setText(units[1]);
            Helper.enableNode(fractionLine, true);
        } else {
            upperUnitText.setText(" ");
            unitText.setText(tile.getUnit());
            Helper.enableNode(fractionLine, false);
        }
        rankingText.setText(Integer.toString(tile.getRank().getRanking().getAsInt()));

        resizeDynamicText();
        resizeStaticText();

        Ranking ranking   = tile.getRank().getRanking();
        Color   rankColor = tile.getRank().getColor();
        roundFrame.setStroke(Ranking.NONE == ranking ? tile.getForegroundColor() : rankColor);
        rectangularFrame.setStroke(Ranking.NONE == ranking ? tile.getForegroundColor() : rankColor);

        rankingCircle.setFill(rankColor);
        rankingText.setFill(tile.getBackgroundColor());
        rankingContainer.setVisible(Ranking.NONE == ranking ? false : true);

        titleText.setFill(tile.getTitleColor());
        valueText.setFill(tile.getValueColor());
        upperUnitText.setFill(tile.getUnitColor());
        fractionLine.setStroke(tile.getUnitColor());
        unitText.setFill(tile.getUnitColor());
        text.setFill(tile.getTextColor());
    }
}
