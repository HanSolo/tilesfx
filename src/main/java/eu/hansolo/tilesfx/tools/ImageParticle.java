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
package eu.hansolo.tilesfx.tools;

import eu.hansolo.tilesfx.Tile;
import javafx.scene.image.Image;

import java.util.Random;


public class ImageParticle {
    private static final Random RND = new Random();
    private double x;
    private double y;
    private double vX;
    private double vY;
    private double opacity;
    private double size;
    private Image  image;
    private double halfWidth;
    private double halfHeight;
    private double life;
    private double remainingLife;


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

    public double getX() { return x; }
    public void setX(final double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(final double y) { this.y = y; }

    public double getvX() { return vX; }
    public void setvX(final double vX) { this.vX = vX; }

    public double getvY() { return vY; }
    public void setvY(final double vY) { this.vY = vY; }

    public double getOpacity() { return opacity; }
    public void setOpacity(final double opacity) { this.opacity = opacity; }

    public double getSize() { return size; }
    public void setSize(final double size) { this.size = size; }

    public Image getImage() { return image; }
    public void setImage(final Image image) { this.image = image; }

    public double getHalfWidth() { return halfWidth; }
    public void setHalfWidth(final double halfWidth) { this.halfWidth = halfWidth; }

    public double getHalfHeight() { return halfHeight; }
    public void setHalfHeight(final double halfHeight) { this.halfHeight = halfHeight; }

    public double getLife() { return life; }
    public void setLife(final double life) { this.life = life; }

    public double getRemainingLife() { return remainingLife; }
    public void setRemainingLife(final double remainingLife) { this.remainingLife = remainingLife; }
}
