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

import eu.hansolo.tilesfx.chart.PixelMatrix.PixelShape;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.scene.paint.Color;

import java.util.HashMap;


/**
 * Created by hansolo on 20.03.17.
 */
public class PixelMatrixBuilder<B extends PixelMatrixBuilder<B>> {
    private HashMap<String, Property> properties = new HashMap<>();


    // ******************** Constructors **************************************
    protected PixelMatrixBuilder() {}


    // ******************** Methods *******************************************
    public static final PixelMatrixBuilder create() {
        return new PixelMatrixBuilder();
    }

    public final B colsAndRows(final int[] COLS_AND_ROWS) { return colsAndRows(COLS_AND_ROWS[0], COLS_AND_ROWS[1]); }
    public final B colsAndRows(final int COLS, final int ROWS) {
        properties.put("cols", new SimpleIntegerProperty(COLS));
        properties.put("rows", new SimpleIntegerProperty(ROWS));
        return (B)this;
    }

    public final B pixelOnColor(final Color COLOR) {
        properties.put("pixelOnColor", new SimpleObjectProperty(COLOR));
        return (B)this;
    }
    public final B pixelOffColor(final Color COLOR) {
        properties.put("pixelOffColor", new SimpleObjectProperty(COLOR));
        return (B)this;
    }

    public final B pixelShape(final PixelShape SHAPE) {
        properties.put("pixelShape", new SimpleObjectProperty(SHAPE));
        return (B)this;
    }

    public final B matrixFont(final MatrixFont FONT) {
        properties.put("matrixFont", new SimpleObjectProperty(FONT));
        return (B)this;
    }

    public final B useSpacer(final boolean USE) {
        properties.put("useSpacer", new SimpleBooleanProperty(USE));
        return (B)this;
    }

    public final B squarePixels(final boolean SQUARE) {
        properties.put("squarePixels", new SimpleBooleanProperty(SQUARE));
        return (B)this;
    }

    public final B spacerSizeFactor(final double FACTOR) {
        properties.put("spacerSizeFactor", new SimpleDoubleProperty(FACTOR));
        return (B)this;
    }

    public final B prefSize(final double WIDTH, final double HEIGHT) {
        properties.put("prefSize", new SimpleObjectProperty<>(new Dimension2D(WIDTH, HEIGHT)));
        return (B)this;
    }
    public final B minSize(final double WIDTH, final double HEIGHT) {
        properties.put("minSize", new SimpleObjectProperty<>(new Dimension2D(WIDTH, HEIGHT)));
        return (B)this;
    }
    public final B maxSize(final double WIDTH, final double HEIGHT) {
        properties.put("maxSize", new SimpleObjectProperty<>(new Dimension2D(WIDTH, HEIGHT)));
        return (B)this;
    }

    public final B prefWidth(final double PREF_WIDTH) {
        properties.put("prefWidth", new SimpleDoubleProperty(PREF_WIDTH));
        return (B)this;
    }
    public final B prefHeight(final double PREF_HEIGHT) {
        properties.put("prefHeight", new SimpleDoubleProperty(PREF_HEIGHT));
        return (B)this;
    }

    public final B minWidth(final double MIN_WIDTH) {
        properties.put("minWidth", new SimpleDoubleProperty(MIN_WIDTH));
        return (B)this;
    }
    public final B minHeight(final double MIN_HEIGHT) {
        properties.put("minHeight", new SimpleDoubleProperty(MIN_HEIGHT));
        return (B)this;
    }

    public final B maxWidth(final double MAX_WIDTH) {
        properties.put("maxWidth", new SimpleDoubleProperty(MAX_WIDTH));
        return (B)this;
    }
    public final B maxHeight(final double MAX_HEIGHT) {
        properties.put("maxHeight", new SimpleDoubleProperty(MAX_HEIGHT));
        return (B)this;
    }

    public final B scaleX(final double SCALE_X) {
        properties.put("scaleX", new SimpleDoubleProperty(SCALE_X));
        return (B)this;
    }
    public final B scaleY(final double SCALE_Y) {
        properties.put("scaleY", new SimpleDoubleProperty(SCALE_Y));
        return (B)this;
    }

    public final B layoutX(final double LAYOUT_X) {
        properties.put("layoutX", new SimpleDoubleProperty(LAYOUT_X));
        return (B)this;
    }
    public final B layoutY(final double LAYOUT_Y) {
        properties.put("layoutY", new SimpleDoubleProperty(LAYOUT_Y));
        return (B)this;
    }

    public final B translateX(final double TRANSLATE_X) {
        properties.put("translateX", new SimpleDoubleProperty(TRANSLATE_X));
        return (B)this;
    }
    public final B translateY(final double TRANSLATE_Y) {
        properties.put("translateY", new SimpleDoubleProperty(TRANSLATE_Y));
        return (B)this;
    }

    public final B padding(final Insets INSETS) {
        properties.put("padding", new SimpleObjectProperty<>(INSETS));
        return (B)this;
    }

    public final PixelMatrix build() {
        final PixelMatrix CONTROL;
        if (properties.keySet().contains("cols") && properties.keySet().contains("rows")) {
            int cols = ((IntegerProperty) properties.get("cols")).get();
            int rows = ((IntegerProperty) properties.get("rows")).get();
            CONTROL = new PixelMatrix(cols, rows);
        } else {
            CONTROL = new PixelMatrix();
        }

        for (String key : properties.keySet()) {
            if ("prefSize".equals(key)) {
                Dimension2D dim = ((ObjectProperty<Dimension2D>) properties.get(key)).get();
                CONTROL.setPrefSize(dim.getWidth(), dim.getHeight());
            } else if("minSize".equals(key)) {
                Dimension2D dim = ((ObjectProperty<Dimension2D>) properties.get(key)).get();
                CONTROL.setMinSize(dim.getWidth(), dim.getHeight());
            } else if("maxSize".equals(key)) {
                Dimension2D dim = ((ObjectProperty<Dimension2D>) properties.get(key)).get();
                CONTROL.setMaxSize(dim.getWidth(), dim.getHeight());
            } else if("prefWidth".equals(key)) {
                CONTROL.setPrefWidth(((DoubleProperty) properties.get(key)).get());
            } else if("prefHeight".equals(key)) {
                CONTROL.setPrefHeight(((DoubleProperty) properties.get(key)).get());
            } else if("minWidth".equals(key)) {
                CONTROL.setMinWidth(((DoubleProperty) properties.get(key)).get());
            } else if("minHeight".equals(key)) {
                CONTROL.setMinHeight(((DoubleProperty) properties.get(key)).get());
            } else if("maxWidth".equals(key)) {
                CONTROL.setMaxWidth(((DoubleProperty) properties.get(key)).get());
            } else if("maxHeight".equals(key)) {
                CONTROL.setMaxHeight(((DoubleProperty) properties.get(key)).get());
            } else if("scaleX".equals(key)) {
                CONTROL.setScaleX(((DoubleProperty) properties.get(key)).get());
            } else if("scaleY".equals(key)) {
                CONTROL.setScaleY(((DoubleProperty) properties.get(key)).get());
            } else if ("layoutX".equals(key)) {
                CONTROL.setLayoutX(((DoubleProperty) properties.get(key)).get());
            } else if ("layoutY".equals(key)) {
                CONTROL.setLayoutY(((DoubleProperty) properties.get(key)).get());
            } else if ("translateX".equals(key)) {
                CONTROL.setTranslateX(((DoubleProperty) properties.get(key)).get());
            } else if ("translateY".equals(key)) {
                CONTROL.setTranslateY(((DoubleProperty) properties.get(key)).get());
            } else if ("padding".equals(key)) {
                CONTROL.setPadding(((ObjectProperty<Insets>) properties.get(key)).get());
            } else if("pixelOnColor".equals(key)) {
                CONTROL.setPixelOnColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if("pixelOffColor".equals(key)) {
                CONTROL.setPixelOffColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("pixelShape".equals(key)) {
                CONTROL.setPixelShape(((ObjectProperty<PixelShape>) properties.get(key)).get());
            } else if ("matrixFont".equals(key)) {
                CONTROL.setMatrixFont(((ObjectProperty<MatrixFont>) properties.get(key)).get());
            } else if ("useSpacer".equals(key)) {
                CONTROL.setUseSpacer(((BooleanProperty) properties.get(key)).get());
            } else if ("spacerSizeFactor".equals(key)) {
                CONTROL.setSpacerSizeFactor(((DoubleProperty) properties.get(key)).get());
            } else if ("squarePixels".equals(key)) {
                CONTROL.setSquarePixels(((BooleanProperty) properties.get(key)).get());
            }
        }
        return CONTROL;
    }
}
