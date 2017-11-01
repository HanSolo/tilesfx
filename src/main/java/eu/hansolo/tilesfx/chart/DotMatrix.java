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

package eu.hansolo.tilesfx.chart;

import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.events.DotMatrixEvent;
import eu.hansolo.tilesfx.events.DotMatrixEventListener;
import eu.hansolo.tilesfx.tools.CtxBounds;
import eu.hansolo.tilesfx.tools.CtxCornerRadii;
import eu.hansolo.tilesfx.tools.Helper;
import javafx.beans.DefaultProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.util.concurrent.CopyOnWriteArrayList;


/**
 * User: hansolo
 * Date: 19.03.17
 * Time: 04:39
 */
@DefaultProperty("children")
public class DotMatrix extends Region {
    public  enum DotShape { ROUND, SQUARE, ROUNDED_RECT }
    public  static final double                                       DEFAULT_SPACER_SIZE_FACTOR = 0.05;
    private static final int                                          RED_MASK                   = 255 << 16;
    private static final int                                          GREEN_MASK                 = 255 << 8;
    private static final int                                          BLUE_MASK                  = 255;
    private static final int                                          ALPHA_MASK                 = 255 << 24;
    private static final double                                       ALPHA_FACTOR               = 1.0 / 255.0;
    private              double                                       preferredWidth;
    private              double                                       preferredHeight;
    private              double                                       width;
    private              double                                       height;
    private              Canvas                                       canvas;
    private              GraphicsContext                              ctx;
    private              StackPane                                    pane;
    private              int                                          dotOnColor;
    private              int                                          dotOffColor;
    private              DotShape                                     dotShape;
    private              int                                          cols;
    private              int                                          rows;
    private              int[][]                                      matrix;
    private              MatrixFont                                   matrixFont;
    private              int                                          characterWidth;
    private              int                                          characterHeight;
    private              int                                          characterWidthMinusOne;
    private              double                                       dotSize;
    private              double                                       spacer;
    private              boolean                                      useSpacer;
    private              double                                       spacerSizeFactor;
    private              double                                       dotSizeMinusDoubleSpacer;
    private              CopyOnWriteArrayList<DotMatrixEventListener> listeners;


    // ******************** Constructors **************************************
    public DotMatrix() {
        this(250, 250, 32, 32, Tile.BLUE, Tile.BACKGROUND.brighter(), DotShape.ROUND, MatrixFont8x8.INSTANCE);
    }
    public DotMatrix(final int COLS, final int ROWS) {
        this(250, 250, COLS, ROWS, Tile.BLUE, Tile.BACKGROUND.brighter(), DotShape.ROUND, MatrixFont8x8.INSTANCE);
    }
    public DotMatrix(final int COLS, final int ROWS, final Color DOT_ON_COLOR) {
        this(250, 250, COLS, ROWS, DOT_ON_COLOR, Tile.BACKGROUND.brighter(), DotShape.ROUND, MatrixFont8x8.INSTANCE);
    }
    public DotMatrix(final double PREFERRED_WIDTH, final double PREFERRED_HEIGHT, final int COLS, final int ROWS, final Color DOT_ON_COLOR, final Color DOT_OFF_COLOR, final DotShape DOT_SHAPE, final MatrixFont FONT) {
        preferredWidth         = PREFERRED_WIDTH;
        preferredHeight        = PREFERRED_HEIGHT;
        dotOnColor             = convertToInt(DOT_ON_COLOR);
        dotOffColor            = convertToInt(DOT_OFF_COLOR);
        dotShape               = DOT_SHAPE;
        cols                   = COLS;
        rows                   = ROWS;
        matrix                 = new int[cols][rows];
        matrixFont             = FONT;
        characterWidth         = matrixFont.getCharacterWidth();
        characterHeight        = matrixFont.getCharacterHeight();
        characterWidthMinusOne = characterWidth - 1;
        useSpacer              = true;
        spacerSizeFactor       = DEFAULT_SPACER_SIZE_FACTOR;
        listeners              = new CopyOnWriteArrayList<>();
        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void initGraphics() {
        // prefill matrix with dotOffColor
        for (int y = 0 ; y < rows ; y++) {
            for (int x = 0 ; x < cols ; x++) {
                matrix[x][y] = dotOffColor;
            }
        }

        if (Double.compare(getPrefWidth(), 0.0) <= 0 || Double.compare(getPrefHeight(), 0.0) <= 0 ||
            Double.compare(getWidth(), 0.0) <= 0 || Double.compare(getHeight(), 0.0) <= 0) {
            if (getPrefWidth() > 0 && getPrefHeight() > 0) {
                setPrefSize(getPrefWidth(), getPrefHeight());
            } else {
                setPrefSize(preferredWidth, preferredHeight);
            }
        }

        canvas = new Canvas(preferredWidth, preferredHeight);
        ctx = canvas.getGraphicsContext2D();

        pane = new StackPane(canvas);

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        widthProperty().addListener(o -> resize());
        heightProperty().addListener(o -> resize());
        canvas.setOnMousePressed(e -> checkForClick(e));
    }


    // ******************** Methods *******************************************
    @Override public ObservableList<Node> getChildren() { return super.getChildren(); }

    public void setColsAndRows(final int COLS, final int ROWS) {
        cols   = COLS;
        rows   = ROWS;
        matrix = new int[cols][rows];
        initGraphics();
        resize();
    }

    public Color getDotOnColor() { return convertToColor(dotOnColor); }
    public void setDotOnColor(final Color COLOR) {
        dotOnColor = convertToInt(COLOR);
        drawMatrix();
    }

    public Color getDotOffColor() { return convertToColor(dotOffColor); }
    public void setDotOffColor(final Color COLOR) {
        dotOffColor = convertToInt(COLOR);
        for (int y = 0 ; y < rows ; y++) {
            for (int x = 0 ; x < cols ; x++) {
                matrix[x][y] = dotOffColor;
            }
        }
        drawMatrix();
    }

    public DotShape getDotShape() { return dotShape; }
    public void setDotShape(final DotShape SHAPE) {
        dotShape = SHAPE;
        drawMatrix();
    }

    public MatrixFont getMatrixFont() { return matrixFont; }
    public void setMatrixFont(final MatrixFont FONT) {
        matrixFont             = FONT;
        characterWidth         = matrixFont.getCharacterWidth();
        characterHeight        = matrixFont.getCharacterHeight();
        characterWidthMinusOne = characterWidth - 1;
        drawMatrix();
    }

    public boolean isUsingSpacer() { return useSpacer; }
    public void setUseSpacer(final boolean USE) {
        useSpacer = USE;
        spacer                   = useSpacer ? dotSize * getSpacerSizeFactor() : 0;
        dotSizeMinusDoubleSpacer = dotSize - spacer * 2;
        drawMatrix();
    }

    public double getSpacerSizeFactor() { return spacerSizeFactor; }
    public void setSpacerSizeFactor(final double FACTOR) {
        spacerSizeFactor         = Helper.clamp(0.0, 0.2, FACTOR);
        spacer                   = useSpacer ? dotSize * spacerSizeFactor : 0;
        dotSizeMinusDoubleSpacer = dotSize - spacer * 2;
        drawMatrix();
    }

    public void setPixel(final int X, final int Y, final boolean VALUE) { setPixel(X, Y, VALUE ? dotOnColor : dotOffColor); }
    public void setPixel(final int X, final int Y, final Color COLOR) { setPixel(X, Y, convertToInt(COLOR)); }
    public void setPixel(final int X, final int Y, final int COLOR_VALUE) {
        if (X >= cols || X < 0) return;
        if (Y >= rows || Y < 0) return;
        matrix[X][Y] = COLOR_VALUE;
    }

    public void setPixelWithRedraw(final int X, final int Y, final boolean ON) {
        setPixel(X, Y, ON ? dotOnColor : dotOffColor);
        drawMatrix();
    }
    public void setPixelWithRedraw(final int X, final int Y, final int COLOR_VALUE) {
        setPixel(X, Y, COLOR_VALUE);
        drawMatrix();
    }

    public void setCharAt(final char CHAR, final int X, final int Y) {
        setCharAt(CHAR, X, Y, dotOnColor);
    }
    public void setCharAt(final char CHAR, final int X, final int Y, final int COLOR_VALUE) {
        int[] c = matrixFont.getCharacter(CHAR);
        for (int x = 0; x < characterWidth; x++) {
            for (int y = 0; y < characterHeight; y++) {
                setPixel(x + X, y + Y, getBitAt(characterWidthMinusOne - x, y, c) == 0 ? dotOffColor : COLOR_VALUE);
            }
        }
        drawMatrix();
    }

    public void setCharAtWithBackground(final char CHAR, final int X, final int Y) {
        setCharAtWithBackground(CHAR, X, Y, dotOnColor);
    }
    public void setCharAtWithBackground(final char CHAR, final int X, final int Y, final int COLOR_VALUE) {
        int[] c = matrixFont.getCharacter(CHAR);
        for (int x = 0; x < characterWidth; x++) {
            for (int y = 0; y < characterHeight; y++) {
                if (getBitAt(characterWidthMinusOne - x, y, c) == 0) continue;
                setPixel(x + X, y + Y, COLOR_VALUE);
            }
        }
        drawMatrix();
    }

    public double getDotSize() { return dotSize; }

    public double getMatrixWidth() { return canvas.getWidth(); }
    public double getMatrixHeight() { return canvas.getHeight(); }

    public Bounds getMatrixLayoutBounds() { return canvas.getLayoutBounds(); }
    public Bounds getMatrixBoundsInParent() { return canvas.getBoundsInParent(); }
    public Bounds getMatrixBoundsInLocal() { return canvas.getBoundsInLocal(); }

    public int getCols() { return cols; }
    public int getRows() { return rows; }

    public int[][] getMatrix() { return matrix; }

    public static Color convertToColor(final int COLOR_VALUE) {
        return Color.rgb((COLOR_VALUE & RED_MASK) >> 16, (COLOR_VALUE & GREEN_MASK) >> 8, (COLOR_VALUE & BLUE_MASK), ALPHA_FACTOR * ((COLOR_VALUE & ALPHA_MASK) >>> 24));
    }

    public static int convertToInt(final Color COLOR) {
        return convertToInt((float) COLOR.getRed(), (float) COLOR.getGreen(), (float) COLOR.getBlue(), (float) COLOR.getOpacity());
    }
    public static int convertToInt(final float RED, final float GREEN, final float BLUE, final float ALPHA) {
        int red   = Math.round(255 * RED);
        int green = Math.round(255 * GREEN);
        int blue  = Math.round(255 * BLUE);
        int alpha = Math.round(255 * ALPHA);
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    public static int getBitAt(final int X, final int Y, final int[] BYTE_ARRAY) { return (BYTE_ARRAY[Y] >> X) & 1; }
    public static boolean getBitAtBoolean(final int X, final int Y, final int[] BYTE_ARRAY) { return ((BYTE_ARRAY[Y] >> X) & 1) == 1; }

    public int getColorValueAt(final int X, final int Y) { return matrix[X][Y]; }

    public Color getColorAt(final int X, final int Y) { return convertToColor(matrix[X][Y]); }

    public void shiftLeft() {
        int[] firstColumn = new int[rows];
        for (int y = 0 ; y < rows ; y++) { firstColumn[y] = matrix[0][y]; }
        for (int y = 0 ; y < rows ; y++) {
            for (int x = 1 ; x < cols ; x++) {
                matrix[x - 1][y] = matrix[x][y];
            }
        }
        for (int y = 0 ; y < rows ; y++) { matrix[cols - 1][y] = firstColumn[y]; }
        drawMatrix();
    }
    public void shiftRight() {
        int[] lastColumn = new int[rows];
        for (int y = 0 ; y < rows ; y++) { lastColumn[y] = matrix[cols - 1][y]; }
        for (int y = 0 ; y < rows ; y++) {
            for (int x = cols - 2 ; x >= 0 ; x--) {
                matrix[x + 1][y] = matrix[x][y];
            }
        }
        for (int y = 0 ; y < rows ; y++) { matrix[0][y] = lastColumn[y]; }
        drawMatrix();
    }

    public void shiftUp() {
        int[] firstRow = new int[cols];
        for (int x = 0 ; x < cols ; x++) { firstRow[x] = matrix[x][0]; }
        for (int y = 1 ; y < rows ; y++) {
            for (int x = 0 ; x < cols ; x++) {
                matrix[x][y - 1] = matrix[x][y];
            }
        }
        for (int x = 0 ; x < cols ; x++) { matrix[x][rows - 1] = firstRow[x]; }
        drawMatrix();
    }
    public void shiftDown() {
        int[] lastRow = new int[cols];
        for (int x = 0 ; x < cols ; x++) { lastRow[x] = matrix[x][rows - 1]; }
        for (int y = rows - 2 ; y >= 0 ; y--) {
            for (int x = 0 ; x < cols ; x++) {
                matrix[x][y + 1] = matrix[x][y];
            }
        }
        for (int x = 0 ; x < cols ; x++) { matrix[x][0] = lastRow[x]; }
        drawMatrix();
    }

    public void setAllDotsOn() {
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                setPixel(x, y, true);
            }
        }
        drawMatrix();
    }
    public void setAllDotsOff() {
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                setPixel(x, y, false);
            }
        }
        drawMatrix();
    }

    public void drawMatrix() {
        ctx.clearRect(0, 0, width, height);
        switch(dotShape) {
            case ROUNDED_RECT:
                CtxBounds      bounds      = new CtxBounds(dotSizeMinusDoubleSpacer, dotSizeMinusDoubleSpacer);
                CtxCornerRadii cornerRadii = new CtxCornerRadii(dotSize * 0.125);
                for (int y = 0; y < rows; y++) {
                    for (int x = 0; x < cols; x++) {
                        ctx.setFill(convertToColor(matrix[x][y]));
                        bounds.setX(x * dotSize + spacer);
                        bounds.setY(y * dotSize + spacer);
                        Helper.drawRoundedRect(ctx, bounds, cornerRadii);
                        ctx.fill();
                    }
                }
                break;
            case SQUARE:
                for (int y = 0; y < rows; y++) {
                    for (int x = 0; x < cols; x++) {
                        ctx.setFill(convertToColor(matrix[x][y]));
                        ctx.fillRect(x * dotSize + spacer, y * dotSize + spacer, dotSizeMinusDoubleSpacer, dotSizeMinusDoubleSpacer);
                    }
                }
                break;
            case ROUND:
            default   :
                for (int y = 0; y < rows; y++) {
                    for (int x = 0; x < cols; x++) {
                        ctx.setFill(convertToColor(matrix[x][y]));
                        ctx.fillOval(x * dotSize + spacer, y * dotSize + spacer, dotSizeMinusDoubleSpacer, dotSizeMinusDoubleSpacer);
                    }
                }
                break;
        }
    }

    public void setOnDotMatrixEvent(final DotMatrixEventListener LISTENER) { addDotMatrixEventListener(LISTENER); }
    public void addDotMatrixEventListener(final DotMatrixEventListener LISTENER) { if (!listeners.contains(LISTENER)) listeners.add(LISTENER); }
    public void removeDotMatrixEventListener(final DotMatrixEventListener LISTENER) { if (listeners.contains(LISTENER)) listeners.remove(LISTENER); }

    public void fireDotMatrixEvent(final DotMatrixEvent EVENT) {
        for (DotMatrixEventListener listener : listeners) { listener.onDotMatrixEvent(EVENT); }
    }

    @Override protected double computePrefWidth(final double HEIGHT) { return super.computePrefWidth(HEIGHT); }
    @Override protected double computePrefHeight(final double WIDTH) { return super.computePrefHeight(WIDTH); }

    private long getRed(final long COLOR_VALUE) { return  (COLOR_VALUE & RED_MASK) >> 16; }
    private long getGreen(final long COLOR_VALUE) { return  (COLOR_VALUE & GREEN_MASK) >> 8; }
    private long getBlue(final long COLOR_VALUE) { return (COLOR_VALUE & BLUE_MASK); }
    private long getAlpha(final long COLOR_VALUE) { return (COLOR_VALUE & ALPHA_MASK) >>> 24; }

    private void checkForClick(final MouseEvent EVT) {
        double spacerPlusDotSizeMinusDoubleSpacer = spacer + dotSizeMinusDoubleSpacer;
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                if (isInRectangle(EVT.getX(), EVT.getY(), x * dotSize + spacer, y * dotSize + spacer, x * dotSize + spacerPlusDotSizeMinusDoubleSpacer, y * dotSize + spacerPlusDotSizeMinusDoubleSpacer)) {
                    fireDotMatrixEvent(new DotMatrixEvent(x, y, EVT.getScreenX(), EVT.getScreenY()));
                    break;
                }
            }
        }
    }

    private static boolean isInRectangle(final double X, final double Y,
                                         final double MIN_X, final double MIN_Y,
                                         final double MAX_X, final double MAX_Y) {
        return (Double.compare(X, MIN_X) >= 0 &&
                Double.compare(X, MAX_X) <= 0 &&
                Double.compare(Y, MIN_Y) >= 0 &&
                Double.compare(Y, MAX_Y) <= 0);
    }


    // ******************** Resizing ******************************************
    private void resize() {
        width                    = getWidth() - getInsets().getLeft() - getInsets().getRight();
        height                   = getHeight() - getInsets().getTop() - getInsets().getBottom();
        dotSize                  = (width / cols) < (height / rows) ? (width / cols) : (height / rows);
        spacer                   = useSpacer ? dotSize * getSpacerSizeFactor() : 0;
        dotSizeMinusDoubleSpacer = dotSize - spacer * 2;

        if (width > 0 && height > 0) {
            pane.setMaxSize(width, height);
            pane.setPrefSize(width, height);
            pane.relocate((getWidth() - width) * 0.5, (getHeight() - height) * 0.5);

            canvas.setWidth(cols * dotSize);
            canvas.setHeight(rows * dotSize);

            drawMatrix();
        }
    }
}
