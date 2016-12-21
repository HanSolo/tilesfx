/*
 * Copyright (c) 2016 by Gerrit Grunwald
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

import eu.hansolo.tilesfx.Country;
import eu.hansolo.tilesfx.CountryPath;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.Helper;
import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;


/**
 * Created by hansolo on 19.12.16.
 */
public class WorldMapTileSkin extends TileSkin {
    protected static final double                         PREFERRED_WIDTH  = 380; //510;
    protected static final double                         PREFERRED_HEIGHT = 250;
    private                Text                           titleText;
    private                Text                           text;
    private                Pane                           worldPane;
    private                Group                          group;
    private                Map<String, List<CountryPath>> countryPaths;


    public WorldMapTileSkin(final Tile TILE) {
        super(TILE);
    }

    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();
        countryPaths = getSkinnable().getCountryPaths();

        titleText = new Text();
        titleText.setFill(getSkinnable().getTitleColor());
        Helper.enableNode(titleText, !getSkinnable().getTitle().isEmpty());

        text = new Text(getSkinnable().getUnit());
        text.setFill(getSkinnable().getUnitColor());
        Helper.enableNode(text, getSkinnable().isTextVisible());

        Color fill   = getSkinnable().getForegroundColor();
        Color stroke = getSkinnable().getBackgroundColor();

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
    }

    @Override protected void registerListeners() {
        super.registerListeners();

    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !getSkinnable().getTitle().isEmpty());
            Helper.enableNode(text, getSkinnable().isTextVisible());
        }
    };

    private void setFillAndStroke() {
        countryPaths.keySet().forEach(name -> {
            Country country = Country.valueOf(name);
            setCountryFillAndStroke(country, null == country.getColor() ? getSkinnable().getForegroundColor() : country.getColor(), getSkinnable().getBackgroundColor());
        });
    }
    private void setCountryFillAndStroke(final Country COUNTRY, final Color FILL, final Color STROKE) {
        List<CountryPath> paths = countryPaths.get(COUNTRY.getName());
        for (CountryPath path : paths) {
            path.setFill(FILL);
            path.setStroke(STROKE);
        }
    }

    private Properties readProperties(final String FILE_NAME) {
        final ClassLoader LOADER     = Thread.currentThread().getContextClassLoader();
        final Properties  PROPERTIES = new Properties();
        try(InputStream resourceStream = LOADER.getResourceAsStream(FILE_NAME)) {
            PROPERTIES.load(resourceStream);
        } catch (IOException exception) {
            System.out.println(exception);
        }
        return PROPERTIES;
    }


    // ******************** Resizing ******************************************
    @Override protected void resizeStaticText() {
        double maxWidth = size * 0.9;
        double fontSize = size * 0.06;

        titleText.setFont(Fonts.latoRegular(fontSize));
        if (titleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(titleText, maxWidth, fontSize); }
        titleText.relocate(size * 0.05, size * 0.05);

        maxWidth = size * 0.9;
        fontSize = size * 0.05;
        text.setText(getSkinnable().getText());
        if (text.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(text, maxWidth, fontSize); }
        text.setX(size * 0.05);
        text.setY(size * 0.95);
    };

    @Override protected void resize() {
        width  = getSkinnable().getWidth() - getSkinnable().getInsets().getLeft() - getSkinnable().getInsets().getRight();
        height = getSkinnable().getHeight() - getSkinnable().getInsets().getTop() - getSkinnable().getInsets().getBottom();
        size   = width < height ? width : height;

        final double ASPECT_RATIO = PREFERRED_HEIGHT / PREFERRED_WIDTH;

        if (ASPECT_RATIO * width > height) {
            width = 1 / (ASPECT_RATIO / height);
        } else if (1 / (ASPECT_RATIO / height) > width) {
            height = ASPECT_RATIO * width;
        }

        if (width > 0 && height > 0) {
            pane.setMaxSize(width, height);
            pane.setPrefSize(width, height);

            resizeStaticText();
            resizeDynamicText();

            double worldMapHeight = height * 0.8;
            double worldMapWidth  = worldMapHeight / 0.65906838;

            System.out.println(worldMapWidth + ", " + worldMapHeight);

            worldPane.setCache(true);
            worldPane.setCacheHint(CacheHint.SCALE);

            worldPane.setScaleX(worldMapWidth / 1009);
            worldPane.setScaleY(worldMapHeight / 665);

            group.resize(worldMapWidth, worldMapHeight);
            group.relocate((width - worldMapWidth) * 0.5, (height - worldMapHeight) * 0.5);

            worldPane.setCache(false);
        }
    };

    @Override protected void redraw() {
        super.redraw();
        titleText.setText(getSkinnable().getTitle());
        text.setText(getSkinnable().getText());

        resizeStaticText();

        titleText.setFill(getSkinnable().getTitleColor());
        text.setFill(getSkinnable().getTextColor());
    };
}
