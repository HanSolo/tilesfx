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


import eu.hansolo.tilesfx.events.BoundsEvent;
import eu.hansolo.tilesfx.events.BoundsEventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class CtxBounds {
    private double                    x;
    private double                    y;
    private double                    width;
    private double                    height;
    private List<BoundsEventListener> listeners;


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
        listeners = new CopyOnWriteArrayList<>();
    }


    // ******************** Methods *******************************************
    public double getX() { return x; }
    public void setX(final double X) {
        x = X;
        fireBoundsEvent();
    }

    public double getY() { return y; }
    public void setY(final double Y) {
        y = Y;
        fireBoundsEvent();
    }

    public double getMinX() { return x; }
    public double getMaxX() { return x + width; }

    public double getMinY() { return y; }
    public double getMaxY() { return y + height; }

    public double getWidth() { return width; }
    public void setWidth(final double WIDTH) {
        width = Helper.clamp(0, Double.MAX_VALUE, WIDTH);
        fireBoundsEvent();
    }

    public double getHeight() { return height; }
    public void setHeight(final double HEIGHT) {
        height = Helper.clamp(0, Double.MAX_VALUE, HEIGHT);
        fireBoundsEvent();
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
        fireBoundsEvent();
    }

    public void setOnBoundsEvent(final BoundsEventListener LISTENER) { addBoundsEventListener(LISTENER); }
    public void addBoundsEventListener(final BoundsEventListener LISTENER) { if (!listeners.contains(LISTENER)) { listeners.add(LISTENER); }}
    public void removeBoundsEventListener(final BoundsEventListener LISTENER) { if (listeners.contains(LISTENER)) { listeners.remove(LISTENER); }}
    public void removeAllListeners() { listeners.clear(); }

    public void fireBoundsEvent() {
        final BoundsEvent boundsEvent = new BoundsEvent(CtxBounds.this);
        for (BoundsEventListener listener : listeners) { listener.onBoundsEvent(boundsEvent); }
    }


    @Override public String toString() {
        return new StringBuilder().append("[x:").append(getX()).append(", ")
                                  .append("y:").append(getY()).append(", ")
                                  .append("w:").append(getWidth()).append(", ")
                                  .append("h:").append(getHeight()).append("]")
                                  .toString();
    }
}
