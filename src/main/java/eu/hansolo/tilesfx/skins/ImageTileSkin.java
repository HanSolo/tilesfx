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
import eu.hansolo.tilesfx.Tile.ImageMask;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.Helper;
import javafx.beans.value.ChangeListener;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;


public class ImageTileSkin extends TileSkin {
    private Text           titleText;
    private Text           text;
    private ImageView      imgView;
    private StackPane      graphicContainer;
    private Circle         roundFrame;
    private Rectangle      rectangularFrame;
    private ChangeListener imageListener;


    // ******************** Constructors **************************************
    public ImageTileSkin(final Tile TILE) {
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

        getPane().getChildren().addAll(titleText, graphicContainer, text);
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
        }
    }

    @Override public void dispose() {
        tile.graphicProperty().removeListener(imageListener);
        super.dispose();
    }


    // ******************** Resizing ******************************************
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
            resizeStaticText();
        }
    }

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(tile.getTitle());
        text.setText(tile.getText());

        resizeStaticText();

        titleText.setFill(tile.getTitleColor());
        text.setFill(tile.getTextColor());
    }
}
