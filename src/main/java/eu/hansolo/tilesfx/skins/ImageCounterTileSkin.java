/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2016-2021 Gerrit Grunwald.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.hansolo.tilesfx.skins;

import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.Tile.ImageMask;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.Helper;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
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


public class ImageCounterTileSkin extends TileSkin {
    private Text           titleText;
    private Text           text;
    private Text           valueText;
    private Text           upperUnitText;
    private Line           fractionLine;
    private Text           unitText;
    private VBox           unitFlow;
    private HBox           valueUnitFlow;
    private Label          description;
    private ImageView      imgView;
    private StackPane      graphicContainer;
    private Circle         roundFrame;
    private Rectangle      rectangularFrame;
    private ChangeListener imageListener;


    // ******************** Constructors **************************************
    public ImageCounterTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        imageListener = (o, ov, nv) -> { if (nv != null) { imgView.setImage(tile.getImage()); }};

        titleText = new Text();
        titleText.setFill(tile.getTitleColor());
        Helper.enableNode(titleText, !tile.getTitle().isEmpty());

        text = new Text(tile.getText());
        text.setFill(tile.getTextColor());
        Helper.enableNode(text, tile.isTextVisible());

        imgView = new ImageView(tile.getImage());

        roundFrame = new Circle();
        roundFrame.setFill(Color.TRANSPARENT);

        rectangularFrame = new Rectangle();
        rectangularFrame.setFill(Color.TRANSPARENT);

        graphicContainer = new StackPane();
        graphicContainer.setMinSize(size * 0.9, tile.isTextVisible() ? size * 0.72 : size * 0.795);
        graphicContainer.setMaxSize(size * 0.9, tile.isTextVisible() ? size * 0.72 : size * 0.795);
        graphicContainer.setPrefSize(size * 0.9, tile.isTextVisible() ? size * 0.72 : size * 0.795);
        graphicContainer.getChildren().setAll(roundFrame, rectangularFrame, imgView);


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
        valueUnitFlow.setAlignment(Pos.BOTTOM_RIGHT);
        valueUnitFlow.setMouseTransparent(true);

        description = new Label(tile.getText());
        description.setAlignment(tile.getDescriptionAlignment());
        description.setWrapText(true);
        description.setTextFill(tile.getTextColor());
        Helper.enableNode(description, tile.isTextVisible());

        getPane().getChildren().addAll(titleText, graphicContainer, valueUnitFlow, fractionLine, description, text);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        tile.imageProperty().addListener(imageListener);
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !tile.getTitle().isEmpty());
            Helper.enableNode(text, tile.isTextVisible());
            graphicContainer.setMaxSize(size * 0.9, tile.isTextVisible() ? size * 0.68 : size * 0.795);
            graphicContainer.setPrefSize(size * 0.9, tile.isTextVisible() ? size * 0.68 : size * 0.795);
            Helper.enableNode(valueText, tile.isValueVisible());
            Helper.enableNode(unitFlow, !tile.getUnit().isEmpty());
        }
    }

    @Override protected void handleCurrentValue(final double VALUE) {
        if (tile.getCustomDecimalFormatEnabled()) {
            valueText.setText(decimalFormat.format(VALUE));
        } else {
            valueText.setText(String.format(locale, formatString, VALUE));
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
        valueText.setFont(Fonts.latoRegular(fontSize));
        if (valueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(valueText, maxWidth, fontSize); }
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

        //maxWidth = size * 0.9;
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

        //maxWidth = width - (width - size * 0.275);
        maxWidth = width - size * 0.05 - contentBounds.getWidth() * 0.5;
        fontSize = upperUnitText.getText().isEmpty() ? size * 0.12 : size * 0.10;
        upperUnitText.setFont(Fonts.latoRegular(fontSize));
        if (upperUnitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(upperUnitText, maxWidth, fontSize); }

        fontSize = upperUnitText.getText().isEmpty() ? size * 0.12 : size * 0.10;
        unitText.setFont(Fonts.latoRegular(fontSize));
        if (unitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(unitText, maxWidth, fontSize); }

        fontSize = size * 0.1;
        description.setFont(Fonts.latoRegular(fontSize));
        if (description.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(description, maxWidth, fontSize); }
    }


    @Override protected void resize() {
        super.resize();
        width  = tile.getWidth() - tile.getInsets().getLeft() - tile.getInsets().getRight();
        height = tile.getHeight() - tile.getInsets().getTop() - tile.getInsets().getBottom();
        size   = width < height ? width : height;

        double containerWidth  = contentBounds.getWidth() * 0.45;
        double containerHeight = contentBounds.getHeight();
        double containerSize   = containerWidth < containerHeight ? containerWidth : containerHeight;

        if (tile.isShowing() && width > 0 && height > 0) {
            pane.setMaxSize(width, height);
            pane.setPrefSize(width, height);

            if (containerWidth > 0 && containerHeight > 0) {
                graphicContainer.setMinSize(containerWidth, containerHeight);
                graphicContainer.setMaxSize(containerWidth, containerHeight);
                graphicContainer.setPrefSize(containerWidth, containerHeight);
                graphicContainer.relocate(contentBounds.getX(), contentBounds.getY());

                if (null != tile) {
                    imgView.setPreserveRatio(true);
                    imgView.setFitWidth(containerWidth * 0.96);
                    imgView.setFitHeight(containerHeight * 0.96);
                    imgView.relocate((width - containerWidth) * 0.5, (height - containerHeight) * 0.5);

                    switch(tile.getImageMask()) {
                        case ROUND:
                            imgView.setClip(new Circle(imgView.getLayoutBounds().getWidth()  * 0.5, imgView.getLayoutBounds().getHeight() * 0.5, containerSize * 0.48));
                            rectangularFrame.setManaged(false);
                            rectangularFrame.setVisible(false);
                            roundFrame.setManaged(true);
                            roundFrame.setVisible(true);
                            roundFrame.setRadius(containerSize * 0.5);
                            roundFrame.setStrokeWidth(size * 0.01);
                            roundFrame.setStroke(tile.getForegroundColor());
                            break;
                        case RECTANGULAR:
                            Rectangle clip = new Rectangle(containerSize * 0.96, containerSize * 0.96);
                            clip.setArcWidth(size * 0.03);
                            clip.setArcHeight(size * 0.03);
                            imgView.setClip(clip);
                            roundFrame.setManaged(false);
                            roundFrame.setVisible(false);
                            rectangularFrame.setManaged(true);
                            rectangularFrame.setVisible(true);
                            rectangularFrame.setWidth(containerSize);
                            rectangularFrame.setHeight(containerSize);
                            rectangularFrame.setStrokeWidth(size * 0.01);
                            rectangularFrame.setArcWidth(size * 0.05);
                            rectangularFrame.setArcHeight(size * 0.05);
                            rectangularFrame.setStroke(tile.getForegroundColor());
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
                        imgView.setClip(new Circle(imgView.getLayoutBounds().getWidth()  * 0.5, imgView.getLayoutBounds().getHeight() * 0.5, containerSize * 0.48));
                        roundFrame.setManaged(true);
                        roundFrame.setVisible(true);
                        roundFrame.setRadius(containerSize * 0.5);
                        roundFrame.setStrokeWidth(size * 0.01);
                        roundFrame.setStroke(tile.getForegroundColor());
                    } else {
                        roundFrame.setManaged(false);
                        roundFrame.setVisible(false);
                    }

                }
            }
            resizeDynamicText();
            resizeStaticText();

            double textWidth = contentBounds.getWidth() * 0.5;
            valueUnitFlow.setPrefWidth(textWidth);
            valueUnitFlow.relocate(width - size * 0.05 - textWidth, contentBounds.getY());
            valueUnitFlow.setMaxHeight(valueText.getFont().getSize());

            fractionLine.setStartX(width - 0.17 * size);
            fractionLine.setStartY(tile.getTitle().isEmpty() ? size * 0.2 : size * 0.3);
            fractionLine.setEndX(width - 0.05 * size);
            fractionLine.setEndY(tile.getTitle().isEmpty() ? size * 0.2 : size * 0.3);
            fractionLine.setStroke(tile.getUnitColor());
            fractionLine.setStrokeWidth(size * 0.005);

            unitFlow.setTranslateY(-size * 0.005);

            description.setPrefSize(textWidth, size * 0.43);
            description.relocate(width - size * 0.05 - textWidth, titleText.isVisible() ? height * 0.42 : height * 0.32);
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
        description.setText(tile.getDescription());
        description.setAlignment(tile.getDescriptionAlignment());

        resizeDynamicText();
        resizeStaticText();

        titleText.setFill(tile.getTitleColor());
        text.setFill(tile.getTextColor());
        valueText.setFill(tile.getValueColor());
        upperUnitText.setFill(tile.getUnitColor());
        fractionLine.setStroke(tile.getUnitColor());
        unitText.setFill(tile.getUnitColor());
        description.setTextFill(tile.getDescriptionColor());
    }
}
