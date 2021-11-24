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

import eu.hansolo.tilesfx.Tile.ImageMask;
import eu.hansolo.tilesfx.Tile.SkinType;
import eu.hansolo.tilesfx.addons.ImageSpinner;
import eu.hansolo.tilesfx.addons.SpinnerBuilder;
import eu.hansolo.tilesfx.addons.SpinnerType;
import eu.hansolo.tilesfx.chart.ChartData;
import eu.hansolo.tilesfx.colors.Bright;
import eu.hansolo.tilesfx.colors.Dark;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.skins.BarChartItem;
import eu.hansolo.tilesfx.tools.Helper;
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
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
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
    private              Tile            tile2;
    private              long            lastTimerCall;
    private AnimationTimer               timer;
    private ChartData                    cpuChartData;
    private ChartData                    memChartData;



    @Override public void init() {
        cpuChartData = new ChartData("CPU", 0.0, Tile.GREEN);
        cpuChartData.setTextColor(Color.WHITE);
        cpuChartData.setFormatString("%.0f%%");

        memChartData = new ChartData("MEM", 0.0, Tile.GREEN);
        memChartData.setTextColor(Color.WHITE);
        memChartData.setFormatString("%.0f%%");

        tile1 = TileBuilder.create()
                           .skinType(SkinType.CLUSTER_MONITOR)
                           .prefSize(WIDTH, HEIGHT)
                           .title("Production")
                           .text("blabla")
                           .maxValue(20000)
                           .locale(Locale.GERMAN)
                           .chartData(cpuChartData, memChartData)
                           .animated(true)
                           .shortenNumbers(true)
                           .build();

        tile2 = TileBuilder.create()
                           .skinType(SkinType.CENTER_TEXT)
                           .title("Server")
                           .text("Last check")
                           .backgroundColor(Dark.GREEN)
                           .description("ONLINE")
                           .build();


        lastTimerCall = System.nanoTime();
        timer = new AnimationTimer() {
            @Override public void handle(final long now) {
                if (now > lastTimerCall + 1_000_000_000l) {
                    //double v = RND.nextDouble() * tile1.getRange() + tile1.getMinValue();
                    //tile.setValue(v);
                    cpuChartData.setValue(RND.nextDouble() * 10000);
                    memChartData.setValue(RND.nextDouble() * 10000);
                    lastTimerCall = now;
                }
            }
        };
    }

    @Override public void start(Stage stage) {
        StackPane pane = new StackPane(new HBox(10, tile1, tile2));
        pane.setPadding(new Insets(10));

        Scene scene = new Scene(pane);

        stage.setTitle("Test");
        stage.setScene(scene);
        stage.show();

        // Calculate number of nodes
        calcNoOfNodes(pane);
        System.out.println(noOfNodes + " Nodes in SceneGraph");

        //tile2.setDescription("OFFLINE");
        //tile2.setBackgroundColor(Dark.RED);
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
