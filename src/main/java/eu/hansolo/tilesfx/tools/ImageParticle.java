/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2016-2020 Gerrit Grunwald.
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
package eu.hansolo.tilesfx.tools;

import eu.hansolo.tilesfx.Tile;
import javafx.scene.image.Image;

import java.util.Random;


public class ImageParticle {
    private static final Random RND = new Random();
    public  double x;
    public  double y;
    public  double vX;
    public  double vY;
    public  double opacity;
    public  double size;
    public  Image  image;
    public  double halfWidth;
    public  double halfHeight;
    public  double life;
    public double remainingLife;


    // ******************** Constructor ***********************************
    public ImageParticle(final Image IMAGE, final double WIDTH, final double HEIGHT) {
        image      = IMAGE;
        halfWidth  = IMAGE.getWidth() * 0.5;
        halfHeight = IMAGE.getHeight() * 0.5;

        // Position
        x = RND.nextDouble() * WIDTH;
        y = HEIGHT + halfHeight;

        // Size
        size = (RND.nextDouble() * 1) + 0.5;

        // Velocity
        vX = (RND.nextDouble() * 0.5) - 0.25;
        vY = -(RND.nextDouble() * 3);

        // Opacity
        opacity = 1.0;

        // Image
        image = IMAGE;

        // Life
        life          = (RND.nextDouble() * 20) + 40;
        remainingLife = life;
    }

    public void reInit(final double WIDTH, final double HEIGHT) {
        // Position
        x = RND.nextDouble() * WIDTH;
        y = HEIGHT + halfHeight;

        // Size
        size = (RND.nextDouble() * 1) + 0.5;

        // Velocity
        vX = (RND.nextDouble() * 0.5) - 0.25;
        vY = -(RND.nextDouble() * 3);

        // Opacity
        opacity = 1.0;

        // Life
        life          = (RND.nextDouble() * 20) + 40;
        remainingLife = life;
    }
}
