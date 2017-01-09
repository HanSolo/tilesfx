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

import eu.hansolo.tilesfx.BarChartSegment;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.events.BarChartEvent;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.Helper;
import javafx.application.Platform;
import javafx.beans.WeakInvalidationListener;
import javafx.collections.WeakListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Created by hansolo on 19.12.16.
 */
public class BarChartTileSkin extends TileSkin {
    private Text                        titleText;
    private Pane                        barChartPane;
    private EventHandler<BarChartEvent> updateHandler;


    // ******************** Constructors **************************************
    public BarChartTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        updateHandler = e -> updateChart();

        List<BarChartSegment> barChartData = getSkinnable().getBarChartData().stream()
                                                           .sorted(Comparator.comparing(BarChartSegment::getValue).reversed())
                                                           .collect(Collectors.toList());

        getSkinnable().getBarChartData().forEach(segment -> {
            segment.addEventHandler(BarChartEvent.UPDATE, updateHandler);
            segment.setMaxValue(getSkinnable().getMaxValue());
            segment.setFormatString(formatString);
            segment.valueProperty().addListener(new WeakInvalidationListener(o -> updateChart()));
        });
        barChartPane = new Pane();
        barChartPane.getChildren().addAll(barChartData);

        titleText = new Text();
        titleText.setFill(getSkinnable().getTitleColor());
        Helper.enableNode(titleText, !getSkinnable().getTitle().isEmpty());

        getPane().getChildren().addAll(titleText, barChartPane);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        getSkinnable().getBarChartData().addListener(new WeakListChangeListener<>(change -> {
            while (change.next()) {
                if (change.wasPermutated()) {
                    //updateChart();
                } else if (change.wasUpdated()) {
                    //updateChart();
                } else if (change.wasAdded()) {
                    change.getAddedSubList().forEach(addedItem -> {
                        barChartPane.getChildren().add(addedItem);
                        addedItem.addEventHandler(BarChartEvent.UPDATE, updateHandler);
                    });
                    updateChart();
                } else if (change.wasRemoved()) {
                    change.getRemoved().forEach(removedItem -> {
                        removedItem.removeEventHandler(BarChartEvent.UPDATE, updateHandler);
                        barChartPane.getChildren().remove(removedItem);
                    });
                    updateChart();
                }
            }
        }));
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !getSkinnable().getTitle().isEmpty());
        } else if ("DATA".equals(EVENT_TYPE)) {
            Collections.sort(getSkinnable().getBarChartData(), Comparator.comparingDouble(BarChartSegment::getValue));
            Collections.reverse(getSkinnable().getBarChartData());
            getSkinnable().getBarChartData().forEach(segment -> {
                segment.setMaxValue(getSkinnable().getMaxValue());
                segment.setFormatString(formatString);
            });
            pane.getChildren().clear();
            pane.getChildren().add(titleText);
            pane.getChildren().addAll(getSkinnable().getBarChartData());
            updateChart();
        }
    };


    // ******************** Resizing ******************************************
    private void updateChart() {
        Platform.runLater(() -> {
            getSkinnable().getBarChartData().sort(Comparator.comparing(BarChartSegment::getValue).reversed());
            List<BarChartSegment> segments = getSkinnable().getBarChartData();
            int noOfSegments = segments.size();
            if (noOfSegments == 0) return;
            double maxValue = getSkinnable().getMaxValue();

            for (int i = 0 ; i < noOfSegments ; i++) {
                BarChartSegment segment = segments.get(i);
                if (i < 4) {
                    segment.setMaxValue(maxValue);
                    segment.setVisible(true);
                    segment.relocate(0, size * 0.18 + i * 0.2 * size);
                } else {
                    segment.setVisible(false);
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
    };

    @Override protected void resize() {
        super.resize();
        barChartPane.setPrefSize(pane.getPrefWidth(), pane.getPrefHeight());
        updateChart();
    };

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(getSkinnable().getTitle());

        getSkinnable().getBarChartData().forEach(segment -> {
            segment.setNameColor(getSkinnable().getTextColor());
            segment.setValueColor(getSkinnable().getValueColor());
        });

        resizeDynamicText();
        resizeStaticText();

        titleText.setFill(getSkinnable().getTitleColor());
    };
}
