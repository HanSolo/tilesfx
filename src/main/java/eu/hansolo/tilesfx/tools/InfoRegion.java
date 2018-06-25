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

package eu.hansolo.tilesfx.tools;

import eu.hansolo.tilesfx.Tile;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.QuadCurveTo;


public class InfoRegion extends Region {
    private static final double  PREFERRED_WIDTH  = 52;
    private static final double  PREFERRED_HEIGHT = 52;
    private static final double  MINIMUM_WIDTH    = 1;
    private static final double  MINIMUM_HEIGHT   = 1;
    private static final double  MAXIMUM_WIDTH    = 1024;
    private static final double  MAXIMUM_HEIGHT   = 1024;
    private              double  size;
    private              double  width;
    private              double  height;
    private              Path    path;
    private              Path    icon;
    private              Color   backgroundColor;
    private              Color   foregroundColor;
    private              boolean roundedCorner;
    private              Tooltip tooltip;


    // ******************** Constructors **************************************
    public InfoRegion() {
        backgroundColor = Tile.DARK_BLUE;
        foregroundColor = Tile.FOREGROUND;
        roundedCorner   = true;
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

        path = new Path();
        path.setStroke(Color.TRANSPARENT);

        icon = new Path();
        icon.setStroke(Color.TRANSPARENT);
        icon.setMouseTransparent(true);

        tooltip = new Tooltip("");

        getChildren().setAll(path, icon);
    }

    private void registerListeners() {
        widthProperty().addListener(o -> resize());
        heightProperty().addListener(o -> resize());
    }


    // ******************** Methods *******************************************
    @Override public void layoutChildren() {
        super.layoutChildren();
    }

    @Override protected double computeMinWidth(final double HEIGHT) { return MINIMUM_WIDTH; }
    @Override protected double computeMinHeight(final double WIDTH) { return MINIMUM_HEIGHT; }
    @Override protected double computePrefWidth(final double HEIGHT) { return super.computePrefWidth(HEIGHT); }
    @Override protected double computePrefHeight(final double WIDTH) { return super.computePrefHeight(WIDTH); }
    @Override protected double computeMaxWidth(final double HEIGHT) { return MAXIMUM_WIDTH; }
    @Override protected double computeMaxHeight(final double WIDTH) { return MAXIMUM_HEIGHT; }

    public Color getBackgroundColor() { return backgroundColor; }
    public void setBackgroundColor(final Color COLOR) {
        backgroundColor = COLOR;
        redraw();
    }

    public Color getForegroundColor() { return foregroundColor; }
    public void setForegroundColor(final Color COLOR) {
        foregroundColor = COLOR;
        redraw();
    }

    public boolean isRoundedCorner() { return roundedCorner; }
    public void setRoundedCorner(final boolean ROUNDED) {
        roundedCorner = ROUNDED;
        redraw();
    }

    public String getToolTipText() { return tooltip.getText(); }
    public void setTooltipText(final String TEXT) {
        if (null == TEXT || TEXT.isEmpty()) {
            Tooltip.uninstall(path, tooltip);
        } else {
            tooltip.setText(TEXT);
            Tooltip.install(path, tooltip);
        }
    }

    @Override public ObservableList<Node> getChildren() { return super.getChildren(); }


    // ******************** Resizing ******************************************
    private void resize() {
        width  = getWidth() - getInsets().getLeft() - getInsets().getRight();
        height = getHeight() - getInsets().getTop() - getInsets().getBottom();
        size   = width < height ? width : height;

        if (width > 0 && height > 0) {
            path.getElements().clear();
            if (isRoundedCorner()) {
                path.getElements().add(new MoveTo(size * 0.23809524, 0));
                path.getElements().add(new LineTo(size, 0));
                path.getElements().add(new LineTo(0, size));
                path.getElements().add(new LineTo(0, size * 0.23809524));
                path.getElements().add(new QuadCurveTo(0, 0, size * 0.23809524, 0));
                path.getElements().add(new ClosePath());
            } else {
                path.getElements().add(new MoveTo(0, 0));
                path.getElements().add(new LineTo(size, 0));
                path.getElements().add(new LineTo(0, size));
                path.getElements().add(new ClosePath());
            }

            icon.getElements().clear();
            icon.getElements().add(new MoveTo(size * 0.185714285714286, size * 0.119047619047619));
            icon.getElements().add(new LineTo(size * 0.254761904761905, size * 0.185714285714286));
            icon.getElements().add(new LineTo(size * 0.185714285714286, size * 0.254761904761905));
            icon.getElements().add(new LineTo(size * 0.119047619047619, size * 0.185714285714286));
            icon.getElements().add(new ClosePath());
            icon.getElements().add(new MoveTo(size * 0.304761904761905, size * 0.238095238095238));
            icon.getElements().add(new LineTo(size * 0.466666666666667, size * 0.4));
            icon.getElements().add(new LineTo(size * 0.4, size * 0.466666666666667));
            icon.getElements().add(new LineTo(size * 0.238095238095238, size * 0.304761904761905));
            icon.getElements().add(new ClosePath());

            redraw();
        }
    }

    private void redraw() {
        path.setFill(getBackgroundColor());
        icon.setFill(getForegroundColor());
    }
}

