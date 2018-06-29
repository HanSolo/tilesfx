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
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;


/**
 * Created by hansolo on 26.12.16.
 */
public class CustomTileSkin extends TileSkin {
    private Text           titleText;
    private Text           text;
    private StackPane      graphicContainer;
    private ChangeListener graphicListener;


    // ******************** Constructors **************************************
    public CustomTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        graphicListener = (o, ov, nv) -> { if (nv != null) { graphicContainer.getChildren().setAll(tile.getGraphic()); }};

        titleText = new Text();
        titleText.setFill(tile.getTitleColor());
        Helper.enableNode(titleText, !tile.getTitle().isEmpty());

        text = new Text(tile.getText());
        text.setFill(tile.getTextColor());
        Helper.enableNode(text, tile.isTextVisible());

        graphicContainer = new StackPane();
        graphicContainer.setMinSize(size * 0.9, tile.isTextVisible() ? size * 0.72 : size * 0.795);
        graphicContainer.setMaxSize(size * 0.9, tile.isTextVisible() ? size * 0.72 : size * 0.795);
        graphicContainer.setPrefSize(size * 0.9, tile.isTextVisible() ? size * 0.72 : size * 0.795);
        if (null != tile.getGraphic()) graphicContainer.getChildren().setAll(tile.getGraphic());

        getPane().getChildren().addAll(titleText, graphicContainer, text);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        tile.graphicProperty().addListener(graphicListener);
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !tile.getTitle().isEmpty());
            Helper.enableNode(text, tile.isTextVisible());
            graphicContainer.setMaxSize(size * 0.9, tile.isTextVisible() ? size * 0.68 : size * 0.795);
            graphicContainer.setPrefSize(size * 0.9, tile.isTextVisible() ? size * 0.68 : size * 0.795);
        } else if ("GRAPHIC".equals(EVENT_TYPE)) {
            if (null != tile.getGraphic()) graphicContainer.getChildren().setAll(tile.getGraphic());
        }
    }

    @Override public void dispose() {
        tile.graphicProperty().removeListener(graphicListener);
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

        if (tile.isShowing() && width > 0 && height > 0) {
            pane.setMaxSize(width, height);
            pane.setPrefSize(width, height);

            if (containerWidth > 0 && containerHeight > 0) {
                graphicContainer.setMinSize(containerWidth, containerHeight);
                graphicContainer.setMaxSize(containerWidth, containerHeight);
                graphicContainer.setPrefSize(containerWidth, containerHeight);
                graphicContainer.relocate(contentBounds.getX(), contentBounds.getY());

                if (null != tile) {
                    Node graphic = tile.getGraphic();
                    if (tile.getGraphic() instanceof Shape) {
                        double graphicWidth  = graphic.getBoundsInLocal().getWidth();
                        double graphicHeight = graphic.getBoundsInLocal().getHeight();

	                    if (graphicWidth > containerWidth || graphicHeight > containerHeight) {
		                    double scale = 1;

		                    if (graphicWidth - containerWidth > graphicHeight - containerHeight) {
			                    scale = containerWidth / graphicWidth;
		                    } else {
		                        scale = containerHeight / graphicHeight;
                            }

		                    graphic.setScaleX(scale);
		                    graphic.setScaleY(scale);
	                    }
                    } else if (tile.getGraphic() instanceof ImageView) {
                        ImageView imgView = (ImageView) graphic;
                        imgView.setPreserveRatio(true);
                        imgView.setFitWidth(containerWidth);
                        imgView.setFitHeight(containerHeight);
                        //((ImageView) graphic).setFitWidth(containerWidth);
                        //((ImageView) graphic).setFitHeight(containerHeight);
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
