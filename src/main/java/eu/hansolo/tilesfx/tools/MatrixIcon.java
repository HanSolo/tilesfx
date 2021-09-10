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

import eu.hansolo.tilesfx.Tile;
import javafx.scene.paint.Color;


public class MatrixIcon {
    public static final Color BACKGROUND = Tile.BACKGROUND.darker();

    private Pixel[][] matrix = { { new Pixel(BACKGROUND), new Pixel(BACKGROUND), new Pixel(BACKGROUND), new Pixel(BACKGROUND), new Pixel(BACKGROUND), new Pixel(BACKGROUND), new Pixel(BACKGROUND), new Pixel(BACKGROUND) },
                                 { new Pixel(BACKGROUND), new Pixel(BACKGROUND), new Pixel(BACKGROUND), new Pixel(BACKGROUND), new Pixel(BACKGROUND), new Pixel(BACKGROUND), new Pixel(BACKGROUND), new Pixel(BACKGROUND) },
                                 { new Pixel(BACKGROUND), new Pixel(BACKGROUND), new Pixel(BACKGROUND), new Pixel(BACKGROUND), new Pixel(BACKGROUND), new Pixel(BACKGROUND), new Pixel(BACKGROUND), new Pixel(BACKGROUND) },
                                 { new Pixel(BACKGROUND), new Pixel(BACKGROUND), new Pixel(BACKGROUND), new Pixel(BACKGROUND), new Pixel(BACKGROUND), new Pixel(BACKGROUND), new Pixel(BACKGROUND), new Pixel(BACKGROUND) },
                                 { new Pixel(BACKGROUND), new Pixel(BACKGROUND), new Pixel(BACKGROUND), new Pixel(BACKGROUND), new Pixel(BACKGROUND), new Pixel(BACKGROUND), new Pixel(BACKGROUND), new Pixel(BACKGROUND) },
                                 { new Pixel(BACKGROUND), new Pixel(BACKGROUND), new Pixel(BACKGROUND), new Pixel(BACKGROUND), new Pixel(BACKGROUND), new Pixel(BACKGROUND), new Pixel(BACKGROUND), new Pixel(BACKGROUND) },
                                 { new Pixel(BACKGROUND), new Pixel(BACKGROUND), new Pixel(BACKGROUND), new Pixel(BACKGROUND), new Pixel(BACKGROUND), new Pixel(BACKGROUND), new Pixel(BACKGROUND), new Pixel(BACKGROUND) },
                                 { new Pixel(BACKGROUND), new Pixel(BACKGROUND), new Pixel(BACKGROUND), new Pixel(BACKGROUND), new Pixel(BACKGROUND), new Pixel(BACKGROUND), new Pixel(BACKGROUND), new Pixel(BACKGROUND) }
                               };


    // ******************** Constructors **************************************
    public MatrixIcon() {

    }


    // ******************** Methods *******************************************
    public Pixel[][] getMatrix() { return matrix; }

    public Pixel getPixelAt(final int X, final int Y) {
        if (!isValid(X) || !isValid(Y)) { throw new IllegalArgumentException("x/y values need to be in the range of 0-7"); }
        return matrix[X][Y];
    }
    public void setPixelAt(final int X, final int Y, final Color COLOR) {
        if (!isValid(X) || !isValid(Y)) { throw new IllegalArgumentException("x/y values need to be in the range of 0-7"); }
        matrix[X][Y].setColor(COLOR);
    }

    public void fillPixels(final int X_START, final int X_END, final int Y, final Color COLOR) {
        if (!isValid(X_START) || !isValid(X_END) || !isValid(Y)) { throw new IllegalArgumentException("x/y values need to be in the range of 0-7"); }
        if (X_START > X_END) { throw new IllegalArgumentException("start x cannot be greater than end x"); }
        for (int x = X_START ; x <= X_END ; x++) {
            matrix[x][Y].setColor(COLOR);
        }
    }

    public String getCode() {
        StringBuilder code = new StringBuilder().append("MatrixIcon icon = new MatrixIcon();").append("\n");
        int startIndex       = -1;
        int endIndex         = -1;
        Color lastPixelColor = null;
        for (int y = 0 ; y < 8 ; y++) {
            for (int x = 0 ; x < 8 ; x++) {
                Color currentPixelColor = getPixelAt(x, y).getColor();
                Color nextPixelColor    = x == 7 ? null : getPixelAt(x + 1, y).getColor();
                if (currentPixelColor.equals(nextPixelColor)) {
                    if (startIndex == -1) {
                        startIndex = x;
                    }
                } else if (x == 7) {
                    if (currentPixelColor.equals(lastPixelColor)) {
                        if (startIndex > -1) {
                            endIndex = x;
                            code.append("icon.fillPixels(").append(startIndex).append(", ").append(endIndex).append(", ").append(y).append(", Color.web(\"").append(currentPixelColor.toString().replace("0x", "#")).append("\"));\n");
                        } else {
                            code.append("icon.setPixel(").append(x).append(", ").append(y).append(", Color.web(\"").append(currentPixelColor.toString().replace("0x", "#")).append("\"));\n");
                        }
                    } else {
                        code.append("icon.setPixel(").append(x).append(", ").append(y).append(", Color.web(\"").append(currentPixelColor.toString().replace("0x", "#")).append("\"));\n");
                    }
                } else {
                    endIndex = x;
                    if (startIndex == -1) {
                        code.append("icon.setPixel(").append(x).append(", ").append(y).append(", Color.web(\"").append(currentPixelColor.toString().replace("0x", "#")).append("\"));\n");
                    } else {
                        code.append("icon.fillPixels(").append(startIndex).append(", ").append(endIndex).append(", ").append(y).append(", Color.web(\"").append(currentPixelColor.toString().replace("0x", "#")).append("\"));\n");
                        startIndex = -1;
                        endIndex   = -1;
                    }
                }
                lastPixelColor = currentPixelColor;
            }
            startIndex     = -1;
            endIndex       = -1;
            lastPixelColor = null;
        }
        return code.toString();
    }

    private boolean isValid(final int VALUE) {
        return VALUE >= 0 && VALUE <= 7;
    }
}
