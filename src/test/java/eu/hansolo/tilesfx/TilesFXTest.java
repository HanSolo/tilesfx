/*
 * Copyright (c) 2018 by Gerrit Grunwald
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

import eu.hansolo.tilesfx.Tile.SkinType;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;
import javafx.scene.Scene;

import java.util.Random;


/**
 * User: hansolo
 * Date: 07.06.18
 * Time: 08:45
 */
public class TilesFXTest extends Application {
    private static final   Random RND = new Random();
    private static int     noOfNodes  = 0;
    private Tile           tile;
    private long           lastTimerCall;
    private AnimationTimer timer;

    @Override public void init() {
        tile = TileBuilder.create()
                          .skinType(SkinType.GAUGE)
                          .prefSize(400, 400)
                          .backgroundImage(new Image(TilesFXTest.class.getResourceAsStream("JavaChampion.png")))
                          //.backgroundImageOpacity(1)
                          //.infoRegionBackgroundColor(Tile.LIGHT_RED)
                          .backgroundImageKeepAspect(true)
                          .infoRegionEventHandler(e -> {
                              EventType type = e.getEventType();
                              if (type.equals(MouseEvent.MOUSE_PRESSED)) {
                                  System.out.println("Info Region pressed");
                              }
                          })
                          //.infoRegionBackgroundColor(Tile.LIGHT_RED)
                          .infoRegionTooltipText("Info Region")
                          .build();

        tile.showNotifyRegion(true);
        tile.showInfoRegion(true);

        /*
        tile = TileBuilder.create()
                          .skinType(SkinType.CUSTOM)
                          .prefSize(750, 750)
                          .title("Image Tile")
                          .text("My Image")
                          .textVisible(true)
                          .graphic(new ImageView(new Image(TilesFXTest.class.getResourceAsStream("JavaChampion.png"))))
                          .build();

        */
        lastTimerCall = System.nanoTime();
        timer = new AnimationTimer() {
            @Override public void handle(final long now) {
                if (now > lastTimerCall + 1_000_000_000l) {
                    tile.setValue(RND.nextDouble() * 25 + 5);
                    lastTimerCall = now;
                }
            }
        };
    }

    @Override public void start(Stage stage) {
        StackPane pane = new StackPane(tile);

        Scene scene = new Scene(pane);

        stage.setTitle("TilesFX Test");
        stage.setScene(scene);
        stage.show();

        // Calculate number of nodes
        calcNoOfNodes(pane);
        System.out.println(noOfNodes + " Nodes in SceneGraph");

        timer.start();
    }

    @Override public void stop() {
        System.exit(0);
    }


    // ******************** Misc **********************************************
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
