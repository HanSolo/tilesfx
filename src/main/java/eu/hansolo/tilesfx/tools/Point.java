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

public class Point implements Comparable<Point> {
    private double x;
    private double y;


    // ******************** Constructors **************************************
    public Point() {
        this(0.0, 0.0);
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

    public void set(final double X, final double Y) {
        x = X;
        y = Y;
    }

    public double distanceTo(final Point P) { return distance(P.getX(), P.getY(), x, y); }
    public double distanceTo(final double X, final double Y) { return distance(X, Y, x, y); }

    public static double distance(final Point P1, final Point P2) { return distance(P1.getX(), P1.getY(), P2.getX(), P2.getY()); }
    public static double distance(final double X1, final double Y1, final double X2, final double Y2) {
        double deltaX = (X2 - X1);
        double deltaY = (Y2 - Y1);
        return Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));
    }

    public int compareTo(final Point POINT) {
        return x != POINT.getX() ? Double.compare(x, POINT.x) : Double.compare(y, POINT.y);
    }

    @Override public String toString() {
        return new StringBuilder().append("x: ").append(x).append(", y: ").append(y).toString();
    }
}

