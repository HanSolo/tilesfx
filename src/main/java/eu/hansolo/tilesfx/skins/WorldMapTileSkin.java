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

package eu.hansolo.tilesfx.skins;

import eu.hansolo.tilesfx.tools.Country;
import eu.hansolo.tilesfx.tools.CountryPath;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.Tile.TextSize;
import eu.hansolo.tilesfx.chart.ChartData;
import eu.hansolo.tilesfx.events.LocationEvent;
import eu.hansolo.tilesfx.events.TileEvent;
import eu.hansolo.tilesfx.events.TileEvent.EventType;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.Helper;
import eu.hansolo.tilesfx.tools.Location;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableMap;
import javafx.collections.WeakListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static eu.hansolo.tilesfx.tools.Helper.clamp;


/**
 * Created by hansolo on 19.12.16.
 */
public class WorldMapTileSkin extends TileSkin {
    protected static final double                                     PREFERRED_WIDTH  = 500; //380; //510;
    protected static final double                                     PREFERRED_HEIGHT = 250;
    private   static final double                                     MAP_ASPECT_RATIO = Helper.MAP_HEIGHT / Helper.MAP_WIDTH;
    private                Text                                       titleText;
    private                Text                                       text;
    private                Pane                                       worldPane;
    private                Group                                      group;
    private                Map<String, List<CountryPath>>             countryPaths;
    private                ObservableMap<Location, Circle>            chartDataLocations;
    private                ObservableMap<Location, Circle>            poiLocations;
    private                Map<CountryPath, EventHandler<MouseEvent>> handlerMap;
    private                ListChangeListener<Location>               poiListener;
    private                ListChangeListener<ChartData>              chartDataListener;
    private                Map<Circle, EventHandler<MouseEvent>>      circleHandlerMap;


    // ******************** Constructors **************************************
    public WorldMapTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        poiLocations       = FXCollections.observableHashMap();
        chartDataLocations = FXCollections.observableHashMap();

        handlerMap       = new HashMap<>();
        circleHandlerMap = new HashMap<>();

        countryPaths = tile.getCountryPaths();

        String formatString = new StringBuilder("%.").append(tile.getDecimals()).append("f").toString();

        poiListener = new WeakListChangeListener<>(change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    change.getAddedSubList().forEach(addedPoi -> {
                        String tooltipText = new StringBuilder(addedPoi.getName()).append("\n")
                                                                                  .append(addedPoi.getInfo())
                                                                                  .toString();
                        EventHandler<MouseEvent> handler = e -> addedPoi.fireLocationEvent(new LocationEvent(addedPoi));
                        Circle circle = new Circle(3, addedPoi.getColor());
                        Tooltip.install(circle, new Tooltip(tooltipText));
                        circleHandlerMap.put(circle, handler);
                        poiLocations.put(addedPoi, circle);
                        circle.setOnMousePressed(handler);
                        getPane().getChildren().add(circle);
                    });
                } else if (change.wasRemoved()) {
                    change.getRemoved().forEach(removedPoi -> {
                        if (circleHandlerMap.get(removedPoi) != null) {
                            poiLocations.get(removedPoi).removeEventHandler(MouseEvent.MOUSE_PRESSED, circleHandlerMap.get(removedPoi));
                        }
                        getPane().getChildren().remove(removedPoi);
                    });
                }
            }
            resize();
        });
        chartDataListener = new WeakListChangeListener<>(change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    change.getAddedSubList().forEach(addedData -> {
                        String tooltipText = new StringBuilder(addedData.getName()).append("\n")
                                                                                   .append(String.format(Locale.US, formatString, addedData.getValue()))
                                                                                   .toString();
                        EventHandler<MouseEvent> handler = e -> tile.fireTileEvent(new TileEvent(EventType.SELECTED_CHART_DATA, addedData));
                        Circle circle = new Circle(3, addedData.getLocation().getColor());
                        Tooltip.install(circle, new Tooltip(tooltipText));
                        circleHandlerMap.put(circle, handler);
                        chartDataLocations.put(addedData.getLocation(), circle);
                        circle.setOnMousePressed(handler);
                        getPane().getChildren().add(circle);
                    });
                } else if (change.wasRemoved()) {
                    change.getRemoved().forEach(removedData -> {
                        if (circleHandlerMap.get(removedData) != null) {
                            chartDataLocations.get(removedData).removeEventHandler(MouseEvent.MOUSE_PRESSED, circleHandlerMap.get(removedData));
                        }
                        getPane().getChildren().remove(removedData);
                    });
                }
            }
            resize();
        });

        tile.getPoiList()
            .forEach(poi -> {
                String tooltipText = new StringBuilder(poi.getName()).append("\n")
                                                                     .append(poi.getInfo())
                                                                     .toString();
                Circle circle = new Circle(3, poi.getColor());
                circle.setOnMousePressed(e -> poi.fireLocationEvent(new LocationEvent(poi)));
                Tooltip.install(circle, new Tooltip(tooltipText));
                poiLocations.put(poi, circle);
            });

        tile.getChartData()
            .stream()
            .filter(chartData -> chartData.getLocation() != null)
            .forEach(chartData -> {
                String tooltipText = new StringBuilder(chartData.getName()).append("\n")
                                                                           .append(String.format(Locale.US, formatString, chartData.getValue()))
                                                                           .toString();
                Circle circle = new Circle(3, chartData.getLocation().getColor());
                circle.setOnMousePressed(e -> tile.fireTileEvent(new TileEvent(EventType.SELECTED_CHART_DATA, chartData)));
                Tooltip.install(circle, new Tooltip(tooltipText));
                chartDataLocations.put(chartData.getLocation(), circle);
            });

        titleText = new Text();
        titleText.setFill(tile.getTitleColor());
        Helper.enableNode(titleText, !tile.getTitle().isEmpty());

        text = new Text(tile.getUnit());
        text.setFill(tile.getUnitColor());
        Helper.enableNode(text, tile.isTextVisible());

        Color fill   = tile.getForegroundColor();
        Color stroke = tile.getBackgroundColor();

        worldPane = new Pane();
        countryPaths.forEach((name, pathList) -> {
            Country country = Country.valueOf(name);
            pathList.forEach(path -> {
                path.setFill(null == country.getColor() ? fill : country.getColor());
                path.setStroke(stroke);
                path.setStrokeWidth(0.2);
            });
            worldPane.getChildren().addAll(pathList);
        });
        group = new Group(worldPane);

        getPane().getChildren().addAll(group, titleText, text);
        getPane().getChildren().addAll(chartDataLocations.values());
        getPane().getChildren().addAll(poiLocations.values());
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        countryPaths.forEach((name , pathList) -> {
            Country country = Country.valueOf(name);
            EventHandler<MouseEvent> clickHandler = e -> tile.fireTileEvent(new TileEvent(EventType.SELECTED_CHART_DATA, new ChartData(country.getName(), country.getValue(), country.getColor())));
            pathList.forEach(path -> {
                handlerMap.put(path, clickHandler);
                path.addEventHandler(MouseEvent.MOUSE_PRESSED, clickHandler);
            });
        });
        tile.getPoiList().addListener(poiListener);
        tile.getChartData().addListener(chartDataListener);
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !tile.getTitle().isEmpty());
            Helper.enableNode(text, tile.isTextVisible());
        } else if ("REFRESH".equals(EVENT_TYPE)) {
            refresh();
        }
    }

    @Override public void dispose() {
        countryPaths.forEach((name, pathList) -> pathList.forEach(path -> path.removeEventHandler(MouseEvent.MOUSE_PRESSED, handlerMap.get(path))));
        tile.getPoiList().removeListener(poiListener);
        tile.getChartData().removeListener(chartDataListener);
        handlerMap.clear();
        circleHandlerMap.clear();
        super.dispose();
    }

    private void setFillAndStroke() {
        countryPaths.keySet().forEach(name -> {
            Country country = Country.valueOf(name);
            setCountryFillAndStroke(country, null == country.getColor() ? tile.getForegroundColor() : country.getColor(), tile.getBackgroundColor());
        });
    }
    private void setCountryFillAndStroke(final Country COUNTRY, final Color FILL, final Color STROKE) {
        List<CountryPath> paths = countryPaths.get(COUNTRY.getName());
        for (CountryPath path : paths) {
            path.setFill(FILL);
            path.setStroke(STROKE);
        }
    }


    // ******************** Resizing ******************************************
    @Override protected void resizeStaticText() {
        double maxWidth = width - size * 0.1;
        double fontSize = size * textSize.factor;

        boolean customFontEnabled = tile.isCustomFontEnabled();
        Font    customFont        = tile.getCustomFont();
        Font    font              = (customFontEnabled && customFont != null) ? Font.font(customFont.getFamily(), fontSize) : Fonts.latoRegular(fontSize);

        titleText.setFont(font);
        if (titleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(titleText, maxWidth, fontSize); }
        switch(tile.getTitleAlignment()) {
            default    :
            case LEFT  : titleText.relocate(size * 0.05, size * 0.05); break;
            case CENTER: titleText.relocate((width - titleText.getLayoutBounds().getWidth()) * 0.5, size * 0.05); break;
            case RIGHT : titleText.relocate(width - (size * 0.05) - titleText.getLayoutBounds().getWidth(), size * 0.05); break;
        }

        text.setText(tile.getText());
        text.setFont(font);
        if (text.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(text, maxWidth, fontSize); }
        switch(tile.getTextAlignment()) {
            default    :
            case LEFT  : text.setX(size * 0.05); break;
            case CENTER: text.setX((width - text.getLayoutBounds().getWidth()) * 0.5); break;
            case RIGHT : text.setX(width - (size * 0.05) - text.getLayoutBounds().getWidth()); break;
        }
        text.setY(height - size * 0.05);
    }

    @Override protected void resize() {
        super.resize();
        width  = tile.getWidth() - tile.getInsets().getLeft() - tile.getInsets().getRight();
        height = tile.getHeight() - tile.getInsets().getTop() - tile.getInsets().getBottom();
        size   = width < height ? width : height;

        double containerWidth  = contentBounds.getWidth();
        double containerHeight = contentBounds.getHeight();

        if (tile.isShowing() && width > 0 && height > 0) {
            pane.setMaxSize(width, height);
            pane.setPrefSize(width, height);

            resizeStaticText();
            resizeDynamicText();

            double worldMapHeight = clamp(0, containerWidth * MAP_ASPECT_RATIO, containerHeight);
            double worldMapWidth  = clamp(0, containerWidth, containerHeight / MAP_ASPECT_RATIO);

            worldPane.setCache(true);
            worldPane.setCacheHint(CacheHint.SCALE);

            worldPane.setScaleX(worldMapWidth / Helper.MAP_WIDTH * (TextSize.NORMAL == textSize ? 1.0 : 0.95));
            worldPane.setScaleY(worldMapHeight / Helper.MAP_HEIGHT * (TextSize.NORMAL == textSize ? 1.0 : 0.95));

            group.resize(worldMapWidth, worldMapHeight);
            group.relocate((width - worldMapWidth) * 0.5, contentBounds.getY() + (contentBounds.getHeight() - worldMapHeight) * 0.5);

            worldPane.setCache(false);

            poiLocations.forEach((location, circle) -> {
                double[] xy = Helper.latLonToXY(location.getLatitude(), location.getLongitude());
                double   x  = xy[0] * worldPane.getScaleX() + group.getBoundsInParent().getMinX();
                double   y  = xy[1] * worldPane.getScaleY() + group.getBoundsInParent().getMinY();
                circle.setCenterX(x);
                circle.setCenterY(y);
                circle.setRadius(size * 0.0075);
            });

            chartDataLocations.forEach((location, circle) -> {
                double[] xy = Helper.latLonToXY(location.getLatitude(), location.getLongitude());
                double   x  = xy[0] * worldPane.getScaleX() + group.getBoundsInParent().getMinX();
                double   y  = xy[1] * worldPane.getScaleY() + group.getBoundsInParent().getMinY();
                circle.setCenterX(x);
                circle.setCenterY(y);
                circle.setRadius(size * 0.0075);
            });
        }
    }

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(tile.getTitle());
        text.setText(tile.getText());

        resizeStaticText();

        titleText.setFill(tile.getTitleColor());
        text.setFill(tile.getTextColor());
    }

    private void refresh() {
        Color fill   = tile.getForegroundColor();
        Color stroke = tile.getBackgroundColor();
        countryPaths.forEach((name, pathList) -> {
            Country country = Country.valueOf(name);
            pathList.forEach(path -> {
                path.setFill(null == country.getColor() ? fill : country.getColor());
                path.setStroke(stroke);
                path.setStrokeWidth(0.2);
            });
        });
    }
}
