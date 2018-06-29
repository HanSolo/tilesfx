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

import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.Tile.MapProvider;
import eu.hansolo.tilesfx.events.LocationEventListener;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.Helper;
import eu.hansolo.tilesfx.tools.Location;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;


/**
 * Created by hansolo on 12.02.17.
 */
public class MapTileSkin extends TileSkin {
    private static final DateTimeFormatter            DF = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter            TF = DateTimeFormatter.ISO_LOCAL_TIME;
    private              Text                         titleText;
    private              Text                         text;
    private              WebView                      webView;
    private              WebEngine                    webEngine;
    private              boolean                      readyToGo;
    private              EventHandler<MouseEvent>     mouseHandler;
    private              LocationEventListener        locationListener;
    private              ListChangeListener<Location> poiListener;


    // ******************** Constructors **************************************
    public MapTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        mouseHandler     = event -> { if (event.getClickCount() == 2) { centerLocation(); } };
        locationListener = e -> redraw();
        poiListener      = c -> {
            while (c.next()) {
                if (c.wasPermutated()) {      // Get items that have been permutated in list
                    for (int i = c.getFrom(); i < c.getTo(); ++i) {
                        updatePoi(tile.getPoiList().get(i));
                    }
                } else if (c.wasUpdated()) {  // Get items that have been updated in list
                    for (int i = c.getFrom(); i < c.getTo(); ++i) {
                        updatePoi(tile.getPoiList().get(i));
                    }
                } else if (c.wasAdded()) {
                    c.getAddedSubList().forEach(poi -> addPoi(poi));
                } else if (c.wasRemoved()) {
                    c.getRemoved().forEach(poi -> removePoi(poi));
                }
            }
        };

        titleText = new Text();
        titleText.setFill(tile.getTitleColor());
        Helper.enableNode(titleText, !tile.getTitle().isEmpty());

        text = new Text(tile.getText());
        text.setFill(tile.getTextColor());
        Helper.enableNode(text, tile.isTextVisible());

        webView = new WebView();
        webView.setMinSize(size * 0.9, tile.isTextVisible() ? size * 0.72 : size * 0.795);
        webView.setMaxSize(size * 0.9, tile.isTextVisible() ? size * 0.72 : size * 0.795);
        webView.setPrefSize(size * 0.9, tile.isTextVisible() ? size * 0.72 : size * 0.795);
        webEngine = webView.getEngine();
        webEngine.getLoadWorker().stateProperty().addListener((ov, o, n) -> {
            if (Worker.State.SUCCEEDED == n) {
                readyToGo = true;
                if (MapProvider.BW != tile.getMapProvider()) { changeMapProvider(tile.getMapProvider()); }
                updateLocation();
                updateLocationColor();
                tile.getPoiList().forEach(poi -> addPoi(poi));
                addTrack(tile.getTrack());
                updateTrackColor();
            }
        });
        URL maps = Tile.class.getResource("osm.html");
        webEngine.load(maps.toExternalForm());

        getPane().getChildren().addAll(titleText, webView, text);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        pane.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseHandler);
        tile.getPoiList().addListener(poiListener);
    }
    

    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !tile.getTitle().isEmpty());
            Helper.enableNode(text, tile.isTextVisible());
            webView.setMaxSize(size * 0.9, tile.isTextVisible() ? size * 0.68 : size * 0.795);
            webView.setPrefSize(size * 0.9, tile.isTextVisible() ? size * 0.68 : size * 0.795);
        } else if ("LOCATION".equals(EVENT_TYPE)) {
            tile.getCurrentLocation().addLocationEventListener(locationListener);
            updateLocation();
        } else if ("TRACK".equals(EVENT_TYPE)) {
            addTrack(tile.getTrack());
        } else if ("MAP_PROVIDER".equals(EVENT_TYPE)) {
            changeMapProvider(tile.getMapProvider());
        }
    }

    @Override public void dispose() {
        pane.removeEventHandler(MouseEvent.MOUSE_CLICKED, mouseHandler);
        tile.getCurrentLocation().removeLocationEventListener(locationListener);
        tile.getPoiList().removeListener(poiListener);
        super.dispose();
    }

    private void updateLocation() {
        if (readyToGo) {
            Platform.runLater(() -> {
                Location      location      = tile.getCurrentLocation();
                double        lat           = location.getLatitude();
                double        lon           = location.getLongitude();
                String        name          = location.getName();
                String        info          = location.getInfo();
                int           zoomLevel     = location.getZoomLevel();
                StringBuilder scriptCommand = new StringBuilder();
                scriptCommand.append("window.lat = ").append(lat).append(";")
                             .append("window.lon = ").append(lon).append(";")
                             .append("window.locationName = \"").append(name).append("\";")
                             .append("window.locationInfo = \"").append(info.toString()).append("\";")
                             .append("window.zoomLevel = ").append(zoomLevel).append(";")
                             .append("document.moveMarker(window.locationName, window.locationInfo, window.lat, window.lon, window.zoomLevel);");
                webEngine.executeScript(scriptCommand.toString());
            });
        }
    }

    private void updatePoi(final Location POI) {
        removePoi(POI);
        addPoi(POI);
    }
    private void addPoi(final Location POI) {
        if (readyToGo) {
            Platform.runLater(() -> {
                double lat   = POI.getLatitude();
                double lon   = POI.getLongitude();
                String name  = POI.getName();
                String info  = POI.getInfo();
                String color = POI.getColor().toString().replace("0x", "#");
                StringBuilder scriptCommand = new StringBuilder();
                scriptCommand.append("window.lat = ").append(lat).append(";")
                             .append("window.lon = ").append(lon).append(";")
                             .append("window.locationName = \"").append(name).append("\";")
                             .append("window.locationInfo = \"").append(info.toString()).append("\";")
                             .append("window.poiColor = \"").append(color).append("\";")
                             .append("document.addPoi(window.locationName, window.locationInfo, window.lat, window.lon, window.poiColor);");
                webEngine.executeScript(scriptCommand.toString());
            });
        }
    }
    private void removePoi(final Location POI) {
        if (readyToGo) {
            Platform.runLater(() -> {
                String        name          = POI.getName();
                StringBuilder scriptCommand = new StringBuilder();
                scriptCommand.append("window.locationName = \"").append(name).append("\";")
                             .append("document.removePoi(window.locationName);");
                webEngine.executeScript(scriptCommand.toString());
            });
        }
    }

    private void updateLocationColor() {
        if (readyToGo) {
            Platform.runLater(() -> {
                String locationColor = tile.getCurrentLocation().getColor().toString().replace("0x", "#");
                StringBuilder scriptCommand = new StringBuilder();
                scriptCommand.append("window.locationColor = '").append(locationColor).append("';")
                             .append("document.setLocationColor(window.locationColor);");
                webEngine.executeScript(scriptCommand.toString());
            });
        }
    }

    private void updateTrackColor() {
        if (readyToGo) {
            Platform.runLater(() -> {
                String trackColor = tile.getTrackColor().styleName;
                StringBuilder scriptCommand = new StringBuilder();
                scriptCommand.append("window.trackColor = '").append(trackColor).append("';")
                             .append("document.setTrackColor(window.trackColor);");
                webEngine.executeScript(scriptCommand.toString());
            });
        }
    }

    private void centerLocation() {
        if (readyToGo) {
            Platform.runLater(() -> {
                StringBuilder scriptCommand = new StringBuilder();
                scriptCommand.append("window.zoomLevel = ").append(tile.getCurrentLocation().getZoomLevel()).append(";");
                scriptCommand.append("document.zoomToLocation(window.zoomLevel);");
                webEngine.executeScript(scriptCommand.toString());
            });
        }
    }

    private void addTrack(final List<Location> LOCATIONS) {
        if (LOCATIONS.isEmpty()) {
            if (readyToGo) {
                Platform.runLater(() -> {
                    StringBuilder scriptCommand = new StringBuilder();
                    scriptCommand.append("document.clearTrack();");
                    webEngine.executeScript(scriptCommand.toString());
                });
                return;
            }
        }
        int length = LOCATIONS.size();
        if (length <= 4) return;
        if (readyToGo) {
            Platform.runLater(() -> {
                StringBuilder scriptCommand = new StringBuilder();
                double lat1;
                double lon1;
                double lat2;
                double lon2;
                String name;
                String date;
                String time;
                String trackColor = tile.getTrackColor().styleName;
                for (int i = 0 ; i < length - 1 ; i++) {
                    scriptCommand.setLength(0);

                    lat1 = LOCATIONS.get(i).getLatitude();
                    lon1 = LOCATIONS.get(i).getLongitude();
                    lat2 = LOCATIONS.get(i + 1).getLatitude();
                    lon2 = LOCATIONS.get(i + 1).getLongitude();
                    name = LOCATIONS.get(i).getName();
                    date = DF.format(LOCATIONS.get(i).getZonedDateTime());
                    time = TF.format(LOCATIONS.get(i).getZonedDateTime());

                    scriptCommand.append("window.lat1 = ").append(lat1).append(";")
                                     .append("window.lon1 = ").append(lon1).append(";")
                                     .append("window.lat2 = ").append(lat2).append(";")
                                     .append("window.lon2 = ").append(lon2).append(";")
                                     .append("window.locationName = \"").append(name).append("\";")
                                     .append("window.locationDate = \"").append(date).append("\";")
                                     .append("window.locationTime = \"").append(time).append("\";")
                                     .append("window.trackColor = \"").append(trackColor).append("\";")
                                     .append("document.addToTrack(window.lat1, window.lon1, window.lat2, window.lon2,window.locationName, window.locationDate, window.locationTime, window.trackColor);");
                    webEngine.executeScript(scriptCommand.toString());
                }

                // Start Marker
                scriptCommand.setLength(0);
                lat1 = LOCATIONS.get(0).getLatitude();
                lon1 = LOCATIONS.get(0).getLongitude();
                name = LOCATIONS.get(0).getName();
                date = DF.format(LOCATIONS.get(0).getZonedDateTime());
                time = TF.format(LOCATIONS.get(0).getZonedDateTime());
                scriptCommand.append("window.lat1 = ").append(lat1).append(";")
                             .append("window.lon1 = ").append(lon1).append(";")
                             .append("window.locationName = \"").append(name).append("\";")
                             .append("window.locationDate = \"").append(date).append("\";")
                             .append("window.locationTime = \"").append(time).append("\";")
                             .append("document.addStartPoiMarker(window.lat1, window.lon1, window.locationName, window.locationDate, window.locationTime);");
                webEngine.executeScript(scriptCommand.toString());

                // Stop Marker
                scriptCommand.setLength(0);
                lat1 = LOCATIONS.get(length - 1).getLatitude();
                lon1 = LOCATIONS.get(length - 1).getLongitude();
                name = LOCATIONS.get(length - 1).getName();
                date = DF.format(LOCATIONS.get(length - 1).getZonedDateTime());
                time = TF.format(LOCATIONS.get(length - 1).getZonedDateTime());
                scriptCommand.append("window.lat1 = ").append(lat1).append(";")
                             .append("window.lon1 = ").append(lon1).append(";")
                             .append("window.locationName = \"").append(name).append("\";")
                             .append("window.locationDate = \"").append(date).append("\";")
                             .append("window.locationTime = \"").append(time).append("\";")
                             .append("document.addStopPoiMarker(window.lat1, window.lon1, window.locationName, window.locationDate, window.locationTime);");
                webEngine.executeScript(scriptCommand.toString());
            });
        }
    }

    private void changeMapProvider(final MapProvider PROVIDER) {
        if (readyToGo) {
            Platform.runLater(() -> {
                StringBuilder scriptCommand = new StringBuilder();
                scriptCommand.append("window.provider = '").append(PROVIDER.name).append("';")
                             .append("document.changeMapProvider(window.provider);");
                webEngine.executeScript(scriptCommand.toString());
            });
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

            if (containerWidth > 0 && containerHeight > 0) {
                webView.setMinSize(containerWidth, containerHeight);
                webView.setMaxSize(containerWidth, containerHeight);
                webView.setPrefSize(containerWidth, containerHeight);
                webView.relocate(contentBounds.getX(), contentBounds.getY());
            }
            resizeStaticText();
        }
    }

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(tile.getTitle());
        text.setText(tile.getText());

        resizeStaticText();

        titleText.setFill(tile.getTitleColor());
        text.setFill(tile.getTextColor());

        updateLocationColor();
        updateTrackColor();
    }
}
