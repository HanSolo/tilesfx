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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    private Tile           tile;
    private long           lastTimerCall;
    private AnimationTimer timer;

    @Override public void init() {
        Image     img     = new Image(TilesFXTest.class.getResourceAsStream("JavaChampion.png"));
        ImageView imgView = new ImageView(img);

        tile = TileBuilder.create()
                          .skinType(SkinType.CUSTOM)
                          .prefSize(750, 750)
                          .title("Image Tile")
                          .text("My Image")
                          .textVisible(true)
                          .graphic(imgView)
                          .build();

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
    }

    @Override public void stop() {
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
