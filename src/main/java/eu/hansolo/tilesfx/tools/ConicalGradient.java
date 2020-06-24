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

import javafx.animation.Interpolator;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;


public class ConicalGradient {
    private static final double        ANGLE_FACTOR = 1.0 / 360.0;
    private              double        centerX;
    private              double        centerY;
    private              List<Stop>    sortedStops;
    private              WritableImage rectRaster;
    private              WritableImage roundRaster;


    // ******************** Constructors **************************************
    public ConicalGradient() {
        this(0, 0, 0, Arrays.asList(new Stop[]{}));
    }
    public ConicalGradient(final Stop... STOPS) {
        this(0, 0, 0, Arrays.asList(STOPS));
    }
    public ConicalGradient(final List<Stop> STOPS) {
        this(0, 0, 0, STOPS);
    }
    public ConicalGradient(final double CENTER_X, final double CENTER_Y, final Stop... STOPS) {
        this(CENTER_X, CENTER_Y, 0.0, Arrays.asList(STOPS));
    }
    public ConicalGradient(final double CENTER_X, final double CENTER_Y, final List<Stop> STOPS) {
        this(CENTER_X, CENTER_Y, 0.0, STOPS);
    }
    public ConicalGradient(final double CENTER_X, final double CENTER_Y, final double OFFSET, final Stop... STOPS) {
        this(CENTER_X, CENTER_Y, OFFSET, Arrays.asList(STOPS));
    }
    public ConicalGradient(final double CENTER_X, final double CENTER_Y, final double OFFSET, final List<Stop> STOPS) {
        centerX     = CENTER_X;
        centerY     = CENTER_Y;
        sortedStops = normalizeStops(OFFSET, STOPS);
    }


    // ******************** Methods *******************************************
    public void recalculateWithAngle(final double ANGLE) {
        double angle = ANGLE % 360.0;
        sortedStops  = calculate(sortedStops, ANGLE_FACTOR * angle);
        rectRaster   = null;
        roundRaster  = null;
    }

    public List<Stop> getStops() { return sortedStops; }
    public void setStops(final Stop... STOPS) {
        setStops(Arrays.asList(STOPS));
    }
    public void setStops(final double OFFSET, final Stop... STOPS) {
        setStops(OFFSET, Arrays.asList(STOPS));
    }
    public void setStops(final List<Stop> STOPS) {
        setStops(0 ,STOPS);
    }
    public void setStops(final double OFFSET, final List<Stop> STOPS) {
        sortedStops = normalizeStops(OFFSET, STOPS);
        rectRaster  = null;
        roundRaster = null;
    }

    public double[] getCenter() { return new double[]{ centerX, centerY }; }
    public double getCenterX() { return centerX; }
    public double getCenterY() { return centerY; }
    public Point2D getCenterPoint() { return new Point2D(centerX, centerY); }

    public Image getImage(final double WIDTH, final double HEIGHT) {
        int width  = (int) WIDTH  <= 0 ? 100 : (int) WIDTH;
        int height = (int) HEIGHT <= 0 ? 100 : (int) HEIGHT;

        if (rectRaster != null && width == rectRaster.getWidth() && height == rectRaster.getHeight()) return rectRaster;

        Color color = Color.TRANSPARENT;
        rectRaster  = new WritableImage(width, height);
        final PixelWriter PIXEL_WRITER = rectRaster.getPixelWriter();
        if (Double.compare(0.0, centerX) == 0) centerX = width * 0.5;
        if (Double.compare(0.0, centerY) == 0) centerY = height * 0.5;

        int calculatedStopsLength = sortedStops.size() - 1;
        for (int y = 0 ; y < height ; y++) {
            for (int x = 0 ; x < width ; x++) {
                double dx       = x - centerX;
                double dy       = y - centerY;
                double distance = Math.sqrt((dx * dx) + (dy * dy));
                distance = Double.compare(distance, 0) == 0 ? 1 : distance;

                double angle = adjustAngle(dx, dy, Math.abs(Math.toDegrees(Math.acos(dx / distance))));

                for (int i = 0; i < calculatedStopsLength; i++) {
                    double offsetI      = (sortedStops.get(i).getOffset() * 360.0);
                    double offsetIPlus1 = (sortedStops.get(i + 1).getOffset() * 360.0);
                    if (Double.compare(angle, offsetI) >= 0 &&
                        Double.compare(angle, offsetIPlus1) < 0) {
                        double fraction = (angle - offsetI) / (offsetIPlus1 - offsetI);
                        color = (Color) Interpolator.LINEAR.interpolate(sortedStops.get(i).getColor(), sortedStops.get(i + 1).getColor(), fraction);
                    }
                }
                PIXEL_WRITER.setColor(x, y, color);
            }
        }
        return rectRaster;
    }
    public Image getRoundImage(final double SIZE) {
        int size  = (int) SIZE  <= 0 ? 100 : (int) SIZE;

        if (roundRaster != null && size == roundRaster.getWidth()) return roundRaster;

        Color color = Color.TRANSPARENT;
        roundRaster = new WritableImage(size, size);
        final PixelWriter   PIXEL_WRITER = roundRaster.getPixelWriter();
        if (Double.compare(0.0, centerX) == 0) centerX = size * 0.5;
        if (Double.compare(0.0, centerY) == 0) centerY = size * 0.5;
        double radius                = size * 0.5;
        int    calculatedStopsLength = sortedStops.size() - 1;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                double dx       = x - centerX;
                double dy       = y - centerY;
                double distance = Math.sqrt((dx * dx) + (dy * dy));
                distance = Double.compare(distance, 0) == 0 ? 1 : distance;

                double angle         = adjustAngle(dx, dy, Math.abs(Math.toDegrees(Math.acos(dx / distance))));
                double radiusMinus05 = radius - 0.25;
                double radiusMinus10 = radius - 0.5;
                double radiusMinus15 = radius - 1.0;
                double radiusMinus20 = radius - 1.5;

                if (distance > radius) {
                    color = Color.TRANSPARENT;
                } else {
                    for (int i = 0; i < calculatedStopsLength; i++) {
                        if (angle >= (sortedStops.get(i).getOffset() * 360) && angle < (sortedStops.get(i + 1).getOffset() * 360)) {
                            double fraction = (angle - sortedStops.get(i).getOffset() * 360) / ((sortedStops.get(i + 1).getOffset() - sortedStops.get(i).getOffset()) * 360);
                            color = (Color) Interpolator.LINEAR.interpolate(sortedStops.get(i).getColor(), sortedStops.get(i + 1).getColor(), fraction);

                            if (distance > radiusMinus05) {
                                color = color.deriveColor(0.0, 1.0, 1.0, 0.25);
                            } else if (distance > radiusMinus10) {
                                color = color.deriveColor(0.0, 1.0, 1.0, 0.45);
                            } else if (distance > radiusMinus15) {
                                color = color.deriveColor(0.0, 1.0, 1.0, 0.65);
                            } else if (distance > radiusMinus20) {
                                color = color.deriveColor(0.0, 1.0, 1.0, 0.85);
                            }
                        }
                    }
                }
                PIXEL_WRITER.setColor(x, y, color);
            }
        }
        return roundRaster;
    }

    public ImagePattern apply(final Shape SHAPE) {
        double x      = SHAPE.getLayoutBounds().getMinX();
        double y      = SHAPE.getLayoutBounds().getMinY();
        double width  = SHAPE.getLayoutBounds().getWidth();
        double height = SHAPE.getLayoutBounds().getHeight();
        centerX       = width * 0.5;
        centerY       = height * 0.5;
        return new ImagePattern(getImage(width, height), x, y, width, height, false);
    }

    public ImagePattern getImagePattern(final Bounds BOUNDS) {
        return getImagePattern(new Rectangle(BOUNDS.getMinX(), BOUNDS.getMinY(), BOUNDS.getWidth(), BOUNDS.getHeight()));
    }
    public ImagePattern getImagePattern(final Rectangle BOUNDS) {
        double x      = BOUNDS.getX();
        double y      = BOUNDS.getY();
        double width  = BOUNDS.getWidth();
        double height = BOUNDS.getHeight();
        centerX       = width * 0.5;
        centerY       = height * 0.5;
        return new ImagePattern(getImage(width, height), x, y, width, height, false);
    }
    public ImagePattern getImagePattern(final CtxBounds BOUNDS) {
        double x      = BOUNDS.getMinX();
        double y      = BOUNDS.getMinY();
        double width  = BOUNDS.getWidth();
        double height = BOUNDS.getHeight();
        centerX       = BOUNDS.getCenterX();
        centerY       = BOUNDS.getCenterY();
        return new ImagePattern(getImage(width, height), x, y, width, height, false);
    }

    private double adjustAngle(final double DX, final double DY, double angle) {
        if (Double.compare(DX, 0) >= 0 && Double.compare(DY, 0) <= 0) {
            angle = 90.0 - angle;   // Upper Right Quadrant
        } else if (Double.compare(DX, 0) >= 0 && Double.compare(DY, 0) >= 0) {
            angle += 90.0;          // Lower Right Quadrant
        } else if (Double.compare(DX, 0) <= 0 && Double.compare(DY, 0) >= 0) {
            angle += 90.0;          // Lower Left Quadrant
        } else if (Double.compare(DX, 0) <= 0 && Double.compare(DY, 0) <= 0) {
            angle = 450.0 - angle;  // Upper Left Qudrant
        }

        /*
        if (DX >= 0 && DY <= 0) {
            angle = 90.0 - angle;   // Upper Right Quadrant
        } else if (DX >= 0 && DY >= 0) {
            angle += 90.0;          // Lower Right Quadrant
        } else if (DX <= 0 && DY >= 0) {
            angle += 90.0;          // Lower Left Quadrant
        } else if (DX <= 0 && DY <= 0) {
            angle = 450.0 - angle;  // Upper Left Qudrant
        }
        */
        return angle;
    }

    private List<Stop> calculate(final List<Stop> STOPS, final double OFFSET) {
        List<Stop>       stops = new ArrayList<>(STOPS.size());
        final BigDecimal STEP  = new BigDecimal(Double.MIN_VALUE);
        for (Stop stop : STOPS) {
            double     offset      = stop.getOffset();
            Color      color       = stop.getColor();
            BigDecimal newOffsetBD = new BigDecimal(offset + OFFSET).remainder(BigDecimal.ONE);
            if (newOffsetBD.equals(BigDecimal.ZERO)) {
                newOffsetBD = BigDecimal.ONE;
                stops.add(new Stop(Double.MIN_VALUE, color));
            } else if (Double.compare((offset + OFFSET), 1.0) > 0) {
                newOffsetBD = newOffsetBD.subtract(STEP);
            }
            stops.add(new Stop(newOffsetBD.doubleValue(), color));
        }

        HashMap<Double, Color> stopMap = new LinkedHashMap<>(stops.size());
        for (Stop stop : stops) { stopMap.put(stop.getOffset(), stop.getColor()); }

        List<Stop>        sortedStops     = new ArrayList<>(stops.size());
        SortedSet<Double> sortedFractions = new TreeSet<>(stopMap.keySet());
        if (sortedFractions.last() < 1) {
            stopMap.put(1.0, stopMap.get(sortedFractions.first()));
            sortedFractions.add(1.0);
        }
        if (sortedFractions.first() > 0) {
            stopMap.put(0.0, stopMap.get(sortedFractions.last()));
            sortedFractions.add(0.0);
        }
        for (double fraction : sortedFractions) { sortedStops.add(new Stop(fraction, stopMap.get(fraction))); }

        return sortedStops;
    }

    /*
    private List<Stop> normalizeStops(final Stop... STOPS) { return normalizeStops(0, Arrays.asList(STOPS)); }
    private List<Stop> normalizeStops(final double OFFSET, final Stop... STOPS) { return normalizeStops(OFFSET, Arrays.asList(STOPS)); }
    private List<Stop> normalizeStops(final List<Stop> STOPS) { return normalizeStops(0, STOPS); }
    */
    private List<Stop> normalizeStops(final double OFFSET, final List<Stop> STOPS) {
        double offset = Helper.clamp(0.0, 1.0, OFFSET);
        List<Stop> stops;
        if (null == STOPS || STOPS.isEmpty()) {
            stops = new ArrayList<>();
            stops.add(new Stop(0.0, Color.TRANSPARENT));
            stops.add(new Stop(1.0, Color.TRANSPARENT));
        } else {
            stops = STOPS;
        }
        List<Stop> sortedStops = calculate(stops, offset);
        return sortedStops;
    }
}
