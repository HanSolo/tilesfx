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
import eu.hansolo.tilesfx.events.ChartDataEvent.EventType;
import eu.hansolo.tilesfx.events.ChartDataEventListener;
import eu.hansolo.tilesfx.events.TileEvent;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.Helper;
import eu.hansolo.tilesfx.tools.PrettyListView;
import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by hansolo on 19.12.16.
 */
public class LeaderBoardTileSkin extends TileSkin {
    private Text                                           titleText;
    private Text                                           text;
    private PrettyListView<LeaderBoardItem>                leaderBoardPane;
    private ChartDataEventListener                         updateHandler;
    private InvalidationListener                           paneSizeListener;
    private Map<LeaderBoardItem, EventHandler<MouseEvent>> handlerMap;
    private ListChangeListener<LeaderBoardItem>            leaderBoardItemListener;


    // ******************** Constructors **************************************
    public LeaderBoardTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        updateHandler           = e -> {
            final EventType TYPE = e.getType();
            switch (TYPE) {
                case UPDATE  : updateChart(); break;
                case FINISHED: sortItems(); break;
            }
        };
        paneSizeListener        = o -> resizeItems();
        handlerMap              = new HashMap<>();
        leaderBoardItemListener = change -> {
            while (change.next()) {
                if (change.wasPermutated()) {
                } else if (change.wasUpdated()) {
                } else if (change.wasAdded()) {
                    change.getAddedSubList().forEach(addedItem -> {
                        addedItem.setFormatString(formatString);
                        addedItem.addChartDataEventListener(updateHandler);
                        addedItem.setItemSortingTopic(tile.getItemSortingTopic());
                        EventHandler<MouseEvent> clickHandler = e -> tile.fireTileEvent(new TileEvent(TileEvent.EventType.SELECTED_CHART_DATA, addedItem.getChartData()));
                        handlerMap.put(addedItem, clickHandler);
                        addedItem.addEventHandler(MouseEvent.MOUSE_PRESSED, clickHandler);
                        leaderBoardPane.getItems().add(addedItem);
                    });
                } else if (change.wasRemoved()) {
                    change.getRemoved().forEach(removedItem -> {
                        removedItem.removeChartDataEventListener(updateHandler);
                        removedItem.removeEventHandler(MouseEvent.MOUSE_PRESSED, handlerMap.get(removedItem));
                        leaderBoardPane.getItems().remove(removedItem);
                    });
                }
            }
            updateChart();
            resizeItems();
        };

        registerItemListeners();

        tile.getLeaderBoardItems().forEach(item -> {
            item.setItemSortingTopic(tile.getItemSortingTopic());
            item.setShortenNumbers(tile.getShortenNumbers());
        });

        leaderBoardPane = new PrettyListView();
        leaderBoardPane.getItems().addAll(tile.getLeaderBoardItems());

        sortItems();

        titleText = new Text();
        titleText.setFill(tile.getTitleColor());
        Helper.enableNode(titleText, !tile.getTitle().isEmpty());

        text = new Text(tile.getText());
        text.setFill(tile.getUnitColor());
        Helper.enableNode(text, tile.isTextVisible());

        getPane().getChildren().addAll(titleText, text, leaderBoardPane);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        pane.widthProperty().addListener(paneSizeListener);
        pane.heightProperty().addListener(paneSizeListener);
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if (TileEvent.EventType.VISIBILITY.name().equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !tile.getTitle().isEmpty());
            Helper.enableNode(text, tile.isTextVisible());
        } else if (TileEvent.EventType.DATA.name().equals(EVENT_TYPE)) {
            registerItemListeners();
        } else if (TileEvent.EventType.RECALC.name().equals(EVENT_TYPE)) {
            tile.getLeaderBoardItems().forEach(item -> item.setShortenNumbers(tile.getShortenNumbers()));
            redraw();
        }
    }

    private void registerItemListeners() {
        tile.getLeaderBoardItems().forEach(item -> {
            item.setFormatString(formatString);
            item.addChartDataEventListener(updateHandler);
            EventHandler<MouseEvent> clickHandler = e -> tile.fireTileEvent(new TileEvent(TileEvent.EventType.SELECTED_CHART_DATA, item.getChartData()));
            handlerMap.put(item, clickHandler);
            item.addEventHandler(MouseEvent.MOUSE_PRESSED, clickHandler);
        });

        tile.getLeaderBoardItems().addListener(leaderBoardItemListener);
    }

    private void sortItems() {
        List<LeaderBoardItem> items = tile.getLeaderBoardItems();
        switch(tile.getItemSorting()) {
            case ASCENDING :
                switch(tile.getItemSortingTopic()) {
                    case TIMESTAMP: items.sort(Comparator.comparing(LeaderBoardItem::getTimestamp)); break;
                    case DURATION : items.sort(Comparator.comparing(LeaderBoardItem::getDuration)); break;
                    case VALUE    :
                    default       : items.sort(Comparator.comparing(LeaderBoardItem::getValue)); break;
                }
                break;
            case DESCENDING:
                switch(tile.getItemSortingTopic()) {
                    case TIMESTAMP: items.sort(Comparator.comparing(LeaderBoardItem::getTimestamp).reversed()); break;
                    case DURATION : items.sort(Comparator.comparing(LeaderBoardItem::getDuration).reversed()); break;
                    case VALUE    :
                    default       : items.sort(Comparator.comparing(LeaderBoardItem::getValue).reversed()); break;
                }
                break;
            case NONE:
            default: break;
        }
        items.forEach(i -> i.setIndex(items.indexOf(i)));
        updateChart();
    }

    @Override public void dispose() {
        pane.widthProperty().removeListener(paneSizeListener);
        pane.heightProperty().removeListener(paneSizeListener);
        tile.getLeaderBoardItems().forEach(item -> {
            item.removeChartDataEventListener(updateHandler);
            item.removeEventHandler(MouseEvent.MOUSE_PRESSED, handlerMap.get(item));
        });
        tile.getLeaderBoardItems().removeListener(leaderBoardItemListener);
        handlerMap.clear();
        super.dispose();
    }


    // ******************** Resizing ******************************************
    private void updateChart() {
        Collections.sort(leaderBoardPane.getItems(), Comparator.comparing(LeaderBoardItem::getValue).reversed());
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
    }

    private void resizeItems() {
        double itemHeight = Helper.clamp(30, 72, height * 0.14);
        leaderBoardPane.getItems().forEach(item -> {
            item.setParentSize(width, height);
            item.setPrefSize(width, itemHeight);
            item.setMaxSize(width, itemHeight);
        });
    }

    @Override protected void resize() {
        super.resize();

        leaderBoardPane.setPrefSize(width, contentBounds.getHeight());
        leaderBoardPane.relocate(0, contentBounds.getY());
        resizeItems();
        updateChart();
    }

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(tile.getTitle());
        text.setText(tile.getText());

        tile.getLeaderBoardItems().forEach(item -> {
            item.setNameColor(tile.getTextColor());
            item.setValueColor(tile.getValueColor());
        });

        resizeDynamicText();
        resizeStaticText();

        titleText.setFill(tile.getTitleColor());
        text.setFill(tile.getTextColor());
    }
}
