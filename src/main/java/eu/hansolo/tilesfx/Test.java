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

import eu.hansolo.tilesfx.Tile.ImageMask;
import eu.hansolo.tilesfx.Tile.ItemSorting;
import eu.hansolo.tilesfx.Tile.SkinType;
import eu.hansolo.tilesfx.colors.Medium;
import eu.hansolo.tilesfx.events.TileEvent.EventType;
import eu.hansolo.tilesfx.icons.Flag;
import eu.hansolo.tilesfx.skins.BarChartItem;
import eu.hansolo.tilesfx.skins.LeaderBoardItem;
import eu.hansolo.tilesfx.tools.Rank;
import eu.hansolo.tilesfx.tools.Ranking;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;


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

    private              Tile           tile2;
    private BarChartItem barChartItem1;
    private BarChartItem barChartItem2;
    private BarChartItem barChartItem3;
    private BarChartItem barChartItem4;
    private BarChartItem barChartItem5;
    private BarChartItem barChartItem6;
    private BarChartItem barChartItem7;
    private BarChartItem barChartItem8;
    private BarChartItem barChartItem9;
    private BarChartItem barChartItem10;

    private              DoubleProperty  value;
    private              long            lastTimerCall;
    private AnimationTimer               timer;

    //private              Instant                  lastTimestamp;
    //private              String                   nightScoutURL;
    //private              HttpClient               httpClient;
    //private              List<ChartData>          initialGlucose;



    @Override public void init() {
        //nightScoutURL  = "http://81.169.252.235/api/v1/entries";
        //httpClient     = HttpClient.newBuilder().version(Version.HTTP_1_1).build();
        //lastTimestamp  = Instant.now();
        //initialGlucose = getInitialGlucoseData();

        value = new SimpleDoubleProperty();

        leaderBoardItem1 = new LeaderBoardItem("Gerrit", 47);
        leaderBoardItem2 = new LeaderBoardItem("Sandra", 43);
        leaderBoardItem3 = new LeaderBoardItem("Lilli", 12);
        leaderBoardItem4 = new LeaderBoardItem("Anton", 8);
        leaderBoardItem5 = new LeaderBoardItem("Neo", 5);
        leaderBoardItem6 = new LeaderBoardItem("Katja", 47);
        leaderBoardItem7 = new LeaderBoardItem("Francis", 43);
        leaderBoardItem8 = new LeaderBoardItem("Luis", 12);
        leaderBoardItem9 = new LeaderBoardItem("Glumse", 8);
        leaderBoardItem10 = new LeaderBoardItem("Gumbert", 5);

        barChartItem1  = new BarChartItem("Gerrit", 47, Tile.BLUE);
        barChartItem2  = new BarChartItem("Sandra", 43, Tile.RED);
        barChartItem3  = new BarChartItem("Lilli", 12, Tile.GREEN);
        barChartItem4  = new BarChartItem("Anton", 8, Tile.ORANGE);
        barChartItem5  = new BarChartItem("Neo", 8, Tile.PINK);
        barChartItem6  = new BarChartItem("Katja", 47, Tile.BLUE);
        barChartItem7  = new BarChartItem("Francis", 43, Tile.RED);
        barChartItem8  = new BarChartItem("Luis", 12, Tile.GREEN);
        barChartItem9  = new BarChartItem("Glumse", 8, Tile.ORANGE);
        barChartItem10 = new BarChartItem("Gumbert", 8, Tile.PINK);

        tile1 = TileBuilder.create()
                           .skinType(SkinType.LEADER_BOARD)
                           .prefSize(WIDTH, HEIGHT)
                           .title("LeaderBoard Tile")
                           .text("Whatever text")
                           .textVisible(false)
                           .sortedData(false)
                           .itemSorting(ItemSorting.ASCENDING)
                           .leaderBoardItems(leaderBoardItem1, leaderBoardItem2, leaderBoardItem3, leaderBoardItem4, leaderBoardItem5,
                                             leaderBoardItem6, leaderBoardItem7, leaderBoardItem8, leaderBoardItem9, leaderBoardItem10)
                           .build();

        tile2 = TileBuilder.create()
                           .skinType(SkinType.BAR_CHART)
                           .prefSize(WIDTH, HEIGHT)
                           .title("BarChart Tile")
                           .text("Whatever text")
                           //.textVisible(false)
                           .barChartItems(barChartItem1, barChartItem2, barChartItem3, barChartItem4, barChartItem5,
                                          barChartItem6, barChartItem7, barChartItem8, barChartItem9, barChartItem10)
                           .build();

        tile1.heightProperty().addListener(o1 -> adjustText());
        tile1.widthProperty().addListener(o1 -> adjustText());

        lastTimerCall = System.nanoTime();
        timer = new AnimationTimer() {
            @Override public void handle(final long now) {
                if (now > lastTimerCall + 5_000_000_000l) {
                    //tile1.addChartData(new ChartData("", RND.nextDouble() * 300 + 50, Instant.now()));
                    //double value = RND.nextDouble() * tile1.getRange() + tile1.getMinValue();
                    //tile1.setValue(value + 20);
                    //System.out.println("No of data in list: " + tile1.getChartData().size());
                    //tile1.setValue(RND.nextDouble() * tile1.getRange() + tile1.getMinValue());
                    tile1.getLeaderBoardItems().get(RND.nextInt(tile1.getLeaderBoardItems().size())).setValue(RND.nextDouble() * 80);
                    //tile2.getBarChartItems().get(RND.nextInt(tile2.getBarChartItems().size())).setValue(RND.nextDouble() * 100);
                    lastTimerCall = now;
                }
            }
        };
    }

    private void adjustText() {
        long noOfItems    = tile1.getBarChartItems().size();
        long visibleItems = tile1.getBarChartItems().stream().filter(item -> item.isVisible()).count();
        tile1.setText(visibleItems + "/" + noOfItems);
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

    /*
    private List<ChartData> getInitialGlucoseData() {
        if (null == httpClient) { httpClient = HttpClient.newBuilder().followRedirects(Redirect.NEVER).version(Version.HTTP_1_1).build(); }

        HttpRequest initialGlucoseRequest = HttpRequest.newBuilder().uri(URI.create(nightScoutURL + ".json?count=288")).GET().build();

        List<ChartData> initialData = new LinkedList<>();
        // Sync request
        try {
            HttpResponse<String> response  = httpClient.send(initialGlucoseRequest, BodyHandlers.ofString());
            Object               obj       = JSONValue.parse(response.body());
            JSONArray            jsonArray = (JSONArray) obj;
            for (int i = 0 ; i < jsonArray.size() ; i++) {
                double  glucose   = Double.parseDouble(((JSONObject) jsonArray.get(i)).get("sgv").toString());
                Instant timestamp = Instant.ofEpochSecond(Long.parseLong(((JSONObject) jsonArray.get(i)).get("date").toString()) / 1000);
                ChartData chartData = new ChartData("", glucose, timestamp);
                initialData.add(chartData);
            }
            Collections.sort(initialData, Comparator.comparing(ChartData::getTimestamp));

            HashSet<ChartData> unique = new HashSet<>();
            initialData.forEach(data -> unique.add(data));
            initialData = new LinkedList<>(unique);
        } catch (InterruptedException | IOException e) {
            System.out.println("Error sync: " + e);
        }
        return initialData;
    }
    */

    public static void main(String[] args) {
        launch(args);
    }
}
