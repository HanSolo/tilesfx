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

import eu.hansolo.tilesfx.tools.Helper;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;


/**
 * User: hansolo
 * Date: 26.05.20
 * Time: 08:26
 */
public class FlagIcon extends Region {
    private static final double    PREFERRED_WIDTH   = 30;
    private static final double    PREFERRED_HEIGHT  = 30;
    private static final double    MINIMUM_WIDTH     = 5;
    private static final double    MINIMUM_HEIGHT    = 5;
    private static final double    MAXIMUM_WIDTH     = 1024;
    private static final double    MAXIMUM_HEIGHT    = 1024;
    private static final double    DEFAULT_FLAG_SIZE = 30;
    private              double    size;
    private              double    width;
    private              double    height;
    private              ImageView imageView;
    private              Flag      flag;
    private              double    flagSize;


    // ******************** Constructors **************************************
    public FlagIcon() {
        this(Flag.GERMANY, 30);
    }
    public FlagIcon(final Flag FLAG) {
        this(FLAG, 30);
    }
    public FlagIcon(final Flag FLAG, final double FLAG_SIZE) {
        flag     = FLAG;
        flagSize = Helper.clamp(5, 1024, FLAG_SIZE);
        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void initGraphics() {
        if (Double.compare(getPrefWidth(), 0.0) <= 0 || Double.compare(getPrefHeight(), 0.0) <= 0 || Double.compare(getWidth(), 0.0) <= 0 ||
            Double.compare(getHeight(), 0.0) <= 0) {
            if (getPrefWidth() > 0 && getPrefHeight() > 0) {
                setPrefSize(flagSize, flagSize);
            } else {
                setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            }
        }
        imageView = new ImageView(flag.getImage(flagSize));
        getChildren().setAll(imageView);
    }

    private void registerListeners() {
        widthProperty().addListener(o -> resize());
        heightProperty().addListener(o -> resize());
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
        setFlag(FLAG, flagSize);
    }
    public void setFlag(final Flag FLAG, final double FLAG_SIZE) {
        if (null == FLAG) { throw new IllegalArgumentException("Flag cannot be null"); }
        flag     = FLAG;
        flagSize = Helper.clamp(5, 1024, FLAG_SIZE);
        redraw();
    }

    public double getFlagSize() { return flagSize; }
    public void setFlagSize(final double FLAG_SIZE) {
        flagSize = Helper.clamp(5, 1024, FLAG_SIZE);
        redraw();
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
        imageView.setImage(flag.getImage(flagSize));
    }
}
