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
    private static final Random         RND       = new Random();
    private static final double         SIZE      = 400;
    private static final double         WIDTH     = 300;
    private static final double         HEIGHT    = 600;
    private static       int            noOfNodes = 0;
    private              Tile           tile1;
    private              long           lastTimerCall;
    private              AnimationTimer timer;
    private              ChartData      chartData1;
    private              ChartData      chartData2;
    private              ChartData      chartData3;
    private              ChartData      chartData4;
    private              ChartData      chartData5;
    private              ChartData      chartData6;
    private              ChartData      chartData7;
    private              ChartData      chartData8;



    @Override public void init() {
        chartData1 = new ChartData("Item 1", 24.0, Tile.GREEN);
        chartData2 = new ChartData("Item 2", 10.0, Tile.BLUE);
        chartData3 = new ChartData("Item 3", 12.0, Tile.RED);
        chartData4 = new ChartData("Item 4", 13.0, Tile.YELLOW_ORANGE);
        chartData5 = new ChartData("Item 5", 13.0, Tile.BLUE);
        chartData6 = new ChartData("Item 6", 13.0, Tile.BLUE);
        chartData7 = new ChartData("Item 7", 13.0, Tile.BLUE);
        chartData8 = new ChartData("Item 8", 13.0, Tile.BLUE);

        tile1 = TileBuilder.create()
                           .skinType(SkinType.CYCLE_STEP)
                           .prefSize(WIDTH, HEIGHT)
                           .title("CycleStep Tile")
                           .textVisible(false)
                           .chartData(chartData1, chartData2, chartData3, chartData4, chartData5)
                           .animated(true)
                           .decimals(0)
                           .autoItemTextColor(false)
                           .shortenNumbers(false)
                           .build();


        lastTimerCall = System.nanoTime();
        timer = new AnimationTimer() {
            @Override public void handle(final long now) {
                if (now > lastTimerCall + 3_000_000_000l) {
                    //double v = RND.nextDouble() * tile1.getRange() + tile1.getMinValue();
                    //tile.setValue(v);
                    chartData1.setValue(RND.nextDouble() * 10_000);
                    chartData2.setValue(RND.nextDouble() * 10_000);
                    chartData3.setValue(RND.nextDouble() * 10_000);
                    chartData4.setValue(RND.nextDouble() * 10_000);
                    chartData5.setValue(RND.nextDouble() * 10_000);
                    chartData6.setValue(RND.nextDouble() * 10_000);
                    chartData7.setValue(RND.nextDouble() * 10_000);
                    chartData8.setValue(RND.nextDouble() * 10_000);
                    lastTimerCall = now;
                }
            }
        };
    }

    @Override public void start(Stage stage) {
        StackPane pane = new StackPane(new HBox(10, tile1));
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
