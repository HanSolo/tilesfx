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
import eu.hansolo.tilesfx.events.UpdateEvent;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.Helper;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.collections.WeakListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Created by hansolo on 19.12.16.
 */
public class BarChartTileSkin extends TileSkin {
    private Text                      titleText;
    private Text                      text;
    private Pane                      barChartPane;
    private EventHandler<UpdateEvent> updateHandler;
    private InvalidationListener      paneSizeListener;


    // ******************** Constructors **************************************
    public BarChartTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        updateHandler    = e -> updateChart();
        paneSizeListener = o -> resizeItems();

        List<BarChartItem> barChartItems = tile.getBarChartItems().stream()
                                                         .sorted(Comparator.comparing(BarChartItem::getValue).reversed())
                                                         .collect(Collectors.toList());

        tile.getBarChartItems().forEach(item -> {
            item.addEventHandler(UpdateEvent.UPDATE_BAR_CHART, updateHandler);
            item.setMaxValue(tile.getMaxValue());
            item.setFormatString(formatString);
            item.valueProperty().addListener(new WeakInvalidationListener(o -> updateChart()));
        });
        barChartPane = new Pane();
        barChartPane.getChildren().addAll(barChartItems);

        titleText = new Text();
        titleText.setFill(tile.getTitleColor());
        Helper.enableNode(titleText, !tile.getTitle().isEmpty());

        text = new Text(tile.getText());
        text.setFill(tile.getUnitColor());
        Helper.enableNode(text, tile.isTextVisible());

        getPane().getChildren().addAll(titleText, text, barChartPane);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        tile.getBarChartItems().addListener(new WeakListChangeListener<>(change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    change.getAddedSubList().forEach(addedItem -> {
                        barChartPane.getChildren().add(addedItem);
                        addedItem.addEventHandler(UpdateEvent.UPDATE_BAR_CHART, updateHandler);
                    });
                    updateChart();
                } else if (change.wasRemoved()) {
                    change.getRemoved().forEach(removedItem -> {
                        removedItem.removeEventHandler(UpdateEvent.UPDATE_BAR_CHART, updateHandler);
                        barChartPane.getChildren().remove(removedItem);
                    });
                    updateChart();
                }
            }
        }));

        pane.widthProperty().addListener(paneSizeListener);
        pane.heightProperty().addListener(paneSizeListener);
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !tile.getTitle().isEmpty());
            Helper.enableNode(text, tile.isTextVisible());
        } else if ("DATA".equals(EVENT_TYPE)) {
            updateChart();
        }
    };

    @Override public void dispose() {
        pane.widthProperty().removeListener(paneSizeListener);
        pane.heightProperty().removeListener(paneSizeListener);
        super.dispose();
    }


    // ******************** Resizing ******************************************
    private void updateChart() {
        Platform.runLater(() -> {
            tile.getBarChartItems().sort(Comparator.comparing(BarChartItem::getValue).reversed());
            List<BarChartItem> items     = tile.getBarChartItems();
            int                noOfItems = items.size();
            if (noOfItems == 0) return;
            double maxValue = tile.getMaxValue();

            for (int i = 0 ; i < noOfItems ; i++) {
                BarChartItem item = items.get(i);
                if (i < 4) {
                    item.setMaxValue(maxValue);
                    item.setVisible(true);
                    item.relocate(0, size * 0.18 + i * 0.175 * size);
                } else {
                    item.setVisible(false);
                }
            }
        });
    }

    @Override protected void resizeStaticText() {
        double maxWidth = size * 0.9;
        double fontSize = size * textSize.factor;

        titleText.setFont(Fonts.latoRegular(fontSize));
        if (titleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(titleText, maxWidth, fontSize); }
        switch(tile.getTitleAlignment()) {
            default    :
            case LEFT  : titleText.relocate(size * 0.05, size * 0.05); break;
            case CENTER: titleText.relocate((width - titleText.getLayoutBounds().getWidth()) * 0.5, size * 0.05); break;
            case RIGHT : titleText.relocate(width - (size * 0.05) - titleText.getLayoutBounds().getWidth(), size * 0.05); break;
        }

        text.setText(tile.getText());
        text.setFont(Fonts.latoRegular(fontSize));
        if (text.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(text, maxWidth, fontSize); }
        switch(tile.getTextAlignment()) {
            default    :
            case LEFT  : text.setX(size * 0.05); break;
            case CENTER: text.setX((width - text.getLayoutBounds().getWidth()) * 0.5); break;
            case RIGHT : text.setX(width - (size * 0.05) - text.getLayoutBounds().getWidth()); break;
        }
        text.setY(height - size * 0.05);
    };

    private void resizeItems() {
        barChartPane.getChildren().forEach(node -> {
            BarChartItem item = (BarChartItem) node;
            item.setParentSize(pane.getWidth(), pane.getHeight());
            item.setPrefSize(pane.getWidth(), pane.getHeight());
        });
    }
    @Override protected void resize() {
        super.resize();
        barChartPane.setPrefSize(pane.getPrefWidth(), pane.getPrefHeight());

        updateChart();
    };

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(tile.getTitle());
        text.setText(tile.getText());

        tile.getBarChartItems().forEach(item -> {
            item.setNameColor(tile.getTextColor());
            item.setValueColor(tile.getValueColor());
        });

        resizeDynamicText();
        resizeStaticText();

        titleText.setFill(tile.getTitleColor());
        text.setFill(tile.getTextColor());
    };
}
