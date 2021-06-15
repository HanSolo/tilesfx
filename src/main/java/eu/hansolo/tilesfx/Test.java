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
package eu.hansolo.tilesfx;

import eu.hansolo.tilesfx.Tile.SkinType;
import eu.hansolo.tilesfx.skins.BarChartItem;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * Just an internal class for testing the library
 * User: hansolo
 * Date: 13.10.17
 * Time: 14:52
 */
public class Test extends Application {
    private static final Random          RND       = new Random();
    private static final double          SIZE      = 400;
    private static final double          WIDTH     = 400;
    private static final double          HEIGHT    = 400;
    private static       int             noOfNodes = 0;
    private              Tile            tile;
    private              DoubleProperty  value;
    private              long            lastTimerCall;
    private AnimationTimer               timer;



    @Override public void init() {
        value = new SimpleDoubleProperty();

        tile = TileBuilder.create()
                          .skinType(SkinType.BAR_CHART)
                          .prefSize(WIDTH, HEIGHT)
                          .title("Tile 1")
                          .animated(false)
                          .build();




        lastTimerCall = System.nanoTime();
        timer = new AnimationTimer() {
            @Override public void handle(final long now) {
                if (now > lastTimerCall + 5_000_000_000l) {
                    tile.setValue(RND.nextDouble() * tile.getRange() + tile.getMinValue());
                    lastTimerCall = now;
                }
            }
        };
    }

    @Override public void start(Stage stage) {
        StackPane pane = new StackPane(tile);
        pane.setPadding(new Insets(10));

        Scene scene = new Scene(pane);

        stage.setTitle("Test");
        stage.setScene(scene);
        stage.show();

        List<BarChartItem> items = new ArrayList<>();
        items.add(new BarChartItem("JDK17", 14, Tile.BLUE));
        items.add(new BarChartItem("JDK11", 23, Tile.BLUE));
        items.add(new BarChartItem("JDK8", 10, Tile.BLUE));
        Platform.runLater(() -> {
            tile.setBarChartItems(items);
        });

        // Calculate number of nodes
        calcNoOfNodes(pane);
        System.out.println(noOfNodes + " Nodes in SceneGraph");

        //timer.start();
    }

    @Override public void stop() {
        Platform.exit();
        System.exit(0);
    }

    private static void calcNoOfNodes(Node node) {
        if (node instanceof Parent) {
            if (((Parent) node).getChildrenUnmodifiable().size() != 0) {
                ObservableList<Node> tempChildren = ((Parent) node).getChildrenUnmodifiable();
                noOfNodes += tempChildren.size();
                for (Node n : tempChildren) { calcNoOfNodes(n); }
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
