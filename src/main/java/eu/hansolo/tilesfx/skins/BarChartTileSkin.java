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


    // ******************** Constructors **************************************
    public BarChartTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        updateHandler = e -> updateChart();

        List<BarChartItem> barChartItems = getSkinnable().getBarChartItems().stream()
                                                         .sorted(Comparator.comparing(BarChartItem::getValue).reversed())
                                                         .collect(Collectors.toList());

        getSkinnable().getBarChartItems().forEach(item -> {
            item.addEventHandler(UpdateEvent.UPDATE_BAR_CHART, updateHandler);
            item.setMaxValue(getSkinnable().getMaxValue());
            item.setFormatString(formatString);
            item.valueProperty().addListener(new WeakInvalidationListener(o -> updateChart()));
        });
        barChartPane = new Pane();
        barChartPane.getChildren().addAll(barChartItems);

        titleText = new Text();
        titleText.setFill(getSkinnable().getTitleColor());
        Helper.enableNode(titleText, !getSkinnable().getTitle().isEmpty());

        text = new Text(getSkinnable().getText());
        text.setFill(getSkinnable().getUnitColor());
        Helper.enableNode(text, getSkinnable().isTextVisible());

        getPane().getChildren().addAll(titleText, text, barChartPane);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        getSkinnable().getBarChartItems().addListener(new WeakListChangeListener<>(change -> {
            while (change.next()) {
                if (change.wasPermutated()) {
                    //updateChart();
                } else if (change.wasUpdated()) {
                    //updateChart();
                } else if (change.wasAdded()) {
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

        pane.widthProperty().addListener(o -> resizeItems());
        pane.heightProperty().addListener(o -> resizeItems());
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !getSkinnable().getTitle().isEmpty());
            Helper.enableNode(text, getSkinnable().isTextVisible());
        } else if ("DATA".equals(EVENT_TYPE)) {
            updateChart();
        }
    };


    // ******************** Resizing ******************************************
    private void updateChart() {
        Platform.runLater(() -> {
            getSkinnable().getBarChartItems().sort(Comparator.comparing(BarChartItem::getValue).reversed());
            List<BarChartItem> items     = getSkinnable().getBarChartItems();
            int                noOfItems = items.size();
            if (noOfItems == 0) return;
            double maxValue = getSkinnable().getMaxValue();

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
        titleText.relocate(size * 0.05, size * 0.05);

        text.setText(getSkinnable().getText());
        text.setFont(Fonts.latoRegular(fontSize));
        if (text.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(text, maxWidth, fontSize); }
        text.setX(size * 0.05);
        text.setY(size * 0.95);
    };

    private void resizeItems() {
        barChartPane.getChildren().forEach(node -> ((BarChartItem) node).setPrefSize(pane.getWidth(), pane.getHeight()));
    }
    @Override protected void resize() {
        super.resize();
        barChartPane.setPrefSize(pane.getPrefWidth(), pane.getPrefHeight());
        updateChart();
    };

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(getSkinnable().getTitle());
        text.setText(getSkinnable().getText());

        getSkinnable().getBarChartItems().forEach(item -> {
            item.setNameColor(getSkinnable().getTextColor());
            item.setValueColor(getSkinnable().getValueColor());
        });

        resizeDynamicText();
        resizeStaticText();

        titleText.setFill(getSkinnable().getTitleColor());
        text.setFill(getSkinnable().getTextColor());
    };
}
