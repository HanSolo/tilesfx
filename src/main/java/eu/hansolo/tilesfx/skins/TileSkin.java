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

import eu.hansolo.tilesfx.Section;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.Tile.TextSize;
import eu.hansolo.tilesfx.events.BoundsEvt;
import eu.hansolo.tilesfx.events.TileEvt;
import eu.hansolo.tilesfx.tools.CtxBounds;
import eu.hansolo.tilesfx.tools.InfoRegion;
import eu.hansolo.tilesfx.tools.LowerRightRegion;
import eu.hansolo.tilesfx.tools.NotifyRegion;
import eu.hansolo.toolbox.evt.EvtObserver;
import javafx.beans.InvalidationListener;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.text.DecimalFormat;
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
    protected              DecimalFormat            decimalFormat;
    protected              String                   tickLabelFormatString;
    protected              Locale                   locale;
    protected              List<Section>            sections;
    protected              boolean                  sectionsVisible;
    protected              TextSize                 textSize;
    protected              DropShadow               shadow;
    protected              InvalidationListener     sizeListener;
    protected              EvtObserver<TileEvt>     observer;
    protected              InvalidationListener     currentValueListener;
    protected              InvalidationListener     timeListener;
    protected              Tile                     tile;
    private                ImageView                backgroundImageView;
    private                NotifyRegion             notifyRegion;
    private                InfoRegion               infoRegion;
    private                LowerRightRegion         lowerRightRegion;
    private                EventHandler<MouseEvent> infoRegionHandler;


    // ******************** Constructors **************************************
    public TileSkin(final Tile TILE) {
        super(TILE);
        tile                  = TILE;
        minValue              = tile.getMinValue();
        maxValue              = tile.getMaxValue();
        range                 = tile.getRange();
        threshold             = tile.getThreshold();
        stepSize              = PREFERRED_WIDTH / range;
        angleRange            = tile.getAngleRange();
        angleStep             = angleRange / range;
        formatString          = new StringBuilder("%.").append(tile.getDecimals()).append("f").toString();
        tickLabelFormatString = new StringBuilder("%.").append(tile.getTickLabelDecimals()).append("f").toString();;
        locale                = tile.getLocale();
        sections              = tile.getSections();
        sectionsVisible       = tile.getSectionsVisible();
        highlightSections     = tile.isHighlightSections();
        textSize              = tile.getTextSize();
        infoRegionHandler     = tile.getInfoRegionHandler();
        sizeListener          = o -> handleEvents("RESIZE");
        observer              = e -> handleEvents(e.getEvtType().getName());
        currentValueListener  = o -> handleCurrentValue(tile.getCurrentValue());
        contentBounds         = new CtxBounds();
        decimalFormat         = tile.getCustomDecimalFormat();

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

        backgroundImageView = new ImageView();
        backgroundImageView.setPreserveRatio(true);
        backgroundImageView.setMouseTransparent(true);
        if (null == tile.getBackgroundImage()) {
            enableNode(backgroundImageView, false);
        } else {
            backgroundImageView.setImage(tile.getBackgroundImage());
            enableNode(backgroundImageView, true);
        }

        notifyRegion = new NotifyRegion();
        enableNode(notifyRegion, false);

        infoRegion = new InfoRegion();
        infoRegion.setPickOnBounds(false);
        enableNode(infoRegion, false);

        lowerRightRegion = new LowerRightRegion();
        enableNode(lowerRightRegion, false);

        pane = new Pane(backgroundImageView, notifyRegion, infoRegion, lowerRightRegion);
        pane.getStyleClass().add("tile");
        pane.setBorder(new Border(new BorderStroke(tile.getBorderColor(), BorderStrokeStyle.SOLID, new CornerRadii(PREFERRED_WIDTH * 0.025), new BorderWidths(tile.getBorderWidth()))));
        pane.setBackground(new Background(new BackgroundFill(tile.getBackgroundColor(), new CornerRadii(PREFERRED_WIDTH * 0.025), Insets.EMPTY)));

        getChildren().setAll(pane);
    }

    protected void registerListeners() {
        tile.widthProperty().addListener(sizeListener);
        tile.heightProperty().addListener(sizeListener);
        tile.addTileObserver(TileEvt.ANY, observer);
        tile.currentValueProperty().addListener(currentValueListener);
        if (null != infoRegionHandler) { infoRegion.addEventHandler(MouseEvent.ANY, infoRegionHandler); }
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
        if (TileEvt.RESIZE.getName().equals(EVENT_TYPE)) {
            resize();
            redraw();
        } else if (TileEvt.REDRAW.getName().equals(EVENT_TYPE)) {
            redraw();
        } else if (TileEvt.RECALC.getName().equals(EVENT_TYPE)) {
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
        } else if (TileEvt.SECTION.getName().equals(EVENT_TYPE)) {
            sections = tile.getSections();
        } else if (TileEvt.SHOW_NOTIFY_REGION.getName().equals(EVENT_TYPE)) {
            enableNode(notifyRegion, true);
        } else if (TileEvt.HIDE_NOTIFY_REGION.equals(EVENT_TYPE)) {
            enableNode(notifyRegion, false);
        } else if (TileEvt.SHOW_INFO_REGION.getName().equals(EVENT_TYPE)) {
            enableNode(infoRegion, true);
        } else if (TileEvt.HIDE_INFO_REGION.getName().equals(EVENT_TYPE)) {
            enableNode(infoRegion, false);
        } else if (TileEvt.SHOW_LOWER_RIGHT_REGION.getName().equals(EVENT_TYPE)) {
            enableNode(lowerRightRegion, true);
        } else if (TileEvt.HIDE_LOWER_RIGHT_REGION.getName().equals(EVENT_TYPE)) {
            enableNode(lowerRightRegion, false);
        } else if (TileEvt.BACKGROUND_IMAGE.getName().equals(EVENT_TYPE)) {
            if (null == tile.getBackgroundImage()) {
                enableNode(backgroundImageView, false);
            } else {
                backgroundImageView.setImage(tile.getBackgroundImage());
                backgroundImageView.setFitWidth(width);
                backgroundImageView.setFitHeight(height);
                enableNode(backgroundImageView, true);
            }
        } else if (TileEvt.REGIONS_ON_TOP.getName().equals(EVENT_TYPE)) {
            // Set upper left and upper right notifiers to front
            notifyRegion.toFront();
            infoRegion.toFront();
        } else if (TileEvt.INFO_REGION_HANDLER.getName().equals(EVENT_TYPE)) {
            if (null != infoRegionHandler) { infoRegion.removeEventHandler(MouseEvent.ANY, infoRegionHandler); }
            infoRegionHandler = tile.getInfoRegionHandler();
            if (null == infoRegionHandler) { return; }
            infoRegion.addEventHandler(MouseEvent.ANY, infoRegionHandler);
        }
    }

    protected void handleCurrentValue(final double VALUE) {}

    /**
     * Returns the bounds of the content area. Keep in mind that
     * the skin property of the Tile has to be set before you can
     * get the content bounds. As long as getSkin() == null you get
     * null as return value here.
     * @return the bounds of the content area.
     */
    public CtxBounds getContentBounds() { return contentBounds; }

    /**
     * Adds a listener to the content bounds area.
     * Keep in mind that you can only add a listener to the bounds if the skin property is set
     * in the control.
     */
    public void setOnContentBoundsChanged(final EvtObserver<BoundsEvt> OBSERVER) { contentBounds.setOnBoundsEvt(OBSERVER); }
    public void removeOnContentBoundsChanged(final EvtObserver<BoundsEvt> OBSERVER) { contentBounds.removeBoundsEvtObserver(OBSERVER); }

    public NotifyRegion getNotifyRegion() { return notifyRegion; }

    public InfoRegion getInfoRegion() { return infoRegion; }

    public LowerRightRegion getLowerRightRegion() { return lowerRightRegion; }

    @Override public void dispose() {
        contentBounds.removeAllBoundsEvtObservers();
        tile.widthProperty().removeListener(sizeListener);
        tile.heightProperty().removeListener(sizeListener);
        tile.removeTileObserver(TileEvt.ANY, observer);
        tile.currentValueProperty().removeListener(currentValueListener);
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

        if (tile.isShowing() && width > 0 && height > 0) {
            //pane.setMaxSize(size, size);
            //pane.relocate((width - size) * 0.5, (height - size) * 0.5);

            double offsetTop    = tile.getTitle().isEmpty() ? inset : size * 0.15;
            double offsetBottom = (tile.getText().isEmpty() || !tile.isTextVisible()) ? height - inset : height - size * 0.15;
            contentBounds.setX(inset);
            contentBounds.setY(offsetTop);
            contentBounds.setWidth(width - doubleInset);
            contentBounds.setHeight(offsetBottom - offsetTop);

            contentCenterX = contentBounds.getX() + contentBounds.getWidth() * 0.5;
            contentCenterY = contentBounds.getY() + contentBounds.getHeight() * 0.5;

            pane.setMaxSize(width, height);
            pane.setPrefSize(width, height);

            if (backgroundImageView.isVisible()) {
                if (tile.getRoundedCorners()) {
                    Rectangle imgClip = new Rectangle(width, height);
                    imgClip.setArcWidth(clamp(0, Double.MAX_VALUE, inset));
                    imgClip.setArcHeight(clamp(0, Double.MAX_VALUE, inset));
                    backgroundImageView.setClip(imgClip);
                }
                backgroundImageView.setFitWidth(width);
                backgroundImageView.setFitHeight(height);
                backgroundImageView.setPreserveRatio(tile.getBackgroundImageKeepAspect());
                backgroundImageView.relocate((width - backgroundImageView.getLayoutBounds().getWidth()) * 0.5, (height - backgroundImageView.getLayoutBounds().getHeight()) * 0.5);
            }

            double regionSize = size * 0.105;
            notifyRegion.setPrefSize(regionSize, regionSize);
            notifyRegion.relocate(width - regionSize, 0);

            infoRegion.setPrefSize(regionSize, regionSize);
            infoRegion.relocate(0, 0);

            lowerRightRegion.setPrefSize(regionSize, regionSize);
            lowerRightRegion.relocate(width - regionSize, height - regionSize);

            resizeStaticText();
            resizeDynamicText();
        }
    }

    protected void redraw() {
        boolean hasRoundedCorners = tile.getRoundedCorners();
        pane.setBorder(new Border(new BorderStroke(tile.getBorderColor(), BorderStrokeStyle.SOLID, hasRoundedCorners ? new CornerRadii(clamp(0, Double.MAX_VALUE, size * 0.025)) : CornerRadii.EMPTY, new BorderWidths(clamp(0, Double.MAX_VALUE, tile.getBorderWidth() / PREFERRED_WIDTH * size)))));
        pane.setBackground(new Background(new BackgroundFill(tile.getBackgroundColor(), hasRoundedCorners ? new CornerRadii(clamp(0, Double.MAX_VALUE, size * 0.025)) : CornerRadii.EMPTY, Insets.EMPTY)));

        backgroundImageView.setOpacity(tile.getBackgroundImageOpacity());

        notifyRegion.setRoundedCorner(hasRoundedCorners);
        notifyRegion.setBackgroundColor(tile.getNotifyRegionBackgroundColor());
        notifyRegion.setForegroundColor(tile.getNotifyRegionForegroundColor());
        notifyRegion.setTooltipText(tile.getNotifyRegionTooltipText());

        infoRegion.setRoundedCorner(hasRoundedCorners);
        infoRegion.setBackgroundColor(tile.getInfoRegionBackgroundColor());
        infoRegion.setForegroundColor(tile.getInfoRegionForegroundColor());
        infoRegion.setTooltipText(tile.getInfoRegionTooltipText());

        lowerRightRegion.setRoundedCorner(hasRoundedCorners);
        lowerRightRegion.setBackgroundColor(tile.getLowerRightRegionBackgroundColor());
        lowerRightRegion.setForegroundColor(tile.getLowerRightRegionForegroundColor());
        lowerRightRegion.setTooltipText(tile.getLowerRightRegionTooltipText());

        locale = tile.getLocale();
        if (tile.getCustomDecimalFormatEnabled()) {
            decimalFormat = tile.getCustomDecimalFormat();
        } else {
            formatString = new StringBuilder("%.").append(tile.getDecimals()).append("f").toString();
        }
        tickLabelFormatString = new StringBuilder("%.").append(tile.getTickLabelDecimals()).append("f").toString();
        sectionsVisible       = tile.getSectionsVisible();
        textSize              = tile.getTextSize();
    }
}
