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
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.Helper;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import java.net.URL;


/**
 * Created by hansolo on 12.02.17.
 */
public class MapTileSkin extends TileSkin {
    private Text                     titleText;
    private Text                     text;
    private WebView                  webView;
    private WebEngine                webEngine;
    private boolean                  readyToGo;
    private EventHandler<MouseEvent> mouseHandler;


    // ******************** Constructors **************************************
    public MapTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        mouseHandler = event -> { if (event.getClickCount() == 2) { centerLocation(); } };

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
                JSObject jsObject = (JSObject) webEngine.executeScript("window");
                jsObject.setMember("java", tile.getCurrentLocation());
                readyToGo = true;
                updateLocation();
                updateLocationColor();
            }
        });
        URL maps = Tile.class.getResource("osm.html");
        webEngine.load(maps.toExternalForm());

        getPane().getChildren().addAll(titleText, webView, text);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        pane.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseHandler);
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
            updateLocation();
        }
    };

    @Override public void dispose() {
        pane.removeEventHandler(MouseEvent.MOUSE_CLICKED, mouseHandler);
        super.dispose();
    }

    private void updateLocation() {
        if (readyToGo) {
            Platform.runLater(() -> {
                double        lat           = tile.getCurrentLocation().getLatitude();
                double        lon           = tile.getCurrentLocation().getLongitude();
                String        name          = tile.getCurrentLocation().getName();
                String        info          = tile.getCurrentLocation().getInfo();
                StringBuilder scriptCommand = new StringBuilder();
                scriptCommand.append("window.lat = ").append(lat).append(";")
                             .append("window.lon = ").append(lon).append(";")
                             .append("window.locationName = \"").append(name).append("\";")
                             .append("window.locationInfo = \"").append(info.toString()).append("\";")
                             .append("document.moveMarker(window.locationName, window.locationInfo, window.lat, window.lon);");
                webEngine.executeScript(scriptCommand.toString());
            });
        }
    }

    private void updateLocationColor() {
        if (readyToGo) {
            Platform.runLater(() -> {
                String locationColor = tile.getLocationColor().styleName;
                StringBuilder scriptCommand = new StringBuilder();
                scriptCommand.append("window.locationColor = '").append(locationColor).append("';")
                             .append("document.setLocationColor(window.locationColor);");
                webEngine.executeScript(scriptCommand.toString());
            });
        }
    }

    private void centerLocation() {
        if (readyToGo) {
            Platform.runLater(() -> {
                StringBuilder scriptCommand = new StringBuilder();
                scriptCommand.append("document.zoomToLocation();");
                webEngine.executeScript(scriptCommand.toString());
            });
        }
    }


    // ******************** Resizing ******************************************
    @Override protected void resizeStaticText() {
        double maxWidth = width - size * 0.1;
        double fontSize = size * textSize.factor;

        titleText.setFont(Fonts.latoRegular(fontSize));
        if (titleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(titleText, maxWidth, fontSize); }
        titleText.relocate(size * 0.05, size * 0.05);

        text.setFont(Fonts.latoRegular(fontSize));
        if (text.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(text, maxWidth, fontSize); }
        text.setX(size * 0.05);
        text.setY(height - size * 0.05);
    };

    @Override protected void resize() {
        width  = tile.getWidth() - tile.getInsets().getLeft() - tile.getInsets().getRight();
        height = tile.getHeight() - tile.getInsets().getTop() - tile.getInsets().getBottom();
        size   = width < height ? width : height;

        double containerWidth  = width - size * 0.1;
        double containerHeight = tile.isTextVisible() ? height - size * 0.28 : height - size * 0.205;

        if (width > 0 && height > 0) {
            pane.setMaxSize(width, height);
            pane.setPrefSize(width, height);

            if (containerWidth > 0 && containerHeight > 0) {
                webView.setMinSize(containerWidth, containerHeight);
                webView.setMaxSize(containerWidth, containerHeight);
                webView.setPrefSize(containerWidth, containerHeight);
                webView.relocate(size * 0.05, size * 0.15);
            }
            resizeStaticText();
        }
    };

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(tile.getTitle());
        text.setText(tile.getText());

        resizeStaticText();

        titleText.setFill(tile.getTitleColor());
        text.setFill(tile.getTextColor());

        updateLocationColor();
    };
}
