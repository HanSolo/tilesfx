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
import eu.hansolo.tilesfx.chart.ChartData;
import eu.hansolo.tilesfx.events.TileEvent;
import eu.hansolo.tilesfx.events.TileEvent.EventType;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.Helper;
import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

import java.util.List;


/**
 * Created by hansolo on 11.06.17.
 */
public class CountryTileSkin extends TileSkin {
    private Text                                       titleText;
    private Text                                       text;
    private Text                                       valueText;
    private Text                                       unitText;
    private TextFlow                                   valueUnitFlow;
    private Country                                    country;
    private StackPane                                  countryContainer;
    private Group                                      countryGroup;
    private EventHandler<MouseEvent>                   clickHandler;
    private List<CountryPath>                          countryPaths;
    private double                                     countryMinX;
    private double                                     countryMinY;
    private double                                     countryMaxX;
    private double                                     countryMaxY;
    //private ObservableMap<Location, Circle>            chartDataLocations;
    //private ObservableMap<Location, Circle>            poiLocations;
    //private ListChangeListener<Location>               poiListener;
    //private ListChangeListener<ChartData>              chartDataListener;
    //private Map<Circle, EventHandler<MouseEvent>>      circleHandlerMap;


    // ******************** Constructors **************************************
    public CountryTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        //poiLocations       = FXCollections.observableHashMap();
        //chartDataLocations = FXCollections.observableHashMap();

        //circleHandlerMap   = new HashMap<>();

        country = tile.getCountry();
        if (null == country) { country = Country.DE; }

        clickHandler = event -> tile.fireTileEvent(new TileEvent(EventType.SELECTED_CHART_DATA, new ChartData(country.getName(), country.getValue(), country.getColor())));

        countryPaths = Helper.getHiresCountryPaths().get(country.name());

        countryMinX = Helper.MAP_WIDTH;
        countryMinY = Helper.MAP_HEIGHT;
        countryMaxX = 0;
        countryMaxY = 0;
        countryPaths.forEach(path -> {
            path.setFill(tile.getBarColor());
            countryMinX = Math.min(countryMinX, path.getBoundsInParent().getMinX());
            countryMinY = Math.min(countryMinY, path.getBoundsInParent().getMinY());
            countryMaxX = Math.max(countryMaxX, path.getBoundsInParent().getMaxX());
            countryMaxY = Math.max(countryMaxY, path.getBoundsInParent().getMaxY());
        });

        /*
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
        */

        titleText = new Text();
        titleText.setFill(tile.getTitleColor());
        Helper.enableNode(titleText, !tile.getTitle().isEmpty());

        text = new Text(tile.getCountry().getDisplayName());
        text.setFill(tile.getTextColor());
        Helper.enableNode(text, tile.isTextVisible());

        countryGroup = new Group();
        countryGroup.getChildren().setAll(countryPaths);

        countryContainer = new StackPane();
        countryContainer.setMinSize(size * 0.9, tile.isTextVisible() ? size * 0.72 : size * 0.795);
        countryContainer.setMaxSize(size * 0.9, tile.isTextVisible() ? size * 0.72 : size * 0.795);
        countryContainer.setPrefSize(size * 0.9, tile.isTextVisible() ? size * 0.72 : size * 0.795);
        countryContainer.getChildren().setAll(countryGroup);

        valueText = new Text(String.format(locale, formatString, ((tile.getValue() - minValue) / range * 100)));
        valueText.setFill(tile.getValueColor());
        valueText.setTextOrigin(VPos.BASELINE);
        Helper.enableNode(valueText, tile.isValueVisible());

        unitText = new Text(" " + tile.getUnit());
        unitText.setFill(tile.getUnitColor());
        unitText.setTextOrigin(VPos.BASELINE);
        Helper.enableNode(unitText, !tile.getUnit().isEmpty());

        valueUnitFlow = new TextFlow(valueText, unitText);
        valueUnitFlow.setTextAlignment(TextAlignment.RIGHT);
        valueUnitFlow.setMouseTransparent(true);

        getPane().getChildren().addAll(titleText, countryContainer, valueUnitFlow, text);
        //getPane().getChildren().addAll(poiLocations.values());
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        tile.addEventHandler(MouseEvent.MOUSE_PRESSED, clickHandler);
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !tile.getTitle().isEmpty());
            Helper.enableNode(text, tile.isTextVisible());
            countryContainer.setMaxSize(size * 0.9, tile.isTextVisible() ? size * 0.68 : size * 0.795);
            countryContainer.setPrefSize(size * 0.9, tile.isTextVisible() ? size * 0.68 : size * 0.795);
        } else if ("RECALC".equals(EVENT_TYPE)) {
            country = tile.getCountry();
            if (null == country) { country = Country.DE; }
            countryPaths = Helper.getHiresCountryPaths().get(country.name());
            countryPaths.forEach(path -> path.setFill(tile.getBarColor()));
            countryGroup.getChildren().setAll(countryPaths);
            text.setText(country.getDisplayName());

            resize();
            redraw();
        }
    }

    @Override protected void handleCurrentValue(final double VALUE) {
        valueText.setText(String.format(locale, formatString, VALUE));
        resizeDynamicText();
    }

    @Override public void dispose() {
        tile.removeEventHandler(MouseEvent.MOUSE_PRESSED, clickHandler);
        super.dispose();
    }


    // ******************** Resizing ******************************************
    @Override protected void resizeDynamicText() {
        double maxWidth = unitText.isVisible() ? width - size * 0.275 : width - size * 0.1;
        double fontSize = size * 0.24;
        valueText.setFont(Fonts.latoRegular(fontSize));
        if (valueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(valueText, maxWidth, fontSize); }
    }
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

        valueUnitFlow.setPrefWidth(contentBounds.getWidth());
        valueUnitFlow.relocate(contentBounds.getX(), contentBounds.getY());

        double containerWidth  = contentBounds.getWidth();
        double containerHeight = contentBounds.getHeight();
        double containerSize   = containerWidth < containerHeight ? containerWidth : containerHeight;

        double countryWidth    = countryGroup.getLayoutBounds().getWidth();
        double countryHeight   = countryGroup.getLayoutBounds().getHeight();
        double countrySize     = countryWidth < countryHeight ? countryHeight : countryWidth; // max size

        if (tile.isShowing() && width > 0 && height > 0) {
            pane.setMaxSize(width, height);
            pane.setPrefSize(width, height);

            if (containerWidth > 0 && containerHeight > 0) {
                countryContainer.setMinSize(containerWidth, containerHeight);
                countryContainer.setMaxSize(containerWidth, containerHeight);
                countryContainer.setPrefSize(containerWidth, containerHeight);
                countryContainer.relocate(contentBounds.getX(), contentBounds.getY());
                double scaleFactor = containerSize / countrySize;
                countryGroup.setScaleX(scaleFactor);
                countryGroup.setScaleY(scaleFactor);
            }

            /*
            poiLocations.forEach((location, circle) -> {
                double[] xy = Helper.latLonToXY(location.getLatitude(), location.getLongitude());
                double   x  = (xy[0] - countryMinX) * countryGroup.getScaleX() + countryGroup.getBoundsInParent().getMinX();
                double   y  = (xy[1] - countryMinY) * countryGroup.getScaleY() + countryGroup.getBoundsInParent().getMinY();
                circle.setCenterX(x);
                circle.setCenterY(y);
                circle.setRadius(size * 0.01);
            });
            */

            resizeStaticText();
        }
    }

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(tile.getTitle());
        text.setText(tile.getCountry().getDisplayName());
        valueText.setText(String.format(locale, formatString, tile.getCurrentValue()));
        unitText.setText(tile.getUnit());

        resizeDynamicText();
        resizeStaticText();

        titleText.setFill(tile.getTitleColor());
        text.setFill(tile.getTextColor());
        valueText.setFill(tile.getValueColor());
        unitText.setFill(tile.getUnitColor());
        countryPaths.forEach(path -> path.setFill(Helper.getColorWithOpacity(tile.getBarColor(), 0.5)));
    }
}
