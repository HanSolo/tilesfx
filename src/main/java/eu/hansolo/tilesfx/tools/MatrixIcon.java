/*
 * Copyright (c) 2020 by Gerrit Grunwald
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


    public MatrixIcon() {

    }


    public Pixel[][] getMatrix() { return matrix; }

    public Pixel getPixelAt(final int X, final int Y) {
        if (X < 0 || X > 7 || Y < 0 || Y > 7) { throw new IllegalArgumentException("x/y values need to be in the range of 0-7"); }
        return matrix[X][Y];
    }
    public void setPixelAt(final int X, final int Y, final Color COLOR) {
        if (X < 0 || X > 7 || Y < 0 || Y > 7) { throw new IllegalArgumentException("x/y values need to be in the range of 0-7"); }
        matrix[X][Y].setColor(COLOR);
    }
}
