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


import eu.hansolo.tilesfx.events.BoundsEvt;
import eu.hansolo.toolbox.evt.EvtObserver;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class CtxBounds {
    private double           x;
    private double           y;
    private double           width;
    private double                       height;
    private List<EvtObserver<BoundsEvt>> observers;


    // ******************** Constructors **************************************
    public CtxBounds() {
        this(0, 0, 0, 0);
    }
    public CtxBounds(final double WIDTH, final double HEIGHT) {
        this(0, 0, WIDTH, HEIGHT);
    }
    public CtxBounds(final double X, final double Y, final double WIDTH, final double HEIGHT) {
        x         = X;
        y         = Y;
        width     = Helper.clamp(0, Double.MAX_VALUE, WIDTH);
        height    = Helper.clamp(0, Double.MAX_VALUE, HEIGHT);
        observers = new CopyOnWriteArrayList<>();
    }


    // ******************** Methods *******************************************
    public double getX() { return x; }
    public void setX(final double X) {
        x = X;
        fireBoundsEvt();
    }

    public double getY() { return y; }
    public void setY(final double Y) {
        y = Y;
        fireBoundsEvt();
    }

    public double getMinX() { return x; }
    public double getMaxX() { return x + width; }

    public double getMinY() { return y; }
    public double getMaxY() { return y + height; }

    public double getWidth() { return width; }
    public void setWidth(final double WIDTH) {
        width = Helper.clamp(0, Double.MAX_VALUE, WIDTH);
        fireBoundsEvt();
    }

    public double getHeight() { return height; }
    public void setHeight(final double HEIGHT) {
        height = Helper.clamp(0, Double.MAX_VALUE, HEIGHT);
        fireBoundsEvt();
    }

    public double getCenterX() { return x + width * 0.5; }
    public double getCenterY() { return y + height * 0.5; }

    public void set(final CtxBounds BOUNDS) {
        set(BOUNDS.getX(), BOUNDS.getY(), BOUNDS.getWidth(), BOUNDS.getHeight());
    }
    public void set(final double X, final double Y, final double WIDTH, final double HEIGHT) {
        x      = X;
        y      = Y;
        width  = WIDTH;
        height = HEIGHT;
        fireBoundsEvt();
    }

    public void setOnBoundsEvt(final EvtObserver<BoundsEvt> OBSERVER) { addBoundsEvtObserver(OBSERVER); }
    public void addBoundsEvtObserver(final EvtObserver<BoundsEvt> OBSERVER) { if (!observers.contains(OBSERVER)) { observers.add(OBSERVER); }}
    public void removeBoundsEvtObserver(final EvtObserver<BoundsEvt> OBSERVER) { if (observers.contains(OBSERVER)) { observers.remove(OBSERVER); }}
    public void removeAllBoundsEvtObservers() { observers.clear(); }

    public void fireBoundsEvt() {
        final BoundsEvt boundsEvent = new BoundsEvt(CtxBounds.this, BoundsEvt.BOUNDS, CtxBounds.this);
        observers.forEach(observer -> observer.handle(boundsEvent));
    }


    @Override public String toString() {
        return new StringBuilder().append("[x:").append(getX()).append(", ")
                                  .append("y:").append(getY()).append(", ")
                                  .append("w:").append(getWidth()).append(", ")
                                  .append("h:").append(getHeight()).append("]")
                                  .toString();
    }
}
