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
import javafx.event.EventHandler;
import javafx.event.WeakEventHandler;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Created by hansolo on 19.12.16.
 */
public class LeaderBoardTileSkin extends TileSkin {
    private Text                      titleText;
    private Text                      text;
    private Pane                      leaderBoardPane;
    private EventHandler<UpdateEvent> updateHandler;


    // ******************** Constructors **************************************
    public LeaderBoardTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        updateHandler = e -> updateChart();

        List<LeaderBoardItem> leaderBoardItems = getSkinnable().getLeaderBoardItems().stream()
                                                               .sorted(Comparator.comparing(LeaderBoardItem::getValue).reversed())
                                                               .collect(Collectors.toList());

        registerItemListeners();

        leaderBoardPane = new Pane();
        leaderBoardPane.getChildren().addAll(leaderBoardItems);

        titleText = new Text();
        titleText.setFill(getSkinnable().getTitleColor());
        Helper.enableNode(titleText, !getSkinnable().getTitle().isEmpty());

        text = new Text(getSkinnable().getText());
        text.setFill(getSkinnable().getUnitColor());
        Helper.enableNode(text, getSkinnable().isTextVisible());

        getPane().getChildren().addAll(titleText, text, leaderBoardPane);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
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
            registerItemListeners();
        }
    };

    private void registerItemListeners() {
        getSkinnable().getLeaderBoardItems().forEach(item -> {
            item.setFormatString(formatString);
            item.addEventFilter(UpdateEvent.UPDATE_LEADER_BOARD, new WeakEventHandler<>(updateHandler));
            item.valueProperty().addListener((o, ov, nv) -> sortItems());
        });
    }

    private void sortItems() {
        List<LeaderBoardItem> items = getSkinnable().getLeaderBoardItems();
        items.sort(Comparator.comparing(LeaderBoardItem::getValue).reversed());
        items.forEach(i -> i.setIndex(items.indexOf(i)));
        updateChart();
    }


    // ******************** Resizing ******************************************
    private void updateChart() {
        Platform.runLater(() -> {
            List<LeaderBoardItem> items = getSkinnable().getLeaderBoardItems();
            int noOfItems = items.size();
            if (noOfItems == 0) return;

            for (int i = 0 ; i < noOfItems ; i++) {
                LeaderBoardItem item = items.get(i);
                if (i < 4) {
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
        leaderBoardPane.getChildren().forEach(node -> ((LeaderBoardItem) node).setPrefSize(pane.getWidth(), pane.getHeight()));
    }
    @Override protected void resize() {
        super.resize();

        leaderBoardPane.setPrefSize(width, height);
        List<LeaderBoardItem> items = getSkinnable().getLeaderBoardItems();
        int noOfItems = items.size();
        if (noOfItems == 0) return;
        for (int i = 0 ; i < noOfItems ; i++) {
            LeaderBoardItem item = items.get(i);
            if (i < 4) {
                item.setVisible(true);
                item.relocate(0, size * 0.18 + i * 0.175 * size);
            } else {
                item.setVisible(false);
            }
        }
    };

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(getSkinnable().getTitle());
        text.setText(getSkinnable().getText());

        getSkinnable().getLeaderBoardItems().forEach(item -> {
            item.setNameColor(getSkinnable().getTextColor());
            item.setValueColor(getSkinnable().getValueColor());
        });

        resizeDynamicText();
        resizeStaticText();

        titleText.setFill(getSkinnable().getTitleColor());
        text.setFill(getSkinnable().getTextColor());
    };
}
