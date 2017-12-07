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

import eu.hansolo.tilesfx.Section;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.Tile.TextSize;
import eu.hansolo.tilesfx.events.TileEventListener;
import eu.hansolo.tilesfx.tools.CtxBounds;
import eu.hansolo.tilesfx.tools.NotifyRegion;
import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.Locale;

import static eu.hansolo.tilesfx.tools.Helper.clamp;
import static eu.hansolo.tilesfx.tools.Helper.enableNode;


/**
 * Created by hansolo on 19.12.16.
 */
public class TileSkin extends SkinBase<Tile> implements Skin<Tile> {
    protected static final double                   PREFERRED_WIDTH  = 250;
    protected static final double                   PREFERRED_HEIGHT = 250;
    protected static final double                   MINIMUM_WIDTH    = 50;
    protected static final double                   MINIMUM_HEIGHT   = 50;
    protected static final double                   MAXIMUM_WIDTH    = 1024;
    protected static final double                   MAXIMUM_HEIGHT   = 1024;
    protected              double                   width;
    protected              double                   height;
    protected              double                   size;
    protected              double                   inset;
    protected              double                   doubleInset;
    protected              CtxBounds                contentBounds;
    protected              double                   contentCenterX;
    protected              double                   contentCenterY;
    protected              Pane                     pane;
    protected              double                   minValue;
    protected              double                   maxValue;
    protected              double                   range;
    protected              double                   threshold;
    protected              double                   stepSize;
    protected              double                   angleRange;
    protected              double                   angleStep;
    protected              boolean                  highlightSections;
    protected              String                   formatString;
    protected              Locale                   locale;
    protected              List<Section>            sections;
    protected              boolean                  sectionsVisible;
    protected              TextSize                 textSize;
    protected              DropShadow               shadow;
    protected              InvalidationListener     sizeListener;
    protected              TileEventListener        tileEventListener;
    protected              InvalidationListener     currentValueListener;
    protected              InvalidationListener     timeListener;
    protected              Tile                     tile;
    private                NotifyRegion             notifyRegion;
    private                ListChangeListener<Node> childrenListener;


    // ******************** Constructors **************************************
    public TileSkin(final Tile TILE) {
        super(TILE);
        tile                 = TILE;
        minValue             = TILE.getMinValue();
        maxValue             = TILE.getMaxValue();
        range                = TILE.getRange();
        threshold            = TILE.getThreshold();
        stepSize             = PREFERRED_WIDTH / range;
        angleRange           = clamp(90.0, 180.0, tile.getAngleRange());
        angleStep            = angleRange / range;
        formatString         = new StringBuilder("%.").append(Integer.toString(TILE.getDecimals())).append("f").toString();
        locale               = TILE.getLocale();
        sections             = TILE.getSections();
        sectionsVisible      = TILE.getSectionsVisible();
        highlightSections    = tile.isHighlightSections();
        textSize             = tile.getTextSize();
        sizeListener         = o -> handleEvents("RESIZE");
        tileEventListener    = e -> handleEvents(e.getEventType().name());
        currentValueListener = o -> handleCurrentValue(tile.getCurrentValue());
        childrenListener     = c -> notifyRegion.toFront();
        contentBounds        = new CtxBounds();

        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    protected void initGraphics() {
        // Set initial size
        if (Double.compare(tile.getPrefWidth(), 0.0) <= 0 || Double.compare(tile.getPrefHeight(), 0.0) <= 0 ||
            Double.compare(tile.getWidth(), 0.0) <= 0 || Double.compare(tile.getHeight(), 0.0) <= 0) {
            if (tile.getPrefWidth() > 0 && tile.getPrefHeight() > 0) {
                tile.setPrefSize(tile.getPrefWidth(), tile.getPrefHeight());
            } else {
                tile.setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            }
        }

        shadow = new DropShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.65), 3, 0, 0, 0);

        notifyRegion = new NotifyRegion();
        enableNode(notifyRegion, false);

        pane = new Pane(notifyRegion);
        pane.setBorder(new Border(new BorderStroke(tile.getBorderColor(), BorderStrokeStyle.SOLID, new CornerRadii(PREFERRED_WIDTH * 0.025), new BorderWidths(tile.getBorderWidth()))));
        pane.setBackground(new Background(new BackgroundFill(tile.getBackgroundColor(), new CornerRadii(PREFERRED_WIDTH * 0.025), Insets.EMPTY)));

        getChildren().setAll(pane);
    }

    protected void registerListeners() {
        tile.widthProperty().addListener(sizeListener);
        tile.heightProperty().addListener(sizeListener);
        tile.setOnTileEvent(tileEventListener);
        tile.currentValueProperty().addListener(currentValueListener);
        pane.getChildren().addListener(childrenListener);
    }


    // ******************** Methods *******************************************
    @Override protected double computeMinWidth(final double HEIGHT, final double TOP, final double RIGHT, final double BOTTOM, final double LEFT)  { return MINIMUM_WIDTH; }
    @Override protected double computeMinHeight(final double WIDTH, final double TOP, final double RIGHT, final double BOTTOM, final double LEFT)  { return MINIMUM_HEIGHT; }
    @Override protected double computePrefWidth(final double HEIGHT, final double TOP, final double RIGHT, final double BOTTOM, final double LEFT) { return super.computePrefWidth(HEIGHT, TOP, RIGHT, BOTTOM, LEFT); }
    @Override protected double computePrefHeight(final double WIDTH, final double TOP, final double RIGHT, final double BOTTOM, final double LEFT) { return super.computePrefHeight(WIDTH, TOP, RIGHT, BOTTOM, LEFT); }
    @Override protected double computeMaxWidth(final double HEIGHT, final double TOP, final double RIGHT, final double BOTTOM, final double LEFT)  { return MAXIMUM_WIDTH; }
    @Override protected double computeMaxHeight(final double WIDTH, final double TOP, final double RIGHT, final double BOTTOM, final double LEFT)  { return MAXIMUM_HEIGHT; }

    protected Pane getPane() { return pane; }

    protected void handleEvents(final String EVENT_TYPE) {
        if ("RESIZE".equals(EVENT_TYPE)) {
            resize();
            redraw();
        } else if ("REDRAW".equals(EVENT_TYPE)) {
            redraw();
        } else if ("RECALC".equals(EVENT_TYPE)) {
            minValue          = tile.getMinValue();
            maxValue          = tile.getMaxValue();
            range             = tile.getRange();
            threshold         = tile.getThreshold();
            stepSize          = size / range;
            angleRange        = clamp(90.0, 180.0, tile.getAngleRange());
            angleStep         = angleRange / range;
            highlightSections = tile.isHighlightSections();
            redraw();
            handleCurrentValue(tile.getCurrentValue());
        } else if ("SECTION".equals(EVENT_TYPE)) {
            sections = tile.getSections();
        } else if ("SHOW_NOTIFIER".equals(EVENT_TYPE)) {
            enableNode(notifyRegion, true);
        } else if ("HIDE_NOTIFIER".equals(EVENT_TYPE)) {
            enableNode(notifyRegion, false);
        }
    }

    protected void handleCurrentValue(final double VALUE) {}

    @Override public void dispose() {
        tile.widthProperty().removeListener(sizeListener);
        tile.heightProperty().removeListener(sizeListener);
        tile.removeTileEventListener(tileEventListener);
        tile.currentValueProperty().removeListener(currentValueListener);
        pane.getChildren().removeListener(childrenListener);
        tile = null;
    }
    

    // ******************** Resizing ******************************************
    protected void resizeDynamicText() {}
    protected void resizeStaticText() {}

    protected void resize() {
        width  = tile.getWidth() - tile.getInsets().getLeft() - tile.getInsets().getRight();
        height = tile.getHeight() - tile.getInsets().getTop() - tile.getInsets().getBottom();
        size   = clamp(0, Double.MAX_VALUE, width < height ? width : height);

        stepSize = width / range;
        shadow.setRadius(size * 0.012);

        inset       = size * 0.05;
        doubleInset = inset * 2;

        if (width > 0 && height > 0) {
            //pane.setMaxSize(size, size);
            //pane.relocate((width - size) * 0.5, (height - size) * 0.5);

            double offsetTop    = tile.getTitle().isEmpty() ? inset : size * 0.15;
            double offsetBottom = tile.isTextVisible() ? height - size * 0.15 : height - inset;
            contentBounds.setX(inset);
            contentBounds.setY(offsetTop);
            contentBounds.setWidth(width - doubleInset);
            contentBounds.setHeight(offsetBottom - offsetTop);

            contentCenterX = contentBounds.getX() + contentBounds.getWidth() * 0.5;
            contentCenterY = contentBounds.getY() + contentBounds.getHeight() * 0.5;

            pane.setMaxSize(width, height);
            pane.setPrefSize(width, height);

            notifyRegion.setPrefSize(size * 0.13, size * 0.13);
            notifyRegion.relocate(width - size * 0.13, 0);

            resizeStaticText();
            resizeDynamicText();
        }
    }

    protected void redraw() {
        pane.setBorder(new Border(new BorderStroke(tile.getBorderColor(), BorderStrokeStyle.SOLID, tile.getRoundedCorners() ? new CornerRadii(clamp(0, Double.MAX_VALUE, size * 0.025)) : CornerRadii.EMPTY, new BorderWidths(clamp(0, Double.MAX_VALUE, tile.getBorderWidth() / PREFERRED_WIDTH * size)))));
        pane.setBackground(new Background(new BackgroundFill(tile.getBackgroundColor(), tile.getRoundedCorners() ? new CornerRadii(clamp(0, Double.MAX_VALUE, size * 0.025)) : CornerRadii.EMPTY, Insets.EMPTY)));
        notifyRegion.setRoundedCorner(tile.getRoundedCorners());
        notifyRegion.setBackgroundColor(tile.getNotificationBackgroundColor());
        notifyRegion.setForegroundColor(tile.getNotificationForegroundColor());

        locale          = tile.getLocale();
        formatString    = new StringBuilder("%.").append(Integer.toString(tile.getDecimals())).append("f").toString();
        sectionsVisible = tile.getSectionsVisible();
        textSize        = tile.getTextSize();
    }
}
