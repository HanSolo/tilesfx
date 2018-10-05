/*
 * Copyright (c) 2017 by Gerrit Grunwald
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

package eu.hansolo.tilesfx.tools;

import java.util.List;


public class Point implements Comparable<Point> {
    public double x;
    public double y;


    // ******************** Constructors **************************************
    public Point() {
        this(-1, -1);
    }
    public Point(final Point P) {
        this(P.getX(), P.getY());
    }
    public Point(final double X, final double Y) {
        x = X;
        y = Y;
    }


    // ******************** Methods *******************************************
    public double getX() { return x; }
    public void setX(final double X) { x = X; }

    public double getY() { return y; }
    public void setY(final double Y) { y = Y; }

    public void set(final Point P) {
      x = P.x;
      y = P.y;
    }
    public void set(final double X, final double Y) {
        x = X;
        y = Y;
    }

    public double distanceTo(final Point P) { return distance(P.getX(), P.getY(), x, y); }
    public double distanceTo(final double X, final double Y) { return distance(X, Y, x, y); }

    public double distanceSquareTo(final Point P) { return distanceSquare(P.getX(), P.getY(), x, y); }

    public Point add(final Point P) { return add(P.getX(), P.getY()); }
    public Point add(final double X, final double Y) { return new Point(getX() + X, getY() + Y); }

    public Point subtract(Point P) { return subtract(P.getX(), P.getY()); }
    public Point subtract(final double X, final double Y) { return new Point(getX() - X, getY() - Y); }

    public Point multiply(final double FACTOR) { return new Point(getX() * FACTOR, getY() * FACTOR); }

    public Point normalize() {
        final double mag = magnitude();
        if (mag == 0.0) { return new Point(0.0, 0.0); }
        return new Point(x / mag, y / mag);
    }

    public Point midpoint(final Point P) { return midpoint(P.getX(), P.getY()); }
    public Point midpoint(final double X, final double Y) { return new Point(X + (getX() - X) / 2.0, Y + (getY() - Y) / 2.0); }

    public double angle(final Point P) { return angle(P.getX(), P.getY()); }
    public double angle(final double X, final double Y) {
        final double ax = getX();
        final double ay = getY();

        final double delta = (ax * X + ay * Y) / Math.sqrt((ax * ax + ay * ay) * (X * X + Y * Y));

        if (delta > 1.0)  { return 0.0; }
        if (delta < -1.0) { return 180.0; }

        return Math.toDegrees(Math.acos(delta));
    }
    public double angle(final Point P1, final Point P2) {
        final double x = getX();
        final double y = getY();

        final double ax = P1.getX() - x;
        final double ay = P1.getY() - y;
        final double bx = P2.getX() - x;
        final double by = P2.getY() - y;

        final double delta = (ax * bx + ay * by) / Math.sqrt((ax * ax + ay * ay) * (bx * bx + by * by));

        if (delta > 1.0) { return 0.0; }
        if (delta < -1.0) { return 180.0; }

        return Math.toDegrees(Math.acos(delta));
    }

    public double magnitude() {
        final double x = getX();
        final double y = getY();
        return Math.sqrt(x * x + y * y);
    }

    public double dotProduct(final Point VECTOR) {
        return dotProduct(VECTOR.getX(), VECTOR.getY());
    }
    public double dotProduct(final double X, final double Y) { return getX() * X + getY() * Y; }

    public static double distance(final Point P1, final Point P2) { return distance(P1.getX(), P1.getY(), P2.getX(), P2.getY()); }
    public static double distance(final double X1, final double Y1, final double X2, final double Y2) {
        double deltaX = (X2 - X1);
        double deltaY = (Y2 - Y1);
        return Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));
    }

    public static double distanceSquare(final Point P1, final Point P2) { return distanceSquare(P1.getX(), P1.getY(), P2.getX(), P2.getY()); }
    public static double distanceSquare(final double X1, final double Y1, final double X2, final double Y2) {
        double deltaX = (X2 - X1);
        double deltaY = (Y2 - Y1);
        return (deltaX * deltaX) + (deltaY * deltaY);
    }

    public static Point nearestWithinRadius(final Point P, final List<Point> POINTS, final double RADIUS) {
        final double radiusSquare = RADIUS * RADIUS;
        Point p = POINTS.get(0);
        for (int i = 0; i < POINTS.size(); i++) {
            double distanceSquare = POINTS.get(i).distanceSquareTo(P);
            if (distanceSquare < radiusSquare && distanceSquare < p.distanceSquareTo(P)) {
                p = POINTS.get(i);
            }
        }
        return p;
    }

    public static Point nearest(final Point P, final List<Point> POINTS) {
        Point p = POINTS.get(0);
        for (int i = 0; i < POINTS.size(); i++) {
            if (POINTS.get(i).distanceSquareTo(P) < p.distanceSquareTo(P)) {
                p = POINTS.get(i);
            }
        }
        return p;
    }

    public int compareTo(final Point P) {
        return x != P.getX() ? Double.compare(x, P.x) : Double.compare(y, P.y);
    }

    @Override public int hashCode() {
        int tmp = (int) (y + ((x + 1) / 2));
        return Math.abs((int) (x + (tmp * tmp)));
    }

    @Override public boolean equals(final Object OBJ) {
        if (OBJ == this) { return true; }
        if (OBJ instanceof Point) {
            Point p = (Point) OBJ;
            return (Double.compare(x, p.getX()) == 0 && Double.compare(y, p.getY()) == 0);
        }
        return false;
    }

    @Override public String toString() {
        return new StringBuilder().append("{ ")
                                  .append("\"x\": ").append(x).append(", ")
                                  .append("\"y\": ").append(y)
                                  .append(" }").toString();
    }
}

