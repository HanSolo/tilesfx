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
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;


/**
 * Created by hansolo on 26.12.16.
 */
public class CustomTileSkin extends TileSkin {
    private Text      titleText;
    private Text      text;
    private StackPane graphicContainer;


    // ******************** Constructors **************************************
    public CustomTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        titleText = new Text();
        titleText.setFill(getSkinnable().getTitleColor());
        Helper.enableNode(titleText, !getSkinnable().getTitle().isEmpty());

        text = new Text(getSkinnable().getText());
        text.setFill(getSkinnable().getTextColor());
        Helper.enableNode(text, getSkinnable().isTextVisible());

        graphicContainer = new StackPane();
        graphicContainer.setMinSize(size * 0.9, getSkinnable().isTextVisible() ? size * 0.72 : size * 0.795);
        graphicContainer.setMaxSize(size * 0.9, getSkinnable().isTextVisible() ? size * 0.72 : size * 0.795);
        graphicContainer.setPrefSize(size * 0.9, getSkinnable().isTextVisible() ? size * 0.72 : size * 0.795);
        if (null != getSkinnable().getGraphic()) graphicContainer.getChildren().setAll(getSkinnable().getGraphic());

        getPane().getChildren().addAll(titleText, graphicContainer, text);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        getSkinnable().graphicProperty().addListener((o, ov, nv) -> {
            if (nv != null) { graphicContainer.getChildren().setAll(getSkinnable().getGraphic()); }
        });
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !getSkinnable().getTitle().isEmpty());
            Helper.enableNode(text, getSkinnable().isTextVisible());
            graphicContainer.setMaxSize(size * 0.9, getSkinnable().isTextVisible() ? size * 0.68 : size * 0.795);
            graphicContainer.setPrefSize(size * 0.9, getSkinnable().isTextVisible() ? size * 0.68 : size * 0.795);
        } else if ("GRAPHIC".equals(EVENT_TYPE)) {
            if (null != getSkinnable().getGraphic()) graphicContainer.getChildren().setAll(getSkinnable().getGraphic());
        }
    };


    // ******************** Resizing ******************************************
    @Override protected void resizeStaticText() {
        double maxWidth = size * 0.9;
        double fontSize = size * textSize.factor;

        titleText.setFont(Fonts.latoRegular(fontSize));
        if (titleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(titleText, maxWidth, fontSize); }
        titleText.relocate(size * 0.05, size * 0.05);

        text.setFont(Fonts.latoRegular(fontSize));
        if (text.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(text, maxWidth, fontSize); }
        text.setX(size * 0.05);
        text.setY(size * 0.95);
    };

    @Override protected void resize() {
        width  = getSkinnable().getWidth() - getSkinnable().getInsets().getLeft() - getSkinnable().getInsets().getRight();
        height = getSkinnable().getHeight() - getSkinnable().getInsets().getTop() - getSkinnable().getInsets().getBottom();
        size   = width < height ? width : height;

        double containerWidth  = width * 0.9;
        double containerHeight = getSkinnable().isTextVisible() ? height * 0.72 : height * 0.795;

        if (width > 0 && height > 0) {
            pane.setMaxSize(width, height);
            pane.setPrefSize(width, height);

            if (containerWidth > 0 && containerHeight > 0) {
                graphicContainer.setMinSize(containerWidth, containerHeight);
                graphicContainer.setMaxSize(containerWidth, containerHeight);
                graphicContainer.setPrefSize(containerWidth, containerHeight);
                graphicContainer.relocate(size * 0.05, size * 0.15);

                if (null != getSkinnable().getGraphic() && getSkinnable().getGraphic() instanceof Shape) {
                    Node   graphic = getSkinnable().getGraphic();
                    double width   = graphic.getBoundsInLocal().getWidth();
                    double height  = graphic.getBoundsInLocal().getHeight();

                    if (width > containerWidth || height > containerHeight) {
                        double aspect = height / width;
                        if (aspect * width > height) {
                            width = 1 / (aspect / height);
                            graphic.setScaleX(containerWidth / width);
                            graphic.setScaleY(containerWidth / width);
                        } else if (1 / (aspect / height) > width) {
                            height = aspect * width;
                            graphic.setScaleX(containerHeight / height);
                            graphic.setScaleY(containerHeight / height);
                        } else {
                            graphic.setScaleX(containerHeight / height);
                            graphic.setScaleY(containerHeight / height);
                        }
                    }
                }
            }
            resizeStaticText();
        }
    };

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(getSkinnable().getTitle());
        text.setText(getSkinnable().getText());

        resizeStaticText();

        titleText.setFill(getSkinnable().getTitleColor());
        text.setFill(getSkinnable().getTextColor());
    };
}
