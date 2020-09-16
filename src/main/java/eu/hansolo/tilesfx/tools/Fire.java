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
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.util.Random;


public class Fire extends Canvas {
    private static final Random          RND             = new Random();
    private static final Image           IMAGE           = new Image(Tile.class.getResourceAsStream("fire.png"));
    private static final double          HALF_WIDTH      = IMAGE.getWidth() * 0.5;
    private static final double          HALF_HEIGHT     = IMAGE.getHeight() * 0.5;
    private static final int             NO_OF_PARTICLES = 150;
    private              double          width;
    private              double          height;
    private              boolean         running;
    private              GraphicsContext ctx;
    private              AnimationTimer  timer;
    // Parameters for array based particles
    private static final int             NO_OF_FIELDS    = 9; // x, y, vx, vy, opacity, size, life, remaining life, active
    private static final int             ARRAY_LENGTH    = NO_OF_PARTICLES * NO_OF_FIELDS;
    private static final int             X               = 0;
    private static final int             Y               = 1;
    private static final int             VX              = 2;
    private static final int             VY              = 3;
    private static final int             OPACITY         = 4;
    private static final int             SIZE            = 5;
    private static final int             LIFE            = 6;
    private static final int             REMAINING_LIFE  = 7;
    private static final int             ACTIVE          = 8;
    private boolean                      particlesVisible;
    private boolean                      initialized;
    private double[]                     particles;


    // ******************** Constructor ***************************************
    public Fire() {
        running          = false;
        ctx              = getGraphicsContext2D();
        width            = getWidth();
        height           = getHeight();
        timer            = new AnimationTimer() {
            @Override public void handle(final long NOW) {
                draw();
            }
        };
        particlesVisible = true;
        initialized      = false;
        setMouseTransparent(true);
        registerListeners();
    }

    public void init() {
        // Initialize particles
        particles = new double[ARRAY_LENGTH];
        int pos   = 0; // next position to insert new particle
        for (int i = 0 ; i < NO_OF_PARTICLES - NO_OF_FIELDS; i++) {
            initParticle(pos);
            pos += NO_OF_FIELDS;
        }
    }

    private void registerListeners() {
        widthProperty().addListener((o, ov, nv)  -> width  = nv.doubleValue());
        heightProperty().addListener((o, ov, nv) -> height = nv.doubleValue());
    }


    // ******************** Methods *******************************************
    public void start() {
        if (running) { return; }
        running = true;
        if (!initialized) { init(); }
        timer.start();
    }

    public void stop() {
        if (!running) { return; }
        running = false;
    }

    public boolean isRunning() { return running; }

    private void initParticle(final int POS) {
        particles[POS + X]              = RND.nextDouble() * width;
        particles[POS + Y]              = height + HALF_HEIGHT;
        particles[POS + VX]             = (RND.nextDouble() * 2.0) - 1.0;
        particles[POS + VY]             = -(RND.nextDouble() * 3);
        particles[POS + OPACITY]        = 1.0;
        particles[POS + SIZE]           = (RND.nextDouble() * 1.0) + 0.5;
        particles[POS + LIFE]           = (RND.nextDouble() * 20) + 40;
        particles[POS + REMAINING_LIFE] = particles[POS + LIFE];
        particles[POS + ACTIVE]         = 1;
    }

    private void update(final int POS) {
        // Update only active particles
        if (particles[POS + ACTIVE] > 0) {
            // Calculate opacity
            particles[POS + OPACITY] = (particles[POS + REMAINING_LIFE] / particles[POS + LIFE] * 0.5);

            // Calculate new pos
            particles[POS + X] += particles[POS + VX];
            particles[POS + Y] += particles[POS + VY];

            // Calculate remaining life
            particles[POS + REMAINING_LIFE]--;

            //regenerate particles
            if(particles[POS + REMAINING_LIFE] < 0 || particles[POS + SIZE] < 0 || particles[POS + OPACITY] < 0.01) {
                if (running) {
                    initParticle(POS);
                } else {
                    if (particles[POS + OPACITY] < 0) {
                        particles[POS + ACTIVE]  = 0;
                    }
                }
            }
        }
    }

    private void draw() {
        ctx.clearRect(0, 0, width, height);
        particlesVisible = false;
        for (int pos = 0 ; pos < NO_OF_PARTICLES; pos += NO_OF_FIELDS) {
            // Update particle data
            update(pos);
            if (particles[pos + OPACITY] > 0.01) { particlesVisible = true; }

            // Draw particle from image
            ctx.save();
            ctx.translate(particles[pos + X], particles[pos + Y]);
            ctx.scale(particles[pos + SIZE], particles[pos + SIZE]);
            ctx.translate(-HALF_WIDTH, -HALF_HEIGHT);
            ctx.setGlobalAlpha(particles[pos + OPACITY]);
            ctx.drawImage(IMAGE, 0, 0);
            ctx.restore();
        }
        if (!particlesVisible) { timer.stop(); }
    }
}
