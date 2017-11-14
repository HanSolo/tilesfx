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
import eu.hansolo.tilesfx.events.PixelMatrixEvent;
import eu.hansolo.tilesfx.events.PixelMatrixEventListener;
import eu.hansolo.tilesfx.tools.CtxBounds;
import eu.hansolo.tilesfx.tools.CtxCornerRadii;
import eu.hansolo.tilesfx.tools.Helper;
import javafx.beans.DefaultProperty;
import javafx.beans.InvalidationListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

import java.util.concurrent.CopyOnWriteArrayList;


/**
 * User: hansolo
 * Date: 19.03.17
 * Time: 04:39
 */
@DefaultProperty("children")
public class PixelMatrix extends Region {
    public  enum PixelShape { SQUARE, ROUNDED_RECT, ROUND }
    public  static final double                                         DEFAULT_SPACER_SIZE_FACTOR = 0.05;
    private static final int                                            RED_MASK                   = 255 << 16;
    private static final int                                            GREEN_MASK                 = 255 << 8;
    private static final int                                            BLUE_MASK                  = 255;
    private static final int                                            ALPHA_MASK                 = 255 << 24;
    private static final double                                         ALPHA_FACTOR               = 1.0 / 255.0;
    private              double                                         preferredWidth;
    private              double                                         preferredHeight;
    private              double                                         width;
    private              double                                         height;
    private              Canvas                                         canvas;
    private              GraphicsContext                                ctx;
    private              int                                            pixelOnColor;
    private              int                                            pixelOffColor;
    private              PixelShape                                     pixelShape;
    private              int                                            cols;
    private              int                                            rows;
    private              int[][]                                        matrix;
    private              MatrixFont                                     matrixFont;
    private              int                                            characterWidth;
    private              int                                            characterHeight;
    private              int                                            characterWidthMinusOne;
    private              double                                         pixelSize;
    private              double                                         pixelWidth;
    private              double                                         pixelHeight;
    private              double                                         spacer;
    private              boolean                                        useSpacer;
    private              boolean                                        squarePixels;
    private              double                                         spacerSizeFactor;
    private              double                                         pixelSizeMinusDoubleSpacer;
    private              double                                         pixelWidthMinusDoubleSpacer;
    private              double                                         pixelHeightMinusDoubleSpacer;
    private              InvalidationListener                           sizeListener;
    private              EventHandler<MouseEvent>                       clickHandler;
    private              CopyOnWriteArrayList<PixelMatrixEventListener> listeners;


    // ******************** Constructors **************************************
    public PixelMatrix() {
        this(250, 250, 32, 32, Tile.BLUE, Tile.BACKGROUND.brighter(), PixelShape.SQUARE, MatrixFont8x8.INSTANCE);
    }
    public PixelMatrix(final int COLS, final int ROWS) {
        this(250, 250, COLS, ROWS, Tile.BLUE, Tile.BACKGROUND.brighter(), PixelShape.SQUARE, MatrixFont8x8.INSTANCE);
    }
    public PixelMatrix(final int COLS, final int ROWS, final Color DOT_ON_COLOR) {
        this(250, 250, COLS, ROWS, DOT_ON_COLOR, Tile.BACKGROUND.brighter(), PixelShape.SQUARE, MatrixFont8x8.INSTANCE);
    }
    public PixelMatrix(final double PREFERRED_WIDTH, final double PREFERRED_HEIGHT, final int COLS, final int ROWS, final Color DOT_ON_COLOR, final Color DOT_OFF_COLOR, final PixelShape DOT_SHAPE, final MatrixFont FONT) {
        preferredWidth         = PREFERRED_WIDTH;
        preferredHeight        = PREFERRED_HEIGHT;
        pixelOnColor           = convertToInt(DOT_ON_COLOR);
        pixelOffColor          = convertToInt(DOT_OFF_COLOR);
        pixelShape             = DOT_SHAPE;
        cols                   = COLS;
        rows                   = ROWS;
        matrix                 = new int[cols][rows];
        matrixFont             = FONT;
        characterWidth         = matrixFont.getCharacterWidth();
        characterHeight        = matrixFont.getCharacterHeight();
        characterWidthMinusOne = characterWidth - 1;
        useSpacer              = true;
        squarePixels           = true;
        spacerSizeFactor       = DEFAULT_SPACER_SIZE_FACTOR;
        sizeListener           = o -> resize();
        clickHandler           = e -> checkForClick(e);
        listeners              = new CopyOnWriteArrayList<>();
        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void initGraphics() {
        // prefill matrix with pixelOffColor
        for (int y = 0 ; y < rows ; y++) {
            for (int x = 0 ; x < cols ; x++) {
                matrix[x][y] = pixelOffColor;
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

        getChildren().setAll(canvas);
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, clickHandler);
    }

    private void registerListeners() {
        widthProperty().addListener(sizeListener);
        heightProperty().addListener(sizeListener);
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, clickHandler);
    }


    // ******************** Methods *******************************************
    @Override public ObservableList<Node> getChildren() { return super.getChildren(); }

    public void setColsAndRows(final int[] COLS_AND_ROWS) { setColsAndRows(COLS_AND_ROWS[0], COLS_AND_ROWS[1]); }
    public void setColsAndRows(final int COLS, final int ROWS) {
        cols   = COLS;
        rows   = ROWS;
        matrix = new int[cols][rows];
        initGraphics();
        resize();
    }

    public Color getPixelOnColor() { return convertToColor(pixelOnColor); }
    public void setPixelOnColor(final Color COLOR) {
        pixelOnColor = convertToInt(COLOR);
        drawMatrix();
    }

    public Color getPixelOffColor() { return convertToColor(pixelOffColor); }
    public void setPixelOffColor(final Color COLOR) {
        pixelOffColor = convertToInt(COLOR);
        for (int y = 0 ; y < rows ; y++) {
            for (int x = 0 ; x < cols ; x++) {
                matrix[x][y] = pixelOffColor;
            }
        }
        drawMatrix();
    }

    public PixelShape getPixelShape() { return pixelShape; }
    public void setPixelShape(final PixelShape SHAPE) {
        pixelShape = SHAPE;
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
        resize();
    }

    public boolean isSquarePixels() { return squarePixels; }
    public void setSquarePixels(final boolean SQUARE) {
        squarePixels = SQUARE;
        resize();
    }

    public double getSpacerSizeFactor() { return spacerSizeFactor; }
    public void setSpacerSizeFactor(final double FACTOR) {
        spacerSizeFactor         = Helper.clamp(0.0, 0.2, FACTOR);
        spacer                   = useSpacer ? pixelSize * spacerSizeFactor : 0;
        pixelSizeMinusDoubleSpacer = pixelSize - spacer * 2;
        drawMatrix();
    }

    public void setPixel(final int X, final int Y, final boolean VALUE) { setPixel(X, Y, VALUE ? pixelOnColor : pixelOffColor); }
    public void setPixel(final int X, final int Y, final Color COLOR) { setPixel(X, Y, convertToInt(COLOR)); }
    public void setPixel(final int X, final int Y, final int COLOR_VALUE) {
        if (X >= cols || X < 0) return;
        if (Y >= rows || Y < 0) return;
        matrix[X][Y] = COLOR_VALUE;
    }

    public void setPixelWithRedraw(final int X, final int Y, final boolean ON) {
        setPixel(X, Y, ON ? pixelOnColor : pixelOffColor);
        drawMatrix();
    }
    public void setPixelWithRedraw(final int X, final int Y, final int COLOR_VALUE) {
        setPixel(X, Y, COLOR_VALUE);
        drawMatrix();
    }

    public void setCharAt(final char CHAR, final int X, final int Y) {
        setCharAt(CHAR, X, Y, pixelOnColor);
    }
    public void setCharAt(final char CHAR, final int X, final int Y, final int COLOR_VALUE) {
        int[] c = matrixFont.getCharacter(CHAR);
        for (int x = 0; x < characterWidth; x++) {
            for (int y = 0; y < characterHeight; y++) {
                setPixel(x + X, y + Y, getBitAt(characterWidthMinusOne - x, y, c) == 0 ? pixelOffColor : COLOR_VALUE);
            }
        }
        drawMatrix();
    }

    public void setCharAtWithBackground(final char CHAR, final int X, final int Y) {
        setCharAtWithBackground(CHAR, X, Y, pixelOnColor);
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

    public double getPixelSize() { return pixelSize; }
    public double getPixelWidth() { return pixelWidth; }
    public double getPixelHeight() { return pixelHeight; }

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

    public void setAllPixelsOn() {
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                setPixel(x, y, true);
            }
        }
        drawMatrix();
    }
    public void setAllPixelsOff() {
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                setPixel(x, y, false);
            }
        }
        drawMatrix();
    }

    public void drawMatrix() {
        ctx.clearRect(0, 0, width, height);
        switch(pixelShape) {
            case ROUNDED_RECT:
                CtxBounds      bounds      = new CtxBounds(pixelWidthMinusDoubleSpacer, pixelHeightMinusDoubleSpacer);
                CtxCornerRadii cornerRadii = new CtxCornerRadii(pixelSize * 0.125);
                for (int y = 0; y < rows; y++) {
                    for (int x = 0; x < cols; x++) {
                        ctx.setFill(convertToColor(matrix[x][y]));
                        bounds.setX(x * pixelWidth + spacer);
                        bounds.setY(y * pixelHeight + spacer);
                        Helper.drawRoundedRect(ctx, bounds, cornerRadii);
                        ctx.fill();
                    }
                }
                break;
            case ROUND:
                for (int y = 0; y < rows; y++) {
                    for (int x = 0; x < cols; x++) {
                        ctx.setFill(convertToColor(matrix[x][y]));
                        ctx.fillOval(x * pixelWidth + spacer, y * pixelHeight + spacer, pixelWidthMinusDoubleSpacer, pixelHeightMinusDoubleSpacer);
                    }
                }
                break;
            case SQUARE:
            default    :
                for (int y = 0; y < rows; y++) {
                    for (int x = 0; x < cols; x++) {
                        ctx.setFill(convertToColor(matrix[x][y]));
                        ctx.fillRect(x * pixelWidth + spacer, y * pixelHeight + spacer, pixelWidthMinusDoubleSpacer, pixelHeightMinusDoubleSpacer);
                    }
                }
                break;
        }
    }

    public void setOnPixelMatrixEvent(final PixelMatrixEventListener LISTENER) { addPixelMatrixEventListener(LISTENER); }
    public void addPixelMatrixEventListener(final PixelMatrixEventListener LISTENER) { if (!listeners.contains(LISTENER)) listeners.add(LISTENER); }
    public void removePixelMatrixEventListener(final PixelMatrixEventListener LISTENER) { if (listeners.contains(LISTENER)) listeners.remove(LISTENER); }
    public void removeAllPixelMatrixEventListeners() { listeners.clear(); }

    public void firePixelMatrixEvent(final PixelMatrixEvent EVENT) {
        for (PixelMatrixEventListener listener : listeners) { listener.onPixelMatrixEvent(EVENT); }
    }

    @Override protected double computePrefWidth(final double HEIGHT) { return super.computePrefWidth(HEIGHT); }
    @Override protected double computePrefHeight(final double WIDTH) { return super.computePrefHeight(WIDTH); }

    public void dispose() {
        listeners.clear();
        widthProperty().removeListener(sizeListener);
        heightProperty().removeListener(sizeListener);
        canvas.removeEventHandler(MouseEvent.MOUSE_PRESSED, clickHandler);
    }

    private long getRed(final long COLOR_VALUE) { return  (COLOR_VALUE & RED_MASK) >> 16; }
    private long getGreen(final long COLOR_VALUE) { return  (COLOR_VALUE & GREEN_MASK) >> 8; }
    private long getBlue(final long COLOR_VALUE) { return (COLOR_VALUE & BLUE_MASK); }
    private long getAlpha(final long COLOR_VALUE) { return (COLOR_VALUE & ALPHA_MASK) >>> 24; }

    public void checkForClick(final MouseEvent EVT) {
        double spacerPlusPixelWidthMinusDoubleSpacer  = spacer + pixelWidthMinusDoubleSpacer;
        double spacerPlusPixelHeightMinusDoubleSpacer = spacer + pixelHeightMinusDoubleSpacer;
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                if (Helper.isInRectangle(EVT.getX(), EVT.getY(), x * pixelWidth + spacer, y * pixelHeight + spacer, x * pixelWidth + spacerPlusPixelWidthMinusDoubleSpacer, y * pixelHeight + spacerPlusPixelHeightMinusDoubleSpacer)) {
                    firePixelMatrixEvent(new PixelMatrixEvent(x, y, EVT.getScreenX(), EVT.getScreenY()));
                    break;
                }
            }
        }
    }


    // ******************** Resizing ******************************************
    private void resize() {
        width                        = getWidth() - getInsets().getLeft() - getInsets().getRight();
        height                       = getHeight() - getInsets().getTop() - getInsets().getBottom();
        pixelSize                    = (width / cols) < (height / rows) ? (width / cols) : (height / rows);
        pixelWidth                   = (width / cols);
        pixelHeight                  = (height / rows);
        spacer                       = useSpacer ? pixelSize * getSpacerSizeFactor() : 0;
        pixelSizeMinusDoubleSpacer   = pixelSize - spacer * 2;
        pixelWidthMinusDoubleSpacer  = pixelWidth - spacer * 2;
        pixelHeightMinusDoubleSpacer = pixelHeight - spacer * 2;


        if (width > 0 && height > 0) {
            if (squarePixels) {
                pixelWidth                   = pixelSize;
                pixelHeight                  = pixelSize;
                pixelWidthMinusDoubleSpacer  = pixelSizeMinusDoubleSpacer;
                pixelHeightMinusDoubleSpacer = pixelSizeMinusDoubleSpacer;
            }
            canvas.setWidth(cols * pixelWidth);
            canvas.setHeight(rows * pixelHeight);

            canvas.relocate((getWidth() - (cols *pixelWidth)) * 0.5, (getHeight() - (rows * pixelHeight)) * 0.5);

            drawMatrix();
        }
    }
}
