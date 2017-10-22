/*
 * Copyright (c) 2016 by Gerrit Grunwald
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

import eu.hansolo.tilesfx.CountryPath;
import eu.hansolo.tilesfx.Section;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;


/**
 * Created by hansolo on 11.12.15.
 */
public class Helper {
    private static final double                         EPSILON                  = 1E-6;
    private static final String                         HIRES_COUNTRY_PROPERTIES = "eu/hansolo/tilesfx/highres.properties";
    private static       Properties                     hiresCountryProperties;
    private static       Map<String, List<CountryPath>> hiresCountryPaths        = new HashMap<>();


    public static final String[] TIME_0_TO_5       = {"1", "2", "3", "4", "5", "0"};
    public static final String[] TIME_5_TO_0       = {"5", "4", "3", "2", "1", "0"};
    public static final String[] TIME_0_TO_9       = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};
    public static final String[] TIME_9_TO_0       = {"9", "8", "7", "6", "5", "4", "3", "2", "1", "0"};
    public static final String[] TIME_0_TO_12      = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
    public static final String[] TIME_0_TO_24      = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12",
                                                      "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "00"};
    public static final String[] TIME_24_TO_0      = {"00", "23", "22", "21", "20", "19", "18", "17", "16", "15", "14", "13",
                                                      "12", "11", "10", "09", "08", "07", "06", "05", "04", "03", "02", "01"};
    public static final String[] TIME_00_TO_59     = {"00", "01", "02", "03", "04", "05", "06", "07", "08", "09",
                                                      "10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
                                                      "20", "21", "22", "23", "24", "25", "26", "27", "28", "29",
                                                      "30", "31", "32", "33", "34", "35", "36", "37", "38", "39",
                                                      "40", "41", "42", "43", "44", "45", "46", "47", "48", "49",
                                                      "50", "51", "52", "53", "54", "55", "56", "57", "58", "59"};
    public static final String[] TIME_59_TO_00     = {"59", "58", "57", "56", "55", "54", "53", "52", "51", "50",
                                                      "49", "48", "47", "46", "45", "44", "43", "42", "41", "40",
                                                      "39", "38", "37", "36", "35", "34", "33", "32", "31", "30",
                                                      "29", "28", "27", "26", "25", "24", "23", "22", "21", "20",
                                                      "19", "18", "17", "16", "15", "14", "13", "12", "11", "10",
                                                      "09", "08", "07", "06", "05", "04", "03", "02", "01", "00"};
    public static final String[] NUMERIC           = {" ", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};
    public static final String[] ALPHANUMERIC      = {" ", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0",
                                                      "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
                                                      "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
                                                      "W", "X", "Y", "Z"};
    public static final String[] ALPHA             = {" ", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
                                                      "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U",
                                                      "V", "W", "X", "Y", "Z"};
    public static final String[] EXTENDED          = {" ", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0",
                                                      "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
                                                      "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
                                                      "W", "X", "Y", "Z", "-", "/", ":", ",", "", ";", "@",
                                                      "#", "+", "?", "!", "%", "$", "=", "<", ">"};
    public static final String[] EXTENDED_UMLAUTE  = {" ", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0",
                                                      "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
                                                      "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
                                                      "W", "X", "Y", "Z", "-", "/", ":", ",", "", ";", "@",
                                                      "#", "+", "?", "!", "%", "$", "=", "<", ">", "Ä", "Ö", "Ü", "ß"};

    public static final <T extends Number> T clamp(final T MIN, final T MAX, final T VALUE) {
        if (VALUE.doubleValue() < MIN.doubleValue()) return MIN;
        if (VALUE.doubleValue() > MAX.doubleValue()) return MAX;
        return VALUE;
    }

    public static final int clamp(final int MIN, final int MAX, final int VALUE) {
        if (VALUE < MIN) return MIN;
        if (VALUE > MAX) return MAX;
        return VALUE;
    }
    public static final long clamp(final long MIN, final long MAX, final long VALUE) {
        if (VALUE < MIN) return MIN;
        if (VALUE > MAX) return MAX;
        return VALUE;
    }
    public static final double clamp(final double MIN, final double MAX, final double VALUE) {
        if (Double.compare(VALUE, MIN) < 0) return MIN;
        if (Double.compare(VALUE, MAX) > 0) return MAX;
        return VALUE;
    }

    public static final double[] calcAutoScale(final double MIN_VALUE, final double MAX_VALUE) {
        double maxNoOfMajorTicks = 10;
        double maxNoOfMinorTicks = 10;
        double niceMinValue;
        double niceMaxValue;
        double niceRange;
        double majorTickSpace;
        double minorTickSpace;
        niceRange      = (calcNiceNumber((MAX_VALUE - MIN_VALUE), false));
        majorTickSpace = calcNiceNumber(niceRange / (maxNoOfMajorTicks - 1), true);
        niceMinValue   = (Math.floor(MIN_VALUE / majorTickSpace) * majorTickSpace);
        niceMaxValue   = (Math.ceil(MAX_VALUE / majorTickSpace) * majorTickSpace);
        minorTickSpace = calcNiceNumber(majorTickSpace / (maxNoOfMinorTicks - 1), true);
        return new double[]{ niceMinValue, niceMaxValue, majorTickSpace, minorTickSpace };
    }

    /**
     * Returns a "niceScaling" number approximately equal to the range.
     * Rounds the number if ROUND == true.
     * Takes the ceiling if ROUND = false.
     *
     * @param RANGE the value range (maxValue - minValue)
     * @param ROUND whether to round the result or ceil
     * @return a "niceScaling" number to be used for the value range
     */
    public static final double calcNiceNumber(final double RANGE, final boolean ROUND) {
        double niceFraction;
        double exponent = Math.floor(Math.log10(RANGE));   // exponent of range
        double fraction = RANGE / Math.pow(10, exponent);  // fractional part of range

        if (ROUND) {
            if (Double.compare(fraction, 1.5) < 0) {
                niceFraction = 1;
            } else if (Double.compare(fraction, 3)  < 0) {
                niceFraction = 2;
            } else if (Double.compare(fraction, 7) < 0) {
                niceFraction = 5;
            } else {
                niceFraction = 10;
            }
        } else {
            if (Double.compare(fraction, 1) <= 0) {
                niceFraction = 1;
            } else if (Double.compare(fraction, 2) <= 0) {
                niceFraction = 2;
            } else if (Double.compare(fraction, 5) <= 0) {
                niceFraction = 5;
            } else {
                niceFraction = 10;
            }
        }
        return niceFraction * Math.pow(10, exponent);
    }

    public static final Color getColorOfSection(final List<Section> SECTIONS, final double VALUE, final Color DEFAULT_COLOR) {
        for (Section section : SECTIONS) {
            if (section.contains(VALUE)) return section.getColor();
        }
        return DEFAULT_COLOR;
    }

    public static final void adjustTextSize(final Text TEXT, final double MAX_WIDTH, double fontSize) {
        final String FONT_NAME = TEXT.getFont().getName();
        while (TEXT.getLayoutBounds().getWidth() > MAX_WIDTH && fontSize > 0) {
            fontSize -= 0.005;
            TEXT.setFont(new Font(FONT_NAME, fontSize));
        }
    }
    public static final void adjustTextSize(final Label TEXT, final double MAX_WIDTH, double fontSize) {
        final String FONT_NAME = TEXT.getFont().getName();
        while (TEXT.getLayoutBounds().getWidth() > MAX_WIDTH && fontSize > 0) {
            fontSize -= 0.005;
            TEXT.setFont(new Font(FONT_NAME, fontSize));
        }
    }

    public static final DateTimeFormatter getDateFormat(final Locale LOCALE) {
        if (Locale.US == LOCALE) {
            return DateTimeFormatter.ofPattern("MM/dd/YYYY");
        } else if (Locale.CHINA == LOCALE) {
            return DateTimeFormatter.ofPattern("YYYY.MM.dd");
        } else {
            return DateTimeFormatter.ofPattern("dd.MM.YYYY");
        }
    }
    public static final DateTimeFormatter getLocalizedDateFormat(final Locale LOCALE) {
        return DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(LOCALE);
    }

    public static final void enableNode(final Node NODE, final boolean ENABLE) {
        NODE.setManaged(ENABLE);
        NODE.setVisible(ENABLE);
    }

    public static final String colorToCss(final Color COLOR) {
        return COLOR.toString().replace("0x", "#");
    }

    public static final ThreadFactory getThreadFactory(final String THREAD_NAME, final boolean IS_DAEMON) {
        return runnable -> {
            Thread thread = new Thread(runnable, THREAD_NAME);
            thread.setDaemon(IS_DAEMON);
            return thread;
        };
    }

    public static final void stopTask(ScheduledFuture<?> task) {
        if (null == task) return;
        task.cancel(true);
        task = null;
    }

    public static final boolean isMonochrome(final Color COLOR) {
        return Double.compare(COLOR.getRed(), COLOR.getGreen()) == 0 && Double.compare(COLOR.getGreen(), COLOR.getBlue()) == 0;
    }

    public static final double colorDistance(final Color COLOR_1, final Color COLOR_2) {
        final double DELTA_R = (COLOR_2.getRed() - COLOR_1.getRed());
        final double DELTA_G = (COLOR_2.getGreen() - COLOR_1.getGreen());
        final double DELTA_B = (COLOR_2.getBlue() - COLOR_1.getBlue());

        return Math.sqrt(DELTA_R * DELTA_R + DELTA_G * DELTA_G + DELTA_B * DELTA_B);
    }

    public static final boolean isBright(final Color COLOR) { return !isDark(COLOR); }
    public static final boolean isDark(final Color COLOR) {
        final double DISTANCE_TO_WHITE = colorDistance(COLOR, Color.WHITE);
        final double DISTANCE_TO_BLACK = colorDistance(COLOR, Color.BLACK);
        return DISTANCE_TO_BLACK < DISTANCE_TO_WHITE;
    }

    public static final Color getTranslucentColorFrom(final Color COLOR, final double FACTOR) {
        return Color.color(COLOR.getRed(), COLOR.getGreen(), COLOR.getBlue(), Helper.clamp(0.0, 1.0, FACTOR));
    }

    public static final void scaleNodeTo(final Node NODE, final double TARGET_WIDTH, final double TARGET_HEIGHT) {
        NODE.setScaleX(TARGET_WIDTH / NODE.getLayoutBounds().getWidth());
        NODE.setScaleY(TARGET_HEIGHT / NODE.getLayoutBounds().getHeight());
    }

    public static final String normalize(final String TEXT) {
        String normalized = TEXT.replaceAll("\u00fc", "ue")
                                .replaceAll("\u00f6", "oe")
                                .replaceAll("\u00e4", "ae")
                                .replaceAll("\u00df", "ss");

        normalized = normalized.replaceAll("\u00dc(?=[a-z\u00fc\u00f6\u00e4\u00df ])", "Ue")
                               .replaceAll("\u00d6(?=[a-z\u00fc\u00f6\u00e4\u00df ])", "Oe")
                               .replaceAll("\u00c4(?=[a-z\u00fc\u00f6\u00e4\u00df ])", "Ae");

        normalized = normalized.replaceAll("\u00dc", "UE")
                               .replaceAll("\u00d6", "OE")
                               .replaceAll("\u00c4", "AE");
        return normalized;
    }

    public static final boolean equals(final double A, final double B) { return A == B || Math.abs(A - B) < EPSILON; }
    public static final boolean biggerThan(final double A, final double B) { return (A - B) > EPSILON; }
    public static final boolean lessThan(final double A, final double B) { return (B - A) > EPSILON; }

    public static Color getContrastColor(final Color COLOR) {
        return COLOR.getBrightness() > 0.5 ? Color.BLACK : Color.WHITE;
    }

    public static Color getColorWithOpacity(final Color COLOR, final double OPACITY) {
        return Color.color(COLOR.getRed(), COLOR.getGreen(), COLOR.getBlue(), OPACITY);
    }

    public static Map<String, List<CountryPath>> getHiresCountryPaths() {
        if (null == hiresCountryProperties) { hiresCountryProperties = readProperties(HIRES_COUNTRY_PROPERTIES); }
        //if (hiresCountryPaths.isEmpty()) {
            hiresCountryProperties.forEach((key, value) -> {
                String            name     = key.toString();
                List<CountryPath> pathList = new ArrayList<>();
                for (String path : value.toString().split(";")) { pathList.add(new CountryPath(name, path)); }
                hiresCountryPaths.put(name, pathList);
            });
        //}
        return hiresCountryPaths;
    }
    private static Properties readProperties(final String FILE_NAME) {
        final ClassLoader LOADER     = Thread.currentThread().getContextClassLoader();
        final Properties  PROPERTIES = new Properties();
        try(InputStream resourceStream = LOADER.getResourceAsStream(FILE_NAME)) {
            PROPERTIES.load(resourceStream);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return PROPERTIES;
    }

    public static void drawRoundedRect(final GraphicsContext CTX, final CtxBounds BOUNDS, final CtxCornerRadii RADII) {
        double x      = BOUNDS.getX();
        double y      = BOUNDS.getY();
        double width  = BOUNDS.getWidth();
        double height = BOUNDS.getHeight();

        CTX.beginPath();
        CTX.moveTo(x + RADII.getUpperRight(), y);
        CTX.lineTo(x + width - RADII.getUpperRight(), y);
        CTX.quadraticCurveTo(x + width, y, x + width, y + RADII.getUpperRight());
        CTX.lineTo(x + width, y + height - RADII.getLowerRight());
        CTX.quadraticCurveTo(x + width, y + height, x + width - RADII.getLowerRight(), y + height);
        CTX.lineTo(x + RADII.getLowerLeft(), y + height);
        CTX.quadraticCurveTo(x, y + height, x, y + height - RADII.getLowerLeft());
        CTX.lineTo(x, y + RADII.getUpperRight());
        CTX.quadraticCurveTo(x, y, x + RADII.getUpperRight(), y);
        CTX.closePath();
    }

    // Smooth given path defined by it's list of path elements
    public static Path smoothPath(final ObservableList<PathElement> ELEMENTS, final boolean FILLED) {
        if (ELEMENTS.isEmpty()) { return new Path(); }
        final Point2D[] dataPoints = new Point2D[ELEMENTS.size()];
        for (int i = 0; i < ELEMENTS.size(); i++) {
            final PathElement element = ELEMENTS.get(i);
            if (element instanceof MoveTo) {
                MoveTo move   = (MoveTo) element;
                dataPoints[i] = new Point2D(move.getX(), move.getY());
            } else if (element instanceof LineTo) {
                LineTo line   = (LineTo) element;
                dataPoints[i] = new Point2D(line.getX(), line.getY());
            }
        }
        double                     zeroY               = ((MoveTo) ELEMENTS.get(0)).getY();
        List<PathElement>          smoothedElements    = new ArrayList<>();
        Pair<Point2D[], Point2D[]> result              = calcCurveControlPoints(dataPoints);
        Point2D[]                  firstControlPoints  = result.getKey();
        Point2D[]                  secondControlPoints = result.getValue();
        // Start path dependent on filled or not
        if (FILLED) {
            smoothedElements.add(new MoveTo(dataPoints[0].getX(), zeroY));
            smoothedElements.add(new LineTo(dataPoints[0].getX(), dataPoints[0].getY()));
        } else {
            smoothedElements.add(new MoveTo(dataPoints[0].getX(), dataPoints[0].getY()));
        }
        // Add curves
        for (int i = 2; i < dataPoints.length; i++) {
            final int ci = i - 1;
            smoothedElements.add(new CubicCurveTo(
                firstControlPoints[ci].getX(), firstControlPoints[ci].getY(),
                secondControlPoints[ci].getX(), secondControlPoints[ci].getY(),
                dataPoints[i].getX(), dataPoints[i].getY()));
        }
        // Close the path if filled
        if (FILLED) {
            smoothedElements.add(new LineTo(dataPoints[dataPoints.length - 1].getX(), zeroY));
            smoothedElements.add(new ClosePath());
        }
        return new Path(smoothedElements);
    }
    private static Pair<Point2D[], Point2D[]> calcCurveControlPoints(Point2D[] dataPoints) {
        Point2D[] firstControlPoints;
        Point2D[] secondControlPoints;
        int n = dataPoints.length - 1;
        if (n == 1) { // Special case: Bezier curve should be a straight line.
            firstControlPoints     = new Point2D[1];
            // 3P1 = 2P0 + P3
            firstControlPoints[0]  = new Point2D((2 * dataPoints[0].getX() + dataPoints[1].getX()) / 3, (2 * dataPoints[0].getY() + dataPoints[1].getY()) / 3);
            secondControlPoints    = new Point2D[1];
            // P2 = 2P1 – P0
            secondControlPoints[0] = new Point2D(2 * firstControlPoints[0].getX() - dataPoints[0].getX(), 2 * firstControlPoints[0].getY() - dataPoints[0].getY());
            return new Pair<>(firstControlPoints, secondControlPoints);
        }

        // Calculate first Bezier control points
        // Right hand side vector
        double[] rhs = new double[n];

        // Set right hand side X values
        for (int i = 1; i < n - 1; ++i) {
            rhs[i] = 4 * dataPoints[i].getX() + 2 * dataPoints[i + 1].getX();
        }
        rhs[0]     = dataPoints[0].getX() + 2 * dataPoints[1].getX();
        rhs[n - 1] = (8 * dataPoints[n - 1].getX() + dataPoints[n].getX()) / 2.0;
        // Get first control points X-values
        double[] x = getFirstControlPoints(rhs);

        // Set right hand side Y values
        for (int i = 1; i < n - 1; ++i) {
            rhs[i] = 4 * dataPoints[i].getY() + 2 * dataPoints[i + 1].getY();
        }
        rhs[0]     = dataPoints[0].getY() + 2 * dataPoints[1].getY();
        rhs[n - 1] = (8 * dataPoints[n - 1].getY() + dataPoints[n].getY()) / 2.0;
        // Get first control points Y-values
        double[] y = getFirstControlPoints(rhs);

        // Fill output arrays.
        firstControlPoints  = new Point2D[n];
        secondControlPoints = new Point2D[n];
        for (int i = 0; i < n; ++i) {
            // First control point
            firstControlPoints[i] = new Point2D(x[i], y[i]);
            // Second control point
            if (i < n - 1) {
                secondControlPoints[i] = new Point2D(2 * dataPoints[i + 1].getX() - x[i + 1], 2 * dataPoints[i + 1].getY() - y[i + 1]);
            } else {
                secondControlPoints[i] = new Point2D((dataPoints[n].getX() + x[n - 1]) / 2, (dataPoints[n].getY() + y[n - 1]) / 2);
            }
        }
        return new Pair<>(firstControlPoints, secondControlPoints);
    }
    private static double[] getFirstControlPoints(double[] rhs) {
        int      n   = rhs.length;
        double[] x   = new double[n]; // Solution vector.
        double[] tmp = new double[n]; // Temp workspace.
        double   b   = 2.0;

        x[0] = rhs[0] / b;

        for (int i = 1; i < n; i++) {// Decomposition and forward substitution.
            tmp[i] = 1 / b;
            b      = (i < n - 1 ? 4.0 : 3.5) - tmp[i];
            x[i]   = (rhs[i] - x[i - 1]) / b;
        }
        for (int i = 1; i < n; i++) {
            x[n - i - 1] -= tmp[n - i] * x[n - i]; // Backsubstitution.
        }
        return x;
    }


    public static boolean isInRectangle(final double X, final double Y,
                                        final double MIN_X, final double MIN_Y,
                                        final double MAX_X, final double MAX_Y) {
        return (Double.compare(X, MIN_X) >= 0 &&
                Double.compare(X, MAX_X) <= 0 &&
                Double.compare(Y, MIN_Y) >= 0 &&
                Double.compare(Y, MAX_Y) <= 0);
    }

    public static boolean isInEllipse(final double X, final double Y,
                                      final double ELLIPSE_CENTER_X, final double ELLIPSE_CENTER_Y,
                                      final double ELLIPSE_RADIUS_X, final double ELLIPSE_RADIUS_Y) {
        return Double.compare(((((X - ELLIPSE_CENTER_X) * (X - ELLIPSE_CENTER_X)) / (ELLIPSE_RADIUS_X * ELLIPSE_RADIUS_X)) +
                               (((Y - ELLIPSE_CENTER_Y) * (Y - ELLIPSE_CENTER_Y)) / (ELLIPSE_RADIUS_Y * ELLIPSE_RADIUS_Y))), 1) <= 0.0;
    }

    public static boolean isInPolygon(final double X, final double Y, final Polygon POLYGON) {
        List<Double> points              = POLYGON.getPoints();
        int          noOfPointsInPolygon = POLYGON.getPoints().size() / 2;
        double[]     pointsX             = new double[noOfPointsInPolygon];
        double[]     pointsY             = new double[noOfPointsInPolygon];
        int          pointCounter        = 0;
        for (int i = 0 ; i < points.size() ; i++) {
            if (i % 2 == 0) {
                pointsX[i] = points.get(pointCounter);
            } else {
                pointsY[i] = points.get(pointCounter);
                pointCounter++;
            }
        }
        return isInPolygon(X, Y, noOfPointsInPolygon, pointsX, pointsY);
    }
    public static boolean isInPolygon(final double X, final double Y, final int NO_OF_POINTS_IN_POLYGON, final double[] POINTS_X, final double[] POINTS_Y) {
        if (NO_OF_POINTS_IN_POLYGON != POINTS_X.length || NO_OF_POINTS_IN_POLYGON != POINTS_Y.length) { return false; }
        boolean inside = false;
        for (int i = 0, j = NO_OF_POINTS_IN_POLYGON - 1; i < NO_OF_POINTS_IN_POLYGON ; j = i++) {
            if (((POINTS_Y[i] > Y) != (POINTS_Y[j] > Y)) && (X < (POINTS_X[j] - POINTS_X[i]) * (Y - POINTS_Y[i]) / (POINTS_Y[j] - POINTS_Y[i]) + POINTS_X[i])) {
                inside = !inside;
            }
        }
        return inside;
    }

    public static boolean isInRingSegment(final double X, final double Y,
                                    final double CENTER_X, final double CENTER_Y,
                                    final double OUTER_RADIUS, final double INNER_RADIUS,
                                    final double START_ANGLE, final double SEGMENT_ANGLE) {
        double angleOffset = 90.0;
        double pointRadius = Math.sqrt((X - CENTER_X) * (X - CENTER_X) + (Y - CENTER_Y) * (Y - CENTER_Y));
        double pointAngle  = getAngleFromXY(X, Y, CENTER_X, CENTER_Y, angleOffset);
        double startAngle  = angleOffset - START_ANGLE;
        double endAngle    = startAngle + SEGMENT_ANGLE;

        return (Double.compare(pointRadius, INNER_RADIUS) >= 0 &&
                Double.compare(pointRadius, OUTER_RADIUS) <= 0 &&
                Double.compare(pointAngle, startAngle) >= 0 &&
                Double.compare(pointAngle, endAngle) <= 0);
    }

    public static double getAngleFromXY(final double X, final double Y, final double CENTER_X, final double CENTER_Y) {
        return getAngleFromXY(X, Y, CENTER_X, CENTER_Y, 90.0);
    }
    public static double getAngleFromXY(final double X, final double Y, final double CENTER_X, final double CENTER_Y, final double ANGLE_OFFSET) {
        // For ANGLE_OFFSET =  0 -> Angle of 0 is at 3 o'clock
        // For ANGLE_OFFSET = 90  ->Angle of 0 is at 12 o'clock
        double deltaX      = X - CENTER_X;
        double deltaY      = Y - CENTER_Y;
        double radius      = Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));
        double nx          = deltaX / radius;
        double ny          = deltaY / radius;
        double theta       = Math.atan2(ny, nx);
        theta              = Double.compare(theta, 0.0) >= 0 ? Math.toDegrees(theta) : Math.toDegrees((theta)) + 360.0;
        double angle       = (theta + ANGLE_OFFSET) % 360;
        return angle;
    }
}
