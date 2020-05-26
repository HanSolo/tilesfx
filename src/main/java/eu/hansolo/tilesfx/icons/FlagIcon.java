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

package eu.hansolo.tilesfx.icons;

import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;


/**
 * User: hansolo
 * Date: 26.05.20
 * Time: 08:26
 */
public class FlagIcon extends Region {
    private static final double    PREFERRED_WIDTH  = 30;
    private static final double    PREFERRED_HEIGHT = 30;
    private static final double    MINIMUM_WIDTH    = 5;
    private static final double    MINIMUM_HEIGHT   = 5;
    private static final double    MAXIMUM_WIDTH    = 1024;
    private static final double    MAXIMUM_HEIGHT   = 1024;
    private              double    size;
    private              double    width;
    private              double    height;
    private              ImageView imageView;
    private              Flag      flag;


    // ******************** Constructors **************************************
    public FlagIcon() {
        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void initGraphics() {
        if (Double.compare(getPrefWidth(), 0.0) <= 0 || Double.compare(getPrefHeight(), 0.0) <= 0 || Double.compare(getWidth(), 0.0) <= 0 ||
            Double.compare(getHeight(), 0.0) <= 0) {
            if (getPrefWidth() > 0 && getPrefHeight() > 0) {
                setPrefSize(getPrefWidth(), getPrefHeight());
            } else {
                setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            }
        }

        flag      = Flag.GERMANY;
        imageView = new ImageView(flag.getImage());
        getChildren().setAll(imageView);
    }

    private void registerListeners() {
        widthProperty().addListener(o -> resize());
        heightProperty().addListener(o -> resize());
        // add listeners to your propertes like
        //value.addListener(o -> handleControlPropertyChanged("VALUE"));
    }


    // ******************** Methods *******************************************
    @Override protected double computeMinWidth(final double HEIGHT) { return MINIMUM_WIDTH; }
    @Override protected double computeMinHeight(final double WIDTH) { return MINIMUM_HEIGHT; }
    @Override protected double computePrefWidth(final double HEIGHT) { return super.computePrefWidth(HEIGHT); }
    @Override protected double computePrefHeight(final double WIDTH) { return super.computePrefHeight(WIDTH); }
    @Override protected double computeMaxWidth(final double HEIGHT) { return MAXIMUM_WIDTH; }
    @Override protected double computeMaxHeight(final double WIDTH) { return MAXIMUM_HEIGHT; }

    public Flag getFlag() { return flag; }
    public void setFlag(final Flag FLAG) {
        setFlag(FLAG, 30);
    }
    public void setFlag(final Flag FLAG, final double SIZE) {
        imageView.setImage(FLAG.getImage(SIZE));
    }


    // ******************** Resizing ******************************************
    private void resize() {
        width  = getWidth() - getInsets().getLeft() - getInsets().getRight();
        height = getHeight() - getInsets().getTop() - getInsets().getBottom();
        size   = width < height ? width : height;

        if (width > 0 && height > 0) {
            imageView.setFitWidth(size);
            imageView.setFitHeight(size);
            imageView.relocate((getWidth() - size) * 0.5, (getHeight() - size) * 0.5);

            redraw();
        }
    }

    private void redraw() {
        if (null == flag) { return; }
        imageView.setImage(flag.getImage());
    }
}
