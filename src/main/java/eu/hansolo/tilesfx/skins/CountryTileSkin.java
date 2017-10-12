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

import eu.hansolo.tilesfx.Country;
import eu.hansolo.tilesfx.CountryPath;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.fonts.Fonts;
import eu.hansolo.tilesfx.tools.Helper;
import javafx.beans.value.ChangeListener;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

import java.util.List;


/**
 * Created by hansolo on 11.06.17.
 */
public class CountryTileSkin extends TileSkin {
    private Text              titleText;
    private Text              text;
    private Text              valueText;
    private Text              unitText;
    private TextFlow          valueUnitFlow;
    private Country           country;
    private StackPane         countryContainer;
    private Group             countryGroup;
    private ChangeListener    countryListener;
    private List<CountryPath> countryPaths;


    // ******************** Constructors **************************************
    public CountryTileSkin(final Tile TILE) {
        super(TILE);
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        super.initGraphics();

        country = null == tile.getCountry() ? Country.DE : tile.getCountry();
        countryListener = (o, ov, nv) -> {
            if (nv != null) {
                country = null == tile.getCountry() ? Country.DE : tile.getCountry();
                countryPaths.clear();
                countryPaths.addAll(Helper.getHiresCountryPaths().get(country.name()));
                countryGroup.getChildren().setAll(countryPaths);
                text.setText(tile.getCountry().getDisplayName());
                resize();
                redraw();
            }
        };
        countryPaths = Helper.getHiresCountryPaths().get(country.name());
        countryPaths.forEach(path -> path.setFill(tile.getBarColor()));

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

        getPane().getChildren().addAll(titleText, countryContainer, valueUnitFlow, text);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        tile.countryProperty().addListener(countryListener);
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);

        if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !tile.getTitle().isEmpty());
            Helper.enableNode(text, tile.isTextVisible());
            countryContainer.setMaxSize(size * 0.9, tile.isTextVisible() ? size * 0.68 : size * 0.795);
            countryContainer.setPrefSize(size * 0.9, tile.isTextVisible() ? size * 0.68 : size * 0.795);
        }
    }

    @Override protected void handleCurrentValue(final double VALUE) {
        valueText.setText(String.format(locale, formatString, VALUE));
        resizeDynamicText();
    }

    @Override public void dispose() {
        tile.countryProperty().removeListener(countryListener);
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

        titleText.setFont(Fonts.latoRegular(fontSize));
        if (titleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(titleText, maxWidth, fontSize); }
        switch(tile.getTitleAlignment()) {
            default    :
            case LEFT  : titleText.relocate(size * 0.05, size * 0.05); break;
            case CENTER: titleText.relocate((width - titleText.getLayoutBounds().getWidth()) * 0.5, size * 0.05); break;
            case RIGHT : titleText.relocate(width - (size * 0.05) - titleText.getLayoutBounds().getWidth(), size * 0.05); break;
        }

        text.setFont(Fonts.latoRegular(fontSize));
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
        width  = tile.getWidth() - tile.getInsets().getLeft() - tile.getInsets().getRight();
        height = tile.getHeight() - tile.getInsets().getTop() - tile.getInsets().getBottom();
        size   = width < height ? width : height;

        valueUnitFlow.setPrefWidth(width - size * 0.1);
        valueUnitFlow.relocate(size * 0.05, size * 0.15);

        double containerWidth  = width - size * 0.1;
        double containerHeight = tile.isTextVisible() ? height - size * 0.28 : height - size * 0.205;
        double containerSize   = containerWidth < containerHeight ? containerWidth : containerHeight;

        double countryWidth    = countryGroup.getLayoutBounds().getWidth();
        double countryHeight   = countryGroup.getLayoutBounds().getHeight();
        double countrySize     = countryWidth < countryHeight ? countryWidth : countryHeight;

        if (width > 0 && height > 0) {
            pane.setMaxSize(width, height);
            pane.setPrefSize(width, height);

            if (containerWidth > 0 && containerHeight > 0) {
                countryContainer.setMinSize(containerWidth, containerHeight);
                countryContainer.setMaxSize(containerWidth, containerHeight);
                countryContainer.setPrefSize(containerWidth, containerHeight);
                countryContainer.relocate(size * 0.05, size * 0.15);
                double scaleFactor = (containerSize * 0.6) / countrySize;
                countryGroup.setScaleX(scaleFactor);
                countryGroup.setScaleY(scaleFactor);
            }
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
