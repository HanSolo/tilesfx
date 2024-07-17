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
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;


public class Smoke extends Canvas {
    private static final Random              RND             = new Random();
    private static final Image               IMAGE           = new Image(Tile.class.getResourceAsStream("smoke.png"));
    private static final double              HALF_WIDTH      = IMAGE.getWidth() * 0.5;
    private static final double              HALF_HEIGHT     = IMAGE.getHeight() * 0.5;
    private static final long                GENERATION_RATE = 1_000_000_000l / 50;
    private static final int                 NO_OF_PARTICLES = 150;
    private              double              width;
    private              double              height;
    private              boolean             running;
    private              GraphicsContext     ctx;
    private              List<ImageParticle> particles;
    private              long                lastTimerCall;
    private              AnimationTimer      timer;


    // ******************** Constructor ***************************************
    public Smoke() {
        running       = false;
        ctx           = getGraphicsContext2D();
        width         = getWidth();
        height        = getHeight();
        particles     = new CopyOnWriteArrayList<>();
        lastTimerCall = System.nanoTime();
        timer         = new AnimationTimer() {
            @Override public void handle(final long NOW) {
                if (NOW > lastTimerCall + GENERATION_RATE) {
                    if (running && particles.size() < NO_OF_PARTICLES) { particles.add(new ImageParticle(IMAGE, width, height)); }
                    if (particles.isEmpty()) timer.stop();
                    lastTimerCall = NOW;
                }
                draw();
            }
        };
        setMouseTransparent(true);
        registerListeners();
    }

    private void registerListeners() {
        widthProperty().addListener((o, ov, nv)  -> width  = nv.doubleValue());
        heightProperty().addListener((o, ov, nv) -> height = nv.doubleValue());
    }


    // ******************** Methods *******************************************
    public void start() {
        if (running) { return; }
        running = true;
        timer.start();
    }

    public void stop() {
        if (!running) { return; }
        running = false;
    }

    public boolean isRunning() { return running; }

    private void draw() {
        ctx.clearRect(0, 0, width, height);

        for (ImageParticle p : particles) {
            p.setOpacity(p.getRemainingLife() / p.getLife() * 0.5);

            // Draw particle from image
            ctx.save();
            ctx.translate(p.getX(), p.getY());
            ctx.scale(p.getSize(), p.getSize());
            //ctx.translate(p.image.getWidth() * (-0.5), p.image.getHeight() * (-0.5));
            ctx.translate(-HALF_WIDTH, -HALF_HEIGHT);
            ctx.setGlobalAlpha(p.getOpacity());
            ctx.drawImage(p.getImage(), 0, 0);
            ctx.restore();

            //p.remainingLife--;
            p.setRemainingLife(p.getRemainingLife() * 0.98);
            //p.size *= 0.99;
            p.setX(p.getX() + p.getvX());
            p.setY(p.getY() + p.getvY());

            //regenerate particles
            if (p.getRemainingLife() < 0 || p.getSize() < 0 || p.getOpacity() < 0.01) {
                if (running) {
                    p.reInit(width, height);
                } else {
                    particles.remove(p);
                }
            }
        }
    }
}
