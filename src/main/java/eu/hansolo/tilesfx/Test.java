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

package eu.hansolo.tilesfx;

import eu.hansolo.tilesfx.Tile.ItemSorting;
import eu.hansolo.tilesfx.Tile.ItemSortingTopic;
import eu.hansolo.tilesfx.Tile.SkinType;
import eu.hansolo.tilesfx.skins.LeaderBoardItem;
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
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
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
    private              Tile            tile1;
    private LeaderBoardItem leaderBoardItem1;
    private LeaderBoardItem leaderBoardItem2;
    private LeaderBoardItem leaderBoardItem3;
    private LeaderBoardItem leaderBoardItem4;
    private LeaderBoardItem leaderBoardItem5;
    private LeaderBoardItem leaderBoardItem6;
    private LeaderBoardItem leaderBoardItem7;
    private LeaderBoardItem leaderBoardItem8;
    private LeaderBoardItem leaderBoardItem9;
    private LeaderBoardItem leaderBoardItem10;

    private              DoubleProperty  value;
    private              long            lastTimerCall;
    private AnimationTimer               timer;



    @Override public void init() {
        value = new SimpleDoubleProperty();

        leaderBoardItem1 = new LeaderBoardItem("Gerrit", 47, Instant.now(), java.time.Duration.ofMillis(1000));
        leaderBoardItem2 = new LeaderBoardItem("Sandra", 43, Instant.now().minusSeconds(10), java.time.Duration.ofMillis(1000));
        leaderBoardItem3 = new LeaderBoardItem("Lilli", 12, Instant.now().minusSeconds(20), java.time.Duration.ofMillis(1000));
        leaderBoardItem4 = new LeaderBoardItem("Anton", 8, Instant.now().minusSeconds(30), java.time.Duration.ofMillis(1000));
        leaderBoardItem5 = new LeaderBoardItem("Neo", 5, Instant.now().minusSeconds(40), java.time.Duration.ofMillis(1000));
        leaderBoardItem6 = new LeaderBoardItem("Katja", 47, Instant.now().minusSeconds(50), java.time.Duration.ofMillis(1000));
        leaderBoardItem7 = new LeaderBoardItem("Francis", 43, Instant.now().minusSeconds(60), java.time.Duration.ofMillis(1000));
        leaderBoardItem8 = new LeaderBoardItem("Luis", 12, Instant.now().minusSeconds(70), java.time.Duration.ofMillis(1000));
        leaderBoardItem9 = new LeaderBoardItem("Glumse", 8, Instant.now().minusSeconds(80), java.time.Duration.ofMillis(1000));
        leaderBoardItem10 = new LeaderBoardItem("Gumbert", 5, Instant.now().minusSeconds(90), java.time.Duration.ofMillis(1000));

        tile1 = TileBuilder.create()
                           .skinType(SkinType.LEADER_BOARD)
                           .prefSize(WIDTH, HEIGHT)
                           .title("LeaderBoard Tile")
                           .textVisible(true)
                           .itemSorting(ItemSorting.DESCENDING)
                           .itemSortingTopic(ItemSortingTopic.DURATION)
                           .leaderBoardItems(leaderBoardItem1, leaderBoardItem2, leaderBoardItem3, leaderBoardItem4, leaderBoardItem5,
                                             leaderBoardItem6, leaderBoardItem7, leaderBoardItem8, leaderBoardItem9, leaderBoardItem10)

                           .build();

        tile1.getLeaderBoardItems().forEach(item -> item.setTimestampFormatter(DateTimeFormatter.ofPattern("hh:mm:ss")));

        lastTimerCall = System.nanoTime();
        timer = new AnimationTimer() {
            @Override public void handle(final long now) {
                if (now > lastTimerCall + 5_000_000_000l) {
                    //tile1.addChartData(new ChartData("", RND.nextDouble() * 300 + 50, Instant.now()));
                    //double value = RND.nextDouble() * tile1.getRange() + tile1.getMinValue();
                    //tile1.setValue(value + 20);
                    //System.out.println("No of data in list: " + tile1.getChartData().size());
                    //tile1.setValue(RND.nextDouble() * tile1.getRange() + tile1.getMinValue());
                    //tile1.getLeaderBoardItems().get(RND.nextInt(tile1.getLeaderBoardItems().size())).setValue(RND.nextDouble() * 80);
                    tile1.getLeaderBoardItems().get(RND.nextInt(tile1.getLeaderBoardItems().size())).setDuration(Duration.ofSeconds(RND.nextInt(1000)));
                    lastTimerCall = now;
                }
            }
        };
    }

    @Override public void start(Stage stage) {
        StackPane pane = new StackPane(tile1);
        pane.setBackground(new Background(new BackgroundFill(Tile.BACKGROUND, CornerRadii.EMPTY, Insets.EMPTY)));
        pane.setPadding(new Insets(10));

        Scene scene = new Scene(pane);

        stage.setTitle("Test");
        stage.setScene(scene);
        stage.show();

        // Calculate number of nodes
        calcNoOfNodes(pane);
        System.out.println(noOfNodes + " Nodes in SceneGraph");

        timer.start();
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
