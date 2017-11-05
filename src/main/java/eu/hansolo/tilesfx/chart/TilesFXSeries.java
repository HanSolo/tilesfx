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

import javafx.geometry.Insets;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;


public class TilesFXSeries<X, Y> {
    private Series<X, Y> series;
    private Paint        stroke;
    private Paint        fill;
    private Background   symbolBackground;
    private Paint        legendSymbolFill;


    // ******************** Constructors **************************************
    public TilesFXSeries(final Series<X, Y> SERIES) {
        this(SERIES, null, null);
    }
    public TilesFXSeries(final Series<X, Y> SERIES, final Paint COLOR) {
        series = SERIES;
        stroke = COLOR;
        fill   = COLOR;
        if (null != COLOR) {
            symbolBackground = new Background(new BackgroundFill(COLOR, new CornerRadii(5), Insets.EMPTY), new BackgroundFill(Color.WHITE, new CornerRadii(5), new Insets(2)));
            legendSymbolFill = COLOR;
        }
    }
    public TilesFXSeries(final Series<X, Y> SERIES, final Paint STROKE, final Paint FILL) {
        series = SERIES;
        stroke = STROKE;
        fill   = FILL;
        if (null != stroke & null != fill) {
            symbolBackground = new Background(new BackgroundFill(STROKE, new CornerRadii(5), Insets.EMPTY), new BackgroundFill(Color.WHITE, new CornerRadii(5), new Insets(2)));
            legendSymbolFill = stroke;
        }
    }
    public TilesFXSeries(final Series<X, Y> SERIES, final Paint STROKE, final Paint FILL, final Paint LEGEND_SYMBOL_FILL) {
        series           = SERIES;
        stroke           = STROKE;
        fill             = FILL;
        legendSymbolFill = LEGEND_SYMBOL_FILL;
    }
    public TilesFXSeries(final Series<X, Y> SERIES, final Paint STROKE, final Paint FILL, final Background SYMBOL_BACKGROUND) {
        series           = SERIES;
        stroke           = STROKE;
        fill             = FILL;
        symbolBackground = SYMBOL_BACKGROUND;
    }
    public TilesFXSeries(final Series<X, Y> SERIES, final Paint STROKE, final Paint FILL, final Background SYMBOL_BACKGROUND, final Paint LEGEND_SYMBOL_FILL) {
        series           = SERIES;
        stroke           = STROKE;
        fill             = FILL;
        symbolBackground = SYMBOL_BACKGROUND;
        legendSymbolFill = LEGEND_SYMBOL_FILL;
    }


    // ******************** Methods *******************************************
    public Series<X, Y> getSeries() { return series; }
    public void setSeries(final Series<X, Y> SERIES) { series = SERIES; }

    public Paint getStroke() { return stroke; }
    public void setStroke(final Paint STROKE) { stroke = STROKE; }

    public Paint getFill() { return fill; }
    public void setFill(final Paint FILL) { fill = FILL; }

    public Background getSymbolBackground() { return symbolBackground; }
    public void setSymbolBackground(final Background BACKGROUND) { symbolBackground = BACKGROUND; }

    public Paint getLegendSymbolFill() { return legendSymbolFill; }
    public void setLegendSymbolFill(final Paint FILL) { legendSymbolFill = FILL; }
}
