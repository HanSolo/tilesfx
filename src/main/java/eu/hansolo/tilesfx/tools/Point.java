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

public class Point {
    private double x, y;


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

    @Override public String toString() {
        return new StringBuilder().append("{\n")
                                  .append("  \"x\" : ").append(x).append(",\n")
                                  .append("  \"y\" : ").append(y).append("\n")
                                  .append("}")
                                  .toString();
    }
}
