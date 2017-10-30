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

package eu.hansolo.tilesfx.weather;

import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.weather.DarkSky.ConditionAndIcon;
import javafx.beans.DefaultProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.StyleablePropertyFactory;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

import java.util.List;


/**
 * User: hansolo
 * Date: 18.01.14
 * Time: 14:39
 */
@DefaultProperty("children")
public class WeatherSymbol extends Region {
    private static final double                                  PREFERRED_WIDTH  = 64;
    private static final double                                  PREFERRED_HEIGHT = 64;
    private static final double                                  MINIMUM_WIDTH    = 10;
    private static final double                                  MINIMUM_HEIGHT   = 10;
    private static final double                                  MAXIMUM_WIDTH    = 1024;
    private static final double                                  MAXIMUM_HEIGHT   = 1024;
    private static final StyleablePropertyFactory<WeatherSymbol> FACTORY          = new StyleablePropertyFactory<>(Region.getClassCssMetaData());
    private static final CssMetaData<WeatherSymbol, Color>       SYMBOL_COLOR     = FACTORY.createColorCssMetaData("-symbol-color", s -> s.symbolColor, Color.WHITE, false);
    private              ObjectProperty<ConditionAndIcon>        condition;
    private              StyleableProperty<Color>                symbolColor;
    private              double                                  size;
    private              Region                                  conditionIcon;
    private              Pane                                    pane;


    // ******************** Constructor ***************************************
    public WeatherSymbol() {
        this(ConditionAndIcon.NONE, PREFERRED_WIDTH, Color.WHITE);
    }
    public WeatherSymbol(final ConditionAndIcon CONDITION, final double SIZE, final Color COLOR) {
        condition   = new ObjectPropertyBase<ConditionAndIcon>(null == CONDITION ? ConditionAndIcon.NONE : CONDITION) {
            @Override protected void invalidated() {
                conditionIcon.setId(get().styleClass);
                resize();
            }
            @Override public Object getBean() { return WeatherSymbol.this; }
            @Override public String getName() { return "condition"; }
        };
        symbolColor = new StyleableObjectProperty<Color>(null == COLOR ? Tile.FOREGROUND : COLOR) {
            @Override public Object getBean() { return WeatherSymbol.this; }
            @Override public String getName() { return "symbolColor"; }
            @Override public CssMetaData<? extends Styleable, Color> getCssMetaData() { return SYMBOL_COLOR; }
        };
        size        = SIZE;
        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void initGraphics() {
        if (Double.compare(getPrefWidth(), 0.0) <= 0 || Double.compare(getPrefHeight(), 0.0) <= 0 ||
            Double.compare(getWidth(), 0.0) <= 0 || Double.compare(getHeight(), 0.0) <= 0) {
            if (getPrefWidth() > 0 && getPrefHeight() > 0) {
                setPrefSize(getPrefWidth(), getPrefHeight());
            } else {
                setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            }
        }

        getStyleClass().setAll("weather-symbol");

        conditionIcon = new Region();
        conditionIcon.setId(condition.get().styleClass);
        conditionIcon.setStyle("-symbol-color: " + getSymbolColor().toString().replace("0x", "#") + ";");

        pane = new Pane(conditionIcon);

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        widthProperty().addListener(o -> resize());
        heightProperty().addListener(o -> resize());
    }


    // ******************** Methods *******************************************
    @Override protected double computeMinWidth(final double HEIGHT) { return MINIMUM_WIDTH; }
    @Override protected double computeMinHeight(final double WIDTH) { return MINIMUM_HEIGHT; }
    @Override protected double computePrefWidth(final double HEIGHT) { return super.computePrefWidth(HEIGHT); }
    @Override protected double computePrefHeight(final double WIDTH) { return super.computePrefHeight(WIDTH); }
    @Override protected double computeMaxWidth(final double HEIGHT) { return MAXIMUM_WIDTH; }
    @Override protected double computeMaxHeight(final double WIDTH) { return MAXIMUM_HEIGHT; }

    @Override public ObservableList<Node> getChildren() { return super.getChildren(); }

    public final ConditionAndIcon getCondition() { return condition.get(); }
    public final void setCondition(final ConditionAndIcon CONDITION) { condition.set(CONDITION); }
    public final ObjectProperty<ConditionAndIcon> conditionProperty() { return condition; }

    public final Color getSymbolColor() { return symbolColor.getValue(); }
    public final void setSymbolColor(final Color COLOR) { symbolColor.setValue(COLOR); }
    public final StyleableProperty<Color> symbolColorProperty() { return symbolColor; }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() { return FACTORY.getCssMetaData(); }
    @Override public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() { return getClassCssMetaData(); }

    public void resize() {
        double width  = getWidth() - getInsets().getLeft() - getInsets().getRight();
        double height = getHeight() - getInsets().getTop() - getInsets().getBottom();
        size = width < height ? width : height;

        if (width > 0 && height > 0) {
            pane.setMaxSize(size, size);
            pane.setPrefSize(size, size);
            pane.relocate((getWidth() - size) * 0.5, (getHeight() - size) * 0.5);

            conditionIcon.setPrefSize(size * getCondition().widthFactor, size * getCondition().heightFactor);
            conditionIcon.relocate((size - conditionIcon.getPrefWidth()) * 0.5, (size - conditionIcon.getPrefHeight()) * 0.5);
        }
    }
}